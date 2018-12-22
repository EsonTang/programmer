/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.android.calendar.R;
import com.android.calendar.animation.MainLinearLayout;
import com.android.calendar.horday.DayHeaderView;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

import com.android.calendar.month.SimpleWeekView;
import com.mediatek.calendar.PDebug;

/**
 * This is the base class for Day and Week Activities.
 */
@SuppressLint("ValidFragment")
public class DayFragment extends Fragment implements CalendarController.EventHandler, LoaderManager.LoaderCallbacks<Cursor>, ViewFactory {
    /**
     * The view id used for all the views we create. It's OK to have all child
     * views have the same ID. This ID is used to pick which view receives
     * focus when a view hierarchy is saved / restore
     */
    ///M:@{
    private static final String TAG = "DayFragment";
    ///@}
    private static final int VIEW_ID = 1;

    protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";

    protected ProgressBar mProgressBar;
    protected ViewSwitcher mViewSwitcher;
    protected Animation mInAnimationForward;
    protected Animation mOutAnimationForward;
    protected Animation mInAnimationBackward;
    protected Animation mOutAnimationBackward;
    EventLoader mEventLoader;

    protected int mSaturdayColor;
    protected int mSundayColor;
    protected int mDayNameColor;

    Time mSelectedDay = new Time();

    protected ViewGroup mDayNamesHeader;

    // When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
    protected int mFirstDayOfWeek;

    // Number of days per week
    protected int mDaysPerWeek = 7;

    protected String[] mDayLabels;

    protected FrameLayout mViewDayWeekFrameLayout;
    protected ViewSwitcher mViewDayWeekSwitcher;
    private static DayFragment Intance;
    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            if (!DayFragment.this.isAdded()) {
                return;
            }
            String tz = Utils.getTimeZone(getActivity(), mTZUpdater);
            mSelectedDay.timezone = tz;
            mSelectedDay.normalize(true);
        }
    };

    private static int mNumDays;

    public DayFragment() {
        mSelectedDay.setToNow();
    }

    public DayFragment(long timeMillis, int numOfDays) {
        mNumDays = numOfDays;
        if (timeMillis == 0) {
            mSelectedDay.setToNow();
        } else {
            mSelectedDay.set(timeMillis);
        }
    }

    /*private String SHARED_PREFS_NAME = "com.android.calendar_preferences";
    private String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
    private String mTimeZoneId;
    private boolean mUseHomeTZ = false;*/
    /**
     * M: pass the context in to get original displayed time in our calendar
     *
     * @param context
     * @param timeMillis
     * @param numOfDays
     */
    public DayFragment(Context context, long timeMillis, int numOfDays) {
        mNumDays = numOfDays;
        this.mContext = context;
        mSelectedDay = Utils.getValidTimeInCalendar(context, timeMillis);
//        mSelectedDay.timezone = TimeZone.getDefault().getID();
        /*SharedPreferences prefs = CalendarUtils.getSharedPreferences(mContext,SHARED_PREFS_NAME);
        mUseHomeTZ = prefs.getBoolean(KEY_HOME_TZ_ENABLED,false);
        mTimeZoneId = Utils.getTimeZone(mContext, null);
        if(!mUseHomeTZ){
            mSelectedDay.timezone = TimeZone.getDefault().getID();
        }else {
            mSelectedDay.timezone = mTimeZoneId;
        }*/
    }

    public static DayFragment getIntance(long timeMillis, int numOfDays) {
        Intance = new DayFragment();

        Bundle bundle = new Bundle();

        bundle.putLong("key", timeMillis);

        Intance.setArguments(bundle);

        mNumDays = numOfDays;

        return Intance;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context mContext = getActivity();


        mInAnimationForward = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_in);
        mOutAnimationForward = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_out);
        mInAnimationBackward = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_in);
        mOutAnimationBackward = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_out);

        mSaturdayColor = mContext.getResources()
                .getColor(R.color.month_saturday);
        mSundayColor = mContext.getResources().getColor(R.color.month_sunday);
        mDayNameColor = mContext.getResources().getColor(
                R.color.month_day_names_color);

        mEventLoader = new EventLoader(mContext);

        mDayLabels = new String[mDaysPerWeek];
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            mDayLabels[i - Calendar.SUNDAY] = DateUtils.getDayOfWeekString(i,
                    DateUtils.LENGTH_SHORTEST).toUpperCase();
        }
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    MainLinearLayout main_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.prize_day_activity, null);

        if(mNumDays==1){
            mViewSwitcher = (ViewSwitcher) v.findViewById(R.id.switcher);
            //add by hekeyi for calendar v8.0
            mViewSwitcher.setVisibility(View.GONE);

            mDayNamesHeader = (ViewGroup) v.findViewById(R.id.day_names);

//		updateHeader();

            mViewSwitcher.setFactory(this);
        }

