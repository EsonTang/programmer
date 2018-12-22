package com.android.calendar.hormonth;

import java.util.Calendar;

import com.android.calendar.CalendarController;
import com.android.calendar.EventLoader;
import com.android.calendar.Utils;
import com.android.calendar.event.CreateEventDialogFragment;
import com.android.calendar.R;

import android.app.Fragment;
import android.app.FragmentManager;
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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

public class MonthFragment extends Fragment implements
        CalendarController.EventHandler, ViewFactory {

	private final static String TAG = "MonthFragment";

	protected ViewSwitcher mViewSwitcher;

	protected ViewGroup mDayNamesHeader;

	protected Animation mInAnimationForward;
	protected Animation mOutAnimationForward;
	protected Animation mInAnimationBackward;
	protected Animation mOutAnimationBackward;

	protected int mSaturdayColor;
	protected int mSundayColor;
	protected int mDayNameColor;

	EventLoader mEventLoader;

	// When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
	protected int mFirstDayOfWeek;

	private Context mContext;

	private Time mSelectedDay = new Time();

	// Number of days per week
	protected int mDaysPerWeek = 7;
	protected String[] mDayLabels;
	
	private CreateEventDialogFragment mEventDialog;
	
	private static final String TAG_EVENT_DIALOG = "event_dialog";

	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			if (!MonthFragment.this.isAdded()) {
				return;
			}
			String tz = Utils.getTimeZone(getActivity(), mTZUpdater);
			mSelectedDay.timezone = tz;
			mSelectedDay.normalize(true);
		}
	};

	public MonthFragment() {
		mSelectedDay.setToNow();
	}

	public MonthFragment(long timeMillis) {
		if (timeMillis == 0) {
			mSelectedDay.setToNow();
		} else {
			mSelectedDay.set(timeMillis);
		}
	}

	/**
	 * M: pass the context in to get original displayed time in our calendar
	 * 
	 * @param context
	 * @param timeMillis
	 * @param numOfDays
	 */
	public MonthFragment(Context context, long timeMillis) {
		mSelectedDay = Utils.getValidTimeInCalendar(context, timeMillis);
//		mSelectedDay.set(1, CurrentMonthDay.month, CurrentMonthDay.year);
		mSelectedDay.normalize(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = getActivity();

		mInAnimationForward = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_left_in);
		mOutAnimationForward = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_left_out);
		mInAnimationBackward = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_right_in);
		mOutAnimationBackward = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_right_out);

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View view = inflater.inflate(R.layout.month_activity, null);

		mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.switcher);

		mDayNamesHeader = (ViewGroup) view.findViewById(R.id.day_names);

		updateHeader();

		mViewSwitcher.setFactory(this);

		mViewSwitcher.getCurrentView().requestFocus();
		

		((MonthView) mViewSwitcher.getCurrentView()).updateTitle();
		
		((MonthView) mViewSwitcher.getCurrentView()).setAllSrollDayCurrent();
		
		return view;
	}

	@Override
	public View makeView() {
		// TODO Auto-generated method stub
		mTZUpdater.run();
		MonthView view = new MonthView(mContext,
				CalendarController.getInstance(getActivity()), mViewSwitcher,
				mEventLoader,mEventDialogHandler);

		view.setLayoutParams(new ViewSwitcher.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
	    view.setSelected(mSelectedDay, false, false);
//		HashMap<String, Integer> drawingParams = new HashMap<>();
		
		// pass in all the view parameters
		
//		drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, mSelectedDay.getWeekNumber());
//
//		drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START,
//				mFirstDayOfWeek);
//		drawingParams.put(SimpleWeekView.VIEW_PARAMS_NUM_DAYS, mDaysPerWeek);
//		drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK,
//				mSelectedDay.getWeekNumber());
//		drawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, 6);
//		view.setWeekParams(drawingParams, mSelectedDay.timezone);
		
		view.invalidate();
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mEventLoader.startBackgroundThread();
		mTZUpdater.run();
		
		MonthView view = (MonthView) mViewSwitcher.getCurrentView();
		view.handleOnResume();
		view.restartCurrentTimeUpdates();

		view = (MonthView) mViewSwitcher.getNextView();
		view.handleOnResume();
		view.restartCurrentTimeUpdates();
	}

	/**
	 * Fixes the day names header to provide correct spacing and updates the
	 * label text. Override this to set up a custom header.
	 */
	protected void updateHeader() {

		int offset = mFirstDayOfWeek - 1;
		for (int i = 1; i < 8; i++) {
			TextView label = (TextView) mDayNamesHeader.getChildAt(i);
			if (i < mDaysPerWeek + 1) {
				int position = (offset + i) % 7;
				label.setText(mDayLabels[position]);
				label.setVisibility(View.VISIBLE);
				/* PRIZE-界面调整 wanzhijuan 2015-7-6 start */
				// 颜色统一为白色 xml设置
				/*if (position == Time.SATURDAY) {
					label.setTextColor(mSaturdayColor);
				} else if (position == Time.SUNDAY) {
					label.setTextColor(mSundayColor);
				} else {
					label.setTextColor(mDayNameColor);
				}*/
				/* PRIZE-界面调整 wanzhijuan 2015-7-6 end */
			} else {
				label.setVisibility(View.GONE);
			}
		}
		mDayNamesHeader.invalidate();
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		 return CalendarController.EventType.GO_TO | CalendarController.EventType.EVENTS_CHANGED;
	}
	
	private void goTo(Time goToTime, boolean ignoreTime, boolean animateToday ){
		 if (mViewSwitcher == null) {
	            // The view hasn't been set yet. Just save the time and use it later.
            mSelectedDay.set(goToTime);
            return;
        }
		
		MonthView currentView = (MonthView) mViewSwitcher.getCurrentView();
		
		if (currentView == null) {
			Log.e(TAG, "getCurrentView() return null,return");
			return;
		}
		int gotoJulianDay = Utils.getJulianDayInGeneral(goToTime, false);
		
		int currentViewJulianDay = Utils.getJulianDayInGeneral(currentView.getSelectedDay(), false);
		
		if(gotoJulianDay>currentViewJulianDay){
			mViewSwitcher.setInAnimation(mInAnimationForward);
            mViewSwitcher.setOutAnimation(mOutAnimationForward);
		}else {
			 mViewSwitcher.setInAnimation(mInAnimationBackward);
             mViewSwitcher.setOutAnimation(mOutAnimationBackward);
		}
		
		MonthView CurrentView = (MonthView) mViewSwitcher.getCurrentView();
		Time SelectTime =  CurrentView.getSelectedDay();
		SelectTime.normalize(true);
		if(SelectTime.year == goToTime.year && goToTime.month == SelectTime.month){
			CurrentView.setSelectOndraw(goToTime);
			return ;
		}
		

		MonthView next = (MonthView) mViewSwitcher.getNextView();
       ///M:@{
        next.selectionFocusShow(false);
        ///@}
        if (ignoreTime) {
            next.setFirstVisibleHour(currentView.getFirstVisibleHour());
        }

        next.setSelected(goToTime, ignoreTime, animateToday);
    
        next.reloadEvents();
        mViewSwitcher.showNext();
        next.requestFocus();
        next.updateTitle();
        next.restartCurrentTimeUpdates();
        MonthView CurrentViews = (MonthView) mViewSwitcher.getCurrentView();
        CurrentViews.setAllSrollDayCurrent();
	}

	@Override
	public void handleEvent(CalendarController.EventInfo event) {
		// TODO Auto-generated method stub
		 if (event.eventType == CalendarController.EventType.GO_TO) {
			 // TODO support a range of time
			 // TODO support event_id
			 // TODO support select message
			
			 goTo(event.selectedTime, (event.extraLong & CalendarController.EXTRA_GOTO_DATE) != 0,
					 (event.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0);
		 } else if (event.eventType == CalendarController.EventType.EVENTS_CHANGED) {
			 eventsChanged();
		 }

	}

	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub

	}
	
//	 @Override
//	public void eventsChanged(Time selectedTime) {
//		
//		// TODO Auto-generated method stub
//		
//	}
	 private Handler mEventDialogHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            final FragmentManager manager = getFragmentManager();
	            if (manager != null) {
	                Time day = (Time) msg.obj;
	                mEventDialog = new CreateEventDialogFragment(day);
	                mEventDialog.show(manager, TAG_EVENT_DIALOG);
	            }
	        }
	    };

	@Override
	public void eventsChanged(Time selectedTime) {
		// TODO Auto-generated method stub
		
	}

}
