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

package com.android.dialer.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.Log;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.dialer.R;
import com.android.dialer.list.ListsFragment;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.EmptyLoader;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.widget.EmptyContentView;
import com.android.dialer.widget.EmptyContentView.OnEmptyViewActionButtonClickedListener;
import com.android.dialerbind.ObjectFactory;

import com.mediatek.contacts.util.VvmUtils;
import com.mediatek.dialer.activities.CallLogSearchResultActivity;
import com.mediatek.dialer.calllog.PhoneAccountInfoHelper;
import com.mediatek.dialer.calllog.PhoneAccountInfoHelper.AccountInfoListener;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.DialerConstants;
import com.mediatek.dialer.util.DialerFeatureOptions;

import java.util.List;
/*PRIZE-add-yuandailin-2016-3-15-start*/
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu.OnDismissListener;
import android.provider.CallLog.Calls;
import com.android.dialer.calllog.CallLogQueryHandler;
import android.widget.PopupMenu;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.view.LayoutInflater;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import com.android.dialer.list.OnListFragmentScrolledListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.android.dialer.dialpad.DialpadFragment.OnDialpadQueryChangedListener;
import com.android.dialer.DialtactsActivity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.mediatek.dialer.activities.CallLogMultipleDeleteActivity;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
/*PRIZE-add-yuandailin-2016-3-15-end*/
import android.content.ComponentName;//PRIZE-add-yuandailin-2016-5-11
import android.content.res.Resources;//PRIZE-add -yuandailin-2016-7-21

/**
 * Displays a list of call log entries. To filter for a particular kind of call
 * (all, missed or voicemails), specify it in the constructor.
 */
