package com.android.contacts.prize;

import android.content.Context;
import android.widget.Toast;

/**
 * Control toast display, make the latest toast cover the old toast, do not line
 * up the show
 * 
 * Created by for huangpengfei on 2017-8-4
 */
public class PrizeToastUtils {

	private static Toast mToast;

	public static void showToast(Context context, int resId, int duration) {
		String content = context.getResources().getString(resId);
		if (mToast == null) {
			mToast = Toast.makeText(context, content, duration);
		} else {
			mToast.setText(content);
			mToast.setDuration(duration);
		}

		mToast.show();
	}
}
