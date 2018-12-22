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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.os.Trace;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.RemoteInputController;

import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManager;

import static com.android.keyguard.KeyguardHostView.OnDismissAction;
/* Dynamically hiding nav bar feature. solve bug-19908. & Nav bar related to mBack key feature. prize-linkh-20160808 */
import com.mediatek.common.prizeoption.PrizeOption;
/*-add for haokan-liufan-2018-01-04-start-*/
import com.mediatek.perfservice.PerfServiceWrapper;
/*-add for haokan-liufan-2018-01-04-end-*/

/**
 * Manages creating, showing, hiding and resetting the keyguard within the status bar. Calls back
 * via {@link ViewMediatorCallback} to poke the wake lock and report that the keyguard is done,
 * which is in turn, reported to this class by the current
 * {@link com.android.keyguard.KeyguardViewBase}.
 */
public class StatusBarKeyguardViewManager implements RemoteInputController.Callback {

    // When hiding the Keyguard with timing supplied from WindowManager, better be early than late.
    private static final long HIDE_TIMING_CORRECTION_MS = -3 * 16;

    // Delay for showing the navigation bar when the bouncer appears. This should be kept in sync
    // with the appear animations of the PIN/pattern/password views.
    private static final long NAV_BAR_SHOW_DELAY_BOUNCER = 320;

    private static final long WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS = 200;

    // Duration of the Keyguard dismissal animation in case the user is currently locked. This is to
    // make everything a bit slower to bridge a gap until the user is unlocked and home screen has
    // dranw its first frame.
    private static final long KEYGUARD_DISMISS_DURATION_LOCKED = 2000;

    private static String TAG = "StatusBarKeyguardViewManager";
    private final boolean DEBUG = true ;

    protected final Context mContext;

    protected LockPatternUtils mLockPatternUtils;
    protected ViewMediatorCallback mViewMediatorCallback;
    protected PhoneStatusBar mPhoneStatusBar;
    private ScrimController mScrimController;
    private FingerprintUnlockController mFingerprintUnlockController;

    private ViewGroup mContainer;
    private StatusBarWindowManager mStatusBarWindowManager;

    private boolean mDeviceInteractive = false;
    private boolean mScreenTurnedOn;
    protected KeyguardBouncer mBouncer;
    protected boolean mShowing;
    protected boolean mOccluded;
    protected boolean mRemoteInputActive;

