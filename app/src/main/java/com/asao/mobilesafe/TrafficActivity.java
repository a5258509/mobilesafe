package com.asao.mobilesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.asao.mobilesafe.bean.Appinfo;
import com.asao.mobilesafe.engine.AppEngine;
import com.asao.mobilesafe.utils.NetworkStatsHelper;

import java.util.List;

public class TrafficActivity extends AppCompatActivity {

    private ListView mListView;
    private List<Appinfo> list;
    private NetworkStatsManager networkStatsManager;
    private LinearLayout mLLLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);

        initView();
    }

    private void initView() {
        mListView = findViewById(R.id.traffic_lv_listview);
        mLLLoading = findViewById(R.id.appmanager_ll_loading);

        initData();
    }

    private void initData() {
        //获取系统中所有应用程序的信息
        new MyAsyncTaks().execute();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
        }


//

    }


    public static long getTimesMonthmorning() {
        Calendar cal = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            return cal.getTimeInMillis();
        }else{
            return 0;
        }
    }


    private class MyAsyncTaks extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            mLLLoading.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            list = AppEngine.getAllAppInfos(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mLLLoading.setVisibility(View.GONE);
            mListView.setAdapter(new Myadapter());
            super.onPostExecute(aVoid);
        }
    }


    private class Myadapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrafficActivity.ViewHolder viewHolder;
            if(convertView==null){
                convertView=View.inflate(getApplicationContext(),R.layout.traffic_listview_item,null);
                viewHolder=new TrafficActivity.ViewHolder();
                viewHolder.mIcon=convertView.findViewById(R.id.item_iv_icon);
                viewHolder.mName=convertView.findViewById(R.id.item_tv_name);
                viewHolder.mUp=convertView.findViewById(R.id.item_tv_up);
                viewHolder.mDown=convertView.findViewById(R.id.item_tv_down);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (TrafficActivity.ViewHolder) convertView.getTag();
            }
            //获取数据,展示数据
            Appinfo appinfo = list.get(position);
            viewHolder.mIcon.setImageDrawable(appinfo.icon);
            viewHolder.mName.setText(appinfo.name);

//            long uidTxBytes = TrafficStats.getUidTxBytes(appinfo.uid);//根据应用的uid获取应用的上传的流量
//            viewHolder.mUp.setText("上传:"+ Formatter.formatFileSize(getApplicationContext(),uidTxBytes));
//
//            long uidRxBytes = TrafficStats.getUidRxBytes(appinfo.uid);//根据应用的uid获取应用的下载的流量
//            viewHolder.mDown.setText("下载:"+Formatter.formatFileSize(getApplicationContext(),uidRxBytes));


            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, appinfo.uid);
            long packageRxBytesMobile = networkStatsHelper.getPackageRxBytesMobile(getApplicationContext());
            viewHolder.mDown.setText("下载:"+Formatter.formatFileSize(getApplicationContext(),packageRxBytesMobile));
            long packageTxBytesMobile = networkStatsHelper.getPackageTxBytesMobile(getApplicationContext());
            viewHolder.mUp.setText("上传:"+ Formatter.formatFileSize(getApplicationContext(),packageTxBytesMobile));


            return convertView;
        }
    }


    static class ViewHolder{
        TextView mName,mUp,mDown;
        ImageView mIcon;
    }


}