/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.Stack;

import com.prize.prizelockscreen.R;

/**
 * A View similar to a textView which contains password text and can animate when the text is
 * changed
 */
public class NewPasswordTextView extends View {

    private static final float DOT_OVERSHOOT_FACTOR = 1.0f;
    private static final long DOT_APPEAR_DURATION_OVERSHOOT = 320;
    private static final long APPEAR_DURATION = 160;
    private static final long DISAPPEAR_DURATION = 160;
    private static final long RESET_DELAY_PER_ELEMENT = 40;
    private static final long RESET_MAX_DELAY = 200;

    /**
     * The overlap between the text disappearing and the dot appearing animation
     */
    private static final long DOT_APPEAR_TEXT_DISAPPEAR_OVERLAP_DURATION = 130;

    /**
     * The duration the text should be visible, starting with the appear animation
     */
    private static final long TEXT_VISIBILITY_DURATION = 1300;

    /**
     * The position in time from [0,1] where the overshoot should be finished and the settle back
     * animation of the dot should start
     */
    private static final float OVERSHOOT_TIME_POSITION = 0.5f;

    /**
     * The raw text size, will be multiplied by the scaled density when drawn
     */
    private final int mTextHeightRaw;
    private ArrayList<CharState> mTextChars = new ArrayList<>();
    private String mText = "";
    private Stack<CharState> mCharPool = new Stack<>();
    private int mDotSize;
    private int mCharPadding;
    private final Paint mDrawPaint = new Paint();
    private final Paint mBigPaint = new Paint();
    private Interpolator mAppearInterpolator;
    private Interpolator mDisappearInterpolator;
    private boolean mShowPassword;
    
    private Context mContext;
    
    private OnContentChange mChangeLsn;
    /**触发事件的字符长度*/
    private int mTriggerLen = 4;
    
    private int mDotPading = 0;
    
    private int mCircleRadius = 10;
    
    private int mCircleBigRadius = 30;

    public NewPasswordTextView(Context context) {
        this(context, null);
    }

