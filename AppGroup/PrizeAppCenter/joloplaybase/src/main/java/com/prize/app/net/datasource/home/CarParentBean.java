package com.prize.app.net.datasource.home;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;

import java.io.Serializable;

/**
 * longbaoxiu
 * 2016/10/12.14:55
 */

public class CarParentBean implements Serializable {
    public AppsItemBean mAppItemBean;
    public PrizeAppsCardData.FocusBean focus;
    public String type;


    public CarParentBean(String type, PrizeAppsCardData.FocusBean focus, AppsItemBean appItemBean) {
        this.type = type;
        this.focus = focus;
        this.mAppItemBean = appItemBean;
    }
}
