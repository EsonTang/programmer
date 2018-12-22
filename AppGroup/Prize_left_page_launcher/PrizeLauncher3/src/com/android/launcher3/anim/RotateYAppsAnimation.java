package com.android.launcher3.anim;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.DecelerateInterpolator;
import com.android.launcher3.LauncherAnimUtils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class RotateYAppsAnimation extends AbsAppsAnimation {

	List<Animator> mlist = new ArrayList<Animator>();

	@Override
	public List<Animator> getAnimators(int duration, DecelerateInterpolator in,
			View toView,boolean isback) {
		final ObjectAnimator rotationAnim = LauncherAnimUtils.ofFloat(toView,
				"rotationY", 0f, 360f).setDuration(duration);
		rotationAnim.setInterpolator(in);
		mlist.add(rotationAnim);
		return mlist;
	}

	@Override
	public void setPreAnimationData(View toView) {

	}

}
