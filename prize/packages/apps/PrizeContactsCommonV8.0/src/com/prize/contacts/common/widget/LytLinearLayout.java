package com.prize.contacts.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LytLinearLayout extends LinearLayout {
	public static final int LEFT_RIGHT = 0;
	public static final int RIGHT_LEFT = 1;
	private static final int SLIDE_GAP_X = 50;
	private static final int SLIDE_GAP_Y = 20;
	private static final int SLIDE_GAP = 80;
	private float mStartX;
	private float mStartY;
	private SlidingListener mSlidingListener;

	public LytLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartX = ev.getX();
			mStartY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float gapX = ev.getX() - mStartX;
			float gapY = ev.getY() - mStartY;
			if ((gapX > SLIDE_GAP_X || gapX < -SLIDE_GAP_X) && gapY > -SLIDE_GAP_Y
					&& gapY < SLIDE_GAP_Y && mSlidingListener != null) {
				return true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float gap = event.getX() - mStartX;
			if (mSlidingListener != null) {
				if (gap > SLIDE_GAP) {
					mSlidingListener.slidingDirection(LEFT_RIGHT);
				} else if (gap < -SLIDE_GAP) {
					mSlidingListener.slidingDirection(RIGHT_LEFT);
				}
			}
		}
		return super.onTouchEvent(event);
	}

	public void setSlidingListener(SlidingListener slidingListener) {
		mSlidingListener = slidingListener;
	}

	public interface SlidingListener {
		public void slidingDirection(int direction);
	}
}