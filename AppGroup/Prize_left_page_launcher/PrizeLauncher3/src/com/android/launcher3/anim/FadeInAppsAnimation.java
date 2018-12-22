package com.android.launcher3.anim;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import com.android.launcher3.DecelerateInterpolator;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.LauncherViewPropertyAnimator;
import com.android.launcher3.R;

public class FadeInAppsAnimation extends AbsAppsAnimation {
	List<Animator> mlist = new ArrayList<Animator>();

	@Override
	public List<Animator> getAnimators(int duration, DecelerateInterpolator in,
			View toView,boolean isback) {
		final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(
				toView);
		scaleAnim.scaleX(1f).scaleY(1f).// 缩放大小
				setDuration(duration).// 时间
				setInterpolator(in);// 速度变化
		// toView 的动画 设置
		mlist.add(scaleAnim);
		return mlist;
	}

	@Override
	public void setPreAnimationData(View toView) {
		final float scale = (float) toView.getResources().getInteger(
				R.integer.config_appsCustomizeZoomScaleFactor); // 比例
		toView.setScaleX(scale);
		toView.setScaleY(scale);
	}

}
