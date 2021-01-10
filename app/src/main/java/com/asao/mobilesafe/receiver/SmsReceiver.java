package com.asao.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;

import com.asao.mobilesafe.GPSService;
import com.asao.mobilesafe.R;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收解析短信的操作
        Object[] objs = (Object[]) intent.getExtras().get("pdus");//获取到短信
        for (Object obj : objs) {
            //转化成短信对象
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
            //获取发件人号码
            String sender = smsMessage.getOriginatingAddress();
            //获取短信内容
            String body = smsMessage.getMessageBody();
            System.out.println("发送号码:"+sender+"内容:"+body);

            //判断短信的内容是否是指令
            isMessage(body,context);
        }

    }

    //判断短信是否是指令
    private void isMessage(String body,Context context){

        //设备的管理者
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //组件的标示,
        ComponentName componentName=new ComponentName(context,AdminReceiver.class);


        if ("#*location*#".equals(body)){
            //GPS追踪
            System.out.println("GPS追踪");
            //开启定位服务,进行定位操作
            context.startService(new Intent(context, GPSService.class));

            //如果是指令,需要拦截短信,不能让系统接收
            abortBroadcast();//拦截广播,终止广播操作 国内系统,比如小米,就没这个功能
        }else if("#*alarm*#".equals(body)){
            //播放报警音乐
            System.out.println("播放报警音乐");
            MediaPlayer mediaPlayer=MediaPlayer.create(context, R.raw.ylzs);
            //设置音量大小
            //leftVolume rightVolume 左右声道
            mediaPlayer.setVolume(1.0f,1.0f);
            mediaPlayer.setLooping(true);//是否循环播放
            mediaPlayer.start();
            abortBroadcast();
        }else if("#*wipedata*#".equals(body)){
            //远程销毁数据
            System.out.println("远程销毁数据");

           //devicePolicyManager.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);//销毁数据
            abortBroadcast();

        }else if("#*lockscreen*#".equals(body)){
            //远程锁屏
            System.out.println("远程锁屏");

            //判断设备管理员权限是否激活
            if(devicePolicyManager.isAdminActive(componentName)){
                devicePolicyManager.lockNow();//锁屏
                abortBroadcast();
            }

        }
    }

}
