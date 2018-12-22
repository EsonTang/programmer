package com.prize.prizethemecenter.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.telecom.Log;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.FontDetailActivity;
import com.prize.prizethemecenter.bean.SingleThemeItemBean.ItemsBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;

import org.xutils.ex.DbException;

import java.io.File;
import java.io.IOException;

import me.myfont.fontsdk.FounderFont;
import me.myfont.fontsdk.bean.Font;
import me.myfont.fontsdk.callback.FontChangeCallback;

/**
 * Created by Administrator on 2016/10/12.
 */
public class DownLoadButton extends Button {


    private static final String TAG = "bian";
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
    private static Drawable DownloadMerNormal = null;
    private static Drawable DownloadMerPress = null;

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
     * 主题详情
     */
    private ItemsBean item;
    private boolean isComment = false;
//    private DownloadInfo mDownloadInfo;

    /**
     * type类型： 1.主题下载  2.壁纸下载 3.
     */
    private int type;
    private boolean mProgressEnable = true;

    public static final String RECEIVER_ACTION = "appley_theme_ztefs";
    private float mScale;


    public DownLoadButton(Context context) {
        super(context);
        initPaint(context);
    }

    public DownLoadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public void setProgressEnable(boolean enable) {
        this.mProgressEnable = enable;
    }

    /**
     * type类型： 1.主题下载  2.壁纸下载  3.字体下载
     */
    public void setData(ItemsBean item, int type) {
        this.type = type;
        this.item = item;
        invalidate();
    }

    public void setGameInfo(ItemsBean item) {
        this.item = item;
        this.type = item.getType();
        invalidate();
    }

