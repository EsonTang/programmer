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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
/*PRIZE-liyu-for k5919 without mback, auto finish-20161203-start*/
import android.os.Handler;
/*PRIZE-liyu-for k5919 without mback, auto finish-20161203-end*/
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import android.util.Log;

import android.os.PowerManager;
import android.os.SystemClock;
import android.content.Context;
/**
 * Activity which handles the actual enrolling for fingerprint.
 */
public class PrizeFingerprintEnrollEnrolling extends FingerprintEnrollBase
        implements FingerprintEnrollSidecar.Listener {

    static final String TAG_SIDECAR = "sidecar";

    private static final int PROGRESS_BAR_MAX = 10000;
    private static final int FINISH_DELAY = 250;

    /**
     * If we don't see progress during this time, we show an error message to remind the user that
     * he needs to lift the finger and touch again.
     */
    private static final int HINT_TIMEOUT_DURATION = 2500;

    /**
     * How long the user needs to touch the icon until we show the dialog.
     */
    private static final long ICON_TOUCH_DURATION_UNTIL_DIALOG_SHOWN = 500;

    /**
     * How many times the user needs to touch the icon until we show the dialog that this is not the
     * fingerprint sensor.
     */
    private static final int ICON_TOUCH_COUNT_SHOW_UNTIL_DIALOG_SHOWN = 3;

    private ProgressBar mProgressBar;
    private ImageView mFingerprintAnimator;
    private ObjectAnimator mProgressAnim;
    private TextView mStartMessage;
    private TextView mRepeatMessage;
    private TextView mErrorText;
    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;
    private int mIconTouchCount;
    private FingerprintEnrollSidecar mSidecar;
    private boolean mAnimationCancelled;
    private AnimatedVectorDrawable mIconAnimationDrawable;
    private int mIndicatorBackgroundRestingColor;
    private int mIndicatorBackgroundActivatedColor;
    private boolean mRestoring;

	private ActionBar mActionBar;

	private String mFpAddType;

    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
    private TextView fingerprint_enroll_status_title;
    private ImageView fingerprint_progress;
    /*private final int[] printImages = new int[] {
            R.drawable.prize_fingerprint_01, R.drawable.prize_fingerprint_02, R.drawable.prize_fingerprint_03,
            R.drawable.prize_fingerprint_04, R.drawable.prize_fingerprint_05, R.drawable.prize_fingerprint_06,
            R.drawable.prize_fingerprint_07, R.drawable.prize_fingerprint_08, R.drawable.prize_fingerprint_09,
            R.drawable.prize_fingerprint_10, R.drawable.prize_fingerprint_11, R.drawable.prize_fingerprint_12,
            R.drawable.prize_fingerprint_13, R.drawable.prize_fingerprint_14, R.drawable.prize_fingerprint_15,
            R.drawable.prize_fingerprint_16, R.drawable.prize_fingerprint_17, R.drawable.prize_fingerprint_17,
            R.drawable.prize_fingerprint_18, R.drawable.prize_fingerprint_19, R.drawable.prize_fingerprint_20};*/
    private final int[] printImages = new int[] {
            R.drawable.prize_fingerprint_01, R.drawable.prize_fingerprint_02, R.drawable.prize_fingerprint_03,
            R.drawable.prize_fingerprint_04, R.drawable.prize_fingerprint_05, R.drawable.prize_fingerprint_06,
            R.drawable.prize_fingerprint_07, R.drawable.prize_fingerprint_08,
            R.drawable.prize_fingerprint_10, R.drawable.prize_fingerprint_11,
            R.drawable.prize_fingerprint_13, R.drawable.prize_fingerprint_14,
            R.drawable.prize_fingerprint_16, R.drawable.prize_fingerprint_17,
            R.drawable.prize_fingerprint_18, R.drawable.prize_fingerprint_19, R.drawable.prize_fingerprint_20};
    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

	 @Override
	 public boolean onNavigateUp() {
	     finish();
	     return true;
	 }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_SubSettings);
        
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        
        setContentView(R.layout.prize_fingerprint_enroll_enrolling);

        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        View view = getLayout().findViewById(R.id.suw_layout_header);
        if (null != view) view.setVisibility(View.GONE);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        
        CharSequence msg = getText(R.string.add_fingerprint);
        setTitle(msg);
        
        mStartMessage = (TextView) findViewById(R.id.start_message);
        mRepeatMessage = (TextView) findViewById(R.id.repeat_message);
        mErrorText = (TextView) findViewById(R.id.error_text);
        mProgressBar = (ProgressBar) findViewById(R.id.fingerprint_progress_bar);
        mFingerprintAnimator = (ImageView) findViewById(R.id.fingerprint_animator);
        mIconAnimationDrawable = (AnimatedVectorDrawable) mFingerprintAnimator.getDrawable();
        mIconAnimationDrawable.registerAnimationCallback(mIconAnimationCallback);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_slow_in);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_linear_in);
        mFingerprintAnimator.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mIconTouchCount++;
                    if (mIconTouchCount == ICON_TOUCH_COUNT_SHOW_UNTIL_DIALOG_SHOWN) {
                        showIconTouchDialog();
                    } else {
                        mFingerprintAnimator.postDelayed(mShowDialogRunnable,
                                ICON_TOUCH_DURATION_UNTIL_DIALOG_SHOWN);
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                        || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    mFingerprintAnimator.removeCallbacks(mShowDialogRunnable);
                }
                return true;
            }
        });
        mIndicatorBackgroundRestingColor
                = getColor(R.color.fingerprint_indicator_background_resting);
        mIndicatorBackgroundActivatedColor
                = getColor(R.color.fingerprint_indicator_background_activated);
        mRestoring = savedInstanceState != null;

        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        fingerprint_enroll_status_title = (TextView) findViewById(R.id.fingerprint_enroll_status_title);
        fingerprint_progress = (ImageView) findViewById(R.id.fingerprint_progress);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

        Intent mIntent = getIntent();
        mFpAddType =  mIntent.getStringExtra(PrizeFpOperationInterface.FP_ADD_TYPE_KEY);
    }
    
    protected void initViews() {
        getResources().getColor(R.color.prize_actionbar_background);
        //getNavigationBar().setVisibility(View.GONE);
        Button nextButton = getNextButton();
        if (nextButton != null) {
            nextButton.setOnClickListener(this);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag(TAG_SIDECAR);
        if (mSidecar == null) {
            mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(mSidecar, TAG_SIDECAR).commit();
        }
        mSidecar.setListener(this);
        updateProgress(false /* animate */);
        updateDescription();
        if (mRestoring) {
            startIconAnimation();
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        mAnimationCancelled = false;
        startIconAnimation();
    }

    private void startIconAnimation() {
        mIconAnimationDrawable.start();
    }

    private void stopIconAnimation() {
        mAnimationCancelled = true;
        mIconAnimationDrawable.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSidecar.setListener(null);
        stopIconAnimation();
        if (!isChangingConfigurations()) {
            finish();
        }
    }

    private void animateProgress(int progress) {
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        fingerprint_progress.setBackgroundResource(printImages[progress / 625]);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        if (mProgressAnim != null) {
            mProgressAnim.cancel();
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(mProgressBar, "progress",
                mProgressBar.getProgress(), progress);
        anim.addListener(mProgressAnimationListener);
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(250);
        anim.start();
        mProgressAnim = anim;
    }

    private void animateFlash() {
        ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundRestingColor,
                mIndicatorBackgroundActivatedColor);
        final ValueAnimator.AnimatorUpdateListener listener =
                new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFingerprintAnimator.setBackgroundTintList(ColorStateList.valueOf(
                        (Integer) animation.getAnimatedValue()));
            }
        };
        anim.addUpdateListener(listener);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundActivatedColor,
                        mIndicatorBackgroundRestingColor);
                anim.addUpdateListener(listener);
                anim.setDuration(300);
                anim.setInterpolator(mLinearOutSlowInInterpolator);
                anim.start();
            }
        });
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(300);
        anim.start();
    }

    private void launchFinish(byte[] token) {
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
        fingerprint_enroll_status_title.setText(getString(R.string.prize_fingerprint_enroll_status_completed));
        mRepeatMessage.setText(getString(R.string.prize_fingerprint_enroll_status_summary_completed));
        fingerprint_progress.setBackgroundResource(R.drawable.prize_fingerprint_load_finished);
        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
		/*PRIZE-liyu-for k5919 without mback, auto finish-20161203-start*/
		new Handler().postDelayed(new Runnable(){    
			public void run() {    
				//execute the task  
				try{
					finish();
				}catch(Exception e){
					
				}
			}    
		}, 1000);   
        /*PRIZE-liyu-for k5919 without mback, auto finish-20161203-end*/
        /*PRIZE-Delete-M_Fingerprint-wangzhong-2016_6_28-start*/
    	/*if(mFpAddType != null && mFpAddType.equals(PrizeFpOperationInterface.ADD_FP_IN_FP_INTRRFACE)){
    		finish();
    	} else {
    		Intent intent = getFinishIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
            startActivity(intent);
            finish();
    	}*/
        /*PRIZE-Delete-M_Fingerprint-wangzhong-2016_6_28-end*/
    }

    protected Intent getFinishIntent() {
        return new Intent(this, FingerprintEnrollFinish.class);
    }

    private void updateDescription() {
        if (mSidecar.getEnrollmentSteps() == -1) {
//            setHeaderText(R.string.security_settings_fingerprint_enroll_start_title);
            mStartMessage.setVisibility(View.VISIBLE);
            mRepeatMessage.setVisibility(View.INVISIBLE);
        } else {
//            setHeaderText(R.string.security_settings_fingerprint_enroll_repeat_title,
//                    true /* force */);
            mStartMessage.setVisibility(View.INVISIBLE);
            mRepeatMessage.setVisibility(View.VISIBLE);
        }
    }

    /*-prize-add by lihuangyuan,for useractivity -2017-11-30-start*/
    public void userActivity()
    {
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
    	 powerManager.userActivity(SystemClock.uptimeMillis(), false);
    }    
    /*-prize-add by lihuangyuan,for useractivity -2017-11-30-end*/
    /*-prize-add by lihuangyuan,for move onEnrollmentHelp to main thread-2018-03-10-start*/
    Handler mHandler = new Handler();
    private String mHelpString;
    private Runnable m_enrollmentHelpRun = new Runnable()
    {
        public void run()
        {
            	 mErrorText.animate().cancel();
	        mErrorText.setAlpha(1f);
	        mErrorText.setTranslationY(0f);
	        mErrorText.setVisibility(View.VISIBLE);
	        //prize-wuliang-20170913 show helpstring end
	        mErrorText.setText(mHelpString);		 	 
			
		 userActivity();		 
        }
    };
    /*-prize-add by lihuangyuan,for move onEnrollmentHelp to main thread-2018-03-10-start*/
    @Override
    public void onEnrollmentHelp(CharSequence helpString) {
        //prize-wuliang-20170913 show helpstring start
        /*-prize-add by lihuangyuan,for move onEnrollmentHelp to main thread-2018-03-10-start*/
        Log.d("lhy","onEnrollmentHelp helpString:"+helpString);
        mHelpString = helpString.toString();
	 mHandler.post(m_enrollmentHelpRun);
	 /*-prize-add by lihuangyuan,for move onEnrollmentHelp to main thread-2018-03-10-end*/
    }

    @Override
    public void onEnrollmentError(int errMsgId, CharSequence errString) {
        int msgId;
        switch (errMsgId) {
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                // This message happens when the underlying crypto layer decides to revoke the
                // enrollment auth token.
                msgId = R.string.security_settings_fingerprint_enroll_error_timeout_dialog_message;
		  /*-prize-add by lihuangyuan,for useractivity -2017-11-30-start*/		 
	 	 userActivity();
	 	 /*-prize-add by lihuangyuan,for useractivity -2017-11-30-end*/
                break;
            default:
                // There's nothing specific to tell the user about. Ask them to try again.
                msgId = R.string.security_settings_fingerprint_enroll_error_generic_dialog_message;
                break;
        }
        showErrorDialog(getText(msgId), errMsgId);
        stopIconAnimation();
        mErrorText.removeCallbacks(mTouchAgainRunnable);
    }

    @Override
    public void onEnrollmentProgressChange(int steps, int remaining) {
        updateProgress(true /* animate */);
        updateDescription();
        clearError();
        animateFlash();
        mErrorText.removeCallbacks(mTouchAgainRunnable);
        mErrorText.postDelayed(mTouchAgainRunnable, HINT_TIMEOUT_DURATION);
	 /*-prize-add by lihuangyuan,for useractivity -2017-11-30-start*/	
	 userActivity();
	 /*-prize-add by lihuangyuan,for useractivity -2017-11-30-end*/
    }

    private void updateProgress(boolean animate) {
        int progress = getProgress(
                mSidecar.getEnrollmentSteps(), mSidecar.getEnrollmentRemaining());
        if (animate) {
            animateProgress(progress);
        } else {
            mProgressBar.setProgress(progress);
        }
    }

    private int getProgress(int steps, int remaining) {
        if (steps == -1) {
            return 0;
        }
        int progress = Math.max(0, steps + 1 - remaining);
        return PROGRESS_BAR_MAX * progress / (steps + 1);
    }

    private void showErrorDialog(CharSequence msg, int msgId) {
        ErrorDialog dlg = ErrorDialog.newInstance(msg, msgId);
        dlg.show(getFragmentManager(), ErrorDialog.class.getName());
    }

    private void showIconTouchDialog() {
        mIconTouchCount = 0;
        new IconTouchDialog().show(getFragmentManager(), null /* tag */);
    }

    private void showError(CharSequence error) {
        mErrorText.setText(error);
        if (mErrorText.getVisibility() == View.INVISIBLE) {
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.setTranslationY(getResources().getDimensionPixelSize(
                    R.dimen.fingerprint_error_text_appear_distance));
            mErrorText.setAlpha(0f);
            mErrorText.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(mLinearOutSlowInInterpolator)
                    .start();
        } else {
            mErrorText.animate().cancel();
            mErrorText.setAlpha(1f);
            mErrorText.setTranslationY(0f);
        }
    }

    private void clearError() {
        if (mErrorText.getVisibility() == View.VISIBLE) {
            mErrorText.animate()
                    .alpha(0f)
                    .translationY(getResources().getDimensionPixelSize(
                            R.dimen.fingerprint_error_text_disappear_distance))
                    .setDuration(100)
                    .setInterpolator(mFastOutLinearInInterpolator)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mErrorText.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
    }

    private final Animator.AnimatorListener mProgressAnimationListener
            = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mProgressBar.getProgress() >= PROGRESS_BAR_MAX) {
            	mSidecar.setListener(null);
            	mErrorText.removeCallbacks(mTouchAgainRunnable);
                mProgressBar.postDelayed(mDelayedFinishRunnable, FINISH_DELAY);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) { }
    };

    // Give the user a chance to see progress completed before jumping to the next stage.
    private final Runnable mDelayedFinishRunnable = new Runnable() {
        @Override
        public void run() {
            launchFinish(mToken);
        }
    };

    private final Animatable2.AnimationCallback mIconAnimationCallback =
            new Animatable2.AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable d) {
            if (mAnimationCancelled) {
                return;
            }

            // Start animation after it has ended.
            mFingerprintAnimator.post(new Runnable() {
                @Override
                public void run() {
                    startIconAnimation();
                }
            });
        }
    };

    private final Runnable mShowDialogRunnable = new Runnable() {
        @Override
        public void run() {
            showIconTouchDialog();
        }
    };

    private final Runnable mTouchAgainRunnable = new Runnable() {
        @Override
        public void run() {
            showError(getString(R.string.security_settings_fingerprint_enroll_lift_touch_again));
        }
    };

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT_ENROLLING;
    }

    public static class IconTouchDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.security_settings_fingerprint_enroll_touch_dialog_title)
                    .setMessage(R.string.security_settings_fingerprint_enroll_touch_dialog_message)
                    .setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
            return builder.create();
        }
    }

    public static class ErrorDialog extends DialogFragment {

        /**
         * Create a new instance of ErrorDialog.
         *
         * @param msg the string to show for message text
         * @param msgId the FingerprintManager error id so we know the cause
         * @return a new ErrorDialog
         */
        static ErrorDialog newInstance(CharSequence msg, int msgId) {
            ErrorDialog dlg = new ErrorDialog();
            Bundle args = new Bundle();
            args.putCharSequence("error_msg", msg);
            args.putInt("error_id", msgId);
            dlg.setArguments(args);
            return dlg;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            CharSequence errorString = getArguments().getCharSequence("error_msg");
            final int errMsgId = getArguments().getInt("error_id");
            builder.setTitle(R.string.security_settings_fingerprint_enroll_error_dialog_title)
                    .setMessage(errorString)
                    .setCancelable(false)
                    .setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    boolean wasTimeout =
                                        errMsgId == FingerprintManager.FINGERPRINT_ERROR_TIMEOUT;
                                    Activity activity = getActivity();
                                    activity.setResult(wasTimeout ?
                                            RESULT_TIMEOUT : RESULT_FINISHED);
                                    activity.finish();
                                }
                            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
    
}
