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


import java.util.ArrayList;

public class PrizeApplockChooseComplexPassword extends SettingsActivity {

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
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeChoosePasswordFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeChoosePasswordFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }   
    Class<? extends Fragment> getFragmentClass() {
        return PrizeChoosePasswordFragment.class;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof PrizeChoosePasswordFragment) {
            ((PrizeChoosePasswordFragment)fragment).onWindowFocusChanged(hasFocus);
        }
    }
    public static class PrizeChoosePasswordFragment extends InstrumentedFragment
            implements OnClickListener,TextWatcher{

        private TextView mPasswordEntry;
        private TextView mDetailsTextView;
	 private String mFirstPassword;
	 private String mSecondPassword;
	 private boolean mIsFirstInput = true;//true first,false second

        private boolean mIsAlpha;		
        private InputMethodManager mImm;
	 private Handler mHandler = new Handler();

	 private Button mContinueButton;

	 public static final int APP_LOCK_COMPLEX_PWD_MAX_LEN = 10;
	 public static final int APP_LOCK_COMPLEX_PWD_MIN_LEN = 4;
	 
	 public static final int APP_LOCK_PWD_TYPE_PIN = 1;
	 public static final int APP_LOCK_PWD_TYPE_PATTERN = 2;
	 public static final int APP_LOCK_PWD_TYPE_COMPLEX = 3;
        private int mAppLockPwdType = 0;        
        // required constructor for fragments
        public PrizeChoosePasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);	    
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mAppLockPwdType = APP_LOCK_PWD_TYPE_COMPLEX;
            View view = inflater.inflate(R.layout.prize_applock_choose_complex_password,  container, false);                 
            
            mPasswordEntry = (TextView) view.findViewById(R.id.password_entry);
            mPasswordEntry.addTextChangedListener(this);  

	     mContinueButton = (Button) view.findViewById(R.id.next_button);
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(false);

            mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
			
            mIsAlpha = mAppLockPwdType == APP_LOCK_PWD_TYPE_COMPLEX;

            mImm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            //set details message
            CharSequence detailsMessage = getString(R.string.prize_applock_ciphercomplex_header_input); 
            mDetailsTextView.setText(detailsMessage);
            
            int currentType = mPasswordEntry.getInputType();
            mPasswordEntry.setInputType(mIsAlpha ? currentType
                    : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));            
                  

            return view;
        }   
	 
	  public void onWindowFocusChanged(boolean hasFocus) {
            if (!hasFocus ) {
                return;
            }
            // Post to let window focus logic to finish to allow soft input show/hide properly.
            mPasswordEntry.post(new Runnable() {
                @Override
                public void run() {                    
                    mImm.showSoftInput(mPasswordEntry, InputMethodManager.SHOW_IMPLICIT);
                }
            });
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
			   final String pin = mPasswordEntry.getText().toString();
			   checkPassword(pin);
                    }    
                    break;    		 
            }
        }   
	 public void saveCipherPassword(String password)
	 {
	     ContentResolver mResolver= getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(PrizeAppLockCipherMetaData.CIPHER_1, password);
	     values.put(PrizeAppLockCipherMetaData.CIPHER_TYPE, PrizeAppLockCipherMetaData.CIPHER_TYPE_COMPLEX);
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
    	 public void checkPassword(String password)
    	 {
    	     if(getActivity() == null || getActivity().getContentResolver() == null)return;
	     if(mIsFirstInput)
	     {
	     	   mIsFirstInput = false;
		   mFirstPassword = mPasswordEntry.getText().toString();
	          mPasswordEntry.setText("");
		   mContinueButton.setEnabled(false);
		   mDetailsTextView.setText(getString(R.string.prize_applock_ciphersimple_header_again));
		   mContinueButton.setText(getString(R.string.prize_applock_ciphersimple_complete));
	     }
	     else
	     {
	     	   String curpassword = mPasswordEntry.getText().toString();
		   if(!mFirstPassword.equals(curpassword))
		   {
		   	mDetailsTextView.setText(getString(R.string.prize_applock_ciphersimple_header_error));
			mPasswordEntry.setText("");
		       mContinueButton.setEnabled(false);
			return;
		   }
		   saveCipherPassword(mFirstPassword);

		   //finish
            	   getActivity().finish();
	     }
    	 }
	
	 public void onTextChanged(CharSequence s, int start, int before, int count){}
	 public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void afterTextChanged(Editable s) {            
            	if (mAppLockPwdType == APP_LOCK_PWD_TYPE_COMPLEX) 
		{
		       if(mPasswordEntry.getText().length() == 1)
		       {
		       	if(mIsFirstInput)
		       	{
		       		mDetailsTextView.setText(getString(R.string.prize_applock_ciphercomplex_header_input));
		       	}
				else
				{
		                     mDetailsTextView.setText(getString(R.string.prize_applock_ciphersimple_header_again));
				}				
		       }
            		if (mPasswordEntry.getText().length() >= APP_LOCK_COMPLEX_PWD_MIN_LEN) 
			{
				mContinueButton.setEnabled(true);            			
            		}
			else
			{
				mContinueButton.setEnabled(false);
            	}     }     
        }
        
    }
}
