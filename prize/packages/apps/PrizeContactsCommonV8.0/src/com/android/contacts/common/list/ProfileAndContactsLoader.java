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
package com.android.contacts.common.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;

import com.google.android.collect.Lists;
import com.mediatek.contacts.util.ContactsCommonListUtils;
import com.mediatek.contacts.util.ContactsPortableUtils;

import java.util.List;

/**
 * A loader for use in the default contact list, which will also query for the user's profile
 * if configured to do so.
 */
public class ProfileAndContactsLoader extends CursorLoader {

    private boolean mLoadProfile;

    private String[] mProjection;

    private Uri mExtraUri;
    private String[] mExtraProjection;
    private String mExtraSelection;
    private String[] mExtraSelectionArgs;
    private boolean mMergeExtraContactsAfterPrimary;

    public ProfileAndContactsLoader(Context context) {
        super(context);
    }

    /** Whether to load the profile and merge results in before any other results. */
    public void setLoadProfile(boolean flag) {
        mLoadProfile = flag;
    }

    public void setProjection(String[] projection) {
        super.setProjection(projection);
        mProjection = projection;
    }

    /** Configure an extra query and merge results in before the primary results. */
    public void setLoadExtraContactsFirst(Uri uri, String[] projection) {
        mExtraUri = uri;
        mExtraProjection = projection;
        mMergeExtraContactsAfterPrimary = false;
    }

    /** Configure an extra query and merge results in after the primary results. */
    public void setLoadExtraContactsLast(Uri uri, String[] projection, String selection,
            String[] selectionArgs) {
        mExtraUri = uri;
        mExtraProjection = projection;
        mExtraSelection = selection;
        mExtraSelectionArgs = selectionArgs;
        mMergeExtraContactsAfterPrimary = true;
    }

    private boolean canLoadExtraContacts() {
        return mExtraUri != null && mExtraProjection != null;
    }

    @Override
    public Cursor loadInBackground() {
    	android.util.Log.d(TAG,"[loadInBackground] mLoadProfile = "+mLoadProfile);//prize-add log for bug[51853] hpf-2018-3-9
        // First load the profile, if enabled.
        List<Cursor> cursors = Lists.newArrayList();
        if (/*mLoadProfile*/false) { //prize-change-huangliemin-2016-7-26
            cursors.add(loadProfile());
        }
        if (canLoadExtraContacts() && !mMergeExtraContactsAfterPrimary) {
            cursors.add(loadExtraContacts());
        }
        /** M: New Feature SDN @{ */
        mSdnContactCount = 0;
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            mSdnContactCount = ContactsCommonListUtils.addCursorAndSetSelection(getContext(),
                    this, cursors, mSdnContactCount);
        }
        /** @} */

        // ContactsCursor.loadInBackground() can return null; MergeCursor
        // correctly handles null cursors.
        Cursor cursor = null;
        try {
            cursor = super.loadInBackground();
        } catch (NullPointerException | SecurityException e) {
            // Ignore NPEs and SecurityExceptions thrown by providers
        }
        final Cursor contactsCursor = cursor;
        cursors.add(contactsCursor);
        if (canLoadExtraContacts() && mMergeExtraContactsAfterPrimary) {
            cursors.add(loadExtraContacts());
        }
        /*prize-add log for bug[51853] hpf-2018-3-9-start*/
        Cursor c = new MergeCursor(cursors.toArray(new Cursor[cursors.size()])) {
            @Override
            public Bundle getExtras() {
                // Need to get the extras from the contacts cursor.
                //prize modify for bug 54802 by zhaojian 20180408 start
                //return contactsCursor == null ? new Bundle() : contactsCursor.getExtras();
                return (contactsCursor == null || contactsCursor.isClosed()) ? new Bundle() : contactsCursor.getExtras();
                //prize modify for bug 54802 by zhaojian 20180408 end
            }
        };
        android.util.Log.i(TAG,"[loadInBackground]  return new MergeCursor...  c = "+c);
        return c;
        /*prize-add log for bug[51853] hpf-2018-3-9-end*/
    }

    /**
     * Loads the profile into a MatrixCursor. On failure returns null, which
     * matches the behavior of CursorLoader.loadInBackground().
     *
     * @return MatrixCursor containing profile or null on query failure.
     */
    private MatrixCursor loadProfile() {
        Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, mProjection,
                null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            MatrixCursor matrix = new MatrixCursor(mProjection);
            Object[] row = new Object[mProjection.length];
            while (cursor.moveToNext()) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = cursor.getString(i);
                }
                matrix.addRow(row);
            }
            return matrix;
        } finally {
            cursor.close();
        }
    }

    private Cursor loadExtraContacts() {
        return getContext().getContentResolver().query(
                mExtraUri, mExtraProjection, mExtraSelection, mExtraSelectionArgs, null);
    }

    /** M: modify. @{ */
    private static final String TAG = "ProfileAndContactsLoader";
    private int mSdnContactCount = 0;

    public int getSdnContactCount() {
        return this.mSdnContactCount;
    }

    @Override
    protected void onStartLoading() {
    	android.util.Log.i(TAG,"[onStartLoading]");
        forceLoad();
    }
    /** @} */
    
    /*prize-add some log for bug[51853] hpf-2018-3-9-start*/
    @Override
    protected void onStopLoading() {
    	super.onStopLoading();
    	android.util.Log.i(TAG,"[onStopLoading]");
    }

    @Override
    public void onCanceled(Cursor cursor) {
    	super.onCanceled(cursor);
    	android.util.Log.i(TAG,"[onCanceled]");
    }

    @Override
    protected void onReset() {
        super.onReset();
        android.util.Log.i(TAG,"[onReset]");
    }
    /*prize-add some log for bug[51853] hpf-2018-3-9-end*/
}
