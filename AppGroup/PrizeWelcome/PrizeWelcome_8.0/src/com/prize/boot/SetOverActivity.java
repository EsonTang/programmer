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

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;

public class SetOverActivity extends AbstractGuideActivity {

	SubscriptionManager subscriptionManager = null;
	
	private TextView titleText;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("prize","~~Prize Welcome ~~~SetOverActivity ---------->onCreate()");
		setContentView(R.layout.layout_complete);
		setGuideTitle(R.drawable.complete_icon, R.string.set_complete);
		subscriptionManager = SubscriptionManager.from(this);
		titleText = (TextView)findViewById(R.id.set_text);
		int paddingTop = (int)getResources().getDimension(R.dimen.setover_padding_top);
		titleText.setPadding(0, paddingTop, 0, 0);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v("prize","~~Prize Welcome ~~~SetOverActivity ---------->onStop()");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v("prize","~~Prize Welcome ~~~SetOverActivity ---------->onDestroy()");
		super.onDestroy();
	}

	public void onClick(View v) {
		if (v.getId() == R.id.next_btn) {
			try {
				int subid = subscriptionManager.getDefaultSubId();
				Log.v("prize","~~Prize Welcome ~~~SetOverActivity ---------->subid = " + subid);
				if(subid != 1){
					isSimExist();
				}
				finishSetupWizard();
				/**modified by xiarui for system UI bug 51774 20180313-start*/
				//startLauncher(SetOverActivity.this,false);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(2), 200);    //xiarui
				/**modified by xiarui for system UI bug 51774 20180313-end*/
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setResult(Utils.RESULT_CODE_FINISH);
			getSharedPreferences("onekeyinstall", Activity.MODE_PRIVATE).edit().putBoolean("unInstalled", false).commit();
			/**modified by xiarui for system UI bug 51774 20180313-start*/
	        //finish();
			mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 10);   //xiarui
			/**modified by xiarui for system UI bug 51774 20180313-end*/
		} else if (v.getId() == R.id.im_back) {
			nextStep(false);
			finish();
		}
	}
	private Handler mHandler = new Handler() {
		  public void dispatchMessage(Message msg) {
		   if (1 == msg.what) {
			   setResult(Utils.RESULT_CODE_FINISH);
	           finish();
		   }else if(2 == msg.what){
			   startLauncher(SetOverActivity.this,false);
		   }
		  };  
	};
	
	public static void startLauncher(Context context,boolean cooee) {
		Log.v("prize","~~Prize Welcome ~~~startLauncher() ---------->start");
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        Log.v("prize","~~Prize Welcome before~~~i.getPackage() = "+i.getPackage());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
        Log.v("prize","~~Prize Welcome ~~~startLauncher() ---------->end");
    }
	
	private void finishAllActivity(){
		Log.v("prize","~~Prize Welcome ~~~finishAllActivity() ---------->start");
		Utils.saveBootStatus(getApplicationContext(), false);
		WelcomeApplication.getInstance().finishAllActivity();
		Log.v("prize","~~Prize Welcome ~~~finishAllActivity() ---------->end");
	}

	private void finishSetupWizard() {
		Log.v("prize","~~Prize Welcome ~~~finishSetupWizard() ---------->start");
		// remove this activity from the package manager.
		PackageManager pm = getPackageManager();
		ComponentName name = new ComponentName(this, WizardActivity.class);
		int state = pm.getComponentEnabledSetting(name);
		if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
			pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
		Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
//		Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);  //add by hekeyi 20180124
		Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
		int  device = Settings.Global.getInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, -1);
		int setup = Settings.Secure.getInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, -1);
		Log.v("prize","~~Prize Welcome ~~~finishSetupWizard() ---------->device = ");
		Log.v("prize","~~Prize Welcome ~~~finishSetupWizard() ---------->device = "+device +"   setup = "+setup);
		Log.v("prize","~~Prize Welcome ~~~finishSetupWizard() ---------->end");
	}
	
	private boolean isSimExist() {
        CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
		telephonyInfo.setCTelephoneInfo();
		boolean sim1State = telephonyInfo.isSIM1Ready();
		boolean sim2State = telephonyInfo.isSIM2Ready();
		Log.v(Utils.TAG, "--zwl--> selPosDefault: setDefaultDataSubId sim1State = "+sim1State+" sim2State = "+sim2State);
		if(sim2State && !sim1State){
			Log.v(Utils.TAG, "--zwl--> selPosDefault: setDefaultDataSubId");
	        subscriptionManager.setDefaultDataSubId(1);
		}
		return (sim1State || sim2State);
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
