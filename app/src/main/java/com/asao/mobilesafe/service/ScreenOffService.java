package com.asao.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.asao.mobilesafe.engine.ProcessEngine;

public class ScreenOffService extends Service {

    private ScreenOffReceiver screenOffReceiver;

    public ScreenOffService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private class ScreenOffReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //清理所有进程的操作
            ProcessEngine.killALLProcess(context);
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();
        //锁屏清理进程的操作
        //设置监听锁屏的广播接受者
        screenOffReceiver = new ScreenOffReceiver();
        //设置接受的广播事件
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOffReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenOffReceiver);
    }
}