package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 自定义scrollview 解决游戏详情中scrollview 自动下滑到底部的bug
 * 
 * @author prize
 * 
 */

public class NonScrollView extends ScrollView {

	public NonScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NonScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonScrollView(Context context) {
		super(context);
	}

	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		return 0;
	}
}
