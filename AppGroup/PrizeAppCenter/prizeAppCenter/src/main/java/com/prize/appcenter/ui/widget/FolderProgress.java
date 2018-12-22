package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.prize.app.BaseApplication;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.PackageUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;

import java.io.File;

import static com.prize.app.download.AppManagerCenter.APP_STATE_DOWNLOADING;

/**
 * longbaoxiu
 * 2018/1/9.20:55
 */

public class FolderProgress extends View {
    /**
     * 进度条画笔
     */
    private Paint paint = null;
    /**
     * 白色线条
     */
    private Paint circlePaint = null;
    /**
     * 进度条画笔
     */
    private Paint whitePaint = null;
    /**
     * 文字画笔
     */
    private Paint paintText = null;

    /**
     * 进度条范围
     */
    private RectF roundProgressRecr = null;
    /**
     * 背景范围
     */
    private Rect bgRect = null;

    /**
     * 启动按钮 按下
     */
    private static Drawable pauseBG = null;
    /**
     * 启动按钮 按下
     */
    private static Drawable nomalBG = null;
    /**
     * 启动按钮 按下
     */
    private static Drawable tranBG = null;
    /**
     * App游戏详情
     */
    private AppsItemBean item;

    public FolderProgress(Context context) {
        super(context);
    }

    public FolderProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public FolderProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float strokeWidth = 2;

    /**
     * 初始化画笔
     */
    protected void initPaint(Context context) {
        Resources resource = context.getResources();
        pauseBG = resource.getDrawable(R.drawable.folder_state_pause);
        nomalBG = resource.getDrawable(R.drawable.bg_folder_pgr);
        tranBG = new ColorDrawable(Color.TRANSPARENT);
        paint = new Paint();
        paint.setAntiAlias(true);
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.parseColor("#ccffffff"));


        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.parseColor("#ccffffff"));
        circlePaint.setStrokeWidth(strokeWidth);              //线宽
        circlePaint.setStyle(Paint.Style.STROKE);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);

