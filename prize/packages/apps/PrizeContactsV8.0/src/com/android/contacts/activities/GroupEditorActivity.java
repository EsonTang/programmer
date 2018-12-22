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
 * limitations under the License
 */

package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;   //prize add for bug 54241 by zhaojian 20180402
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor.SaveMode;
import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.util.DialogManager;

import com.mediatek.contacts.ContactSaveServiceEx;
import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.activities.GroupBrowseActivity;
//import com.mediatek.contacts.activities.GroupBrowseActivity.AccountCategoryInfo;
import com.android.contacts.prize.PrizeGroupBrowseListFragment.AccountCategoryInfo;//prize-change-huangliemin-2016-7-6
import com.mediatek.contacts.group.SimGroupUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.simservice.SimGroupProcessor;
import com.mediatek.contacts.util.Log;

public class GroupEditorActivity extends ContactsActivity
        implements DialogManager.DialogShowingViewActivity, SimGroupProcessor.Listener {

    private static final String TAG = "GroupEditorActivity";

    public static final String ACTION_SAVE_COMPLETED = "saveCompleted";
    public static final String ACTION_ADD_MEMBER_COMPLETED = "addMemberCompleted";
    public static final String ACTION_REMOVE_MEMBER_COMPLETED = "removeMemberCompleted";

    private static final int SUBACTIVITY_DETAIL_GROUP = 1;
    private GroupEditorFragment mFragment;

    private DialogManager mDialogManager = new DialogManager(this);

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        /// M: Fix bug ALPS02855141, if not have permissions, should request it. @{
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG,"[onCreate]GroupEditorActivity not have permissions, return.");
            return;
        }
        /// @}

        /** M: New feature @{ */
        String action = setAccountCategoryInfo();
        /** @} */

        if (ACTION_SAVE_COMPLETED.equals(action)) {
            Log.w(TAG, "[onCreate] action is ACTION_SAVE_COMPLETED,finish activity.");
            finish();
            return;
        }
        /** M: Fixed CR ALPS00542175/ALPS01077147
         * Fix ALPS01466297 finish Activity if phb not ready @{
         */
        if ((mSubId >= 0) && ActivitiesUtils.checkPhoneBookReady(this, savedState, mSubId)) {
            return;
        }
        /** @} */
        /** M: Add for SIM Service refactory @{ */
        mHandler = ActivitiesUtils.initGroupHandler(this);
        /** @} */

        setContentView(R.layout.group_editor_activity);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Inflate a custom action bar that contains the "done" button for saving changes
            // to the group
        	/*prize-change-huangliemin-2016-7-29-start*/
