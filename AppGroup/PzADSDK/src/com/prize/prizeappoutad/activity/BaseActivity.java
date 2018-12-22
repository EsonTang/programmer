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
package com.prize.prizeappoutad.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 ** 
 * 基类：所有的activity需继承此类，包含activity统一退出，沉浸式状态栏设置等
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class BaseActivity extends Activity {
	/**
	 * 用于退出Activity的广播action
	 */
	// protected static final String EXITAPP = "prize.intent.action.exit_cloud";
	// protected BroadcastReceiver receiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// if (action.equals(EXITAPP)) {
	// BaseActivity.this.finish();
	// }
	// }
	// };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// IntentFilter intent = new IntentFilter(EXITAPP);
		// LocalBroadcastManager.getInstance(getApplicationContext())
		// .registerReceiver(receiver, intent);

		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		// //透明状态栏
		// window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		// //透明导航栏
		// window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		// SystemBarTintManager tintManager = new SystemBarTintManager(this);
		// tintManager.setStatusBarTintColor(this.getResources().getColor(R.color.title_color));
		// tintManager.setStatusBarTintEnabled(true);
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

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
