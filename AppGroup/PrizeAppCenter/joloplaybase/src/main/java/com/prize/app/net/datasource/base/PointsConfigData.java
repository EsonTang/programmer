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

import com.prize.app.net.AbstractNetData;

import java.io.Serializable;

/**
 * Desc: 积分规则-积分商城bannertu的bean
 *
 * Created by huangchangguo 
 * Date:  2016/8/18 10:26
 */

public class PointsConfigData extends AbstractNetData implements Serializable {
    //积分商城banner图的url
    public String bannerUrl;

    //积分商城-每天最多可安装6个有奖应用
    public String notice;

    //积分规则的web地址
    public String rule;

    @Override
    public String toString() {
        return "PointsConfigData{" +
                "bannerUrl='" + bannerUrl + '\'' +
                ", notice='" + notice + '\'' +
                ", rule='" + rule + '\'' +
                '}';
    }
}
