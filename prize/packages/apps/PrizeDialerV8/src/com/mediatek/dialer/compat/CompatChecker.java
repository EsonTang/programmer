package com.mediatek.dialer.compat;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.android.contacts.common.util.PermissionsUtil;

/**
 * [portable]Utility class to check whether the columns really existed in db.
 * only need run one time.
 */
public class CompatChecker {
    private static final String TAG = CompatChecker.class.getSimpleName();
    private static CompatChecker sSingleton;
    private Context mContext;

    protected CompatChecker(Context context) {
        mContext = context;
    }

    /**
     * get the singleton instance of CompatChecker.
     */
    public static synchronized CompatChecker getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new CompatChecker(context.getApplicationContext());
        }
        return sSingleton;
    }

    /**
     * start the database columns check in the background.
     */
    public void startCheckerThread() {
        if (PermissionsUtil.hasContactsPermissions(mContext)) {
            new SimContactAsyncTask().execute();
        }
        if (PermissionsUtil.hasPhonePermissions(mContext)) {
            new CallsAsyncTask().execute();
        }
    }

    private class SimContactAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            checkSimContacts();
            return null;
        }
    }

    private class CallsAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            checkIpPrefix();
            checkConfCallLog();
            return null;
        }
    }

    private void checkSimContacts() {
        Cursor cursor = null;
        try {
            String[] projection = new String[] { Contacts.INDICATE_PHONE_SIM };
            cursor = mContext.getContentResolver().query(Contacts.CONTENT_URI, projection,
                    Contacts._ID + "=1", null, null);
            // if no exception means it supports INDICATE_PHONE_SIM
            DialerCompatEx.setSimContactsCompat(true);
        } catch (IllegalArgumentException e) {
            // if exception means it not support INDICATE_PHONE_SIM
            DialerCompatEx.setSimContactsCompat(false);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void checkIpPrefix() {
        Cursor callCursor = null;
        try {
            callCursor = mContext.getContentResolver().query(Calls.CONTENT_URI,
                    new String[] { Calls.IP_PREFIX }, Calls._ID + "=1", null, null);
            // if no exception means it supports the columns
            DialerCompatEx.setIpPrefixCompat(true);
        } catch (IllegalArgumentException e) {
            DialerCompatEx.setIpPrefixCompat(false);
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    private void checkConfCallLog() {
        Cursor callCursor = null;
        try {
            callCursor = mContext.getContentResolver().query(Calls.CONTENT_URI,
                    new String[] { Calls.CONFERENCE_CALL_ID }, Calls._ID + "=1", null, null);
            // if no exception means it supports the columns
            DialerCompatEx.setConferenceCallLogCompat(true);
        } catch (IllegalArgumentException e) {
            DialerCompatEx.setConferenceCallLogCompat(false);
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }
}
