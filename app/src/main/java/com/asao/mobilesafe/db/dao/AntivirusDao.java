package com.asao.mobilesafe.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class AntivirusDao {

    //查询app是否为病毒,md5:app的特征码md5值
    public static boolean isAntivirus(Context context,String md5){
        boolean isAntiVirus=false;
        File file = new File(context.getFilesDir(), "antivirus.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = database.query("datable", new String[]{"md5"}, "md5=?", new String[]{md5}, null, null, null);
        if(cursor.moveToNext()){
            isAntiVirus=true;
        }
        cursor.close();
        database.close();
        return isAntiVirus;

    }




}
