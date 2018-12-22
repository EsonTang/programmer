package com.prize.prizethemecenter.ui.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by bxh on 2016/9/9.
 */
public class LinearLayoutForDetail extends LinearLayout{

	public LinearLayoutForDetail(Context context) {
		super(context);
	}

	public LinearLayoutForDetail(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LinearLayoutForDetail(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpc = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
