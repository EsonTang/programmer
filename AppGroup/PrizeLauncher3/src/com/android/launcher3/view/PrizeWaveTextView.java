/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：包装BubleTextView
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import com.android.download.DownLoadService;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ImageUtils;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;

/**
 * 目的：此类主要的目的是波浪下载进度
 * 
 * @author Administrator
 * 
 */
@RemoteView
public class PrizeWaveTextView extends BubbleTextView {
	public PrizeWaveTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private Paint paint;
	private int roundColor;

	private int roundProgressColor;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;

	/**
	 * 最大进度
	 */
	private int max;
	Context mContext;

	private float mProgress = 0;

	private int mDownLoadState = -1;

	public float getDownLoadState() {
		return mDownLoadState;
	}

	public void setDownLoadState(int mDownLoadState) {
		this.mDownLoadState = mDownLoadState;
	}

	public float getProgress() {
		return mProgress;
	}

	private Drawable bg;

	private Drawable mMask;
	private Drawable mPauseDrawable;
	private Bitmap mMaskBitmap;
	private Bitmap mBitmapBg;

	public void setProgress(float mProgress) {
		this.mProgress = mProgress;
		this.invalidate();
	}

	public PrizeWaveTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	
	public void initThemeMaskBitmap() {
		bg = getResources().getDrawable(R.drawable.blankbackground);
		mMask = getResources().getDrawable(R.drawable.wall_local_press);
		mMaskBitmap = ImageUtils.drawableToBitmap1(mMask);

		if (LqShredPreferences.isLqtheme(mContext)) {
			mMaskBitmap = LqService.getInstance().getIcon(null,
					mMaskBitmap, true, "");
		}
			
		Bitmap source = ImageUtils.drawableToBitmap1(bg);
		if(mMaskBitmap !=null)
		mBitmapBg = ImageUtils.createMaskImage(source, mMaskBitmap);
		
	}
	public PrizeWaveTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources r = context.getResources();
		mContext = context;
		initThemeMaskBitmap();
		mPauseDrawable = getResources().getDrawable(R.drawable.zanting);

		paint = new Paint();

		// 获取自定义属性和默认值
		roundColor = 0xe07b7a7a;
		roundProgressColor = 0xe0b7b5b5;
		roundWidth = 80;
		max = 100;

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		if(mBitmapBg ==null) {
			return;
		}
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		int w = mBitmapBg.getWidth();
		int h = mBitmapBg.getHeight();
		canvas.translate(scrollX, scrollY);
		int x = (getWidth() - w) / 2;
		int y = getPaddingTop();
		ItemInfo info = (ItemInfo) getTag();
		if (info != null && info.fromAppStore == 1) {
			final Bitmap mask = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			final Canvas maskCanvas = new Canvas(mask);
			mMask.setBounds(0, 0, w, h - (int) (h * (info.progress / 100f)));
			mMask.draw(maskCanvas);
			final Bitmap result = ImageUtils.createMaskImage(mBitmapBg, mask);
			canvas.save();
			canvas.drawBitmap(result, x, y, null);
			canvas.restore();
			if (info.down_state == DownLoadService.STATE_DOWNLOAD_PAUSE) {
				int pW = mPauseDrawable.getIntrinsicWidth();
				int pH = mPauseDrawable.getIntrinsicHeight();
				mPauseDrawable.setBounds(0, 0, pW, pH);
				canvas.save();
				int pX = x + w / 2 - pW / 2;
				int pY = y + h / 2 - pH / 2;
				canvas.translate(pX, pY);
				mPauseDrawable.draw(canvas);
				canvas.restore();

			}
		}

		canvas.restore();
		canvas.translate(-scrollX, -scrollY);
	}
	
	@Override
	public void setSingleLine() {
		// TODO Auto-generated method stub
		setSingleLine(true);
	}
}
