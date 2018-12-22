package com.prize.boot;

import java.util.ArrayList;
import java.util.List;

import com.prize.boot.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.app.StatusBarManager;

public class MainActivity extends Activity {

	protected static final String TAG = "prize";
    private static final String ACTION_LANGUAGE_SETTINGS = "com.prize.boot.LANGUAGE_SETTING";
    private static final String ACTION_WIFI_SETTINGS = "com.prize.boot.WIFI_SETTING";
    private static final String ACTION_USE_TERMS = "com.prize.boot.USE_TERMS";
    private static final String ACTION_OTHER_SETTINGS = "com.prize.boot.OTHER_SETTING";
    private static final String ACTION_OVER = "com.prize.boot.OVER";
	
    private boolean mIsStepInitiated = false;
    private int mCurrentStep = -1;
    private int mTotalStep = 0;
    private List<String> mStepActivityies = new ArrayList<String>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableStatusBar(false);
		initStep();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onNewIntent() mCurrentStep=" + mCurrentStep);
		super.onNewIntent(intent);
		transtActivity();
	}

	private void initStep() {
		Log.i(TAG, "initStep() mCurrentStep=" + mCurrentStep);
        /*if (mIsStepInitiated) {
            return;
        }
        mIsStepInitiated = true;*/
        setActivityList();

        if (mCurrentStep == -1) {
            nextActivity(true);
        } else {
        	transtActivity();
        }
    }
	
	// Get the step list
    protected void setActivityList() {
        mStepActivityies.clear();
        mStepActivityies.add(ACTION_LANGUAGE_SETTINGS);
        mStepActivityies.add(ACTION_WIFI_SETTINGS);
        mStepActivityies.add(ACTION_USE_TERMS);
        mStepActivityies.add(ACTION_OTHER_SETTINGS);
        mStepActivityies.add(ACTION_OVER);
        mTotalStep = mStepActivityies.size();
    }
    
    protected void nextActivity(boolean nextStep) {
        Log.i(TAG, "mCurrentStep:" + mCurrentStep + "mTotalStep:" + mTotalStep + " mStepActivityies.size()=" + mStepActivityies.size());

        if (nextStep) {
            mCurrentStep++;
        } else {
            mCurrentStep--;
        }

        if (mCurrentStep >= 0 && mCurrentStep < mStepActivityies.size()) {

        	Intent intent = new Intent();
        	intent.setAction(mStepActivityies.get(mCurrentStep));
            startActivityForResult(intent, mCurrentStep);

            if (nextStep) {
                overridePendingTransition(R.anim.activity_open_in_anim, R.anim.activity_close_out_anim);
            } else {
                overridePendingTransition(R.anim.activity_close_in_anim, R.anim.activity_open_out_anim);
            }
        }
    }
    
    private void transtActivity() {
    	if (mCurrentStep >= 0 && mCurrentStep < mStepActivityies.size()) {
        	Intent intent = new Intent();
        	intent.setAction(mStepActivityies.get(mCurrentStep));
            startActivityForResult(intent, mCurrentStep);
            overridePendingTransition(R.anim.activity_close_in_anim, R.anim.activity_open_out_anim);
        } else {
        	finish();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult resultCode = " + resultCode + " mCurrentStep=" + mCurrentStep);
        switch (resultCode) {
            case Utils.RESULT_CODE_BACK:
                if (0 == mCurrentStep) {
                    finish();
                } else {
                    nextActivity(false);
                }
                break;
            case Utils.RESULT_CODE_NEXT:
                nextActivity(true);
                break;
            case Utils.RESULT_CODE_FINISH:
                finishOOBE();
                break;
            default:
            	transtActivity();
                break;
        }
    }
    
    /**
     * Enable or disable the status bar 
     * @param enable true to enable, false to disable.
     */
    private void enableStatusBar(boolean enable) {
        Log.i(TAG, "enable status bar " + enable);
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        if (statusBarManager != null) {
            statusBarManager.disable(enable ? StatusBarManager.DISABLE_NONE : StatusBarManager.DISABLE_EXPAND);
        }
    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
    	enableStatusBar(true);
		super.onDestroy();
	}

	private void finishOOBE() {
    	finish();
    }
}
