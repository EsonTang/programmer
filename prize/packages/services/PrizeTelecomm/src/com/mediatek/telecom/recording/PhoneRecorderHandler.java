/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.telecom.recording;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.widget.Toast;

import com.android.server.telecom.Call;
import com.android.server.telecom.CallState;
import com.android.server.telecom.CallsManagerListenerBase;
import com.android.server.telecom.R;
import com.android.server.telecom.TelecomSystem;
import com.android.server.telecom.TelephonyUtil;

public class PhoneRecorderHandler extends CallsManagerListenerBase{

    private static final String LOG_TAG = "PhoneRecorderHandler";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;
    private static PhoneRecorderHandler sInstance;

    private IPhoneRecorder mPhoneRecorder;
    private int mPhoneRecorderState = PhoneRecorder.IDLE_STATE;
    private Listener mListener;
    private Call mRecordingCall;

    public static final long PHONE_RECORD_LOW_STORAGE_THRESHOLD = 2L * 1024L * 1024L; // unit is BYTE, totally 2MB
    public static final int PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE = 0;

    public interface Listener {
        /**
         *
         * @param state
         * @param customValue
         */
        void requestUpdateRecordState(final int state, final int customValue);

        void onStorageFull();
    }

    private PhoneRecorderHandler() {
    }

    public static synchronized PhoneRecorderHandler getInstance() {
        if (sInstance == null) {
            sInstance = new PhoneRecorderHandler();
        }
        return sInstance;
    }

    /**
     *
     * @param listener
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     *
     * @param listener
     */
    public void clearListener(Listener listener) {
        if (listener == mListener) {
            mListener = null;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mPhoneRecorder = IPhoneRecorder.Stub.asInterface(service);
            try {
                log("onServiceConnected");
                if (null != mPhoneRecorder) {
                    mPhoneRecorder.listen(mPhoneRecordStateListener);
                    if (okToRecordVoice(mRecordingCall)) {
                        mPhoneRecorder.startRecord();
                    }
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onServiceConnected: couldn't register to record service",
                        new IllegalStateException());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            log("[onServiceDisconnected]");
            mPhoneRecorder = null;
        }
    };

    private IPhoneRecordStateListener mPhoneRecordStateListener = new IPhoneRecordStateListener.Stub() {
        /**
         *
         * @param state
         */
        public void onStateChange(int state) {
            log("[onStateChange] state is " + state);
            mPhoneRecorderState = state;
            if (null != mListener) {
                mListener.requestUpdateRecordState(state, PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE);
            }
        }

        public void onStorageFull() {
            log("[onStorageFull] " + mListener);
            mRecordingCall = null;
            if (null != mListener) {
                mListener.onStorageFull();
            }
        }

        /**
        *
        * @param iError
        */
       public void onError(int iError) {
           mRecordingCall = null;
           mHandler.sendEmptyMessage(convertStatusToEventId(iError));
           mPhoneRecorderState = PhoneRecorder.IDLE_STATE;
       }

       public void onFinished(int cause, String data) {
           int eventId = convertStatusToEventId(cause);
           if (data == null) {
               mHandler.sendEmptyMessage(eventId);
           } else {
               Message msg = mHandler.obtainMessage();
               msg.what = eventId;
               msg.obj = data;
               mHandler.sendMessage(msg);
           }
       }
    };

    @Override
    public void onCallStateChanged(Call call, int oldState, int newState) {
        if (call.getState() == CallState.DIALING || call.getState() == CallState.CONNECTING
                || (mRecordingCall == call && !okToRecordVoice(call))) {
            stopVoiceRecord();
        }
    }

    public void onForegroundCallChanged(Call oldForegroundCall, Call newForegroundCall) {
        /// M: Need to stop recording when foreground call changed, e.g. when merge calls
        if (mRecordingCall != null && mRecordingCall != newForegroundCall) {
            stopVoiceRecord();
        }
    }

    /**
     * M: Return append or remove phone record capability action
     * @param call the target call which need to append or remove phone record capability
     * @return true need to append, otherwise need to remove
     */
    public static boolean shouldAppendOrRemovePhoneRecordCapability(Call call) {
        return okToRecordVoice(call) ? true : false;
    }

    private static boolean okToRecordVoice(Call call) {
        if (call == null) {
            return false;
        }

        if (call.getState() != CallState.ACTIVE) {
            return false;
        }

        PhoneAccountHandle accountHandle = call.getTargetPhoneAccountEx();
        if (accountHandle != null) {
            ComponentName name = accountHandle.getComponentName();
            if (TelephonyUtil.isPstnComponentNameEx(name)) {
                Log.v(LOG_TAG, "okToRecordVoice isPstnComponentName");
                return true;
            }
        }

        return false;
    }

    /**
     * Start to record voice of a active call
     * @param activeCall
     * @param customValue
     */
    public void startVoiceRecord(Call activeCall, final int customValue) {
        log("startVoiceRecord: call: " + activeCall);
        if (okToRecordVoice(activeCall)) {
            mRecordingCall = activeCall;
            mPhoneRecorderState = PhoneRecorder.RECORDING_STATE;
            Intent recorderServiceIntent = new Intent(TelecomSystem.getInstance().getContext(),
                    PhoneRecorderServices.class);
            if (null == mPhoneRecorder) {
                TelecomSystem.getInstance().getContext().bindService(recorderServiceIntent,
                        mConnection, Context.BIND_AUTO_CREATE);
            } else {
                try {
                    mPhoneRecorder.startRecord();
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "start to record failed", new IllegalStateException());
                }
            }
        } else {
            Log.w(LOG_TAG, "cannot start to record with call: " + activeCall);
        }
    }

