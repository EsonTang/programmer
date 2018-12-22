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

package com.prize.music;

import java.util.ArrayList;

import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.custmerxutils.XExtends;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.provider.AppInfoProvider;
import com.prize.music.receiver.NetStateReceiver;
import com.prize.music.receiver.ScreenListener;
import com.prize.music.receiver.ScreenListener.ScreenStateListener;
import com.prize.onlinemusibean.response.RecomendTagsResponse;
import com.umeng.analytics.MobclickAgent;
import com.xiami.sdk.utils.UTUtil;

public class MainApplication extends BaseApplication {
	public static ArrayList<RecomendTagsResponse> recomendTagsResponse;
	private static NetStateReceiver netstateReceiver;
	private String mUserId;
		
	private static Context mContext;
	
	public static Context getContext() {
		return mContext;		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();	
				
		mContext = getApplicationContext();
		String processName = MusicUtils.getProcessName(this,
				android.os.Process.myPid());
		if (processName != null) {
			boolean defaultProcess = processName.equals("com.prize.music:main");
			if (defaultProcess) {
				UTUtil.init(this);
				initBaseApp();
				XExtends.Ext.init(this);
				//XExtends.Ext.setDebug(true);
				initImageLoader(this);
				MobclickAgent.openActivityDurationTrack(false);
				registerObserver();
				registerScreenLister();
				// 必要的初始化资源操作
			}
		}
		regisetReceiver();

		JLog.i("0000", "onCreate()-processName=" + processName);

		// UM禁止默认的页面统计方式
		// 刷新下数据
		// if (ClientInfo.networkType == ClientInfo.WIFI) {
		// if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
		// String whereClause = DatabaseConstant.SONG_SOURCE_TYPE + "=?";
		// String[] whereArgs = { DatabaseConstant.ONLIEN_TYPE };
		// MusicUtils
		// .deleteData(getApplicationContext(),
		// DatabaseConstant.TABLENAME_LOVE, whereClause,
		// whereArgs);
		// String whereClause2 = DatabaseConstant.LIST_SOURCE_TYPE + "=?";
		// MusicUtils.deleteData(getApplicationContext(),
		// DatabaseConstant.TABLENAME_LIST, whereClause2,
		// whereArgs);
		// MusicUtils.requestSortSongsMenuFromServe(curContext);
		// if(!isLoading){
		// MusicUtils.requestSortSongsFromServe(curContext);
		// isLoading=false;
		// }
		// }
		// }
	}

	/**
	 * 方法描述：注册锁屏监听者
	 */
	public void registerScreenLister() {
		ScreenListener screenListener = new ScreenListener(this);
		screenListener.begin(new ScreenStateListener() {

			@Override
			public void onUserPresent() {

			}

			@Override
			public void onScreenOn() {
			}

			// 锁屏并且电量高于30%时才会调用
			@Override
			public void onScreenOff() {
			}

			@Override
			public void onScreenOffNoRLLevel() {

			}
		});
	}

	/**
	 * 
	 * 网络监听
	 */
	private void regisetReceiver() {
		netstateReceiver = new NetStateReceiver();
		this.registerReceiver(netstateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	/**
	 * 方法描述：注册数据库监听器，已便实时刷新数据
	 */
	private void registerObserver() {
		PersonObserver personResolver = new PersonObserver(this, handler);
		Uri personUri = Uri.parse(AppInfoProvider.TABLE_PERSON_PATH);
		this.getContentResolver().registerContentObserver(personUri, true,
				personResolver);
	}

	private class PersonObserver extends ContentObserver {

		public PersonObserver(Context context, Handler handler) {
			super(handler);
		}

		/**
		 * 当所监听的Uri发生改变时，就会回调此方法
		 * 
		 * @param selfChange
		 *            此值意义不大 一般情况下该回调值false
		 */
		@Override
		public void onChange(boolean selfChange) {
			String userId = CommonUtils.queryUserId();
			JLog.i("0000", "onChange=" + selfChange);
			if (!TextUtils.isEmpty(userId)) {
				if (TextUtils.isEmpty(mUserId)
						|| (!TextUtils.isEmpty(mUserId) && !userId
								.equals(mUserId))) {
					MusicUtils.requestSortSongsMenuFromServe(curContext);
					MusicUtils.requestSortSongsFromServe(curContext);
				}
			} else {
				String whereClause = DatabaseConstant.SONG_SOURCE_TYPE + "=?";
				String[] whereArgs = { DatabaseConstant.ONLIEN_TYPE };
				MusicUtils
						.deleteData(getApplicationContext(),
								DatabaseConstant.TABLENAME_LOVE, whereClause,
								whereArgs);
				String whereClause2 = DatabaseConstant.LIST_SOURCE_TYPE + "=?";
				MusicUtils.deleteData(getApplicationContext(),
						DatabaseConstant.TABLENAME_LIST, whereClause2,
						whereArgs);
			}
			mUserId = CommonUtils.queryUserId();
		}

	}

}
