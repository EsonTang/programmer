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

import java.io.Serializable;

import com.prize.app.net.datasource.base.AppsItemBean;

public class HeadBeauBean implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 6012940740302553308L;
	public int id;
	/** 最美头部名称 */
	public String title;
	/** 最美头部介绍 */
	public String description;
	/** 最美头部url */
	public String imageUrl;
	/** 游戏名称 */
	public String status;
	public String createTime;
	public String ikey;
	public AppsItemBean app;
}
