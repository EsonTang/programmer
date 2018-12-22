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
 * limitations under the License.
 */

package com.android.contacts.interactions;

import com.google.common.collect.Sets;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnDismissListener;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.RawContacts;

import com.google.common.collect.Lists;

import com.mediatek.contacts.eventhandler.BaseEventHandlerFragment;
import com.mediatek.contacts.eventhandler.GeneralEventHandler;
import com.mediatek.contacts.list.service.MultiChoiceHandlerListener;
import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.util.ContactsPortableUtils;
import com.mediatek.contacts.util.Log;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * An interaction invoked to delete multiple contacts.
 *
 * This class is very similar to {@link ContactDeletionInteraction}.
 */
public class ContactMultiDeletionInteraction extends BaseEventHandlerFragment
        implements LoaderCallbacks<Cursor> {

    public interface MultiContactDeleteListener {
        void onDeletionFinished();
    }

    private static final String FRAGMENT_TAG = "deleteMultipleContacts";
    private static final String TAG = "ContactMultiDeletionInteraction";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_CONTACTS_IDS = "contactIds";
    public static final String ARG_CONTACT_IDS = "contactIds";

    private static final String[] RAW_CONTACT_PROJECTION_INTERNAL = new String[] {
            RawContacts._ID,
            RawContacts.ACCOUNT_TYPE,
            RawContacts.DATA_SET,
            RawContacts.CONTACT_ID,
    };
    private static final String[] RAW_CONTACT_PROJECTION;

    static {
        List<String> projectionList = Lists.newArrayList(RAW_CONTACT_PROJECTION_INTERNAL);

        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            /// M: Add some columns for Contacts extensions. @{
            projectionList.add(RawContacts.INDICATE_PHONE_SIM);   //4
            projectionList.add(RawContacts.INDEX_IN_SIM);         //5
            projectionList.add(RawContacts.DISPLAY_NAME_PRIMARY); //6
            /// @}
        }

        RAW_CONTACT_PROJECTION = projectionList.toArray(new String[projectionList.size()]);
    }

    private static final int COLUMN_INDEX_RAW_CONTACT_ID = 0;
    private static final int COLUMN_INDEX_ACCOUNT_TYPE = 1;
    private static final int COLUMN_INDEX_DATA_SET = 2;
    private static final int COLUMN_INDEX_CONTACT_ID = 3;

    private static final int COLUMN_INDICATE_PHONE_SIM = 4;
    private static final int COLUMN_INDEX_IN_SIM = 5;
    private static final int COLUMN_INDEX_DISPLAY_NAME_PRIMARY = 6;

    private boolean mIsLoaderActive;
    private TreeSet<Long> mContactIds;

    private Context mContext;
    private AlertDialog mDialog;

    List<MultiChoiceRequest> mRequests = null;
    /**
     * Starts the interaction.
     *
     * @param activity the activity within which to start the interaction
     * @param contactIds the IDs of contacts to be deleted
     * @return the newly created interaction
     */
    public static ContactMultiDeletionInteraction start(
            Activity activity, TreeSet<Long> contactIds) {
        if (contactIds == null) {
            return null;
        }

        final FragmentManager fragmentManager = activity.getFragmentManager();
        ContactMultiDeletionInteraction fragment =
                (ContactMultiDeletionInteraction) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            Log.i(TAG, "[start] new...");
            fragment = new ContactMultiDeletionInteraction();
            fragment.setContactIds(contactIds);
            fragmentManager.beginTransaction().add(fragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            fragment.setContactIds(contactIds);
        }

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        Log.i(TAG, "[onAttach].");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.setOnDismissListener(null);
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void setContactIds(TreeSet<Long> contactIds) {
        Log.i(TAG, "[setContactIds]");
        mContactIds = contactIds;
        mIsLoaderActive = true;
        if (isStarted()) {
            Log.i(TAG, "[setContactIds]isStarted");
            Bundle args = new Bundle();
            args.putSerializable(ARG_CONTACT_IDS, mContactIds);
            getLoaderManager().restartLoader(R.id.dialog_delete_multiple_contact_loader_id,
                    args, this);
        }
    }

    private boolean isStarted() {
        return isAdded();
    }

    @Override
    public void onStart() {
        Log.i(TAG, "[onStart]mIsLoaderActive = " + mIsLoaderActive);
        if (mIsLoaderActive) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_CONTACT_IDS, mContactIds);
            getLoaderManager().initLoader(
                    R.id.dialog_delete_multiple_contact_loader_id, args, this);
        }
        super.onStart();
    }

    /*MTK no need.cause by refactor phb state change. we should hold mDialog instance to operate it
     * and Dialog should not hide when onStop,because the dialog need show again when reStart.
    @Override
    public void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.hide();
        }
    }
    */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "[onCreateLoader]...");

        final TreeSet<Long> contactIds = (TreeSet<Long>) args.getSerializable(ARG_CONTACT_IDS);
        final Object[] parameterObject = contactIds.toArray();
        final StringBuilder builder = new StringBuilder();

        /// M: fixed only can delete 1000 contacts issue @{
        // Original code:
        // final String[] parameters = new String[contactIds.size()];
        // for (int i = 0; i < contactIds.size(); i++) {
        //     parameters[i] = String.valueOf(parameterObject[i]);
        //     builder.append(RawContacts.CONTACT_ID + " =?");
        //     if (i == contactIds.size() - 1) {
        //         break;
        //    }
        //    builder.append(" OR ");
        // }
        //  return new CursorLoader(mContext, RawContacts.CONTENT_URI, RAW_CONTACT_PROJECTION,
        //          builder.toString(), parameters, null);

        builder.append(RawContacts.CONTACT_ID + " IN (");
        for (int i = 0; i < contactIds.size(); i++) {
            builder.append(String.valueOf(parameterObject[i]));
            if (i < contactIds.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");

        return new CursorLoader(mContext, RawContacts.CONTENT_URI, RAW_CONTACT_PROJECTION,
                builder.toString(), null, null);
        /// @}
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(TAG, "[onLoadFinished]...");

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (!mIsLoaderActive) {
            Log.e(TAG, "[onLoadFinished]mIsLoaderActive is false, return!");
            return;
        }

        if (cursor == null || cursor.isClosed()) {
            Log.e(TAG, "Failed to load contacts");
            return;
        }

        // This cursor may contain duplicate raw contacts, so we need to de-dupe them first
        final HashSet<Long> readOnlyRawContacts = Sets.newHashSet();
        final HashSet<Long> writableRawContacts = Sets.newHashSet();
        final HashSet<Long> contactIds = Sets.newHashSet();

        AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
        cursor.moveToPosition(-1);
        mRequests = new ArrayList<MultiChoiceRequest>();
        while (cursor.moveToNext()) {
            final long rawContactId = cursor.getLong(COLUMN_INDEX_RAW_CONTACT_ID);
            final String accountType = cursor.getString(COLUMN_INDEX_ACCOUNT_TYPE);
            final String dataSet = cursor.getString(COLUMN_INDEX_DATA_SET);
            final long contactId = cursor.getLong(COLUMN_INDEX_CONTACT_ID);
            if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
                final long indicatePhoneSimId = cursor.getLong(COLUMN_INDICATE_PHONE_SIM);
                final long indexInSim = cursor.getLong(COLUMN_INDEX_IN_SIM);
                final String displayName = cursor.getString(COLUMN_INDEX_DISPLAY_NAME_PRIMARY);
                mRequests.add(new MultiChoiceRequest((int) indicatePhoneSimId, (int) indexInSim,
                        (int) contactId, displayName));
            } else {
                mRequests.add(new MultiChoiceRequest(-1, -1, (int) contactId, null));
            }
            Log.d(TAG, "[onLoadFinished]rawContactId = " + rawContactId + ",contactId = "
                    + contactId + ",accountType = " + accountType);
            final AccountType type = accountTypes.getAccountType(accountType, dataSet);
            boolean writable = type == null || type.areContactsWritable();
            if (writable) {
                writableRawContacts.add(rawContactId);
            } else {
                readOnlyRawContacts.add(rawContactId);
            }
        }

        final int readOnlyCount = readOnlyRawContacts.size();
        final int writableCount = writableRawContacts.size();

        final int messageId;
        int positiveButtonId = android.R.string.ok;
        if (readOnlyCount > 0 && writableCount > 0) {
            messageId = R.string.batch_delete_multiple_accounts_confirmation;
        } else if (readOnlyCount > 0 && writableCount == 0) {
            messageId = R.string.batch_delete_read_only_contact_confirmation;
            positiveButtonId = R.string.readOnlyContactWarning_positive_button;
        } else if (writableCount == 1) {
            messageId = R.string.single_delete_confirmation;
            positiveButtonId = R.string.deleteConfirmation_positive_button;
        } else {
            messageId = R.string.batch_delete_confirmation;
            positiveButtonId = R.string.deleteConfirmation_positive_button;
        }

        // Convert set of contact ids into a format that is easily parcellable and iterated upon
        // for the sake of ContactSaveService.

        /// M: Forbid user to delete the read only account contacts in Contacts AP. The delete
        // flow is not suit for these accounts.
        // ToDo:
        // Change the dialog message, because we can't make it in-visible or delete it clearly. @{
        if (readOnlyCount > 0) {
            showReadonlyDialog();
        } else {
            final Long[] contactIdObjectArray = contactIds.toArray(new Long[contactIds.size()]);
            final long[] contactIdArray = new long[contactIds.size()];
            for (int i = 0; i < contactIds.size(); i++) {
                contactIdArray[i] = contactIdObjectArray[i];
            }

            showDialog(messageId, positiveButtonId, contactIdArray);

        }
        /// @}

        // We don't want onLoadFinished() calls any more, which may come when the database is
        // updating.
        getLoaderManager().destroyLoader(R.id.dialog_delete_multiple_contact_loader_id);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showDialog(int messageId, int positiveButtonId, final long[] contactIds) {
        mDialog = new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(messageId)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(positiveButtonId,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            doDeleteContact();
                        }
                    }
                )
                .create();

        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "[onDismiss]");
                mIsLoaderActive = false;
            }
        });
        mDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ACTIVE, mIsLoaderActive);
        outState.putSerializable(KEY_CONTACTS_IDS, mContactIds);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mIsLoaderActive = savedInstanceState.getBoolean(KEY_ACTIVE);
            mContactIds = (TreeSet<Long>) savedInstanceState.getSerializable(KEY_CONTACTS_IDS);
        }
    }

    protected void doDeleteContact() {
        /// M: Modify for SIM Contacs feature. @{
        final ArrayList<Long> contactIds = new ArrayList<Long>();
        for (MultiChoiceRequest request : mRequests) {
            contactIds.add(Long.valueOf(request.mContactId));
        }
        final Long[] contactIdObjectArray = contactIds.toArray(new Long[contactIds.size()]);
        final long[] contactIdArray = new long[contactIds.size()];
        for (int i = 0; i < contactIds.size(); i++) {
            contactIdArray[i] = contactIdObjectArray[i];
        }
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            handleDelete();
        } else {
            mContext.startService(ContactSaveService.createDeleteMultipleContactsIntent(mContext,
                    contactIdArray));
        }
        notifyListenerActivity();
        /// @}
    }

    private void notifyListenerActivity() {
        if (getActivity() instanceof MultiContactDeleteListener) {
            final MultiContactDeleteListener listener = (MultiContactDeleteListener) getActivity();
            listener.onDeletionFinished();
        }
    }

    ///M: MTK delete flow include SIM
    private SendRequestHandler mRequestHandler;
    private HandlerThread mHandlerThread;

    private DeleteRequestConnection mConnection = null;

    private void handleDelete() {
        Log.d(TAG, "[handleDelete]...");
        if (mConnection != null) {
            Log.w(TAG, "[handleDelete]abort due to mConnection is not null,return.");
            return ;
        }

        startDeleteService();

        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mRequestHandler = new SendRequestHandler(mHandlerThread.getLooper());
        }

        if (mRequests.size() > 0) {
            mRequestHandler.sendMessage(mRequestHandler.obtainMessage(
                    SendRequestHandler.MSG_REQUEST, mRequests));
        } else {
            mRequestHandler.sendMessage(mRequestHandler.obtainMessage(SendRequestHandler.MSG_END));
        }
    }

    private class DeleteRequestConnection implements ServiceConnection {
        private MultiChoiceService mService;

        public boolean sendDeleteRequest(final List<MultiChoiceRequest> requests) {
            Log.d(TAG, "[sendDeleteRequest] Send an delete request");
            if (mService == null) {
                Log.i(TAG, "[sendDeleteRequest] mService is not ready");
                return false;
            }
            mService.handleDeleteRequest(requests, new MultiChoiceHandlerListener(/*mService*/ContactMultiDeletionInteraction.this.getActivity()));//prize-change for custom progressbar -hpf-2017-12-27
            return true;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "[onServiceConnected]");
            mService = ((MultiChoiceService.MyBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "[onServiceDisconnected] Disconnected from MultiChoiceService");
        }
    }

    private class SendRequestHandler extends Handler {
        public static final int MSG_REQUEST = 100;
        public static final int MSG_END = 200;

        private int mRetryCount = 20;

        public SendRequestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "[handleMessage]msg.what = " + msg.what);
            if (msg.what == MSG_REQUEST) {
                if (!mConnection.sendDeleteRequest((List<MultiChoiceRequest>) msg.obj)) {
                    Log.i(TAG, "[handleMessage]send fail, mRetryCount = " + mRetryCount);
                    if (mRetryCount-- > 0) {
                        sendMessageDelayed(obtainMessage(msg.what, msg.obj), 500);
                    } else {
                        sendMessage(obtainMessage(MSG_END));
                    }
                } else {
                    sendMessage(obtainMessage(MSG_END));
                }
                return;
            } else if (msg.what == MSG_END) {
                destroyMyself();
                return;
            }
            super.handleMessage(msg);
        }

    }

    void startDeleteService() {
        Log.i(TAG, "[startDeleteService]");
        mConnection = new DeleteRequestConnection();
        // We don't want the service finishes itself just after this connection.
        Intent intent = new Intent(this.getActivity(), MultiChoiceService.class);
        getContext().startService(intent);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void destroyMyself() {
        Log.i(TAG, "[destroyMyself]mHandlerThread:" + mHandlerThread);
        if (mConnection != null) {
            if (null != getContext()) {
                getContext().unbindService(mConnection);
            } else {
                Log.e(TAG, "[destroyMyself] getContext() is null !!!");
            }
            mConnection = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    /// M: refactor phb state change @{
    @Override
    public void onReceiveEvent(String eventType, Intent extraData) {
        Log.i(TAG, "[onReceiveEvent] eventType: " + eventType);
        if (GeneralEventHandler.EventType.PHB_STATE_CHANGE_EVENT.equals(eventType)
                && (mDialog != null) && mDialog.isShowing()) {
            Log.i(TAG, "[onReceiveEvent] mDialog will dismiss");
            mDialog.dismiss();
        }
    }
    /// @}

    /**
     * M: add for alert the read only contact can not be delete in Contact APP.
     */
    private void showReadonlyDialog() {
        mDialog = new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.readOnlyContactWarning)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        notifyListenerActivity();
                    }
                }).create();

        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsLoaderActive = false;
                mDialog = null;
            }
        });
        mDialog.show();
    }
}
