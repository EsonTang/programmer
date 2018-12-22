/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.dialer;

import android.app.ActionBar;/*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24*/
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
//import android.support.design.widget.CoordinatorLayout;/*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24*/
import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBar;/*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24*/
import android.telecom.PhoneAccount;
import android.text.BidiFormatter;
import android.text.Editable;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.FrameLayout;/*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24*/
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.dialog.ClearFrequentsDialog;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.dialer.calllog.CallLogActivity;
import com.android.dialer.calllog.CallLogFragment;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.DialpadFragment;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.dialpad.SmartDialPrefix;
import com.android.dialer.interactions.PhoneNumberInteraction;
import com.android.dialer.list.DragDropController;
import com.android.dialer.list.ListsFragment;
import com.android.dialer.list.OnDragDropListener;
import com.android.dialer.list.OnListFragmentScrolledListener;
import com.android.dialer.list.PhoneFavoriteSquareTileView;
import com.android.dialer.list.RegularSearchFragment;
import com.android.dialer.list.SearchFragment;
import com.android.dialer.list.SmartDialSearchFragment;
import com.android.dialer.list.SpeedDialFragment;
import com.android.dialer.logging.Logger;
import com.android.dialer.logging.ScreenEvent;
import com.android.dialer.settings.DialerSettingsActivity;
import com.android.dialer.util.Assert;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.IntentUtil.CallIntentBuilder;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.voicemail.VoicemailArchiveActivity;
import com.android.dialer.widget.ActionBarController;
import com.android.dialer.widget.SearchEditTextLayout;
import com.android.dialerbind.DatabaseHelperManager;
import com.android.dialerbind.ObjectFactory;
import com.android.ims.ImsManager;
import com.android.phone.common.animation.AnimUtils;
import com.android.phone.common.animation.AnimationListenerAdapter;
import com.google.common.annotations.VisibleForTesting;

import com.mediatek.contacts.util.ContactsIntent;
import com.mediatek.dialer.activities.NeedTestActivity;
import com.mediatek.dialer.compat.CompatChecker;
import com.mediatek.dialer.database.DialerDatabaseHelperEx;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.search.ThrottleContentObserver;
//import com.mediatek.dialer.util.CallAccountSelectionNotificationUtil;
import com.mediatek.dialer.util.DialerFeatureOptions;
import com.mediatek.dialer.util.DialerVolteUtils;

import java.util.ArrayList;
import java.util.List;

/*PRIZE-add-yuandailin-2016-3-15-start*/
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.Thread;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.PowerManager;
import com.mediatek.common.prizeoption.PrizeOption;
import com.mediatek.dialer.activities.CallLogMultipleDeleteActivity;
import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.dialer.calllog.CallLogFragment;
import android.net.Uri;
import java.util.TimerTask;
import java.util.Timer;
import android.content.ComponentName;
import android.provider.Settings;
/*PRIZE-add -yuandailin-2016-7-28-start*/
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
/*PRIZE-add -yuandailin-2016-7-28-end*/
/*PRIZE-add-yuandailin-2016-3-15-end*/

/*PRIZE-add-huangpengfei-2016-11-25-start*/
import com.android.dialer.calllog.CallLogNotificationsHelper;
import android.app.NotificationManager;
import android.content.ContentValues;
import com.android.dialer.compat.UserManagerCompat;
import android.os.AsyncTask;
/*PRIZE-add-huangpengfei-2016-11-25-end*/
/**
 * M: Inherited from NeedTestActivity for easy mock testing
 * The dialer tab's title is 'phone', a more common name (see strings.xml).
 */
