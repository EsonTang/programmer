package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class DialogScrollListView extends ListView {
	private static final String TAG = "DialogScrollListView";
	
	public DialogScrollListView(Context context) {
		super(context);
	}

	public DialogScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DialogScrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d(TAG, "onTouchEvent()");
		return super.onTouchEvent(ev);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
