/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.contacts.simservice;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;

import com.android.contacts.ContactSaveService;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor.SaveMode;
import com.android.contacts.editor.ContactEditorFragment;
import com.google.android.collect.Lists;
import com.mediatek.contacts.group.SimGroupUtils;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.simservice.SimProcessorManager.ProcessorCompleteListener;
import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimGroupProcessor extends SimProcessorBase {
    private static final String TAG = "SimGroupProcessor";

    private Context mContext;
    private Intent mIntent = null;
    private Uri mLookupUri = null;
    private int mSubId = SubInfoUtils.getInvalidSubId();
    private static final int GROUP_SIM_ABSENT = 4;
    private static final int MAX_OPERATIONS_SIZE = 400;

    private static List<Listener> sListeners = new ArrayList<Listener>();
    private static Map<Listener, Handler> sListenerHolder = new HashMap<Listener, Handler>();

    public interface Listener {
        public void onSimGroupCompleted(Intent callbackIntent);
    }

    public static void registerListener(Listener listener, Handler handler) {
        if (!(listener instanceof Activity)) {
            throw new ClassCastException("Only activities can be registered to"
                    + " receive callback from " + SimProcessorService.class.getName());
        }
        Log.d(TAG, "[registerListener]listener added to SIMGroupProcessor: " + listener);
        sListeners.add(listener);
        sListenerHolder.put(listener, handler);
        Log.d(TAG, "[registerListener] sListenerHolder = " + listener.hashCode() + " mHandler = "
                        + handler.hashCode());
    }

    public static void unregisterListener(Listener listener) {
        Log.d(TAG, "[unregisterListener]listener removed from SIMGroupProcessor: " + listener);
        Handler handler = sListenerHolder.get(listener);
        if (handler != null) {
            Log.d(TAG, "[unregisterListener] handler = " + handler.hashCode()
                    + " listener = " + listener.hashCode());
            handler = null;
            sListeners.remove(listener);
            sListenerHolder.remove(listener);
            listener = null;
        }
    }

    public static boolean isNeedRegisterHandlerAgain(Handler handler) {
        Log.d(TAG, "[isNeedRegisterHandlerAgain] handler: " + handler);
        for (Listener listener : sListeners) {
            if (handler.equals(sListenerHolder.get(listener))) {
                return false;
            }
        }
        return true;
    }

    public SimGroupProcessor(Context context, int subId, Intent intent,
            ProcessorCompleteListener listener) {
        super(intent, listener);
        mContext = context;
        mSubId = subId;
        mIntent = intent;
        Log.i(TAG, "[SIMGroupProcessor]new mSubId = " + mSubId);
    }

    @Override
    public int getType() {
        return SimServiceUtils.SERVICE_WORK_GROUP;
    }

    @Override
    public void doWork() {
        if (mIntent == null) {
            Log.e(TAG, "[doWork]onHandleIntent: could not handle null intent");
            return;
        }

        // Call an appropriate method. If we're sure it affects how incoming phone calls are
        // handled, then notify the fact to in-call screen.
        String action = mIntent.getAction();
        Log.d(TAG, "[doWork]action = " + action);
        if (ContactSaveService.ACTION_CREATE_GROUP.equals(action)) {
            createGroup(mIntent);
        } else if (ContactSaveService.ACTION_RENAME_GROUP.equals(action)) {
            renameGroup(mIntent);
        } else if (ContactSaveService.ACTION_DELETE_GROUP.equals(action)) {
            deleteGroup(mIntent);
        } else if (ContactSaveService.ACTION_UPDATE_GROUP.equals(action)) {
            updateGroup(mIntent);
        }
    }

    private void createGroup(Intent intent) {
        String accountType = intent.getStringExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE);
        String accountName = intent.getStringExtra(ContactSaveService.EXTRA_ACCOUNT_NAME);
        String dataSet = intent.getStringExtra(ContactSaveService.EXTRA_DATA_SET);
        String label = intent.getStringExtra(ContactSaveService.EXTRA_GROUP_LABEL);
        final long[] rawContactsToAdd = intent.getLongArrayExtra(
                ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD);

        Log.d(TAG, "[createGroup]groupName:" + label + " ,accountName:" + accountName
                + ",AccountType:" + accountType);
        Intent callbackIntent = intent
                .getParcelableExtra(ContactSaveService.EXTRA_CALLBACK_INTENT);
        if (TextUtils.isEmpty(label)) {
            Log.w(TAG, "[createGroup]Group name can't be empty!");
            callbackIntent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.RELOAD);
            deliverCallback(callbackIntent);
            return ;
        }
        if (!SimGroupUtils.checkGroupNameExist(mContext, label, accountName, accountType)) {
            Log.w(TAG, "[createGroup]Group Name exist!");
            callbackIntent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.RELOAD);
            deliverCallback(callbackIntent);
            return;
        }

        int[] rawContactsIndexInIcc = intent
                .getIntArrayExtra(SimGroupUtils.EXTRA_SIM_INDEX_ARRAY);
        int subId = intent.getIntExtra(SimGroupUtils.EXTRA_SUB_ID, -1);
        int groupIdInIcc = -1;
        if (subId > 0) {
            groupIdInIcc = SimGroupUtils.createGroupToIcc(mContext, intent);
            if (groupIdInIcc < 0) {
                Log.w(TAG, "[createGroup]createGroupToIcc fail!");
                callbackIntent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.RELOAD);
                deliverCallback(callbackIntent);
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(Groups.ACCOUNT_TYPE, accountType);
        values.put(Groups.ACCOUNT_NAME, accountName);
        values.put(Groups.DATA_SET, dataSet);
        values.put(Groups.TITLE, label);

        final ContentResolver resolver = mContext.getContentResolver();

        // Create the new group
        final Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);

        // If there's no URI, then the insertion failed. Abort early because group members can't be
        // added if the group doesn't exist
        if (groupUri == null) {
            Log.e(TAG, "[createGroup]Couldn't create group with label " + label);
            return;
        }

        boolean isSuccess = addMembersToGroup(resolver, rawContactsToAdd, ContentUris
                .parseId(groupUri), rawContactsIndexInIcc, intent, groupIdInIcc);
        // fix ALPS921231,check if usim have been removed after save
        if (subId > 0 && !SubInfoUtils.isActiveForSubscriber(subId)) {
            Log.w(TAG, "[createGroup] Sim card is not ready");
            SimGroupUtils.showMoveUSIMGroupErrorToast(GROUP_SIM_ABSENT, subId);
            deliverCallback(callbackIntent);
            return;
        }

        // TODO: Move this into the contact editor where it belongs. This needs to be integrated
        // with the way other intent extras that are passed to the {@link ContactEditorActivity}.
        values.clear();
        values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
        values.put(GroupMembership.GROUP_ROW_ID, ContentUris.parseId(groupUri));

        Uri groupUriReture = isSuccess ? groupUri : null;
        callbackIntent.setData(groupUriReture);
        // fix ALPS00784408
        long rawContactId = intent.getLongExtra(ContactSaveService.EXTRA_RAW_CONTACTS_ID, -1);
        callbackIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_ID, rawContactId);

        callbackIntent.putExtra(ContactsContract.Intents.Insert.DATA, Lists.newArrayList(values));
        deliverCallback(callbackIntent);
    }

    private void renameGroup(Intent intent) {
        long groupId = intent.getLongExtra(ContactSaveService.EXTRA_GROUP_ID, -1);
        String label = intent.getStringExtra(ContactSaveService.EXTRA_GROUP_LABEL);

        if (groupId == -1) {
            Log.e(TAG, "[renameGroup]Invalid arguments for renameGroup request");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Groups.TITLE, label);
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        Log.d(TAG, "[renameGroup]update group uri = " + groupUri + ", values = " + values);
        mContext.getContentResolver().update(groupUri, values, null, null);

        Intent callbackIntent = intent.getParcelableExtra(ContactSaveService.EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        deliverCallback(callbackIntent);
    }

    private void deleteGroup(Intent intent) {
        // Bug Fix for CR ALPS00463033
        if (ContactSaveService.sDeleteEndListener != null) {
            ContactSaveService.sDeleteEndListener.onDeleteStart();
        }

        long groupId = intent.getLongExtra(ContactSaveService.EXTRA_GROUP_ID, -1);
        if (groupId == -1) {
            Log.e(TAG, "[deleteGroup]Invalid arguments for deleteGroup request");
            return;
        }

        //delete group in usim
        String groupLabel = intent.getStringExtra(ContactSaveService.EXTRA_GROUP_LABEL);
        int subId = intent.getIntExtra(SimGroupUtils.EXTRA_SUB_ID, -1);
        Log.i(TAG, "[deleteGroup]groupLabel:" + groupLabel + ",subId:" + subId);
        if (subId > 0 && !TextUtils.isEmpty(groupLabel)) {
            boolean success = SimGroupUtils.deleteGroupInIcc(mContext, intent, groupId);
            if (!success) {
                Log.w(TAG, "[deleteGroup] delete gorup in Icc is fail, return");
                return;
            }
        }

        mContext.getContentResolver().delete(
                ContentUris.withAppendedId(Groups.CONTENT_URI, groupId), null, null);

        // Bug Fix for CR ALPS00463033
        if (ContactSaveService.sDeleteEndListener != null) {
            ContactSaveService.sDeleteEndListener.onDeleteEnd();
        }
    }

    private void updateGroup(Intent intent) {
        long groupId = intent.getLongExtra(ContactSaveService.EXTRA_GROUP_ID, -1);
        String label = intent.getStringExtra(ContactSaveService.EXTRA_GROUP_LABEL);
        long[] rawContactsToAdd = intent.getLongArrayExtra(
                ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD);
        long[] rawContactsToRemove = intent.getLongArrayExtra(
                ContactSaveService.EXTRA_RAW_CONTACTS_TO_REMOVE);

        if (groupId == -1) {
            Log.e(TAG, "[updateGroup]Invalid arguments for updateGroup request");
            return;
        }

        Intent callbackIntent = intent.getParcelableExtra(ContactSaveService.EXTRA_CALLBACK_INTENT);
        //check group exist
        String accountType = intent.getStringExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE);
        String accountName = intent.getStringExtra(ContactSaveService.EXTRA_ACCOUNT_NAME);
        if (groupId > 0 && label != null && !SimGroupUtils.checkGroupNameExist(
                    mContext, label, accountName, accountType)) {
            Log.w(TAG, "[updateGroup] Group Name exist!");
            callbackIntent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.RELOAD);
            deliverCallback(callbackIntent);
            return;
        }

        // update usim first
        int[] rawContactsToAddIndexInIcc = intent
                .getIntArrayExtra(SimGroupUtils.EXTRA_SIM_INDEX_TO_ADD);
        int subId = intent.getIntExtra(SimGroupUtils.EXTRA_SUB_ID, -1);
        int groupIdInIcc = -1;
        if (subId > 0) {
            groupIdInIcc = SimGroupUtils.updateGroupToIcc(mContext, intent);
            if (groupIdInIcc < 0) {
                Log.w(TAG, "[updateGroup] groupIdInIcc fail!");
                callbackIntent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, SaveMode.RELOAD);
                deliverCallback(callbackIntent);
                return;
            }
        }

        final ContentResolver resolver = mContext.getContentResolver();
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);

        // Update group name if necessary
        if (label != null) {
            ContentValues values = new ContentValues();
            values.put(Groups.TITLE, label);
            resolver.update(groupUri, values, null, null);
        }

        // Add and remove members if necessary
        int[] simIndexToAddArray = intent
                .getIntArrayExtra(SimGroupUtils.EXTRA_SIM_INDEX_TO_ADD);
        int[] simIndexToRemoveArray = intent
                .getIntArrayExtra(SimGroupUtils.EXTRA_SIM_INDEX_TO_REMOVE);
        boolean isRemoveSuccess = removeMembersFromGroup(resolver, rawContactsToRemove, groupId,
                simIndexToRemoveArray, subId, groupIdInIcc);
        boolean isAddSuccess = addMembersToGroup(resolver, rawContactsToAdd, groupId,
                rawContactsToAddIndexInIcc, intent, groupIdInIcc);
        // fix ALPS921231 check if sim removed after save
        if (subId > 0 && !SubInfoUtils.isActiveForSubscriber(subId)) {
            Log.w(TAG, "[updateGroup] Find sim not ready");
            SimGroupUtils.showMoveUSIMGroupErrorToast(GROUP_SIM_ABSENT, subId);
            deliverCallback(callbackIntent);
            return;
        }

        // make sure both remove and add are successful
        Log.i(TAG, "[updateGroup]isAddSuccess:" + isAddSuccess + ",groupUri:" + groupUri);
        Uri groupUriReture = isRemoveSuccess && isAddSuccess ? groupUri : null;

        callbackIntent.setData(groupUriReture);
        deliverCallback(callbackIntent);
    }

    /**
     * true if all are ok,false happened some errors.
     */
    private static boolean addMembersToGroup(ContentResolver resolver, long[] rawContactsToAdd,
            long groupId, int[] rawContactsIndexInIcc, Intent intent, int groupIdInIcc) {
        boolean isAllOk = true;
        if (rawContactsToAdd == null) {
            Log.e(TAG, "[addMembersToGroup rawContactsToAdd = null]");
            return false;
        }
        // add members to usim
        int subId = intent.getIntExtra(SimGroupUtils.EXTRA_SUB_ID, -1);
        int i = -1;
        for (long rawContactId : rawContactsToAdd) {
            try {
                // add members to usim first
                i++;
                if (subId > 0 && groupIdInIcc >= 0 && rawContactsIndexInIcc[i] >= 0) {
                    int simIndex = rawContactsIndexInIcc[i];
                    boolean success = ContactsGroupUtils.USIMGroup.addUSIMGroupMember(subId,
                            simIndex, groupIdInIcc);
                    if (!success) {
                        isAllOk = false;
                        Log.w(TAG, "[addMembersToGroup] fail simIndex:" + simIndex
                                + ",groupId:" + groupId);
                        continue;
                    }
                }

                final ArrayList<ContentProviderOperation> rawContactOperations =
                        new ArrayList<ContentProviderOperation>();

                // Build an assert operation to ensure the contact is not already in the group
                final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
                        .newAssertQuery(Data.CONTENT_URI);
                assertBuilder.withSelection(Data.RAW_CONTACT_ID + "=? AND " +
                        Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                        new String[] { String.valueOf(rawContactId),
                        GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
                assertBuilder.withExpectedCount(0);
                rawContactOperations.add(assertBuilder.build());

                // Build an insert operation to add the contact to the group
                final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                        .newInsert(Data.CONTENT_URI);
                insertBuilder.withValue(Data.RAW_CONTACT_ID, rawContactId);
                insertBuilder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                insertBuilder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
                rawContactOperations.add(insertBuilder.build());

                // Apply batch
                if (!rawContactOperations.isEmpty()) {
                    resolver.applyBatch(ContactsContract.AUTHORITY, rawContactOperations);
                }
            } catch (RemoteException e) {
                // Something went wrong, bail without success
                Log.e(TAG, "[addMembersToGroup]Problem persisting user edits for raw contact ID " +
                        String.valueOf(rawContactId), e);
                isAllOk = false;
            } catch (OperationApplicationException e) {
                // The assert could have failed because the contact is already in the group,
                // just continue to the next contact
                Log.w(TAG, "[addMembersToGroup] Assert failed in adding raw contact ID " +
                        String.valueOf(rawContactId) + ". Already exists in group " +
                        String.valueOf(groupId), e);
                isAllOk = false;
            }
        }
        return isAllOk;
    }

    // To remove USIM group members and contactsProvider if necessary.
    private boolean removeMembersFromGroup(ContentResolver resolver, long[] rawContactsToRemove,
            long groupId, int[] simIndexArray, int subId, int ugrpId) {
        boolean isRemoveSuccess = true;
        if (rawContactsToRemove == null) {
            Log.w(TAG, "[removeMembersFromGroup]RawContacts to be removed is empty!");
            return isRemoveSuccess;
        }

        int simIndex;
        int i = -1;
        for (long rawContactId : rawContactsToRemove) {
            // remove group member from icc card
            i++;
            simIndex = simIndexArray[i];
            boolean ret = false;
            if (subId > 0 && simIndex >= 0 && ugrpId >= 0) {
                ret = ContactsGroupUtils.USIMGroup.deleteUSIMGroupMember(subId, simIndex, ugrpId);
                if (!ret) {
                    isRemoveSuccess = false;
                    Log.i(TAG, "[removeMembersFromGroup]Remove failed RawContactid: "
                            + rawContactId);
                    continue;
                }
            }

            // Apply the delete operation on the data row for the given raw contact's
            // membership in the given group. If no contact matches the provided selection, then
            // nothing will be done. Just continue to the next contact.
            resolver.delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND " +
                    Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                    new String[] { String.valueOf(rawContactId),
                    GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
        }
        return isRemoveSuccess;
    }

    private void deliverCallbackOnUiThread(final Intent intent) {
        Log.d(TAG, "[deliverCallbackOnUiThread] callbackIntent call onSimGroupCompleted");
        for (final Listener listener : sListeners) {
            Handler handler = sListenerHolder.get(listener);
            if (handler != null) {
                handler.post(new Runnable() {
                    public void run() {
                        listener.onSimGroupCompleted(intent);
                    }
                });
            }
        }
    }

    private void deliverCallback(Intent callbackIntent) {
        deliverCallbackOnUiThread(callbackIntent);
    }

}
