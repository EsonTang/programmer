package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * 滑动切换activity的layout, 默认开启Slide
 * 
 * @author prize
 *
 */
public class SlideLinearLayout extends LinearLayout {
//	private float mLastMotionX = 0;
//	private Activity thisActivity;
//	private GestureDetector localGestureDetector;
//	private static float slideMinX; // 滑动的最小距离
//	private boolean enableSlide;

	public SlideLinearLayout(Context context) {
		super(context);
//		thisActivity = (Activity) MainApplication.curContext;
//		SlideGestureListener listener = new SlideGestureListener(thisActivity);
//		GestureDetector localGestureDetector = new GestureDetector(
//				thisActivity, listener);
//		this.localGestureDetector = localGestureDetector;
//		slideMinX = applyDimension(20.0F);
//		enableSlide = true;
	}

//	/**
//	 * 设置开启滑动，默认开启，
//	 *
//	 * @param setEnable
//	 *            ： false: 关闭
//	 */
//	public void enableSlide(boolean setEnable) {
//		enableSlide = setEnable;
//	}
//
//	public static float applyDimension(float paramFloat) {
//		DisplayMetrics localDisplayMetrics = Resources.getSystem()
//				.getDisplayMetrics();
//		float valule = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//				paramFloat, localDisplayMetrics);
//		return valule;
//	}
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		if (!enableSlide) {
//			return false;
//		}
//
//		float x = ev.getRawX();
//		int action = ev.getAction();
//		switch (action) {
//		case MotionEvent.ACTION_DOWN:
//			// 首先拦截down事件,记录y坐标
//			mLastMotionX = x;
//			break;
//		case MotionEvent.ACTION_MOVE:
//			// deltaX > 0 右滑动
//			int deltaX = (int) (x - mLastMotionX);
//			if (deltaX > slideMinX) {
//				return true;
//			}
//			break;
//		case MotionEvent.ACTION_UP:
//		case MotionEvent.ACTION_CANCEL:
//			break;
//		}
//		return false;
//	}
//
//	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
//		if (!enableSlide) {
//			return false;
//		}
//		return localGestureDetector.onTouchEvent(paramMotionEvent);
//	}
}
