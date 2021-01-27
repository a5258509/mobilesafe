package com.asao.mobilesafe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.asao.mobilesafe.bean.Appinfo;
import com.asao.mobilesafe.engine.AppEngine;
import com.asao.mobilesafe.view.CustomProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private CustomProgressBar mMemory;
    private CustomProgressBar mSD;
    private ListView mListView;
    private List<Appinfo> allAppInfos;

    //用户程序的集合
    private List<Appinfo> userAppInfos;
    //系统程序的集合
    private List<Appinfo> systemAppInfos;
    private TextView mCount;
    //被点击条目对应的应用程序信息
    private Appinfo appinfo;
    private PopupWindow window;
    private LinearLayout mLLLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        initView();

        //设置显示存储空间信息
        setMemoryMsg();

        initData();


    }



    private void initView() {
        mMemory = findViewById(R.id.app_cpb_memory);
        mListView = findViewById(R.id.app_lv_apps);
        mCount = findViewById(R.id.app_tv_count);
        mLLLoading = findViewById(R.id.appmanager_ll_loading);

        //耗时操作,加载应用程序数据之前，显示进度条
        mLLLoading.setVisibility(View.VISIBLE);

        //设置listview的滚动监听事件
        setOnListViewOnScrollListener();
        //设置listview条目点击事件,显示气泡弹窗
        setListViewOnItemListener();

    }


    private void setOnListViewOnScrollListener() {
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            //当listview的滚动状态改变的时候调用的方法
            //scrollState : listview的滚动状态
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            //firstVisibleItem : 界面显示的第一个条目
            //visibleItemCount : 可见条目的总数
            //totalItemCount : listview的总条目数据
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(userAppInfos!=null&&systemAppInfos!=null){

                    //listview滚动隐藏气泡
                    hidepopupwindow();
                    //判断当前界面显示的第一个条目是否大于或者等于用户程序个数的条目的索引
                    if(firstVisibleItem>=userAppInfos.size()+1){
                        mCount.setText("系统程序("+systemAppInfos.size()+")");
                    }else{
                        mCount.setText("用户程序("+userAppInfos.size()+")");
                    }
                }
            }
        });
    }


    private void setListViewOnItemListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {



            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //显示气泡
                //1.屏蔽textview条目的点击事件
                if(position==0||position==userAppInfos.size()+1){
                    return;
                }
                //2.获取被点击条目应用程序信息,包括卸载,打开,分享,应用信息等操作
                if(position<=userAppInfos.size()){
                    //用户集合中获取数据
                    appinfo=userAppInfos.get(position-1);
                }else{
                    //系统集合中获取数据
                    appinfo=systemAppInfos.get(position-userAppInfos.size()-2);
                }


                //3.显示气泡弹窗
                //判断气泡是否显示，如果显示，取消气泡显示，重新再新的条目显示气泡
                hidepopupwindow();
                //参数1:用在弹窗中的View样式
                //参数2,3:弹窗的宽高
                //参数4(focusable):能否获取焦点
                View contentView=View.inflate(getApplicationContext(),R.layout.popupwindow_item,null);
                
                //初始化控件,设置点击事件
                contentView.findViewById(R.id.pop_ll_uninstall).setOnClickListener(AppManagerActivity.this);
                contentView.findViewById(R.id.pop_ll_info).setOnClickListener(AppManagerActivity.this);
                contentView.findViewById(R.id.pop_ll_share).setOnClickListener(AppManagerActivity.this);
                contentView.findViewById(R.id.pop_ll_open).setOnClickListener(AppManagerActivity.this);


                window = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
                //设置popupwindow样式动画
                window.setAnimationStyle(R.style.AddressStyleAnimation);
                //参数1:anchor:锚(依附于哪个view) 将气泡显示在哪个控件的下方
                //参数2,3:相对于锚在x,y方向的偏移量
                //getHeight获取view条目的高度
                window.showAsDropDown(view,(view.getWidth())/2,-view.getHeight());

            }
        });


    }





    private void setMemoryMsg() {
        //1.闪存
        File dataDirectory = Environment.getDataDirectory();//获取闪存存储空间的文件
        long totalSpace = dataDirectory.getTotalSpace();//获取总的空间大小
        long freeSpace = dataDirectory.getFreeSpace();//获取可用的空间大小
        //已用空间=总空间-可用空间
        long useSpace=totalSpace-freeSpace;
        //展示空间信息
        mMemory.setText("存储空间:");
        //要将空间大小进行换算
        String userSize = Formatter.formatFileSize(this, useSpace);
        String freeSize = Formatter.formatFileSize(this, freeSpace);
        mMemory.setLeft("已用:"+userSize);
        mMemory.setRight("可用:"+freeSize);
        //设置进度,已用空间占用总空间的比例
        int progress= (int) (useSpace*100f/totalSpace+0.5f);
        mMemory.setProgress(progress);
        //2.SD卡
        //File storageDirectory = Environment.getExternalStorageDirectory();//获取SD卡存储空间的文件
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                allAppInfos = AppEngine.getAllAppInfos(getApplicationContext());
                userAppInfos=new ArrayList<>();
                systemAppInfos=new ArrayList<>();
                //遍历集合,将用户程序放到用户集合中,系统程序放到系统集合中
                for (Appinfo info:allAppInfos){
                    //系统应用
                    if(info.isSystem){
                        systemAppInfos.add(info);
                    }else{
                        //用户应用
                        userAppInfos.add(info);
                    }
                }



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //在加载完数据,隐藏进度条
                        mLLLoading.setVisibility(View.GONE);
                        //设置textview显示用户程序的个数
                        mCount.setText("用户程序("+userAppInfos.size()+")");
                        mListView.setAdapter(new Myadapter());
                    }
                });
            }
        }.start();
    }



    private class Myadapter extends BaseAdapter{



        @Override
        public int getCount() {
            //2.因为listview显示的数据要从两个集合中获取，所有listview的条目数应该是两个集合中的长度的总和
            //因为多了两个textview条目，所以总条目数要+2
            return userAppInfos.size()+systemAppInfos.size()+2;
        }
        //position是listview的角标
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
           // 4.设置条目显示的样式
            //获取条目显示样式的标示
            int itemViewType = getItemViewType(position);
            if(itemViewType==0){
                //使用程序个数的样式
                if(position==0){
                    //用户程序个数
                    TextView textView=new TextView(getApplicationContext());
                    //textView.setText("用户程序("+userAppInfos.size()+")");
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(0x99429ED6);
                    //textView.setTextSize(17);
                    //textView.setPadding(6,6,6,6);
                    return textView;
                }else{
                    //系统程序个数
                    TextView textView=new TextView(getApplicationContext());
                    textView.setText("系统程序("+systemAppInfos.size()+")");
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(0x99429ED6);
                    textView.setTextSize(17);
                    textView.setPadding(6,6,6,6);
                    return textView;
                }
            }else if(itemViewType==1){
                //使用条目的样式
                ViewHolder viewHolder;
                if(convertView==null){
                    view = View.inflate(getApplicationContext(), R.layout.app_listview_item, null);
                    viewHolder=new ViewHolder();
                    viewHolder.mIcon=view.findViewById(R.id.item_iv_icon);
                    viewHolder.mName=view.findViewById(R.id.item_tv_name);
                    viewHolder.mSize=view.findViewById(R.id.item_tv_size);
                    viewHolder.misSD=view.findViewById(R.id.item_tv_issd);
                    view.setTag(viewHolder);
                }else{
                    view=convertView;
                    viewHolder= (ViewHolder) view.getTag();
                }
                //获取数据展示数据
                Appinfo appinfo;
                //要从两个集合中获取数据
                if(position<=userAppInfos.size()){
                    //用户集合中获取数据
                    appinfo=userAppInfos.get(position-1);
                }else{
                    //系统集合中获取数据
                    appinfo=systemAppInfos.get(position-userAppInfos.size()-2);
                }
                viewHolder.mName.setText(appinfo.name);
                viewHolder.mIcon.setImageDrawable(appinfo.icon);
                String size = Formatter.formatFileSize(getApplicationContext(), appinfo.size);
                viewHolder.mSize.setText(size);
                viewHolder.misSD.setText("内部存储");
            }
            return view;
        }

        @Override
        public Object getItem(int position) {
            return allAppInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //3.设置显示两种样式，并设置条目应该显示的样式
        //3.1.提供给系统调用的，设置listview有几种条目样式，返回几就表示有几种样式
        @Override
        public int getViewTypeCount() {
            return 2;
        }
        //不知道什么条目显示什么样式
        //3.2.提供给getview调用的，根据条目的索引设置条目应该显示什么样式
        //position : 条目的索引
        @Override
        public int getItemViewType(int position) {
            if(position==0||position==userAppInfos.size()+1){
                //使用程序个数的样式
                return 0;
            }else{
                //使用条目的样式
                return 1;
            }
        }
    }





    static class ViewHolder{
        TextView mName,misSD,mSize;
        ImageView mIcon;
    }



    //隐藏popupwindow
    private void hidepopupwindow() {
        if (window != null) {
            window.dismiss();
            window = null;
        }
    }


    //气泡弹窗中的点击事件
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pop_ll_uninstall:
                //卸载
                uninstall();
                break;
            case R.id.pop_ll_open:
                //打开
                open();
                break;
            case R.id.pop_ll_share:
                //分享
                share();
                break;
            case R.id.pop_ll_info:
                //信息
                info();
                break;
        }
    }




    //卸载apk,安卓api28后需要REQUEST_DELETE_PACKAGES权限

    private void uninstall() {
//        if(!appinfo.isSystem){
//
//        }

        Intent intent=new Intent();
        intent.setAction("android.intent.action.DELETE");
       // intent.setAction("android.intent.action.PACKAGE_REMOVED");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:"+appinfo.packageName));
        //startActivity(intent);
        //因为从系统的卸载界面回到我们的界面的时候，还需要刷新界面，将已经卸载的应用程序的信息给删除
        startActivityForResult(intent,0);
    }
    private void open() {
        PackageManager pm=getPackageManager();
        //获取应用程序启动意图
        Intent intent = pm.getLaunchIntentForPackage(appinfo.packageName);
        if(intent!=null)
        startActivity(intent);
    }

    //提取apk
    private void share() {
        String path = appinfo.sourceDir1;
        String packageName = appinfo.packageName;
        File formFile=new File(path);
        if (!formFile.exists()) {
            Toast.makeText(this, "app路径不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        String appBackupPath=Environment.getExternalStorageDirectory()+"/asao/mobilesafe";
        String toFileName=packageName+"_"+".apk";
        File toFilePath=new File(appBackupPath);
        if (!toFilePath.exists()) {
            boolean result = toFilePath.mkdirs();
            if (!result) {
                Toast.makeText(this, "创建文件夹失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        File toFile = new File(toFilePath, toFileName);
        if (toFile.exists()) {
            boolean result = toFile.delete();
            if (!result) {
                Toast.makeText(this, "删除文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            boolean result = toFile.createNewFile();
            if (!result) {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!toFile.exists()) {
            Toast.makeText(this, "目标文件生成失败", Toast.LENGTH_SHORT).show();
            return;
        }
        javaNioTransfer(formFile, toFile);
        Toast.makeText(this, "Apk已保存到\n" + toFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    private void javaNioTransfer(File source, File target) {

        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(in.position(), in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                in.close();
                outStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //信息
    private void info() {
        //通过随便打开一个应用信息,去找到系统查看信息的intent是什么,然后在logcat中输入act=就能过滤出系统调用了哪个intent
        //START u0 {act=android.settings.APPLICATION_DETAILS_SETTINGS
        // dat=package:tv.danmaku.bili flg=0x10008000
        // cmp=com.android.settings/.applications.InstalledAppDetails} from uid 10036
        Intent intent=new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:"+appinfo.packageName));
        startActivity(intent);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //刷新界面
        initData();
        super.onActivityResult(requestCode, resultCode, data);
    }



}