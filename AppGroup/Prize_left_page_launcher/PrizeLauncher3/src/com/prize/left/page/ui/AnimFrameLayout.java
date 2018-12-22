package com.prize.left.page.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
/***
 * 带动画的FrameLayout
 * @author fanjunchen
 *
 */
public class AnimFrameLayout extends FrameLayout {
	
	private View oldChild = null;
	
	private static final String TRANSLATION_Y = "translationY";
	
	private final int DURATION_TRANSLATE = 400;
	
	private final int DURATION_DELAY = 100;
	
	private AnimatorSet mAnimSet = null;
	
	private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {
			oldChild = null;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if (oldChild != null)
				removeView(oldChild);
			oldChild = null;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}

		@Override
		public void onAnimationStart(Animator animation) {
			
		}
		
	};
	
	private FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 
			FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);

	public AnimFrameLayout(Context context) {
		this(context, null);
	}

	public AnimFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnimFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/***
	 * 替换主View, 带动画的替换
	 * @param v
	 * @param pos
	 */
	public void replaceView(View v, int pos) {
		int c = getChildCount();
		if (c > 0) {
			int h = getHeight();
			if (mAnimSet != null)
				mAnimSet.end();
			mAnimSet = new AnimatorSet();
			
			addView(v, mParams);
			
			ObjectAnimator a = ObjectAnimator.ofFloat(v, TRANSLATION_Y, -h, 0);
			oldChild = getChildAt(0);
			ObjectAnimator b = ObjectAnimator.ofFloat(oldChild, TRANSLATION_Y, 0, h);
			b.addListener(mAnimListener);
			
			mAnimSet.playTogether(a, b);
			mAnimSet.setDuration(DURATION_TRANSLATE);
			mAnimSet.setStartDelay(pos * DURATION_DELAY);
			mAnimSet.start();
		}
		else {
			addView(v, mParams);
		}
	}

}
