package com.asao.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import com.asao.mobilesafe.bean.BlackNumberInfo;
import com.asao.mobilesafe.db.BlackNumberConstants;
import com.asao.mobilesafe.db.BlackNumberOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单数据库操作
 * */
public class BlackNumberDao {

    private final BlackNumberOpenHelper blackNumberOpenHelper;

    public BlackNumberDao(Context context){
        blackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }

    //增删改查
    /**
     *添加数据
     * blacknumber:号码
     * mode:拦截类型
     *
     * @return*/
    public boolean add(String blacknumber, int mode){
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        //values.put方法 key对应的是列名(数据库表中的字段名),value对应的是要插入到这一列的具体数据
        values.put(BlackNumberConstants.BLACKNUMBER,blacknumber);
        values.put(BlackNumberConstants.MODE,mode);
        long insert = database.insert(BlackNumberConstants.TABLE_NAME, null, values);
        database.close();
        //判断是否添加成功
        return insert !=-1;
    }

    //删除
    public boolean delete(String blackNumber){
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        String whereClause=BlackNumberConstants.BLACKNUMBER+"=?";//查询条件
        String[] whereArgs={blackNumber};//查询条件的参数
        int delete = database.delete(BlackNumberConstants.TABLE_NAME, whereClause, whereArgs);
        database.close();
        return delete !=0;
    }

    //更新
    public boolean update(String blacknumber, int mode){
        SQLiteDatabase database = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values=new ContentValues();//要更新的数据
        values.put(BlackNumberConstants.MODE,mode);
        String whereClause=BlackNumberConstants.BLACKNUMBER+"=?";//更新条件
        String[] whereArgs={blacknumber};//更新条件的参数
        int update = database.update(BlackNumberConstants.TABLE_NAME, values, whereClause, whereArgs);
        //如果update为0,则表示更新失败
        database.close();
        return update!=0;
    }

    //查询单个数据
    public int queryMode(String blackNumber) {

        int mode = -1;// 设置初始的默认值

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();
        // columns :设置查询哪一列的数据
        // selection : 查询条件
        // selectionArgs : 查询条件的参数
        // groupBy : 分组
        // having : 去重
        // orderBy : 排序
        Cursor cursor = database.query(BlackNumberConstants.TABLE_NAME,
                new String[] { BlackNumberConstants.MODE },
                BlackNumberConstants.BLACKNUMBER + "=?",
                new String[] { blackNumber }, null, null, null);
        // 因为是根据黑名单号码查询拦截类型，一个黑名单号码对应的是一个拦截类型
        if (cursor.moveToNext()) {
            mode = cursor.getInt(0);
        }
        // 关闭
        cursor.close();
        database.close();
        return mode;
    }

    //查询全部数据
    public List<BlackNumberInfo> queryAll(){

        //SystemClock.sleep(2000);

         List<BlackNumberInfo> list =new ArrayList<>();

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(BlackNumberConstants.TABLE_NAME, new String[]{BlackNumberConstants.BLACKNUMBER, BlackNumberConstants.MODE}, null, null, null, null, "_id desc");//根据id降序查询
        while (cursor.moveToNext()){
            String blacknumber = cursor.getString(0);
            int mode = cursor.getInt(1);

            // 将黑名单号码和拦截类型保存到bean类中，方便后面使用
            BlackNumberInfo blackNumberInfo=new BlackNumberInfo(blacknumber,mode);

            //将bean类存放到集合中,方便listview显示
            list.add(blackNumberInfo);
        }
        //关闭数据库
        cursor.close();
        database.close();

        return list;
    }


    //查询部分数据(分页)
    public List<BlackNumberInfo> queryPartAll(int maxNum,int startIndex){

        //SystemClock.sleep(2000);

        List<BlackNumberInfo> list =new ArrayList<>();

        SQLiteDatabase database = blackNumberOpenHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select blacknumber,mode from info order by _id desc limit ? offset ?", new String[]{maxNum + "", startIndex + ""});
        while (cursor.moveToNext()){
            String blacknumber = cursor.getString(0);
            int mode = cursor.getInt(1);

            // 将黑名单号码和拦截类型保存到bean类中，方便后面使用
            BlackNumberInfo blackNumberInfo=new BlackNumberInfo(blacknumber,mode);

            //将bean类存放到集合中,方便listview显示
            list.add(blackNumberInfo);
        }
        //关闭数据库
        cursor.close();
        database.close();

        return list;
    }
















}
