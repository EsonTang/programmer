package com.example.longshotscreen.ui;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import com.example.longshotscreen.utils.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.longshotscreen.R;

import com.example.longshotscreen.utils.ImageMap2d;
import com.example.longshotscreen.utils.ImageRangInfo;
import com.example.longshotscreen.utils.SpanFill2d;
import com.example.longshotscreen.utils.SuperShotUtils;

public class PathEffectsImageView extends ImageView {
	public static boolean mIsProcessing = false;
	private Handler RelayoutWindowHandler;
	private boolean bIsCircle = false;
	private boolean bIsDraw = false;
	private Handler exitLayoutHandler;
	private boolean mBIsExit = false;
	private boolean mBReShow = false;
	private boolean mBShouldSave = false;
	private Bitmap mBackBitmap;
	private Canvas mBackCanvas = new Canvas();
	private Paint mBackgroundPaint = new Paint();
	protected boolean mBisDown = false;
	protected boolean mBisTouch = true;
	protected LinearLayout mBottomStatusBar;
	private boolean mConfigurationChanged;
	protected Context mContext;
	private Bitmap mDestBitmap;
	protected int mDestH = 0;
	protected int mDestW = 0;
	private PathEffect[] mEffects;
	protected TextView mExit;
	private TextView mGuide;
	private PathEffectHandler mHandler = new PathEffectHandler();
	private ImageMap2d mImageMap2d;
	private ImageMap2d mImageMap2dOper;
	private ImageRangInfo mImgRangInfo;
	private boolean mIsDebug = true;
	private boolean mIsProcessEnd = true;
	private boolean mIsProcessEndExKey = false;
	protected boolean mIsScreenOn = true;
	private Bitmap mMidBitamp;
	private Canvas mMidCanvas = new Canvas();
	private Paint mMiddlePaint = new Paint();
	private float mMinDistance = 0.0F;
	protected int mOffsetX = 0;
	protected int mOffsetY = 0;
	private Paint mPaint;
	private Path mPath = new Path();
	private Path mPathEx = new Path();
	private Path mPathReshow = new Path();
	private float mPhase;
	private Point mPixelSeed;
	private ProgressBar mProgress;
	protected TextView mRedraw;
	protected TextView mSave;
	protected ScreenReceiver mScrReceiver;
	private Bitmap mScreenBitmap;
	private int mScreenHeight;
	private int mScreenWidth;
	private SpanFill2d mSpanFill2d;
	private int mX;
	private int mY;
	
