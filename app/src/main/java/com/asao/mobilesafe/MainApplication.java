package com.asao.mobilesafe;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

//Application相当于应用程序,程序先执行的application,再执行的activity
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//Xutils初始化
        x.Ext.setDebug(false);//输出debug日志，开启会影响性能

        //设置监听异常
        Thread.currentThread().setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    }

    private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        //当有未捕获的异常的时候调用的方法
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            try {
                e.printStackTrace(new PrintStream(new File("mnt/sdcard/asao/error.log")));
                //上传异常文件到服务器
                Log.d("main1","uncaughtException");
                uploadfile();
                Log.d("main1","uncaughtException1");
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            //自己杀死自己
            android.os.Process.killProcess(android.os.Process.myPid());//myPid()获取当前进程pid
        }
    }


    private void uploadfile() {
        RequestParams params=new RequestParams("http://192.168.1.7:8080/img/");
        params.addBodyParameter("file",new File("mnt/sdcard/asao/error.log"));
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("main1","onSuccess");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("main1","onError");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("main1","onCancelled");
            }

            @Override
            public void onFinished() {
                Log.d("main1","onFinished");
            }
        });
    }

}
