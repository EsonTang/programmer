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

package com.prize.onlinemusibean.response;

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.onlinemusibean.RecomendRankBean;

/**
 * 
 **
 * banner返回解析data后的返回对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecomendRankResponse implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -5918206245621073156L;
	public String category;
	public ArrayList<RecomendRankBean> items;
}
