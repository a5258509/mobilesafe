package com.asao.mobilesafe.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.asao.mobilesafe.HomeActivity;
import com.asao.mobilesafe.R;
import com.asao.mobilesafe.splashActivity;

public class ProtectedService extends Service {
    public ProtectedService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
        String CHANNEL_ONE_NAME= "手机卫士";
        NotificationChannel notificationChannel= null;

        //进行8.0的判断
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel= new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }


//        Notification notification=new Notification();
//        //设置通知栏显示的样式
//        notification.contentView=new RemoteViews(getPackageName(), R.layout.protectedservice);
//
//        //设置通知到来的显示的样式
//        notification.icon = R.mipmap.icon;
//        notification.tickerText = "手机卫士欢迎您...";

        //点击通知消息，打开应用程序
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.protectedservice);

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext()).setChannelId(CHANNEL_ONE_ID)
                    .setTicker("手机卫士欢迎您...")
                    .setContentTitle("手机卫士,你值得拥有")
                    .setContentText("您身边的安全专家")
                    .setContentIntent(pendingIntent)
                    .setCustomContentView(remoteViews)
                    .setSmallIcon(R.mipmap.icon)
                    .build();
            //id：服务的id
            //notification : 通知栏
            startForeground(100,notification);
        }else{

        Notification notification1=new Notification();
        //设置通知栏显示的样式
        notification1.contentView=new RemoteViews(getPackageName(), R.layout.protectedservice);

        //设置通知到来的显示的样式
        notification1.icon = R.mipmap.icon;
        notification1.tickerText = "手机卫士欢迎您...";

        //设置点击事件
        Intent intent1 = new Intent(this, HomeActivity.class);
        notification1.contentIntent=PendingIntent.getActivity(this, 101, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        startForeground(101, notification1);

        }




    }
}