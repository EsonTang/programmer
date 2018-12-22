package com.android.launcher3.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;

public class MyExpandableListView extends ExpandableListView {

	public MyExpandableListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean performItemClick(View v, int position, long id) {
		// TODO Auto-generated method stub
		return false;// super.performItemClick(v, position, id);
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
	}

	public MyExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setIndicatorBounds(0, 0);
		setGroupIndicator(null);
		// TODO Auto-generated constructor stub
	}
/*
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}*/

	public MyExpandableListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setIndicatorBounds(int left, int right) {
		// TODO Auto-generated method stub
		super.setIndicatorBounds(left, right);
	}

}
