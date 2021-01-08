package com.asao.mobilesafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;

public class SetUp2Activity extends BaseActivity {
    private RelativeLayout mRelSIM;
    private ImageView mIsLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up2);

        initView();
    }

    /**
     * 初始化控件
     *
     * 2016-10-10 下午4:44:50
     */
    private void initView() {
        mRelSIM = (RelativeLayout) findViewById(R.id.setup2_rel_sim);
        mIsLock = (ImageView) findViewById(R.id.setup2_iv_islock);


        //2.再次进入界面，回显是否绑定的操作
        String sp_sim = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SIM, "");
        if (TextUtils.isEmpty(sp_sim)) {
            mIsLock.setImageResource(R.mipmap.unlock);
        }else{
            mIsLock.setImageResource(R.mipmap.lock);
        }

        //1.点击绑定SIM卡
        //设置按钮的点击事件
        mRelSIM.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //绑定/解绑SIM卡

                //需要动态获取permission.READ_PHONE_STATE该权限
                if (ContextCompat.checkSelfPermission(SetUp2Activity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SetUp2Activity.this,new String[]{Manifest.permission.READ_PHONE_STATE},1);

                }else{
                    //绑定sim卡
                    bindsimcard();
                }

            }
        });
    }

    //申请权限结果的回调
    //grantResults参数为数组,即要申请的权限数组 每个权限只有两个值,用户同意授权即为0,否则为-1
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==0){
            bindsimcard();
        }else{
            Toast.makeText(getApplicationContext(),"骚年,你不同意怎么用我?",Toast.LENGTH_SHORT).show();
        }

    }


    private void bindsimcard() {
        //绑定 -> 点击解绑
        //未绑定 -> 点击绑定
        //判断是否绑定
        String sp_sim = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SIM, "");
        if (TextUtils.isEmpty(sp_sim)) {
            //未绑定 -> 点击绑定
            //绑定SIM卡：本质保存SIM卡的序列号
            //获取电话的管理者
            TelephonyManager tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            //String number = tel.getLine1Number();//获取和SIM卡绑定的电话号码，在中国有时候获取不到,因为开发商针对双卡手机对框架进行了修改，添加了新的代码，通过原始方法去获得手机卡号会存在获取不到的情况。这时只能针对特定手机，通过反射查找是否有可用的方法
            //Log.d("Main1",number);
            @SuppressLint("MissingPermission") String sim = tel.getSimSerialNumber();//获取SIM卡的序列号，唯一标示
            Log.d("Main1",sim);
            SharedPreferencesUtil.savaString(getApplicationContext(), Constants.SIM, sim);
            //修改的图片
            mIsLock.setImageResource(R.mipmap.lock);
        }else{
            //绑定 -> 点击解绑
            SharedPreferencesUtil.savaString(getApplicationContext(), Constants.SIM, "");
            //修改的图片
            mIsLock.setImageResource(R.mipmap.unlock);
        }
    }



    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp1Activity.class);
        startActivity(intent);
        return false;
    }
    @Override
    public boolean next_activity() {

        //判断是否绑定sim卡，如果绑定跳转，如果没有绑定禁止跳转
        String sp_sim = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SIM, "");
        if (TextUtils.isEmpty(sp_sim)) {
            Toast.makeText(getApplicationContext(), "请先绑定sim卡...", Toast.LENGTH_SHORT).show();
            return true;
        }
        Intent intent = new Intent(this,SetUp3Activity.class);
        startActivity(intent);
        return false;
    }
}