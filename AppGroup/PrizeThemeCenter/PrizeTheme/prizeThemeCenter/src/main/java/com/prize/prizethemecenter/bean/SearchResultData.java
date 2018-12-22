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
 * 搜索返回数据
 * @author pengy
 */
public class SearchResultData implements Serializable {

	public ArrayList<ResultData> tag = new ArrayList<>();

	public class ResultData  implements Serializable {

		private static final long serialVersionUID = -4921593634744607844L;

		public String id;
		/**名称 */
		public String name;
		public String wallpaper_pic;
		public String category_name;
		public String tag;
		/**图片路径 */
		public String ad_pictrue;
		/**价格 */
		public String price;
		public String wallpaper_type;
		public String index_name;
		/**是否更新*/
		public String is_update;
		/**是否新品*/
		public String is_latest;

		/**服务上对应的MD5值*/
		public String md5_val;

	}

}
