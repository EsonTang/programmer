/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：新建事件未选择全天时的时间选择对话框
 *当前版本：1.0
 *作	者：wanzhijuan
 *完成日期：2015-7-3
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize;

import java.util.Calendar;

import com.android.calendar.R;
import com.android.calendar.widget.ArrayWheelAdapter;
import com.android.calendar.widget.NumericWheelAdapter;
import com.android.calendar.widget.OnWheelChangedListener;
import com.android.calendar.widget.OnWheelScrollListener;
import com.android.calendar.widget.WheelView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimeSelectDialog extends Dialog {

	private ISelectTime mISelectTime;
	private Context mContext;
	private final static String TAG  = "TimeSelectDialog";
	private Button mConfirmButton;
	private Button mCancleButton;
	private TextView mPrizeTitle;
	private TextView mTimeTextView;
	private final int MONTY_DAYS[] = { 31, 28, 31, 30, 31, 30, 31, 31,
			30, 31, 30, 31 };

	private String yearStr;
	private String monthStr;
	private String dayStr;
	private String hoursStr;
	private String minutsStr;
	
	private WheelView mMonthWheelView ;
	private WheelView mDayWheelView;
	private WheelView mHourWheelView;
	private WheelView mMinutWheelView;
	
	private int mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinuts, mCurrentYearToDay;
	private int mYear, mMonth, mDay, tempYear, tempMonth, tempDay;
	private String[] mDatedays;
	private String[] mDatedays100 = new String[201];
	
	private static final int LEAP_MONTH_DAY = 29;
	private static final int LEAP_MONTH = 2;
	
	private String mLabelToday;
	private String[] mWeekdays;
	
	private int mDialogType = -1;
	public static final int DIALOG_START_TIME_TITLE = 1;
	public static final int DIALOG_END_TIME_TITLE = 2;
	public static final int DIALOG_SELECT_DATE_TITLE = 3;
	
	private static final int BASE_YEAR = 100;
	private boolean mIsLeapYear = false;
	private int mMaxYear = 0;
	
	public TimeSelectDialog(Context context, ISelectTime selectTime, int dialogType ,Time mTime) {
		super(context, R.style.prize_event_status);
		Log.v("zwl", "-------->");
		this.mContext = context;
		mDialogType = dialogType;
		this.mISelectTime = selectTime;
		yearStr = mContext.getResources().getString(
				R.string.prize_lunar_year);
		monthStr = mContext.getResources().getString(
				R.string.prize_lunar_month);
		dayStr = mContext.getResources().getString(
				R.string.prize_lunar_day);
		hoursStr = mContext.getResources().getString(
				R.string.prize_lunar_hours);
		minutsStr = mContext.getResources().getString(
				R.string.prize_lunar_minuts);
		mLabelToday = mContext.getResources().getString(
				R.string.today);
		mWeekdays = mContext.getResources().getStringArray(
				R.array.days_in_week);
		
		Calendar c = Calendar.getInstance();
		mCurrentYear = c.get(Calendar.YEAR);
		mYear = mCurrentYear;
		tempYear = mCurrentYear;
		mCurrentMonth = c.get(Calendar.MONTH);
		mMonth = mCurrentMonth;
		tempMonth = mCurrentMonth;
		mCurrentDay = c.get(Calendar.DAY_OF_MONTH);
		mDay = mCurrentDay;
		tempDay = mCurrentDay;
		mCurrentYearToDay = c.get(Calendar.DAY_OF_YEAR);
		mCurrentHours = c.get(Calendar.HOUR_OF_DAY);
		mCurrentMinuts = c.get(Calendar.MINUTE);
		if(mTime != null){
			mCurrentYear = mTime.year;	
			tempYear = mCurrentYear;
			mCurrentMonth = mTime.month;
			tempMonth = mCurrentMonth;
			mCurrentDay = mTime.monthDay;
			tempDay = mCurrentDay;
			mCurrentYearToDay = mTime.yearDay;
			mCurrentHours = mTime.hour;
			mCurrentMinuts = mTime.minute;
		}
		mMaxYear = mCurrentYear + 50;
		Log.v("zwl", "init mMaxYear = " + mMaxYear +", mCurrentYear = " + mCurrentYear);
		mDatedays = new String[getYearToDays(1970, mMaxYear)];
		//initday();
		//initDate();
		initDateTo100();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_select_layout);
		Calendar c = Calendar.getInstance();
		mPrizeTitle = (TextView) findViewById(R.id.name_title);	
		if (mDialogType == DIALOG_START_TIME_TITLE) {
			mPrizeTitle.setText(R.string.prize_start_time);
		} else if(mDialogType == DIALOG_END_TIME_TITLE){
			mPrizeTitle.setText(R.string.prize_end_time);
		} else if(mDialogType == DIALOG_SELECT_DATE_TITLE){
			mPrizeTitle.setText(R.string.prize_select_date);
		}
		
		mTimeTextView = (TextView) findViewById(R.id.time_title);
		
		updateShowTime();
		
		mConfirmButton =  (Button) findViewById(R.id.prize_button_done);
		mCancleButton  =  (Button) findViewById(R.id.prize_button_cancel);
		
		mConfirmButton.setOnClickListener(new ButtonOnClickListener());
		mCancleButton.setOnClickListener(new ButtonOnClickListener());
		
		mMonthWheelView = (WheelView) findViewById(R.id.month);
		mMonthWheelView.setAdapter(new ArrayWheelAdapter<String>(mDatedays100));
		
		mMonthWheelView.setCurrentItem(BASE_YEAR);
		mMonthWheelView.setCyclic(true);
		/*mMonthWheelView.setAdapter(new NumericMonthWheelAdapter(mContext));
		mMonthWheelView.setCurrentItem((mCurrentYear - 1970) * 12 + (mCurrentMonth - 1));
		mMonthWheelView.setCyclic(true);
		
		mDayWheelView = (WheelView) findViewById(R.id.day);
		mDayWheelView.setLabel(mLabelDay);
		initDay();*/
		
		mHourWheelView = (WheelView) findViewById(R.id.hour);
		NumericWheelAdapter hourNumericWheelAdapter = new NumericWheelAdapter(0, 23);
		mHourWheelView.setAdapter(hourNumericWheelAdapter);			
		mHourWheelView.setCurrentItem(mCurrentHours);
		//mHourWheelView.setLabel(mLabelHours);
		mHourWheelView.setCyclic(true);
		
		mMinutWheelView = (WheelView) findViewById(R.id.mins);
		mMinutWheelView.setDrawRightLine(true);
		mMinutWheelView.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		//mMinutWheelView.setLabel(mLabelMinuts);
		mMinutWheelView.setCyclic(true);
		mMinutWheelView.setCurrentItem(mCurrentMinuts);
		
		
		OnWheelChangedListener monthOnWheelChangedListener = new OnWheelChangedListener() {
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				
				int monthSize = mMonthWheelView.getCurrentItem();
				//mCurrentYear = 1970 + monthSize / 12;
				//mCurrentMonth = monthSize % 12 + 1;
				/** 返回的是index**/
				//updateDay();
				//Log.v("zwl", "WheelView oldValue = " + oldValue + ", newValue = " +newValue);
				//updateMonth(oldValue, newValue);
				getCurMonth(newValue);
				//getCurrentDate(newValue);
				updateShowTime();
			}
		};
		
		OnWheelScrollListener monthOnWheelScrollListener = new OnWheelScrollListener() {
			
			@Override
			public void onScrollingStarted(View wheel) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScrollingFinished(View wheel) {
				initDateTo100();
				mMonthWheelView.setAdapter(new ArrayWheelAdapter<String>(mDatedays100));
				mMonthWheelView.setCurrentItem(BASE_YEAR);
				mMonthWheelView.setCyclic(true);
				updateShowTime();
			}
		};
		
		
		OnWheelChangedListener dayOnWheelChangedListener = new OnWheelChangedListener() {
			public void onChanged(View wheel, int oldValue, int newValue) {
				int mDay = mDayWheelView.getCurrentItem() + 1;
				mCurrentDay = mDay;
				updateShowTime();
			}
		};
		
		OnWheelChangedListener hourOnWheelChangedListener = new OnWheelChangedListener() {
			
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				
				mCurrentHours = mHourWheelView.getCurrentItem();
				updateShowTime();
			}
		};
		
		OnWheelChangedListener minutOnWheelChangedListener = new OnWheelChangedListener() {
			
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				
				mCurrentMinuts = mMinutWheelView.getCurrentItem();
				updateShowTime();
			}
		};
		
		mMonthWheelView.addChangingListener(monthOnWheelChangedListener);
		mMonthWheelView.addScrollingListener(monthOnWheelScrollListener);
		//mDayWheelView.addChangingListener(dayOnWheelChangedListener);
		mHourWheelView.addChangingListener(hourOnWheelChangedListener);
		mMinutWheelView.addChangingListener(minutOnWheelChangedListener);
	}
	
	/**prize-by-zhongweilin*/
	private void  getCurMonth(int dayindex){
		Log.v("zwl", "WheelView getCurMonth dayindex = " + dayindex + ", tempYear = " + tempYear+", tempMonth = " +tempMonth
				+", tempDay = "+tempDay);
		int offset = dayindex - BASE_YEAR;
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, tempDay);
		c.set(Calendar.MONTH, tempMonth);
		c.set(Calendar.YEAR, tempYear);
		int basicday = c.get(Calendar.DAY_OF_YEAR);
		Calendar c1 = Calendar.getInstance();
		int yeartoday = 0; 
		if(isLeapYear(tempYear)){
			yeartoday = 366;
		}else{
			yeartoday = 365;
		}
		if(basicday + offset <= 0){
			if(isLeapYear(tempYear-1)){
				yeartoday = 366;
			}else{
				yeartoday = 365;
			}
			c1.set(Calendar.DAY_OF_YEAR, yeartoday + (offset + basicday));
			c1.set(Calendar.YEAR, tempYear-1);
		}else if((offset + basicday)-yeartoday > 0){
			c1.set(Calendar.DAY_OF_YEAR, (offset + basicday) - yeartoday);
			c1.set(Calendar.YEAR, tempYear+1);
		}else{
			c1.set(Calendar.DAY_OF_YEAR, basicday + offset);
			c1.set(Calendar.YEAR, tempYear);
		}
		mCurrentYear = c1.get(Calendar.YEAR);
		mCurrentMonth = c1.get(Calendar.MONTH);
		mCurrentDay = c1.get(Calendar.DAY_OF_MONTH);
	}
	
	private class ButtonOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if(view.getId() == R.id.prize_button_done) {
				if (mISelectTime != null) {
					//mISelectTime.onSelectDone(mIsStart, mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinuts);
					mISelectTime.onSelectDone(mDialogType, mCurrentYear, mCurrentMonth+1, mCurrentDay, mCurrentHours, mCurrentMinuts);
				}
				TimeSelectDialog.this.dismiss();
			}else if(view.getId() == R.id.prize_button_cancel){
				TimeSelectDialog.this.dismiss();
			}
		}
	}
	
	private void updateShowTime(){
		String mTimeString = mCurrentYear + yearStr + (mCurrentMonth+1)
				+ monthStr + mCurrentDay + dayStr + mCurrentHours + hoursStr + mCurrentMinuts + minutsStr;		
		mTimeTextView.setText(mTimeString);
	}
	

	private static boolean isLeapYear(int year) {
		if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
			return true;
		}
		return false;
	}
	
	private void initDateTo100(){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, mCurrentDay);
		c.set(Calendar.MONTH, mCurrentMonth);
		c.set(Calendar.YEAR, mCurrentYear);
		int daytoyear = c.get(Calendar.DAY_OF_YEAR);
		int yeartoday = 0;
		for (int i = 0; i <= BASE_YEAR; i++) {
			if(i == 0){
				if(mCurrentYear == mYear && mCurrentMonth == mMonth && mCurrentDay == mDay){
					mDatedays100[BASE_YEAR] = mLabelToday;
				}else{
					mDatedays100[BASE_YEAR] = getDateValue(mCurrentYear, mCurrentMonth, mCurrentDay);
				}
				continue;
			}
			Calendar c1 = Calendar.getInstance();
			if(daytoyear - i <= 0 ){
				if(isLeapYear(mCurrentYear-1)){
					yeartoday = 366;
				}else{
					yeartoday = 365;
				}
				c1.set(Calendar.DAY_OF_YEAR, yeartoday - (i - daytoyear));
				c1.set(Calendar.YEAR, mCurrentYear-1);
			}else{
				c1.set(Calendar.DAY_OF_YEAR, daytoyear-i);
				c1.set(Calendar.YEAR, mCurrentYear);
			}
			if(c1.get(Calendar.YEAR) == mYear && c1.get(Calendar.MONTH) == mMonth && c1.get(Calendar.DAY_OF_MONTH) == mDay){
				mDatedays100[BASE_YEAR - i] = mLabelToday;
			}else{
				mDatedays100[BASE_YEAR - i] = getDateValue(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH));
			}
			Calendar c2 = Calendar.getInstance();
			if(isLeapYear(mCurrentYear)){
				yeartoday = 366;
			}else{
				yeartoday = 365;
			}
			if((yeartoday - (i+daytoyear)) < 0){
				c2.set(Calendar.DAY_OF_YEAR, (i + daytoyear) - yeartoday);
				c2.set(Calendar.YEAR, mCurrentYear+1);
			}else{
				c2.set(Calendar.DAY_OF_YEAR, (i + daytoyear));
				c2.set(Calendar.YEAR, mCurrentYear);
			}
			if(c2.get(Calendar.YEAR) == mYear && c2.get(Calendar.MONTH) == mMonth && c2.get(Calendar.DAY_OF_MONTH) == mDay){
				mDatedays100[BASE_YEAR + i] = mLabelToday;
			}else{
				mDatedays100[BASE_YEAR + i] = getDateValue(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH));
			}
		}
		tempYear = mCurrentYear;
		tempMonth = mCurrentMonth;
		tempDay = mCurrentDay;
		Log.v("zwl", "calendar year = " + mCurrentYear + ", month = " + mCurrentMonth
				+ ", days = " + mCurrentDay + ", mDatedays100[100] = " + mDatedays100[100]);
	}
	
	
	private static int getYearToDays(int startyear, int endyear){
		int days = 0;
		for (int i = startyear; i < endyear; i++) {
			if(isLeapYear(i)){
				days += 366; 
			}else{
				days += 365;
			}
		}
		Log.v("zwl", "getYearToDays() ----> days = " + days);
		return days;
	}
	private static int getDateToDays(int year, int month, int day){
		int days = 0;
		days = getYearToDays(BASE_YEAR, year);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.YEAR, year);
		days = days + c.get(Calendar.DAY_OF_YEAR);
		Log.v("zwl", "getDateToDays() ----> days = " + days);
		return days;
	}
	private int getDaysToYear(int days){
		for (int i = BASE_YEAR; i < (mYear + 50); i++) {
			int leapday = 0;
			if(isLeapYear(i)){
				leapday = 366;
			}else{
				leapday = 365;
			}
			days = days - leapday;
			if(days <= leapday){
				mCurrentYear = i;
				Log.v("zwl", "getDaysToYear() ----> days = " + days);
				return days-1;
			}
		}
		return 1;
	}
	private void initDate(){
		int index = 0;
		Log.v("zwl", "mDatedays.length = " + mDatedays.length);
		for (int i = BASE_YEAR; i < (mCurrentYear+50); i++) {
			if(isLeapYear(i)){
				mIsLeapYear = true;
			}else{
				mIsLeapYear = false;
			}
			for (int j = 0; j < MONTY_DAYS.length; j++) {
				int length = MONTY_DAYS[j];
				if(mIsLeapYear){
					if(j == (LEAP_MONTH - 1)){
						length = LEAP_MONTH_DAY;
					}
				}
				for (int s = 0; s < length; s++) {
					if(index > (mDatedays.length - 1)){
						continue;
					}
					mDatedays[index] = getDateValue(i, j, s);
					index++;
				}
			}
		}
	}
	
	private String getDateValue(int year, int month, int day){
		return String.valueOf(month + 1) + monthStr + String.valueOf(day) + dayStr + " "
				+ getWeek(year, month, day);
	}
	
	private String getWeek(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.YEAR, year);
		Log.v("zwl", "calendar year = " + c.get(Calendar.YEAR) + ", month = " + (c.get(Calendar.MONTH) + 1)
				+ ", days = " + c.get(Calendar.DAY_OF_MONTH));
		return mWeekdays[c.get(Calendar.DAY_OF_WEEK)-c.getFirstDayOfWeek()];
	}

}

