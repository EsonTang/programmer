
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：新建事件选择全天时的时间选择对话框
 *当前版本：1.0
 *作	者：wanzhijuan
 *完成日期：2015-6-25
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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.calendar.widget.NumericWheelAdapter;
import com.android.calendar.widget.OnWheelChangedListener;
import com.android.calendar.widget.WheelView;
import com.android.calendar.R;

public class DaySelectDialog extends Dialog {

	private ISelectTime mISelectTime;
	private Context mContext;
	private final static String TAG  = "DaySelectDialog";
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
	
	private String mLabelYear;
	private String mLabelMonth;
	private String mLabelDay;
	
	private WheelView mYearWheelView;
	private WheelView mMonthWheelView ;
	private WheelView mDayWheelView;
	
	private int mCurrentYear,mCurrentMonth,mCurrentDay;
	
	private int mMinYear;
	private int mMaxYear;
	private boolean mIsStart;
	private static final int DAY_MIN = 1;
	private static final int MONTH_MIN = 1;
	private static final int YEAR_RANGE = 100;
	private static final int LEAP_MONTH_DAY = 29;
	private static final int LEAP_MONTH = 2;

	public DaySelectDialog(Context context, ISelectTime selectTime, boolean isStart ,Time mTime) {
		super(context, R.style.prize_event_status);
		this.mContext = context;
		mIsStart = isStart;
		this.mISelectTime = selectTime;
		yearStr = mContext.getResources().getString(
				R.string.prize_lunar_year);
		monthStr = mContext.getResources().getString(
				R.string.prize_lunar_month);
		dayStr = mContext.getResources().getString(
				R.string.prize_lunar_day);
		
		mLabelYear = mContext.getResources().getString(
				R.string.label_year);
		mLabelMonth = mContext.getResources().getString(
				R.string.label_month);
		mLabelDay = mContext.getResources().getString(
				R.string.label_day);
		
		Calendar c = Calendar.getInstance();
		mCurrentYear = c.get(Calendar.YEAR);			
		mCurrentMonth = c.get(Calendar.MONTH) + 1;
		mCurrentDay = c.get(Calendar.DAY_OF_MONTH);
		
		
		if(mTime != null){
			mCurrentYear = mTime.year;			
			mCurrentMonth = mTime.month + 1;
			mCurrentDay = mTime.monthDay;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.day_select_layout);
		
		mPrizeTitle = (TextView) findViewById(R.id.name_title);	
		if (mIsStart) {
			mPrizeTitle.setText(R.string.prize_start_time);
		} else {
			mPrizeTitle.setText(R.string.prize_end_time);
		}
		
		mTimeTextView = (TextView) findViewById(R.id.time_title);
		
		updateShowTime(mCurrentYear, mCurrentMonth, mCurrentDay);
		
		mConfirmButton =  (Button) findViewById(R.id.prize_button_done);
		mCancleButton  =  (Button) findViewById(R.id.prize_button_cancel);
		
		mConfirmButton.setOnClickListener(new ButtonOnClickListener());
		mCancleButton.setOnClickListener(new ButtonOnClickListener());
		
		mYearWheelView = (WheelView) findViewById(R.id.wv_year);
		mMinYear = mCurrentYear - YEAR_RANGE;
		mMaxYear = mCurrentYear + YEAR_RANGE;
		NumericWheelAdapter yearWheelAdapter = new NumericWheelAdapter(mMinYear, mMaxYear);
		mYearWheelView.setAdapter(yearWheelAdapter);
		mYearWheelView.setCurrentItem(mCurrentYear - mMinYear);
		mYearWheelView.setLabel(mLabelYear);
		
		mMonthWheelView = (WheelView) findViewById(R.id.wv_month);
		NumericWheelAdapter mNumericWheelAdapter = new NumericWheelAdapter(1, 12);
		mMonthWheelView.setAdapter(mNumericWheelAdapter);
		mMonthWheelView.setCurrentItem(mCurrentMonth - 1);
		mMonthWheelView.setLabel(mLabelMonth);
		mMonthWheelView.setCyclic(true);
		
		mDayWheelView = (WheelView) findViewById(R.id.wv_day);
		mDayWheelView.setDrawRightLine(true);
		mDayWheelView.setLabel(mLabelDay);
		initDay();
		
		OnWheelChangedListener monthOnWheelChangedListener = new OnWheelChangedListener() {
			@Override
			public void onChanged(View wheel, int oldValue, int newValue) {
				
				/** 返回的是index**/
				mCurrentMonth = mMonthWheelView.getCurrentItem() + 1;
				updateDay();
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
		
		
		OnWheelChangedListener yearOnWheelChangedListener = new OnWheelChangedListener() {
			public void onChanged(View wheel, int oldValue, int newValue) {
				mCurrentYear = mYearWheelView.getCurrentItem() + mMinYear;
				updateDay();
				updateShowTime();
			}
		};
		
		mYearWheelView.addChangingListener(yearOnWheelChangedListener);
		mMonthWheelView.addChangingListener(monthOnWheelChangedListener);
		mDayWheelView.addChangingListener(dayOnWheelChangedListener);
		
	}
	
	/**
	 * 
	 * 方法描述：初始化日期，日期与年、月相关
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void initDay() {
		int maxMonth = MONTY_DAYS[mCurrentMonth - 1];
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
		int maxMonth = MONTY_DAYS[mCurrentMonth - 1];
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
	
	private class ButtonOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if(view.getId() == R.id.prize_button_done) {
				if (mISelectTime != null) {
					mISelectTime.onSelectDone(mIsStart, mCurrentYear, mCurrentMonth, mCurrentDay, 0, 0);
				}
				DaySelectDialog.this.dismiss();
			}else if(view.getId() == R.id.prize_button_cancel){
				DaySelectDialog.this.dismiss();
			}
		}
	}

	
	private void updateShowTime(int year , int month , int day){
		String mTimeString = year + yearStr + month
				+ monthStr + day + dayStr;			
		mTimeTextView.setText(mTimeString);
	}	
	
	private void updateShowTime(){
		String mTimeString = mCurrentYear + yearStr + (mCurrentMonth)
				+ monthStr + mCurrentDay + dayStr;		
		mTimeTextView.setText(mTimeString);
	}
	
	private String getSelectDay(){
		String mTimeString = mCurrentYear + yearStr + (mCurrentMonth)
				+ monthStr + mCurrentDay + dayStr;		
		return mTimeString;
	}
}

