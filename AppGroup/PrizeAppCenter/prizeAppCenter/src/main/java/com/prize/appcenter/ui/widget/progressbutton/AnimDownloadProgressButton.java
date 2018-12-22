package com.prize.appcenter.ui.widget.progressbutton;

import android.animation.Animator;
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
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppUpdateActivity;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;

import java.io.File;

import static com.prize.app.download.AppManagerCenter.APP_STATE_DOWNLOADING;

/*
 * Created by longbaoxiu on 10/11/17.
 */
public class AnimDownloadProgressButton extends TextView {
    private final static String TAG = "AnimDownloadProgressButton";

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
    //下载中后半部分后面背景颜色
    private int mBackgroundSecondColor;
    //文字颜色
    private int mTextColor;
    //覆盖后颜色
    private int mTextCoverColor;
    //文字大小
    private float mAboveTextSize = 50;


    private float mProgress = -1;
//    private float mToProgress;
    private int mMaxProgress;
    private int mMinProgress;
    private float mProgressPercent;

    private float mButtonRadius;

    //两个点向右移动距离
    private float mDot1transX;
    private float mDot2transX;

    private RectF mBackgroundBounds;
    private RectF mBackgroundStoke;
    private LinearGradient mFillBgGradient;
    private LinearGradient mProgressBgGradient;
    private LinearGradient mProgressTextGradient;

//    //点运动动画
//    private AnimatorSet mDotAnimationSet;
//    //下载平滑动画
//    private ValueAnimator mProgressAnimation;

    //记录当前文字
    private CharSequence mCurrentText;

    //普通状态
    public static final int NORMAL = 0;
    //下载中
    public static final int DOWNLOADING = 1;
    //有点运动状态
    public static final int INSTALLING = 2;

    private ButtonController mDefaultController;

    private ButtonController mCustomerController;


    private int mState;
    private float borderWidth = 2.0f;
    private boolean showProgress;

    public AnimDownloadProgressButton(Context context) {
        this(context, null);

    }