public class DialtactsActivity extends NeedTestActivity implements View.OnClickListener,
        DialpadFragment.OnDialpadQueryChangedListener,
        OnListFragmentScrolledListener,
        CallLogFragment.HostInterface,
        DialpadFragment.HostInterface,
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        //ListsFragment.HostInterface,
        //SpeedDialFragment.HostInterface,
        SearchFragment.HostInterface,
        //OnDragDropListener,
        OnPhoneNumberPickerActionListener,
        PopupMenu.OnMenuItemClickListener,
        //ViewPager.OnPageChangeListener,
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        ActionBarController.ActivityUi {

    /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-start*/
    class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";//home key
        static final String SYSTEM_RECENT_APPS = "recentapps";//long home key

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (reason != null) {
                    if (reason.equals(SYSTEM_HOME_KEY)) {
                        isHomeKeyPressHappened = true;
                    }
                }
            }
        }
    }
    /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-end*/

    private static final String TAG = "DialtactsActivity";

    /// M: For the purpose of debugging in eng load
    public static final boolean DEBUG = Build.TYPE.equals("eng");

    public static final String SHARED_PREFS_NAME = "com.android.dialer_preferences";

    private static final String KEY_IN_REGULAR_SEARCH_UI = "in_regular_search_ui";
    private static final String KEY_IN_DIALPAD_SEARCH_UI = "in_dialpad_search_ui";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_IS_DIALPAD_SHOWN = "is_dialpad_shown";
    /// M: Save and restore the mPendingSearchViewQuery
    private static final String KEY_PENDING_SEARCH_QUERY = "pending_search_query";

    @VisibleForTesting
    public static final String TAG_DIALPAD_FRAGMENT = "dialpad";
    private static final String TAG_REGULAR_SEARCH_FRAGMENT = "search";
    private static final String TAG_SMARTDIAL_SEARCH_FRAGMENT = "smartdial";
    private static final String TAG_FAVORITES_FRAGMENT = "favorites";

    /**
     * Just for backward compatibility. Should behave as same as {@link Intent#ACTION_DIAL}.
     */
    private static final String ACTION_TOUCH_DIALER = "com.android.phone.action.TOUCH_DIALER";
    public static final String EXTRA_SHOW_TAB = "EXTRA_SHOW_TAB";

    private static final int ACTIVITY_REQUEST_CODE_VOICE_SEARCH = 1;
    /// M: Add for import/export function
    private static final int IMPORT_EXPORT_REQUEST_CODE = 2;

    private static final int FAB_SCALE_IN_DELAY_MS = 300;

    /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
    //private CoordinatorLayout mParentLayout;
    private FrameLayout mParentLayout;
    /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/

    /**
     * Fragment containing the dialpad that slides into view
     */
    protected DialpadFragment mDialpadFragment;

    /**
     * Fragment for searching phone numbers using the alphanumeric keyboard.
     */
    private RegularSearchFragment mRegularSearchFragment;

    /**
     * Fragment for searching phone numbers using the dialpad.
     */
    private SmartDialSearchFragment mSmartDialSearchFragment;

    /**
     * Animation that slides in.
     */
    private Animation mSlideIn;

    /**
     * Animation that slides out.
     */
    private Animation mSlideOut;

    AnimationListenerAdapter mSlideInListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            /*PRIZE-remove-yuandailin-2016-3-28-start*/
            //maybeEnterSearchUi();
            /*PRIZE-remove-yuandailin-2016-3-28-end*/

            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            if (null != floatingActionButton) floatingActionButton.setClickable(true);
            if (null != prizeDialerLayout) prizeDialerLayout.setClickable(true);
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        }
    };

    /**
     * Listener for after slide out animation completes on dialer fragment.
     */
    AnimationListenerAdapter mSlideOutListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            commitDialpadFragmentHide();

            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            if (null != floatingActionButton) floatingActionButton.setClickable(true);
            if (null != prizeDialerLayout) prizeDialerLayout.setClickable(true);
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        }
    };

    /**
     * Fragment containing the speed dial list, call history list, and all contacts list.
     */
    private ListsFragment mListsFragment;

    /**
     * Tracks whether onSaveInstanceState has been called. If true, no fragment transactions can
     * be commited.
     */
    private boolean mStateSaved;
    private boolean mIsRestarting;
    private boolean mInDialpadSearch;
    private boolean mInRegularSearch;
    private boolean mClearSearchOnPause;
    private boolean mIsDialpadShown = true;/*PRIZE-change-yuandailin-2015-10-14*/
    private boolean mShowDialpadOnResume = true;/*PRIZE-change-yuandailin-2015-12-8*/

    /**
     * Whether or not the device is in landscape orientation.
     */
    private boolean mIsLandscape;

    /**
     * True if the dialpad is only temporarily showing due to being in call
     */
    private boolean mInCallDialpadUp;

    /**
     * True when this activity has been launched for the first time.
     */
    private boolean mFirstLaunch;

    /**
     * Search query to be applied to the SearchView in the ActionBar once
     * onCreateOptionsMenu has been called.
     */
    private String mPendingSearchViewQuery;

    private PopupMenu mOverflowMenu;
    private EditText mSearchView;
    private View mVoiceSearchButton;

    private String mSearchQuery;
    private String mDialpadQuery;
    /* M: ALPS03361423 add for "+" is shown behind number */
    private BidiFormatter mBidiFormatter = BidiFormatter.getInstance();
    private DialerDatabaseHelper mDialerDatabaseHelper;
    private DragDropController mDragDropController;
    private ActionBarController mActionBarController;

    private FloatingActionButtonController mFloatingActionButtonController;

    private int mActionBarHeight;
    private int mPreviouslySelectedTabIndex;

    /**
     * The text returned from a voice search query.  Set in {@link #onActivityResult} and used in
     * {@link #onResume()} to populate the search box.
     */
    private String mVoiceSearchQuery;

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    private LinearLayout prizeDialerLayout;
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    /*PRIZE-add-yuandailin-2016-3-15-start*/
    private ImageButton floatingActionButton;
    private View mfloatingActionButtonContainer;
    private ImageView options_menu_button; 
    private ActionBar actionBar;
    private SearchEditTextLayout searchEditTextLayout;
    private ViewTreeObserver observer;
    private PopupMenu actionbarMenu ;
    private boolean isOnBackPressHappened=false;
    private CallLogFragment mCallLogFragment;
    private HomeKeyEventBroadCastReceiver homeKeyReceiver;
    private boolean isHomeKeyPressHappened=false;
    /*PRIZE-add-yuandailin-2016-3-15-end*/
    private int simCount;//PRIZE-add -yuandailin-2016-7-28

    /// M: [MTK Dialer Search] @{
    /**Dialer search database helper.*/
    private DialerDatabaseHelperEx mDialerDatabaseHelperEx;
    private final Handler mHandler = new Handler();
    private final ThrottleContentObserver mContactsObserver = new ThrottleContentObserver(mHandler,
            this, new Runnable() {
                @Override
                public void run() {
                    DialerDatabaseHelperEx dbHelper = DatabaseHelperManager
                            .getDialerSearchDbHelper(getApplicationContext());
                    dbHelper.startContactUpdateThread();
                }
            }, "ContactsObserver");
    private final ThrottleContentObserver mCallLogObserver = new ThrottleContentObserver(mHandler,
            this, new Runnable() {
                @Override
                public void run() {
                    DialerDatabaseHelperEx dbHelper = DatabaseHelperManager
                            .getDialerSearchDbHelper(getApplicationContext());
                    dbHelper.startCallLogUpdateThread();
                }
            }, "CallLogObserver");
    /// @}

    /*PRIZE-remove-yuandailin-2016-3-15-start*/
    /*protected class OptionsPopupMenu extends PopupMenu {
        public OptionsPopupMenu(Context context, View anchor) {
            super(context, anchor, Gravity.END);
        }

        @Override
        public void show() {
            final boolean hasContactsPermission =
                    PermissionsUtil.hasContactsPermissions(DialtactsActivity.this);
            final Menu menu = getMenu();
            final MenuItem clearFrequents = menu.findItem(R.id.menu_clear_frequents);
            clearFrequents.setVisible(mListsFragment != null &&
                    mListsFragment.getSpeedDialFragment() != null &&
                    mListsFragment.getSpeedDialFragment().hasFrequents() && hasContactsPermission);

            menu.findItem(R.id.menu_import_export).setVisible(hasContactsPermission);
            menu.findItem(R.id.menu_add_contact).setVisible(hasContactsPermission);

            menu.findItem(R.id.menu_history).setVisible(
                    PermissionsUtil.hasPhonePermissions(DialtactsActivity.this));
            /// M: [VoLTE ConfCall] Show conference call menu for VoLTE @{
            boolean visible = DialerVolteUtils
                    .isVolteConfCallEnable(DialtactsActivity.this) && hasContactsPermission;
            menu.findItem(R.id.menu_volte_conf_call).setVisible(visible);
            /// @}
            super.show();
        }
    }*/

    /**
     * Listener that listens to drag events and sends their x and y coordinates to a
     * {@link DragDropController}.
     */
    /*private class LayoutOnDragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                mDragDropController.handleDragHovered(v, (int) event.getX(), (int) event.getY());
            }
            return true;
        }
    }*/

    /**
     * Listener used to send search queries to the phone search fragment.
     */
    /*private final TextWatcher mPhoneSearchQueryTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String newText = s.toString();
            if (newText.equals(mSearchQuery)) {
                // If the query hasn't changed (perhaps due to activity being destroyed
                // and restored, or user launching the same DIAL intent twice), then there is
                // no need to do anything here.
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "onTextChange for mSearchView called with new query: " + newText);
                Log.d(TAG, "Previous Query: " + mSearchQuery);
            }
            mSearchQuery = newText;

            // Show search fragment only when the query string is changed to non-empty text.
            if (!TextUtils.isEmpty(newText)) {
                // Call enterSearchUi only if we are switching search modes, or showing a search
                // fragment for the first time.
                final boolean sameSearchMode = (mIsDialpadShown && mInDialpadSearch) ||
                        (!mIsDialpadShown && mInRegularSearch);
                if (!sameSearchMode) {*/
                    //enterSearchUi(mIsDialpadShown, mSearchQuery, true /* animate */);
                /*}
            }

            if (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()) {*/
                //mSmartDialSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
            //} else if (mRegularSearchFragment != null && mRegularSearchFragment.isVisible()) {
                //mRegularSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
            /*}
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };*/


    /**
     * Open the search UI when the user clicks on the search box.
     */
    /*private final View.OnClickListener mSearchViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isInSearchUi()) {
                mActionBarController.onSearchBoxTapped();*/
                //enterSearchUi(false /* smartDialSearch */, mSearchView.getText().toString(),
                //        true /* animate */);
            /*}
        }
    };*/

    /**
     * Handles the user closing the soft keyboard.
     */
    /*private final View.OnKeyListener mSearchEditTextLayoutListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (TextUtils.isEmpty(mSearchView.getText().toString())) {
                    // If the search term is empty, close the search UI.
                    maybeExitSearchUi();
                    /// M: end the back key dispatch to avoid activity onBackPressed is called.
                    return true;
                } else {
                    // If the search term is not empty, show the dialpad fab.
                    showFabInSearchUi();
                }
            }
            return false;
        }
    };*/
    /*PRIZE-remove-yuandailin-2016-3-15-end*/

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace.beginSection(TAG + " onCreate");
        super.onCreate(savedInstanceState);

        Settings.System.putInt(getContentResolver(), Settings.System.VOICE_CALL_REJECT_MODE, 2);//PRIZE-black number list-yuandailin-2016-1-29
        mFirstLaunch = true;
        /*PRIZE-change-yuandailin-2016-3-28-start*/
        /*final Resources resources = getResources();
        mActionBarHeight = resources.getDimensionPixelSize(R.dimen.action_bar_height_large);*/

        Trace.beginSection(TAG + " setContentView");
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        /*setContentView(R.layout.dialtacts_activity);*/
        setContentView(R.layout.prize_dialtacts_activity);
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        Trace.endSection();
        getWindow().setBackgroundDrawable(null);

        Trace.beginSection(TAG + " setup Views");

        actionBar = getActionBar();
        actionBar.hide();
        /*final ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.search_edittext);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(null);

        SearchEditTextLayout searchEditTextLayout = (SearchEditTextLayout) actionBar
                .getCustomView().findViewById(R.id.search_view_container);
        searchEditTextLayout.setPreImeKeyListener(mSearchEditTextLayoutListener);

        mActionBarController = new ActionBarController(this, searchEditTextLayout);

        mSearchView = (EditText) searchEditTextLayout.findViewById(R.id.search_view);
        mSearchView.addTextChangedListener(mPhoneSearchQueryTextListener);
        mVoiceSearchButton = searchEditTextLayout.findViewById(R.id.voice_search_button);
        searchEditTextLayout.findViewById(R.id.search_magnifying_glass)
                .setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.findViewById(R.id.search_box_start_search)
                .setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.setOnClickListener(mSearchViewOnClickListener);
        searchEditTextLayout.setCallback(new SearchEditTextLayout.Callback() {
            @Override
            public void onBackButtonClicked() {
                onBackPressed();
            }

            @Override
            public void onSearchViewClicked() {
                // Hide FAB, as the keyboard is shown.
                mFloatingActionButtonController.scaleOut();
            }
        });*/

        mIsLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        mPreviouslySelectedTabIndex = ListsFragment.TAB_INDEX_SPEED_DIAL;
        final View floatingActionButtonContainer = findViewById(R.id.floating_action_button_container);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*final LinearLayout prizeDialerLayout= (LinearLayout)findViewById(R.id.prize_dialer_layout);*/
        prizeDialerLayout = (LinearLayout) findViewById(R.id.prize_dialer_layout);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/

        prizeDialerLayout.setOnClickListener(this);
