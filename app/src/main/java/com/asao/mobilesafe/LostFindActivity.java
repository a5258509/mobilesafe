package com.asao.mobilesafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.asao.mobilesafe.receiver.AdminReceiver;
import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;

/***
 * 手机防盗页面
 */
public class LostFindActivity extends AppCompatActivity {
    private TextView mAginEnter;
    private TextView mSafeNumber;
    private ImageView mIsProtected;
    private RelativeLayout mRelProtected;
    private MyConnection conn;

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            gotoLocation();
        };
    };
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mAccuracy;
    private TextView mAltitude;
    private TextView mSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_find);
        mLatitude = findViewById(R.id.latitude);
        mLongitude = findViewById(R.id.longitude);
        mAccuracy = findViewById(R.id.accuracy);
        mAltitude = findViewById(R.id.altitude);
        mSpeed = findViewById(R.id.speed);

        initView();
    }
    /**
     * 初始化控件
     *
     * 2016-10-10 上午11:48:44
     */
    private void initView() {
        mAginEnter = findViewById(R.id.lostfind_tv_aginenter);

        //设置重新进入设置向导的点击事件
        mAginEnter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LostFindActivity.this,SetUp1Activity.class);
                startActivity(intent);
                finish();
            }
        });

        //根据保存的安全号码和防盗保护是否开启的状态设置页面展示的信息
        mSafeNumber = findViewById(R.id.lostfind_tv_safenumber);
        mIsProtected = findViewById(R.id.lostfind_iv_isprotected);
        mRelProtected = findViewById(R.id.lostfind_rel_protected);
        String sp_safenumber = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SAFENUMBER, "");
        mSafeNumber.setText(sp_safenumber);
        Boolean sp_protected = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.PROTECTED, false);
        if(sp_protected){
            mIsProtected.setImageResource(R.mipmap.lock);
        }else{
            mIsProtected.setImageResource(R.mipmap.unlock);
        }
        //点击防盗保护是否开启的条目,实现快速开启和关闭防盗保护
        mRelProtected.setOnClickListener(v -> {
            Boolean aBoolean = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.PROTECTED, false);
            if(aBoolean){
                SharedPreferencesUtil.savaBoolean(getApplicationContext(),Constants.PROTECTED,false);
                mIsProtected.setImageResource(R.mipmap.unlock);
            }else{
                SharedPreferencesUtil.savaBoolean(getApplicationContext(),Constants.PROTECTED,true);
                mIsProtected.setImageResource(R.mipmap.lock);
            }
        });
    }


    public void playalarm(View view) {
        System.out.println("播放报警音乐");
        MediaPlayer mediaPlayer=MediaPlayer.create(getApplicationContext(), R.raw.ylzs);
        //设置音量大小
        //leftVolume rightVolume 左右声道
        mediaPlayer.setVolume(1.0f,1.0f);
        mediaPlayer.setLooping(true);//是否循环播放
        mediaPlayer.start();
    }

    public void lockscreen(View view) {
        //设备的管理者
        DevicePolicyManager devicePolicyManager= (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        //组件的标示,
        ComponentName componentName=new ComponentName(getApplicationContext(), AdminReceiver.class);
        //判断设备管理员权限是否激活
        if(devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow();//锁屏
        }
    }


    public void location(View view) {
        if (ContextCompat.checkSelfPermission(LostFindActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(LostFindActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }else{
            gotoLocation();
        }
    }

    //申请权限结果的回调
    //grantResults参数为数组,即要申请的权限数组 每个权限只有两个值,用户同意授权即为0,否则为-1
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==0){
            gotoLocation();
        }else{
            Toast.makeText(getApplicationContext(),"不开启定位权限怎么能知道您的位置呢?",Toast.LENGTH_SHORT).show();
        }

    }

    private void gotoLocation() {
        //混合方式开启服务
        Intent service=new Intent(this,GPSService.class);
        startService(service);
        conn = new MyConnection();
        bindService(service, conn,BIND_AUTO_CREATE);
    }




    class MyConnection implements ServiceConnection {

        private GPSService.MyBinder mylocation;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mylocation = (GPSService.MyBinder) service;
            double longitude = mylocation.getLongitude();
            double latitude = mylocation.getLatitude();
            double altitude = mylocation.getAltitude();
            float accuracy = mylocation.getAccuracy();
            float speed = mylocation.getSpeed();
//            Log.d("main1", "经度1："+String.valueOf(longitude));
//            Log.d("main1", "纬度1："+String.valueOf(latitude));
//            Log.d("main1", "海拔1："+String.valueOf(altitude));
//            Log.d("main1", "精确度1："+String.valueOf(accuracy));
//            Log.d("main1", "速度1："+String.valueOf(speed));
            mLongitude.setText(longitude+"");
            mLatitude.setText(latitude+"");
            mSpeed.setText(speed+"");
            mAccuracy.setText(accuracy+"");
            mAltitude.setText(altitude+"");

            Message msg=Message.obtain();
            //sendMessageDelayed发送一条延迟执行的消息
            handler.sendMessageDelayed(msg,1000);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }




    protected void onDestroy(){
        super.onDestroy();
        if(conn!=null){
            unbindService(conn);
        }

    }
}
