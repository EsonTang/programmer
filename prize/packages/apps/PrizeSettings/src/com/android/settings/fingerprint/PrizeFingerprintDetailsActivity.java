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

package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnShowListener;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;

public class PrizeFingerprintDetailsActivity extends FingerprintEnrollBase {
	
	public static final String TAG = "PrizeFingerprintDetailsActivity";
	private RelativeLayout mNotRelationRl;
	private LinearLayout mRelationLl;
	private TextView mNotRelatFpNameView;
	private TextView mRelatFpNameView;
	private ImageView mRelationAppIconView;
	private TextView mNRelatAppNameView;
	private TextView mRelatFpView;
	private TextView mRenameFpNameView;
	private TextView mDeleteFpView;
	private Dialog mRenameDialog;
	private Dialog mDeleteDialog;
	private String mFpOriginalName;

	private ActionBar mActionBar;
	
	private FingerprintManager mFingerprintManager;
	private Fingerprint mFingerprint;
	
	private OnClickListener mOperationClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
//			case R.id.relation_app_button:
//				Intent intent = new Intent(FpDetailActivity.this,FpRelatedAppActivity.class);
//				if(null != mFpInfo){
//					intent.putExtra(ConstantUtil.RELATED_APP_PKGNAME_KEY, mFpInfo.relatedPkgName);
//				}
//				startActivityForResult(intent, ConstantUtil.RELATED_APP_REQUEST_CODE);
//				break;

