package com.prize.music.views;

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

	public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent) {
		if ((paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE)
				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}

		return super.onInterceptTouchEvent(paramMotionEvent);
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if ((paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE)
				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
			getParent().requestDisallowInterceptTouchEvent(true);
		} else if ((paramMotionEvent.getAction() == MotionEvent.ACTION_CANCEL)
				|| (paramMotionEvent.getAction() == MotionEvent.ACTION_UP)) {
			getParent().requestDisallowInterceptTouchEvent(false);
		}

		return super.onTouchEvent(paramMotionEvent);
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int kEvent;

		if (isScrollingLeft(e1, e2)) { // Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else { // Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}

		onKeyDown(kEvent, null);

		return true;
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	public void moveNext() {
		this.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
	}

}
