package com.prize.prizethemecenter.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SlideGestureListener extends SimpleOnGestureListener {

	private Context ctx;
	private float floatX;
	private float floatY;

	public SlideGestureListener(Context paramContext) {
		this.ctx = paramContext;
		floatX = SlideLinearLayout.applyDimension(200.0F);
		floatY = SlideLinearLayout.applyDimension(300.0F);
	}

	public final boolean onDown(MotionEvent paramMotionEvent) {
		return true;
	}

	public final boolean onFling(MotionEvent paramMotionEvent1,
			MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {

		// Y方向不能速度太快
		paramFloat2 = Math.abs(paramFloat2);

		if ((paramFloat1 > floatX) && (this.ctx instanceof Activity)
				&& (paramFloat2 < floatY)) {
			Activity localActivity = (Activity) this.ctx;
			if (!localActivity.isFinishing())
				localActivity.onBackPressed();
		}
		return super.onFling(paramMotionEvent1, paramMotionEvent2, paramFloat1,
				paramFloat2);
	}
}
