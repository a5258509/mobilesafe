package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.asao.mobilesafe.receiver.AdminReceiver;

public class SetUp4Activity extends BaseActivity {

    private RelativeLayout mAdmin;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private ImageView mIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up4);

        initView();
    }

    private void initView() {
        mAdmin = findViewById(R.id.setup4_rel_admin);
        mIcon = findViewById(R.id.setup4_iv_icon);
        //设备的管理者
        devicePolicyManager = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);

        //组件的标示,如果想要使用组件，获取组件一些信息，但是又没有办法去创建组件的对象来获取，就可以通过组件的标示来获取
        componentName = new ComponentName(this, AdminReceiver.class);

        //2.再次进入界面的时候,回显激活状态
        if(devicePolicyManager.isAdminActive(componentName)){
            mIcon.setImageResource(R.mipmap.admin_activated);
        }else{
            mIcon.setImageResource(R.mipmap.admin_inactivated);
        }

        //1.激活/取消激活管理员权限
        mAdmin.setOnClickListener(v -> {

            //需要知道设备管理员权限是否激活
            if(devicePolicyManager.isAdminActive(componentName)){
                devicePolicyManager.removeActiveAdmin(componentName);//取消激活设备管理员权限
                //更改图标
                mIcon.setImageResource(R.mipmap.admin_inactivated);
            }else{
                //激活
                //表示激活超级管理员权限
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                //设置激活哪个超级管理员权限
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                //设置描述信息
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"手机卫士");
                startActivityForResult(intent, 0);
            }
        });

    }
    //判断激活管理员权限是否成功的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 当退出系统的激活界面的时候，判断设备管理员权限是否激活，激活，显示激活的图标，没有激活，显示没有激活的图标
        //判断设备管理员权限是否激活
        if(devicePolicyManager.isAdminActive(componentName)) {
            mIcon.setImageResource(R.mipmap.admin_activated);
        }else{
            mIcon.setImageResource(R.mipmap.admin_inactivated);
        }

    }



    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp3Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        //3.判断是否激活设备管理员权限
        if(!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(getApplicationContext(), "请先激活设备管理器", Toast.LENGTH_SHORT).show();
            return true;
        }

        Intent intent = new Intent(this,SetUp5Activity.class);
        startActivity(intent);
        return false;
    }
}