    public AnimDownloadProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            mContext = context;
            initController();
            initAttrs(context, attrs);
            init();
            setupAnimations();
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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimDownloadProgressButton);
//        int bgColor = a.getColor(R.styleable.AnimDownloadProgressButton_progressbtn_backgroud_color, Color.parseColor("#1D82FF"));
        int bgColor = a.getColor(R.styleable.AnimDownloadProgressButton_progressbtn_backgroud_color, Color.parseColor("#009def"));
        //初始化背景颜色数组
        initGradientColor(bgColor, bgColor);
        mBackgroundSecondColor = a.getColor(R.styleable.AnimDownloadProgressButton_progressbtn_backgroud_second_color, Color.WHITE);
        mButtonRadius = a.getDimensionPixelSize(R.styleable.AnimDownloadProgressButton_progressbtn_radius, getMeasuredHeight() / 2 / 2);
        mAboveTextSize = a.getDimensionPixelSize(R.styleable.AnimDownloadProgressButton_progressbtn_text_size, 25);
        mTextColor = a.getColor(R.styleable.AnimDownloadProgressButton_progressbtn_text_color, bgColor);
        mTextCoverColor = a.getColor(R.styleable.AnimDownloadProgressButton_progressbtn_text_covercolor, Color.WHITE);
        boolean enableGradient = a.getBoolean(R.styleable.AnimDownloadProgressButton_progressbtn_enable_gradient, false);
        boolean enablePress = a.getBoolean(R.styleable.AnimDownloadProgressButton_progressbtn_enable_press, false);
        showProgress = a.getBoolean(R.styleable.AnimDownloadProgressButton_progressbtn_show_progress, false);
        ((DefaultButtonController) mDefaultController).setEnableGradient(enableGradient).setEnablePress(enablePress);
        if (enableGradient) {
            initGradientColor(mDefaultController.getLighterColor(mBackgroundColor[0]), mBackgroundColor[0]);
        }
        a.recycle();
    }

    private void init() {
        mMaxProgress = 100;
        mMinProgress = 0;
        mProgress = 0;


        //设置背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //设置stoke画笔
        mStokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setAntiAlias(true);
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


        //初始化状态设为NORMAL
        mState = NORMAL;
        invalidate();

    }

    //初始化渐变色
    private int[] initGradientColor(int leftColor, int rightColor) {
        mBackgroundColor = new int[2];
        mBackgroundColor[0] = leftColor;
        mBackgroundColor[1] = rightColor;
        return mBackgroundColor;
    }
    private boolean isNewStyle = false;
    //初始化渐变色
    public void setBackgroundColor(int leftColor, int rightColor) {
        isNewStyle=true;
        initGradientColor(leftColor,rightColor);
        if (mOriginBackgroundColor == null) {
            mOriginBackgroundColor = new int[2];
        }
        mOriginBackgroundColor[0] = leftColor;
        mOriginBackgroundColor[1] = rightColor;
        mTextColor=rightColor;
        mBackgroundSecondColor=Color.parseColor("#dedede");
        mTextCoverColor=mBackgroundSecondColor;
        mStokePaint.setColor(mBackgroundColor[0]);
//        this.invalidate();
    }
    private void setupAnimations() {

        //两个点向右移动动画
        ValueAnimator dotMoveAnimation = ValueAnimator.ofFloat(0, 20);
        TimeInterpolator pathInterpolator = PathInterpolatorCompat.create(0.11f, 0f, 0.12f, 1f);
        dotMoveAnimation.setInterpolator(pathInterpolator);
        dotMoveAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float transX = (float) animation.getAnimatedValue();
                mDot1transX = transX;
                mDot2transX = transX;
                invalidate();
            }
        });
        dotMoveAnimation.setDuration(1243);
        dotMoveAnimation.setRepeatMode(ValueAnimator.RESTART);
        dotMoveAnimation.setRepeatCount(ValueAnimator.INFINITE);


        //两个点渐显渐隐动画
        final ValueAnimator dotAlphaAnim = ValueAnimator.ofInt(0, 1243).setDuration(1243);
        dotAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int time = (int) dotAlphaAnim.getAnimatedValue();
                int dot1Alpha = calculateDot1AlphaByTime(time);
                int dot2Alpha = calculateDot2AlphaByTime(time);
                mDot1Paint.setColor(mTextCoverColor);
                mDot2Paint.setColor(mTextCoverColor);
                mDot1Paint.setAlpha(dot1Alpha);
                mDot2Paint.setAlpha(dot2Alpha);
            }

        });


        dotAlphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDot1Paint.setAlpha(0);
                mDot2Paint.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        dotAlphaAnim.setRepeatMode(ValueAnimator.RESTART);
        dotAlphaAnim.setRepeatCount(ValueAnimator.INFINITE);
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


    }

    //第一个点透明度计算函数
    private int calculateDot2AlphaByTime(int time) {
        int alpha;
        if (0 <= time && time <= 83) {
            double DAlpha = 255.0 / 83.0 * time;
            alpha = (int) DAlpha;
        } else if (83 < time && time <= 1000) {
            alpha = 255;
        } else if (1000 < time && time <= 1083) {
            double DAlpha = -255.0 / 83.0 * (time - 1083);
            alpha = (int) DAlpha;
        } else if (1083 < time && time <= 1243) {
            alpha = 0;
        } else {
            alpha = 255;
        }
        return alpha;
    }

    //第二个点透明度计算函数
    private int calculateDot1AlphaByTime(int time) {
        int alpha;
        if (0 <= time && time <= 160) {
            alpha = 0;
        } else if (160 < time && time <= 243) {
            double DAlpha = 255.0 / 83.0 * (time - 160);
            alpha = (int) DAlpha;
        } else if (243 < time && time <= 1160) {
            alpha = 255;
        } else if (1160 < time && time <= 1243) {
            double DAlpha = -255.0 / 83.0 * (time - 1243);
            alpha = (int) DAlpha;
        } else {
            alpha = 255;
        }
        return alpha;
    }


