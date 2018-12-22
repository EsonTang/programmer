package com.android.contacts.prize;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import com.android.contacts.R;
import android.util.Log;

/**
 * Create by huangpengfei on 2018-2-6
 */
public class PrizeBootBroadcastReceiver extends BroadcastReceiver {

	private final String TAG = "PrizeBootBroadcastReceiver";
	private boolean sIsRunningNumberCheck = false;
	public final String ACCOUNT_NAME_LOCAL_PHONE = "Phone";
	public final String ACCOUNT_TYPE_LOCAL_PHONE = "Local Phone Account";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		addDefaultGroup(context);
	}

	private void addDefaultGroup(final Context context) {
		Log.i(TAG, "[addDefaultGroup]...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "isRunningNumberCheck before:" + sIsRunningNumberCheck);
				if (sIsRunningNumberCheck) {
					return;
				}
				sIsRunningNumberCheck = true;
				Log.i(TAG, "isRunningNumberCheck after:" + sIsRunningNumberCheck);
				Uri uri = Groups.CONTENT_URI;
				Cursor groupCursor = context.getContentResolver().query(uri, new String[] { Groups._ID, Groups.TITLE },
						Groups.DELETED + "=0", null, null);
				try {
					if (groupCursor != null && groupCursor.getCount() > 0) {
						return;
					} else {
						String defaultGroups[] = { 
								context.getResources().getString(R.string.prize_worker),
								context.getResources().getString(R.string.prize_family),
								context.getResources().getString(R.string.prize_friends), 
								context.getResources().getString(R.string.prize_schoolmate)};
						final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
						ContentValues groupValues = new ContentValues();
						for (int i = 0; i < defaultGroups.length; i++) {
							groupValues.put(Groups.ACCOUNT_NAME, ACCOUNT_NAME_LOCAL_PHONE);
							groupValues.put(Groups.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL_PHONE);
							groupValues.put(Groups.TITLE, defaultGroups[i]);
							groupValues.put(Groups.GROUP_IS_READ_ONLY, 1);
							builder.withValues(groupValues);
							operationList.add(builder.build());
							try {
								context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
								groupValues.clear();
								operationList.clear();
							} catch (RemoteException e) {
								Log.e(TAG, String.format("%s:%s", e.toString(), e.getMessage()));
							} catch (OperationApplicationException e) {
								Log.e(TAG, String.format("%s:%s", e.toString(), e.getMessage()));
							}
						}
					}
				} finally {
					if (groupCursor != null) {
						groupCursor.close();
					}
				}
				Log.i(TAG, "isRunningNumberCheck insert:" + sIsRunningNumberCheck);
				sIsRunningNumberCheck = false;
			}
		}).start();
	}

}
