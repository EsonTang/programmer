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
 * 有奖活动返回实体
 * @author longbaoxiu
 * @version V1.0
 */
public class AwardaProgramBean implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;

	/** 有奖活动标题 */
	public String title;
	/** 有奖活动图片地址 */
	public String bannerUrl;
	/** 有奖活动开始时间 */
	public String startTime;
	/** 有奖活动截止时间 */
	public String endTime;
	/** 有奖活动状态 1：正在进行 2：即将开始  3：结束*/
	public int istatus;
	/** 有奖活动跳转的url*/
	public String url;

}
