package com.prize;

import java.util.Calendar;

import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.widget.NumericWheelAdapter;
import com.android.calendar.widget.OnWheelChangedListener;
import com.android.calendar.widget.WheelView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DateSelectDialog extends Dialog {
	private ISelectTime mISelectTime;
	private Context mContext;
	private final static String TAG  = "TimeSelectDialog";
	private Button mConfirmButton;
	private Button mCancleButton;
	private TextView mPrizeTitle;
	private TextView mTimeTextView;
	private final int MONTY_DAYS[] = { 31, 28, 31, 30, 31, 30, 31, 31,
			30, 31, 30, 31 };
	private Time time;
	private String yearStr;
	private String monthStr;
	private String dayStr;
	private String hoursStr;
	private String minutsStr;
	
	private String mLabelDay;
	private String mLabelMonth;
	private String mLabelYear;
	private String[] mWeekdays;
	
	private WheelView mMonthWheelView ;
	private WheelView mDayWheelView;
	private WheelView mYearWheelView;
	
	private int mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinuts, mCurrentYearToDay, mYear,mCurrentWeek;
	private String[] monthdays = new String[365];
	private String[] monthdaysleap = new String[366];
	private String[] curmonthdays = new String[365];
	private String[] curmonthdaysleap = new String[366];
	private int indexmonths = 0;
	
	private boolean mIsStart;
	private static final int DAY_MIN = 1;
	private static final int MONTH_MIN = 1;
	private static final int YEAR_RANGE = 100;
	private static final int LEAP_MONTH_DAY = 29;
	private static final int LEAP_MONTH = 1;//prize-public-bug:Feb -pengcancan-20160805
	
	private static final int BASE_YEAR = 1970;
	
	private int mDialogType = -1;
	public static final int DIALOG_START_TIME_TITLE = 1;
	public static final int DIALOG_END_TIME_TITLE = 2;
	public static final int DIALOG_SELECT_DATE_TITLE = 3;

	private CalendarController mCalendarController;

	public DateSelectDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public DateSelectDialog(Context context, ISelectTime selectTime, int dialogType ,Time mTime) {
		super(context, R.style.prize_event_status);
		Log.v("zwl", "DateSelectDialog-------->");
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
		mLabelDay = mContext.getResources().getString(
				R.string.label_day);
		mLabelMonth = mContext.getResources().getString(
				R.string.label_month);
		mLabelYear = mContext.getResources().getString(
				R.string.label_year);
		mWeekdays = mContext.getResources().getStringArray(
				R.array.days_in_week);
		
		Calendar c = Calendar.getInstance();
		mCurrentYear = c.get(Calendar.YEAR);
		mYear = mCurrentYear;
		mCurrentMonth = c.get(Calendar.MONTH);
		mCurrentDay = c.get(Calendar.DAY_OF_MONTH);
		mCurrentYearToDay = c.get(Calendar.DAY_OF_YEAR);
		mCurrentWeek = c.get(Calendar.WEEK_OF_MONTH);
		
		if(mTime != null){
			mCurrentYear = mTime.year;	
			mYear = mCurrentYear;
			mCurrentMonth = mTime.month;
			mCurrentDay = mTime.monthDay;
			mCurrentYearToDay = mTime.yearDay;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_select_layout);
		mCalendarController = CalendarController.getInstance(mContext);
		Calendar c = Calendar.getInstance();
		mPrizeTitle = (TextView) findViewById(R.id.date_name_title);	
		if (mDialogType == DIALOG_START_TIME_TITLE) {
			mPrizeTitle.setText(R.string.prize_start_time);
		} else if(mDialogType == DIALOG_END_TIME_TITLE){
			mPrizeTitle.setText(R.string.prize_end_time);
		} else if(mDialogType == DIALOG_SELECT_DATE_TITLE){
			mPrizeTitle.setText(R.string.prize_select_date);
		}
		
		mTimeTextView = (TextView) findViewById(R.id.date_title);
		
		updateShowTime();
		
		mConfirmButton =  (Button) findViewById(R.id.prize_button_date_done);
		mCancleButton  =  (Button) findViewById(R.id.prize_button_date_cancel);
		
		mConfirmButton.setOnClickListener(new ButtonOnClickListener());
		mCancleButton.setOnClickListener(new ButtonOnClickListener());
		
		mYearWheelView = (WheelView) findViewById(R.id.date_year);
//		mYearWheelView.setAdapter(new NumericWheelAdapter(BASE_YEAR, mCurrentYear + 50));
		mYearWheelView.setAdapter(new NumericWheelAdapter(BASE_YEAR, mCurrentYear + 20));
		mYearWheelView.setCurrentItem(mCurrentYear - BASE_YEAR);
		mYearWheelView.setLabel(mLabelYear);
		mYearWheelView.setCyclic(true);

		mMonthWheelView = (WheelView) findViewById(R.id.date_month);
		mMonthWheelView.setAdapter(new NumericWheelAdapter(1, 12));
		mMonthWheelView.setCurrentItem(mCurrentMonth);
		mMonthWheelView.setLabel(mLabelMonth);
		mMonthWheelView.setCyclic(true);
		
		
		mDayWheelView = (WheelView) findViewById(R.id.date_day);
		mDayWheelView.setLabel(mLabelDay);
		initDay();
		
		OnWheelChangedListener monthOnWheelChangedListener = new OnWheelChangedListener() {
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				mCurrentMonth = mMonthWheelView.getCurrentItem();
				Log.v("zwl", "DateSelectDialog-------->mCurrentMonth = " + mCurrentMonth);
				/** 返回的是index**/
				updateDay();
				updateShowTime();
			}
		};
		
		
		OnWheelChangedListener dayOnWheelChangedListener = new OnWheelChangedListener() {
			public void onChanged(View wheel, int oldValue, int newValue) {
				mCurrentDay = mDayWheelView.getCurrentItem() + 1;
				updateShowTime();
			}
		};
		
		OnWheelChangedListener yearOnWheelChangedListener = new OnWheelChangedListener() {
			
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				int index = mYearWheelView.getCurrentItem();
				mCurrentYear = mCurrentYear + (index - (mCurrentYear - BASE_YEAR));
				updateShowTime();
			}
		};
		
		mMonthWheelView.addChangingListener(monthOnWheelChangedListener);
		mDayWheelView.addChangingListener(dayOnWheelChangedListener);
		mYearWheelView.addChangingListener(yearOnWheelChangedListener);
	}
	
	
	private class ButtonOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if(view.getId() == R.id.prize_button_date_done) {
				if (mISelectTime != null) {
					mISelectTime.onSelectDone(mDialogType, mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHours, mCurrentMinuts);
				}
				DateSelectDialog.this.dismiss();
			}else if(view.getId() == R.id.prize_button_date_cancel){
				DateSelectDialog.this.dismiss();
			}
		}
	}
	
	/**
	 * 
	 * 方法描述：初始化日期，日期与年、月相关
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void initDay() {
		int maxMonth = MONTY_DAYS[mCurrentMonth];
		int minMonth = MONTH_MIN;
		if (mCurrentMonth == LEAP_MONTH && (mCurrentYear % 4 == 0 && mCurrentYear % 100 != 0 || mCurrentYear % 400 == 0)) {
			maxMonth = LEAP_MONTH_DAY;
		}
		mDayWheelView.setAdapter(new NumericWheelAdapter(minMonth, maxMonth));
		mDayWheelView.setCurrentItem(mCurrentDay - DAY_MIN);
		mDayWheelView.setCyclic(true);
	}
	
	/**
	 * 
	 * 方法描述：年、月发生改变时，更新日期
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void updateDay() {
		int maxMonth = MONTY_DAYS[mCurrentMonth];//prize-public-bug:19458 crash -pengcancan-20160805
		int minMonth = MONTH_MIN;
		if (mCurrentMonth == LEAP_MONTH && (mCurrentYear % 4 == 0 && mCurrentYear % 100 != 0 || mCurrentYear % 400 == 0)) {
			maxMonth = LEAP_MONTH_DAY;
		}
		mDayWheelView.setAdapter(new NumericWheelAdapter(minMonth, maxMonth));
		if (mDayWheelView.getCurrentItem() + 1 > maxMonth) {
			mDayWheelView.setCurrentItem(maxMonth - minMonth);
		}
		mDayWheelView.setCyclic(true);
	}
	
	private void updateShowTime(){
		String mTimeString = (mCurrentYear) + yearStr + (mCurrentMonth+1)
				+ monthStr + mCurrentDay + dayStr + "  " + getWeek();		
		mTimeTextView.setText(mTimeString);
	}
	
	private String getSelectDay(){
		String mTimeString = (mCurrentYear) + yearStr + (mCurrentMonth+1)
				+ monthStr + mCurrentDay + dayStr + "  " + getWeek();		
		return mTimeString;
	}
	
	
	private String getWeek() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, mCurrentDay);
		c.set(Calendar.MONTH, mCurrentMonth);
		c.set(Calendar.YEAR, (mCurrentYear));
		Log.v("zwl", "calendar year = " + c.get(Calendar.YEAR) + ", month = " + c.get(Calendar.MONTH)
				+ ", days = " + c.get(Calendar.DAY_OF_MONTH));
		return mWeekdays[c.get(Calendar.DAY_OF_WEEK)-c.getFirstDayOfWeek()];
	}
}
