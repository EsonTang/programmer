package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 类描述：
 * 
 * @author huanglingjun
 * @version 版本
 */
public class MyInnerScrollView extends ScrollView {
	private ScrollView mParentScrollView;
	private int mLastY;

	public void setParentScrollView(ScrollView scrollView) {
		this.mParentScrollView = scrollView;
	}

	public MyInnerScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = (int) ev.getY();
			if (mParentScrollView.getScrollY() + mParentScrollView.getHeight() == mParentScrollView
					.getChildAt(0).getHeight()) {
				mParentScrollView.requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			int y = (int) ev.getY();
			int dy = y - mLastY;

			if (getScrollY() == 0 && dy > 0) {
				mParentScrollView.requestDisallowInterceptTouchEvent(false);
				return false;
			}
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
			mParentScrollView.requestDisallowInterceptTouchEvent(false);
			break;
		}
		return super.onTouchEvent(ev);
	}

}
