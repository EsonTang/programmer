package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;

/**
 * 带进度的button
 * 
 * @author prize
 * 
 */
public class DownLoadButton extends ProgressBar {
	/** 进度条画笔 */
	protected Paint paint = null;
	/** 文字画笔 */
	protected Paint paintText = null;
	/** 进度条范围 */
	protected RectF roundProgressRecr = null;
	/** 背景范围 */
	protected Rect bgRect = null;
	/** App游戏详情 */
	protected AppsItemBean item;

	// /** 带进度的背景 */
	// protected static Drawable downloadBm = null;
	// /** 暂停图标 */
	// protected static Bitmap pauseBm = null;
	// /** 继续图标 */
	// protected static Bitmap continueBm = null;
	//
	// /** 安装按钮 正常 */
	// protected static Drawable installNormalBG = null;
	// /** 安装按钮 按下 */
	// protected static Drawable installPressBG = null;
	/** 启动按钮 正常 */
	protected static Drawable startNormalBG = null;
	/** 启动按钮 按下 */
	protected static Drawable startPressBG = null;

	public DownLoadButton(Context context) {
		super(context);
		initPaint(context);
	}

	public DownLoadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint(context);
	}

	public DownLoadButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint(context);
	}

	/**
	 * 初始化画笔
	 */
	protected void initPaint(Context context) {
		setProgressDrawable(null);
		setIndeterminateDrawable(null);
		// if (downloadBm == null) {
		Resources resource = context.getResources();
		// downloadBm =
		// resource.getDrawable(R.drawable.progress_bg_download);
		// downloadBm = resource
		// .getDrawable(R.drawable.progress_bg_install_normal);
		//
		// // 暂停图片
		// pauseBm = BitmapFactory.decodeResource(resource,
		// R.drawable.progress_btn_pause);
		// // 继续图片
		// continueBm = BitmapFactory.decodeResource(resource,
		// R.drawable.progress_btn_continue);
		// // 安装
		// installNormalBG = resource
		// .getDrawable(R.drawable.progress_bg_install_normal);
		// installPressBG = resource
		// .getDrawable(R.drawable.progress_bg_install_press);
		// 启动
		startNormalBG = resource
				.getDrawable(R.drawable.progress_bg_start_normal);
		startPressBG = resource.getDrawable(R.drawable.progress_bg_start_press);
		// }

		paint = new Paint();
		// paint.setColor(getResources().getColor(R.color.progress_btn_progress));
		paint.setAntiAlias(true);

		paintText = new Paint();
		paintText.setAntiAlias(true);
		paintText.setTextAlign(Align.CENTER);

	}

	/**
	 * 设置Item
	 * 
	 * @param gameInfo
	 */
	public void setGameInfo(AppsItemBean gameInfo) {
		this.item = gameInfo;
		invalidate();
		// postInvalidate();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		drawButton(canvas);
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
					R.string.progress_btn_wait, Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_DOWNLOADED:
			// 下载完成
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_start, Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_INSTALLED:
			// 安装完成(启动应用)
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_start, Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
			// 暂停
			drawPauseBg(canvas, startNormalBG, R.string.progress_continue,
					Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_DOWNLOADING:
			// 下载中
			drawPauseBg(canvas, startNormalBG, R.string.progress_pause,
					Color.parseColor("#ff33cccc"));

			break;
		case AppManagerCenter.APP_STATE_UNEXIST:
			// 下载（没有下载的）
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_install,
					Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_UPDATE:
			// 更新
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_upload, Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_STATE_INSTALLING:
			// 安装中
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_installing,
					Color.parseColor("#ff33cccc"));
			break;
		case AppManagerCenter.APP_PATCHING:
			// 合成中
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.progress_btn_patching,
					Color.parseColor("#ff33cccc"));
			break;
		}
	}

	// /**
	// * 画下载中图片
	// *
	// * @param canvas
	// */
	// protected void drawDownloadBg(Canvas canvas, Bitmap stateIcon) {
	// // 进度条进度指示
	// int progress = AppManagerCenter.getDownloadProgress(item.packageName);
	// int height = getHeight();
	// int width = getWidth();
	// // 画下载中背景
	// downloadBm.setBounds(0, 0, width, height);
	// downloadBm.draw(canvas);
	//
	// // 取背景范围
	// // bgRect = downloadBm.getBounds();
	// // 算进度条范围
	// /*
	// * roundProgressRecr = new RectF(bgRect.left + 1, bgRect.top + 1,
	// * bgRect.right - 1, bgRect.bottom - 1f);
	// *
	// * roundProgressRecr.right = (int) ((progress / 100f) *
	// * (roundProgressRecr.right - roundProgressRecr.left)) +
	// * roundProgressRecr.left;
	// *
	// * Shader shader = new LinearGradient(roundProgressRecr.right,
	// * roundProgressRecr.top, roundProgressRecr.right,
	// * roundProgressRecr.bottom, new int[] { 0XFF02A2EB, 0XFF02A2EB }, new
	// * float[] { 0, 1 }, Shader.TileMode.MIRROR); paint.setShader(shader);
	// * // 绘制进度条 canvas.drawRoundRect(roundProgressRecr, 3.0f, 3.0f, paint);
	// */
	// // 画状态按钮
	// int top = (height - stateIcon.getHeight()) / 2;
	// int left = (width - stateIcon.getWidth()) / 2;
	// // int left = 1;//
	// canvas.drawBitmap(stateIcon, left, top, null);
	//
	// // 画文字
	// String text = getResources().getString(R.string.progress_btn_progess,
	// progress);
	// // 文字显示位置
	// int x = getWidth() * 7 / 10;
	// int y = (int) (getHeight() - (paintText.ascent() + paintText.descent()))
	// / 2;
	// // drawText(canvas, text, x, y, Color.WHITE);
	// canvas.save();
	// }

	/**
	 * 文字显示
	 * 
	 * @param canvas
	 * @param text
	 */
	protected void drawText(Canvas canvas, String text, int x, int y, int color) {
		// 文字大小
		int textSize = calculateTextSize();
		paintText.setTextSize(textSize);

		paintText.setColor(color);
		canvas.drawText(text, x, y, paintText);
	}

	/**
	 * 计算文字大小
	 * 
	 * @return
	 */
	private int calculateTextSize() {
		return getHeight() / 2 - 1;
	}

	/**
	 * 画正常形态图片
	 * 
	 * @param canvas
	 * @param normal
	 * @param press
	 * @param textId
	 * @param textColor
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
	 */
	public int onClick() {
		if (null == item) {
			return 0;
		}
		int state = AIDLUtils.getGameAppState(item.packageName, item.id
				+ "", item.versionCode);
		switch (state) {
		case AppManagerCenter.APP_STATE_DOWNLOADED:
			AppManagerCenter.installGameApk(item);
			break;
		case AppManagerCenter.APP_STATE_INSTALLED:
			UIUtils.startGame(item);
			break;
		case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
		case AppManagerCenter.APP_STATE_UNEXIST:
		case AppManagerCenter.APP_STATE_UPDATE:
			UIUtils.downloadApp(item);
			break;
		case AppManagerCenter.APP_STATE_DOWNLOADING:
		case AppManagerCenter.APP_STATE_WAIT:
			AppManagerCenter.pauseDownload(item, true);
			break;
		}

		int lastState = AIDLUtils.getGameAppState(item.packageName,
				item.id + "", item.versionCode);
		return lastState;
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
	 * 画暂停形态图片
	 * 
	 * @param canvas
	 * @param normal
	 */
	private void drawPauseBg(Canvas canvas, Drawable normal, int textId,
			int textColor) {
		int height = getHeight();
		int width = getWidth();
		normal.setBounds(0, 0, width, height);
		normal.draw(canvas);
		// 文字显示位置居中
		int x = getWidth() / 2;
		int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
		drawText(canvas, getResources().getString(textId), x, y, textColor);
	}
}