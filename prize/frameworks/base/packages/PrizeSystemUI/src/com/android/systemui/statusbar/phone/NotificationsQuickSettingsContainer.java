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

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.R;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.customize.QSCustomizer;
/*PRIZE import package liyao 2015-07-13 start*/
import android.os.RemoteException;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.view.Display;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
/*PRIZE import package liyao 2015-07-13 end*/

/**
 * The container with notification stack scroller and quick settings inside.
 */
public class NotificationsQuickSettingsContainer extends FrameLayout
        implements ViewStub.OnInflateListener, AutoReinflateContainer.InflateListener {


    private AutoReinflateContainer mQsContainer;
    private View mUserSwitcher;
    private View mStackScroller;
    private View mKeyguardStatusBar;
    private boolean mInflated;
    private boolean mQsExpanded;
    private boolean mCustomizerAnimating;

    private int mBottomPadding;
    private int mStackScrollerMargin;

    /*PRIZE virtual key overlapping liyao 2015-07-13 1-start*/
    private Context mContext;
    private boolean mShowNav = false;
    protected IWindowManager mWindowManagerService;
    private int mNavBarHeight = 0;
    private WindowManager mWindowManager;
    private Display mDisplay;
    /*PRIZE virtual key overlapping liyao 2015-07-13 1-end*/
    public NotificationsQuickSettingsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*PRIZE virtual key overlapping liyao 2015-07-13 2-start*/
        mContext = context;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        try {
            mShowNav = mWindowManagerService.hasNavigationBar();
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            mDisplay = mWindowManager.getDefaultDisplay();
            if(mShowNav) mNavBarHeight = mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }
        /*PRIZE virtual key overlapping liyao 2015-07-13 2-end*/
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mQsContainer = (AutoReinflateContainer) findViewById(R.id.qs_auto_reinflate_container);
        mQsContainer.addInflateListener(this);
        mStackScroller = findViewById(R.id.notification_stack_scroller);
        mStackScrollerMargin = ((LayoutParams) mStackScroller.getLayoutParams()).bottomMargin;
        mKeyguardStatusBar = findViewById(R.id.keyguard_header);
        ViewStub userSwitcher = (ViewStub) findViewById(R.id.keyguard_user_switcher);
        userSwitcher.setOnInflateListener(this);
        mUserSwitcher = userSwitcher;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*PRIZE-cancel reload- liufan-2016-12-03-start*/
        //reloadWidth(mQsContainer);
        //reloadWidth(mStackScroller);
        /*PRIZE-cancel reload- liufan-2016-12-03-end*/

        /*PRIZE-remeause mIsHorizontalScreen,bugid:44342- liufan-2017-12-15-start*/
        mDisplay.getRealMetrics(mDisplayMetrics);
        mIsHorizontalScreen = mDisplayMetrics.widthPixels > mDisplayMetrics.heightPixels ? true : false;
        /*PRIZE-remeause mIsHorizontalScreen,bugid:44342- liufan-2017-12-15-end*/
    }

    private void reloadWidth(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.width = getContext().getResources().getDimensionPixelSize(
                R.dimen.notification_panel_width);
        view.setLayoutParams(params);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mBottomPadding = insets.getStableInsetBottom();
        /*PRIZE-cancel the padding bottom-liufan-2016-12-10-start*/
        Log.d("NotificationsQuickSettingsContainer","mBottomPadding = " + mBottomPadding);
        setPadding(0, 0, 0, padMode == 0 ? 0 : mBottomPadding);
        /*PRIZE-cancel the padding bottom-liufan-2016-12-10-end*/
        /*PRIZE virtual key overlapping liyao 2015-07-13 3-start*/
        boolean inLockScreen = Settings.System.getInt(mContext.getContentResolver(), "in_lock_screen", 0) == 1;
        Log.d("NotificationsQuickSettingsContainer","onApplyWindowInsets() inLockScreen "+inLockScreen);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.bottomMargin= inLockScreen || mIsHorizontalScreen ? 0 : mNavBarHeight;
        setLayoutParams(layoutParams);
        /*PRIZE virtual key overlapping liyao 2015-07-13 3-end*/
        return insets;
    }

    /*PRIZE-add for bugid:43587-liufan-2017-12-12-start*/
    private int padMode = 0;
    public void changePadding(int padding){
        padMode = padding;
        setPadding(0, 0, 0, padMode == 0 ? 0 : mBottomPadding);
    }
    /*PRIZE-add for bugid:43587-liufan-2017-12-12-end*/

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean userSwitcherVisible = mInflated && mUserSwitcher.getVisibility() == View.VISIBLE;
        boolean statusBarVisible = mKeyguardStatusBar.getVisibility() == View.VISIBLE;

        final boolean qsBottom = mQsExpanded && !mCustomizerAnimating;
        View stackQsTop = qsBottom ? mStackScroller : mQsContainer;
        View stackQsBottom = !qsBottom ? mStackScroller : mQsContainer;
        // Invert the order of the scroll view and user switcher such that the notifications receive
        // touches first but the panel gets drawn above.
        if (child == mQsContainer) {
            return super.drawChild(canvas, userSwitcherVisible && statusBarVisible ? mUserSwitcher
                    : statusBarVisible ? mKeyguardStatusBar
                    : userSwitcherVisible ? mUserSwitcher
                    : stackQsBottom, drawingTime);
        } else if (child == mStackScroller) {
            return super.drawChild(canvas,
                    userSwitcherVisible && statusBarVisible ? mKeyguardStatusBar
                    : statusBarVisible || userSwitcherVisible ? stackQsBottom
                    : stackQsTop,
                    drawingTime);
        } else if (child == mUserSwitcher) {
            return super.drawChild(canvas,
                    userSwitcherVisible && statusBarVisible ? stackQsBottom
                    : stackQsTop,
                    drawingTime);
        } else if (child == mKeyguardStatusBar) {
            return super.drawChild(canvas,
                    stackQsTop,
                    drawingTime);
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        if (stub == mUserSwitcher) {
            mUserSwitcher = inflated;
            mInflated = true;
        }
    }


    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    //Prize-liyao-20150804-weather horizontal
    private boolean mIsHorizontalScreen = false;
    @Override
    public void onInflated(View v) {
        QSCustomizer customizer = ((QSContainer) v).getCustomizer();
        customizer.setContainer(this);
        mDisplay.getRealMetrics(mDisplayMetrics);
        mIsHorizontalScreen = mDisplayMetrics.widthPixels > mDisplayMetrics.heightPixels ? true : false;      }

    public void setQsExpanded(boolean expanded) {
        if (mQsExpanded != expanded) {
            mQsExpanded = expanded;
            invalidate();
        }
    }

    public void setCustomizerAnimating(boolean isAnimating) {
        if (mCustomizerAnimating != isAnimating) {
            mCustomizerAnimating = isAnimating;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean isShowing) {
        if (isShowing) {
            // Clear out bottom paddings/margins so the qs customization can be full height.
            setPadding(0, 0, 0, 0);
            setBottomMargin(mStackScroller, 0);
        } else {
            setPadding(0, 0, 0, mBottomPadding);
            setBottomMargin(mStackScroller, mStackScrollerMargin);
        }

    }

    private void setBottomMargin(View v, int bottomMargin) {
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        params.bottomMargin = bottomMargin;
        v.setLayoutParams(params);
    }
}
