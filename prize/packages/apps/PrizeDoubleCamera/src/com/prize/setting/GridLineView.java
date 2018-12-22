/*******************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：自定义九宫格view
*当前版本：1.0
*作	者：chenyao,fuqiang
*完成日期：2015-04-06
*修改记录：添加一个完全的新自定义view
*修改日期：2015-04-06
*版 本 号：1.0
*修 改 人：chenyao
*********************************************/


package com.prize.setting;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.Util;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingUtils;

public class GridLineView extends View {
	
	protected static final String TAG = "GridLineView";
	Paint paint;
	int screenWidth;
	int screenHeight;
	
	private int pictureRotaionType;
	public static final int PICTURE169 = 0;
	public static final int PICTURE43 = 1;
	private int mMarginBottom;
	
	/*PRIZE-12383-wanzhijuan-2016-03-02-start*/
	private int mPreviewWidth;
	private int mPreviewHeight;
	/*PRIZE-12383-wanzhijuan-2016-03-02-end*/
	private int mNavigationBarHeight;
	
	public GridLineView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public GridLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		if(NavigationBarUtils.checkDeviceHasNavigationBar(getContext())){
			mNavigationBarHeight = NavigationBarUtils.getNavigationBarHeight(getContext());
		}
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.gridline_strokewidth));
		paint.setAlpha(0x77);
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);
		screenWidth = metric.widthPixels;
		screenHeight = metric.heightPixels;
		mMarginBottom = screenHeight - (int)getContext().getResources().getDimension(R.dimen.shutter_group_height);
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT);
//		LogTools.i(TAG, "<onDraw> mPreviewHeight=" + mPreviewHeight + " screenHeight=" + screenHeight + " mMargin=" + mMargin);
		if(Math.abs(((double)mPreviewHeight / mPreviewWidth  - SettingUtils.getFullScreenRatio())) <= Util.ASPECT_TOLERANCE){
			onDraw199(canvas);
		}else if(Math.abs((double)mPreviewHeight / mPreviewWidth  - 1.777) <= Util.ASPECT_TOLERANCE){
			onDraw169(canvas);
		}else{
			onDraw43(canvas);
		}
	}

	protected void onDraw199(Canvas canvas){
		final int width = screenWidth;
		int topMargin = (screenHeight - mPreviewHeight);
		final int height = mPreviewHeight;
		int vertz = height / 3;
		int hortz = width / 3;
		Log.i(TAG, "width: "+width+",height: "+height+",leftMargin: "+topMargin+",vertz: "+vertz+",hortz: "+hortz);
		for (int i = 0; i < 2; i++) {
			canvas.drawLine(0, vertz, width, vertz, paint);
			canvas.drawLine(hortz, topMargin, hortz, height + topMargin, paint);
			vertz += height / 3;
			hortz += width / 3;
		}
	}
	
	protected void onDraw169(Canvas canvas){
		final int width = screenWidth;
		int topMargin = ((CameraActivity)getContext()).getPreviewSurfaceView().getBottom() - mPreviewHeight;
		final int height = mPreviewHeight;
		int vertz = height / 3+topMargin;
		int hortz = width / 3;
		Log.i(TAG, "width: "+width+",height: "+height+",leftMargin: "+topMargin+",vertz: "+vertz+",hortz: "+hortz+",mMarginBottom: "+mMarginBottom+",screenHeight: "+screenHeight);
		for (int i = 0; i < 2; i++) {
			canvas.drawLine(0, vertz, width, vertz, paint);
			canvas.drawLine(hortz, topMargin, hortz, height + topMargin, paint);
			vertz += height / 3;
			hortz += width / 3;
		}
	}
	
	protected void onDraw43(Canvas canvas){
		final int width = screenWidth;
		final int height = mPreviewHeight;
	    int topMargin = mMarginBottom - mPreviewHeight;
		int vertz = height / 3 + topMargin;
		int hortz = width / 3;
		Log.i(TAG, "width: "+width+",height: "+height+",leftMargin: "+topMargin+",vertz: "+vertz+",hortz: "+hortz);
		for (int i = 0; i < 2; i++) {
			canvas.drawLine(0, vertz, width, vertz, paint);
			canvas.drawLine(hortz, topMargin, hortz, height + topMargin, paint);
			vertz += height / 3;
			hortz += width / 3;
		}
	}
	
	public void setPictureRotaionType(int pictureRotaionType){
		this.pictureRotaionType = pictureRotaionType;
		invalidate();
	}

	/*PRIZE-12383-wanzhijuan-2016-03-02-start*/
	public void setPreviewSize(int width, int height) {
		if (width != 0 && height != 0) {
			mPreviewWidth = width;
			mPreviewHeight = height;//height * screenWidth / width;
			invalidate();
		}
	}
	/*PRIZE-12383-wanzhijuan-2016-03-02-end*/
	
}



























