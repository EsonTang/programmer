package com.android.download;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel.Callbacks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class DownLoadService extends Service {
	private IDownLoad.Stub iDownload = new DownLoad();
	private IDownLoadCallback mCallback;
	private Launcher mLauncher;
	/** 下载状态, 注意:不要轻易修改值,数据库有记录!!!! */
	public static final int STATE_DOWNLOAD_WAIT = 0;
	// 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
	// STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
	/** 下载成功 */
	public static final int STATE_DOWNLOAD_SUCESS = 1;
	/** 下载过程发生了错误 */
	public static final int STATE_DOWNLOAD_ERROR = 2;
	/** 下载中 */
	public static final int STATE_DOWNLOAD_START_LOADING = 3;
	/** 通知下载开始 */
	public static final int STATE_DOWNLOAD_PAUSE = 4;
	// 注意：STATE_DOWNLOAD_SUCESS, STATE_DOWNLOAD_ERROR,
	// STATE_DOWNLOAD_LOADING，值不允许改变，和老版本兼容的状态值，数据库中有记录
	/** 通知进度 */
	public static final int STATE_DOWNLOAD_UPDATE_PROGRESS = 5;
	/** 下载，并安装成功 */
	public static final int STATE_DOWNLOAD_INSTALLED = 6;
	/** 取消下载 */
	public static final int STATE_DOWNLOAD_CANCEL = 7;
	
	
	
	

	// ------------------ APP状态 Start ------------------
	/** * 应用不存在 */
	public static final int APP_STATE_UNEXIST = 0x1000;
	/** * 应用正在被下载 */
	public static final int APP_STATE_DOWNLOADING = APP_STATE_UNEXIST + 1;
	/** * 应用下载被暂停 */
	public static final int APP_STATE_DOWNLOAD_PAUSE = APP_STATE_DOWNLOADING + 1;
	/** * 应用已完成下载，但尚未安装 */
	public static final int APP_STATE_DOWNLOADED = APP_STATE_DOWNLOAD_PAUSE + 1;
	/** * 应用已被安装 */
	public static final int APP_STATE_INSTALLED = APP_STATE_DOWNLOADED + 1;
	/** * 应用需要更新 */
	public static final int APP_STATE_UPDATE = APP_STATE_INSTALLED + 1;
	/** * 应用等待下载 */
	public static final int APP_STATE_WAIT = APP_STATE_UPDATE + 1;
	/** 应用正在被安装（仅静默安装时使用） **/
	public static final int APP_STATE_INSTALLING = APP_STATE_WAIT + 1;
	/** 查看礼包） **/
	public static final int APP_LOKUP_GIFT = APP_STATE_INSTALLING + 1;
	/** 已经领取 **/
	public static final int APP_RECEIVED_GIFT = APP_LOKUP_GIFT + 1;
	/** 全部领完 **/
	public static final int APP_NO_ACTIVATION_CODE = APP_RECEIVED_GIFT + 1;
	/** 礼包活动结束 **/
	public static final int APP_ACTIVITIES_OVER = APP_NO_ACTIVATION_CODE + 1;

	@Override
	public IBinder onBind(Intent intent) {
		LauncherAppState app = LauncherAppState.getInstance();
		app.getModel().setDownLoadService(this);
		return iDownload;
	}

	public void pikerDownLoadState(String pkg, int state) {
		try {
			if (mCallback != null) {
				mCallback.updateDownLoadState(pkg, state);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pikerRemoveCurrentTask(String pkg) {

		if (mCallback != null) {
			try {
				mCallback.removeCurrentTask(pkg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class DownLoad extends IDownLoad.Stub {

		public DownLoadService getDownLoadService() {
			return DownLoadService.this;
		}

		@Override
		public void setDownLoadCallback(IDownLoadCallback callback)
				throws RemoteException {
			mCallback = callback;

		}

		@Override
		public void updateDownLoadTaskInfo(DownLoadTaskInfo info)
				throws RemoteException {

			LogUtils.i("zhouerlong", "progress:" + info.progress + " pkg"
					+ info.pkgName);/*

			SoftReference<Callbacks> callback = LauncherAppState.getInstance()
					.getModel().getCallBacks();
			Callbacks cb = callback != null ? callback.get() : null;
			if (cb != null && cb instanceof Launcher) {
				mLauncher = (Launcher) cb;
				mLauncher.setDownLoadService(DownLoadService.this);
			}*/
			LauncherAppState app = LauncherAppState.getInstance();
			app.getModel().updateProgressFromAppStore(info);

		}

		@Override
		public void removeDownLoadTask(String packageName)
				throws RemoteException {
			LogUtils.i("zhouerlong", "packageName:" + packageName);

			LauncherAppState app = LauncherAppState.getInstance();
			app.getModel().removeAppFromAppStore(packageName);

		}

		@Override
		public void startDownLoadTask(DownLoadTaskInfo info)
				throws RemoteException {
			LauncherAppState app = LauncherAppState.getInstance();
			app.getModel().addAppStoreItems(info);

		}
	}

}
