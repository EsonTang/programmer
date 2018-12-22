/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：自定义SeekBarPreference调节亮度
 *当前版本：v1.0
 *作	者: 黄典俊
 *完成日期：2015-05-20
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

//package com.android.settings;
package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;

import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBarPrize;
import android.widget.SeekBarPrize.OnSeekBarChangeListener;

import java.util.Timer;
import java.util.TimerTask;

public class PrizeLightContrlPreference extends Preference {

	public static final String TAG = "prize";
	private Context mContext;
	private LayoutInflater inflater;
	private LinearLayout mLightControl;
	private SeekBarPrize mSeekBar;
	private int real_degree;
	private boolean mAutomatic;
	int mMinimumBacklight;
	private PowerManager pm;
	private IPowerManager mPower;
	int mMaximumBacklight;
	private BrightnessObserver mBrightnessObserver;
	private static final float BRIGHTNESS_ADJ_RESOLUTION = 100f;
	private int mFlag = 2;
	private int mMax;
	private boolean isTouch = false;
	private SwitchPreference mAutoBrightnessPreference;
    public PrizeLightContrlPreference(Context context) {
        this(context, null);
        mContext = context;
    }
    
    public PrizeLightContrlPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.seekBarPreferenceStyle);
        mContext = context;
    }
    
    public PrizeLightContrlPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        mContext = context;
    }
	
	public PrizeLightContrlPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        setMax(a.getInt(com.android.internal.R.styleable.ProgressBar_max, mMax));
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        final int layoutResId = a.getResourceId(
                com.android.internal.R.styleable.SeekBarPreference_layout,
                com.android.internal.R.layout.preference_widget_seekbar);
        a.recycle();

        setLayoutResource(R.layout.prize_light_control);
    }

	@Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
		mLightControl = (LinearLayout) view.findViewById(R.id.prize_light_control);
		mSeekBar = (SeekBarPrize) view.findViewById(R.id.seekbar);
		mSeekBar.setMax(mMax);
		mAutomatic = getBrightnessMode();
		
		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
		mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
		Handler handler = new Handler();
		mBrightnessObserver = new BrightnessObserver(handler);
		mBrightnessObserver.startObserving();
		mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
		updateBrightnessSeekBar();
		mSeekBar.setOnSeekBarChangeListener(new PrizeSeekBarChangeListener());
	}
	
    public void setMax(int max) {
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

	public class PrizeSeekBarChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBarPrize seekBar, int value,
									  boolean fromUser) {
			Log.v("wuliang", "onProgressChanged seekBar value: " + (value + mMinimumBacklight) + " mAutomatic: " + mAutomatic + " fromUser: " + fromUser);
			if (!mAutomatic) {
				final int val = value + mMinimumBacklight;
				setBrightness(val);
			} else {
				//final float adj = value / (BRIGHTNESS_ADJ_RESOLUTION / 2f) - 1;
				//setBrightnessAdj(adj);
				if(fromUser){
					final int val = value + mMinimumBacklight;
					Settings.System.putIntForUser(
							mContext.getContentResolver(),
							Settings.System.SCREEN_AUTO_MODE_TEMP_BRIGHTNESS,
							val, UserHandle.USER_CURRENT);
				}
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBarPrize seekBar) {

		}

		@Override
		public void onStopTrackingTouch( SeekBarPrize seekBar) {
			final  int val = seekBar.getProgress();
			Log.v("wuliang", "onStopTrackingTouch seekBar value: " + (val + mMinimumBacklight) + " mAutomatic: " + mAutomatic);
			if (!mAutomatic) {
				AsyncTask.execute(new Runnable() {
					public void run() {
						Settings.System.putIntForUser(
								mContext.getContentResolver(),
								Settings.System.SCREEN_BRIGHTNESS, val  + mMinimumBacklight,
								UserHandle.USER_CURRENT);
					}
				});

			} else {
				AsyncTask.execute(new Runnable() {
					public void run() {
						Settings.System.putIntForUser(
								mContext.getContentResolver(),
								Settings.System.SCREEN_AUTO_MODE_TEMP_BRIGHTNESS,
								val+mMinimumBacklight, UserHandle.USER_CURRENT);
					}
				});
			}
		}

	}

	private boolean getBrightnessMode() {
		int automatic = Settings.System.getIntForUser(
				mContext.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
				UserHandle.USER_CURRENT);

		boolean isAutoMode = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
		Log.d(TAG, "getBrightnessMode:" + isAutoMode);
		return isAutoMode;

	}

	private void setBrightnessAdj(float adj) {
		try {
			mPower.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(adj);
		} catch (RemoteException ex) {
		}
	}

	private void setBrightness(int brightness) {
		Log.v(TAG, "****** brightness = " + brightness + "**********");
		try {
			mPower.setTemporaryScreenBrightnessSettingOverride(brightness);
		} catch (RemoteException ex) {
		}
	}

	/** ContentObserver to watch brightness **/
	private class BrightnessObserver extends ContentObserver {

		private final Uri BRIGHTNESS_MODE_URI = Settings.System
				.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
		private final Uri BRIGHTNESS_URI = Settings.System
				.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
		private final Uri BRIGHTNESS_ADJ_URI = Settings.System
				.getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ);
		private final Uri BRIGHTNESS_AUTO_MODE_URI = Settings.System
				.getUriFor(Settings.System.SCREEN_AUTO_MODE_BRIGHTNESS);

		public BrightnessObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Log.v(TAG, "onChange() selfChange:" + selfChange);
			if (selfChange)
				return;
			if (BRIGHTNESS_MODE_URI.equals(uri)) {
				mAutomatic = getBrightnessMode();
				Log.v(TAG, "onChange BRIGHTNESS_MODE_URI mAutomatic:"
						+ mAutomatic);
				if(!mAutomatic){
					updateBrightnessSeekBar();
				}
			} else if (BRIGHTNESS_URI.equals(uri) && !mAutomatic) {
				Log.v(TAG, "onChange BRIGHTNESS_URI mAutomatic:"
						+ mAutomatic);
				updateBrightnessSeekBar();
			} else if (BRIGHTNESS_ADJ_URI.equals(uri) && mAutomatic) {
				updateBrightnessSeekBar();
			}else if(BRIGHTNESS_AUTO_MODE_URI.equals(uri) && mAutomatic){
				updateBrightnessSeekBar();
			}
		}

		public void startObserving() {
			final ContentResolver cr = mContext.getContentResolver();
			cr.unregisterContentObserver(this);
			cr.registerContentObserver(BRIGHTNESS_MODE_URI, false, this,
					UserHandle.USER_ALL);
			cr.registerContentObserver(BRIGHTNESS_URI, false, this,
					UserHandle.USER_ALL);
			cr.registerContentObserver(BRIGHTNESS_ADJ_URI, false, this,
					UserHandle.USER_ALL);
			cr.registerContentObserver(BRIGHTNESS_AUTO_MODE_URI, false, this,
					UserHandle.USER_ALL);
		}

		public void stopObserving() {
			final ContentResolver cr = mContext.getContentResolver();
			cr.unregisterContentObserver(this);
		}
	}

	private void updateBrightnessSeekBar() {

		if (mAutomatic) {
			float value = Settings.System.getFloatForUser(
					mContext.getContentResolver(),
					Settings.System.SCREEN_AUTO_MODE_BRIGHTNESS, 0,
					UserHandle.USER_CURRENT);
			if (mSeekBar != null) {
				mSeekBar.setMax(mMaximumBacklight - mMinimumBacklight);
				mSeekBar.setProgress((int)value - mMinimumBacklight);
			}
			Log.v(TAG, "updateBrightnessSeekBar  value : " + value);
		} else {

			 int value = Settings.System.getIntForUser(
					mContext.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, mMaximumBacklight,
					UserHandle.USER_CURRENT);
			if (mSeekBar != null) {
				mSeekBar.setMax(mMaximumBacklight - mMinimumBacklight);
				mSeekBar.setProgress(value - mMinimumBacklight);
			}

			Log.v(TAG, "updateBrightnessSeekBar  value : " + value);
		}

	}
}
