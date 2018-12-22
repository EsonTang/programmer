/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.editor.ContactEditorBaseFragment;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.util.DialogManager;
import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simservice.SimEditProcessor;
import com.mediatek.contacts.util.ContactsPortableUtils;
import com.mediatek.contacts.util.Log;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Base Activity for contact editors.
 */
abstract public class ContactEditorBaseActivity extends ContactsActivity
        implements DialogManager.DialogShowingViewActivity ,SimEditProcessor.Listener {
    protected static final String TAG = "ContactEditorBaseActivity";

    /**
     * Intent action to edit a contact with all available field inputs displayed.
     *
     * Only used to open the "fully expanded" editor -- {@link ContactEditorActivity}.
     */
    public static final String ACTION_EDIT = "com.android.contacts.action.FULL_EDIT";

    /**
     * Intent action to insert a new contact with all available field inputs displayed.
     *
     * Only used to open the "fully expanded" editor -- {@link ContactEditorActivity}.
     */
    public static final String ACTION_INSERT = "com.android.contacts.action.FULL_INSERT";

    public static final String ACTION_JOIN_COMPLETED = "joinCompleted";
    public static final String ACTION_SAVE_COMPLETED = "saveCompleted";

    public static final int RESULT_CODE_SPLIT = 2;

    protected int mActionBarTitleResId;

    // prize add for bug 52850 by zhaojian 20180316 start
    public static boolean sIsClickedSaveText = false;
    // prize add for bug 52850 by zhaojian 20180316 end

    /**
     * Contract for contact editors Fragments that are managed by this Activity.
     */
    public interface ContactEditor {

        /**
         * Modes that specify what the AsyncTask has to perform after saving
         */
        public interface SaveMode {
            /**
             * Close the editor after saving
             */
            public static final int CLOSE = 0;

            /**
             * Reload the data so that the user can continue editing
             */
            public static final int RELOAD = 1;

            /**
             * Split the contact after saving
             */
            public static final int SPLIT = 2;

            /**
             * Join another contact after saving
             */
            public static final int JOIN = 3;

            /**
             * Navigate to the compact editor view after saving.
             */
            public static final int COMPACT = 4;
        }

        /**
         * The status of the contact editor.
         */
        public interface Status {
            /**
             * The loader is fetching data
             */
            public static final int LOADING = 0;

            /**
             * Not currently busy. We are waiting for the user to enter data
             */
            public static final int EDITING = 1;

            /**
             * The data is currently being saved. This is used to prevent more
             * auto-saves (they shouldn't overlap)
             */
            public static final int SAVING = 2;

            /**
             * Prevents any more saves. This is used if in the following cases:
             * - After Save/Close
             * - After Revert
             * - After the user has accepted an edit suggestion
             * - After the user chooses to expand the compact editor
             */
            public static final int CLOSING = 3;

            /**
             * Prevents saving while running a child activity.
             */
            public static final int SUB_ACTIVITY = 4;
        }

        /**
         * Sets the hosting Activity that will receive callbacks from the contact editor.
         */
        void setListener(ContactEditorBaseFragment.Listener listener);

        /**
         * Initialize the contact editor.
         */
        void load(String action, Uri lookupUri, Bundle intentExtras);

        /**
         * Applies extras from the hosting Activity to the first writable raw contact.
         */
        void setIntentExtras(Bundle extras);

        /**
         * Saves or creates the contact based on the mode, and if successful
         * finishes the activity.
         */
        boolean save(int saveMode);

        /**
         * If there are no unsaved changes, just close the editor, otherwise the user is prompted
         * before discarding unsaved changes.
         */
        boolean revert();

        /**
         * Invoked after the contact is saved.
         */
        void onSaveCompleted(boolean hadChanges, int saveMode, boolean saveSucceeded,
                Uri contactLookupUri, Long joinContactId);

        /**
         * Invoked after the contact is joined.
         */
        void onJoinCompleted(Uri uri);

        /// M: insert sim card contacts completed @{
        void onSaveSIMContactCompleted(boolean hadChanges, Intent data);
        /// @}
    }

    /**
     * Boolean intent key that specifies that this activity should finish itself
     * (instead of launching a new view intent) after the editor changes have been
     * saved.
     */
    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED =
            "finishActivityOnSaveCompleted";

    protected ContactEditor mFragment;
    private boolean mFinishActivityOnSaveCompleted;

    private DialogManager mDialogManager = new DialogManager(this);

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        Log.i(TAG, "[onCreate] SIMEditProcessor.registerListener,action = " + action);
        /** M: Add for SIM Service refactory @{ */
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            mHandler = ActivitiesUtils.initHandler(this);
            SimCardUtils.ShowSimCardStorageInfoTask.showSimCardStorageInfo(this, false);
        }
        /** @} */

        // Determine whether or not this activity should be finished after the user is done
        // editing the contact or if this activity should launch another activity to view the
        // contact's details.
        mFinishActivityOnSaveCompleted = intent.getBooleanExtra(
                INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, false);

        // The only situation where action could be ACTION_JOIN_COMPLETED is if the
        // user joined the contact with another and closed the activity before
        // the save operation was completed.  The activity should remain closed then.
        if (ACTION_JOIN_COMPLETED.equals(action)) {
            finish();
            return;
        }

        if (ACTION_SAVE_COMPLETED.equals(action)) {
            finish();
            return;
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
        	/*prize-change-huangliemin-2016-7-19-start*/
        	/*
            if (Intent.ACTION_EDIT.equals(action) || ACTION_EDIT.equals(action)) {
                actionBar.setTitle(getResources().getString(
                        R.string.contact_editor_title_existing_contact));
            } else {
                actionBar.setTitle(getResources().getString(
                        R.string.contact_editor_title_new_contact));
            }
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_dk);//zhangzhonghao
            actionBar.setElevation(0); //zhangzhonghao 20160413
            */
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(true);
        	actionBar.setDisplayHomeAsUpEnabled(false);
        	actionBar.setDisplayShowTitleEnabled(false);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setCustomView(R.layout.prize_custom_editor_action_bar_huangliemin_2016_7_19);
        	View backtext = actionBar.getCustomView().findViewById(R.id.back_container);
        	TextView backTv = (TextView)actionBar.getCustomView().findViewById(R.id.back);
        	backTv.setText(R.string.back_button_label);
        	mSavetext = actionBar.getCustomView().findViewById(R.id.save_container);
        	backtext.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});

            mSavetext.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
                    // prize add for bug 52850 by zhaojian 20180316 start
                    sIsClickedSaveText = true;
                    // prize add for bug 52850 by zhaojian 20180316 end
					onSavePressed();
				}
			});
            /*prize-change-huangliemin-2016-7-19-end*/
        }
    }

    //prize-add-huangliemin-2016-7-19-start
    public void onSavePressed() {
    	Log.d(TAG, "[onSavePressed]");
    }
    //prize-add-huangliemin-2016-7-19-end

    /*prize-add-hpf-2017-12-4-start*/
    private View mSavetext;
    public void updateActionbarSaveTextEnable(boolean enable) {
        Log.d(TAG, "[prizeUpdateActionbarSaveText]");
        if (mSavetext != null){
            mSavetext.setEnabled(!enable);
        }
    }
    /*prize-add-hpf-2017-12-4-end*/


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[onPause]");
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        final View currentFocus = getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[onDestroy]");
        /** M: Add for SIM Service refactory @{ */
        SimEditProcessor.unregisterListener(this);
        /** @} */
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (mFragment == null) {
            Log.w(TAG, "[onNewIntent]fragment is null,return!");
            return;
        }

        String action = intent.getAction();
        Log.w(TAG, "[onNewIntent]action = " + action);
        if (Intent.ACTION_EDIT.equals(action) || ACTION_EDIT.equals(action)) {
            mFragment.setIntentExtras(intent.getExtras());
        } else if (ACTION_SAVE_COMPLETED.equals(action)) {
            mFragment.onSaveCompleted(true,
                    intent.getIntExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY,
                            ContactEditor.SaveMode.CLOSE),
                    intent.getBooleanExtra(ContactSaveService.EXTRA_SAVE_SUCCEEDED, false),
                    intent.getData(),
                    intent.getLongExtra(ContactEditorFragment.JOIN_CONTACT_ID_EXTRA_KEY, -1));
        } else if (ACTION_JOIN_COMPLETED.equals(action)) {
            mFragment.onJoinCompleted(intent.getData());
        } else if (SimEditProcessor.EDIT_SIM_ACTION.equals(action)) {
            /// M: insert sim card contacts completed @{
            mFragment.onSaveSIMContactCompleted(true, intent);
            /// @}
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (DialogManager.isManagedId(id)) return mDialogManager.onCreateDialog(id, args);

        // Nobody knows about the Dialog
        Log.w(TAG, "Unknown dialog requested, id: " + id + ", args: " + args);
        return null;
    }

    @Override
    public void onBackPressed() {
        if (mFragment != null) {
            mFragment.revert();
        }
    }
    
    protected final ContactEditorBaseFragment.Listener  mFragmentListener =
            new ContactEditorBaseFragment.Listener() {

        @Override
        public void onDeleteRequested(Uri contactUri) {
            Log.d(TAG, "[onDeleteRequested]uri = " + contactUri);
            ContactDeletionInteraction.start(ContactEditorBaseActivity.this, contactUri, true);
        }

        @Override
        public void onReverted() {
            Log.d(TAG, "[onReverted]finish.");
            finish();
        }

        @Override
        public void onSaveFinished(Intent resultIntent) {
            if (mFinishActivityOnSaveCompleted) {
                setResult(resultIntent == null ? RESULT_CANCELED : RESULT_OK, resultIntent);
            } else if (resultIntent != null) {
                ImplicitIntentsUtil.startActivityInApp(ContactEditorBaseActivity.this,
                        resultIntent);
            }
            finish();
        }

        @Override
        public void onContactSplit(Uri newLookupUri) {
            setResult(RESULT_CODE_SPLIT, /* data */ null);
            finish();
        }

        @Override
        public void onContactNotFound() {
            Log.d(TAG, "[onContactNotFound]finish.");
            finish();
        }

        @Override
        public void onEditOtherContactRequested(
                Uri contactLookupUri, ArrayList<ContentValues> values) {
            final Intent intent = EditorIntents.createEditOtherContactIntent(
                    contactLookupUri, values);
            Log.d(TAG, "[onEditOtherContactRequested]intent = " + intent);
            ImplicitIntentsUtil.startActivityInApp(ContactEditorBaseActivity.this, intent);
            finish();
        }

        @Override
        public void onCustomCreateContactActivityRequested(AccountWithDataSet account,
                Bundle intentExtras) {
            Log.d(TAG, "[onCustomCreateContactActivityRequested]");
            final AccountTypeManager accountTypes =
                    AccountTypeManager.getInstance(ContactEditorBaseActivity.this);
            final AccountType accountType = accountTypes.getAccountType(
                    account.type, account.dataSet);

            Intent intent = new Intent();
            intent.setClassName(accountType.syncAdapterPackageName,
                    accountType.getCreateContactActivityClassName());
            intent.setAction(Intent.ACTION_INSERT);
            intent.setType(Contacts.CONTENT_ITEM_TYPE);
            if (intentExtras != null) {
                intent.putExtras(intentExtras);
            }
            intent.putExtra(RawContacts.ACCOUNT_NAME, account.name);
            intent.putExtra(RawContacts.ACCOUNT_TYPE, account.type);
            intent.putExtra(RawContacts.DATA_SET, account.dataSet);
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }

        @Override
        public void onCustomEditContactActivityRequested(AccountWithDataSet account,
                Uri rawContactUri, Bundle intentExtras, boolean redirect) {
            Log.d(TAG, "[onCustomEditContactActivityRequested]redirect = " + redirect);
            final AccountTypeManager accountTypes =
                    AccountTypeManager.getInstance(ContactEditorBaseActivity.this);
            final AccountType accountType = accountTypes.getAccountType(
                    account.type, account.dataSet);

            Intent intent = new Intent();
            intent.setClassName(accountType.syncAdapterPackageName,
                    accountType.getEditContactActivityClassName());
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(rawContactUri);
            if (intentExtras != null) {
                intent.putExtras(intentExtras);
            }

            if (redirect) {
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            } else {
                startActivity(intent);
            }
        }
    };

    @Override
    public DialogManager getDialogManager() {
        return mDialogManager;
    }

    /** M: @{ */

    /// M: Add for SIM Service refactory
    private Handler mHandler = null;

    /// M: Need to register a handler for sim edit processor if necessary. @{
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "[onResume]");
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT
                && SimEditProcessor.isNeedRegisterHandlerAgain(mHandler)) {
            Log.d(TAG, " [onResume] register a handler again! Handler: " + mHandler);
            SimEditProcessor.registerListener(this, mHandler);
        }
    }
    /// @}

    /// M: Add for SIM Service refactory @{
    @Override
    public void onSIMEditCompleted(Intent callbackIntent) {
        Log.d(TAG, "[onSIMEditCompleted]callbackIntent = " + callbackIntent);
        onNewIntent(callbackIntent);
    }
    /// @}

    /** @} */
}