			case R.id.rename_fp_button:
				//mFpOriginalName = mFingerprint.getName().toString();
				mFpOriginalName = mNotRelatFpNameView.getText().toString();
				mRenameDialog = PrizeFpOperationDialogUtils.createDoubleButtonEditDialog(
						PrizeFingerprintDetailsActivity.this, getString(R.string.prize_fp_dialog_title_edit_fingerprint), 
						mFpOriginalName, mRenameConfirmClick, mRenameCancelClick);
				mRenameDialog.setOnShowListener(new OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						Timer timer = new Timer();
						timer.schedule( new TimerTask() {
							@Override
							public void run() {
								((InputMethodManager) getSystemService(Context. INPUT_METHOD_SERVICE)).toggleSoftInput(0,
										InputMethodManager. HIDE_NOT_ALWAYS);
							}
						}, 100);
					}
				});
				mRenameDialog.show();
				break;
			case R.id.delete_fp_button:
				String notice = getString(R.string.prize_fp_dialog_do_you_want_to_remove)+ mFingerprint.getName().toString()+"?";
				mDeleteDialog = PrizeFpOperationDialogUtils.createDoubleButtonTextDialog(
						PrizeFingerprintDetailsActivity.this, getString(R.string.prize_fp_operation_prompt), 
						notice, mDeleteConfirmClick, mDeleteCancelClick);
				break;
			default:
				break;
			}
		}
	};
	
	private OnClickListener mRenameConfirmClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			PrizeFpCustomEditText renameView = (PrizeFpCustomEditText)mRenameDialog.findViewById(R.id.content_text_edit);
			String newName = renameView.getText().toString().trim();
			final List<Fingerprint> items = mFingerprintManager.getEnrolledFingerprints();
			boolean isRepeated = false;
			for(Fingerprint fingerprint:items){
				if(fingerprint.getName().equals(newName)){
					isRepeated = true;
				}
			}
			if(null == newName || newName.length() == 0){
				renameView.setText(mFpOriginalName);
				renameView.setSelection(mFpOriginalName.length());
				Toast.makeText(PrizeFingerprintDetailsActivity.this, R.string.prize_fingeprint_name_cannot_empty, Toast.LENGTH_SHORT).show();
				return;
			}else if(isRepeated){
				renameView.setText(mFpOriginalName);
				renameView.setSelection(mFpOriginalName.length());
				Toast.makeText(PrizeFingerprintDetailsActivity.this, R.string.prize_fingeprint_name_cannot_repeated, Toast.LENGTH_SHORT).show();
				return;
			}else{
				if(View.VISIBLE == mNotRelationRl.getVisibility()){
					mNotRelatFpNameView.setText(newName);
				}else{
					mRelatFpNameView.setText(newName);
				}
				MetricsLogger.action(PrizeFingerprintDetailsActivity.this, MetricsEvent.ACTION_FINGERPRINT_RENAME, 
						mFingerprint.getFingerId());
				mFingerprintManager.rename(mFingerprint.getFingerId(), mUserId, newName);
				mRenameDialog.dismiss();
			}
		}
	};

	private OnClickListener mRenameCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mRenameDialog.dismiss();
		}	
	}; 

	private OnClickListener mDeleteConfirmClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			MetricsLogger.action(PrizeFingerprintDetailsActivity.this, MetricsEvent.ACTION_FINGERPRINT_DELETE,
					mFingerprint.getFingerId());
			mFingerprintManager.remove(mFingerprint, mUserId, mRemoveCallback);
			mDeleteDialog.dismiss();
		}
	};

	private OnClickListener mDeleteCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mDeleteDialog.dismiss();
		}	
	};
	
	private RemovalCallback mRemoveCallback = new RemovalCallback() {
        @Override
        public void onRemovalSucceeded(Fingerprint fingerprint) {
        	Log.d(TAG, "RemovalCallback onRemovalSucceeded()");
        	finish();
        }

        @Override
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        	Log.d(TAG, "RemovalCallback onRemovalError() Error: "+errString);
        	Toast.makeText(PrizeFingerprintDetailsActivity.this, errString, Toast.LENGTH_SHORT);
        }
    };
	
	 @Override
	 public boolean onNavigateUp() {
	     finish();
	     return true;
	 }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_SubSettings);

        setContentView(R.layout.prize_fingerprint_details_activity);
        
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        setTitle(getText(R.string.prize_fingerprint_details_title));
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

        initData();
		initView();
    }
    
    protected void initViews() {
        getResources().getColor(R.color.prize_actionbar_background);
    }
    
    private void initData() {
    	mFingerprintManager = (FingerprintManager) getSystemService( Context.FINGERPRINT_SERVICE);
    	Intent intent = getIntent();
    	Bundle mBundle = intent.getBundleExtra(PrizeFpOperationInterface.FP_DETAILS_BUNDLE_KEY);
    	mFingerprint = (Fingerprint)mBundle.getParcelable(PrizeFpOperationInterface.FP_DETAILS_KEY);
    	mFpOriginalName = mFingerprint.getName().toString();
	}

	private void initView() {
		mNotRelationRl = (RelativeLayout) findViewById(R.id.not_relation_rl);
		mRelationLl = (LinearLayout) findViewById(R.id.relation_rl);
		mNotRelatFpNameView = (TextView) findViewById(R.id.not_relation_fp_name);
		mRelatFpNameView = (TextView) findViewById(R.id.relation_fp_name);
		mRelationAppIconView = (ImageView) findViewById(R.id.relation_app_icon);
		mNRelatAppNameView = (TextView) findViewById(R.id.relation_app_name);
		mRelatFpView = (TextView) findViewById(R.id.relation_app_button);
		mRenameFpNameView = (TextView) findViewById(R.id.rename_fp_button);
		mDeleteFpView = (TextView) findViewById(R.id.delete_fp_button);

//		mRelatFpView.setOnClickListener(mOperationClick);
		mRelatFpView.setVisibility(View.GONE);
		mRenameFpNameView.setOnClickListener(mOperationClick);
		mDeleteFpView.setOnClickListener(mOperationClick);

//		if(null == mFpInfo.relatedPkgName || mFpInfo.relatedPkgName.length() == 0){
			mNotRelationRl.setVisibility(View.VISIBLE);
			mRelationLl.setVisibility(View.GONE);
			mNotRelatFpNameView.setText(mFpOriginalName);
//			mRelatFpView.setText(R.string.add_relation);
			mNRelatAppNameView.setText(null);
			mRelationAppIconView.setBackground(null);
//		}else{
//			mNotRelationRl.setVisibility(View.GONE);
//			mRelationLl.setVisibility(View.VISIBLE);
//			mRelatFpNameView.setText(mFpInfo.name);
//			mRelatFpView.setText(R.string.modify_relation);
//			setRelatedAppInfo(mFpInfo.getRelatedPkgName());
//		}
	}
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected Intent getFinishIntent() {
        return new Intent(this, FingerprintEnrollFinish.class);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT_OPERATION;
    }
    
}
