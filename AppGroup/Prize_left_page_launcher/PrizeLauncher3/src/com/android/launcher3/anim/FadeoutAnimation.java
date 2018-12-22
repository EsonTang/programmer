package com.android.launcher3.anim;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.DecelerateInterpolator;
import com.android.launcher3.LauncherAnimUtils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class FadeoutAnimation extends AbsAppsAnimation {

	List<Animator> mlist = new ArrayList<Animator>();

	@Override
	public List<Animator> getAnimators(int duration, DecelerateInterpolator in,
			View toView,boolean isback) {
		final ObjectAnimator rotationAnim = LauncherAnimUtils.ofFloat(toView,
				"rotation", 0f, 720f).setDuration(duration);
		rotationAnim.setInterpolator(in);
		final ObjectAnimator scaleX = LauncherAnimUtils.ofFloat(toView,
				"scaleX", 0f, 1f).setDuration(duration);
		final ObjectAnimator scaleY = LauncherAnimUtils.ofFloat(toView,
				"scaleY", 0f, 1f).setDuration(duration);
		rotationAnim.setInterpolator(in);
		mlist.add(scaleX);
		mlist.add(scaleY);
		return mlist;
	}

	@Override
	public void setPreAnimationData(View toView) {

	}

}
