package com.lqsoft.lqtheme;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppState;

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
	public static final String RECEIVER_ACTION= "appley_theme_ztefs";
	public static final String RECEIVER_THEME_PATH= "themePath";


	private static ArrayList<OLThemeChangeListener> mThemeChangeListeners = new ArrayList<OLThemeChangeListener>();

	private static Object mLock = new Object();

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
	public static void registerThemeChange(Object target,
			OLThemeChangeListener observer, Object userObject) {
		synchronized (mLock) {
			if (observer != null && !mThemeChangeListeners.contains(observer)) {
				mThemeChangeListeners.add(observer);
			}

		}
	}

	/**
	 * 解除注册主题变化
	 * 
	 * @param target
	 *            监听对象
	 */
	public static void unRegisterThemeChange(OLThemeChangeListener observer,
			Object target) {
		synchronized (mLock) {
			if (observer != null && mThemeChangeListeners.contains(observer)) {
				mThemeChangeListeners.remove(observer);
			}
		}
	}

	/**
	 * 通知主题发生变化
	 */
	public static void notifyThemeChange() {
		IconCache iconCache = LauncherAppState.getInstance().getIconCache();
        if(iconCache != null) {
            iconCache.flush();
        }
        ExecutorService cachedThreadPool=Executors.newCachedThreadPool();
		for (int i = 0; i < mThemeChangeListeners.size(); i++) {
			final int s=i;
			cachedThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					mThemeChangeListeners.get(s).onThemeChange();
				}
			});
			
		}
		cachedThreadPool.shutdown();
	}

}
