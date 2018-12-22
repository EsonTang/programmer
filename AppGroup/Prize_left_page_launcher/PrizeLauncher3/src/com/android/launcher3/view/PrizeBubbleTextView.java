/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：包装BubleTextView
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.view;

import java.sql.Date;
import java.util.Calendar;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DrawEditIcons;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.LqThemeParser;

/**
 * This widget display an analogic clock with two hands for hours and minutes.
 */
/**
 * 目的：此类主要的目的是实现时钟实时刷新的功能
 * 
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

	public PrizeBubbleTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PrizeBubbleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources r = context.getResources();
		mContext = context;
		updateDeskcomponent();
		mCalendar = new Time();
	}
	
	public void onTick() {

		if (isDeskClockView) {
			mCalendar.setToNow();

			int hour = mCalendar.hour;
			int minute = mCalendar.minute;
			int second = mCalendar.second;
			Calendar Cld = Calendar.getInstance(); 
			int mi = Cld.get(Calendar.MILLISECOND)+1000*second; 

			mSecond = 0.006f * mi;
			
			Date dt= new Date(second, second, second);
			Long time= dt.getTime();
			
			mSeconds = true;
			PrizeBubbleTextView.this.invalidate();
		}
	
		
	}
	
	public void clear() {
		if(mDial !=null&& !mDial.isRecycled()) {
//			mDial.recycle();
			mDial =null;
		}
		if(mHourHand !=null&& !mHourHand.isRecycled()) {
//			mHourHand.recycle();
			mHourHand =null;
		}
		if(mMinuteHand !=null&& !mMinuteHand.isRecycled()) {
//			mMinuteHand.recycle();
			mMinuteHand =null;
		}
		if(mSecondHand !=null&& !mSecondHand.isRecycled()) {
//			mSecondHand.recycle();
			mSecondHand =null;
		}

    	System.gc();
	}
	public void updateDeskcomponent() {
		clear();
		updateClockBg();

		isResourceOK = mHourHand != null && mMinuteHand != null
				&& mDial != null && mSecondHand != null;
	}
	
	public void updateClockBg() {
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY);
		if(h >6&&h <18) {
			mDial= LqThemeParser.getDeskIcon(mContext,
						LqShredPreferences.getLqThemePath(), "clock");
			final Bitmap bg= LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "bg_clock");
			mHourHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "hour");

			mMinuteHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "min");
			if(bg!=null) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
		    			 setCompoundDrawables(null,
		    					 Utilities.createIconDrawable(bg), null, null);
					}
				});
			}
		}else {
			final Bitmap bg= LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "bg_clock_n");
			if(bg!=null) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
		    			 setCompoundDrawables(null,
		    					 Utilities.createIconDrawable(bg), null, null);
					}
				});
			}
			mDial= LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "clock_n");
			mHourHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "hour_n");

			mMinuteHand = LqThemeParser.getDeskIcon(mContext,
					LqShredPreferences.getLqThemePath(), "min_n");
		}

		mSecondHand = LqThemeParser.getDeskIcon(mContext,
				LqShredPreferences.getLqThemePath(), "sec");
	}

	/**
	 * 判断此app是否为无效的app
	 * 
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

	/*
	 * (non-Javadoc) 1.监听时间注册 2.只监听deskClock 应用icon
	 * 
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
			((Launcher)mContext).getCounter().start();
		}
	}

	/*
	 * (non-Javadoc) 注销
	 * 
	 * @see com.android.launcher3.BubbleTextView#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (isDeskClockView) {

			if (mAttached) {
				((Launcher)mContext).getCounter().cancel();
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
		if (isDeskClockView && isResourceOK) {
			boolean changed = mChanged;
			if (changed) {
				mChanged = false;
			}
			
			boolean redeay=mHourHand != null && mMinuteHand != null
					&& mDial != null && mSecondHand != null;
			if(!redeay) {
				return;
			}
			

			boolean seconds = mSeconds&&Utilities.getDeskTick()!=-1;
			if (seconds) {
				mSeconds = false;
			}

			int availableWidth = Utilities.sIconWidth;
			int availableHeight = Utilities.sIconWidth;

			float x = this.getWidth() / 2f;
			float y = this.getCompoundPaddingTop() / 2f + this.getPaddingTop() / 2f+2f;
			if(Launcher.scale == 3){
				 x = this.getWidth() / 2f -1f;
				 y = this.getCompoundPaddingTop() / 2f + this.getPaddingTop() / 2f+4f;
			}

			int posX = this.getScrollX();
			int posY = this.getScrollY();

			final Bitmap dial = mDial;
			if(dial==null) {
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
			if(dialRect!=null)
			canvas.drawBitmap(dial, null, dialRect, null);

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.rotate(mSecond, x, y);
			if(mSecondHand==null || mSecondHand.isRecycled()) {
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
			
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.rotate(mHour / 12.0f * 360.0f, x, y);
			final Bitmap hourHand = mHourHand;
			if(hourHand==null || hourHand.isRecycled()) {
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
			if(minuteHand==null || mMinuteHand.isRecycled() ) {
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
			
			if (scaled) {
				canvas.restore();
			}

		}
			
			Launcher launcher = null;
			if (this.getContext() instanceof Launcher) {
				launcher = (Launcher) this.getContext();

			}
	        ShortcutInfo shortcutInfo = (ShortcutInfo) this.getTag();
			if (launcher != null) {
				if (launcher.getWorkspace().isInSpringLoadMoed()) {
					boolean isSpringfinish = launcher.getworkspace().ismSpringLoadFinish();
					if (isSpringfinish) {
						
						if (shortcutInfo !=null &&(shortcutInfo.flags&AppInfo.DOWNLOADED_FLAG)!=0) {
							
						}
						
						if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS&&shortcutInfo !=null &&shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL) {

							int w = FolderIcon.getIndicatorSize(mContext);
							int h = FolderIcon.getIndicatorSize(mContext);
							DrawEditIcons.drawStateIconForBatch(canvas, this,R.drawable.in_use,w,h,1f);//此处为画文件夹编辑小图标
						}
					}
				}else {
					if (launcher.getSpringState() != Launcher.SpringState.BATCH_EDIT_APPS) {
						if (shortcutInfo != null
								&& (shortcutInfo.flags & AppInfo.DOWNLOADED_FLAG) != 0) {
						}
					}
				}

				if (shortcutInfo !=null &&shortcutInfo.select) {
				}
				if (shortcutInfo!=null&&shortcutInfo.firstInstall==1 && !(getParent() instanceof ExplosionView)) {
					DrawEditIcons.drawFirstInstall(canvas, this,R.drawable.first_install);
				}
			}
		
	}

//	MyCount counter = new MyCount(10000, 1000);

	/*public class MyCount extends CountDownTimer {
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
	}*/

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