//    private ValueAnimator createDotAlphaAnimation(int i, Paint mDot1Paint, int i1, int i2, int i3) {
//
//        return new ValueAnimator();
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            drawing(canvas);
        }
    }

    private void drawing(Canvas canvas) {
        if(item==null|| TextUtils.isEmpty(item.packageName))
            return;
        mState = AIDLUtils.getGameAppState(item.packageName,
                String.valueOf(item.id), item.versionCode);
        drawBackground(canvas);
        drawTextAbove(canvas);
    }

    private void drawBackground(Canvas canvas) {
        mBackgroundBounds = new RectF();
        if (mButtonRadius == 0) {
            mButtonRadius = getMeasuredHeight() / 2;
        }
        mBackgroundBounds.left = 0;
        mBackgroundBounds.top = 0;
        mBackgroundBounds.right = getMeasuredWidth();
        mBackgroundBounds.bottom = getMeasuredHeight();

        mBackgroundStoke = new RectF();
        if (mButtonRadius == 0) {
            mButtonRadius = getMeasuredHeight()/2;
        }
//        mBackgroundStoke.left = 1f;//数字越大，越往右
//        mBackgroundStoke.top =0.5f;//数字越大，越往下
//        mBackgroundStoke.right = getMeasuredWidth()-0.5f;//减得越大，越往左
//        mBackgroundStoke.bottom = getMeasuredHeight()-0.5f;//减得越大 越向上
        mBackgroundStoke.left = 1;//数字越大，越往右
        mBackgroundStoke.top =1;//数字越大，越往下
        mBackgroundStoke.right = getMeasuredWidth()-1;//减得越大，越往左
        mBackgroundStoke.bottom = getMeasuredHeight()-1;//减得越大 越向上


        ButtonController buttonController = switchController();
        //color
        switch (mState) {
            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
            case AppManagerCenter.APP_STATE_INSTALLED:
            case AppManagerCenter.APP_PATCHING:
            case AppManagerCenter.APP_STATE_INSTALLING:
            case AppManagerCenter.APP_STATE_DOWNLOADED:
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
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                mProgress = AIDLUtils.getDownloadProgress(item.packageName);
                if (buttonController.enableGradient()) {
                    mProgressPercent = mProgress / (mMaxProgress + 0f);
                    int[] colorList = new int[]{mBackgroundColor[0], mBackgroundColor[1], mBackgroundSecondColor};
                    mProgressBgGradient = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                            colorList,
                            new float[]{0, mProgressPercent, mProgressPercent + 0.001f},
                            Shader.TileMode.CLAMP
                    );
                    mBackgroundPaint.setShader(mProgressBgGradient);
                } else {
                    mProgressPercent = mProgress / (mMaxProgress + 0f);
                    mProgressBgGradient = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                            new int[]{mBackgroundColor[0], mBackgroundSecondColor},
                            new float[]{mProgressPercent, mProgressPercent + 0.001f},
                            Shader.TileMode.CLAMP
                    );
                    mBackgroundPaint.setColor(mBackgroundColor[0]);
                    mBackgroundPaint.setShader(mProgressBgGradient);
                }
                canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
                if (!isNewStyle) {
                    canvas.drawRoundRect(mBackgroundStoke, mButtonRadius, mButtonRadius, mStokePaint);
                }
                break;
            case AppManagerCenter.APP_STATE_WAIT:
                if (buttonController.enableGradient()) {
                    mFillBgGradient = new LinearGradient(0, getMeasuredHeight() / 2, getMeasuredWidth(), getMeasuredHeight() / 2,
                            mBackgroundColor,
                            null,
                            Shader.TileMode.CLAMP);
                    mBackgroundPaint.setShader(mFillBgGradient);
                } else {
                    mBackgroundPaint.setShader(null);
                    mBackgroundPaint.setColor(mBackgroundColor[0]);
                }
                canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
                break;
        }
    }

    private void drawTextAbove(Canvas canvas) {
        final float y = canvas.getHeight() / 2 - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2);
        if (mCurrentText == null) {
            mCurrentText = "";
        }
        //color
        float textWidth = 0.0f;
        switch (mState) {
            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
            case AppManagerCenter.APP_STATE_INSTALLED:
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                switch (mState){
                    case AppManagerCenter.APP_STATE_UNEXIST:
                        if (BaseApplication.isThird) {
                            mCurrentText = mContext.getString(R.string.progress_btn_download);
                        } else {
                            mCurrentText = mContext.getString(R.string.progress_btn_install);
                        }
                        if(mContext instanceof AppUpdateActivity){
                            mCurrentText = mContext.getString(R.string.progress_btn_upload);
                        }
                        break;
                    case AppManagerCenter.APP_STATE_UPDATE:
                        mCurrentText = mContext.getString(R.string.progress_btn_upload);
                        break;
                    case AppManagerCenter.APP_STATE_INSTALLED:
                        mCurrentText = mContext.getString(R.string.progress_btn_start);
                        break;
                    case AppManagerCenter.APP_STATE_DOWNLOADED:
                        mCurrentText = mContext.getString(R.string.progress_btn_install);
                        break;
                }
                textWidth = mTextPaint.measureText(mCurrentText.toString());
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextCoverColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                mCurrentText = mState == AppManagerCenter.APP_STATE_DOWNLOADING ? mContext.getString(R.string.progress_pause) : mContext.getString(R.string.progress_continue);
                if(showProgress&&mState== AppManagerCenter.APP_STATE_DOWNLOADING){
                    mCurrentText = CommonUtils.paresDownLoadPercent(mProgress);
                }
                textWidth = mTextPaint.measureText(mCurrentText.toString());
                //进度条压过距离
                float coverlength = getMeasuredWidth() * mProgressPercent;
                //开始渐变指示器
                float indicator1 = getMeasuredWidth() / 2 - textWidth / 2;
                //结束渐变指示器
                float indicator2 = getMeasuredWidth() / 2 + textWidth / 2;
                //文字变色部分的距离
                float coverTextLength = textWidth / 2 - getMeasuredWidth() / 2 + coverlength;
                float textProgress = coverTextLength / textWidth;
                if (coverlength <= indicator1) {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextColor);
                } else if (indicator1 < coverlength && coverlength <= indicator2) {
                    mProgressTextGradient = new LinearGradient((getMeasuredWidth() - textWidth) / 2, 0, (getMeasuredWidth() + textWidth) / 2, 0,
                            new int[]{mTextCoverColor, mTextColor},
                            new float[]{textProgress, textProgress + 0.001f},
                            Shader.TileMode.CLAMP);
                    mTextPaint.setColor(mTextColor);
                    mTextPaint.setShader(mProgressTextGradient);
                } else {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextCoverColor);
                }
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case AppManagerCenter.APP_STATE_WAIT:
                mCurrentText = mContext.getString(R.string.progress_btn_wait);
                textWidth = mTextPaint.measureText(mCurrentText.toString());
                mTextPaint.setColor(mTextCoverColor);
                mTextPaint.setShader(null);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case AppManagerCenter.APP_STATE_INSTALLING:
                mCurrentText = mContext.getString(R.string.progress_btn_installing);
                textWidth = mTextPaint.measureText(mCurrentText.toString());
                mTextPaint.setColor(mTextCoverColor);
                mTextPaint.setShader(null);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case AppManagerCenter.APP_PATCHING:
                mCurrentText = mContext.getString(R.string.progress_btn_patching);
                textWidth = mTextPaint.measureText(mCurrentText.toString());
                mTextPaint.setColor(mTextCoverColor);
                mTextPaint.setShader(null);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;

        }

    }

    private ButtonController switchController() {
        if (mCustomerController != null) {
            return mCustomerController;
        } else {
            return mDefaultController;
        }
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        if (mState != state) {//状态确实有改变
            this.mState = state;
            invalidate();
//            if (state == AnimDownloadProgressButton.INSTALLING) {
//                //开启两个点动画
//                mDotAnimationSet.start();
//            } else if (state == NORMAL) {
//                mDotAnimationSet.cancel();
//            } else if (state == DOWNLOADING) {
//                mDotAnimationSet.cancel();
//            }
        }

    }

    /**
     * 设置按钮文字
     */
    public void setCurrentText(CharSequence charSequence) {
        mCurrentText = charSequence;
        invalidate();
    }


