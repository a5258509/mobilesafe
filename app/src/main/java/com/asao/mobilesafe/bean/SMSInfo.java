package com.asao.mobilesafe.bean;

//短信备份bean类
public class SMSInfo {

    public String address;
    public String date;
    public String type;
    public String body;

    public SMSInfo(String address, String date, String type, String body) {
        this.address = address;
        this.date = date;
        this.type = type;
        this.body = body;
    }
}
