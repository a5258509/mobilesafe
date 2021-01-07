package com.asao.mobilesafe.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferencesUtil 的工具类
 * SharedPreferences 用于安卓轻量级存储的类
 * */
public class SharedPreferencesUtil {

    private static SharedPreferences sp;

    //保存boolean信息
    public static void savaBoolean(Context context,String key,Boolean value){
        if(sp==null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        //保存数据
        sp.edit().putBoolean(key,value).commit();
    }

    //获取boolean信息
    public static Boolean getBoolean(Context context,String key,Boolean defvalue){
        if(sp==null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key,defvalue);
    }

    //保存String信息
    public static void savaString(Context context,String key,String value){
        if(sp==null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        //保存数据
        sp.edit().putString(key,value).commit();
    }

    //获取String信息
    public static String getString(Context context,String key,String defvalue){
        if(sp==null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        }
        return sp.getString(key,defvalue);
    }

}
