package com.mediatek.incallui.ext;

import android.app.Notification.Builder;
import android.graphics.Bitmap;

public class DefaultStatusBarExt implements IStatusBarExt {
    /**
     * Show status bar hd icon when the call have property of HIGH_DEF_AUDIO.
     * Plugin need to use call capability to show or dismiss statuar bar icon.
     *
     * @param obj    the incallui call
     */
    @Override
    public void updateInCallNotification(Object obj) {
        // do nothing.
    }

    /**
      * Update the incall notification when there exist dual incoming calls.
      *
      * @param clist         the incallui calllist
      * @param builder    the incallui notification builder
      * @param icon         the notification large icon
      * @return boolean
      */
    @Override
    public boolean buildAndSendNotification(Object clist, Builder builder, Bitmap icon) {
        return false;
    }

    /**
      * Check for notification change when call updated in calllist.
      *
      * @return boolean
      */
    public boolean checkForNotificationChange() {
        return false;
    }

    /**
      * Update the incall notification when vowifi call quality status changes.
      *
      * @param clist         the incallui calllist
      * @param builder    the incallui notification builder
      * @param icon         the notification large icon
      */
    @Override
    public void customizeNotification(Object clist, Builder builder, Bitmap icon) {
        // do nothing
    }

    /**
      * Update status bar to change notifitcation if OP18
      */
    @Override
    public boolean needUpdateNotification() {
        return false;
    }
}