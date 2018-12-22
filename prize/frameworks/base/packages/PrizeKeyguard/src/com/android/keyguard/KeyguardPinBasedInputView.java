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

package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.os.SystemProperties;

/**
 * A Pin based Keyguard input view
 */
public abstract class KeyguardPinBasedInputView extends KeyguardAbsKeyInputView
        implements View.OnKeyListener, View.OnTouchListener {

    private static final String TAG = "KeyguardPinBasedInputView" ;
    private static final boolean DEBUG = KeyguardConstants.DEBUG;

    protected PasswordTextView mPasswordEntry;
    private View mOkButton;
    private View mDeleteButton;
    private View mButton0;
    private View mButton1;
    private View mButton2;
    private View mButton3;
    private View mButton4;
    private View mButton5;
    private View mButton6;
    private View mButton7;
    private View mButton8;
    private View mButton9;
    
    private ImageView pw1,pw2,pw3,pw4;

    public KeyguardPinBasedInputView(Context context) {
        this(context, null);
    }

    public KeyguardPinBasedInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void reset() {
        mPasswordEntry.requestFocus();
        super.reset();
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // send focus to the password field
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void resetState() {
        setPasswordEntryEnabled(true);
	 /*prize-add by lihuangyuan,for disable input when try too many times-2017-09-04-start*/
	 mOkButton.setEnabled(true);
	 mDeleteButton.setEnabled(true);
	 mButton0.setEnabled(true);
	 mButton1.setEnabled(true);
	 mButton2.setEnabled(true);
	 mButton3.setEnabled(true);
	 mButton4.setEnabled(true);
	 mButton5.setEnabled(true);
	 mButton6.setEnabled(true);
	 mButton7.setEnabled(true);
	 mButton8.setEnabled(true);
	 mButton9.setEnabled(true);
	 /*prize-add by lihuangyuan,for disable input when try too many times-2017-09-04-end*/
    }

    @Override
    protected void setPasswordEntryEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
        mOkButton.setEnabled(enabled);
    }

    @Override
    protected void setPasswordEntryInputEnabled(boolean enabled) {
        mPasswordEntry.setEnabled(enabled);
        /*prize-add by lihuangyuan,for disable input when try too many times-2017-09-04-start*/
	 mOkButton.setEnabled(enabled);
	 mDeleteButton.setEnabled(enabled);
	 mButton0.setEnabled(enabled);
	 mButton1.setEnabled(enabled);
	 mButton2.setEnabled(enabled);
	 mButton3.setEnabled(enabled);
	 mButton4.setEnabled(enabled);
	 mButton5.setEnabled(enabled);
	 mButton6.setEnabled(enabled);
	 mButton7.setEnabled(enabled);
	 mButton8.setEnabled(enabled);
	 mButton9.setEnabled(enabled);
	 /*prize-add by lihuangyuan,for disable input when try too many times-2017-09-04-end*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            performClick(mOkButton);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            performClick(mDeleteButton);
            return true;
        }
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            int number = keyCode - KeyEvent.KEYCODE_0;
            performNumberClick(number);
            return true;
        }
        if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            int number = keyCode - KeyEvent.KEYCODE_NUMPAD_0;
            performNumberClick(number);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getPromtReasonStringRes(int reason) {
        switch (reason) {
            case PROMPT_REASON_RESTART:
		   //modify by lihuangyuan
                //return R.string.kg_prompt_reason_restart_pin;
                return R.string.kg_password_instructions;
            case PROMPT_REASON_TIMEOUT:
                return R.string.kg_prompt_reason_timeout_pin;
            case PROMPT_REASON_DEVICE_ADMIN:
                return R.string.kg_prompt_reason_device_admin;
            case PROMPT_REASON_USER_REQUEST:
                return R.string.kg_prompt_reason_user_request;
            case PROMPT_REASON_NONE:
                return 0;
            default:
                return R.string.kg_prompt_reason_timeout_pin;
        }
    }

    private void performClick(View view) {
        view.performClick();
    }

    private void performNumberClick(int number) {
        switch (number) {
            case 0:
                performClick(mButton0);
                break;
            case 1:
                performClick(mButton1);
                break;
            case 2:
                performClick(mButton2);
                break;
            case 3:
                performClick(mButton3);
                break;
            case 4:
                performClick(mButton4);
                break;
            case 5:
                performClick(mButton5);
                break;
            case 6:
                performClick(mButton6);
                break;
            case 7:
                performClick(mButton7);
                break;
            case 8:
                performClick(mButton8);
                break;
            case 9:
                performClick(mButton9);
                break;
        }
    }

    @Override
    protected void resetPasswordText(boolean animate, boolean announce) {
        mPasswordEntry.reset(animate, announce);
        setPwStatus();
    }

    @Override
    protected String getPasswordText() {
        return mPasswordEntry.getText();
    }

    /*PRIZE-PIN commit password-liufan-2015-07-21-start*/
    public void commit(){
        if (mPasswordEntry.isEnabled()) {
            setPasswordEntryEnabled(false);
            verifyPasswordAndUnlock();
            
        }
    }
    /*PRIZE-PIN commit password-liufan-2015-07-21-end*/

    @Override
    protected void onFinishInflate() {
        mPasswordEntry = (PasswordTextView) findViewById(getPasswordTextViewId());
        mPasswordEntry.setOnKeyListener(this);
        /*PRIZE-set this obj-liufan-2015-07-22-start*/ 
        boolean isFpUnlock = SystemProperties.get("persist.sys.prize_fp_enable").equals("1");
        isFpUnlock = true;
        if(isFpUnlock){
            mPasswordEntry.setKeyguardPinBasedInputView(this);
        }
        /*PRIZE-set this obj-liufan-2015-07-22-end*/ 

        // Set selected property on so the view can send accessibility events.
        mPasswordEntry.setSelected(true);

        mPasswordEntry.setUserActivityListener(new PasswordTextView.UserActivityListener() {
            @Override
            public void onUserActivity() {
                onUserInput();
            }
        });

        mOkButton = findViewById(R.id.key_enter);
        /*PRIZE-hide commit button-liufan-2015-07-21-start*/
        if(isFpUnlock){
            decideShowOKButton();
        }
        /*PRIZE-hide commit button-liufan-2015-07-21-end*/
        if (mOkButton != null) {
            mOkButton.setOnTouchListener(this);
            mOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            mOkButton.setOnHoverListener(new LiftToActivateListener(getContext()));
        }

        mDeleteButton = findViewById(R.id.delete_button);
        mDeleteButton.setVisibility(View.VISIBLE);
        mDeleteButton.setOnTouchListener(this);
        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for time-based lockouts
                if (mPasswordEntry.isEnabled()) {
                    mPasswordEntry.deleteLastChar();
                    setPwStatus();
                }
                doHapticKeyClick();
            }
        });
        mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // check for time-based lockouts
                if (mPasswordEntry.isEnabled()) {
                    resetPasswordText(true /* animate */, true /* announce */);
                }
                doHapticKeyClick();
                return true;
            }
        });

        pw1 = (ImageView)findViewById(R.id.pw1);
        pw2 = (ImageView)findViewById(R.id.pw2);
        pw3 = (ImageView)findViewById(R.id.pw3);
        pw4 = (ImageView)findViewById(R.id.pw4);
        
        mButton0 = findViewById(R.id.key0);
        mButton1 = findViewById(R.id.key1);
        mButton2 = findViewById(R.id.key2);
        mButton3 = findViewById(R.id.key3);
        mButton4 = findViewById(R.id.key4);
        mButton5 = findViewById(R.id.key5);
        mButton6 = findViewById(R.id.key6);
        mButton7 = findViewById(R.id.key7);
        mButton8 = findViewById(R.id.key8);
        mButton9 = findViewById(R.id.key9);

        mPasswordEntry.requestFocus();
        super.onFinishInflate();
    }

    public void decideShowOKButton(){
        if(mOkButton!=null){
            if(isPukInput() || isPinInput()){
                mOkButton.setVisibility(View.VISIBLE);
            }else{
                mOkButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            doHapticKeyClick();
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // M: add for debug
            if (DEBUG) {
                Log.d(TAG, "keyCode: " + keyCode + " event: " + event);
            }
            return onKeyDown(keyCode, event);
        }
        return false;
    }
/*prize-puk-input or not-zhangjialong-start*/
	public boolean isPukInput(){
       Log.e("zhangjialong","isPukInput null");
	   return false;
	};
/*prize-puk-input or not-zhangjialong-end*/	 

    public boolean isPinInput(){
       return false;
    }

	public void setPwStatus(){
		Log.d("hky","setPwStatus.....mPasswordEntry = "+mPasswordEntry.getText());
		if((pw1==null)||(pw2==null)||(pw3==null)||(pw4==null)){
			return;
		}
		if(getPasswordText().length()==1){
			pw1.setImageResource(R.drawable.pw_src_sel);	
			pw2.setImageResource(R.drawable.pw_src);
			pw3.setImageResource(R.drawable.pw_src);
			pw4.setImageResource(R.drawable.pw_src);
		}else if(getPasswordText().length()==2){
			pw1.setImageResource(R.drawable.pw_src_sel);
			pw2.setImageResource(R.drawable.pw_src_sel);
			pw3.setImageResource(R.drawable.pw_src);
			pw4.setImageResource(R.drawable.pw_src);
		}else if(getPasswordText().length()==3){
			pw1.setImageResource(R.drawable.pw_src_sel);
			pw2.setImageResource(R.drawable.pw_src_sel);
			pw3.setImageResource(R.drawable.pw_src_sel);
			pw4.setImageResource(R.drawable.pw_src);
		}else if(getPasswordText().length()==4){
			pw1.setImageResource(R.drawable.pw_src_sel);
			pw2.setImageResource(R.drawable.pw_src_sel);
			pw3.setImageResource(R.drawable.pw_src_sel);
			pw4.setImageResource(R.drawable.pw_src_sel);
		}else if(getPasswordText().equals("")){
			pw1.setImageResource(R.drawable.pw_src);
			pw2.setImageResource(R.drawable.pw_src);
			pw3.setImageResource(R.drawable.pw_src);
			pw4.setImageResource(R.drawable.pw_src);
		}
	}
}
