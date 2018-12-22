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
 * 分类返回数据
 * @author pengy
 */
public class ClassifyData implements Serializable {

	public ArrayList<ClassifyBean> categories = new ArrayList<>();

	public class ClassifyBean  implements Serializable {
		private static final long serialVersionUID = -4921593634744607844L;
		public String id;
		/** * 名称 */
		public String name;
		/** Icon*/
		public String icon;
		/** * 图片地址 */
		public String image;
		/** * 更新时间 */
		public String updateTime;
	}

}
