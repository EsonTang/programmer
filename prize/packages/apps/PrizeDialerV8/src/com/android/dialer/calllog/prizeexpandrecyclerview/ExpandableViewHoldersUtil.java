package com.android.dialer.calllog.prizeexpandrecyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

public class ExpandableViewHoldersUtil {

    public static void openHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.VISIBLE);
            expandView.setAlpha(1);
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 1);
                    alphaAnimator.addListener(new ViewHolderAnimator.ViewHolderAnimatorListener(holder));
                    alphaAnimator.start();
                }
            });
            animator.start();
        } else {
            expandView.setVisibility(View.VISIBLE);
            expandView.setAlpha(1);
        }
    }

    public static void closeHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.GONE);
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 0);
                    alphaAnimator.addListener(new ViewHolderAnimator.ViewHolderAnimatorListener(holder));
                    alphaAnimator.start();
                }
            });
            animator.start();
        } else {
            expandView.setVisibility(View.GONE);
            expandView.setAlpha(1);
        }
    }
}