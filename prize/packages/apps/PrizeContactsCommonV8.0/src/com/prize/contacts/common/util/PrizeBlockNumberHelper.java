package com.prize.contacts.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import java.util.List;
import android.widget.Toast;//PRIZE-add-yuandailin-2016-6-12

public class PrizeBlockNumberHelper {
	private static PrizeBlockNumberHelper prizeBlockNumberHelper = null;
	private static final String[] BLACK_LIST_PROJECTION = { "Number" };
	private final Uri CALLREJECT_URI = Uri.parse("content://reject/list");
	private Context mContext;

	private PrizeBlockNumberHelper(Context context) {
		mContext = context;
	}

	public static PrizeBlockNumberHelper getInstance(Context context) {

		if (prizeBlockNumberHelper == null) {
			prizeBlockNumberHelper = new PrizeBlockNumberHelper(context);
		}
		return prizeBlockNumberHelper;
	}

	public void insertNumber(String number) {
		final String mNumber = number.replaceAll(" ", "");
		ContentValues contentValues = new ContentValues();
		contentValues.put("Number", mNumber);
		mContext.getContentResolver().insert(CALLREJECT_URI, contentValues);
		Toast.makeText(mContext,
				mNumber + mContext.getResources().getString(com.android.contacts.common.R.string.insert_number_succes),
				Toast.LENGTH_SHORT).show();// PRIZE-add-yuandailin-2016-6-12

	}

	public void insertNumberList(List list) {
		if (list != null && list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				insertNumber((String) list.get(i));
			}
		}

	}

	public void deleteNumber(String number) {

		final String mNumber = number.replaceAll(" ", "");
		mContext.getContentResolver().delete(CALLREJECT_URI, "number=?", new String[] { mNumber });
		Toast.makeText(mContext,
				mNumber + mContext.getResources().getString(com.android.contacts.common.R.string.delete_number_succes),
				Toast.LENGTH_SHORT).show();// PRIZE-add-yuandailin-2016-6-12

	}

	public void deleteNumberList(List list) {
		if (list != null && list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				deleteNumber((String) list.get(i));
			}
		}
	}

	public boolean isNumberInBlacklist(String number) {
		boolean result = false;
		Cursor cursor = null;
		try{
			cursor = mContext.getContentResolver().query(CALLREJECT_URI, BLACK_LIST_PROJECTION, null, null, null);
			if (cursor == null || cursor.moveToFirst() == false) {
				return false;
			}
			String blockNumber;
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				blockNumber = cursor.getString(0);
				if (blockNumber != null && blockNumber.equals(number)) {
					result = true;
					break;
				}
				cursor.moveToNext();
			}
		}catch(Exception e){
			
		}finally {
			if(cursor != null){
				cursor.close();
			}
		}
		
		return result;
	}

	public void deleteSingleCallLog(String number, String callid) {
		final String mNumber = number;
		final String mCallid = callid;
		Thread deleteThread = new Thread() {
			@Override
			public void run() {
				mContext.getContentResolver().delete(android.provider.CallLog.Calls.CONTENT_URI, "number=? and  _id= ?",
						new String[] { mNumber, mCallid });
			}
		};
		deleteThread.start();
	}

}
