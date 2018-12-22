package com.prize.lockscreen.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class NumberView extends TextView {

	public NumberView(Context context) {
		super(context);
		init();
	}

	public NumberView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public NumberView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {

	}

	@Override
	public boolean onHoverEvent(MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_HOVER_ENTER:
			System.out.println("hover_enter------>" + getTag());
			performClick();
			break;
		case MotionEvent.ACTION_HOVER_MOVE:
			System.out.println("hover_move------>" + getTag());

			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			System.out.println("hover_exit------>" + getTag());

			break;
		}
		return super.onHoverEvent(event);
	}

}
