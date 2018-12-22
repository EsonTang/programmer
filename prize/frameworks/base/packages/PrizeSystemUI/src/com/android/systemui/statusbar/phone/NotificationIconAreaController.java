package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;

import java.util.ArrayList;
//add for statusbar inverse. prize-linkh-20150903
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyleListener;
import android.app.StatusBarManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Icon;
import com.android.systemui.power.PowerNotificationWarnings;
import android.util.Log;
import java.util.HashMap;
/* app multi instances feature. prize-linkh-20151228 */
import android.util.PrizeAppInstanceUtils;

/**
 * A controller for the space in the status bar to the left of the system icons. This area is
 * normally reserved for notifications.
 */
public class NotificationIconAreaController {

    private static final String TAG = "NotificationIconAreaController";
    private final NotificationColorUtil mNotificationColorUtil;

    private int mIconSize;
    private int mIconHPadding;
    private int mIconTint = Color.WHITE;

    private PhoneStatusBar mPhoneStatusBar;
    protected View mNotificationIconArea;
    private IconMerger mNotificationIcons;
    private ImageView mMoreIcon;
    private final Rect mTintArea = new Rect();


    /*PRIZE-notification icon size-liufan-2016-11-24-start*/
    //add for notification icons in status bar.
    private int mNotificationIconWidthInSb = 0;
    private int mNotificationIconHeightInSb = 0;
    /*PRIZE-notification icon size-liufan-2016-11-24-end*/
    public NotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        mPhoneStatusBar = phoneStatusBar;
        mNotificationColorUtil = NotificationColorUtil.getInstance(context);