public class CallLogFragment extends Fragment implements CallLogQueryHandler.Listener,
        CallLogAdapter.CallFetcher, OnEmptyViewActionButtonClickedListener,
        FragmentCompat.OnRequestPermissionsResultCallback, /*M:*/AccountInfoListener {
    private static final String TAG = "CallLogFragment";

    /** M: request full group permissions instead of READ_CALL_LOG,
     * Because MTK changed the group permissions granting logic.
     */
    private static final String[] READ_CALL_LOG = PermissionsUtil.PHONE_FULL_GROUP;

    /**
     * ID of the empty loader to defer other fragments.
     */
    private static final int EMPTY_LOADER_ID = 0;

    private static final String KEY_FILTER_TYPE = "filter_type";
    private static final String KEY_LOG_LIMIT = "log_limit";
    private static final String KEY_DATE_LIMIT = "date_limit";

    private static final String KEY_IS_CALL_LOG_ACTIVITY = "is_call_log_activity";

    // No limit specified for the number of logs to show; use the CallLogQueryHandler's default.
    private static final int NO_LOG_LIMIT = -1;
    // No date-based filtering.
    private static final int NO_DATE_LIMIT = 0;

    private static final int READ_CALL_LOG_PERMISSION_REQUEST_CODE = 1;

    private static final int EVENT_UPDATE_DISPLAY = 1;

    private static final long MILLIS_IN_MINUTE = 60 * 1000;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CallLogAdapter mAdapter;
    private CallLogQueryHandler mCallLogQueryHandler;
    private boolean mScrollToTop;

    private View mEmptyListView;//PRIZE-change-yuandailin-2016-3-28
    private KeyguardManager mKeyguardManager;

    private boolean mEmptyLoaderRunning;
    private boolean mCallLogFetched;
    private boolean mVoicemailStatusFetched;

    /*prize-add-huangliemin-2016-6-8-start*/
    public static boolean isEmpty;
    /*prize-add-huangliemin-2016-6-8-end*/

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    private int mRecyclerViewScrollState = -1;
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-start*/
    private View mRootView;
    private boolean isInflatedEmptyView = false;
    private android.view.ViewStub prize_viewstub_empty_view;
    /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-end*/

    private final Handler mDisplayUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_DISPLAY:
                    refreshData();
                    rescheduleDisplayUpdate();
                    break;
            }
        }
    };

    private final Handler mHandler = new Handler();

    protected class CustomContentObserver extends ContentObserver {
        public CustomContentObserver() {
            super(mHandler);
        }
        @Override
        public void onChange(boolean selfChange) {
            mRefreshDataRequired = true;
        }
    }

    // See issue 6363009
    private final ContentObserver mCallLogObserver = new CustomContentObserver();
    private final ContentObserver mContactsObserver = new CustomContentObserver();
    private boolean mRefreshDataRequired = true;

    private boolean mHasReadCallLogPermission = false;

    // Exactly same variable is in Fragment as a package private.
    private boolean mMenuVisible = true;

    // Default to all calls.
    private int mCallTypeFilter = CallLogQueryHandler.CALL_TYPE_ALL;

    // Log limit - if no limit is specified, then the default in {@link CallLogQueryHandler}
    // will be used.
    private int mLogLimit = NO_LOG_LIMIT;

    // Date limit (in millis since epoch) - when non-zero, only calls which occurred on or after
    // the date filter are included.  If zero, no date-based filtering occurs.
    private long mDateLimit = NO_DATE_LIMIT;

    /// M: [Call Log Account Filter] @{
    private TextView mNoticeText;
    private View mNoticeTextDivider;
    /// @}


    /*
     * True if this instance of the CallLogFragment shown in the CallLogActivity.
     */
    private boolean mIsCallLogActivity = false;

    public interface HostInterface {
        public void showDialpad();
    }

    /*PRIZE-add-yuandailin-2016-3-15-start*/
    private ImageView calllogOptionsMenuButton;
    private RelativeLayout menuLinearlayout;
    private TextView title;
    /*PRIZE-add -yuandailin-2016-7-18-start*/
    private PopupWindow popupWindow = null;
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*private PopupWindow dialerMenuPopupWindow = null;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    /*PRIZE-add -yuandailin-2016-7-18-end*/
    /*PRIZE-add -hpf-2017-10-13-start*/
    TextView mAllCalls;
    TextView mUnreceiveCalls;
    TextView mReceivedCalls;
    TextView mOutgoingCalls;
    boolean mIsDisplayedAnimation = false;
    /*PRIZE-add -hpf-2017-10-13-end*/
    private RelativeLayout dialerMenuButton;
    private static final int MAX_RECENTS_ENTRIES = 20;
    private OnListFragmentScrolledListener mActivityScrollListener;
    private final View.OnClickListener popWindowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_all_calls:
                    mCallTypeFilter = CallLogQueryHandler.CALL_TYPE_ALL;
                    title.setText(R.string.all_calls);
                    updateCallList(CallLogQueryHandler.CALL_TYPE_ALL, MAX_RECENTS_ENTRIES);
                    popWindowDismiss();
                    break;
                case R.id.menu_unreceive_calls:
                    mCallTypeFilter = Calls.MISSED_TYPE;
                    title.setText(R.string.unreceive_calls);
                    updateCallList(Calls.MISSED_TYPE, MAX_RECENTS_ENTRIES);
                    popWindowDismiss();
                    break;
                case R.id.menu_received_calls:
                    mCallTypeFilter = Calls.INCOMING_TYPE;
                    title.setText(R.string.received_calls);
                    updateCallList(Calls.INCOMING_TYPE, MAX_RECENTS_ENTRIES);
                    popWindowDismiss();
                    break;
                case R.id.menu_outgoing_calls:
                    mCallTypeFilter = Calls.OUTGOING_TYPE;
                    title.setText(R.string.outgoing_calls);
                    updateCallList(Calls.OUTGOING_TYPE, MAX_RECENTS_ENTRIES);
                    popWindowDismiss();
                    break;
            }
        }
    };

    private final View.OnClickListener menuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* DialpadFragment.isDigitsEmpty */
            if (null != getActivity() && getActivity() instanceof DialtactsActivity && !((DialtactsActivity) getActivity()).isDigitsEmpty()) {
                return;
            }
            if (popupWindow == null) {
                /*PRIZE-remove-yuandailin-2016-8-2-start*/
                /*WindowManager.LayoutParams params=getActivity().getWindow().getAttributes();
                params.alpha=0.7f;
                getActivity().getWindow().setAttributes(params);*/
                /*PRIZE-remove-yuandailin-2016-8-2-start*/

                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                /*View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.calllog_pop_window, null);
                popupWindow = new PopupWindow(contentView, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, true);*/
                View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.prize_calllog_pop_window, null);
                prize_popwindow_anim_layout = (LinearLayout) contentView.findViewById(R.id.prize_popwindow_anim_layout);
                popupWindow = new PopupWindow(contentView, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, false);
                /* background */
                android.view.animation.AlphaAnimation alphaAnimation = new android.view.animation.AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(300);
                alphaAnimation.setFillAfter(true);
                prize_popwindow_anim_layout.setAnimation(alphaAnimation);
                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/

                popupWindow.setOutsideTouchable(true);
                LinearLayout popwindowLayout = (LinearLayout) contentView.findViewById(R.id.popwindow_layout);
                popwindowLayout.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                    	if(!mIsDisplayedAnimation) return false;//prize-add-hpf-2017-10-13
                        popWindowDismiss();
                        return false;
                    }
                });
                popwindowLayout.setFocusable(true);
                popwindowLayout.setFocusableInTouchMode(true);

                /*PRIZE-add -yuandailin-2016-8-2-start*/
                //calllogOptionsMenuButton.setBackground(getResources().getDrawable(R.drawable.calllog_menu_up));
                RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(300);
                rotateAnimation.setFillAfter(true);
                calllogOptionsMenuButton.startAnimation(rotateAnimation);
                /*PRIZE-add -yuandailin-2016-8-2-end*/
                mAllCalls = (TextView) contentView.findViewById(R.id.menu_all_calls);
                mAllCalls.setOnClickListener(popWindowOnClickListener);
                mUnreceiveCalls = (TextView) contentView.findViewById(R.id.menu_unreceive_calls);
                mUnreceiveCalls.setOnClickListener(popWindowOnClickListener);
                mReceivedCalls = (TextView) contentView.findViewById(R.id.menu_received_calls);
                mReceivedCalls.setOnClickListener(popWindowOnClickListener);
                mOutgoingCalls = (TextView) contentView.findViewById(R.id.menu_outgoing_calls);
                mOutgoingCalls.setOnClickListener(popWindowOnClickListener);

                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                /*popupWindow.showAsDropDown(menuLinearlayout);*/
                Resources resources = getActivity().getResources();
                if (mCallTypeFilter == CallLogQueryHandler.CALL_TYPE_ALL) {
                    mAllCalls.setTextColor(resources.getColor(R.color.prize_dialer_call_log_pop_selected_text_color));
                    setCompoundDrawablesVisible(true, mAllCalls);
                    setCompoundDrawablesVisible(false, mUnreceiveCalls);
                    setCompoundDrawablesVisible(false, mReceivedCalls);
                    setCompoundDrawablesVisible(false, mOutgoingCalls);
                } else if (mCallTypeFilter == Calls.MISSED_TYPE) {
                    mUnreceiveCalls.setTextColor(resources.getColor(R.color.prize_dialer_call_log_pop_selected_text_color));
                    setCompoundDrawablesVisible(false, mAllCalls);
                    setCompoundDrawablesVisible(true, mUnreceiveCalls);
                    setCompoundDrawablesVisible(false, mReceivedCalls);
                    setCompoundDrawablesVisible(false, mOutgoingCalls);
                } else if (mCallTypeFilter == Calls.INCOMING_TYPE) {
                    mReceivedCalls.setTextColor(resources.getColor(R.color.prize_dialer_call_log_pop_selected_text_color));
                    setCompoundDrawablesVisible(false, mAllCalls);
                    setCompoundDrawablesVisible(false, mUnreceiveCalls);
                    setCompoundDrawablesVisible(true, mReceivedCalls);
                    setCompoundDrawablesVisible(false, mOutgoingCalls);
                } else if (mCallTypeFilter == Calls.OUTGOING_TYPE) {
                    mOutgoingCalls.setTextColor(resources.getColor(R.color.prize_dialer_call_log_pop_selected_text_color));
                    setCompoundDrawablesVisible(false, mAllCalls);
                    setCompoundDrawablesVisible(false, mUnreceiveCalls);
                    setCompoundDrawablesVisible(false, mReceivedCalls);
                    setCompoundDrawablesVisible(true, mOutgoingCalls);
                }
                popupWindow.showAtLocation(menuLinearlayout, Gravity.TOP, 0, 0);
                mIsDisplayedAnimation = true;//prize-add-hpf-2017-10-13
                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
            } else {
                //popWindowDismiss();
            }
        }
    };

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    private LinearLayout prize_popwindow_anim_layout;
    private void setCompoundDrawablesVisible(boolean isVisible, TextView tv) {
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(R.drawable.prize_calllog_pop_selected);
        if (isVisible) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        tv.setCompoundDrawables(null, null, drawable, null);
    }

    public boolean isVisibleCallLogPopupWindow() {
        if (null != popupWindow) {
            return popupWindow.isShowing();
        }
        return false;
    }
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    /*PRIZE-change-yuandailin-2016-8-24-start*/
    /*private final View.OnClickListener dialerMenupopWindowOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.delete_some_calls:
                    if (DialerFeatureOptions.MULTI_DELETE) {
                        dialerMenuPopWindowDismiss();
                        final Intent delIntent = new Intent(getActivity(), CallLogMultipleDeleteActivity.class);
                        delIntent.putExtra(CallLogQueryHandler.CALL_LOG_TYPE_FILTER, mCallTypeFilter);
                        getActivity().startActivity(delIntent);
                    }
                    break;
                case R.id.menu_call_settings:
                    dialerMenuPopWindowDismiss();
                    ((DialtactsActivity) getActivity()).handleMenuSettings();
                    break;
            }
        }
    };*/

    /*private final View.OnClickListener dialerMenuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dialerMenuPopupWindow == null) {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 0.7f;
                getActivity().getWindow().setAttributes(params);
                View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialer_menu_pop_window, null);
                dialerMenuPopupWindow = new PopupWindow(contentView, 200, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, true);
                dialerMenuPopupWindow.setOutsideTouchable(true);
                dialerMenuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                        params.alpha = 1f;
                        getActivity().getWindow().setAttributes(params);
                        dialerMenuPopupWindow = null;//PRIZE-add -yuandailin-2016-7-22
                    }
                });
                LinearLayout dialerMenuPopwindowLayout = (LinearLayout) contentView.findViewById(R.id.dialer_menu_popwindow_layout);
                dialerMenuPopwindowLayout.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        dialerMenuPopWindowDismiss();
                        return false;
                    }
                });
                dialerMenuPopwindowLayout.setFocusable(true);
                dialerMenuPopwindowLayout.setFocusableInTouchMode(true);
                dialerMenuPopwindowLayout.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                dialerMenuPopWindowDismiss();
                                return true;
                        }
                        return false;
                    }
                });
                TextView deleteSomeCalls = (TextView) contentView.findViewById(R.id.delete_some_calls);
                deleteSomeCalls.setOnClickListener(dialerMenupopWindowOnClickListener);
                TextView menuCallsSeetings = (TextView) contentView.findViewById(R.id.menu_call_settings);
                menuCallsSeetings.setOnClickListener(dialerMenupopWindowOnClickListener);
                dialerMenuPopupWindow.showAsDropDown(dialerMenuButton);
            } else {
                dialerMenuPopWindowDismiss();
            }
        }
    };*/
    /*PRIZE-add -yuandailin-2016-7-18-end*/

    private PopupMenu dialerMenu = null;
    private final PopupMenu.OnMenuItemClickListener dialerMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_call_settings:
                    ((DialtactsActivity) getActivity()).handleMenuSettings();
                    return true;
                case R.id.delete_some_calls:
                    if (DialerFeatureOptions.MULTI_DELETE) {
                        final Intent delIntent = new Intent(getActivity(), CallLogMultipleDeleteActivity.class);
                        delIntent.putExtra(CallLogQueryHandler.CALL_LOG_TYPE_FILTER, mCallTypeFilter);

                        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                        if (null != mAdapter) {
                            mAdapter.resetAllItem();
                        }
                        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
                        getActivity().startActivity(delIntent);
                    }
                    return true;

            }
            return false;
        }
    };

    private final View.OnClickListener dialerMenuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
            /*if (dialerMenu == null) {
                dialerMenu = new PopupMenu(getActivity(), dialerMenuButton);
                dialerMenu.getMenuInflater().inflate(R.menu.actionbar_menu, dialerMenu.getMenu());
                MenuItem deleteItem = dialerMenu.getMenu().findItem(R.id.delete_some_calls);
                if (isEmpty) {
                    deleteItem.setVisible(false);
                } else {
                    deleteItem.setVisible(true);
                }
                dialerMenu.setOnMenuItemClickListener(dialerMenuItemClickListener);
                dialerMenu.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        dialerMenu = null;
                    }
                });
                dialerMenu.show();
            } else if (dialerMenu != null) {
                dialerMenu.dismiss();
                dialerMenu = null;
            }*/
            updateCallLogMenuStatus();
            /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
        }
    };
    /*PRIZE-change-yuandailin-2016-8-24-end*/

    /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-start*/
    public void updateCallLogMenuStatus() {
        if (dialerMenu == null) {
            dialerMenu = new PopupMenu(getActivity(), dialerMenuButton);
            dialerMenu.getMenuInflater().inflate(R.menu.actionbar_menu, dialerMenu.getMenu());
            MenuItem deleteItem = dialerMenu.getMenu().findItem(R.id.delete_some_calls);
            if (isEmpty) {
                deleteItem.setVisible(false);
            } else {
                deleteItem.setVisible(true);
            }
            dialerMenu.setOnMenuItemClickListener(dialerMenuItemClickListener);
            dialerMenu.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    dialerMenu = null;
                }
            });
            dialerMenu.show();
        } else if (dialerMenu != null) {
            dialerMenu.dismiss();
            dialerMenu = null;
        }
    }
    /*PRIZE-Add-PrizeInDialer_N-wangzhong-2016_10_24-end*/

    public CallLogFragment() {
        this(CallLogQueryHandler.CALL_TYPE_ALL, NO_LOG_LIMIT);
    }

    public CallLogFragment(int filterType) {
        this(filterType, NO_LOG_LIMIT);
    }

    public CallLogFragment(int filterType, boolean isCallLogActivity) {
        this(filterType, NO_LOG_LIMIT);
        mIsCallLogActivity = isCallLogActivity;
    }

    public CallLogFragment(int filterType, int logLimit) {
        this(filterType, logLimit, NO_DATE_LIMIT);
    }

    /**
     * Creates a call log fragment, filtering to include only calls of the desired type, occurring
     * after the specified date.
     * @param filterType type of calls to include.
     * @param dateLimit limits results to calls occurring on or after the specified date.
     */
    public CallLogFragment(int filterType, long dateLimit) {
        this(filterType, NO_LOG_LIMIT, dateLimit);
    }

    public CallLogFragment(int filterType, int logLimit, long dateLimit) {
        mCallTypeFilter = filterType;
        mLogLimit = logLimit;
        mDateLimit = dateLimit;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            mCallTypeFilter = state.getInt(KEY_FILTER_TYPE, mCallTypeFilter);
            mLogLimit = state.getInt(KEY_LOG_LIMIT, mLogLimit);
            mDateLimit = state.getLong(KEY_DATE_LIMIT, mDateLimit);

            /// M: [Call Log Account Filter]
            mNeedAccountFilter = state.getBoolean(KEY_NEED_ACCOUNT_FILTER);

            mIsCallLogActivity = state.getBoolean(KEY_IS_CALL_LOG_ACTIVITY, mIsCallLogActivity);
        }

        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
        String currentCountryIso = GeoUtil.getCurrentCountryIso(activity);
        mCallLogQueryHandler = new CallLogQueryHandler(activity, resolver, this, mLogLimit);
        mKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        resolver.registerContentObserver(CallLog.CONTENT_URI, true, mCallLogObserver);
        resolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                mContactsObserver);
        setHasOptionsMenu(true);

        /// M: [Call Log Account Filter] add account change listener
        PhoneAccountInfoHelper.getInstance(getActivity()).registerForAccountChange(this);
        /// @}
    }

    /** Called by the CallLogQueryHandler when the list of calls has been fetched or updated. */
    @Override
    public boolean onCallsFetched(Cursor cursor) {
        if (getActivity() == null || getActivity().isFinishing()) {
            // Return false; we did not take ownership of the cursor
            return false;
        }
        mAdapter.invalidatePositions();
        mAdapter.setLoading(false);
        mAdapter.changeCursor(cursor);
        // This will update the state of the "Clear call log" menu item.
        getActivity().invalidateOptionsMenu();

        boolean showListView = cursor != null && cursor.getCount() > 0;
        isEmpty = !showListView;//prize-add-huangliemin-2016-6-8
        mRecyclerView.setVisibility(showListView ? View.VISIBLE : View.GONE);
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mEmptyListView.setVisibility(!showListView ? View.VISIBLE : View.GONE);*/
        if (!showListView) {
            if (null != prize_viewstub_empty_view && !isInflatedEmptyView) {
                prize_viewstub_empty_view.inflate();
                mEmptyListView =  mRootView.findViewById(R.id.empty_list_view);
            }
            mEmptyListView.setVisibility(View.VISIBLE);
            if (null != mEmptyListView) DialerUtils.configureCallEmptyListView(mEmptyListView, getResources());
        } else {
            if (null != mEmptyListView) {
                mEmptyListView.setVisibility(View.GONE);
            }
        }
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/

        if (mScrollToTop) {
            // The smooth-scroll animation happens over a fixed time period.
            // As a result, if it scrolls through a large portion of the list,
            // each frame will jump so far from the previous one that the user
            // will not experience the illusion of downward motion.  Instead,
            // if we're not already near the top of the list, we instantly jump
            // near the top, and animate from there.
            if (mLayoutManager.findFirstVisibleItemPosition() > 5) {
                // TODO: Jump to near the top, then begin smooth scroll.
                mRecyclerView.smoothScrollToPosition(0);
            }
            // Workaround for framework issue: the smooth-scroll doesn't
            // occur if setSelection() is called immediately before.
            mHandler.post(new Runnable() {
               @Override
               public void run() {
                   if (getActivity() == null || getActivity().isFinishing()) {
                       return;
                   }
                   mRecyclerView.smoothScrollToPosition(0);
               }
            });

            mScrollToTop = false;
        }
        mCallLogFetched = true;
        destroyEmptyLoaderIfAllDataFetched();

        /** M:  [Dialer Global Search] notify search activity update search result. @{*/
        updateSearchResultIfNeed(cursor);
        /** @}*/
        return true;
    }

    /**
     * Called by {@link CallLogQueryHandler} after a successful query to voicemail status provider.
     */
    @Override
    public void onVoicemailStatusFetched(Cursor statusCursor) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        mVoicemailStatusFetched = true;
        destroyEmptyLoaderIfAllDataFetched();
    }

    private void destroyEmptyLoaderIfAllDataFetched() {
        if (mCallLogFetched && mVoicemailStatusFetched && mEmptyLoaderRunning) {
            mEmptyLoaderRunning = false;
            getLoaderManager().destroyLoader(EMPTY_LOADER_ID);
        }
    }

    @Override
    public void onVoicemailUnreadCountFetched(Cursor cursor) {}

    @Override
    public void onMissedCallsUnreadCountFetched(Cursor cursor) {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*View view = inflater.inflate(R.layout.call_log_fragment, container, false);*/
        View view = inflater.inflate(R.layout.prize_call_log_fragment, container, false);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        setupView(view, null);
        return view;
    }

    protected void setupView(
            View view, @Nullable VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-start*/
        mRootView = view;
        prize_viewstub_empty_view = (android.view.ViewStub) view.findViewById(R.id.prize_viewstub_empty_view);
        prize_viewstub_empty_view.setOnInflateListener(new android.view.ViewStub.OnInflateListener() {

            @Override
            public void onInflate(android.view.ViewStub stub, View inflated) {
                isInflatedEmptyView = true;
            }
        });
        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /** M: [Call Log Account Filter] add Notice for account filter @{ */
        mNoticeText = (TextView) view.findViewById(R.id.notice_text);
        mNoticeTextDivider = view.findViewById(R.id.notice_text_divider);
        /** @} */

        /*PRIZE-add-yuandailin-2016-3-15-start*/
        menuLinearlayout = (RelativeLayout) view.findViewById(R.id.menu_linearlayout);
        menuLinearlayout.setOnClickListener(menuOnClickListener);
        calllogOptionsMenuButton = (ImageView) view.findViewById(R.id.calllog_options_menu_button);
        title = (TextView) view.findViewById(R.id.title);
        dialerMenuButton = (RelativeLayout) view.findViewById(R.id.prize_dialer_menu);
        dialerMenuButton.setOnClickListener(dialerMenuOnClickListener);
        /*PRIZE-add-yuandailin-2016-3-15-end*/
        
        /*prize-add- for LiuHai screen-hpf-2018-4-20-start*/
        boolean hasSystemFeature = getActivity().getPackageManager().hasSystemFeature("com.prize.notch.screen");
        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams)title.getLayoutParams();
        if(hasSystemFeature){
            rp.addRule(RelativeLayout.CENTER_VERTICAL);
        }else{
        	rp.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        /*prize-add- for LiuHai screen-hpf-2018-4-20-end*/

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        /*PRIZE-add-yuandailin-2016-4-13-start*/
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                mRecyclerViewScrollState = scrollState;
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

                mActivityScrollListener.onListFragmentScrollStateChange(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        /*PRIZE-add-yuandailin-2016-4-13-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mEmptyListView = view.findViewById(R.id.empty_list_view);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*mEmptyListView.setImage(R.drawable.empty_call_log);
        mEmptyListView.setActionClickedListener(this);*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/

        int activityType = mIsCallLogActivity ? CallLogAdapter.ACTIVITY_TYPE_CALL_LOG :
                CallLogAdapter.ACTIVITY_TYPE_DIALTACTS;
        String currentCountryIso = GeoUtil.getCurrentCountryIso(getActivity());
        mAdapter = ObjectFactory.newCallLogAdapter(
                        getActivity(),
                        this,
                        new ContactInfoHelper(getActivity(), currentCountryIso),
                        voicemailPlaybackPresenter,
                        activityType);
        mRecyclerView.setAdapter(mAdapter);
        /// M: listening recyclerview scroll state
        mRecyclerView.addOnScrollListener(new ViewScrollListener());
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        mRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (mRecyclerViewScrollState == RecyclerView.SCROLL_STATE_IDLE
                        || mRecyclerViewScrollState == -1) {
                    mAdapter.resetAllItem();
                }
                return false;
            }
        });
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        fetchCalls();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateEmptyMessage(mCallTypeFilter);
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    /*PRIZE-add-yuandailin-2016-4-13-start*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivityScrollListener = (OnListFragmentScrolledListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnListFragmentScrolledListener");
        }
    }
    /*PRIZE-add-yuandailin-2016-4-13-end*/

    @Override
    public void onStart() {
        // Start the empty loader now to defer other fragments.  We destroy it when both calllog
        // and the voicemail status are fetched.
        getLoaderManager().initLoader(EMPTY_LOADER_ID, null,
                new EmptyLoader.Callback(getActivity()));
        mEmptyLoaderRunning = true;
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean hasReadCallLogPermission =
                PermissionsUtil.hasPermission(getActivity(), READ_CALL_LOG);
        if (!mHasReadCallLogPermission && hasReadCallLogPermission) {
            // We didn't have the permission before, and now we do. Force a refresh of the call log.
            // Note that this code path always happens on a fresh start, but mRefreshDataRequired
            // is already true in that case anyway.
            mRefreshDataRequired = true;
            updateEmptyMessage(mCallTypeFilter);
        }

        mHasReadCallLogPermission = hasReadCallLogPermission;
        refreshData();
        mAdapter.onResume();

        rescheduleDisplayUpdate();
        /*PRIZE-change-yuandailin-2016-9-21-start*/
        int simCount = ((DialtactsActivity) getActivity()).getSimCountFromDialtactsActivity();
        mAdapter.setSimCount(simCount);
        /*PRIZE-change-yuandailin-2016-9-21-end*/
    }

    @Override
    public void onPause() {
        cancelDisplayUpdate();
        mAdapter.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        updateOnTransition();
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if (null != mAdapter) {
            mAdapter.resetAllItem();
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        /*prize-add fix bug[37952]-hpf-2017-10-13-start*/
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindow = null;
        }
        /*prize-add fix bug[37952]-hpf-2017-10-13-end*/
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mAdapter.changeCursor(null);

        getActivity().getContentResolver().unregisterContentObserver(mCallLogObserver);
        getActivity().getContentResolver().unregisterContentObserver(mContactsObserver);

        /// M: [Call Log Account Filter] unregister account change listener
        PhoneAccountInfoHelper.getInstance(getActivity()).unRegisterForAccountChange(this);

        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-start*/
        if (null != mAdapter) {
            mAdapter.closeCallMarkDB();
        }
        /*PRIZE-Add-Optimize_Dialer-wangzhong-2018_3_5-end*/
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FILTER_TYPE, mCallTypeFilter);
        outState.putInt(KEY_LOG_LIMIT, mLogLimit);
        outState.putLong(KEY_DATE_LIMIT, mDateLimit);
        outState.putBoolean(KEY_IS_CALL_LOG_ACTIVITY, mIsCallLogActivity);

        /// M: [Call Log Account Filter]
        outState.putBoolean(KEY_NEED_ACCOUNT_FILTER, mNeedAccountFilter);

        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void fetchCalls() {
        /// M: Do nothing while view scrolling to improve scroll performance
        if (mIsScrolling) {
            return;
        }
        /** M: [Dialer Global Search] Displays a list of call log entries @{ */
        if (isQueryMode()) {
            startSearchCalls(mQueryData);
        } else {
        /** @} */
            /// M: [Call Log Account Filter] add call log account filter support @{
            mCallLogQueryHandler.fetchCalls(mCallTypeFilter, mDateLimit, getAccountFilterId());
            /// @}
            if (!mIsCallLogActivity) {
                /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*/
                /*((ListsFragment) getParentFragment()).updateTabUnreadCounts();*/
                ListsFragment parentFragment = (ListsFragment) getParentFragment();
                if (null != parentFragment) parentFragment.updateTabUnreadCounts();
                /*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*/
            }
        }
    }

    /*PRIZE-add-yuandailin-2016-3-19-start*/
    private void updateCallList(final int filterType, final long dateLimit) {
        //It is delayed for bug[38001] -hpf-2017-10-13
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mAdapter.setCurrentFilter(filterType);
				mCallLogQueryHandler.fetchCalls(filterType, dateLimit, getAccountFilterId());
				
			}
		}, 200);
    }

    public void popWindowDismiss() {
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
                //calllogOptionsMenuButton.setBackground(getResources().getDrawable(R.drawable.calllog_menu));
                *//*PRIZE-remove-yuandailin-2016-8-2-start*//*
                *//*WindowManager.LayoutParams params=getActivity().getWindow().getAttributes();
                params.alpha=1f;
                getActivity().getWindow().setAttributes(params);*//*
                *//*PRIZE-remove-yuandailin-2016-8-2-end*//*
            }
            popupWindow = null;
        }*/
        if (null != popupWindow && popupWindow.isShowing()) {
        	/*prize-add-hpf-2017-10-13-start*/
        	mIsDisplayedAnimation = false;
        	mAllCalls.setOnClickListener(null);
            mUnreceiveCalls.setOnClickListener(null);
            mReceivedCalls.setOnClickListener(null);
            mOutgoingCalls.setOnClickListener(null);
            /*prize-add-hpf-2017-10-13-end*/
            prize_popwindow_anim_layout.clearAnimation();
            Animation animationPopOut = android.view.animation.AnimationUtils.loadAnimation(getActivity(), R.anim.prize_calllog_popwindow_anim_out);
            final android.view.animation.LayoutAnimationController controller = new android.view.animation.LayoutAnimationController(animationPopOut);
            controller.setOrder(android.view.animation.LayoutAnimationController.ORDER_REVERSE);
            animationPopOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (popupWindow != null) {
                        if (popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                        popupWindow = null;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            prize_popwindow_anim_layout.setLayoutAnimation(controller);
            prize_popwindow_anim_layout.startLayoutAnimation();
            /* arrow */
            RotateAnimation rotateAnimation = new RotateAnimation(180.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            calllogOptionsMenuButton.startAnimation(rotateAnimation);
            /* background */
            android.view.animation.AlphaAnimation alphaAnimation = new android.view.animation.AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(300);
            alphaAnimation.setFillAfter(true);
            prize_popwindow_anim_layout.setAnimation(alphaAnimation);
        }
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
    }
    /*PRIZE-add-yuandailin-2016-3-19-end*/

    private void updateEmptyMessage(int filterType) {
        final Context context = getActivity();
        if (context == null) {
            return;
        }

        /*PRIZE-remove-yuandailin-2016-3-28-start*/
        /*if (!PermissionsUtil.hasPermission(context, READ_CALL_LOG)) {
            mEmptyListView.setDescription(R.string.permission_no_calllog);
            mEmptyListView.setActionLabel(R.string.permission_single_turn_on);
            return;
        }*/
        /*PRIZE-remove-yuandailin-2016-3-28-end*/

        final int messageId;
        switch (filterType) {
            case Calls.MISSED_TYPE:
                messageId = R.string.call_log_missed_empty;
                break;
            case Calls.VOICEMAIL_TYPE:
                messageId = R.string.call_log_voicemail_empty;
                break;
            case CallLogQueryHandler.CALL_TYPE_ALL:
                /** M: [Dialer Global Search] Search mode with customer empty string. */
                messageId = isQueryMode() ? R.string.noMatchingCalllogs
                            : R.string.call_log_all_empty;
                /** @} */
                break;
            /** M: [CallLog Incoming and Outgoing Filter] @{ */
            case Calls.INCOMING_TYPE:
                messageId = R.string.call_log_all_empty;
                break;
            case Calls.OUTGOING_TYPE:
                messageId = R.string.call_log_all_empty;
                break;
            /** @} */
            default:
                throw new IllegalArgumentException("Unexpected filter type in CallLogFragment: "
                        + filterType);
        }
        /*PRIZE-change-yuandailin-2016-3-28-start*/
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*DialerUtils.configureCallEmptyListView(mEmptyListView, getResources());*/
        if (null != mEmptyListView) DialerUtils.configureCallEmptyListView(mEmptyListView, getResources());
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /*mEmptyListView.setDescription(messageId);
        if (mIsCallLogActivity) {
            mEmptyListView.setActionLabel(EmptyContentView.NO_LABEL);
        } else if (filterType == CallLogQueryHandler.CALL_TYPE_ALL) {
            mEmptyListView.setActionLabel(R.string.call_log_all_empty_action);
        }*/
        /*PRIZE-change-yuandailin-2016-3-28-end*/
    }

    public CallLogAdapter getAdapter() {//PRIZE-change-yuandailin-2016-3-15
        return mAdapter;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (mMenuVisible != menuVisible) {
            mMenuVisible = menuVisible;
            if (!menuVisible) {
                updateOnTransition();
            } else if (isResumed()) {
                refreshData();
            }
        }
    }

    /** Requests updates to the data to be shown. */
    private void refreshData() {
        // Prevent unnecessary refresh.
        if (mRefreshDataRequired) {
            /// M: Do nothing while view scrolling to improve scroll performance
            if (mIsScrolling) {
                return;
            }
            // Mark all entries in the contact info cache as out of date, so they will be looked up
            // again once being shown.
            mAdapter.invalidateCache();
            mAdapter.setLoading(true);

            fetchCalls();
            mCallLogQueryHandler.fetchVoicemailStatus();
            mCallLogQueryHandler.fetchMissedCallsUnreadCount();
            updateOnTransition();
            mRefreshDataRequired = false;
        } else {
            // Refresh the display of the existing data to update the timestamp text descriptions.
            mAdapter.notifyDataSetChanged();
        }

        /** M: [Call Log Account Filter] @{ */
        if (mNeedAccountFilter) {
            updateNotice();
        }
        /** @} */
    }

    /**
     * Updates the voicemail notification state.
     *
     * TODO: Move to CallLogActivity
     */
    private void updateOnTransition() {
        // We don't want to update any call data when keyguard is on because the user has likely not
        // seen the new calls yet.
        // This might be called before onCreate() and thus we need to check null explicitly.
        if (mKeyguardManager != null && !mKeyguardManager.inKeyguardRestrictedInputMode()
                && mCallTypeFilter == Calls.VOICEMAIL_TYPE) {
            CallLogNotificationsHelper.updateVoicemailNotifications(getActivity());
        }
    }

    @Override
    public void onEmptyViewActionButtonClicked() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (!PermissionsUtil.hasPermission(activity, READ_CALL_LOG)) {
          FragmentCompat.requestPermissions(this, /*M:*/READ_CALL_LOG,
              READ_CALL_LOG_PERMISSION_REQUEST_CODE);
        } else if (!mIsCallLogActivity) {
            // Show dialpad if we are not in the call log activity.
            ((HostInterface) activity).showDialpad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == READ_CALL_LOG_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Force a refresh of the data since we were missing the permission before this.
                mRefreshDataRequired = true;
            }
        }
    }

    /**
     * Schedules an update to the relative call times (X mins ago).
     */
    private void rescheduleDisplayUpdate() {
        if (!mDisplayUpdateHandler.hasMessages(EVENT_UPDATE_DISPLAY)) {
            long time = System.currentTimeMillis();
            // This value allows us to change the display relatively close to when the time changes
            // from one minute to the next.
            long millisUtilNextMinute = MILLIS_IN_MINUTE - (time % MILLIS_IN_MINUTE);
            mDisplayUpdateHandler.sendEmptyMessageDelayed(
                    EVENT_UPDATE_DISPLAY, millisUtilNextMinute);
        }
    }

    /**
     * Cancels any pending update requests to update the relative call times (X mins ago).
     */
    private void cancelDisplayUpdate() {
        mDisplayUpdateHandler.removeMessages(EVENT_UPDATE_DISPLAY);
    }

    /// M: [Multi-Delete] For CallLog delete @{
    @Override
    public void onCallsDeleted() {
        // Do nothing
    }
    /// @}

    /// M: [Call Log Account Filter] @{
    private static final String KEY_NEED_ACCOUNT_FILTER = "need_account_filter";
    // Whether or not to use account filter, currently call log screen use account filter
    // while recents call log  need not
    private boolean mNeedAccountFilter = DialerFeatureOptions.isCallLogAccountFilterEnabled();

    public void setAccountFilterState(boolean enable) {
        mNeedAccountFilter = enable;
    }

    private String getAccountFilterId() {
        if (DialerFeatureOptions.isCallLogAccountFilterEnabled() && mNeedAccountFilter) {
            return PhoneAccountInfoHelper.getInstance(getActivity()).getPreferAccountId();
        } else {
            return PhoneAccountInfoHelper.FILTER_ALL_ACCOUNT_ID;
        }
    }

    private void updateNotice() {
        String lable = null;
        String id = PhoneAccountInfoHelper.getInstance(getActivity()).getPreferAccountId();
        if (getActivity() != null && !PhoneAccountInfoHelper.FILTER_ALL_ACCOUNT_ID.equals(id)) {
            PhoneAccountHandle account = PhoneAccountUtils.getPhoneAccountById(getActivity(), id);
            if (account != null) {
                lable = PhoneAccountUtils.getAccountLabel(getActivity(), account);
            }
        }
        if (!TextUtils.isEmpty(lable) && mNoticeText != null && mNoticeTextDivider != null) {
            mNoticeText.setText(getActivity().getString(R.string.call_log_via_sim_name_notice,
                    lable));
            mNoticeText.setVisibility(View.VISIBLE);
            mNoticeTextDivider.setVisibility(View.VISIBLE);
        } else {
            mNoticeText.setVisibility(View.GONE);
            mNoticeTextDivider.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAccountInfoUpdate() {
        // clear account cache, and refresh list items
        mAdapter.pauseCache();
        forceToRefreshData();
    }

    @Override
    public void onPreferAccountChanged(String id) {
        forceToRefreshData();
    }
    /// @}

    /**
     * M : force refresh calllog data
     */
    public void forceToRefreshData() {
        mRefreshDataRequired = true;
        /// M: for ALPS01683374
        // refreshData only when CallLogFragment is in foreground
        if (isResumed()) {
            refreshData();
            // refreshData would cause ContactInfoCache.invalidate
            // and cache thread starting would be stopped seldom.
            // we have to call adapter onResume again to start cache thread.
            mAdapter.onResume();
        }
    }

    /**
     * M: [Dialer Global Search] Displays a list of call log entries.
     * CallLogSearch activity reused CallLogFragment.  @{
     */
    // Default null, while in search mode it is not null.
    private String mQueryData = null;

    /**
     * Use it to inject search data.
     * This is the entrance of call log search mode.
     * @param query
     */
    public void setQueryData(String query) {
        mQueryData = query;
        mAdapter.setQueryString(query);
    }

    private void startSearchCalls(String query) {
        Uri uri = Uri.withAppendedPath(DialerConstants.CALLLOG_SEARCH_URI_BASE, query);
        /// support search Voicemail calllog
        uri = VvmUtils.buildVvmAllowedUri(uri);

        mCallLogQueryHandler.fetchSearchCalls(uri);
    }

    private boolean isQueryMode() {
        return !TextUtils.isEmpty(mQueryData) && DialerFeatureOptions.DIALER_GLOBAL_SEARCH;
    }

    private void updateSearchResultIfNeed(Cursor result) {
        if (isQueryMode() && getActivity() instanceof CallLogSearchResultActivity) {
            int count = result != null ? result.getCount() : 0;
            ((CallLogSearchResultActivity) getActivity()).updateSearchResult(count);
        }
    }

    public int getItemCount() {
        return mAdapter.getItemCount();
    }
    /** @} */


    /** M: To improve scroll performance, add scroll listener to ignore fetching calls while
     *  scroll view @{*/
    private boolean mIsScrolling = false;
    private class ViewScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                mIsScrolling = true;
            } else {
                mIsScrolling = false;
                if (mRefreshDataRequired) {
                    refreshData();
                    mAdapter.onResume();
                    Log.d(TAG, " scroll state changed to idle, refresh data");
                }
            }
        }
    }
    /** @}*/

    /*PRIZE-add-yuandailin-2015-10-16-start*/
    /**
     * @return void
     * @see com/android/dialer/calllog/CallLogFragment#getCurrentCallTypeFilter()
     */
    public void setCurrentCallTypeFilterInAll() {
        mCallTypeFilter = CallLogQueryHandler.CALL_TYPE_ALL;
        title.setText(R.string.all_calls);
        /*PRIZE-add-yuandailin-2015-12-2-start*/
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateCallList(CallLogQueryHandler.CALL_TYPE_ALL, MAX_RECENTS_ENTRIES);
            }
        });
        /*PRIZE-add-yuandailin-2015-12-2-end*/
    }

    /**
     * @return int
     * @see com/android/dialer/calllog/CallLogFragment#getCurrentCallTypeFilter()
     */
    public int getCurrentCallTypeFilter() {
        return mCallTypeFilter;
    }
    /*PRIZE-add-yuandailin-2015-10-16-end*/
}