    private void initPaint(Context context) {
//        setProgressDrawable(null);
//        setIndeterminateDrawable(null);
        Resources resource = context.getResources();
        downloadDrawable = resource
                .getDrawable(R.drawable.detail_progress_bg_open);
        mDownLoadProgressDrawable = resource
                .getDrawable(R.drawable.detail_progress_bg_start_nomal);
        // 启动
        startNormalBG = resource
                .getDrawable(R.drawable.detail_progress_bg_start_nomal);
        startPressBG = resource
                .getDrawable(R.drawable.detail_progress_bg_start_press);

        DownloadMerNormal = resource
                .getDrawable(R.drawable.download_status_normal);
        DownloadMerPress = resource
                .getDrawable(R.drawable.download_status_press);


        mMask = resource.getDrawable(R.drawable.bg_update_dialog);

//        openNormalBG = resource.getDrawable(R.drawable.detail_progress_bg_open);
        openPressBG = resource
                .getDrawable(R.drawable.detail_progress_bg_open_press);
        installingBG = resource
                .getDrawable(R.drawable.detail_progress_bg_installing);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int textSize = calculateTextSize();
        paintText.setTextSize(textSize);
        try {
            drawButton(canvas);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void drawButton(Canvas canvas) throws DbException {
//        drawDownloadBg(canvas, R.string.progress_continue) ;

        if (item == null) return;
        JLog.e(TAG, "status= " + AppManagerCenter.getGameAppState(item, type));
        int status = AppManagerCenter.getGameAppState(item, type);

        switch (status) {
            case AppManagerCenter.APP_STATE_UNEXIST:
                if (!isComment) {
                    drawNormalBg(canvas, startNormalBG, startPressBG, R.string.progress_btn_download, Color.WHITE);
                } else {
                    drawNormalBg(canvas, startNormalBG, startPressBG, R.string.comment_download_hint, Color.WHITE);
                }
                break;
            case AppManagerCenter.APP_STATE_WAIT:
                // 等待下载
                if(mProgressEnable){
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.progress_btn_wait, Color.WHITE);
                }else {
                    drawNormalBg(canvas, DownloadMerNormal, DownloadMerPress,
                            R.string.progress_btn_wait,Color.parseColor("#33cccc"));
                }

                break;
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                if(!mProgressEnable) return;
                if (!isComment) {
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.progress_btn_install, Color.WHITE);
                } else {
                    // 安装完成(启动应用)
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.app_comment, Color.WHITE);
                }
                if (completeCallBack != null) {
                    completeCallBack.onStates();
                    completeCallBack = null;
                }
                break;
            case AppManagerCenter.APP_STATE_INSTALLED:
                if (!isComment) {
                    if (type == 2 && completeCallBack != null) {
                        completeCallBack.onStates();
                        completeCallBack = null;
                        return;
                    }
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.isUseed, Color.WHITE);
                } else {
                    drawNormalBg(canvas, startNormalBG, startPressBG,
                            R.string.app_comment, Color.WHITE);
                }
                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                // 暂停
                if(mProgressEnable){
                    drawDownloadBg(canvas, R.string.progress_continue);
                }else{
                    drawNormalBg(canvas, DownloadMerNormal, DownloadMerPress, R.string.progress_continue, Color.parseColor("#33cccc"));
                }
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
                if(mProgressEnable){
                    drawDownloadBg(canvas, R.string.progress_pause);
                }else{
                    drawNormalBg(canvas, DownloadMerNormal, DownloadMerPress, R.string.progress_pause, Color.parseColor("#33cccc"));
                }
                break;
            case AppManagerCenter.APP_STATE_UPDATE:
                // 更新
                drawNormalBg(canvas, startNormalBG, startPressBG,
                        R.string.progress_btn_upload, Color.WHITE);
                break;
        }
    }

    /**
     * 画下载中图片
     *
     * @param canvas
     */
    private void drawDownloadBg(Canvas canvas, int textId) {

        /** 按钮进度条宽度 */
        int mPreviewWidth = -1;
        //进度条指示
        int progress = DownloadTaskMgr.getInstance().getDownloadProgress(item.getId()+type);
//        int progress = (int) mDownloadInfo.curentProgress;
        JLog.d(TAG, "progress=" + progress);
        int width = getWidth();
        int height = getHeight();
        //画下载中背景
        downloadDrawable.setBounds(0, 0, width, height);
        downloadDrawable.draw(canvas);
        Bitmap src = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas srcCanvas = new Canvas(src);
        mDownLoadProgressDrawable.setBounds(0, 0, width, height);
        mDownLoadProgressDrawable.draw(srcCanvas);
        //去背景范围
        bgRect = downloadDrawable.getBounds();
        //取进度条范围
        Rect rect = new Rect(bgRect.left, bgRect.top, bgRect.right, bgRect.bottom);
        //算进度条范围
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
                    Bitmap.Config.ARGB_8888);
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
//        if(mProgressEnable && mDownloadInfo.currentState != DownloadManager.STATE_PAUSE){
//            drawText(canvas, progress+"%", x, y, Color.parseColor("#404040"));
//        }else{
//            drawText(canvas, "继续下载", x, y, Color.parseColor("#404040"));
//        }
        canvas.save();
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
                Bitmap.Config.ARGB_8888);

        // 将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCanvas.drawBitmap(source, 0, 0, null);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(mask, (source.getWidth() - mask.getWidth()) / 2,
                ((source.getHeight() - mask.getHeight()) / 2 + 2), paint);
        paint.setXfermode(null);
        return result;

    }

    /**
     * 画正常形态图片
     *
     * @param canvas
     * @param textColor
     */
    private void drawNormalBg(Canvas canvas, Drawable normal, Drawable press, int textId, int textColor) {
        int height = getHeight();
        int width = getWidth();
        if (onTouching) {
            //画按住的背景
            press.setBounds(0, 0, width, height);
            press.draw(canvas);
        } else {
            //画正常的背景
            normal.setBounds(0, 0, width, height);
            normal.draw(canvas);
        }

        int x = getWidth() / 2;
        int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
        drawText(canvas, getResources().getString(textId), x, y, textColor);
    }

    /**
     * 文字显示
     *
     * @param canvas
     * @param
     */
    protected void drawText(Canvas canvas, String text, int x, int y, int color) {

        // 文字大小
        int textSize = calculateTextSize();
        if(mProgressEnable){
            paintText.setTextSize(textSize);
        }else{
            mScale = getResources().getDisplayMetrics().density;
            if (mScale == 3.0){
                paintText.setTextSize(34);
            }else {
                paintText.setTextSize(24);
            }
        }

        paintText.setColor(color);
        canvas.drawText(text, x, y, paintText);
    }


    /**
     * 计算文字大小
     *
     * @return
     */
    private int calculateTextSize() {
        return getHeight() / 2 - 9;
    }


    //评论按钮复用
    public void isComment(boolean isComment) {
        this.isComment = isComment;
        invalidate();
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

    private Context mCtx;
    private AlertDialog dialog;

    /**
     * 点击button
     */
    public void OnClick(final Context pContext) {
        mCtx = pContext;
        if (null == item) return;
        int state = AppManagerCenter.getGameAppState(item, type);
        if (ClientInfo.getAPNType(pContext) == ClientInfo.NONET &&state!= AppManagerCenter.APP_STATE_DOWNLOADED) {
            ToastUtils.showToast(R.string.web_error);
            return;
        }
        if (ClientInfo.getAPNType(pContext) != ClientInfo.WIFI && DataStoreUtils.readLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD).
                equals(DataStoreUtils.CHECK_OFF)) {
            //流量下弹框
            dialog = UIUtils.initTraPop(pContext, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnClickDownload(pContext);
                    dialog.dismiss();
                    DataStoreUtils.saveLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD, DataStoreUtils.CHECK_ON);
                }
            }, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    DataStoreUtils.saveLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD, DataStoreUtils.CHECK_OFF);
                }
            });
    }else{
            OnClickDownload(pContext);
        }
    }

    public String md5;
    /**字体是否应用成功*/
    public boolean isSuccess = false;

    public void OnClickDownload(Context pContext) {
        Log.d(TAG, "OnClickDownload ");
        int state = AppManagerCenter.getGameAppState(item, type);
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADED:
                final DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(item.getId()+type);
                if (type == 1) {
                    Intent intent = new Intent(RECEIVER_ACTION);
                    intent.putExtra("themePath", new File(FileUtils.getDir("theme"), item.getId()+type + ".zip").getAbsolutePath());
                    if (task != null && task.loadGame != null) {
                        intent.putExtra("freeName", task.loadGame.getTitle());
                        DownloadTaskMgr.getInstance().setDownlaadTaskState(item.getId(), 1);
                        DownloadTaskMgr.getInstance().setDownloadTaskState(2);
                    }
                    DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, item.getId()+type);
//                    MainApplication.curContext.sendBroadcast(intent);
                    DBUtils.saveOrUpdateDownload(item.getId(), 1);
                    UIUtils.backToLauncher(pContext,intent);
                } else if (type == 3) {
                    //应用字体
                    String path = new File(FileUtils.getDir("font"), item.getId()+3+ ".ttf").getAbsolutePath();
                    File file = new File(path);
                    if (!file.exists())
                        return;
                    try {
                        String frommd5 = MD5Util.getFileMD5String(file);
                        md5 = item.getMd5();
                        JLog.i("hu", "frommd5==" + frommd5 + "--md5==" + md5);
                        if(!frommd5.equals(md5)){
                            ToastUtils.showToast("下载文件出错，请重新下载");
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final Font mFont = new Font();
                    mFont.fontLocalPath = path;
                    mFont.md5 = md5;
                    FontDetailActivity mContext = null;
                    if(pContext instanceof FontDetailActivity){
                        mContext = (FontDetailActivity)pContext;
                    }
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FounderFont.getInstance().changeKooBeeSystemFont(mFont, new FontChangeCallback() {
                                @Override
                                public void onSuccess() {
                                    JLog.i("hu", "  on onSuccess ");
                                    ToastUtils.showToast("字体切换成功");
                                    isSuccess = true;
                                    DBUtils.saveOrUpdateDownload(item.getId(), 3);
                                    if (task != null) {
                                        DownloadTaskMgr.getInstance().setDownlaadTaskState(item.getId(), 3);
                                    }
                                    DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, item.getId() + type);
                                    try {
                                        DBUtils.updateLocalFontTable();
                                    } catch (DbException pE) {
                                        pE.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailed(String msg) {
                                    ToastUtils.showToast("字体切换失败");
                                    isSuccess = false;
                                    JLog.i("hu", "onFailed==" + msg);
                                }
                            });
                        }
                    });
//                    if(pContext instanceof FontDetailActivity){
//                        ((FontDetailActivity) pContext).showProgressBar(isSuccess);
//                    }
                }

                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
            case AppManagerCenter.APP_STATE_UNEXIST:
                AppManagerCenter.startDownload(item, type);
                if (ClientInfo.getAPNType(pContext) != ClientInfo.WIFI) {
                    ToastUtils.showToast(getResources().getString(R.string.traffic_download));
                }
                break;
            case AppManagerCenter.APP_STATE_UPDATE:
                AppManagerCenter.cancelDownload(item,type);
                AppManagerCenter.startDownload(item, type);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOADING:
            case AppManagerCenter.APP_STATE_WAIT:
                AppManagerCenter.pauseDownload(item, true,type);
                break;
        }
    }

    /**
     * *回调
     */
    public static interface CompleteCallBack {
        void onStates();
    }

    private CompleteCallBack completeCallBack;

    public void setCompleteCallBack(CompleteCallBack mCallBack) {
        this.completeCallBack = mCallBack;
    }


}
