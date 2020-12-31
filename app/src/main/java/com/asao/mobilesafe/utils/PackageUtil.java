package com.asao.mobilesafe.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 获取当前应用程序的版本号和版本名称
 * */
public class PackageUtil {

    /**
     * 获取当前应用程序的版本号
     * */
    public static int getVersionCode(Context context){
        //1.包的管理者
        PackageManager pm= context.getPackageManager();
        try {
            //2.根据应用程序的包名获取应用程序清单文件中的信息
            //flags:获取额外信息,0表示获取基本信息(包名,版本号,版本名称)
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
            return -1;
    }

    /**
     * 获取当前应用程序的版本名称
     * */
    public static String getVersionName(Context context){
        //1.包的管理者
        PackageManager pm= context.getPackageManager();
        try {
            //2.根据应用程序的包名获取应用程序清单文件中的信息
            //flags:获取额外信息,0表示获取基本信息(包名,版本号,版本名称)
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