	//syc add start
	private boolean ismove = false;
	private boolean multipoint = false;
	private int mcount = 0;
	//syc add end
	public PathEffectsImageView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.mContext = paramContext;
		this.mEffects = new PathEffect[6];
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setDither(true);
		this.mPaint.setStrokeWidth(4.0F);
		this.mPaint.setColor(-1);
		makeEffects(this.mEffects, this.mPhase);
		this.mPaint.setPathEffect(this.mEffects[4]);
		this.mPaint.setStyle(Paint.Style.STROKE);
		if (this.mMinDistance == 0.0F)
			this.mMinDistance = 60.0F;
	}

	private static void makeEffects(PathEffect[] paramArrayOfPathEffect,
			float paramFloat) {
		paramArrayOfPathEffect[0] = null;
		paramArrayOfPathEffect[1] = new CornerPathEffect(5.0F);
		float[] arrayOfFloat = new float[4];
		arrayOfFloat[0] = 5.0F;
		arrayOfFloat[1] = 5.0F;
		arrayOfFloat[2] = 5.0F;
		arrayOfFloat[3] = 5.0F;
		paramArrayOfPathEffect[2] = new DashPathEffect(arrayOfFloat, paramFloat);
		paramArrayOfPathEffect[3] = new PathDashPathEffect(makePathDash(),
				12.0F, paramFloat, PathDashPathEffect.Style.ROTATE);
		paramArrayOfPathEffect[4] = new ComposePathEffect(
				paramArrayOfPathEffect[2], paramArrayOfPathEffect[1]);
		paramArrayOfPathEffect[5] = new ComposePathEffect(
				paramArrayOfPathEffect[3], paramArrayOfPathEffect[1]);
	}

	private static Path makePathDash() {
		Path localPath = new Path();
		localPath.moveTo(4.0F, 0.0F);
		localPath.lineTo(0.0F, -4.0F);
		localPath.lineTo(8.0F, -4.0F);
		localPath.lineTo(12.0F, 0.0F);
		localPath.lineTo(8.0F, 4.0F);
		localPath.lineTo(0.0F, 4.0F);
		return localPath;
	}

	private void processHistoryPoint(MotionEvent paramMotionEvent) {
		for (int i = 0; i < paramMotionEvent.getHistorySize(); i++)
			touch_move((int) paramMotionEvent.getHistoricalX(i),
					(int) paramMotionEvent.getHistoricalY(i));
	}

	private void sendExitMessage() {
		this.exitLayoutHandler.sendEmptyMessage(0x45c);
	}

	private void sendReLayoutWindowMessage() {
		this.RelayoutWindowHandler.sendEmptyMessage(0x13);
	}

	private void setmProgressStatus(boolean paramBoolean) {
		if (mProgress != null) {
			mProgress.setVisibility(paramBoolean ? View.VISIBLE : View.GONE);
		}
	}

	private void touch_move(int paramInt1, int paramInt2) {
		if (this.mIsDebug) {
			Log.i("PathEffectsImageView","[touch_move] x = "+paramInt1+"   y = "+paramInt2);
		}
		if (this.mBisDown){
			this.mImgRangInfo.checkRang(paramInt1, paramInt2);
			this.mImgRangInfo.addPathList(paramInt1, paramInt2);
			this.mImgRangInfo.addPathList((paramInt1 + this.mX) / 2,
					(paramInt2 + this.mY) / 2);
			this.mPath.quadTo(this.mX, this.mY, (paramInt1 + this.mX) / 2,
					(paramInt2 + this.mY) / 2);
			this.mX = paramInt1;
			this.mY = paramInt2;
		}
	}

	private void touch_start(int paramInt1, int paramInt2) {
		if (this.mIsDebug) {
			Log.i("PathEffectsImageView","[touch_start] x = "+paramInt1+"   y = "+paramInt2);
		}
		this.mPath.reset();
		this.mPath.moveTo(paramInt1, paramInt2);
		this.mPath.lineTo(paramInt1 + 1, paramInt2 + 1);
		if(this.mImgRangInfo.mBegin != null){
		this.mImgRangInfo.mBegin.setPixelCoor(paramInt1, paramInt2);
		}
		this.mImgRangInfo.initRang(paramInt1, paramInt2);
		this.mImgRangInfo.addPathList(paramInt1, paramInt2);
		this.mX = paramInt1;
		this.mY = paramInt2;
		setIsDown(true);
		setIsProcessEnd(false);
		this.mIsProcessEndExKey = true;
		if (this.mGuide != null){
		this.mGuide.setVisibility(View.GONE);
		}
		
	}

	private void touch_up(int paramInt1, int paramInt2) {
		this.mPath.lineTo(paramInt1, paramInt2);
		this.mImgRangInfo.mEnd.setPixelCoor(paramInt1, paramInt2);
		this.mImgRangInfo.checkRang(paramInt1, paramInt2);
		this.mImgRangInfo.addPathList(paramInt1, paramInt2);
		if (this.mIsDebug) {
			Log.i("PathEffectsImageView","[touch_up] x = "+paramInt1+"   y = "+paramInt2);
			Log.i("PathEffectsImageView",
					"touch_up--="
							+ SuperShotUtils.getDistance(
									this.mImgRangInfo.mBegin,
									this.mImgRangInfo.mEnd));
			Log.i("PathEffectsImageView", "touch_up--mMinDistance="
					+ this.mMinDistance);
		}
		if (!ismove && (SuperShotUtils.getDistance(this.mImgRangInfo.mBegin,this.mImgRangInfo.mEnd) <= this.mMinDistance))	
			
		this.mPath.close();
		this.mMidCanvas.drawPath(this.mPath, this.mMiddlePaint);
		this.mBackCanvas.drawPath(this.mPath, this.mBackgroundPaint);
		setTouchEnable(false);
		setIsDown(false);
		setIsReShow(true);
		
		if (this.mGuide != null)
			this.mGuide.setVisibility(View.GONE);
		if (this.mIsProcessEndExKey) {
			this.mIsProcessEndExKey = false;
			setmProgressStatus(true);
			mIsProcessing = true;
			if (this.mConfigurationChanged) {
				this.mConfigurationChanged = false;
				sendExitMessage();
			}
			processImg();
			return;
		}
		setmProgressStatus(false);
		this.mHandler.sendEmptyMessage(2);
	}

	public void getButton(LinearLayout paramLinearLayout,
			TextView paramTextView1, TextView paramTextView2,
			TextView paramTextView3) {
		this.mBottomStatusBar = paramLinearLayout;
		this.mExit = paramTextView1;
		this.mRedraw = paramTextView2;
		this.mSave = paramTextView3;
	}

	public void getGuideTextView(TextView paramTextView) {
		this.mGuide = paramTextView;
	}

	public boolean getImageProcessStatus() {
		return this.mIsProcessEnd;
	}

	public void getProgressBar(ProgressBar paramProgressBar) {
		this.mProgress = paramProgressBar;
		this.mProgress.setVisibility(View.GONE);
	}

	public void init(int paramInt1, int paramInt2, Bitmap paramBitmap) {
		this.mScreenWidth = paramInt1;
		this.mScreenHeight = paramInt2;
		this.mScreenBitmap = paramBitmap;
		setBackgroundColor(this.mContext.getResources().getColor(
				R.color.translucent));
		this.mBackBitmap = Bitmap.createBitmap(this.mScreenWidth,
				this.mScreenHeight, Bitmap.Config.ARGB_8888);
		this.mBackCanvas.setBitmap(this.mBackBitmap);
		this.mBackgroundPaint.setColor(-1);
		this.mBackgroundPaint.setStrokeWidth(1.0F);
		this.mBackgroundPaint.setAntiAlias(true);
		this.mBackgroundPaint.setStyle(Paint.Style.STROKE);
		this.mBackgroundPaint.setDither(true);
		this.mMidBitamp = Bitmap.createBitmap(this.mScreenWidth,
				this.mScreenHeight, Bitmap.Config.ARGB_8888);
		this.mMidCanvas.setBitmap(this.mMidBitamp);
		this.mMiddlePaint.setColor(0xffff0000);
		this.mMiddlePaint.setStrokeWidth(1.0F);
		this.mMiddlePaint.setAntiAlias(true);
		this.mMiddlePaint.setStyle(Paint.Style.FILL);
		this.mMiddlePaint.setDither(true);
		this.mImgRangInfo = new ImageRangInfo();
		this.mSpanFill2d = new SpanFill2d();
		this.mImageMap2d = new ImageMap2d(this.mBackBitmap,
				this.mBackBitmap.getWidth(), this.mBackBitmap.getHeight());
		this.mImageMap2dOper = new ImageMap2d(this.mMidBitamp,
				this.mMidBitamp.getWidth(), this.mMidBitamp.getHeight());
		this.mImgRangInfo.getGap((int) this.mPaint.getStrokeWidth());
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("android.intent.action.SCREEN_OFF");
		localIntentFilter.addAction("android.intent.action.SCREEN_ON");
		this.mScrReceiver = new ScreenReceiver();
		this.mContext.registerReceiver(this.mScrReceiver, localIntentFilter);
		mIsProcessing = false;
		this.mBIsExit = false;
	}

	protected void onConfigurationChanged(Configuration paramConfiguration) {
		this.mConfigurationChanged = true;
		sendReLayoutWindowMessage();
		if (!mIsProcessing) {

			setIsProcessEnd(true);
			sendExitMessage();
		}
	}

	protected void onDraw(Canvas canvas) {
		if (!mBIsExit) {
			super.onDraw(canvas);
			canvas.translate((float) -mOffsetX, (float) -mOffsetY);
			if ((bIsDraw) && (!bIsCircle)) {
				makeEffects(mEffects, mPhase);
				mPhase = (float) ((double) mPhase + 1.3);
				if (mIsScreenOn) {
					invalidate();
				}
				mPaint.setPathEffect(mEffects[0x4]);
				canvas.drawPath(mPathEx, mPaint);
				return;
			}
			if (bIsCircle) {
				makeEffects(mEffects, mPhase);
				mPhase = (float) ((double) mPhase + 1.3);
				if (mIsScreenOn) {
					invalidate();
				}
				mPaint.setPathEffect(mEffects[0x4]);
				if (mBReShow) {
					canvas.drawPath(mPathReshow, mPaint);
					return;
				}
				canvas.drawPath(mPath, mPaint);
				return;
			}
			if (mBReShow) {
				canvas.drawPath(mPathReshow, mPaint);
				return;
			}
			canvas.drawPath(mPath, mPaint);
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (mBisTouch) {
			
			//syc add start
			if(ismove && event.getPointerCount() > 1){
				setTouchEnable(true);
				mHandler.sendEmptyMessage(0x2);
				multipoint = true;
				return true;	
			}
			
			//syc add end
			
			if (event.getPointerCount() > 1) {
					setTouchEnable(true);
					//mHandler.sendEmptyMessage(0x2);
					multipoint = true;
					return true;
			}
			int x = (int) event.getX();
			int y = (int) event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				if (event.getPointerCount() > 1) {
						setTouchEnable(true);
						//mHandler.sendEmptyMessage(0x2);
						multipoint = true;
						return true;
				}
				
				ismove = false;
				
				
//				processHistoryPoint(event);
//				touch_start(x, y);
		
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				mcount++;
				if(!multipoint){
				if(mcount == 1){
					processHistoryPoint(event);
					touch_start(x, y);
				}
				}
				if (event.getPointerCount() > 1) {
						setTouchEnable(false);
						mHandler.sendEmptyMessage(0x2);
						multipoint = true;
						return true;
				}
				
				ismove = true;
				processHistoryPoint(event);
				touch_move(x, y);
				break;
			}
			case MotionEvent.ACTION_UP: {
				mcount = 0;
//				if(mcount > 1){
//					processHistoryPoint(event);
//					touch_up(x, y);
//					}
				
				//syc add start
				if (event.getPointerCount() > 1) {
						setTouchEnable(true);
						//mHandler.sendEmptyMessage(0x2);
						return true;
				}
				
				if(multipoint){
					setTouchEnable(true);
					//mHandler.sendEmptyMessage(0x2);
					multipoint = false;
					return true;
				}
				
				if(!ismove){
					
					setTouchEnable(true);
					//mHandler.sendEmptyMessage(0x2);
					return true;
				}
				//syc add end
				processHistoryPoint(event);
				touch_up(x, y);
				break;
			}
			}
			invalidate();
		}
		return true;
	}

	protected void processImg() {
		new Thread(new Runnable() {
			public void run() {
				
				int left = (int) (mImgRangInfo.mLeft - mBackgroundPaint.getStrokeWidth());
				if (left <= 0)
					left = 0;
				int top = (int) (mImgRangInfo.mTop - mBackgroundPaint.getStrokeWidth());
				if (top <= 0)
					top = 0;
				int right = (int) (mImgRangInfo.mRight + mBackgroundPaint.getStrokeWidth());
				if (right >= mScreenWidth)
					right = mScreenWidth;
				int bottom = (int) (mImgRangInfo.mBottom + mBackgroundPaint.getStrokeWidth());
				if (bottom >= PathEffectsImageView.this.mScreenHeight)
					bottom = PathEffectsImageView.this.mScreenHeight;
				if (((int) (mImgRangInfo.mRight - mImgRangInfo.mLeft + 2.0F * mBackgroundPaint
						.getStrokeWidth()) >= mScreenWidth)
						&& ((int) (mImgRangInfo.mBottom - mImgRangInfo.mTop + 2.0F * mBackgroundPaint
								.getStrokeWidth()) < mScreenHeight)) {
					mSpanFill2d.setRang(left, top, right, bottom);
					mSpanFill2d.setBoundColor(mBackgroundPaint.getColor());
				}
				Point localString1 = new Point(left, top);
				PathEffectsImageView.this.mPixelSeed = localString1;
				Point localPoint = new Point(left, bottom - 1);
				Log.i("lgr", "left|right = " + left + " | " + right);
				Log.i("lgr", "width|height = "
						+ PathEffectsImageView.this.mScreenWidth + " | "
						+ PathEffectsImageView.this.mScreenHeight);
				//if (left == 0){
				//	return;
				//}
				Bitmap localBitmap = null;
				/*PRIZE-remove-fix bug[35142] -huangpengfei-2017-6-21-start*/
//				if (right == PathEffectsImageView.this.mScreenWidth) {
//					try {
//						PathEffectsImageView.this.mSpanFill2d.ExcuteSpanFill(
//								PathEffectsImageView.this.mImageMap2d,
//								PathEffectsImageView.this.mImageMap2dOper,
//								PathEffectsImageView.this.mPixelSeed);
//						PathEffectsImageView.this.mSpanFill2d.ExcuteSpanFill(
//								PathEffectsImageView.this.mImageMap2d,
//								PathEffectsImageView.this.mImageMap2dOper,
//								localPoint);
//					} catch (Exception e) {
					
//					}
//					return;
//				}
				/*PRIZE-remove-fix bug[35142] -huangpengfei-2017-6-21-end*/
				localBitmap = PathEffectsImageView.this.mScreenBitmap.copy(
						Bitmap.Config.ARGB_8888, true);
				if (!ismove && SuperShotUtils.getDistance(
						PathEffectsImageView.this.mImgRangInfo.mBegin,
						PathEffectsImageView.this.mImgRangInfo.mEnd) <= PathEffectsImageView.this.mMinDistance) {
					Log.i("lgr", "go 1");
					if (!SuperShotUtils.getColorAndRemoveColor(
							PathEffectsImageView.this.mBackBitmap,
							PathEffectsImageView.this.mMidBitamp, new Rect(left,
									top, right, bottom),
									PathEffectsImageView.this.mBackgroundPaint
									.getColor(),
									PathEffectsImageView.this.mMiddlePaint.getColor(),
									(int) PathEffectsImageView.this.mBackgroundPaint
									.getStrokeWidth())) {
						Log.i("lgr", "go 2");
						PathEffectsImageView.this.mHandler.sendEmptyMessage(2);
						// PathEffectsImageView.this.mSpanFill2d.ExcuteSpanFill(PathEffectsImageView.this.mImageMap2d,
						// PathEffectsImageView.this.mImageMap2dOper,
						// PathEffectsImageView.this.mPixelSeed);
					}
				} else {
					try {
						PathEffectsImageView.this.mDestW = (right - left);
						PathEffectsImageView.this.mDestH = (bottom - top);
						SuperShotUtils.mergeImage(
								PathEffectsImageView.this.mMidBitamp,
								localBitmap, PorterDuff.Mode.SRC_IN);
						mDestBitmap = Bitmap.createBitmap(mMidBitamp, left, top,
								mDestW, mDestH);
						localBitmap.recycle();
						PathEffectsImageView.this.mOffsetX = (left + (PathEffectsImageView.this.mDestW - PathEffectsImageView.this.mScreenWidth) / 2);
						PathEffectsImageView.this.mOffsetY = (top + (PathEffectsImageView.this.mDestH - PathEffectsImageView.this.mScreenHeight) / 2);
						
						if (PathEffectsImageView.this.mDestBitmap != null) {
							// Log.i("lgr", "go 4");
							// PathEffectsImageView.this.mHandler.sendEmptyMessage(2);
							Log.i("lgr", "go 5");
							PathEffectsImageView.this.mHandler.sendEmptyMessage(1);
						} else {
							 //PathEffectsImageView.this.mHandler.sendEmptyMessage(2);
							// boolean bool =
							// SuperShotUtils.getColorAndRemoveColor(PathEffectsImageView.this.mBackBitmap,
							// PathEffectsImageView.this.mMidBitamp, new Rect(i,
							// j, k, l),
							// PathEffectsImageView.this.mBackgroundPaint.getColor(),
							// PathEffectsImageView.this.mMiddlePaint.getColor(),
							// (int)PathEffectsImageView.this.mBackgroundPaint.getStrokeWidth());
							boolean bool = true;
							if (!bool) {
								PathEffectsImageView.this.mHandler
								.sendEmptyMessage(2);
							} else {
								PathEffectsImageView.this.mDestW = (right - left);
								PathEffectsImageView.this.mDestH = (bottom - top);
								SuperShotUtils.mergeImage(
										PathEffectsImageView.this.mMidBitamp,
										localBitmap, PorterDuff.Mode.SRC_IN);
								mDestBitmap = Bitmap.createBitmap(mMidBitamp,
										left, top, mDestW, mDestH);
								localBitmap.recycle();
								PathEffectsImageView.this.mOffsetX = (left + (PathEffectsImageView.this.mDestW - PathEffectsImageView.this.mScreenWidth) / 2);
								PathEffectsImageView.this.mOffsetY = (top + (PathEffectsImageView.this.mDestH - PathEffectsImageView.this.mScreenHeight) / 2);
								if(mImgRangInfo.mPathList != null){
								PathEffectsImageView.this
								.setPath(
										PathEffectsImageView.this.mPathEx,
										PathEffectsImageView.this.mImgRangInfo
										.getFourUnion(
												PathEffectsImageView.this.mMidBitamp,
												PathEffectsImageView.this.mImgRangInfo.mPathList));
								}
								PathEffectsImageView.this.mPathEx.close();
								PathEffectsImageView.this.mMidCanvas.drawPath(
										PathEffectsImageView.this.mPathEx,
										PathEffectsImageView.this.mPaint);
								if (PathEffectsImageView.this.mDestBitmap != null) {
									Log.i("lgr", "else 22");
									PathEffectsImageView.this.mHandler.sendEmptyMessage(1);
								}
								Log.i("lgr", "else 333");
								PathEffectsImageView.this.mHandler.sendEmptyMessage(2);
							}
						}

					} catch (Exception e) {
						// TODO: handle exception
						/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-start*/
						Log.i("lgr", "ERROR2");
						PathEffectsImageView.this.mHandler.sendEmptyMessage(4);
						/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-end*/
					}
				}

			}
		}

				).start();
	}

	public void reDraw() {
		this.mBackgroundPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.CLEAR));
		this.mBackCanvas.drawPaint(this.mBackgroundPaint);
		this.mBackgroundPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.SRC));
		this.mMiddlePaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.CLEAR));
		this.mMidCanvas.drawPaint(this.mMiddlePaint);
		this.mMiddlePaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.SRC));
		setBackgroundColor(Color.parseColor("#55000000"));
		setImageBitmap(null);
		if (this.mProgress != null)
			this.mProgress.setVisibility(View.GONE);
		this.mOffsetX = 0;
		this.mOffsetY = 0;
		release();
		setTouchEnable(true);
		setShouldSave(false);
		this.mRedraw.setEnabled(false);
		setButtonVisible(false);
		setIsDown(false);
		setIsReShow(false);
		if (this.mConfigurationChanged) {
			this.mConfigurationChanged = false;
			sendExitMessage();
		}
		mIsProcessing = false;
		if (this.mGuide == null)
			return;
		this.mGuide.setVisibility(View.VISIBLE);
	}

	protected void release() {
		if(mImgRangInfo.mPathList != null){
		this.mImgRangInfo.mPathList.clear();
		}
		this.mPath.reset();
		this.mPathEx.reset();
		this.bIsDraw = false;
		this.bIsCircle = false;
		System.gc();
	}

	public void releaseAllRes() {
		this.mBackBitmap.recycle();
		this.mBackBitmap = null;
		this.mMidBitamp.recycle();
		this.mMidBitamp = null;
		this.mScreenBitmap = null;
		this.mImgRangInfo.mPathList.clear();
		this.mImgRangInfo = null;
		this.mSpanFill2d = null;
		this.mImageMap2d = null;
		this.mImageMap2dOper = null;
		this.mPath.reset();
		this.mPathEx.reset();
		this.mContext.unregisterReceiver(this.mScrReceiver);
		this.mScrReceiver = null;
		this.mBIsExit = true;
		System.gc();
	}

	public void saveImg() {
		this.mBIsExit = true;
		System.gc();
		if (this.mIsProcessEnd)
			setIsProcessEnd(false);
		Bitmap localBitmap1 = this.mScreenBitmap.copy(Bitmap.Config.ARGB_8888,
				true);
		Bitmap localBitmap2 = null;
		if (this.mDestBitmap == null) {
			this.mDestBitmap = localBitmap1;
			localBitmap2 = Bitmap.createBitmap(this.mScreenWidth,
					this.mScreenHeight, Bitmap.Config.ARGB_8888);
		} else {
			localBitmap2 = Bitmap.createBitmap(this.mDestW, this.mDestH,
					Bitmap.Config.ARGB_8888);
		}
		new Canvas(localBitmap2).drawBitmap(this.mDestBitmap, 0.0F, 0.0F, null);
		SuperShotUtils.saveImageToTFCard(localBitmap2, "/sdcard/",
				this.mContext);
		this.mDestBitmap.recycle();
		this.mDestBitmap = null;
		setIsProcessEnd(true);
		return;
	}

	protected void setButtonVisible(boolean paramBoolean) {
		mBottomStatusBar.setVisibility(paramBoolean ? View.VISIBLE : View.GONE);
	}

	public void setHandler(Handler paramHandler) {
		this.exitLayoutHandler = paramHandler;
	}

	protected void setIsDown(boolean paramBoolean) {
		this.mBisDown = paramBoolean;
	}

	protected void setIsProcessEnd(boolean paramBoolean) {
		this.mIsProcessEnd = paramBoolean;
	}

	protected void setIsReShow(boolean paramBoolean) {
		this.mBReShow = paramBoolean;
	}

	protected void setPath(Path paramPath,
			ArrayList<ImageRangInfo.pixelCo> paramArrayList) {
		int i = 0;
		int j = 0;
		int k = 0;
		paramPath.reset();
		int l = 0;
		if (l < paramArrayList.size()) {
			if (l == 0) {
				label15: paramPath.reset();
			paramPath.moveTo(
					((ImageRangInfo.pixelCo) paramArrayList.get(l)).mx,
					((ImageRangInfo.pixelCo) paramArrayList.get(l)).my);
			}
			if (k == 0)
				;
			for (j = ((ImageRangInfo.pixelCo) paramArrayList.get(l)).my;; j = k) {
				int i1 = ((ImageRangInfo.pixelCo) paramArrayList.get(l)).mx;
				k = ((ImageRangInfo.pixelCo) paramArrayList.get(l)).my;
				i = i1;
				paramPath.quadTo(i, j, (i1 + i) / 2, (k + j) / 2);
				++l;
				break;
			}
		}
		paramPath.lineTo(i, j);
	}

	public void setRelayoutWindowHandler(Handler paramHandler) {
		this.RelayoutWindowHandler = paramHandler;
	}

	protected void setShouldSave(boolean paramBoolean) {
		this.mBShouldSave = paramBoolean;
	}

	protected void setTouchEnable(boolean paramBoolean) {
		this.mBisTouch = paramBoolean;
	}

	class PathEffectHandler extends Handler {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1: {
					if (mProgress != null) {
						mProgress.setVisibility(View.GONE);
					}
					setBackgroundColor(mContext.getResources().getColor(R.color.translucent));
					setScaleType(ImageView.ScaleType.CENTER);
					setImageBitmap(mDestBitmap);
					invalidate();
					setShouldSave(true);
					mRedraw.setEnabled(true);
					setButtonVisible(true);
					setIsReShow(false);
					setIsProcessEnd(true);
					if (mConfigurationChanged) {
						mConfigurationChanged = false;
						sendExitMessage();//syc add
					}
					PathEffectsImageView.mIsProcessing = false;
					break;
				}
				case 2: {
					reDraw();
					setIsProcessEnd(true);
					break;
				}
				case 3: {
					if (mIsProcessEndExKey) {
						reDraw();
						invalidate();//syc add
					}
					break;
				}
				/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-start*/
				case 4: {
					Log.i("PathEffectHandler","[onError]");
					if(mOnShotErrorListener != null)
					mOnShotErrorListener.onError();
					break;
				}
				/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-start*/
			}
		}
	}

	class ScreenReceiver extends BroadcastReceiver {
		ScreenReceiver() {
		}

		public void onReceive(Context paramContext, Intent paramIntent) {
			String str = paramIntent.getAction();
			if (str.equals("android.intent.action.SCREEN_OFF")){
				  mIsScreenOn = false;
	                return;
			}
			  if((str.equals("android.intent.action.SCREEN_ON")) && (!mIsScreenOn)) {
	                mHandler.sendEmptyMessageDelayed(0x3, 0x514);
	                mIsScreenOn = true;
	                invalidate();//syc add
	            }
		}
	}
	/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-start*/
	private OnShotErrorListener mOnShotErrorListener;
	public void setOnShotErrorListener(OnShotErrorListener onShotErrorListener){
		this.mOnShotErrorListener = onShotErrorListener;
		
	}
	public interface OnShotErrorListener{
		void onError();
	}
	/*PRIZE-add-fix bug[35142] -huangpengfei-2017-6-21-end*/
}
