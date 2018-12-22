package com.prize.contacts.common.util;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.contacts.common.R;

/**
 * Created by for huangpengfei on 2017-8-12
 */
public class PrizeAnimationHelper {

	private static String TAG = "PrizeAnimationHelper";
	public static boolean HIDE = true;
	public static boolean SHOW = false;
	public static int mDuration = 150;
	private Activity mActivity;
	private InputMethodManager imm;
	private float mPrizeActionBarHeight;
	private float mPrizeSearchLayoutHeight;
	
	public PrizeAnimationHelper(Activity activity){
		this.mActivity = activity;
		imm = (InputMethodManager) mActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
		 mPrizeActionBarHeight = activity.getResources().getDimension(R.dimen.prize_actionbar_custom_height);
	     mPrizeSearchLayoutHeight = activity.getResources().getDimension(R.dimen.prize_search_editor_layout_hight);
	}

	public static void bladeViewAnimation(View v,boolean isHide) {
		Log.d(TAG,"[bladeViewAnimation]  isHide = " + isHide
				+ "  v.getWidth() "+v.getWidth());
		if (isHide) {
			ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX",0,
					v.getWidth());
			animator.setDuration(mDuration);
			animator.start();
		}else{
			ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX",v.getWidth(),
					0);
//			animator.setRepeatCount(1);
//			animator.setRepeatMode(ValueAnimator.RESTART);
			animator.setDuration(mDuration);
			animator.start();
		}
	}
	
	public void peopleSearchAnimation(final View searchEditorView,final EditText searchEt,View toobarLayout,
			final View listContainer,final View bottomButton,final View bottomButtonShadow,boolean isHide) {
		Log.d(TAG,"[bladeViewAnimation]  isHide = " + isHide);
		if (isHide) {
			ObjectAnimator translationY = ObjectAnimator.ofFloat(searchEditorView, "translationY",0,
					-mPrizeActionBarHeight);
			ObjectAnimator translationY2 = ObjectAnimator.ofFloat(toobarLayout, "translationY",0,
					-mPrizeActionBarHeight);
			ObjectAnimator translationY3 = ObjectAnimator.ofFloat(listContainer, "translationY",0,
					-mPrizeSearchLayoutHeight);
			ObjectAnimator translationY4 = ObjectAnimator.ofFloat(bottomButton, "translationY",0,
					bottomButton.getHeight());
			ObjectAnimator translationY5 = ObjectAnimator.ofFloat(bottomButtonShadow, "translationY",0,
					bottomButtonShadow.getHeight());
			ObjectAnimator alpha = ObjectAnimator.ofFloat(searchEditorView, "alpha", 0f, 1f);
			translationY4.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {}
				
				@Override
				public void onAnimationRepeat(Animator animation) {}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					bottomButton.setVisibility(View.GONE);
					searchEt.requestFocus();
					showInputMethod(searchEt);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) listContainer.getLayoutParams();
					lp.setMargins(0,0, 0, -(int)mPrizeSearchLayoutHeight);
					listContainer.setLayoutParams(lp);
					listContainer.requestLayout();
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			
			AnimatorSet set = new AnimatorSet();
			set.play(translationY).with(translationY2).with(translationY3).with(translationY4).with(translationY5).with(alpha);
			set.setDuration(mDuration);
			set.start();
			
		}else{
			ObjectAnimator translationY = ObjectAnimator.ofFloat(searchEditorView, "translationY",
					-mPrizeActionBarHeight,0);
			ObjectAnimator translationY2 = ObjectAnimator.ofFloat(toobarLayout, "translationY",
					-mPrizeActionBarHeight,0);
			ObjectAnimator translationY3 = ObjectAnimator.ofFloat(listContainer, "translationY",
					-mPrizeSearchLayoutHeight,0);
			ObjectAnimator translationY4 = ObjectAnimator.ofFloat(bottomButton, "translationY",
					bottomButton.getHeight(),0);
			ObjectAnimator translationY5 = ObjectAnimator.ofFloat(bottomButtonShadow, "translationY",
					bottomButtonShadow.getHeight(),0);
			ObjectAnimator alpha = ObjectAnimator.ofFloat(searchEditorView, "alpha", 1f, 0f);
			alpha.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					bottomButton.setVisibility(View.VISIBLE);
					bottomButtonShadow.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					searchEditorView.setVisibility(View.GONE);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) listContainer.getLayoutParams();
					lp.setMargins(0,0,0,0);
					listContainer.setLayoutParams(lp);
					listContainer.requestLayout();
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			AnimatorSet set = new AnimatorSet();
			set.play(translationY).with(translationY2).with(translationY3).with(translationY4).with(translationY5).with(alpha);
			set.setDuration(mDuration);
			set.start();
		}
	}
	
	private void showInputMethod(View view) {
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }
	public void hideInputMethod() {
		if (imm != null) {
		      imm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}

}
