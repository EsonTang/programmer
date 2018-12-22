/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mediatek.providers.contacts;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.CallLog.ConferenceCalls;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.providers.contacts.CallLogDatabaseHelper;
import com.android.providers.contacts.CallLogDatabaseHelper.Tables;
import com.android.providers.contacts.ContactsProvider2;
import com.android.providers.contacts.DatabaseModifier;
import com.android.providers.contacts.DbModifierWithNotification;
import com.android.providers.contacts.VoicemailPermissions;
import com.android.providers.contacts.util.UserUtils;
import com.mediatek.providers.contacts.CallLogSearchSupport;
import com.mediatek.providers.contacts.ContactsProviderUtils;
import com.mediatek.providers.contacts.LogUtils;

/**
 * Call log content provider.
 */
public class CallLogProviderEx {
    private static final String TAG = CallLogProviderEx.class.getSimpleName();

    private final Context mContext;
    private static final int CALLS_SEARCH_FILTER = 4;
    private static final int CONFERENCE_CALLS = 5;
    private static final int CONFERENCE_CALLS_ID = 6;
    private static final int SEARCH_SUGGESTIONS = 10001;
    private static final int SEARCH_SHORTCUT = 10002;
    private CallLogSearchSupport mCallLogSearchSupport;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        sURIMatcher.addURI(CallLog.AUTHORITY, "calls/search_filter/*", CALLS_SEARCH_FILTER);
        sURIMatcher.addURI(CallLog.AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGESTIONS);
        sURIMatcher.addURI(CallLog.AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGESTIONS);
        sURIMatcher.addURI(CallLog.AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH_SHORTCUT);
        sURIMatcher.addURI(CallLog.AUTHORITY, "conference_calls", CONFERENCE_CALLS);
        sURIMatcher.addURI(CallLog.AUTHORITY, "conference_calls/#", CONFERENCE_CALLS_ID);
    }

    private VoicemailPermissions mVoicemailPermissions;
    private static CallLogProviderEx sCallLogProviderEx;

    private CallLogProviderEx(Context context) {
        mContext = context;
    }

    public static synchronized CallLogProviderEx getInstance(Context context) {
        if (sCallLogProviderEx == null) {
            sCallLogProviderEx = new CallLogProviderEx(context);
            sCallLogProviderEx.initialize();
        }
        return sCallLogProviderEx;
    }

    private void initialize() {
        mVoicemailPermissions = new VoicemailPermissions(mContext);
        mCallLogSearchSupport = new CallLogSearchSupport(mContext);
    }

    public Cursor queryCallLog(SQLiteDatabase db, SQLiteQueryBuilder qb, Uri uri,
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String groupBy = null;
        final int match = sURIMatcher.match(uri);
        LogUtils.d(TAG, "queryCallLog match == " + match);
        switch (match) {
            case CALLS_SEARCH_FILTER:
                String query = uri.getPathSegments().get(2);
                String nomalizeName = query;//NameNormalizer.normalize(query);
                /// M: keep this logic same with CallogManager's logCall, just strip separators with
                /// phone number before query happen, don't normalize it totally.
                /// Otherwise, some non-separator char in calllog can't be searched
                /// like "*" "#", etc. @{
                String number = query;
                if (!TextUtils.isEmpty(query)
                        && ContactsProvider2.countPhoneNumberDigits(query) > 0) {
                    number = PhoneNumberUtils.stripSeparators(query);
                }
                /// @}
                StringBuilder sb = new StringBuilder();
                sb.append(Calls.NUMBER + " GLOB '*" + number + "*'");
                sb.append(" OR (" + Calls.CACHED_NAME + " GLOB '*" + nomalizeName + "*')");
                qb.appendWhere(sb);
                groupBy = Calls._ID;

                LogUtils.d(TAG, " CALLS_SEARCH_FILTER" + " query=" + query
                        + ", sb=" + sb.toString());
                break;
            case CONFERENCE_CALLS_ID:
                LogUtils.d(TAG, "CONFERENCE_CALLS_ID. Uri:" + uri);
                long confCallId = ContentUris.parseId(uri);
                qb.appendWhere(Calls.CONFERENCE_CALL_ID + "=" + confCallId);
                break;
            // here, query real conference_calls table
            case CONFERENCE_CALLS:
                LogUtils.d(TAG, "CONFERENCE_CALLS");
                qb.setTables(Tables.CONFERENCE_CALLS);
                qb.setProjectionMap(null);
                break;
            case SEARCH_SUGGESTIONS:
                LogUtils.d(TAG, "SEARCH_SUGGESTIONS");
                return mCallLogSearchSupport.handleSearchSuggestionsQuery(db, uri, getLimit(uri));
            case SEARCH_SHORTCUT: {
                LogUtils.d(TAG, "SEARCH_SHORTCUT. Uri:" + uri);
                String callId = uri.getLastPathSegment();
                String filter = uri.getQueryParameter(
                        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                return mCallLogSearchSupport.handleSearchShortcutRefresh(db, projection,
                        callId, filter);
            }
        }

        final int limit = getIntParam(uri, Calls.LIMIT_PARAM_KEY, 0);
        final int offset = getIntParam(uri, Calls.OFFSET_PARAM_KEY, 0);
        String limitClause = null;
        if (limit > 0) {
            limitClause = offset + "," + limit;
        }

        final Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy ,
                null, sortOrder, limitClause );

        if (c != null) {
            c.setNotificationUri(mContext.getContentResolver(), CallLog.CONTENT_URI);
            LogUtils.d(TAG, "queryCallLog count == " + c.getCount());
        }
        return c;
    }

    public Uri insertConferenceCall(SQLiteDatabase db, Uri uri, ContentValues values) {
        if (CONFERENCE_CALLS == sURIMatcher.match(uri)) {
            final long confCallId = db.insert(
                    Tables.CONFERENCE_CALLS, ConferenceCalls.GROUP_ID, values);

            if (confCallId < 0) {
                LogUtils.w(TAG, "Insert Conference Call Failed, Uri:" + uri);
                return null;
            }
            return ContentUris.withAppendedId(uri, confCallId);
        }
        return null;
    }

    /**
     * delete the entry in conference_calls table, and its child call log in
     * calls table
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int deleteConferenceCalls(SQLiteDatabase db, Uri uri, String selection,
            String[] selectionArgs) {
        int count = 0;

        // query the conference_calls table and save their ids.
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Tables.CONFERENCE_CALLS);
        Cursor conferenceCursor = qb.query(db, new String[] { ConferenceCalls._ID }, selection,
                selectionArgs, null, null, null, null);
        // save conference_calls' ids
        StringBuilder conferenceIds = new StringBuilder();
        while (conferenceCursor.moveToNext()) {
            conferenceIds.append(conferenceCursor.getString(0)).append(",");
        }
        try {
            db.beginTransaction();
            // delete the conference calls
            count = getDatabaseModifier(db).delete(Tables.CONFERENCE_CALLS, selection,
                    selectionArgs);

            // delete their child call log
            if (conferenceIds.length() > 0) {
                // remove the last ',' here
                conferenceIds.replace(conferenceIds.length() - 1, conferenceIds.length(), "");
                String deleteSelection = Calls.CONFERENCE_CALL_ID + " IN ("
                        + conferenceIds.toString() + ")";
                int deletedCounts = db.delete(Tables.CALLS, deleteSelection, null);
                LogUtils.d(TAG, "[deleteConferenceCalls] deleteSelection=" + deleteSelection
                        + "; delete related Calls count=" + deletedCounts);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (conferenceCursor != null) {
                conferenceCursor.close();
            }
        }
        LogUtils.d(TAG, "[deleteConferenceCalls] delete Conference Calls. count: " + count);
        return count;
    }

    private static final boolean DBG_DIALER_SEARCH = ContactsProviderUtils.DBG_DIALER_SEARCH;

    private void notifyDialerSearchChange() {
        mContext.getContentResolver().notifyChange(
                ContactsContract.AUTHORITY_URI.buildUpon().appendPath("dialer_search")
                        .appendPath("call_log").build(), null, false);
    }

    // send new Calls broadcast to launcher to update unread icon
    public static final void notifyNewCallsCount(Context context) {
        SQLiteDatabase db = null;
        Cursor c = null;
        int newCallsCount = 0;
        try {
            db = getDatabaseHelper(context).getReadableDatabase();

            if (db == null || context == null) {
                LogUtils.w(TAG, "[notifyNewCallsCount] Cannot notify with null db or context.");
                return;
            }

            c = db.rawQuery("SELECT count(*) FROM " + Tables.CALLS
                    + " WHERE " + Calls.TYPE + " in (" + Calls.MISSED_TYPE
                    + "," + Calls.VOICEMAIL_TYPE
                    + ") AND " + Calls.NEW + "=1", null);

            if (c != null && c.moveToFirst()) {
                newCallsCount = c.getInt(0);
            }
        } catch (SQLiteException e) {
            LogUtils.w(TAG, "[notifyNewCallsCount] SQLiteException:" + e);
            return;
        } finally {
            if (c != null) {
                c.close();
            }
        }

        LogUtils.i(TAG, "[notifyNewCallsCount] newCallsCount = " + newCallsCount);
        //send count=0 to clear the unread icon
        if (newCallsCount >= 0) {
            Intent newIntent = new Intent(Intent.ACTION_UNREAD_CHANGED);
            newIntent.putExtra(Intent.EXTRA_UNREAD_NUMBER, newCallsCount);
            newIntent.putExtra(Intent.EXTRA_UNREAD_COMPONENT,
                    new ComponentName(ConstantsUtils.CONTACTS_PACKAGE,
                    ConstantsUtils.CONTACTS_DIALTACTS_ACTIVITY));
            context.sendBroadcast(newIntent);
            // use the public key CONTACTS_UNREAD_KEY that statement in Setting Provider.com_android_contacts_mtk_unread
            Settings.System.putInt(context.getContentResolver(),
                    "com_android_contacts_mtk_unread", Integer.valueOf(newCallsCount));
        }
    }

    protected static CallLogDatabaseHelper getDatabaseHelper(final Context context) {
        return CallLogDatabaseHelper.getInstance(context);
    }

    /**
     * Returns a {@link DatabaseModifier} that takes care of sending necessary notifications
     * after the operation is performed.
     */
    private DatabaseModifier getDatabaseModifier(SQLiteDatabase db) {
        return new DbModifierWithNotification(Tables.CALLS, db, mContext);
    }

    /**
     * Same as {@link #getDatabaseModifier(SQLiteDatabase)} but used for insert helper operations
     * only.
     */
    private DatabaseModifier getDatabaseModifier(DatabaseUtils.InsertHelper insertHelper) {
        return new DbModifierWithNotification(Tables.CALLS, insertHelper, mContext);
    }

    /**
     * Gets the value of the "limit" URI query parameter.
     *
     * @return A string containing a non-negative integer, or <code>null</code> if
     *         the parameter is not set, or is set to an invalid value.
     */
    public String getLimit(Uri uri) {
        String limitParam = uri.getQueryParameter("limit");
        if (limitParam == null) {
            return null;
        }
        // make sure that the limit is a non-negative integer
        try {
            int l = Integer.parseInt(limitParam);
            if (l < 0) {
                Log.w(TAG, "Invalid limit parameter: " + limitParam);
                return null;
            }
            return String.valueOf(l);
        } catch (NumberFormatException ex) {
            Log.w(TAG, "Invalid limit parameter: " + limitParam);
            return null;
        }
    }

    /**
     * Gets an integer query parameter from a given uri.
     *
     * @param uri The uri to extract the query parameter from.
     * @param key The query parameter key.
     * @param defaultValue A default value to return if the query parameter does not exist.
     * @return The value from the query parameter in the Uri.  Or the default value if the parameter
     * does not exist in the uri.
     * @throws IllegalArgumentException when the value in the query parameter is not an integer.
     */
    private int getIntParam(Uri uri, String key, int defaultValue) {
        String valueString = uri.getQueryParameter(key);
        if (valueString == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            String msg = "Integer required for " + key + " parameter but value '" + valueString +
                    "' was found instead.";
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * M: create conference calls table.
     * @param db
     */
    public static void createConferenceCallsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CONFERENCE_CALLS);
        db.execSQL("CREATE TABLE " + Tables.CONFERENCE_CALLS + " (" +
                ConferenceCalls._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ConferenceCalls.GROUP_ID + " INTEGER," +
                ConferenceCalls.CONFERENCE_DATE + " INTEGER " +
        ");");
    }

    /**
     * Drop dialer search tables, it is out of date.
     * @param db the writable database
     */
    public static void dropDialerSearchTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS dialer_search;");
        db.execSQL("DROP VIEW IF EXISTS view_dialer_search;");
    }

    /**
     * remove the duplicated call log for non-owner user before insert.
     * @param dbHelper db helper
     * @param values call log ContentValues
     */
    public void removeDuplictedCallLogForUser(CallLogDatabaseHelper dbHelper,
            ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final UserManager userManager = UserUtils.getUserManager(mContext);
        final String[] args = new String[3];
        args[0] = values.getAsString(Calls.DATE);
        args[1] = values.getAsString(Calls.NUMBER);
        args[2] = values.getAsString(Calls.TYPE);
        String selection = Calls.DATE + " = ? AND " + Calls.NUMBER + " = ? AND " + Calls.TYPE
                + " = ?";
        if (userManager != null && userManager.getUserHandle() != UserHandle.USER_OWNER
                && (DatabaseUtils.queryNumEntries(db, Tables.CALLS, selection, args) > 0)) {
            int count = getDatabaseModifier(db).delete(Tables.CALLS, selection, args);
            Log.i(TAG, "removeDuplictedCallLogForUser, delete count=" + count);
        }
    }
}
