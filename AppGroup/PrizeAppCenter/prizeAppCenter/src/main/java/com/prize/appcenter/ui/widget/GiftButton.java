package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppDetailData.GiftsItem;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.Giftdata;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.AIDLUtils;

/**
 * 带进度的button
 * 
 * @author prize
 * 
 */
public class GiftButton extends ProgressBar {
	private String TAG = "ProgressButton";

	/** * 应用没有下载 */
	public static final int APP_STATE_UNEXIST = 0x1000;
	/** * 应用已被安装 */
	public static final int APP_STATE_INSTALLED = APP_STATE_UNEXIST + 1;

	/** 进度条画笔 */
	private Paint paint = null;
	/** 文字画笔 */
	private Paint paintText = null;
	/** App游戏详情 */
	private AppsItemBean item;
	private GiftsItem giftItem;
	private Giftdata mGiftdata;
	private static Drawable startNormalBG = null;
	/** 启动按钮 按下 */
	private static Drawable startPressBG = null;

	public GiftButton(Context context) {
		super(context);
		initPaint(context);
	}

	public GiftButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint(context);
	}

	public GiftButton(Context context, AttributeSet attrs, int defStyle) {
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
		// modify by huanglingjun 2015-12-2
		startNormalBG = resource
				.getDrawable(R.drawable.progress_bg_start_normal);
		startPressBG = resource.getDrawable(R.drawable.progress_bg_start_press);

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
	 */
	public void setGameInfo(AppsItemBean gameInfo, GiftsItem gift) {
		this.item = gameInfo;
		this.giftItem = gift;
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

	int mGiftState = -1;

	public void setGiftState(int mGiftState) {
		this.mGiftState = mGiftState;
	}

	private void drawButton(Canvas canvas) {
		if (item == null) {
			return;
		}
		int state = AIDLUtils.getGameAppState(item.packageName, item.id
				+ "", item.versionCode);
		if (giftItem != null) {
			if (giftItem.giftType == 0)
				state = AppManagerCenter.APP_LOKUP_GIFT;
			if (giftItem.activationCode != null) {

				state = AppManagerCenter.APP_RECEIVED_GIFT;
			}
		}

		if (mGiftState != -1 && mGiftState != state) {
			state = mGiftState;
		}
		switch (state) {
		case AppManagerCenter.APP_NO_ACTIVATION_CODE:
			// 已经领取
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.gift_no_code, Color.parseColor("#737373"));
			break;
		case AppManagerCenter.APP_RECEIVED_GIFT:
			// 已经领完了 没有了
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.gift_received, Color.parseColor("#737373"));
			break;
		case AppManagerCenter.APP_STATE_INSTALLED:
			// 领取
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.gift_receive, Color.parseColor("#ff7500"));
			break;
		case AppManagerCenter.APP_LOKUP_GIFT:
			// 查看
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.gift_receive, Color.parseColor("#ff7500"));
			break;
		case AppManagerCenter.APP_STATE_UNEXIST:
		case AppManagerCenter.APP_STATE_WAIT:
		case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
		case AppManagerCenter.APP_STATE_DOWNLOADING:
		case AppManagerCenter.APP_STATE_INSTALLING:
		case AppManagerCenter.APP_STATE_DOWNLOADED:
			// 下载（没有下载的） 安装领取
			drawNormalBg(canvas, startNormalBG, startPressBG,
					R.string.gift_receive, Color.parseColor("#ff7500"));
			break;
		}
	}

	/**
	 * 画下载中图片
	 * 
	 * @param canvas
	 */
	protected void drawDownloadBg(Canvas canvas, Bitmap stateIcon) {
		// 进度条进度指示
		float progress = AppManagerCenter.getDownloadProgress(item.packageName);

		// 画文字
		String text = getResources().getString(R.string.progress_btn_progess,
				progress);
		// 文字显示位置
		int x = getWidth() * 7 / 10;
		int y = (int) (getHeight() - (paintText.ascent() + paintText.descent())) / 2;
		drawText(canvas, text, x, y, Color.parseColor("#ff737373"));
		canvas.save();
	}

	/**
	 * 文字显示
	 * 
	 * @param canvas
	 * @param textId
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
		return getHeight() / 2;
	}

	/**
	 * 画正常形态图片
	 * 
	 * @param canvas
	 * @param text
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

}