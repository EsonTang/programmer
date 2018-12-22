package com.prize.app;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.HashMap;


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
//	public static ArrayList<AppsItemBean> updateApps = new ArrayList<AppsItemBean>();
	public static HashMap<String, String> lastModifyTimes = new HashMap<String, String>();
	public static HashMap<String, String> jsonData = new HashMap<String, String>();
	public static Bitmap bitmap;
	public static String pushPkgName;

	private UpdateCach() {
	}

	public static UpdateCach getInstance() {
		if (mCach == null) {
			mCach = new UpdateCach();
		}
		return mCach;
	}

//	public ArrayList<AppsItemBean> getApps() {
//		return updateApps;
//	}

//	public void setApps(List<AppsItemBean> apps) {
//		if (updateApps.size() <= 0){
//			updateApps.addAll(apps);
//		}
//	}
	
//	public void delete(String packageName) {
//		if(packageName == null) return;
//		for (int i = 0; i < updateApps.size(); i++) {
//			if (packageName.equals(updateApps.get(i).packageName)) {
//				updateApps.remove(i);
//			}
//		}
//	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return this.bitmap;
	}
	
	public String getPkgName() {
		return this.pushPkgName;
	}
	
	public void setPkgName(String pkgName) {
		this.pushPkgName = pkgName;
	}
	
	public long getLastModifyTime(String tag) {
		String param=lastModifyTimes.get(tag);
		return TextUtils.isEmpty(param)?-1:Long.parseLong(param);
	}

	public void setlastModifyTime(String tag,String lastModifyTime) {
		lastModifyTimes.put(tag, lastModifyTime);
	}
	
	public String getJsonData(String tag) {
		return jsonData.get(tag);
	}

	public void setJsonData(String tag,String json) {
		jsonData.put(tag, json);
	}
}
