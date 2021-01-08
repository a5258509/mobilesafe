package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SetUp4Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up4);
    }

    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp3Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        Intent intent = new Intent(this,SetUp5Activity.class);
        startActivity(intent);
        return false;
    }
}