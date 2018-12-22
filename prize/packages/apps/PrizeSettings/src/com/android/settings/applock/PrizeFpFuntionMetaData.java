package com.android.settings.applock;

import android.net.Uri;
import android.provider.BaseColumns;

public interface PrizeFpFuntionMetaData extends BaseColumns  {
	public static final String AUTHORITY = "com.android.settings.provider.fpdata.share";
	//table for applock switch
	String TABLE_NAME = "fp_function_data";
	
    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    
    String CONTENT_TYPE = "vnd.android.cursor.dir/fp.function.share.tasks";

    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/fp.function.share.task";

    String DEFAULT_SORT_ORDER = "modified DESC";

    String FUNCTION_NAME = "function_name";
    String FUNCTION_STATUS = "function_status";
    
    // Function Key Name
    String FP_LOCK_SCREEN_FC = "fp_lock_screen_fc";
    String APP_LOCK_FC = "app_lock_fc";
    public static final int APP_LOCK_FUNCTION_OPEN = 1;
    public static final int APP_LOCK_FUNCTION_CLOSE = 0;	
}
