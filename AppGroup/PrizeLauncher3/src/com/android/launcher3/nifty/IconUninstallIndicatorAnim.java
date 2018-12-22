package com.android.launcher3.nifty;

import com.android.launcher3.StateInfo;
import com.android.launcher3.Workspace;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup;

public class IconUninstallIndicatorAnim {

	private ValueAnimator mAcceptAnimator;
	public double mOuterRingSize=0f;
	
	private boolean isStart = false;

	public boolean isStart() {
		return isStart;
	}
	
	


	public void animateToIconIndcatorDraw(final View v, final float from,
			final float to,final StateInfo p) {
		mAcceptAnimator = ValueAnimator.ofFloat(from, to);
		mAcceptAnimator.setDuration(/*00*/300);
		mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {

			public void onAnimationUpdate(ValueAnimator animation) {
				final float percent = (Float) animation.getAnimatedValue();
				mOuterRingSize = percent;//from + percent * (to - from);// 这个是放到的值
				v.invalidate();
			}
		});
		mAcceptAnimator.start();
		mAcceptAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				isStart=true;
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				isStart =false;
				try {
					ViewGroup vp=(ViewGroup) (v.getParent().getParent().getParent());
					if (vp instanceof Workspace) {
						Workspace wk = (Workspace) vp;
						if (p.lasted)
						wk.OnSpringFinish(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	public double getOuterRingSize() {
		return mOuterRingSize;
	}

	public void setOuterRingSize(double mOuterRingSize) {
		this.mOuterRingSize = mOuterRingSize;
	}
}
