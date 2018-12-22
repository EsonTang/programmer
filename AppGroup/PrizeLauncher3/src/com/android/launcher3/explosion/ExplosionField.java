/*
 * Copyright (C) 2015 tyrantgit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.explosion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ImageUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;


public class ExplosionField extends View {

    private List<ExplosionAnimator> mExplosions = new ArrayList<>();
    private int[] mExpandInset = new int[2];

    public ExplosionField(Context context) {
        super(context);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplosionField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Arrays.fill(mExpandInset, Utils.dp2Px(32));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ExplosionAnimator explosion : mExplosions) {
            explosion.draw(canvas);
        }
    }

    public void expandExplosionBound(int dx, int dy) {
        mExpandInset[0] = dx;
        mExpandInset[1] = dy;
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration,final Runnable r) {
        final ExplosionAnimator explosion = new ExplosionAnimator(this, bitmap, bound);
        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mExplosions.remove(animation);
                if (r != null) {
                    r.run();
                }
            }
        });
        explosion.setStartDelay(startDelay);
        explosion.setDuration(duration);
        mExplosions.add(explosion);
        explosion.start();
    }

    public void explode(final View view,Runnable run) {
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);

            }
        });
        animator.start();
        animator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
        view.animate().setDuration(850).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        if(view instanceof BubbleTextView) {
            explode(ImageUtils.drawableToBitmap1(((BubbleTextView)view).getCompoundDrawables()[1]), r, startDelay, ExplosionAnimator.DEFAULT_DURATION,run);
        }
    }

    public void clear() {
        mExplosions.clear();
        invalidate();
    }

    public static ExplosionField attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ExplosionField explosionField = new ExplosionField(activity);
        rootView.addView(explosionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        explosionField.bringToFront();
        return explosionField;
    }

}
