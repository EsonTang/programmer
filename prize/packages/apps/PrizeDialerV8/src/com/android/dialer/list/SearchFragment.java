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
package com.android.dialer.list;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;//PRIZE-add-yuandailin-2016-4-20
import android.widget.ListView;
import android.widget.Space;

import com.android.contacts.common.list.ContactEntryListAdapter;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.common.util.Constants;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.common.util.PhoneNumberHelper;
import com.android.contacts.common.util.ViewUtil;
import com.android.contacts.commonbind.analytics.AnalyticsUtil;
import com.android.dialer.DialtactsActivity;
import com.android.dialer.R;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.dialpad.DialpadFragment.ErrorDialogFragment;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.widget.DialpadSearchEmptyContentView;
import com.android.dialer.widget.EmptyContentView;
import com.android.phone.common.animation.AnimUtils;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.DialerFeatureOptions;
/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
import com.android.contacts.common.prize.PrizeBatteryBroadcastReciver;
import android.widget.Toast;
import android.content.IntentFilter;
/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/

public class SearchFragment extends PhoneNumberPickerFragment {
    private static final String TAG  = SearchFragment.class.getSimpleName();
    /// M: Add key for saving mAddToContactNumber value when switching to landscape mode
    private static final String KEY_ADD_TO_CONTACT_NUMBER = "addToContactNumber";
    /*prize  add  for  58469 -longzhongping-2018.05.25-start*/
    private static final String PRIZE_CONTACTS_PACKAGE_NAME = "com.android.contacts";
    private static final String PRIZE_CONTACTS_EDITOR_ACTIVITY_NAME = "com.android.contacts.activities.ContactEditorActivity";
    private static final String PRIZE_CONTACTS_SELECTION_ACTIVITY_NAME = "com.android.contacts.activities.ContactSelectionActivity";
    /*prize  add  for  58469 -longzhongping-2018.05.25-end*/
    private OnListFragmentScrolledListener mActivityScrollListener;
    private View.OnTouchListener mActivityOnTouchListener;
    private PrizeBatteryBroadcastReciver mPrizeBatteryBroadcastReciver; //prize-add huangpengfei-2017-6-26
    /*
     * Stores the untouched user-entered string that is used to populate the add to contacts
     * intent.
     */
    private String mAddToContactNumber;
    private int mActionBarHeight;
    private int mShadowHeight;
    private int mPaddingTop;
    private int mShowDialpadDuration;
    private int mHideDialpadDuration;

    /**
     * Used to resize the list view containing search results so that it fits the available space
     * above the dialpad. Does not have a user-visible effect in regular touch usage (since the
     * dialpad hides that portion of the ListView anyway), but improves usability in accessibility
     * mode.
     */
    private Space mSpacer;

    private HostInterface mActivity;

    protected EmptyContentView mEmptyView;

    public interface HostInterface {
        public boolean isActionBarShowing();
        public boolean isDialpadShown();
        public int getDialpadHeight();
        public int getActionBarHideOffset();
        public int getActionBarHeight();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ///M: It is for fix bug ALPS03333590 {
        mActivity = (HostInterface)activity;
        ///@}

        setQuickContactEnabled(true);
        setAdjustSelectionBoundsEnabled(false);
        setDarkTheme(false);
        setPhotoPosition(ContactListItemView.getDefaultPhotoPosition(false /* opposite */));
        setUseCallableUri(true);

        try {
            mActivityScrollListener = (OnListFragmentScrolledListener) activity;
        } catch (ClassCastException e) {
            Log.d(TAG, activity.toString() + " doesn't implement OnListFragmentScrolledListener. " +
                    "Ignoring.");
        }
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mPrizeBatteryBroadcastReciver = new PrizeBatteryBroadcastReciver();
        activity.registerReceiver(mPrizeBatteryBroadcastReciver, intentFilter);
        /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
        
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSearchMode()) {
            getAdapter().setHasHeader(0, false);
        }

