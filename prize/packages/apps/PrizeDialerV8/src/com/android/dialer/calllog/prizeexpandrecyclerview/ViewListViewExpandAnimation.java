package com.android.dialer.calllog.prizeexpandrecyclerview;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout.LayoutParams;

public class ViewListViewExpandAnimation extends Animation {

    private View mAnimationView = null;
    private LayoutParams mViewLayoutParams = null;
    private int mStart = 0;
    private int mEnd = 0;

    public ViewListViewExpandAnimation(View view, boolean expand) {
        animationSettings(view, 100, expand);
    }

    public ViewListViewExpandAnimation(View view, int duration, boolean expand) {
        animationSettings(view, duration, expand);
    }

    private void animationSettings(View view, int duration, boolean expand) {
        setDuration(duration);
        mAnimationView = view;
        mViewLayoutParams = (LayoutParams) view.getLayoutParams();
        mStart = mViewLayoutParams.bottomMargin;
        //mEnd = (mStart == 0 ? (0 - view.getHeight()) : 0);
        if (expand) {
            mEnd = 0;
        } else {
            mEnd = 0 - view.getHeight();
        }
        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if (interpolatedTime < 1.0f) {
            mViewLayoutParams.bottomMargin = mStart + (int) ((mEnd - mStart) * interpolatedTime);
            //Log.d("PrizeExpandView", "reverseTransformation mStart : " + mStart + ",  mEnd : " + mEnd + ",  mViewLayoutParams.bottomMargin : " + mViewLayoutParams.bottomMargin);
            // invalidate
            mAnimationView.requestLayout();
        } else {
            mViewLayoutParams.bottomMargin = mEnd;
            mAnimationView.requestLayout();
            if (mEnd != 0) {
                mAnimationView.setVisibility(View.GONE);
            }
        }
    }

}
