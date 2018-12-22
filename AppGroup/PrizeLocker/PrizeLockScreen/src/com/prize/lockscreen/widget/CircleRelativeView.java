package com.prize.lockscreen.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.ICanTouch;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

/**
 * 圆圈左右上下解锁控件
 * @author fanjunchen
 * 
 */
public class CircleRelativeView extends BaseRootView implements IResetView, ICanTouch {

	private final static String TAG = "CircleRelativeView";
	
	private StarLockView mCoreView;

	public CircleRelativeView(Context context) {
		this(context, null);
	}
	
	public CircleRelativeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CircleRelativeView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isTouchable)
			return false;
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mCoreView = (StarLockView)findViewById(R.id.four_group);
		mCoreView.setUnlockListener(mUnlockListener);
		
	}
	@Override
	public void setUnlockListener(IUnLockListener listener) {
		mUnlockListener = listener;
		if (mCoreView != null)
			mCoreView.setUnlockListener(mUnlockListener);
	}
	
	/*@Override
	public void computeScroll() {

		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 不要忘记更新界面
			postInvalidate();
		} else {
			if (mCloseFlag && mUnlockListener != null) {
				mUnlockListener.onUnlockFinish();
				this.setVisibility(View.GONE);
			}
		}
	}*/
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mBitmap || mBitmap.isRecycled())
			setBgBitmap(LockScreenApplication.getBgImg());
		canvas.drawBitmap(mBitmap, 0, 0, null);
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
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	protected int getLockType() {
		return LockConfigBean.CIRCLE_LOCK_TYPE;
	}

	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		mCoreView.resetView();
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
