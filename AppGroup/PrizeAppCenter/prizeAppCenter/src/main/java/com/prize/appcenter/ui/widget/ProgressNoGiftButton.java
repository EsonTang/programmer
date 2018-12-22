package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

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

import java.io.File;

/**
 * 带进度的button
 *
 * @author prize
 */
public class ProgressNoGiftButton extends ProgressBar {
    /**
     * 进度条画笔
     */
    private Paint paint = null;
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
     * App游戏详情
     */
    private AppsItemBean item;


    private static Drawable downloadingNomalBG = null;
    //	private static Drawable downloadDrawable = null;
    private static Drawable mMask = null;
    private static Drawable mDownLoadProgressDrawable = null;

    /**
     * 启动按钮 正常
     */
    private static Drawable startNormalBG = null;
    /**
     * 启动按钮 按下
     */
    private static Drawable startPressBG = null;
    /** 打开按钮 正常 */
    // private static Drawable openNormalBG = null;
    // /** 打开按钮 按下 */
    // private static Drawable openPressBG = null;
    /**
     * 安装中按钮 按下
     */
    private static Drawable installingBG = null;

    public ProgressNoGiftButton(Context context) {
        super(context);
        initPaint(context);
//        this.mcontext = context;
    }

    public ProgressNoGiftButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
//        this.mcontext = context;
    }

    public ProgressNoGiftButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint(context);
//        this.mcontext = context;
    }

    /**
     * 初始化画笔
     */
    protected void initPaint(Context context) {
        setProgressDrawable(null);
        setIndeterminateDrawable(null);
        Resources resource = context.getResources();
        downloadingNomalBG = resource
                .getDrawable(R.drawable.progress_downing_nomal);
        mDownLoadProgressDrawable = resource
                .getDrawable(R.drawable.progress_bg_start_nomal);
        // 启动
        startNormalBG = resource
                .getDrawable(R.drawable.progress_bg_start_nomal);
        startPressBG = resource
                .getDrawable(R.drawable.progress_bg_start_pressed);
        mMask = resource.getDrawable(R.drawable.bg_update_dialog);

        installingBG = resource.getDrawable(R.drawable.bg_progress_installing);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Align.CENTER);

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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        // drawButton(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int textSize = calculateTextSize();
        paintText.setTextSize(textSize);
        drawButton(canvas);
    }

    private void drawButton(final Canvas canvas) {
        if (item == null) {
            return;
        }
        int state = AIDLUtils.getGameAppState(item.packageName,
                String.valueOf(item.id), item.versionCode);
        drawRealButton(canvas, state);

    }

    private  void drawRealButton(Canvas canvas, int state) {
        switch (state) {
            case AppManagerCenter.APP_STATE_WAIT:
                // 等待中 下载
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_wait, Color.parseColor("#12b7f5"));
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                // 下载完成
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_install, Color.parseColor("#12b7f5"));
                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                // 安装完成(启动应用)
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_start, Color.parseColor("#12b7f5"));
                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                // 暂停
                drawDownloadPause(canvas, R.string.progress_continue);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
                // 下载中
                drawDownloadBg(canvas);
                break;
            case AppManagerCenter.APP_STATE_UNEXIST:
                Drawable nomal = startNormalBG;
                Drawable press = startPressBG;
                int textId = R.string.progress_btn_download;
                int color = Color.parseColor("#12b7f5");
                // 下载（没有下载的）
                    if (BaseApplication.isThird) {
                        textId = R.string.progress_btn_download;
                    } else {
                        textId = R.string.progress_btn_install;
                    }
                drawNormalBg(canvas, nomal, press, textId, color);
                break;
            case AppManagerCenter.APP_STATE_UPDATE:
                // 更新
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_upload, Color.parseColor("#12b7f5"));
                break;
            case AppManagerCenter.APP_STATE_INSTALLING:
                // 安装中
                drawNormalBg(canvas, installingBG, installingBG,
                        R.string.progress_btn_installing,
                        Color.parseColor("#12b7f5"));
                break;
            case AppManagerCenter.APP_PATCHING:
                // 安装中
                drawNormalBg(canvas, installingBG, installingBG,
                        R.string.progress_btn_patching,
                        Color.parseColor("#12b7f5"));
                break;
        }
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
        // float progress =
        // AppManagerCenter.getDownloadProgress(item.packageName);
