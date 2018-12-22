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
import java.util.ArrayList;

/**
 * Desc: 积分规则-兑换记录
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/18 10:26
 */

public class ConvertRecordsData extends AbstractNetData implements Serializable {

    private final long serialVersionUID = 1L;
    //分页的数量
    public int pageCount;
    //分页的索引
    public int pageIndex;
    //每页的大小
    public int pageSize;
    //item的数量
    public int pageItemCount;

    public ArrayList<ConvertRecordsItemBean> record;


}
