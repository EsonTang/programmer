package com.prize.weather;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class WeatherViewPagerAdapter extends PagerAdapter {
	
	List<View> viewLists;
	
	public WeatherViewPagerAdapter(List<View> lists) {
		this.viewLists = lists;
	}

	@Override
	public int getCount() {
		return viewLists.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(View view, int position, Object object) {
		((ViewPager) view).removeView(viewLists.get(position));
	}

	@Override
	public Object instantiateItem(View view, int position) {
		((ViewPager) view).addView(viewLists.get(position), 0);
		return viewLists.get(position);
	}
	
}
