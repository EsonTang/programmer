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
import java.util.List;

/**
 * Desc: 积分商城item详细信息
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/19 17:45
 */

public class ParentPointsMallItemBean implements Serializable {
    /**
     * 用一句话描述这个变量表示什么
     */
    private  final long serialVersionUID = 1L;
    public int    id;
    /**
     * 判断是否是秒杀商品 1：是
     */
    public int    saleFlag;
    public String title;
    public String updateTime;
    public List<PointsMallItemDataBean> goods;
}
