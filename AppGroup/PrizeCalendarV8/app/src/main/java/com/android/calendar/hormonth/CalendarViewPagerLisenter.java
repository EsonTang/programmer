package com.android.calendar.hormonth;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.R;
import com.android.calendar.animation.MainLinearLayout;
import com.android.calendar.hormonth.CalendarView.CallBack;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.LinearLayout;

public class CalendarViewPagerLisenter implements OnPageChangeListener {

	private SildeDirection mDirection = SildeDirection.NO_SILDE;
	int mCurrIndex = 498;
	private static CalendarView[] mShowViews;

	public static int ViewPageCurrentStatus = 0;
	
	private CallBack mCallBack;

	public CalendarViewPagerLisenter(
			CustomViewPagerAdapter<CalendarView> viewAdapter,CallBack mCallBack) {
		super();
		this.mCallBack = mCallBack;
		this.mShowViews = viewAdapter.getAllItems();
	}

	@Override
	public void onPageSelected(int arg0) {
		CustomDate date = mShowViews[arg0 % mShowViews.length].getShowDate();
//		if((date.year>=2037 && date.month>11) || (date.year<1970)) return;
		measureDirection(arg0);
		updateCalendarView(arg0);
		/*PRIZE-获取本月的事件-lixing- 2015-8-21- start */
		mShowViews[arg0 % mShowViews.length].reloadEvents();    //remove by hekeyi for calendar v8.0,resolve the problem of when change pages,the data reload.
		/*PRIZE-获取本月的事件-lixing- 2015-8-21- end */
	}




	MainLinearLayout main_layout;
	private void updateCalendarView(int arg0) {
		if (mDirection == SildeDirection.RIGHT) {
			mShowViews[arg0 % mShowViews.length].rightSilde();
		} else if (mDirection == SildeDirection.LEFT) {
			mShowViews[arg0 % mShowViews.length].leftSilde();
		}
		mDirection = SildeDirection.NO_SILDE;
//		mShowViews[arg0 % mShowViews.length].setPaneHeight();
//		mShowViews[arg0 % mShowViews.length].monthChangeAnimation();
	}

	private void measureDirection(int arg0) {
		if (arg0 > mCurrIndex) {
//			if(mShowViews[arg0 % mShowViews.length].getShowDate().year>=2037 && mShowViews[arg0 % mShowViews.length].getShowDate().month>=12) return;
			mDirection = SildeDirection.RIGHT;
		} else if (arg0 < mCurrIndex) {
			mDirection = SildeDirection.LEFT;
		}
		mCurrIndex = arg0;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		Log.d("hekeyi","[CalendarViewPagerLisenter]-onPageScrolled arg0= "+arg0+" arg1="+arg1+" arg2 = "+arg2);
		if(mShowViews[arg0 % mShowViews.length].getShowDate().year>=2037){   //20171030
			mShowViews[arg0 % mShowViews.length].setScrollX(0);
		}
	}

	public static boolean isScrolling;
	@Override
	public void onPageScrollStateChanged(int arg0) {
//		Log.d("hekeyi","[onPageScrollStateChanged] arg0 = "+arg0+" time = "+System.currentTimeMillis());
		ViewPageCurrentStatus = arg0;
		if(arg0==0){
			mCallBack.clickDate(null);
		}else if(arg0==1){
			/*if(mShowViews[arg0 % mShowViews.length].getShowDate().year>2037){
				isScrolling = false;
			}else {
				isScrolling = true;
			}*/
			isScrolling = true;
		}else if(arg0==2){
			isScrolling=false;
//            mShowViews[arg0 % mShowViews.length].monthChangeAnimation();
		}
//		Log.d("hekeyi","[onPageScrollStateChanged] isScrolling = "+isScrolling);

	}

	enum SildeDirection {
		RIGHT, LEFT, NO_SILDE;
	}


}