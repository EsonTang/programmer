package com.prize.appcenter.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.ButtonController;
import com.prize.appcenter.ui.widget.progressbutton.DefaultButtonController;

import java.io.File;

import static com.prize.app.download.AppManagerCenter.APP_STATE_DOWNLOADING;
import static com.prize.appcenter.R.id.item;

/*
 * Created by longbaoxiu on 10/11/17.
 */
public class PrizeCommButton extends TextView {
    private final static String TAG = "PrizeCommButton";

    private Context mContext;

    //背景画笔
    private Paint mBackgroundPaint;
    //描边画笔
    private Paint mStokePaint;
    //按钮文字画笔
    private volatile Paint mTextPaint;
    //第一个点画笔
    private Paint mDot1Paint;
    //第二个点画笔
    private Paint mDot2Paint;


    //背景颜色
    private int[] mBackgroundColor;
    private int[] mOriginBackgroundColor;
    //文字颜色
    private int mTextColor;
    //文字大小
    private float mAboveTextSize = 50;

    private float mButtonRadius;


    private RectF mBackgroundBounds;
    private LinearGradient mFillBgGradient;
    private LinearGradient mProgressBgGradient;
    private LinearGradient mProgressTextGradient;
    //记录当前文字
    private CharSequence mCurrentText;

    //普通状态
    public static final int NORMAL = 0;

    private ButtonController mDefaultController;

    private float borderWidth = 2.0f;

    public PrizeCommButton(Context context) {
        this(context, null);

    }

    public PrizeCommButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            mContext = context;
            initController();
            initAttrs(context, attrs);
            init();
//            setupAnimations();
        } else {
            initController();
        }

    }

    private void initController() {
        mDefaultController = new DefaultButtonController();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        ButtonController buttonController = switchController();
        if (buttonController.enablePress()) {
            if (mOriginBackgroundColor == null) {
                mOriginBackgroundColor = new int[2];
                mOriginBackgroundColor[0] = mBackgroundColor[0];
                mOriginBackgroundColor[1] = mBackgroundColor[1];
            }
            if (this.isPressed()) {
                int pressColorleft = buttonController.getPressedColor(mBackgroundColor[0]);
                int pressColorright = buttonController.getPressedColor(mBackgroundColor[1]);
                if (buttonController.enableGradient()) {
                    initGradientColor(pressColorleft, pressColorright);
                } else {
                    initGradientColor(pressColorleft, pressColorleft);
                }
            } else {
                if (buttonController.enableGradient()) {
                    initGradientColor(mOriginBackgroundColor[0], mOriginBackgroundColor[1]);
                } else {
                    initGradientColor(mOriginBackgroundColor[0], mOriginBackgroundColor[0]);
                }
            }
            invalidate();
        }

    }

    private void initAttrs(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PrizeCommButton);
        int bgColor = a.getColor(R.styleable.PrizeCommButton_btn_backgroud_color, Color.parseColor("#009def"));
        //初始化背景颜色数组
        initGradientColor(bgColor, bgColor);
        mButtonRadius = a.getDimensionPixelSize(R.styleable.PrizeCommButton_btn_radius, getMeasuredHeight() / 2 / 2);
        mAboveTextSize = a.getDimensionPixelSize(R.styleable.PrizeCommButton_btn_text_size, 25);
        mTextColor = a.getColor(R.styleable.PrizeCommButton_btn_text_color, Color.parseColor("#ffffff"));
        mCurrentText = a.getString(R.styleable.PrizeCommButton_btn_content);
        boolean enableGradient = a.getBoolean(R.styleable.PrizeCommButton_btn_enable_gradient, false);
        boolean enablePress = a.getBoolean(R.styleable.PrizeCommButton_btn_enable_press, false);
        ((DefaultButtonController) mDefaultController).setEnableGradient(enableGradient).setEnablePress(enablePress);
        if (enableGradient) {
            initGradientColor(mDefaultController.getLighterColor(mBackgroundColor[0]), mBackgroundColor[0]);
        }
        a.recycle();
    }

    private void init() {
        //设置背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //设置stoke画笔
        mStokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setStrokeWidth(borderWidth);
        mStokePaint.setColor(mBackgroundColor[0]);

        //设置文字画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mAboveTextSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //解决文字有时候画不出问题
            setLayerType(LAYER_TYPE_SOFTWARE, mTextPaint);
        }

        //设置第一个点画笔
        mDot1Paint = new Paint();
        mDot1Paint.setAntiAlias(true);
        mDot1Paint.setTextSize(mAboveTextSize);

        //设置第二个点画笔
        mDot2Paint = new Paint();
        mDot2Paint.setAntiAlias(true);
        mDot2Paint.setTextSize(mAboveTextSize);
        invalidate();

    }

    //初始化渐变色
    private int[] initGradientColor(int leftColor, int rightColor) {
        mBackgroundColor = new int[2];
        mBackgroundColor[0] = leftColor;
        mBackgroundColor[1] = rightColor;
        return mBackgroundColor;
    }


