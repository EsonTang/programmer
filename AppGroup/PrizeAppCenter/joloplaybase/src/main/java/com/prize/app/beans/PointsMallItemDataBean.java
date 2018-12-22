/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.app.beans;

import java.io.Serializable;

/**
 * Desc: 积分商城item详细信息
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/19 17:45
 */

public class PointsMallItemDataBean implements Serializable {
    /**
     * 用一句话描述这个变量表示什么
     */
    private  final long serialVersionUID = 1L;

    public int    id;
    public String title;
    public int    points;
    //banner图地址
    public String bannerUrl;
    public String iconUrl;
    public String introduction;
    public String description;
    public String rule;
    public int    type;
    public int    istatus;
    public String endDate;
    public String updateTime;
    public int    sn;
    public int    needPost;
//    public int    leftCount;
    public int    total;
    /**
     * 判断是否是秒杀商品 1：是
     */
    public int    saleFlag;
    /**
     * 秒杀后的所有兑换的积分
     */
    public int    salePoints;
    /**
     * 秒杀剩余时间
     */
    public String  saleTag;

    @Override
    public String toString() {
        return "PointsMallItemDataBean{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", points=" + points +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", introduction='" + introduction + '\'' +
                ", description='" + description + '\'' +
                ", rule='" + rule + '\'' +
                ", type=" + type +
                ", istatus=" + istatus +
                ", endDate='" + endDate + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", sn=" + sn +
                ", needPost=" + needPost +
                '}';
    }
}
