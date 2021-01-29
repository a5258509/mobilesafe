package com.asao.mobilesafe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.asao.mobilesafe.bean.SMSInfo;
import com.asao.mobilesafe.engine.SmsEngine;
import com.asao.mobilesafe.service.AppLockService1;
import com.asao.mobilesafe.service.BlackNumberService;
import com.asao.mobilesafe.utils.LogUtil;
import com.asao.mobilesafe.utils.ServiceUtil;
import com.asao.mobilesafe.view.SettingView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class CommonToolActivity extends AppCompatActivity implements View.OnClickListener {

    private SettingView mAddress;
    private SettingView mReadSMS;
    private SettingView mWriteSMS;
    private String defaultSmsApp;
    private SettingView mAppLock;
    private SettingView mAppLockService1;
    private SettingView mAppLockService2;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tool);

        initView();
    }

    private void initView() {
        mAddress = findViewById(R.id.commontool_sv_address);
        mReadSMS = findViewById(R.id.commontool_sv_readsms);
        mWriteSMS = findViewById(R.id.commontool_sv_writesms);
        mAppLock = findViewById(R.id.commontool_sv_applock);
        mAppLockService1 = findViewById(R.id.commontool_sv_applockservice1);
        mAppLockService2 = findViewById(R.id.commontool_sv_applockservice2);

        //设置点击事件
        mAddress.setOnClickListener(this);

        //设置短信备份和还原的点击事件
        mReadSMS.setOnClickListener(this);
        mWriteSMS.setOnClickListener(this);

        //设置程序锁点击事件
        mAppLock.setOnClickListener(this);

        //设置开启电子狗服务的点击事件
        mAppLockService1.setOnClickListener(this);
        mAppLockService2.setOnClickListener(this);



    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.commontool_sv_address:
                //跳转号码归属地查询界面
                Intent intent=new Intent(CommonToolActivity.this, AddressActivity.class);
                startActivity(intent);
                break;
            case R.id.commontool_sv_readsms:
                //备份短信
                //显示进度条对话框
                ProgressDialog dialog = new ProgressDialog(CommonToolActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条对话框进度的样式，显示圆形还是进度操作
                dialog.setCancelable(true);//设置对话框不可消失
                dialog.show();
                new Thread(){
                    @Override
                    public void run() {
                        SmsEngine.readSms(getApplicationContext(), new SmsEngine.ReadSmsListener() {
                            @Override
                            public void setMax(int max) {
                                dialog.setMax(max);
                            }

                            @Override
                            public void setProgress(int progress) {
                                dialog.setProgress(progress);
                            }
                        });
                        dialog.dismiss();
                    }
                }.start();
                break;
            case R.id.commontool_sv_writesms:
                //还原短信
                String currentPn = getPackageName();//获取当前程序包名
                //获取手机当前设置的默认短信应用的包名,通过输出发现系统的默认短信包名为com.android.mms
                defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
                //用临时变量将第一次获取到的系统默认短信应用的包名存起来,防止用户改变默认应用后defaultSmsApp变为自己app的包名
                //String temp=defaultSmsApp;
                //Log.d("main1",defaultSmsApp);
                if (!defaultSmsApp.equals(currentPn)){
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("提示:");
                    builder.setMessage("该操作需要设置当前应用为默认短信应用,在恢复完成后会还原系统默认的短信应用!");
                    builder.setPositiveButton("确定", (dialog1, which) -> {
                        Intent mysmsintent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        mysmsintent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, currentPn);
                        startActivityForResult(mysmsintent,1);
                    });
                    builder.setNegativeButton("取消", (dialog1, which) -> {
                    });
                    builder.show();
                }
                break;
            case R.id.commontool_sv_applock:
                Intent intent1=new Intent(this,AppLockActivity.class);
                startActivity(intent1);
                break;
            case R.id.commontool_sv_applockservice1:
                //1.开启/关闭服务
                    //需要知道服务是否开启
                    //不能SharedPreferences的原因：因为在系统的设置操作可以手动的停止服务，手动停止服务因为是在系统的设置界面中的，所以不好更改SharedPreferences保存的值
                    // 动态的获取服务是否开启
                    Intent intent2=new Intent(getApplicationContext(), AppLockService1.class);
                    if(ServiceUtil.isServiceRunning(CommonToolActivity.this,"com.asao.mobilesafe.service.AppLockService1")){
                        //开启->点击关闭服务
                        stopService(intent2);
                    }else {
                        //关闭->点击开启服务
                        startService(intent2);
                    }
                    //开关
                    mAppLockService1.toggle();
                break;
            case R.id.commontool_sv_applockservice2:
                //跳转到系统无障碍界面
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("main1", String.valueOf(resultCode));
        //当允许修改默认应用时,返回-1,不允许时返回0
        if(resultCode==-1){
            //还原短信
            //读取文件中的短信
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("mnt/sdcard/asao/sms.txt")));
                String readLine=bufferedReader.readLine();
                //LogUtil.d("main1",readLine);
                //将json串转化成的list集合
                Gson gson=new Gson();
                List<SMSInfo> list = gson.fromJson(readLine,new TypeToken<List<SMSInfo>>(){}.getType());
                for(SMSInfo smsInfo:list){
                    ContentResolver contentResolver=getContentResolver();
                    Uri uri=Uri.parse("content://sms/");
                    ContentValues contentValues=new ContentValues();
                    contentValues.put("address",smsInfo.address);
                    contentValues.put("date",smsInfo.date);
                    contentValues.put("type",smsInfo.type);
                    contentValues.put("body",smsInfo.body);
                    contentResolver.insert(uri,contentValues);
                }
                Toast.makeText(CommonToolActivity.this,"恢复完成",Toast.LENGTH_LONG).show();
                //恢复系统默认的短信应用
                //通过输出发现系统的默认短信包名为com.android.mms
                //要再设置本应用之前 去获取系统默认短信应用，不然你设置了自己的应用为默认短信应用，你再去获取，那获取到的肯定就是你自己app的包名
                //由于我是在设置本应用之前 就获取到了系统默认短信应用,所以当前的defaultSmsApp值为com.android.mms
                if (defaultSmsApp.equals("com.android.mms")) {
                    Intent defaultsmsintent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    defaultsmsintent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "com.android.mms");
                    startActivity(defaultsmsintent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        //2.再次进入的时候，判断服务是否开启，设置开关状态
        boolean serviceIsRunning = ServiceUtil.isServiceRunning(CommonToolActivity.this, "com.asao.mobilesafe.service.AppLockService1");
        mAppLockService1.setToggleOn(serviceIsRunning);
    }







}