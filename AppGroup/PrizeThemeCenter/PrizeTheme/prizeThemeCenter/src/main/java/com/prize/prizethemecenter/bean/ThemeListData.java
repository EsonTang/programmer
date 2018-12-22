/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：首页gallery的广告bean
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.prizethemecenter.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 分类返回数据
 * @author pengy
 */
public class ThemeListData implements Serializable {

	public ArrayList<ThemeItemBean> item = new ArrayList<ThemeItemBean>();

	public int pageCount;
	public int pageIndex;
	public short pageSize;
	public int pageItemCount;

}
