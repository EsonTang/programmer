/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：首页gallery的广告bean
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
 * 首页gallery的广告bean
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeAdBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -4921593634744607844L;
	/** 所指对象, [collect/album/artist/song/h5]:[id/链接]的格式 */
	public String url;
	/** * 图片地址 */
	public String pic_url_yasha;

}
