package com.asao.mobilesafe.db.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.asao.mobilesafe.bean.BlackNumberInfo;
import com.asao.mobilesafe.db.AppLockConstants;
import com.asao.mobilesafe.db.AppLockOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单数据库操作
 * */
public class AppLockDao {

    private final AppLockOpenHelper appLockOpenHelper;
    private Context mcontext;

    public AppLockDao(Context context){
        appLockOpenHelper = new AppLockOpenHelper(context);
        this.mcontext=context;
    }

    //增删改查
    /**
     *添加数据

     * @return*/
    //加锁
    public boolean add(String packagename){
         //同步锁
        //synchronized (AppLockDao.class){
            SQLiteDatabase database = appLockOpenHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            //values.put方法 key对应的是列名(数据库表中的字段名),value对应的是要插入到这一列的具体数据
            values.put(AppLockConstants.PACKAGENAME,packagename);
            long insert = database.insert(AppLockConstants.TABLE_NAME, null, values);
            database.close();

        //当数据添加成功的时候，发送一个数据更新的消息，告诉内容观察者，这样内容观察者就可以观察到数据的变化，实现更新数据库操作
        ContentResolver contentResolver=mcontext.getContentResolver();
        Uri uri = Uri.parse("content://com.asao.mobilesafe.UPDATESQLITE");
        //uri : uri地址
        //observer : 通知哪个内容观察者，如果是null，表示通知所有通过uri地址注册的内容观察者
        contentResolver.notifyChange(uri, null);//通知内容观察者数据发生变化了

            //判断是否添加成功
            return insert !=-1;
        //}

    }

    //删除
    //解锁
    public boolean delete(String packagename){
        SQLiteDatabase database = appLockOpenHelper.getWritableDatabase();
        String whereClause=AppLockConstants.PACKAGENAME+"=?";//查询条件
        String[] whereArgs={packagename};//查询条件的参数
        int delete = database.delete(AppLockConstants.TABLE_NAME, whereClause, whereArgs);
        database.close();

        //当数据删除成功的时候，也发送一个数据更新的消息，告诉内容观察者，这样内容观察者就可以观察到数据的变化，实现更新数据库操作
        ContentResolver contentResolver=mcontext.getContentResolver();
        Uri uri = Uri.parse("content://com.asao.mobilesafe.UPDATESQLITE");
        //uri : uri地址
        //observer : 通知哪个内容观察者，如果是null，表示通知所有通过uri地址注册的内容观察者
        contentResolver.notifyChange(uri, null);//通知内容观察者数据发生变化了

        return delete !=0;
    }



    //查询单个数据
    //判断程序是否加锁,判断数据库中是否有相应的包名
    public Boolean queryIsLock(String packagename) {

        boolean isHave = false;// 数据库是否存在数据

        SQLiteDatabase database = appLockOpenHelper.getReadableDatabase();
        // columns :设置查询哪一列的数据
        // selection : 查询条件
        // selectionArgs : 查询条件的参数
        // groupBy : 分组
        // having : 去重
        // orderBy : 排序
        Cursor cursor = database.query(AppLockConstants.TABLE_NAME,
                new String[] { AppLockConstants.PACKAGENAME },
                AppLockConstants.PACKAGENAME + "=?",
                new String[] { packagename }, null, null, null);
        // 因为是根据黑名单号码查询拦截类型，一个黑名单号码对应的是一个拦截类型
        if (cursor.moveToNext()) {
            isHave=true;
        }
        // 关闭
        cursor.close();
        database.close();
        return isHave;
    }

    //查询全部数据
    public List<String> queryAll(){


         List<String> list =new ArrayList<>();

        SQLiteDatabase database = appLockOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(AppLockConstants.TABLE_NAME, new String[]{AppLockConstants.PACKAGENAME}, null, null, null, null, null);
        while (cursor.moveToNext()){
            String packagename = cursor.getString(0);


            //因为只有一个数据packagename,所以不用再将其存放到bean类中了,直接放到listview显示
            list.add(packagename);
        }
        //关闭数据库
        cursor.close();
        database.close();
        return list;
    }


















}
