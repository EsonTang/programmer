package com.prize.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.music.MainApplication;
import com.prize.music.helpers.utils.PollMgr;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.prize.music.receiver.ScreenListener;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 提示服务类
 * @author liukun
 */
public class PrizeMusicService extends Service {

	public static final String OPT_TYPE = "optType";
	private static final String TAG = "lk";
	public static final String ACTION = "action";
	public static final int ACT_DOWNLOAD = 92;
//	public static final int ACT_PAUSEALL_TASK = ACT_CONTINUE_DOWNLOAD +1;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null == intent)
			return START_STICKY;
		int type = intent.getIntExtra(OPT_TYPE, 0);
		int action = intent.getIntExtra(ACTION, 0);
		JLog.i("3333", "type==" + type + "--action==" + action);
	
		if (action==ACT_DOWNLOAD) {
    	if(intent.getExtras()
				.getParcelable("bean") !=null){
			SongDetailInfo itemBean = (SongDetailInfo) intent.getExtras()
					.getParcelable("bean");
			AppManagerCenter.startDownload(itemBean);
			
		}else{
			ArrayList<SongDetailInfo> lists=	intent.getParcelableArrayListExtra("list");
			if(lists !=null&&lists.size()>0){
				AppManagerCenter.startBatchDownload(lists);
			}
		}
    	}
		switch (type) {
			case 5:
//				PollMgr.stopPollingService(MainApplication.curContext,
//						PrizeMusicService.class, "");
//				JLog.i(TAG, "111111" + ScreenListener.isScreenoff);
//				if (ScreenListener.isScreenoff) {
//					if (new File(Constants.APKFILEPATH).exists()) {
//						if (MD5Util.Md5Check(Constants.APKFILEPATH,
//								PreferencesUtils.getString(
//										MainApplication.curContext,
//										Constants.APP_MD5))) {
//							JLog.i(TAG, "onScreenOff--Md5Checkok");
//							Runnable task = new Runnable() {
//								@Override
//								public void run() {
//									boolean installFlag = PackageUtils.installApkDefaul(
//											MainApplication.curContext,
//											Constants.APKFILEPATH);
//									JLog.i(TAG, "installFlag==" + installFlag);
//									if (installFlag != true) {
//										PreferencesUtils.putLong(
//												MainApplication.curContext,
//												Constants.KEY_NAME_DOWNLOAD_ID,
//												-1);
//									}
//								}
//							};
//							new Thread(task).start();
//							return START_STICKY;
//						}
//					}
//				}
				break;
			default:
				break;
		}
		return START_STICKY;
	}
}
