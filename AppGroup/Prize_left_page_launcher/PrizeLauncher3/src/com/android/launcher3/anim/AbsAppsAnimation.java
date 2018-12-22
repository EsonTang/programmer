package com.android.launcher3.anim;

import java.util.List;

import com.android.launcher3.DecelerateInterpolator;

import android.animation.Animator;
import android.util.Log;
import android.view.View;

public abstract class AbsAppsAnimation {

	public abstract List<Animator> getAnimators(int duration,
			DecelerateInterpolator in, View toView,boolean isback);

	public abstract void setPreAnimationData(View toView);
	public  void OnAnimationEnd(View toView){
		toView.setRotation(0f);
		toView.setRotationX(0f);
		toView.setRotationY(0f);
		toView.setScaleX(1f);
		toView.setScaleY(1f);
		
	}

}
