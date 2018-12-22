/*
 * Copyright (C) 2015 Mert Şimşek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.playview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.WorkSource;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Folder;
import com.android.launcher3.Hotseat;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;

//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;

public class PrizePlayView extends BubbleTextView implements
		OnPlayPauseToggleListener {

	/**
	 * Rect for get time height and width
	 */
	private static Rect mRectText;

	/**
	 * Paint for drawing left and passed time.
	 */
	private static Paint mPaintTime;

	/**
	 * RectF for draw circle progress.
	 */
	private RectF rectF;  //绘制的区域

	/**
	 * Paint for circle progress left
	 */
	private static Paint mPaintProgressEmpty;//这个是外部进度条的圈圈

	/**
	 * Paint for circle progress loaded
	 */
	private static Paint mPaintProgressLoaded;

	/**
	 * Modified OnClickListener. We do not want all view click. notify onClick()
	 * only button area touched.
	 */
	private OnClickListener onClickListener;

	/**
	 * Button paint for play/pause control button
	 */
	private static Paint mPaintButton;

	/**
	 * Play/Pause button region for handle onTouch
	 */
	private static Region mButtonRegion;

	/**
	 * Paint to draw cover photo to canvas
	 */
	private static Paint mPaintCover;

	/**
	 * Bitmap for shader.
	 */
	private Bitmap mBitmapCover;

	/**
	 * Shader for make drawable circle
	 */
	private BitmapShader mShader;

	/**
	 * Scale image to view width/height
	 */
	private float mCoverScale;

	/**
	 * Image Height and Width values.
	 */
	private int mHeight;
	private int mWidth;

	/**
	 * Center values for cover image.
	 */
	private float mCenterX;
	private float mCenterY;

	/**
	 * Cover image is rotating. That is why we hold that value.
	 */
	private int mRotateDegrees;

	/**
	 * Handler for posting runnable object
	 */
	private Handler mHandlerRotate;

	/**
	 * Runnable for turning image (default velocity is 10)
	 */
	private final Runnable mRunnableRotate = new Runnable() {
		@Override
		public void run() {
			if (isRotating) {

				if (currentProgress > maxProgress) {
					currentProgress = 0;
					setProgress(currentProgress);
					stop();
				}

				updateCoverRotate();
				mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
			}
		}
	};

	/**
	 * Handler for posting runnable object
	 */
	private Handler mHandlerProgress;

	/**
	 * Runnable for turning image (default velocity is 10)
	 */
	private Runnable mRunnableProgress = new Runnable() {
		@Override
		public void run() {
			if (isRotating) {
				currentProgress++;
				mHandlerProgress.postDelayed(mRunnableProgress,
						PROGRESS_SECOND_MS);
			}
		}
	};

	private int mDownLoadState=-1;

	
	public void setDownLoadState(int mDownLoadState) {
		this.mDownLoadState = mDownLoadState;
	}
	
	/**
	 * isRotating
	 */
	private boolean isRotating=true;

	/**
	 * Handler will post runnable object every @ROTATE_DELAY seconds.
	 */
	private static int ROTATE_DELAY = 10;

	/**
	 * 1 sn = 1000 ms
	 */
	private static int PROGRESS_SECOND_MS = 1000;

	/**
	 * mRotateDegrees count increase 1 by 1 default. I used that parameter as
	 * velocity.
	 */
	private static int VELOCITY = 1;

	/**
	 * Default color code for cover
	 */
	private int mCoverColor = Color.TRANSPARENT;

	/**
	 * Play/Pause button radius.(default = 120)
	 */
	private float mButtonRadius = 120f;

	/**
	 * Play/Pause button color(Default = dark gray)
	 */
	private int mButtonColor = Color.TRANSPARENT;

	/**
	 * Color code for progress left.
	 */
	private int mProgressEmptyColor = 0x40FFFFFF;

	/**
	 * Color code for progress loaded.
	 */
	private int mProgressLoadedColor = 0x70ffffff;

	/**
	 * Time text size
	 */
	private int mTextSize = 40;

	/**
	 * Default text color
	 */
	private int mTextColor = 0xFFFFFFFF;

	/**
	 * Current progress value
	 */
	private int currentProgress = 0;

	/**
	 * Max progress value
	 */
	private int maxProgress = 100;

	/**
	 * Auto progress value start progressing when cover image start rotating.
	 */
	private boolean isAutoProgress = false;

	/**
	 * Progressview and time will be visible/invisible depends on this
	 */
	private boolean mProgressVisibility = true;

	/**
	 * play pause animation duration
	 */
	private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

	/**
	 * Play Pause drawable
	 */
	private PlayPauseDrawable mPlayPauseDrawable;

	/**
	 * Animator set for play pause toggle
	 */
	private AnimatorSet mAnimatorSet;

	private boolean mFirstDraw = true;

	private boolean isStoke=true;

	/**
	 * Constructor
	 */
	public PrizePlayView(Context context) {
		super(context);
			init(context, null);
	}

	/**
	 * Constructor
	 */
	public PrizePlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
			init(context, attrs);
	}

	/**
	 * Constructor
	 */
	public PrizePlayView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
			init(context, attrs);
	}

	/**
	 * Constructor
	 */
	// @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	// public MusicPlayerView(Context context, AttributeSet attrs, int
	// defStyleAttr, int defStyleRes) {
	// super(context, attrs, defStyleAttr, defStyleRes);
	// init(context, attrs);
	// }

	/**
	 * Initializes resource values, create objects which we need them later.
	 * Object creation must not called onDraw() method, otherwise it won't be
	 * smooth.
	 */
	private void init(Context context, AttributeSet attrs) {

		setWillNotDraw(false);
		mPlayPauseDrawable = new PlayPauseDrawable(context);
		mPlayPauseDrawable.setCallback(callback);
		mPlayPauseDrawable.setToggleListener(this);

		// Get Image resource from xml
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.playerview);
		Drawable mDrawableCover = a.getDrawable(R.styleable.playerview_cover);
		if (mDrawableCover != null)
			mBitmapCover = drawableToBitmap(mDrawableCover);

		mButtonColor = a.getColor(R.styleable.playerview_buttonColor,
				mButtonColor);
		mProgressEmptyColor = a.getColor(
				R.styleable.playerview_progressEmptyColor, mProgressEmptyColor);
		mProgressLoadedColor = a.getColor(
				R.styleable.playerview_progressLoadedColor,
				mProgressLoadedColor);
		mTextColor = a.getColor(R.styleable.playerview_TextColor, mTextColor);
		mTextSize = a.getDimensionPixelSize(R.styleable.playerview_TextSize,
				mTextSize);
		a.recycle();

		mRotateDegrees = 0;

		// Handler and Runnable object for turn cover image by updating rotation
		// degrees
		mHandlerRotate = new Handler();

		// Handler and Runnable object for progressing.
		mHandlerProgress = new Handler();

		// Play/Pause button circle paint
		mPaintButton = new Paint();
		// mPaintButton.setAntiAlias(true);
		// mPaintButton.setStyle(Paint.Style.FILL);
		mPaintButton.setAntiAlias(true);
		mPaintButton.setColor(mButtonColor);

		// Progress paint object creation
		mPaintProgressEmpty = new Paint();
		mPaintProgressEmpty.setAntiAlias(true);
		mPaintProgressEmpty.setColor(mProgressEmptyColor);
		mPaintProgressEmpty.setStyle(Paint.Style.STROKE);
		mPaintProgressEmpty.setStrokeWidth(6f*Launcher.scale);

		mPaintProgressLoaded = new Paint();
		mPaintProgressLoaded.setAntiAlias(true);
		mPaintProgressLoaded.setColor(mProgressLoadedColor);
		if(isStoke) {
			mPaintProgressLoaded.setStyle(Paint.Style.STROKE);
		}else {
			mPaintProgressLoaded.setStyle(Paint.Style.FILL);
		}
		mPaintProgressLoaded.setStrokeWidth(6.0f*Launcher.scale);

		mPaintTime = new Paint();
		// mPaintTime.setColor(mTextColor);
		// mPaintTime.setAntiAlias(true);
		// mPaintTime.setTextSize(mTextSize);

		// rectF and rect initializes
		rectF = new RectF();
		mRectText = new Rect();
	}

	/**
	 * Calculate mWidth, mHeight, mCenterX, mCenterY values and scale resource
	 * bitmap. Create shader. This is not called multiple times.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		ItemInfo info = (ItemInfo) getTag();

		if (info != null && info.fromAppStore == 1) {
			int w = MeasureSpec.getSize(widthMeasureSpec);
			int h = MeasureSpec.getSize(heightMeasureSpec);
			mWidth = Utilities.sIconTextureHeight;
			mHeight = mWidth;

			int minSide = Math.min(mWidth, mHeight);
			mWidth = minSide;
			mHeight = minSide;

			this.setMeasuredDimension(mWidth, mHeight);

			float x = w/2-mWidth/2;
			float y = getPaddingTop();
			mCenterX = x+mWidth / 2f;
			mCenterY = y+mHeight/ 2f;
			// set RectF left, top, right, bottom coordiantes
			float pad =Launcher.scale*6f;
			rectF.set(x+pad, y+pad, mWidth+x-pad, mHeight+y-pad);

			// button size is about to 1/4 of image size then we divide it to 8.
			mButtonRadius = mWidth / 3.0f;

			WindowManager wm = (WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE);

			int width = wm.getDefaultDisplay().getWidth();
			if (width < 720) {
				// We resize play/pause drawable with button radius. button
				// needs to be inside circle.
				mPlayPauseDrawable.resize((1.2f * mButtonRadius / 8.0f),
						(3.0f * mButtonRadius / 8.0f) + 10.0f,
						(mButtonRadius / 5.0f));
			} else {
				mPlayPauseDrawable.resize((1.2f * mButtonRadius / 5.0f),
						(3.0f * mButtonRadius / 5.0f) + 10.0f,
						(mButtonRadius / 5.0f));
			}

			mPlayPauseDrawable.setBounds(0, 0, mWidth, mHeight);

			mButtonRegion = new Region((int) (mCenterX - mButtonRadius),
					(int) (mCenterY - mButtonRadius),
					(int) (mCenterX + mButtonRadius),
					(int) (mCenterY + mButtonRadius));

			createShader();
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public boolean isFromAppStore() {

		ItemInfo info = (ItemInfo) getTag();

		if (info != null && info.fromAppStore == 1) {
			return true;
		}
		return false;
	}
	/**
	 * This is where magic happens as you know.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		ItemInfo info = (ItemInfo) getTag();

		if (info != null && info.fromAppStore == 1) {
			if (mShader == null)
				return;

			// Draw cover image
			float radius = mCenterX <= mCenterY ? mCenterX - 10.0f
					: mCenterY - 10.0f; // 75
			canvas.rotate(mRotateDegrees, mCenterX, mCenterY);
			canvas.drawCircle(mCenterX, mCenterY, radius, mPaintCover);
	        Drawable drawable=this.getCompoundDrawables()[1];

		     if(drawable!=null) {
			if(currentProgress <maxProgress) {

		        
	            if(drawable!=null){
	                //设置滤镜
	                drawable.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);  
	            }
			}else {
			            drawable.setColorFilter(null); 
			}
		     }
			// Rotate back to make play/pause button stable(No turn)
			canvas.rotate(-mRotateDegrees, mCenterX, mCenterY);

			// Draw Play/Pause button
			canvas.drawCircle(mCenterX, mCenterY, radius - 20, mPaintButton);
			// canvas.drawCircle(mCenterX, mCenterY, mButtonRadius,
			// mPaintButton);

			if (mProgressVisibility) {
				// Draw empty progress
				canvas.drawArc(rectF, 90, 360, false, mPaintProgressEmpty);

				// Draw loaded progress
				canvas.drawArc(rectF, -90, calculatePastProgressDegree(),
						false, mPaintProgressLoaded);

				// Draw left time text
				// String leftTime = secondsToTime(calculateLeftSeconds());
				// mPaintTime.getTextBounds(leftTime, 0, leftTime.length(),
				// mRectText);
				//
				// canvas.drawText(leftTime, (float) (mCenterX *
				// Math.cos(Math.toRadians(35.0))) + mWidth / 2.0f
				// - mRectText.width() / 1.5f,
				// (float) (mCenterX * Math.sin(Math.toRadians(35.0))) + mHeight
				// /
				// 2.0f + mRectText.height()
				// + 15.0f, mPaintTime);
				//
				// //Draw passed time text
				// String passedTime = secondsToTime(calculatePassedSeconds());
				// mPaintTime.getTextBounds(passedTime, 0, passedTime.length(),
				// mRectText);
				//
				// canvas.drawText(passedTime,
				// (float) (mCenterX * -Math.cos(Math.toRadians(35.0))) + mWidth
				// /
				// 2.0f
				// - mRectText.width() / 3.0f,
				// (float) (mCenterX * Math.sin(Math.toRadians(35.0))) + mHeight
				// /
				// 2.0f + mRectText.height()
				// + 15.0f, mPaintTime);
			}

			if (mFirstDraw) {
				toggle();
				mFirstDraw = false;
			}
			canvas.save();
			
			try {
				if(this.getParent().getParent().getParent() instanceof Workspace) {
					canvas.translate(radius/2, radius/2-getPaddingTop());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {

				if(this.getParent().getParent().getParent() instanceof Hotseat) {
					canvas.translate(radius/2, radius/2-getPaddingTop());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {

				if(this.getParent().getParent().getParent().getParent().getParent() instanceof Folder) {
					canvas.translate(radius/1.5f, radius/1.5f-getPaddingTop());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(isStoke) {
//				mPlayPauseDrawable.draw(canvas);
			}
			  if (false) {
	                // draw a big box for the icon for debugging
	                canvas.drawColor(Color.RED);
	                Paint debugPaint = new Paint();
	                debugPaint.setColor(0xffcccc00);
	                canvas.drawRect(0, 0, mPlayPauseDrawable.getMinimumWidth(), mPlayPauseDrawable.getMinimumHeight(), debugPaint);
	            }
			canvas.restore();
		}
	}

	/**
	 * We need to convert drawable (which we get from attributes) to bitmap to
	 * prepare if for BitmapShader
	 */
	private Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	/**
	 * Create shader and set shader to mPaintCover
	 */
	private void createShader() {

		if (mWidth == 0)
			return;

		// if mBitmapCover is null then create default colored cover
		if (mBitmapCover == null) {
			mBitmapCover = Bitmap.createBitmap(mWidth, mHeight,
					Bitmap.Config.ARGB_8888);
			mBitmapCover.eraseColor(mCoverColor);
		}

		mCoverScale = ((float) mWidth) / (float) mBitmapCover.getWidth();

		mBitmapCover = Bitmap.createScaledBitmap(mBitmapCover,
				(int) (mBitmapCover.getWidth() * mCoverScale),
				(int) (mBitmapCover.getHeight() * mCoverScale), true);

		mShader = new BitmapShader(mBitmapCover, Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP);
		mPaintCover = new Paint();
		mPaintCover.setAntiAlias(true);
		mPaintCover.setShader(mShader);
		// mPaintCover.setStrokeWidth(5.0f);
	}

	/**
	 * Update rotate degree of cover and invalide onDraw();
	 */
	public void updateCoverRotate() {
		mRotateDegrees += VELOCITY;
		mRotateDegrees = mRotateDegrees % 360;
		postInvalidate();
	}

	/**
	 * Checks is rotating
	 */
	public boolean isRotating() {
		return isRotating;
	}

	/**
	 * Start turning image
	 */
	public void start() {
		LogUtils.i("zhouerlong", "start。。。。。。");
		if(isRotating!=true) {
			isRotating = true;
		}else {
			isRotating=false;
			return ;
		}
		mPlayPauseDrawable.setPlaying(isRotating);
		mHandlerRotate.removeCallbacksAndMessages(null);
		mHandlerRotate.postDelayed(mRunnableRotate, ROTATE_DELAY);
		if (isAutoProgress) {
			mHandlerProgress.removeCallbacksAndMessages(null);
			mHandlerProgress.postDelayed(mRunnableProgress, PROGRESS_SECOND_MS);
		}
		invalidate();
	}

	/**
	 * Stop turning image
	 */
	public void stop() {
		if(isRotating!=false) {
			isRotating = false;
		}else {
			isRotating=true;
			return;
		}
		LogUtils.i("zhouerlong", "暂停。。。。。。");
		mPlayPauseDrawable.setPlaying(isRotating);
		postInvalidate();
	}

	/**
	 * Set velocity.When updateCoverRotate() method called, increase degree by
	 * velocity value.
	 */
	public void setVelocity(int velocity) {
		if (velocity > 0)
			VELOCITY = velocity;
	}

	/**
	 * set cover image resource
	 */
	public void setCoverDrawable(int coverDrawable) {
		Drawable drawable = getContext().getResources().getDrawable(
				coverDrawable);
		mBitmapCover = drawableToBitmap(drawable);
		createShader();
		postInvalidate();
	}

	/**
	 * sets cover image
	 * 
	 * @param drawable
	 */
	public void setCoverDrawable(Drawable drawable) {
		mBitmapCover = drawableToBitmap(drawable);
		createShader();
		postInvalidate();
	}

	/**
	 * gets image URL and load it to cover image.It uses Picasso Library.
	 */
	public void setCoverURL(String imageUrl) {
		// Picasso.with(getContext()).load(imageUrl).into(target);
	}

	/**
	 * When picasso load into target. Overrider methods are called. Invalidate
	 * view onBitmapLoaded.
	 */
	// private Target target = new Target() {
	// @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom
	// from) {
	// mBitmapCover = bitmap;
	// createShader();
	// postInvalidate();
	// }
	//
	// @Override public void onBitmapFailed(Drawable errorDrawable) {
	//
	// }
	//
	// @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
	//
	// }
	// };

	/**
	 * This is detect when mButtonRegion is clicked. Which means play/pause
	 * action happened.
	 */
