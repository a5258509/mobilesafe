package com.asao.mobilesafe.service;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.asao.mobilesafe.AppUnLockActivity;
import com.asao.mobilesafe.MainApplication;
import com.asao.mobilesafe.db.dao.AppLockDao;

import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppLockService1 extends Service {

    private boolean isRunning;
    private ActivityManager am;
    private AppLockDao appLockDao;
    private MyReceiver myReceiver;
    private String unlockpackageName;
    private List<String> list;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class MyReceiver extends BroadcastReceiver{

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

        isRunning=true;

        //注册接受解锁和锁屏的广播接受者
        myReceiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.asao.mobilesafe.UNLOCK");
        //锁屏重新加锁
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(myReceiver,filter);


        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //时刻监听用户打开的应用程序,如果是加锁的应用,弹出解锁界面
        isLock();
    }

    //监听用户打开的应用程序
    private void isLock() {


        new Thread(){
            @Override
            public void run() {


                //因为每隔200毫秒，打开关闭查询一次数据，比较浪费资源，因为最终的目标就是查询包名在不在数据库中，所以可以先把数据库中的数据一次性查询出来，放到内存中（list）,
                //然后去内存中查询包名是否在数据中
                //1.获取数据库的所有数据，放到内存中
                list = appLockDao.queryAll();
                //但是，当有新的加锁应用的时候，发现数据库更新了，但是内存中的数据没有更新
                //解决：使用内容观察者，设置当数据库改变的时候，更新内存中的数据了
                //注册内容观察者观察更新通知消息、
                Uri uri = Uri.parse("content://com.asao.mobilesafe.UPDATESQLITE");
                //notifyForDescendents : 匹配模式，true:精确匹配，false:模糊匹配
                getContentResolver().registerContentObserver(uri, true, new ContentObserver(null) {
                        //当数据库中的数据更新的时候调用的方法
                        public  void onChange(boolean selfChange){
                            list.clear();
                            list=appLockDao.queryAll();
                        }
                });


                //死循环，容易阻塞主线程
                while(isRunning){
                    //监听用户打开的应用程序了
                    //获取正在运行的任务栈
                    //maxNum : 获取正在运行的任务栈的上限个数
//                    List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
//                    for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTasks) {
//                        ComponentName baseactivity = runningTaskInfo.baseActivity;//获取任务栈栈底的activity
//                        //runningTaskInfo.topActivity;//获取栈顶的activity
//                        String packageName = baseactivity.getPackageName();//根据应用程序的任务栈的activity获取应用程序的包名
//                        System.out.println(packageName);
//
//                    }
                    //判断是否有使用情况访问权限,该权限在系统安全的特殊应用权限中
                    boolean useGranted = isUseGranted();
                    Log.e("TopAppService", "use 权限 是否允许授权=" + useGranted);
                    if (useGranted) {
                        String topApp = getHigherPackageName();//获取顶层的activity的包名,即当前用户打开的是哪个activity
                        // Log.e("TopAppService", "顶层app=" + topApp);
                        //得到顶层的activity的包名后,就可以判断打开的应用程序是否是加锁的应用程序了
                        //判断包名是否在集合中(集合中的数据就是数据库中全部数据获取到内存中的操作)
                        //contains:判断集合是否包含某个数据
                        if(list.contains(topApp)){
                            //判断加锁的应用程序的包名和解锁的应用程序的包名是否一致，一致，表示加锁的应用已经解锁，不需要弹出解锁界面
                            if (!topApp.equals(unlockpackageName)) {
                                //弹出解锁界面
                                Intent intent =new Intent(AppLockService1.this, AppUnLockActivity.class);
                                //FLAG_ACTIVITY_NEW_TASK: 如果跳转的activity没有保存的任务栈，创建一个新的，如果已经有保存的任务栈，直接保存到已有的任务栈中
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//给跳转的activity指定保存的任务栈
                                intent.putExtra("packageName",topApp);
                                startActivity(intent);
                            }
                        }
                    } else {
                        //开启应用授权界面
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    SystemClock.sleep(200);
                }
            }
        }.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning=false;
        //注销广播接收者
        unregisterReceiver(myReceiver);
    }


    /**
     * 判断  用户查看使用情况的权利是否给予app
     *
     * @return
     */
    private boolean isUseGranted() {
        Context appContext = getApplicationContext();
        //AppOpsManager是Android 4.3开始谷歌将权限管理功能集成系统里而提供的外部类，所以对于低于4.3版本将不提供该类的实现。
        AppOpsManager appOps = (AppOpsManager) appContext
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = -1;
        mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), appContext.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }


    /**
     * 获取顶层的activity的包名
     *
     * @return
     */
    private String getHigherPackageName() {
        String topPackageName = "";
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        //time - 1000 * 1000, time 开始时间和结束时间的设置，在这个时间范围内 获取栈顶Activity 有效
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        // Sort the stats by the last time used
        if (stats != null) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                //Log.e("TopPackage Name", topPackageName);
            }
        }
        return topPackageName;
    }







}