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

package com.prize.app.beans;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 **
 * 
 * @author nieligang
 * @version V1.0
 * 抽屉推荐数据
 */
public class DrawerData implements Serializable {
	private static final long serialVersionUID = 2586294894297341629L;
	public ArrayList<AppsItemBean> firstAppInfos = new ArrayList<>();
	public ArrayList<AppsItemBean> secondAppInfos = new ArrayList<>();
	public ArrayList<AppsItemBean> Apps = new ArrayList<>();
}
