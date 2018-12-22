package com.koobee.koobeecenter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by yiyi on 2015/5/21.
 */
public class InnerGridView extends GridView {

	public InnerGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InnerGridView(Context context) {
		super(context);
	}

	public InnerGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
