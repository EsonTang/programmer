package com.android.launcher3.tsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.android.launcher3.R;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class NumberView extends View {

	private int mWidth;
	private int mHeight;
	private int MAXKEYS = 12;
	private int[] selfPosition = new int[2];
	Drawable mPress;
	Drawable mNormal;
	HashMap<Integer, NumberInfo> numbers = new HashMap<Integer, NumberInfo>();
	private Context mContext;

	public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public NumberView(Context context, AttributeSet attrs) {

		super(context, attrs);

		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();

		display = ((Activity) context).getWindowManager().getDefaultDisplay();
		mWidth = display.getWidth() / 3; /* mHeight = display.getHeight(); */
		mPress = context.getDrawable(R.drawable.ic_digits_pressed);
		mNormal = context.getDrawable(R.drawable.ic_digits_normal);
		mContext = context;
		setup();
	}

	public void setup() {
		for (int i = 0; i < MAXKEYS; i++) {
			NumberInfo info = new NumberInfo();
			switch (i) {
			case 0:

				info.icon = mContext.getDrawable(R.drawable.dial_num_1_normal);
				info.number = "1";
				break;
			case 1:
				info.icon = mContext.getDrawable(R.drawable.dial_num_2_normal);
				info.number = "2";

				mHeight = info.icon.getIntrinsicHeight() * 2;
				mPress.setBounds(0, 0, mWidth, mHeight);
				mNormal.setBounds(0, 0, mWidth, mHeight);

				break;
			case 2:
				info.icon = mContext.getDrawable(R.drawable.dial_num_3_normal);
				info.number = "3";
				break;
			case 3:

				info.icon = mContext.getDrawable(R.drawable.dial_num_4_normal);
				info.number = "4";
				break;
			case 4:

				info.icon = mContext.getDrawable(R.drawable.dial_num_5_normal);
				info.number = "5";
				break;
			case 5:

				info.icon = mContext.getDrawable(R.drawable.dial_num_6_normal);
				info.number = "6";
				break;
			case 6:

				info.icon = mContext.getDrawable(R.drawable.dial_num_7_normal);
				info.number = "7";
				break;
			case 7:

				info.icon = mContext.getDrawable(R.drawable.dial_num_8_normal);
				info.number = "8";
				break;
			case 8:

				info.icon = mContext.getDrawable(R.drawable.dial_num_9_normal);
				info.number = "9";
				break;
			case 9:

				info.icon = mContext.getDrawable(R.drawable.ic_back);
				info.number = "back";
				break;
			case 10:

				info.icon = mContext.getDrawable(R.drawable.dial_num_0_normal);
				info.number = "0";
				break;
			case 11:

				info.icon = mContext.getDrawable(R.drawable.ic_del);
				info.number = "del";
				break;

			default:
				break;
			}

			numbers.put(i, info);
		}
	}

	public NumberView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private MotionEvent mEvent;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		this.getLocationInWindow(selfPosition);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			mCallback.onClick(findNumber(event));
			ValueAnimator update = ObjectAnimator.ofFloat(0f, 1f);
			update.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float f = (Float) animation.getAnimatedValue();
					mPress.setAlpha((int) (255 * f));
					invalidate();
				}
			});
			update.start();
			break;
		case MotionEvent.ACTION_UP:

			// mCallback.onClick(mInfo.number);
			update = ObjectAnimator.ofFloat(1f, 0f);
			update.setDuration(1000);
			update.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float f = (Float) animation.getAnimatedValue();
					mPress.setAlpha((int) (255 * f));
					invalidate();
				}
			});
			update.start();
			break;

		default:
			break;
		}

		mEvent = event;
		// this.invalidate();
		return true;
	}

	SearchView mCallback;

	public void setCallback(SearchView call) {
		mCallback = call;
	}

	private NumberInfo mInfo;

	public String findNumber(MotionEvent event) {

		for (Entry<Integer, NumberInfo> info : numbers.entrySet()) {

			if (info.getValue().mContainer.contains((int) event.getX(),
					(int) event.getY())) {
				return info.getValue().number;

			}
		}
		return null;
	}

	private void drawView(Canvas canvas) {
		int w = mWidth;
		int h = mHeight;

		for (int i = 0; i < numbers.size(); i++) {
			NumberInfo info = numbers.get(i);
			Drawable icon = info.icon;
			int iconW = icon.getIntrinsicWidth();
			int iconH = icon.getIntrinsicHeight();
			int centerX = w / 2 - iconW / 2;
			int centerY = h / 2 - iconH / 2;
			icon.setBounds(centerX, centerY, centerX + iconW, centerY + iconH);
			int left = (i % 3) * w;
			int top = (i / 3) * h;
			int right = left + w;
			int botoom = top + h;
			if (mEvent == null)
				info.mContainer = new Rect(left + selfPosition[0], top
						+ selfPosition[1], right + selfPosition[0], botoom
						+ selfPosition[1]);
			canvas.save();
			canvas.translate(left, top);
			mNormal.draw(canvas);
			icon.draw(canvas);
			if (mEvent != null) {
				Rect contain = new Rect(left + selfPosition[0], top
						+ selfPosition[1], right + selfPosition[0], botoom
						+ selfPosition[1]);
				switch (mEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (contain.contains((int) mEvent.getX(),
							(int) mEvent.getY())) {
						mPress.draw(canvas);

					}
					break;
				case MotionEvent.ACTION_UP:
					if (contain.contains((int) mEvent.getX(),
							(int) mEvent.getY())) {
						mPress.draw(canvas);
					}
					break;
				default:
					break;
				}
			}

			canvas.restore();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawView(canvas);
		super.onDraw(canvas);
	}

}
