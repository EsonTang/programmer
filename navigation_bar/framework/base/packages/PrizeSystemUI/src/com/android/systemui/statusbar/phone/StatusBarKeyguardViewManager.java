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

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.os.Trace;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.statusbar.CommandQueue;

import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;

import static com.android.keyguard.KeyguardHostView.OnDismissAction;

// Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
import com.mediatek.common.prizeoption.PrizeOption;

/**
 * Manages creating, showing, hiding and resetting the keyguard within the status bar. Calls back
 * via {@link ViewMediatorCallback} to poke the wake lock and report that the keyguard is done,
 * which is in turn, reported to this class by the current
 * {@link com.android.keyguard.KeyguardViewBase}.
 */
public class StatusBarKeyguardViewManager {

    // When hiding the Keyguard with timing supplied from WindowManager, better be early than late.
    private static final long HIDE_TIMING_CORRECTION_MS = -3 * 16;

    // Delay for showing the navigation bar when the bouncer appears. This should be kept in sync
    // with the appear animations of the PIN/pattern/password views.
    private static final long NAV_BAR_SHOW_DELAY_BOUNCER = 320;

    private static final long WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS = 200;

    private static String TAG = "StatusBarKeyguardViewManager";
    private final boolean DEBUG = true ;

    private final Context mContext;

    private LockPatternUtils mLockPatternUtils;
    private ViewMediatorCallback mViewMediatorCallback;
    private PhoneStatusBar mPhoneStatusBar;
    private ScrimController mScrimController;
    private FingerprintUnlockController mFingerprintUnlockController;

    private ViewGroup mContainer;
    private StatusBarWindowManager mStatusBarWindowManager;

    private boolean mDeviceInteractive = false;
    private boolean mScreenTurnedOn;
    private KeyguardBouncer mBouncer;
    private boolean mShowing;
    private boolean mOccluded;

