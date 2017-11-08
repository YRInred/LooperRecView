package com.inred.looperrecview;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by inred on 2017/11/8.
 */

public abstract class LooprecAdapter<T,K extends BaseViewHolder> extends BaseQuickAdapter<T,K>{


    private static final int defaultshowitemnum = 5;
    private boolean isloop = true;

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
