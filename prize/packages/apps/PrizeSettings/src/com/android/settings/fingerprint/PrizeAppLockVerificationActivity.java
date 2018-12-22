/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.fingerprint;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ConfirmDeviceCredentialBaseActivity;
import com.android.settings.Utils;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.app.ActivityManagerNative;

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;

import com.android.settings.R;

public class PrizeAppLockVerificationActivity extends ConfirmDeviceCredentialBaseActivity 
implements PrizeFpOperationInterface {
    final static String TAG = "PrizeAppLockVerificationActivity";
    
    private LockPatternUtils mLockPatternUtils;
    private int mEffectiveUserId;
    private int mStoredQuality;
    
    private int mDeviceLockMode = 0;

	private ActionBar mActionBar ;
	
	public static final String PRIZE_FP_PKGNAME = "prize_fingerprint_pkgname";
	
	public static final String PRIZE_FP_CLASSNAME = "prize_fingerprint_classname";
	
	/** Fingerprint state: Not listening to fingerprint. */
    private static final int FINGERPRINT_STATE_STOPPED = 0;
	
	/** Fingerprint state: Listening. */
    private static final int FINGERPRINT_STATE_RUNNING = 1;

    /**
     * Fingerprint state: Cancelling and waiting for the confirmation from FingerprintService to
     * send us the confirmation that cancellation has happened.
     */
    private static final int FINGERPRINT_STATE_CANCELLING = 2;

    /**
     * Fingerprint state: During cancelling we got another request to start listening, so when we
     * receive the cancellation done signal, we should start listening again.
     */
    private static final int FINGERPRINT_STATE_CANCELLING_RESTARTING = 3;
    
    private int mFingerprintRunningState = FINGERPRINT_STATE_STOPPED;
	
	private FingerprintManager mFpm;
	private CancellationSignal mFingerprintCancelSignal;
    
    private static final int PASSWORD_MODE = 101;
    private static final int PATTERN_MODE = 102;
    
    private String mPkgName;
	private String mClassName;
    
    private final FingerprintManager.LockoutResetCallback mLockoutResetCallback
            = new FingerprintManager.LockoutResetCallback() {
        @Override
        public void onLockoutReset() {
        	Log.d("Fp_Auth", "onLockoutReset(), mFingerprintRunningState = "+mFingerprintRunningState);
        	updateFingerprintListeningState();
        }
    };

    private FingerprintManager.AuthenticationCallback mAuthenticationCallback
            = new AuthenticationCallback() {
        @Override
        public void onAuthenticationFailed() {
            Log.d("Fp_Auth", "onAuthenticationFailed(), mFingerprintRunningState"+mFingerprintRunningState);
            updateFingerprintListeningState();
            mFingerprintRunningState = FINGERPRINT_STATE_STOPPED;
            updateFingerprintListeningState();
        };

        @Override
        public void onAuthenticationSucceeded(AuthenticationResult result) {
        	Log.d("Fp_Auth", "onAuthenticationSucceeded(), mFingerprintRunningState = "+mFingerprintRunningState);
        	updateFingerprintListeningState();
        	startVerificApp();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        	Log.d("Fp_Auth", "onAuthenticationHelp(), mFingerprintRunningState = "+mFingerprintRunningState);
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        	Log.d("Fp_Auth", "onAuthenticationError(), mFingerprintRunningState = "+mFingerprintRunningState);
        }

        @Override
        public void onAuthenticationAcquired(int acquireInfo) {
        	Log.d("Fp_Auth", "onAuthenticationAcquired(), mFingerprintRunningState"+mFingerprintRunningState);
        }
    };

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        switch (mDeviceLockMode) {
		case PASSWORD_MODE:
			modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizePasswordUnlockFragment.class.getName());
			break;
		case PATTERN_MODE:
			modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizePatternUnlockFragment.class.getName());
			break;
		default:
			break;
		}
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
    	boolean isValidFragment = false;
    	switch (mDeviceLockMode) {
		case PASSWORD_MODE:
			if (PrizePasswordUnlockFragment.class.getName().equals(fragmentName)){
				isValidFragment = true;
			}
			break;
		case PATTERN_MODE:
			if (PrizePatternUnlockFragment.class.getName().equals(fragmentName)){
				isValidFragment = true;
			}
			break;
		default:
			isValidFragment = false;
			break;
		}
        return isValidFragment;
    }
    
    @Override
    protected void onCreate(Bundle savedState) {
    	mLockPatternUtils = new LockPatternUtils(this);
        mEffectiveUserId = Utils.getCredentialOwnerUserId(this);
        mStoredQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality(mEffectiveUserId);
        
        switch (mStoredQuality) {
		case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:  // Pattern
			mDeviceLockMode = PATTERN_MODE;
			break;
		case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:  // Simple Password
		case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
		case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:  // Complex Password
		case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
		case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
	        	mDeviceLockMode = PASSWORD_MODE;
			break;
		}
        super.onCreate(savedState);
        
        CharSequence msg = getText(R.string.application_lock);
        setTitle(msg);
        
        mFpm = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        
        Intent intent = getIntent();
		if (intent != null) {
			mPkgName = intent.getStringExtra(PrizeAppLockVerificationActivity.PRIZE_FP_PKGNAME);
			mClassName = intent.getStringExtra(PrizeAppLockVerificationActivity.PRIZE_FP_CLASSNAME);
		}
        
//        ContentResolver mResolver= getContentResolver();
//    	ContentValues values = new ContentValues();
//    	String where = PrizeFpFuntionMetaData.FUNCTION_NAME + " =?" + " OR "+PrizeFpFuntionMetaData.FUNCTION_NAME + " =?";
//    	String[] selectionArgs = new String[]{PrizeFpFuntionMetaData.FP_LOCK_SCREEN_FC,
//    			PrizeFpFuntionMetaData.APP_LOCK_FC};
//    	Cursor mCursor = mResolver.query(PrizeFpFuntionMetaData.CONTENT_URI, null, where, selectionArgs,null);
//    	boolean isLockScreenChecked = false;
//    	boolean isAppLockChecked = false;
//    	if(mCursor != null && mCursor.getCount() > 0){
//    		String mFunctName = null;
//    		int mFunctStatus = 0;
//    		while (mCursor.moveToNext()) {  
//    			mFunctName = mCursor.getString(mCursor.getColumnIndex(PrizeFpFuntionMetaData.FUNCTION_NAME));
//    			mFunctStatus = mCursor.getInt(mCursor.getColumnIndex(PrizeFpFuntionMetaData.FUNCTION_STATUS));
//    			if(PrizeFpFuntionMetaData.FP_LOCK_SCREEN_FC.equals(mFunctName) && mFunctStatus == 1){
//    				isLockScreenChecked = true;
//    			}else if(PrizeFpFuntionMetaData.FP_LOCK_SCREEN_FC.equals(mFunctName) && mFunctStatus == 0){
//    				isLockScreenChecked = false;
//    			}
//            } 
//    	}
//    	
//    	mCursor = mResolver.query(PrizeFpFuntionMetaData.CONTENT_URI, null, where, selectionArgs,null);
//    	if(mCursor != null && mCursor.getCount() > 0){
//    		String mFunctName = null;
//    		int mFunctStatus = 0;
//    		while (mCursor.moveToNext()) {  
//    			mFunctName = mCursor.getString(mCursor.getColumnIndex(PrizeFpFuntionMetaData.FUNCTION_NAME));
//    			mFunctStatus = mCursor.getInt(mCursor.getColumnIndex(PrizeFpFuntionMetaData.FUNCTION_STATUS));
//    			if(isLockScreenChecked && PrizeFpFuntionMetaData.APP_LOCK_FC.equals(mFunctName) && mFunctStatus == 1){
//    				isAppLockChecked = true;
//    			} else {
//    				isAppLockChecked = false;
//    			}
//            } 
//    	}
//    	
//    	if(mCursor != null){
//    		mCursor.close();
//    	}
//    	
//    	if(!isLockScreenChecked || !isAppLockChecked){
//    		finish();
//    	}
    	
    }
    
    @Override
    public void onPause() {
    	mFingerprintRunningState = FINGERPRINT_STATE_RUNNING;
    	updateFingerprintListeningState();
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	if (mFpm != null) {
            mFpm.addLockoutResetCallback(mLockoutResetCallback);
        }
    	mFingerprintRunningState = FINGERPRINT_STATE_STOPPED;
    	updateFingerprintListeningState();
    	super.onResume();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof PrizePasswordUnlockFragment) {
            ((PrizePasswordUnlockFragment)fragment).onWindowFocusChanged(hasFocus);
        }
    }
    
    private void updateFingerprintListeningState() {
    	Log.d("Fp_Auth", "updateFingerprintListeningState(), mFingerprintRunningState = "+mFingerprintRunningState);
        if (mFingerprintRunningState == FINGERPRINT_STATE_RUNNING) {
            stopListeningForFingerprint();
        } else if (mFingerprintRunningState != FINGERPRINT_STATE_RUNNING) {
            startListeningForFingerprint();
        }
    }

    private void startListeningForFingerprint() {
    	Log.d("Fp_Auth", "startListeningForFingerprint(), mFingerprintRunningState = "+mFingerprintRunningState);
        if (mFingerprintRunningState == FINGERPRINT_STATE_CANCELLING) {
            setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING_RESTARTING);
            return;
        }
        int userId = ActivityManager.getCurrentUser();
        if (isUnlockWithFingerprintPossible(userId)) {
            if (mFingerprintCancelSignal != null) {
                mFingerprintCancelSignal.cancel();
            }
            mFingerprintCancelSignal = new CancellationSignal();
            mFpm.authenticate(null, mFingerprintCancelSignal, 0, mAuthenticationCallback, null, userId);
            setFingerprintRunningState(FINGERPRINT_STATE_RUNNING);
        }
    }

    public boolean isUnlockWithFingerprintPossible(int userId) {
        return mFpm != null && mFpm.isHardwareDetected() && mFpm.getEnrolledFingerprints(userId).size() > 0;
    }
    
    private void setFingerprintRunningState(int fingerprintRunningState) {
    	Log.d("Fp_Auth", "setFingerprintRunningState(), fingerprintRunningState = "+fingerprintRunningState);
        boolean wasRunning = mFingerprintRunningState == FINGERPRINT_STATE_RUNNING;
        boolean isRunning = fingerprintRunningState == FINGERPRINT_STATE_RUNNING;
        mFingerprintRunningState = fingerprintRunningState;

    }
    
    private void stopListeningForFingerprint() {
    	Log.d("Fp_Auth", "stopListeningForFingerprint(), mFingerprintRunningState = "+mFingerprintRunningState);
        if (mFingerprintRunningState == FINGERPRINT_STATE_RUNNING) {
        	if(mFingerprintCancelSignal != null){
        		mFingerprintCancelSignal.cancel();
                mFingerprintCancelSignal = null;
        	}
            setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING);
        }
        if (mFingerprintRunningState == FINGERPRINT_STATE_CANCELLING_RESTARTING) {
            setFingerprintRunningState(FINGERPRINT_STATE_CANCELLING);
        }
    }

	@Override
	public void startVerificApp() {
		try {
			Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
	        if (fragment != null && fragment instanceof PrizePasswordUnlockFragment) {
	        	((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
	        }
			
			Intent intent = new Intent();
			ComponentName mComponentName=new ComponentName(mPkgName,mClassName);    
			intent.setComponent(mComponentName);
			startActivity(intent);
		} catch (Exception e) {
			Log.d(TAG, "Start_Verific_App Error: "+e.getMessage());
		} finally {
			finish();
		}
	}
    
}
