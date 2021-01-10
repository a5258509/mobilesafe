package com.asao.mobilesafe.db;

/**
 * 黑名单数据库的公共接口
 * */
public class BlackNumberConstants {

    //数据库名称
    public static final String DB_NAME="blacknumber.db";

    //数据库版本
    public static final int DB_VERSION=1;

    // 为了方便后期修改表的名称和字段的名称，把表名和字段名也抽取出来
    /** 表名 **/
    public static final String TABLE_NAME = "info";
    /** _id字段的名称 **/
    public static final String ID = "_id";
    /** 号码的字段的名称 **/
    public static final String BLACKNUMBER = "blacknumber";
    /** 类型的字段的名称 **/
    public static final String MODE = "mode";

    //创建表的sql语句
    public static final String DB_SQL = "create table " + TABLE_NAME + "(" + ID
            + " integer primary key autoincrement," + BLACKNUMBER
            + " varchar(20)," + MODE + " varchar(2))";

}
