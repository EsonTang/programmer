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
import java.util.ArrayList;

import com.prize.app.net.datasource.base.AppsItemBean;

/**
 * 
 **
 * 游戏礼包激活码
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppGiftCodes implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 8871730394665426852L;
	public ArrayList<GiftCodesBean> giftCodes = new ArrayList<GiftCodesBean>();
	public AppsItemBean app;
}
