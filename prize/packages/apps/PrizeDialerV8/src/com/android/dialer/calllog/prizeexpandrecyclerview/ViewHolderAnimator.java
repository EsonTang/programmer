package com.android.dialer.calllog.prizeexpandrecyclerview;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.lang.IllegalStateException;

public class ViewHolderAnimator {

    public static Animator ofItemViewHeight(RecyclerView.ViewHolder holder) {
        View parent = (View) holder.itemView.getParent();
        if (parent == null)
            throw new IllegalStateException("Cannot animate the layout of a view that has no parent");

        int start = holder.itemView.getMeasuredHeight();
        holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int end = holder.itemView.getMeasuredHeight();
        final Animator animator = LayoutAnimator.ofHeight(holder.itemView, start, end);
        animator.addListener(new ViewHolderAnimatorListener(holder));
        animator.addListener(new LayoutParamsAnimatorListener(holder.itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return animator;
    }

    public static class ViewHolderAnimatorListener implements Animator.AnimatorListener {

        private RecyclerView.ViewHolder mHolder;

        public ViewHolderAnimatorListener(RecyclerView.ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mHolder.setIsRecyclable(false);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mHolder.setIsRecyclable(true);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mHolder.setIsRecyclable(true);
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    public static class LayoutParamsAnimatorListener extends AnimatorListenerAdapter {
        private final View mView;
        private final int mParamsWidth;
        private final int mParamsHeight;

        public LayoutParamsAnimatorListener(View view, int paramsWidth, int paramsHeight) {
            mView = view;
            mParamsWidth = paramsWidth;
            mParamsHeight = paramsHeight;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            final ViewGroup.LayoutParams params = mView.getLayoutParams();
            params.width = mParamsWidth;
            params.height = mParamsHeight;
            mView.setLayoutParams(params);
        }
    }
}