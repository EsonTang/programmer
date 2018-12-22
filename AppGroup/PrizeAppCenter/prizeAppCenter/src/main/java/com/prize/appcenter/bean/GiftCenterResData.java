package com.prize.appcenter.bean;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/9/7.13:59
 * @描述
 */
public class GiftCenterResData implements Serializable {

    private static final long serialVersionUID = -6254919865152673167L;
    public HomeAdBean banner;
    public ArrayList<AppsItemBean> apps;
}
