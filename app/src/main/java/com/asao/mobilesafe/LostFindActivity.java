package com.asao.mobilesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/***
 * 手机防盗页面
 */
public class LostFindActivity extends AppCompatActivity {
    private TextView mAginEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_find);

        initView();
    }
    /**
     * 初始化控件
     *
     * 2016-10-10 上午11:48:44
     */
    private void initView() {
        mAginEnter = findViewById(R.id.lostfind_tv_aginenter);

        //设置重新进入设置向导的点击事件
        mAginEnter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LostFindActivity.this,SetUp1Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
