/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.systemui.statusbar.policy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.policy.NetworkController.EmergencyListener;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;

import java.util.ArrayList;
import java.util.List;
/*PRIZE-import package-liufan-2016-04-20-start*/
import android.app.StatusBarManager;
import com.mediatek.common.prizeoption.PrizeOption;
import android.util.Log;
import com.android.systemui.statusbar.SignalClusterView;
/*PRIZE-import package-liufan-2016-04-20-end*/

/**
 * Implements network listeners and forwards the calls along onto other listeners but on
 * the current or specified Looper.
 */
public class CallbackHandler extends Handler implements EmergencyListener, SignalCallback {
    private static final int MSG_EMERGENCE_CHANGED           = 0;
    private static final int MSG_SUBS_CHANGED                = 1;
    private static final int MSG_NO_SIM_VISIBLE_CHANGED      = 2;
    private static final int MSG_ETHERNET_CHANGED            = 3;
    private static final int MSG_AIRPLANE_MODE_CHANGED       = 4;
    private static final int MSG_MOBILE_DATA_ENABLED_CHANGED = 5;
    private static final int MSG_ADD_REMOVE_EMERGENCY        = 6;
    private static final int MSG_ADD_REMOVE_SIGNAL           = 7;

    // All the callbacks.
    private final ArrayList<EmergencyListener> mEmergencyListeners = new ArrayList<>();
    private final ArrayList<SignalCallback> mSignalCallbacks = new ArrayList<>();

    public CallbackHandler() {
        super();
    }

    @VisibleForTesting
    CallbackHandler(Looper looper) {
        super(looper);
    }

