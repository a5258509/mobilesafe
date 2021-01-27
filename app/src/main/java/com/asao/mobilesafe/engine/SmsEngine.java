package com.asao.mobilesafe.engine;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;

import com.asao.mobilesafe.bean.SMSInfo;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//读取与备份短信操作
public class SmsEngine {

    public interface ReadSmsListener{

        public void setMax(int max);
        public void setProgress(int progress);
    }




    //读取短信操作
    public static void readSms(Context context, ReadSmsListener readSmsListener){
        List<SMSInfo> list = new ArrayList<>();
        //获取内容解析者
        ContentResolver contentResolver=context.getContentResolver();
        //设置地址
        Uri uri=Uri.parse("content://sms/");
        Cursor cursor = contentResolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);

        //设置总进度
        //getCount();获取cursor保存数据的个数
        readSmsListener.setMax(cursor.getCount());
        //设置当前进度,每循环一次,加一个进度
        int progress=0;//初始化当前进度
        while(cursor.moveToNext()){
            SystemClock.sleep(50);
            String address = cursor.getString(0);
            String date = cursor.getString(1);
            String type = cursor.getString(2);
            String body = cursor.getString(3);
            //System.out.println("address:"+address+"date:"+date+"type:"+type+"body"+body);
            SMSInfo smsInfo=new SMSInfo(address,date,type,body);
            list.add(smsInfo);
            //每循环一次,进度+1;
            progress++;
            //将进度设置给dialog显示
            readSmsListener.setProgress(progress);
        }

        //备份短信,通过谷歌提供的gson.jar包,可以很方便的将list集合转为json字符串进行保存,然后再将json字符串转成list集合进行展示
        Gson gson=new Gson();
        String json = gson.toJson(list);
        //将json串写到文件中
        try {
            FileWriter fileWriter=new FileWriter(new File("mnt/sdcard/asao/sms.txt"));
            fileWriter.write(json);
            fileWriter.flush();//刷新数据
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
