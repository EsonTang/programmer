package com.android.lpserver.bean;

/**
 * Created by prize on 2016/11/11.
 */
public class Recorder {
    private String sender;
    private String date;
    private String getMoney;

    public Recorder(String sender, String getMoney, String date) {
        this.sender = sender;
        this.getMoney = getMoney;
        this.date = date;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGetMoney() {
        return getMoney;
    }

    public void setGetMoney(String getMoney) {
        this.getMoney = getMoney;
    }
}
