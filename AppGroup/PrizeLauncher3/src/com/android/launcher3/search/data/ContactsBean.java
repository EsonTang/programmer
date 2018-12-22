package com.android.launcher3.search.data;

/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：联系人信息
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class ContactsBean {
	public String mineType;
	public String data1;
	public String data2;
	public String lookup_key;
	public String raw_contact_id;
	public long contact_id;
	public String display_name;
	public String photo_id;
	
	public String contentType;

	public Uri lookupUri;
	
	public Intent intent;

	public void setLookupUri() {
		lookupUri= ContentUris.withAppendedId(
				Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookup_key),
				contact_id);
	}

}
