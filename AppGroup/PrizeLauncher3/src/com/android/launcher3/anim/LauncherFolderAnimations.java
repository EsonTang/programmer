package com.android.launcher3.anim;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class LauncherFolderAnimations extends TransitionAnims {

	public void play(View view, float scaleFrom[], float scaleTo[], float[] from,
			float[] to, float alpha) {
		this.ofFloat(view, "scaleX", scaleFrom[0], scaleTo[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "scaleY", scaleFrom[1], scaleTo[1]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "translationX", from[0], to[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "translationY", from[1], to[1]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "alpha", alpha).setInterpolator(new AccelerateInterpolator());
	}
	


	public void play(View view, int[] from,
			int[] to, float alpha) {
		this.ofFloat(view, "translationX", from[0], to[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "translationY", from[1], to[1]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "alpha", alpha).setInterpolator(new AccelerateInterpolator());
	}


	public LauncherFolderAnimations(Interpolator mZoomInInterpolator) {
		super(mZoomInInterpolator);
	}
	
	public void play(View view, float scaleFrom[], float scaleTo[], int[] from,
			int[] to) {
		this.ofFloat(view, "scaleX", scaleFrom[0], scaleTo[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "scaleY", scaleFrom[1], scaleTo[1]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "translationX", from[0], to[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "translationY", from[1], to[1]).setInterpolator(mTimeInterpolator);
	}

	public void play(View view, float scaleFrom[], float scaleTo[]) {
		this.ofFloat(view, "scaleX", scaleFrom[0], scaleTo[0]).setInterpolator(mTimeInterpolator);
		this.ofFloat(view, "scaleY", scaleFrom[1], scaleTo[1]).setInterpolator(mTimeInterpolator);
	}
}
