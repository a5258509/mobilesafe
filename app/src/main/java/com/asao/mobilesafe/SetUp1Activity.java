package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 第一个设置向导页
 * */
public class SetUp1Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up1);

    }

    @Override
    public boolean pre_activity() {
        Toast.makeText(getApplicationContext(), "已经是第一张了", Toast.LENGTH_SHORT).show();
        //第一个界面的上一步是不能执行跳转操作
        return true;
    }

    @Override
    public boolean next_activity() {
        Intent intent = new Intent(this,SetUp2Activity.class);
        startActivity(intent);
        return false;
    }


}