    protected boolean mFirstUpdate = true;
    protected boolean mLastShowing;
    protected boolean mLastOccluded;
    private boolean mLastBouncerShowing;
    private boolean mLastBouncerDismissible;
    protected boolean mLastRemoteInputActive;

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
        mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(mContext,
                mViewMediatorCallback, mLockPatternUtils, mStatusBarWindowManager, container);
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
    protected void showBouncerOrKeyguard() {
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
        }
    }

    //add for bugid:56647-liufan-2018-5-9-start
    public boolean needsFullscreenBouncer(){
        return mBouncer != null && mBouncer.needsFullscreenBouncer();
    }
    //add for bugid:56647-liufan-2018-5-9-end

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
            mPhoneStatusBar.dismissKeyguardImmediately();
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

                if(PrizeOption.PRIZE_FACE_ID && !PrizeOption.PRIZE_FACE_ID_KOOBEE) {
                    sendPowerKeyScreenOnBroadcast(2);
                }
            } else {
                showBouncerOrKeyguard();
            }
            KeyguardUpdateMonitor.getInstance(mContext).sendKeyguardReset();
            updateStates();
        }
    }
	
	/*prize-add by lihuangyuan,for faceid-2017-10-26-start*/
    private void sendPowerKeyScreenOnBroadcast(int screenonoff)
    {
		/*Intent intent = new Intent("com.prize.faceid.service");
		mContext.sendBroadcast(intent);*/
        ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.face.FaceShowService");
        Intent intent = new Intent();
        intent.putExtra("screen_onoff", screenonoff);
        intent.setComponent(cn);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent);*/
        mContext.startService(intent);
    }
	/*prize-add by lihuangyuan,for faceid-2017-10-26-end*/


    public void onStartedGoingToSleep() {
        mPhoneStatusBar.onStartedGoingToSleep();
        /*prize remove for fast press power key activity lifecycle abnormal  liuwei 20180412 start*/
        /*-add for haokan-liufan-2018-01-04-start-*/
        //perfBoost();
        //if(mPanelView!=null){
        //    mPanelView.updateScreenOff();
        //}
        /*-add for haokan-liufan-2018-01-04-end-*/
        /*prize remove for fast press power key activity lifecycle abnormal  liuwei 20180412 end*/
    }

    public void onFinishedGoingToSleep() {
        mDeviceInteractive = false;

        /*prize add for fast press power key activity lifecycle abnormal  liuwei 20180412 start*/
        perfBoost();
        if(mPanelView!=null){
            mPanelView.updateScreenOff();
        }
        /*prize add for fast press power key activity lifecycle abnormal  liuwei 20180412 end*/

        mPhoneStatusBar.onFinishedGoingToSleep();
        mBouncer.onScreenTurnedOff();
    }

    public void onStartedWakingUp() {
        Trace.beginSection("StatusBarKeyguardViewManager#onStartedWakingUp");
        mDeviceInteractive = true;
        mDeviceWillWakeUp = false;
        if (mPhoneStatusBar != null) {
            mPhoneStatusBar.onStartedWakingUp();
        }

        /*-add for haokan-liufan-2018-01-04-start-*/
        if(mPanelView!=null){
            mPanelView.updateScreenOn();
        }
        /*-add for haokan-liufan-2018-01-04-end-*/
        Trace.endSection();
    }

    public void onScreenTurningOn() {
        Trace.beginSection("StatusBarKeyguardViewManager#onScreenTurningOn");
        if (mPhoneStatusBar != null) {
            mPhoneStatusBar.onScreenTurningOn();
        }
        Trace.endSection();
    }

    public boolean isScreenTurnedOn() {
        return mScreenTurnedOn;
    }

    public void onScreenTurnedOn() {
        Trace.beginSection("StatusBarKeyguardViewManager#onScreenTurnedOn");
        mScreenTurnedOn = true;
        if (mDeferScrimFadeOut) {
            mDeferScrimFadeOut = false;
            animateScrimControllerKeyguardFadingOut(0, WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS,
                    true /* skipFirstFrame */);
            updateStates();
        }
        if (mPhoneStatusBar != null) {
            mPhoneStatusBar.onScreenTurnedOn();
        }
        Trace.endSection();
    }

    @Override
    public void onRemoteInputActive(boolean active) {
        mRemoteInputActive = active;
        updateStates();
    }

    public void onScreenTurnedOff() {
        mScreenTurnedOn = false;
        mPhoneStatusBar.onScreenTurnedOff();
    }

    /*-add for haokan-liufan-2018-01-04-start-*/
    private NotificationPanelView mPanelView;
    public void setNotificationPanelView(NotificationPanelView view){
        mPanelView = view;
    }

    private PerfServiceWrapper mPerfService;
    public void perfBoost(){
        int mPerfHandle = -1;
        if (mPerfService == null) {
          mPerfService = new PerfServiceWrapper(null);
        }
        if (mPerfService != null) {
            mPerfHandle = mPerfService.userRegScn();
            Log.e(TAG, "StatusBarKeyguardViewManager Increase cpu!");
			mPerfService.userRegScnConfig(mPerfHandle, 50,99,0,0,0);
            mPerfService.userRegScnConfig(mPerfHandle, 15,1,4,0,0);
            mPerfService.userRegScnConfig(mPerfHandle, 15,0,4,0,0);
            mPerfService.userRegScnConfig(mPerfHandle, 17,1,2500000,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 17,0,2500000,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 10,3,0,0,0);
            /*prize modify for performance with async liuwei 20180412 start*/
            //mPerfService.userEnableTimeoutMs(mPerfHandle, 5000);
            mPerfService.userEnableTimeoutMsAsync(mPerfHandle, 5000);
            /*prize modify for performance with async liuwei 20180412 end*/
        } else {
            Log.e(TAG, "StatusBarKeyguardViewManager PerfService is not ready!");
        }
    }

    public void showMagazineView(){
        if(mPanelView!=null && !isOccluded()){
            mPanelView.showMagazineView();
        }
    }
    /*-add for haokan-liufan-2018-01-04-end-*/

    public void notifyDeviceWakeUpRequested() {
        mDeviceWillWakeUp = !mDeviceInteractive;
    }

    public void verifyUnlock() {
        ///M: fix ALPS01807921
        ///   We make VerifyUnlock flow just the same as it in KK.
        ///   This can prevent status unsynced.
        show(null);
        dismiss();
    }

    public void setNeedsInput(boolean needsInput) {
        Log.d(TAG, "setNeedsInput() - needsInput = " + needsInput) ;
        mStatusBarWindowManager.setKeyguardNeedsInput(needsInput);
    }

    public boolean isUnlockWithWallpaper() {
        return mStatusBarWindowManager.isShowingWallpaper();
    }

    public void setOccluded(boolean occluded, boolean animate) {
        /*PRIZE-lockscreen black : 43725,43594,43611-liufan-2016-11-30-start*/
        animate = false;
        /*PRIZE-lockscreen black : 43725,43594,43611-liufan-2016-11-30-end*/
        if (occluded && !mOccluded && mShowing) {
            //prize add by xiarui for Bug#52143 2018-03-16 @{
            //if (mPhoneStatusBar.isInLaunchTransition()) {
            if (mPhoneStatusBar.isInLaunchTransitionRunning()) {
            //@}
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
        mPhoneStatusBar.updateMediaMetaData(false, animate && !occluded);
        /*-modify for haokan-liufan-2017-1-27-start-*/
        if(!mOccluded){
            if(mPanelView != null){
                mPanelView.setWillOccluded(false);
                mPanelView.onBackPressedForHaoKan();
                NotificationPanelView.debugMagazine("isWillOccluded turn false");
            }
        }
        /*-modify for haokan-liufan-2017-1-27-end-*/
        mStatusBarWindowManager.setKeyguardOccluded(occluded);
        reset();
        if (animate && !occluded) {
            mPhoneStatusBar.animateKeyguardUnoccluding();
        }
    }

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

    /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
    public void hideKeyguard()
    {
        mShowing = false;
        mPhoneStatusBar.hideKeyguard();
        //mBouncer.hide(true /* destroyView */);
    }
    public void showKeyguard()
    {
        mPhoneStatusBar.showKeyguard();
        mShowing = true;
    }
    public void setFingerWakeup(int wakeupFinger)
    {
        mFingerWakeup = wakeupFinger;
    }
    private int mFingerWakeup = 0;
    /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
    /**
     * Hides the keyguard view
     */
    public void hide(long startTime, long fadeoutDuration) {
        mShowing = false;

        if (KeyguardUpdateMonitor.getInstance(mContext).needsSlowUnlockTransition()) {
            fadeoutDuration = KEYGUARD_DISMISS_DURATION_LOCKED;
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        long delay = Math.max(0, startTime + HIDE_TIMING_CORRECTION_MS - uptimeMillis);
        // @prize fanjunchen 2015-09-05{
        mPhoneStatusBar.setBackdropGone();
        // @prize end }
        /*PRIZE-for bug:42005-liufan-2017-11-10-start*/
        StatusBarWindowView.IS_HIDE_KEYGUARD_WHEN_SLIP =  true;
        android.util.Log.d(TAG,"42005 set IS_HIDE_KEYGUARD_WHEN_SLIP true");
        /*PRIZE-for bug:42005-liufan-2017-11-10-end*/
        if (mPhoneStatusBar.isInLaunchTransition() ) {
            mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable() {
                @Override
                public void run() {
                    mStatusBarWindowManager.setKeyguardShowing(false);
                    /* PRIZE-for lockscreen speed-liufan-2018-04-02 -start*/
                    if(PrizeOption.PRIZE_KEYGUARD_UNLOCK_FAST) {
                    mStatusBarWindowManager.setKeyguardFadingAway(false);
                    }else{
                    mStatusBarWindowManager.setKeyguardFadingAway(true);
                    }
                    /* PRIZE-for lockscreen speed-liufan-2018-04-02 -end*/ 
                    mBouncer.hide(true /* destroyView */);
                    updateStates();
                    mScrimController.animateKeyguardFadingOut(
                            PhoneStatusBar.FADE_KEYGUARD_START_DELAY,
                            PhoneStatusBar.FADE_KEYGUARD_DURATION, null,
                            false /* skipFirstFrame */);
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
                mPhoneStatusBar.setKeyguardFadingAway(startTime, 0, 240);
                mStatusBarWindowManager.setKeyguardFadingAway(true);
                mPhoneStatusBar.fadeKeyguardWhilePulsing();
                animateScrimControllerKeyguardFadingOut(0, 240, new Runnable() {
                    @Override
                    public void run() {
                        mPhoneStatusBar.hideKeyguard();
                    }
                }, false /* skipFirstFrame */);
            } else {
                mFingerprintUnlockController.startKeyguardFadingAway();
                mPhoneStatusBar.setKeyguardFadingAway(startTime, delay, fadeoutDuration);
		  /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
                boolean staying = false;
		  if(mFingerWakeup != 1)
		  {
		  	staying = mPhoneStatusBar.hideKeyguard();
		  }
		  /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/		  
                if (!staying) {
                    mStatusBarWindowManager.setKeyguardFadingAway(true);
                    if (mFingerprintUnlockController.getMode()
                            == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK) {
                        /*if (!mScreenTurnedOn) {
                            mDeferScrimFadeOut = true;
                        } else {

                            // Screen is already on, don't defer with fading out.
                            animateScrimControllerKeyguardFadingOut(0,
                                    WAKE_AND_UNLOCK_SCRIM_FADEOUT_DURATION_MS,
                                    true );
                        }*/
                        animateScrimControllerKeyguardFadingOut(0, 0, true /* skipFirstFrame */);
                    } else {
                        animateScrimControllerKeyguardFadingOut(delay, fadeoutDuration,
                                false /* skipFirstFrame */);
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
        }
    }

    public void onDensityOrFontScaleChanged() {
        mBouncer.hide(true /* destroyView */);
    }

    private void animateScrimControllerKeyguardFadingOut(long delay, long duration,
            boolean skipFirstFrame) {
        animateScrimControllerKeyguardFadingOut(delay, duration, null /* endRunnable */,
                skipFirstFrame);
    }

    private void animateScrimControllerKeyguardFadingOut(long delay, long duration,
            final Runnable endRunnable, boolean skipFirstFrame) {
        Trace.asyncTraceBegin(Trace.TRACE_TAG_VIEW, "Fading out", 0);
        mScrimController.animateKeyguardFadingOut(delay, duration, new Runnable() {
            @Override
            public void run() {
                if (endRunnable != null) {
                    endRunnable.run();
                }
                mStatusBarWindowManager.setKeyguardFadingAway(false);
                mPhoneStatusBar.finishKeyguardFadingAway();
                mFingerprintUnlockController.finishKeyguardFadingAway();
                WindowManagerGlobal.getInstance().trimMemory(
                        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
                Trace.asyncTraceEnd(Trace.TRACE_TAG_VIEW, "Fading out", 0);
            }
        }, skipFirstFrame);
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
            mPhoneStatusBar.endAffordanceLaunch();
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
            /* Dynamically hiding nav bar feature. solve bug-19908 &
             * Nav bar related to mBack key feature. prize-linkh-20160808 */
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
        boolean remoteInputActive = mRemoteInputActive;

        if ((bouncerDismissible || !showing || remoteInputActive) !=
                (mLastBouncerDismissible || !mLastShowing || mLastRemoteInputActive)
                || mFirstUpdate) {
            if (bouncerDismissible || !showing || remoteInputActive) {
                mContainer.setSystemUiVisibility(vis & ~View.STATUS_BAR_DISABLE_BACK);
            } else {
                mContainer.setSystemUiVisibility(vis | View.STATUS_BAR_DISABLE_BACK);
            }
        }
        //prize tangzhengrong 20180515 Swipe-up Gesture Navigation bar start
        if(PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION && mPhoneStatusBar.isEnableGestureNavigation()){
            if(showing && !occluded && !bouncerShowing){
                mPhoneStatusBar.hideGestureIndicator();
            }else{
                mPhoneStatusBar.showGestureIndicator();
            }
        }
        //prize tangzhengrong 20180515 Swipe-up Gesture Navigation bar start

        boolean navBarVisible = isNavBarVisible();
        boolean lastNavBarVisible = getLastNavBarVisible();
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

                    /* Dynamically hiding nav bar feature. solve bug-19908. & 
                     * Nav bar related to mBack key feature. prize-linkh-20160808 */
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
                    /* Dynamically hiding nav bar feature. solve bug-19908.
                     * Nav bar related to mBack key feature. prize-linkh-20160808 */
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

		 /*-prize-add by lihuangyuan,for fingerprint Authentication when occluded-2017-12-08-start*/
	     updateMonitor.setOccluded(occluded);
	     /*-prize-add by lihuangyuan,for fingerprint Authentication when occluded-2017-12-08-end*/
            updateMonitor.onKeyguardVisibilityChanged(showing && !occluded);
        }
        if (bouncerShowing != mLastBouncerShowing || mFirstUpdate) {
            updateMonitor.sendKeyguardBouncerChanged(bouncerShowing);
        }

        mFirstUpdate = false;
        mLastShowing = showing;
        mLastOccluded = occluded;
        mLastBouncerShowing = bouncerShowing;
        mLastBouncerDismissible = bouncerDismissible;
        mLastRemoteInputActive = remoteInputActive;

        mPhoneStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    /**
     * @return Whether the navigation bar should be made visible based on the current state.
     */
    protected boolean isNavBarVisible() {
        return !(mShowing && !mOccluded) || mBouncer.isShowing() || mRemoteInputActive;
    }

    /**
     * @return Whether the navigation bar was made visible based on the last known state.
     */
    protected boolean getLastNavBarVisible() {
        return !(mLastShowing && !mLastOccluded) || mLastBouncerShowing || mLastRemoteInputActive;
    }

    public boolean shouldDismissOnMenuPressed() {
        return mBouncer.shouldDismissOnMenuPressed();
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

    public ViewRootImpl getViewRootImpl() {
        return mPhoneStatusBar.getStatusBarView().getViewRootImpl();
    }
	
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-start*/
    public PhoneStatusBar getStatusBar() {
	    return mPhoneStatusBar;
    } 
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-end*/

	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-start*/
	public void nofityPowerKeyPower(boolean interactive) {
		if(mPhoneStatusBar != null){
			mPhoneStatusBar.nofityPowerKeyPower(interactive);
		}
    }
	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-end*/
}
