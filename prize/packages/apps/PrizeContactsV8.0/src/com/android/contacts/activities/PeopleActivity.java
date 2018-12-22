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

package com.android.contacts.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.BlockedNumberContract;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.QuickContact;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
//import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.compat.TelecomManagerUtil;
import com.android.contacts.common.dialog.ClearFrequentsDialog;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.list.ContactEntryListFragment;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.ContactTileAdapter.DisplayType;
import com.android.contacts.common.list.DirectoryListLoader;
import com.android.contacts.common.list.ViewPagerTabs;
import com.android.contacts.common.logging.Logger;
import com.android.contacts.common.logging.ScreenEvent.ScreenType;
import com.android.contacts.common.preference.ContactsPreferenceActivity;
import com.android.contacts.common.util.AccountFilterUtil;
import com.android.contacts.common.util.Constants;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.ViewUtil;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.interactions.ContactMultiDeletionInteraction;
import com.android.contacts.interactions.ContactMultiDeletionInteraction.MultiContactDeleteListener;
import com.android.contacts.interactions.JoinContactsDialogFragment;
import com.android.contacts.interactions.JoinContactsDialogFragment.JoinContactsListener;
import com.android.contacts.list.ContactTileListFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.list.MultiSelectContactsListFragment;
import com.android.contacts.list.MultiSelectContactsListFragment.OnCheckBoxListActionListener;
import com.android.contacts.list.OnContactBrowserActionListener;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusWatcher;
import com.android.contacts.list.ProviderStatusWatcher.ProviderStatusListener;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contactsbind.HelpUtils;
import com.android.contacts.util.PhoneCapabilityTester;

import com.mediatek.common.prizeoption.PrizeOption;
import com.mediatek.contacts.ContactsApplicationEx;
import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.model.AccountTypeManagerEx;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.util.PDebug;
import com.mediatek.contacts.util.VolteUtils;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.activities.GroupBrowseActivity;
import com.mediatek.contacts.activities.ActivitiesUtils;

import com.mediatek.contacts.list.DropMenu;
import com.mediatek.contacts.list.DropMenu.DropDownMenu;
import com.mediatek.contacts.simcontact.SlotUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlertDialog;//PRIZE-add-yuandailin-2016-6-1
import android.view.View.OnTouchListener;//prize-add-huangliemin-2016-7-13

import java.sql.Date;//prize-add-huangpengfei-2016-9-30

import android.os.Debug;//prize-add-huangpengfei-2016-9-30
/*prize-add for dido os8.0-hpf-2017-8-11-start*/
import android.os.Handler;

import com.prize.contacts.common.util.PrizeAnimationHelper;

import android.text.Editable;
import android.text.TextWatcher;
import android.app.ActionBar;
/*prize-add for dido os8.0-hpf-2017-8-11-end*/

/**
 * Displays a list to browse contacts.
 */