    public void stopVoiceRecord() {
        log("stopVoiceRecord: call: " + mRecordingCall);
        if (mRecordingCall != null) {
            mRecordingCall = null;
            stopVoiceRecord(true);
        }
    }

    private void stopVoiceRecord(boolean isMount) {
        try {
            log("stopRecord");
            if (null != mPhoneRecorder) {
                mPhoneRecorder.stopRecord(isMount);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "stopRecord: couldn't call to record service",
                    new IllegalStateException());
        }
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static final int EVENT_STORAGE_FULL = 0;
    private static final int EVENT_SAVE_SUCCESS = 1;
    private static final int EVENT_STORAGE_UNMOUNTED = 2;
    private static final int EVENT_SDCARD_ACCESS_ERROR = 3;
    private static final int EVENT_INTERNAL_ERROR = 4;

    private int convertStatusToEventId(int statusCode) {
        int eventId = EVENT_INTERNAL_ERROR;
        switch (statusCode) {
            case Recorder.SDCARD_ACCESS_ERROR:
                eventId = EVENT_SDCARD_ACCESS_ERROR;
                break;
            case Recorder.SUCCESS:
                eventId = EVENT_SAVE_SUCCESS;
                break;
            case Recorder.STORAGE_FULL:
                eventId = EVENT_STORAGE_FULL;
                break;
            case Recorder.STORAGE_UNMOUNTED:
                eventId = EVENT_STORAGE_UNMOUNTED;
                break;
            case Recorder.INTERNAL_ERROR:
            default:
                eventId = EVENT_INTERNAL_ERROR;
                break;
        }
        return eventId;
    }

    private MyHandler mHandler = new MyHandler(Looper.getMainLooper());
    private class MyHandler extends Handler {
        public MyHandler(Looper loop) {
            super(loop);
        }
        private Context getServiceContext() {
            return TelecomSystem.getInstance().getContext();
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_STORAGE_FULL:
                    Toast.makeText(
                            getServiceContext(),
                            getServiceContext().getApplicationContext().getResources()
                                    .getText(R.string.confirm_device_info_full), Toast.LENGTH_LONG)
                            .show();
                    break;
                case EVENT_SAVE_SUCCESS:
                    String path = (String) msg.obj;
                    Toast.makeText(getServiceContext(), path, Toast.LENGTH_LONG).show();
                    break;
                case EVENT_STORAGE_UNMOUNTED:
                    Toast.makeText(
                            getServiceContext(),
                            getServiceContext().getApplicationContext()
                                    .getText(R.string.ext_media_badremoval_notification_title),
                            Toast.LENGTH_LONG).show();
                    break;
                case EVENT_SDCARD_ACCESS_ERROR:
                    Toast.makeText(
                            getServiceContext(),
                            getServiceContext().getApplicationContext().getResources().getString(
                                    R.string.error_sdcard_access),
                            Toast.LENGTH_LONG).show();
                    break;
                case EVENT_INTERNAL_ERROR:
                    Toast.makeText(
                            getServiceContext(),
                            getServiceContext().getApplicationContext().getResources().getString(
                            R.string.alert_device_error),
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            if (mPhoneRecorderState == PhoneRecorder.IDLE_STATE && mPhoneRecorder != null) {
                log("Ready to unbind service");
                TelecomSystem.getInstance().getContext().unbindService(mConnection);
                mPhoneRecorder = null;
            }
        }
    };
}
