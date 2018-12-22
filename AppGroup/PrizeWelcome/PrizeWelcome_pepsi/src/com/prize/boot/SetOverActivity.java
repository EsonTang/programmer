/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年8月3日
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

import java.util.ArrayList;
import java.util.List;

import com.prize.boot.util.CTelephoneInfo;
import com.prize.boot.util.Utils;

import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class SetOverActivity extends AbstractGuideActivity {

	private static String packageName = "com.cooee.unilauncher";// 默认launcher包名
	private static String className = "com.iLoong.launcher.desktop.iLoongLauncher";//// 默认launcher入口

	SubscriptionManager subscriptionManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_complete);
		setGuideTitle(R.drawable.complete_icon, R.string.set_complete);
		subscriptionManager = SubscriptionManager.from(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.next_btn) {
			try {
				int subid = subscriptionManager.getDefaultSubId();
				Log.v("prize", "~~Prize Welcome ~~~SetOverActivity ---------->subid = " + subid);
				if (subid != 1) {
					isSimExist();
				}
				finishSetupWizard();
				setDefaultLauncher();
				//sendHomeKeystroke(KeyEvent.KEYCODE_HOME);
				startLauncher(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// com.cooee.unilauncher/com.iLoong.launcher.desktop.iLoongLauncher
			mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 1800);
		} else if (v.getId() == R.id.im_back) {
			// finish();
			nextStep(false);
		}
	}

	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			if (1 == msg.what) {
				// finishAllActivity();
				setResult(Utils.RESULT_CODE_FINISH);
				finish();
			} else if (2 == msg.what) {
				startLauncher(SetOverActivity.this);
			}
		};
	};

	public static void startLauncher(Context context) {
		Log.v("prize", "~~Prize Welcome ~~~startLauncher() ---------->start");
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);// com.cooee.unilauncher/com.iLoong.launcher.desktop.iLoongLauncher
		Log.v("prize", "~~Prize Welcome before~~~i.getPackage() = " + i.getPackage());
		//i.setClassName(packageName, className);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(i);
		Log.v("prize", "~~Prize Welcome ~~~startLauncher() ---------->end");
	}

	private void finishAllActivity() {
		Log.v("prize", "~~Prize Welcome ~~~finishAllActivity() ---------->start");
		Utils.saveBootStatus(getApplicationContext(), false);
		WelcomeApplication.getInstance().finishAllActivity();
		Log.v("prize", "~~Prize Welcome ~~~finishAllActivity() ---------->end");
	}

	private void finishSetupWizard() {
		Log.v("prize", "~~Prize Welcome ~~~finishSetupWizard() ---------->start");
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
		Log.v("prize", "~~Prize Welcome ~~~finishSetupWizard() ---------->end");
	}

	private boolean isSimExist() {
		CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
		telephonyInfo.setCTelephoneInfo();
		boolean sim1State = telephonyInfo.isSIM1Ready();
		boolean sim2State = telephonyInfo.isSIM2Ready();
		if (sim2State && !sim1State) {
			Log.v(Utils.TAG, "--zwl--> selPosDefault: setDefaultDataSubId");
			subscriptionManager.setDefaultDataSubId(1);
		}
		return (sim1State || sim2State);
	}

	public static void sendHomeKeystroke(final int KeyCode) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyCode);
					Log.v(TAG, "## sendKey ### inhomeKey susscceful #####");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void setDefaultLauncher() {
		// get default component
		IPackageManager pm = ActivityThread.getPackageManager();

		// 判断指定的launcher是否存在
		if (hasApkInstalled(packageName)) {

			Log.i(TAG, "defautl packageName = " + packageName + ", default className = " + className);

			// 清除当前默认launcher
			ArrayList<IntentFilter> intentList = new ArrayList<IntentFilter>();
			ArrayList<ComponentName> cnList = new ArrayList<ComponentName>();
			getPackageManager().getPreferredActivities(intentList, cnList, null);
			IntentFilter dhIF = null;
			for (int i = 0; i < cnList.size(); i++) {
				dhIF = intentList.get(i);
				if (dhIF.hasAction(Intent.ACTION_MAIN) && dhIF.hasCategory(Intent.CATEGORY_HOME)) {
					getPackageManager().clearPackagePreferredActivities(cnList.get(i).getPackageName());
				}
			}

			// 获取所有launcher activity
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			List<ResolveInfo> list = new ArrayList<ResolveInfo>();
			try {
				list = pm.queryIntentActivities(intent, intent.resolveTypeIfNeeded(getContentResolver()),
						PackageManager.MATCH_DEFAULT_ONLY, 0);
			} catch (RemoteException e) {
				throw new RuntimeException("Package manager has died", e);
			}
			// get all components and the best match
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_MAIN);
			filter.addCategory(Intent.CATEGORY_HOME);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			final int N = list.size();
			Log.d(TAG, "N:::::hyhyhyhy:::: = " + N);

			// 设置默认launcher
			ComponentName launcher = new ComponentName(packageName, className);

			ComponentName[] set = new ComponentName[N];
			int defaultMatch = 0;
			for (int i = 0; i < N; i++) {
				ResolveInfo r = list.get(i);
				set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
				Log.d(TAG, "r.activityInfo.packageName:::::hyhyhyhy:::: = " + r.activityInfo.packageName);
				Log.d(TAG, "r.activityInfo.name:::::hyhyhyhy:::: = " + r.activityInfo.name);
				if (launcher.getClassName().equals(r.activityInfo.name)) {
					defaultMatch = r.match;
				}
			}

			try {
				pm.addPreferredActivity(filter, defaultMatch, set, launcher, 0);
			} catch (RemoteException e) {
				throw new RuntimeException("com.coship.factorytest.MainActivity : Package manager has died", e);
			}

		} // end if

	}

	private boolean hasApkInstalled(String pkgname) {
		try {
			this.getPackageManager().getPackageInfo(pkgname, 0);
		} catch (Exception e) {
			Log.d(TAG, "PackageManager.NameNotFoundException: = " + e.getMessage());
			return false;
		}
		return true;
	}
}
