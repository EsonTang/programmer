/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：通知栏快速设置中，亮度的Tile
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.os.SystemClock;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import android.util.Log;
import com.android.systemui.statusbar.policy.BluetoothController;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.provider.Settings;
import android.net.Uri;
import android.os.UserHandle;
import android.os.Handler;
import android.content.ContentResolver;
import android.content.Intent;
import com.android.internal.logging.MetricsProto.MetricsEvent;


/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-start*/
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.PowerManager;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-end*/

/**
* 类描述：通知栏快速设置中，亮度的Tile
* @author liufan
* @version V1.0
*/
public class BrightnessTileDefined extends QSTile<QSTile.BooleanState> 
{
    private static final long RECENTLY_ON_DURATION_MILLIS = 3000;
    private int labelId;
    private int drawableOn;
    private int drawableOff;
    private String configName;
    private BrightnessObserver mBrightnessObserver;

    private long mWasLastOn;
    
    public BrightnessTileDefined(Host host,int labelId,int drawableOn,int drawableOff,OnTileClickListener onTileClickListener,String configName) {
        super(host);
        this.labelId = labelId;
        this.drawableOn = drawableOn;
        this.drawableOff = drawableOff;
        super.onTileClickListener = onTileClickListener;
        this.configName = configName;
        mBrightnessObserver = new BrightnessObserver(mHandler);
        mBrightnessObserver.startObserving();
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mBrightnessObserver.stopObserving();
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }
	
    @Override
    public int getMetricsCategory() {
		return MetricsEvent.QS_PANEL;
    }
	
    public Intent getLongClickIntent(){
		return null;
	}
	
    public CharSequence getTileLabel(){
        return mContext.getString(labelId);
	}

    @Override
    protected void handleClick() {

		Log.d("yh","yuhao BrightnessTileDefined handleClick ");
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
			Log.d("yh","yuhao BrightnessTileDefined handleClick enter SuperSaverMode ");
            return;
        }
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/

        if (ActivityManager.isUserAMonkey()) {
            return;
        }

        boolean newState = getBrightnessMode();
        if(super.onTileClickListener!=null){
            super.onTileClickListener.onTileClick(newState,configName);
            Log.d("BrightnessTileDefined","BrightnessTileDefined--->"+configName);
        }
        toggleAutoMode(newState);
        newState = !newState;
        refreshState(newState);
    }
    
    /**
    * 切换亮度模式
    */
    private void toggleAutoMode(final boolean isAuto){ 
       AsyncTask.execute(new Runnable() {
            public void run() {
               Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    isAuto ? Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL : Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC ,
                    UserHandle.USER_CURRENT                                          
                );
            }         
         });
    }
    
    /**
    * 得到当前亮度是否为自动模式
    */
    private boolean getBrightnessMode(){
          int automatic = Settings.System.getIntForUser(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE, 
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                        UserHandle.USER_CURRENT);

          boolean isAutoMode = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
          return  isAutoMode;

     }

    
    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.value) {
            mWasLastOn = SystemClock.uptimeMillis();
        }

        if (arg instanceof Boolean) {
            state.value = (Boolean) arg;
        }

        if (!state.value && mWasLastOn != 0) {
            if (SystemClock.uptimeMillis() > mWasLastOn + RECENTLY_ON_DURATION_MILLIS) {
                mWasLastOn = 0;
            } else {
                mHandler.removeCallbacks(mRecentlyOnTimeout);
                mHandler.postAtTime(mRecentlyOnTimeout, mWasLastOn + RECENTLY_ON_DURATION_MILLIS);
            }
        }
        state.value = getBrightnessMode();

        // Always show the tile when the flashlight is or was recently on. This is needed because
        // the camera is not available while it is being used for the flashlight.
        //state.visible = mWasLastOn != 0 || mFlashlightController.isAvailable();
        state.label = mContext.getString(labelId);
        //state.iconId = state.value ? drawableOn : drawableOff;
        state.icon = ResourceIcon.get(state.value ? drawableOn : drawableOff);		

		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
//        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
//			Log.d("yh","yuhao  BrightnessTileDefined handleUpdateState set state.icon = null ");
//			state.icon = null;
//        }
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/
		
        state.colorId = state.value ? 1 : 0;
        state.contentDescription = state.label;
    }

    @Override
    protected String composeChangeAnnouncement() {
        //if (mState.value) {
        //    return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        //} else {
        //    return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        //}
        return null;
    }

    private Runnable mRecentlyOnTimeout = new Runnable() {
        @Override
        public void run() {
            refreshState();
        }
    };
    
    private class BrightnessObserver extends ContentObserver {

        private final Uri BRIGHTNESS_MODE_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
        private final Uri BRIGHTNESS_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        private final Uri BRIGHTNESS_ADJ_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ);
        ///prize-wuliang 20180227 auto brightness
        private final Uri AUTO_MODE_BRIGHTNESS_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_AUTO_MODE_BRIGHTNESS);

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;              
            refreshState();
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    BRIGHTNESS_MODE_URI,
                    false, this, UserHandle.USER_ALL);
            cr.registerContentObserver(
                    BRIGHTNESS_URI,
                    false, this, UserHandle.USER_ALL);
            cr.registerContentObserver(
                    BRIGHTNESS_ADJ_URI,
                    false, this, UserHandle.USER_ALL);
            ///prize-wuliang 20180227 auto brightness
            cr.registerContentObserver(
                    AUTO_MODE_BRIGHTNESS_URI,
                    false, this, UserHandle.USER_ALL);

        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
}
