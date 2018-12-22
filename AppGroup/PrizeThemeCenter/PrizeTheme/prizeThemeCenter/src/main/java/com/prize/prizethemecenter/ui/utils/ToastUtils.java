package com.prize.prizethemecenter.ui.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;

public class ToastUtils {
	private static Toast tipToast = null;

	/**
	 * Ui线程/非UI线程中显示 Toast
	 */
	public static void showToast(final int strID) {
		showToast(strID, Gravity.BOTTOM);
	}

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(final String str) {
		showToast(str, Gravity.BOTTOM);
	}

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(final int strID, final int gravity) {
		showToast(BaseApplication.curContext.getString(strID), gravity);
	}

	private static Toast toast = null;
	private static Context context = null;

	/**
	 * UI线程/非UI线程均可调用 显示 Toast
	 */
	public static void showToast(final String str, final int gravity) {
		if (toast == null) {
			try {
				context = MainApplication.curContext;
				toast = Toast.makeText(BaseApplication.curContext, str,
						Toast.LENGTH_SHORT);
				if (gravity == Gravity.BOTTOM) {
					toast.setGravity(gravity, 0, 100);
				} else {
					toast.setGravity(gravity, 0, 0);
				}
				toast.show();
			} catch (Exception e) {
				BaseApplication.handler.post(new Runnable() {
					@Override
					public void run() {
						showToast(str, gravity);
					}
				});
			}
		} else {
			toast.cancel();
			toast = null;
			showToast(str, gravity);
		}
	}

	/**
	 * 错误
	 * 
	 * @param msgId
	 */
	public static void showErrorToast(int msgId) {
		// showTipToast(msgId, R.drawable.toast_result_error);
		// HTC需求，修改为普通的toast
		showToast(msgId);
	}

	/**
	 * tip Toast
	 * 
	 * @param msgId
	 * @param iconId
	 */
	public static void showTipToast() {

		if (tipToast == null) {
			try {
				context = MainApplication.curContext;
				tipToast = new Toast(context);
				View layout = LayoutInflater.from(context).inflate(
						R.layout.toast_result, null);
				ImageView icon = (ImageView) layout
						.findViewById(R.id.toast_image);
				// 设置Toast的位置
				tipToast.setGravity(Gravity.CENTER, 0, 0);
				tipToast.setDuration(Toast.LENGTH_SHORT);
				// 让Toast显示为我们自定义的样子
				tipToast.setView(layout);
				tipToast.show();
			} catch (Exception e) {
				MainApplication.handler.post(new Runnable() {
					@Override
					public void run() {
						showTipToast();
					}
				});
			}
		} else {
			tipToast.cancel();
			tipToast = null;
			showTipToast();
		}
	}
}
