/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *文件名称：CalendarMonthFragment.java
 *内容摘要：
 *当前版本：v 1.0
 *作	者：刘栋
 *完成日期：

 *修改记录：
 *修改日期：2015-5-18 下午8:35:01
 *版 本 号：v  1.0
 *修 改 人：刘栋
 *修改内容：
 ********************************************/
package com.android.calendar.hormonth;

import java.util.Calendar;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.EventLoader;
import com.android.calendar.Utils;
import com.android.calendar.animation.CalendarViewPager;
import com.android.calendar.animation.MainLinearLayout;
import com.android.calendar.hormonth.CalendarView.CallBack;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.calendar.R;

public class CalendarMonthFragment extends Fragment implements CallBack,
        CalendarController.EventHandler {

	private Context mContext = null;

	protected ViewGroup mDayNamesHeader = null;

	private CalendarViewPager viewPager = null;

	protected int mSaturdayColor;
	protected int mSundayColor;
	protected int mDayNameColor;

	public EventLoader mEventLoader = null;

	// When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
	protected int mFirstDayOfWeek;

	private Time mSelectedDay = new Time(Utils.getTimeZone(mContext, null));

	// Number of days per week
	protected int mDaysPerWeek = 7;
	protected String[] mDayLabels = null;


	private static final String TAG_EVENT_DIALOG = "event_dialog";

	private static final int ViewSize = 5;

	public static CalendarView[] views  = null;

	private CalendarViewBuilder builder = new CalendarViewBuilder();

	private CalendarController mCalendarController;

	private CustomViewPagerAdapter<CalendarView> viewPagerAdapter;

	private CalendarViewPagerLisenter mCalendarViewPagerLisenter;

	private final static int ShowOnFourceTime = 500;

	private final static int SHOWONFOURCE_FLAG = 0x00;

	private static CalendarMonthFragment Intance;

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			if (!CalendarMonthFragment.this.isAdded()) {
				return;
			}
			String tz = Utils.getTimeZone(getActivity(), mTZUpdater);
			mSelectedDay.timezone = tz;
			mSelectedDay.normalize(true);
		}
	};
	
	public static CalendarMonthFragment getIntance(long timeMillis){
		Intance = new CalendarMonthFragment();


		Bundle bundle = new Bundle();  

        bundle.putLong("key", timeMillis);  

        Intance.setArguments(bundle);

		return Intance;
	}

	public CalendarMonthFragment() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		Log.d("hekeyi","monthToDayAnimation-onCreate  =  mMainChangeListener = "+mMainChangeListener+"  "+System.currentTimeMillis());
		if(mMainChangeListener!=null){
			mMainChangeListener.onChange();
		}
		mContext = getActivity();

		long timeMillis = getArguments().getLong("key");
		if (timeMillis == 0) {
			mSelectedDay.setToNow();
		} else {
			mSelectedDay.set(timeMillis);
		}
		mSelectedDay.normalize(true);
		
		mCalendarController = CalendarController.getInstance(getActivity());

		mSaturdayColor = mContext.getResources().getColor(
				R.color.month_saturday);
		mSundayColor = mContext.getResources().getColor(R.color.month_sunday);
		mDayNameColor = mContext.getResources().getColor(
				R.color.month_day_names_color);

		mEventLoader = new EventLoader(mContext);

		mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);

		mDayLabels = new String[mDaysPerWeek];
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			mDayLabels[i - Calendar.SUNDAY] = DateUtils.getDayOfWeekString(i,
					DateUtils.LENGTH_SHORTEST).toUpperCase();
		}
//		reMarkEvents();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		Log.d("hekeyi","monthToDayAnimation-onResume  =  "+System.currentTimeMillis());
		mEventLoader.startBackgroundThread();
		mTZUpdater.run();
		this.clickDate(null);
		
		
		/*PRIZE-获取本月的事件-lixing- 2015-8-21- start */
//		reMarkEvents();
		/*PRIZE-获取本月的事件-lixing- 2015-8-21- end */
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		
		super.onPause();
	}

	
	/**
	 * @see 在onResume()中重新获取事件按,并标记
	 * @author lixing
	 * 2015-8-19
	 */
	public void reMarkEvents(){
		if (viewPager == null || views == null) {
			return;
		}

		views[viewPager.getCurrentItem() % ViewSize].reloadEvents();
	}

	MainLinearLayout main_layout;
	DateUtil mDateUtil = DateUtil.dateUtil;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View view = inflater.inflate(R.layout.month_calendar_activity,
				container, false);

		mDayNamesHeader = (ViewGroup) view.findViewById(R.id.day_names);