//
//        bgPaint = new Paint();
//        bgPaint.setColor(Color.parseColor("#99000000"));

    }

    /**
     * 设置Item
     *
     * @param gameInfo AppsItemBean
     */
    public void setGameInfo(AppsItemBean gameInfo) {
        this.item = gameInfo;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int textSize = calculateTextSize();
        paintText.setTextSize(textSize);
        drawButton(canvas);
    }

    /**
     * 计算文字大小
     *
     * @return int 文字大小
     */
    private int calculateTextSize() {
        return (int) com.prize.app.util.DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,12);
    }

    private void drawButton(final Canvas canvas) {
        if (item == null) {
            return;
        }
        int state = AIDLUtils.getGameAppState(item.packageName,
                String.valueOf(item.id), item.versionCode);
        drawRealButton(canvas, state);

    }

    private void drawRealButton(Canvas canvas, int state) {
        switch (state) {
            case AppManagerCenter.APP_STATE_WAIT:
                // 等待中 下载
                drawDowning(canvas);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                // 下载完成
                drawIntalling(canvas,R.string.progress_btn_install);
//                drawNormalBg(canvas, R.string.progress_btn_install, Color.parseColor("#12b7f5"));
                break;
//            case AppManagerCenter.APP_STATE_INSTALLED:
//                // 安装完成(启动应用)
//                drawIntalling(canvas, R.string.progress_btn_start);
//                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                // 暂停
                drawDownloadPause(canvas);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
                // 下载中
                drawDowning(canvas);
                break;
            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
                int height = getHeight();
                int width = getWidth();
                // 画下载中背景
                tranBG.setBounds(0, 0, width, height);
                canvas.save();
                break;
            case AppManagerCenter.APP_STATE_INSTALLING:
                // 安装中
//                drawNormalBg(canvas, R.string.progress_btn_installing,
//                        Color.parseColor("#12b7f5"));
                drawIntalling(canvas, R.string.progress_btn_installing);
                break;
            case AppManagerCenter.APP_PATCHING:
                // 安装中
//                drawNormalBg(canvas,
//                        R.string.progress_btn_patching,
//                        Color.parseColor("#12b7f5"));
                drawIntalling(canvas, R.string.progress_btn_patching);
                break;
        }
    }

    /**
     * 画下载暂停中图片
     *
     * @param canvas Canvas
     */
    protected void drawDownloadPause(Canvas canvas) {

//        /* 按钮进度条宽度 */
//        int mPreviewWidth = -1;
//        // 进度条进度指示
//        float progress = AIDLUtils.getDownloadProgress(item.packageName);
//        if (progress > 0.0 && progress < 1.0f) {
//            progress = 1;
//        }
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        nomalBG.setBounds(0, 0, width, height);
        nomalBG.draw(canvas);
        pauseBG.setBounds(0, 0, width, height);
        pauseBG.draw(canvas);
        canvas.save();
//        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//        final Canvas srcCanvs = new Canvas(src);
//        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
//        mDownLoadProgressDrawable.draw(srcCanvs);
//        // 取背景范围
//        bgRect = downloadingNomalBG.getBounds();
        // 算进度条范围
//        roundProgressRecr = new RectF(bgRect.left, bgRect.top, bgRect.right,
//                bgRect.bottom);
//
//        roundProgressRecr.right = (int) ((progress / 100f) * (roundProgressRecr.right - roundProgressRecr.left))
//                + roundProgressRecr.left;
//
//        int w = (int) roundProgressRecr.width();
//        if (w >= mPreviewWidth) {
//            mPreviewWidth = w;
//        }
//        int h = (int) roundProgressRecr.height();

//        if (mPreviewWidth > 0 && h > 0) {
//            final Bitmap mask = Bitmap.createBitmap(mPreviewWidth, h,
//                    Config.ARGB_8888);
//            final Canvas maskCanvas = new Canvas(mask);
//            mMask.setBounds(0, 0, mPreviewWidth, h);
//            mMask.draw(maskCanvas);
//            final Bitmap result = createMaskImage(src, mask);
//            canvas.save();
//            canvas.drawBitmap(result, 0, 0, null);
//            canvas.restore();
//        }

//        // 画文字
//        String text = getResources().getString(textId);
//        // 文字显示位置
//        int x = getWidth() / 2;
//        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
//        drawText(canvas, text, x, y, Color.parseColor("#12b7f5"));
        canvas.save();
    }

    /**
     * 画正常形态图片
     *
     * @param canvas    Canvas
     * @param textId    文字id
     * @param textColor 颜色id
     */
    private void drawNormalBg(Canvas canvas,
                              int textId, int textColor) {

        int height = getHeight();
        int width = getWidth();
//        if (onTouching) {
//            // 画按住图片背景
//            press.setBounds(0, 0, width, height);
//            press.draw(canvas);
//        } else {
//            // 画正常图片背景
//            normal.setBounds(0, 0, width, height);
//            normal.draw(canvas);
//        }
        // 文字显示位置居中
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, getResources().getString(textId), x, y, textColor);
    }
    /**
     * 画安装中状态
     *
     * @param canvas    Canvas
     */
    private void drawIntalling(Canvas canvas,int textId) {
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        nomalBG.setBounds(0, 0, width, height);
        nomalBG.draw(canvas);
        // 文字显示位置居中
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, getResources().getString(textId), x, y, Color.WHITE);
    }


    /**
     * 文字显示
     *
     * @param canvas Canvas画笔
     * @param text   需要绘制的文字
     * @param x      x坐标
     * @param y      y坐标
     * @param color  颜色值
     */
    protected void drawText(Canvas canvas, String text, int x, int y, int color) {
        Typeface font = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        paintText.setTypeface(font);
        // 文字大小
        int textSize = calculateTextSize();
        paintText.setTextSize(textSize);
        paintText.setColor(color);
        canvas.drawText(text, x, y, paintText);
    }

    private boolean onTouching = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouching = true;
                invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                onTouching = false;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingBottom() + getPaddingTop());

        float wwd = (float) w - xpad;
        float hhd = (float) h - ypad;
        oval = new RectF(getPaddingLeft() + strokeWidth, getPaddingTop() + strokeWidth, getPaddingLeft() + wwd - strokeWidth, getPaddingTop() + hhd - strokeWidth);
    }

    //    private Paint fgPaint;
    private RectF oval;
    //    private float startAngle;
