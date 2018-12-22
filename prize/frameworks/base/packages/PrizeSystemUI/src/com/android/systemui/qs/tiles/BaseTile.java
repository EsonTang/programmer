/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：自定义通知栏的快速设置，满足基本点击状态切换的功能
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
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.content.Intent;

/**
* 类描述：通知栏快速设置中，电量的Tile
* @author liufan
* @version V1.0
*/
public class BaseTile extends QSTile<QSTile.BooleanState> 
{
    private static final long RECENTLY_ON_DURATION_MILLIS = 3000;
    private int labelId;
    private int drawableOn;
    private int drawableOff;
    private String configName;

    private long mWasLastOn;
    
    public BaseTile(Host host,int labelId,int drawableOn,int drawableOff,OnTileClickListener onTileClickListener,String configName) {
        super(host);
        this.labelId = labelId;
        this.drawableOn = drawableOn;
        this.drawableOff = drawableOff;
        super.onTileClickListener = onTileClickListener;
        this.configName = configName;
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
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
        if(super.onTileClickListener!=null){
            super.onTileClickListener.onTileClick(newState,configName);
            Log.d("BaseTile","BaseTile--->"+configName);
        }
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
        state.label = mContext.getString(labelId);
		if(labelId == R.string.quick_settings_prizerings || labelId == R.string.quick_settings_no_prizerings){
			 state.label = mContext.getString(state.value ? R.string.quick_settings_no_prizerings : R.string.quick_settings_prizerings);
		}
        //state.iconId = state.value ? drawableOn : drawableOff;
		android.util.Log.d("liuji","state.value == "+state.value);
        state.icon = ResourceIcon.get(state.value ? drawableOn : drawableOff);
        state.contentDescription = state.label;
    }

    @Override
    protected String composeChangeAnnouncement() {
        return null;
    }

    private Runnable mRecentlyOnTimeout = new Runnable() {
        @Override
        public void run() {
            refreshState();
        }
    };
}