//		updateHeader();

		viewPager = (CalendarViewPager) view.findViewById(R.id.viewpager);
		viewPager.setSelectedDay(mSelectedDay);

		int count = mDateUtil.setRows(mSelectedDay.year,mSelectedDay.month+1,mContext);
		main_layout = (MainLinearLayout)getActivity().findViewById(R.id.main_pane);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)main_layout.getLayoutParams();
//		layoutParams.height = count*(int)getResources().getDimension(R.dimen.prize_week_height);
		layoutParams.height = count*(int)getResources().getDimension(R.dimen.prize_week_height);
//		Log.d("hekeyi","[MonthChangeAnimation]  onCreateView setHeight= "+layoutParams.height);

		views = builder.createMassCalendarViews(mContext, ViewSize, this,
				mSelectedDay,mEventLoader);

		setViewPager();

		mHandler.sendEmptyMessageDelayed(SHOWONFOURCE_FLAG, ShowOnFourceTime);

		return view;
	}

	private void setViewPager() {
		viewPagerAdapter = new CustomViewPagerAdapter<CalendarView>(views);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setCurrentItem(498);
		mCalendarViewPagerLisenter = new CalendarViewPagerLisenter(
				viewPagerAdapter, this);
		viewPager.setOnPageChangeListener(mCalendarViewPagerLisenter);

	}

	/**
	 * Fixes the day names header to provide correct spacing and updates the
	 * label text. Override this to set up a custom header.
	 */
	protected void updateHeader() {
		int offset = mFirstDayOfWeek - 1;
		for (int i = 1; i < 8; i++) {
			if(!(mDayNamesHeader.getChildAt(i) instanceof  TextView)) continue;
			TextView label = (TextView) mDayNamesHeader.getChildAt(i);
			if (i < mDaysPerWeek + 1) {
				int position = (offset + i) % 7;
				label.setText(mDayLabels[position]);
				label.setVisibility(View.VISIBLE);
			} else {
				label.setVisibility(View.GONE);
			}
		}
		mDayNamesHeader.invalidate();
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void clickDate(CustomDate date) {
		// TODO Auto-generated method stub
		if (viewPager == null || views == null) {
			return;
		}
//		Log.d("hekeyi","[MonthChangeAnimation] clickDate = "+System.currentTimeMillis());
//		views[viewPager.getCurrentItem() % ViewSize].monthChangeAnimation();
		views[viewPager.getCurrentItem() % ViewSize].reloadEvents();
		if (date == null) {
			if (mCalendarViewPagerLisenter.ViewPageCurrentStatus != 1) {
				date = views[viewPager.getCurrentItem() % ViewSize].getShowDate();
				Time startTime = new Time();
				startTime.set(1, 0, 0, date.day, date.month - 1, date.year);
				startTime.normalize(true);
				Time EndTime = new Time();
				EndTime.set(1, 0, 0, date.day + 1, date.month - 1, date.year);
				EndTime.normalize(true);
				/*long formatFlags = DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_YEAR;
				mCalendarController.sendEvent(this, CalendarController.EventType.UPDATE_TITLE,
						startTime, EndTime, null, -1, CalendarController.ViewType.CURRENT,
						formatFlags, null, null);*/
				mCalendarController.sendEvent(mContext,
						CalendarController.EventType.UPDATE_AGENDA_SHOW_TIME, startTime, EndTime,
						-1, CalendarController.ViewType.CURRENT,
						CalendarController.EXTRA_GOTO_DATE, null, null);
				
				mHandler.removeMessages(SHOWONFOURCE_FLAG);
				mHandler.sendEmptyMessageDelayed(SHOWONFOURCE_FLAG,
						ShowOnFourceTime / 10);
			}
			return;
		}
		if((date.getYear()==mSelectedDay.year)&&(date.getMonth()==mSelectedDay.month)&&(date.getDay()==mSelectedDay.monthDay)){
			return;
		}
		if(mCalendarController.getTime()==mSelectedDay.toMillis(true)){
			return;
		}

		CustomDate mCustomDate = views[viewPager.getCurrentItem() % ViewSize]
				.getShowDate();

		if (!(date.month == mCustomDate.month && date.year == mCustomDate.year)) {
			if (mCustomDate.year > date.year) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
			} else if (mCustomDate.year < date.year) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
			} else if ((mCustomDate.year == date.year)
					&& (mCustomDate.month > date.month)) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
			} else if ((mCustomDate.year == date.year)
					&& (mCustomDate.month < date.month)) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
			}
		}
		Time startTime = new Time();
		startTime.set(1, 0, 0, date.day, date.month - 1, date.year);
		startTime.normalize(true);
		Time EndTime = new Time();
		EndTime.set(1, 0, 0, date.day + 1, date.month - 1, date.year);
		EndTime.normalize(true);
		/*long formatFlags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_YEAR;
		mCalendarController.sendEvent(this, CalendarController.EventType.UPDATE_TITLE, startTime,
				EndTime, null, -1, CalendarController.ViewType.CURRENT, formatFlags, null, null);*/
		mCalendarController.sendEvent(mContext,
				CalendarController.EventType.UPDATE_AGENDA_SHOW_TIME, startTime, EndTime, -1,
				CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null,
				null);

		mHandler.removeMessages(SHOWONFOURCE_FLAG);
		mHandler.sendEmptyMessageDelayed(SHOWONFOURCE_FLAG,
				ShowOnFourceTime / 10);
		/*views[viewPager.getCurrentItem() % ViewSize].reloadEvents();*/
	}

	@Override
	public void onMesureCellHeight(int cellSpace) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ClearOnFouces() {
		// TODO Auto-generated method stub
		mHandler.removeMessages(SHOWONFOURCE_FLAG);
	}


	@Override
	public void sendFocusToParent() {

	}


	@Override
	public void ChangeOtherMont(CustomDate date, boolean froward) {
		// TODO Auto-generated method stub
		Time startTime = new Time();
		startTime.set(1, 0, 0, date.day, date.month - 1, date.year);
		startTime.normalize(true);
		Time EndTime = new Time();
		EndTime.set(1, 0, 0, date.day + 1, date.month - 1, date.year);
		EndTime.normalize(true);
		mCalendarController.sendEvent(mContext, CalendarController.EventType.GO_TO, startTime,
				EndTime, -1, CalendarController.ViewType.CURRENT,
				CalendarController.EXTRA_GOTO_DATE, null, null);
	}

	@Override
	public void changeDate(CustomDate date) {
		// TODO Auto-generated method stub
		Time startTime = new Time();
		startTime.set(1, 0, 0, date.day, date.month - 1, date.year);
		startTime.normalize(true);
		Time EndTime = new Time();
		EndTime.set(1, 0, 0, date.day + 1, date.month - 1, date.year);
		EndTime.normalize(true);
		/*long formatFlags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_SHOW_YEAR;
		mCalendarController.sendEvent(this, CalendarController.EventType.UPDATE_TITLE, startTime,
				EndTime, null, -1, CalendarController.ViewType.CURRENT, formatFlags, null, null);*/
		mCalendarController.sendEvent(mContext,
				CalendarController.EventType.UPDATE_AGENDA_SHOW_TIME, startTime, EndTime, -1,
				CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null,
				null);
		mHandler.removeMessages(SHOWONFOURCE_FLAG);
		mHandler.sendEmptyMessageDelayed(SHOWONFOURCE_FLAG, ShowOnFourceTime);
	}

	private void GotoData(Time goToTime, boolean ignoreTime,
			boolean animateToday) {
		if (viewPager == null || AllInOneActivity.viewTypeFlag== CalendarController.ViewType.WEEK) {
			return;
		}
//		if(goToTime.toMillis(true)!=mCalendarController.getTime()){
		CalendarView mCalendarView = views[viewPager.getCurrentItem()
				% ViewSize];
		CustomDate CurrentCustomDate = mCalendarView.getShowDate();

		if (CurrentCustomDate.month == (goToTime.month + 1)
				&& CurrentCustomDate.year == goToTime.year
				&& CurrentCustomDate.day != goToTime.monthDay) {
			CurrentCustomDate = new CustomDate(CurrentCustomDate.year,
					CurrentCustomDate.month, goToTime.monthDay);
			mCalendarView.setShowDate(CurrentCustomDate, true);
			mCalendarView.setViewCurrentDate(CurrentCustomDate);    //add by hekeyi for calendarV8.0 modify a bug:when change time and then click go to today button,the today icon error
		} else {
			int GoToJulianday = Utils.getJulianDayInGeneral(goToTime, false);

			Time CurrentTime = new Time();

			CurrentTime.set(1, 0, 0, CurrentCustomDate.day,
					CurrentCustomDate.month - 1, CurrentCustomDate.year);

			CurrentTime.normalize(true);

			int CurrentJulianday = Utils.getJulianDayInGeneral(CurrentTime,
					false);

			if (GoToJulianday > CurrentJulianday) {
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month, goToTime.monthDay);
				mCalendarView.setShowDate(CurrentCustomDate);
				mCalendarView.setViewCurrentDate(CurrentCustomDate);
				mCalendarView.invalidate();
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month + 1, goToTime.monthDay);
				CalendarView NextCalendarView = views[(viewPager
						.getCurrentItem() + 1) % ViewSize];
				NextCalendarView.setViewCurrentDate(CurrentCustomDate);
				NextCalendarView.invalidate();
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month + 2, goToTime.monthDay);
				CalendarView NextTwoCalendarView = views[(viewPager
						.getCurrentItem() + 2) % ViewSize];
				NextTwoCalendarView.setViewCurrentDate(CurrentCustomDate);
				NextTwoCalendarView.invalidate();
				viewPagerAdapter.IsGotoShow = true;
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

			} else {
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month + 2, goToTime.monthDay);
				mCalendarView.setShowDate(CurrentCustomDate);
				mCalendarView.setViewCurrentDate(CurrentCustomDate);
				mCalendarView.invalidate();
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month + 1, goToTime.monthDay);
				CalendarView ProCalendarView = views[(viewPager
						.getCurrentItem() - 1) % ViewSize];
				ProCalendarView.setViewCurrentDate(CurrentCustomDate);
				ProCalendarView.invalidate();
				CurrentCustomDate = new CustomDate(goToTime.year,
						goToTime.month, goToTime.monthDay);
				if(viewPager.getCurrentItem()>=2){
					CalendarView ProTwoCalendarView = views[(viewPager
							.getCurrentItem() - 2) % ViewSize];
					ProTwoCalendarView.setViewCurrentDate(CurrentCustomDate);
					ProTwoCalendarView.invalidate();
				}
				viewPagerAdapter.IsGotoShow = true;
				viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, false);
			}
		}
		mHandler.removeMessages(SHOWONFOURCE_FLAG);
		mHandler.sendEmptyMessageDelayed(SHOWONFOURCE_FLAG, ShowOnFourceTime);
		Time startTime = new Time();
		startTime
				.set(1, 0, 0, goToTime.monthDay, goToTime.month, goToTime.year);
		startTime.normalize(true);
		Time EndTime = new Time();
		EndTime.set(1, 0, 0, goToTime.monthDay + 1, goToTime.month,
				goToTime.year);
		EndTime.normalize(true);
		mCalendarController.sendEvent(mContext,
				CalendarController.EventType.UPDATE_AGENDA_SHOW_TIME, startTime, EndTime, -1,
				CalendarController.ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null,
				null);

		views[viewPager.getCurrentItem() % ViewSize].reloadEvents();
