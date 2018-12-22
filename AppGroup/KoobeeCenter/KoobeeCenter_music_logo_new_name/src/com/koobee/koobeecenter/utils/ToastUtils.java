package com.koobee.koobeecenter.utils;

import com.koobee.koobeecenter02.R;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	private static android.widget.Toast toast;

	public static void showShortToast(Context context, String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
	}

	public static void showLongToast(Context context, String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	public static void showOnceToast(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		} else {
			toast.setText(text);
		}
		toast.show();
	}

	public static void showOnceLongToast(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		} else {
			toast.setText(text);
		}
		toast.show();
	}
	
	public static void showOnceToast(Context context, int id) {
		if (toast == null) {
			toast = Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT);
		} else {
			toast.setText(context.getString(id));
		}
		toast.show();
	}
}
