/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import com.android.keyguard.KeyguardConstants;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;

/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
import android.hardware.fingerprint.FingerprintManager;
/*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/

import com.mediatek.common.prizeoption.PrizeOption;

/**
 * Controller which coordinates all the fingerprint unlocking actions with the UI.
 */
public class FingerprintUnlockController extends KeyguardUpdateMonitorCallback {

    private static final String TAG = "FingerprintController";
    private static final boolean DEBUG_FP_WAKELOCK = KeyguardConstants.DEBUG_FP_WAKELOCK;
    private static final long FINGERPRINT_WAKELOCK_TIMEOUT_MS = 15 * 1000;
    private static final String FINGERPRINT_WAKE_LOCK_NAME = "wake-and-unlock wakelock";

    /**
     * Mode in which we don't need to wake up the device when we get a fingerprint.
     */
    public static final int MODE_NONE = 0;

    /**
     * Mode in which we wake up the device, and directly dismiss Keyguard. Active when we acquire
     * a fingerprint while the screen is off and the device was sleeping.
     */
    public static final int MODE_WAKE_AND_UNLOCK = 1;

    /**
     * Mode in which we wake the device up, and fade out the Keyguard contents because they were
     * already visible while pulsing in doze mode.
     */
    public static final int MODE_WAKE_AND_UNLOCK_PULSING = 2;

    /**
     * Mode in which we wake up the device, but play the normal dismiss animation. Active when we
     * acquire a fingerprint pulsing in doze mode.
     */
    public static final int MODE_SHOW_BOUNCER = 3;

    /**
     * Mode in which we only wake up the device, and keyguard was not showing when we acquired a
     * fingerprint.
     * */
    public static final int MODE_ONLY_WAKE = 4;

    /**
     * Mode in which fingerprint unlocks the device.
     */
    public static final int MODE_UNLOCK = 5;

    /**
     * Mode in which fingerprint brings up the bouncer because fingerprint unlocking is currently
     * not allowed.
     */
    public static final int MODE_DISMISS_BOUNCER = 6;

    /**
     * How much faster we collapse the lockscreen when authenticating with fingerprint.
     */
    private static final float FINGERPRINT_COLLAPSE_SPEEDUP_FACTOR = 1.3f;

    private PowerManager mPowerManager;
    private Handler mHandler = new Handler();
    private PowerManager.WakeLock mWakeLock;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private int mMode;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarWindowManager mStatusBarWindowManager;
    private DozeScrimController mDozeScrimController;
    private KeyguardViewMediator mKeyguardViewMediator;
    private ScrimController mScrimController;
    private PhoneStatusBar mPhoneStatusBar;
    private boolean mGoingToSleep;
    private int mPendingAuthenticatedUserId = -1;

    public FingerprintUnlockController(Context context,
            StatusBarWindowManager statusBarWindowManager,
            DozeScrimController dozeScrimController,
            KeyguardViewMediator keyguardViewMediator,
            ScrimController scrimController,
            PhoneStatusBar phoneStatusBar) {
        mPowerManager = context.getSystemService(PowerManager.class);
        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
	 /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
        //mUpdateMonitor.registerCallback(this);
        mUpdateMonitor.setFingerprintAuthCallback(this);
        /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
        mStatusBarWindowManager = statusBarWindowManager;
        mDozeScrimController = dozeScrimController;
        mKeyguardViewMediator = keyguardViewMediator;
        mScrimController = scrimController;
        mPhoneStatusBar = phoneStatusBar;
    }