//        TextView prizeDialerText = (TextView) findViewById(R.id.prize_dialer_text);
//        prizeDialerText.setOnClickListener(this);
	    mfloatingActionButtonContainer=floatingActionButtonContainer;
        floatingActionButton = (ImageButton) findViewById(R.id.floating_action_button);	
	    floatingActionButton.setBackground(getResources().getDrawable(R.drawable.prize_hide_dialpad));
        floatingActionButton.setOnClickListener(this);
        mFloatingActionButtonController = new FloatingActionButtonController(this,floatingActionButtonContainer, floatingActionButton);
		final ImageButton  prizeJumpIntoContactsButton =(ImageButton) findViewById(R.id.prize_jump_into_contacts_button); 
        final LinearLayout prizeContactsLayout= (LinearLayout)findViewById(R.id.prize_contacts_layout);

        /*PRIZE-change-huangpenegfei-2016-8-9-start*/
        final TextView prizeContactsText = (TextView) findViewById(R.id.prize_contacts_text);
        prizeContactsLayout.setOnClickListener(this);
        prizeContactsLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        prizeJumpIntoContactsButton.setBackgroundResource(R.drawable.prize_into_contacts_off);
                        prizeContactsText.setTextColor(DialtactsActivity.this.getResources().getColor(R.color.prize_dialer_bottom_text_selected_color));
                        break;
                    case MotionEvent.ACTION_UP:
                        prizeJumpIntoContactsButton.setBackgroundResource(R.drawable.prize_into_contacts_normal);
                        prizeContactsText.setTextColor(DialtactsActivity.this.getResources().getColor(R.color.prize_dialer_bottom_text_default_color));
                        break;
                }
                return false;
            }
        });
        /*PRIZE-change-huangpenegfei-2016-8-9-end*/

        /*ImageButton optionsMenuButton =
                (ImageButton) searchEditTextLayout.findViewById(R.id.dialtacts_options_menu_button);
        optionsMenuButton.setOnClickListener(this);
        mOverflowMenu = buildOptionsMenu(searchEditTextLayout);
        optionsMenuButton.setOnTouchListener(mOverflowMenu.getDragToOpenListener());*/

        // Add the favorites fragment but only if savedInstanceState is null. Otherwise the
        // fragment manager is responsible for recreating it.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                      .add(R.id.dialtacts_container, new CallLogFragment(), TAG_FAVORITES_FRAGMENT)
                      .add(R.id.dialtacts_frame, new DialpadFragment(), TAG_DIALPAD_FRAGMENT)
                    .commit();
        } else {
            mSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
            mInRegularSearch = savedInstanceState.getBoolean(KEY_IN_REGULAR_SEARCH_UI);
            mInDialpadSearch = savedInstanceState.getBoolean(KEY_IN_DIALPAD_SEARCH_UI);
            mFirstLaunch = savedInstanceState.getBoolean(KEY_FIRST_LAUNCH);
            mShowDialpadOnResume = savedInstanceState.getBoolean(KEY_IS_DIALPAD_SHOWN);
            /// M: Save and restore the mPendingSearchViewQuery
            /*mPendingSearchViewQuery = savedInstanceState.getString(KEY_PENDING_SEARCH_QUERY);
            mActionBarController.restoreInstanceState(savedInstanceState);*/
        }

        final boolean isLayoutRtl = DialerUtils.isRtl();
        if (mIsLandscape) {
            mSlideIn = AnimationUtils.loadAnimation(this,
                    isLayoutRtl ? R.anim.dialpad_slide_in_left : R.anim.dialpad_slide_in_right);
            mSlideOut = AnimationUtils.loadAnimation(this,
                    isLayoutRtl ? R.anim.dialpad_slide_out_left : R.anim.dialpad_slide_out_right);
        } else {
            mSlideIn = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_in_bottom);
            mSlideOut = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_out_bottom);
        }

        mSlideIn.setInterpolator(AnimUtils.EASE_IN);
        mSlideOut.setInterpolator(AnimUtils.EASE_OUT);

        mSlideIn.setAnimationListener(mSlideInListener);
        mSlideOut.setAnimationListener(mSlideOutListener);

        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        //mParentLayout = (CoordinatorLayout) findViewById(R.id.dialtacts_mainlayout);
        mParentLayout = (FrameLayout) findViewById(R.id.dialtacts_mainlayout);
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
        //mParentLayout.setOnDragListener(new LayoutOnDragListener());
        floatingActionButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final ViewTreeObserver observer =
                                floatingActionButtonContainer.getViewTreeObserver();
                        if (!observer.isAlive()) {
                            return;
                        }
                        observer.removeOnGlobalLayoutListener(this);
                        int screenWidth = mParentLayout.getWidth();
                        mFloatingActionButtonController.setScreenWidth(screenWidth);
                        //mFloatingActionButtonController.align(
                        //        getFabAlignment(), false /* animate */);
                    }
                });
        /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-start*/
        homeKeyReceiver = new HomeKeyEventBroadCastReceiver();
        registerReceiver(homeKeyReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-end*/
        /*PRIZE-change-yuandailin-2016-3-28-end*/

        Trace.endSection();

        Trace.beginSection(TAG + " initialize smart dialing");

        /// M: [MTK Dialer Search] @{
        if (DialerFeatureOptions.isDialerSearchEnabled()) {
            mDialerDatabaseHelperEx = DatabaseHelperManager.getDialerSearchDbHelper(this);
            mDialerDatabaseHelperEx.startSmartDialUpdateThread();

            // Monitor this so that we can update callLog info if dismiss an incoming call or
            // hang up a call in dialer UI
            mCallLogObserver.register(Calls.CONTENT_URI);
            // Monitor this so that we can update contact info
            // when importing a large number of contacts
            mContactsObserver.register(ContactsContract.Contacts.CONTENT_URI);
        } else {
            mDialerDatabaseHelper = DatabaseHelperManager.getDatabaseHelper(this);
            SmartDialPrefix.initializeNanpSettings(this);
        }
        /// @}
        ///M:[portable]
        CompatChecker.getInstance(this).startCheckerThread();

        Trace.endSection();
        Trace.endSection();
    }

    /*PRIZE-add -yuandailin-2016-7-28-start*/
    private int getSimCountInfact(){
        List<SubscriptionInfo> mSubInfoList = SubscriptionManager.from(this).getActiveSubscriptionInfoList();
        int simCount=0;
        if(mSubInfoList !=null){
            simCount=mSubInfoList.size();
        }
        return simCount;
    }

    public int getSimCountFromDialtactsActivity(){
        return simCount;
    }
    /*PRIZE-add -yuandailin-2016-7-28-end*/

    @Override
    protected void onResume() {
        Trace.beginSection(TAG + " onResume");
        super.onResume();
         /*prize  fixbug  for  57880   @longzhongping-2018.05.16-start*/
         /* mStateSaved = false;*/
        if(mStateSaved){
            mStateSaved = false;
            if(TextUtils.isEmpty(mSearchQuery)){
                    hideSearchView();
            }
        }
         /*prize  fixbug  for  57880   @longzhongping-2018.05.16-end*/

        if (mFirstLaunch) {
            displayFragment(getIntent());
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*} else if (!phoneIsInUse() && mInCallDialpadUp) {
            hideDialpadFragment(false, true);
            mInCallDialpadUp = false;*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        } else if (mShowDialpadOnResume) {
            showDialpadFragment(false);
            mShowDialpadOnResume = false;
        }

        /*PRIZE-change-yuandailin-2015-12-2-start*/
        if(mCallLogFragment!=null && mCallLogFragment.getCurrentCallTypeFilter()!=-1){
            mCallLogFragment.setCurrentCallTypeFilterInAll();
        }
        /*PRIZE-change-yuandailin-2015-12-2-end*/

        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        // If there was a voice query result returned in the {@link #onActivityResult} callback, it
        // will have been stashed in mVoiceSearchQuery since the search results fragment cannot be
        // shown until onResume has completed.  Active the search UI and set the search term now.
        /*if (!TextUtils.isEmpty(mVoiceSearchQuery)) {
            mActionBarController.onSearchBoxTapped();
            mSearchView.setText(mVoiceSearchQuery);
            mVoiceSearchQuery = null;
        }*/

        mFirstLaunch = false;

        /*if (mIsRestarting) {
            // This is only called when the activity goes from resumed -> paused -> resumed, so it
            // will not cause an extra view to be sent out on rotation
            if (mIsDialpadShown) {
                Logger.logScreenView(ScreenEvent.DIALPAD, this);
            }
            mIsRestarting = false;
        }

        prepareVoiceSearchButton();

        /// M: [MTK Dialer Search] @{
        if (!DialerFeatureOptions.isDialerSearchEnabled()) {
            mDialerDatabaseHelper.startSmartDialUpdateThread();
        }*/
        /// @}

        //mFloatingActionButtonController.align(getFabAlignment(), false /* animate */);

        //CallAccountSelectionNotificationUtil.getInstance(this).showNotification(true, this);
        /*if (Calls.CONTENT_TYPE.equals(getIntent().getType())) {
            // Externally specified extras take precedence to EXTRA_SHOW_TAB, which is only
            // used internally.
            final Bundle extras = getIntent().getExtras();
            if (extras != null
                    && extras.getInt(Calls.EXTRA_CALL_TYPE_FILTER) == Calls.VOICEMAIL_TYPE) {
                mListsFragment.showTab(ListsFragment.TAB_INDEX_VOICEMAIL);
            } else {
                mListsFragment.showTab(ListsFragment.TAB_INDEX_HISTORY);
            }
        } else if (getIntent().hasExtra(EXTRA_SHOW_TAB)) {
            int index = getIntent().getIntExtra(EXTRA_SHOW_TAB, ListsFragment.TAB_INDEX_SPEED_DIAL);
            if (index < mListsFragment.getTabCount()) {
                mListsFragment.showTab(index);
            }
        }*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/

        setSearchBoxHint();
        simCount = getSimCountInfact();//PRIZE-add -yuandailin-2016-7-28
        /*prize-add-huangpengfei-2016-11-25-start*/
        Log.d(TAG,"[onResume]");

        clearMissedCalls();
  
        /*prize-add-huangpengfei-2016-11-25-end*/
        
        Trace.endSection();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*PRIZE-add-yuandailin-2016-3-28-start*/
        if(isOnBackPressHappened){
            isOnBackPressHappened=false;
            if(!mShowDialpadOnResume)
            mShowDialpadOnResume =true;
        }
        if(mCallLogFragment !=null)
            mCallLogFragment.popWindowDismiss();
            //mCallLogFragment.dialerMenuPopWindowDismiss();//PRIZE-change -yuandailin-2016-8-24
        if(isHomeKeyPressHappened){
            isHomeKeyPressHappened = false;
            if(!mShowDialpadOnResume)
            mShowDialpadOnResume =true;
        }
        /*PRIZE-add-yuandailin-2016-3-28-end*/
        mIsRestarting = true;
    }

    @Override
    protected void onPause() {
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        /*if (null != mDialpadFragment) {
            mDialpadFragment.clearDialpad();
        }*/
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        // Only clear missed calls if the pause was not triggered by an orientation change
        // (or any other confirguration change)
    	Log.d(TAG, "[onPause]   isChangingConfigurations() = "+isChangingConfigurations());
        if (!isChangingConfigurations()) {
            updateMissedCalls();
        }
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*if (mClearSearchOnPause) {
            hideDialpadAndSearchUi();
            mClearSearchOnPause = false;
        }
        if (mSlideOut.hasStarted() && !mSlideOut.hasEnded()) {
            commitDialpadFragmentHide();
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (ImsManager.isWfcEnabledByUser(this) && (mDialpadFragment != null)) {
            mDialpadFragment.stopWfcNotification();
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /// @}
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /// M: [MTK Dialer Search] @{
        if (DialerFeatureOptions.isDialerSearchEnabled()) {
            mCallLogObserver.unregister();
            mContactsObserver.unregister();
        }
        super.onDestroy();
        /// @}
        /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-start*/
        unregisterReceiver(homeKeyReceiver);
        /*PRIZE-add the brocast for the home key-yuandailin-2015-11-3-end*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_QUERY, mSearchQuery);
        outState.putBoolean(KEY_IN_REGULAR_SEARCH_UI, mInRegularSearch);
        outState.putBoolean(KEY_IN_DIALPAD_SEARCH_UI, mInDialpadSearch);
        outState.putBoolean(KEY_FIRST_LAUNCH, mFirstLaunch);
        outState.putBoolean(KEY_IS_DIALPAD_SHOWN, mIsDialpadShown);
        /// M: Save and restore the mPendingSearchViewQuery
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*outState.putString(KEY_PENDING_SEARCH_QUERY, mPendingSearchViewQuery);
        mActionBarController.saveInstanceState(outState);*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        mStateSaved = true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof DialpadFragment) {
            mDialpadFragment = (DialpadFragment) fragment;
            if (mShowDialpadOnResume) {//PRIZE-change-yuandailin-2015-12-8
                final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.show(mDialpadFragment);//change-yuandailin-2015-10-14
                transaction.commit();
            }
        } else if (fragment instanceof SmartDialSearchFragment) {
            mSmartDialSearchFragment = (SmartDialSearchFragment) fragment;
            mSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
            /*PRIZE-remove-yuandailin-2016-4-19-start*/
            /*if (!TextUtils.isEmpty(mDialpadQuery)) {
                mSmartDialSearchFragment.setAddToContactNumber(mDialpadQuery);
            }*/
            /*PRIZE-remove-yuandailin-2016-4-19-end*/
        } else if (fragment instanceof SearchFragment) {
            mRegularSearchFragment = (RegularSearchFragment) fragment;
            mRegularSearchFragment.setOnPhoneNumberPickerActionListener(this);
        /*PRIZE-change-yuandailin-2016-3-28-start*/
        /*} else if (fragment instanceof ListsFragment) {
            mListsFragment = (ListsFragment) fragment;
            mListsFragment.addOnPageChangeListener(this);
        }*/
        }else if (fragment instanceof CallLogFragment){
            mCallLogFragment = (CallLogFragment) fragment;
        }
        /// M: Show the FAB when the user touches the SearchFragment @{
        if (fragment instanceof SearchFragment) {
            ((SearchFragment)fragment).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Show the FAB when the user touches the lists fragment and the soft
                    // keyboard is hidden.
                    if (!mFloatingActionButtonController.isVisible()) {
                        hideDialpadFragment(true, false);
                        //showFabInSearchUi();/*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24*/
                    }
                    return false;
                }
            });
        }
        /// @}
        /*PRIZE-change-yuandailin-2016-3-28-end*/
    }

    public void handleMenuSettings() {//PRIZE-change-yuandailin-2016-4-6
        final Intent intent = new Intent(this, DialerSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int resId = view.getId();
        if (resId == R.id.floating_action_button || resId == R.id.prize_dialer_layout) {
            /// M: To make sure that it can not add contact in any search mode(regular or smart)
            /*PRIZE-remove-yuandailin-2016-3-28-start*/	
            /*if (mListsFragment.getCurrentTabIndex() == ListsFragment.TAB_INDEX_ALL_CONTACTS
                    && !mInRegularSearch && !mInDialpadSearch) {
                DialerUtils.startActivityWithErrorToast(
                        this,
                        IntentUtil.getNewContactIntent(),
                        R.string.add_contact_not_available);
            } else if (!mIsDialpadShown) {
                mInCallDialpadUp = false;*/
            /*PRIZE-remove-yuandailin-2016-3-28-end*/

            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            if (null != floatingActionButton) floatingActionButton.setClickable(false);
            if (null != prizeDialerLayout) prizeDialerLayout.setClickable(false);
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

            if (mIsDialpadShown) {
                hideDialpadFragment(true, false);
            } else {
                showDialpadFragment(true);
            }
        /*} else if (resId == R.id.voice_search_button) {
            try {
                startActivityForResult(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
                        ACTIVITY_REQUEST_CODE_VOICE_SEARCH);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(DialtactsActivity.this, R.string.voice_search_not_available,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (resId == R.id.dialtacts_options_menu_button) {
            mOverflowMenu.show();*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        /*PRIZE-change-huangpengfei-2016-8-9*/      
        } else if (resId == R.id.prize_contacts_layout) {
            Intent intent = new Intent();
            intent.setAction("com.android.contacts.action.LIST_DEFAULT");
            startActivity(intent);
        } else {
            Log.wtf(TAG, "Unexpected onClick event from " + view);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!isSafeToCommitTransactions()) {
            return true;
        }

        int resId = item.getItemId();
        if (resId == R.id.menu_history) {// Use explicit CallLogActivity intent instead of ACTION_VIEW +
            // CONTENT_TYPE, so that we always open our call log from our dialer
            final Intent intent = new Intent(this, CallLogActivity.class);
            startActivity(intent);
        } else if (resId == R.id.menu_add_contact) {
            DialerUtils.startActivityWithErrorToast(
                    this,
                    IntentUtil.getNewContactIntent(),
                    R.string.add_contact_not_available);
        } else if (resId == R.id.menu_import_export) {// We hard-code the "contactsAreAvailable" argument because doing it properly would
            // involve querying a {@link ProviderStatusLoader}, which we don't want to do right
            // now in Dialtacts for (potential) performance reasons. Compare with how it is
            // done in {@link PeopleActivity}.
            /**
             * M: When it is A1 project,use Google import/export function or
             * use MTK. @{
             */
            if (DialerFeatureOptions.isA1ProjectEnabled()) {
                if (mListsFragment.getCurrentTabIndex() == ListsFragment.TAB_INDEX_SPEED_DIAL) {
                    ImportExportDialogFragment.show(getFragmentManager(), true,
                            DialtactsActivity.class, ImportExportDialogFragment.EXPORT_MODE_FAVORITES);
                } else {
                    ImportExportDialogFragment.show(getFragmentManager(), true,
                            DialtactsActivity.class, ImportExportDialogFragment.EXPORT_MODE_DEFAULT);
                }
            } else {
                final Intent importIntent = new Intent(
                        ContactsIntent.LIST.ACTION_IMPORTEXPORT_CONTACTS);
                importIntent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
                        DialtactsActivity.class.getName());
                try {
                    startActivityForResult(importIntent, IMPORT_EXPORT_REQUEST_CODE);
                } catch (ActivityNotFoundException ex) {
                    if (mListsFragment.getCurrentTabIndex() == ListsFragment.TAB_INDEX_SPEED_DIAL) {
                        ImportExportDialogFragment.show(getFragmentManager(), true,
                                DialtactsActivity.class, ImportExportDialogFragment.EXPORT_MODE_FAVORITES);
                    } else {
                        ImportExportDialogFragment.show(getFragmentManager(), true,
                                DialtactsActivity.class, ImportExportDialogFragment.EXPORT_MODE_DEFAULT);
                    }
                }
            }
            /** @} */
            Logger.logScreenView(ScreenEvent.IMPORT_EXPORT_CONTACTS, this);
            return true;
        } else if (resId == R.id.menu_clear_frequents) {
            ClearFrequentsDialog.show(getFragmentManager());
            Logger.logScreenView(ScreenEvent.CLEAR_FREQUENTS, this);
            return true;
        } else if (resId == R.id.menu_call_settings) {
            handleMenuSettings();
            Logger.logScreenView(ScreenEvent.SETTINGS, this);
            return true;
        } else if (resId == R.id.menu_archive) {
            final Intent intent = new Intent(this, VoicemailArchiveActivity.class);
            startActivity(intent);
            return true;
        /** M: [VoLTE ConfCall] handle conference call menu. @{ */
        } else if (resId == R.id.menu_volte_conf_call) {
            DialerVolteUtils.handleMenuVolteConfCall(this);
            return true;
        /** @} */
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_CODE_VOICE_SEARCH) {
            if (resultCode == RESULT_OK) {
                final ArrayList<String> matches = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (matches.size() > 0) {
                    final String match = matches.get(0);
                    mVoiceSearchQuery = match;
                } else {
                    Log.e(TAG, "Voice search - nothing heard");
                }
            } else {
                Log.e(TAG, "Voice search failed");
            }
        }
        /** M: [VoLTE ConfCall] Handle the volte conference call. @{ */
        else if (requestCode == DialerVolteUtils.ACTIVITY_REQUEST_CODE_PICK_PHONE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                DialerVolteUtils.launchVolteConfCall(this, data);
            } else {
                Log.d(TAG, "No contacts picked, Volte conference call cancelled.");
            }
        }
        /** @} */
        /** M: [Import/Export] Handle the import/export activity result. @{ */
        else if (requestCode == IMPORT_EXPORT_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Import/Export activity create failed! ");
            } else {
                Log.d(TAG, "Import/Export activity create successfully! ");
            }
        }
        /** @} */

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Update the number of unread voicemails (potentially other tabs) displayed next to the tab
     * icon.
     */
    public void updateTabUnreadCounts() {
        mListsFragment.updateTabUnreadCounts();
    }

    /**
     * Initiates a fragment transaction to show the dialpad fragment. Animations and other visual
     * updates are handled by a callback which is invoked after the dialpad fragment is shown.
     * @see #onDialpadShown
     */
    private void showDialpadFragment(boolean animate) {
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if (null != mCallLogFragment && null != mCallLogFragment.getAdapter()) {
            mCallLogFragment.getAdapter().resetAllItem();
        }
        if (null != mSmartDialSearchFragment && null != ((SearchFragment) mSmartDialSearchFragment).getAdapter()) {
            if (((SearchFragment) mSmartDialSearchFragment).getAdapter() instanceof com.android.dialer.list.DialerPhoneNumberListAdapter) {
                ((com.android.dialer.list.DialerPhoneNumberListAdapter) ((SearchFragment) mSmartDialSearchFragment).getAdapter()).hideExpandView();
                ((com.android.dialer.list.DialerPhoneNumberListAdapter) ((SearchFragment) mSmartDialSearchFragment).getAdapter()).recordClickedItem(-1, null);
            }
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        /*PRIZE-change-yuandailin-2016-3-28-start*/
        /*if (mIsDialpadShown || mStateSaved) {
            return;
        }*/
        mIsDialpadShown = true;
        mShowDialpadOnResume = true;//PRIZE-add-yuandailin-2015-12-8
        //mListsFragment.setUserVisibleHint(false);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        /*if (mDialpadFragment == null) {
            mDialpadFragment = new DialpadFragment();
            ft.add(R.id.dialtacts_container, mDialpadFragment, TAG_DIALPAD_FRAGMENT);
        } else {*/
            ft.show(mDialpadFragment);
        //}

        mDialpadFragment.setAnimate(animate);
        Logger.logScreenView(ScreenEvent.DIALPAD, this);
        ft.commit();

        floatingActionButton.setBackground(getResources().getDrawable(R.drawable.prize_hide_dialpad));
        /*if (animate) {
            mFloatingActionButtonController.scaleOut();
        } else {
            mFloatingActionButtonController.setVisible(false);
            maybeEnterSearchUi();
        }
        mActionBarController.onDialpadUp();

        mListsFragment.getView().animate().alpha(0).withLayer();*/
        /*PRIZE-change-yuandailin-2016-3-28-end*/
        //adjust the title, so the user will know where we're at when the activity start/resumes.
        setTitle(R.string.launcherDialpadActivityLabel);
    }

    /**
     * Callback from child DialpadFragment when the dialpad is shown.
     */
    public void onDialpadShown() {
        Assert.assertNotNull(mDialpadFragment);
        if (mDialpadFragment.getAnimate()) {
            mDialpadFragment.getView().startAnimation(mSlideIn);
        } else {
            mDialpadFragment.setYFraction(0);
        }

        //updateSearchFragmentPosition();
    }

    /**
     * Initiates animations and other visual updates to hide the dialpad. The fragment is hidden in
     * a callback after the hide animation ends.
     * @see #commitDialpadFragmentHide
     */
    public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
        if (mDialpadFragment == null || mDialpadFragment.getView() == null) {
            return;
        }
        Log.d(TAG,"hideDialpadFragment enter");
        if (clearDialpad) {
            // Temporarily disable accessibility when we clear the dialpad, since it should be
            // invisible and should not announce anything.
            mDialpadFragment.getDigitsWidget().setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            //mDialpadFragment.clearDialpad();//PRIZE-remove-yuandailin-2015-12-14
            mDialpadFragment.getDigitsWidget().setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
        if (!mIsDialpadShown) {
            return;
        }
        mShowDialpadOnResume = false;//PRIZE-add-yuandailin-2015-12-8

        mIsDialpadShown = false;
        mDialpadFragment.setAnimate(animate);
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*mListsFragment.setUserVisibleHint(true);
        mListsFragment.sendScreenViewForCurrentPosition();

        updateSearchFragmentPosition();

        mFloatingActionButtonController.align(getFabAlignment(), animate);*/
        if (animate) {
            mDialpadFragment.getView().startAnimation(mSlideOut);
        } else {
            commitDialpadFragmentHide();
        }
        floatingActionButton.setBackground(getResources().getDrawable(R.drawable.prize_show_dialpad_drawable));
        /*mActionBarController.onDialpadDown();

        if (isInSearchUi()) {
            if (TextUtils.isEmpty(mSearchQuery)) {
                exitSearchUi();
            }
        }*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        //reset the title to normal.
        setTitle(R.string.launcherActivityLabel);
    }

    /**
     * Finishes hiding the dialpad fragment after any animations are completed.
     */
    private void commitDialpadFragmentHide() {
        if (!mStateSaved && mDialpadFragment != null && !mDialpadFragment.isHidden()) {
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(mDialpadFragment);
            ft.commit();
        }
        //mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
    }

    /*private void updateSearchFragmentPosition() {
        SearchFragment fragment = null;
        **
         * M: update the space height after dialpad show when in smart search
         * mode, even SmartDialerSearchFragment is not visible, in order to
         * resize ListView Height right after rotation(can not get dialpad
         * height before onHiddenChanged, that make ListView height wrong).
         *
        if (mSmartDialSearchFragment != null && (mSmartDialSearchFragment.isVisible()
                || mInDialpadSearch)) {
            fragment = mSmartDialSearchFragment;
        } else if (mRegularSearchFragment != null && mRegularSearchFragment.isVisible()) {
            fragment = mRegularSearchFragment;
        }
        if (fragment != null && fragment.isVisible()) {*/
            //fragment.updatePosition(true /* animate */);
        /*}
    }*/

    @Override
    public boolean isInSearchUi() {
        return mInDialpadSearch || mInRegularSearch;
    }

    @Override
    public boolean hasSearchQuery() {
        return !TextUtils.isEmpty(mSearchQuery);
    }

    @Override
    public boolean shouldShowActionBar() {
        Log.d(TAG, "shouldShowActionBar = " + mListsFragment.shouldShowActionBar());
    /*PRIZE-change-yuandailin-2016-3-28-start*/	
        //return mListsFragment.shouldShowActionBar();
        return false;
    /*PRIZE-change-yuandailin-2016-3-28-end*/	
    }

    private void setNotInSearchUi() {
        mInDialpadSearch = false;
        mInRegularSearch = false;
    }

    private void hideDialpadAndSearchUi() {
        if (mIsDialpadShown) {
            hideDialpadFragment(false, true);
        } else {
            exitSearchUi();
        }
    }
    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /*private void prepareVoiceSearchButton() {
        final Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);*/
        /**
         * M: [ALPS02227737] set value for view to record the voice search
         * button status @{
         */
        /*boolean canBeHandled = canIntentBeHandled(voiceIntent);
        SearchEditTextLayout searchBox = (SearchEditTextLayout) getSupportActionBar()
                .getCustomView();
        if (searchBox != null) {
            searchBox.setCanHandleSpeech(canBeHandled);
        }*/
        /** @} */
        /*if (canBeHandled) {
            mVoiceSearchButton.setVisibility(View.VISIBLE);
            mVoiceSearchButton.setOnClickListener(this);
        } else {
            mVoiceSearchButton.setVisibility(View.GONE);
        }
    }*/

    public boolean isNearbyPlacesSearchEnabled() {
        return false;
    }

    protected int getSearchBoxHint () {
        return R.string.dialer_hint_find_contact;
    }

    /**
     * Sets the hint text for the contacts search box
     */
    private void setSearchBoxHint() {
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        /*SearchEditTextLayout searchEditTextLayout = (SearchEditTextLayout) getSupportActionBar()
                .getCustomView().findViewById(R.id.search_view_container);
        ((TextView) searchEditTextLayout.findViewById(R.id.search_box_start_search))
                .setHint(getSearchBoxHint());*/

        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        //actionBar = getSupportActionBar();
        actionBar = getActionBar();
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
        if (null != actionBar) {
            View v = actionBar.getCustomView();
            if (null != v) {
                SearchEditTextLayout searchEditTextLayout = (SearchEditTextLayout) v.findViewById(R.id.search_view_container);
                if (null != searchEditTextLayout) {
                    ((TextView) searchEditTextLayout.findViewById(R.id.search_box_start_search))
                            .setHint(getSearchBoxHint());
                }
            }
        }
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
    }

    /*protected OptionsPopupMenu buildOptionsMenu(View invoker) {
        final OptionsPopupMenu popupMenu = new OptionsPopupMenu(this, invoker);
        popupMenu.inflate(R.menu.dialtacts_options);
        if (ObjectFactory.isVoicemailArchiveEnabled(this)) {
            popupMenu.getMenu().findItem(R.id.menu_archive).setVisible(true);
        }

        /// M: add for plug-in. @{
        final Menu menu = popupMenu.getMenu();
        ExtensionManager.getInstance().getDialPadExtension().buildOptionsMenu(this, menu);
        /// @}

        popupMenu.setOnMenuItemClickListener(this);
        return popupMenu;
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/
    /*PRIZE-change-yuandailin-2016-3-28-start*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** M: Modify to set the pending search query only when dialpad is visible. @{ */
        /*if (mPendingSearchViewQuery != null
                && mDialpadFragment != null && mDialpadFragment.isVisible()) {
            mSearchView.setText(mPendingSearchViewQuery);
            mPendingSearchViewQuery = null;
        }*/
        /** @} */
        /*if (mActionBarController != null) {
            mActionBarController.restoreActionBarOffset();
        }
        return false;*/
        /*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        //getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        /*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24-end*/
        return super.onCreateOptionsMenu(menu);
    }

	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
         case R.id.menu_call_settings:
                handleMenuSettings();
                return true;
		 case R.id.delete_some_calls:
		 	  if (DialerFeatureOptions.MULTI_DELETE) {
                    final Intent delIntent = new Intent(this, CallLogMultipleDeleteActivity.class);
                    delIntent.putExtra(CallLogQueryHandler.CALL_LOG_TYPE_FILTER,
                            mCallLogFragment.getCurrentCallTypeFilter());

                    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                    if (null != mCallLogFragment && null != mCallLogFragment.getAdapter()) {
                        mCallLogFragment.getAdapter().resetAllItem();
                    }
                    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
                    startActivity(delIntent);
                }
			    return true;

        	}
        return super.onOptionsItemSelected(item);  
    }  
    /*PRIZE-change-yuandailin-2016-3-28-end*/

    /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-start*/
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (null != mCallLogFragment) {
                    mCallLogFragment.updateCallLogMenuStatus();
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
    /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-end*/

    /**
     * Returns true if the intent is due to hitting the green send key (hardware call button:
     * KEYCODE_CALL) while in a call.
     *
     * @param intent the intent that launched this activity
     * @return true if the intent is due to hitting the green send key while in a call
     */
    private boolean isSendKeyWhileInCall(Intent intent) {
        // If there is a call in progress and the user launched the dialer by hitting the call
        // button, go straight to the in-call screen.
        final boolean callKey = Intent.ACTION_CALL_BUTTON.equals(intent.getAction());

        if (callKey) {
            TelecomUtil.showInCallScreen(this, false);
            return true;
        }

        return false;
    }

    /**
     * Sets the current tab based on the intent's request type
     *
     * @param intent Intent that contains information about which tab should be selected
     */
    private void displayFragment(Intent intent) {
        // If we got here by hitting send and we're in call forward along to the in-call activity
        if (isSendKeyWhileInCall(intent)) {
            //finish();//PRIZE-remove-yuandailin-2016-3-15
            return;
        }

        final boolean showDialpadChooser = phoneIsInUse() && !DialpadFragment.isAddCallMode(intent);
        if (showDialpadChooser || (intent.getData() != null && isDialIntent(intent))) {
            showDialpadFragment(false);
            mDialpadFragment.setStartedFromNewIntent(true);
            if (showDialpadChooser && !mDialpadFragment.isVisible()) {
                mInCallDialpadUp = true;
            } else {
                /// M: Clear the mInCallDialpadUp if phone not in use
                mInCallDialpadUp = false;
            }
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        mStateSaved = false;
        displayFragment(newIntent);
        invalidateOptionsMenu();
        
    }

    /** Returns true if the given intent contains a phone number to populate the dialer with */
    private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || ACTION_TOUCH_DIALER.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && PhoneAccount.SCHEME_TEL.equals(data.getScheme())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shows the search fragment
     */
    private void enterSearchUi(boolean smartDialSearch, String query, boolean animate) {
        if (mStateSaved || getFragmentManager().isDestroyed()) {
            // Weird race condition where fragment is doing work after the activity is destroyed
            // due to talkback being on (b/10209937). Just return since we can't do any
            // constructive here.
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "Entering search UI - smart dial " + smartDialSearch);
        }

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mInDialpadSearch && mSmartDialSearchFragment != null) {
            transaction.remove(mSmartDialSearchFragment);
        } else if (mInRegularSearch && mRegularSearchFragment != null) {
            transaction.remove(mRegularSearchFragment);
        }

        final String tag;
        if (smartDialSearch) {
            tag = TAG_SMARTDIAL_SEARCH_FRAGMENT;
        } else {
            tag = TAG_REGULAR_SEARCH_FRAGMENT;
        }
        mInDialpadSearch = smartDialSearch;
        mInRegularSearch = !smartDialSearch;

        //mFloatingActionButtonController.scaleOut();

        SearchFragment fragment = (SearchFragment) getFragmentManager().findFragmentByTag(tag);
        if (animate) {
            transaction.setCustomAnimations(android.R.animator.fade_in, 0);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        }

        /// M: If switch to a new fragment, it need to set query string to this
        // fragment, otherwise the query result would show nothing. @{
        boolean needToSetQuery = false;
        if (fragment == null) {
            needToSetQuery = true;
            if (smartDialSearch) {
                fragment = new SmartDialSearchFragment();
            } else {
                fragment = ObjectFactory.newRegularSearchFragment();
                /// M: Why only listening touch event for regular search?
                /// Do it at onListFragmentScrollStateChange for all.
//                fragment.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        // Show the FAB when the user touches the lists fragment and the soft
//                        // keyboard is hidden.
//                        hideDialpadFragment(true, false);
//                        showFabInSearchUi();
//                        return false;
//                    }
//                });
            }
            transaction.add(R.id.dialtacts_container, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        // DialtactsActivity will provide the options menu
        fragment.setHasOptionsMenu(false);
        //fragment.setShowEmptyListForNullQuery(true);
        fragment.setShowEmptyListForNullQuery(false);
        if (!smartDialSearch || needToSetQuery) {
            fragment.setQueryString(query, false /* delaySelection */);
        }
        // @}
        transaction.commit();

        /*if (animate) {
            mListsFragment.getView().animate().alpha(0).withLayer();
        }
        mListsFragment.setUserVisibleHint(false);*/
        /*PRIZE-change-yuandailin-2016-3-28-end*/

        if (smartDialSearch) {
            Logger.logScreenView(ScreenEvent.SMART_DIAL_SEARCH, this);
        } else {
            Logger.logScreenView(ScreenEvent.REGULAR_SEARCH, this);
        }
    }

    /**
     * Hides the search fragment
     */
    private void exitSearchUi() {
        // See related bug in enterSearchUI();
        if (getFragmentManager().isDestroyed() || mStateSaved) {
            return;
        }
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*mSearchView.setText(null);

        if (mDialpadFragment != null) {
            mDialpadFragment.clearDialpad();
        }

        setNotInSearchUi();

        // Restore the FAB for the lists fragment.
        if (getFabAlignment() != FloatingActionButtonController.ALIGN_END) {
            mFloatingActionButtonController.setVisible(false);
        }
        mFloatingActionButtonController.scaleIn(FAB_SCALE_IN_DELAY_MS);*/
        //onPageScrolled(mListsFragment.getCurrentTabIndex(), 0 /* offset */, 0 /* pixelOffset */);
        //onPageSelected(mListsFragment.getCurrentTabIndex());

        /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        setNotInSearchUi();
        /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-end*/

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mSmartDialSearchFragment != null) {
            transaction.remove(mSmartDialSearchFragment);
        }
        if (mRegularSearchFragment != null) {
            transaction.remove(mRegularSearchFragment);
        }
        transaction.commit();

        /*mListsFragment.getView().animate().alpha(1).withLayer();

        if (mDialpadFragment == null || !mDialpadFragment.isVisible()) {
            // If the dialpad fragment wasn't previously visible, then send a screen view because
            // we are exiting regular search. Otherwise, the screen view will be sent by
            // {@link #hideDialpadFragment}.
            mListsFragment.sendScreenViewForCurrentPosition();
            mListsFragment.setUserVisibleHint(true);
        }

        mActionBarController.onSearchUiExited();*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
    }

    @Override
    public void onBackPressed() {
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if (null != mCallLogFragment && mCallLogFragment.isVisibleCallLogPopupWindow()) {
            mCallLogFragment.popWindowDismiss();
            return;
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        if (mStateSaved) {
            return;
        }
        /*PRIZE-remove-yuandailin-2016-3-28-start*/		
        /*if (mIsDialpadShown) {
            if (TextUtils.isEmpty(mSearchQuery) ||
                    (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()
                            && mSmartDialSearchFragment.getAdapter().getCount() == 0)) {
                exitSearchUi();
            }
            hideDialpadFragment(true, false);
        } else if (isInSearchUi()) {
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        if (isTaskRoot()) {
            mDialpadFragment.getView().clearAnimation();
            /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
            /*moveTaskToBack(false);*/
            if (!(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode())) {
                moveTaskToBack(false);
            }
            /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
            isOnBackPressHappened = true;/*PRIZE-add-yuandailin-2015-10-14*/
            Log.d(TAG, "onBackPressed, moveTaskToBack~");
        } else {
            super.onBackPressed();
        }
        /*PRIZE-add-power-saving-mode-yuandailin-2015-12-10-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()) {
            /*prize modify Solve flash wallpaper and black screen yueliu 20180426 start*/
            /*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.superpowersave", "com.android.superpowersave.SuperPowerActivity");
            intent.setComponent(cn);
            startActivity(intent);*/
            //DialtactsActivity.this.finish();
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.superpowersave",
                    "com.android.superpowersave.SuperPowerActivity");
            intent.setComponent(cn);
            startActivity(intent);
            /*prize modify Solve flash wallpaper and black screen yueliu 20180426 end*/
        }
        /*PRIZE-add-power-saving-mode-yuandailin-2015-12-10-end*/
    }

    private void maybeEnterSearchUi() {
        if (!isInSearchUi()) {
            enterSearchUi(true /* isSmartDial */, mSearchQuery, false);
        }
    }

    /**
     * @return True if the search UI was exited, false otherwise
     */
    private boolean maybeExitSearchUi() {
        if (isInSearchUi() && TextUtils.isEmpty(mSearchQuery)) {
            exitSearchUi();
            DialerUtils.hideInputMethod(mParentLayout);
            return true;
        }
        return false;
    }
    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /*private void showFabInSearchUi() {
        mFloatingActionButtonController.changeIcon(
                getResources().getDrawable(R.drawable.fab_ic_dial),
                getResources().getString(R.string.action_menu_dialpad_button));*/
        //mFloatingActionButtonController.align(getFabAlignment(), false /* animate */);
        /*mFloatingActionButtonController.scaleIn(FAB_SCALE_IN_DELAY_MS);
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/
    @Override
    public void onDialpadQueryChanged(String query) {
        mDialpadQuery = query;
        if (mSmartDialSearchFragment != null) {
            mSmartDialSearchFragment.setAddToContactNumber(query);
        }
        final String normalizedQuery = SmartDialNameMatcher.normalizeNumber(query,
                /* M: [MTK Dialer Search] use mtk enhance dialpad map */
                DialerFeatureOptions.isDialerSearchEnabled() ?
                        SmartDialNameMatcher.SMART_DIALPAD_MAP
                        : SmartDialNameMatcher.LATIN_SMART_DIAL_MAP);
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*if (!TextUtils.equals(mSearchView.getText(), normalizedQuery)) {
            if (DEBUG) {
                Log.d(TAG, "onDialpadQueryChanged - new query: " + query);
            }
            if (mDialpadFragment == null || !mDialpadFragment.isVisible()) {
                // This callback can happen if the dialpad fragment is recreated because of
                // activity destruction. In that case, don't update the search view because
                // that would bring the user back to the search fragment regardless of the
                // previous state of the application. Instead, just return here and let the
                // fragment manager correctly figure out whatever fragment was last displayed.
                if (!TextUtils.isEmpty(normalizedQuery)) {
                    mPendingSearchViewQuery = normalizedQuery;
                }
                return;
            }
            // M: ALPS03361423 add for "+" is shown behind number
            mSearchView.setText(mBidiFormatter.unicodeWrap(normalizedQuery,
                           TextDirectionHeuristics.FIRSTSTRONG_LTR));
        }*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/
        try {
            if (mDialpadFragment != null && mDialpadFragment.isVisible()) {
                mDialpadFragment.process_quote_emergency_unquote(normalizedQuery);
            }
        } catch (Exception ignored) {
            // Skip any exceptions for this piece of code
        }
    }

    @Override
    public boolean onDialpadSpacerTouchWithEmptyQuery() {
        if (mInDialpadSearch && mSmartDialSearchFragment != null
                && !mSmartDialSearchFragment.isShowingPermissionRequest()) {
            hideDialpadFragment(true /* animate */, true /* clearDialpad */);
            return true;
        }
        return false;
    }

    @Override
    public void onListFragmentScrollStateChange(int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            hideDialpadFragment(true, false);
            DialerUtils.hideInputMethod(mParentLayout);
        }
    }

    @Override
    public void onListFragmentScroll(int firstVisibleItem, int visibleItemCount,
                                     int totalItemCount) {
        // TODO: No-op for now. This should eventually show/hide the actionBar based on
        // interactions with the ListsFragments.
    }

    private boolean phoneIsInUse() {
        return TelecomUtil.isInCall(this);
    }

    private boolean canIntentBeHandled(Intent intent) {
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.size() > 0;
    }

    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /**
     * Called when the user has long-pressed a contact tile to start a drag operation.
     */
    /*@Override
    public void onDragStarted(int x, int y, PhoneFavoriteSquareTileView view) {
        mListsFragment.showRemoveView(true);
    }

    @Override
    public void onDragHovered(int x, int y, PhoneFavoriteSquareTileView view) {
    }*/

    /**
     * Called when the user has released a contact tile after long-pressing it.
     */
    /*@Override
    public void onDragFinished(int x, int y) {
        mListsFragment.showRemoveView(false);
    }

    @Override
    public void onDroppedOnRemove() {}*/

    /**
     * Allows the SpeedDialFragment to attach the drag controller to mRemoveViewContainer
     * once it has been attached to the activity.
     */
    /*@Override
    public void setDragDropController(DragDropController dragController) {
        mDragDropController = dragController;
        mListsFragment.getRemoveView().setDragDropController(dragController);
    }*/

    /**
     * Implemented to satisfy {@link SpeedDialFragment.HostInterface}
     */
    /*@Override
    public void showAllContactsTab() {
        if (mListsFragment != null) {
            mListsFragment.showTab(ListsFragment.TAB_INDEX_ALL_CONTACTS);
        }
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/

    /**
     * Implemented to satisfy {@link CallLogFragment.HostInterface}
     */
    @Override
    public void showDialpad() {
        showDialpadFragment(true);
    }

    @Override
    public void onPickDataUri(Uri dataUri, boolean isVideoCall, int callInitiationType) {
        mClearSearchOnPause = true;
        PhoneNumberInteraction.startInteractionForPhoneCall(
                DialtactsActivity.this, dataUri, isVideoCall, callInitiationType);
    }

    @Override
    public void onPickPhoneNumber(String phoneNumber, boolean isVideoCall, int callInitiationType) {
        if (phoneNumber == null) {
            // Invalid phone number, but let the call go through so that InCallUI can show
            // an error message.
            phoneNumber = "";
        }

        final Intent intent = new CallIntentBuilder(phoneNumber)
                .setIsVideoCall(isVideoCall)
                .setCallInitiationType(callInitiationType)
                .build();

        DialerUtils.startActivityWithErrorToast(this, intent);
        mClearSearchOnPause = true;
    }

    @Override
    public void onShortcutIntentCreated(Intent intent) {
        Log.w(TAG, "Unsupported intent has come (" + intent + "). Ignoring.");
    }

    @Override
    public void onHomeInActionBarSelected() {
        exitSearchUi();
    }

    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /*@Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int tabIndex = mListsFragment.getCurrentTabIndex();

        // Scroll the button from center to end when moving from the Speed Dial to Call History tab.
        // In RTL, scroll when the current tab is Call History instead, since the order of the tabs
        // is reversed and the ViewPager returns the left tab position during scroll.
        boolean isRtl = DialerUtils.isRtl();
        if (!isRtl && tabIndex == ListsFragment.TAB_INDEX_SPEED_DIAL && !mIsLandscape) {
            mFloatingActionButtonController.onPageScrolled(positionOffset);
        } else if (isRtl && tabIndex == ListsFragment.TAB_INDEX_HISTORY && !mIsLandscape) {
            mFloatingActionButtonController.onPageScrolled(1 - positionOffset);
        } else if (tabIndex != ListsFragment.TAB_INDEX_SPEED_DIAL) {
            mFloatingActionButtonController.onPageScrolled(1);
        }
    }

    @Override
    public void onPageSelected(int position) {
        updateMissedCalls();
        int tabIndex = mListsFragment.getCurrentTabIndex();
        mPreviouslySelectedTabIndex = tabIndex;
        /// M: if under search mode, don't change icon to add contact
        if (tabIndex == ListsFragment.TAB_INDEX_ALL_CONTACTS && !isInSearchUi()) {
            mFloatingActionButtonController.changeIcon(
                    getResources().getDrawable(R.drawable.ic_person_add_24dp),
                    getResources().getString(R.string.search_shortcut_create_new_contact));
        } else {
            mFloatingActionButtonController.changeIcon(
                    getResources().getDrawable(R.drawable.fab_ic_dial),
                    getResources().getString(R.string.action_menu_dialpad_button));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/

    @Override
    public boolean isActionBarShowing() {
        /*PRIZE-change-yuandailin-2016-3-28-start*/	
        //return mActionBarController.isActionBarShowing();
        return false;
    }

    /*@Override
    public ActionBarController getActionBarController() {
        return mActionBarController;
    }*/
    /*PRIZE-change-yuandailin-2016-3-28-end*/

    @Override
    public boolean isDialpadShown() {
        return mIsDialpadShown;
    }

    @Override
    public int getDialpadHeight() {
        if (mDialpadFragment != null) {
            return mDialpadFragment.getDialpadHeight();
        }
        return 0;
    }

    @Override
    public int getActionBarHideOffset() {
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
        //return getSupportActionBar().getHideOffset();
        return getActionBar().getHideOffset();
        /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
    }

    @Override
    public void setActionBarHideOffset(int offset) {
        //getSupportActionBar().setHideOffset(offset);//PRIZE-remove-yuandailin-2016-3-28
    }

    @Override
    public int getActionBarHeight() {
        return mActionBarHeight;
    }

    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /*private int getFabAlignment() {
        if (!mIsLandscape && !isInSearchUi() &&
                mListsFragment.getCurrentTabIndex() == ListsFragment.TAB_INDEX_SPEED_DIAL) {
            return FloatingActionButtonController.ALIGN_MIDDLE;
        }
        return FloatingActionButtonController.ALIGN_END;
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/

    private void updateMissedCalls() {
    	Log.d(TAG, "[updateMissedCalls]   mPreviouslySelectedTabIndex = "+mPreviouslySelectedTabIndex);
        if (mPreviouslySelectedTabIndex == ListsFragment.TAB_INDEX_HISTORY) {
            mListsFragment.markMissedCallsAsReadAndRemoveNotifications();
        }
    }

    /**
     * M: Set to clear dialpad and exit search ui while activity on pause
     * @param clearSearch If true clear dialpad and exit search ui while activity on pause
     */
    public void setClearSearchOnPause(boolean clearSearch) {
        mClearSearchOnPause = clearSearch;
    }

    ///: M Fix for ALPS03452619 {
    public void refreshSearchFragment() {
          Log.d(TAG, "Call refreshSearchFragment");
          //updateSearchFragmentPosition();
    }
    /// @}

    /*PRIZE-add-yuandailin-2016-3-21-start*/
    public void hideSearchView() {
        exitSearchUi();
    }

    public void showSearchView(CharSequence s) {
        final String newText = s.toString();
        if (newText.equals(mSearchQuery)) {
            // If the query hasn't changed (perhaps due to activity being destroyed
            // and restored, or user launching the same DIAL intent twice), then there is
            // no need to do anything here.
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "onTextChange for mSearchView called with new query: " + newText);
            Log.d(TAG, "Previous Query: " + mSearchQuery);
        }
        mSearchQuery = newText;

        // Show search fragment only when the query string is changed to non-empty text.
        if (!TextUtils.isEmpty(newText)) {
            // Call enterSearchUi only if we are switching search modes, or showing a search
            // fragment for the first time.
                /*final boolean sameSearchMode = (mIsDialpadShown && mInDialpadSearch) ||
                        (!mIsDialpadShown && mInRegularSearch);*/
            final boolean sameSearchMode = mInDialpadSearch || mInRegularSearch;
            if (!sameSearchMode) {
                //enterSearchUi(mIsDialpadShown, mSearchQuery, true /* animate */);
				// prize-modify-For 65279 - longzhongping-2018.08.03-start 
                //enterSearchUi(true, mSearchQuery, true /* animate */);
				enterSearchUi(true, mSearchQuery, false /* animate */);
				// prize-modify-For 65279 - longzhongping-2018.08.03-end 
            }
        }

        if (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()) {
            mSmartDialSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
        } else if (mRegularSearchFragment != null && mRegularSearchFragment.isVisible()) {
            mRegularSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
        }
    }
    /*PRIZE-add-yuandailin-2016-3-21-end*/

    /*PRIZE-add-huangpengfei-2016-11-25-start*/
    private void clearMissedCalls() {
        Log.d(TAG, "[clearMissedCalls]");
        CallLogNotificationsHelper.removeMissedCallNotifications(this);
    }
    /*PRIZE-add-huangpengfei-2016-11-25-end*/

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    public boolean isDigitsEmpty() {
        if (null != mDialpadFragment) {
            return mDialpadFragment.isDigitsEmpty();
        }
        return true;
    }
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
}
