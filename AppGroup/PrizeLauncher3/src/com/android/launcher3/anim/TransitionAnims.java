package com.android.launcher3.anim;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;

public abstract class TransitionAnims {

	private long mDuration = 800;
	private long mStartDelay = 0;
	protected Interpolator mTimeInterpolator;
	public TransitionAnims(Interpolator mTimeInterpolator) {
		super();
		this.mTimeInterpolator = mTimeInterpolator;
	}



	public TransitionAnims() {
		super();
		// TODO Auto-generated constructor stub
	}

	private TransitionListener mListener;
	protected AnimatorSet mAnimatorSet = new AnimatorSet();

	public interface TransitionListener {
		public void onTransitionStart(Animator animator, Animation animation);

		public void onTransitionEnd(Animator animator, Animation animation);
	}
	
	
	public void start() {
		mAnimatorSet.start();
	}
	
	

	public AnimatorSet getAnimatorSet() {
		return mAnimatorSet;
	}



	public ObjectAnimator ofFloat(Object target, String propertyName,
			float... values) {
		ObjectAnimator objectAnima = null;
		if (propertyName.equals("scaleX")) {
			objectAnima = ObjectAnimator.ofFloat(target, propertyName, values);
		}
		if (propertyName.equals("scaleY")) {
			objectAnima = ObjectAnimator.ofFloat(target, propertyName, values);

		}
		if (propertyName.equals("translationX")) {
			objectAnima = ObjectAnimator.ofFloat(target, propertyName, values);
		}
		if (propertyName.equals("translationY")) {
			objectAnima = ObjectAnimator.ofFloat(target, propertyName, values);

		}
		if (propertyName.equals("alpha")) {
			objectAnima = ObjectAnimator.ofFloat(target, propertyName, values);

		}
		if (objectAnima != null) {
			mAnimatorSet.play(objectAnima);
		}

		return objectAnima;
	}

	public void setDuration(long duration) {
		this.mDuration = duration;
		mAnimatorSet.setDuration(mDuration);
	}

	public void setStartDelay(long startDelay) {
		this.mStartDelay = startDelay;
		mAnimatorSet.setStartDelay(mStartDelay);
	}

	public void setInterpolator(android.view.animation.Interpolator mZoomInInterpolator) {
		this.mTimeInterpolator = (Interpolator) mZoomInInterpolator;
		mAnimatorSet.setInterpolator((TimeInterpolator) mTimeInterpolator);
	}

	public void setListener(TransitionListener mListener) {
		this.mListener = mListener;
		mAnimatorSet.addListener(new TransitionAnimsListener());
	}

	protected class TransitionAnimsListener implements AnimatorListener,
			AnimationListener {

		@Override
		public void onAnimationStart(Animator animator) {
			// TODO 自动生成的方法存根
			if (mListener != null) {
				mListener.onTransitionStart(animator, null);
			}
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (mListener != null) {
				mListener.onTransitionEnd(animator, null);
			}
		}

		@Override
		public void onAnimationRepeat(Animator animator) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			if (mListener != null) {
				mListener.onTransitionStart(null, animation);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (mListener != null) {
				mListener.onTransitionEnd(null, animation);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}

	}

}
