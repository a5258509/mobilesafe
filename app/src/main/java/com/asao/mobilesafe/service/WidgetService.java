package com.asao.mobilesafe.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class WidgetService extends Service {

    private AppWidgetManager appWidgetManager;
    private MyReceiver myReceiver;
    private AlarmManager alarmManager;
    private PendingIntent operation;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("widget更新了..");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //更新桌面小控件内容
        appWidgetManager = AppWidgetManager.getInstance(this);
        //每隔一段时间更新一次，定时器，handler,timer，while(true){sleep(3000)},alarmManager(闹铃的管理者)
		/*Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		}, 3000);*///3000 : 间隔时间

        //2.因为定时操作是每隔3秒钟发送一个自定义广播，所以需要创建广播接受者接受广播
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.asao.mobilesafe.WIDGET_UPDATE");
        registerReceiver(myReceiver, filter);

        //1.获取闹铃的管理者
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //2.发送闹铃的间隔消息
        Intent intent = new Intent();
        intent.setAction("com.asao.mobilesafe.WIDGET_UPDATE");
        //get....对应的intent中的startActivity,startService,sendbrodcast
        //getBroadcast : 发送广播，因为是我们自己发送，发送的肯定是自定义的广播事件
        //requestCode : 请求码
        //intent : pendingIntent中封装的intent,决定PendingIntent的具体操作
        //FLAG_CANCEL_CURRENT : 更新的时候，放弃老数据，直接创建新数据更新
        //FLAG_UPDATE_CURRENT : 更新的时候，直接更新老数据
        operation = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //type : 闹铃类型
        //RTC ：使用硬件的时间，发送闹铃不通过手机提醒
        // RTC_WAKEUP : 使用硬件的时间，发送闹铃通过手机提醒
        //ELAPSED_REALTIME : 使用真实的时间，发送闹铃不通过手机提醒
        //ELAPSED_REALTIME_WAKEUP : 使用真实的时间，发送闹铃通过手机提醒
        //triggerAtMillis : 当前的时间
        //intervalMillis : 间隔时间
        //operation : 执行的操作,PendingIntent:延迟意图，类似点击事件的使用方式，只有触发条件生效，才会执行intent操作
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 3000, operation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
        //取消定时操作
        alarmManager.cancel(operation);//取消定时操作
    }
}