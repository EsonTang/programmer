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

package com.prize.app.net.datasource.base;

import com.prize.app.beans.PointsMallItemDataBean;

import java.io.Serializable;

/**
 * Desc: 积分规则-兑换记录
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/18 10:26
 */

public class ConvertRecordsItemBean implements Serializable {

    private final long serialVersionUID = 1L;

    //"id":100162
    public int    id;
    //"accountId":123456
    public int    accountId;
    // "goodsId":4
    public int    goodsId;
    // "addressId":0
    public int    addressId;
    // "address": null,
    //public PointsMallAddressBean   address;
    // "createTime":"2016-08-24 14:32:27",
    public String createTime;
    //"mark":"待发放",
    public String mark;
    // "points":50
    public int    points;
    //"istatus":0,发放状态
    public int    istatus;
    //"needPost": 1,
    public int    needPost;
    // "user": null
    public String user;
    // "result": 0,
    public int    result;
    //商品详细信息
    public PointsMallItemDataBean goods;

}
