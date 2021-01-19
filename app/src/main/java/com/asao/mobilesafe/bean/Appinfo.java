package com.asao.mobilesafe.bean;

import android.graphics.drawable.Drawable;

public class Appinfo {

    public String packageName;
    public String name;
    public Drawable icon;
    public long size;
    public boolean isSystem;

    public Appinfo(String packageName, String name, Drawable icon, long size, boolean isSystem) {
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.size = size;
        this.isSystem = isSystem;
    }
}