        mActivity = (HostInterface) getActivity();

        final Resources res = getResources();
        mActionBarHeight = mActivity.getActionBarHeight();
        mShadowHeight  = res.getDrawable(R.drawable.search_shadow).getIntrinsicHeight();
        mPaddingTop = res.getDimensionPixelSize(R.dimen.search_list_padding_top);
        mShowDialpadDuration = res.getInteger(R.integer.dialpad_slide_in_duration);
        mHideDialpadDuration = res.getInteger(R.integer.dialpad_slide_out_duration);

        final View parentView = getView();

        final ListView listView = getListView();

        if (mEmptyView == null) {
            if (this instanceof SmartDialSearchFragment) {
                mEmptyView = new DialpadSearchEmptyContentView(getActivity());
            } else {
                mEmptyView = new EmptyContentView(getActivity());
            }
            ((ViewGroup) getListView().getParent()).addView(mEmptyView);
            getListView().setEmptyView(mEmptyView);
            setupEmptyView();
        }

        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*listView.setBackgroundColor(res.getColor(R.color.background_dialer_results));*/
        listView.setBackgroundColor(res.getColor(R.color.prize_dialer_phone_number_list_item_bg_color));
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        listView.setClipToPadding(false);
        setVisibleScrollbarEnabled(false);

