package com.android.providers.settings.fingerprint;

import android.net.Uri;
import android.provider.BaseColumns;

public interface PrizeAppLockMetaData extends BaseColumns {
	public static final String AUTHORITY = "com.android.settings.provider.fpdata.share";
	//table for applock selected applications
	String TABLE_NAME = "lock_app_data";

    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    
    String CONTENT_TYPE = "vnd.android.cursor.dir/app.lock.share.tasks";

    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/app.lock.share.task";

    String DEFAULT_SORT_ORDER = "modified DESC";

    String PKG_NAME = "pkg_name";
    String CLASS_NAME = "class_name";
    String LOCK_STATUS = "lock_status";//Activity
    String LOCK_STATUS_SETTINGS = "lock_status_settings";//Settings

    static final int LOCK_STATUS_LOCK = 1;
    static final int LOCK_STATUS_UNLOCK = 0;
}