    public NewPasswordTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewPasswordTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NewPasswordTextView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PasswordTextView);
        try {
            mTextHeightRaw = a.getInt(R.styleable.PasswordTextView_scaledTextSize, 0);
        } finally {
            a.recycle();
        }
        
        mDrawPaint.setFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        
        mDrawPaint.setTextAlign(Paint.Align.CENTER);
        
        mDrawPaint.setColor(0xffffffff);
        
        mBigPaint.setFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mBigPaint.setTextAlign(Paint.Align.CENTER);
        mBigPaint.setColor(getContext().getResources().getColor(R.color.white_w_a_90));
        
        mDrawPaint.setTypeface(Typeface.create("sans-serif-light", 0));
        mDotSize = getContext().getResources().getDimensionPixelSize(R.dimen.password_dot_size);
        mCharPadding = getContext().getResources().getDimensionPixelSize(R.dimen
                .password_char_padding);
        mShowPassword = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.TEXT_SHOW_PASSWORD, 1) == 1;
        mAppearInterpolator = AnimationUtils.loadInterpolator(mContext,
                android.R.interpolator.linear_out_slow_in);
        mDisappearInterpolator = AnimationUtils.loadInterpolator(mContext,
                android.R.interpolator.fast_out_linear_in);
        
        mDotPading = getContext().getResources().getDimensionPixelSize(R.dimen.dot_pading);
        
        mCircleRadius = getContext().getResources().getDimensionPixelSize(R.dimen.circle_radius);
        
        mCircleBigRadius = getContext().getResources().getDimensionPixelSize(R.dimen.circle_big_radius);
    }
    
    public void setOnChangeListener(OnContentChange l) {
    	mChangeLsn = l;
    }
    /***
     * 设置触发长度
     * @param len
     */
    public void setTriggerLen(int len) {
    	if (len < 1)
    		return;
    	mTriggerLen = len;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        /*float totalDrawingWidth = getDrawingWidth();
        float currentDrawPosition = getWidth() / 2 - totalDrawingWidth / 2;
        int length = mTextChars.size();
        Rect bounds = getCharBounds();
        int charHeight = (bounds.bottom - bounds.top);
        float yPosition = getHeight() / 2;
        float charLength = bounds.right - bounds.left;
        for (int i = 0; i < length; i++) {
            CharState charState = mTextChars.get(i);
            float charWidth = charState.draw(canvas, currentDrawPosition, charHeight, yPosition,
                    charLength);
            currentDrawPosition += charWidth;
        }*/
    	
    	float totalDrawingWidth = getCircleWidth();
        float x = getWidth() / 2 - totalDrawingWidth / 2;
        int length = mTextChars.size();
        float y = getHeight() / 2;
        for (int i = 0; i < mTriggerLen; i++) {
        	CharState charState = null;
        	float x1 = x + (i * mCircleBigRadius * 2 + i * mDotPading);
        	if (i < length) {
        		charState = mTextChars.get(i);
            	charState.draw(canvas, x1, y, true);
        	}
        	else {
        		charState = obtainCharState('c');
        		charState.draw(canvas, x1, y, false);
        	}
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    private Rect getCharBounds() {
        float textHeight = mTextHeightRaw * getResources().getDisplayMetrics().scaledDensity;
        mDrawPaint.setTextSize(textHeight);
        Rect bounds = new Rect();
        mDrawPaint.getTextBounds("0", 0, 1, bounds);
        return bounds;
    }

    private float getDrawingWidth() {
        int width = 0;
        int length = mTextChars.size();
        Rect bounds = getCharBounds();
        int charLength = bounds.right - bounds.left;
        for (int i = 0; i < length; i++) {
            CharState charState = mTextChars.get(i);
            if (i != 0) {
                width += mCharPadding * charState.currentWidthFactor;
            }
            width += charLength * charState.currentWidthFactor;
        }
        return width;
    }
    /***
     * 获取总共的圆的宽度
     * @return
     */
    private int getCircleWidth() {
        int width = mTriggerLen * mCircleBigRadius * 2 + mDotPading * (mTriggerLen - 1);
        return width;
    }


    public void append(char c) {
        int visibleChars = mTextChars.size();
        if (visibleChars >= mTriggerLen) {
        	return;
        }
        String textbefore = mText;
        mText = mText + c;
        int newLength = mText.length();
        CharState charState;
        if (newLength > visibleChars) {
            charState = obtainCharState(c);
            mTextChars.add(charState);
        } else {
            charState = mTextChars.get(newLength - 1);
            charState.whichChar = c;
        }
        charState.startAppearAnimation();

        // ensure that the previous element is being swapped
        if (newLength > 1) {
            CharState previousState = mTextChars.get(newLength - 2);
            if (previousState.isDotSwapPending) {
                previousState.swapToDotWhenAppearFinished();
            }
        }
        sendAccessibilityEventTypeViewTextChanged(textbefore, textbefore.length(), 0, 1);
        
        if (newLength >= mTriggerLen) {
        	if (mChangeLsn != null) {
        		mChangeLsn.onChange(mText);
        	}
        }
    }

    public void deleteLastChar() {
        int length = mText.length();
        String textbefore = mText;
        if (length > 0) {
            mText = mText.substring(0, length - 1);
            CharState charState = mTextChars.get(length - 1);
            charState.startRemoveAnimation(0, 0);
        }
        sendAccessibilityEventTypeViewTextChanged(textbefore, textbefore.length() - 1, 1, 0);
    }

    public String getText() {
        return mText;
    }

    private CharState obtainCharState(char c) {
        CharState charState;
        if(mCharPool.isEmpty()) {
            charState = new CharState();
        } else {
            charState = mCharPool.pop();
            charState.reset();
        }
        charState.whichChar = c;
        return charState;
    }

    public void reset(boolean animated) {
        Log.d("PasswordTextView", "reset() is called, set PwEntry true.") ;
        setEnabled(true);

        String textbefore = mText;

        mText = "";
        int length = mTextChars.size();
        int middleIndex = (length - 1) / 2;
        long delayPerElement = RESET_DELAY_PER_ELEMENT;
        for (int i = 0; i < length; i++) {
            CharState charState = mTextChars.get(i);
            if (animated) {
                int delayIndex;
                if (i <= middleIndex) {
                    delayIndex = i * 2;
                } else {
                    int distToMiddle = i - middleIndex;
                    delayIndex = (length - 1) - (distToMiddle - 1) * 2;
                }
                long startDelay = delayIndex * delayPerElement;
                startDelay = Math.min(startDelay, RESET_MAX_DELAY);
                long maxDelay = delayPerElement * (length - 1);
                maxDelay = Math.min(maxDelay, RESET_MAX_DELAY) + DISAPPEAR_DURATION;
                charState.startRemoveAnimation(startDelay, maxDelay);
                charState.removeDotSwapCallbacks();
            } else {
                mCharPool.push(charState);
            }
        }
        if (!animated) {
            mTextChars.clear();
        }
        sendAccessibilityEventTypeViewTextChanged(textbefore, 0, textbefore.length(), 0);
    }

    void sendAccessibilityEventTypeViewTextChanged(String beforeText, int fromIndex,
                                                   int removedCount, int addedCount) {
    	AccessibilityManager manager = (AccessibilityManager) mContext
    	        .getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager.isEnabled() && (isFocused() || isSelected() && isShown())) {
            beforeText = null;
            AccessibilityEvent event =
                    AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
            event.setFromIndex(fromIndex);
            event.setRemovedCount(removedCount);
            event.setAddedCount(addedCount);
            event.setBeforeText(beforeText);
            event.setPassword(true);
            sendAccessibilityEventUnchecked(event);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);

        event.setClassName(NewPasswordTextView.class.getName());
        event.setPassword(true);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);

        info.setClassName(NewPasswordTextView.class.getName());
        info.setPassword(true);

        info.setEditable(true);

        info.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    private class CharState {
        char whichChar;
        Animator dotAnimator;
        boolean dotAnimationIsGrowing;
        ValueAnimator widthAnimator;
        float currentDotSizeFactor;
        float currentWidthFactor;
        boolean isDotSwapPending;

        Animator.AnimatorListener dotFinishListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dotAnimator = null;
            }
        };

        Animator.AnimatorListener removeEndListener = new AnimatorListenerAdapter() {
            private boolean mCancelled;
            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mCancelled) {
                    mTextChars.remove(CharState.this);
                    mCharPool.push(CharState.this);
                    reset();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mCancelled = false;
            }
        };
        
        private ValueAnimator.AnimatorUpdateListener dotSizeUpdater
                = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentDotSizeFactor = (float) animation.getAnimatedValue();
                invalidate();
            }
        };

        private Runnable dotSwapperRunnable = new Runnable() {
            @Override
            public void run() {
                performSwap();
                isDotSwapPending = false;
            }
        };

        void reset() {
            whichChar = 0;
            currentDotSizeFactor = 0.0f;
            cancelAnimator(dotAnimator);
            dotAnimator = null;
            cancelAnimator(widthAnimator);
            widthAnimator = null;
            removeDotSwapCallbacks();
        }

        void startRemoveAnimation(long startDelay, long widthDelay) {
            boolean dotNeedsAnimation = (currentDotSizeFactor > 0.0f && dotAnimator == null)
                    || (dotAnimator != null && dotAnimationIsGrowing);
            if (dotNeedsAnimation) {
                startDotDisappearAnimation(startDelay);
            }
        }

        void startAppearAnimation() {
        	startDotAppearAnimation(0);
            /*boolean dotNeedsAnimation = !mShowPassword
                    && (dotAnimator == null || !dotAnimationIsGrowing);
            if (dotNeedsAnimation) {
                startDotAppearAnimation(0);
            }
            if (mShowPassword) {
                postDotSwap(TEXT_VISIBILITY_DURATION);
            }*/
        }

        /**
         * Posts a runnable which ensures that the text will be replaced by a dot after {@link
         * com.android.keyguard.PasswordTextView#TEXT_VISIBILITY_DURATION}.
         */
        private void postDotSwap(long delay) {
            removeDotSwapCallbacks();
            postDelayed(dotSwapperRunnable, delay);
            isDotSwapPending = true;
        }

        private void removeDotSwapCallbacks() {
            removeCallbacks(dotSwapperRunnable);
            isDotSwapPending = false;
        }

        void swapToDotWhenAppearFinished() {
            removeDotSwapCallbacks();
            performSwap();
        }

        private void performSwap() {
            startDotAppearAnimation(DISAPPEAR_DURATION
                    - DOT_APPEAR_TEXT_DISAPPEAR_OVERLAP_DURATION);
        }

        private void startDotDisappearAnimation(long startDelay) {
            cancelAnimator(dotAnimator);
            ValueAnimator animator = ValueAnimator.ofFloat(currentDotSizeFactor, 0.0f);
            animator.addUpdateListener(dotSizeUpdater);
            animator.addListener(dotFinishListener);
            animator.addListener(removeEndListener);
            animator.setInterpolator(mDisappearInterpolator);
            long duration = (long) (DISAPPEAR_DURATION * Math.min(currentDotSizeFactor, 1.0f));
            animator.setDuration(duration);
            animator.setStartDelay(startDelay);
            animator.start();
            dotAnimator = animator;
            dotAnimationIsGrowing = false;
        }

        private void startDotAppearAnimation(long delay) {
            cancelAnimator(dotAnimator);
            if (!mShowPassword) {
                // We perform an overshoot animation
                ValueAnimator overShootAnimator = ValueAnimator.ofFloat(currentDotSizeFactor,
                        DOT_OVERSHOOT_FACTOR);
                overShootAnimator.addUpdateListener(dotSizeUpdater);
                overShootAnimator.setInterpolator(mAppearInterpolator);
                long overShootDuration = (long) (DOT_APPEAR_DURATION_OVERSHOOT
                        * OVERSHOOT_TIME_POSITION);
                overShootAnimator.setDuration(overShootDuration);
                ValueAnimator settleBackAnimator = ValueAnimator.ofFloat(DOT_OVERSHOOT_FACTOR,
                        1.0f);
                settleBackAnimator.addUpdateListener(dotSizeUpdater);
                settleBackAnimator.setDuration(DOT_APPEAR_DURATION_OVERSHOOT - overShootDuration);
                settleBackAnimator.addListener(dotFinishListener);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(overShootAnimator, settleBackAnimator);
                animatorSet.setStartDelay(delay);
                animatorSet.start();
                dotAnimator = animatorSet;
            } else {
                ValueAnimator growAnimator = ValueAnimator.ofFloat(currentDotSizeFactor, 1.0f);
                growAnimator.addUpdateListener(dotSizeUpdater);
                growAnimator.setDuration((long) (APPEAR_DURATION * (1.0f - currentDotSizeFactor)));
                growAnimator.addListener(dotFinishListener);
                growAnimator.setStartDelay(delay);
                growAnimator.start();
                dotAnimator = growAnimator;
            }
            dotAnimationIsGrowing = true;
        }

        private void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        /**
         * Draw this char to the canvas.
         *
         * @return The width this character contributes, including padding.
         */
        public float draw(Canvas canvas, float currentDrawPosition, int charHeight, float yPosition,
                float charLength) {
            boolean dotVisible = currentDotSizeFactor > 0;
            float charWidth = charLength * currentWidthFactor;
            if (dotVisible) {
                canvas.save();
                float centerX = currentDrawPosition + charWidth / 2;
                canvas.translate(centerX, yPosition);
                canvas.drawCircle(0, 0, mDotSize / 2, mDrawPaint);
                canvas.restore();
            }
            return charWidth + mCharPadding * currentWidthFactor;
        }
        
        /**
         * draw circle
         * @param canvas
         * @param x
         * @param y
         * @param isFill
         */
        public void draw(Canvas canvas, float x, float y,
                boolean isFill) {
        	
            boolean dotVisible = currentDotSizeFactor > 0;
            int circleWidth = mCircleRadius;
            
            if (isFill) {
            	canvas.save();
                canvas.translate(x + mCircleBigRadius, y);
                canvas.drawCircle(0, 0, circleWidth, mDrawPaint);
                canvas.restore();
            	if (dotVisible) {
	            	circleWidth = mCircleBigRadius;
	                canvas.save();
	                canvas.translate(x + circleWidth, y);
	                canvas.drawCircle(0, 0, mCircleBigRadius * currentDotSizeFactor, mBigPaint);
	                canvas.restore();
            	}
            }
            else {
            	canvas.save();
                canvas.translate(x + mCircleBigRadius, y);
                canvas.drawCircle(0, 0, circleWidth, mBigPaint);
                canvas.restore();
            }
        }
    }
    /***
     * 当输入内容发生变化时
     * @author fanjunchen
     *
     */
    public interface OnContentChange {
    	void onChange(String str);
    }
}
