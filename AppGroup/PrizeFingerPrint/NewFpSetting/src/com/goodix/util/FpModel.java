package com.goodix.util;

import java.util.ArrayList;
import java.util.List;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.model.AppLockEvent;
import com.goodix.model.Appinfo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.util.Log;

public class FpModel {

	private Context mContext;
	private ArrayList<Appinfo> mApplist;
	private List<String> mPkgNameList = new ArrayList<String>();
	public static final String[] mOnlyOnePKGARR = new String[]{"com.android.settings","com.android.settings","com.cooee.unilauncher"
		,"com.prize.lockscreen","com.android.keyguard.test","com.baidu.input","com.prize.weather","com.tencent.android.qqdownload",
		"com.android.flash","com.goodix.fpsetting","com.android.providers.downloads.ui","com.android.music","com.baidu.BaiduMap"
		,"com.android.launcher5.mi.changhong","com.sankuai.meituan","com.assistant.icontrol","com.sensky.sunshinereader"};
	public String mThemeHeader = "com.coco.themes";
	private FpDbOperarionImpl mDao;

	public FpModel(Context context) {
		mContext = context;
		mApplist = new ArrayList<Appinfo>();
		mDao = FpDbOperarionImpl.getInstance(context);
		initKeyPkgNameList();
		loadAllAppsByPM();
	}

	private void initKeyPkgNameList() {
		for(String pkgName:mOnlyOnePKGARR){
			mPkgNameList.add(pkgName);
		}
	}

	private void loadAllAppsByPM() {
		int N = Integer.MAX_VALUE;
		final PackageManager packageManager = mContext.getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> apps = null;
		apps = packageManager.queryIntentActivities(mainIntent, 0);
		N = apps.size();

		boolean isContain = false;
		String pkgName = null;
		for (int i = 0; i < N; i++) {
			pkgName = apps.get(i).activityInfo.packageName;
			isContain = isContainingKeyPackageName(pkgName);
			
			if(!isContain){
				insertToDb(pkgName);
				mApplist.add(new Appinfo(mContext, apps.get(i)));
			}
		}
	}
	
	private void insertToDb(String pkgName){
		String selection = AppLockEvent.PKG_NAME+"=?";
		String[] selectionArgs = new String[]{pkgName};
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_TB_NAME, null, selection, selectionArgs, null);
		if(null != cursor && cursor.moveToFirst()){
			cursor.close();
			return;
		}else{
			if(null != cursor){
				cursor.close();
			}
			ContentValues values = new ContentValues();
			values.put(AppLockEvent.PKG_NAME, pkgName);
			mDao.insert(ConstantUtil.APP_LOCK_TB_NAME, values);
		}
	}

	private boolean isContainingKeyPackageName(String pkgName){
		String keyPkgName = null;
		for(int i=0;i<mPkgNameList.size();i++) { 
			keyPkgName = mPkgNameList.get(i);
			if(pkgName.startsWith(keyPkgName)){
				mPkgNameList.remove(i);
				return true;
			}
		}
		if(pkgName.startsWith(mThemeHeader)){
			return true;
		}
		return false;
	}

	public ArrayList<Appinfo> getAppsList() {
		return mApplist;
	}

}
