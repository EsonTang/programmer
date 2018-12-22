package com.prize.app;

import java.util.ArrayList;

import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;

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

/**
 * 类描述：需要更新的数据缓存
 * 
 * @author huanglingjun
 * @version 版本
 */
public class UpdateCach {
	private static UpdateCach mCach = new UpdateCach();
	public static ArrayList<AppsItemBean> updateApps = new ArrayList<AppsItemBean>();

	private UpdateCach() {
	}

	public static UpdateCach getInstance() {
		return mCach;
	}

	public ArrayList<AppsItemBean> getApps() {
		return updateApps;
	}

	public void setApps(ArrayList<AppsItemBean> apps) {
		if (updateApps.size() <= 0){
			JLog.e("huang", "==========setApps(ArrayList<AppsItemBean> apps)===========");
			updateApps.addAll(apps);
		}
	}
	
	public void delete(String packageName) {
		if(packageName == null) return;
		for (int i = 0; i < updateApps.size(); i++) {
			if (packageName.equals(updateApps.get(i).packageName)) {
				updateApps.remove(i);
			}
		}
	}

}
