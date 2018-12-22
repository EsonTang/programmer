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

package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 详情的相关推荐&大家也喜欢 应用列表信息
 * @author longbaoixiu
 *
 */
public class AppDetailRecommandData  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3597874528825367485L;
	/**相关推荐***/
	public ArrayList<AppsItemBean> relatedApps = new ArrayList<AppsItemBean>();
	/**大家也喜欢***/
	public ArrayList<AppsItemBean> likeApps = new ArrayList<AppsItemBean>();
	
}