public class PeopleActivity extends ContactsActivity implements
        View.OnCreateContextMenuListener,
        View.OnClickListener,
        ActionBarAdapter.Listener,
        DialogManager.DialogShowingViewActivity,
        ContactListFilterController.ContactListFilterListener,
        ProviderStatusListener,
        MultiContactDeleteListener,
        JoinContactsListener {

    private static final String TAG = "PeopleActivity";

    private static final String ENABLE_DEBUG_OPTIONS_HIDDEN_CODE = "debug debug!";

    // These values needs to start at 2. See {@link ContactEntryListFragment}.
    private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

    private static final String ACTION_REFRESH_SIM_CONTACT =
            "com.android.contacts.REFRESH_SIM_CONTACT";

    private final DialogManager mDialogManager = new DialogManager(this);

    private ContactsIntentResolver mIntentResolver;
    private ContactsRequest mRequest;

    private ActionBarAdapter mActionBarAdapter;
    private FloatingActionButtonController mFloatingActionButtonController;
    private View mFloatingActionButtonContainer;
    private View prizePeopleActivityBottomDivider;
    private boolean wasLastFabAnimationScaleIn = false;

    private ContactTileListFragment.Listener mFavoritesFragmentListener =
            new StrequentContactListFragmentListener();

    private ContactListFilterController mContactListFilterController;

    private ContactsUnavailableFragment mContactsUnavailableFragment;
    private ProviderStatusWatcher mProviderStatusWatcher;
    private Integer mProviderStatus;

    private boolean mOptionsMenuContactsAvailable;

    /**
     * Showing a list of Contacts. Also used for showing search results in search mode.
     */
    private MultiSelectContactsListFragment mAllFragment;
    /*prize-add-huangliemin-2016-7-19-start*/
    private TextView mTitle;
    // prize modify for lhp by zhaojian 20180421 start
    private TextView mTitleLhp;
    // prize modify for lhp by zhaojian 20180421 end
    /*prize-add-huangliemin-2016-7-19-end*/
//    private ContactTileListFragment mFavoritesFragment; zhangzhonghao remove viewpager 20160304

    /* prize remove viewpager zhangzhonghao 20160304 start */
    /**
     * ViewPager for swipe
     */
//    private ViewPager mTabPager;
//    private ViewPagerTabs mViewPagerTabs;
//    private TabPagerAdapter mTabPagerAdapter;
//    private String[] mTabTitles;
//    private final TabPagerListener mTabPagerListener = new TabPagerListener();
    /* prize remove viewpager zhangzhonghao 20160304 end */

    private boolean mEnableDebugMenuOptions;

    /**
     * True if this activity instance is a re-created one.  i.e. set true after orientation change.
     * This is set in {@link #onCreate} for later use in {@link #onStart}.
     */
    private boolean mIsRecreatedInstance;

    /**
     * If {@link #configureFragments(boolean)} is already called.  Used to avoid calling it twice
     * in {@link #onStart}.
     * (This initialization only needs to be done once in onStart() when the Activity was just
     * created from scratch -- i.e. onCreate() was just called)
     */
    private boolean mFragmentInitialized;

    /**
     * This is to disable {@link #onOptionsItemSelected} when we trying to stop the activity.
     */
    private boolean mDisableOptionItemSelected;

    /**
     * Sequential ID assigned to each instance; used for logging
     */
    private final int mInstanceId;
    private static final AtomicInteger sNextInstanceId = new AtomicInteger();

    /* prize add some params zhangzhonghao 20160307 start */
    private ImageButton prizeContactToDialer; // move to dialer
    private ImageButton prizeContactOthers; //others operation
    /* prize add some params zhangzhonghao 20160307 end */

    /*prize-add-huangliemin-2016-7-19-start*/
    private LinearLayout prizeDialerLayout;
    private TextView prizeDialerText;
    private TextView prizeContactsText;
    /*prize-add-huangliemin-2016-7-19-end*/

    /*prize-add-huangliemin-2016-6-8-start*/
    final static String KEY_SELECT_MODE = "isSelectMode";
    final static String KEY_SEARCH_MODE = "isPrizeSearchMode";
    boolean isSearchMode = false;
    boolean isSelectMode = false;
    private ProgressDialog dialog = null;
    /*prize-add-huangliemin-2016-6-8-end*/

    /*prize-add-huangliemin-2016-7-13-start*/
    private RelativeLayout mSearchLayout;
    private EditText mSearchEditText;
    /*prize-add-huangliemin-2016-7-13-end*/
    private boolean isPrizeSearchMode;//PRIZE-add -yuandailin-2016-8-8

    /*prize-add-huangliemin-2016-6-29-start*/
    BroadcastReceiver mJoinReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    };
    /*prize-add-huangliemin-2016-6-29-end*/

    /*prize-add for dido os8.0 -hpf-2017-7-31-start*/
    private View mPrizePageContainer;
    private Toolbar mPrizeToolbar;
    private View mPrizeSearchBox;
    private View mPrizeActionBarShadow;
    private PrizeAnimationHelper mPrizeAnimationHelper;
    private EditText mSearchBoxEditText;
    private View mSearchBoxClearBtn;
    /*prize-add for dido os8.0 -hpf-2017-7-31-end*/

    // prize modify for lhp by zhaojian 20180421 start
    private boolean hasSystemFeature;
    // prize modify for lhp by zhaojian 20180421 end

    public PeopleActivity() {
        Log.d(TAG, "[PeopleActivity]new");
        mInstanceId = sNextInstanceId.getAndIncrement();
        mIntentResolver = new ContactsIntentResolver(this);
        /** M: Bug Fix for ALPS00407311 @{ */
        mProviderStatusWatcher = ProviderStatusWatcher.getInstance(ContactsApplicationEx
                .getContactsApplication());
        /** @} */
    }

    @Override
    public String toString() {
        // Shown on logcat
        return String.format("%s@%d", getClass().getSimpleName(), mInstanceId);
    }

    public boolean areContactsAvailable() {
        return ((mProviderStatus != null) && mProviderStatus.equals(ProviderStatus.STATUS_NORMAL)) ||
                ExtensionManager.getInstance().getOp01Extension()
                        .areContactAvailable(mProviderStatus);
    }

    private boolean areGroupWritableAccountsAvailable() {
        return ContactsUtils.areGroupWritableAccountsAvailable(this);
    }

    /**
     * Initialize fragments that are (or may not be) in the layout.
     * <p>
     * For the fragments that are in the layout, we initialize them in
     * {@link #createViewsAndFragments(Bundle)} after inflating the layout.
     * <p>
     * However, the {@link ContactsUnavailableFragment} is a special fragment which may not
     * be in the layout, so we have to do the initialization here.
     * <p>
     * The ContactsUnavailableFragment is always created at runtime.
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d(TAG, "[onAttachFragment]");
        if (fragment instanceof ContactsUnavailableFragment) {
            mContactsUnavailableFragment = (ContactsUnavailableFragment) fragment;
            mContactsUnavailableFragment.setOnContactsUnavailableActionListener(
                    new ContactsUnavailableFragmentListener());
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        Log.i(TAG, "[onCreate]");
        super.onCreate(savedState);

        // prize modify for lhp by zhaojian 20180421 start
        hasSystemFeature = getPackageManager().hasSystemFeature("com.prize.notch.screen");
        // prize modify for lhp by zhaojian 20180421 end

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG, "[onCreate]startPermissionActivity,return.");
            return;
        }

        /// M: Add for ALPS02383518, when received PHB_CHANGED intent but has no
        // READ_PHONE permission, marked NEED_REFRESH_SIM_CONTACTS as true. So refresh
        // all SIM contacts after open all permission and back to contacts at here. @{
        Log.d(TAG, "[onCreate] refresh all SIM contacts");
        Intent intent = new Intent(ACTION_REFRESH_SIM_CONTACT);
        sendBroadcast(intent);
        /// @}

        if (!processIntent(false)) {
            finish();
            Log.w(TAG, "[onCreate]can not process intent:" + getIntent());
            return;
        }

        Log.d(TAG, "[Performance test][Contacts] loading data start time: ["
                + System.currentTimeMillis() + "]");

        mContactListFilterController = ContactListFilterController.getInstance(this);
        mContactListFilterController.checkFilterValidity(false);
        mContactListFilterController.addListener(this);

        mProviderStatusWatcher.addListener(this);

        mIsRecreatedInstance = (savedState != null);

        PDebug.Start("createViewsAndFragments");
        createViewsAndFragments(savedState);
        /*prize-add for dido os8.0 -hpf-2017-8-12-start*/
        mPrizePageContainer = findViewById(R.id.mContactsPage);
        mPrizeSearchBox = findViewById(R.id.prize_search_box);
        mPrizeActionBarShadow = findViewById(R.id.prize_actionbar_shadow);
        mSearchBoxEditText = (EditText) findViewById(R.id.prize_search_box_editor);
        mSearchBoxClearBtn = findViewById(R.id.prize_search_box_clear_button);
        mSearchBoxClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchBoxEditText.setText("");
                mSearchBoxClearBtn.setVisibility(View.GONE);
            }
        });
        mPrizeAnimationHelper = new PrizeAnimationHelper(this);
        configPrizeSearchModle();
        /*prize-add for dido os8.0 -hpf-2017-8-12-end*/
        /// M: Modify for SelectAll/DeSelectAll Feature. @{
        Button selectcount = (Button) mActionBarAdapter.mSelectionContainer
                .findViewById(R.id.selection_count_text);
        selectcount.setOnClickListener(this);
        /// @}
        getWindow().setBackgroundDrawable(null);

        PDebug.End("Contacts.onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        PDebug.Start("onNewIntent");
        setIntent(intent);
        if (!processIntent(true)) {
            finish();
            Log.w(TAG, "[onNewIntent]can not process intent:" + getIntent());
            return;
        }
        Log.d(TAG, "[onNewIntent]");
        mActionBarAdapter.initialize(null, mRequest);

        mContactListFilterController.checkFilterValidity(false);

        // Re-configure fragments.
        configureFragments(true /* from request */);
        initializeFabVisibility();
        invalidateOptionsMenuIfNeeded();
        PDebug.End("onNewIntent");
    }

    /**
     * Resolve the intent and initialize {@link #mRequest}, and launch another activity if redirect
     * is needed.
     *
     * @param forNewIntent set true if it's called from {@link #onNewIntent(Intent)}.
     * @return {@code true} if {@link PeopleActivity} should continue running.  {@code false}
     * if it shouldn't, in which case the caller should finish() itself and shouldn't do
     * farther initialization.
     */
    private boolean processIntent(boolean forNewIntent) {
        // Extract relevant information from the intent
        mRequest = mIntentResolver.resolveIntent(getIntent());
//        if (Log.isLoggable(TAG, Log.DEBUG)) {
//            Log.d(TAG, this + " processIntent: forNewIntent=" + forNewIntent
//                    + " intent=" + getIntent() + " request=" + mRequest);
//        }
        if (!mRequest.isValid()) {
            Log.w(TAG, "[processIntent]request is inValid");
            setResult(RESULT_CANCELED);
            return false;
        }

        if (mRequest.getActionCode() == ContactsRequest.ACTION_VIEW_CONTACT) {
            Log.d(TAG, "[processIntent]start QuickContactActivity");
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    mRequest.getContactUri(), QuickContactActivity.MODE_FULLY_EXPANDED);
            intent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenType.UNKNOWN);
            ImplicitIntentsUtil.startActivityInApp(this, intent);
            return false;
        }
        return true;
    }

    private void createViewsAndFragments(Bundle savedState) {
        Log.d(TAG, "[createViewsAndFragments]");
        PDebug.Start("createViewsAndFragments, prepare fragments");
        // Disable the ActionBar so that we can use a Toolbar. This needs to be called before
        // setContentView().
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.people_activity);

        final FragmentManager fragmentManager = getFragmentManager();

        // Hide all tabs (the current tab will later be reshown once a tab is selected)
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        /* prize remove viewpager zhangzhonghao 20160304 start */
//        mTabTitles = new String[TabState.COUNT];
//        mTabTitles[TabState.FAVORITES] = getString(R.string.favorites_tab_label);
//        mTabTitles[TabState.ALL] = getString(R.string.all_contacts_tab_label);
//        mTabPager = getView(R.id.tab_pager);
//        mTabPagerAdapter = new TabPagerAdapter();
//        mTabPager.setAdapter(mTabPagerAdapter);
//        mTabPager.setOnPageChangeListener(mTabPagerListener);
        /* prize remove viewpager zhangzhonghao 20160304 end */

        // Configure toolbar and toolbar tabs. If in landscape mode, we  configure tabs differntly.
        mPrizeToolbar = getView(R.id.toolbar);//prize-change for dido os8.0-hpf-2017-8-14
        /*prize-add-huangliemin-2016-7-19-start*/
        mTitle = (TextView) mPrizeToolbar.findViewById(R.id.contacts_title);
        // prize modify for lhp by zhaojian 20180421 start
        mTitleLhp = (TextView) mPrizeToolbar.findViewById(R.id.contacts_title_lhp);
        // prize modify for lhp by zhaojian 20180421 end
        mPrizeToolbar.setTitle("");
        /*prize-add-huangliemin-2016-7-19-end*/
        setActionBar(mPrizeToolbar);
        
        /*prize-add-hpf-2018-2-24-start*/
        // prize modify for lhp by zhaojian 20180421 start
        Log.d(TAG, "hasSystemFeature = " + hasSystemFeature);
        if (!hasSystemFeature) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setCustomView(R.layout.prize_people_activity_actionbar);
                ImageButton newContactsButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.prize_new_contacts_btn);
                newContactsButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                        Bundle extras = getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtras(extras);
                        }
                        try {
                            ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(PeopleActivity.this, R.string.missing_app,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        // prize modify for lhp by zhaojian 20180421 end

        /*prize-add-hpf-2018-2-24-end*/
        
        /* prize remove viewpager zhangzhonghao 20160304 start */
//        final ViewPagerTabs portraitViewPagerTabs
//                = (ViewPagerTabs) findViewById(R.id.lists_pager_header);
//        ViewPagerTabs landscapeViewPagerTabs = null;
//        if (portraitViewPagerTabs ==  null) {
//            landscapeViewPagerTabs = (ViewPagerTabs) getLayoutInflater().inflate(
//                    R.layout.people_activity_tabs_lands, toolbar, /* attachToRoot = */ false);
//            mViewPagerTabs = landscapeViewPagerTabs;
//        } else {
//            mViewPagerTabs = portraitViewPagerTabs;
//        }
//        mViewPagerTabs.setViewPager(mTabPager);
        /* prize remove viewpager zhangzhonghao 20160304 end */

//        final String FAVORITE_TAG = "tab-pager-favorite"; zhangzhonghao 20160304
        final String ALL_TAG = "tab-pager-all";

        // Create the fragments and add as children of the view pager.
        // The pager adapter will only change the visibility; it'll never create/destroy
        // fragments.
        // However, if it's after screen rotation, the fragments have been re-created by
        // the fragment manager, so first see if there're already the target fragments
        // existing.
        
        /* prize remove viewpager zhangzhonghao 20160304 start */
//        mFavoritesFragment = (ContactTileListFragment)
//                fragmentManager.findFragmentByTag(FAVORITE_TAG);

//        mAllFragment = (MultiSelectContactsListFragment)
//                fragmentManager.findFragmentByTag(ALL_TAG);

//        if (mFavoritesFragment == null) {
//            mFavoritesFragment = new ContactTileListFragment();
//            mAllFragment = new MultiSelectContactsListFragment();
//
//            transaction.add(R.id.tab_pager, mFavoritesFragment, FAVORITE_TAG);
//            transaction.add(R.id.mContactsPage, mAllFragment, ALL_TAG);
        
    	/*prize-add for dido os8.0 -hpf-2017-7-31-start*/
        mPrizePageContainer = findViewById(R.id.mContactsPage);
    	/*prize-add for dido os8.0 -hpf-2017-7-31-end*/
        mAllFragment = new MultiSelectContactsListFragment(true);
        transaction.replace(R.id.mContactsPage, mAllFragment);
//        }
//
//        mFavoritesFragment.setListener(mFavoritesFragmentListener);
        /* prize remove viewpager zhangzhonghao 20160304 end */

        mAllFragment.setOnContactListActionListener(new ContactBrowserActionListener());
        mAllFragment.setCheckBoxListListener(new CheckBoxListListener());

        // Hide all fragments for now.  We adjust visibility when we get onSelectedTabChanged()
        // from ActionBarAdapter.
//        transaction.hide(mFavoritesFragment); zhangzhonghao remvoe viewpager
//        transaction.hide(mAllFragment); zhangzhonghao remvoe viewpager

        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

        // Setting Properties after fragment is created
//        mFavoritesFragment.setDisplayType(DisplayType.STREQUENT); zhangzhonghao remove viewpager

        /* prize remove viewpager zhangzhonghao 20160304 start */
//        mActionBarAdapter = new ActionBarAdapter(this, this, getActionBar(),
//                portraitViewPagerTabs, landscapeViewPagerTabs, toolbar);
        mActionBarAdapter = new ActionBarAdapter(this, this, getActionBar(),
                mPrizeToolbar);
        mActionBarAdapter.initialize(savedState, mRequest);
        /* prize remove viewpager zhangzhonghao 20160304 end */

        // Add shadow under toolbar
        //ViewUtil.addRectangularOutlineProvider(findViewById(R.id.toolbar_parent), getResources()); zhangzhonghao

        // Configure floating action button
        prizePeopleActivityBottomDivider = findViewById(R.id.prize_people_activity_bottom_divider);//PRIZE-add -yuandailin-2016-8-5
        mFloatingActionButtonContainer = findViewById(R.id.floating_action_button_container);
        final ImageButton floatingActionButton
                = (ImageButton) findViewById(R.id.floating_action_button);
        /*prize-add-huangliemin-2016-7-15-start*/
        floatingActionButton.setVisibility(View.GONE);
        /*prize-add-huangliemin-2016-7-15-end*/
        floatingActionButton.setOnClickListener(this);
        mFloatingActionButtonController = new FloatingActionButtonController(this,
                mFloatingActionButtonContainer, floatingActionButton);
        
        /* prize instance my params zhangzhonghao 20160307 start */
        prizeContactToDialer = (ImageButton) findViewById(R.id.prize_contacts_dialer);
        prizeDialerLayout = (LinearLayout) findViewById(R.id.prize_contacts_dialer_layout);
        prizeContactOthers = (ImageButton) findViewById(R.id.prize_contacts_others);
        /* prize instance my params zhangzhonghao 20160307 end */
        
        /* prize set adapter for our view zhangzhonghao 20160314 start */
        //prizeContactToDialer.setOnClickListener(this); 
        prizeContactToDialer.setClickable(false);//prize-add-huangpengfei-2016-8-9
        prizeContactOthers.setOnClickListener(/*this*/null);//prize-change-for-only-show-contacts-image-huangliemin-2016-7-15
        prizeDialerLayout.setOnClickListener(this);
        prizeContactOthers.setClickable(false);//prize-add-huangliemin-2016-7-15
        /* prize set adapter for our view zhangzhonghao 20160314 end */
        
        /*prize-add-huangliemin-2016-7-19-start*/
        prizeDialerText = (TextView) findViewById(R.id.prize_dialer_text);
        prizeContactsText = (TextView) findViewById(R.id.prize_contacts_text);
        /*prize-add-huangliemin-2016-7-19-end*/

        //prize-add-huangliemin-2016-6-29-start
        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.please_wait_join));
        //prize-add-huangliemin-2016-6-29-end

        //prize-add-huangliemin-2016-7-13-start
        mSearchLayout = (RelativeLayout) findViewById(R.id.search_edit_layout);
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        mSearchEditText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //onSearchRequested();
					/*prize-add for dido os8.0 -hpf-2017-7-31-start*/
                    onAction(ActionBarAdapter.Listener.Action.START_SEARCH_MODE);
					/*prize-add for dido os8.0 -hpf-2017-7-31-end*/
                }
                return true;
            }
        });
        //prize-add-huangliemin-2016-7-13-end

        initializeFabVisibility();

        invalidateOptionsMenuIfNeeded();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "[onStart]mFragmentInitialized = " + mFragmentInitialized
                + ",mIsRecreatedInstance = " + mIsRecreatedInstance);

        if (!mFragmentInitialized) {
            mFragmentInitialized = true;
            /* Configure fragments if we haven't.
             *
             * Note it's a one-shot initialization, so we want to do this in {@link #onCreate}.
             *
             * However, because this method may indirectly touch views in fragments but fragments
             * created in {@link #configureContentView} using a {@link FragmentTransaction} will NOT
             * have views until {@link Activity#onCreate} finishes (they would if they were inflated
             * from a layout), we need to do it here in {@link #onStart()}.
             *
             * (When {@link Fragment#onCreateView} is called is different in the former case and
             * in the latter case, unfortunately.)
             *
             * Also, we skip most of the work in it if the activity is a re-created one.
             * (so the argument.)
             */
            configureFragments(!mIsRecreatedInstance);
        }
        /*prize-add-huangliemin-2016-6-29-start*/
        IntentFilter filter = new IntentFilter();
        filter.addAction("prize.close.dialog");
        registerReceiver(mJoinReceiver, filter);
        /*prize-add-huangliemin-2016-6-29-end*/
        /// M: register sim change @{
        AccountTypeManagerEx.registerReceiverOnSimStateAndInfoChanged(this, mBroadcastReceiver);
        /// @}
        super.onStart();

    }

    @Override
    protected void onPause() {
        Log.i(TAG, "[onPause]");
        mOptionsMenuContactsAvailable = false;
        mProviderStatusWatcher.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "[onResume]");

        mProviderStatusWatcher.start();
        updateViewConfiguration(true);

        // Re-register the listener, which may have been cleared when onSaveInstanceState was
        // called.  See also: onSaveInstanceState
        mActionBarAdapter.setListener(this);
        mDisableOptionItemSelected = false;
        
        /* prize remove viewpager zhangzhonghao 20160304 start */
