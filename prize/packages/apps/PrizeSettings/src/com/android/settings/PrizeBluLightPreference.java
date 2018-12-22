package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.provider.Settings;

import com.mediatek.pq.PictureQuality;


public class PrizeBluLightPreference extends Preference {
	public static final String TAG = "PrizeBluLightPreference";
	private Context mContext;
	private TextView mModeStatus;
	private BluLightObserver mBluLightObserver;

	public PrizeBluLightPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public PrizeBluLightPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.prize_blulight_material_summary_right);
		setWidgetLayoutResource(R.layout.prize_blulight_widget_text_right_arrow);
		mContext = context;
	}


	@Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        	super.onBindViewHolder(view);
			
		mModeStatus = (TextView) view.findViewById(R.id.mode_status);
		Handler handler = new Handler();
		mBluLightObserver = new BluLightObserver(handler);
		mBluLightObserver.startObserving();
		setStatus();
	}

	/** ContentObserver to watch blulight mode status **/
	private class BluLightObserver extends ContentObserver {

		private final Uri BLULIGHT_MODE_URI = Settings.System
				.getUriFor(Settings.System.PRIZE_BLULIGHT_MODE_STATE);
		private final Uri BLULIGHT_TIME_URI = Settings.System
				.getUriFor(Settings.System.PRIZE_BLULIGHT_TIME_STATE);

		public BluLightObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Log.d(TAG, "onChange() selfChange:" + selfChange);
			if (selfChange)
				return;
			if (BLULIGHT_MODE_URI.equals(uri)) {
				setStatus();
			} else if (BLULIGHT_TIME_URI.equals(uri)) {
				setStatus();
			}

		}

		public void startObserving() {
			final ContentResolver cr = mContext.getContentResolver();
			cr.unregisterContentObserver(this);
			cr.registerContentObserver(BLULIGHT_MODE_URI, false, this,
					UserHandle.USER_ALL);
			cr.registerContentObserver(BLULIGHT_TIME_URI, false, this,
					UserHandle.USER_ALL);
		}

		public void stopObserving() {
			final ContentResolver cr = mContext.getContentResolver();
			cr.unregisterContentObserver(this);
		}
	}

	public void setStatus(){
		final ContentResolver resolver = mContext.getContentResolver();
		int mBluLightStatus = Settings.System.getInt(resolver,
				Settings.System.PRIZE_BLULIGHT_MODE_STATE, 0);

		final String[] entries = mContext.getResources().getStringArray(R.array.bluLight_entries);
		String modeStatus = null;
		if(mBluLightStatus == 0){
			modeStatus = entries[0];
		} else {
			modeStatus = entries[1];
		}
		Log.d(TAG,"setStatus() mModeStatus = "+mModeStatus);
		if(modeStatus != null && mModeStatus != null){
			mModeStatus.setText(modeStatus);
		}
	}

}
