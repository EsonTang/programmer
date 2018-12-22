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

package com.android.contacts.interactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Entity;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
//import android.util.Log;
import android.widget.Toast;

import com.google.common.collect.Lists;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.internal.telephony.PhoneConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.util.ContactsConstants;
import com.mediatek.contacts.util.ContactsPortableUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.simservice.SimDeleteProcessor;

import java.util.HashSet;
import java.util.List;

import com.mediatek.contacts.eventhandler.BaseEventHandlerFragment;
import com.mediatek.contacts.eventhandler.GeneralEventHandler;
import com.mediatek.contacts.interactions.ContactDeletionInteractionUtils;

/*prize-add-hpf-2018-2-26-start*/
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
/*prize-add-hpf-2018-2-26-end*/

/**
 * An interaction invoked to delete a contact.
 */
public class ContactDeletionInteraction extends BaseEventHandlerFragment
        implements LoaderCallbacks<Cursor>,
        OnDismissListener, SimDeleteProcessor.Listener {

    private static final String TAG = "ContactDeletionInteraction";
    private static final String FRAGMENT_TAG = "deleteContact";

    private static final String KEY_ACTIVE = "active";
    private static final String KEY_CONTACT_URI = "contactUri";

    private static final String KEY_FINISH_WHEN_DONE = "finishWhenDone";
    public static final String ARG_CONTACT_URI = "contactUri";
    public static final int RESULT_CODE_DELETED = 3;

    private static final String[] ENTITY_PROJECTION_INTERNAL = new String[] {
        Entity.RAW_CONTACT_ID, //0
        Entity.ACCOUNT_TYPE, //1
        Entity.DATA_SET, // 2
        Entity.CONTACT_ID, // 3
        Entity.LOOKUP_KEY // 4
    };
    private static final String[] ENTITY_PROJECTION;

    static {
        List<String> projectionList = Lists.newArrayList(ENTITY_PROJECTION_INTERNAL);

        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            /// M: Add some columns for Contacts extensions. @{
            projectionList.add(RawContacts.INDICATE_PHONE_SIM);   //5
            projectionList.add(RawContacts.INDEX_IN_SIM);         //6
            /// @}
        }

        ENTITY_PROJECTION = projectionList.toArray(new String[projectionList.size()]);
    }

    private static final int COLUMN_INDEX_RAW_CONTACT_ID = 0;
    private static final int COLUMN_INDEX_ACCOUNT_TYPE = 1;
    private static final int COLUMN_INDEX_DATA_SET = 2;
    private static final int COLUMN_INDEX_CONTACT_ID = 3;
    private static final int COLUMN_INDEX_LOOKUP_KEY = 4;
    private static final int COLUMN_INDEX_INDICATE_PHONE_SIM = 5;
    private static final int COLUMN_INDEX_IN_SIM = 6;

    private boolean mActive;
    private Uri mContactUri;
    private boolean mFinishActivityWhenDone;
    private Context mContext;
    private AlertDialog mDialog;

    /** This is a wrapper around the fragment's loader manager to be used only during testing. */
    private TestLoaderManagerBase mTestLoaderManager;

    @VisibleForTesting
    int mMessageId;

    /**
     * Starts the interaction.
     *
     * @param activity the activity within which to start the interaction
     * @param contactUri the URI of the contact to delete
     * @param finishActivityWhenDone whether to finish the activity upon completion of the
     *        interaction
     * @return the newly created interaction
     */
    public static ContactDeletionInteraction start(
            Activity activity, Uri contactUri, boolean finishActivityWhenDone) {
        Log.d(FRAGMENT_TAG, "[start] set mSimUri and mSimWhere are null");
        ContactDeletionInteraction deletion = startWithTestLoaderManager(activity,
                contactUri, finishActivityWhenDone, null);
        return deletion;
    }

    /**
     * Starts the interaction and optionally set up a {@link TestLoaderManagerBase}.
     *
     * @param activity the activity within which to start the interaction
     * @param contactUri the URI of the contact to delete
     * @param finishActivityWhenDone whether to finish the activity upon completion of the
     *        interaction
     * @param testLoaderManager the {@link TestLoaderManagerBase} to use to load the data,
     *        may be null in which case the default {@link LoaderManager} is used
     * @return the newly created interaction
     */
    @VisibleForTesting
    static ContactDeletionInteraction startWithTestLoaderManager(
            Activity activity, Uri contactUri, boolean finishActivityWhenDone,
            TestLoaderManagerBase testLoaderManager) {
        if (contactUri == null || activity.isDestroyed()) {
            return null;
        }

        FragmentManager fragmentManager = activity.getFragmentManager();
        ContactDeletionInteraction fragment =
                (ContactDeletionInteraction) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new ContactDeletionInteraction();
            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
            fragmentManager.beginTransaction().add(fragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
        }
        /** M: add for sim contact @ { */
        fragment.mSimUri = null;
        fragment.mSimIndex = -1;
        /** @ } */
        return fragment;
    }

    @Override
    public LoaderManager getLoaderManager() {
        // Return the TestLoaderManager if one is set up.
        LoaderManager loaderManager = super.getLoaderManager();
        if (mTestLoaderManager != null) {
            // Set the delegate: this operation is idempotent, so let's just do it every time.
            mTestLoaderManager.setDelegate(loaderManager);
            return mTestLoaderManager;
        } else {
            return loaderManager;
        }
    }

    /** Sets the TestLoaderManager that is used to wrap the actual LoaderManager in tests. */
    private void setTestLoaderManager(TestLoaderManagerBase mockLoaderManager) {
        mTestLoaderManager = mockLoaderManager;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        /// M: initial progress handler to show delay progressDialog
        mProgressHandler = new ContactDeletionInteractionUtils.ProgressHandler(mContext, this);

        /// M: Add for SIM Service refactory
        SimDeleteProcessor.registerListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.setOnDismissListener(null);
            mDialog.dismiss();
            mDialog = null;
        }

        /** M: Add for SIM Service refactory @{ */
        SimDeleteProcessor.unregisterListener(this);
        /** @} */
    }

    public void setContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mActive = true;
        if (isStarted()) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_CONTACT_URI, mContactUri);
            getLoaderManager().restartLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
    }

    private void setFinishActivityWhenDone(boolean finishActivityWhenDone) {
        this.mFinishActivityWhenDone = finishActivityWhenDone;

    }

    /* Visible for testing */
    boolean isStarted() {
        return isAdded();
    }

    @Override
    public void onStart() {
        if (mActive) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_CONTACT_URI, mContactUri);
            getLoaderManager().initLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contactUri = args.getParcelable(ARG_CONTACT_URI);
        return new CursorLoader(mContext,
                Uri.withAppendedPath(contactUri, Entity.CONTENT_DIRECTORY), ENTITY_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    	/*prize-remove-hpf-2018-2-26-start*/
        /*if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }*/
        /*prize-remove-hpf-2018-2-26-end*/
        if (!mActive) {
            return;
        }

        if (cursor == null || cursor.isClosed()) {
            Log.e(TAG, "Failed to load contacts");
            return;
        }

        long contactId = 0;
        String lookupKey = null;

        // This cursor may contain duplicate raw contacts, so we need to de-dupe them first
        HashSet<Long>  readOnlyRawContacts = Sets.newHashSet();
        HashSet<Long>  writableRawContacts = Sets.newHashSet();

        AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            final long rawContactId = cursor.getLong(COLUMN_INDEX_RAW_CONTACT_ID);
            final String accountType = cursor.getString(COLUMN_INDEX_ACCOUNT_TYPE);
            final String dataSet = cursor.getString(COLUMN_INDEX_DATA_SET);
            contactId = cursor.getLong(COLUMN_INDEX_CONTACT_ID);
            lookupKey = cursor.getString(COLUMN_INDEX_LOOKUP_KEY);
            if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
                mSubId = cursor.getInt(COLUMN_INDEX_INDICATE_PHONE_SIM);
                mSimIndex = cursor.getInt(COLUMN_INDEX_IN_SIM);
                mSimUri = SubInfoUtils.getIccProviderUri(mSubId);
            }
            AccountType type = accountTypes.getAccountType(accountType, dataSet);
            boolean writable = type == null || type.areContactsWritable();
            if (writable) {
                writableRawContacts.add(rawContactId);
            } else {
                readOnlyRawContacts.add(rawContactId);
            }
        }
        if (TextUtils.isEmpty(lookupKey)) {
            Log.e(TAG, "Failed to find contact lookup key");
            getActivity().finish();
            return;
        }

        int readOnlyCount = readOnlyRawContacts.size();
        int writableCount = writableRawContacts.size();
        int positiveButtonId = android.R.string.ok;
        if (readOnlyCount > 0 && writableCount > 0) {
            mMessageId = R.string.readOnlyContactDeleteConfirmation;
        } else if (readOnlyCount > 0 && writableCount == 0) {
            mMessageId = R.string.readOnlyContactWarning;
            positiveButtonId = R.string.readOnlyContactWarning_positive_button;
        } else if (readOnlyCount == 0 && writableCount > 1) {
            mMessageId = R.string.multipleContactDeleteConfirmation;
            positiveButtonId = R.string.deleteConfirmation_positive_button;
        } else {
            mMessageId = R.string.deleteConfirmation;
            positiveButtonId = R.string.deleteConfirmation_positive_button;
        }

        /// M: Forbid user to delete the read only account contacts in Contacts AP. The delete
        // flow is not suit for these accounts.
        // ToDo:
        // Change the dialog message, because we can't make it in-visible or delete it clearly. @{
        if (readOnlyCount > 0) {
            showReadonlyDialog();
        } else {
            final Uri contactUri = Contacts.getLookupUri(contactId, lookupKey);
            
            /*prize-change-hpf-2018-2-26-start*/
            //showDialog(mMessageId, positiveButtonId, contactUri);
            prizeShowBottomDialog(contactUri);
            /*prize-change-hpf-2018-2-26-end*/
        }
        /// @}

        // We don't want onLoadFinished() calls any more, which may come when the database is
        // updating.
        getLoaderManager().destroyLoader(R.id.dialog_delete_contact_loader_id);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showDialog(int messageId, int positiveButtonId, final Uri contactUri) {
        mDialog = new AlertDialog.Builder(getActivity())
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.prize_editor_delete_contact)//prize-add-hpf-2017-12-15
                .setMessage(messageId)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(positiveButtonId,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            doDeleteContact(contactUri);
                        }
                    }).create();
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }
    
    /*prize-add-hpf-2018-2-26-start*/
	private void prizeShowBottomDialog(final Uri contactUri) {
		Log.d(TAG,"[prizeShowBottomDialog]");
		View rootView = View.inflate(mContext, R.layout.prize_contacts_delete_dialog, null);
		View delete = rootView.findViewById(R.id.delete_contact);
		View cancel = rootView.findViewById(R.id.cancel_btn);
		delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doDeleteContact(contactUri);
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog = new AlertDialog.Builder(getActivity()).setView(rootView).create();
		Window dialogWindow = mDialog.getWindow();
		dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
		dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
		WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.gravity = Gravity.BOTTOM;
		dialogWindow.setAttributes(mParams);
		dialogWindow.setWindowAnimations(R.style.GetDialogBottomMenuAnimation);
		mDialog.setOnDismissListener(this);
		mDialog.show();
	}
	/*prize-add-hpf-2018-2-26-end*/

    @Override
    public void onDismiss(DialogInterface dialog) {
        mActive = false;
        //mDialog = null;//prize-remove-hpf-2018-2-26
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ACTIVE, mActive);
        outState.putParcelable(KEY_CONTACT_URI, mContactUri);
        /** M: to save sim_uri and sim_index to delete @{ */
        outState.putParcelable(KEY_CONTACT_SIM_URI, mSimUri);
        outState.putInt(KEY_CONTACT_SIM_INDEX, mSimIndex);
        outState.putInt(KEY_CONTACT_SUB_ID, mSubId);
        /**@}*/
        outState.putBoolean(KEY_FINISH_WHEN_DONE, mFinishActivityWhenDone);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mActive = savedInstanceState.getBoolean(KEY_ACTIVE);
            mContactUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            /** M: to get sim_uri and sim_index to delete @{ */
            mSimUri = savedInstanceState.getParcelable(KEY_CONTACT_SIM_URI);
            mSimIndex = savedInstanceState.getInt(KEY_CONTACT_SIM_INDEX);
            mSubId = savedInstanceState.getInt(KEY_CONTACT_SUB_ID);
            /**@}*/
            mFinishActivityWhenDone = savedInstanceState.getBoolean(KEY_FINISH_WHEN_DONE);
        }
    }

    protected void doDeleteContact(final Uri contactUri) {
        /** M: Add for SIM Contact @{ */
        if (!isAdded()) {
            Log.w(FRAGMENT_TAG, "[doDeleteContact] This Fragment is not add to the Activity.");
            return;
        }
        if (!ContactDeletionInteractionUtils.doDeleteSimContact(mContext,
                 contactUri, mSimUri, mSimIndex, mSubId, this)) {
            /** @} */
            mContext.startService(ContactSaveService.createDeleteContactIntent(mContext,
                    contactUri));
            if (isAdded() && mFinishActivityWhenDone) {
                Log.d(FRAGMENT_TAG, "[doDeleteContact] finished");
                getActivity().setResult(RESULT_CODE_DELETED);
                getActivity().finish();
                final String deleteToastMessage = getResources().getQuantityString(R.plurals
                        .contacts_deleted_toast, /* quantity */ 1);
                Toast.makeText(mContext, deleteToastMessage, Toast.LENGTH_LONG).show();
                 /**  prize add by bxh  start */
                if(contactUri != null){
                	Intent intent = new Intent();
                	intent.setAction("prize_delete_contact");
                	intent.putExtra("ContactUri", contactUri.toString());
                	mContext.sendBroadcast(intent);
                }
                 /**  prize add by bxh  end */
            }
        }
    }

    /** M: @{ */

    private Uri mSimUri = null;
    private int mSimIndex = -1;
    /// M: change for SIM Service refactoring
    private static int mSubId = SubInfoUtils.getInvalidSubId();

    /** M: show loading when load data in back ground @{ */
    private ContactDeletionInteractionUtils.ProgressHandler mProgressHandler;
    /** @}*/

    /** M: Add for SIM Service refactory @{ */
    @Override
    public void onSIMDeleteFailed() {
        if (isAdded()) {
            getActivity().finish();
        }
        return;
    }

    @Override
    public void onSIMDeleteCompleted() {
        if (isAdded() && mFinishActivityWhenDone) {
            getActivity().setResult(RESULT_CODE_DELETED);
            /// M: Fix JE: Conn't show toast in non UI thread. @{
            final String deleteToastMessage = getResources().getQuantityString(R.plurals
                    .contacts_deleted_toast, /* quantity */ 1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, deleteToastMessage, Toast.LENGTH_LONG).show();
                }
            });
            getActivity().finish();
            /// @}
        }
        return;
    }
    /** @} */

    /// M: refactor phb state change @{
    @Override
    public void onReceiveEvent(String eventType, Intent extraData) {
        int stateChangeSubId = extraData.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                ContactsConstants.ERROR_SUB_ID);
        Log.i(TAG ,
                "[onReceiveEvent] eventType: " + eventType + ", extraData: " + extraData.toString()
                        + ",stateChangeSubId: " + stateChangeSubId + ",mSubId: " + mSubId);
        if (GeneralEventHandler.EventType.PHB_STATE_CHANGE_EVENT.equals(eventType)
                && (mSubId == stateChangeSubId)) {
            Log.i(TAG, "[onReceiveEvent] phb state change,finish EditorActivity ");
            getActivity().setResult(RESULT_CODE_DELETED);
            getActivity().finish();
            return;
        }
    }
    /** @} */

    /**
     * M: add for alert the read only contact can not be delete in Contact APP.
     */
    private void showReadonlyDialog() {
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.deleteConfirmation_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.readOnlyContactWarning)
                .setPositiveButton(android.R.string.ok, null).create();
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }
    /** @} */

    /** M: key to map sim_uri and sim_index to delete @{ */
    private static final String KEY_CONTACT_SIM_URI = "contact_sim_uri";
    private static final String KEY_CONTACT_SIM_INDEX = "contact_sim_index";
    private static final String KEY_CONTACT_SUB_ID = "contact_sub_id";

    /// M: add for sim conatct
    private static final int ERROR_SUBID = -10;

    /**@}*/
}
