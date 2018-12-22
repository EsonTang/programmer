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

package com.android.calendar.event;
import android.Manifest;
import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.android.calendar.AbstractCalendarActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarEventModel.ReminderEntry;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.mediatek.vcalendar.valuetype.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class EditEventActivity extends AbstractCalendarActivity {
    private static final String TAG = "EditEventActivity";

    private static final boolean DEBUG = false;

    private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";

    public static final String EXTRA_EVENT_COLOR = "event_color";

    public static final String EXTRA_EVENT_REMINDERS = "reminders";

    private static boolean mIsMultipane;

    private EditEventFragment mEditFragment;

    private ArrayList<ReminderEntry> mReminders;

    private int mEventColor;

    private boolean mEventColorInitialized;

    private EventInfo mEventInfo;

    private TextView titleTx;

    @Override
    protected void onCreate(Bundle icicle) {
        Log.d(TAG, "[Calendar] onCreate() of EditEventActivity");
        super.onCreate(icicle);
        initStatusBar();
        getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_custom_actionbar, null,false));
        titleTx = (TextView) findViewById(R.id.title);
        Log.d("hekeyi","[EditEventActivity]-titleTx = "+titleTx);
//        getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_editevent_actionbar, null,false));
        setContentView(R.layout.simple_frame_layout);
        getActionBarView().setVisibility(View.GONE);

        mEventInfo = getEventInfoFromIntent(icicle);
        mReminders = getReminderEntriesFromIntent();
        mEventColorInitialized = getIntent().hasExtra(EXTRA_EVENT_COLOR);
        mEventColor = getIntent().getIntExtra(EXTRA_EVENT_COLOR, -1);


        mEditFragment = (EditEventFragment) getFragmentManager().findFragmentById(R.id.main_frame);

        mIsMultipane = Utils.getConfigBool(this, R.bool.multiple_pane_config);

        if (mIsMultipane) {
            getActionBar().setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP/* | ActionBar.DISPLAY_SHOW_HOME*/
                            | ActionBar.DISPLAY_SHOW_TITLE /*| ActionBar.DISPLAY_SHOW_CUSTOM*/
                            );
            getActionBar().setTitle(
                    mEventInfo.id == -1 ? R.string.event_create : R.string.event_edit);
            getActionBar().setElevation(getResources().getDimension(R.dimen.prizeactionbar_lines));
            if(mEventInfo.id == -1){
                titleTx.setText(R.string.event_create);
            }else{
                titleTx.setText(R.string.event_edit);
            }


        }
        else {
//            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
//                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
//                    |ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        	ActionBar bar = getActionBar();
        	if(bar != null){
	        	bar.setDisplayOptions( ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
	            bar.setTitle(getString(R.string.event_create));
        	}
        }

        if (mEditFragment == null) {
            Intent intent = null;
            if (mEventInfo.id == -1) {
                intent = getIntent();
            }

            mEditFragment = new EditEventFragment(mEventInfo, mReminders, mEventColorInitialized,
                    mEventColor, false, intent);

            mEditFragment.mShowModifyDialogOnLaunch = getIntent().getBooleanExtra(
                    CalendarController.EVENT_EDIT_ON_LAUNCH, false);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, mEditFragment);
            ft.show(mEditFragment);
            ft.commit();
        }
//        getActionBar().setElevation(getResources().getDimension(R.dimen.prizeactionbar_lines));

    }

    @SuppressWarnings("unchecked")
    private ArrayList<ReminderEntry> getReminderEntriesFromIntent() {
        Intent intent = getIntent();
        return (ArrayList<ReminderEntry>) intent.getSerializableExtra(EXTRA_EVENT_REMINDERS);
    }

    private EventInfo getEventInfoFromIntent(Bundle icicle) {
        EventInfo info = new EventInfo();
        long eventId = -1;
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            try {
                eventId = Long.parseLong(data.getLastPathSegment());
            } catch (NumberFormatException e) {
                if (DEBUG) {
                    Log.d(TAG, "Create new event");
                }
            }
        } else if (icicle != null && icicle.containsKey(BUNDLE_KEY_EVENT_ID)) {
            eventId = icicle.getLong(BUNDLE_KEY_EVENT_ID);
        }

        boolean allDay = intent.getBooleanExtra(EXTRA_EVENT_ALL_DAY, false);

        long begin = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
        long end = intent.getLongExtra(EXTRA_EVENT_END_TIME, -1);
        if (end != -1) {
            info.endTime = new Time();
            if (allDay) {
                info.endTime.timezone = Time.TIMEZONE_UTC;
            }
            info.endTime.set(end);
        }
        if (begin != -1) {
            info.startTime = new Time();
            if (allDay) {
                info.startTime.timezone = Time.TIMEZONE_UTC;
            }
            info.startTime.set(begin);
        }
        info.id = eventId;
        info.eventTitle = intent.getStringExtra(Events.TITLE);
        info.calendarId = intent.getLongExtra(Events.CALENDAR_ID, -1);

        if (allDay) {
            info.extraLong = CalendarController.EXTRA_CREATE_ALL_DAY;
        } else {
            info.extraLong = 0;
        }
        return info;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            Utils.returnToCalendarHome(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public FrameLayout getActionBarContainer() {
	    Window window = getWindow();
	    View v = window.getDecorView();
	    int resId = getResources().getIdentifier("action_bar_container", "id", "android");
	    return (FrameLayout)v.findViewById(resId);
	}
    
    public ViewGroup getActionBarView() {
	    Window window = getWindow();
	    View v = window.getDecorView();
	    int resId = getResources().getIdentifier("action_bar", "id", "android");
	    return (ViewGroup)v.findViewById(resId);
	}

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[Calendar] onDestroy() of EditEventActivity");
        super.onDestroy();
        ///M: To remove its CalendarController instance if exists @{
        CalendarController.removeInstance(this);
        ///@}
    }
    
    private static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] CALENDAR_PERMISSION = {Manifest.permission.READ_CALENDAR,
                                                    Manifest.permission.WRITE_CALENDAR};
    private static final String[] CONTACTS_PERMISSION = {Manifest.permission.READ_CONTACTS};

    protected boolean hasRequiredPermission(String[] permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[Calendar] onResume() of EditEventActivity");
        super.onResume();
        if (!hasRequiredPermission(CALENDAR_PERMISSION) ||
                !hasRequiredPermission(STORAGE_PERMISSION) ||
                !hasRequiredPermission(CONTACTS_PERMISSION)) {
                Toast.makeText(getApplicationContext(),
                    getResources().getString(com.mediatek.R.string.denied_required_permission),
                    Toast.LENGTH_LONG).show();
                mEditFragment.mView.mAttendeesList.setText(null);
                finish();
                return;
        }
    }

    private void initStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.event_info_title_bg));


        WindowManager.LayoutParams lp = getWindow().getAttributes();
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.d(TAG,"error");
        }
    }

}
