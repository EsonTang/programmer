package com.android.settings.applock;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.settings.R;

import com.android.settings.InstrumentedFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.pinlockview.IndicatorDots;
import com.android.settings.pinlockview.PinLockListener;
import com.android.settings.pinlockview.PinLockView;

import java.util.List;

/**
 * Created by wangzhong on 2016/7/11.
 */
public class PrizeAppLockChooseNumPassword extends SettingsActivity {

    private static final String TAG = "PrizeAppLock";

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, getFragmentClass().getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeAppLockChooseNumPasswordFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

    /* package */ Class<? extends Fragment> getFragmentClass() {
        return PrizeAppLockChooseNumPasswordFragment.class;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        CharSequence msg = getText(R.string.prize_applock_cipher_operation_title);
        setTitle(msg);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrizeAppLockChooseNumPasswordFragment extends InstrumentedFragment {

        private static final int MSG_NOTICE_FIRST = 188;
        private static final int MSG_NOTICE_COMPLETE = 189;
        private static final int MSG_NOTICE_ERROE = 190;
        private static final int NOTICE_COMPLETE_MESSAGE_TIMEOUT = 300;

        private Stage mUiStage = Stage.Introduction;

        private String mFirstPin;

        private TextView tv_header;
        private IndicatorDots indicator_dots;
        private Button bt_next;
        private TextView tv_other_method;
        private PinLockView pin_lock_view;

        private FingerprintManager mFingerprintManager;
        private ContentResolver mContentResolver = null;

        protected enum Stage {

            Introduction(0),

            NeedToConfirm(1),

            ConfirmWrong(2);

            Stage(int hintInNumeric) {
                this.hintInNumeric = hintInNumeric;
            }

            public final int hintInNumeric;
        }

        public void toSaveCipher(String pin) {
            ContentResolver mResolver= tv_header.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(PrizeAppLockCipherMetaData.CIPHER_1, pin);
	     values.put(PrizeAppLockCipherMetaData.CIPHER_TYPE, PrizeAppLockCipherMetaData.CIPHER_TYPE_NUM);
	     values.put(PrizeAppLockCipherMetaData.CIPHER_STATUS,PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID);
			
	     Log.d(TAG,"toSaveCipher "+pin);
            String where = PrizeAppLockCipherMetaData.CIPHER_STATUS + " =?";
            String[] selectionArgs = new String[]{""+PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID};//effective.
            Cursor mCursor = mResolver.query(PrizeAppLockCipherMetaData.CONTENT_URI, null, where, selectionArgs, null);
            if (null != mCursor && mCursor.getCount() > 0) {
                mResolver.update(PrizeAppLockCipherMetaData.CONTENT_URI, values, "", null);
            } else {
                mResolver.insert(PrizeAppLockCipherMetaData.CONTENT_URI, values);
            }

            //open the app lock function.            
            updateDBAppLockData(PrizeFpFuntionMetaData.APP_LOCK_FUNCTION_OPEN, PrizeFpFuntionMetaData.APP_LOCK_FC);
            
            if(mCursor != null){
            	mCursor.close();
            }
            
            //finish
            getActivity().finish();
        }
        
        //set the function column to 1
        public void updateDBAppLockData(int functionStatus, String keyName) {
            if (null == mContentResolver) {
                mContentResolver = tv_header.getContext().getContentResolver();
            }
	     //the table created with initial record --default value 0
            ContentValues values = new ContentValues();
            values.put(PrizeFpFuntionMetaData.FUNCTION_STATUS, functionStatus);
            String where = PrizeFpFuntionMetaData.FUNCTION_NAME + " =?";
            String[] selectionArgs = new String[]{keyName};

            mContentResolver.update(PrizeFpFuntionMetaData.CONTENT_URI, values, where, selectionArgs);
        }

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_NOTICE_FIRST) {
                    if (null != pin_lock_view) {
                        pin_lock_view.resetPinLockView();//The first pin.
                        tv_header.setText(getString(R.string.prize_applock_ciphersimple_header_again));
                    }
                } else if (msg.what == MSG_NOTICE_COMPLETE) {
                    bt_next.setText(R.string.prize_applock_ciphersimple_complete);
                    toSaveCipher(mFirstPin);
                } else if (msg.what == MSG_NOTICE_ERROE) {
                    if (null != pin_lock_view) {
                        pin_lock_view.resetPinLockView();//The second input errors, restore the mPinLockView.
                        tv_header.setText(getString(R.string.prize_applock_ciphersimple_header_error));
                    }
                }
            }

        };

        private PinLockListener mPinLockListener = new PinLockListener() {

            @Override
            public void onComplete(String pin) {
                Log.d("john", "Pin complete:  pin : " + pin);
                if (mUiStage == Stage.Introduction) {
                    mFirstPin = pin;
                    //pin_lock_view.setShowDeleteButton(false);
                    mUiStage = Stage.NeedToConfirm;
                    updatePinLockViewDelayed(MSG_NOTICE_FIRST, pin);
                } else if (mUiStage == Stage.NeedToConfirm || mUiStage == Stage.ConfirmWrong) {
                    if (mFirstPin.equals(pin)) {
                        updatePinLockViewDelayed(MSG_NOTICE_COMPLETE, pin);
                    } else {
                        mUiStage = Stage.ConfirmWrong;
                        if (null != pin_lock_view) {
                            pin_lock_view.updatePinLockViewError();
                            updatePinLockViewDelayed(MSG_NOTICE_ERROE, "");
                        }
                    }
                }
            }

            @Override
            public void onEmpty() {
                //Log.d("john", "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                //Log.d("john", "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (!(getActivity() instanceof PrizeAppLockChooseNumPassword)) {
                throw new SecurityException("Fragment contained in wrong activity");
            }
        }

        @Override
        protected int getMetricsCategory() {
            return MetricsEvent.CHOOSE_LOCK_PASSWORD;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.prize_applock_choose_num_password, container, false);
            tv_header = (TextView) layout.findViewById(R.id.tv_header);
            indicator_dots = (IndicatorDots) layout.findViewById(R.id.indicator_dots);
            bt_next = (Button) layout.findViewById(R.id.bt_next);
            tv_other_method = (TextView) layout.findViewById(R.id.tv_other_method);
            pin_lock_view = (PinLockView) layout.findViewById(R.id.pin_lock_view);

            pin_lock_view.attachIndicatorDots(indicator_dots);
            pin_lock_view.setPinLockListener(mPinLockListener);
			
            return layout;
        }

        private void updatePinLockViewDelayed(int what, String pin) {
            Message msg = mHandler.obtainMessage(what, pin);
            mHandler.removeMessages(what);
            mHandler.sendMessageDelayed(msg, NOTICE_COMPLETE_MESSAGE_TIMEOUT);
        }

    }
}