        initializeNotificationAreaViews(context);
    }

    protected View inflateIconArea(LayoutInflater inflater) {
        return inflater.inflate(R.layout.notification_icon_area, null);
    }

    /**
     * Initializes the views that will represent the notification area.
     */
    protected void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mNotificationIconArea = inflateIconArea(layoutInflater);

        mNotificationIcons =
                (IconMerger) mNotificationIconArea.findViewById(R.id.notificationIcons);

        mMoreIcon = (ImageView) mNotificationIconArea.findViewById(R.id.moreIcon);
        if (mMoreIcon != null) {
            /*PRIZE-cancel more icon tint-liufan-2016-11-24-start*/
            //mMoreIcon.setImageTintList(ColorStateList.valueOf(mIconTint));
            /*PRIZE-cancel more icon tint-liufan-2016-11-24-end*/
            mNotificationIcons.setOverflowIndicator(mMoreIcon);
        }
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {

            mNotificationIconWidthInSb = context.getResources().getDimensionPixelSize(
                R.dimen.notification_icon_w_in_sb);
            mNotificationIconHeightInSb = context.getResources().getDimensionPixelSize(
                R.dimen.notification_icon_h_in_sb);
            Log.d(TAG, "mNotificationIconWidthInSb=" + mNotificationIconWidthInSb + ", mNotificationIconHeightInSb=" 
                + mNotificationIconHeightInSb);
        }
		//end...
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        /*PRIZE-update for budid:43535-liufan-2017-12-11-start*/
        //final LinearLayout.LayoutParams params = generateIconLayoutParams();
        final LinearLayout.LayoutParams params = PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR  ? 
                new LinearLayout.LayoutParams(mNotificationIconWidthInSb, mNotificationIconHeightInSb) : generateIconLayoutParams();
        /*PRIZE-update for budid:43535-liufan-2017-12-11-end*/
        for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
            child.setLayoutParams(params);
        }
    }

    @NonNull
    private LinearLayout.LayoutParams generateIconLayoutParams() {
        return new LinearLayout.LayoutParams(
                mIconSize + 2 * mIconHPadding, getHeight());
    }

    private void reloadDimens(Context context) {
        Resources res = context.getResources();
        mIconSize = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_icon_size);
        mIconHPadding = res.getDimensionPixelSize(R.dimen.status_bar_icon_padding);
    }

    /**
     * Returns the view that represents the notification area.
     */
    public View getNotificationInnerAreaView() {
        return mNotificationIconArea;
    }

    /**
     * See {@link StatusBarIconController#setIconsDarkArea}.
     *
     * @param tintArea the area in which to tint the icons, specified in screen coordinates
     */
    public void setTintArea(Rect tintArea) {
        if (tintArea == null) {
            mTintArea.setEmpty();
        } else {
            mTintArea.set(tintArea);
        }
        applyNotificationIconsTint();
    }

    /**
     * Sets the color that should be used to tint any icons in the notification area. If this
     * method is not called, the default tint is {@link Color#WHITE}.
     */
    public void setIconTint(int iconTint) {
        mIconTint = iconTint;
        /*PRIZE-cancel more icon tint-liufan-2016-11-24-start*/
        /*if (mMoreIcon != null) {
            mMoreIcon.setImageTintList(ColorStateList.valueOf(mIconTint));
        }*/
        /*PRIZE-cancel more icon tint-liufan-2016-11-24-end*/
        applyNotificationIconsTint();
    }

    protected int getHeight() {
        return mPhoneStatusBar.getStatusBarHeight();
    }

    protected boolean shouldShowNotification(NotificationData.Entry entry,
            NotificationData notificationData) {
        if (notificationData.isAmbient(entry.key)
                && !NotificationData.showNotificationEvenIfUnprovisioned(entry.notification)) {
            return false;
        }
        if (!PhoneStatusBar.isTopLevelChild(entry)) {
            return false;
        }
        if (entry.row.getVisibility() == View.GONE) {
            return false;
        }

        return true;
    }

    /**
     * Updates the notifications with the given list of notifications to display.
     */
    public void updateNotificationIcons(NotificationData notificationData) {
        //remove. prize-linkh-20150911
        final LinearLayout.LayoutParams params = PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR  ? 
                new LinearLayout.LayoutParams(mNotificationIconWidthInSb, mNotificationIconHeightInSb) : generateIconLayoutParams();
        //end.....

        ArrayList<NotificationData.Entry> activeNotifications =
                notificationData.getActiveNotifications();
        final int size = activeNotifications.size();
        ArrayList<StatusBarIconView> toShow = new ArrayList<>(size);

        /// M: StatusBar IconMerger feature, hash{pkg+icon}=iconlevel
        HashMap<String, Integer> uniqueIcon = new HashMap<String, Integer>();
        // Filter out ambient notifications and notification children.
        for (int i = 0; i < size; i++) {
            NotificationData.Entry ent = activeNotifications.get(i);
            if (shouldShowNotification(ent, notificationData)) {

                /// M: StatusBar IconMerger feature @{
                String key = ent.notification.getPackageName()
                        + String.valueOf(ent.notification.getNotification().icon);
                /* app multi instances feature. prize-linkh-20151228 -temp-delete-
                if(PrizeOption.PRIZE_APP_MULTI_INSTANCES && 
                      PrizeAppInstanceUtils.getInstance(mNotificationIcons.getContext()).supportMultiInstance(ent.notification.getPackageName())) {
                    key = key + ent.notification.getAppInstanceIndex();
                }//end...
                /*prize-update for bugid:43525-liufan-2017-12-22-start*/
                if(ent.notification.getPackageName().equals("com.android.bluetooth")){
                    key = ent.notification.getPackageName();
                }
                /*prize-update for bugid:43525-liufan-2017-12-22-end*/
                if (uniqueIcon.containsKey(key) && uniqueIcon.get(key)
                        == ent.notification.getNotification().iconLevel) {
                    Log.d(TAG, "IconMerger feature, skip pkg / icon / iconlevel ="
                        + ent.notification.getPackageName()
                        + "/" + ent.notification.getNotification().icon
                        + "/" + ent.notification.getNotification().iconLevel);
                    continue;
                }
                uniqueIcon.put(key, ent.notification.getNotification().iconLevel);
                /// @}

                toShow.add(ent.icon);
            }
        }

        ArrayList<View> toRemove = new ArrayList<>();
        for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        final int toRemoveCount = toRemove.size();
        for (int i = 0; i < toRemoveCount; i++) {
            mNotificationIcons.removeView(toRemove.get(i));
        }

        for (int i = 0; i < toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mNotificationIcons.addView(v, i, params);
            }
        }

        // Re-sort notification icons
        final int childCount = mNotificationIcons.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View actual = mNotificationIcons.getChildAt(i);
            StatusBarIconView expected = toShow.get(i);
            if (actual == expected) {
                continue;
            }
            mNotificationIcons.removeView(expected);
            mNotificationIcons.addView(expected, i);
        }

        applyNotificationIconsTint();
    }

    /**
     * Applies {@link #mIconTint} to the notification icons.
     */
    private void applyNotificationIconsTint() {
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-start*/
        /*for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mNotificationIcons.getChildAt(i);
            boolean isPreL = Boolean.TRUE.equals(v.getTag(R.id.icon_is_pre_L));
            boolean colorize = !isPreL || NotificationUtils.isGrayscale(v, mNotificationColorUtil);
            if (colorize) {
                v.setImageTintList(ColorStateList.valueOf(
                        StatusBarIconController.getTint(mTintArea, v, mIconTint)));
            }
        }*/
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-end*/
    }

    /*PRIZE-add for statusbar inverse-liufan-2016-04-26-start*/
    public ImageView getMoreIcon(){
        return mMoreIcon;
    }
    /*PRIZE-add for statusbar inverse-liufan-2016-04-26-end*/
}
