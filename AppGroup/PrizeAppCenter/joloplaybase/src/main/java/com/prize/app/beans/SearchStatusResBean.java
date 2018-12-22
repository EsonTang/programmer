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
 * 搜索结果返回的字段
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchStatusResBean implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;
	/** 0:默认  1：第一个完全匹配，展示99%... 2;不完全匹配 */
	public int type;
	/** 搜索结果提示eg：99%用户搜索该词后下载 */
	public String displayText;
	/** 大家还在搜索*/
	public String[] keywords;
	/** 截屏*/
	public String screenshotsUrl;

}
