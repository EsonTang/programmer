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

package com.prize.app.net.datasource.base;

import com.prize.app.beans.HotCategoryBean;
import com.prize.app.net.AbstractNetData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 **
 * 应用-分类 游戏-分类 返回数据的bean
 * 
 * @author huangchangguo
 * 
 * @version V1.0
 */
public class AppGameCategoryData extends AbstractNetData {

	public Data categories = new Data();
	public class Data implements Serializable {
		
		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = 1L;
		public ArrayList<Categories> items = new ArrayList<Categories>();
		public ArrayList<HotCategoryBean> hot_topic = new ArrayList<HotCategoryBean>();

	}



}
