package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

public class BlackNumberActivity extends AppCompatActivity {

    private ImageView mEmpty;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number);

        initView();
    }

    private void initView() {
        mEmpty = findViewById(R.id.blacknumber_iv_empty);
        mListView = findViewById(R.id.blacknumber_lv_listview);

        //如果listview没有数据,就显示image,如果listview有数据,就隐藏imageview
        mListView.setEmptyView(mEmpty);
    }
}