//        if (progress > 0.0 && progress < 1.0f) {
//            progress = 1;
//        }
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        downloadingNomalBG.setBounds(0, 0, width, height);
        downloadingNomalBG.draw(canvas);
        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas srcCanvs = new Canvas(src);
        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
        mDownLoadProgressDrawable.draw(srcCanvs);
        // 取背景范围
        bgRect = downloadingNomalBG.getBounds();
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

        if (mPreviewWidth > 0 && h > 0) {
            final Bitmap mask = Bitmap.createBitmap(mPreviewWidth, h,
                    Config.ARGB_8888);
            final Canvas maskCanvas = new Canvas(mask);
            mMask.setBounds(0, 0, mPreviewWidth, h);
            mMask.draw(maskCanvas);
            final Bitmap result = createMaskImage(src, mask);
            canvas.save();
            canvas.drawBitmap(result, 0, 0, null);
            canvas.restore();
        }

        // 画文字
        String text = CommonUtils.paresDownLoadPercent(progress);
        // 文字显示位置
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, text, x, y, Color.parseColor("#12b7f5"));
        canvas.save();
    }

    /**
     * 画下载暂停中图片
     *
     * @param canvas Canvas
     */
    protected void drawDownloadPause(Canvas canvas, int textId) {

        /* 按钮进度条宽度 */
        int mPreviewWidth = -1;
        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
        if (progress > 0.0 && progress < 1.0f) {
            progress = 1;
        }
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        downloadingNomalBG.setBounds(0, 0, width, height);
        downloadingNomalBG.draw(canvas);
        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas srcCanvs = new Canvas(src);
        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
        mDownLoadProgressDrawable.draw(srcCanvs);
        // 取背景范围
        bgRect = downloadingNomalBG.getBounds();
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

        if (mPreviewWidth > 0 && h > 0) {
            final Bitmap mask = Bitmap.createBitmap(mPreviewWidth, h,
                    Config.ARGB_8888);
            final Canvas maskCanvas = new Canvas(mask);
            mMask.setBounds(0, 0, mPreviewWidth, h);
            mMask.draw(maskCanvas);
            final Bitmap result = createMaskImage(src, mask);
            canvas.save();
            canvas.drawBitmap(result, 0, 0, null);
            canvas.restore();
        }

        // 画文字
        String text = getResources().getString(textId);
        // 文字显示位置
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, text, x, y, Color.parseColor("#12b7f5"));
        canvas.save();
    }

    /**
     * 文字显示
     *
     * @param canvas  Canvas画笔
     * @param text  需要绘制的文字
     * @param x  x坐标
     * @param y y坐标
     * @param color  颜色值
     */
    protected void drawText(Canvas canvas, String text, int x, int y, int color) {
        Typeface font = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        paintText.setTypeface(font);
        // 文字大小
        int textSize = calculateTextSize();
        paintText.setTextSize(textSize);
        // paintText.setStrokeWidth(10f);

        paintText.setColor(color);
        canvas.drawText(text, x, y, paintText);
    }

    /**
     * 计算文字大小
     *
     * @return int 文字大小
     */
    private int calculateTextSize() {
        return getHeight() / 2;
    }

    /**
     * 画正常形态图片
     *
     * @param canvas  Canvas
     * @param normal  Drawable
     * @param press  Drawable
     * @param textId   文字id
     * @param textColor  颜色id
     */
    private void drawNormalBg(Canvas canvas, Drawable normal, Drawable press,
                              int textId, int textColor) {

        int height = getHeight();
        int width = getWidth();
        if (onTouching) {
            // 画按住图片背景
            press.setBounds(0, 0, width, height);
            press.draw(canvas);
        } else {
            // 画正常图片背景
            normal.setBounds(0, 0, width, height);
            normal.draw(canvas);
        }
        // 文字显示位置居中
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, getResources().getString(textId), x, y, textColor);
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
        JLog.i("ProgressNoGiftButton","onClick-state="+state);
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                if(BaseApplication.isThird){
                    AppManagerCenter.installGameApk(item);
                }else{
                    String gameAPKFilePath = FileUtils.getGameAPKFilePath(item.id);
                    File gameAPKFile = new File(gameAPKFilePath);
                    if(gameAPKFile.exists()){
                        PackageUtils.installNormal(BaseApplication.curContext, gameAPKFilePath);
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
                MTAUtil.onClickDownload(BaseApplication.curContext, item.name,
                        item.packageName);
                if(this.getContext() instanceof SearchActivity){
                    PrizeStatUtil.onSearchResultItemClick(item.id,item.packageName,item.name,((SearchActivity) this.getContext()).getKeyWord(),false);
                }
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_WAIT:
                AIDLUtils.pauseDownload(item, true);
                break;
            default:
                JLog.i("ProgressNoGiftButton", "onClick=--state不在执行范围-state="+state);
                break;
        }
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
//		return true;
        return super.onTouchEvent(event);
    }


    /**
     * 创建图片遮罩
     *
     * @param source 原图
     * @param mask   遮罩图片 可以实现不同形状的图片
     * @return Bitmap
     */
    public Bitmap createMaskImage(Bitmap source, Bitmap mask) {

        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
                Config.ARGB_8888);

        // 将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        mCanvas.drawBitmap(source, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        // mCanvas.drawBitmap(mask, (source.getWidth() - mask.getWidth()) / 2,
        // (source.getHeight() - mask.getHeight()) / 2, paint);
        paint.setXfermode(null);
        return result;

    }


}