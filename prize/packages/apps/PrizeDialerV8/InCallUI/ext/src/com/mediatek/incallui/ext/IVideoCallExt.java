package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.MotionEvent;
import android.view.TextureView;
import android.widget.FrameLayout;

/**
 * Plugin APIs for video call.
 */
public interface IVideoCallExt {

    /**
     * called to show toast when call downgraded.
     * @param call  call object
     * @param event to notify call downgrade
     */
    void onCallSessionEvent(Object call, int event);

    /**
     * called to change video call decline timer.
     * @return duration
     */
    int getDeclineTimer();

    /**
     * handle on touch event.
     * @param layout preview layout
     * @param ev motion event
     * @return whether TouchEvent was handled or not
     */
    boolean onTouchEvent(FrameLayout layout, MotionEvent ev);

    /**
     * handle on touch event.
     * @param context host app context
     */
    void setContext(Context context);

    /**
     * handle resizing for preview video.
     */
    void onResizePreview();

    /**
     * handle destroy of preview video.
     */
    void onDestroyPreview();

    /**
     * handle destroy of preview video.
     * @param incomingVideoView incoming video texture view
     */
    void onInflateVideoCallViews(TextureView incomingVideoView);

     /**
     * called to judge whether show toast for downgrade nor not.
     * @return whether to show toast for downgrade or not.
     */
    boolean showToastForDowngrade();

    /**
     * called to handle video call state change.
     * @param call call whose state changed
     * @param presenter Call button presenter
     */
    void onVideoStateChange(Object call, Object presenter);
}
