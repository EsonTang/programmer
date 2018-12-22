package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

public class MyHorizontalScrollView extends HorizontalScrollView {
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_HORIZONTAL_SCROLLING = 1;
	private static final int TOUCH_STATE_VERTICAL_SCROLLING = -1;
	private float mLastMotionX;
	private float mLastMotionY;
	private int mTouchSlop;
	private int mTouchState = TOUCH_STATE_REST;

	public MyHorizontalScrollView(Context context) {
		super(context);
		init();
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		// 设置HorizontalScrollView滑动到最左端和最右端时不做监听器处理

		/*
		 * this.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { switch
		 * (event.getAction()) { // 如果触动屏幕就执行 case MotionEvent.ACTION_MOVE :
		 * View view = ((HorizontalScrollView) v).getChildAt(0);
		 * 
		 * // 判断是否滑动最右端 if (view.getMeasuredWidth() <= v.getScrollX() +
		 * v.getWidth()) { getParent().requestDisallowInterceptTouchEvent(
		 * false); } else { getParent()
		 * .requestDisallowInterceptTouchEvent(true); } // 判断是否滑动最左端 if
		 * (v.getScrollX() <= 0) {
		 * getParent().requestDisallowInterceptTouchEvent( false); } else {
		 * getParent() .requestDisallowInterceptTouchEvent(true); }
		 * 
		 * break; } return false; } });
		 */
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
				getParent().requestDisallowInterceptTouchEvent(true);
			} else if (mTouchState == TOUCH_STATE_VERTICAL_SCROLLING) {
				getParent().requestDisallowInterceptTouchEvent(false);
			} else {
				final float x = ev.getX();
				final int xDiff = (int) Math.abs(x - mLastMotionX);
				boolean xMoved = xDiff > mTouchSlop;
				final float y = ev.getY();
				final int yDiff = (int) Math.abs(y - mLastMotionY);
				boolean yMoved = yDiff > mTouchSlop;
				if (xMoved) {
					if (xDiff >= yDiff)
						mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
					mLastMotionX = x;
				}
				if (yMoved) {
					if (yDiff > xDiff)
						mTouchState = TOUCH_STATE_VERTICAL_SCROLLING;
					mLastMotionY = y;
				}
			}

			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			getParent().requestDisallowInterceptTouchEvent(false);
			break;
		case MotionEvent.ACTION_DOWN:
			mTouchState = TOUCH_STATE_REST;
			mLastMotionY = ev.getY();
			mLastMotionX = ev.getX();
			getParent().requestDisallowInterceptTouchEvent(true);
			break;
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}
}
