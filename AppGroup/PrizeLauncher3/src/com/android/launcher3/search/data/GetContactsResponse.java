package com.android.launcher3.search.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;

public class GetContactsResponse extends RSTResponse {

	public GetContactsResponse(Context mContext,
			List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,
			HashMap<String, AsyncTaskCallback> groupClass, List<String> groups) {
		super(mContext, mGroupBeanList, mGroupChildList, groupTitle, adpter, r,
				groupClass, groups);
		// TODO Auto-generated constructor stub
	}

	public static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION = "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

	// 查询所有联系人的姓名，电话，邮箱
	public List<GroupMemberBean> getContact() throws Exception {
		List<GroupMemberBean> list = new ArrayList<GroupMemberBean>();

		Uri uri = Uri.parse("content://com.android.contacts/contacts");
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { "_id" }, null, null,
				null);
		while (cursor.moveToNext()) {
			int contractID = cursor.getInt(0);
			StringBuilder sb = new StringBuilder("contractID=");
			sb.append(contractID);
			uri = Uri.parse("content://com.android.contacts/contacts/"
					+ contractID + "/data");
			Cursor cursor1 = resolver.query(uri, new String[] { Data.MIMETYPE,
					Data.DATA1, Data.DATA2, Contacts.LOOKUP_KEY,
					Contacts.Entity.RAW_CONTACT_ID, Contacts.Entity.CONTACT_ID,
					Contacts.DISPLAY_NAME, Contacts.PHOTO_ID }, null, null,
					null);
			GroupMemberBean childBean = null;
			ContactsBean contact = null;
			while (cursor1.moveToNext()) {
				String number = cursor1.getString(cursor1
						.getColumnIndex(Data.DATA1));
				String mimeType = cursor1.getString(cursor1
						.getColumnIndex(Data.MIMETYPE));
				contact = new ContactsBean();
				contact.mineType = mimeType;
				contact.data1 = number;
				contact.data2 = cursor1.getString(cursor1
						.getColumnIndex(Data.DATA2));
				contact.lookup_key = cursor1.getString(cursor1
						.getColumnIndex(Data.LOOKUP_KEY));
				contact.contact_id = Long.valueOf(cursor1.getString(cursor1
						.getColumnIndex(Contacts.Entity.CONTACT_ID)));
				contact.raw_contact_id = cursor1.getString(cursor1
						.getColumnIndex(Contacts.Entity.RAW_CONTACT_ID));
				contact.display_name = cursor1.getString(cursor1 
						.getColumnIndex(Contacts.DISPLAY_NAME));
				contact.photo_id = cursor1.getString(cursor1
						.getColumnIndex(Contacts.PHOTO_ID));

				contact.data1 = number;
				contact.setLookupUri();

				contact.contentType = mContext.getContentResolver().getType(
						contact.lookupUri);

				contact.intent = createContactShortcutIntent(contact.lookupUri,
						contact.contentType, contact.display_name);

				Bitmap b = loadImageFromUrl(mContext, contact.photo_id);
				if ("vnd.android.cursor.item/name".equals(mimeType)) { // 是姓名
					childBean = this.fillData(number);
				} else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { // 手机

					childBean = this.fillData(number);
				}
				childBean.setContacts(contact);
				Drawable icon = new BitmapDrawable(b);
				/*if (b == null) {
					icon = mContext
							.getResources()
							.getDrawable(
									R.drawable.com_android_contacts_activities_peopleactivity);
				}*/
				childBean.setIcon(icon);
				mTask.doPublishProgress(childBean);
				list.add(childBean);
			}
			cursor1.close();
		}
		cursor.close();
		return list;
	}

	public synchronized static Bitmap loadImageFromUrl(Context ct,
			String photo_id) {
		Bitmap d = null;
		if (photo_id == null || photo_id.equals(""))
			return d;
		try {
			String[] projection = new String[] { ContactsContract.Data.DATA15 };
			String selection = "ContactsContract.Data._ID = " + photo_id;
			Cursor cur = ct.getContentResolver().query(
					ContactsContract.Data.CONTENT_URI, projection, selection,
					null, null);
			cur.moveToFirst();
			byte[] contactIcon = null;
			if (cur.getBlob(cur.getColumnIndex(ContactsContract.Data.DATA15)) != null) {
				contactIcon = cur.getBlob(cur
						.getColumnIndex(ContactsContract.Data.DATA15));
				d = BitmapFactory.decodeByteArray(contactIcon, 0,
						contactIcon.length);
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	private Intent createContactShortcutIntent(Uri contactUri,
			String contentType, String displayName) {

		Intent shortcutIntent = new Intent(
				ContactsContract.QuickContact.ACTION_QUICK_CONTACT);

		// When starting from the launcher, start in a new, cleared task.
		// CLEAR_WHEN_TASK_RESET cannot reset the root of a task, so we
		// clear the whole thing preemptively here since QuickContactActivity
		// will
		// finish itself when launching other detail activities. We need to use
		// Intent.FLAG_ACTIVITY_NO_ANIMATION since not all versions of launcher
		// will respect
		// the INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION intent extra.
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NO_ANIMATION);

		// Tell the launcher to not do its animation, because we are doing our
		// own
		shortcutIntent.putExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION, true);

		shortcutIntent.setDataAndType(contactUri, contentType);
		shortcutIntent.putExtra(
				ContactsContract.QuickContact.EXTRA_EXCLUDE_MIMES,
				(String[]) null);

		Intent intent = new Intent();
		// Bitmap b = loadImageFromUrl(mContext, photo_id);
		// intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, b);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		if (TextUtils.isEmpty(displayName)) {
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "");
		} else {
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, displayName);
		}

		return shortcutIntent;
	}

	@Override
	public List<GroupMemberBean> run() {
		try {
			return getContact();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onClick(GroupMemberBean item) {
		// TODO Auto-generated method stub
		mContext.startActivity(item.getContacts().intent);
	}

}