    public void setStatusBarKeyguardViewManager(
            StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    private final Runnable mReleaseFingerprintWakeLockRunnable = new Runnable() {
        @Override
        public void run() {
            if (DEBUG_FP_WAKELOCK) {
                Log.i(TAG, "fp wakelock: TIMEOUT!!");
            }
            releaseFingerprintWakeLock();
        }
    };

    private void releaseFingerprintWakeLock() {
        if (mWakeLock != null) {
            mHandler.removeCallbacks(mReleaseFingerprintWakeLockRunnable);
            if (DEBUG_FP_WAKELOCK) {
                Log.i(TAG, "releasing fp wakelock");
            }
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onFingerprintAcquired() {
        Trace.beginSection("FingerprintUnlockController#onFingerprintAcquired");
        releaseFingerprintWakeLock();
        if (!mUpdateMonitor.isDeviceInteractive()) {
            mWakeLock = mPowerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, FINGERPRINT_WAKE_LOCK_NAME);
            Trace.beginSection("acquiring wake-and-unlock");
            mWakeLock.acquire();
            Trace.endSection();
            if (DEBUG_FP_WAKELOCK) {
                Log.i(TAG, "fingerprint acquired, grabbing fp wakelock");
            }
            mHandler.postDelayed(mReleaseFingerprintWakeLockRunnable,
                    FINGERPRINT_WAKELOCK_TIMEOUT_MS);
            if (mDozeScrimController.isPulsing()) {

                // If we are waking the device up while we are pulsing the clock and the
                // notifications would light up first, creating an unpleasant animation.
                // Defer changing the screen brightness by forcing doze brightness on our window
                // until the clock and the notifications are faded out.
                mStatusBarWindowManager.setForceDozeBrightness(true);
            }
        }
        Trace.endSection();
    }
    /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-start*/
    private int mFingerFailedtimes = 0;
    private boolean mIsLockOutError = false;
    public void showBrouncer()
    {
        boolean wasDeviceInteractive = mUpdateMonitor.isDeviceInteractive();
	 Log.d(TAG,"showBrouncer wasDeviceInteractive:"+wasDeviceInteractive);
	 if (!wasDeviceInteractive) 
	 {
              mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
        }
        /*-add for haokan-liufan-2018-01-23-start-*/
        mStatusBarKeyguardViewManager.showMagazineView();
        /*-add for haokan-liufan-2018-01-23-end-*/
        mStatusBarKeyguardViewManager.animateCollapsePanels(FINGERPRINT_COLLAPSE_SPEEDUP_FACTOR);
    }
    /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-end*/
    @Override
    public void onFingerprintAuthenticated(int userId) {
        Trace.beginSection("FingerprintUnlockController#onFingerprintAuthenticated");
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-start*/
	 mFingerFailedtimes = 0;
	 mIsLockOutError =  false;
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-end*/
	 /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
	 mHandler.removeCallbacks(mFingerWakeupTimeout);
        if(mKeyguardViewMediator.isFingerWakeup() != 1)
        {
            if(mPowerManager.isSleep() == 1)
            {
                mPowerManager.wakeUp(SystemClock.uptimeMillis(),"android.policy:FINGERPRINT");
            }            
        }
	  /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
        if (mUpdateMonitor.isGoingToSleep()) {
            mPendingAuthenticatedUserId = userId;
            Trace.endSection();
            return;
        }
        boolean wasDeviceInteractive = mUpdateMonitor.isDeviceInteractive();
        mMode = calculateMode();
        if (!wasDeviceInteractive) {
            if (DEBUG_FP_WAKELOCK) {
                Log.i(TAG, "fp wakelock: Authenticated, waking up...");
            }
	     /*-prize-removed by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
            //mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
	     /*-prize-removed by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
        }
        Trace.beginSection("release wake-and-unlock");
        releaseFingerprintWakeLock();
        Trace.endSection();
	  //add by lihuanguan
	  Log.v(TAG, "onFingerprintAuthenticated, mMode="+mMode);
        switch (mMode) {
	     /*prize-add by lihuangyuan,for fingerprint Authentication when occluded -2017-12-08-start*/
	     case MODE_UNLOCK:
	              mKeyguardViewMediator.handleHideForFingerPrint();
	          break;
	     /*prize-add by lihuangyuan,for fingerprint Authentication when occluded -2017-12-08-end*/
            case MODE_DISMISS_BOUNCER:
                Trace.beginSection("MODE_DISMISS");
                mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(
                        false /* strongAuth */);
                Trace.endSection();
                break;
            
            case MODE_SHOW_BOUNCER:
                Trace.beginSection("MODE_UNLOCK or MODE_SHOW_BOUNCER");
                if (!wasDeviceInteractive) {
                    mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                }
                /* PRIZE-for lockscreen speed-liufan-2018-04-02 -start*/
                //removed by lihuangyuan,do not unlock for show bouncer
                /*if(PrizeOption.PRIZE_KEYGUARD_UNLOCK_FAST) {
                    mKeyguardViewMediator.keyguardDone(true);
                }else*/
                {	                
                mStatusBarKeyguardViewManager.animateCollapsePanels(
                        FINGERPRINT_COLLAPSE_SPEEDUP_FACTOR);
                }
                /* PRIZE-for lockscreen speed-liufan-2018-04-02 -end*/                        
                Trace.endSection();
                break;
            case MODE_WAKE_AND_UNLOCK_PULSING:
                Trace.beginSection("MODE_WAKE_AND_UNLOCK_PULSING");
                mPhoneStatusBar.updateMediaMetaData(false /* metaDataChanged */, 
                        true /* allowEnterAnimation */);
                // Fall through.
                Trace.endSection();
            case MODE_WAKE_AND_UNLOCK:
                Trace.beginSection("MODE_WAKE_AND_UNLOCK");
		  mKeyguardViewMediator.onWakeAndUnlocking();
                mStatusBarWindowManager.setStatusBarFocusable(false);
                mDozeScrimController.abortPulsing();
                mScrimController.setWakeAndUnlocking();
                if (mPhoneStatusBar.getNavigationBarView() != null) {
                    mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
                }
                Trace.endSection();
                break;
            case MODE_ONLY_WAKE:
            case MODE_NONE:
                break;
        }
        if (mMode != MODE_WAKE_AND_UNLOCK_PULSING) {
            mStatusBarWindowManager.setForceDozeBrightness(false);
        }
        mPhoneStatusBar.notifyFpAuthModeChanged();
        Trace.endSection();
    }

    @Override
    public void onStartedGoingToSleep(int why) {
        mPendingAuthenticatedUserId = -1;
    }

    @Override
    public void onFinishedGoingToSleep(int why) {
        Trace.beginSection("FingerprintUnlockController#onFinishedGoingToSleep");
        if (mPendingAuthenticatedUserId != -1) {

            // Post this to make sure it's executed after the device is fully locked.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onFingerprintAuthenticated(mPendingAuthenticatedUserId);
                }
            });
        }
        mPendingAuthenticatedUserId = -1;
        Trace.endSection();
    }

    public int getMode() {
        return mMode;
    }

    private int calculateMode() {
        boolean unlockingAllowed = mUpdateMonitor.isUnlockingWithFingerprintAllowed();
        if (!mUpdateMonitor.isDeviceInteractive()) {
            if (!mStatusBarKeyguardViewManager.isShowing()) {
                return MODE_WAKE_AND_UNLOCK;//MODE_ONLY_WAKE;//modify by lihuangyuan,for fingerprint quick unlock-2018-03-05
            } else if (mDozeScrimController.isPulsing() && unlockingAllowed) {
                return MODE_WAKE_AND_UNLOCK_PULSING;
            } else if (unlockingAllowed) {
                return MODE_WAKE_AND_UNLOCK;
            } else {
                return MODE_SHOW_BOUNCER;
            }
        }
        if (mStatusBarKeyguardViewManager.isShowing()) {
            if (mStatusBarKeyguardViewManager.isBouncerShowing() && unlockingAllowed) {
                return MODE_DISMISS_BOUNCER;
            } else if (unlockingAllowed) {
                return MODE_UNLOCK;
            } else if (!mStatusBarKeyguardViewManager.isBouncerShowing()) {
                return MODE_SHOW_BOUNCER;
            }
        }
        return MODE_NONE;
    }

    @Override
    public void onFingerprintAuthFailed() {
        /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
	 Log.d(TAG,"onFingerprintAuthFailed...");
        synchronized (mFingerWakeupTimeout)
	 {
            mFingerWakeupTimeout.run();
	 }	 
	 /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
        cleanup();
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-start*/
	 mFingerFailedtimes ++;
	 Log.v(TAG, "onFingerprintAuthFailed, mFingerFailedtimes="+mFingerFailedtimes);
	 if(mFingerFailedtimes >= 3)
	 {	     
             int screenoff = mPowerManager.isSleep();	     
	     if(screenoff != 1)
	    {
	          showBrouncer();
	     }
	     else
	     {	         
		  mKeyguardViewMediator.setFingerWakeup(3);
	         //wakeup
	         mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
	     }
	     mFingerFailedtimes = 0;
	 }	 
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2017-12-12-end*/
    }
    /*-prize-add by lihuangyuan,for error finger show brouncer-2018-03-05-start*/
    public void clearFingerprintFailTimes()
    {        
	 mFingerFailedtimes = 0;
	 mIsLockOutError =  false; 
	 mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
    }
    public void handleFingerprintLockoutReset()
    {
        Log.v(TAG, "handleFingerprintLockoutReset...");
        mIsLockOutError =  false;  
	 mFingerFailedtimes = 0;
    }
    private void doFingerprintErrorLockout()
    {
            mIsLockOutError = true;
	     int screenoff = mPowerManager.isSleep();	  
	     Log.v(TAG, "doFingerprintErrorLockout screenoff:"+screenoff);
	     if(screenoff == 1)
	    {
	         Log.v(TAG, "onFingerprintError, wakeup ..");
		  mKeyguardViewMediator.setFingerWakeup(3);
	         mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
	     }
	     else
	     {
	         showBrouncer();
	     }
	     mFingerFailedtimes = 0;
    }
    /*-prize-add by lihuangyuan,for error finger show brouncer-2018-03-05-end*/
    @Override
    public void onFingerprintError(int msgId, String errString) {
        cleanup();
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2018-03-05-start*/
	 Log.v(TAG, "onFingerprintError, mIsLockOutError="+mIsLockOutError+",msgId:"+msgId);
	 if(FingerprintManager.FINGERPRINT_ERROR_LOCKOUT == msgId && !mIsLockOutError && mKeyguardViewMediator.isShowing())
	 {
	     mHandler.postDelayed(new Runnable()
		 	{
		 	    public void run()
		 	    {
		 	        doFingerprintErrorLockout();
		 	    }
		 	},50);
		 	//doFingerprintErrorLockout();
	 }
	 /*-prize-add by lihuangyuan,for error finger show brouncer-2018-03-05-end*/
    }

    /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-start*/
    @Override
    public void onFingerprintHelp(int msgId, String helpString) 
    {
        synchronized (mFingerWakeupTimeout)
	 {
           int screenoff = mPowerManager.isSleep();
           if(msgId == FingerprintManager.FINGERPRINT_ERROR_VENDOR_BASE + 2)
           {
        	    if(screenoff == 1 && mKeyguardViewMediator.isFingerWakeup() != 1)//sometime 1002 comes 2times
        	    {
        	           Log.d(TAG,"onFingerprintHelp wakeup");
        	           mKeyguardViewMediator.setFingerWakeup(1);	    
        	           mPowerManager.wakeUp(SystemClock.uptimeMillis(),"android.policy:FINGER");
        		    //mStatusBarKeyguardViewManager.perfBoost();
        			
        		    //hide keyguard elary
        		    mKeyguardViewMediator.notifiyHideKeyguard();
        		    mHandler.removeCallbacks(mFingerWakeupTimeout);
        		    mHandler.postDelayed(mFingerWakeupTimeout, 600);
        	    }	
        	    else if(mKeyguardViewMediator.isFingerWakeup() != 1)//sometime 1002 comes 2times
        	    {
        	        mKeyguardViewMediator.setFingerWakeup(2);
        	    }    		
           }
        }
    }
     private final Runnable mFingerWakeupTimeout = new Runnable() 
    {
        @Override
        public void run() {
            int wakeupreason = mPowerManager.wakeupReason();
	     if(mKeyguardViewMediator.isFingerWakeup() == 1)
	     {
	         Log.d(TAG,"mFingerWakeupTimeout go to sleep");
		  mUpdateMonitor.stopFingerprintListeningState();
		  mKeyguardViewMediator.setFingerWakeup(0);
		  //mKeyguardViewMediator.handleShowKeyguard();
		  if(wakeupreason == 1)
		  {		  
		       mStatusBarKeyguardViewManager.showKeyguard();
	         	mPowerManager.goToSleep(SystemClock.uptimeMillis(),100,0);		  
		  }
		  else//wakeup again other reason
		  {
		      Log.d(TAG,"mFingerWakeupTimeout wakeup by other reason,only reset keyguard");

		      /*prize add for power key black screen  liuwei 20180412 start*/
		      mStatusBarKeyguardViewManager.showKeyguard();
		      /*prize add for power key black screen  liuwei 20180412 end*/

		      mStatusBarKeyguardViewManager.reset();
		  }
	      }
        }
    };
    /*-prize-add by lihuangyuan,for fingerprint quick unlock-2018-03-05-end*/
    private void cleanup() {
        mMode = MODE_NONE;
        releaseFingerprintWakeLock();
        mStatusBarWindowManager.setForceDozeBrightness(false);
        mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    public void startKeyguardFadingAway() {

        // Disable brightness override when the ambient contents are fully invisible.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStatusBarWindowManager.setForceDozeBrightness(false);
            }
        }, PhoneStatusBar.FADE_KEYGUARD_DURATION_PULSING);
    }

    public void finishKeyguardFadingAway() {
        mMode = MODE_NONE;
        if (mPhoneStatusBar.getNavigationBarView() != null) {
            mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        mPhoneStatusBar.notifyFpAuthModeChanged();
    }
}