    private boolean mFirstUpdate = true;
    private boolean mLastShowing;
    private boolean mLastOccluded;
    private boolean mLastBouncerShowing;
    private boolean mLastBouncerDismissible;
    private boolean mLastDeferScrimFadeOut;
    private OnDismissAction mAfterKeyguardGoneAction;
    private boolean mDeviceWillWakeUp;
    private boolean mDeferScrimFadeOut;

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback callback,
            LockPatternUtils lockPatternUtils) {
        mContext = context;
        mViewMediatorCallback = callback;
        mLockPatternUtils = lockPatternUtils;
    }

    public void registerStatusBar(PhoneStatusBar phoneStatusBar,
            ViewGroup container, StatusBarWindowManager statusBarWindowManager,
            ScrimController scrimController,
            FingerprintUnlockController fingerprintUnlockController) {
        mPhoneStatusBar = phoneStatusBar;
        mContainer = container;
        mStatusBarWindowManager = statusBarWindowManager;
        mScrimController = scrimController;
        mFingerprintUnlockController = fingerprintUnlockController;
        mBouncer = new KeyguardBouncer(mContext, mViewMediatorCallback, mLockPatternUtils,
                mStatusBarWindowManager, container);
    }

    /**
     * Show the keyguard.  Will handle creating and attaching to the view manager
     * lazily.
     */
    public void show(Bundle options) {
        if (DEBUG) Log.d(TAG, "show() is called.") ;
        mShowing = true;
        mStatusBarWindowManager.setKeyguardShowing(true);
        mScrimController.abortKeyguardFadingOut();
        reset();
    }

    /**
     * Shows the notification keyguard or the bouncer depending on
     * {@link KeyguardBouncer#needsFullscreenBouncer()}.
     */
    private void showBouncerOrKeyguard() {
        if (DEBUG) Log.d(TAG, "showBouncerOrKeyguard() is called.") ;

        if (mBouncer.needsFullscreenBouncer()) {
            if (DEBUG) {
                Log.d(TAG, "needsFullscreenBouncer() is true, show \"Bouncer\" view directly.");
            }
            // The keyguard might be showing (already). So we need to hide it.
            mPhoneStatusBar.hideKeyguard();
            mBouncer.show(true /* resetSecuritySelection */);
        } else {
            if (DEBUG) {
                Log.d(TAG, "needsFullscreenBouncer() is false,"
                    + "show \"Notification Keyguard\" view.");
            }
            mPhoneStatusBar.showKeyguard();
            mBouncer.hide(false /* destroyView */);
            mBouncer.prepare();
            /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-23-start*/
            //mPhoneStatusBar.isShowKeyguardChargingAnimation(true,true,true);
            /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-23-end*/
        }
    }

    private void showBouncer() {
        showBouncer(false) ;
    }

    private void showBouncer(boolean authenticated) {
        if (mShowing) {
            mBouncer.show(false, authenticated);
        }
        updateStates();
    }

    public void dismissWithAction(OnDismissAction r, Runnable cancelAction,
            boolean afterKeyguardGone) {
        if (mShowing) {
            if (!afterKeyguardGone) {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = false," +
                    "call showWithDismissAction") ;
                mBouncer.showWithDismissAction(r);
            } else {
                Log.d(TAG, "dismissWithAction() - afterKeyguardGone = true, call bouncer.show()") ;
                mBouncer.show(false /* resetSecuritySelection */);
                mAfterKeyguardGoneAction = r;
            }
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-start*/
            mPhoneStatusBar.dismissKeyguard();
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-end*/
        }
        updateStates();
    }

    /**
     * Reset the state of the view.
     */
    public void reset() {
        if (DEBUG) {
            Log.d(TAG, "reset() is called, mShowing = " + mShowing + " ,mOccluded = " + mOccluded);
        }
        if (mShowing) {
            if (mOccluded) {
                /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-Start*/
                mPhoneStatusBar.overTouchEvent();
                /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-End*/
                mPhoneStatusBar.hideKeyguard();
                mPhoneStatusBar.stopWaitingForKeyguardExit();
                mBouncer.hide(false /* destroyView */);
            } else {
                showBouncerOrKeyguard();
            }
            KeyguardUpdateMonitor.getInstance(mContext).sendKeyguardReset();
            updateStates();
        }
    }

    public void onFinishedGoingToSleep() {
        mDeviceInteractive = false;
        mPhoneStatusBar.onFinishedGoingToSleep();
        mBouncer.onScreenTurnedOff();
    }

    public void onStartedWakingUp() {
        mDeviceInteractive = true;
        mDeviceWillWakeUp = false;
        mPhoneStatusBar.onStartedWakingUp();
    }

    public void onScreenTurningOn() {
        mPhoneStatusBar.onScreenTurningOn();
    }

    public void onScreenTurnedOn() {
        mScreenTurnedOn = true;
        if (mDeferScrimFadeOut) {
            mDeferScrimFadeOut = false;
            animateScrimControllerKeyguardFadingOut(0, WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS);
            updateStates();
        }
        mPhoneStatusBar.onScreenTurnedOn();
    }

    public void onScreenTurnedOff() {
        mScreenTurnedOn = false;
    }

    public void notifyDeviceWakeUpRequested() {
        mDeviceWillWakeUp = !mDeviceInteractive;
    }

    public void verifyUnlock() {
        ///M: fix ALPS01807921
        ///   We make VerifyUnlock flow just the same as it in KK.
        ///   This can prevent status unsynced.
        /*PRIZE-add for 360 Smart lockscreen,bugid:29415-liufan-2017-02-28-start*/
        //show(null);
        /*PRIZE-add for 360 Smart lockscreen,bugid:29415-liufan-2017-02-28-end*/
        dismiss();
    }

    public void setNeedsInput(boolean needsInput) {
        Log.d(TAG, "setNeedsInput() - needsInput = " + needsInput) ;
        mStatusBarWindowManager.setKeyguardNeedsInput(needsInput);
    }

    public void updateUserActivityTimeout() {
        mStatusBarWindowManager.setKeyguardUserActivityTimeout(mBouncer.getUserActivityTimeout());
    }

    public void setOccluded(boolean occluded) {
        if (occluded && !mOccluded && mShowing) {
            if (mPhoneStatusBar.isInLaunchTransition()) {
                mOccluded = true;
                mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(null /* beforeFading */,
                        new Runnable() {
                            @Override
                            public void run() {
                                ///M: [ALPS01807921]
                                ///   mOccluded may be changed before the runnable is executed.
                                if (mOccluded) {
                                    Log.d(TAG, "setOccluded.run() - setKeyguardOccluded(true)") ;
                                    mStatusBarWindowManager.setKeyguardOccluded(true);
                                    reset();
                                } else {
                                    Log.d(TAG, "setOccluded.run() - mOccluded was set to false") ;
                                }
                            }
                        });
                return;
            }
        }
        mOccluded = occluded;
        Log.d(TAG, "setOccluded() - setKeyguardOccluded(" + occluded + ")") ;
        mStatusBarWindowManager.setKeyguardOccluded(occluded);
        reset();
    }
	
	//add by vlife-liufan-2016-08-22-start
	public void hideVlifeKeyguardView(){
		mPhoneStatusBar.hideVlifeKeyguardView();
	}
	//add by vlife-liufan-2016-08-22-end
	
	//add by zookingsoft-20161114-start
	public void hideZookingKeyguardView(){
		mPhoneStatusBar.hideZookingKeyguardView();
	}
	//add by zookingsoft-20161114-end

    public boolean isOccluded() {
        return mOccluded;
    }

    /**
     * Starts the animation before we dismiss Keyguard, i.e. an disappearing animation on the
     * security view of the bouncer.
     *
     * @param finishRunnable the runnable to be run after the animation finished, or {@code null} if
     *                       no action should be run
     */
    public void startPreHideAnimation(Runnable finishRunnable) {
        if (mBouncer.isShowing()) {
            mBouncer.startPreHideAnimation(finishRunnable);
        } else if (finishRunnable != null) {
            finishRunnable.run();
        }
    }

    /**
     * Hides the keyguard view
     */
    public void hide(long startTime, final long fadeoutDuration) {
        if (DEBUG) Log.d(TAG, "hide() is called.") ;

        mShowing = false;

        long uptimeMillis = SystemClock.uptimeMillis();
        long delay = Math.max(0, startTime + HIDE_TIMING_CORRECTION_MS - uptimeMillis);
        // @prize fanjunchen 2015-09-05{
        mPhoneStatusBar.setBackdropGone();
        // @prize end }
        if (mPhoneStatusBar.isInLaunchTransition() ) {
            mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable() {
                @Override
                public void run() {
                    mStatusBarWindowManager.setKeyguardShowing(false);
                    mStatusBarWindowManager.setKeyguardFadingAway(true);
                    mBouncer.hide(true /* destroyView */);
                    updateStates();
                    mScrimController.animateKeyguardFadingOut(
                            PhoneStatusBar.FADE_KEYGUARD_START_DELAY,
                            PhoneStatusBar.FADE_KEYGUARD_DURATION, null);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    mPhoneStatusBar.hideKeyguard();
                    mStatusBarWindowManager.setKeyguardFadingAway(false);
                    mViewMediatorCallback.keyguardGone();
                    executeAfterKeyguardGoneAction();
                }
            });
        } else {
            if (mFingerprintUnlockController.getMode()
                    == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING) {
                mFingerprintUnlockController.startKeyguardFadingAway();
                mPhoneStatusBar.setKeyguardFadingAway(startTime, 0, 250);
                mPhoneStatusBar.fadeKeyguardWhilePulsing();
                animateScrimControllerKeyguardFadingOut(0, 250);
            } else {
                mFingerprintUnlockController.startKeyguardFadingAway();
                mPhoneStatusBar.setKeyguardFadingAway(startTime, delay, fadeoutDuration);
                boolean staying = mPhoneStatusBar.hideKeyguard();
                if (!staying) {
                    mStatusBarWindowManager.setKeyguardFadingAway(true);
                    if (mFingerprintUnlockController.getMode()
                            == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK) {
                        if (!mScreenTurnedOn) {
                            mDeferScrimFadeOut = true;
                        } else {

                            // Screen is already on, don't defer with fading out.
                            animateScrimControllerKeyguardFadingOut(0,
                                    WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS);
                        }
                    } else {
                        animateScrimControllerKeyguardFadingOut(delay, fadeoutDuration);
                    }
                } else {
                    mScrimController.animateGoingToFullShade(delay, fadeoutDuration);
                    mPhoneStatusBar.finishKeyguardFadingAway();
                }
            }
            mStatusBarWindowManager.setKeyguardShowing(false);
            mBouncer.hide(true /* destroyView */);
            mViewMediatorCallback.keyguardGone();
            executeAfterKeyguardGoneAction();
            updateStates();
			mPhoneStatusBar.hideVlifeKeyguardView();//add by vlife-liufan-2016-08-20
			mPhoneStatusBar.hideZookingKeyguardView();//add by zookingsoft 20161114
        }
    }

    private void animateScrimControllerKeyguardFadingOut(long delay, long duration) {
        Trace.asyncTraceBegin(Trace.TRACE_TAG_VIEW, "Fading out", 0);
        mScrimController.animateKeyguardFadingOut(delay, duration, new Runnable() {
            @Override
            public void run() {
                mStatusBarWindowManager.setKeyguardFadingAway(false);
                mPhoneStatusBar.finishKeyguardFadingAway();
                mFingerprintUnlockController.finishKeyguardFadingAway();
                WindowManagerGlobal.getInstance().trimMemory(
                        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
                Trace.asyncTraceEnd(Trace.TRACE_TAG_VIEW, "Fading out", 0);
            }
        });
    }

    private void executeAfterKeyguardGoneAction() {
        if (mAfterKeyguardGoneAction != null) {
            Log.d(TAG, "executeAfterKeyguardGoneAction() is called");
            mAfterKeyguardGoneAction.onDismiss();
            mAfterKeyguardGoneAction = null;
        }
    }

    /**
     * Dismisses the keyguard by going to the next screen or making it gone.
     */
    public void dismiss() {
        dismiss(false);
    }

    ///M:
    public void dismiss(boolean authenticated) {
        if (DEBUG) Log.d(TAG, "dismiss(authenticated = " + authenticated + ") is called." +
             " mScreenOn = " + mDeviceInteractive) ;
        // VoiceWakeup will need to dismiss keyguard even if screen is off.
        if (mDeviceInteractive || VoiceWakeupManager.getInstance().isDismissAndLaunchApp()) {
            showBouncer(authenticated);
        }

        if (mDeviceInteractive || mDeviceWillWakeUp) {
            showBouncer();
        }
    }

    /**
     * WARNING: This method might cause Binder calls.
     */
    public boolean isSecure() {
        return mBouncer.isSecure();
    }

    /**
     * @return Whether the keyguard is showing
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Notifies this manager that the back button has been pressed.
     *
     * @return whether the back press has been handled
     */
    public boolean onBackPressed() {
        Log.d(TAG, "onBackPressed()") ;
        if (mBouncer.isShowing()) {
            Log.d(TAG, "onBackPressed() - reset & return true") ;
            reset();

            ///M : fix ALPS01852958, clear mAfterKeyguardGoneAction when leaving bouncer.
            mAfterKeyguardGoneAction = null ;
            return true;
        }
        Log.d(TAG, "onBackPressed() - reset & return false") ;
        return false;
    }

    public boolean isBouncerShowing() {
        return mBouncer.isShowing();
    }

    private long getNavBarShowDelay() {
        if (mPhoneStatusBar.isKeyguardFadingAway()) {
            return mPhoneStatusBar.getKeyguardFadingAwayDelay();
        } else {

            // Keyguard is not going away, thus we are showing the navigation bar because the
            // bouncer is appearing.
            return NAV_BAR_SHOW_DELAY_BOUNCER;
        }
    }

    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mMakeNavigationBarVisibleRunnable - set nav bar VISIBLE.") ;
            mPhoneStatusBar.getNavigationBarView().setVisibility(View.VISIBLE);
            // Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
            if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || 
                PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
                mPhoneStatusBar.updateNavigationBarView(null);
            }//END...
        }
    };

    public void updateStates() {
        int vis = mContainer.getSystemUiVisibility();
        boolean showing = mShowing;
        boolean occluded = mOccluded;
        boolean bouncerShowing = mBouncer.isShowing();
        boolean bouncerDismissible = !mBouncer.isFullscreenBouncer();
        boolean deferScrimFadeOut = mDeferScrimFadeOut;

        if ((bouncerDismissible || !showing) != (mLastBouncerDismissible || !mLastShowing)
                || mFirstUpdate) {
            if (bouncerDismissible || !showing) {
                mContainer.setSystemUiVisibility(vis & ~View.STATUS_BAR_DISABLE_BACK);
            } else {
                mContainer.setSystemUiVisibility(vis | View.STATUS_BAR_DISABLE_BACK);
            }
        }

        // Hide navigation bar on Keyguard but not on bouncer and also if we are deferring a scrim
        // fade out, i.e. we are waiting for the screen to have turned on.
        boolean navBarVisible = !deferScrimFadeOut && (!(showing && !occluded) || bouncerShowing);
        boolean lastNavBarVisible = !mLastDeferScrimFadeOut && (!(mLastShowing && !mLastOccluded)
                || mLastBouncerShowing);
        if (navBarVisible != lastNavBarVisible || mFirstUpdate) {
            Log.d(TAG, "updateStates() - showing = " + showing
                    + ", mLastShowing = " + mLastShowing
                    + "\nupdateStates() - occluded = " + occluded
                    + "mLastOccluded = " + mLastOccluded
                    + "\nupdateStates() - bouncerShowing = " + bouncerShowing
                    + ", mLastBouncerShowing = " + mLastBouncerShowing
                    + "\nupdateStates() - mFirstUpdate = " + mFirstUpdate) ;

            if (mPhoneStatusBar.getNavigationBarView() != null) {
                if (navBarVisible) {

                    //mod for controlling nav bar.  prize-linkh-20150805
                    if((PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) 
                        && mPhoneStatusBar.getNavigationBarView().mHideByUser) {
                        Log.d(TAG, "updateStates() - NavBar has been hidden by user. So ignore revealing!");
                    } else { //end....
                        long delay = getNavBarShowDelay();
                        if (delay == 0) {
                            mMakeNavigationBarVisibleRunnable.run();
                        } else {
                            mContainer.postOnAnimationDelayed(mMakeNavigationBarVisibleRunnable,
                                    delay);
                        }
                    }
                } else {
                    Log.d(TAG, "updateStates() - set nav bar GONE"
                            + " for showing notification keyguard.");
                    mContainer.removeCallbacks(mMakeNavigationBarVisibleRunnable);
                    // Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
                    if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || 
                        PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
                        if (mPhoneStatusBar.getNavigationBarView().mHideByUser) {
                            Log.d(TAG, "updateStates() - nav bar has been GONE!");
                        } else {
                            mPhoneStatusBar.getNavigationBarView().setVisibility(View.GONE);
                            mPhoneStatusBar.updateNavigationBarView(PhoneStatusBar.NAV_BAR_GONE_BECAUSE_OF_KEYGUARD);
                        }
                    } else {
                        mPhoneStatusBar.getNavigationBarView().setVisibility(View.GONE);
                    }
                }
            }
        }

        if (bouncerShowing != mLastBouncerShowing || mFirstUpdate) {
            Log.d(TAG, "updateStates() - setBouncerShowing(" + bouncerShowing + ")") ;
            mStatusBarWindowManager.setBouncerShowing(bouncerShowing);
            mPhoneStatusBar.setBouncerShowing(bouncerShowing);
            mScrimController.setBouncerShowing(bouncerShowing);
        }

        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        if ((showing && !occluded) != (mLastShowing && !mLastOccluded) || mFirstUpdate) {
            updateMonitor.onKeyguardVisibilityChanged(showing && !occluded);
        }
        if (bouncerShowing != mLastBouncerShowing || mFirstUpdate) {
            updateMonitor.sendKeyguardBouncerChanged(bouncerShowing);
        }

        mFirstUpdate = false;
        mLastShowing = showing;
        mLastOccluded = occluded;
        mLastDeferScrimFadeOut = deferScrimFadeOut;
        mLastBouncerShowing = bouncerShowing;
        mLastBouncerDismissible = bouncerDismissible;

        mPhoneStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    public boolean onMenuPressed() {
        return mBouncer.onMenuPressed();
    }

    public boolean interceptMediaKey(KeyEvent event) {
        return mBouncer.interceptMediaKey(event);
    }

    public void onActivityDrawn() {
        if (mPhoneStatusBar.isCollapsing()) {
            mPhoneStatusBar.addPostCollapseAction(new Runnable() {
                @Override
                public void run() {
                    mViewMediatorCallback.readyForKeyguardDone();
                }
            });
        } else {
            mViewMediatorCallback.readyForKeyguardDone();
        }
    }

    public boolean shouldDisableWindowAnimationsForUnlock() {
        return mPhoneStatusBar.isInLaunchTransition();
    }

    public boolean isGoingToNotificationShade() {
        return mPhoneStatusBar.isGoingToNotificationShade();
    }

    public boolean isSecure(int userId) {
        return mBouncer.isSecure() || mLockPatternUtils.isSecure(userId);
    }

    public boolean isInputRestricted() {
        return mViewMediatorCallback.isInputRestricted();
    }

    public void keyguardGoingAway() {
        mPhoneStatusBar.keyguardGoingAway();
    }

    public void animateCollapsePanels(float speedUpFactor) {
        mPhoneStatusBar.animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE, true /* force */,
                false /* delayed */, speedUpFactor);
    }

    /**
     * Notifies that the user has authenticated by other means than using the bouncer, for example,
     * fingerprint.
     */
    public void notifyKeyguardAuthenticated(boolean strongAuth) {
        mBouncer.notifyKeyguardAuthenticated(strongAuth);
    }

    public void showBouncerMessage(String message, int color) {
        mBouncer.showMessage(message, color);
    }
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-start*/
    public PhoneStatusBar getStatusBar() {
	return mPhoneStatusBar;
    } 
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-end*/
}
