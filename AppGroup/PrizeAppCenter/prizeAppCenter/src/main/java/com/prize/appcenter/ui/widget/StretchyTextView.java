package com.prize.appcenter.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * longbaoxiu
 * 2018/1/29.16:48
 * 可伸展的文本显示布局
 */

public class StretchyTextView extends LinearLayout implements View.OnClickListener {
    //默认显示的最大行数
    private static final int DEFAULT_MAX_LINE_COUNT = 5;
    //当前展开标志显示的状态
    private static final int SPREADTEXT_STATE_NONE = 0;
    private static final int SPREADTEXT_STATE_RETRACT = 1;
    private static final int SPREADTEXT_STATE_SPREAD = 2;

    private TextView contentText;
    //    private TextView operateText;
    private LinearLayout bottomTextLayout;

    private int mState;
    private boolean flag = false;
    private int maxLineCount = DEFAULT_MAX_LINE_COUNT;
    private InnerRunnable runable;
    private TextView app_introduce_id;
    private ImageView expandView;

    public StretchyTextView(Context context) {
        this(context, null);
    }

    public StretchyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = inflate(context, R.layout.stretchy_text_layout, this);
        contentText = (TextView) view.findViewById(R.id.content_textview);
        bottomTextLayout = (LinearLayout) view.findViewById(R.id.bottom_text_layout);
        app_introduce_id = (TextView) view.findViewById(R.id.app_introduce_id);
        expandView = (ImageView) view.findViewById(R.id.expand_view);
        expandView.setOnClickListener(this);
        contentText.setOnClickListener(this);
        bottomTextLayout.setOnClickListener(this);
        runable = new InnerRunnable();
    }

    @Override
    public void onClick(View v) {
        flag = false;
        requestLayout();
    }

    public void setTitleColorDrawables(int color, Drawable drawables) {
        if (app_introduce_id != null) {
            app_introduce_id.setTextColor(color);
            app_introduce_id.setCompoundDrawables(drawables, null, null, null);
        }
    }

    public void setDescripColor(int color) {
        if (contentText != null) {
            contentText.setTextColor(color);
        }

    }

    public void setColorFilter(int color) {
        if (expandView != null) {
            expandView.setColorFilter(color);
        }
    }

    public final void setContentDesc(String charSequence) {

        if (!TextUtils.isEmpty(charSequence)) {
            String contentOne = charSequence.replace("\r\n\r\n", "\r\n");
            String contentTwo = contentOne.replace("\r\n\r\n", "\r\n");
            contentText.setText(contentTwo.trim());
        } else {
            contentText.setText(getResources().getString(
                    R.string.none));
        }
        flag = false;
        mState = SPREADTEXT_STATE_RETRACT;
        requestLayout();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!flag) {
            flag = true;
            if (contentText.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
                mState = SPREADTEXT_STATE_NONE;
                expandView.setVisibility(View.GONE);
                contentText.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
            } else {
                post(runable);
            }
        }
    }

    class InnerRunnable implements Runnable {
        @Override
        public void run() {
            if (mState == SPREADTEXT_STATE_SPREAD) {
                contentText.setMaxLines(maxLineCount);
                expandView.setVisibility(View.VISIBLE);
                expendText(true);
                mState = SPREADTEXT_STATE_RETRACT;
            } else if (mState == SPREADTEXT_STATE_RETRACT) {
                contentText.setMaxLines(Integer.MAX_VALUE);
                expandView.setVisibility(View.VISIBLE);
                mState = SPREADTEXT_STATE_SPREAD;
                expendText(false);
            }
        }
    }

    public void setMaxLineCount(int maxLineCount) {
        this.maxLineCount = maxLineCount;
    }

    public void setContentTextColor(int color) {
        this.contentText.setTextColor(color);
    }

    public void setContentTextSize(float size) {
        this.contentText.setTextSize(size);
    }

    /**
     * 内容字体加粗
     */
    public void setContentTextBold() {
        TextPaint textPaint = contentText.getPaint();
        textPaint.setFakeBoldText(true);
    }

    /**
     * 设置展开标识的显示位置
     *
     * @param gravity int
     */
    public void setBottomTextGravity(int gravity) {
        bottomTextLayout.setGravity(gravity);
    }

    public void expendText(boolean isExpand) {
        contentText.clearAnimation();
        final int deltaValue;
        final int startValue = contentText.getHeight();
        int durationMillis = 350;
        if (isExpand) {
            deltaValue = contentText.getLineHeight()
                    * contentText.getLineCount() - startValue;
            RotateAnimation animation = new RotateAnimation(0, 180,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        } else {
            deltaValue = contentText.getLineHeight() * maxLineCount
                    - startValue;
            RotateAnimation animation = new RotateAnimation(180, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        }
        Animation animation = new Animation() {
            protected void applyTransformation(float interpolatedTime,
                                               Transformation t) {
                contentText.setHeight((int) (startValue + deltaValue
                        * interpolatedTime));
            }
        };
        animation.setDuration(durationMillis);
        contentText.startAnimation(animation);
    }
}
