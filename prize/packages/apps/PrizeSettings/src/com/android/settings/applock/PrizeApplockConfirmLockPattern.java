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

package com.android.settings.applock;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.SettingsActivity;
import com.android.settings.R;
import com.android.settings.InstrumentedFragment;
import com.android.settings.Utils;
import android.os.Handler;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.ContentValues;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import static com.android.internal.widget.LockPatternView.DisplayMode;

import java.util.ArrayList;
import java.util.List;

public class PrizeApplockConfirmLockPattern extends SettingsActivity {

    final static String TAG = "PrizeAppLock";
    
    @Override
    protected void onCreate(Bundle savedState) {
    	 //setTheme(R.style.Theme_ConfirmDeviceCredentialsDark);
        super.onCreate(savedState);
	 setTitle(getString(R.string.prize_applock_manager_ciphersettings));
	 getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeApplockConfirmLockPatternFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeApplockConfirmLockPatternFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }   
    Class<? extends Fragment> getFragmentClass() {
        return PrizeApplockConfirmLockPatternFragment.class;
    }

    public static class PrizeApplockConfirmLockPatternFragment extends InstrumentedFragment 
    {

        private LockPatternView mLockPatternView;
        private TextView mHeaderTextView;
        private TextView mDetailsTextView;
	 protected TextView mErrorTextView;		 
 
	 private Handler mHandler = new Handler();

	 // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;	 
	
        private int mAppLockPwdType = -1; 
	 private String mSavedPassword;
        // required constructor for fragments
        public PrizeApplockConfirmLockPatternFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
	     Intent intent = getActivity().getIntent();
	     mAppLockPwdType = intent.getIntExtra("passwordtype",-1);
	     Log.e(TAG,"onCreate mAppLockPwdType :"+mAppLockPwdType);
	     if(mAppLockPwdType != PrizeAppLockCipherMetaData.CIPHER_TYPE_PATTERN)
	     {
	        //Log.e(TAG,"mAppLockPwdType :"+mAppLockPwdType);
	     	  getActivity().finish();
	     }
            mSavedPassword = getSavedPassword();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.prize_applock_confirm_lock_pattern,  container, false);                 
            
            mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern); 
	     mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
	  
	     mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);	   
	     mErrorTextView = (TextView) view.findViewById(R.id.errorText);	   
            //set details message
            CharSequence detailsMessage = getString(R.string.prize_applock_pattern_conform_detail); 
            mDetailsTextView.setText(detailsMessage);
	     mHeaderTextView.setText(getString(R.string.prize_applock_pattern_conform_header));
           

            return view;
        }   	 

        @Override
        public void onPause() {
            super.onPause();            
        }
        @Override
        public void onResume() {
            super.onResume();            
        } 
        @Override
        protected int getMetricsCategory() {
            return MetricsEvent.CONFIRM_LOCK_PASSWORD;
        }         
	 public String getSavedPassword()
	 {
	     ContentResolver resolverr = getActivity().getContentResolver();
    	     String where = PrizeAppLockCipherMetaData.CIPHER_STATUS + " =?";
            String[] selectionArgs = new String[]{""+PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID};//effective.
            String[] selectioncolums = new String[]{PrizeAppLockCipherMetaData.CIPHER_1};
            Cursor mCursor = resolverr.query(PrizeAppLockCipherMetaData.CONTENT_URI, selectioncolums, where, selectionArgs, null);
	     String strPassword = null;
            if (null != mCursor && mCursor.getCount() > 0) 
	     {
	     	   mCursor.moveToNext();
		   strPassword = mCursor.getString(0);
            }
	     if(mCursor != null)
	     {
	     	mCursor.close();
	     }
	     return strPassword;
	 }	 
	 public void startAppLockManagerSetting() 
	 {	     
	            Intent intent = new Intent();
	            intent.setClassName("com.android.settings",
	                    PrizeAppLockManagerActivity.class.getName());
	            startActivity(intent);
	 }
	 private Runnable mClearPatternRunnable = new Runnable() 
	 {
            public void run() 
	     {
                mLockPatternView.clearPattern();
		  mErrorTextView.setText("");
		  mLockPatternView.setDisplayMode(DisplayMode.Correct);
            }
        };	 
        private void postClearPatternRunnable() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }

	 protected LockPatternView.OnPatternListener mChooseNewLockPatternListener =
                new LockPatternView.OnPatternListener() {

                public void onPatternStart() {
                    mLockPatternView.removeCallbacks(mClearPatternRunnable);
                }

                public void onPatternCleared() {
                    mLockPatternView.removeCallbacks(mClearPatternRunnable);
                }
                @Override
                public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                    if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE)
		      {
                           mErrorTextView.setText(getString(R.string.lockpattern_need_to_unlock_wrong));
			       mLockPatternView.enableInput();
				mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			       postClearPatternRunnable();
                    } 
		      else 
		      {
		      		String strpattern = LockPatternUtils.patternToString(pattern);
				if(mSavedPassword.equals(strpattern))
				{
					startAppLockManagerSetting();
					getActivity().finish();
				}
				else
				{
					mErrorTextView.setText(getString(R.string.lockpattern_need_to_unlock_wrong));
			       	mLockPatternView.enableInput();
					mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			       	postClearPatternRunnable();
				}
                    }
                }
                @Override
                public void onPatternCellAdded(List<Cell> pattern) {

                }
               
         };
        
    }
}
