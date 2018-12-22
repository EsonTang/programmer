package com.prize.appcenter.ui.widget;

import android.app.Activity;
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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import com.prize.app.BaseApplication;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.threads.SingleThreadExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.PointsLotteryUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 带进度的button，积分系统-赚取积分中使用到
 *
 * @author prize
 */
public class ProgressButtonPoints extends ProgressBar {
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
    //	private int state;

    /**
     * 带进度的背景
     */
    private static Drawable downloadBm = null;
    /**
     * 暂停图标
     */
    private static Bitmap pauseBm = null;
    /**
     * 继续图标
     */
    private static Bitmap continueBm = null;

    /**
     * 安装有礼按钮 正常
     */
    private static Drawable installGiftNormalBG = null;
    /**
     * 安装有礼按钮 按下
     */
    private static Drawable installGiftPressBG = null;

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
    private static Drawable gettedBG = null;

    public ProgressButtonPoints(Context context) {
        super(context);
        initPaint(context);

    }

    public ProgressButtonPoints(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public ProgressButtonPoints(Context context, AttributeSet attrs, int defStyle) {
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

        // 启动
        startNormalBG = resource.getDrawable(
                R.drawable.progress_bg_start_normal);
        startPressBG =
                resource.getDrawable(R.drawable.progress_bg_start_press);

        downloadingNomalBG = resource
                .getDrawable(R.drawable.progress_downing_nomal);
        //		downloadDrawable = resource.getDrawable(R.drawable.progress_bg_open);
        mDownLoadProgressDrawable = resource
                .getDrawable(R.drawable.progress_bg_start_nomal);
        // 启动
        installGiftNormalBG = resource
                .getDrawable(R.drawable.installgift_nomal);
        installGiftPressBG = resource.getDrawable(R.drawable.installgift_press);
        // 启动
        startNormalBG = resource
                .getDrawable(R.drawable.progress_bg_start_nomal);
        startPressBG = resource
                .getDrawable(R.drawable.progress_bg_start_pressed);
        // mMask = resource.getDrawable(R.drawable.download_progress_mask);

        // mMask = resource.getDrawable(R.drawable.progress_bg_start_nomal);
        mMask = resource.getDrawable(R.drawable.bg_update_dialog);

        openNormalBG = resource.getDrawable(R.drawable.progress_bg_open);
        openPressBG = resource.getDrawable(R.drawable.progress_bg_open_press);
        installingBG = resource.getDrawable(R.drawable.bg_progress_installing);
        gettedBG = resource.getDrawable(R.drawable.bg_progress_getted);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Align.CENTER);

    }

    /**
     * 设置Item
     *
     * @param gameInfo
     * @param state
     */
    public void setGameInfo(AppsItemBean gameInfo, int state) {
        this.item = gameInfo;
        //		this.state = state;
        invalidate();
    }

    /**
     * 设置Item
     *
     * @param gameInfo
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
        if (PointsLotteryUtils.isInHashSet(item.packageName, true)) {
            // 领取中
            drawNormalBg(canvas, installingBG, installingBG,
                    R.string.progress_btn_getting,
                    Color.parseColor("#12b7f5"));
            setEnabled(false);
            return;
        }
        if (PointsLotteryUtils.isInHashSet(item.packageName, false)) {
            // 已领取
            drawNormalBg(canvas, gettedBG, gettedBG,
                    R.string.progress_btn_geted,
                    Color.parseColor("#6c6c6c"));
            setEnabled(false);
            return;
        }
        setEnabled(true);
        int state = AIDLUtils.getGameAppState(item.packageName,
                String.valueOf(item.id), item.versionCode);
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
                int color1 = Color.parseColor("#ff7f14");
                int textId1 = R.string.open_receive;
                drawNormalBg(canvas, installGiftNormalBG, installGiftPressBG, textId1, color1);
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
//                if (item.giftCount > 0) {
//                    nomal = installGiftNormalBG;
//                    press = installGiftPressBG;
//                    textId = R.string.install_Gift;
//                    color = Color.parseColor("#ff7f14");
//                } else {
                if (BaseApplication.isThird) {
                    textId = R.string.progress_btn_download;
                } else {
                    textId = R.string.progress_btn_install;
                }
//                }
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
        }
    }

    /**
     * 画下载中图片
     *
     * @param canvas
     */
    protected void drawDownloadBg(Canvas canvas) {

        /** 按钮进度条宽度 */
        int mPreviewWidth = -1;
        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
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
        // paintText.setStrokeWidth(10f);

        paintText.setColor(color);
        canvas.drawText(text, x, y, paintText);
    }

