package com.prize.app.net.datasource.base;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Navbars;
import com.prize.app.beans.RecomandSearchWords;
import com.prize.app.net.AbstractNetData;

import java.util.ArrayList;

/**
 * *
 * 首页广告
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class AppData extends AbstractNetData {

    public String listTitle;
    public ArrayList<HomeAdBean> ads = new ArrayList<HomeAdBean>();
    public ArrayList<Navbars> navbars = new ArrayList<Navbars>();
//    public ArrayList<Navblocks> navblocks = new ArrayList<Navblocks>();
    public ArrayList<RecomandSearchWords> wordkvs = new ArrayList<RecomandSearchWords>();
    public ArrayList<String> words = new ArrayList<String>();
    /**常驻角标对象 2.2新增corner, 2.3版本改为corners**/
    public ArrayList<HomeAdBean> corners = new ArrayList<HomeAdBean>();
    public ArrayList<HomeAdBean> broadcasts = new ArrayList<HomeAdBean>();
    /**插屏广告 2.9新增**/
    public ArrayList<HomeAdBean> interstitial = new ArrayList<HomeAdBean>();
    /**应用页增加头部分类 3.0新增**/
    public ArrayList<AppHeadCategories> categorys = new ArrayList<AppHeadCategories>();
    public AppData() {
        ads = new ArrayList<HomeAdBean>();
    }

}
