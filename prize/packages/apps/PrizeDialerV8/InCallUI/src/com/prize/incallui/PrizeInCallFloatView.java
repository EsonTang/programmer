package com.prize.incallui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Create by hpf on 2018-3-26 for Game-Modle
 * 
 */
public class PrizeInCallFloatView extends LinearLayout {
	
	private String TAG = "PrizeInCallFloatView";
	private OnScreenOrientationChangeListener mOnScreenOrientationChangeListener;
	
	public PrizeInCallFloatView(Context context) {
        super(context, null);
    }

    public PrizeInCallFloatView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "[onConfigurationChanged]  newConfig = "+newConfig);
		if(mOnScreenOrientationChangeListener != null){
			mOnScreenOrientationChangeListener.onChange(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
		}
	}
	
	public interface OnScreenOrientationChangeListener{
		void onChange(boolean isLandScape);
	}
	
	public void setOnScreenOrientationChangeListener(OnScreenOrientationChangeListener onScreenOrientationChangeListener){
		this.mOnScreenOrientationChangeListener = onScreenOrientationChangeListener;
	}

}
