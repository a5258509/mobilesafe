package com.asao.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import androidx.annotation.Nullable;

import com.asao.mobilesafe.db.dao.BlackNumberDao;

import java.lang.reflect.Method;

public class BlackNumberService extends Service {

    private SmsReceiver smsReceiver;
    private BlackNumberDao blackNumberDao;
    private TelephonyManager tel;
    private MyPhoneStateListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收解析短信,获取发件人的拦截,进行拦截设置了
            //接收解析短信的操作
            Object[] objs = (Object[]) intent.getExtras().get("pdus");//获取到短信
            for (Object obj : objs) {
                //转化成短信对象
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                //获取发件人号码
                String sender = smsMessage.getOriginatingAddress();

                //根据号码查询发件人的拦截类型,是否拦截
                int mode = blackNumberDao.queryMode(sender);
                if(mode==1 || mode==2){
                    //拦截短信
                    abortBroadcast();
                }
            }

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        blackNumberDao = new BlackNumberDao(this);
        //1.短信拦截
        //判断发件人的拦截类型
        //代码注册广播接受者
        //1.1.广播接受者
        smsReceiver = new SmsReceiver();
        //1.2.设置接收的广播事件
        IntentFilter filter=new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");//设置接收的广播事件
        //1.3.注册广播接收者
        registerReceiver(smsReceiver,filter);


        //2.电话拦截
        //监听电话的状态
        //2.1.获取电话的管理者
        tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //2.2.监听电话的事件
        listener = new MyPhoneStateListener();
        //参数一listener : 回调监听
        //参数二events : 监听的事件
        tel.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);//监听电话状态
    }

    private class MyPhoneStateListener extends PhoneStateListener{
        //监听电话状态的方法
        //state :电话的状态
        //phoneNumber:来电号码
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE://空闲状态，挂断的状态

                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃状态
                    Log.d("main1","响铃"+phoneNumber);
                    //获取来电话的拦截类型，判断是否应该挂断电话
                    int mode = blackNumberDao.queryMode(phoneNumber);
                    if(mode==0||mode==2){
                        //挂断电话
                        Log.d("main1","1");
                        endCall();
                    }

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://通话状态，接听状态

                    break;
            }


        }
    }

    //挂断电话
    private void endCall() {
        //ITelephony.Stub.asInterface(TelephonyFrameworkInitializer.getTelephonyServiceManager().getTelephonyServiceRegisterer().get());
        //1.5版本
        //tel.endCall();
//        try {
//            //因为ServiceManager被系统隐藏了，所以我们不能使用，如果想使用，需要通过反射进行操作
//            //1.获取ServiceManager的.class文件的对象（字节码），根据类型的全类名，获取字节码文件
//            Class<?> loadClass = Class.forName("android.telephony.TelephonyFrameworkInitializer");
//            //2.从字节码文件中获取相应的方法
//            //参数1：方法名
//            //参数2：方法所需参数的类型的.class形式
//            Method method = loadClass.getDeclaredMethod("getTelephonyServiceManager");
//            Object o = method.invoke(null);
//
//            Class<?> loadClass1 = Class.forName("android.os.TelephonyServiceManager");
//            Method method1 = loadClass1.getDeclaredMethod("getTelephonyServiceRegisterer");
//
//
//
//            Method method2 = loadClass1.getDeclaredMethod("get");
//            IBinder iBinder2 = (IBinder) method2.invoke(null);
//
//
//
//
//
//            //3.执行获取到的方法
//            //参数1：方法所在类型的对象，如果方法是静态的方法，可以为null，如果方法不是静态的，一定要写方法所在类的对象
//            //参数2：方法执行所需的参数
//            //IBinder iBinder = (IBinder) method.invoke(null);
//
//            //4.将方法的返回结果设置给相应的方法，使用
//            //ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
//            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
//            //5.挂断电话了
//            iTelephony.endCall();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            Class<?> loadClass = Class.forName("android.telephony.TelephonyFrameworkInitializer");
//            Log.d("main1", String.valueOf(loadClass));
//
//            Method method = loadClass.getDeclaredMethod("getTelephonyServiceManager", (Class<?>[]) null);
//            Log.d("main1", String.valueOf(method));
//            Object o = method.invoke(null);
//            Log.d("main1", String.valueOf(o));
//            Method method1 = o.getClass().getMethod("getTelephonyServiceRegisterer", (Class<?>[]) null);
//            Object o1 = method1.invoke(o);
//            Method method2 = o1.getClass().getMethod("get", (Class<?>[]) null);
//            IBinder iBinder = (IBinder)method2.invoke(o1);
//            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
//            //5.挂断电话了
//            iTelephony.endCall();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 首先拿到TelephonyManager,.获取TelephonyManager的.class文件的对象（字节码），
//        Class<TelephonyManager> c = TelephonyManager.class;
//
//        // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
//        //Method mthEndCall = null;
//        try {
//            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//            //允许访问私有方法
//            mthEndCall.setAccessible(true);
//            //得到的obj 就是ITelephony对象
//            final Object obj = mthEndCall.invoke(tel, (Object[]) null);
//
//            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
//            Method mt = obj.getClass().getMethod("endCall");
//            //允许访问私有方法
//            mt.setAccessible(true);
//            mt.invoke(obj);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



//        Class<TelephonyManager> c = TelephonyManager.class;
//
//        // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
//        //Method mthEndCall = null;
//        try {
//            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//            //允许访问私有方法
//            mthEndCall.setAccessible(true);
//            //得到的obj 就是ITelephony对象
//            final Object obj = mthEndCall.invoke(tel, (Object[]) null);
//            Class<?> aClass = Class.forName(obj.getClass().getName());
//
//
//            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
//            Method mt = aClass.getMethod("endCall");
//            //允许访问私有方法
//            mt.setAccessible(true);
//            mt.invoke(obj);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        try {
            //因为ServiceManager被系统隐藏了，所以我们不能使用，如果想使用，需要通过反射进行操作
            //1.获取ServiceManager的.class文件的对象（字节码），根据类型的全类名，获取字节码文件
            Class<?> loadClass = Class.forName("android.os.ServiceManager");
            //2.从字节码文件中获取相应的方法
            //参数1：方法名
            //参数2：方法所需参数的类型的.class形式
            Method method = loadClass.getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            //3.执行获取到的方法
            //参数1：方法所在类型的对象，如果方法是静态的方法，可以为null，如果方法不是静态的，一定要写方法所在类的对象
            //参数2：方法执行所需的参数
            IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            //4.将方法的返回结果设置给相应的方法，使用
            //ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
            //5.挂断电话了
            iTelephony.endCall();
        }catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
//            getITelephonyMethod.setAccessible(true);
//            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(tel, (Object[]) null);
//            iTelephony.endCall();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



}


    @Override
    public void onDestroy() {
        super.onDestroy();
        //1.4.当服务退出,注销广播接收者
        unregisterReceiver(smsReceiver);
        //2.3.服务关闭，停止监听电话状态的操作
        tel.listen(listener, PhoneStateListener.LISTEN_NONE);//设置不在监听任何状态，停止监听操作
    }
}
