package com.android.launcher3;

import java.util.ArrayList;
import java.util.Collection;

import com.android.gallery3d.util.LogUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

public class ShowList {
	public static int DELAY = 600;
	private static ShowList mInstance;

	public static ShowList getInstance() {
		if (mInstance == null) {
			mInstance = new ShowList();
		}
		return mInstance;
	}
	private OnPauseListener mOnPauseListener;
	public interface OnPauseListener {
		void pause();
		void resume();
	}
	
	public void setOnPauseListener(OnPauseListener l) {
		mOnPauseListener = l;
		
	}

	private ShowList() {

	}

	public Collection<Animator> createAnimation(final View view,
			final int startPos, final int duration) {
		Collection<Animator> Objs = new ArrayList<Animator>();
		ObjectAnimator trans;
		ObjectAnimator alpha;
		view.setTranslationY(startPos);
		view.setAlpha(0f);
		view.setScaleX(0.5f);
		view.setScaleY(0.5f);
		trans = ObjectAnimator.ofFloat(view, "translationY", startPos, 0);
		alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1f);
		ObjectAnimator scaleX = ObjectAnimator
				.ofFloat(view, "scaleX", 0.5f, 1f);
		ObjectAnimator scaleY = ObjectAnimator
				.ofFloat(view, "scaleY", 0.5f, 1f);
		alpha.setDuration(duration * 2);
		alpha.setStartDelay(100);
		view.setScaleX(1f);
		view.setScaleY(1f);
		view.setAlpha(0f);
		scaleY.setDuration(duration);
		scaleX.setDuration(duration);
		trans.setDuration(duration);
		trans.setStartDelay(100);
		scaleX.setStartDelay(100);
		scaleY.setStartDelay(100);
		if(view.getAnimation()!=null)
		view.getAnimation().cancel();
		alpha.setInterpolator(new OvershootInterpolator(0.5f));
		trans.setInterpolator(new OvershootInterpolator(0.5f));
		scaleX.setInterpolator(new OvershootInterpolator(0.5f));
		scaleY.setInterpolator(new OvershootInterpolator(0.5f));
		alpha.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				try {
					if(mOnPauseListener!=null) {
						mOnPauseListener.pause();
					}
					view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
					View v = (View) view.getParent().getParent();
					if (v instanceof PagedView) {
						v.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
				try {
					if(mOnPauseListener!=null) {
						mOnPauseListener.resume();
					}
					View v = (View) view.getParent().getParent();
					if (v instanceof PagedView) {
						ViewGroup vp =(ViewGroup) v.getParent();
						if(vp.indexOfChild(v) != vp.getChildCount()-1) {
							
//							v.setVisibility(View.GONE);
						}else {
							v.setVisibility(View.VISIBLE);
						}
						if(v instanceof PrizeGroupScrollView) {

							v.setVisibility(View.VISIBLE);
							v.requestLayout();
							
						}
//						v.bringToFront()
						LogUtils.i("zhouerlong", "sdf");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		Objs.add(alpha);
		Objs.add(trans);
		Objs.add(scaleX);
		Objs.add(scaleY);
		return Objs;

	}

	public Collection<Animator> backAnimation(final View view, int duration) {

		Collection<Animator> Objs = new ArrayList<Animator>();
		ObjectAnimator scaleX = ObjectAnimator
				.ofFloat(view, "scaleX", 1f, 0.5f);
		ObjectAnimator scaleY = ObjectAnimator
				.ofFloat(view, "scaleY", 1f, 0.5f);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0f);
		alpha.setDuration(duration);
		scaleX.setDuration(duration);
		scaleY.setDuration(duration);
		if(view.getAnimation()!=null)
		view.getAnimation().cancel();
		alpha.setInterpolator(new DecelerateInterpolator());
		scaleX.setInterpolator(new DecelerateInterpolator());
		scaleY.setInterpolator(new DecelerateInterpolator());
		alpha.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator anim) {
				View v = (View) view.getParent().getParent();
				if (v instanceof PagedView) {
					v.setVisibility(View.GONE);
				}
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);

			}
		});
		Objs.add(alpha);
		Objs.add(scaleX);
		Objs.add(scaleY);
		return Objs;

	}

	public void play() {
	}
	public void start(final ViewGroup view, boolean showOrback) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(add(view, showOrback));
		set.start();
	}

	public Collection<Animator> add(final ViewGroup c, boolean showOrback) {

		if (c == null) {
			return null;
		}
		Collection<Animator> mObjs = new ArrayList<Animator>();
		for (int i = 0; i < c.getChildCount(); i++) {
			View child = c.getChildAt(i);
			if (showOrback) {
				Collection<Animator> objs = createAnimation(child,
						child.getWidth(), getDelay(child));

				mObjs.addAll(objs);
			} else {
				Collection<Animator> objs = backAnimation(child, 600);
				mObjs.addAll(objs);

			}
		}
		return mObjs;
	}

	public int getDelay(View child) {
		ViewGroup vg = (ViewGroup) child.getParent();
		int i = vg.indexOfChild(child);
		int d = i * 150 + Utilities.getShowListEditDuration();

		return d;
	}
}
