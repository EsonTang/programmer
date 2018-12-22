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
import java.util.List;

import com.prize.app.net.datasource.base.AppsInstalledListData.AppsInstallItemBean;

/**
 * 类描述：
 * 
 * @author huanglingjun
 * @version 版本
 */
public class InstalledCategory implements Serializable {
	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 6384548634019188007L;
	private String mCategoryName;
	/**
	 * 每个Item对应的HeaderId
	 */
	public int headerId;
	private List<AppsInstallItemBean> mCategoryItem = new ArrayList<AppsInstallItemBean>();

	public InstalledCategory(String mCategroyName) {
		mCategoryName = mCategroyName;
	}

	public String getmCategoryName() {
		return mCategoryName;
	}

	public void addItem(AppsInstallItemBean pItemName) {
		mCategoryItem.add(pItemName);
	}

	/**
	 * 获取Item内容
	 * 
	 * @param pPosition
	 * @return
	 */
	public Object getItem(int pPosition) {
		// Category排在第一位
		if (pPosition == 0) {
			return mCategoryName;
		} else {
			return mCategoryItem.get(pPosition - 1).app;
		}
	}

	/**
	 * 方法描述：
	 * 
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void removeItem(int pPosition) {
		if (pPosition > 0) {
			mCategoryItem.remove(pPosition - 1);
		}
	}

	/**
	 * 当前类别Item总数。Category也需要占用一个Item
	 * 
	 * @return
	 */
	public int getItemCount() {
		return mCategoryItem.size() + 1;
	}
}
