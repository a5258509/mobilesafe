package com.asao.mobilesafe.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.Log;

import com.asao.mobilesafe.R;
import com.asao.mobilesafe.bean.ProcessInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//获取进程管理信息
public class ProcessEngine {

    //获取正在运行的进程数
    public static int getRunningProcessCount(Context context){
//        ActivityManager activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
//        for(ActivityManager.RunningServiceInfo serviceInfo:runningServices){
//            Log.d("main1", String.valueOf(serviceInfo.service));
//            Log.d("main1", String.valueOf(serviceInfo.process));
//            Log.d("main1", String.valueOf(serviceInfo));
//        }
        ActivityManager am= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();//获取正在运行的进程的信息
        //Log.d("main1", String.valueOf(runningAppProcesses.size()));
        return runningAppProcesses.size();
    }

    //获取总的进程数
    public static int getAllProcessCount(Context context){
        //通过包管理者获取已安装的应用程序信息,一个应用程序运行在一个进程中的，所以理论上一个应用程序对应一个进程
        PackageManager pm=context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES|PackageManager.GET_PROVIDERS|PackageManager.GET_RECEIVERS|PackageManager.GET_SERVICES);
        //因为android允许应用的四大组件单独存放到一个一个进程，所以有时候就不是一个应用程序对应一个进程，可能是一个应用程序对应多个进程
        Set<String> set = new HashSet<>();//使用set集合是因为其保存元素不重复的特性,这里就是需要保存不重复的进程名
        for(PackageInfo packageInfo:installedPackages){
            //将应用程序的进程名称保存到set中
            //processName : 进程名称
            set.add(packageInfo.applicationInfo.processName);
            //获取四大组件所在的进程
            ActivityInfo[] activities = packageInfo.activities;//获取应用程序中清单文件中所有acitivity信息
            if(activities!=null){
                for(ActivityInfo activityInfo:activities){
                    //activityInfo.processName : 组件所在的进程的名称
                    set.add(activityInfo.processName);
                }
            }
            ProviderInfo[] providers = packageInfo.providers;//获取应用程序中清单文件中所有内容提供者信息
            if(providers!=null){
                for(ProviderInfo providerInfo:providers){
                    set.add(providerInfo.processName);
                }
            }
            ActivityInfo[] receivers = packageInfo.receivers;//获取应用程序中清单文件中所有广播接收者信息
            if(receivers!=null){
                for(ActivityInfo activityInfo:receivers){
                    set.add(activityInfo.processName);
                }
            }
            ServiceInfo[] services = packageInfo.services;//获取应用程序中清单文件中所有服务信息
            if(services!=null){
                for(ServiceInfo serviceInfo:services){
                    set.add(serviceInfo.processName);
                }
            }
        }
        return set.size();//返回set集合大小,set有几个,那么就有几个进程
    }

    //获取空闲内存
    public static long getFreeMemory(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outinfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outinfo);//获取进程内存信息，保存到MemoryInfo对象中
        return outinfo.availMem;//获取可用的内存
        //outinfo.totalMem;//获取总的内存
    }

    //获取总内存
    public static long getALLMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outinfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outinfo);//获取进程内存信息，保存到MemoryInfo对象中
        //return outinfo.availMem;//获取可用的内存
        return outinfo.totalMem;//获取总的内存
    }

    //获取正在运行的进程信息
    public static List<ProcessInfo> getRunningProcessInfo(Context context){
        List<ProcessInfo> list = new ArrayList<>();

        //1.获取进程管理者
        ActivityManager am= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm=context.getPackageManager();
        //2.获取所有正在运行的进程信息
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //3.遍历集合，获取每个正在运行的进行的具体信息
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo:runningAppProcesses){

            ProcessInfo processInfo=new ProcessInfo();

            //包名
            String packageName = runningAppProcessInfo.processName;//获取进程名称，进程的名称就是应用程序的包名
            //保存包名
            processInfo.packageName=packageName;
            //获取占用内存的大小
            //int[] pids : 进程的id的数组，传递几个进程的id到数组中，最终就会返回几个进程占用的内存大小
            //runningAppProcessInfo.pid :获取进程id
            Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});//获取进程占用的内存的大小
            int totalPss = memoryInfo[0].getTotalPss();//获取内存大小，返回单位是kb
            long size=totalPss*1024;//kb -> b
            //保存大小
            processInfo.size=size;
            try {
                //获取名称图标和是否是系统进程
                //packageName : 包名
                //flags : 额外的信息标示
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                //获取应用程序名称
                String name = applicationInfo.loadLabel(pm).toString();
                //保存名称
                processInfo.name=name;
                //获取应用程序图标
                Drawable icon = applicationInfo.loadIcon(pm);
                //保存图标
                processInfo.icon=icon;
                //获取应用程序在系统中的标示
                int flags=applicationInfo.flags;
                //判断是否是系统进程
                boolean isSystem;
                if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                    //系统进程
                    isSystem=true;
                }else{
                    //用户进程
                    isSystem=false;
                }
                //保存是否系统进程
                processInfo.isSystem=isSystem;

            } catch (PackageManager.NameNotFoundException e) {
                //名称信息没有找到的异常
                //没有获取到进程的名称等信息，设置默认的信息
                processInfo.name=packageName;
                processInfo.icon = context.getResources().getDrawable(R.mipmap.ic_default,null);
                processInfo.isSystem=true;
                e.printStackTrace();
            }
            //将bean类保存到集合中，方便listview进行操作
            list.add(processInfo);
        }
        return list;

    }

    /**
     * 清理所有进程
     *
     */
    public static void killALLProcess(Context context){
        ActivityManager am= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> allProcess = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo runningAppProcessInfo:allProcess) {
            //屏蔽不能清理我们自己应用程序的进程
            if (!runningAppProcessInfo.processName.equals(context.getPackageName())) {
                am.killBackgroundProcesses(runningAppProcessInfo.processName);
            }
        }

    }











}
