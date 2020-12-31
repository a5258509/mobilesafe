package com.asao.mobilesafe;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asao.mobilesafe.utils.PackageUtil;

import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class splashActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String url="http://192.168.1.6:8080/img/ADBlock.exe";
    private TextView mVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏,必须放在setContentView之前,只在当前activity生效
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        initView();
    }

    //初始化控件
    private void initView() {
        mVersion = findViewById(R.id.splash_tv_version);

        //获取当前应用程序的版本名称,设置给textview展示
        mVersion.setText("版本"+ PackageUtil.getVersionName(this));
        
        update();
    }

    //更新版本
    private void update() {
        //1.连接服务器
        //1.1联网操作,耗时操作,在子线程中
        //1.2权限
        //1.3 联网框架  okhttp xutils
        RequestParams params = new RequestParams(url);
        //设置连接超时时间
       params.setConnectTimeout(2000);
        params.setAutoResume(true);
        params.setAutoRename(false);
        params.setExecutor(new PriorityExecutor(2, true));
        params.setCancelFast(true);
        x.http().get(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(String result) {
                //2.连接成功,获取服务器返回数据(哪些数据 版本号:code,新版本下载地址:apkurl,更新描述信息:msg)
                //问题 如何封装数据 xml和json
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });


        }


}