//    private float percent;
    private Paint bgPaint;

    /**
     * 画下载中图片
     *
     * @param canvas Canvas
     */
    protected void drawDowning(Canvas canvas) {
//        /* 按钮进度条宽度 */
//        int mPreviewWidth = -1;
        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
        int height = getHeight();
        int width = getWidth();
        nomalBG.setBounds(0, 0, width, height);
        nomalBG.draw(canvas);//画背景

//        canvas.drawArc(oval, 0, 360, true, bgPaint);
        canvas.drawCircle(height / 2, width / 2, oval.width() / 2, circlePaint);//画白线
        canvas.drawArc(oval, -90, progress * 3.6f, true, whitePaint);//画扇形


//        canvas.drawArc(mCircleRectF,DEFAULT_INITIAL_ANGLE, progress * 360 / getMax(),true, mSectorPaint);
        // float progress =
        // AppManagerCenter.getDownloadProgress(item.packageName);
//        if (progress > 0.0 && progress < 1.0f) {
//            progress = 1;
//        }
//        int height = getHeight();
//        int width = getWidth();
//        // 画下载中背景
////        downloadingNomalBG.setBounds(0, 0, width, height);
////        downloadingNomalBG.draw(canvas);
////        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
////        final Canvas srcCanvs = new Canvas(src);
////        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
////        mDownLoadProgressDrawable.draw(srcCanvs);
////        // 取背景范围
////        bgRect = downloadingNomalBG.getBounds();
//        // 算进度条范围
//        roundProgressRecr = new RectF(bgRect.left, bgRect.top, bgRect.right,
//                bgRect.bottom);
//
//        roundProgressRecr.right = (int) ((progress / 100f) * (roundProgressRecr.right - roundProgressRecr.left))
//                + roundProgressRecr.left;
//
//        int w = (int) roundProgressRecr.width();
//        if (w >= mPreviewWidth) {
//            mPreviewWidth = w;
//        }
//        int h = (int) roundProgressRecr.height();
//
////        if (mPreviewWidth > 0 && h > 0) {
////            final Bitmap mask = Bitmap.createBitmap(mPreviewWidth, h,
////                    Config.ARGB_8888);
////            final Canvas maskCanvas = new Canvas(mask);
////            mMask.setBounds(0, 0, mPreviewWidth, h);
////            mMask.draw(maskCanvas);
////            final Bitmap result = createMaskImage(src, mask);
////            canvas.save();
////            canvas.drawBitmap(result, 0, 0, null);
////            canvas.restore();
////        }
//
//        // 画文字
//        String text = CommonUtils.paresDownLoadPercent(progress);
//        // 文字显示位置
//        int x = getWidth() / 2;
//        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
//        drawText(canvas, text, x, y, Color.parseColor("#12b7f5"));
//        canvas.save();
    }

    /**
     * 画下载中图片
     *
     * @param canvas Canvas
     */
    protected void drawDownloadBg(Canvas canvas) {
        /* 按钮进度条宽度 */
        int mPreviewWidth = -1;
        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
//        canvas.drawArc(mCircleRectF,DEFAULT_INITIAL_ANGLE, progress * 360 / getMax(),true, mSectorPaint);
        // float progress =
        // AppManagerCenter.getDownloadProgress(item.packageName);
//        if (progress > 0.0 && progress < 1.0f) {
//            progress = 1;
//        }
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
//        downloadingNomalBG.setBounds(0, 0, width, height);
//        downloadingNomalBG.draw(canvas);
//        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//        final Canvas srcCanvs = new Canvas(src);
//        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
//        mDownLoadProgressDrawable.draw(srcCanvs);
//        // 取背景范围
//        bgRect = downloadingNomalBG.getBounds();
        // 算进度条范围
        roundProgressRecr = new RectF(bgRect.left, bgRect.top, bgRect.right,
                bgRect.bottom);

        roundProgressRecr.right = (int) ((progress / 100f) * (roundProgressRecr.right - roundProgressRecr.left))
                + roundProgressRecr.left;

        int w = (int) roundProgressRecr.width();
        if (w >= mPreviewWidth) {
            mPreviewWidth = w;
        }
        int h = (int) roundProgressRecr.height();

//        if (mPreviewWidth > 0 && h > 0) {
//            final Bitmap mask = Bitmap.createBitmap(mPreviewWidth, h,
//                    Config.ARGB_8888);
//            final Canvas maskCanvas = new Canvas(mask);
//            mMask.setBounds(0, 0, mPreviewWidth, h);
//            mMask.draw(maskCanvas);
//            final Bitmap result = createMaskImage(src, mask);
//            canvas.save();
//            canvas.drawBitmap(result, 0, 0, null);
//            canvas.restore();
//        }

        // 画文字
        String text = CommonUtils.paresDownLoadPercent(progress);
        // 文字显示位置
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, text, x, y, Color.parseColor("#12b7f5"));
        canvas.save();
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
                UIUtils.downloadApp(item);
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
