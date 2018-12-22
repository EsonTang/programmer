package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public interface OnTouchStateListener {
		public void onTouchStateListener();
	}

	private OnTouchStateListener mOnTouchStateListener;

	public void setOnTouchStateListener(
			OnTouchStateListener mOnTouchStateListener) {
		this.mOnTouchStateListener = mOnTouchStateListener;
	}

	public void init() {
		/******************** 监听ScrollView滑动停止 *****************************/
		this.setOnTouchListener(new OnTouchListener() {
			private int lastY = 0;
			private int touchEventId = -9983761;
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					View scroller = (View) msg.obj;
					if (msg.what == touchEventId) {
						if (lastY == scroller.getScrollY()) {
							handleStop(scroller);
						} else {
							handler.sendMessageDelayed(handler.obtainMessage(
									touchEventId, scroller), 5);
							lastY = scroller.getScrollY();
						}
					}
				}
			};

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.sendMessageDelayed(
							handler.obtainMessage(touchEventId, v), 5);
				}
				return false;
			}

			private void handleStop(Object view) {
//				if (isfling) {
					mOnTouchStateListener.onTouchStateListener();
					isfling = false;
//				}
			}
		});
		/***********************************************************/
	}

	public MyScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void fling(int velocityY) {
		isfling = true;
		super.fling(velocityY);

	}

	public boolean isfling = false;

}
