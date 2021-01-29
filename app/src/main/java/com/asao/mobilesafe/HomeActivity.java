package com.asao.mobilesafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asao.mobilesafe.service.ProtectedService;
import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.MD5Util;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ImageView mLogo;


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private GridView mGridView;
    private final String[] TITLES = new String[] { "手机防盗", "骚扰拦截", "软件管家",
            "进程管理", "流量统计", "手机杀毒", "缓存清理", "常用工具" };
    private final String[] DESCS = new String[] { "远程定位手机", "全面拦截骚扰", "管理您的软件",
            "管理运行进程", "流量一目了然", "病毒无处藏身", "系统快如火箭", "工具大全" };
    private final int[] ICONS = new int[] { R.mipmap.sjfd, R.mipmap.srlj,
            R.mipmap.rjgj, R.mipmap.jcgl, R.mipmap.lltj, R.mipmap.sjsd,
            R.mipmap.hcql, R.mipmap.cygj };
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        initView();

        //拷贝归属地数据库
        copyDB("address.db");
        
        //开启守护进程
        startService(new Intent(this, ProtectedService.class));
    }


    private void copyDB(String dbName) {
        //判断如果数据库已经拷贝成功，不需要再次拷贝
        File file = new File(getFilesDir(), dbName);
        if(!file.exists()){
            //1.获取assets目录的管理者
            AssetManager assets = getAssets();
            InputStream inputStream = null;
            FileOutputStream outputStream=null;
            try {
                //2.读取数据资源
                inputStream = assets.open(dbName);
                //getFilesDir() : data -> data -> -> files//获取data/data/应用程序的包名/files目录
                //getCacheDir() : data -> data ->-> cache获取data/data/应用程序的包名/cache目录
                outputStream = new FileOutputStream(file);
                //3.读写操作
                byte[]b=new byte[1024];//缓冲区域
                int len=-1;//保存读取的长度
                while ((len= inputStream.read(b))!=-1){
                    outputStream.write(b,0,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream!=null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }


    private void initView() {
        mLogo = findViewById(R.id.home_iv_logo);
        mGridView = findViewById(R.id.home_gv_gridview);
        //实现logo的旋转动画效果
        setAnimation();
        //通过GridView显示数据
        mGridView.setAdapter(new Myadapter());

        //设置GridView的条目点击事件
        mGridView.setOnItemClickListener(this);
    }

    //GridView的条目点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                //手机防盗item被点击
                //判断是弹出设置密码对话框还是验证密码对话框
                String sp_psw = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SJFDPSW, "");
                if(TextUtils.isEmpty(sp_psw)){
                    //弹出设置密码对话框
                    showSetPassWordDialog();
                }else{
                    showEnterPassWordDialog();
                }
                break;
            case 1:
                //骚扰拦截
                Intent intent=new Intent(HomeActivity.this,BlackNumberActivity.class);
                startActivity(intent);
                break;
            case 2:
                //软件管家
                Intent intent2=new Intent(HomeActivity.this,AppManagerActivity.class);
                startActivity(intent2);
                break;
            case 3:
                //进程管理
                Intent intent3=new Intent(HomeActivity.this,ProcessManagerActivity.class);
                startActivity(intent3);
                break;
            case 4:
                //流量统计
                Intent intent4=new Intent(HomeActivity.this,TrafficActivity.class);
                startActivity(intent4);
                break;
            case 6:
                //缓存清理
                Intent intent6=new Intent(HomeActivity.this,ClearCacheActivity.class);
                startActivity(intent6);
                break;
            case 7:
                // 常用工具
                Intent intent7=new Intent(HomeActivity.this,CommonToolActivity.class);
                startActivity(intent7);
                break;


        }
    }

    //弹出验证密码对话框
    private void showEnterPassWordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view = View.inflate(getApplicationContext(),R.layout.home_enterpassword_dialog, null);
        //初始化控件
        EditText mPsw=view.findViewById(R.id.dialog_et_psw);
        Button mOk=view.findViewById(R.id.dialog_ok);
        Button mCancel=view.findViewById(R.id.dialog_cancel);

        mCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        mOk.setOnClickListener(v -> {
            String psw = mPsw.getText().toString().trim();
            //判断密码是否为空
            if(TextUtils.isEmpty(psw)){
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            //获取保存的密码,判断输入的密码是否正确
            String sp_psw = SharedPreferencesUtil.getString(getApplicationContext(), Constants.SJFDPSW, "");
            //因为md5加密不可逆，所以比较的时候，将输入的密码再次加密，让两个密文比较
            String password2=MD5Util.msgToMD5(sp_psw);
            if(MD5Util.msgToMD5(psw).equals(sp_psw)){
                Toast.makeText(this, "密码正确", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                //跳转到手机防盗设置向导的第一个界面
                enterLostFind();
            }else{
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            }

        });


        //将一个view对象添加到对话框中显示
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

    }

    /**
     * 跳转到手机防盗设置向导第一个界面
     *
     */
    protected void enterLostFind() {

        //获取是否设置成功的标示，保存：跳转到手机防盗信息展示页面，没有保存：跳转到设置向导页面
        boolean b = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.ISSJFDSETTING, false);
        if (b) {
            Intent intent = new Intent(this,LostFindActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this,SetUp1Activity.class);
            startActivity(intent);
        }

    }

    //弹出设置密码对话框
    private void showSetPassWordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view = View.inflate(getApplicationContext(),R.layout.home_setpassword_dialog, null);

        //初始化控件
        EditText mPsw=view.findViewById(R.id.dialog_et_psw);
        EditText mConfirm=view.findViewById(R.id.dialog_et_confirm);
        Button mOk=view.findViewById(R.id.dialog_ok);
        Button mCancel=view.findViewById(R.id.dialog_cancel);

        //设置点击事件
        mOk.setOnClickListener(v -> {
            //获取输入的密码
            String psw = mPsw.getText().toString().trim();
            //判断密码是否为空
            if(TextUtils.isEmpty(psw)){
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            //判断两个密码是否一致
            String confirm = mConfirm.getText().toString().trim();
            if(psw.equals(confirm)){
                Toast.makeText(this, "密码设置成功", Toast.LENGTH_SHORT).show();
                //隐藏对话框
                dialog.dismiss();
                SharedPreferencesUtil.savaString(getApplicationContext(), Constants.SJFDPSW, MD5Util.msgToMD5(psw));

            }else{
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            }
        });

        mCancel.setOnClickListener(v -> {
           dialog.dismiss();
        });



        //将一个view对象添加到对话框中显示
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    //gridview的adapter
    private class Myadapter extends BaseAdapter{
        //设置条目个数
        @Override
        public int getCount() {
            return ICONS.length;
        }
        //根据条目的位置获取条目数据
        @Override
        public Object getItem(int position) {
            return null;
        }
        //获取条目的id
        @Override
        public long getItemId(int position) {
            return 0;
        }
        //设置条目样式
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.home_grideview_item, null);

            //初始化控件 显示内容
            ImageView mIcon=view.findViewById(R.id.item_iv_icon);
            TextView mTitle=view.findViewById(R.id.item_tv_title);
            TextView mDesc=view.findViewById(R.id.item_tv_desc);

            //显示内容,根据条目的位置获取响应的展示
            mIcon.setImageResource(ICONS[position]);
            mTitle.setText(TITLES[position]);
            mDesc.setText(DESCS[position]);

           return view;
        }
    }


    //LOGO旋转动画
    private void setAnimation() {
        //参数1:执行动画的控件
        //参数2:执行动画的方法名称
        //参数3:执行动画所需的参数
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(mLogo,"rotationY",0f,90f,270f,360f);
        objectAnimator.setDuration(1000);//设置持续时间
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);//设置动画执行次数,INFINITE 一直执行
        objectAnimator.setRepeatMode(objectAnimator.RESTART);//设置动画执行类型
        objectAnimator.start();//执行动画
    }

    //设置按钮点击事件
    public void enterSetting(View view) {
        Intent intent =new Intent(this,SettingActivity.class);
        startActivity(intent);
    }


}