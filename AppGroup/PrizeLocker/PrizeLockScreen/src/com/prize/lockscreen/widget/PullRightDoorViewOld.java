package com.prize.lockscreen.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.utils.ColorHelper;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

/**
 * 右滑解锁控件
 * @author fanjunchen
 * 
 */
public class PullRightDoorViewOld extends BaseRootView {

	private final static String TAG = "PullRightDoorView";

	private int mLastDownX = 0, mLastDownY;

	private int mCurryY, mCurryX;

	private int mDelX, mDelY;

	private boolean mCloseFlag = false;

	/**是否按下Camera*/
	private boolean isCameraDown = false;
	/**监控对象的res ID*/
	private int mCameraId = R.id.img_camera;
	/**监控对象的显示区域*/
	private Rect mRect = null;
	
	public PullRightDoorViewOld(Context context) {
		this(context, null);
	}
	
	public PullRightDoorViewOld(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PullRightDoorViewOld(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void setupView() {
		// 这个Interpolator你可以设置别的 我这里选择的是有弹跳效果的Interpolator
//		Interpolator polator = new BounceInterpolator();
//		mScroller = new Scroller(mContext, polator);
		mScroller = new Scroller(mContext);
		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (mContext
				.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenHeigh = dm.heightPixels;
		mScreenWidth = dm.widthPixels;

		// 这里你一定要设置成透明背景,不然会影响你看到底层布局
		this.setBackgroundColor(Color.argb(0, 0, 0, 0));
		mColorHelper = ColorHelper.getIntance(mContext);
		
		Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap)
			bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.default_);
		
		setBgBitmap(bitmap);
	}

	// 弹簧动画
	public void startBounceAnim(int startX, int dX, int duration) {
		mScroller.startScroll(startX, 0, dX, 0, duration);
		invalidate();
	}
	
	// 弹簧动画
	public void startBounceAnimY(int startY, int dY, int duration) {
		mScroller.startScroll(0, startY, 0, dY, duration);
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			if (null == mRect) {
				mRect = new Rect();
				View v = findViewById(mCameraId);
				if (v!= null)
					v.getHitRect(mRect);
			}
			mVelTracker.addMovement(event);	
			
			mLastDownX = (int) event.getX();
			mLastDownY = (int) event.getY();
			
			mScroller.abortAnimation();
			
			if (mRect.contains(mLastDownX, mLastDownY)) {
				LogUtil.i(TAG, "===Camera area down==");
				isCameraDown = true;
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			mVelTracker.addMovement(event);
			mCurryY = (int) event.getY();
			mCurryX = (int) event.getX();
			
			mVelTracker.computeCurrentVelocity(1000);
			
			mDelX = mCurryX - mLastDownX;
			mDelY = mLastDownY - mCurryY;
			if (isCameraDown && mDelY > 150) {
				scrollTo(0, mDelY);
			}
			// 只准上滑有效
			else if (!isCameraDown && mDelX > 0) {
				scrollTo(-mDelX, 0);
			}
			
			break;
		case MotionEvent.ACTION_UP:
			mCurryY = (int) event.getY();
			mDelY = mLastDownY - mCurryY;
			
			if (isCameraDown && mDelY >= 0) {
				
				float vY = mVelTracker.getYVelocity();
				if (calAndMoveToY(vY)) {
					isCameraDown = false;
					break;
				}
				
				if (Math.abs(mDelY) > mScreenHeigh / 2) {
					// 向上滑动超过半个屏幕高的时候 开启向上消失动画
					mPos = 3;
					startBounceAnimY(getScrollY(), mScreenHeigh, 450);
					mCloseFlag = true;
				} else {
					// 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
					startBounceAnimY(getScrollY(), -getScrollY(), 1000);
				}
				isCameraDown = false;
				break;
			}
			mPos = 0;
			
			float vX = mVelTracker.getXVelocity();
			
			Log.i(TAG, "===Yvelocity==" + vX);
			if (calAndMoveTo(vX))
				break;
			mCurryX = (int) event.getX();
			mDelX = mCurryX - mLastDownX;
			
			if (mDelX >= 0) {
				if (Math.abs(mDelX) > mScreenWidth / 2) {
					// 向右滑动超过半个屏幕高的时候 开启向左消失动画
					startBounceAnim(getScrollX(), -mScreenWidth, 450);
					mCloseFlag = true;
				} else {
					// 向右滑动未超过半个屏幕高的时候 开启向左弹动动画
					startBounceAnim(getScrollX(), -getScrollX(), 1000);
				}
			}
			isCameraDown = false;
			// mVelTracker.recycle();
			break;
		case MotionEvent.ACTION_CANCEL:
			isCameraDown = false;
			break;
		}
		return super.onTouchEvent(event);
	}
	/***
	 * 计算时间及距离并滑动到目标
	 * @param velocity
	 * @return
	 */
	private boolean calAndMoveTo(float velocityX) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocityX));
		int duration = getSplineFlingDuration((int)Math.abs(velocityX));
		
		boolean rs = false;
		
		int startX = -getScrollX();
		if (velocityX > 0) {
			int aim = (int)(startX + dis);
			if (aim > mScreenWidth || aim > mScreenWidth / 2 + 10) {
				duration = (int)((duration * (mScreenWidth - startX)) / dis);
			}
			if (aim > (mScreenWidth >> 1) + 10 
					&& duration < 1000) {
				startBounceAnim(getScrollX(), -(mScreenWidth + 100), duration);
				rs = mCloseFlag = true;
			}
		}
		else {
			int aim = (int)(startX - dis);
			if (aim < 0 || aim < mScreenWidth/2 - 20) {
				duration = (int)((duration * startX) / dis);
				if (duration < 220)
					duration = 220;
				else if (duration > 1200)
					duration = 1200;
				// 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
				startBounceAnim(this.getScrollX(), -this.getScrollX(), duration);
				rs = true;
			}
		}
		return rs;
	}
	
	/***
	 * 计算时间及距离并滑动到目标
	 * @param velocityY
	 * @return
	 */
	private boolean calAndMoveToY(float velocityY) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocityY));
		int duration = getSplineFlingDuration((int)Math.abs(velocityY));
		
		boolean rs = false;
		mPos = 0;
		int startY = getScrollY();
		if (velocityY < 0) {
			int aim = (int)(startY + dis);
			if (aim > mScreenHeigh || aim > mScreenHeigh/2 + 20) {
				duration = (int)((duration * (mScreenHeigh - startY)) / dis);
			}
			if (aim > (mScreenHeigh >> 1) + 20 
					&& duration < 1000) {
				mPos = 3;
				startBounceAnimY(this.getScrollY(), mScreenHeigh + 100, duration);
				rs = mCloseFlag = true;
			}
		}
		else {
			int aim = (int)(startY - dis);
			if (aim < 0 || aim < mScreenHeigh/2 - 20) {
				duration = (int)((duration * startY) / dis);
				if (duration < 220)
					duration = 220;
				else if (duration > 1200)
					duration = 1200;
				// 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
				startBounceAnimY(this.getScrollY(), -this.getScrollY(), duration);
				rs = true;
			}
		}
		return rs;
	}
	
	@Override
	public void computeScroll() {

		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 不要忘记更新界面
			postInvalidate();
		} else {
			if (mCloseFlag && mUnlockListener != null) {
				if (!mUnlockListener.checkPwd(mPos)) {
					mUnlockListener.onUnlockFinish();
					this.setVisibility(View.GONE);
				}
				mCloseFlag = false;
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mBitmap || mBitmap.isRecycled())
			setBgBitmap(LockScreenApplication.getBgImg());
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}
	
	
	/*private final static int MSG_PRODUCE = 0X0001;
	
	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				default:
					break;
			}
		}
	};*/
	
	@Override
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (mVelTracker != null)
			mVelTracker.recycle();
		mVelTracker = null;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	protected int getLockType() {
		return LockConfigBean.DEFAULT_LOCK_TYPE;
	}
	/***
	 * 获取启动哪一个应用
	 * @return
	 */
	public int getPos() {
		return mPos;
	}
	
	public void setPos(int pos) {
		mPos = pos;
	}
	
	public void updateTime() {
		if (mTimeTxt != null) {
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (mDateTxt != null) {
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		}
	}
}
