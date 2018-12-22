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

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;

public class PrizeApplockConfirmLockPassword extends SettingsActivity {

    final static String TAG = "PrizeAppLock";
    
    @Override
    protected void onCreate(Bundle savedState) {
    	 //setTheme(R.style.Theme_ConfirmDeviceCredentialsDark);
        super.onCreate(savedState);
	 setTitle(getString(R.string.application_lock));
	 getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeConfirmLockPasswordFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeConfirmLockPasswordFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }   
    Class<? extends Fragment> getFragmentClass() {
        return PrizeConfirmLockPasswordFragment.class;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof PrizeConfirmLockPasswordFragment) {
            ((PrizeConfirmLockPasswordFragment)fragment).onWindowFocusChanged(hasFocus);
        }
    }
    public static class PrizeConfirmLockPasswordFragment extends InstrumentedFragment
            implements OnClickListener,TextWatcher,OnEditorActionListener{

        private TextView mPasswordEntry;
        private TextView mHeaderTextView;
        private TextView mDetailsTextView;
	 protected TextView mErrorTextView;
        private boolean mIsAlpha;		
        private InputMethodManager mImm;
	 private Handler mHandler = new Handler();

	 private Button mContinueButton;
	 
        private int mAppLockPwdType = -1;   
	 private String mSavedPassword;
        // required constructor for fragments
        public PrizeConfirmLockPasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);	    
	     Intent intent = getActivity().getIntent();
	     mAppLockPwdType = intent.getIntExtra("passwordtype",-1);
	     Log.e(TAG,"onCreate mAppLockPwdType :"+mAppLockPwdType);
	     if(mAppLockPwdType != PrizeAppLockCipherMetaData.CIPHER_TYPE_COMPLEX
		  && mAppLockPwdType != PrizeAppLockCipherMetaData.CIPHER_TYPE_NUM)
	     {
	        //Log.e(TAG,"mAppLockPwdType :"+mAppLockPwdType);
	     	  getActivity().finish();
	     }
            mSavedPassword = getSavedPassword();		
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.prize_applock_confirm_lock_password,  container, false);                 
            
            mPasswordEntry = (TextView) view.findViewById(R.id.password_entry);
	     mPasswordEntry.setOnEditorActionListener(this);
            mPasswordEntry.addTextChangedListener(this);  

	     view.findViewById(R.id.cancel_button).setOnClickListener(this);
	     mContinueButton = (Button) view.findViewById(R.id.next_button);
            mContinueButton.setOnClickListener(this);
            mContinueButton.setEnabled(false);

            mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
            mErrorTextView = (TextView) view.findViewById(R.id.errorText);
			
            mIsAlpha = mAppLockPwdType == PrizeAppLockCipherMetaData.CIPHER_TYPE_COMPLEX;

            mImm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            //set header/details message
            CharSequence headerMessage = getString(R.string.lockpassword_confirm_your_password_header);
            CharSequence detailsMessage = getString(R.string.prize_applock_manager_inputpwd);           
            mHeaderTextView.setText(headerMessage);
            mDetailsTextView.setText(detailsMessage);
            
            int currentType = mPasswordEntry.getInputType();
            mPasswordEntry.setInputType(mIsAlpha ? currentType
                    : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));            
                  

            return view;
        }   
	 private int getErrorMessage() {
            return mIsAlpha ? R.string.lockpassword_invalid_password
                    : R.string.prize_applock_manager_inputpwd_pin_error;
        }
	  private final Runnable mResetErrorRunnable = new Runnable() {
	        @Override
	        public void run() {
	            mErrorTextView.setText("");
	        }
	    };

	    protected void showError(CharSequence msg, long timeout) {
	        mErrorTextView.setText(msg);
	        mPasswordEntry.setText(null);
	        mHandler.removeCallbacks(mResetErrorRunnable);
	        if (timeout != 0) {
	            mHandler.postDelayed(mResetErrorRunnable, timeout);
	        }
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
			   checkPassword(pin,true);
                    }    
                    break;    
		  case R.id.cancel_button:
                    getActivity().setResult(RESULT_CANCELED);
                    getActivity().finish();
                    break;
            }
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
    	 public void checkPassword(String password,boolean isShowError)
    	 {
    	     if(getActivity() == null || getActivity().getContentResolver() == null)return;
			 
    	     
	     Log.d(TAG,"checkPassword password:"+password+",saved:"+mSavedPassword);
	     if(password.equals(mSavedPassword))
	     {
	     	   startAppLockManagerSetting();
		   getActivity().finish();
	     }
	     else
	     {
    	 	    if(isShowError)
			showError(getString(getErrorMessage()),2000);
	     }
    	 }
	 public void startAppLockManagerSetting() 
	 {	     
	            Intent intent = new Intent();
	            intent.setClassName("com.android.settings",
	                    PrizeAppLockManagerActivity.class.getName());
	            startActivity(intent);
	 }
	 public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
	 {
            // Check if this was the result of hitting the enter or "done" key
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) 
           {
                checkPassword(mPasswordEntry.getText().toString(),true);
                return true;
            }
            return false;
        }
	 public void onTextChanged(CharSequence s, int start, int before, int count){}
	 public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void afterTextChanged(Editable s) {            
            	if (mAppLockPwdType == PrizeAppLockCipherMetaData.CIPHER_TYPE_NUM) 
		{
            		if (mPasswordEntry.getText().length() == 4) 
			{
            			final String pin = mPasswordEntry.getText().toString();
			   	checkPassword(pin,true);
            		}
            	}  
		else
		{
			if (mPasswordEntry.getText().length() >= 4) 
			{
				mContinueButton.setEnabled(true);
				final String strpassword = mPasswordEntry.getText().toString();
				checkPassword(strpassword,false);
			}
		}
        }
        
    }
}
