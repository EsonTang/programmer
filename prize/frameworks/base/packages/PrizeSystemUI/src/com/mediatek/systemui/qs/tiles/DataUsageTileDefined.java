/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：DataUsageTile的复制类，修改ui图片
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.mediatek.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.telephony.SubscriptionManager;

import com.android.internal.telephony.PhoneConstants;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.internal.logging.MetricsProto.MetricsEvent;

/**
* 类描述：DataUsageTile的复制类，DataUsageTileDefined用UI给的图片
* @author liufan
* @version V1.0
*/
public class DataUsageTileDefined extends QSTile<QSTile.State> {
    private static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName(
            "com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));

    /**
     * Constructor.
     * @param host The QSTileHost.
     */
    public DataUsageTileDefined(Host host) {
        super(host);
    }

    @Override
    public State newTileState() {
        return new State();
    }
	
    public Intent getLongClickIntent(){
		return null;
	}
	
    public CharSequence getTileLabel(){
        return mContext.getString(R.string.quick_settings_cell_label);
	}
	
    @Override
    public int getMetricsCategory() {
		return MetricsEvent.QS_DATAUSAGEDETAIL;
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleClick() {
        final long subId = SubscriptionManager.getDefaultDataSubscriptionId();
        CELLULAR_SETTINGS.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        mHost.startActivityDismissingKeyguard(CELLULAR_SETTINGS);
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        //state.iconId = R.drawable.ic_qs_cell_on;
        state.icon = ResourceIcon.get(R.drawable.ic_qs_cell_on);
        state.label = mContext.getString(R.string.quick_settings_cell_label);
        state.contentDescription = mContext.getString(R.string.quick_settings_cell_label);
    }
}