    /*PRIZE-add for bugid:52545-liufan-2018-03-13-start*/
    private NetworkControllerImpl mNetworkControllerImpl;
    public void setNetworkControllerImpl(NetworkControllerImpl networkImpl){
        mNetworkControllerImpl = networkImpl;
    }
    /*PRIZE-add for bugid:52545-liufan-2018-03-13-end*/

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_EMERGENCE_CHANGED:
                for (EmergencyListener listener : mEmergencyListeners) {
                    listener.setEmergencyCallsOnly(msg.arg1 != 0);
                }
                break;
            case MSG_SUBS_CHANGED:
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    signalCluster.setSubs((List<SubscriptionInfo>) msg.obj);
                }
                break;
            case MSG_NO_SIM_VISIBLE_CHANGED:
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    signalCluster.setNoSims(msg.arg1 != 0);
                }
                break;
            case MSG_ETHERNET_CHANGED:
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    signalCluster.setEthernetIndicators((IconState) msg.obj);
                }
                break;
            case MSG_AIRPLANE_MODE_CHANGED:
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    signalCluster.setIsAirplaneMode((IconState) msg.obj);
                }
                break;
            case MSG_MOBILE_DATA_ENABLED_CHANGED:
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    signalCluster.setMobileDataEnabled(msg.arg1 != 0);
                }
                break;
            case MSG_ADD_REMOVE_EMERGENCY:
                if (msg.arg1 != 0) {
                    mEmergencyListeners.add((EmergencyListener) msg.obj);
                } else {
                    mEmergencyListeners.remove((EmergencyListener) msg.obj);
                }
                break;
            case MSG_ADD_REMOVE_SIGNAL:
                if (msg.arg1 != 0) {
                    mSignalCallbacks.add((SignalCallback) msg.obj);
                    /*PRIZE-add for bugid:52545-liufan-2018-03-13-start*/
                    if(msg.obj != null && msg.obj instanceof SignalClusterView){
                        SignalClusterView scv = (SignalClusterView)msg.obj;
                        int style = scv.getStatusBarStyle();
                        scv.onStatusBarStyleChanged(style);
                        if(mNetworkControllerImpl != null){
                            mNetworkControllerImpl.notifyAllListenersForInverse();
                        }
                    }
                    /*PRIZE-add for bugid:52545-liufan-2018-03-13-end*/
                } else {
                    mSignalCallbacks.remove((SignalCallback) msg.obj);
                }
                break;
        }
    }

	/*PRIZE-add statusIconGray-liufan-2016-04-20-start*/
    @Override
    public void setWifiIndicators(final boolean enabled, final IconState statusIcon, final IconState statusIconGray, 
            final IconState qsIcon,final int activityIcon, final boolean activityIn, final boolean activityOut,
            final String description) {
        post(new Runnable() {
            @Override
            public void run() {
                for (SignalCallback callback : mSignalCallbacks) {
                    callback.setWifiIndicators(enabled, statusIcon, statusIconGray, qsIcon, activityIcon, activityIn, activityOut,
                            description);
                }
            }
        });
    }

    @Override
    public void setWifiIndicatorsForInverse(final boolean enabled, final IconState statusIcon, final IconState statusIconGray,
            final IconState qsIcon,final int activityIcon, final boolean activityIn, final boolean activityOut,
            final String description) {
        //post(new Runnable() {
        //    @Override
        //    public void run() {
                for (SignalCallback callback : mSignalCallbacks) {
                    callback.setWifiIndicators(enabled, statusIcon, statusIconGray, qsIcon, activityIcon, activityIn, activityOut,
                            description);
                }
        //    }
        //});
    }
	/*PRIZE-add statusIconGray-liufan-2016-04-20-end*/
	
	
	/*PRIZE-notify all Callback-liufan-2016-04-20-start*/
	public void notifyAllSignalCallbacks(int style){
		for (SignalCallback callback : mSignalCallbacks) {
			if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !callback.shouldIgnoreStatusBarStyleChanged()){
				callback.onStatusBarStyleChanged(style);
			}
		}
	}
	
	public void notifyDataActivityChanged(final int style, final int direction, final SubscriptionInfo mSubscriptionInfo){
		/*PRIZE-add try catch-liufan-2016-06-15-start*/
        try {
		for (SignalCallback callback : mSignalCallbacks) {
			if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !callback.shouldIgnoreStatusBarStyleChanged()){
                if(callback != null && callback instanceof SignalClusterView){
                    SignalClusterView scv = (SignalClusterView)callback;
                    if(scv.isInverseFlag()){
                        int inout_icon =  TelephonyIcons.getDataActivityIcon(scv.getStatusBarStyle(), direction);
                        callback.setMobileActivityId(inout_icon,
                                mSubscriptionInfo.getSubscriptionId());
                        return ;
                    }
                }
	            int inout_icon =  TelephonyIcons.getDataActivityIcon(style, direction);
	            callback.setMobileActivityId(inout_icon,
	                    mSubscriptionInfo.getSubscriptionId());
			} else {
                callback.setMobileActivityId(TelephonyIcons.DATA_ACTIVITY[direction],
                        mSubscriptionInfo.getSubscriptionId());
            }
		}	
        } catch (Exception e){
            Log.d("CallbackHandler","notifyDataActivityChanged---->" + e);
            postDelayed(new Runnable(){
                @Override
                public void run() {
                    notifyDataActivityChangedAgain(style, direction, mSubscriptionInfo);
                }
            }, 200);
        }
		/*PRIZE-add try catch-liufan-2016-06-15-end*/
	}

    public void notifyDataActivityChangedAgain(int style, int direction, SubscriptionInfo mSubscriptionInfo){
        try {
            for (SignalCallback callback : mSignalCallbacks) {
                if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !callback.shouldIgnoreStatusBarStyleChanged()){
                    int inout_icon =  TelephonyIcons.getDataActivityIcon(style, direction);
                    callback.setMobileActivityId(inout_icon,
                            mSubscriptionInfo.getSubscriptionId());
                } else {
                    callback.setMobileActivityId(TelephonyIcons.DATA_ACTIVITY[direction],
                            mSubscriptionInfo.getSubscriptionId());
                }
            }
        } catch (Exception e){
            Log.d("CallbackHandler","notifyDataActivityChangedAgain---->" + e);
        }
    }
	/*PRIZE-notify all Callback-liufan-2016-04-20-end*/
	
	/*PRIZE-add statusIconGray-liufan-2016-04-20-start*/
    /// M: Modify to support [Network Type and volte on Statusbar], change the implement methods,
    /// add more parameter for network type and volte.
    @Override
    public void setMobileDataIndicators(final IconState statusIcon, final IconState statusIconGray, final IconState qsIcon,
            final int statusType, final int networkIcon, final int volteType,
            final int qsType,final boolean activityIn,
            final boolean activityOut, final String typeContentDescription,
            final String description, final boolean isWide, final int subId) {
        post(new Runnable() {
            @Override
            public void run() {
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    ///M: Support[Network Type and volte on StatusBar].
                    /// add more parameter networkIcon and volte.
                    signalCluster.setMobileDataIndicators(statusIcon, statusIconGray, qsIcon, statusType,
                            networkIcon, volteType, qsType, activityIn, activityOut,
                            typeContentDescription, description, isWide, subId);
                }
            }
        });
    }

    @Override
    public void setMobileDataIndicatorsForInverse(final IconState statusIcon, final IconState statusIconGray, final IconState qsIcon,
            final int statusType, final int networkIcon, final int volteType,
            final int qsType,final boolean activityIn,
            final boolean activityOut, final String typeContentDescription,
            final String description, final boolean isWide, final int subId) {
        //post(new Runnable() {
        //    @Override
        //    public void run() {
                for (SignalCallback signalCluster : mSignalCallbacks) {
                    ///M: Support[Network Type and volte on StatusBar].
                    /// add more parameter networkIcon and volte.
                    signalCluster.setMobileDataIndicators(statusIcon, statusIconGray, qsIcon, statusType,
                            networkIcon, volteType, qsType, activityIn, activityOut,
                            typeContentDescription, description, isWide, subId);
                }
        //    }
        //});
    }

    @Override
    public void setSubs(List<SubscriptionInfo> subs) {
        obtainMessage(MSG_SUBS_CHANGED, subs).sendToTarget();
    }

    @Override
    public void setNoSims(boolean show) {
        obtainMessage(MSG_NO_SIM_VISIBLE_CHANGED, show ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public void setMobileDataEnabled(boolean enabled) {
        obtainMessage(MSG_MOBILE_DATA_ENABLED_CHANGED, enabled ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public void setEmergencyCallsOnly(boolean emergencyOnly) {
        obtainMessage(MSG_EMERGENCE_CHANGED, emergencyOnly ? 1 : 0, 0).sendToTarget();
    }

    @Override
    public void setEthernetIndicators(IconState icon) {
        obtainMessage(MSG_ETHERNET_CHANGED, icon).sendToTarget();
    }

    @Override
    public void setIsAirplaneMode(IconState icon) {
        obtainMessage(MSG_AIRPLANE_MODE_CHANGED, icon).sendToTarget();
    }

    public void setListening(EmergencyListener listener, boolean listening) {
        obtainMessage(MSG_ADD_REMOVE_EMERGENCY, listening ? 1 : 0, 0, listener).sendToTarget();
    }

    public void setListening(SignalCallback listener, boolean listening) {
        obtainMessage(MSG_ADD_REMOVE_SIGNAL, listening ? 1 : 0, 0, listener).sendToTarget();
    }
	
	/*PRIZE-Override methos-liufan-2016-04-20-start*/
    @Override
    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon,
            boolean activityIn, boolean activityOut, String description) {
    }
	
    @Override
    public void setMobileActivityId(int mobileActivityId, int subId) {
    }
	
    @Override
    public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType,
                int networkIcon, int volteType, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId) {
    }
	
    @Override
    public void setVolteStatusIcon(final int iconId, final int grayIconId) {
    }
	
    @Override
    public void onStatusBarStyleChanged(int style) {
    }
	
    @Override
    public void setIgnoreStatusBarStyleChanged(boolean ignored) {
    }
	
    @Override
    public boolean shouldIgnoreStatusBarStyleChanged() {
		return true;
    }
	
    @Override
    public int getStatusBarStyle() {
		return StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    }
	/*PRIZE-Override methos-liufan-2016-04-20-end*/
}
