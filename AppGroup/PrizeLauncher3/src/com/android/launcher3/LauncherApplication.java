/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.android.launcher3.lq.FindDefaultResoures;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.LqtThemeParserAdapter;
import com.lqsoft.lqtheme.OLThemeNotification;
import com.mediatek.launcher3.ext.LauncherLog;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class LauncherApplication extends Application {
    private static final String TAG = "LauncherApplication";

    /// M: flag for starting Launcher from application
    private boolean mTotallyStart = false;

    /// M: added for unread feature.
    private MTKUnreadLoader mUnreadLoader;
    /// M: flag for multi window support    
    //public static final boolean FLOAT_WINDOW_SUPPORT = FeatureOption.MTK_MULTI_WINDOW_SUPPORT;
    @Override
    public void onCreate() {
        super.onCreate();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "LauncherApplication onCreate");
        }

        LauncherAppState.setApplicationContext(this);
        initImageLoader(this);
        LauncherAppState.getInstance().setLauncehrApplication(this);
        
        /**M: register unread broadcast.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            mUnreadLoader = new MTKUnreadLoader(getApplicationContext());
            // Register unread change broadcast.
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_UNREAD_CHANGED);
            registerReceiver(mUnreadLoader, filter);
        }
        /**@}**/


        //begin add by ouyangjin for lqtheme
		if (LqShredPreferences.isLqtheme(this)) {
        LqService.getInstance().initSync(this);
        LqShredPreferences.init(this);
        Log.i("oyj","Oncreat LqService.getInstance() ");
        	String lqThmePath =LqShredPreferences.getLqThemePath();
        	if(!lqThmePath.equals("")){
        		try {
            		boolean result = LqService.getInstance().notifyLqThemeChanged(lqThmePath);
            		 LqService.getInstance().applyWallpaper(true);
           		 Log.i("oyj","Oncreat notifyLqThemeChanged : "+result);
				} catch (Exception e) {
					e.printStackTrace();
					}
				}
        	}
    }
    private void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

	}
    @Override
    public void onTerminate() {
        super.onTerminate();
        /**M: added for unread feature, unregister unread receiver.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            unregisterReceiver(mUnreadLoader);
        }
        /**@}**/
        LauncherAppState.getInstance().onTerminate();
    }

    /// M: LauncherApplication start flag @{
    public void setTotalStartFlag() {
        mTotallyStart = true;
    }

    public void resetTotalStartFlag() {
        mTotallyStart = false;
    }

    public boolean isTotalStart() {
        return mTotallyStart;
    }
    /// M: }@
    /**M: Added for unread message feature.@{**/
    /**
     * M: Get unread loader, added for unread feature.
     */
    public MTKUnreadLoader getUnreadLoader() {
        return mUnreadLoader;
    }
    /**@}**/
    
}