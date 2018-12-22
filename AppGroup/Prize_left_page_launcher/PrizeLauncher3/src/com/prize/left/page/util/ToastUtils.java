package com.prize.left.page.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {

	/**
	 * Ui线程/非UI线程中显示 Toast
	 */
	public static void showToast(Context ctx, final int strID) {
		showToast(ctx, strID, Gravity.BOTTOM);
	}

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(Context ctx, final String str) {
		showToast(ctx, str, Gravity.BOTTOM);
	}

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(Context ctx, final int strID, final int gravity) {
		showToast(ctx, ctx.getString(strID), gravity);
	}

	private static Toast toast = null;

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(Context ctx, final String str, final int gravity) {
		if (toast == null) {
			try {
				toast = Toast.makeText(ctx, str,
						Toast.LENGTH_SHORT);
				if (gravity == Gravity.BOTTOM) {
					toast.setGravity(gravity, 0, 100);
				} else {
					toast.setGravity(gravity, 0, 0);
				}
				toast.show();
			} catch (Exception e) {
			}
		} else {
			toast.cancel();
			toast = null;
			showToast(ctx, str, gravity);
		}
	}

	/**
	 * 错误
	 * @param msgId
	 */
	public static void showErrorToast(Context ctx, int msgId) {
		// HTC需求，修改为普通的toast
		showToast(ctx, msgId);
	}

}
