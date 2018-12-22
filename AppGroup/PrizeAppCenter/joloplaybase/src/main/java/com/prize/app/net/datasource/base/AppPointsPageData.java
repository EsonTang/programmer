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
 * Desc:赚取积分里面的 itembean
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/17 15:09
 */

public class AppPointsPageData extends AbstractNetData implements Serializable {

    //总页数
    public int pageCount;
    //当前的页数索引
    public int pageIndex;
    //每页显示的数量
    public int pageSize;
    //总的数量
    public int pageItemCount;

    public ArrayList<AppsItemBean>  apps;

}
