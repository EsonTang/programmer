package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutForDetail extends LinearLayout {
	public LinearLayoutForDetail(Context context) {
		super(context);
	}

	public LinearLayoutForDetail(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LinearLayoutForDetail(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
