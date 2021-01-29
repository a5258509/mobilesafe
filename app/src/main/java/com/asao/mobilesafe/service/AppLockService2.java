package com.asao.mobilesafe.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.asao.mobilesafe.AppUnLockActivity;
import com.asao.mobilesafe.db.dao.AppLockDao;

import java.util.List;

public class AppLockService2 extends AccessibilityService {
    private AppLockDao appLockDao;
    private AppLockService2.MyReceiver myReceiver;
    private String unlockpackageName;
    private List<String> list;

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //判断接收的是哪种广播
            String action = intent.getAction();//获取接收的广播事件
            //判断是解锁还是锁屏的广播接收者
            if("com.asao.mobilesafe.UNLOCK".equals(action)){
                //接受传递过来的解锁的应用程序的包名
                unlockpackageName = intent.getStringExtra("packageName");
            }else if((Intent.ACTION_SCREEN_OFF).equals(action)){
                //重新加锁
                unlockpackageName=null;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appLockDao = new AppLockDao(this);
        list = appLockDao.queryAll();


        //注册接受解锁和锁屏的广播接受者
        myReceiver = new AppLockService2.MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.asao.mobilesafe.UNLOCK");
        //锁屏重新加锁
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(myReceiver,filter);

        //内容观察者
        Uri uri = Uri.parse("content://com.asao.mobilesafe.UPDATESQLITE");
        //notifyForDescendents : 匹配模式，true:精确匹配，false:模糊匹配
        getContentResolver().registerContentObserver(uri, true, new ContentObserver(null) {
            //当数据库中的数据更新的时候调用的方法
            public  void onChange(boolean selfChange){
                list.clear();
                list=appLockDao.queryAll();
            }
        });

    }

    //监听用户的行为
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //获取用户打开的应用程序的包名,判断包名是否是加锁应用程序,实现加锁和解锁操作
        String packagename = event.getPackageName().toString();
        //Log.d("main1",packagename);

        if(list.contains(packagename)){
            //判断加锁的应用程序的包名和解锁的应用程序的包名是否一致，一致，表示加锁的应用已经解锁，不需要弹出解锁界面
            if (!packagename.equals(unlockpackageName)) {
                //弹出解锁界面
                Intent intent =new Intent(AppLockService2.this, AppUnLockActivity.class);
                //FLAG_ACTIVITY_NEW_TASK: 如果跳转的activity没有保存的任务栈，创建一个新的，如果已经有保存的任务栈，直接保存到已有的任务栈中
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//给跳转的activity指定保存的任务栈
                intent.putExtra("packageName",packagename);
                startActivity(intent);
            }
        }

    }

    //接收反馈信息的操作
    @Override
    public void onInterrupt() {

    }
}
