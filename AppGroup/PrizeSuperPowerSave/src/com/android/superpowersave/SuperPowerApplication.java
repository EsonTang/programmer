package com.android.superpowersave;

import com.android.util.MtkFeatureOption;
import android.app.Application;
import android.content.IntentFilter;

public class SuperPowerApplication extends Application {

	private MTKUnreadLoader mUnreadLoader;

	@Override
	public void onCreate() {
		super.onCreate();

		// / M: register unread broadcast.
		if (MtkFeatureOption.getUnreadSupport()) {
			mUnreadLoader = new MTKUnreadLoader(getApplicationContext());
			// Register unread change broadcast.
			IntentFilter filter = new IntentFilter();
			filter.addAction(MTKUnreadLoader.MTK_ACTION_UNREAD_CHANGED);
			registerReceiver(mUnreadLoader, filter);
		}

	}
	
    public MTKUnreadLoader getUnreadLoader() {
        return mUnreadLoader;
    }
    
	public void setSuperPower(SuperPowerActivity superPowerActivity) {
		// / M: added for unread feature, initialize unread loader.
		if (MtkFeatureOption.getUnreadSupport()) {
			mUnreadLoader.initialize(superPowerActivity);
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		// / M: added for unread feature, unregister unread receiver.
		if (MtkFeatureOption.getUnreadSupport()) {
			unregisterReceiver(mUnreadLoader);
		}
	}

}
