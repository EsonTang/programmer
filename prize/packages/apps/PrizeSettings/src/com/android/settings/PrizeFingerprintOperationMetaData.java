package com.android.settings;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by wangzhong on 2016/7/12.
 */
public interface PrizeFingerprintOperationMetaData extends BaseColumns {

    public static final String AUTHORITY = "com.android.settings.provider.fpdata.share";

    String TABLE_NAME = "fingerprint_operation_data";

    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    String CONTENT_TYPE = "vnd.android.cursor.dir/fingerprint.operation.share.tasks";

    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/fingerprint.operation.share.task";

    String DEFAULT_SORT_ORDER = "modified DESC";

    /**
     * @see PrizeFingerprintOperationSettings
     */
    String OPERATION_NAME = "operation_name";
    /**
     * 0 : On behalf of the effective; 1 : Deleted(default).
     */
    String OPERATION_STATUS = "operation_status";
    /**
     * "click" and "longpress"
     */
    String OPERATION_EVENT_TYPE = "operation_event_type";
    /**
     * 0 : On behalf of the effective(default); 1 : Deleted.
     */
    String OPERATION_EVENT_STATUS = "operation_event_status";

    //OPERATION_EVENT_TYPE
    String EVENT_TYPE_LONGPRESS = "longpress";
    String EVENT_TYPE_CLICK = "click";

    //OPERATION_NAME
    String LONGPRESS_INCALL = "longpress_incall";
    String LONGPRESS_TAKE = "longpress_take";
    String LONGPRESS_CALLRECORD = "longpress_callrecord";
    String LONGPRESS_SCREENCAPTURE = "longpress_screencapture";
    String LONGPRESS_RETURNHOME = "longpress_returnhome";
    String LONGPRESS_NOTICE = "longpress_notice";
    String CLICK_BACK = "click_back";
    String CLICK_SLIDELAUNCHER = "click_slidelauncher";
    String CLICK_MUSIC = "click_music";
    String CLICK_VIDEO = "click_video";

}
