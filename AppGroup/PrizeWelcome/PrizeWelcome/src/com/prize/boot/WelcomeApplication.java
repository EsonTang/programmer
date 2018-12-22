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

import org.xutils.x;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;

public class WelcomeApplication extends Application {
	
	private static final String TAG = "WelcomeApplication";
	private static Stack<Activity> sActivityStack;
	private static WelcomeApplication sSingleton;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	private static final int 	PRODUCT_INFO_INDEX	  = 45;
	private static final int 	PRODUCT_INFO_INDEX2	  = 37;

	@Override
	public void onCreate() {
		super.onCreate();
		sSingleton = this;
		x.Ext.init(this);
		x.Ext.setDebug(true);
		String result = readProInfo(PRODUCT_INFO_INDEX);
		String result2 = readProInfo(PRODUCT_INFO_INDEX2);
		Log.i(TAG, "[WelcomeApplication] result : " + result);
		if (!("P".equals(result) || "P".equals(result2))) {
			finishSetupWizard();
			startLauncher(this);
			System.exit(0);
		}
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
	
	private void finishSetupWizard() {
		Log.v(TAG,"~~Prize Welcome ~~~finishSetupWizard() ---------->start");
		// remove this activity from the package manager.
		PackageManager pm = getPackageManager();
		ComponentName name = new ComponentName(this, WizardActivity.class);
		int state = pm.getComponentEnabledSetting(name);
		if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
			pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
		Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
		Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
		Log.v(TAG,"~~Prize Welcome ~~~finishSetupWizard() ---------->end");
	}
	
	public void startLauncher(Context context) {
		Log.v(TAG,"~~Prize Welcome ~~~startLauncher() ---------->start");
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);//com.cooee.unilauncher/com.iLoong.launcher.desktop.iLoongLauncher
        Log.v(TAG,"~~Prize Welcome before~~~i.getPackage() = "+i.getPackage());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
        Log.v(TAG,"~~Prize Welcome ~~~startLauncher() ---------->end");
    }
	
	private String readProInfo(int index) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		char c=(char)buff[index];
		String sn=new String(buff);
		return String.valueOf((char)buff[index]);
	}
}
