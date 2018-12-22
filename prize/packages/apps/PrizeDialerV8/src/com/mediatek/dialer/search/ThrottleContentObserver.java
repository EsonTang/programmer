package com.mediatek.dialer.search;


import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * M: This class aim to ignore some notification when database changed
 * frequently. This is a better solution to handle the case of data change too
 * frequently.
 */
public class ThrottleContentObserver extends ContentObserver {

    private static final String TAG = "ThrottleContentObserver";
    private final Throttle mThrottle;
    private Context mInnerContext;
    private boolean mRegistered;
    private String mName;

    public ThrottleContentObserver(Handler handler, Context context,
        Runnable runnable, String name) {
        super(handler);
        mInnerContext = context;
        mName = name;
        mThrottle = new Throttle(name, runnable, handler, 500, Throttle.DEFAULT_MAX_TIMEOUT);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (mRegistered) {
            mThrottle.onEvent();
        }
    }

    public void unregister() {
        if (!mRegistered) {
            return;
        }
        mThrottle.cancelScheduledCallback();
        mInnerContext.getContentResolver().unregisterContentObserver(this);
        mRegistered = false;
    }

    public void register(Uri notifyUri) {
        unregister();
        mInnerContext.getContentResolver().registerContentObserver(notifyUri, true, this);
        mRegistered = true;
    }

}
