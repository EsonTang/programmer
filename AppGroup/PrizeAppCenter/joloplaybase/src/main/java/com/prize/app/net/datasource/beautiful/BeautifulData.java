/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
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

package com.prize.app.net.datasource.beautiful;

import java.util.ArrayList;

import com.prize.app.beans.HeadBeauBean;
import com.prize.app.net.AbstractNetData;
import com.prize.app.net.datasource.base.AppsItemBean;

/**
 **
 * 最美应用
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class BeautifulData extends AbstractNetData {
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();
	public int pageCount;
	public int pageIndex;
	public int pageSize;
	public int pageItemCount;
	public HeadBeauBean topic;
}
