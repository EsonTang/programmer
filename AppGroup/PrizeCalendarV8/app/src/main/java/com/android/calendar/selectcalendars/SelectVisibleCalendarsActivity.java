/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.calendar.selectcalendars;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.calendar.AbstractCalendarActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.R;

import java.lang.reflect.Field;

public class SelectVisibleCalendarsActivity extends AbstractCalendarActivity {
    private SelectVisibleCalendarsFragment mFragment;
    private CalendarController mController;

    // Create an observer so that we can update the views whenever a
    // Calendar event changes.
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
          mController.sendEvent(this, EventType.EVENTS_CHANGED, null, null, -1, ViewType.CURRENT);
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initStatusBar();
        getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_sync_actionbar, null,false));
        setContentView(R.layout.simple_frame_layout);

        mController = CalendarController.getInstance(this);
        mFragment = (SelectVisibleCalendarsFragment) getFragmentManager().findFragmentById(
                R.id.main_frame);

        if (mFragment == null) {
            mFragment = new SelectVisibleCalendarsFragment(R.layout.calendar_sync_item);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, mFragment);
            ft.show(mFragment);
            ft.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(CalendarContract.Events.CONTENT_URI,
                true, mObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    // Needs to be in proguard whitelist
    // Specified as listener via android:onClick in a layout xml
    public void handleSelectSyncedCalendarsClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, SelectSyncedCalendarsMultiAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar mActionBar = getActionBar();
      mActionBar.setElevation(getResources().getDimension(R.dimen.prizeactionbar_lines));
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /*PRIZE-modifi return PrizeSettingsActivity-xiaoping-2016-11-25-start*/
//                Utils.returnToCalendarHome(this);
                this.finish();
                /*PRIZE-modifi return PrizeSettingsActivity-xiaoping-2016-11-25-end*/
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ///M: To remove its CalendarController instance if exists @{
        CalendarController.removeInstance(this);
        ///@}
    }
    
    @Override
    public void onBackPressed() {
        /*PRIZE-modifi return PrizeSettingsActivity-xiaoping-2016-12-01-start*/
//    	Utils.returnToCalendarHome(this);
        this.finish();
        /*PRIZE-modifi return PrizeSettingsActivity-xiaoping-2016-12-01-start*/
    }

    private void initStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));

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
        }
    }

    public FrameLayout getActionBarContainer() {
        Window window = getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return (FrameLayout)v.findViewById(resId);
    }

}
