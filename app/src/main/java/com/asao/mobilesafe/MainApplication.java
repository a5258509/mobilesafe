package com.asao.mobilesafe;

import android.app.Application;

import org.xutils.x;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//Xutils初始化
        x.Ext.setDebug(false);//输出debug日志，开启会影响性能
    }
}
