package com.prize.prizethemecenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Administrator on 2016/9/14.
 */
public class ListViewForScrollView extends ListView {

	public ListViewForScrollView(Context context) {
		super(context);
	}

	public ListViewForScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, measureSpec);
	}
}
