package com.android.dialer.prize.tmsdkcallmark;

import android.database.Cursor;

/**
 * Created by wangzhong on 2017/5/5.
 */

public interface ICallMarkCacheDao {
    void updateCallMarkCache(String phoneNumber, String type);
    Cursor getCallMarkCache();
}
