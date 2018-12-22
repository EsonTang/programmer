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

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.app.net.AbstractNetData;

/**
 **
 * app分类返回数据
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CategoryData extends AbstractNetData {

	public ArrayList<CategoriesParent> mCategoriesParent = new ArrayList<CategoriesParent>();

	/**
	 **
	 * app分类bean
	 * 
	 * @author longbaoxiu
	 * @version V1.0
	 */
	public class Categories implements Serializable {

		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = 1L;
		public int id;
		/** icon地址 */
		public String icon;
		/** 显示名称 */
		public String typeName;

	}

	/**
	 **
	 * app分类bean
	 * 
	 * @author longbaoxiu
	 * @version V1.0
	 */
	public class CategoriesParent implements Serializable {

		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = 1L;
		public String name;
		public ArrayList<Categories> items = new ArrayList<Categories>();

	}

}
