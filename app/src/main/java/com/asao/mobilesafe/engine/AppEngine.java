package com.asao.mobilesafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.asao.mobilesafe.bean.Appinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 获取系统安装的应用程序信息
 *
 */
public class AppEngine {

    /**
     * 获取系统中安装的所有应用程序的信息
     *
     * @return
     */
    public static List<Appinfo> getAllAppInfos(Context context){
        List<Appinfo> list = new ArrayList<>();

        //1.包的管理者
        PackageManager pm=context.getPackageManager();
        //2.获取安装的所有应用程序信息
        //flags:获取额外信息的标示
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        //3.遍历集合,从应用程序的信息中获取我们需要的信息
        for(PackageInfo packageInfo:installedPackages){
            //获取应用包名
            String packageName = packageInfo.packageName;
            //获取清单文件中的application信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            int uid = applicationInfo.uid;//获取app的uid
            //UUID storageUuid = applicationInfo.storageUuid;//获取存储uid

            //获取应用程序名称
            String name = applicationInfo.loadLabel(pm).toString();
            //获取应用程序图标
            Drawable icon = applicationInfo.loadIcon(pm);
            //获取资源路径
            String sourceDir1 = applicationInfo.sourceDir;
            //占用空间大小
            String sourceDir = applicationInfo.sourceDir;//data/app/目录下的apk路径
            long size = new File(sourceDir).length();//获取apk文件大小
            //获取应用程序在系统中的标示
            int flags=applicationInfo.flags;
            //判断是否是系统应用程序
            boolean isSystem;
            if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                //系统应用程序
                isSystem=true;
            }else{
                //用户程序
                isSystem=false;
            }
            //将信息保存在bean类中
            Appinfo appinfo=new Appinfo(packageName,name,icon,size,isSystem,sourceDir1,uid);
            //将bean类保存到list集合中
            list.add(appinfo);
        }
        return list;

    }




}
