package com.prize.lockscreen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.bean.CircleBean;
import com.prize.lockscreen.interfaces.ICanTouch;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;

/**
 * 滑动汽泡解锁
 * @author fanjunchen
 * 
 */
public class ColorBubbleView extends BaseRootView implements IResetView, ICanTouch {

	private final static String TAG = ColorBubbleView.class.getName();

//	private VelocityTracker mVelTracker;

	private int mLastY = 0, mLastX = 0;

	private int mCurY, mCurX;

	private int mDelY;

	private List<CircleBean> mListCircleInfo;
	
	private int finishWeight = 0;
	
	private Random random = new Random();
	/**是否按下*/
	private boolean isDown = false;
	/**触摸是否已经失效*/
	private boolean isTouchDis = false;
	
	public ColorBubbleView(Context context) {
		this(context, null);
	}
	
	public ColorBubbleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ColorBubbleView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		mListCircleInfo = new ArrayList<CircleBean>();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	/**计时用的变量*/
	private long aTime = 0, moveTime = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!isTouchable)
			return false;
		if (isTouchDis)
			return super.onTouchEvent(event);
		
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			mVelTracker.addMovement(event);	*/
			setWeight();
			
			isDown = true;
			LogUtil.d(TAG,"------>onTouchEvent x = " + event.getX() + "  ,y = " + event.getY());
			mLastX = mCurX = (int) event.getX();
			mLastY = mCurY = (int) event.getY();
			Message msg = mHandler.obtainMessage();
			msg.arg1 = mCurX;
			msg.arg2 = mCurY;
			msg.what = MSG_PRODUCE;
			msg.sendToTarget();
			moveTime = aTime = SystemClock.elapsedRealtime();
			return true;
		case MotionEvent.ACTION_MOVE:
			// mVelTracker.addMovement(event);
			mCurY = (int) event.getY();
			mCurX = (int) event.getX();
			
			if ((SystemClock.elapsedRealtime() - aTime) > 200) {
				aTime = SystemClock.elapsedRealtime();
				msg = mHandler.obtainMessage();
				msg.arg1 = mCurX;
				msg.arg2 = mCurY;
				
				msg.what = MSG_PRODUCE;
				mHandler.sendMessage(msg);
			}
			
			if ((SystemClock.elapsedRealtime() - moveTime) > 100) {
				moveTime = SystemClock.elapsedRealtime();
				
				int x = mLastX - mCurX;
				int y = mLastY - mCurY;
				int x1 = Math.abs(x);
				int y1 = Math.abs(y);
				if (x1 > 150 || Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) > 150 ||
						y1 > 150) {
					isTouchDis = true;
					transparent();
					mHandler.sendEmptyMessageDelayed(MSG_CAN_TOUCH, 600);
//					if (y < 0 && y1 > x1)
//						moveDown();
//					else if (y > 0 && y1 > x1)
//						moveUp();
//					else if (x < 0 && x1 >= y1)
//						moveRight();
//					else if (x > 0 && x1 >= y1)
//						moveLeft();
				}
				mLastX = mCurX;
				mLastY = mCurY;
			}
			break;
		case MotionEvent.ACTION_UP:
			isDown = false;
			/*float vY = mVelTracker.getYVelocity();
			
			Log.i(TAG, "===Yvelocity==" + vY);
			if (calAndMoveTo(vY))
				break;*/
			mCurY = (int) event.getY();
			mDelY = mCurY - mLastY;
			if (mDelY <= 0) {
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			isDown = false;
			break;
		}
		return super.onTouchEvent(event);
	}
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
				rs = true;
			}
		}
		return rs;
	}
    
	@Override
	public void computeScroll() {
		super.computeScroll();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mBitmap || mBitmap.isRecycled())
			setBgBitmap(LockScreenApplication.getBgImg());
		//canvas.drawBitmap(mBitmap,0,0,null);
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
	private final static int MSG_MOVE = MSG_PRODUCE + 1;
	/**没有移动但是为按下未抬起状态**/
	private final static int MSG_NO_MOV = MSG_MOVE + 1;
	/**可以触摸**/
	private final static int MSG_CAN_TOUCH = MSG_NO_MOV + 1;
	
	private final static int delayMillis = 10;
	/**1秒钟时长**/
	private final int ONE_SEC = 1000;
	
	private final static int circleCount = 12;
	private final static int circleTotal = 60;
	private static boolean circleTag = false;
	
	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_PRODUCE:
				createCirle(msg.arg1, msg.arg2);
				mHandler.sendEmptyMessage(MSG_MOVE);
				mHandler.removeMessages(MSG_NO_MOV);
				mHandler.sendEmptyMessageDelayed(MSG_NO_MOV, ONE_SEC);
				break;
			case MSG_MOVE:
				circleTag = true;
				moveCirle();
				mHandler.removeMessages(MSG_MOVE);
				mHandler.sendEmptyMessageDelayed(MSG_MOVE, delayMillis);
				break;
			case MSG_NO_MOV:
				if (isDown) {
					createCirle(mCurX, mCurY);
					mHandler.sendEmptyMessage(MSG_MOVE);
					mHandler.removeMessages(MSG_NO_MOV);
					mHandler.sendEmptyMessageDelayed(MSG_NO_MOV, ONE_SEC);
				}
				break;
			case MSG_CAN_TOUCH:
				isTouchDis = false;
				isDown = false;
				break;
				default:
					break;
			}
		}
	};
	
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
	private void createCirle(int startX, int startY) {
		
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
	 * 移动汽泡
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
			mCircleInfo.setCurrentPostionX((float) (mCircleInfo.getCurrentPostionX() + 
					mCircleInfo.getMoveSize() / Math.tan(mCircleInfo.getAngle())));
			mCircleInfo.setCurrentPostionY(mCircleInfo.getCurrentPostionY() - mCircleInfo.getMoveSize());
			int alf = mCircleInfo.getAlpha() - 3;
			if (alf < 0)
				alf = 0;
			mCircleInfo.setAlpha(alf);
		}
		postInvalidate();
	}
	/***
	 * 随机获取透明度
	 * @return
	 */
	private int getRandomsetAlpha() {
		Random rad = new Random();
		int mRandom = 100 + rad.nextInt(100);
		return mRandom;
	}
	@Override
	protected void finalize() {
		mHandler = null;
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		random = null;
	}

	@Override
	protected int getLockType() {
		return LockConfigBean.COLOR_LOCK_TYPE;
	}
	@Override
	public void resetView() {
		setAlpha(1f);
		setVisibility(View.VISIBLE);
	}
	
	@Override
	public void updateTime() {
		// TODO Auto-generated method stub mDateTxt
		if (mTimeTxt != null) {
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (mDateTxt != null) {
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		}
	}
	
	private boolean isTouchable = true;
	@Override
	public void setTouchable(boolean isTouch) {
		isTouchable = isTouch;
	}
}
