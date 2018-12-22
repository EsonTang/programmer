package com.android.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.provider.Settings;

import java.util.Calendar;

public class PrizeBluLightTimePreference extends Preference {
	public static final String TAG = "PrizeBluLightPreference";
	private Context mContext;
	private TextView mModeStatus;

	private int mHourOfDay;
	private int mMinute;
	private Callback mCallback;

	public PrizeBluLightTimePreference(Context context, final FragmentManager mgr) {
		super(context);
		setLayoutResource(R.layout.prize_blulight_material_summary_right);
		setWidgetLayoutResource(R.layout.prize_blulight_widget_text_right_arrow);
		
		mContext = context;
		setPersistent(false);
		setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				TimePickerFragment frag = new TimePickerFragment();
				frag.pref = PrizeBluLightTimePreference.this;
				frag.show(mgr, PrizeBluLightTimePreference.class.getName());
				return true;
			}
		});
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}	

	@Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        	super.onBindViewHolder(view);

		mModeStatus = (TextView) view.findViewById(R.id.mode_status);
		final ContentResolver resolver = mContext.getContentResolver();
		String mBluLightTimeStatus = Settings.System.getString(resolver, getKey());
		if(mBluLightTimeStatus == null){
			String key = getKey();
			if(key.equals(Settings.System.PRIZE_BLULIGHT_START_TIME)){
				mBluLightTimeStatus ="22:00";
			} else if(key.equals(Settings.System.PRIZE_BLULIGHT_END_TIME)){
				mBluLightTimeStatus ="07:00";
			}
			Settings.System.putString(resolver, getKey(),mBluLightTimeStatus);
		}
		setStatus(mBluLightTimeStatus);

	}

	public void setStatus(String modeStatus){
		Log.d(TAG,"setStatus() modeStatus = "+modeStatus);
		if(modeStatus != null && mModeStatus != null){
			mModeStatus.setText(modeStatus);
		}
	}

	public void setTime(int hourOfDay, int minute) {
		if (mCallback != null && !mCallback.onSetTime(hourOfDay, minute)) return;
		mHourOfDay = hourOfDay;
		mMinute = minute;
		Log.d(TAG,"setTime() mHourOfDay = "+mHourOfDay+", mMinute = "+mMinute);
		updateSummary();
	}

	private void updateSummary() {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, mHourOfDay);
		c.set(Calendar.MINUTE, mMinute);
		String hourAndMinute = formatTime(c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE));
		setStatus(hourAndMinute);
	}

	private String formatTime(int hour, int minute){
		String hourStr = null;
		if(hour < 10){
			hourStr = "0"+hour;
		} else {
			hourStr = ""+hour;
		}
		String minuteStr = null;
		if(minute < 10){
			minuteStr = "0"+minute;
		} else {
			minuteStr = ""+minute;
		}
		String hourAndMinute = hourStr+":"+minuteStr;
		return hourAndMinute;
	}

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {
		public PrizeBluLightTimePreference pref;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if(pref != null){
				int[] mHourAndMinuteArr = PrizeBluLightUtil.parseBluLightTime(pref.getContext(),pref.getKey());
				final Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, mHourAndMinuteArr[0]);
				calendar.set(Calendar.MINUTE, mHourAndMinuteArr[1]);
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				Log.d("BluLight","onCreateDialog() hour = "+hour+", minute = "+minute);
				return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
			}
			return super.onCreateDialog(savedInstanceState);
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (pref != null) {
				pref.setTime(hourOfDay, minute);
			}
		}
	}

	public interface Callback {
		boolean onSetTime(int hour, int minute);
	}

}
