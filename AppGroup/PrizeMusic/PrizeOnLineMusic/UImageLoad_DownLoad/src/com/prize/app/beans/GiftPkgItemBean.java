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
 * 
 **
 * 礼包item bean
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GiftPkgItemBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -5508630549250846738L;
	/** 游戏id */
	public int appId;
	/** 游戏id */
	public int id;
	/** l礼包名称 */
	public String title;
	/** 游戏介绍 */
	public String content;
	/** 游戏名称 */
	public String startTime;
	/** 游戏名称 */
	public String endTime;
	/** 游戏名称 */
	public String createTime;
	/** 礼包使用说明 */
	public String usage;
	/** 激活码 */
	public String activationCode;
	/**
	 * 礼包类型 0:直接查看 1或2：
	 */
	public int giftType;
	/**
	 * 礼包状态0 - 初始； 1 - 活动结束； 2 - 进行中； 3 - 激活码领取完成；
	 */
	public int giftStatus;

}
