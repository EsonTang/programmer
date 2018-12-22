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

package com.prize.prizethemecenter.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 专题返回数据
 * @author pengy
 */
public class TopicData implements Serializable {

	public ArrayList<TopicBean> topics = new ArrayList<>();

	public class TopicBean  implements Serializable {
		private static final long serialVersionUID = -4921593634744607844L;

		public String id;
		/** * 名称 */
		public String title;
		/** 简介*/
		public String intro;
		/** * 图片地址 */
		public String image;
		/** * 图片地址 */
		public String big_image;
	}
}
