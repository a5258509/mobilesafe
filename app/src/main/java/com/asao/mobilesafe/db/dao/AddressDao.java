package com.asao.mobilesafe.db.dao;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

//号码归属地查询操作
public class AddressDao {

    private static Cursor cursor;

    public static String getAddress(Context context, String number){
        //查询数据库
        File file = new File(context.getFilesDir(), "address.db");
        String location="";//号码归属地
        // 1.打开数据库
        // 打开已有的数据
        // 参数1：数据库的路径
        // 参数2：游标工厂
        // 参数3：操作的权限
        // getAbsolutePath() : 获取文件的绝对路径
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        //2.查询数据库
        // substring : 截取字符串,左闭右开,包含头不包含尾
        if(number.length()<7){
            location="公共电话";
            return location;
        }else {
            cursor = database.rawQuery("select location from data2 where id=(select outkey from data1 where id=?)", new String[]{number.substring(0, 7)});
        }
        //3.解析cursor,获取数据
        if(cursor.moveToNext()){
            location = cursor.getString(0);
        }
        //4.关闭数据库
        cursor.close();
        database.close();
        return location;
    }











}
