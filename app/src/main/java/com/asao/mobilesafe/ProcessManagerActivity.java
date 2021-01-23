package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asao.mobilesafe.bean.Appinfo;
import com.asao.mobilesafe.bean.ProcessInfo;
import com.asao.mobilesafe.engine.AppEngine;
import com.asao.mobilesafe.engine.ProcessEngine;
import com.asao.mobilesafe.view.CustomProgressBar;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ProcessManagerActivity extends AppCompatActivity {

    private CustomProgressBar mCount;
    private CustomProgressBar mMemory;

    //用户进程的集合
    private List<ProcessInfo> userProcessInfos;
    //系统进程的集合
    private List<ProcessInfo> systemProcessInfos;

    private List<ProcessInfo> runningProcessInfos;
    private StickyListHeadersListView mListView;
    private LinearLayout mLLLoading;
    private Myadapter myadapter;
    private int runningProcessCount;
    private int allProcessCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);

        initView();

    }

    private void initView() {
        mCount = findViewById(R.id.process_cpb_count);
        mMemory = findViewById(R.id.process_cpb_memory);
        mListView = findViewById(R.id.process_lv_listview);
        mLLLoading = findViewById(R.id.appmanager_ll_loading);

        //耗时操作,加载应用程序数据之前，显示进度条
        mLLLoading.setVisibility(View.VISIBLE);


        //设置展示进程和内存信息
        setMsg();

        initData();

        //设置条目点击事件
        onListViewUtemClickListener();
    }

    private void onListViewUtemClickListener() {

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            //选中/取消选中checkbox
            //获取被点击条目的bean类
            ProcessInfo appinfo;
            if(position<userProcessInfos.size()){
                //用户集合中获取数据
                appinfo=userProcessInfos.get(position);
            }else{
                //系统集合中获取数据
                appinfo=systemProcessInfos.get(position-userProcessInfos.size());
            }
            //设置选中/取消选中checkbox
            if(appinfo.ischecked){
                appinfo.ischecked=false;
            }else{
                //判断如果是当前应用程序，不能被选中
                if(!appinfo.packageName.equals(getPackageName())){
                    appinfo.ischecked=true;
                }
            }
            myadapter.notifyDataSetChanged();//会重新调用adapter的getcount和getview方法
        });


    }

    //展示进程和内存信息
    private void setMsg() {
        //展示进程
        setProcessCount();
        //展示内存
        setMemory();
    }

    private void setProcessCount() {
        runningProcessCount = ProcessEngine.getRunningProcessCount(this);
        allProcessCount = ProcessEngine.getAllProcessCount(this);
        mCount.setText("进程数:    ");
        mCount.setLeft("正在运行"+ runningProcessCount +"个");
        mCount.setRight("总进程"+ allProcessCount +"个");
        int progress= (int) (runningProcessCount *100f/ allProcessCount +0.5f);
        mCount.setProgress(progress);
    }

    private void setMemory() {
        long freeMemory = ProcessEngine.getFreeMemory(this);
        long allMemory = ProcessEngine.getALLMemory(this);
        //已用内存
        long useMemory = allMemory - freeMemory;
        mMemory.setText("内存:        ");
        String useSize = Formatter.formatFileSize(this, useMemory);
        mMemory.setLeft("占用内存"+useSize);
        String freeSize = Formatter.formatFileSize(this, freeMemory);
        mMemory.setRight("可用内存"+freeSize);
        int progress= (int) (useMemory*100f/allMemory+0.5f);
        mMemory.setProgress(progress);

    }


    private void initData() {
        new Thread(){

            @Override
            public void run() {
                runningProcessInfos = ProcessEngine.getRunningProcessInfo(ProcessManagerActivity.this);
                userProcessInfos=new ArrayList<>();
                systemProcessInfos=new ArrayList<>();
                //遍历集合,将用户程序放到用户集合中,系统程序放到系统集合中
                for (ProcessInfo info:runningProcessInfos){
                    //系统进程
                    if(info.isSystem){
                        systemProcessInfos.add(info);
                    }else{
                        //用户进程
                        userProcessInfos.add(info);
                    }
                }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //在加载完数据,隐藏进度条
                        mLLLoading.setVisibility(View.GONE);
                        myadapter = new Myadapter();
                        mListView.setAdapter(myadapter);
                    }
                });
            }
        }.start();
    }

    private class Myadapter extends BaseAdapter implements StickyListHeadersAdapter {



        @Override
        public int getCount() {
            return userProcessInfos.size()+systemProcessInfos.size();
        }
        //position是listview的角标
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            //使用条目的样式
            ProcessManagerActivity.ViewHolder viewHolder;
            if(convertView==null){
                view = View.inflate(getApplicationContext(), R.layout.process_listview_item, null);
                viewHolder=new ProcessManagerActivity.ViewHolder();
                viewHolder.mIcon=view.findViewById(R.id.item_iv_icon);
                viewHolder.mName=view.findViewById(R.id.item_tv_name);
                viewHolder.mSize=view.findViewById(R.id.item_tv_size);
                viewHolder.mIsChecked=view.findViewById(R.id.item_cb_ischecked);
                view.setTag(viewHolder);
            }else{
                view=convertView;
                viewHolder= (ProcessManagerActivity.ViewHolder) view.getTag();
            }
            //获取数据展示数据
            ProcessInfo appinfo;
            //要从两个集合中获取数据,当listview的角标小于用户集合的长度时,从用户集合获取数据
            if(position<userProcessInfos.size()){
                //用户集合中获取数据
                appinfo=userProcessInfos.get(position);
            }else{
                //系统集合中获取数据
                appinfo=systemProcessInfos.get(position-userProcessInfos.size());
            }
            viewHolder.mName.setText(appinfo.name);
            viewHolder.mIcon.setImageDrawable(appinfo.icon);
            String size = Formatter.formatFileSize(getApplicationContext(), appinfo.size);
            viewHolder.mSize.setText(size);

            //根据保存在bean类中的checkbox的选中操作，动态的设置checkbox是选中还是不选中
            viewHolder.mIsChecked.setChecked(appinfo.ischecked);

            //判断如果是当前应用程序，checkbox隐藏
            if (appinfo.packageName.equals(getPackageName())) {
                //隐藏
                viewHolder.mIsChecked.setVisibility(View.GONE);
            }else{
                //显示
                viewHolder.mIsChecked.setVisibility(View.VISIBLE);
            }
        return view;
        }

        @Override
        public Object getItem(int position) {
            return runningProcessInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        //设置头的样式
        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            TextView textView=new TextView(getApplicationContext());
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(0x99429ED6);
            textView.setTextSize(20);
            textView.setPadding(8,8,8,8);
            //获取数据展示数据
            ProcessInfo appinfo;
            //要从两个集合中获取数据,当listview的角标小于用户集合的长度时,从用户集合获取数据
            if(position<userProcessInfos.size()){
                //用户集合中获取数据
                appinfo=userProcessInfos.get(position);
            }else{
                //系统集合中获取数据
                appinfo=systemProcessInfos.get(position-userProcessInfos.size());
            }
            textView.setText(appinfo.isSystem?"系统进程("+systemProcessInfos.size()+")":"用户进程("+userProcessInfos.size()+")");
            return textView;
        }

        //设置头的id
        //设置几个id,表示有几个头条目就要展示出来几个
        @Override
        public long getHeaderId(int position) {
            //获取数据展示数据
            ProcessInfo appinfo;
            //要从两个集合中获取数据,当listview的角标小于用户集合的长度时,从用户集合获取数据
            if(position<userProcessInfos.size()){
                //用户集合中获取数据
                appinfo=userProcessInfos.get(position);
            }else{
                //系统集合中获取数据
                appinfo=systemProcessInfos.get(position-userProcessInfos.size());
            }

            return appinfo.isSystem?0:1;
        }
    }

    static class ViewHolder{
        TextView mName,mSize;
        ImageView mIcon;
        CheckBox mIsChecked;
    }

    //全选
    public void all(View v){
        //将所有的标示改为true
        for (ProcessInfo info : userProcessInfos) {
            //判断如果是当前应用程序，不做全选操作
            if (!info.packageName.equals(getPackageName())) {
                info.ischecked = true;
            }
        }
        for (ProcessInfo info : systemProcessInfos) {
                info.ischecked = true;
        }
        myadapter.notifyDataSetChanged();
    }

    //反选
    public void unall(View v){
        //取反的操作
        for (ProcessInfo info : userProcessInfos) {
            //判断如果是当前应用程序，不做全选操作
            if (!info.packageName.equals(getPackageName())) {
                info.ischecked = !info.ischecked;
            }
        }
        for (ProcessInfo info : systemProcessInfos) {
            //判断如果是当前应用程序，不做全选操作
            info.ischecked = !info.ischecked;
        }
        //更新界面操作
        myadapter.notifyDataSetChanged();
    }

    //清理进程
    public void clearn(View v){
        ActivityManager am= (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //am.killBackgroundProcesses(packageName);//根据应用程序的包名，清除应用程序的进程
        /**保存删除的bean类**/
        List<ProcessInfo> deleteInfos = new ArrayList<>();
        //用户集合
        for (ProcessInfo info : userProcessInfos) {
            if (info.ischecked) {
                am.killBackgroundProcesses(info.packageName);
                //当系统清理完数据之后，还有更新界面的数据，因为界面的数据使用两个集合中数据的，所以更新集合中的数据
                //集合还在运行使用中，不能修改集合中的数据
                //userProcessInfos.remove(info);
                deleteInfos.add(info);
            }
        }
        //系统集合
        for (ProcessInfo info : systemProcessInfos) {
            if (info.ischecked) {
                am.killBackgroundProcesses(info.packageName);
                deleteInfos.add(info);
            }
        }

        //遍历删除集合的数据，根据数据的是否是系统进程标示，分别从用户集合和系统集合中删除数据
        long deletememory=0;
        for (ProcessInfo processInfo : deleteInfos) {
            if (processInfo.isSystem) {
                systemProcessInfos.remove(processInfo);
            }else{
                userProcessInfos.remove(processInfo);
            }
            deletememory+=processInfo.size;
        }

        // 显示toast提醒用户，清理多少进程，释放多少内存
        Toast.makeText(getApplicationContext(), "清理" + deleteInfos.size() + "个进程，释放" + Formatter.formatFileSize(getApplicationContext(), deletememory) + "内存", Toast.LENGTH_SHORT).show();

        //因为android系统中有些进程是清理不掉，但是在界面中要给用户的感觉是已经清理了，所以，要手动更改正在运行的进程的数量，不能重新获取
        //setProcessCount();
        //重新获取正在运行的进程的数量
        runningProcessCount = runningProcessCount-deleteInfos.size();
        //重新设置显示进度
        int progress = (int) (runningProcessCount * 100f / allProcessCount + 0.5f);
        mCount.setLeft("正在运行"+runningProcessCount+"个");
        mCount.setProgress(progress);
        setMemory();

        //更新界面
        myadapter.notifyDataSetChanged();
    }








}