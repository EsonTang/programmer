/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.policy.HeadsUpManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import android.os.Debug;
import com.mediatek.common.prizeoption.PrizeOption;

public abstract class PanelView extends FrameLayout {
    public static final boolean DEBUG = PanelBar.DEBUG;
    public static final String TAG = PanelView.class.getSimpleName();
    protected MotionEvent mEvent = null;

    private final void logf(String fmt, Object... args) {
        Log.v(TAG, (mViewName != null ? (mViewName + ": ") : "") + String.format(fmt, args));
    }

    protected PhoneStatusBar mStatusBar;
    protected HeadsUpManager mHeadsUpManager;

    private float mPeekHeight;
    private float mHintDistance;
    private float mInitialOffsetOnTouch;
    private boolean mCollapsedAndHeadsUpOnDown;
    private float mExpandedFraction = 0;
    protected float mExpandedHeight = 0;
    private boolean mPanelClosedOnDown;
    private boolean mHasLayoutedSinceDown;
    private float mUpdateFlingVelocity;
    private boolean mUpdateFlingOnLayout;
    private boolean mPeekTouching;
    private boolean mJustPeeked;
    private boolean mClosing;
    protected boolean mTracking;
    private boolean mTouchSlopExceeded;
    private int mTrackingPointer;
    protected int mTouchSlop;
    protected boolean mHintAnimationRunning;
    private boolean mOverExpandedBeforeFling;
    private boolean mTouchAboveFalsingThreshold;
    private int mUnlockFalsingThreshold;
    private boolean mTouchStartedInEmptyArea;
    private boolean mMotionAborted;
    private boolean mUpwardsWhenTresholdReached;
    private boolean mAnimatingOnDown;

    private ValueAnimator mHeightAnimator;
    private ObjectAnimator mPeekAnimator;
    private VelocityTrackerInterface mVelocityTracker;
    private FlingAnimationUtils mFlingAnimationUtils;
    private FalsingManager mFalsingManager;

    /**
     * Whether an instant expand request is currently pending and we are just waiting for layout.
     */
    private boolean mInstantExpanding;
    private boolean mAnimateAfterExpanding;

    PanelBar mBar;

    private String mViewName;
    private float mInitialTouchY;
    private float mInitialTouchX;
    private boolean mTouchDisabled;

    private Interpolator mBounceInterpolator;
    protected KeyguardBottomAreaView mKeyguardBottomArea;

    private boolean mPeekPending;
    private boolean mCollapseAfterPeek;

    /**
     * Speed-up factor to be used when {@link #mFlingCollapseRunnable} runs the next time.
     */
    private float mNextCollapseSpeedUpFactor = 1.0f;

    protected boolean mExpanding;
    private boolean mGestureWaitForTouchSlop;
    private boolean mIgnoreXTouchSlop;
    private Runnable mPeekRunnable = new Runnable() {
        @Override
        public void run() {
            mPeekPending = false;
            runPeekAnimation();
        }
    };

    //M: This is use to improve keyguard unlock performance
    private final static int UNLOCK_DURATION = 48;

    protected void onExpandingFinished() {
        mBar.onExpandingFinished();
    }

    protected void onExpandingStarted() {
    }

    private void notifyExpandingStarted() {
        if (!mExpanding) {
            mExpanding = true;
            onExpandingStarted();
        }
    }

    protected final void notifyExpandingFinished() {
        endClosing();
        if (mExpanding) {
            mExpanding = false;
            onExpandingFinished();
        }
    }

    private void schedulePeek() {
        mPeekPending = true;
        long timeout = ViewConfiguration.getTapTimeout();
        postOnAnimationDelayed(mPeekRunnable, timeout);
        notifyBarPanelExpansionChanged();
    }

