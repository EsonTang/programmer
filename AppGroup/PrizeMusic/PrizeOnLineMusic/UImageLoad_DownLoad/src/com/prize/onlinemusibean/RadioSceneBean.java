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
 * 场景bean
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioSceneBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1143941570851332364L;
	/** 电台id */
	public int radio_id;
	/** 电台分类id */
	public int radio_type;
	/** 场景名称 */
	public String title;
	/** 场景图片 */
	public String logo;
	/** 场景对应tag */
	public String tag;
}
