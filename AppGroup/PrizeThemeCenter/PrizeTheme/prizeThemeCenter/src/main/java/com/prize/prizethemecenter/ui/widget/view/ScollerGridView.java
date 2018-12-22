package com.prize.prizethemecenter.ui.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 解决嵌套只显示部分的GridView
 */
public class ScollerGridView extends GridView {

	public ScollerGridView(Context context) {
		super(context);
	}

	public ScollerGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScollerGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
