package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建者 longbaoxiu
 * @创建者 2017/3/29.17:51
 * @描述
 */

public class RankOverViewBean implements Serializable {
    /**榜单类型，用于请求该榜单详情*/
    public String rankType;
    /**榜单名字*/
    public String rankName;

    public List<AppsItemBean> apps = new ArrayList<>();
}
