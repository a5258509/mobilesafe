package com.asao.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AppLockOpenHelper extends SQLiteOpenHelper {
    //设置数据库信息 创建数据库
    //name:数据库名称
    //factory:游标工厂
    //version:数据库的版本
    public AppLockOpenHelper(@Nullable Context context) {
        super(context, AppLockConstants.DB_NAME, null, AppLockConstants.DB_VERSION);
    }

    //创建表结构
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppLockConstants.DB_SQL);
    }
    //更新数据库调用的方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
