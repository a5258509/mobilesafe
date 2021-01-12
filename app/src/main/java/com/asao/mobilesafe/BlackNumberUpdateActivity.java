package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.asao.mobilesafe.db.dao.BlackNumberDao;

public class BlackNumberUpdateActivity extends AppCompatActivity {

    private String blackNumber;
    private int mode;
    private int position;
    private EditText mBlackNumber;
    private RadioGroup mModes;
    private BlackNumberDao blackNumberDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_number_update);
        blackNumberDao = new BlackNumberDao(this);
        getValue();

        initViwe();
    }



    //接收传递过来的回显数据
    private void getValue() {
        Intent intent = getIntent();// 获取跳转过来的intent
        blackNumber = intent.getStringExtra("number");
        mode = intent.getIntExtra("mode", -1);
        position = intent.getIntExtra("position",-1);
    }

    private void initViwe() {
        mBlackNumber = findViewById(R.id.blacknumberupdate_et_blacknumber);
        mModes = findViewById(R.id.blacknumberupdate_rg_modes);

        //将传递过来的数据设置给控件显示
        setData();
    }

    //将传递过来的数据设置给控件显示
    private void setData() {
        //1.显示号码
        mBlackNumber.setText(blackNumber);
        //2.显示类型
        int checkId=-1;
        switch (mode){
            case 0:
                checkId=R.id.blacknumberupdate_rbtn_call;
                break;
            case 1:
                checkId=R.id.blacknumberupdate_rbtn_sms;
                break;
            default:
                checkId=R.id.blacknumberupdate_rbtn_all;
                break;
        }
        mModes.check(checkId);//设置选中哪个radiobutton
    }


    public void update(View view) {
    //1.获取数据
        String updateblacknumber = mBlackNumber.getText().toString().trim();
        //1.判断号码是否为空
        if(TextUtils.isEmpty(updateblacknumber)){
            Toast.makeText(getApplicationContext(),"请输入黑名单号码",Toast.LENGTH_SHORT).show();
            //如果号码为空,不执行其他操作
            return;
        }
        //2.判断拦截类型是否选择
        int updatemode=-1;//初始化拦截类型
        //选中哪个RadioButton,表示选中哪个类型
        int radioButtonId = mModes.getCheckedRadioButtonId();//获取被选中的radiobuttonid
        switch (radioButtonId){
            case R.id.blacknumberupdate_rbtn_call:
                updatemode=0;
                break;
            case R.id.blacknumberupdate_rbtn_sms:
                updatemode=1;
                break;
            case R.id.blacknumberupdate_rbtn_all:
                updatemode=2;
                break;
            default:
                Toast.makeText(getApplicationContext(),"请选择拦截类型",Toast.LENGTH_SHORT).show();
                return;
        }
        //2.更新数据库数据
        boolean update = blackNumberDao.update(updateblacknumber, updatemode);
        if(update){
            Toast.makeText(getApplicationContext(),"更新成功",Toast.LENGTH_SHORT).show();

            //3.更新成功,回传给黑名单管理界面
            Intent data=new Intent();
            data.putExtra("mode",updatemode);
            data.putExtra("position",position);
            setResult(RESULT_OK,data);
            finish();
        }else{
            Toast.makeText(getApplicationContext(),"系统繁忙,稍候再试",Toast.LENGTH_SHORT).show();
        }


    }

    public void cancel(View view) {
        finish();
    }















}