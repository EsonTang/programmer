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

package com.prize.onlinemusibean;

import java.io.Serializable;

/**
 * 
 **
 * 歌单二级分类标签
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SubTagsBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	/** 标签名称 */
	public String name;
	/** 标志，热门为hot，其他为none */
	public String flag;
}
