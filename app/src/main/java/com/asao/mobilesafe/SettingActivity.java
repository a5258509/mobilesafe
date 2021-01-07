package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;
import com.asao.mobilesafe.view.SettingView;

public class SettingActivity extends AppCompatActivity {

    private SettingView mUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        
        initView();
    }

    //初始化控件
    private void initView() {
        mUpdate = findViewById(R.id.setting_sv_update);

        //设置自动更新条目的点击事件
        update();
    }

    private void update() {
        //进入界面时,获取保存的开关状态,
        Boolean isupdate = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.ISUPDATE, true);
        mUpdate.setToggleOn(isupdate);

        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mUpdate.toggle();
               //开启关闭成功,保存开关状态
                SharedPreferencesUtil.savaBoolean(getApplicationContext(),Constants.ISUPDATE,mUpdate.istoggle());
            }
        });
    }
}