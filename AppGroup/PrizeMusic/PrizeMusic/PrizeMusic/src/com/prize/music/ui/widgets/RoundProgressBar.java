package com.prize.music.ui.widgets;

import com.prize.music.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class RoundProgressBar extends ProgressBar {
	/** 加载状态，同下载状态 **/
	private int loadState = 0;
	private static Bitmap pauseBitmap; // 暂停的图片
	private Resources res;
	private float fontSize = 0;
	private Paint p;
	private FontMetrics fm;
	private float roundThickness = 0;

	private int viewH;
	private int viewW;
	private int viewTop;
	private int viewLeft;
	private float mSweep;
	private float mStart = 270; // 默认12点开始
	private Paint pArc;
	private RectF oval;

	public RoundProgressBar(Context context) {
		super(context);
		init();
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		if (null == p) {
			p = new Paint();
		}
		if (null == fm) {
			fm = p.getFontMetrics();
		}
		if (null == res) {
			res = getResources();
		}

		if (null == pauseBitmap) {
			pauseBitmap = BitmapFactory.decodeResource(res,
					R.drawable.ic_launcher);
		}
		if (0 == fontSize) {
			fontSize = res.getDimension(R.dimen.audio_player_artwork_padding);
		}

		if (0 == roundThickness) {
			roundThickness = res
					.getDimension(R.dimen.audio_player_artwork_padding);
		}

		oval = new RectF();
		pArc = new Paint();
		pArc.setStyle(Paint.Style.STROKE);
		pArc.setStrokeWidth(roundThickness);
		pArc.setAntiAlias(true); // 消除锯齿
	}

	public void setLoadState(int state) {
		loadState = state;
	}

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		mSweep = progress * 360 / 100;
		invalidate();
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		if ((0 == viewH) || (0 == viewW)) {
			viewH = getHeight();
			viewW = getWidth();
			viewTop = getTop();
			viewLeft = getLeft();
			float padding = 2;

			oval.set((viewTop + padding + roundThickness),
					(viewLeft + padding + roundThickness), (viewW
							- roundThickness - padding), (viewW
							- roundThickness - padding));
		}
		// 画底色圈圈
		pArc.setColor(0xcc636363);
		canvas.drawArc(oval, 0, 360, false, pArc);
		// 画圆弧
		pArc.setColor(0xff71c106);
		canvas.drawArc(oval, mStart, mSweep, false, pArc);

		String text = null;
		Bitmap drawBP = null;
		// if (AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE == loadState) {
		// drawBP = pauseBitmap;
		// } else if (AppManagerCenter.APP_STATE_DOWNLOADED == loadState) {
		// text = res.getString(R.string.install);
		// } else if (AppManagerCenter.APP_STATE_DOWNLOADING == loadState) {
		// text = getProgress() + "%";
		// } else if (AppManagerCenter.APP_STATE_INSTALLING == loadState) {
		// text = res.getString(R.string.my_intalling);
		// } else if (AppManagerCenter.APP_STATE_WAIT == loadState) {
		// text = res.getString(R.string.my_waiting);
		// }
		if (null != text) {
			p.setTypeface(Typeface.DEFAULT_BOLD);
			p.setColor(Color.WHITE);

			p.setTextSize(fontSize);
			int textH = (int) (fm.bottom - fm.top);
			int textW = (int) p.measureText(text);
			canvas.drawText(text, (viewW >> 1) - (textW >> 1) + 2, (viewH >> 1)
					+ (textH >> 1) + 2, p);
		}
		if (null != drawBP) {
			int top = (viewH - drawBP.getHeight()) >> 1;
			int left = (viewW - drawBP.getWidth()) >> 1;
			canvas.drawBitmap(drawBP, left, top, null);
		}
	}
}
