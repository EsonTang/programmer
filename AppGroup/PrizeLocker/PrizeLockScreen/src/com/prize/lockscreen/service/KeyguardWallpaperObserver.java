package com.prize.lockscreen.service;

import com.prize.lockscreen.receiver.GetImg;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
/***
 * 用于锁屏壁纸监听器, 主要是用于与第三方对接
 * @author fanjunchen
 *
 */
public class KeyguardWallpaperObserver extends ContentObserver {

	private Context mContext;
	
	private static final String KEYGUARD_WALLPAPER_URI = "keyguard_wallpaper";
	
    public KeyguardWallpaperObserver(Handler handler, Context ctx) {
        super(handler);
        mContext = ctx;
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (selfChange) return;
        final String strPath = Settings.System.getString(mContext.getContentResolver(), KEYGUARD_WALLPAPER_URI);
        if (!TextUtils.isEmpty(strPath)) {
        	GetImg g = new GetImg(strPath, mContext);
        	g.execute();
        }
    }

    public void startObserving() {
        final ContentResolver cr = mContext.getContentResolver();
        cr.unregisterContentObserver(this);
        cr.registerContentObserver(
                Settings.System.getUriFor(KEYGUARD_WALLPAPER_URI),
                false, this);

    }

    public void stopObserving() {
        final ContentResolver cr = mContext.getContentResolver();
        cr.unregisterContentObserver(this);
    }

}