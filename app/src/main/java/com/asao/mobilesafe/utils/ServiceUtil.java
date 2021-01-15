package com.asao.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * 动态获取服务是否开启操作
 * */
public class ServiceUtil {
    //className:服务的全类名
    public static boolean isServiceRunning(Context context, String className){
        //1.进程的管理者(活动的管理者)
        ActivityManager am= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取进程中正在运行的服务的集合
        //参数：获取的服务的个数的上限，没有1000,有多少返回多少，超过1000，只返回1000个
        //getRunningServices已被更改,不会返回别的应用的服务,只会返回自己的
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(1500);
        //3.遍历正在运行的服务的集合中是否有我们的服务
            for(ActivityManager.RunningServiceInfo runningServiceInfo : runningServices){
                //获取正在运行的服务的组件标示
                ComponentName service = runningServiceInfo.service;
                //获取正在运行的服务的全类名
                String clsname = service.getClassName();
                //判断正在运行的服务的全类名跟我们的服务的全类名是否一致
                if(className.equals(clsname)){
                    return true;
                }
        }
        return false;
    }
}
