package com.android.calendar.hormonth;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class CustomViewPagerAdapter<V extends View> extends PagerAdapter {

	private V[] views;

	private CustomDate mCustomDate = new CustomDate();

	private static CustomDate mShowCustomDate = new CustomDate();

	public static boolean IsGotoShow = false;

	DateUtil mDateUtil = DateUtil.dateUtil;

	public CustomViewPagerAdapter(V[] views) {
		super();
		this.views = views;
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		if (((ViewPager) arg0).getChildCount() == views.length) {
			if (views[arg1 % views.length].getParent() != null
					&& views[arg1 % views.length].getParent().equals(arg0)) {
				((ViewPager) arg0).removeView(views[arg1 % views.length]);
			}
		}
		if (views[arg1 % views.length].getParent() != null
				&& !views[arg1 % views.length].getParent().equals(arg0)) {
			((ViewPager) views[arg1 % views.length].getParent())
					.removeView(views[arg1 % views.length]);
		}
		try {
			((ViewPager) arg0).addView(views[arg1 % views.length], 0);
		} catch (IllegalStateException e) {
			// TODO: handle exception
			((ViewPager) views[arg1 % views.length].getParent())
					.removeView(views[arg1 % views.length]);
			((ViewPager) arg0).addView(views[arg1 % views.length], 0);
		}

		if (!IsGotoShow) {
			mCustomDate = ((CalendarView) views[arg1 % views.length])
					.getShowDate();

			int month = mCustomDate.month + arg1
					- ((ViewPager) arg0).getCurrentItem();

			if (month > 12) {
				mShowCustomDate.setMonth(1);
				mShowCustomDate.setYear(mCustomDate.year + 1);
			} else if (month < 1) {
				mShowCustomDate.setMonth(12);
				mShowCustomDate.setYear(mCustomDate.year - 1);
			} else {
				mShowCustomDate.setMonth(month);
				mShowCustomDate.setYear(mCustomDate.year);
			}

			if (mCustomDate.day > mDateUtil.getMonthDays(mShowCustomDate.year,
					mShowCustomDate.month)) {
				mShowCustomDate.setDay(mDateUtil.getMonthDays(
						mShowCustomDate.year, mShowCustomDate.month));
			} else {
				mShowCustomDate.setDay(mCustomDate.day);
			}

//			Log.d("hekeyi","[MonthChangeAnimation] instantiateItem = "+System.currentTimeMillis());
			((CalendarView) views[arg1 % views.length])
					.setViewCurrentDate(mShowCustomDate);
		} else {
			IsGotoShow = false;
		}

		return views[arg1 % views.length];
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startUpdate(View arg0) {
	}

	public V[] getAllItems() {
		return views;
	}
	
	
	
	
	
	
	
	
	
}
