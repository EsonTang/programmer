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
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;


/**
 * 带进度的button
 *
 * @author huanglingjun
 */
public class DownLoadButtonDetail extends ProgressBar {
    protected String TAG = "DownLoadButtonDetail";

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

    /**
     * 下载中以及暂停状态
     */
    private static Drawable downloadDrawable = null;
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

    /**
     * 打开按钮 正常
     */
    private static Drawable openNormalBG = null;

    /**
     * 打开按钮 按下
     */
    private static Drawable openPressBG = null;

    /**
     * 安装中按钮 按下
     */
    private static Drawable installingBG = null;

    private boolean isComment = false;

    public DownLoadButtonDetail(Context context) {
        super(context);
        initPaint(context);
    }

    public DownLoadButtonDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public DownLoadButtonDetail(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        initPaint(context);
    }

    /**
     * 初始化画笔
     */
    protected void initPaint(Context context) {
        setProgressDrawable(null);
        setIndeterminateDrawable(null);
        Resources resource = context.getResources();
        downloadDrawable = resource
                .getDrawable(R.drawable.detail_progress_bg_open);
        mDownLoadProgressDrawable = resource
                .getDrawable(R.drawable.detail_progress_bg);
        // 启动
        startNormalBG = resource
                .getDrawable(R.drawable.detail_progress_bg);
//        startNormalBG = resource
//                .getDrawable(R.drawable.detail_progress_bg_start_nomal);
        startPressBG = resource
                .getDrawable(R.drawable.detail_progress_bg_press);

        mMask = resource.getDrawable(R.drawable.bg_update_dialog);

        openNormalBG = resource.getDrawable(R.drawable.detail_progress_bg_open);
        openPressBG = resource
                .getDrawable(R.drawable.detail_progress_bg_open_press);
        installingBG = resource
                .getDrawable(R.drawable.detail_progress_bg_installing);

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
        invalidate();
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

    private void drawButton(Canvas canvas) {
        if (item == null) {
            return;
        }
        int state = AIDLUtils.getGameAppState(item.packageName, item.id
                + "", item.versionCode);
        switch (state) {
            case AppManagerCenter.APP_STATE_WAIT:
                // 等待下载
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_wait, Color.WHITE);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                if (!isComment) {
                    // 下载完成
                    // if (BaseApplication.isThird) {
                    // 安装完成(启动应用)
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.progress_btn_install, Color.WHITE);
                    // } else {
                    // drawNormalBg(canvas, startNormalBG, startPressBG,
                    // R.string.progress_btn_start, Color.WHITE);
                    // }
                } else {
                    // 安装完成(启动应用)
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.app_comment, Color.WHITE);
                }
                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                if (!isComment) {
                    // 安装完成(启动应用)
                    drawNormalBg(canvas, openNormalBG, openPressBG,
                            R.string.progress_btn_start,
                            Color.parseColor("#12b7f5"));
                } else {
                    // 安装完成(启动应用)
                    drawNormalBg(canvas, startNormalBG, startNormalBG,
                            R.string.app_comment, Color.WHITE);
                }
                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                // 暂停
                drawDownloadBg(canvas, R.string.progress_continue);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
                drawDownloadIingBg(canvas, R.string.progress_pause);
                break;
            case AppManagerCenter.APP_STATE_UNEXIST:
                if (!isComment) {
                    if (BaseApplication.isThird) {
                        // 安装完成(启动应用)
                        drawNormalBg(canvas, startNormalBG, startPressBG,
                                R.string.progress_btn_download, Color.WHITE);
                    } else {
                        // 下载（没有下载的）
                        drawNormalBg(canvas, startNormalBG, startPressBG,
                                R.string.progress_btn_install, Color.WHITE);
                    }
                } else {
                    // 下载（没有下载的）
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.uninstall_app, Color.WHITE);
                }
                break;
            case AppManagerCenter.APP_STATE_UPDATE:
                // 更新
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_upload, Color.WHITE);
                break;
            case AppManagerCenter.APP_STATE_INSTALLING:
                // 安装中
                drawNormalBg(canvas, installingBG, installingBG,
                        R.string.progress_btn_installing, Color.WHITE);
                break;
            case AppManagerCenter.APP_PATCHING:
                // 安装中
                drawNormalBg(canvas, installingBG, installingBG,
                        R.string.progress_btn_patching,
                        Color.WHITE);
                break;
        }
    }

    /**
     * 画下载暂停状态
     *
     * @param canvas Canvas
     */
    protected void drawDownloadBg(Canvas canvas, int textId) {

        /* 按钮进度条宽度 */
        int mPreviewWidth = -1;

        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        downloadDrawable.setBounds(0, 0, width, height);
        downloadDrawable.draw(canvas);
        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas srcCanvs = new Canvas(src);
        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
        mDownLoadProgressDrawable.draw(srcCanvs);
        // 取背景范围
        bgRect = downloadDrawable.getBounds();
        // 算进度条范围
        roundProgressRecr = new RectF(bgRect.left, bgRect.top, bgRect.right,
                bgRect.bottom);

        roundProgressRecr.right = (int) ((progress / 100f) * (roundProgressRecr.right - roundProgressRecr.left))
                + roundProgressRecr.left;
        int w = (int) roundProgressRecr.width();
        if (w > mPreviewWidth) {
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
        drawText(canvas, text, x, y, Color.parseColor("#404040"));
        canvas.save();
    }

    /**
     * 画下载中图片
     *
     * @param canvas
     */
    protected void drawDownloadIingBg(Canvas canvas, int textId) {

        /* 按钮进度条宽度 */
        int mPreviewWidth = -1;

        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
        int height = getHeight();
        int width = getWidth();
        // 画下载中背景
        downloadDrawable.setBounds(0, 0, width, height);
        downloadDrawable.draw(canvas);
        final Bitmap src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas srcCanvs = new Canvas(src);
        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
        mDownLoadProgressDrawable.draw(srcCanvs);
        // 取背景范围
        bgRect = downloadDrawable.getBounds();
        // 算进度条范围
        roundProgressRecr = new RectF(bgRect.left, bgRect.top, bgRect.right,
                bgRect.bottom);

        roundProgressRecr.right = (int) ((progress / 100f) * (roundProgressRecr.right - roundProgressRecr.left))
                + roundProgressRecr.left;
        int w = (int) roundProgressRecr.width();
        if (w > mPreviewWidth) {
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
        String text = getResources().getString(textId)+" "+ CommonUtils.paresDownLoadPercent(progress);
        // 文字显示位置
        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, text, x, y, Color.parseColor("#404040"));
        canvas.save();
    }

    /**
     * 文字显示
     *
     * @param canvas
     * @param text
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

    /**
     * 计算文字大小
     *
     * @return int
     */
    private int calculateTextSize() {
        return getHeight() / 2 - 9;
    }

    /**
     * 画正常形态图片
     *
     * @param canvas Canvas
     * @param normal Drawable
     * @param press Drawable
     * @param textId 文字资源id
     * @param textColor 颜色资源id
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
        int state = AIDLUtils.getGameAppState(item.packageName, item.id
                + "", item.versionCode);
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                AppManagerCenter.installGameApk(item);
                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                if (!isComment) {
                    UIUtils.startGame(item);
                } else {
                    if (commentCallBack != null) {
                        commentCallBack.showCommentDialog();
                    }
                }
                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
            case AppManagerCenter.APP_STATE_UNEXIST:
            case AppManagerCenter.APP_STATE_UPDATE:
                UIUtils.downloadApp(item);
                MTAUtil.onClickDownload(BaseApplication.curContext, item.name, item.packageName);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_WAIT:
                AIDLUtils.pauseDownload(item, true);
                break;
            default:
                JLog.i(TAG, "onClick=--state不在执行范围-state="+state);
                break;
        }
    }

    public interface CommentCallBack {
        void showCommentDialog();
    }

    public CommentCallBack commentCallBack;

    public void setCommentCallBack(CommentCallBack commentCallBack) {
        this.commentCallBack = commentCallBack;
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
        mCanvas.drawBitmap(source, 0, 0, null);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // mCanvas.drawBitmap(mask, 0,(source.getHeight() - mask.getHeight()) /
        // 2, paint);
        mCanvas.drawBitmap(mask, (source.getWidth() - mask.getWidth()) / 2,
                (source.getHeight() - mask.getHeight()) / 2, paint);
        paint.setXfermode(null);
        return result;

    }

    public void isComment(boolean isComment) {
        this.isComment = isComment;
        invalidate();
    }

}