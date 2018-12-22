/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
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
 * Desc: 积分商城-服务器返回收货地址的bean
 * <p>
 * Created by huangchangguo
 * Date:  2016/8/19 17:45
 */

public class PointsMallAddressBean implements Serializable {
    /**
     * 用一句话描述这个变量表示什么
     */
    private final long serialVersionUID = 1L;

    public int id;
    //用户ID
    public int accountId;
    //收货人
    public String name;
    //电话
    public String tel;
    //邮编
    public String postcode;
    //地址
    public String address;
    //创建时间
    public String createTime;

}
