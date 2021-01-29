package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.asao.mobilesafe.bean.Appinfo;
import com.asao.mobilesafe.engine.AppEngine;

import java.lang.reflect.Method;
import java.util.List;

public class ClearCacheActivity extends AppCompatActivity {

    private ImageView mLine;
    private MyAsyncTask myAsyncTask;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_cache);
        initView();
        initData();

    }

    private void initView() {

        mLine =findViewById(R.id.clearcache_iv_line);
    }

    //加载数据,显示数据
    private void initData() {
        pm = getPackageManager();
        myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    //查询系统中所有应用程序的信息并展示

    private class MyAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //2.获取系统中安装应用程序的信息，并且实现获取一个展示一个的效果
            List<Appinfo> allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                //遍历集合
                for (Appinfo appInfo : allAppInfos) {
                    //获取每个应用程序的信息，并展示
                    //2.1.反射getPackageSizeInfo方法根据应用程序的包名，获取应用程序的缓存大小
                    //参数1：应用程序的包名
                    //参数2：应用程序缓存信息存放的aidl文件
                    //pm.getPackageSizeInfo(appInfo.packageName, mStatsObserver);
                    //因为getPackageSizeInfo系统隐藏了所以需要反射进行操作
                    try {
                        Method method = PackageManager.class.getDeclaredMethod("getPackageSizeInfo", String.class,IPackageStatsObserver.class);
                        method.invoke(pm, appInfo.packageName,mStatsObserver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else {
                //遍历集合
                for (Appinfo appInfo : allAppInfos) {

                    @SuppressLint("WrongConstant")
                    final StorageStatsManager storageStatsManager = (StorageStatsManager)getSystemService(Context.STORAGE_STATS_SERVICE);
                    //final StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
                    try {
                        ApplicationInfo ai = getPackageManager().getApplicationInfo(appInfo.packageName, 0);
                        StorageStats storageStats = storageStatsManager.queryStatsForUid(ai.storageUuid, appInfo.uid);

                        //long dataBytes = storageStats.getDataBytes();//用户数据大小
                        long cacheBytes = storageStats.getCacheBytes();//缓存
                        //long appBytes = storageStats.getAppBytes();//应用大小
                        String cachesize = Formatter.formatFileSize(getApplicationContext(), cacheBytes);
                        System.out.println("packageName:"+appInfo.packageName+"cache:"+cachesize);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {

            //1.实现线的平移动画
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_clearcache_line);//加载一个xml文件设置的动画
            mLine.startAnimation(animation);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }



    /**
     * 2.2.在aidl中获取缓存大小操作
     */
    IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            //获取缓存大小操作
            long cachesize = stats.cacheSize;//获取应用程序的缓存大小
            String packageName = stats.packageName;//获取应用程序的包名

            System.out.println(packageName+":"+cachesize);
        }
        };





}