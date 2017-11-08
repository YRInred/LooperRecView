package com.inred.looperrecview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by inred on 2017/11/6.
 */

public class NoScrollRecyclerView extends RecyclerView {

    private boolean noScroll = true;

    private LinearLayoutManager manager;

    private CompositeDisposable compositeDisposable;

    private Context mContext;

    private int speed;

    public void setSpeed(int speed) {
        this.speed = speed;
        notifyLayoutManager(mContext);
    }

    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }


    public NoScrollRecyclerView(Context context) {
        this(context, null);
    }

    public NoScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.noscrollRec);
        speed= ta.getInteger(R.styleable.noscrollRec_speed,10);
        this.mContext = context;
        initRec(context);
    }

    private void initRec(Context context) {
        compositeDisposable = new CompositeDisposable();
       notifyLayoutManager(context);
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
    }

    private void notifyLayoutManager(Context context){
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
        };
        setLayoutManager(manager);
    }

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


    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (!isVisible)
            compositeDisposable.clear();
    }


    public LooprecAdapter getLooperAdapter(){
        if (getAdapter() instanceof LooprecAdapter)
            return (LooprecAdapter) getAdapter();
        else
            return null;
    }


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


}
