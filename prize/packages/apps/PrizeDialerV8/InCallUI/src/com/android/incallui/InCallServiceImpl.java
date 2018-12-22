/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.ext.ExtensionManager;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
/*prize-add for Game-Modle -hpf-2018-3-16-start*/
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import com.android.incallui.InCallActivity;
import com.prize.incallui.PrizeCallGameModleManager;
/*prize-add for Game-Modle -hpf-2018-3-16-end*/

/**
 * Used to receive updates about calls from the Telecom component.  This service is bound to
 * Telecom while there exist calls which potentially require UI. This includes ringing (incoming),
 * dialing (outgoing), and active calls. When the last call is disconnected, Telecom will unbind to
 * the service triggering InCallActivity (via CallList) to finish soon after.
 */
public class InCallServiceImpl extends InCallService {

    @Override
    public void onCallAudioStateChanged(CallAudioState audioState) {
        AudioModeProvider.getInstance().onAudioStateChanged(audioState.isMuted(),
                audioState.getRoute(), audioState.getSupportedRouteMask());
    }

    @Override
    public void onBringToForeground(boolean showDialpad) {
        InCallPresenter.getInstance().onBringToForeground(showDialpad);
    }

    @Override
    public void onCallAdded(Call call) {
        /**
         * M: When in upgrade progress or in requesting for VILTE call,
         * It should reject the incoming call and disconnect other calls,
         * except the emergency call.
         * @{
         */
        if ((CallList.getInstance().getVideoUpgradeRequestCall() != null ||
                CallList.getInstance().getSendingVideoUpgradeRequestCall() != null)
                && !isEmergency(call)) {
            if (call.getState() == Call.STATE_RINGING) {
                call.reject(false, null);
            } else {
                call.disconnect();
            }
            Log.d(this, "[Debug][CC][InCallUI][OP][Hangup][null][null]" +
            "auto disconnect call while upgrading to video");
            InCallUtils.showOutgoingFailMsg(getApplicationContext(), call);
        } else {
            InCallPresenter.getInstance().onCallAdded(call);
        }
        /** @} */
    }

    /// M: Add phone record interface @{
    @Override
    public void onUpdateRecordState(final int state, final int customValue) {
        CallList.getInstance().onUpdateRecordState(state, customValue);
    }

    @Override
    public void onStorageFull() {
        CallList.getInstance().onStorageFull();
    }
    /// @}

    @Override
    public void onCallRemoved(Call call) {
        InCallPresenter.getInstance().onCallRemoved(call);
    }

    @Override
    public void onCanAddCallChanged(boolean canAddCall) {
        InCallPresenter.getInstance().onCanAddCallChanged(canAddCall);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this, "onBind");
        final Context context = getApplicationContext();
        /// M: [plugin]ensure a context is valid.
        ExtensionManager.registerApplicationContext(context);
        final ContactInfoCache contactInfoCache = ContactInfoCache.getInstance(context);
        InCallPresenter.getInstance().setUp(
                getApplicationContext(),
                CallList.getInstance(),
                AudioModeProvider.getInstance(),
                new StatusBarNotifier(InCallServiceImpl.this/*context*/, contactInfoCache),//prize-change for Game-Modle -hpf-2018-3-16
                contactInfoCache,
                new ProximitySensor(
                        context,
                        AudioModeProvider.getInstance(),
                        new AccelerometerListener(context))
                );
        InCallPresenter.getInstance().onServiceBind();
        InCallPresenter.getInstance().maybeStartRevealAnimation(intent);
        TelecomAdapter.getInstance().setInCallService(this);
        /*prize-add for Game-Modle -hpf-2018-3-16-start*/
        mPrizeScreenOffBroadcastReceiver = new PrizeScreenOffBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(999);
	    filter.addAction("android.intent.action.SCREEN_OFF");
	    Log.i(TAG, "[init] registerReceiver...   mPrizeScreenOffBroadcastReceiver = "+mPrizeScreenOffBroadcastReceiver);
	    InCallServiceImpl.this.registerReceiver(mPrizeScreenOffBroadcastReceiver,filter);
	    /*prize-add for Game-Modle -hpf-2018-3-16-end*/
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        /// M: optimize process. @{
        /*
         * Google code:
        super.onUnbind(intent);

        InCallPresenter.getInstance().onServiceUnbind();
        tearDown();

        return false;
         */
        Log.d(this, "onUnbind");
        InCallPresenter.getInstance().onServiceUnbind();
        tearDown();

        return super.onUnbind(intent);
        /// @}
    }

    private void tearDown() {
        Log.v(this, "tearDown");
        // Tear down the InCall system
        TelecomAdapter.getInstance().clearInCallService();
        InCallPresenter.getInstance().tearDown();
    }

    /// M: fix CR:ALPS02696713,can not dial ECC when requesting for vilte call. @{
    private boolean isEmergency(Call call) {
        Uri handle = call.getDetails().getHandle();
        return PhoneNumberUtils.isEmergencyNumber(
                handle == null ? "" : handle.getSchemeSpecificPart());
    }
    /// @}
    
    /*prize-add for Game-Modle -hpf-2018-3-16-start*/
    @Override
    public void onDestroy(){
    	Log.i(TAG, "[onDestory]");
    	if(mPrizeScreenOffBroadcastReceiver != null){
			Log.i(TAG, "[destory] unregisterReceiver...  mPrizeScreenOffBroadcastReceiver = "+mPrizeScreenOffBroadcastReceiver);
			InCallServiceImpl.this.unregisterReceiver(mPrizeScreenOffBroadcastReceiver);
			mPrizeScreenOffBroadcastReceiver = null;			
		}
    	super.onDestroy();
    }
    private String TAG = "InCallServiceImpl";
    private PrizeScreenOffBroadcastReceiver mPrizeScreenOffBroadcastReceiver;
    public class PrizeScreenOffBroadcastReceiver extends BroadcastReceiver{
		
		 @Override
		    public void onReceive(Context context, Intent intent) {
		        final String action = intent.getAction();
		        Log.d(TAG, "[onReceive]: " + action);
		        if (action.equals("android.intent.action.SCREEN_OFF")) {
		        	PrizeCallGameModleManager.removeFloatView();
		        	final Intent intentInCallActivity = new Intent(Intent.ACTION_MAIN, null);
		        	intentInCallActivity.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
		        	intentInCallActivity.setClass(InCallServiceImpl.this, InCallActivity.class);
		        	intentInCallActivity.putExtra(InCallActivity.NEW_OUTGOING_CALL_EXTRA, false);
		    		InCallServiceImpl.this.startActivity(intentInCallActivity);
		    	}
		        
		    }
	}
    /*prize-add for Game-Modle -hpf-2018-3-16-end*/
}
