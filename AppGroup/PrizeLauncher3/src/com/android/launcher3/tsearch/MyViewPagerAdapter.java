package com.android.launcher3.tsearch;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.Folder;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.GridView;

/**
 * 实现ViewPager页卡
 * 
 * @author jiangqq
 * 
 */
public class MyViewPagerAdapter extends PagerAdapter {
	private List<View> mLists;

	public MyViewPagerAdapter(Context context, List<View> mFolders) {
		this.mLists = mFolders;
	}

	@Override
	public int getCount() {
		return mLists.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(View arg0, int arg1) {
		View v = mLists.get(arg1);
		v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		((ViewPager) arg0).addView(v);
		return mLists.get(arg1);
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		View v = (View) arg2;
		v.setLayerType(View.LAYER_TYPE_NONE, null);
		((ViewPager) arg0).removeView(v);
	}

}
