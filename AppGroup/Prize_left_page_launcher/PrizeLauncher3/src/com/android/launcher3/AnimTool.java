package com.android.launcher3;

import java.util.ArrayList;

import com.android.launcher3.nifty.NiftyObservers;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

public class AnimTool {

	private AnimatorSet mSet;
	private long DURATION = 600;

	public AnimatorSet createAnim(final View view) {
		mSet=null;
		if (mSet == null) {
			mSet = new AnimatorSet();
		}
		
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f);
		ObjectAnimator translationY = ObjectAnimator.ofFloat(view,
				"translationY", -6);
		scaleX.setRepeatMode(Animation.REVERSE);
		scaleX.setRepeatCount(Animation.INFINITE);
		scaleY.setRepeatMode(Animation.REVERSE);
		scaleY.setRepeatCount(Animation.INFINITE);
		translationY.setRepeatMode(Animation.REVERSE);
		translationY.setRepeatCount(Animation.INFINITE);
		mSet.playTogether(scaleX);
		mSet.playTogether(scaleY);
		mSet.playTogether(translationY);
		mSet.setDuration(Utilities.getUninstallDuration());
		mSet.setStartDelay(getDelay());
		mSet.removeAllListeners();
		mSet.setInterpolator(new LinearInterpolator());

		mSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator a) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}

			@Override
			public void onAnimationRepeat(Animator a) {

			}

			@Override
			public void onAnimationEnd(Animator a) {

				view.setLayerType(View.LAYER_TYPE_NONE, null);
				if(ios) {
					okIos(view);
				}else {
					ok(view);
				}

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		return mSet;
	}
	
	boolean ios=false;
	
	
	
	public AnimatorSet createAnimIos(final View view) {
		mSet=null;
		if (mSet == null) {
			mSet = new AnimatorSet();
		}
		
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "rotation", 5);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f);
		ObjectAnimator translationY = ObjectAnimator.ofFloat(view,
				"translationY", -6);
		scaleX.setRepeatMode(Animation.REVERSE);
		scaleX.setRepeatCount(Animation.INFINITE);
		scaleY.setRepeatMode(Animation.REVERSE);
		scaleY.setRepeatCount(Animation.INFINITE);
		translationY.setRepeatMode(Animation.REVERSE);
		translationY.setRepeatCount(Animation.INFINITE);
		mSet.playTogether(scaleX);
//		mSet.playTogether(scaleY);
//		mSet.playTogether(translationY);
		mSet.setDuration(Utilities.getUninstallDuration());
		mSet.setStartDelay(getDelay());
		mSet.removeAllListeners();
		mSet.setInterpolator(new LinearInterpolator());

		mSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator a) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}

			@Override
			public void onAnimationRepeat(Animator a) {

			}

			@Override
			public void onAnimationEnd(Animator a) {

				view.setLayerType(View.LAYER_TYPE_NONE, null);
				if(true) {
					okIos(view);
				}else {
					ok(view);
				}

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		return mSet;
	}

	public void ok(final View view) {
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
		ObjectAnimator translationY = ObjectAnimator.ofFloat(view,
				"translationY", 0);
		set.play(scaleX);
		set.play(scaleY);
		set.play(translationY);
		set.setDuration(Utilities.getUninstallDuration());
//		set.setStartDelay(getDelay());
		set.setInterpolator(new LinearInterpolator());
		set.start();
		set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	public void okIos(final View view) {
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "rotation", 0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
		ObjectAnimator translationY = ObjectAnimator.ofFloat(view,
				"translationY", 0);
		set.play(scaleX);
//		set.play(scaleY);
//		set.play(translationY);
		set.setDuration(Utilities.getUninstallDuration());
//		set.setStartDelay(getDelay());
		set.setInterpolator(new LinearInterpolator());
		set.start();
		set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void cacel() {
		if(mSet==null) {
			return;
		}
		ArrayList<Animator> list = mSet.getChildAnimations();
		for (Animator a : list) {
			if (a instanceof ObjectAnimator) {
				ObjectAnimator s = (ObjectAnimator) a;
				/*if(s.getPropertyName().equals("scaleY")) {
					s.setValues(PropertyValuesHolder.ofFloat("scaleX", 1f,1f));
				}else if(s.getPropertyName().equals("scaleX")) {
					s.setValues(PropertyValuesHolder.ofFloat("scaleY", 1f,1f));
				}else if(s.getPropertyName().equals("translationY")) {
					s.setValues(PropertyValuesHolder.ofFloat("translationY", 0,0));
				}*/
				s.setRepeatCount(1);
			}
		}
	}

	private int DEAY = 600;
	public static  CellLayout mLastCellLayout;

	public long getDelay() {
		int progress = (Utilities.random(6));
		int result = progress *50 + DEAY;
		return result;
	}
	
	public void starts(CellLayout cell) {
		cancels(mLastCellLayout);
		if(mLastCellLayout!=cell) {
			mLastCellLayout=cell;
		}
		if(cell == null) {
			return ;
		}
		ShortcutAndWidgetContainer s = cell.getShortcutsAndWidgets();
		for(int i=0;i<s.getChildCount();i++) {
			NiftyObservers child =  (NiftyObservers) s.getChildAt(i);

			StateInfo p = new StateInfo();
			p.state = true;
			if(child.getAinmTool()!=null)
			child.getAinmTool().cacel();
			child.onChanged(p);
		}
	}
	
	
	public void cancelAlls(ShortcutAndWidgetContainer s) {
		if(s == null) {
			return ;
		}
		for(int i=0;i<s.getChildCount();i++) {
			NiftyObservers child =  (NiftyObservers) s.getChildAt(i);
			StateInfo p = new StateInfo();
			p.state = false;
			child.onChanged(p);
		}
	}
	
	public void cancels(CellLayout cell) {
		if(cell == null) {
			return ;
		}
		ShortcutAndWidgetContainer s = cell.getShortcutsAndWidgets();
		for(int i=0;i<s.getChildCount();i++) {
			NiftyObservers child = (NiftyObservers) s.getChildAt(i);

			StateInfo p = new StateInfo();
			p.state = false;
			child.onChanged(p);
		}
	
		
	}

	public void start(View view) {
		AnimatorSet set ;
		if(ios) {
			set = createAnimIos(view);
		}else {
			set = createAnim(view);
		}
		set.start();
	}

}
