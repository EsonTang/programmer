package com.prize.smartcleaner.bean;

import java.util.ArrayList;

/**
 * Created by xiarui on 2018/1/17.
 */

public class ServiceInfo {
    public String pkg;
    public int killType;
    public ArrayList<String> serviceList;

    public ServiceInfo(String pkg, ArrayList<String> serviceList, int killType) {
        this.pkg = pkg;
        this.serviceList = serviceList;
        this.killType = killType;
    }
}