//            LayoutInflater inflater = (LayoutInflater) getSystemService
//                    (Context.LAYOUT_INFLATER_SERVICE);
//            View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar,
//                    null);
//            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
//            saveMenuItem.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                /* prize change this function zhangzhonghao start */
////                    mFragment.onDoneClicked();
//					mFragment.revert();
//				/* prize change this function zhangzhonghao end */
//                }
//            });
//            // Show the custom action bar but hide the home icon and title
//            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
//                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
//                    ActionBar.DISPLAY_SHOW_TITLE);
//            actionBar.setCustomView(customActionBarView);
        	
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    		actionBar.setDisplayShowCustomEnabled(true);
    		actionBar.setDisplayHomeAsUpEnabled(false);
    		actionBar.setDisplayShowTitleEnabled(false);
    		actionBar.setDisplayUseLogoEnabled(false);
    		actionBar.setCustomView(R.layout.prize_custom_delete_contacts_actionbar);
    		TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
    		TitleText.setText(R.string.prize_add_group);
    		View BackButton = actionBar.getCustomView().findViewById(R.id.back_container);
    		TextView backTv = (TextView)actionBar.getCustomView().findViewById(R.id.back);
    		backTv.setText(R.string.back_button_label);
    		BackButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mFragment.revert();
				}
			});
    		
    		TextView SaveButton = (TextView)actionBar.getCustomView().findViewById(R.id.select_all);
    		SaveButton.setText(R.string.menu_done);
    		SaveButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mFragment.onDoneClicked();
					//mFragment.revert();
				}
			});
            /*prize-change-huangliemin-2016-7-29-end*/
        }

        mFragment = (GroupEditorFragment) getFragmentManager().findFragmentById(
                R.id.group_editor_fragment);
        mFragment.setListener(mFragmentListener);
        mFragment.setContentResolver(getContentResolver());

        // NOTE The fragment will restore its state by itself after orientation changes, so
        // we need to do this only for a new instance.
        if (savedState == null) {
            Uri uri = Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;
            /** M: New feature @{ */
            Log.d(TAG, " savedState == null mSubId : " + mSubId);
            mFragment.load(action, uri, getIntent().getExtras(), mSubId);
            /** @} */

        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (DialogManager.isManagedId(id)) {
            return mDialogManager.onCreateDialog(id, args);
        } else {
            // Nobody knows about the Dialog
            Log.w(TAG, "Unknown dialog requested, id: " + id + ", args: " + args);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        // If the change could not be saved, then revert to the default "back" button behavior.
        /** M: New feature CR ID :ALPS00228918 @{ */
        if (!mFragment.save(SaveMode.CLOSE, false)) {
            if (!mFragment.checkOnBackPressedState()) {
                super.onBackPressed();
            }
        }
        /** @} */
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (mFragment == null) {
            Log.w(TAG, "[onNewIntent] the mFragment is null,return.");
            return;
        }

        /// M: @{
        mSubId = intent.getIntExtra(SimGroupUtils.EXTRA_SUB_ID, -1);
        int saveMode = intent
                .getIntExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.CLOSE);
        Log.d(TAG, "[onNewIntent] mSubId:" + mSubId + ",saveMode:" + saveMode + ",action:"
                + intent.getAction());
        /// @}
        String action = intent.getAction();
        if (ACTION_SAVE_COMPLETED.equals(action)) {
            mFragment.onSaveCompleted(true, intent.getData());
            /// M: @{
            boolean isSuccess = intent.getData() != null;
            if (isSuccess && saveMode != SaveMode.RELOAD) {
                Toast.makeText(getApplicationContext(), R.string.groupSavedToast,
                        Toast.LENGTH_SHORT).show();
            }
            /// @}
        }
    }

    private final GroupEditorFragment.Listener mFragmentListener =
            new GroupEditorFragment.Listener() {
        @Override
        public void onGroupNotFound() {
            Log.w(TAG, "[onGroupNotFound] finish activity..");
            finish();
        }

        @Override
        public void onReverted() {
            finish();
        }

        @Override
        public void onAccountsNotFound() {
            Log.w(TAG, "[onAccountsNotFound] finish activity..");
            finish();
        }

        @Override
        public void onSaveFinished(int resultCode, Intent resultIntent) {
            if (resultIntent != null) {
                Intent intent = new Intent(GroupEditorActivity.this, /*GroupDetailActivity.class*/GroupBrowseActivity.class);//prize-change-huangliemin-2016-7-6
                /// M: For move to other groups feature @{
                Bundle bundle = resultIntent.getExtras();
                final AccountCategoryInfo accountCategoryInfo = bundle == null ? null
                        : (AccountCategoryInfo) bundle
                                .getParcelable(SimGroupUtils.KEY_ACCOUNT_CATEGORY);
                if (accountCategoryInfo != null) {
                    Log.d(TAG, "onSaveFinished " + accountCategoryInfo);
                    accountCategoryInfo.mAccountGroupMemberCount = mAccountGroupMemberCount;
                }
                /// @}
                intent.setData(resultIntent.getData());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                /// M: @{
                intent.putExtra("mSubId", mSubId);
                intent.putExtra("callBackIntent", "callBackIntent");
                // add the AccountCategoryInfo will be use in
                // GroupDetailActivity
                intent.putExtra(SimGroupUtils.KEY_ACCOUNT_CATEGORY,
                        (Parcelable)resultIntent.getExtras().getParcelable(
                                SimGroupUtils.KEY_ACCOUNT_CATEGORY));
                startActivityForResult(intent, SUBACTIVITY_DETAIL_GROUP);
                /// @}
            }
            finish();
        }
    };

    @Override
    public DialogManager getDialogManager() {
        return mDialogManager;
    }

    /** M: @{ */

    /// M: @{
    private String setAccountCategoryInfo() {
        Intent intent = getIntent();
        String action = intent.getAction();
        mIntentExtras = intent.getExtras();
        Log.d(TAG, " [setAccountCategoryInfo] mIntentExtras : " + mIntentExtras);
        final AccountCategoryInfo accountCategoryInfo = mIntentExtras == null ? null
                : (AccountCategoryInfo) mIntentExtras
                        .getParcelable(SimGroupUtils.KEY_ACCOUNT_CATEGORY);
        if (accountCategoryInfo != null) {
            Log.d(TAG, "[setAccountCategoryInfo] accountCategoryInfo: " + accountCategoryInfo);
            mSubId = accountCategoryInfo.mSubId;
        /// M: For move to other groups feature.
            mAccountGroupMemberCount = accountCategoryInfo.mAccountGroupMemberCount;
        } else {
            mSubId = intent.getIntExtra("SIM_ID", mSubId);
            /// M: For move to other groups feature.
            mAccountGroupMemberCount = intent.getIntExtra("GROUP_NUMS", 0);
        }
        Log.d(TAG, "[setAccountCategoryInfo] mSubId: " + mSubId);
        return action;
    }
    /// @}

    /// M: Add for SIM Service refactory @{
    @Override
    public void onSimGroupCompleted(Intent callbackIntent) {
        Log.d(TAG, "[onSIMGroupCompleted]callbackIntent = " + callbackIntent);
        onNewIntent(callbackIntent);
    }
    /// @}

    /// M: Need to register a handler for sim group processor if necessary. @{
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "[onResume]");
        if (SimGroupProcessor.isNeedRegisterHandlerAgain(mHandler)) {
            Log.d(TAG, " [onResume] register a handler again! Handler: " + mHandler);
            SimGroupProcessor.registerListener(this, mHandler);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        //prize add for bug 54241 by zhaojian 20180402 start
        InputMethodManager inputmgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmgr.prizeHideSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, null);
        //prize add for bug 54241 by zhaojian 20180402 end
        /** M: Add for SIM Service refactory @{ */
        SimGroupProcessor.unregisterListener(this);
        /** @} */
        super.onDestroy();
    }
    /// @}

    private int mSubId = SubInfoUtils.getInvalidSubId();
    private Bundle mIntentExtras;
    private int mAccountGroupMemberCount;

    /// M: Add for SIM Service refactory
    private Handler mHandler = null;

    /** @} */

}
