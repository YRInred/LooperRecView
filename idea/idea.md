# 写法思路
这个控件的写法有许多,但是大多是靠动画实现,于是本人想可不可以用控件本身的功能进行操作
## 命题分析

1.使用场景:<br />
&emsp;放置首页循环播放给用户一种很多人使用该app的心理暗示<br />
2.控件特点<br />
&emsp;固定高度<br />
&emsp;一屏展示固定个数的item<br />
&emsp;循环播放<br />
## 控件图文简介
 根据控件特点我们可以想到 利用recyclerview自带的滑动方法 只需要让其匀速滑动 到达底部之后再次到顶部重新滑动作为循环即可<br />
 我们来看该动画的特点<br />
 假设7个item 可视试图5个 一开始如下图显示<br />
 ![img](https://github.com/YRInred/LooperRecView/blob/master/idea/start.png)<br />
 动画开始走过一半<br />
 ![img](https://github.com/YRInred/LooperRecView/blob/master/idea/middle.png)<br />
 第一个循环走完最后停止<br />
 --->![img](https://github.com/YRInred/LooperRecView/blob/master/idea/end.png)<br />
这里我们可以想到假设整个数据m个  可视试图n个<br />
那么我们把这前n个试图加到整个数据m个后面m+n  然后调用recyclerview的smoothtoposition到最后一个不就完成这个动画了吗<br />
想到这里我们来实现一下
### 第一阶段
adapter里面修改就比较简单 这里我们引用了cymd大神的BaseQuickAdapter 再其基础上修改一下构造函数
```java
public abstract class LooprecAdapter<T,K extends BaseViewHolder> extends BaseQuickAdapter<T,K>{

 
    private static final int defaultshowitemnum = 5;  //默认显示5个
    private boolean isloop = true;  //是否循环

    public LooprecAdapter(int layoutResId, @Nullable List data) {
        this(layoutResId, data,defaultshowitemnum);
    }

    public LooprecAdapter(int layoutResId, @Nullable List data, int showitemnum){
        super(layoutResId,data);
        if (this.mData.size() < showitemnum) {
            isloop = false;
        } else {
            for (int i = 0; i < showitemnum; i++) {
                getData().add(this.mData.get(i));
            }
        }

    }

    public boolean getIsloop(){
        return isloop;
    }

}
```
使用recyclerview的 layoutmanager.smoothScrollToPosition方法 发现速度和时间上没法控制 于是查看一下源码
```java
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
            int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
```
再点进去看 LinearSmoothScroller 发现有几个配置项
```java
    /**
     * Calculates the scroll speed.
     *
     * @param displayMetrics DisplayMetrics to be used for real dimension calculations
     * @return The time (in ms) it should take for each pixel. For instance, if returned value is
     * 2 ms, it means scrolling 1000 pixels with LinearInterpolation should take 2 seconds.
     */
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }
    /**
     * <p>Calculates the time for deceleration so that transition from LinearInterpolator to
     * DecelerateInterpolator looks smooth.</p>
     *
     * @param dx Distance to scroll
     * @return Time for DecelerateInterpolator to smoothly traverse the distance when transitioning
     * from LinearInterpolation
     */
    protected int calculateTimeForDeceleration(int dx) {
        // we want to cover same area with the linear interpolator for the first 10% of the
        // interpolation. After that, deceleration will take control.
        // area under curve (1-(1-x)^2) can be calculated as (1 - x/3) * x * x
        // which gives 0.100028 when x = .3356
        // this is why we divide linear scrolling time with .3356
        return  (int) Math.ceil(calculateTimeForScrolling(dx) / .3356);
    }

    /**
     * Calculates the time it should take to scroll the given distance (in pixels)
     *
     * @param dx Distance in pixels that we want to scroll
     * @return Time in milliseconds
     * @see #calculateSpeedPerPixel(android.util.DisplayMetrics)
     */
    protected int calculateTimeForScrolling(int dx) {
        // In a case where dx is very small, rounding may return 0 although dx > 0.
        // To avoid that issue, ceil the result so that if dx > 0, we'll always return positive
        // time.
        return (int) Math.ceil(Math.abs(dx) * MILLISECONDS_PER_PX);
    }
```
根据文档和改值实验 <br />
calculateSpeedPerPixel 返回的就是速度值  值越小 速度越快<br />
calculateTimeForDeceleration和calculateTimeForScrolling控制滑动阶段根据不同的状态和两个方法返回的时间控制当前状态滑动的速度<br />
于是我们把calculateTimeForScrolling返回值改为0这样就能匀速滑动了<br />
```java
    manager = new LinearLayoutManager(context){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
                LinearSmoothScroller linearSmoothScroller =
                        new LinearSmoothScroller(recyclerView.getContext()){
                            @Override
                            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                                return speed;
                            }
                            @Override
                            protected int calculateTimeForDeceleration(int dx) {
                                return 0;
                            }
                        };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
            
            setLayoutManager(manager);
            ...
            manager.smoothScrollToPosition(RecyclerView.this, null, getLooperAdapter().getData().size() - 1);
                           
```
### 第二阶段
现在第一个循环动画完成 只需要重复就可以了 下一步的思路很清晰 只需要监听滑动到底部   
```java
manager.scrollToPosition(0); 
//回到第一个item再次调用
 manager.smoothScrollToPosition(RecyclerView.this, null, getLooperAdapter().getData().size() - 1);
 ```
 这里我们随便网上找一个监听recyclerview的监听方法
 ```java
  addOnScrollListener(new OnRcvScrollListener() {
            @Override
            public void onBottom() {
                super.onBottom();
                if (getLooperAdapter().getIsloop()) {
                    manager.scrollToPosition(0);
                    compositeDisposable.add(Observable.timer(speed/2, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    manager.smoothScrollToPosition(NoScrollRecyclerView.this, null, getLooperAdapter().getData().size() - 1);
                                }
                            }));
                }
            }
        });
 ```
这里的延迟操作只是我有强迫症感觉给一点延迟会好一点 实际上可能不需要
### 实际问题
为了程序体验好一点 我们是想在看到的时候滑动 看不到的时候关闭 这里我们简单监听
```java
   @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (!isVisible)
            compositeDisposable.clear();
    }
```
由于黑屏再次进入状态view是获取不到监听的 于是我们把这个问题交给activity的onresume来做 正好也可以让这个控件的启动一并解决了
```java
    public void onResume(){
        compositeDisposable.add(Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (getLooperAdapter().getIsloop()) {
                            manager.smoothScrollToPosition(NoScrollRecyclerView.this, null, getLooperAdapter().getData().size() - 1);
                        }
                    }
                }));
    }
    
    ...
```
activity中调用
```java
    @Override
    public void onResume() {
        super.onResume();
        noscroll_rec.onResume();
    }
```
因为这个控件是不需要手动控制滑动的 所以我们的自定义recyclerview要禁止手动滑动事件
```java
   @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        /* return false;//super.onTouchEvent(arg0); */
        if (noScroll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (noScroll)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }
```
这里再记录一下recyclerview和scrollview 冲突的问题<br />
直接使用NestedScrollView包裹recyclerview<br />
在代码上写 recyclerview.setNestedScrollingEnabled(false);
就解决了
