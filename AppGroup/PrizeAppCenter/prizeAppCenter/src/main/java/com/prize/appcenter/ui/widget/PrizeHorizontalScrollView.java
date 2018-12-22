package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * @author prize
 */
public class PrizeHorizontalScrollView extends HorizontalScrollView {
	private Handler mHandler;

	/**
	 * 滚动状态:
	 * IDLE=滚动停止
	 * TOUCH_SCROLL=手指拖动滚动
	 * FLING=滚动
	 */
//	enum ScrollType{IDLE,TOUCH_SCROLL,FLING};
	public static final  int IDLE=0;
	public static final  int TOUCH_SCROLL=1;
	public static final  int FLING=2;

	/**
	 * 记录当前滚动的距离
	 */
	private int currentX = -9999999;

	/**
	 * 当前滚动状态
	 */
	private int scrollType = IDLE;

	/**
	 * @author prize
	 */
	public interface OnScrollChangedListener {
		void onScrollChanged(int scrollType);
	}

	private OnScrollChangedListener mOnScrollChangedListener;

	public PrizeHorizontalScrollView(Context context) {
		super(context);
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new Handler();
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnScrollChangedListener(OnScrollChangedListener listener) {
		mOnScrollChangedListener = listener;
	}

	/**
	 * 滚动监听runnable
	 */
	private Runnable scrollRunnable = new Runnable() {
		@Override
		public void run() {
			if (getScrollX()==currentX) {
				//滚动停止,取消监听线程
				scrollType = IDLE;
				if (mOnScrollChangedListener!=null) {
					mOnScrollChangedListener.onScrollChanged(scrollType);
				}
				mHandler.removeCallbacks(this);
				return;
			} else {
				//手指离开屏幕,但是view还在滚动
				scrollType = FLING;
				if(mOnScrollChangedListener!=null){
					mOnScrollChangedListener.onScrollChanged(scrollType);
				}
			}
			currentX = getScrollX();
			//滚动监听间隔:milliseconds
			mHandler.postDelayed(this, 50);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				this.scrollType = TOUCH_SCROLL;
				if(mOnScrollChangedListener!=null){
					mOnScrollChangedListener.onScrollChanged(scrollType);
				}
				mHandler.removeCallbacks(scrollRunnable);
				break;
			case MotionEvent.ACTION_UP:
				mHandler.post(scrollRunnable);
				break;
		}
		return super.onTouchEvent(ev);
	}
}