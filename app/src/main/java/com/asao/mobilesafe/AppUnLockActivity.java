package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AppUnLockActivity extends AppCompatActivity {

    private String packageName;
    private ImageView mIcon;
    private TextView mName;
    private EditText mPsw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_un_lock);
        //接受传递过来的加锁应用程序的包名
        packageName = getIntent().getStringExtra("packageName");

        initView();

    }

    private void initView() {
        mIcon = findViewById(R.id.appunlock_iv_icon);
        mName = findViewById(R.id.appunlock_tv_name);
        mPsw = findViewById(R.id.appunlock_et_psw);
    
        getMessage();
    }

    /**
     * 根据包名获取应用程序信息，展示应用程序的信息
     *
     */
    private void getMessage() {
        PackageManager pm=getPackageManager();
        try {
            ApplicationInfo applicationInfo=pm.getApplicationInfo(packageName,0);
            String name = applicationInfo.loadLabel(pm).toString();
            Drawable icon = applicationInfo.loadIcon(pm);

            mIcon.setImageDrawable(icon);
            mName.setText(name);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击返回键调用的方法
    //通过按home键观察到系统的如下操作,所以直接匹配意图就可以完成点击返回键实现回主页的操作
    //I/ActivityManager: START u0 {
    // act=android.intent.action.MAIN
    // cat=[android.intent.category.HOME] flg=0x10200000
    // cmp=com.miui.home/.launcher.Launcher (has extras)} from uid 1000
    @Override
    public void onBackPressed(){
        // 跳转到桌面
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        finish();
        super.onStop();
    }

    //解锁的点击事件
    public void unlock(View view) {
        //获取输入的密码
        String psw = mPsw.getText().toString().trim();
        if("123".equals(psw)){
            //密码正确
            //告诉服务，该应用已经解锁，不要在加锁了
            //问题：如何告诉服务
            //解决：1.广播，2：回调
            Intent intent =new Intent();
            intent.setAction("com.asao.mobilesafe.UNLOCK");
            //告诉服务解锁的时候那个应用程序
            intent.putExtra("packageName",packageName);
            sendBroadcast(intent);//发送自定义的广播
            finish();
        }else{
            Toast.makeText(getApplicationContext(),"密码错误",Toast.LENGTH_SHORT).show();
        }
    }
}