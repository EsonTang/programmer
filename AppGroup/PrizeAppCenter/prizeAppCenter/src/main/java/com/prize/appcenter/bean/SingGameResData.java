package com.prize.appcenter.bean;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  longbaoxiu
 * 2016/9/7.13:59
 *
 */
public class SingGameResData implements Serializable {

    public HomeAdBean banner;
    public ArrayList<SingleGamesBean> games;
    public ArrayList<AppsItemBean> onlineGames;
    public ArrayList<AppHeadCategories> typeList = new ArrayList<AppHeadCategories>();
    public static class SingleGamesBean {
        public String title;
        public String iconUrl;//桌面文件夹多出的字段
        public ArrayList<AppsItemBean> apps;


    }
}