    /**
     * 计算文字大小
     *
     * @return
     */
    private int calculateTextSize() {
        return getHeight() / 2;
    }

    /**
     * 画正常形态图片
     *
     * @param canvas
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
     *
     * @param
     */
    public void onClick() {
        if (null == item) {
            return;
        }
        if (PointsLotteryUtils.isInHashSet(item.packageName, true) || PointsLotteryUtils.isInHashSet(item.packageName, false)) {
            // 领取中，已领
            setEnabled(false);
            return;
        }
        setEnabled(true);
        int state = AIDLUtils.getGameAppState(item.packageName, item.id + "",
                item.versionCode);
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                AppManagerCenter.installGameApk(item);
                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                    UIUtils.gotoActivity(LoginActivityNew.class, (Activity) getContext());
                    return;
                }
                PointsLotteryUtils.add2HashSet(item.packageName, true);
                invalidate();
                PointsLotteryUtils.requstGetPoints(item, new PointsLotteryUtils.ResultLinstener() {
                    @Override
                    public void getresult(String result, final AppsItemBean bean) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                int code = new JSONObject(result).getInt("code");
                                final String msg = new JSONObject(result).getString("msg");

                                //code=0，表示领取成功。1表示交互成功，但是不能够被领取（eg：已经领取了6个）
                                if (0 == code || 1 == code) {
                                    PointsLotteryUtils.remoceFromHashSet(bean.packageName, true);
                                    XutilsDAO.storeEarnPointsBean(bean.packageName);
                                    if (0 == code) {
                                        PointsLotteryUtils.add2HashSet(bean.packageName, false);
                                    }
                                    SingleThreadExecutor.getInstance().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    postInvalidate();
                                                    ToastUtils.showToast(msg);
                                                }
                                            }, 1000);
                                        }
                                    });
                                    SingleThreadExecutor.getInstance().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UIUtils.startGame(bean);
                                                }
                                            }, 2000);
                                        }
                                    });


                                } else {
                                    PointsLotteryUtils.remoceFromHashSet(bean.packageName, true);
                                    PointsLotteryUtils.remoceFromHashSet(bean.packageName, false);
                                    if (!TextUtils.isEmpty(msg)) {
                                        ToastUtils.showToast(msg);
                                    } else {
                                        ToastUtils.showToast(R.string.net_error);
                                    }
                                    invalidate();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtils.showToast(R.string.net_error);
                            PointsLotteryUtils.remoceFromHashSet(item.packageName, true);
                            PointsLotteryUtils.remoceFromHashSet(item.packageName, false);
                            invalidate();
                        }
                    }
                });
                break;


            case AppManagerCenter.APP_STATE_UNEXIST:
                MTAUtil.onPointDownloadInstall(item.name, item.packageName);
                ToastUtils.showToast(R.string.rule);
            case AppManagerCenter.APP_STATE_UPDATE:
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                UIUtils.downloadApp(item);
                MTAUtil.onClickDownload(BaseApplication.curContext, item.name,
                        item.packageName);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_WAIT:
                AIDLUtils.pauseDownload(item, true);
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

    /**
     * 画下载暂停中图片
     *
     * @param canvas
     */
    protected void drawDownloadPause(Canvas canvas, int textId) {

        /** 按钮进度条宽度 */
        int mPreviewWidth = -1;
        // 进度条进度指示
        float progress = AIDLUtils.getDownloadProgress(item.packageName);
        if (progress > 0.0 && progress < 1.0f) {
            progress = 1;
        }
        JLog.i("ProgressButton", "drawDownloadBg-progress=" + progress);
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

}