package com.prize.appcenter.bean;

import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  longbaoxiu
 * 2016/9/7.13:59
 *
 */
public class NetTypeBean implements Serializable {

    public AppsItemBean mAppItemBean;
    public ArrayList<AppHeadCategories> typeList;
    public String type;
    private NetTypeBean() {
    }

    public NetTypeBean(AppsItemBean mAppItemBean, ArrayList<AppHeadCategories> typeList, String type) {
        this.mAppItemBean = mAppItemBean;
        this.typeList = typeList;
        this.type = type;
    }




}
