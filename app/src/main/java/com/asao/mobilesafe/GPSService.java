package com.asao.mobilesafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.security.Provider;

public class GPSService extends Service {

    private LocationManager locationManager;
    private MyLocationListener listener;
    private MyBinder my;
    private double longitude;
    private float speed;
    private double latitude;
    private float accuracy;
    private double altitude;
    private String Provider;


    @Override
    public IBinder onBind(Intent intent) {
        my = new MyBinder();
        return my;
    }

    public class MyBinder extends Binder {

        public double getLongitude(){
            return longitude;
        }
        public double getLatitude(){
            return latitude;
        }
        public double getAltitude(){
            return altitude;
        }
        public float getAccuracy(){
            return accuracy;
        }
        public float getSpeed(){
            return speed;
        }




//        double longitude;
//        double latitude;

//        public double getLongitude() {
//            return longitude;
//        }
//
//        public void setLongitude(double longitude) {
//            this.longitude = longitude;
//        }
//
//        public double getLatitude() {
//            return latitude;
//        }
//
//        public void setLatitude(double latitude) {
//            this.latitude = latitude;
//        }


    }





    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        //定位操作
        //1.获取位置的管理者
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 2.获取 Wifi 管理器 WifiManager,通过wifimanager获取获取 Wifi 状态,通过wifi状态判断使用哪种定位方式
        WifiManager wifiManagerer = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        boolean wifiEnabled = wifiManagerer.isWifiEnabled();
        //Log.d("main1", String.valueOf(wifiEnabled));
        if(wifiEnabled){
            Provider="network";
        }else{
            Provider="gps";
        }

        //3.定位实现
        listener = new MyLocationListener();

//        由系统根据用户手机不同情况选择合适的定位方式,是gps还是网络定位(不好用)
//        Criteria criteria=new Criteria();
//         获得最好的定位效果
//        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(false);
//        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//        String bestProvider = locationManager.getBestProvider(criteria, true);
//        Log.d("main1",bestProvider);

        //参数1：定位
        //参数2：最小的定位的间隔时间
        //参数3：最小的定位的间隔距离
        //参数4：定位的回调监听
        locationManager.requestLocationUpdates(Provider, 0, 0, listener);
    }












    /**定位的回调函数**/
    private class MyLocationListener implements LocationListener {
        //当定位位置和定位时间改变的时候调用
        //location : 定位位置
        @Override
        public void onLocationChanged(Location location) {
            Log.d("main1", "onLocationChanged");


            //获取定位的速度
            speed = location.getSpeed();
            long time=location.getTime();//定位的时间
            //获取定位的纬度
            latitude = location.getLatitude();
            //获取定位的经度
            longitude = location.getLongitude();
            //获取定位的精确度
            accuracy = location.getAccuracy();
            //获取定位的海拔
            altitude = location.getAltitude();
            Log.d("main1", "经度："+String.valueOf(longitude));
            Log.d("main1", "纬度："+String.valueOf(latitude));
//            Log.d("main1", "速度："+String.valueOf(speed));
//            Log.d("main1", "时间："+String.valueOf(time));
//            Log.d("main1", "精确度："+String.valueOf(accuracy));
//            Log.d("main1", "海拔："+String.valueOf(altitude));
//            my.setLongitude(longitude);
//            my.setLatitude(latitude);
            //根据经纬度坐标获取实际地理位置
        }

        //定位状态改变的时候调用的方法
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("main1", "onStatusChanged");

        }
        //定位方法可用调用的方法
        @Override
        public void onProviderEnabled(String provider) {
            Log.d("main1", "onProviderEnabled");

        }
        //定位方式不可用调用的方法
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("main1", "onProviderDisabled");

        }

    }

    @Override
    public void onDestroy() {
        //3.当activity退出的时候，取消定位操作
        locationManager.removeUpdates(listener);//取消定位，并不会将手机中的GPS关闭，android高版本中不允许程序员通过代码开启和关闭GPS
        super.onDestroy();
    }

}