//		}
	}

	private void setShowFource(boolean isFource) {
		if (viewPager == null) {
			return;
		}

		CalendarView mCalendarView = views[viewPager.getCurrentItem()
				% ViewSize];

		mCalendarView.setIsShowFouce(isFource);
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		return CalendarController.EventType.GO_TO | CalendarController.EventType.EVENTS_CHANGED;
	}

	@Override
	public void handleEvent(CalendarController.EventInfo event) {
		// TODO Auto-generated method stub
		if (event.eventType == CalendarController.EventType.GO_TO) {
			if(mCalendarController!=null){
				Log.d("hekeyi","[CalendarMonthFragment]-handleEvent selectedTime = "+event.selectedTime );
				GotoData(
						event.selectedTime,
						(event.extraLong & CalendarController.EXTRA_GOTO_DATE) != 0,
						(event.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0);
			}
		}
	}

	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub

	}

	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {

			switch (msg.what) {
			case SHOWONFOURCE_FLAG:
				mHandler.removeMessages(SHOWONFOURCE_FLAG);
				setShowFource(true);
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void eventsChanged(Time selectedTime) {
		// TODO Auto-generated method stub
		
	}

	private OnMainChangeListener mMainChangeListener;

	public interface OnMainChangeListener {
		void onChange();
	}

	public void setOnMainChangeListener(OnMainChangeListener onMainChangeListener) {
//		Log.d("hekeyi","monthToDayAnimation  =  onMainChangeListener = "+onMainChangeListener+"  "+System.currentTimeMillis());
		this.mMainChangeListener = onMainChangeListener;
	}

}
