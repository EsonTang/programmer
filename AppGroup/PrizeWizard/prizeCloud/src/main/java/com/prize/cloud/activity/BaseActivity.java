/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.cloud.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.prize.cloud.R;
import com.prize.cloud.util.AppManager;

/**
 **
 * 基类：所有的activity需继承此类，包含activity统一退出，沉浸式状态栏设置等
 * 
 * @author yiyi
 * @version 1.0.0
 *
 */
public class BaseActivity extends Activity {
	/**
	 * 用于退出Activity的广播action
	 */
	protected static final String EXITAPP = "prize.intent.action.exit_cloud";
	protected BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(EXITAPP)) {
				BaseActivity.this.finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		initStatusBar();
		IntentFilter intent = new IntentFilter(EXITAPP);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(receiver, intent);
	}

	/**
	 * 方法描述：沉浸式状态栏
	 */
	@SuppressLint("NewApi")
	private void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (!AppManager.isThird) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
						| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				window.getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(getResources().getColor(R.color.title_color));
				return;
			}
		}
		
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			//透明状态栏  
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  
//	        //透明导航栏  
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION); 
//			SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintColor(this.getResources().getColor(R.color.title_color));
//            tintManager.setStatusBarTintEnabled(true);
//	    }
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(receiver);
	}

	public void onBackClk(View v) {
		onBackPressed();
	}
	
	public void hideSoftInput() {
		View view = this.getWindow().peekDecorView();
		if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
	}
}
