package com.prize.lockscreen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.bean.CircleBean;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.utils.ColorHelper;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.TextView;

/**
 * 上滑解锁控件
 * @author 
 * 
 */
public class PullDoorView extends BaseRootView implements IResetView {

	private final static String TAG = PullDoorView.class.getName();

	private int mLastDownY = 0;

	private int mCurryY;

	private int mDelY;

	private boolean mCloseFlag = false;

	private List<CircleBean> mListCircleInfo;
	
	private int finishWeight = 0;
	
	private Random random = new Random();
	/**是否按下*/
	private boolean isDown = false;
	
	public PullDoorView(Context context) {
		this(context, null);
	}
	
	public PullDoorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PullDoorView(Context context, AttributeSet attrs, int defStyle){
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
		mListCircleInfo = new ArrayList<CircleBean>();
		mColorHelper = ColorHelper.getIntance(mContext);
		
		Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap)
			bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.default_);
		setBgBitmap(bitmap);
		if (shutterView != null) {
			shutterView.setBitmap(bitmap);
		}
	}

	// 弹簧动画
	public void startBounceAnim(int startY, int dy, int duration) {
		mScroller.startScroll(0, startY, 0, dy, duration);
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
			mVelTracker.addMovement(event);	
			setWeight();
			mLastDownY = (int) event.getY();
			
			mScroller.abortAnimation();
			isDown = true;
			LogUtil.d(TAG,"------>onTouchEvent x = " + event.getX() + "  ,y = " + event.getY());
			/*int currentX = (int) event.getX();
			int currentY = (int) event.getY();
			Message msg = mHandler.obtainMessage();
			msg.arg1 = currentX;
			msg.arg2 = currentY;
			msg.what = MSG_PRODUCE;
			msg.sendToTarget();*/
			return true;
		case MotionEvent.ACTION_MOVE:
			mVelTracker.addMovement(event);
			mCurryY = (int) event.getY();
			// currentX = (int) event.getX();
			
			mVelTracker.computeCurrentVelocity(1000);
			
			mDelY = mCurryY - mLastDownY;
			// 只准上滑有效
			if (mDelY < 0) {
				scrollTo(0, -mDelY);
				if (shutterView != null) {
					shutterView.shutter(-mDelY);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			isDown = false;
			float vY = mVelTracker.getYVelocity();
			
			Log.i(TAG, "===Yvelocity==" + vY);
			if (calAndMoveTo(vY))
				break;
			mCurryY = (int) event.getY();
			mDelY = mCurryY - mLastDownY;
			if (mDelY <= 0) {
				if (Math.abs(mDelY) > mScreenHeigh / 2) {
					// 向上滑动超过半个屏幕高的时候 开启向上消失动画
					startBounceAnim(this.getScrollY(), mScreenHeigh, 450);
					mCloseFlag = true;
				} else {
					// 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
					startBounceAnim(this.getScrollY(), -this.getScrollY(), 1000);
				}
			}
			// mVelTracker.recycle();
			break;
		case MotionEvent.ACTION_CANCEL:
			isDown = false;
			break;
		}
		return super.onTouchEvent(event);
	}
	//================================added fanjunchen=============//
	/***
	 * 计算时间及距离并滑动到目标
	 * @param velocity
	 * @return
	 */
	private boolean calAndMoveTo(float velocity) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocity));
		int duration = getSplineFlingDuration((int)Math.abs(velocity));
		
		boolean rs = false;
		
		int startY = getScrollY();
		if (velocity < 0) {
			int aim = (int)(startY + dis);
			if (aim > mScreenHeigh || aim > mScreenHeigh/2 + 20) {
				duration = (int)((duration * (mScreenHeigh - startY)) / dis);
			}
			if (aim > (mScreenHeigh >> 1) + 20 
					&& duration < 1000) {
				startBounceAnim(this.getScrollY(), mScreenHeigh + 100, duration);
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
				startBounceAnim(this.getScrollY(), -this.getScrollY(), duration);
				rs = true;
			}
		}
		return rs;
	}
    //================================end added fanjunchen==========//
	@Override
	public void computeScroll() {

		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			Log.i("scroller", "getCurrX()= " + mScroller.getCurrX()
					+ "     getCurrY()=" + mScroller.getCurrY()
					+ "  getFinalY() =  " + mScroller.getFinalY());
			// 不要忘记更新界面
			postInvalidate();
			
			if (shutterView != null) {
				shutterView.shutter(mScroller.getCurrY());
			}
		} else {
			if (mCloseFlag) {
				if (mUnlockListener != null)
					mUnlockListener.onUnlockFinish();
				setVisibility(View.GONE);
			}
			mCloseFlag = false;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == shutterView)
			canvas.drawBitmap(mBitmap,0,0,null);
		if (circleTag) {
			circleTag = false;
			for (int i = 0; i < mListCircleInfo.size(); i++) {
				CircleBean mCircleInfo = mListCircleInfo.get(i);
				onDrawAnim(canvas, mCircleInfo);
			}
		}
	}
	
	private void onDrawAnim(Canvas canvas, CircleBean info) {
		Paint paint = new Paint();
		paint.setColor(info.getColor());
		paint.setAlpha(info.getAlpha());
		canvas.drawCircle(info.getCurrentPostionX(), info.getCurrentPostionY(), info.getCircleRadius(), paint);
	}
	
	private final static int MSG_PRODUCE = 0X0001;
	private final static int MSG_MOVE = 0X0002;
	private final static int delayMillis = 10;
	private final static int circleCount = 12;
	private final static int circleTotal = 60;
	private static boolean circleTag = false;
	
	/*private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_PRODUCE:
				Bundle bundle = msg.getData();
				createCirle(msg.arg1, msg.arg2);
				mHandler.sendEmptyMessage(MSG_MOVE);
				break;
			case MSG_MOVE:
				circleTag = true;
				moveCirle();
				mHandler.removeMessages(MSG_MOVE);
				mHandler.sendEmptyMessageDelayed(MSG_MOVE, delayMillis);
				break;
				default:
					break;
			}
		}
	};*/
	
	private void setWeight() {
		if (getWidth() > getHeight()) {
			finishWeight = getHeight() / 10;
		} else {
			finishWeight = getWidth() / 10;
		}
	}
	
	/**
	 * 当点击屏幕时，创建气泡
	 * @param startX 气泡初始化X坐标
	 * @param startY 气泡初始化Y坐标
	 */
	
	private void createCirle(int startX, int startY){
		
		for (int i = 0; i < circleCount; i++) {
			CircleBean mCircleInfo = new CircleBean();

			mCircleInfo.setAngle((30 + random.nextInt(90)) * 0.017453293f);
			mCircleInfo.setMoveSize(8.0f + random.nextInt(4));
			float mCircleSize = (float) (finishWeight / 30 + random.nextInt(finishWeight/3));//random.nextInt(finishWeight / 6)
			mCircleInfo.setCurrentPostionX(startX);
			mCircleInfo.setCurrentPostionY(startY);
			mCircleInfo.setCircleRadius(mCircleSize);
			mCircleInfo.setColor(mColorHelper
					.getColor(startX, startY));
			mCircleInfo.setAlpha(getRandomsetAlpha());
			mListCircleInfo.add(mCircleInfo);
		}
		
		if (mListCircleInfo.size() >= circleTotal) {
			for (int i = 0; i < circleCount; i++) {
				mListCircleInfo.remove(0);
			}
		}
		circleTag = true;
		postInvalidate();
	}
	
	/***
	 * 移动气泡(改变气泡位置)
	 */
	private void moveCirle() {
		for (int i = 0; i < mListCircleInfo.size(); i++) {
			CircleBean mCircleInfo=mListCircleInfo.get(i);
			/*float radius = mCircleInfo.getCircleRadius() + 0.4f;
			if (radius > 30) {
				radius = 30;
			}*/
			if (mCircleInfo.getCurrentPostionY() <= 0) {
				mListCircleInfo.remove(i);
				continue;
			}
//			mCircleInfo.setCircleRadius(radius);
			mCircleInfo.setCurrentPostionX((float) (mCircleInfo.getCurrentPostionX() + mCircleInfo.getMoveSize()/ Math.tan(mCircleInfo.getAngle())));
			mCircleInfo.setCurrentPostionY(mCircleInfo.getCurrentPostionY() - mCircleInfo.getMoveSize());
			int alf = mCircleInfo.getAlpha() - 3;
			if (alf < 0)
				alf = 0;
			mCircleInfo.setAlpha(alf);
		}
		postInvalidate();
	}
	
	private int getRandomsetAlpha() {
		Random rad = new Random();
		int mRandom = 100 + rad.nextInt(100);
		return mRandom;
	}
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
		random = null;
	}

	@Override
	protected int getLockType() {
		// TODO Auto-generated method stub
		return LockConfigBean.FLY_LOCK_TYPE;
	}

	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		setVisibility(View.VISIBLE);
		scrollTo(0, 0);
	}

	@Override
	public void updateTime() {
		if (mTimeTxt != null) {
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (mDateTxt != null) {
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		}
	}
	
	private TurnPageView shutterView;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (null == shutterView) {
			shutterView = (TurnPageView) findViewById(R.id.surface);
			
			if (shutterView != null) {
				Bitmap bitmap = LockScreenApplication.getBgImg();
				if (null == bitmap)
					bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.default_);
				shutterView.setBitmap(bitmap);
			}
		}
	}
}
