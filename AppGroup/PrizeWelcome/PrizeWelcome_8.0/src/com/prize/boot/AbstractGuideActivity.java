/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年8月1日
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

import java.lang.reflect.Field;

import com.prize.boot.util.Utils;

import android.R.integer;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 开机引导的各activity需继承该类
 * @author yiyi
 * @version 1.0.0
 */
public abstract class AbstractGuideActivity extends BaseActivity {
	private ImageView mIconImage;
	private TextView mTitleText;
	protected static final String TAG = "AbstractGuideActivity";
	
	private static final int MSG_FINISH = 1;
	private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_FINISH) {
				mHandler.removeMessages(MSG_FINISH);
			}
		}
	};
	
	/**
	 * 设置该页的图片与标题
	 * @param drawableId
	 * @param stringId
	 */
	public final void setGuideTitle(int drawableId, int stringId) {
		initStatusBar();
//		if (mIconImage == null)
			mIconImage = (ImageView) findViewById(R.id.set_icon_image);
//		if (mTitleText == null)
			mTitleText = (TextView) findViewById(R.id.set_text);
		mIconImage.setImageResource(drawableId);
		mTitleText.setText(stringId);
	}
	
	protected void nextStep(boolean isNext) {
        int result = isNext ? Utils.RESULT_CODE_NEXT : Utils.RESULT_CODE_BACK;
        finishActivityByResult(result);
    }

    /**
     * Set result code and finish
     * @param resultCode true to start next step, false to start last step
     */
	protected void finishActivityByResult(int resultCode) {
        Log.d(TAG, "finishActivityByResult, resultCode: " + resultCode);
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "onKeyDown isFinishing()=" + isFinishing());
		if (keyCode == KeyEvent.KEYCODE_BACK && !isFinishing()) {
			if (!mHandler.hasMessages(MSG_FINISH)) {
				mHandler.sendEmptyMessageDelayed(MSG_FINISH, 1000);
				finishActivityByResult(Utils.RESULT_CODE_BACK);
			}
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	
	protected void initStatusBar() {
        Window window = getWindow();
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(getResources().getColor(R.color.white));

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
        }
    }
}
