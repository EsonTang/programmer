package com.prize.appcenter.bean;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;

/**
 * @创建者 聂礼刚
 * @创建者 2017/3/31
 * @描述
 */
public class GameCommentBean implements Serializable {
    private static final long serialVersionUID = -5072201829826412149L;
    public String appId;
    public String comment;
    public String model;
    public String updateTime;
    public AppsItemBean app;
}
