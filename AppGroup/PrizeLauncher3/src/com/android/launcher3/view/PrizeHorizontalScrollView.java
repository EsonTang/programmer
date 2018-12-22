package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.HorizontalScrollView;

public class PrizeHorizontalScrollView extends HorizontalScrollView {
	private static final int MAX_SCROLL_HEIGHT = 280;
	private VelocityTracker mVelocityTracker;

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();// 读取速率
		}
		mVelocityTracker.addMovement(ev);// 增加MoventEvent
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		acquireVelocityTrackerAndAddMovement(ev);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
			releaseVelocityTracker();
			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		return super.onTouchEvent(ev);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.clear();
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PrizeHorizontalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

		int newScrollX = scrollX + deltaX;

		int newScrollY = scrollY + deltaY;

		// Clamp values if at the limits and record
		final int left = -maxOverScrollX - MAX_SCROLL_HEIGHT;
		final int right = maxOverScrollX + scrollRangeX + MAX_SCROLL_HEIGHT;
		final int top = -maxOverScrollY;
		final int bottom = maxOverScrollY + scrollRangeY;

		boolean clampedX = false;
		if (newScrollX > right) {
			newScrollX = right;
			clampedX = true;
		} else if (newScrollX < left) {
			newScrollX = left;
			clampedX = true;
		}

		boolean clampedY = false;
		if (newScrollY > bottom) {
			newScrollY = bottom;
			clampedY = true;
		} else if (newScrollY < top) {
			newScrollY = top;
			clampedY = true;
		}

		onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

		return clampedX || clampedY;
	}

}