//            mViewSwitcher.getCurrentView().requestFocus();
//        ((DayHeaderView) mViewSwitcher.getCurrentView()).updateTitle();   //change for calendar v8.0
        if (mNumDays == 7) {
            mViewDayWeekFrameLayout = (FrameLayout) v
                    .findViewById(R.id.weekdayframelayout);
            mViewDayWeekFrameLayout.setVisibility(View.VISIBLE);
            mViewDayWeekSwitcher = (ViewSwitcher) v
                    .findViewById(R.id.weekdayswitcher);
            mViewDayWeekSwitcher.setFactory(new DayViewFactory());
            main_layout = (MainLinearLayout) getActivity().findViewById(R.id.main_pane);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) main_layout.getLayoutParams();
            layoutParams.height = (int) getResources().getDimension(R.dimen.prize_week_height);
        }

        return v;
    }

    /**
     * Fixes the day names header to provide correct spacing and updates the
     * label text. Override this to set up a custom header.
     */
    protected void updateHeader() {

        int offset = mFirstDayOfWeek - 1;
        for (int i = 1; i < 8; i++) {
            if (!(mDayNamesHeader.getChildAt(i) instanceof TextView)) continue;
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

    public View makeView() {
        PDebug.Start("DayFragment.makeView");

        mTZUpdater.run();
        /*DayView view = new DayView(getActivity(),
				CalendarController.getInstance(getActivity()), mViewSwitcher,
				mEventLoader, mNumDays);*/
        DayHeaderView dayView = new DayHeaderView(getActivity(),
                CalendarController.getInstance(getActivity()), mViewSwitcher,
                mEventLoader/*,1*/);                                                      //change by hekeyi for calendar v8.0
        dayView.setId(VIEW_ID);
        dayView.setLayoutParams(new ViewSwitcher.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        dayView.setSelected(mSelectedDay, false, false);

//        view.reloadEvents();
        return dayView;
    }

    @Override
    public void onResume() {

        super.onResume();
        mEventLoader.startBackgroundThread();
        mTZUpdater.run();
        eventsChanged();
//        DayView view = (DayView) mViewSwitcher.getCurrentView();
        if (mNumDays == 1) {
            DayHeaderView dayView = (DayHeaderView) mViewSwitcher.getCurrentView();   //change by hekeyi for calendar v8.0
            dayView.reloadEvents();
            dayView.handleOnResume();
            dayView.restartCurrentTimeUpdates();
        } else if (mNumDays == 7) {
            DayHeaderView weekView = (DayHeaderView) mViewDayWeekSwitcher.getCurrentView();   //change by hekeyi for calendar v8.0
            weekView.reloadEvents();
            weekView.handleOnResume();
            weekView.restartCurrentTimeUpdates();
        }


//		view = (DayView) mViewSwitcher.getNextView();
        /*view = (DayHeaderView) mViewSwitcher.getNextView();   //change by hekeyi for calendar v8.0
		view.handleOnResume();
		view.restartCurrentTimeUpdates();
        view.reloadEvents();*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        long time = getSelectedTimeInMillis();
        if (time != -1) {
            outState.putLong(BUNDLE_KEY_RESTORE_TIME, time);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        DayView view = (DayView) mViewSwitcher.getCurrentView();
        if (mNumDays == 1) {
            DayHeaderView dayView = (DayHeaderView) mViewSwitcher.getCurrentView();   //change by hekeyi for calendar v8.0
            dayView.cleanup();
//        view = (DayView) mViewSwitcher.getNextView();
            dayView = (DayHeaderView) mViewSwitcher.getNextView();  //change by hekeyi for calendar v8.0
            dayView.cleanup();
            dayView.stopEventsAnimation();

        } else if (mNumDays == 7) {
            DayHeaderView weekView = (DayHeaderView) mViewDayWeekSwitcher.getCurrentView();   //change by hekeyi for calendar v8.0
            weekView.cleanup();
//        view = (DayView) mViewSwitcher.getNextView();
            weekView = (DayHeaderView) mViewDayWeekSwitcher.getNextView();  //change by hekeyi for calendar v8.0
            weekView.cleanup();
            weekView.stopEventsAnimation();
        }
        mEventLoader.stopBackgroundThread();

        // Stop events cross-fade animation
//        ((DayView) mViewSwitcher.getNextView()).stopEventsAnimation();
    }

    void startProgressSpinner() {
        // start the progress spinner
        mProgressBar.setVisibility(View.VISIBLE);
    }

    void stopProgressSpinner() {
        // stop the progress spinner
        mProgressBar.setVisibility(View.GONE);
    }

    private void goTo(Time goToTime, boolean ignoreTime, boolean animateToday) {
        Log.d("hekeyi","[DayFragment]-goTo goToTime = "+goToTime);
        if (mViewDayWeekSwitcher == null) {
            // The view hasn't been set yet. Just save the time and use it later.
            mSelectedDay.set(goToTime);
        } else {
            //        if(mSelectedDay.monthDay == goToTime.monthDay)  return;
//        long selectedTime  = getSelectedTimeInMillis();
//        if(goToTime.toMillis(false) == selectedTime)  return;   //add by hekeyi for calendar v8.0 20170814
//        DayView currentView = (DayView) mViewSwitcher.getCurrentView();
            DayHeaderView currentView = (DayHeaderView) mViewDayWeekSwitcher.getCurrentView();  //change by hekeyi for calendar v8.0

            ///M:@{
            if (currentView == null) {
                return;
            }
            currentView.selectionFocusShow(false);
//        currentView.reloadEvents();
            ///@}
            // How does goTo time compared to what's already displaying?
            int diff = currentView.compareToVisibleTimeRange(goToTime);

            if (diff == 0) {
                // In visible range. No need to switch view
                currentView.setSelected(goToTime, ignoreTime, animateToday);
            } else {
                // Figure out which way to animate
                if (diff > 0) {
                    mViewDayWeekSwitcher.setInAnimation(mInAnimationForward);
                    mViewDayWeekSwitcher.setOutAnimation(mOutAnimationForward);
                } else {
                    mViewDayWeekSwitcher.setInAnimation(mInAnimationBackward);
                    mViewDayWeekSwitcher.setOutAnimation(mOutAnimationBackward);
                }

//            DayView next = (DayView) mViewSwitcher.getNextView();
                DayHeaderView next = (DayHeaderView) mViewDayWeekSwitcher.getNextView();
                ///M:@{
                next.selectionFocusShow(false);
                ///@}
                if (ignoreTime) {
                    next.setFirstVisibleHour(currentView.getFirstVisibleHour());
                }

                next.setSelected(goToTime, ignoreTime, animateToday);
                next.reloadEvents();
                mViewDayWeekSwitcher.showNext();
                next.requestFocus();
                next.updateTitle();
                next.restartCurrentTimeUpdates();
            }
        }


        /*if (mNumDays == 7
                && mViewDayWeekSwitcher != null
                && (DayHeaderView) mViewDayWeekSwitcher.getCurrentView() != null) {
            ((DayHeaderView) mViewDayWeekSwitcher.getCurrentView()).setSelectGoTo(goToTime);
        }*/
    }

    /**
     * Returns the selected time in milliseconds. The milliseconds are measured
     * in UTC milliseconds from the epoch and uniquely specifies any selectable
     * time.
     *
     * @return the selected time in milliseconds
     */
    public long getSelectedTimeInMillis() {
        if (mViewDayWeekSwitcher == null) {
            return -1;
        }
//        DayView view = (DayView) mViewSwitcher.getCurrentView();
        DayHeaderView view = (DayHeaderView) mViewDayWeekSwitcher.getCurrentView();
        if (view == null) {
            return -1;
        }
//        view.reloadEvents();
        return view.getSelectedTimeInMillis();
    }

    public void eventsChanged() {
        PDebug.Start("DayFragment.eventsChanged");

        if (mViewDayWeekSwitcher == null) {
            return;
        }
//        DayView view = (DayView) mViewSwitcher.getCurrentView();
        DayHeaderView view = (DayHeaderView) mViewDayWeekSwitcher.getCurrentView();
        view.clearCachedEvents();
//        view.reloadEvents();

//        view = (DayView) mViewSwitcher.getNextView();
//        view = (DayHeaderView) mViewSwitcher.getNextView();  //change by hekeyi for calendar v8.0
//        view.clearCachedEvents();

    }

    Event getSelectedEvent() {
        DayView view = (DayView) mViewDayWeekSwitcher.getCurrentView();
        return view.getSelectedEvent();
    }

    boolean isEventSelected() {
        DayView view = (DayView) mViewDayWeekSwitcher.getCurrentView();
        return view.isEventSelected();
    }

    Event getNewEvent() {
        DayView view = (DayView) mViewDayWeekSwitcher.getCurrentView();
        return view.getNewEvent();
    }

    public DayView getNextView() {
        return (DayView) mViewDayWeekSwitcher.getNextView();
    }

    public long getSupportedEventTypes() {
        return CalendarController.EventType.GO_TO | CalendarController.EventType.EVENTS_CHANGED;
    }

    public void handleEvent(CalendarController.EventInfo msg) {
        if (msg.eventType == CalendarController.EventType.GO_TO) {
// TODO support a range of time
// TODO support event_id
// TODO support select message
            goTo(msg.selectedTime, (msg.extraLong & CalendarController.EXTRA_GOTO_DATE) != 0,
                    (msg.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0);
        } else if (msg.eventType == CalendarController.EventType.EVENTS_CHANGED) {
            eventsChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        synchronized (mUpdateLoader) {
            mFirstLoadedJulianDay =
                    Time.getJulianDay(mSelectedDay.toMillis(true), mSelectedDay.gmtoff)
                            - (mNumWeeks * 7 / 2);
            mEventUri = updateUri();
            String where = updateWhere();

            loader = new CursorLoader(
                    getActivity(), mEventUri, Event.EVENT_PROJECTION, where,
                    null /* WHERE_CALENDARS_SELECTED_ARGS */, INSTANCES_SORT_ORDER);
            loader.setUpdateThrottle(LOADER_THROTTLE_DELAY);
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        synchronized (mUpdateLoader) {
            CursorLoader cLoader = (CursorLoader) loader;
            if (mEventUri == null) {
                mEventUri = cLoader.getUri();
                updateLoadedDays();
            }
            if (cLoader.getUri().compareTo(mEventUri) != 0) {
                // We've started a new query since this loader ran so ignore the
                // result
                return;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class DayViewFactory implements ViewFactory {
        @Override
        public View makeView() {
            if (mNumDays == 7) {
                mTZUpdater.run();
                DayHeaderView weekView = new DayHeaderView(getActivity(),
                            CalendarController.getInstance(getActivity()),
                            mViewDayWeekSwitcher, mEventLoader/*,2*/);
                weekView.setId(VIEW_ID);
                weekView.setLayoutParams(new ViewSwitcher.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                if (mSelectedDay == null) {
                    mSelectedDay = new Time();
                    mSelectedDay.setToNow();
                    mSelectedDay.normalize(true);
                }

                weekView.setSelected(mSelectedDay, false, false);
                weekView.reloadEvents();
                return weekView;
            } else {
                return null;
            }
        }
    }

    @Override
    public void eventsChanged(Time selectedTime) {
        // TODO Auto-generated method stub
    }

    private volatile boolean mShouldLoad = true;
    private CursorLoader mLoader;
    protected Handler mHandler = new Handler();
    protected Context mContext;
    private Uri mEventUri;
    protected int mFirstLoadedJulianDay;
    protected int mLastLoadedJulianDay;
    protected int mNumWeeks = 6;
    private static final String WHERE_CALENDARS_VISIBLE = CalendarContract.Calendars.VISIBLE + "=1";
    private static final String INSTANCES_SORT_ORDER = CalendarContract.Instances.START_DAY + ","
            + CalendarContract.Instances.START_MINUTE + "," + CalendarContract.Instances.TITLE;
    protected boolean mHideDeclined;
    protected ListView mListView;
    protected Time mTempTime = new Time();
    private static final int WEEKS_BUFFER = 1;
    private static final int LOADER_THROTTLE_DELAY = 500;
    private final Runnable mUpdateLoader = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                if (!mShouldLoad || mLoader == null) {
                    return;
                }
                // Stop any previous loads while we update the uri
                stopLoader();

                // Start the loader again
                mEventUri = updateUri();

                mLoader.setUri(mEventUri);
                mLoader.startLoading();
                mLoader.onContentChanged();
            }
        }
    };

    private void stopLoader() {
        synchronized (mUpdateLoader) {
            mHandler.removeCallbacks(mUpdateLoader);
            if (mLoader != null) {
                mLoader.stopLoading();
            }
        }
    }

    private Uri updateUri() {
        SimpleWeekView child = (SimpleWeekView) mListView.getChildAt(0);
        if (child != null) {
            int julianDay = child.getFirstJulianDay();
            mFirstLoadedJulianDay = julianDay;
        }
        // -1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mFirstLoadedJulianDay - 1);
        long start = mTempTime.toMillis(true);
        mLastLoadedJulianDay = mFirstLoadedJulianDay + (mNumWeeks + 2 * WEEKS_BUFFER) * 7;
        // +1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mLastLoadedJulianDay + 1);
        long end = mTempTime.toMillis(true);

        // Create a new uri with the updated times
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        return builder.build();
    }

    protected String updateWhere() {
        // TODO fix selection/selection args after b/3206641 is fixed
        String where = WHERE_CALENDARS_VISIBLE;
        if (mHideDeclined /*M: || !mShowDetailsInMonth*/) {
            where += " AND " + CalendarContract.Instances.SELF_ATTENDEE_STATUS + "!="
                    + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED;
        }
        return where;
    }

    private void updateLoadedDays() {
        List<String> pathSegments = mEventUri.getPathSegments();
        int size = pathSegments.size();
        if (size <= 2) {
            return;
        }
        long first = Long.parseLong(pathSegments.get(size - 2));
        long last = Long.parseLong(pathSegments.get(size - 1));
        mTempTime.set(first);
        mFirstLoadedJulianDay = Time.getJulianDay(first, mTempTime.gmtoff);
        mTempTime.set(last);
        mLastLoadedJulianDay = Time.getJulianDay(last, mTempTime.gmtoff);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
