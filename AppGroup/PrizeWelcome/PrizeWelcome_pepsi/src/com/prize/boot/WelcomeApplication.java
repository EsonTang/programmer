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

package com.prize.boot;

import java.util.Stack;

import android.app.Activity;
import android.app.Application;

public class WelcomeApplication extends Application {
	private static Stack<Activity> sActivityStack;
	private static WelcomeApplication sSingleton;

	@Override
	public void onCreate() {
		super.onCreate();
		sSingleton = this;
	}

	// Returns the application instance
	public static WelcomeApplication getInstance() {
		return sSingleton;
	}

	/**
	 * add Activity 添加Activity到栈
	 */
	public void addActivity(Activity activity) {
		if (sActivityStack == null) {
			sActivityStack = new Stack<Activity>();
		}
		sActivityStack.add(activity);
	}

	/**
	 * get current Activity 获取当前Activity（栈中最后一个压入的）
	 */
	public Activity currentActivity() {
		Activity activity = sActivityStack.lastElement();
		return activity;
	}

	/**
	 * 结束当前Activity（栈中最后一个压入的）
	 */
	public void finishActivity() {
		Activity activity = sActivityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * 结束指定的Activity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			sActivityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void finishActivity(Class<?> cls) {
		for (Activity activity : sActivityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() {
		for (int i = 0, size = sActivityStack.size(); i < size; i++) {
			if (null != sActivityStack.get(i)) {
				sActivityStack.get(i).finish();
			}
		}
		sActivityStack.clear();
	}

	/**
	 * 退出应用程序
	 */
	public void AppExit() {
		try {
			finishAllActivity();
		} catch (Exception e) {
		}
	}
}
