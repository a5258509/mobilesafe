package com.asao.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asao.mobilesafe.engine.ProcessEngine;

public class KillProcessReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //清理进程
        ProcessEngine.killALLProcess(context);
    }
}
