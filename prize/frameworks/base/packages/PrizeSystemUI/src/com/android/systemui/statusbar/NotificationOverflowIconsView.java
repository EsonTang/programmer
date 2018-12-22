/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.IconMerger;
/*PRIZE-import package- liufan-2016-04-26-start*/
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.statusbar.PrizeNotificaionIconUtil;
import android.graphics.drawable.Drawable;
/*PRIZE-import package- liufan-2016-04-26-end*/

/*prize add by xiarui 2018-04-25 start*/
import android.graphics.drawable.Icon;
import com.android.internal.statusbar.StatusBarIcon;
/*prize add by xiarui 2018-04-25 start*/

/**
 * A view to display all the overflowing icons on Keyguard.
 */
public class NotificationOverflowIconsView extends IconMerger {

    private TextView mMoreText;
    private int mTintColor;
    private int mIconSize;
    private NotificationColorUtil mNotificationColorUtil;

    public NotificationOverflowIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNotificationColorUtil = NotificationColorUtil.getInstance(getContext());
        /*PRIZE-notification color white-liufan-2015-09-07-start*/
        mTintColor = getResources().getColor(R.color.keyguard_overflow_content_color_define);
        /*PRIZE-notification color white-liufan-2015-09-07-end*/
        mIconSize = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_icon_size);
    }

    public void setMoreText(TextView moreText) {
        mMoreText = moreText;
    }

    public void addNotification(NotificationData.Entry notification) {
        StatusBarIconView v = new StatusBarIconView(getContext(), "",
                notification.notification.getNotification());
        v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(v, mIconSize, mIconSize);
        //prize modify by xiarui for bug55086 2018-04-25 @{
        Icon newIcon = PrizeNotificaionIconUtil.getNewIcon(getContext(), notification);
        if(newIcon != null) {
            StatusBarIcon icon = notification.icon.getStatusBarIcon().clone();
            icon.icon = newIcon;
            v.set(icon);
        } else {
            v.set(notification.icon.getStatusBarIcon());
        }
        // @}
        applyColor(notification.notification.getNotification(), v);
        updateMoreText();
    }

    private void applyColor(Notification notification, StatusBarIconView view) {
        view.setColorFilter(mTintColor, PorterDuff.Mode.MULTIPLY);
    }

    private void updateMoreText() {
        mMoreText.setText(
                getResources().getString(R.string.keyguard_more_overflow_text, getChildCount()));
    }
}
