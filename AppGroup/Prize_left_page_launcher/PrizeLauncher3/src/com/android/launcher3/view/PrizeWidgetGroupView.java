package com.android.launcher3.view;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.util.AttributeSet;

import com.android.launcher3.BubbleTextView;

public class PrizeWidgetGroupView extends BubbleTextView {

	private AppWidgetProviderInfo mInfo;

	private List<Object> mInfos ;

	public PrizeWidgetGroupView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PrizeWidgetGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PrizeWidgetGroupView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void applyFromAppWidgetProviderInfo(List<Object> infos) {
		if (mInfos == null) {
			mInfos = new ArrayList<Object>();
		}
		mInfos.clear();
		mInfos.addAll(infos);
		if(mInfos.size()>0) {
			
		}
		
	}

}
