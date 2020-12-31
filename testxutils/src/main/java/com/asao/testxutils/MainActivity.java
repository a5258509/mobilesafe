package com.asao.testxutils;

import androidx.appcompat.app.AppCompatActivity;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.x;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String url="http://192.168.1.6:8080/img/ADBlock.exe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url="http://192.168.1.6:8080/img/ADBlock.exe";

        RequestParams params = new RequestParams(url);
        params.setAutoResume(true);
        params.setAutoRename(false);
        //params.setSaveFilePath(getFileName(url));
        params.setExecutor(new PriorityExecutor(2, true));
        params.setCancelFast(true);




    }
}