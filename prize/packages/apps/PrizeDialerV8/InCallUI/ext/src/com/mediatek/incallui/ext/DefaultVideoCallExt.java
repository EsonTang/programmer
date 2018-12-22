package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.MotionEvent;
import android.view.TextureView;
import android.widget.FrameLayout;

/**
 * Default implementation for IVideoCallExt.
 */
public class DefaultVideoCallExt implements IVideoCallExt {

    private static final int DEFAULT_COUNT_DOWN_SECONDS = 20;

    @Override
    public void onCallSessionEvent(Object call, int event) {
        // do nothing.
    }

    @Override
    public int getDeclineTimer() {
        return DEFAULT_COUNT_DOWN_SECONDS;
    }

    @Override
    public boolean onTouchEvent(FrameLayout layout, MotionEvent ev) {
        return false;
    }

    @Override
    public void setContext(Context context) {
        // do nothing
    }
    @Override
    public void onResizePreview() {
        // do nothing
    }

    @Override
    public void onDestroyPreview() {
        // do nothing
    }

    @Override
    public void onInflateVideoCallViews(TextureView incomingVideoView) {
        // do nothing
    }

    @Override
    public boolean showToastForDowngrade() {
        return true;
    }

    @Override
    public void onVideoStateChange(Object call, Object presenter) {
        // do nothing.
    }
}