//        if (mTabPager != null) {
//            mTabPager.setOnPageChangeListener(mTabPagerListener);
//        }
        /* prize remove viewpager zhangzhonghao 20160304 end */

        // Current tab may have changed since the last onSaveInstanceState().  Make sure
        // the actual contents match the tab.
        updateFragmentsVisibility();
 
        /*prize-add for dido os8.0-hpf-2017-8-15-start*/
        if (mIsSearchModle) {
            mAllFragment.setManagerGroupsButtonVisibility(false);
            mFloatingActionButtonContainer.setVisibility(View.GONE);
            prizePeopleActivityBottomDivider.setVisibility(View.GONE);
        } else {
            mAllFragment.setManagerGroupsButtonVisibility(true);
            mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
            prizePeopleActivityBottomDivider.setVisibility(View.VISIBLE);
        }
        /*prize-add for dido os8.0-hpf-2017-8-15-end*/

        Log.d(TAG, "[Performance test][Contacts] loading data end time: ["
                + System.currentTimeMillis() + "]");
        PDebug.End("Contacts.onResume");

    }

    @Override
    protected void onStop() {
        Log.i(TAG, "[onStop]");
        PDebug.Start("onStop");
        /*prize-add-huangliemin-2016-6-29-start*/
        unregisterReceiver(mJoinReceiver);
        /*prize-add-huangliemin-2016-6-29-end*/
        /// M: unregister sim change @{
        unregisterReceiver(mBroadcastReceiver);
        /// @
        super.onStop();
        PDebug.End("onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "[onDestroy]");
        PDebug.Start("onDestroy");
        mProviderStatusWatcher.removeListener(this);

        // Some of variables will be null if this Activity redirects Intent.
        // See also onCreate() or other methods called during the Activity's initialization.
        if (mActionBarAdapter != null) {
            mActionBarAdapter.setListener(null);
        }
        if (mContactListFilterController != null) {
            mContactListFilterController.removeListener(this);
        }

        /// M: Add for SelectAll/DeSelectAll Feature. @{
        /// Fix for ALPS03183579
        if (mSelectionMenu != null && mSelectionMenu.isShown()) {
            mSelectionMenu.dismiss();
        }
        /// @}

        super.onDestroy();
        PDebug.End("onDestroy");
    }

    private void configureFragments(boolean fromRequest) {
        Log.d(TAG, "[configureFragments]fromRequest = " + fromRequest);
        if (fromRequest) {
            ContactListFilter filter = null;
            int actionCode = mRequest.getActionCode();
            boolean searchMode = mRequest.isSearchMode();
            
            /* prize remove viewpager zhangzhonghao 20160304 start */
//            final int tabToOpen;
//            switch (actionCode) {
//                case ContactsRequest.ACTION_ALL_CONTACTS:
//                    filter = ContactListFilter.createFilterWithType(
//                            ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
//                    tabToOpen = TabState.ALL;
//                    break;
//                case ContactsRequest.ACTION_CONTACTS_WITH_PHONES:
//                    filter = ContactListFilter.createFilterWithType(
//                            ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY);
//                    tabToOpen = TabState.ALL;
//                    break;
//
//                case ContactsRequest.ACTION_FREQUENT:
//                case ContactsRequest.ACTION_STREQUENT:
//                case ContactsRequest.ACTION_STARRED:
//                    tabToOpen = TabState.FAVORITES;
//                    break;
//                case ContactsRequest.ACTION_VIEW_CONTACT:
//                    tabToOpen = TabState.ALL;
//                    break;
//                default:
//                    tabToOpen = -1;
//                    break;
//            }
//            if (tabToOpen != -1) {
//                mActionBarAdapter.setCurrentTab(tabToOpen);
//            }
            /* prize remove viewpager zhangzhonghao 20160304 end */

            if (filter != null) {
                mContactListFilterController.setContactListFilter(filter, false);
                searchMode = false;
            }

            if (mRequest.getContactUri() != null) {
                searchMode = false;
            }

            mActionBarAdapter.setSearchMode(searchMode);
            configureContactListFragmentForRequest();
        }

        configureContactListFragment();

        invalidateOptionsMenuIfNeeded();
    }

    private void initializeFabVisibility() {
    	
        /*prize-change-huangliemin-2016-6-7-start*/
        /*
        final boolean hideFab = mActionBarAdapter.isSearchMode()
                || mActionBarAdapter.isSelectionMode();
        */
        final boolean hideFab = mActionBarAdapter.isSelectionMode();
        /*prize-change-huangliemin-2016-6-7-end*/
        Log.d(TAG, "[initializeFabVisibility]   hideFab = " + hideFab);
        mFloatingActionButtonContainer.setVisibility(hideFab ? View.GONE : View.VISIBLE);
        mFloatingActionButtonController.resetIn();
        wasLastFabAnimationScaleIn = !hideFab;
    }

    private void showFabWithAnimation(boolean showFab) {
        Log.d(TAG, "[showFabWithAnimation]   showFab = " + showFab);
        if (mFloatingActionButtonContainer == null) {
            return;
        }
        if (showFab) {
            if (!wasLastFabAnimationScaleIn) {
                mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
//                mFloatingActionButtonController.scaleIn(0); zhangzhonghao 20160314
            }
            wasLastFabAnimationScaleIn = true;

        } else {
            if (wasLastFabAnimationScaleIn) {
                mFloatingActionButtonContainer.setVisibility(View.GONE);
//                mFloatingActionButtonController.scaleOut(); zhangzhonghao 20160314
            }
            wasLastFabAnimationScaleIn = false;
        }
    }

    @Override
    public void onContactListFilterChanged() {
        if (mAllFragment == null || !mAllFragment.isAdded()) {
            return;
        }

        mAllFragment.setFilter(mContactListFilterController.getFilter());

        invalidateOptionsMenuIfNeeded();
    }

    /**
     * Handler for action bar actions.
     */
    @Override
    public void onAction(int action) {
        Log.d(TAG, "[onAction]action = " + action);
        switch (action) {
            case ActionBarAdapter.Listener.Action.START_SELECTION_MODE:
                mAllFragment.displayCheckBoxes(true);
                showFabWithAnimation(/* showFabWithAnimation = */ false);
                /*prize-add-huangliemin-2016-6-8-start*/
                mAllFragment.setBladeViewVisibility(false);
                mAllFragment.setSelectAllButtonVisibility(true);
                mAllFragment.setManagerGroupsButtonVisibility(false);
                if (mSearchLayout != null) {
                    mSearchLayout.setVisibility(View.GONE);
                }
                if (mTitle != null) {
                    mTitle.setVisibility(View.GONE);
                }
                // prize modify for lhp by zhaojian 20180421 start
                if (mTitleLhp != null) {
                    mTitleLhp.setVisibility(View.GONE);
                }
                // prize modify for lhp by zhaojian 20180421 end
                /*prize-add-huangliemin-2016-6-8-end*/
                startSearchOrSelectionMode();
                break;
            case ActionBarAdapter.Listener.Action.START_SEARCH_MODE:
                /*prize-add-huangliemin-2016-6-8-start*/
                if (!mActionBarAdapter.isSelectionMode()) {
                    mAllFragment.setBladeViewVisibility(false);
                    mAllFragment.setSelectAllButtonVisibility(false);
                    mAllFragment.setManagerGroupsButtonVisibility(true);
                    if (mPrizeSearchBox != null && mPrizeToolbar != null) {
                        //mSearchLayout.setVisibility(View.GONE);
                	  /*prize-add for dido os8.0 -hpf-2017-7-31-start*/
                        mIsSearchModle = true;
                        mPrizeSearchBox.setVisibility(View.VISIBLE);
                        mPrizeActionBarShadow.bringToFront();
                        mPrizeAnimationHelper.peopleSearchAnimation(mPrizeSearchBox, mSearchBoxEditText, mPrizeToolbar,
                                mPrizePageContainer, mFloatingActionButtonContainer, prizePeopleActivityBottomDivider,
                                PrizeAnimationHelper.HIDE);
                	  /*prize-add for dido os8.0 -hpf-2017-7-31-end*/
                    }
                    if (mTitle != null) {
                        mTitle.setVisibility(View.GONE);
                    }
                    // prize modify for lhp by zhaojian 20180421 start
                    if (mTitleLhp != null) {
                        mTitleLhp.setVisibility(View.GONE);
                    }
                    // prize modify for lhp by zhaojian 20180421 end
                }
                /*prize-add-huangliemin-2016-6-8-end*/
                if (!mIsRecreatedInstance) {
                    Logger.logScreenView(this, ScreenType.SEARCH);
                }
                startSearchOrSelectionMode();
                break;
            case ActionBarAdapter.Listener.Action.BEGIN_STOPPING_SEARCH_AND_SELECTION_MODE:
                showFabWithAnimation(/* showFabWithAnimation = */ true);
                /*prize-add-huangliemin-2016-6-8-start*/
                mAllFragment.setBladeViewVisibility(true);
                mAllFragment.setSelectAllButtonVisibility(false);
                mAllFragment.setManagerGroupsButtonVisibility(true);
                if (mPrizeSearchBox != null && mPrizeToolbar != null) {
                    mSearchLayout.setVisibility(View.VISIBLE);
                }
                if (mTitle != null) {
                    mTitle.setVisibility(View.GONE);
                }
                // prize add for lhp by zhaojian 20180421 start
                if (mTitleLhp != null) {
                    mTitleLhp.setVisibility(View.GONE);
                }
                // prize add for lhp by zhaojian 20180421 end
                /*prize-add-huangliemin-2016-6-8-end*/
                break;
            case ActionBarAdapter.Listener.Action.STOP_SEARCH_AND_SELECTION_MODE:
                setQueryTextToFragment("");
                updateFragmentsVisibility();
                invalidateOptionsMenu();
                showFabWithAnimation(/* showFabWithAnimation = */ true);
                /*prize-add-huangliemin-2016-6-8-start*/
                mAllFragment.setBladeViewVisibility(true);
                mAllFragment.setSelectAllButtonVisibility(false);
                mAllFragment.setManagerGroupsButtonVisibility(true);
                if (mPrizeSearchBox != null && mPrizeToolbar != null) {
                    mSearchLayout.setVisibility(View.VISIBLE);
                	/*prize-add for dido os8.0 -hpf-2017-7-31-start*/
                    mSearchBoxEditText.setText("");
                    mIsSearchModle = false;
                    mPrizeAnimationHelper.peopleSearchAnimation(mPrizeSearchBox, mSearchBoxEditText, mPrizeToolbar,
                            mPrizePageContainer, mFloatingActionButtonContainer, prizePeopleActivityBottomDivider,
                            PrizeAnimationHelper.SHOW);
                	/*prize-add for dido os8.0 -hpf-2017-7-31-end*/
                }
                // prize modify for lhp by zhaojian 20180421 start
                /*if(mTitle!=null) {
                    mTitle.setVisibility(View.VISIBLE);
                }*/
                if (hasSystemFeature) {
                    if (mTitleLhp != null) {
                        mTitleLhp.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mTitle != null) {
                        mTitle.setVisibility(View.VISIBLE);
                    }
                }
                // prize modify for lhp by zhaojian 20180421 end
                /*prize-add-huangliemin-2016-6-8-end*/
                break;
            case ActionBarAdapter.Listener.Action.CHANGE_SEARCH_QUERY:
                //final String queryString = mActionBarAdapter.getQueryString();
                setQueryTextToFragment(mQueryString);
                Log.d(TAG, "[onAction]  mQueryString=" + mQueryString);
                updateDebugOptionsVisibility(
                        ENABLE_DEBUG_OPTIONS_HIDDEN_CODE.equals(mQueryString));
                break;
            default:
                throw new IllegalStateException("Unkonwn ActionBarAdapter action: " + action);
        }
    }

    private void startSearchOrSelectionMode() {
        configureFragments(false /* from request */);
        updateFragmentsVisibility();
        invalidateOptionsMenu();
        //showFabWithAnimation(/* showFabWithAnimation = */ false);//prize-remove-huangpengfei-2016-11-5
    }

    @Override
    public void onSelectedTabChanged() {
        Log.d(TAG, "[onSelectedTabChanged]");
        updateFragmentsVisibility();
    }

    @Override
    public void onUpButtonPressed() {
        Log.d(TAG, "[onUpButtonPressed]");
        onBackPressed();
    }

    private void updateDebugOptionsVisibility(boolean visible) {
        if (mEnableDebugMenuOptions != visible) {
            mEnableDebugMenuOptions = visible;
            invalidateOptionsMenu();
        }
    }

    /* prize remove viewpager zhangzhonghao 20160304 start */

    /**
     * Updates the fragment/view visibility according to the current mode, such as
     * {@link ActionBarAdapter#isSearchMode()} and {@link ActionBarAdapter#getCurrentTab()}.
     */
    private void updateFragmentsVisibility() {
//        int tab = mActionBarAdapter.getCurrentTab();
//
//        if (mActionBarAdapter.isSearchMode() || mActionBarAdapter.isSelectionMode()) {
//            mTabPagerAdapter.setTabsHidden(true);
//        } else {
//            // No smooth scrolling if quitting from the search/selection mode.
//            final boolean wereTabsHidden = mTabPagerAdapter.areTabsHidden()
//                    || mActionBarAdapter.isSelectionMode();
//            mTabPagerAdapter.setTabsHidden(false);
//            if (mTabPager.getCurrentItem() != tab) {
//                mTabPager.setCurrentItem(tab, !wereTabsHidden);
//            }
//        }
        if (!mActionBarAdapter.isSelectionMode()) {
            mAllFragment.displayCheckBoxes(false);
        }
        invalidateOptionsMenu();
//        showEmptyStateForTab(tab);
    }
//
//    private void showEmptyStateForTab(int tab) {
//        if (mContactsUnavailableFragment != null) {
//            switch (getTabPositionForTextDirection(tab)) {
//                case TabState.FAVORITES:
//                    mContactsUnavailableFragment.setMessageText(
//                            R.string.listTotalAllContactsZeroStarred, -1);
//                    break;
//                case TabState.ALL:
//                    mContactsUnavailableFragment.setMessageText(R.string.noContacts, -1);
//                    break;
//                default:
//                    break;
//            }
//            // When using the mContactsUnavailableFragment the ViewPager doesn't contain two views.
//            // Therefore, we have to trick the ViewPagerTabs into thinking we have changed tabs
//            // when the mContactsUnavailableFragment changes. Otherwise the tab strip won't move.
//            mViewPagerTabs.onPageScrolled(tab, 0, 0);
//        }
//    }

//    private class TabPagerListener implements ViewPager.OnPageChangeListener {
//
//        // This package-protected constructor is here because of a possible compiler bug.
//        // PeopleActivity$1.class should be generated due to the private outer/inner class access
//        // needed here.  But for some reason, PeopleActivity$1.class is missing.
//        // Since $1 class is needed as a jvm work around to get access to the inner class,
//        // changing the constructor to package-protected or public will solve the problem.
//        // To verify whether $1 class is needed, javap PeopleActivity$TabPagerListener and look for
//        // references to PeopleActivity$1.
//        //
//        // When the constructor is private and PeopleActivity$1.class is missing, proguard will
//        // correctly catch this and throw warnings and error out the build on user/userdebug builds.
//        //
//        // All private inner classes below also need this fix.
//        TabPagerListener() {}
//
//        @Override
//        public void onPageScrollStateChanged(int state) {
//            if (!mTabPagerAdapter.areTabsHidden()) {
//                mViewPagerTabs.onPageScrollStateChanged(state);
//            }
//        }
//
//        @Override
//        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            if (!mTabPagerAdapter.areTabsHidden()) {
//                mViewPagerTabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            }
//        }
//
//        @Override
//        public void onPageSelected(int position) {
//            // Make sure not in the search mode, in which case position != TabState.ordinal().
//            if (!mTabPagerAdapter.areTabsHidden()) {
//                mActionBarAdapter.setCurrentTab(position, false);
//                mViewPagerTabs.onPageSelected(position);
//                showEmptyStateForTab(position);
//                /// M: [vcs] @{
//                if (mVcsController != null) {
//                    mVcsController.onPageSelectedVcs();
//                }
//                /// @}
//                invalidateOptionsMenu();
//            }
//        }
//    }

    /**
     * Adapter for the {@link ViewPager}.  Unlike {@link FragmentPagerAdapter},
     * {@link #instantiateItem} returns existing fragments, and {@link #instantiateItem}/
     * {@link #destroyItem} show/hide fragments instead of attaching/detaching.
     * <p>
     * In search mode, we always show the "all" fragment, and disable the swipe.  We change the
     * number of items to 1 to disable the swipe.
     * <p>
     * TODO figure out a more straight way to disable swipe.
     */
//    private class TabPagerAdapter extends PagerAdapter {
//        private final FragmentManager mFragmentManager;
//        private FragmentTransaction mCurTransaction = null;
//
//        private boolean mAreTabsHiddenInTabPager;
//
//        private Fragment mCurrentPrimaryItem;
//
//        public TabPagerAdapter() {
//            mFragmentManager = getFragmentManager();
//        }
//
//        public boolean areTabsHidden() {
//            return mAreTabsHiddenInTabPager;
//        }
//
//        public void setTabsHidden(boolean hideTabs) {
//            if (hideTabs == mAreTabsHiddenInTabPager) {
//                return;
//            }
//            mAreTabsHiddenInTabPager = hideTabs;
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public int getCount() {
//            return mAreTabsHiddenInTabPager ? 1 : TabState.COUNT;
//        }
//
//        /** Gets called when the number of items changes. */
//        @Override
//        public int getItemPosition(Object object) {
//            if (mAreTabsHiddenInTabPager) {
//                if (object == mAllFragment) {
//                    return 0; // Only 1 page in search mode
//                }
//            } else {
//                if (object == mFavoritesFragment) {
//                    return getTabPositionForTextDirection(TabState.FAVORITES);
//                }
//                if (object == mAllFragment) {
//                    return getTabPositionForTextDirection(TabState.ALL);
//                }
//            }
//            return POSITION_NONE;
//        }
//
//        @Override
//        public void startUpdate(ViewGroup container) {
//        }
//
//        private Fragment getFragment(int position) {
//            position = getTabPositionForTextDirection(position);
//            if (mAreTabsHiddenInTabPager) {
//                if (position != 0) {
//                    // This has only been observed in monkey tests.
//                    // Let's log this issue, but not crash
//                    Log.w(TAG, "Request fragment at position=" + position + ", eventhough we " +
//                            "are in search mode");
//                }
//                return mAllFragment;
//            } else {
//                if (position == TabState.FAVORITES) {
//                    return mFavoritesFragment;
//                } else if (position == TabState.ALL) {
//                    return mAllFragment;
//                }
//            }
//            throw new IllegalArgumentException("position: " + position);
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            if (mCurTransaction == null) {
//                mCurTransaction = mFragmentManager.beginTransaction();
//            }
//            Fragment f = getFragment(position);
//            mCurTransaction.show(f);
//
//            // Non primary pages are not visible.
//            f.setUserVisibleHint(f == mCurrentPrimaryItem);
//            return f;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            if (mCurTransaction == null) {
//                mCurTransaction = mFragmentManager.beginTransaction();
//            }
//            mCurTransaction.hide((Fragment) object);
//        }
//
//        @Override
//        public void finishUpdate(ViewGroup container) {
//            if (mCurTransaction != null) {
//                mCurTransaction.commitAllowingStateLoss();
//                mCurTransaction = null;
//                mFragmentManager.executePendingTransactions();
//            }
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return ((Fragment) object).getView() == view;
//        }
//
//        @Override
//        public void setPrimaryItem(ViewGroup container, int position, Object object) {
//            Fragment fragment = (Fragment) object;
//            if (mCurrentPrimaryItem != fragment) {
//                if (mCurrentPrimaryItem != null) {
//                    mCurrentPrimaryItem.setUserVisibleHint(false);
//                }
//                if (fragment != null) {
//                    fragment.setUserVisibleHint(true);
//                }
//                mCurrentPrimaryItem = fragment;
//            }
//        }
//
//        @Override
//        public Parcelable saveState() {
//            return null;
//        }
//
//        @Override
//        public void restoreState(Parcelable state, ClassLoader loader) {
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mTabTitles[position];
//        }
//    }
    /* prize remove viewpager zhangzhonghao 20160304 end */
    private void setQueryTextToFragment(String query) {
        mAllFragment.setQueryString(query, true);
        mAllFragment.setVisibleScrollbarEnabled(!mAllFragment.isSearchMode());
    }

    private void configureContactListFragmentForRequest() {
        Uri contactUri = mRequest.getContactUri();
        if (contactUri != null) {
            mAllFragment.setSelectedContactUri(contactUri);
        }

        mAllFragment.setFilter(mContactListFilterController.getFilter());
        setQueryTextToFragment(mActionBarAdapter.getQueryString());

        if (mRequest.isDirectorySearchEnabled()) {
            mAllFragment.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_DEFAULT);
        } else {
            mAllFragment.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_NONE);
        }
    }

    private void configureContactListFragment() {
        // Filter may be changed when this Activity is in background.
        mAllFragment.setFilter(mContactListFilterController.getFilter());

        mAllFragment.setVerticalScrollbarPosition(getScrollBarPosition());
        mAllFragment.setSelectionVisible(false);
    }

    private int getScrollBarPosition() {
        return isRTL() ? View.SCROLLBAR_POSITION_LEFT : View.SCROLLBAR_POSITION_RIGHT;
    }

    private boolean isRTL() {
        final Locale locale = Locale.getDefault();
        return TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public void onProviderStatusChange() {
        Log.d(TAG, "[onProviderStatusChange]");
        updateViewConfiguration(false);
    }

    private void updateViewConfiguration(boolean forceUpdate) {
        Log.d(TAG, "[updateViewConfiguration]forceUpdate = " + forceUpdate);
        int providerStatus = mProviderStatusWatcher.getProviderStatus();
        if (!forceUpdate && (mProviderStatus != null)
                && (mProviderStatus.equals(providerStatus))) return;
        mProviderStatus = providerStatus;

        View contactsUnavailableView = findViewById(R.id.contacts_unavailable_view);

        if (mProviderStatus.equals(ProviderStatus.STATUS_NORMAL) ||
                ExtensionManager.getInstance().getRcsExtension().isRcsServiceAvailable()) {
            // Ensure that the mTabPager is visible; we may have made it invisible below.
            contactsUnavailableView.setVisibility(View.GONE);
            
            /* prize remove viewpager zhangzhonghao 20160304 start */
//            if (mTabPager != null) {
//                mTabPager.setVisibility(View.VISIBLE);
//            }
            /* prize remove viewpager zhangzhonghao 20160304 end */

            if (mAllFragment != null) {
                mAllFragment.setEnabled(true);
            }
        } else {

            // Setting up the page so that the user can still use the app
            // even without an account.
            if (mAllFragment != null) {
                mAllFragment.setEnabled(false);
            }
            if (mContactsUnavailableFragment == null) {
                mContactsUnavailableFragment = new ContactsUnavailableFragment();
                mContactsUnavailableFragment.setOnContactsUnavailableActionListener(
                        new ContactsUnavailableFragmentListener());
                getFragmentManager().beginTransaction()
                        .replace(R.id.contacts_unavailable_container, mContactsUnavailableFragment)
                        .commitAllowingStateLoss();
            }
            mContactsUnavailableFragment.updateStatus(mProviderStatus);

            // Show the contactsUnavailableView, and hide the mTabPager so that we don't
            // see it sliding in underneath the contactsUnavailableView at the edges.
            
            /* prize remove viewpager zhangzhonghao 20160304 start */
            /**
             * M: Bug Fix @{
             * CR ID: ALPS00113819 Descriptions:
             * remove ContactUnavaliableFragment
             * Fix wait cursor keeps showing while no contacts issue
             */
//            ActivitiesUtils.setAllFramgmentShow(contactsUnavailableView, mAllFragment,
//                    this, mTabPager, mContactsUnavailableFragment, mProviderStatus);
//
//            showEmptyStateForTab(mActionBarAdapter.getCurrentTab());
            /* prize remove viewpager zhangzhonghao 20160304 end */

        }

        invalidateOptionsMenuIfNeeded();
    }

    private final class ContactBrowserActionListener implements OnContactBrowserActionListener {
        ContactBrowserActionListener() {
        }

        @Override
        public void onSelectionChange() {

        }

        @Override
        public void onViewContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                // No implicit intent as user may have a different contacts app in work profile.
                QuickContact.showQuickContact(PeopleActivity.this, new Rect(), contactLookupUri,
                        QuickContactActivity.MODE_FULLY_EXPANDED, null);
            } else {
                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                        contactLookupUri, QuickContactActivity.MODE_FULLY_EXPANDED);
                intent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                        mAllFragment.isSearchMode() ? ScreenType.SEARCH : ScreenType.ALL_CONTACTS);
                ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
            }
        }

        @Override
        public void onDeleteContactAction(Uri contactUri) {
            Log.d(TAG, "[onDeleteContactAction]contactUri = " + contactUri);
            ContactDeletionInteraction.start(PeopleActivity.this, contactUri, false);
        }

        @Override
        public void onFinishAction() {
            Log.d(TAG, "[onFinishAction]call onBackPressed");
            onBackPressed();
        }

        @Override
        public void onInvalidSelection() {
            ContactListFilter filter;
            ContactListFilter currentFilter = mAllFragment.getFilter();
            if (currentFilter != null
                    && currentFilter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
                mAllFragment.setFilter(filter);
            } else {
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_SINGLE_CONTACT);
                mAllFragment.setFilter(filter, false);
            }
            mContactListFilterController.setContactListFilter(filter, true);
        }
    }

    private final class CheckBoxListListener implements OnCheckBoxListActionListener {
        @Override
        public void onStartDisplayingCheckBoxes() {
            Log.d(TAG, "[onStartDisplayingCheckBoxes]");
            mActionBarAdapter.setSelectionMode(true);
            invalidateOptionsMenu();
        }

        @Override
        public void onSelectedContactIdsChanged() {
            Log.d(TAG, "[onSelectedContactIdsChanged]size = "
                    + mAllFragment.getSelectedContactIds().size());
            mActionBarAdapter.setSelectionCount(mAllFragment.getSelectedContactIds().size());
            invalidateOptionsMenu();
        }

        @Override
        public void onStopDisplayingCheckBoxes() {
            Log.d(TAG, "[onStopDisplayingCheckBoxes]");
            mActionBarAdapter.setSelectionMode(false);
        }
    }

    private class ContactsUnavailableFragmentListener
            implements OnContactsUnavailableActionListener {
        ContactsUnavailableFragmentListener() {
        }

        @Override
        public void onCreateNewContactAction() {
            Log.d(TAG, "[onCreateNewContactAction]");
            ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this,
                    EditorIntents.createCompactInsertContactIntent());
        }

        @Override
        public void onAddAccountAction() {
            Log.d(TAG, "[onAddAccountAction]");
            final Intent intent = ImplicitIntentsUtil.getIntentForAddingAccount();
            ImplicitIntentsUtil.startActivityOutsideApp(PeopleActivity.this, intent);
        }

        @Override
        public void onImportContactsFromFileAction() {
            //showImportExportDialogFragment();
            Log.d(TAG, "[onImportContactsFromFileAction]");
            /** M: New Feature.use mtk importExport function,use the
             * encapsulate class do this.@{*/
            ActivitiesUtils.doImportExport(PeopleActivity.this);
            /** @} */
        }
    }

    private final class StrequentContactListFragmentListener
            implements ContactTileListFragment.Listener {
        StrequentContactListFragmentListener() {
        }

        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(contactUri,
                    QuickContactActivity.MODE_FULLY_EXPANDED);
            intent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenType.FAVORITES);
            ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            // No need to call phone number directly from People app.
            Log.w(TAG, "unexpected invocation of onCallNumberDirectly()");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "[onCreateOptionsMenu]   mActionBarAdapter.isSearchMode() = " +
                mActionBarAdapter.isSearchMode() +
                "   mActionBarAdapter.isSelectionMode() = " + mActionBarAdapter.isSelectionMode());

        if (/*!areContactsAvailable()*/false) {//prize-change-huangliemin-2016-7-4
            Log.i(TAG, "[onCreateOptionsMenu]contacts aren't available, hide all menu items");
            // If contacts aren't available, hide all menu items.
            /// M:Fix option menu disappearance issue when change language. @{
            mOptionsMenuContactsAvailable = false;
            /// @}
            /// M: ALPS03277435 fix menu still show when no contacts available
            menu.close();
            /// @}
            return false;
        }
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        // prize modify for lhp by zhaojian 20180421 start
        //inflater.inflate(R.menu.people_options, menu);
        if (hasSystemFeature) {
            inflater.inflate(R.menu.prize_people_options, menu);
        } else {
            inflater.inflate(R.menu.people_options, menu);
        }
        // prize modify for lhp by zhaojian 20180421 end

        /// M: Op01 will add "show sim capacity" item
        ExtensionManager.getInstance().getOp01Extension().addOptionsMenu(this, menu);

        /// M:OP01 RCS will add people menu item
        ExtensionManager.getInstance().getRcsExtension().addPeopleMenuOptions(menu);

        PDebug.End("onCreateOptionsMenu");
        return true;
    }

    private void invalidateOptionsMenuIfNeeded() {
        if (isOptionsMenuChanged()) {
            invalidateOptionsMenu();
        }
    }

    public boolean isOptionsMenuChanged() {
        if (mOptionsMenuContactsAvailable != areContactsAvailable()) {
            return true;
        }

        if (mAllFragment != null && mAllFragment.isOptionsMenuChanged()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "[onPrepareOptionsMenu]");
        PDebug.Start("onPrepareOptionsMenu");
        mPrizeAnimationHelper.hideInputMethod();//prize-add-fix bug[41530]-hpf-2017-11-2
        /// M: Fix ALPS01612926,smartbook issue @{
        if (mActionBarAdapter == null) {
            Log.w(TAG, "[onPrepareOptionsMenu]mActionBarAdapter is null,return..");
            return true;
        }
        /// @}
        mOptionsMenuContactsAvailable = areContactsAvailable();
        if (/*!mOptionsMenuContactsAvailable*/false) {//prize-change-huangliemin-2016-7-4
            Log.w(TAG, "[onPrepareOptionsMenu]areContactsAvailable is false,return..");
            return false;
        }
        
        /*prize-add-for-hide-menu-huangliemin-2016-7-13-start*/
        if (mActionBarAdapter.isSearchMode() || mActionBarAdapter.isSelectionMode()) {
            return false;
        }
        /*prize-add-for-hide-menu-huangliemin-2016-7-13-end*/
        // Get references to individual menu items in the menu
        final MenuItem contactsFilterMenu = menu.findItem(R.id.menu_contacts_filter);

        /** M: New Feature @{ */
        final MenuItem groupMenu = menu.findItem(R.id.menu_groups);
        /** @} */
        /// M: [VoLTE ConfCall]
        final MenuItem conferenceCallMenu = menu.findItem(R.id.menu_conference_call);


        final MenuItem clearFrequentsMenu = menu.findItem(R.id.menu_clear_frequents);
        final MenuItem helpMenu = menu.findItem(R.id.menu_help);
        /*prize-add-huangliemin-2016-7-2-start*/
        //final MenuItem addContactsMenu = menu.findItem(R.id.menu_new_contacts);
        /*prize-add-huangliemin-2016-7-2-end*/

        final boolean isSearchOrSelectionMode = mActionBarAdapter.isSearchMode()
                || mActionBarAdapter.isSelectionMode();
        if (isSearchOrSelectionMode) {
            contactsFilterMenu.setVisible(false);
            clearFrequentsMenu.setVisible(false);
            helpMenu.setVisible(false);
            /** M: New Feature @{ */
            groupMenu.setVisible(false);
            /** @} */
            /// M: [VoLTE ConfCall]
            conferenceCallMenu.setVisible(false);
            //prize-add-huangliemin-2016-7-2-start
            //addContactsMenu.setVisible(false);
            //prize-add-huangliemin-2016-7-2-end
        } else {
            
            /* prize remove viewpager zhangzhonghao 20160304 start */
//            switch (getTabPositionForTextDirection(mActionBarAdapter.getCurrentTab())) {
//                case TabState.FAVORITES:
//                    contactsFilterMenu.setVisible(false);
//                    clearFrequentsMenu.setVisible(hasFrequents());
//                    break;
//                case TabState.ALL:
            //prize-change-huangliemin-2016-7-2-start
            //contactsFilterMenu.setVisible(true);
            contactsFilterMenu.setVisible(true);
            //prize-change-huangliemin-2016-7-2-end
            clearFrequentsMenu.setVisible(false);
            //prize-add-huangliemin-2016-7-2-start
            //addContactsMenu.setVisible(true);
            //prize-add-huangliemin-2016-7-2-end
//                    break;
//                default:
//                     break;
//            }
            /* prize remove viewpager zhangzhonghao 20160304 end */

            helpMenu.setVisible(HelpUtils.isHelpAndFeedbackAvailable());
        }
        final boolean showMiscOptions = !isSearchOrSelectionMode;
        final boolean showBlockedNumbers = PhoneCapabilityTester.isPhone(this)
                && ContactsUtils.FLAG_N_FEATURE
                && BlockedNumberContract.canCurrentUserBlockNumbers(this)
                /// M: Op01 will use special black list instead of google defaullt feature
                && !ExtensionManager.getInstance().getOp01Extension()
                .areContactAvailable(ProviderStatus.STATUS_NORMAL);
        makeMenuItemVisible(menu, R.id.menu_search,  /*showMiscOptions*/false);//prize-change-huangliemin-2016-7-2
        makeMenuItemVisible(menu, R.id.menu_import_export,
                /*showMiscOptions*/false && ActivitiesUtils.showImportExportMenu(this));//prize-change-huangliemin-2016-7-2
        makeMenuItemVisible(menu, R.id.menu_accounts, /*showMiscOptions*/false);//prize-change-huangliemin-2016-7-2
        //makeMenuItemVisible(menu, R.id.menu_blocked_numbers, showMiscOptions && showBlockedNumbers);//prize-remove-huangpengfei-2016-11-14
        makeMenuItemVisible(menu, R.id.menu_settings,
                /*showMiscOptions && !ContactsPreferenceActivity.isEmpty(this)*/true);//prize-change-huangliemin-2016-7-2

        /* prize change select menu zhangzhonghao 20160314 start */
        final boolean showSelectedContactOptions = false;
        /* prize change select menu zhangzhonghao 20160314 end */
//        final boolean showSelectedContactOptions = mActionBarAdapter.isSelectionMode()
//                && mAllFragment.getSelectedContactIds().size() != 0;
        /* prize change select menu zhangzhonghao 20160314 end */

        makeMenuItemVisible(menu, R.id.menu_share, showSelectedContactOptions);
        makeMenuItemVisible(menu, R.id.menu_delete, /*showSelectedContactOptions*/true);//prize-change-huangliemin-2016-7-2

        // prize modify for lhp by zhaojian 20180421 start
        if (hasSystemFeature) {
            makeMenuItemVisible(menu, R.id.menu_new_contact, true);
        }
        // prize modify for lhp by zhaojian 20180421 end

        final boolean showLinkContactsOptions = mActionBarAdapter.isSelectionMode()
                && mAllFragment.getSelectedContactIds().size() > 1;
        makeMenuItemVisible(menu, R.id.menu_join, showLinkContactsOptions);

        // Debug options need to be visible even in search mode.
        makeMenuItemVisible(menu, R.id.export_database, mEnableDebugMenuOptions &&
                hasExportIntentHandler());

        /// M: [VoLTE ConfCall] @{
        if (!VolteUtils.isVoLTEConfCallEnable(this)) {
            conferenceCallMenu.setVisible(false);
        }
        /// @}

        /// M: add for A1 @ {
        /*prize-change-huangliemin-2016-7-2-start*/
//        if (SystemProperties.get("ro.mtk_a1_feature").equals("1")) {
//            Log.i(TAG, "[onPrepareOptionsMenu]enable a1 feature.");
        groupMenu.setVisible(false);//prize-huangliemin-2016-7-4
//        }
        /// @ }
        /*prize-change-huangliemin-2016-7-2-end*/
        return true;
    }

    private boolean hasExportIntentHandler() {
        final Intent intent = new Intent();
        intent.setAction("com.android.providers.contacts.DUMP_DATABASE");
        final List<ResolveInfo> receivers = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return receivers != null && receivers.size() > 0;
    }

    /**
     * Returns whether there are any frequently contacted people being displayed
     *
     * @return
     */
    private boolean hasFrequents() {
//        return mFavoritesFragment.hasFrequents(); zhangzhonghao remove viewpager 20160304
        return false;
    }

    private void makeMenuItemVisible(Menu menu, int itemId, boolean visible) {
        final MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(visible);
        }
    }

    private void makeMenuItemEnabled(Menu menu, int itemId, boolean visible) {
        final MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setEnabled(visible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "[onOptionsItemSelected] mDisableOptionItemSelected = "
                + mDisableOptionItemSelected);
        if (mDisableOptionItemSelected) {
            return false;
        }

        switch (item.getItemId()) {
            case android.R.id.home: {
                // The home icon on the action bar is pressed
                if (mActionBarAdapter.isUpShowing()) {
                    // "UP" icon press -- should be treated as "back".
                    onBackPressed();
                }
                return true;
            }
            case R.id.menu_settings: {
                startActivity(new Intent(this, ContactsPreferenceActivity.class));
                return true;
            }
            case R.id.menu_contacts_filter: {
                AccountFilterUtil.startAccountFilterActivityForResult(
                        this, SUBACTIVITY_ACCOUNT_FILTER,
                        mContactListFilterController.getFilter());
                return true;
            }
            case R.id.menu_search: {
                onSearchRequested();
                return true;
            }
            case R.id.menu_share:
                shareSelectedContacts();
                return true;
            case R.id.menu_join:
                joinSelectedContacts();
                return true;
            case R.id.menu_delete:
            	/*prize-change-huangliemin-2016-7-2-start*/
                //deleteSelectedContacts();
                ActivitiesUtils.deleteContact(this);
                /*prize-change-huangliemin-2016-7-2-end*/
                return true;
            case R.id.menu_import_export: {
                //Android default code
                //showImportExportDialogFragment();
                //return true;
                /** M: Change Feature */
                return ActivitiesUtils.doImportExport(this);
            }
            case R.id.menu_clear_frequents: {
                ClearFrequentsDialog.show(getFragmentManager());
                return true;
            }
            case R.id.menu_help:
                HelpUtils.launchHelpAndFeedbackForMainScreen(this);
                return true;
            case R.id.menu_accounts: {
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{
                        ContactsContract.AUTHORITY
                });
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                ImplicitIntentsUtil.startActivityInAppIfPossible(this, intent);
                return true;
            }
            /*prize-remove-huangpengfei-2016-11-14-start*/
           /* case R.id.menu_blocked_numbers: {
                final Intent intent = TelecomManagerUtil.createManageBlockedNumbersIntent(
                        (TelecomManager) getSystemService(Context.TELECOM_SERVICE));
                if (intent != null) {
                    startActivity(intent);
                }
                return true;
            }*/
            /*prize-remove-huangpengfei-2016-11-14-end*/
            case R.id.export_database: {
                final Intent intent = new Intent("com.android.providers.contacts.DUMP_DATABASE");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                ImplicitIntentsUtil.startActivityOutsideApp(this, intent);
                return true;
            }
            /** M: New feature @{ */
            /** M: Group related */
            case R.id.menu_groups: {
                startActivity(new Intent(PeopleActivity.this, GroupBrowseActivity.class));
                return true;
            }
            /** @} */
            /** M: [VoLTE ConfCall]Conference call @{*/
            case R.id.menu_conference_call: {
                Log.d(TAG, "[onOptionsItemSelected]menu_conference_call");
                return ActivitiesUtils.conferenceCall(this);
            }
            /** @} */
            /*prize-add-huangliemin-2016-7-2-start*/
            /*case R.id.menu_new_contacts: {
            	 Log.d(TAG,"[onOptionsItemSelected]menu_new_contacts");
            	Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    intent.putExtras(extras);
                }
                try {
                    ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(PeopleActivity.this, R.string.missing_app,
                            Toast.LENGTH_SHORT).show();
                }
            	return true;
            }*/
            /*prize-add-huangliemin-2016-7-2-end*/

            // prize modify for lhp by zhaojian 20180421 start
            case R.id.menu_new_contact:
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    intent.putExtras(extras);
                }
                try {
                    ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(PeopleActivity.this, R.string.missing_app,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            // prize modify for lhp by zhaojian 20180421 end
        }
        return false;
    }

    private void showImportExportDialogFragment() {
        /*final boolean isOnFavoriteTab = mTabPagerAdapter.mCurrentPrimaryItem == mFavoritesFragment;
        if (isOnFavoriteTab) {
            ImportExportDialogFragment.show(getFragmentManager(), areContactsAvailable(),
                    PeopleActivity.class, ImportExportDialogFragment.EXPORT_MODE_FAVORITES);
        } else {
            ImportExportDialogFragment.show(getFragmentManager(), areContactsAvailable(),
                    PeopleActivity.class, ImportExportDialogFragment.EXPORT_MODE_ALL_CONTACTS);
        }*/
    }

    @Override
    public boolean onSearchRequested() { // Search key pressed.
        Log.d(TAG, "[onSearchRequested]");
        if (!mActionBarAdapter.isSelectionMode()) {
            mActionBarAdapter.setSearchMode(true);
        }
        return true;
    }

    /**
     * Share all contacts that are currently selected in mAllFragment. This method is pretty
     * inefficient for handling large numbers of contacts. I don't expect this to be a problem.
     */
    private void shareSelectedContacts() {
        Log.d(TAG, "[shareSelectedContacts],set ARG_CALLING_ACTIVITY.");

        final StringBuilder uriListBuilder = new StringBuilder();
        for (Long contactId : mAllFragment.getSelectedContactIds()) {
            final Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            final Uri lookupUri = Contacts.getLookupUri(getContentResolver(), contactUri);
            if (lookupUri == null) {
                continue;
            }
            final List<String> pathSegments = lookupUri.getPathSegments();
            if (pathSegments.size() < 2) {
                continue;
            }
            final String lookupKey = pathSegments.get(pathSegments.size() - 2);
            if (uriListBuilder.length() > 0) {
                uriListBuilder.append(':');
            }
            uriListBuilder.append(Uri.encode(lookupKey));
        }
        if (uriListBuilder.length() == 0) {
            return;
        }
        final Uri uri = Uri.withAppendedPath(
                Contacts.CONTENT_MULTI_VCARD_URI,
                Uri.encode(uriListBuilder.toString()));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        intent.putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY,
                PeopleActivity.class.getName());
        /*prize-change-huangliemin-2016-6-8-start*/
        //ImplicitIntentsUtil.startActivityOutsideApp(this, intent);
        final CharSequence chooseTitle = getResources().getString(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);
        try {
            startActivity(chooseIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
        }
        /*prize-change-huangliemin-2016-6-8-end*/
    }

    private void joinSelectedContacts() {
        Log.d(TAG, "[joinSelectedContacts]");
        JoinContactsDialogFragment.start(this, mAllFragment.getSelectedContactIds());
    }

    @Override
    public void onContactsJoined() {
        Log.d(TAG, "[onContactsJoined]");
        mActionBarAdapter.setSelectionMode(false);
        /*prize-add-huangliemin-2016-6-29-start*/
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        /*prize-add-huangliemin-2016-6-29-end*/
    }

    private void deleteSelectedContacts() {
        Log.d(TAG, "[deleteSelectedContacts]...");
        ContactMultiDeletionInteraction.start(PeopleActivity.this,
                mAllFragment.getSelectedContactIds());
    }

    @Override
    public void onDeletionFinished() {
        Log.d(TAG, "[onDeletionFinished]");
        mActionBarAdapter.setSelectionMode(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "[onActivityResult]requestCode = " + requestCode
                + ",resultCode = " + resultCode);
        switch (requestCode) {
            case SUBACTIVITY_ACCOUNT_FILTER: {
                AccountFilterUtil.handleAccountFilterResult(
                        mContactListFilterController, resultCode, data);
                break;
            }

            // TODO: Using the new startActivityWithResultFromFragment API this should not be needed
            // anymore
            case ContactEntryListFragment.ACTIVITY_REQUEST_CODE_PICKER:
                if (resultCode == RESULT_OK) {
                    mAllFragment.onPickerResult(data);
                }

// TODO fix or remove multipicker code
//                else if (resultCode == RESULT_CANCELED && mMode == MODE_PICK_MULTIPLE_PHONES) {
//                    // Finish the activity if the sub activity was canceled as back key is used
//                    // to confirm user selection in MODE_PICK_MULTIPLE_PHONES.
//                    finish();
//                }
//                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO move to the fragment

        // Bring up the search UI if the user starts typing
        final int unicodeChar = event.getUnicodeChar();
        if ((unicodeChar != 0)
                // If COMBINING_ACCENT is set, it's not a unicode character.
                && ((unicodeChar & KeyCharacterMap.COMBINING_ACCENT) == 0)
                && !Character.isWhitespace(unicodeChar)) {
            if (mActionBarAdapter.isSelectionMode()) {
                // Ignore keyboard input when in selection mode.
                return true;
            }
            String query = new String(new int[]{unicodeChar}, 0, 1);
            if (!mActionBarAdapter.isSearchMode()) {
                mActionBarAdapter.setSearchMode(true);
                mActionBarAdapter.setQueryString(query);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "[onBackPressed]");
        if (!isSafeToCommitTransactions()) {
            return;
        }

        if (mActionBarAdapter.isSelectionMode()) {
            mActionBarAdapter.setSelectionMode(false);
            mAllFragment.displayCheckBoxes(false);
            /*prize-change for dido os8.0-hpf-2017-8-15-start*/
        } else if (/*mActionBarAdapter.isSearchMode()*/mIsSearchModle) {
            //mActionBarAdapter.setSearchMode(false);
            mPrizeAnimationHelper.hideInputMethod();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    onAction(ActionBarAdapter.Listener.Action.STOP_SEARCH_AND_SELECTION_MODE);
                    mIsSearchModle = false;
                }
            }, 300);
        	
            /*prize-change for dido os8.0-hpf-2017-8-15-end*/
            if (mAllFragment.wasSearchResultClicked()) {
                mAllFragment.resetSearchResultClicked();
            } else {
                Logger.logScreenView(this, ScreenType.SEARCH_EXIT);
                Logger.logSearchEvent(mAllFragment.createSearchState());
            }
            /** M: New Feature @{ */
        } else if (!ContactsSystemProperties.MTK_PERF_RESPONSE_TIME && isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
            /** @} */
        } else if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()) {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.superpowersave",
                    "com.android.superpowersave.SuperPowerActivity");
            intent.setComponent(cn);
            startActivity(intent);  
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mActionBarAdapter.onSaveInstanceState(outState);
        /*prize-add-huangliemin-2016-6-8-start*/
        isSelectMode = mActionBarAdapter.isSelectionMode();
        outState.putBoolean(KEY_SELECT_MODE, isSelectMode);
        /*prize-add-huangliemin-2016-6-8-end*/
        isPrizeSearchMode = mActionBarAdapter.isSearchMode();
        outState.putBoolean(KEY_SEARCH_MODE, isPrizeSearchMode);
        // Clear the listener to make sure we don't get callbacks after onSaveInstanceState,
        // in order to avoid doing fragment transactions after it.
        // TODO Figure out a better way to deal with the issue.
        mDisableOptionItemSelected = true;
        mActionBarAdapter.setListener(null);
        
        /* prize remove viewpager zhangzhonghao 20160304 start */
