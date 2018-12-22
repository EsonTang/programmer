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

/**
 **
 * 首页宫格模块
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class Navblocks implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 2589657075007566169L;
	/** 1:新品;2:必备；3：专题 */
	public String id;
	/** icon地址 */
	public String iconUrl;
	public String backgroudUrl;
	/** 显示名称 */
	public String title;
	/** 关键词 */
	public String ikey;
	public String titleColor;
	public String brief;
	public String ivalue;
	public String sn;

	@Override
	public String toString() {
		return "Navblocks [id=" + id + ", iconUrl=" + iconUrl
				+ ", backgroudUrl=" + backgroudUrl + ", title=" + title
				+ ", ikey=" + ikey + ", titleColor=" + titleColor + ", brief="
				+ brief + ", ivalue=" + ivalue + ", sn=" + sn + "]";
	}

}
