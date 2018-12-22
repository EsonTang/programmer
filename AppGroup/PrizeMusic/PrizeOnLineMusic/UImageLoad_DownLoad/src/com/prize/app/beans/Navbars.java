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
 ** 推荐位bean
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class Navbars implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;
	/** 1:新品;2:必备；3：专题 */
	public String id;
	/** icon地址 */
	public String iconUrl;
	/** 显示名称 */
	public String title;
	/** 关键词 */
	public String key;
	public String titleColor;
	public String cornerUrl;
}