//    /**
//     * 设置带下载进度的文字
//     */
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    public void setProgressText(String text, float progress) {
//        if (progress >= mMinProgress && progress < mMaxProgress) {
//            mCurrentText = text + getResources().getString(R.string.downloaded, (int) progress);
//            mToProgress = progress;
//            if (mProgressAnimation.isRunning()) {
//                mProgressAnimation.start();
//            } else {
//                mProgressAnimation.start();
//            }
//        } else if (progress < mMinProgress) {
//            mProgress = 0;
//        } else if (progress >= mMaxProgress) {
//            mProgress = 100;
//            mCurrentText = text + getResources().getString(R.string.downloaded, (int) mProgress);
//            invalidate();
//        }
//    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;

    }

//    /**
//     * Sometimes you should use the method to avoid memory leak
//     */
//    public void removeAllAnim() {
//        mDotAnimationSet.cancel();
//        mDotAnimationSet.removeAllListeners();
//        mProgressAnimation.cancel();
//        mProgressAnimation.removeAllListeners();
//    }


    public void setProgressBtnBackgroundColor(int color) {
        initGradientColor(color, color);
    }


    public void setProgressBtnBackgroundSecondColor(int color) {

        mBackgroundSecondColor = color;
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

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    @Override
    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public int getTextCoverColor() {
        return mTextCoverColor;
    }

    public void setTextCoverColor(int textCoverColor) {
        mTextCoverColor = textCoverColor;
    }

    public int getMinProgress() {
        return mMinProgress;
    }

    public void setMinProgress(int minProgress) {
        mMinProgress = minProgress;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
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

    public AnimDownloadProgressButton setCustomerController(ButtonController customerController) {
        mCustomerController = customerController;
        return this;
    }

//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//        SavedState ss = (SavedState) state;
//        super.onRestoreInstanceState(ss.getSuperState());
//        mState = ss.state;
//        mProgress = ss.progress;
//        mCurrentText = ss.currentText;
//    }

//    @Override
//    public Parcelable onSaveInstanceState() {
//        Parcelable superState = super.onSaveInstanceState();
//        return new SavedState(superState, (int) mProgress, mState, mCurrentText.toString());
//    }

//    public static class SavedState extends BaseSavedState {
//
//        private int progress;
//        private int state;
//        private String currentText;
//
//        public SavedState(Parcelable parcel, int progress, int state, String currentText) {
//            super(parcel);
//            this.progress = progress;
//            this.state = state;
//            this.currentText = currentText;
//        }
//
//        private SavedState(Parcel in) {
//            super(in);
//            progress = in.readInt();
//            state = in.readInt();
//            currentText = in.readString();
//        }
//
//        @Override
//        public void writeToParcel(Parcel out, int flags) {
//            super.writeToParcel(out, flags);
//            out.writeInt(progress);
//            out.writeInt(state);
//            out.writeString(currentText);
//        }
//
//        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
//
//            @Override
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//
//            @Override
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//        };
//
//    }

    /**
     * App游戏详情
     */
    private AppsItemBean item;

    /**
     * 设置Item
     *
     * @param gameInfo AppsItemBean
     */
    public void setGameInfo(AppsItemBean gameInfo) {
        this.item = gameInfo;
        invalidate();
    }

    /**
     * 点击Button
     */
    public void onClick() {
        if (null == item) {
            return;
        }
        int state = AIDLUtils.getGameAppState(item.packageName, item.id + "",
                item.versionCode);
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                if (BaseApplication.isThird) {
                    AppManagerCenter.installGameApk(item);
                } else {
                    String gameAPKFilePath = FileUtils.getGameAPKFilePath(item.id);
                    File gameAPKFile = new File(gameAPKFilePath);
                    if (gameAPKFile.exists()) {
                        PackageUtils.installNormal(getContext(), gameAPKFilePath);
                    }
                }

                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                UIUtils.startGame(item);
                break;

            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                if (this.getContext() instanceof SearchActivity) {
                    PrizeStatUtil.onSearchResultItemClick(item.id, item.packageName, item.name, ((SearchActivity) this.getContext()).getKeyWord(), false);
                }
                UIUtils.downloadApp(item);
                MTAUtil.onClickDownload(this.getContext(), item.name,
                        item.packageName);
                break;
            case APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_WAIT:
                AIDLUtils.pauseDownload(item, true);
                break;

            default:
                Log.i("ProgressButton", "onClick=--state不在执行范围-state=" + state);
                break;
        }
    }

}
