/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.format.Time;

import com.android.launcher3.Launcher.MyCount;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.LqThemeParser;

public class DeskClockDragView extends DragView {

	private Time mCalendar;

	private Bitmap mHourHand;
	private Bitmap mMinuteHand;
	private Bitmap mSecondHand;
	private Bitmap mDial;

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

	private RectF dialRect = new RectF();
	private RectF hourRect = new RectF();
	private RectF minRect = new RectF();
	private RectF secRect = new RectF();

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

	public void updateDeskcomponent() {
		clear();
		updateClockBg();

		isResourceOK = mHourHand != null && mMinuteHand != null
				&& mDial != null && mSecondHand != null;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

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

	@Override
	protected void onDetachedFromWindow() {

		if (isDeskClockView) {

			if (mAttached) {
				getContext().unregisterReceiver(mIntentReceiver);
				clear();
				counter.cancel();
				counter=null;
				mAttached = false;
			}
		}
		super.onDetachedFromWindow();
	}

	public void onTick() {

		if (isDeskClockView) {
			mCalendar.setToNow();

			int hour = mCalendar.hour;
			int minute = mCalendar.minute;
			int second = mCalendar.second;
			Calendar Cld = Calendar.getInstance();
			int mi = Cld.get(Calendar.MILLISECOND) + 1000 * second;

			mSecond = 0.006f * mi;

			Date dt = new Date(second, second, second);
			Long time = dt.getTime();

			mSeconds = true;
			DeskClockDragView.this.invalidate();
		}

	}

	public void clear() {
		if (mDial != null && !mDial.isRecycled()) {
			mDial.recycle();
			mDial = null;
		}
		if (mHourHand != null && !mHourHand.isRecycled()) {
			mHourHand.recycle();
			mHourHand = null;
		}
		if (mMinuteHand != null && !mMinuteHand.isRecycled()) {
			mMinuteHand.recycle();
			mMinuteHand = null;
		}
		if (mSecondHand != null && !mSecondHand.isRecycled()) {
			mSecondHand.recycle();
			mSecondHand = null;
		}
    	System.gc();
	}

	public void updateClockBg() {
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY);
		if (h > 6 && h < 18) {
			mDial = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "clock");
			final Bitmap bg = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "bg_clock");
			mHourHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "hour");

			mMinuteHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "min");
		} else {
			final Bitmap bg = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "bg_clock_n");
			mDial = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "clock_n");
			mHourHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "hour_n");

			mMinuteHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "min_n");
		}

		mSecondHand = LqThemeParser.getDeskIcon(mContext,
				LqShredPreferences.getLqThemePath(), "sec");
	}

	public DeskClockDragView(Launcher launcher, Bitmap bitmap,
			int registrationX, int registrationY, int left, int top, int width,
			int height, float initialScale) {
		super(launcher, bitmap, registrationX, registrationY, left, top, width,
				height, initialScale);

		mContext = launcher;
		updateDeskcomponent();
		mCalendar = new Time();
		onAttachedToWindow();
	}
	
	


	int desktick= Utilities.getDeskTick();
	int tick = desktick!=-1?desktick:1000;
	MyCount counter = new MyCount(10000, tick);

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
			DeskClockDragView.this.onTick();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isDeskClockView && isResourceOK) {
			boolean changed = mChanged;
			if (changed) {
				mChanged = false;
			}

			boolean redeay = mHourHand != null && mMinuteHand != null
					&& mDial != null && mSecondHand != null;
			if (!redeay) {
				return;
			}

			boolean seconds = mSeconds&&Utilities.getDeskTick()!=-1;
			if (seconds) {
				mSeconds = false;
			}

			int availableWidth = Utilities.sIconWidth;
			int availableHeight = Utilities.sIconWidth;

			float x = this.getWidth() / 2f;
			float y = this.getHeight() / 2f;

			int posX = this.getScrollX();
			int posY = this.getScrollY();

			final Bitmap dial = mDial;
			if (dial == null) {
				return;
			}
			int w = dial.getWidth();
			int h = dial.getHeight();

			boolean scaled = false;

			if (availableWidth < w || availableHeight < h) {
				scaled = true;
				float scale = Math.min((float) availableWidth / (float) w,
						(float) availableHeight / (float) h);
				canvas.save();
				canvas.scale(scale, scale, x, y);
			}

			if (changed) {
				dialRect.set(x - (w / 2f), y - (h / 2f), x + (w / 2f), y
						+ (h / 2f));
			}
			canvas.translate(posX, posY);
			if (dialRect != null)
				canvas.drawBitmap(dial, null, dialRect, null);

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.rotate(mHour / 12.0f * 360.0f, x, y);
			final Bitmap hourHand = mHourHand;
			if (hourHand == null) {
				return;
			}
			if (changed) {
				w = hourHand.getWidth();
				h = hourHand.getHeight();
				hourRect.set(x - (w / 2f), y - (h / 2f), x + (w / 2f), y
						+ (h / 2f));

				updateClockBg();
			}
			canvas.drawBitmap(hourHand, null, hourRect, null);
			canvas.restore();

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
			final Bitmap minuteHand = mMinuteHand;
			if (minuteHand == null) {
				return;
			}
			if (changed) {
				w = minuteHand.getWidth();
				h = minuteHand.getHeight();
				minRect.set(x - (w / 2f), y - (h / 2f), x + (w / 2f), y
						+ (h / 2f));
			}
			canvas.drawBitmap(minuteHand, null, minRect, null);
			canvas.restore();
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.rotate(mSecond, x, y);
			if (mSecondHand == null) {
				return;
			}
			if (seconds) {
				w = mSecondHand.getWidth();
				h = mSecondHand.getHeight();
				secRect.set(x - (w / 2f), y - (h / 2f), x + (w / 2f), y
						+ (h / 2f));

			}
			canvas.drawBitmap(mSecondHand, null, secRect, null);
			canvas.restore();
			if (scaled) {
				canvas.restore();
			}

		}

	}
}
