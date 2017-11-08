package com.inred.looperrecviewdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.inred.looperrecview.NoScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inred on 2017/11/8.
 */

public class MainActivity extends AppCompatActivity {

    private NoScrollRecyclerView noscroll_rec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        noscroll_rec = findViewById(R.id.noscroll_rec);
        LooperAdapter adapter = new LooperAdapter(getList());
        noscroll_rec.setAdapter(adapter);

    }


    @Override
    public void onResume() {
        super.onResume();
        noscroll_rec.onResume();
    }

    public List<String> getList() {
        List<String> strings = new ArrayList<>();
        strings.add("1000");
        strings.add("2000");
        strings.add("3000");
        strings.add("4000");
        strings.add("5000");
        strings.add("6000");
        strings.add("7000");
        return strings;
    }


}
