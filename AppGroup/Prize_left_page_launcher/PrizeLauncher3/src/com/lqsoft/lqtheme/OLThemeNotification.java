package com.lqsoft.lqtheme;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.AsyncTask;
import android.os.Handler;

import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.nifty.NiftyObservers;

/**
 * 
 * @author liuwei
 * @since 2015-03-26 10:49:16 Thursday
 * @version 1.0.0
 */
public class OLThemeNotification {

	public static final String KEY_LQTHEME = "lqtheme";
	public static final String KEY_IS_LQTHEME = "is_lqtheme";
	public static final String KEY_LQTHEME_PATH = "lqtheme_path";
	public static final String RECEIVER_ACTION = "appley_theme_ztefs";
	public static final String RECEIVER_THEME_PATH = "themePath";

	private ArrayList<OLThemeChangeListener> mThemeChangeListeners = new ArrayList<OLThemeChangeListener>();

	private Object mLock = new Object();

	/**
	 * 注册主题变化
	 * 
	 * @param target
	 *            监听对象
	 * @param observer
	 *            监听器
	 * @param userObject
	 *            参数对象
	 */
	public void registerThemeChange(Object target,
			OLThemeChangeListener observer, Object userObject) {
		synchronized (mLock) {
			if (observer != null && !mThemeChangeListeners.contains(observer)) {
				mThemeChangeListeners.add(observer);
			}

		}
	}
	
	private static OLThemeNotification mInstance;
	
	public static OLThemeNotification getInstance() {
		if(mInstance==null) {
			mInstance = new OLThemeNotification();
		}
		return mInstance;
	}
	/**
	 * 解除注册主题变化
	 * 
	 * @param target
	 *            监听对象
	 */
	public void unRegisterThemeChange(OLThemeChangeListener observer,
			Object target) {
		synchronized (mLock) {
			if (observer != null && mThemeChangeListeners.contains(observer)) {
				mThemeChangeListeners.remove(observer);
			}
		}
	}

	public ArrayList<OLThemeChangeListener> getmThemeChangeListeners() {
		return mThemeChangeListeners;
	}
	/**
	 * 解除注册主题变化
	 * 
	 * @param target
	 *            监听对象
	 */
	public void clear() {
		synchronized (mLock) {
			if (mThemeChangeListeners != null) {
				mThemeChangeListeners.clear();
			}
		}
	}

	class NotifyTask extends AsyncTask<Void, OLThemeChangeListener, Boolean> {

		private boolean end;

		@Override
		protected Boolean doInBackground(Void... params) {
			synchronized (mThemeChangeListeners) {
				for (int i = 0; i < mThemeChangeListeners.size(); i++) {
					final int s = i;
					end = i == mThemeChangeListeners.size() - 1;
					publishProgress(mThemeChangeListeners.get(i));
				}
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(final OLThemeChangeListener... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

			ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
			cachedThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					values[0].onThemeChange(end);
				}
			});

			cachedThreadPool.shutdown();

		}

	}

	/**
	 * 通知主题发生变化
	 */
	public  void notifyThemeChange() {
		// final Handler h = new Handler();
		IconCache iconCache = LauncherAppState.getInstance().getIconCache();
		if (iconCache != null) {
			iconCache.flush();
		}

		NotifyTask task = new NotifyTask();
		task.execute();
	}

}