        //Turn of accessibility live region as the list constantly update itself and spam messages.
        listView.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_NONE);
        ContentChangedFilter.addToParent(listView);

        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mActivityScrollListener != null) {
                    mActivityScrollListener.onListFragmentScrollStateChange(scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            }
        });
        if (mActivityOnTouchListener != null) {
            listView.setOnTouchListener(mActivityOnTouchListener);
        }

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        ((DialerPhoneNumberListAdapter) getAdapter()).setIDialerSearchItemViewActionListener(new DialerPhoneNumberListAdapter.IDialerSearchItemViewActionListener() {
            @Override
            public void sendMessage(int position) {
                String number = getPhoneNumber(position);
                Intent intent = IntentUtil.getSendSmsIntent(number);
                DialerUtils.startActivityWithErrorToast(getActivity(), intent);
            }

            @Override
            public void callPhone(int position) {
                if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                    PhoneAccountHandle phoneAccountHandle = ((DialerPhoneNumberListAdapter) getAdapter())
                            .getSuggestPhoneAccountHandle(position);
                    String number = getPhoneNumber(position);
                    IntentProvider intentProvider = IntentProvider
                            .getSuggestedReturnCallIntentProvider(number, phoneAccountHandle);
                    if (intentProvider != null) {
                        Intent intent = intentProvider.getIntent(getActivity());
                        if (intent != null) {
                            DialerUtils.startActivityWithErrorToast(getActivity(), intent);
                        }
                        // Clear dialpad and exit search ui while starting call
                        ((DialtactsActivity) getActivity()).setClearSearchOnPause(true);
                    }
                }
            }
        });
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        updatePosition(false /* animate */);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtil.addBottomPaddingToListViewForFab(getListView(), getResources());
        view.setBackgroundColor(getActivity().getResources().getColor(R.color.prize_dialer_white));
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animator = null;
        if (nextAnim != 0) {
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
        }
        if (animator != null) {
            final View view = getView();
            final int oldLayerType = view.getLayerType();
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setLayerType(oldLayerType, null);
                }
            });
        }
        return animator;
    }

    @Override
    protected void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
        // This hides the "All contacts with phone numbers" header in the search fragment
        final ContactEntryListAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setHasHeader(0, false);
        }
    }

    public void setAddToContactNumber(String addToContactNumber) {
        mAddToContactNumber = addToContactNumber;
    }

    /**
     * Return true if phone number is prohibited by a value -
     * (R.string.config_prohibited_phone_number_regexp) in the config files. False otherwise.
     */
    public boolean checkForProhibitedPhoneNumber(String number) {
        // Regular expression prohibiting manual phone call. Can be empty i.e. "no rule".
        String prohibitedPhoneNumberRegexp = getResources().getString(
            R.string.config_prohibited_phone_number_regexp);

        // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
        // test equipment.
        if (number != null
                && !TextUtils.isEmpty(prohibitedPhoneNumberRegexp)
                && number.matches(prohibitedPhoneNumberRegexp)) {
            Log.d(TAG, "The phone number is prohibited explicitly by a rule.");
            if (getActivity() != null) {
                DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                        R.string.dialog_phone_call_prohibited_message);
                dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
            }

            return true;
        }
        return false;
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
        DialerPhoneNumberListAdapter adapter = new DialerPhoneNumberListAdapter(getActivity());
        adapter.setDisplayPhotos(true);
        adapter.setUseCallableUri(super.usesCallableUri());
        adapter.setListener(this);
        return adapter;
    }

    @Override
    protected void onItemClick(int position, long id) {
        final DialerPhoneNumberListAdapter adapter = (DialerPhoneNumberListAdapter) getAdapter();
        final int shortcutType = adapter.getShortcutTypeFromPosition(position);
        final OnPhoneNumberPickerActionListener listener;
        final Intent intent;
        final String number;

        Log.i(TAG, "onItemClick: shortcutType=" + shortcutType);

        switch (shortcutType) {
            case DialerPhoneNumberListAdapter.SHORTCUT_INVALID:
                /// M: [Suggested Account] Supporting suggested account @{
                if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                    final PhoneAccountHandle phoneAccountHandle = adapter
                            .getSuggestPhoneAccountHandle(position);
                    number = getPhoneNumber(position);
                    IntentProvider intentProvider = IntentProvider
                            .getSuggestedReturnCallIntentProvider(number,
                                    phoneAccountHandle);
                    if (intentProvider != null) {
                        intent = intentProvider.getIntent(getActivity());
                        if (intent != null) {
                            DialerUtils.startActivityWithErrorToast(getActivity(),
                                    intent);
                        }
                        // Clear dialpad and exit search ui while starting call
                        ((DialtactsActivity)getActivity()).setClearSearchOnPause(true);
                    }
                } else {
                    super.onItemClick(position, id);
                }
                /// @}
                break;
            case DialerPhoneNumberListAdapter.SHORTCUT_DIRECT_CALL:
                number = adapter.getQueryString();
                listener = getOnPhoneNumberPickerListener();
                if (listener != null && !checkForProhibitedPhoneNumber(number)) {
                    listener.onPickPhoneNumber(number, false /* isVideoCall */,
                            getCallInitiationType(false /* isRemoteDirectory */));
                }
                break;
            case DialerPhoneNumberListAdapter.SHORTCUT_CREATE_NEW_CONTACT:
                number = TextUtils.isEmpty(mAddToContactNumber) ?
                        adapter.getFormattedQueryString() : mAddToContactNumber;
                intent = IntentUtil.getNewContactIntent(number);
                /*prize  add  for  58469 -longzhongping-2018.05.25-start*/
                intent.setClassName(PRIZE_CONTACTS_PACKAGE_NAME,PRIZE_CONTACTS_EDITOR_ACTIVITY_NAME);
                /*prize  add  for  58469 -longzhongping-2018.05.25-end*/
                DialerUtils.startActivityWithErrorToast(getActivity(), intent);
                break;
            case DialerPhoneNumberListAdapter.SHORTCUT_ADD_TO_EXISTING_CONTACT:
                /// M: [IMS Call] Save SIP URI to IMS Call field. @{
                if (DialerFeatureOptions.isImsCallSupport()
                        && PhoneNumberHelper.isUriNumber(adapter.getQueryString())) {
                    intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.putExtra(Intents.Insert.IMS_ADDRESS, adapter.getQueryString());
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    DialerUtils.startActivityWithErrorToast(getActivity(), intent,
                            R.string.add_contact_not_available);
                    break;
                }
                /// @}
                number = TextUtils.isEmpty(mAddToContactNumber) ?
                        adapter.getFormattedQueryString() : mAddToContactNumber;
                intent = IntentUtil.getAddToExistingContactIntent(number);
                /*prize  add  for  58469 -longzhongping-2018.05.25-start*/
                intent.setClassName(PRIZE_CONTACTS_PACKAGE_NAME,PRIZE_CONTACTS_SELECTION_ACTIVITY_NAME);
                 /*prize  add  for  58469 -longzhongping-2018.05.25-end*/
                DialerUtils.startActivityWithErrorToast(getActivity(), intent,
                        R.string.add_contact_not_available);
                break;
            case DialerPhoneNumberListAdapter.SHORTCUT_SEND_SMS_MESSAGE:
                number = adapter.getFormattedQueryString();
                intent = IntentUtil.getSendSmsIntent(number);
                DialerUtils.startActivityWithErrorToast(getActivity(), intent);
                break;
            case DialerPhoneNumberListAdapter.SHORTCUT_MAKE_VIDEO_CALL:
            	/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
             	if(PrizeBatteryBroadcastReciver.isBatteryLow){
             		android.widget.Toast.makeText(getActivity(), R.string.prize_video_call_attention, 
                             android.widget.Toast.LENGTH_SHORT).show();
             	}
             	/*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
             	
                number = TextUtils.isEmpty(mAddToContactNumber) ?
                        adapter.getQueryString() : mAddToContactNumber;
                listener = getOnPhoneNumberPickerListener();
                if (listener != null && !checkForProhibitedPhoneNumber(number)) {
                    listener.onPickPhoneNumber(number, true /* isVideoCall */,
                            getCallInitiationType(false /* isRemoteDirectory */));
                }
                break;
            /// M: [IMS Call] For SIP URI IMS Call @{
            case DialerPhoneNumberListAdapter.SHORTCUT_MAKE_IMS_CALL:
                Intent callIntent = IntentUtil.getCallIntent(
                        Uri.fromParts(PhoneAccount.SCHEME_TEL, adapter.getQueryString(), null),
                        getCallInitiationType(false), Constants.DIAL_NUMBER_INTENT_IMS);
                DialerUtils.startActivityWithErrorToast(getActivity(), callIntent);
                break;
            /// @}
        }
    }

    /**
     * Updates the position and padding of the search fragment, depending on whether the dialpad is
     * shown. This can be optionally animated.
     * @param animate
     */
    public void updatePosition(boolean animate) {
        // Use negative shadow height instead of 0 to account for the 9-patch's shadow.
        int startTranslationValue =
                mActivity.isDialpadShown() ? mActionBarHeight - mShadowHeight : -mShadowHeight;
        int endTranslationValue = 0;
        // Prevents ListView from being translated down after a rotation when the ActionBar is up.
        if (animate || mActivity.isActionBarShowing()) {
            endTranslationValue =
                    mActivity.isDialpadShown() ? 0 : mActionBarHeight - mShadowHeight;
        }
        if (animate) {
            // If the dialpad will be shown, then this animation involves sliding the list up.
            final boolean slideUp = mActivity.isDialpadShown();

            Interpolator interpolator = slideUp ? AnimUtils.EASE_IN : AnimUtils.EASE_OUT ;
            int duration = slideUp ? mShowDialpadDuration : mHideDialpadDuration;
            ///M: add for [ALPS03371473] {
            if (getView() == null) {
                Log.d(TAG, "In updatePosition, getView == null !");
                return;
            }
            /// @}
            getView().setTranslationY(startTranslationValue);
            getView().animate()
                    .translationY(endTranslationValue)
                    .setInterpolator(interpolator)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (!slideUp) {
                                resizeListView();
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (slideUp) {
                                resizeListView();
                            }
                        }
                    });

        } else {
            ///M: add for [ALPS03371473] {
            if (getView() == null) {
                Log.d(TAG, "In updatePosition, getView == null !");
                return;
            }
            /// @}
            getView().setTranslationY(endTranslationValue);
            resizeListView();
        }

        // There is padding which should only be applied when the dialpad is not shown.
        int paddingTop = mActivity.isDialpadShown() ? 0 : mPaddingTop;
        final ListView listView = getListView();
        listView.setPaddingRelative(
                listView.getPaddingStart(),
                paddingTop,
                listView.getPaddingEnd(),
                listView.getPaddingBottom());
    }

    public void resizeListView() {
        if (mSpacer == null) {
            return;
        }
        int spacerHeight = mActivity.isDialpadShown() ? mActivity.getDialpadHeight() : 0;
        if (spacerHeight != mSpacer.getHeight()) {
            /*PRIZE-change-yuandailin-2016-4-20-start*/
            /*final LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) mSpacer.getLayoutParams();*/
            final RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) mSpacer.getLayoutParams();
            /*PRIZE-change-yuandailin-2016-4-20-end*/
            lp.height = spacerHeight;
            mSpacer.setLayoutParams(lp);
        }
    }

    @Override
    protected void startLoading() {
        if (getActivity() == null) {
            return;
        }

        if (PermissionsUtil.hasContactsPermissions(getActivity())
              /**M:[MTK Dialer Search] to handle contact & phone permission separately.*/
              ||DialerFeatureOptions.isDialerSearchEnabled()) {
            super.startLoading();
        } else if (TextUtils.isEmpty(getQueryString())) {
            // Clear out any existing call shortcuts.
            final DialerPhoneNumberListAdapter adapter =
                    (DialerPhoneNumberListAdapter) getAdapter();
            adapter.disableAllShortcuts();
        } else {
            // The contact list is not going to change (we have no results since permissions are
            // denied), but the shortcuts might because of the different query, so update the
            // list.
            getAdapter().notifyDataSetChanged();
        }

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        ListView listView = getListView();
        if (null != listView) {
            Log.d("PrizeExpandView", "listView.requestFocus");
            listView.setFocusable(true);
            listView.setFocusableInTouchMode(true);
            listView.requestFocus();
            listView.requestFocusFromTouch();
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        setupEmptyView();
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mActivityOnTouchListener = onTouchListener;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        /*PRIZE-change-yuandailin-2016-4-20-start*/
        //final LinearLayout parent = (LinearLayout) super.inflateView(inflater, container);
        final RelativeLayout parent = (RelativeLayout) super.inflateView(inflater, container);
        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSpacer = new Space(getActivity());
            parent.addView(mSpacer,
                    //new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));
        /*PRIZE-change-yuandailin-2016-4-20-end*/
        }
        return parent;
    }

    protected void setupEmptyView() {}

    /** M: Add for saving mAddToContactNumbervalue when switching to landscape mode. @{ */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ADD_TO_CONTACT_NUMBER, mAddToContactNumber);
    }

    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);
        if (savedState == null) {
            return;
        }
        mAddToContactNumber = savedState.getString(KEY_ADD_TO_CONTACT_NUMBER);
    }
    /** @} */

    /**
     * M: For query presence after search info changed.
     */
    @Override
    public void setQueryString(String queryString, boolean delaySelection) {
        super.setQueryString(queryString, delaySelection);
        ExtensionManager.getInstance().getDialPadExtension()
                .onSetQueryString(queryString);
    }

    /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-start*/
    @Override
    public void onDetach() {
        super.onDetach();
       
        if(mPrizeBatteryBroadcastReciver != null){
        	getActivity().unregisterReceiver(mPrizeBatteryBroadcastReciver);
        }
    }
    /*prize-add for BatteryBroadcastReciver huangpengfei-2017-6-26-end*/
}