    private void runPeekAnimation() {
        mPeekHeight = getPeekHeight();
        if (DEBUG) logf("peek to height=%.1f", mPeekHeight);
        if (mHeightAnimator != null) {
            return;
        }
        /*PRIZE-update the length of pull down frist-liufan-2015-06-09-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            mPeekHeight = 10;
        }
        /*PRIZE-update the length of pull down frist-liufan-2015-06-09-end*/
        mPeekAnimator = ObjectAnimator.ofFloat(this, "expandedHeight", mPeekHeight)
                .setDuration(250);
        mPeekAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        mPeekAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPeekAnimator = null;
                if (mCollapseAfterPeek && !mCancelled) {
                    postOnAnimation(mPostCollapseRunnable);
                }
                mCollapseAfterPeek = false;
            }
        });
        notifyExpandingStarted();
        mPeekAnimator.start();
        mJustPeeked = true;
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlingAnimationUtils = new FlingAnimationUtils(context, 0.6f);
        mBounceInterpolator = new BounceInterpolator();
        mFalsingManager = FalsingManager.getInstance(context);
    }

    protected void loadDimens() {
        final Resources res = getContext().getResources();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mHintDistance = res.getDimension(R.dimen.hint_move_distance);
        mUnlockFalsingThreshold = res.getDimensionPixelSize(R.dimen.unlock_falsing_threshold);
    }

    private void trackMovement(MotionEvent event) {
        // Add movement to velocity tracker using raw screen X and Y coordinates instead
        // of window coordinates because the window frame may be moving at the same time.
        float deltaX = event.getRawX() - event.getX();
        float deltaY = event.getRawY() - event.getY();
        event.offsetLocation(deltaX, deltaY);
        if (mVelocityTracker != null) mVelocityTracker.addMovement(event);
        event.offsetLocation(-deltaX, -deltaY);
    }

    public void setTouchDisabled(boolean disabled) {
        mTouchDisabled = disabled;
        if (mTouchDisabled && mTracking) {
            onTrackingStopped(true /* expanded */);
        }
    }
    /*PRIZE-override the method of panel view-liufan-2015-06-10-start*/
    public void expandImmediateSetting(){}
    public void showBlurBackground(){}
    public void cancelNotificationBackground(){}
    /*PRIZE-override the method of panel view-liufan-2015-06-10-end*/

    /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-Start*/
    public void overTouchEvent(){
        if(mEvent!=null){
            MotionEvent event =  MotionEvent.obtain(mEvent);
            event.setAction(MotionEvent.ACTION_UP);
            onTouchEvent(event);
            event.recycle();
            mEvent = null;
        }
    }
    /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-End*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mInstantExpanding || mTouchDisabled
                || (mMotionAborted && event.getActionMasked() != MotionEvent.ACTION_DOWN)) {
            return false;
        }

        // On expanding, single mouse click expands the panel instead of dragging.
        if (isFullyCollapsed() && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                expand(true);
            }
            return true;
        }

        /*
         * We capture touch events here and update the expand height here in case according to
         * the users fingers. This also handles multi-touch.
         *
         * If the user just clicks shortly, we show a quick peek of the shade.
         *
         * Flinging is also enabled in order to open or close the shade.
         */

        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mGestureWaitForTouchSlop = isFullyCollapsed() || hasConflictingGestures();
            mIgnoreXTouchSlop = isFullyCollapsed() || shouldGestureIgnoreXTouchSlop(x, y);
            Log.e("liufan","mGestureWaitForTouchSlop = " + mGestureWaitForTouchSlop);
            Log.e("liufan","mIgnoreXTouchSlop = " + mIgnoreXTouchSlop);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-Start*/
                mEvent = event;
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-End*/
                startExpandMotion(x, y, false /* startTracking */, mExpandedHeight);
                mJustPeeked = false;
                mPanelClosedOnDown = isFullyCollapsed();
                mHasLayoutedSinceDown = false;
                mUpdateFlingOnLayout = false;
                mMotionAborted = false;
                mPeekTouching = mPanelClosedOnDown;
                mTouchAboveFalsingThreshold = false;
                mCollapsedAndHeadsUpOnDown = isFullyCollapsed()
                        && mHeadsUpManager.hasPinnedHeadsUp();
                if (mVelocityTracker == null) {
                    initVelocityTracker();
                }
                trackMovement(event);
                if (!mGestureWaitForTouchSlop || (mHeightAnimator != null && !mHintAnimationRunning) ||
                        mPeekPending || mPeekAnimator != null) {
                    cancelHeightAnimator();
                    cancelPeek();
                    mTouchSlopExceeded = (mHeightAnimator != null && !mHintAnimationRunning)
                            || mPeekPending || mPeekAnimator != null;
                    onTrackingStarted();
                }
                if (isFullyCollapsed() && !mHeadsUpManager.hasPinnedHeadsUp()) {
                    /*PRIZE-cancel the animation when click the statusbar,bug:17318-liufan-2016-06-15-start*/
                    //schedulePeek();
                    /*PRIZE-cancel the animation when click the statusbar,bug:17318-liufan-2016-06-15-end*/
                    /*PRIZE-show NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-start*/
                    //notifyBarPanelExpansionChanged();
                    notifyBarPanelExpansionChangedWithoutQsExpand();//update for bugid:53824-liufan-2018-3-27
                    /*PRIZE-show NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-end*/
                }
                /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-start*/
                isHaveHeadsUpNotificationFlag = NotificationPanelView.isHaveHeadsUpNotification;
                /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-end*/
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    final float newY = event.getY(newIndex);
                    final float newX = event.getX(newIndex);
                    mTrackingPointer = event.getPointerId(newIndex);
                    startExpandMotion(newX, newY, true /* startTracking */, mExpandedHeight);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {
                    mMotionAborted = true;
                    endMotionEvent(event, x, y, true /* forceCancel */);
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-Start*/
                mEvent = event;
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-End*/
                float h = y - mInitialTouchY;

                // If the panel was collapsed when touching, we only need to check for the
                // y-component of the gesture, as we have no conflicting horizontal gesture.
                if (Math.abs(h) > mTouchSlop
                        && (Math.abs(h) > Math.abs(x - mInitialTouchX)
                                || mIgnoreXTouchSlop)) {
                    mTouchSlopExceeded = true;
                    if (mGestureWaitForTouchSlop && !mTracking && !mCollapsedAndHeadsUpOnDown) {
                        if (!mJustPeeked && mInitialOffsetOnTouch != 0f) {
                            startExpandMotion(x, y, false /* startTracking */, mExpandedHeight);
                            h = 0;
                        }
                        cancelHeightAnimator();
                        removeCallbacks(mPeekRunnable);
                        mPeekPending = false;
                        onTrackingStarted();
                    }
                }
                final float newHeight = Math.max(0, h + mInitialOffsetOnTouch);
                if (newHeight > mPeekHeight) {
                    if (mPeekAnimator != null) {
                        mPeekAnimator.cancel();
                    }
                    mJustPeeked = false;
                }
                if (-h >= getFalsingThreshold()) {
                    mTouchAboveFalsingThreshold = true;
                    mUpwardsWhenTresholdReached = isDirectionUpwards(x, y);
                }
                if (!mJustPeeked && (!mGestureWaitForTouchSlop || mTracking) && !isTrackingBlocked()) {
                    setExpandedHeightInternal(newHeight);
                    /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-start*/
                    if(isHaveHeadsUpNotificationFlag) dismissNotificationBgWhenHeadsUp(true);
                    /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-end*/
                }

                trackMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-Start*/
                mEvent = null;
                /*PRIZE-add for bugid: 34326-zhudaopeng-2017-06-13-End*/
                trackMovement(event);
                endMotionEvent(event, x, y, false /* forceCancel */);
                break;
        }
        return !mGestureWaitForTouchSlop || mTracking;
    }

    /**
     * @return whether the swiping direction is upwards and above a 45 degree angle compared to the
     * horizontal direction
     */
    private boolean isDirectionUpwards(float x, float y) {
        float xDiff = x - mInitialTouchX;
        float yDiff = y - mInitialTouchY;
        if (yDiff >= 0) {
            return false;
        }
        return Math.abs(yDiff) >= Math.abs(xDiff);
    }

    protected void startExpandMotion(float newX, float newY, boolean startTracking,
            float expandedHeight) {
        mInitialOffsetOnTouch = expandedHeight;
        mInitialTouchY = newY;
        mInitialTouchX = newX;
        if (startTracking) {
            mTouchSlopExceeded = true;
            /*PRIZE-cancel set, same as android M,for bugid:44940-liufan-2017-12-20-start*/
            //setExpandedHeight(mInitialOffsetOnTouch);
            /*PRIZE-cancel set, same as android M,for bugid:44940-liufan-2017-12-20-end*/
            onTrackingStarted();
        }
    }

    private void endMotionEvent(MotionEvent event, float x, float y, boolean forceCancel) {
        mTrackingPointer = -1;
        if ((mTracking && mTouchSlopExceeded)
                || Math.abs(x - mInitialTouchX) > mTouchSlop
                || Math.abs(y - mInitialTouchY) > mTouchSlop
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || forceCancel) {
            float vel = 0f;
            float vectorVel = 0f;
            if (mVelocityTracker != null) {
                mVelocityTracker.computeCurrentVelocity(1000);
                vel = mVelocityTracker.getYVelocity();
                vectorVel = (float) Math.hypot(
                        mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity());
            }
            boolean expand = flingExpands(vel, vectorVel, x, y)
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL
                    || forceCancel;
            /*PRIZE-for bug:42005-liufan-2017-11-10-start*/
            expand = expand && !StatusBarWindowView.IS_HIDE_KEYGUARD_WHEN_SLIP;
            Log.d(TAG,"42005 PanelView endMotionEvent expand : "+expand);
            /*PRIZE-for bug:42005-liufan-2017-11-10-end*/
            /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-start*/
            if(!mTracking && expand){
                cancelHeightAnimator();
                onTrackingStarted();
            }
            /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-end*/
            DozeLog.traceFling(expand, mTouchAboveFalsingThreshold,
                    mStatusBar.isFalsingThresholdNeeded(),
                    mStatusBar.isWakeUpComingFromTouch());
                    // Log collapse gesture if on lock screen.
                    if (!expand && mStatusBar.getBarState() == StatusBarState.KEYGUARD) {
                        float displayDensity = mStatusBar.getDisplayDensity();
                        int heightDp = (int) Math.abs((y - mInitialTouchY) / displayDensity);
                        int velocityDp = (int) Math.abs(vel / displayDensity);
                        EventLogTags.writeSysuiLockscreenGesture(
                                EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_UP_UNLOCK,
                                heightDp, velocityDp);
                    }
            /*PRIZE-dismiss NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-start*/
            if(getQsExpandImmediate() && !expand) setQsExpandImmediate(false);
            /*PRIZE-dismiss NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-end*/
            fling(vel, expand, isFalseTouch(x, y));
            onTrackingStopped(expand);
            mUpdateFlingOnLayout = expand && mPanelClosedOnDown && !mHasLayoutedSinceDown;
            if (mUpdateFlingOnLayout) {
                mUpdateFlingVelocity = vel;
            }
        } else {
            /*PRIZE-add for background color-liufan-2017-08-28-start*/
            Log.d(TAG, "responseStatusBarBackgroundClick mExpandedHeight : " + mExpandedHeight + ", state = " + mStatusBar.getBarState());
            if(mExpandedHeight == 0 && mStatusBar.getBarState() == StatusBarState.SHADE){
                 mStatusBar.responseStatusBarBackgroundClick();
            }
            /*PRIZE-add for background color-liufan-2017-08-28-end*/
            boolean expands = onEmptySpaceClick(mInitialTouchX);
            /*PRIZE-dismiss NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-start*/
            if(getQsExpandImmediate()) setQsExpandImmediate(false);
            /*PRIZE-dismiss NotificationStackScrollLayout,bug:25212-liufan-2016-12-05-end*/
            onTrackingStopped(expands);
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        mPeekTouching = false;
    }

    private int getFalsingThreshold() {
        float factor = mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
        return (int) (mUnlockFalsingThreshold * factor);
    }

    protected abstract boolean hasConflictingGestures();

    protected abstract boolean shouldGestureIgnoreXTouchSlop(float x, float y);

    /*PRIZE-add isNeedAnimToDismissNotificationBg for dismiss notification bg slowly- liufan-2016-06-28-start*/
    public static boolean isNeedAnimToDismissNotificationBg = false;
    protected void onTrackingStopped(boolean expand) {
        mTracking = false;
        isNeedAnimToDismissNotificationBg = true;
        mBar.onTrackingStopped(expand);
        notifyBarPanelExpansionChanged();
        isNeedAnimToDismissNotificationBg = false;
    }
    /*PRIZE-add isNeedAnimToDismissNotificationBg for dismiss notification bg slowly- liufan-2016-06-28-end*/

    protected void onTrackingStarted() {
        endClosing();
        mTracking = true;
        mCollapseAfterPeek = false;
        mBar.onTrackingStarted();
        notifyExpandingStarted();
        notifyBarPanelExpansionChanged();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mInstantExpanding
                || (mMotionAborted && event.getActionMasked() != MotionEvent.ACTION_DOWN)) {
            return false;
        }

        /*
         * If the user drags anywhere inside the panel we intercept it if the movement is
         * upwards. This allows closing the shade from anywhere inside the panel.
         *
         * We only do this if the current content is scrolled to the bottom,
         * i.e isScrolledToBottom() is true and therefore there is no conflicting scrolling gesture
         * possible.
         */
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);
        boolean scrolledToBottom = isScrolledToBottom();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mStatusBar.userActivity();
                mAnimatingOnDown = mHeightAnimator != null;
                if (mAnimatingOnDown && mClosing && !mHintAnimationRunning || mPeekPending || mPeekAnimator != null) {
                    cancelHeightAnimator();
                    cancelPeek();
                    mTouchSlopExceeded = true;
                    return true;
                }
                mInitialTouchY = y;
                mInitialTouchX = x;
                mTouchStartedInEmptyArea = !isInContentBounds(x, y);
                mTouchSlopExceeded = false;
                mJustPeeked = false;
                mMotionAborted = false;
                mPanelClosedOnDown = isFullyCollapsed();
                mCollapsedAndHeadsUpOnDown = false;
                mHasLayoutedSinceDown = false;
                mUpdateFlingOnLayout = false;
                mTouchAboveFalsingThreshold = false;
                initVelocityTracker();
                trackMovement(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialTouchX = event.getX(newIndex);
                    mInitialTouchY = event.getY(newIndex);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {
                    mMotionAborted = true;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                trackMovement(event);
                if (scrolledToBottom || mTouchStartedInEmptyArea || mAnimatingOnDown) {
                    float hAbs = Math.abs(h);
                    if ((h < -mTouchSlop || (mAnimatingOnDown && hAbs > mTouchSlop))
                            && hAbs > Math.abs(x - mInitialTouchX)) {
                        cancelHeightAnimator();
                        startExpandMotion(x, y, true /* startTracking */, mExpandedHeight);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return false;
    }

    /**
     * @return Whether a pair of coordinates are inside the visible view content bounds.
     */
    protected abstract boolean isInContentBounds(float x, float y);

    protected void cancelHeightAnimator() {
        if (mHeightAnimator != null) {
            mHeightAnimator.cancel();
        }
        endClosing();
    }

    private void endClosing() {
        if (mClosing) {
            mClosing = false;
            onClosingFinished();
        }
    }

    private void initVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTrackerFactory.obtain(getContext());
    }

    protected boolean isScrolledToBottom() {
        return true;
    }

    protected float getContentHeight() {
        return mExpandedHeight;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        loadDimens();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadDimens();
    }

    /**
     * @param vel the current vertical velocity of the motion
     * @param vectorVel the length of the vectorial velocity
     * @return whether a fling should expands the panel; contracts otherwise
     */
    protected boolean flingExpands(float vel, float vectorVel, float x, float y) {
        if (isFalseTouch(x, y)) {
            return true;
        }
        if (Math.abs(vectorVel) < mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return getExpandedFraction() > 0.5f;
        } else {
            return vel > 0;
        }
    }

    /**
     * @param x the final x-coordinate when the finger was lifted
     * @param y the final y-coordinate when the finger was lifted
     * @return whether this motion should be regarded as a false touch
     */
    private boolean isFalseTouch(float x, float y) {
        if (!mStatusBar.isFalsingThresholdNeeded()) {
            return false;
        }
        if (mFalsingManager.isClassiferEnabled()) {
            return mFalsingManager.isFalseTouch();
        }
        if (!mTouchAboveFalsingThreshold) {
            return true;
        }
        if (mUpwardsWhenTresholdReached) {
            return false;
        }
        return !isDirectionUpwards(x, y);
    }

    protected void fling(float vel, boolean expand) {
        fling(vel, expand, 1.0f /* collapseSpeedUpFactor */, false);
    }

    protected void fling(float vel, boolean expand, boolean expandBecauseOfFalsing) {
        fling(vel, expand, 1.0f /* collapseSpeedUpFactor */, expandBecauseOfFalsing);
    }

    protected void fling(float vel, boolean expand, float collapseSpeedUpFactor,
            boolean expandBecauseOfFalsing) {
        cancelPeek();
        /*PRIZE-collapse when the bouncer is showing，bugid：4897-liufan-2015-11-24-start*/
        if(mStatusBar.isBouncerShowing()){
            expand = false;
        }
        /*PRIZE-collapse when the bouncer is showing，bugid：4897-liufan-2015-11-24-end*/
        float target = expand ? getMaxPanelHeight() : 0.0f;
        /*PRIZE-add isFromFlingCollapseRunnable,bugid:17060- liufan-2016-06-15-start*/
        if (!expand && !isFromFlingCollapseRunnable) {
            mClosing = true;
        }
        /*PRIZE-add isFromFlingCollapseRunnable,bugid:17060- liufan-2016-06-15-end*/
        flingToHeight(vel, expand, target, collapseSpeedUpFactor, expandBecauseOfFalsing);
    }

    protected void flingToHeight(float vel, boolean expand, float target,
            float collapseSpeedUpFactor, boolean expandBecauseOfFalsing) {
        // Hack to make the expand transition look nice when clear all button is visible - we make
        // the animation only to the last notification, and then jump to the maximum panel height so
        // clear all just fades in and the decelerating motion is towards the last notification.

        final boolean clearAllExpandHack = expand && fullyExpandedClearAllVisible()
                && mExpandedHeight < getMaxPanelHeight() - getClearAllHeight()
                && !isClearAllVisible();
        if (clearAllExpandHack) {
            target = getMaxPanelHeight() - getClearAllHeight();
        }
        final boolean isHaveHeadsUpNotification = isHaveHeadsUpNotificationFlag;//update for headsupnotification bg alpha,bugid: 30241-liufan-2017-03-14
        Log.d(TAG,"expand: " + expand + ", mExpandedHeight: " + mExpandedHeight 
            + ", isHaveHeadsUpNotification: " + isHaveHeadsUpNotification);
        if (target == mExpandedHeight || getOverExpansionAmount() > 0f && expand) {
            notifyExpandingFinished();
            /*PRIZE-Modify for bugid: 32654-zhudaopeng-2017-04-18-Start*/
            /*PRIZE-top notification background alpha,bugid:21854- liufan-2016-09-14-start*/
            // if(isHaveHeadsUpNotification) dismissNotificationBgWhenHeadsUp(expand);
            /*PRIZE-top notification background alpha,bugid:21854- liufan-2016-09-14-end*/
            if(NotificationPanelView.isHaveHeadsUpNotification) {
                Log.d(TAG,"flingToHeight()  NotificationPanelView.isHaveHeadsUpNotification " +
                        NotificationPanelView.isHaveHeadsUpNotification);
                dismissNotificationBgWhenHeadsUp(expand);
            }
            /*PRIZE-Modify for bugid: 32654-zhudaopeng-2017-04-18-End*/
            return;
        }
        mOverExpandedBeforeFling = getOverExpansionAmount() > 0f;
        //ValueAnimator animator = createHeightAnimator(target);
        ValueAnimator animator = ValueAnimator.ofFloat(mExpandedHeight, target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setExpandedHeightInternal((Float) animation.getAnimatedValue());
                if(isHaveHeadsUpNotification) dismissNotificationBgWhenHeadsUp(expand);
            }
        });
        if (expand) {
            if (expandBecauseOfFalsing) {
                vel = 0;
            }
            mFlingAnimationUtils.apply(animator, mExpandedHeight, target, vel, getHeight());
            if (vel == 0) {
                animator.setDuration(350);
            }
        } else {
            mFlingAnimationUtils.applyDismissing(animator, mExpandedHeight, target, vel,
                    getHeight());
            //M: Improve animation performance
            animator.setDuration(UNLOCK_DURATION);

            // Make it shorter if we run a canned animation
            if (vel == 0) {
                animator.setDuration((long)
                        (animator.getDuration() * getCannedFlingDurationFactor()
                                / collapseSpeedUpFactor));
            }
        }
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (clearAllExpandHack && !mCancelled) {
                    setExpandedHeightInternal(getMaxPanelHeight());
                }
                mHeightAnimator = null;
                if (!mCancelled) {
                    notifyExpandingFinished();
                }
                /*PRIZE-add isNeedAnimToDismissNotificationBg for dismiss notification bg slowly- liufan-2016-06-28-start*/
                isNeedAnimToDismissNotificationBg = true;
                notifyBarPanelExpansionChanged();
                isNeedAnimToDismissNotificationBg = false;
                /*PRIZE-add isNeedAnimToDismissNotificationBg for dismiss notification bg slowly- liufan-2016-06-28-end*/
                /*PRIZE-collapse when the bouncer is showing，bugid：4897-liufan-2015-11-24-start*/
                if(mStatusBar.isBouncerShowing()){
                    flingImmediately(0,false);
                }
                /*PRIZE-Modify for bugid: 32654-zhudaopeng-2017-04-18-Start*/
                if(NotificationPanelView.isHaveHeadsUpNotification) {
                    Log.d(TAG,"onAnimationEnd()  NotificationPanelView.isHaveHeadsUpNotification " +
                            NotificationPanelView.isHaveHeadsUpNotification);
                    dismissNotificationBgWhenHeadsUp(expand);
                }
                /*PRIZE-Modify for bugid: 32654-zhudaopeng-2017-04-18-End*/
                /*PRIZE-collapse when the bouncer is showing，bugid：4897-liufan-2015-11-24-end*/
            }
        });
        mHeightAnimator = animator;
        animator.start();
    }

    /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-start*/
    private boolean isHaveHeadsUpNotificationFlag = false;
    /*PRIZE-add for headsupnotification bg alpha,bugid: 30241- liufan-2017-03-14-end*/
    /*PRIZE-add for headsup notification- liufan-2016-07-02-start*/
    public void dismissNotificationBgWhenHeadsUp(boolean expand){}
    /*PRIZE-add for headsup notification- liufan-2016-07-02-end*/
    
    /*PRIZE-avoid the KeyguardBouncer and lockscreen overlapping，bugid：4897-liufan-2015-08-27-start*/
	private boolean isBelowFalsingThreshold() {
        return !mTouchAboveFalsingThreshold && mStatusBar.isFalsingThresholdNeeded();
    }
    protected void flingImmediately(float vel, boolean expand) {
        cancelPeek();
        cancelHeightAnimator();
        float target = expand ? getMaxPanelHeight() : 0.0f;

        // Hack to make the expand transition look nice when clear all button is visible - we make
        // the animation only to the last notification, and then jump to the maximum panel height so
        // clear all just fades in and the decelerating motion is towards the last notification.
        final boolean clearAllExpandHack = expand && fullyExpandedClearAllVisible()
                && mExpandedHeight < getMaxPanelHeight() - getClearAllHeight()
                && !isClearAllVisible();
        if (clearAllExpandHack) {
            target = getMaxPanelHeight() - getClearAllHeight();
        }
        if (target == mExpandedHeight || getOverExpansionAmount() > 0f && expand) {
            notifyExpandingFinished();
            return;
        }
        mOverExpandedBeforeFling = getOverExpansionAmount() > 0f;
        ValueAnimator animator = createHeightAnimator(target);
        if (expand) {
            boolean belowFalsingThreshold = isBelowFalsingThreshold();
            if (belowFalsingThreshold) {
                vel = 0;
            }
            mFlingAnimationUtils.apply(animator, mExpandedHeight, target, vel, getHeight());
            if (belowFalsingThreshold) {
                animator.setDuration(350);
            }
        } else {
            mFlingAnimationUtils.applyDismissing(animator, mExpandedHeight, target, vel,
                    getHeight());

            // Make it shorter if we run a canned animation
            if (vel == 0) {
                animator.setDuration((long)
                        (animator.getDuration() * getCannedFlingDurationFactor()));
            }
        }
        animator.setDuration(0);
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (clearAllExpandHack && !mCancelled) {
                    setExpandedHeightInternal(getMaxPanelHeight());
                }
                if (!mCancelled) {
                    notifyExpandingFinished();
                }
            }
        });
        animator.start();
    }
    /*PRIZE-avoid the KeyguardBouncer and lockscreen overlapping，bugid：4897-liufan-2015-08-27-end*/

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mViewName = getResources().getResourceName(getId());
    }

    public String getName() {
        return mViewName;
    }

    public void setExpandedHeight(float height) {
        if (DEBUG) logf("setExpandedHeight(%.1f)", height);
        setExpandedHeightInternal(height + getOverExpansionPixels());
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mStatusBar.onPanelLaidOut();
        requestPanelHeightUpdate();
        mHasLayoutedSinceDown = true;
        if (mUpdateFlingOnLayout) {
            abortAnimations();
            fling(mUpdateFlingVelocity, true /* expands */);
            mUpdateFlingOnLayout = false;
        }
    }

    protected void requestPanelHeightUpdate() {
        float currentMaxPanelHeight = getMaxPanelHeight();

        // If the user isn't actively poking us, let's update the height
        if ((!mTracking || isTrackingBlocked())
                && mHeightAnimator == null
                && !isFullyCollapsed()
                && currentMaxPanelHeight != mExpandedHeight
                && !mPeekPending
                && mPeekAnimator == null
                && !mPeekTouching) {
            setExpandedHeight(currentMaxPanelHeight);
        }
    }

    public final String TOUCH_TAG = "panel_touch";
    public void setExpandedHeightInternal(float h) {
        float fhWithoutOverExpansion = getMaxPanelHeight() - getOverExpansionAmount();
        if (mHeightAnimator == null) {
            float overExpansionPixels = Math.max(0, h - fhWithoutOverExpansion);
            if (getOverExpansionPixels() != overExpansionPixels && mTracking) {
                setOverExpansion(overExpansionPixels, true /* isPixels */);
            }
            mExpandedHeight = Math.min(h, fhWithoutOverExpansion) + getOverExpansionAmount();
        } else {
            mExpandedHeight = h;
            if (mOverExpandedBeforeFling) {
                setOverExpansion(Math.max(0, h - fhWithoutOverExpansion), false /* isPixels */);
            }
        }

        mExpandedHeight = Math.max(0, mExpandedHeight);
        mExpandedFraction = Math.min(1f, fhWithoutOverExpansion == 0
                ? 0
                : mExpandedHeight / fhWithoutOverExpansion);
        Log.d(TOUCH_TAG, "mExpandedHeight: " + mExpandedHeight);
        Log.d(TOUCH_TAG, Debug.getCallers(10));
        onHeightUpdated(mExpandedHeight);
        notifyBarPanelExpansionChanged();
        /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-start*/
        if(mExpandedHeight == 0 && PhoneStatusBar.isCollapseAllPanelsAnim){
            Log.d(TAG,"isCollapseAllPanelsAnim trun false");
            PhoneStatusBar.isCollapseAllPanelsAnim = false;
        }
        /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-end*/
    }

    /**
     * @return true if the panel tracking should be temporarily blocked; this is used when a
     *         conflicting gesture (opening QS) is happening
     */
    protected abstract boolean isTrackingBlocked();

    protected abstract void setOverExpansion(float overExpansion, boolean isPixels);

    protected abstract void onHeightUpdated(float expandedHeight);

    protected abstract float getOverExpansionAmount();

    protected abstract float getOverExpansionPixels();

    /**
     * This returns the maximum height of the panel. Children should override this if their
     * desired height is not the full height.
     *
     * @return the default implementation simply returns the maximum height.
     */
    protected abstract int getMaxPanelHeight();

    public void setExpandedFraction(float frac) {
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-start*/
        if(mHeightAnimator!=null){
            Log.d(TAG,"setExpandedFraction--frac: "+frac);
            return ;
        }
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-end*/
        setExpandedHeight(getMaxPanelHeight() * frac);
    }

    public float getExpandedHeight() {
        return mExpandedHeight;
    }

    public float getExpandedFraction() {
        return mExpandedFraction;
    }

    public boolean isFullyExpanded() {
        return mExpandedHeight >= getMaxPanelHeight();
    }

    public boolean isFullyCollapsed() {
        return mExpandedHeight <= 0;
    }

    public boolean isCollapsing() {
        return mClosing;
    }

    public boolean isTracking() {
        return mTracking;
    }

    public void setBar(PanelBar panelBar) {
        mBar = panelBar;
    }

    public void collapse(boolean delayed, float speedUpFactor) {
        if (DEBUG) logf("collapse: " + this);
        if (mPeekPending || mPeekAnimator != null) {
            mCollapseAfterPeek = true;
            if (mPeekPending) {

                // We know that the whole gesture is just a peek triggered by a simple click, so
                // better start it now.
                removeCallbacks(mPeekRunnable);
                mPeekRunnable.run();
            }
        /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-start*/
        } else if (!isFullyCollapsed() && !mTracking && !mClosing || PhoneStatusBar.isCollapseAllPanelsAnim) {
        /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-end*/
            cancelHeightAnimator();
            notifyExpandingStarted();

            // Set after notifyExpandingStarted, as notifyExpandingStarted resets the closing state.
            mClosing = true;
            if (delayed) {
                mNextCollapseSpeedUpFactor = speedUpFactor;
                postDelayed(mFlingCollapseRunnable, 120);
            } else {
                fling(0, false /* expand */, speedUpFactor, false /* expandBecauseOfFalsing */);
            }
        }
    }

    /*PRIZE-add isFromFlingCollapseRunnable,bugid:17060- liufan-2016-06-15-start*/
    private boolean isFromFlingCollapseRunnable = false;
    private final Runnable mFlingCollapseRunnable = new Runnable() {
        @Override
        public void run() {
            isFromFlingCollapseRunnable = true;
            fling(0, false /* expand */, mNextCollapseSpeedUpFactor,
                    false /* expandBecauseOfFalsing */);
            isFromFlingCollapseRunnable = false;
        }
    };
    /*PRIZE-add isFromFlingCollapseRunnable,bugid:17060- liufan-2016-06-15-end*/

    public void cancelPeek() {
        boolean cancelled = mPeekPending;
        if (mPeekAnimator != null) {
            cancelled = true;
            mPeekAnimator.cancel();
        }
        removeCallbacks(mPeekRunnable);
        mPeekPending = false;

        if (cancelled) {
            // When peeking, we already tell mBar that we expanded ourselves. Make sure that we also
            // notify mBar that we might have closed ourselves.
            notifyBarPanelExpansionChanged();
        }
    }

    public void expand(final boolean animate) {
        if (!isFullyCollapsed() && !isCollapsing()) {
            return;
        }

        mInstantExpanding = true;
        mAnimateAfterExpanding = animate;
        mUpdateFlingOnLayout = false;
        abortAnimations();
        cancelPeek();
        if (mTracking) {
            onTrackingStopped(true /* expands */); // The panel is expanded after this call.
        }
        if (mExpanding) {
            notifyExpandingFinished();
        }
        notifyBarPanelExpansionChanged();

        // Wait for window manager to pickup the change, so we know the maximum height of the panel
        // then.
        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!mInstantExpanding) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            return;
                        }
                        if (mStatusBar.getStatusBarWindow().getHeight()
                                != mStatusBar.getStatusBarHeight()) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if (mAnimateAfterExpanding) {
                                notifyExpandingStarted();
                                /*PRIZE-update the speed of animateExpandNotificationsPanel- liufan-2016-06-28-start*/
                                fling(4000, true /* expand */);
                                /*PRIZE-update the speed of animateExpandNotificationsPanel- liufan-2016-06-28-end*/
                            } else {
                                setExpandedFraction(1f);
                            }
                            mInstantExpanding = false;
                            /*PRIZE-if mStatusBar.isOccluded(),instantCollapse,bugid:16582- liufan-2016-05-31-start*/
                            if(mStatusBar.isOccluded()) instantCollapse();
                            /*PRIZE-if mStatusBar.isOccluded(),instantCollapse,bugid:16582- liufan-2016-05-31-end*/
                            /*PRIZE-showKeyguard and hideKeyguard quickly,bugid:16664- liufan-2016-05-31-start*/
                            if(mStatusBar.isShowKeyguard()){
                                mStatusBar.showBlurOnGloableLayout();
                            }
                            /*PRIZE-showKeyguard and hideKeyguard quickly,bugid:16664- liufan-2016-05-31-end*/
                            /*PRIZE-show with panelview,bugid:29275,52696- liufan-2017-03-01-start*/
                            if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW
                                && mStatusBar.getBarState() != StatusBarState.SHADE){
                                mStatusBar.showHaoKanView();
                                resetViews();
                            }
                            /*PRIZE-show with panelview,bugid:29275,52696- liufan-2017-03-01-end*/
                            mStatusBar.refreshBlurBgWhenLockscreen();//add for bugid:23195
                        }
                    }
                });

        // Make sure a layout really happens.
        requestLayout();
    }

    public void instantCollapse() {
        abortAnimations();
        setExpandedFraction(0f);
        if (mExpanding) {
            notifyExpandingFinished();
        }
        if (mInstantExpanding) {
            mInstantExpanding = false;
            notifyBarPanelExpansionChanged();
        }
    }

    private void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        removeCallbacks(mPostCollapseRunnable);
        removeCallbacks(mFlingCollapseRunnable);
    }

    protected void onClosingFinished() {
        mBar.onClosingFinished();
    }


    protected void startUnlockHintAnimation() {

        // We don't need to hint the user if an animation is already running or the user is changing
        // the expansion.
        if (mHeightAnimator != null || mTracking) {
            return;
        }
        cancelPeek();
        notifyExpandingStarted();
        startUnlockHintAnimationPhase1(new Runnable() {
            @Override
            public void run() {
                notifyExpandingFinished();
                mStatusBar.onHintFinished();
                mHintAnimationRunning = false;
            }
        });
        mStatusBar.onUnlockHintStarted();
        mHintAnimationRunning = true;
    }

    /**
     * Phase 1: Move everything upwards.
     */
    private void startUnlockHintAnimationPhase1(final Runnable onAnimationFinished) {
        float target = Math.max(0, getMaxPanelHeight() - mHintDistance);
        ValueAnimator animator = createHeightAnimator(target);
        animator.setDuration(250);
        animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCancelled) {
                    mHeightAnimator = null;
                    onAnimationFinished.run();
                } else {
                    startUnlockHintAnimationPhase2(onAnimationFinished);
                }
            }
        });
        animator.start();
        mHeightAnimator = animator;
        mKeyguardBottomArea.getIndicationView().animate()
                .translationY(-mHintDistance)
                .setDuration(250)
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mKeyguardBottomArea.getIndicationView().animate()
                                .translationY(0)
                                .setDuration(450)
                                .setInterpolator(mBounceInterpolator)
                                .start();
                    }
                })
                .start();
    }

    /**
     * Phase 2: Bounce down.
     */
    private void startUnlockHintAnimationPhase2(final Runnable onAnimationFinished) {
        ValueAnimator animator = createHeightAnimator(getMaxPanelHeight());
        animator.setDuration(450);
        animator.setInterpolator(mBounceInterpolator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHeightAnimator = null;
                onAnimationFinished.run();
                notifyBarPanelExpansionChanged();
            }
        });
        animator.start();
        mHeightAnimator = animator;
    }

    private ValueAnimator createHeightAnimator(float targetHeight) {
        ValueAnimator animator = ValueAnimator.ofFloat(mExpandedHeight, targetHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setExpandedHeightInternal((Float) animation.getAnimatedValue());
            }
        });
        return animator;
    }

    /*PRIZE-to public- liufan-2015-06-11-start*/
    public abstract boolean getQsExpandImmediate();
    public abstract void setQsExpandImmediate(boolean isQsExpandImmediate);
    public void notifyBarPanelExpansionChanged() {
        mBar.panelExpansionChanged(mExpandedFraction, mExpandedFraction > 0f || mPeekPending
                || mPeekAnimator != null || mInstantExpanding || isPanelVisibleBecauseOfHeadsUp()
                || mTracking || mHeightAnimator != null || getQsExpandImmediate());
    }
    public void notifyBarPanelExpansionChangedWithoutQsExpand(){
        mBar.panelExpansionChanged(mExpandedFraction, mExpandedFraction > 0f || mPeekPending
                || mPeekAnimator != null || mInstantExpanding || isPanelVisibleBecauseOfHeadsUp()
                || mTracking || mHeightAnimator != null);
    }
    /*PRIZE-to public- liufan-2015-06-11-end*/
    protected abstract boolean isPanelVisibleBecauseOfHeadsUp();

    /**
     * Gets called when the user performs a click anywhere in the empty area of the panel.
     *
     * @return whether the panel will be expanded after the action performed by this method
     */
    protected boolean onEmptySpaceClick(float x) {
        if (mHintAnimationRunning) {
            return true;
        }
        return onMiddleClicked();
    }

    protected final Runnable mPostCollapseRunnable = new Runnable() {
        @Override
        public void run() {
            collapse(false /* delayed */, 1.0f /* speedUpFactor */);
        }
    };

    protected abstract boolean onMiddleClicked();

    protected abstract boolean isDozing();

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(String.format("[PanelView(%s): expandedHeight=%f maxPanelHeight=%d closing=%s"
                + " tracking=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s touchDisabled=%s"
                + "]",
                this.getClass().getSimpleName(),
                getExpandedHeight(),
                getMaxPanelHeight(),
                mClosing?"T":"f",
                mTracking?"T":"f",
                mJustPeeked?"T":"f",
                mPeekAnimator, ((mPeekAnimator!=null && mPeekAnimator.isStarted())?" (started)":""),
                mHeightAnimator, ((mHeightAnimator !=null && mHeightAnimator.isStarted())?" (started)":""),
                mTouchDisabled?"T":"f"
        ));
    }

    public abstract void resetViews();

    protected abstract float getPeekHeight();

    protected abstract float getCannedFlingDurationFactor();

    /**
     * @return whether "Clear all" button will be visible when the panel is fully expanded
     */
    protected abstract boolean fullyExpandedClearAllVisible();

    protected abstract boolean isClearAllVisible();

    /**
     * @return the height of the clear all button, in pixels
     */
    protected abstract int getClearAllHeight();

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        mHeadsUpManager = headsUpManager;
    }
}
