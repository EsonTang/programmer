package com.prize.setting;

import android.content.Context;
import android.util.AttributeSet;

import com.android.camera.ui.RotateLayout;
/**
 * 
 * @author wanzhijuan
 * When the virtual button, set the lock direction
 */
public class SettingRotateLayout extends RotateLayout {

	private static final String TAG = "SettingRotateLayout";
	private Context mContext;
	public SettingRotateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected int adjustOrientation(int orientation) {
		LogTools.i(TAG, "adjustOrientation orientation=" + orientation);
		if (NavigationBarUtils.checkDeviceHasNavigationBar(mContext)) {
			return 0;
		}
		return super.adjustOrientation(orientation);
	}
}
