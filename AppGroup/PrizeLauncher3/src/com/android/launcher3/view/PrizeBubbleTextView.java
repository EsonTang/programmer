
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：包装BubleTextView
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.view;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;

/**
 * This widget display an analogic clock with two hands for hours and minutes.
 */
/**目的：此类主要的目的是实现时钟实时刷新的功能
 * @author Administrator
 * 
 */
@RemoteView
public class PrizeBubbleTextView extends BubbleTextView {
	public PrizeBubbleTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private Time mCalendar;

	private Drawable mHourHand;
	private Drawable mMinuteHand;
	private Drawable mSecondHand;
	private Drawable mDial;

	/**
	 * 时钟信息
	 */
	private ComponentName mDeskClockComponentName = new ComponentName(
			"com.android.deskclock", "com.android.deskclock.DeskClock");

	/**
	 * 指定为时钟icon
	 */
	private boolean isDeskClockView = false;
	private boolean isResourceOK = false;

	private boolean mAttached;

	private final Handler mHandler = new Handler();
	private float mMinutes;
	private float mHour;
	private boolean mChanged;

	Context mContext;

	public PrizeBubbleTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PrizeBubbleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources r = context.getResources();
		mContext = context;
		/*mDial = ThemeInfoUtils.getThemeDeskIcon("clock", context);
		mHourHand = ThemeInfoUtils.getThemeDeskIcon("hour", context);

		mMinuteHand = ThemeInfoUtils.getThemeDeskIcon("min", context);
		mSecondHand = ThemeInfoUtils.getThemeDeskIcon("point", context);*/
		
		isResourceOK = mHourHand != null && mMinuteHand != null
				&& mDial != null && mSecondHand != null;
		

		mCalendar = new Time();
	}
	/**
	 * 判断此app是否为无效的app
	 * @param pm
	 * @param cn
	 * @return
	 */
	private boolean isValidPackageComponent(PackageManager pm, ComponentName cn) {
        if (cn == null) {
            return false;
        }

        try {
            // Skip if the application is disabled
            PackageInfo pi = pm.getPackageInfo(cn.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                return false;
            }

            // Check the activity
            return (pm.getActivityInfo(cn, 0) != null);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

	/* (non-Javadoc)
	 * 1.监听时间注册 
	 * 2.只监听deskClock 应用icon
	 * @see com.android.launcher3.BubbleTextView#onAttachedToWindow()
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ShortcutInfo info = (ShortcutInfo) this.getTag();
		if (info == null || info.intent == null) {
			return;
		}
		ComponentName cn = info.intent.getComponent();
		PackageManager pm = this.getContext().getPackageManager();
		boolean isVaild = isValidPackageComponent(pm, cn);
		if (cn != null && isVaild
				&& info.intent.getComponent().equals(mDeskClockComponentName)) {

			isDeskClockView = true;
			if (!mAttached) {
				mAttached = true;
				IntentFilter filter = new IntentFilter();

				filter.addAction(Intent.ACTION_TIME_TICK);
				filter.addAction(Intent.ACTION_TIME_CHANGED);
				filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

				getContext().registerReceiver(mIntentReceiver, filter, null,
						mHandler);
			}
			mCalendar = new Time();
			onTimeChanged();
			counter.start();
		}
	}

	/* (non-Javadoc)
	 * 注销
	 * @see com.android.launcher3.BubbleTextView#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (isDeskClockView) {

			if (mAttached) {
				counter.cancel();
				getContext().unregisterReceiver(mIntentReceiver);
				mAttached = false;
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (isDeskClockView) {
			mChanged = true;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isDeskClockView&&isResourceOK) {
			boolean changed = mChanged;
			if (changed) {
				mChanged = false;
			}

			boolean seconds = mSeconds;
			if (seconds) {
				mSeconds = false;
			}

			int availableWidth = Utilities.sIconWidth;
			int availableHeight = Utilities.sIconWidth;

			int x = this.getWidth() / 2;
			int y = this.getCompoundPaddingTop() / 2 + this.getPaddingTop() / 2;

			int posX = this.getScrollX();
			int posY = this.getScrollY();

			final Drawable dial = mDial;
			int w = dial.getIntrinsicWidth();
			int h = dial.getIntrinsicHeight();

			boolean scaled = false;

			if (availableWidth < w || availableHeight < h) {
				scaled = true;
				float scale = Math.min((float) availableWidth / (float) w,
						(float) availableHeight / (float) h);
				canvas.save();
				canvas.scale(scale, scale, x, y);
			}

			if (changed) {
				dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
						+ (h / 2));
			}
			canvas.translate(posX, posY);

			dial.draw(canvas);

			canvas.save();
			canvas.rotate(mHour / 12.0f * 360.0f, x, y);
			final Drawable hourHand = mHourHand;
			if (changed) {
				w = hourHand.getIntrinsicWidth();
				h = hourHand.getIntrinsicHeight();
				hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
						+ (h / 2));
			}
			hourHand.draw(canvas);
			canvas.restore();

			canvas.save();
			canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
			final Drawable minuteHand = mMinuteHand;
			if (changed) {
				w = minuteHand.getIntrinsicWidth();
				h = minuteHand.getIntrinsicHeight();
				minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
						+ (h / 2));
			}
			minuteHand.draw(canvas);
			canvas.restore();
			canvas.save();
			canvas.rotate(mSecond, x, y);
			if (seconds) {
				w = mSecondHand.getIntrinsicWidth();
				h = mSecondHand.getIntrinsicHeight();
				mSecondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
						+ (h / 2));

			}
			mSecondHand.draw(canvas);
			canvas.restore();
			if (scaled) {
				canvas.restore();
			}

		}
	}

	MyCount counter = new MyCount(10000, 1000);

	public class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			counter.start();
		}

		@Override
		public void onTick(long millisUntilFinished) {

			if (isDeskClockView) {
				mCalendar.setToNow();

				int hour = mCalendar.hour;
				int minute = mCalendar.minute;
				int second = mCalendar.second;

				mSecond = 6.0f * second;
				mSeconds = true;
				PrizeBubbleTextView.this.invalidate();
			}
		}
	}

	boolean mSeconds = false;
	float mSecond = 0;

	private void onTimeChanged() {
		mCalendar.setToNow();

		int hour = mCalendar.hour;
		int minute = mCalendar.minute;
		int second = mCalendar.second;

		mMinutes = minute + second / 60.0f;
		mHour = hour + mMinutes / 60.0f;
		mChanged = true;
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (isDeskClockView) {
				if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
					String tz = intent.getStringExtra("time-zone");
					mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
				}

				onTimeChanged();

				invalidate();
			}
		}
	};
}
