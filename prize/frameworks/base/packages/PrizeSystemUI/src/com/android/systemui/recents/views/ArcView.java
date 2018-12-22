package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;

public class ArcView extends ImageView {

    public ArcView(Context context) {
        super(context);
        init();
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private Paint paint;
    
    /*prize modify by xiarui 2017-11-25 start*/
    //private int roundColor = 0x4dffffff;//arc background color
    //private int arcColor = 0xff82bc26;// arc color
    //private float roundWidth = 4;// arc width
    private int roundColor = 0x4cffffff;//arc background color
    private int arcColor = 0xff00ffc6;// arc color
    private float roundWidth = 2;// arc width
    /*prize modify by xiarui 2017-11-25 end*/
    
    private float curProgress = 0.01f;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private ValueAnimator animatior;
    private final int SPACE = 5;

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setStyle(Style.STROKE);

        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) {
            viewWidth = getWidth();
            viewHeight = getHeight();
        }
        if (viewWidth != 0 && viewHeight != 0) {
            paint.setStrokeWidth(roundWidth);
            int center = Math.min(viewWidth, viewHeight) / 2;
            float radius = center - roundWidth / 2 - SPACE;
            RectF oval = new RectF(center - radius, center - radius, center
                    + radius, center + radius);
            drawProgressArc(canvas, oval);
            drawOtherArc(canvas, oval);

        }
    }

    private void drawProgressArc(Canvas canvas, RectF oval) {
        paint.setColor(arcColor);
        canvas.drawArc(oval, -90, 360 * curProgress, false, paint);
    }

    private void drawOtherArc(Canvas canvas, RectF oval) {
        paint.setColor(roundColor);
        canvas.drawArc(oval, 360 * curProgress - 90, 360 - 360 * curProgress,
                false, paint);
    }

    public void switchingNewProgress(final float progress, int duration,
            final Runnable runnable) {
        float curP = curProgress;
        if (animatior != null) {
            animatior.cancel();
        }
        animatior = ValueAnimator.ofFloat(curP, 0, progress);
        animatior.setDuration(duration);
        animatior.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                curProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatior.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatior = null;
                curProgress = progress;
                invalidate();
            }
        });
        animatior.start();
    }

    public void setRoundColor(int roundColor) {
        this.roundColor = roundColor;
        invalidate();
    }

    public void setArcColor(int arcColor) {
        this.arcColor = arcColor;
        invalidate();
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
        invalidate();
    }

    public void setCurProgress(float curProgress) {
        this.curProgress = curProgress;
        invalidate();
    }

    public int getRoundColor() {
        return roundColor;
    }

    public int getArcColor() {
        return arcColor;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public float getCurProgress() {
        return curProgress;
    }

}
