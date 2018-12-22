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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.mediatek.pq.PictureQuality;

public class PrizeBluLightContrlPreference extends Preference {
	private static final String TAG = "PrizeBluLightContrlPreference";

	private Context mContext;
	private LayoutInflater inflater;
	private LinearLayout mLightControl;
	private SeekBar mSeekBar;
	private Range mBlueLightRange;

	public PrizeBluLightContrlPreference(Context context) {
		this(context, null, 0);		
	}

	public PrizeBluLightContrlPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PrizeBluLightContrlPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setLayoutResource(R.layout.prize_blulight_control);
		mContext = context;
	}

	@Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
		
		mLightControl = (LinearLayout) view
				.findViewById(R.id.blulight_control_ll);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		Log.d("lhy","onBindViewHolder mLightControl:"+mLightControl+",mSeekBar:"+mSeekBar);

		mBlueLightRange = getBlueLightIndexRange();

		updateBrightnessSeekBar();
		mSeekBar.setOnSeekBarChangeListener(new PrizeSeekBarChangeListener());
	}

	public class PrizeSeekBarChangeListener implements OnSeekBarChangeListener {


		@Override
		public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
			Log.d(TAG, "onProgressChanged() Progress Value = " + value);								
			/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
			//if(PrizeBluLightUtil.BLUELIGHT_MTK)
			{
				value = value+PrizeBluLightUtil.BLULIGHT_MIN_VALUE-mBlueLightRange.min;
				PictureQuality.setBlueLightStrength(value);
			}			
			/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
			Log.d(TAG, "onProgressChanged() BlueLightStrength Value = " + value);	
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private void updateBrightnessSeekBar() {
		if (mSeekBar != null) {
			int maxProgress = mBlueLightRange.max - mBlueLightRange.min-PrizeBluLightUtil.BLULIGHT_MIN_VALUE;
			Log.d(TAG,"updateBrightnessSeekBar() maxProgress = "+maxProgress);
			mSeekBar.setMax(maxProgress);
			/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
			int progress = 0;
			if(PrizeBluLightUtil.BLUELIGHT_MTK)
			{
				progress = PictureQuality.getBlueLightStrength()- mBlueLightRange.min - PrizeBluLightUtil.BLULIGHT_MIN_VALUE;
			}
			
			/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/
			
			Log.d(TAG,"updateBrightnessSeekBar() progress = "+progress);
			if(PrizeBluLightUtil.BLUELIGHT_MTK)
			{
				mSeekBar.setProgress(progress);
			}			
		}

	}

	public  Range getBlueLightIndexRange() {
		Range mRange = new Range();				
		/*prize-modify for huyanmode-lihuangyuan-2017-06-09-start*/
		if(PrizeBluLightUtil.BLUELIGHT_MTK)
		{
			PictureQuality.Range mPQrange;
			mPQrange = PictureQuality.getBlueLightStrengthRange();
			mRange.set(mPQrange.min, mPQrange.max, mPQrange.defaultValue);
		}
		
		/*prize-modify for huyanmode-lihuangyuan-2017-06-09-end*/		
		return mRange;
	}

	public static class Range {
		public int min;
		public int max;
		public int defaultValue;

		public Range() {
			set(0, 0, 0);
		}

		public void set(int min, int max, int defaultValue) {
			this.min = min;
			this.max = max;
			this.defaultValue = defaultValue;
		}
	}

}
