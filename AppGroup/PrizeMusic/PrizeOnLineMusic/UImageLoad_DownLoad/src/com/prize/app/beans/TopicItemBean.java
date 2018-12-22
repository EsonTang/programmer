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
 * 专题
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class TopicItemBean implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;

	public String id;
	public String title;
	public String description;
	public String imageUrl;
	public String createTime;
	public int status;
	public String bigImageUrl;
	public int styleId;
	public TopicType style;
	
	public class TopicType implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public String id;
		public String name;
		public String styleKey;
		public String contentColor;
		public String backgroudColor;
		public String titleColor;
		public String nameColor;
		public String ratingColor;
		public String imageUrl;
	}
	

}
