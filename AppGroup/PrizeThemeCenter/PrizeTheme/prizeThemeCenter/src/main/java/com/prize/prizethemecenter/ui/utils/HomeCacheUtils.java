package com.prize.prizethemecenter.ui.utils;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.bean.HomeAdBean;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.bean.table.AdTable;
import com.prize.prizethemecenter.bean.table.SearchHistoryTable;
import com.prize.prizethemecenter.bean.table.ThemeItemTable;
import com.prize.prizethemecenter.bean.table.TipsTable;

import org.xutils.ex.DbException;

/**
 * Created by pnegy on 2016/9/5.
 */
public  class HomeCacheUtils {

    /**
     * 装成单个主题表
     * @return
     */
    public static ThemeItemTable toThemeTable(ThemeItemBean b){
        ThemeItemBean bean = b;
        ThemeItemTable tt = new ThemeItemTable();
        tt.id = Integer.parseInt(b.id);
        tt.title = bean.name;
        tt.price = bean.price;
        tt.ad_pictrue = bean.ad_pictrue;
        return  tt;
    }


    /**
     * 转成广告表
     * @return
     */
    public static AdTable toADTable(HomeAdBean adBean , int i){
        HomeAdBean bean = adBean;
        AdTable a = new AdTable();
        a.id = i;
        a.imageurl = bean.imageUrl;
        a.bigimageurl = bean.bigImageUrl;
        return a;
    }


    /**
     * 转成提示表
     * @return
     */
    public static TipsTable toTipsTable(String tip , int i){
        TipsTable tipsTable = new TipsTable();
        tipsTable.id = i;
        tipsTable.tip = tip;
        return tipsTable;
    }
    /**
     * 转成实体类
     * @param ad
     * @return
     */
    public static HomeAdBean toAD(AdTable ad){
        HomeAdBean b = new HomeAdBean();
        b.bigImageUrl = ad.bigimageurl;
        b.imageUrl = ad.imageurl;
        return b;
    }

    /**
     * 转成实体类
     * @param ti
     * @return
     */
    public static ThemeItemBean toTheme(ThemeItemTable ti){
        ThemeItemBean b = new ThemeItemBean();
        b.id = String.valueOf(ti.id);
        b.name = ti.title;
        b.price = ti.price;
        b.ad_pictrue = ti.ad_pictrue;
        return b;
    }

    /**
     * 转成实体类
     * @param ti
     * @return
     */
    public static String toTips(TipsTable ti){
        return ti.tip;
    }

    public static void addToHistory(String text ,String type){
        SearchHistoryTable historyTB = new SearchHistoryTable();
        historyTB.word = text;
        historyTB.timestamp = java.lang.System.currentTimeMillis();
        historyTB.type = type;
        try {
            SearchHistoryTable s = MainApplication.getDbManager().selector(SearchHistoryTable.class).
                    where("word","==",historyTB.word).and("type","==",historyTB.type).findFirst();
            if(s!=null){
                MainApplication.getDbManager()
                        .delete(s);
                MainApplication.getDbManager().save(historyTB);
            }else{
                MainApplication.getDbManager().save(historyTB);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