/*	@Override
	public boolean onTouchEvent(MotionEvent event) {



		ItemInfo info = (ItemInfo) getTag();

		if (info != null && info.fromAppStore == 1) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			return true;
		}
		case MotionEvent.ACTION_UP: {
			if (mButtonRegion.contains((int) x, (int) y)) {
				if (onClickListener != null)
					onClickListener.onClick(this);
			}
		}
			break;

		default:
			break;
		}
		}

		return super.onTouchEvent(event);
	}*/

	/**
	 * onClickListener.onClick will be called when button clicked. We dont want
	 * all view click. We only want button area click. That is why we override
	 * it.
	 */
	/*@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		ItemInfo info = (ItemInfo) getTag();
		if (info != null && info.fromAppStore == 1) {
			onClickListener = l;
		}
	}*/

	/**
	 * Resize bitmap with @newHeight and @newWidth parameters
	 */
	private Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	/**
	 * Sets button color
	 */
	public void setButtonColor(int color) {
		mButtonColor = color;
		mPaintButton.setColor(mButtonColor);
		postInvalidate();
	}

	/**
	 * sets progress empty color
	 */
	public void setProgressEmptyColor(int color) {
		mProgressEmptyColor = color;
		mPaintProgressEmpty.setColor(mProgressEmptyColor);
		postInvalidate();
	}

	/**
	 * sets progress loaded color
	 */
	public void setProgressLoadedColor(int color) {
		mProgressLoadedColor = color;
		mPaintProgressLoaded.setColor(mProgressLoadedColor);
		postInvalidate();
	}

	/**
	 * Sets total seconds of music
	 */
	public void setMax(int maxProgress) {
		this.maxProgress = maxProgress;
		postInvalidate();
	}

	/**
	 * Sets current seconds of music
	 */
	public void setProgress(int currentProgress) {
		if (0 <= currentProgress && currentProgress <= maxProgress) {
			this.currentProgress = currentProgress;
			postInvalidate();
		}
	}

	/**
	 * Get current progress seconds
	 */
	public int getProgress() {
		return currentProgress;
	}

	/**
	 * Calculate left seconds
	 */
	private int calculateLeftSeconds() {
		return maxProgress - currentProgress;
	}

	/**
	 * Return passed seconds
	 */
	private int calculatePassedSeconds() {
		return currentProgress;
	}

	/**
	 * Convert seconds to time
	 */
	private String secondsToTime(int seconds) {
		String time = "";

		String minutesText = String.valueOf(seconds / 60);
		if (minutesText.length() == 1)
			minutesText = "0" + minutesText;

		String secondsText = String.valueOf(seconds % 60);
		if (secondsText.length() == 1)
			secondsText = "0" + secondsText;

		time = minutesText + ":" + secondsText;

		return time;
	}

	/**
	 * Calculate passed progress degree
	 */
	private int calculatePastProgressDegree() {
		return (360 * currentProgress) / maxProgress;
	}

	/**
	 * If you do not want to automatic progress, you can disable it and
	 * implement your own handler by using setProgress method repeatedly.
	 */
	public void setAutoProgress(boolean isAutoProgress) {
		this.isAutoProgress = isAutoProgress;
	}

	/**
	 * Sets time text color
	 */
	public void setTimeColor(int color) {
		mTextColor = color;
		mPaintTime.setColor(mTextColor);
		postInvalidate();
	}

	public void setProgressVisibility(boolean mProgressVisibility) {
		this.mProgressVisibility = mProgressVisibility;
		postInvalidate();
	}

	/**
	 * Play pause drawable callback
	 */
	Drawable.Callback callback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable who) {
			postInvalidate();
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {

		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {

		}
	};

	/**
	 * Notified when button toggled
	 */
	@Override
	public void onToggled() {
		toggle();
	}

	/**
	 * Animate play/pause image
	 */
	public void toggle() {
		if (mAnimatorSet != null) {
			mAnimatorSet.cancel();
		}

		mAnimatorSet = new AnimatorSet();
		final Animator pausePlayAnim = mPlayPauseDrawable
				.getPausePlayAnimator();
		mAnimatorSet.setInterpolator(new DecelerateInterpolator());
		mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
		mAnimatorSet.playTogether(pausePlayAnim);
		mAnimatorSet.start();
	}
}
