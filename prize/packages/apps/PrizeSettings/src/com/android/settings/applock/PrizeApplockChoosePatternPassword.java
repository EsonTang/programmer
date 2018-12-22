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

public class PrizeApplockChoosePatternPassword extends SettingsActivity {

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
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeChoosePatternFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeChoosePatternFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }   
    Class<? extends Fragment> getFragmentClass() {
        return PrizeChoosePatternFragment.class;
    }

    public static class PrizeChoosePatternFragment extends InstrumentedFragment implements View.OnClickListener
    {

        private LockPatternView mLockPatternView;
        private TextView mDetailsTextView;
	 private String mFirstPassword;
	 private String mSecondPassword;
	 private int mInputStatus = INPUT_FIRST;//true first,false second
	 public static final int INPUT_FIRST = 0;
	 public static final int INPUT_FIRST_COMPLETE = 1;
	 public static final int INPUT_SECOND = 2;
	 public static final int INPUT_SECOND_COMPLETE = 3;	 
 
	 private Handler mHandler = new Handler();

	 private Button mContinueButton;

	 // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;	 
	 
        private int mAppLockPwdType = 0;        
        // required constructor for fragments
        public PrizeChoosePatternFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);	    
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {            
            View view = inflater.inflate(R.layout.prize_applock_choose_pattern_password,  container, false);                 
            
            mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern); 
	     mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);

	     mContinueButton = (Button) view.findViewById(R.id.next_button);
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(false);

            mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);	

          
            //set details message
            CharSequence detailsMessage = getString(R.string.prize_applock_pattern_header_input); 
            mDetailsTextView.setText(detailsMessage);
           

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
        
        public void onClick(View v) 
	 {
            switch (v.getId()) 
	     {
                case R.id.next_button:
		      {
			   if(mInputStatus == INPUT_FIRST_COMPLETE)
			   {
			   	mInputStatus = INPUT_SECOND;
			   	mLockPatternView.enableInput();
			   	mClearPatternRunnable.run();
			   }
			   if(mInputStatus == INPUT_SECOND_COMPLETE)
			   {
			   	saveCipherPassword(mFirstPassword);
			   	//finish
	            	   	getActivity().finish();
			   }			   
                    }    
                    break;    		 
            }
        }   
	 public void saveCipherPassword(String password)
	 {
	     ContentResolver mResolver= getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(PrizeAppLockCipherMetaData.CIPHER_1, password);
	     values.put(PrizeAppLockCipherMetaData.CIPHER_TYPE, PrizeAppLockCipherMetaData.CIPHER_TYPE_PATTERN);
	     values.put(PrizeAppLockCipherMetaData.CIPHER_STATUS,PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID);
			
	     Log.d(TAG,"toSaveCipher "+password);
            String where = PrizeAppLockCipherMetaData.CIPHER_STATUS + " =?";
            String[] selectionArgs = new String[]{""+PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID};//effective.
            Cursor mCursor = mResolver.query(PrizeAppLockCipherMetaData.CONTENT_URI, null, where, selectionArgs, null);
            if (null != mCursor && mCursor.getCount() > 0) 
	     {
                mResolver.update(PrizeAppLockCipherMetaData.CONTENT_URI, values, "", null);
            } 
	     else 
	     {
                mResolver.insert(PrizeAppLockCipherMetaData.CONTENT_URI, values);
            }

            //open the app lock function.            
            updateDBAppLockData(PrizeFpFuntionMetaData.APP_LOCK_FUNCTION_OPEN, PrizeFpFuntionMetaData.APP_LOCK_FC);
            
            if(mCursor != null){
            	mCursor.close();
            }           
            
	 }
	 //set the function column to 1
        public void updateDBAppLockData(int functionStatus, String keyName) {
            ContentResolver mResolver= getActivity().getContentResolver();
	     //the table created with initial record --default value 0
            ContentValues values = new ContentValues();
            values.put(PrizeFpFuntionMetaData.FUNCTION_STATUS, functionStatus);
            String where = PrizeFpFuntionMetaData.FUNCTION_NAME + " =?";
            String[] selectionArgs = new String[]{keyName};

            mResolver.update(PrizeFpFuntionMetaData.CONTENT_URI, values, where, selectionArgs);
        }    	 	
	 private Runnable mClearPatternRunnable = new Runnable() 
	 {
            public void run() 
	     {
			 if (!isAdded()) return;
                mLockPatternView.clearPattern();
		  if(mInputStatus == INPUT_FIRST)
		  {
            		mDetailsTextView.setText(getString(R.string.prize_applock_pattern_header_input));
			mContinueButton.setText(getString(R.string.prize_applock_ciphersimple_continue));
		  }
		  else if(mInputStatus == INPUT_SECOND)
		  {
		  	mDetailsTextView.setText(getString(R.string.prize_applock_pattern_need_confirm));
			mContinueButton.setText(getString(R.string.prize_applock_ciphersimple_complete));
		  }
		  mContinueButton.setEnabled(false);
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
                    patternInProgress();
                }

                public void onPatternCleared() {
                    mLockPatternView.removeCallbacks(mClearPatternRunnable);
                }
                @Override
                public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                    if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE)
		      {
                           mDetailsTextView.setText(getString(R.string.lockpattern_recording_incorrect_too_short,LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
			       mLockPatternView.enableInput();
				mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			       postClearPatternRunnable();
                    } 
		      else 
		      {
		      		//save first input password
		      	       if(mInputStatus == INPUT_FIRST)
		      	       {
		      	       	String strpattern = LockPatternUtils.patternToString(pattern);
					mFirstPassword = strpattern;
					//set recorded status,press next button					
					mDetailsTextView.setText(getString(R.string.lockpattern_pattern_entered_header));
					mContinueButton.setEnabled(true);
					mLockPatternView.disableInput();
					mLockPatternView.setDisplayMode(DisplayMode.Correct);
					mInputStatus = INPUT_FIRST_COMPLETE;
		      	       }
				else if(mInputStatus == INPUT_SECOND)//check second password with first password
				{
					String strpattern = LockPatternUtils.patternToString(pattern);
					if(!mFirstPassword.equals(strpattern))
					{
						mDetailsTextView.setText(getString(R.string.prize_applock_pattern_conform_err));
						mLockPatternView.enableInput();
						mLockPatternView.setDisplayMode(DisplayMode.Wrong);
						postClearPatternRunnable();
					}
					else
					{
						//set recorded status,press next button					
						mDetailsTextView.setText(getString(R.string.lockpattern_pattern_entered_header));
						mContinueButton.setEnabled(true);
						mLockPatternView.disableInput();
						mLockPatternView.setDisplayMode(DisplayMode.Correct);
						mInputStatus = INPUT_SECOND_COMPLETE;
					}
		      	       }
                    }
                }
                @Override
                public void onPatternCellAdded(List<Cell> pattern) {

                }

                private void patternInProgress() {
                    mDetailsTextView.setText(R.string.lockpattern_recording_inprogress);
                    mContinueButton.setEnabled(false);
                }
         };
        
    }
}
