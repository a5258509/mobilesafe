package com.asao.mobilesafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.asao.mobilesafe.utils.Constants;
import com.asao.mobilesafe.utils.PackageUtil;
import com.asao.mobilesafe.utils.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

public class splashActivity extends AppCompatActivity  {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String url="http://192.168.1.6:8080/img/update.html";
    private TextView mVersion;
    private int mNewsCode;
    private String mNewsUrl;
    private String mNewsMsg;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏,必须放在setContentView之前,只在当前activity生效
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        if (ContextCompat.checkSelfPermission(splashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(splashActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.SEND_SMS},1);
        }else{
            initView();
        }

    }
    //申请权限结果的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initView();
    }

    //初始化控件
    private void initView() {
        mVersion = findViewById(R.id.splash_tv_version);

        //获取当前应用程序的版本名称,设置给textview展示
        mVersion.setText("版本"+ PackageUtil.getVersionName(this));
        //通过hanlder反延迟消息,两种实现方式,延迟几秒时间再更新
        //第一个参数r:handler接受消息执行的操作
        //第二个参数:延迟时间
        //方式一
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //根据设置中心保存的开关状态,设置是更新还是不更新的操作
                Boolean isupdate = SharedPreferencesUtil.getBoolean(getApplicationContext(), Constants.ISUPDATE, true);
                if(isupdate){
                    update();
                }else{
                    enterHome();
                }

            }
        },2000);
        //方式二
//        new Handler(){
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                update();
//            };
//        }.sendEmptyMessageDelayed(0,2000);

    }

    //更新版本
    private void update() {
        //1.连接服务器
        //1.1联网操作,耗时操作,在子线程中
        //1.2权限
        //1.3 联网框架  okhttp xutils
        RequestParams params = new RequestParams(url);
        //设置连接超时时间
        //   params.setConnectTimeout(5000);
//        params.setAutoResume(true);
//        params.setAutoRename(false);
//        params.setExecutor(new PriorityExecutor(2, true));
//        params.setCancelFast(true);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            //网络请求成功调用的方法
            public void onSuccess(String result) {
                //result是访问服务器成功后获取到的数据
                //Log.d("Main1",result);
                //解析服务器返回的json数据
                processJson(result);

            }

            @Override
            //网络请求失败调用的方法
            public void onError(Throwable ex, boolean isOnCallback) {
                enterHome();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("Main1","onCancelled");
            }

            @Override
            public void onFinished() {

            }
        });

    }

    /**
     * 解析json数据
     * */
    private void processJson(String result) {
        try {
            //将result封装成json对象,通过key-value进行解析
            JSONObject jsonObject=new JSONObject(result);
            mNewsCode = jsonObject.getInt("code");
            mNewsUrl = jsonObject.getString("apkurl");
            mNewsMsg = jsonObject.getString("msg");

            //判断是否有最新版本
            if(PackageUtil.getVersionCode(this)==mNewsCode){
                //如何一致,说明没有最新版本,没有最新版本就跳转到首页
                enterHome();
            }else{
                //弹出更新版本对话框
                showUpdateDialog();

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //设置对话框是否可以消失,屏蔽返回键
        //builder.setCancelable(false);
        builder.setTitle("最新版本"+mNewsCode+".0");
        builder.setIcon(R.mipmap.icon);
        builder.setMessage(mNewsMsg);
        //监听对话框消失的操作
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //隐藏对话框
                dialog.dismiss();
                enterHome();
            }
        });

        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //下载最新版本的apk
                downloadAPK();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enterHome();
            }
        });
        builder.show();
    }

    private void downloadAPK() {
        //2.显示下载进度条对话框
        showProgressDialog();
        //1.链接服务器,下载最新版本
        RequestParams params = new RequestParams(mNewsUrl);
        params.setAutoResume(true);
        params.setAutoRename(false);
        params.setExecutor(new PriorityExecutor(3, true));
        params.setSaveFilePath(getFileName(mNewsCode));
        params.setCancelFast(true);
        x.http().get(params, new Callback.ProgressCallback<File>(){

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                    progressDialog.setMax((int) total);
                    progressDialog.setProgress((int) current);
            }

            @Override
            public void onSuccess(File result) {
                //下载成功,隐藏对话框
                progressDialog.dismiss();
                //安装APK
                Log.d("Main1","1");
                installApk(getApplicationContext());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //下载失败,隐藏对话框,跳转首页
                progressDialog.dismiss();
                enterHome();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }



    //显示下载进度条对话框
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);//对话框是否可以消失
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条样式
        progressDialog.show();
    }

    private void enterHome() {
       Intent intent=new Intent(this,HomeActivity.class);
       startActivity(intent);
       //移除开屏动画页面
        finish();
    }



    //未知来源应用安装权限决绝方法
    //兼容8.0
//    private void CaninstallApkAllowed(Context context) {
//        boolean installAllowed;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            installAllowed = context.getPackageManager().canRequestPackageInstalls();
//            if (installAllowed) {
//                installApk(context);
//            } else {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.getPackageName()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//                installApk(context);
//                return;
//            }
//        } else {
//            installApk(context);
//        }
//    }

    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    private void installApk(Context context) {
            File file = new File(getFileName(mNewsCode));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.addCategory("android.intent.category.DEFAULT");
            // 此处不使用FLAG_ACTIVITY_NEW_TASK是因为将无法获取onActivityResult结果
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
                //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
                Uri apkuri= FileProvider.getUriForFile(context,"com.asao.mobilesafe.fileprovider",file);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkuri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }
            //当跳转到的安装activity退出时,会调用当前activity的onactivityresult方法
        startActivityForResult(intent,123);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //跳转到首页
        enterHome();
    }

    private  String getFileName(int path) {
       return Environment.getExternalStorageDirectory()+"/asao/mobilesafe"+String.valueOf(path)+".apk";
        //return "mnt/sdcard/asao/mobilesafe"+String.valueOf(path)+".apk";
    }




}