//    private void setupAnimations() {
//
//        //两个点向右移动动画
//        ValueAnimator dotMoveAnimation = ValueAnimator.ofFloat(0, 20);
//        TimeInterpolator pathInterpolator = PathInterpolatorCompat.create(0.11f, 0f, 0.12f, 1f);
//        dotMoveAnimation.setInterpolator(pathInterpolator);
//        dotMoveAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float transX = (float) animation.getAnimatedValue();
//                mDot1transX = transX;
//                mDot2transX = transX;
//                invalidate();
//            }
//        });
//        dotMoveAnimation.setDuration(1243);
//        dotMoveAnimation.setRepeatMode(ValueAnimator.RESTART);
//        dotMoveAnimation.setRepeatCount(ValueAnimator.INFINITE);
//
//
//        //两个点渐显渐隐动画
//        final ValueAnimator dotAlphaAnim = ValueAnimator.ofInt(0, 1243).setDuration(1243);
//        dotAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int time = (int) dotAlphaAnim.getAnimatedValue();
//                int dot1Alpha = calculateDot1AlphaByTime(time);
//                int dot2Alpha = calculateDot2AlphaByTime(time);
//                mDot1Paint.setAlpha(dot1Alpha);
//                mDot2Paint.setAlpha(dot2Alpha);
//            }
//
//        });
//
//
//        dotAlphaAnim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mDot1Paint.setAlpha(0);
//                mDot2Paint.setAlpha(0);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        dotAlphaAnim.setRepeatMode(ValueAnimator.RESTART);
//        dotAlphaAnim.setRepeatCount(ValueAnimator.INFINITE);
//        //两个点的动画集合
//        mDotAnimationSet = new AnimatorSet();
//        mDotAnimationSet.playTogether(dotAlphaAnim, dotMoveAnimation);
//
//        //ProgressBar的动画
//        mProgressAnimation = ValueAnimator.ofFloat(0, 1).setDuration(500);
//        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float timepercent = (float) animation.getAnimatedValue();
//                mProgress = ((mToProgress - mProgress) * timepercent + mProgress);
//                invalidate();
//            }
//        });
//
//
//    }

//    //第一个点透明度计算函数
//    private int calculateDot2AlphaByTime(int time) {
//        int alpha;
//        if (0 <= time && time <= 83) {
//            double DAlpha = 255.0 / 83.0 * time;
//            alpha = (int) DAlpha;
//        } else if (83 < time && time <= 1000) {
//            alpha = 255;
//        } else if (1000 < time && time <= 1083) {
//            double DAlpha = -255.0 / 83.0 * (time - 1083);
//            alpha = (int) DAlpha;
//        } else if (1083 < time && time <= 1243) {
//            alpha = 0;
//        } else {
//            alpha = 255;
//        }
//        return alpha;
//    }
//
//    //第二个点透明度计算函数
//    private int calculateDot1AlphaByTime(int time) {
//        int alpha;
//        if (0 <= time && time <= 160) {
//            alpha = 0;
//        } else if (160 < time && time <= 243) {
//            double DAlpha = 255.0 / 83.0 * (time - 160);
//            alpha = (int) DAlpha;
//        } else if (243 < time && time <= 1160) {
//            alpha = 255;
//        } else if (1160 < time && time <= 1243) {
//            double DAlpha = -255.0 / 83.0 * (time - 1243);
//            alpha = (int) DAlpha;
//        } else {
//            alpha = 255;
//        }
//        return alpha;
//    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            drawing(canvas);
        }
    }

    private void drawing(Canvas canvas) {
        drawBackground(canvas);
        drawTextAbove(canvas);
    }

    private void drawBackground(Canvas canvas) {
        mBackgroundBounds = new RectF();
        if (mButtonRadius == 0) {
            mButtonRadius = getMeasuredHeight() / 2;
        }
        mBackgroundBounds.left = 2;
        mBackgroundBounds.top = 2;
        mBackgroundBounds.right = getMeasuredWidth() - 2;
        mBackgroundBounds.bottom = getMeasuredHeight() - 2;

        ButtonController buttonController = switchController();
        if (buttonController.enableGradient()) {
            mFillBgGradient = new LinearGradient(0, getMeasuredHeight() / 2, getMeasuredWidth(), getMeasuredHeight() / 2,
                    mBackgroundColor,
                    null,
                    Shader.TileMode.CLAMP);
            mBackgroundPaint.setShader(mFillBgGradient);
        } else {
            if (mBackgroundPaint.getShader() != null) {
                mBackgroundPaint.setShader(null);
            }
            mBackgroundPaint.setColor(mBackgroundColor[0]);
        }
        canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
    }

    private void drawTextAbove(Canvas canvas) {
        final float y = canvas.getHeight() / 2 - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2);
        if (mCurrentText == null) {
            mCurrentText = "";
        }
        //color
        float textWidth = mTextPaint.measureText(mCurrentText.toString());
        mTextPaint.setShader(null);
        mTextPaint.setColor(mTextColor);
        canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);


    }

    private ButtonController switchController() {
        return mDefaultController;
    }

    /**
     * 设置按钮文字
     */
    public void setCurrentText(CharSequence charSequence) {
        mCurrentText = charSequence;
        invalidate();
    }


    public void setProgressBtnBackgroundColor(int color) {
        initGradientColor(color, color);
    }

    public float getButtonRadius() {
        return mButtonRadius;
    }

    public void setButtonRadius(float buttonRadius) {
        mButtonRadius = buttonRadius;
    }

    public int getTextColor() {
        return mTextColor;
    }


    @Override
    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }


    public void enabelDefaultPress(boolean enable) {
        if (mDefaultController != null) {
            ((DefaultButtonController) mDefaultController).setEnablePress(enable);
        }
    }

    public void enabelDefaultGradient(boolean enable) {
        if (mDefaultController != null) {
            ((DefaultButtonController) mDefaultController).setEnableGradient(enable);
            initGradientColor(mDefaultController.getLighterColor(mBackgroundColor[0]), mBackgroundColor[0]);
        }
    }

    @Override
    public void setTextSize(float size) {
        mAboveTextSize = size;
        mTextPaint.setTextSize(size);
    }

    @Override
    public float getTextSize() {
        return mAboveTextSize;
    }

}
