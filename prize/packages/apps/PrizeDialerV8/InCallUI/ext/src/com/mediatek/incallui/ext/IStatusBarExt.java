package com.mediatek.incallui.ext;

import android.app.Notification.Builder;
import android.graphics.Bitmap;

public interface IStatusBarExt {
    /**
     * Show status bar hd icon when the call have property of HIGH_DEF_AUDIO.
     * Plugin need to use call capability to show or dismiss statuar bar icon.
     *
     * @param obj    the incallui call
     */
    void updateInCallNotification(Object obj);


    /**
      * Update the incall notification when there exist dual incoming calls.
      *
      * @param clist         the incallui calllist
      * @param builder    the incallui notification builder
      * @param icon         the notification large icon
      * @return boolean
      */
    public boolean buildAndSendNotification(Object clist, Builder builder, Bitmap icon);

    /**
      * Check for notification change when call updated in calllist.
      *
      * @return boolean
      */
    public boolean checkForNotificationChange();
    /**
      * Update the incall notification when vowifi call quality status changes.
      *
      * @param clist         the incallui calllist
      * @param builder    the incallui notification builder
      * @param icon         the notification large icon
      */
    void customizeNotification(Object clist, Builder builder, Bitmap icon);

    /**
      * Update the status bar notification only when registered for Rssi in OP18
      *
      */
    boolean needUpdateNotification();
}