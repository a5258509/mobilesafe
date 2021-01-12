package com.asao.mobilesafe.bean;

public class BlackNumberInfo {
    public String blacknumber;
    public int mode;

    public BlackNumberInfo(String blacknumber, int mode) {
        this.blacknumber = blacknumber;
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "BlackNumberInfo{" +
                "blacknumber='" + blacknumber + '\'' +
                ", mode=" + mode +
                '}';
    }
}
