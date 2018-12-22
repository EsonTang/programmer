package com.android.settings.applock;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wangzhong on 2016/7/11.
 */
public interface PrizeAppLockCipherMetaData extends BaseColumns {

    public static final String AUTHORITY = "com.android.settings.provider.fpdata.share";

    //table for save password for applock
    String TABLE_NAME = "lock_app_cipher_data";

    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    String CONTENT_TYPE = "vnd.android.cursor.dir/app.lock.cipher.share.tasks";

    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/app.lock.cipher.share.task";

    String DEFAULT_SORT_ORDER = "modified DESC";

    //the password for applock
    String CIPHER_1 = "cipher_1";
    //0 number password,1 complex password,2 pattern
    String CIPHER_TYPE = "cipher_type";
    public static final int CIPHER_TYPE_NUM = 0;
    public static final int CIPHER_TYPE_COMPLEX = 1;
    public static final int CIPHER_TYPE_PATTERN = 2;	
	
    /**
     * 1 : On behalf of the effective(default); 2 : Deleted.
     */
    String CIPHER_STATUS = "cipher_status";
    public static final int CHIPHER_STATUS_VALID = 1;
    public static final int CHIPHER_STATUS_INVALID = 0;	
}
