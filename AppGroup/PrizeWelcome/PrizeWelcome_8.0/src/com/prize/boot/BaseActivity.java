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
 *修 改 人：zhongweilin
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/
package com.prize.boot;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 **
 * 基类：所有的activity需继承此类，包含activity统一退出，沉浸式状态栏设置等
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class BaseActivity extends Activity {
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		overridePendingTransition(0, 0);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		overridePendingTransition(0, 0);
	}
}
