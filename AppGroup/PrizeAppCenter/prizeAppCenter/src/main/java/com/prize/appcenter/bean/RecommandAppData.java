package com.prize.appcenter.bean;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * 指定应用池应用推荐数据
 * @创建者 longbaoxiu
 * @创建者 2016/9/7.13:59
 * @描述
 */
public class RecommandAppData implements Serializable {

    /**
     * 第一个位置
     */
    public ArrayList<AppsItemBean> type1;
    /**
     * 第二个位置
     */
    public ArrayList<AppsItemBean> type2;
    /**
     * 第三个位置
     */
    public ArrayList<AppsItemBean> type3;
    /**
     * 第四个位置
     */
    public ArrayList<AppsItemBean> type4;

}
