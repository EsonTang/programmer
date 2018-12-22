package com.android.appwidget;

import com.android.launcher3.Launcher;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;

public class AppWidgetHostView {
	public static Rect getDefaultPaddingForWidget(Context context,
			ComponentName cn, String s) {
		Rect r = new Rect();
		r.top = (int) (Launcher.scale * 8);
		r.left = (int) (Launcher.scale * 8);
		r.right = (int) (Launcher.scale * 8);
		r.bottom = (int) (Launcher.scale * 8);
		return r;

	}
}
