/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：DataConnectionTile的复制类，修改ui图片
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-13
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/

package com.mediatek.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.qs.QSTile;
import com.android.systemui.R;

import com.mediatek.systemui.statusbar.util.SIMHelper;
import android.util.Log;
/**PRIZE import package liyao 2015-05-27 start*/
import android.util.Log;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.os.AsyncTask;
import android.provider.Settings;
/**PRIZE import package liyao 2015-05-27 end*/
import android.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.content.DialogInterface;
import android.telephony.SubscriptionInfo;
import java.util.List;


/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-start*/
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.PowerManager;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-07-20-end*/

//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
import android.widget.Toast;
import com.mediatek.systemui.PluginManager;
//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end


/**
* Class Description：DataConnectionTile
* @author liufan
* @version V1.0
*/
public class DataConnectionTileDefined extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "DataConnectionTileDefined";
    private static final boolean DEBUG = true;
    private boolean mListening;
    private int mDataState = R.drawable.ic_qs_dataconnection_off;
    private boolean mAirPlaneMode = false;
    private IccCardConstants.State mSimState = IccCardConstants.State.UNKNOWN;
    private TelephonyManager mTelephonyManager;
    private boolean mDataEnable = false;
    private int mDefaultDataSim = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private int mSlotID = SIMHelper.INVALID_SLOT_ID;
    private int mCurrentRadioMode = 3;
    private int mSlotCount = 0;

    public static final int DATA_DISCONNECT = 0;
    public static final int DATA_CONNECT = 1;
    public static final int AIRPLANE_DATA_CONNECT = 2;
    public static final int DATA_CONNECT_DISABLE = 3;
    public static final int DATA_RADIO_OFF = 4;

    public static final int DEFAULT_DATA_SIM_UNSET = 0;
    public static final int DEFAULT_DATA_SIM_SET = 1;
    public static final int MODE_PHONE1_ONLY = 1;
    /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
    private SubscriptionManager mSubscriptionManager;
    private int oldSimNum = 0;
    private boolean isShowedDialog;
    /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
    private AlertDialog mAlert;

    public DataConnectionTileDefined(Host host) {
        super(host);
        if (DEBUG) {
            Log.d(TAG, "DataConnectionTileDefined");
        }
        mSlotCount = SIMHelper.getSlotCount();
        SIMHelper.updateSIMInfos(mContext);//2015-05-08 update sSimInfos
        if (DEBUG) {
            Log.d(TAG, "mSlotCount = " + mSlotCount);
        }
        oldSimNum = 0;
        isShowedDialog = false;
        /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
        mSubscriptionManager = SubscriptionManager.from(mContext);
        /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
        /**PRIZE-add new listener to listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-start*/
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mContext.registerReceiver(mAirPlaneModeReceiver, filter);
        /**PRIZE-add new listener to listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-end*/
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (DEBUG) Log.d( TAG, "DataConnectionTile setListening= " +  listening);
        if (mListening == listening) {
            return;
        }
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            /**PRIZE-cancel listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-start*/
            //filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            /**PRIZE-cancel listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-end*/
            filter.addAction(Intent.ACTION_MSIM_MODE_CHANGED);
            filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
            mContext.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Global.MOBILE_DATA)
                        , true, mMobileStateForSingleCardChangeObserver);

            /// M:Register for settings change.
            mContext.getContentResolver().registerContentObserver(
                        Settings.Global.getUriFor(
                        Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION),
                        false, mDefaultDataSIMObserver);

            /// M:Register for monitor radio state change
            mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.MSIM_MODE_SETTING)
                        , true, mSimRadioStateChangeObserver);
        } else {
            /// M: Unregister receiver for the QSTile @{
            mContext.unregisterReceiver(mReceiver);
            mContext.getContentResolver().unregisterContentObserver(
                        mMobileStateForSingleCardChangeObserver);
            mContext.getContentResolver().unregisterContentObserver(
                        mDefaultDataSIMObserver);
            mContext.getContentResolver().unregisterContentObserver(
                        mSimRadioStateChangeObserver);
            /// M: Unregister receiver for the QSTile @}
        }
    }

    @Override
    protected void handleDestroy() {
        /// M: It will do setListening(false) in the parent's handleDestroy()
        super.handleDestroy();
        if (DEBUG) Log.d(TAG, "handle destroy");
        /**PRIZE-unregister listener -liufan-2015-12-03-start*/
        mContext.unregisterReceiver(mAirPlaneModeReceiver);
        /**PRIZE-unregister listener -liufan-2015-12-03-end*/
    }
    
    @Override
    public int getMetricsCategory() {
		return MetricsEvent.QS_PANEL;
    }
	
    public Intent getLongClickIntent(){
		return null;
	}
	
    public CharSequence getTileLabel(){
        return mContext.getString(R.string.mobile);
	}
	
    @Override
    protected void handleClick() {
    
		Log.d("yh","yuhao DataConnectionTileDefined handleClick ");
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
			Log.d("yh","yuhao DataConnectionTileDefined handleClick enter SuperSaverMode ");
            return;
        }
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/
	
        getDefaultDataSlotID();
        /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
        final boolean isDataConnecting = isDataConnecting();
        /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/
        /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
        boolean hasReady = false;
        for (int slot = 0; slot < mSlotCount ; slot++) {
            if (hasReady(slot)) {
                if (DEBUG) {
                    Log.d(TAG, "hasService slot=" + slot);
                }
                hasReady = true;
                break;
            }
        }
        /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
        if (DEBUG) {
            Log.d(TAG, "handleClick sim state " + isDefaultSimSet() +
                    "data enable state=" + mTelephonyManager.getDataEnabled() +
                    " mCurrentRadioMode=" + mCurrentRadioMode);
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
            Log.d(TAG, "hasSimInsert():" + hasSimInsert()+" isDataConnecting:"+isDataConnecting+" hasReady:"+hasReady);
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/
        }
        /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
        /*if (!hasSimInsert()
            || mCurrentRadioMode == 0
            && isDefaultSimSet() != DEFAULT_DATA_SIM_UNSET
            || mAirPlaneMode
            || !isDefaultDataSimRadioOn()) {*/
        if( mAirPlaneMode || isAirplaneModeOn() || !hasSimInsert() || !hasReady) {
        /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/
            if (DEBUG) {
                Log.d(TAG, "handleClick mAirPlaneMode= " + mAirPlaneMode +
                " mSimState= " + mSimState +
                " mSlotID= " + mSlotID +
                " mDefaultDataSim= " + mDefaultDataSim);
            }
            if (!mAirPlaneMode) {
                mDataState = R.drawable.ic_qs_dataconnection_off;
            }
            refreshState();
            return;
        }

        isShowedDialog = false;
        if (mTelephonyManager == null)
            mTelephonyManager = TelephonyManager.from(mContext);
        int slotReady = 0;
        try {
            boolean[] hasSlotReady = new boolean[mSlotCount];
            for (int slot = 0; slot < mSlotCount ; slot++) {
                hasSlotReady[slot] = hasReady(slot);
                if (DEBUG) {
                    Log.d(TAG, "hasReady slot=" +slot+" hasSlotReady[slot] "+ hasSlotReady[slot]);
                }
                boolean isOn = isOpenSimCard(slot);
                Log.d(TAG, "SIMHelper.isRadioOn isOn= " + isOn);
                if(hasReady(slot) && isOn) slotReady++;
            }
            
            int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            boolean enabled = mTelephonyManager.getDataEnabled();
            if (DEBUG) Log.d( TAG, "handleClick data state= " + enabled );
                    Log.d(TAG, "aaaaaaaaaaaaaaaaaaa--->"+slotReady);
                    Log.d(TAG, "aaaaaaaaaaaaaaaaaaa--->"+mTelephonyManager.getSimCount());
            if(slotReady > 1){
                showSelectDialog(defaultDataSubId,enabled,slotReady);
                isShowedDialog = true;
                return;
            }else{
                Log.d(TAG, "bbbbbbbbbbbbbbbbbbbb--->");
                if(isShowedDialog&&oldSimNum<2){
                    Log.d("slotReady","sim card number is error--------->"+mTelephonyManager.getSimCount());
                    oldSimNum++;
                    return;
                }
                Log.d(TAG, "ccccccccccccccccccc--->");
                isShowedDialog = false;
                oldSimNum = 0;
                //SubscriptionInfo currentSir =  SIMHelper.getSubInfoBySlot(mContext,slot);
                //mSubscriptionManager.setDefaultDataSubId(currentSir.getSubscriptionId());
                //mSubscriptionManager.setDefaultDataSubId(defaultDataSubId);
                //mTelephonyManager.setDataEnabled(defaultDataSubId,!enabled);
                List<SubscriptionInfo> mSubInfoList = SIMHelper.getSIMInfoList(mContext);
                    Log.d(TAG, "mSubInfoList.size--->" + mSubInfoList.size());
                for (SubscriptionInfo subInfo : mSubInfoList) {
                    Log.d(TAG, "subInfo--->" + subInfo);
                    if (subInfo!=null) {
                        int subid = subInfo.getSimSlotIndex();
                        boolean isOn = isOpenSimCard(subid);
                        if(!isOn){
                            continue;
                        }
                        //mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(), false);
						mSubscriptionManager.setDefaultDataSubId(subInfo.getSubscriptionId());
						mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(),!enabled);
						break;
                    }
                }
				
                /**PRIZE-delay refresh-liufan-2015-12-08-start*/
                sendBroadcastToSettings();
                refreshDelayed();
                /**PRIZE-delay refresh-liufan-2015-12-08-end*/
            }

            /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
            /*if (DEBUG) Xlog.d( TAG, "handleClick defaultDataSubId= " + defaultDataSubId );
            boolean[] hasSlotReady = new boolean[mSlotCount];
            for (int slot = 0; slot < mSlotCount ; slot++) {
                hasSlotReady[slot] = hasReady(slot);
                if (DEBUG) {
                    Xlog.d(TAG, "hasReady slot=" +slot+" hasSlotReady[slot] "+ hasSlotReady[slot]);
                }
                if(hasReady(slot)) slotReady++;
            }
            if(oldSimNum == 0){
                oldSimNum = slotReady;
            }else{
                if(slotReady < oldSimNum){
                    Log.d("slotReady","sim card number is error--------->"+slotReady);
                    return;
                }
            }
            Log.d("slotReady","slotReady-----3---->"+slotReady);
            if(defaultDataSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID  ){
                int hasSlot = 0;
                for (int slot = 0; slot < mSlotCount ; slot++) {
                    if(slotReady>1) {
                        showSelectDialog(defaultDataSubId,enabled,slotReady);
                    } else if (hasSlotReady[slot]) {
                        if (DEBUG) {
                            Xlog.d(TAG, "setDefaultDataSubId slot=" + slot);
                        }
                        SubscriptionInfo currentSir =  SIMHelper.getSubInfoBySlot(mContext,slot);
                        mSubscriptionManager.setDefaultDataSubId(currentSir.getSubscriptionId());
                        mTelephonyManager.setDataEnabled(currentSir.getSubscriptionId(),true);
                        break;
                    }
                }
            }*/
            /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
            /*else if(!isDataConnecting ) {
                if(slotReady>1) {
                    showSelectDialog(defaultDataSubId,enabled,slotReady);
                } else {
                    for (int slot = 0; slot < mSlotCount ; slot++) {
                        if (hasSlotReady[slot]) {
                            if (DEBUG) {
                                Xlog.d(TAG, "setDefaultDataSubId slot=" + slot);
                            }
                            SubscriptionInfo currentSir =  SIMHelper.getSubInfoBySlot(mContext,slot);
                            mSubscriptionManager.setDefaultDataSubId(currentSir.getSubscriptionId());
                            mTelephonyManager.setDataEnabled(currentSir.getSubscriptionId(),!enabled);
                            break;
                       }
                   } 
                }
            }*/
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/
            /**PRIZE-cancel refresh immediately-liufan-2015-12-08-start*/
            /*if (!enabled) {
                mDataState = R.drawable.ic_qs_dataconnection_on;
            } else {
                mDataState = R.drawable.ic_qs_dataconnection_off;
            }*/
            /**PRIZE-cancel refresh immediately-liufan-2015-12-08-end*/
        } catch (NullPointerException e) {
            if (DEBUG) Log.d(TAG, "failed get  TelephonyManager exception" + e);
        }

        //if(slotReady<=1) refreshState();
        refreshState();
    }

    public boolean isOpenSimCard(int slot){
        int[] subArr = SubscriptionManager.getSubId(slot);
        boolean isOn = true;
        boolean flag = true;
        for(int j = 0; j < subArr.length; j++){
            int sub = subArr[j];
            boolean value = SIMHelper.isRadioOn(sub);
            if(!value){
                flag = false;
                break;
            }
        }
        if(!flag) isOn = false;
        Log.d(TAG, "isOpenSimCard isOn= " + isOn);
        return isOn;
    }
    
    /**PRIZE-delay refresh-liufan-2015-12-08-start*/
    private void refreshDelayed(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataConnectionState();
                refreshState();
            }
        }, 700);
    }
    private void sendBroadcastToSettings(){
        Intent intent = new Intent("com.prize.datachange.toSettings");
        mContext.sendBroadcast(intent);
    }
    /**PRIZE-delay refresh-liufan-2015-12-08-end*/

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mContext.getString(R.string.mobile);
        state.icon = ResourceIcon.get(mDataState);

		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
			Log.d("yh","yuhao  DataConnectionTileDefined handleUpdateState set state.icon = null ");
			state.icon = null;
        }
		/*PRIZE-PowerExtendMode-yuhao-2016-12-10-end*/
		
        if(mDataState == R.drawable.ic_qs_dataconnection_off){
            state.colorId = 0;
        }else{
            state.colorId = 1;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                if (DEBUG) Log.d(TAG, "onReceive ACTION_ANY_DATA_CONNECTION_STATE_CHANGED");
                getDataConnectionState();
            } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                if (DEBUG) Log.d(TAG, "onReceive ACTION_SIM_STATE_CHANGED");
                updateSimState(intent);
                //getDataConnectionState();
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mAirPlaneMode = intent.getBooleanExtra("state", false);
                if (DEBUG) Log.d(TAG, "onReceive ACTION_AIRPLANE_MODE_CHANGED mAirPlaneMode= " + mAirPlaneMode);
                /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 start*/
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Settings.Global.putInt(mContext.getContentResolver(),"airplane_mode_on" ,mAirPlaneMode ? 1 : 0);
                    }
                });
                /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 end*/
                getDataConnectionState();
            } else if (action.equals(Intent.ACTION_MSIM_MODE_CHANGED)) {
                mCurrentRadioMode = intent.getIntExtra(Intent.EXTRA_MSIM_MODE,
                    convertPhoneCountIntoRadioState(mSlotCount));
                getDataConnectionState();
                if (DEBUG) {
                    Log.d(TAG, "onReceive ACTION_MSIM_MODE_CHANGED mCurrentRadioMode" +
                        mCurrentRadioMode);
                }
            }
            refreshState();
        }
    };

    /**PRIZE-new listener to listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-start*/
    private final BroadcastReceiver mAirPlaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mAirPlaneMode = intent.getBooleanExtra("state", false);
                if (DEBUG) Log.d(TAG, "mAirPlaneModeReceiver onReceive ACTION_AIRPLANE_MODE_CHANGED mAirPlaneMode= " + mAirPlaneMode);
                /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 start*/
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Settings.Global.putInt(mContext.getContentResolver(),"airplane_mode_on" ,mAirPlaneMode ? 1 : 0);
                    }
                });
                /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 end*/
                getDataConnectionState();
                refreshState();
            }
        }
    };
    /**PRIZE-new listener to listen ACTION_AIRPLANE_MODE_CHANGED-liufan-2015-12-03-end*/

    public int getDataConnectionState() {
        if (DEBUG) Log.d( TAG, "getDataConnectionState mSimState= "+ mSimState);

        if (mTelephonyManager == null) {
                mTelephonyManager = TelephonyManager.from(mContext);
            }

        try {
            getDefaultDataSlotID();

            boolean dataEnable = mTelephonyManager.getDataEnabled();
            if (DEBUG) {
                Log.d(TAG, "getDataConnectionState dataEnable= " + dataEnable);
            }

            int dataResult = DATA_DISCONNECT;

            if (dataEnable == false /*|| mSimState == IccCardConstants.State.ABSENT*/) {
                dataResult = DATA_DISCONNECT;
            } else if (dataEnable && mAirPlaneMode) {
                dataResult = AIRPLANE_DATA_CONNECT;
            } else if (dataEnable && !mAirPlaneMode /*&& mSimState != IccCardConstants.State.ABSENT*/) {
                dataResult = DATA_CONNECT;
            }
            mCurrentRadioMode = Settings.System.getInt(mContext.getContentResolver(),
                          Settings.System.MSIM_MODE_SETTING,
                          convertPhoneCountIntoRadioState(mSlotCount));

            boolean radiomode = (mCurrentRadioMode & (mSlotID + 1)) == 0;
            Log.d(TAG, "getDataConnectionState DATA_RADIO_OFF mCurrentRadioMode= "
                    + mCurrentRadioMode + " mSlotID= " + mSlotID +
                    " (mCurrentRadioMode & (mSlotID + 1)) =" + radiomode);
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
            /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
            /**PRIZE sometimes don't change icon when click the dataconnection-liufan-2016-03-19-start*/
            if (mAirPlaneMode || isAirplaneModeOn()  || (dataEnable == true && (mCurrentRadioMode & (mSlotID + 1)) == 0) ||
                    (dataEnable == true ) && !SIMHelper.isSimInsertedBySlot(mContext, mSlotID)
                    /*isDefaultSimSet() != DEFAULT_DATA_SIM_UNSET */) {
                Log.d(TAG, "getDataConnectionState mCurrentRadioMode= " +
                        mCurrentRadioMode + " mSlotID= " + mSlotID +
                        " dataEnable = "+dataEnable +" error_state");
            /**PRIZE sometimes don't change icon when click the dataconnection-liufan-2016-03-19-end*/
            /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
            /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/
                dataResult = DATA_RADIO_OFF;
            }

            setDataConnectionUI(dataResult);
            if (DEBUG) {
                Log.d(TAG, "getDataConnectionState dataResult= " +
                        dataResult + " mSimState= " + mSimState);
            }
            return dataResult;
        } catch (NullPointerException e) {
            if (DEBUG) {
                Log.d(TAG, "failed get  TelephonyManager exception" + e);
            }
        }

        return DATA_DISCONNECT;
    }

    private void setDataConnectionUI(int dataState) {
        if (DEBUG) Log.d( TAG, "setDataConnectionUI = " + dataState);
        switch(dataState) {
            case DATA_DISCONNECT:
                mDataState = R.drawable.ic_qs_dataconnection_off;
                break;
            case DATA_RADIO_OFF:
            case DATA_CONNECT_DISABLE:
            case AIRPLANE_DATA_CONNECT:
                mDataState = R.drawable.ic_qs_dataconnection_off;
                break;
            case DATA_CONNECT:
                mDataState = R.drawable.ic_qs_dataconnection_on;
                break;
            default :
                mDataState = R.drawable.ic_qs_dataconnection_off;
                break;
        }
    }

    private final void updateSimState(Intent intent) {
        getDefaultDataSlotID();

        int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
                SIMHelper.INVALID_SLOT_ID);
        if (DEBUG) {
            Log.d(TAG, "updateSimState default data mSlotID= " + mSlotID + " slotId= " + slotId);
        }
        if (mSlotID == slotId) {
            String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
                mSimState = IccCardConstants.State.ABSENT;
            } else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
                mSimState = IccCardConstants.State.READY;
            } else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
                final String lockedReason =
                        intent.getStringExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON);
                if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                    mSimState = IccCardConstants.State.PIN_REQUIRED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                    mSimState = IccCardConstants.State.PUK_REQUIRED;
                } else {
                    mSimState = IccCardConstants.State.NETWORK_LOCKED;
                }
            } else {
                mSimState = IccCardConstants.State.UNKNOWN;
            }
        }
        if (DEBUG) Log.d(TAG, "updateSimState mSimState= " + mSimState);
    }

    private ContentObserver mMobileStateForSingleCardChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (!SIMHelper.isWifiOnlyDevice()) {
                final boolean dataEnable = Settings.Global.getInt(mContext.getContentResolver(),
                        Settings.Global.MOBILE_DATA, 1) == 1;
                if (DEBUG) {
                    Log.d(TAG, "onChange dataEnable= " + dataEnable);
                }
                getDataConnectionState();
                refreshState();
            }
        }
    };

    private ContentObserver mDefaultDataSIMObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mDefaultDataSim = SubscriptionManager.getDefaultDataSubscriptionId();
            if (mDefaultDataSim != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                mSlotID = SubscriptionManager.getSlotId(mDefaultDataSim);
            } else {
                mSlotID = SIMHelper.INVALID_SLOT_ID;
            }
            if (DEBUG) {
                Log.d(TAG, "mDefaultDataSIMObserver mDefaultDataSim= " + mDefaultDataSim +
                    " mSlotID=" + mSlotID);
            }
        }
    };

    private ContentObserver mSimRadioStateChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mCurrentRadioMode = Settings.System.getInt(mContext.getContentResolver(),
                          Settings.System.MSIM_MODE_SETTING,
                          convertPhoneCountIntoRadioState(mSlotCount));

            if (DEBUG) {
                Log.d(TAG, "mSimRadioStateChangeObserver mCurrentRadioMode= "
                    + mCurrentRadioMode);
            }
        }
    };

    private void getDefaultDataSlotID() {
        try {
            mDefaultDataSim = SubscriptionManager.getDefaultDataSubscriptionId();
            if (mDefaultDataSim != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                mSlotID = SubscriptionManager.getSlotId(mDefaultDataSim);
            } else {
                mSlotID = SIMHelper.INVALID_SLOT_ID;
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                Log.d(TAG, "failed get  SubscriptionManager exception" + e);
            }
        }
    }

    private boolean hasSimInsert() {
        for (int slot = 0; slot < mSlotCount ; slot++) {
            if (SIMHelper.isSimInsertedBySlot(mContext, slot)) {
                if (DEBUG) {
                    Log.d(TAG, "hasSimInsert slot=" + slot);
                }
                return true;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "No Sim Insert");
        }
        return false;
    }

    private int isDefaultSimSet() {
        if (hasSimInsert() && (mDefaultDataSim == SubscriptionManager.INVALID_SUBSCRIPTION_ID)) {
            if (DEBUG) {
                Log.d(TAG, "DefaultSim is unset ");
            }
            return DEFAULT_DATA_SIM_UNSET;
        } else if (hasSimInsert() && (mDefaultDataSim != SubscriptionManager.INVALID_SUBSCRIPTION_ID)) {
            if (DEBUG) {
                    Log.d(TAG, "DefaultSim is set ");
            }
            return DEFAULT_DATA_SIM_SET;
        } else {
            return -1;
        }
    }

    private boolean isDefaultDataSimRadioOn() {
        if (isDefaultSimSet() == DEFAULT_DATA_SIM_SET) {
            if ((mCurrentRadioMode & (mSlotID + 1)) == 0) {
                if (DEBUG) {
                        Log.d(TAG, "DefaultDataSimRadio is Off mSlotID= " + mSlotID);
                }
                return false;
            } else {
                if (DEBUG) {
                    Log.d(TAG, "DefaultDataSimRadio is on mSlotID= " + mSlotID);
                }
                return true;
            }
        } else if (isDefaultSimSet() == DEFAULT_DATA_SIM_UNSET) {
            if (DEBUG) {
                Log.d(TAG, "isDefaultSimSet() == DEFAULT_DATA_SIM_UNSET");
            }
            return true;
        } else {
            return false;
        }

    }
    private int convertPhoneCountIntoRadioState(int phoneCount) {
        int ret = 0;
        for (int i = 0; i < phoneCount; i++) {
            ret += MODE_PHONE1_ONLY << i;
        }
        Log.d(TAG, "Convert phoneCount " + phoneCount + " into RadioState " + ret);
        return ret;
    }
    /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 start*/
    private boolean isDataConnecting() {
        for (int i = 0; i < mSlotCount ; i++) {
            if (mTelephonyManager != null
                    && mTelephonyManager.getDataState(i) == TelephonyManager.DATA_CONNECTING) {
                if (DEBUG) Log.d( TAG, "isDataConnecting true");
                return true;
            }
        }
        if (DEBUG) Log.d( TAG, "isDataConnecting false");
        return false;
    }
    /**PRIZE there is no response when click this tile(bug 399) liyao 2015-05-27 end*/

    /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 start*/
    private boolean isAirplaneModeOn() {
        boolean airplaneMode = Settings.Global.getInt(mContext.getContentResolver(),"airplane_mode_on" ,0) == 1;
        if (DEBUG) Log.d( TAG, "isAirplaneModeOn() airplaneMode:"+airplaneMode);
        return airplaneMode;
    }
    /**PRIZE DataConnection State change to Power on when set Airplanemode and reboot(bug 1452) liyao 2015-05-27 end*/

    /* soltId = subId -1 */
    /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 start*/
    public boolean hasReady(final int soltId) {
        boolean slotReady = Settings.Global.getInt(mContext.getContentResolver(),soltId+"_slot_ready" ,0) ==1;
        if (DEBUG) Log.d( TAG, "hasReady() soltId:"+soltId+" slotReady: "+slotReady);
        return slotReady;
    }
    /**PRIZE refresh DataConnection State when there is no SIM card or have two SIM cards(bug 1468) liyao 2015-05-28 end*/
    
	//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
    private boolean isLimitUnCMCC = false;
    private boolean isContainCMCC = false;
    private boolean isContainUnCMCC = false;
	//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end
	
    /*
    * PRIZE show Dialog to select DataConnection SIM card
    * liyao 20150731
    */
    private void showSelectDialog(final int defaultDataSubId,final boolean enabled,final int slotsReady){
		//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
		if(!PrizeOption.PRIZE_CMCC_SWITCH){//bugid 57339 modif by taoyingyou-20180514
			
		}else{
			
        if(isShowedDialog) return;
        isShowedDialog = true;
        isLimitUnCMCC = false;
        isContainCMCC = false;
        isContainUnCMCC = false;
		
		}
		//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end
        SubscriptionInfo currentSir ;
        for(int i=0; i<slotsReady;i++ ){
            currentSir =  SIMHelper.getSubInfoBySlot(mContext,i);
            if(currentSir!=null){
               Log.d(TAG,i+"<=i  currentSir.getSubscriptionId "+currentSir.getSubscriptionId());
            }
        }

        String slot = mContext.getString(com.android.keyguard.R.string.slot);
        final String[] items = new String[slotsReady+1];
        items[slotsReady] = mContext.getString(R.string.close_data_connection);
        int select = slotsReady;
        for(int i=0; i<slotsReady;i++ ){
			//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
			if(!PrizeOption.PRIZE_CMCC_SWITCH){
				
			  if(SIMHelper.getSubInfoBySlot(mContext,i)!=null){
                items[i] = slot +(i+1)+":"+SIMHelper.getSubInfoBySlot(mContext,i).getDisplayName().toString();
                if(defaultDataSubId == SIMHelper.getSubInfoBySlot(mContext,i).getSubscriptionId()){
                    select = i;
                }
              }
			  
            }else{
				
            SubscriptionInfo si = SIMHelper.getSubInfoBySlot(mContext,i);
            if(si!=null){
                String subscriberId = mTelephonyManager.getSubscriberId(si.getSubscriptionId());
                if(isCMCCSimCard(subscriberId)){
                    isContainCMCC = true;
                }else{
                    isContainUnCMCC = true;
                }
                items[i] = slot +(i+1)+":"+si.getDisplayName().toString();
                if(defaultDataSubId == si.getSubscriptionId()){
                    select = i;
                }
            }
			}
        }
		
		if(!PrizeOption.PRIZE_CMCC_SWITCH){
			
		}else{
			
        if(isContainCMCC && isContainUnCMCC){
            isLimitUnCMCC = true;
        }
		
		}
		//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end
        if(!mTelephonyManager.getDataEnabled()){
            select = slotsReady;
        }
        mAlert = new AlertDialog.Builder(mContext).setTitle(R.string.select_data_connection)
            .setSingleChoiceItems(items, select,new DialogInterface.OnClickListener() { 
                public void onClick(DialogInterface dialog, int item) { 
                    if (DEBUG) Log.d( TAG, "showSelectDialog soltId:"+item);
                    if(item == slotsReady){
                        mTelephonyManager.setDataEnabled(defaultDataSubId,false);
                    } else {
                        SubscriptionInfo currentSir2 =  SIMHelper.getSubInfoBySlot(mContext,item);
						//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
						if(!PrizeOption.PRIZE_CMCC_SWITCH){
							
						}else{
							
                        if(isLimitUnCMCC){
                            String subscriberId = mTelephonyManager.getSubscriberId(currentSir2.getSubscriptionId());
                            if(!isCMCCSimCard(subscriberId)){
                                String msg = mContext.getString(R.string.only_cmcc_data_connection_prize);
                                showToast(msg);
                                mAlert.dismiss();
                                return ;
                            }
                        }
						
						}
						//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end
                        mSubscriptionManager.setDefaultDataSubId(currentSir2.getSubscriptionId());
                        mTelephonyManager.setDataEnabled(currentSir2.getSubscriptionId(),true);
                        for(int i=0; i<slotsReady;i++ ){
                            if(i != item && SIMHelper.getSubInfoBySlot(mContext,i) != null){
                                mTelephonyManager.setDataEnabled(SIMHelper.getSubInfoBySlot(mContext,i).getSubscriptionId(),false);
                            }
                        }
                    }
                    /**PRIZE-delay refresh-liufan-2015-12-08-start*/
                    sendBroadcastToSettings();
                    refreshDelayed();
                    /**PRIZE-delay refresh-liufan-2015-12-08-end*/
                    mAlert.dismiss();
                } 
            }).create();
        Window window = mAlert.getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        window.setFormat(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW);
		//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
		if(!PrizeOption.PRIZE_CMCC_SWITCH){
			
		}else{
        mAlert.setOnDismissListener(new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                isShowedDialog = false;
            }
        });
		}
		//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end
        if(!mAlert.isShowing()) mAlert.show();
    }
    
	//prize add by xiaoyunhui 2018-04-09 lock cmcc sim start
    public boolean isCMCCSimCard(String subscriberId){
        if(subscriberId.startsWith("46000") || subscriberId.startsWith("46002")
            || subscriberId.startsWith("46004") || subscriberId.startsWith("46007") || subscriberId.startsWith("46008")){
            return true;
        } else {
            return false;
        }
    }
    
    public void showToast(String msg){
        Toast toast = Toast.makeText(mContext,msg,Toast.LENGTH_LONG);
        WindowManager.LayoutParams params = toast.getWindowParams();
        params.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
        toast.show();
    }
	//prize add by xiaoyunhui 2018-04-09 lock cmcc sim end

}
