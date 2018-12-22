package com.android.dialer.calllog.prizeexpandrecyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class LayoutAnimator {

    public static class LayoutHeightUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final View mView;

        public LayoutHeightUpdateListener(View view) {
            mView = view;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();
            lp.height = (int) animation.getAnimatedValue();
            mView.setLayoutParams(lp);
        }

    }

    public static Animator ofHeight(View view, int start, int end) {
        final ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new LayoutHeightUpdateListener(view));
        return animator;
    }

}