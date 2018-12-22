/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：通知栏快速设置中，休眠的Tile
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
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.provider.Settings;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.UserHandle;
import android.os.Handler;
import android.content.ContentResolver;
import android.content.Intent;
import com.android.internal.logging.MetricsProto.MetricsEvent;

/**
* Class Description：DormancyTileDefined
* @author liufan
* @version V1.0
*/
public class DormancyTileDefined extends QSTile<QSTile.BooleanState>
{
    private static final long RECENTLY_ON_DURATION_MILLIS = 3000;
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    
    private int labelId;
    private int drawableOn;
    private int drawableOff;
    private String configName;
    private DormancyTimeObserver mDormancyTimeObserver;
    /**
    * Dormancy array，as the same as the screen_timeout_values in PrizeSettings
    * path：Settings/res/values/arrays.xml
    */
    private final int[] mDormancyTime = new int[]{15000,30000,60000,120000,300000,600000,1800000};
    private final int[] mDormancyResIdArr = new int[]{R.drawable.dormancy_15s,R.drawable.dormancy_30s,R.drawable.dormancy_60s,R.drawable.dormancy_120s,R.drawable.dormancy_300s,R.drawable.dormancy_600s,R.drawable.dormancy_1800s};

    //private final FlashlightController mFlashlightController;
    private long mWasLastOn;
    
    public DormancyTileDefined(Host host,int labelId,int drawableOn,int drawableOff,OnTileClickListener onTileClickListener,String configName) {
        super(host);
        this.labelId = labelId;
        this.drawableOn = drawableOn;
        this.drawableOff = drawableOff;
        super.onTileClickListener = onTileClickListener;
        this.configName = configName;
        mDormancyTimeObserver = new DormancyTimeObserver(mHandler);
        mDormancyTimeObserver.startObserving();
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mDormancyTimeObserver.stopObserving();
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
        if (ActivityManager.isUserAMonkey()) {
            return;
        }
        boolean newState = !mState.value;
        //mFlashlightController.setFlashlight(newState);
        if(super.onTileClickListener!=null){
            super.onTileClickListener.onTileClick(newState,configName);
            Log.d("DormancyTileDefined","DormancyTileDefined--->"+configName);
        }
        int currentValue = Settings.System.getInt(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        int toValue = mDormancyTime[0];
        if (currentValue == mDormancyTime[0]) {
            toValue = mDormancyTime[1];
        } else if (currentValue == mDormancyTime[1]) {
            toValue = mDormancyTime[2];
        }
        /*PRIZE 15s->30s->60s->15s, sync with PrizeSettings liyao 2015-7-15 start */
        /* else if (currentValue == mDormancyTime[2]) {
            toValue = mDormancyTime[3];
        } else if (currentValue == mDormancyTime[3]) {
            toValue = mDormancyTime[4];
        } else if (currentValue == mDormancyTime[4]) {
            toValue = mDormancyTime[5];
        } else if (currentValue == mDormancyTime[5]) {
            toValue = mDormancyTime[6];
        } else if (currentValue == mDormancyTime[6]) {
            toValue = mDormancyTime[0];
        }*/
        else{
            toValue = mDormancyTime[0];
        }
        /*PRIZE 15s->30s->60s->15s, sync with PrizeSettings liyao 2015-7-15 end */
        Settings.System.putInt(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT, toValue);
        refreshState(newState);
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

        // Always show the tile when the flashlight is or was recently on. This is needed because
        // the camera is not available while it is being used for the flashlight.
        //state.visible = mWasLastOn != 0 || mFlashlightController.isAvailable();
        state.label = mContext.getString(labelId);;
        //state.iconId = drawableOn;
        state.icon = ResourceIcon.get(R.drawable.dormancy_never);
        int currentValue = Settings.System.getInt(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        if (currentValue == mDormancyTime[0]) {
            //state.iconId = mDormancyResIdArr[0];
            state.icon = ResourceIcon.get(mDormancyResIdArr[0]);
        } else if (currentValue == mDormancyTime[1]) {
            //state.iconId = mDormancyResIdArr[1];
            state.icon = ResourceIcon.get(mDormancyResIdArr[1]);
        } else if (currentValue == mDormancyTime[2]) {
            //state.iconId = mDormancyResIdArr[2];
            state.icon = ResourceIcon.get(mDormancyResIdArr[2]);
        } else if (currentValue == mDormancyTime[3]) {
            //state.iconId = mDormancyResIdArr[3];
            state.icon = ResourceIcon.get(mDormancyResIdArr[3]);
        } else if (currentValue == mDormancyTime[4]) {
            //state.iconId = mDormancyResIdArr[4];
            state.icon = ResourceIcon.get(mDormancyResIdArr[4]);
        } else if (currentValue == mDormancyTime[5]) {
            //state.iconId = mDormancyResIdArr[5];
            state.icon = ResourceIcon.get(mDormancyResIdArr[5]);
        } else if (currentValue == mDormancyTime[6]) {
            //state.iconId = mDormancyResIdArr[6];
            state.icon = ResourceIcon.get(mDormancyResIdArr[6]);
        }
        //state.iconId = state.value ? drawableOn : drawableOff;
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
    
    private class DormancyTimeObserver extends ContentObserver {

        private final Uri DORMANCY_TIME_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);

        public DormancyTimeObserver(Handler handler) {
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
                    DORMANCY_TIME_URI,
                    false, this, UserHandle.USER_ALL);

        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
}
