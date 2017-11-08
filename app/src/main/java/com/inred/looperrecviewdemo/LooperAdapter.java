package com.inred.looperrecviewdemo;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseViewHolder;
import com.inred.looperrecview.LooprecAdapter;

import java.util.List;

/**
 * Created by inred on 2017/11/6.
 */

public class LooperAdapter extends LooprecAdapter<String, BaseViewHolder> {


    public LooperAdapter(@Nullable List<String> data) {
        super(R.layout.layout_rec_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.money_txt,item);
    }





}
