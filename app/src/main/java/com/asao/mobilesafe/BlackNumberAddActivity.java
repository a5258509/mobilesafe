package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.asao.mobilesafe.db.dao.BlackNumberDao;

public class BlackNumberAddActivity extends AppCompatActivity {

    private EditText mBlackNumber;
    private RadioGroup mModes;
    private BlackNumberDao blackNumberDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number_add);
        blackNumberDao = new BlackNumberDao(this);
        initView();

    }

    private void initView() {
        mBlackNumber = findViewById(R.id.blacknumber_et_blacknumber);
        mModes = findViewById(R.id.blacknumber_rg_modes);
    }


    //保存黑名单
    public void save(View view) {
        String blacknumber = mBlackNumber.getText().toString().trim();
        //1.判断号码是否为空
        if(TextUtils.isEmpty(blacknumber)){
            Toast.makeText(getApplicationContext(),"请输入黑名单号码",Toast.LENGTH_SHORT).show();
            //如果号码为空,不执行其他操作
            return;
        }
        //2.判断拦截类型是否选择
        int mode=-1;//初始化拦截类型
        //选中哪个RadioButton,表示选中哪个类型
        int radioButtonId = mModes.getCheckedRadioButtonId();//获取被选中的radiobuttonid
        switch (radioButtonId){
            case R.id.blacknumber_rbtn_call:
                mode=0;
                break;
            case R.id.blacknumber_rbtn_sms:
                mode=1;
                break;
            case R.id.blacknumber_rbtn_all:
                mode=2;
                break;
            default:
                Toast.makeText(getApplicationContext(),"请选择拦截类型",Toast.LENGTH_SHORT).show();
                return;
        }
        //3.添加数据
        //3.1 添加数据到数据库
        //3.1.1 判断数据库中是否已有号码
        //如果根据号码,没有从数据库中获取到拦截类型(返回-1),表示该号码不在数据库中
        if(blackNumberDao.queryMode(blacknumber)==-1){
            //3.1.2将号码添加到数据库
            boolean add = blackNumberDao.add(blacknumber, mode);
            if(add){
                Toast.makeText(getApplicationContext(),"号码添加成功",Toast.LENGTH_SHORT).show();

                //3.2 添加数据库成功,将数据返回给黑名单管理界面显示,并移除当前的界面
                Intent data=new Intent();
                data.putExtra("number",blacknumber);
                data.putExtra("mode",mode);
                setResult(RESULT_OK,data);
                finish();
            }else{
                Toast.makeText(getApplicationContext(),"系统繁忙,请稍候再试!",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"号码已存在",Toast.LENGTH_SHORT).show();
        }
        //3.2添加数据库成功,将数据返回给黑名单管理界面显示




    }

    //取消操作
    public void cancel(View view) {
        //移除activity
        finish();
    }















}