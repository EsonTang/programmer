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

package com.android.systemui.statusbar.policy;

import android.view.LayoutInflater;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
/*PRIZE-import package- liufan-2015-05-14-start*/
import com.android.systemui.statusbar.phone.FeatureOption;
import com.android.systemui.statusbar.phone.ObservableScrollView;
import android.util.Log;
/*PRIZE-import package- liufan-2015-05-14-end*/

/**
 * Controls showing and hiding of the brightness mirror.
 */
public class BrightnessMirrorController {

    private final NotificationStackScrollLayout mStackScroller;
    /*PRIZE-update for brightness controller- liufan-2016-06-29-start*/
    public long TRANSITION_DURATION_OUT = 600;
    public long TRANSITION_DURATION_IN = 600;
    /*PRIZE-update for brightness controller- liufan-2016-06-29-end*/

    private final StatusBarWindowView mStatusBarWindow;
    private final ScrimView mScrimBehind;
    private final View mNotificationPanel;
    private final int[] mInt2Cache = new int[2];
    private View mBrightnessMirror;
    /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
    private View mBrightnessMirrorChild;
    public static boolean isRegulateBrightness = false;
    private PhoneStatusBar mBar;
    public void setPhoneStatusBar(PhoneStatusBar bar){
        mBar = bar;
    }
    /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/

    public BrightnessMirrorController(StatusBarWindowView statusBarWindow) {
        mStatusBarWindow = statusBarWindow;
        mScrimBehind = (ScrimView) statusBarWindow.findViewById(R.id.scrim_behind);
        mBrightnessMirror = statusBarWindow.findViewById(R.id.brightness_mirror);
        mNotificationPanel = statusBarWindow.findViewById(R.id.notification_panel);
        mStackScroller = (NotificationStackScrollLayout) statusBarWindow.findViewById(
                R.id.notification_stack_scroller);
        /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
        mBrightnessMirrorChild = statusBarWindow.findViewById(R.id.brightness_mirror_child);
        /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/
    }

    /*PRIZE-update for brightness controller- liufan-2016-06-29-start*/
    public void showMirror() {
        if(!mBar.isShadeState()){
            return ;
        }
        mBrightnessMirror.bringToFront();
        mBrightnessMirror.setVisibility(View.VISIBLE);
        mStackScroller.setFadingOut(true);
        inAnimation(mBrightnessMirror.animate(),0).withLayer();
        mBrightnessMirrorChild.setBackgroundResource(R.drawable.brightness_black_mirror_background_prize);
        mScrimBehind.animateViewAlpha(0.0f, TRANSITION_DURATION_OUT, Interpolators.ALPHA_OUT);
        isRegulateBrightness = true;
        if(mBar!=null){
            mBar.collapseQsSetting();
        }
        if(mBar!=null && mBar.isShadeState()){
            outAnimation(mNotificationPanel.animate())
                    .withLayer();
        }
    }

    public void hideMirror() {
        if(!mBar.isShadeState()){
            return ;
        }
        isRegulateBrightness = false;
        mScrimBehind.postDelayed(new Runnable() {
            @Override
            public void run() {
            outAnimation(mBrightnessMirror.animate(),300).withLayer()
                    .withEndAction(new Runnable() {
                @Override
                public void run() {
                    if(mBar!=null){
                        mBar.collapsePanels(false);
                    }
                    mBrightnessMirror.setVisibility(View.GONE);
                    mBrightnessMirrorChild.setBackgroundResource(R.drawable.brightness_mirror_background);
                    mScrimBehind.animateViewAlpha(1.0f, TRANSITION_DURATION_IN, Interpolators.ALPHA_IN);
                    inAnimation(mNotificationPanel.animate())
                            .withLayer()
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mBrightnessMirror.setVisibility(View.INVISIBLE);
                                    mStackScroller.setFadingOut(false);
                                }
                    });
                }
            });
            }
        }, 0);
    }
    /*PRIZE-update for brightness controller- liufan-2016-06-29-end*/

    /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator a,long duration) {
        return a.alpha(0.0f)
                .setDuration(duration)
                .setInterpolator(PhoneStatusBar.ALPHA_OUT);
    }
    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator a,long duration) {
        return a.alpha(1.0f)
                .setDuration(duration)
                .setInterpolator(PhoneStatusBar.ALPHA_IN);
    }
    /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator a) {
        return a.alpha(0.0f)
                .setDuration(TRANSITION_DURATION_OUT)
                .setInterpolator(Interpolators.ALPHA_OUT)
                .withEndAction(null);
    }
    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator a) {
        return a.alpha(1.0f)
                .setDuration(TRANSITION_DURATION_IN)
                .setInterpolator(Interpolators.ALPHA_IN);
    }


    public void setLocation(View original) {
        original.getLocationInWindow(mInt2Cache);

        // Original is slightly larger than the mirror, so make sure to use the center for the
        // positioning.
        int originalX = mInt2Cache[0] + original.getWidth() / 2;
        int originalY = mInt2Cache[1] + original.getHeight() / 2;
        mBrightnessMirror.setTranslationX(0);
        mBrightnessMirror.setTranslationY(0);
        mBrightnessMirror.getLocationInWindow(mInt2Cache);
        int mirrorX = mInt2Cache[0] + mBrightnessMirror.getWidth() / 2;
        int mirrorY = mInt2Cache[1] + mBrightnessMirror.getHeight() / 2;
        mBrightnessMirror.setTranslationX(originalX - mirrorX);
        mBrightnessMirror.setTranslationY(originalY - mirrorY);
    }

    public View getMirror() {
        return mBrightnessMirror;
    }

    public void updateResources() {
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) mBrightnessMirror.getLayoutParams();
        lp.width = mBrightnessMirror.getResources().getDimensionPixelSize(
                R.dimen.notification_panel_width);
        lp.gravity = mBrightnessMirror.getResources().getInteger(
                R.integer.notification_panel_layout_gravity);
        /*PRIZE-add for bugid:53841,same as NotificationPanelView.updateResources- liufan-2018-4-5-start*/
        if(lp.width > 0){
            lp.width += 120;
        }
        /*PRIZE-add for bugid:53841,same as NotificationPanelView.updateResources- liufan-2018-4-5-end*/
        mBrightnessMirror.setLayoutParams(lp);
    }

    public void onDensityOrFontScaleChanged() {
        int index = mStatusBarWindow.indexOfChild(mBrightnessMirror);
        mStatusBarWindow.removeView(mBrightnessMirror);
        mBrightnessMirror = LayoutInflater.from(mBrightnessMirror.getContext()).inflate(
                R.layout.brightness_mirror, mStatusBarWindow, false);
        mStatusBarWindow.addView(mBrightnessMirror, index);
    }
}
