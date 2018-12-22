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

import java.io.Serializable;

/**
 * Desc: 积分商城-抽奖-兑换的bean
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/18 10:26
 */

public class PointsLotteryData implements Serializable {

    /**
     * 用一句话描述这个变量表示什么，没什么用，虚拟机编译的时候用到
     */
    private static final long serialVersionUID = 1L;

    //result，返回的结果
    public int result;

    //msg 返回的信息
    public String msg;

    //返回兑换商品编号
    public String orderId;

    //积分
    public String leftPoints;


    @Override
    public String toString() {
        return "PointsLotteryData{" +
                "result='" + result + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
