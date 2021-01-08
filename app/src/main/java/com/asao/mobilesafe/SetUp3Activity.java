package com.asao.mobilesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;

public class SetUp3Activity extends BaseActivity {

    private EditText mSafeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up3);

        initView();


    }

    private void initView() {
        mSafeNumber = findViewById(R.id.setup3_et_safenumber);

        String sp_safenumber = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SAFENUMBER, "");
        if(TextUtils.isEmpty(sp_safenumber)){
            mSafeNumber.setText(sp_safenumber);
        }
    }

    //选择安全号码的点击事件
    public void selectContacts(View view) {

        if (ContextCompat.checkSelfPermission(SetUp3Activity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SetUp3Activity.this,new String[]{Manifest.permission.READ_CONTACTS},1);
        }else{
            gotoContactActivity();

        }

    }



    //申请权限结果的回调
    //grantResults参数为数组,即要申请的权限数组 每个权限只有两个值,用户同意授权即为0,否则为-1
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==0){
            gotoContactActivity();
        }else{
            Toast.makeText(getApplicationContext(),"亲,同意下!",Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public boolean pre_activity() {
        Intent intent = new Intent(this,SetUp2Activity.class);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean next_activity() {
        String safenumber = mSafeNumber.getText().toString().trim();
        if(TextUtils.isEmpty(safenumber)){
            Toast.makeText(getApplicationContext(),"请输入安全号码",Toast.LENGTH_SHORT).show();
            return true;
        }
        SharedPreferencesUtil.savaString(getApplicationContext(), Constants.SAFENUMBER,safenumber);
        Intent intent = new Intent(this,SetUp4Activity.class);
        startActivity(intent);
        return false;
    }


    private void gotoContactActivity() {
        Intent intent=new Intent(this, ContactActivity.class);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //接收ContactActivity传递过来的数据
        if(resultCode==RESULT_OK){
            if(data!=null){
                String number = data.getStringExtra("number");//获取传递来的数据
                mSafeNumber.setText(number);
            }
        }
    }
}