package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.asao.mobilesafe.service.BlackNumberService;
import com.asao.mobilesafe.service.GPSService;
import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.ServiceUtil;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;
import com.asao.mobilesafe.view.SettingView;

public class SettingActivity extends AppCompatActivity {

    private SettingView mUpdate;
    private SettingView mBlackNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        
        initView();
    }

    //初始化控件
    private void initView() {
        mUpdate = findViewById(R.id.setting_sv_update);
        mBlackNumber = findViewById(R.id.setting_sv_blacknumber);
        //设置自动更新条目的点击事件
        update();
        //设置骚扰拦截的条目的点击事件
        blackNumber();
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

    //开启/关闭骚扰拦截服务的操作
    private void blackNumber() {

        //1.开启/关闭服务
        mBlackNumber.setOnClickListener(v -> {
            //需要知道服务是否开启
            //不能SharedPreferences的原因：因为在系统的设置操作可以手动的停止服务，手动停止服务因为是在系统的设置界面中的，所以不好更改SharedPreferences保存的值
            // 动态的获取服务是否开启
            Intent intent=new Intent(getApplicationContext(), BlackNumberService.class);
            if(ServiceUtil.isServiceRunning(SettingActivity.this,"com.asao.mobilesafe.service.BlackNumberService")){
                //开启->点击关闭服务
                Log.d("main1", "关闭服务");
                stopService(intent);
            }else {
                //关闭->点击开启服务
                Log.d("main1", "开启服务");
                startService(intent);
            }

            //开关
            mBlackNumber.toggle();

        });

    }


//    当界面最小化的时候，从系统的设置界面中关闭服务，不能回显
//    原因：回显操作写在blacknumber方法中的，而blacknumber方法是在oncreate方法中调用的，最小化在打开界面是不会调用oncreate方法，所以不会调用回显操作
//    解决：将回显操作写到onstart方法中

    @Override
    protected void onStart() {
        super.onStart();
        //2.再次进入的时候，判断服务是否开启，设置开关状态
        boolean serviceIsRunning = ServiceUtil.isServiceRunning(SettingActivity.this, "com.asao.mobilesafe.service.BlackNumberService");
        mBlackNumber.setToggleOn(serviceIsRunning);
    }





}