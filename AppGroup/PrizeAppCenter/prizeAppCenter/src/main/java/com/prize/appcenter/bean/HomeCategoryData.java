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

package com.prize.appcenter.bean;

import com.prize.app.beans.HotCategoryBean;
import com.prize.app.net.datasource.base.Categories;

import java.io.Serializable;
import java.util.ArrayList;

/**
 **
 * Home进入分类后需要获得的数据
 * 
 * @author nieligang
 * @version V1.0
 */
public class HomeCategoryData implements Serializable {

	private static final long serialVersionUID = 7201781968162549656L;
	public ArrayList<HotCategoryBean> hot_topic = new ArrayList<HotCategoryBean>();
	public ArrayList<Categories> game_cat = new ArrayList<Categories>();
	public ArrayList<Categories> app_cat = new ArrayList<Categories>();


}
