package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.asao.mobilesafe.R;
import com.asao.mobilesafe.db.dao.AddressDao;

public class AddressActivity extends AppCompatActivity {

    private EditText mNumber;
    private TextView mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        initView();

    }

    private void initView() {
        mNumber = findViewById(R.id.address_et_number);
        mAddress = findViewById(R.id.address_tv_address);

        //监听输入框时时输入内容的操作
        mNumber.addTextChangedListener(new TextWatcher() {
            //在输入内容改变时调用
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String number=s.toString();//获取输入的内容
                String address = AddressDao.getAddress(AddressActivity.this, number);
                if(!TextUtils.isEmpty(number)){
                    mAddress.setText("归属地:"+address);
                }
            }
            //输入内容之前调用
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //输入内容之后调用
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //查询按钮点击事件
    public void address(View view) {
        String number = mNumber.getText().toString().trim();
        Log.d("main1", number);
        if(!TextUtils.isEmpty(number)){
            String address = AddressDao.getAddress(this, number);
            if(!TextUtils.isEmpty(address)){
                mAddress.setText("归属地:"+address);
            }

        }else{
            Toast.makeText(this, "号码不能为空", Toast.LENGTH_SHORT).show();
            Animation shake= AnimationUtils.loadAnimation(this,R.anim.shake);
            mNumber.startAnimation(shake);

            //振动
            //Vibrator vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
            //vibrator.vibrate(1000);
            //振动的频率
            //vibrator.vibrate(new long[]{30l,60l,70l,100l},0);

        }

    }
}