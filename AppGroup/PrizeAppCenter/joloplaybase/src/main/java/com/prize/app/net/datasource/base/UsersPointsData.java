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
 * Title:    用户积分信息
 * Desc:    个人中心-积分显示
 * Version:
 * Created by huangchangguo
 * on   2016/8/16  14:11
 * <p/>
 * Update Description: 更新描述
 * Updater:  更新者
 * Update Time:  更新时间
 */

public class UsersPointsData extends AbstractNetData implements Serializable {
    /**
     * serialVersionUID:TODO（用一句话描述这个变量表示什么）
     *
     * @since 1.0.0
     */
    private static final long serialVersionUID = -8016981216304294204L;

    public UsersCounts summary;

    public class UsersCounts implements Serializable {
        private static final long serialVersionUID = -8016981216304294204L;

        public int    id;
        //用户ID
        public int    accountId;
        //更新时间
        public String updateTime;
        //用户积分
        public int    points;
    }
}