package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;

/**
 * @创建者 聂礼刚
 * @创建者 2017/3/31
 * @描述
 */
public class WelfareBean implements Serializable {
    private static final long serialVersionUID = -5072201829826412149L;
    public String appId;
    public String imageUrl;
    public String type;
    public String title;
    public String points;
    public String giftText;
    public String updateTime;
    public AppsItemBean app;
    public int weight=0;//共有八种权重状态

    @Override
    public boolean equals(Object o) {
        if(o instanceof WelfareBean){
            WelfareBean st=(WelfareBean) o;
            return (appId.equals(st.appId));
        }else{
            return super.equals(o);
        }
    }

    @Override
    public String toString() {
        return "WelfareBean{" +
                "title='" + title + '\'' +
                ", weight=" + weight +
                '}';
    }
}
