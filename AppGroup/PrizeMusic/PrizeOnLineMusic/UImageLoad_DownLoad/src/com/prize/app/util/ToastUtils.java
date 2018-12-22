package com.prize.app.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.BaseApplication;

public class ToastUtils {

	private static android.widget.Toast toast;

	public static void showShortToast(Context context, String message) {
		if (!TextUtils.isEmpty(message)) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
	}

	public static void showLongToast(Context context, String message) {
		if (!TextUtils.isEmpty(message)) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	public static void showOnceToast(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			TextView v = (TextView) toast.getView().findViewById(android.R.id.message); 
			v.setTextSize(15);
		} else {
			toast.setText(text);
		}
		toast.show();
	}

	public static void showOnceLongToast(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
			TextView v = (TextView) toast.getView().findViewById(android.R.id.message); 
			v.setTextSize(15);
		} else {
			toast.setText(text);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * Ui�߳�/��UI�߳�����ʾ Toast
	 */
	public static void showToast(final int strID) {
		showToast(strID, Gravity.BOTTOM);
	}

	/**
	 * UI�߳�/��UI�߳̾�ɵ��� ��ʾ Toast
	 */
	public static void showToast(final String str) {
		showToast(str, Gravity.BOTTOM);
	}

	/**
	 * UI�߳�/��UI�߳̾�ɵ��� ��ʾ Toast
	 */
	public static void showToast(final int strID, final int gravity) {
		showToast(BaseApplication.curContext.getString(strID), gravity);
	}

	private static Context context = null;

	/**
	 * UI�߳�/��UI�߳̾�ɵ��� ��ʾ Toast
	 */
	public static void showToast(final String str, final int gravity) {
		if (toast == null) {
			try {
				context = BaseApplication.curContext;
				toast = Toast.makeText(BaseApplication.curContext, str,
						Toast.LENGTH_SHORT);
				TextView v = (TextView) toast.getView().findViewById(android.R.id.message); 
				v.setTextSize(15);
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
	 * ����
	 * 
	 * @param msgId
	 */
	public static void showErrorToast(int msgId) {
		// showTipToast(msgId, R.drawable.toast_result_error);
		// HTC�����޸�Ϊ��ͨ��toast
		showToast(msgId);
	}

}
