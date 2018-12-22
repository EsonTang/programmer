package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 
 * 首页顶部推荐游戏的控件实现，实现图片倒影和横拖功能
 * 
 */
public class GalleryFlow extends Gallery {

	public GalleryFlow(Context context) {
		super(context);
	}

	public GalleryFlow(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

//	public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent) {//解决49386 【V3.3-应用市场】推荐页轮播轮区域下滑不能下拉刷新
//		if ((paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE)
//				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
//			getParent().requestDisallowInterceptTouchEvent(true);
//		}
//
//		return super.onInterceptTouchEvent(paramMotionEvent);
//	}

//	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
//		if ((paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE)
//				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
//			getParent().requestDisallowInterceptTouchEvent(true);
//		} else if ((paramMotionEvent.getAction() == MotionEvent.ACTION_CANCEL)
//				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_UP)) {
//			getParent().requestDisallowInterceptTouchEvent(false);
//		}
//
//		return super.onTouchEvent(paramMotionEvent);
//	}
//	@Override
//	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//			float velocityY) {
//		int kEvent;
//
//		if (isScrollingLeft(e1, e2)) { // Check if scrolling left
//			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
//		} else { // Otherwise scrolling right
//			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
//		}
//
//		onKeyDown(kEvent, null);
//
//		return true;
//	}
	/**
	 * 滑屏监测
	 * @param e1 手势起点的移动事件
	 * @param e2 当前手势点的移动事件
	 * @param velocityX .每秒x轴方向移动的像素
	 * @param velocityY 每秒y轴方向移动的像素
	 * @return boolean
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						   float velocityY) {
		float minMove = 50;         //最小滑动距离
//		float minVelocity = 0;      //最小滑动速度
		float beginX = e1.getX();
		float endX = e2.getX();
//		float beginY = e1.getY();
//		float endY = e2.getY();
		if(beginX-endX>minMove&&Math.abs(velocityX)>velocityY){   //左滑
			onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
			return true;
		}else if(endX-beginX>minMove&&Math.abs(velocityX)>velocityY){   //右滑
			onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
			return true;
		}

		return false;
	}


	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	public void moveNext() {
		this.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
	}

}
