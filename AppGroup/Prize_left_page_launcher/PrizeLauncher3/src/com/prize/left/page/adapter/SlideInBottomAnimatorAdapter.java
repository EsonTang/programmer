package com.prize.left.page.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * An implementation of the AnimatorAdapter class which applies a
 * swing-in-from-bottom-animation to views.
 *
 * @Author Gabriele Mariotti
 */
public class SlideInBottomAnimatorAdapter<T extends RecyclerView.ViewHolder> extends AnimatorAdapter<T> {

    private static final String TRANSLATION_Y = "translationY";

    public SlideInBottomAnimatorAdapter(RecyclerView.Adapter<T> adapter,
                                        RecyclerView recyclerView) {
        super(adapter, recyclerView);
    }

    @NonNull
    @Override
    public Animator[] getAnimators(@NonNull View view) {
        // return new Animator[]{ObjectAnimator.ofFloat(view, TRANSLATION_Y, mRecyclerView.getMeasuredHeight() >> 1, 0)};
        return new Animator[]{ObjectAnimator.ofFloat(view, TRANSLATION_Y, -view.getMeasuredHeight(), 0)};
    }
}