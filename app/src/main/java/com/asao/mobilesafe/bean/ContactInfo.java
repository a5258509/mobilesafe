package com.asao.mobilesafe.bean;

//联系人的bean类
public class ContactInfo {
    public String name;
    public String number;
    public int id;

    public ContactInfo(String name, String number, int id) {
        super();
        this.name = name;
        this.number = number;
        this.id = id;
    }
}