//        if (mTabPager != null) {
//            mTabPager.setOnPageChangeListener(null);
//        }
        /* prize remove viewpager zhangzhonghao 20160304 end */

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // In our own lifecycle, the focus is saved and restore but later taken away by the
        // ViewPager. As a hack, we force focus on the SearchView if we know that we are searching.
        // This fixes the keyboard going away on screen rotation
        if (mActionBarAdapter.isSearchMode()) {
            mActionBarAdapter.setFocusOnSearchView();
        }
        /*prize-add-huangliemin-2016-6-8-start*/
        isSelectMode = savedInstanceState.getBoolean(KEY_SELECT_MODE, false);
        /*prize-add-huangliemin-2016-6-8-end*/
        isPrizeSearchMode = savedInstanceState.getBoolean(KEY_SEARCH_MODE, false);
    }

    @Override
    public DialogManager getDialogManager() {
        return mDialogManager;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_action_button:
                Log.d(TAG, "[onClick]floating_action_button");
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    intent.putExtras(extras);
                }
                try {
                    ImplicitIntentsUtil.startActivityInApp(PeopleActivity.this, intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(PeopleActivity.this, R.string.missing_app,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            /// M: Add for SelectAll/DeSelectAll Feature. @{
            case R.id.selection_count_text:
                Log.d(TAG, "[onClick]selection_count_text");
                // if the Window of this Activity hasn't been created,
                // don't show Popup. because there is no any window to attach .
                if (getWindow() == null) {
                    Log.w(TAG, "[onClick]current Activity window is null");
                    return;
                }
                if (mSelectionMenu == null || !mSelectionMenu.isShown()) {
                    View parent = (View) view.getParent();
                    mSelectionMenu = updateSelectionMenu(parent);
                    mSelectionMenu.show();
                } else {
                    Log.w(TAG, "mSelectionMenu is already showing, ignore this click");
                }
                break;
            /// @}
            
            /* prize add to move to dialer zhangzhonghao 20160307 start */               
           /* case R.id.prize_contacts_dialer:
                Intent intent1 = new Intent("android.intent.action.DIAL");
                intent1.addCategory("android.intent.category.DEFAULT");
                startActivity(intent1);
                break;*/  //prize modification huangpengfei 20160809

            case R.id.prize_contacts_dialer_layout:
                Log.i("logtest", "log");
                Intent intent2 = new Intent("android.intent.action.DIAL");
                intent2.addCategory("android.intent.category.DEFAULT");
                startActivity(intent2);
                break;
            case R.id.prize_contacts_others:
                mAllFragment.getCheckBoxListener().onStartDisplayingCheckBoxes();
                break;

           /* prize add to move to dialer zhangzhonghao 20160307 end */

            default:
                Log.wtf(TAG, "Unexpected onClick event from " + view);
        }
    }

    /**
     * Returns the tab position adjusted for the text direction.
     */
    private int getTabPositionForTextDirection(int position) {
        if (isRTL()) {
            return TabState.COUNT - 1 - position;
        }
        return position;
    }

    /// M: Add for SelectAll/DeSelectAll Feature. @{
    private DropDownMenu mSelectionMenu;

    /**
     * add dropDown menu on the selectItems.The menu is "Select all" or
     * "Deselect all"
     *
     * @param customActionBarView
     * @return The updated DropDownMenu
     */
    private DropDownMenu updateSelectionMenu(View customActionBarView) {
        Log.d(TAG, "[updateSelectionMenu]");
        DropMenu dropMenu = new DropMenu(this);
        // new and add a menu.
        DropDownMenu selectionMenu = dropMenu.addDropDownMenu(
                (Button) customActionBarView.findViewById(R.id.selection_count_text),
                R.menu.mtk_selection);

        Button selectView = (Button) customActionBarView.findViewById(R.id.selection_count_text);
        // when click the selectView button, display the dropDown menu.
        selectView.setOnClickListener(this);
        MenuItem item = selectionMenu.findItem(R.id.action_select_all);

        // get mIsSelectedAll from fragment.
        mAllFragment.updateSelectedItemsView();
        //the menu will show "Deselect_All/ Select_All".
        if (mAllFragment.isSelectedAll()) {
            // dropDown menu title is "Deselect all".
            item.setTitle(R.string.menu_select_none);
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    // clear select all items
                    mAllFragment.updateCheckBoxState(false);
                    mAllFragment.displayCheckBoxes(true); //zhangzhonghao 20160412
                    mActionBarAdapter.setSelectionMode(true); //zhangzhonghao 20160314
                    initializeFabVisibility();
                    return true;
                }
            });
        } else {
            // dropDown Menu title is "Select all"
            item.setTitle(R.string.menu_select_all);
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    mAllFragment.updateCheckBoxState(true);
                    mAllFragment.displayCheckBoxes(true);
                    return true;
                }
            });
        }
        return selectionMenu;
    }
    /// @}

    /// M: Listen sim change intent @{
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "[onReceive] Received Intent:" + intent);
            // M:refactor menu show wrong postion in airMode
            closeAllMenuIfOpen();

            updateViewConfiguration(true);
            updateFragmentsVisibility();
        }
    };
    /// @}

    /// M: refactor "select all" menu ,optionMenu show wrong position in airMode ALPS02454655,
    /// ALPS02477744 @{
    private Menu mOptionMenu = null;

    private void closeAllMenuIfOpen() {
        Log.i(TAG, "[closeAllMenuIfOpen]");
        if (mSelectionMenu != null && mSelectionMenu.isShown()) {
            Log.i(TAG, "[closeAllMenuIfOpen] select All menu is dismiss!");
            mSelectionMenu.dismiss();
        }
        if (mOptionMenu != null) {
            Log.i(TAG, "[closeAllMenuIfOpen] close mOptionMenu if open!");
            mOptionMenu.close();
        }
    }
    /// @}

    /*PRIZE-add-yuandailin-2016-6-1-start*/
    private void showDialogForNoContactsSelected() {
        AlertDialog mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.no_contacts_is_selected)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        mDialog.show();
    }
	/*PRIZE-add-yuandailin-2016-6-1-end*/

    /*PRIZE-add for dido os8.0-hpf-2017-8-14-start*/
    private boolean mIsSearchModle = false;
    private String mQueryString;

    private void configPrizeSearchModle() {
        View prizeSearchBoxBackBtn = findViewById(R.id.prize_search_box_back_button);
        prizeSearchBoxBackBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsSearchModle) {
                    mIsSearchModle = false;
                    mPrizeAnimationHelper.hideInputMethod();
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onAction(ActionBarAdapter.Listener.Action.STOP_SEARCH_AND_SELECTION_MODE);
                        }
                    }, 300);
                }
            }
        });

        mSearchBoxEditText.addTextChangedListener(new PrizeSearchTextWatcher());

    }


    private class PrizeSearchTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence queryString, int start, int before, int count) {
            Log.d(TAG, "[onTextChanged] queryString = " + queryString);
            if ("".equals(queryString.toString()) || queryString == null) {
                mSearchBoxClearBtn.setVisibility(View.GONE);
            } else {
                mSearchBoxClearBtn.setVisibility(View.VISIBLE);
            }
            if (queryString.equals(mQueryString)) {
                return;
            }
            mQueryString = queryString.toString();
            if (!mIsSearchModle) {
                if (!TextUtils.isEmpty(queryString)) {
                    mIsSearchModle = true;
                }
            } else {
                PeopleActivity.this.onAction(ActionBarAdapter.Listener.Action.CHANGE_SEARCH_QUERY);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }
    /*PRIZE-add for dido os8.0-hpf-2017-8-14-start*/

}
