/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.mediatek.phone;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.android.internal.R;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * An information collect service for RF tuning.
 */
public class ApInfoCollectService extends Service {
    private class ApInfo {
        String phoneState;
        boolean isNear;
        int voiceCallDevice;
        int deviceRotation;
    }

    private AudioManager mAudioManager;
    private SensorManager mSensors;
    private ProximityCheck mProximity;
    private OrientationEventListener mOrientationEventListener;
    private ApInfo mApInfo = new ApInfo();
    private Phone mPhone = null;

    private static String TAG = "ApInfoCollectService";

    /* property to check which information we should monitor */
    public static final String PROPERTY_AP_INFO_MONITOR = "ro.ap_info_monitor";

    public static final int MONITOR_NONE = 0;
    public static final int MONITOR_STREAM_DEVICE = 1;
    public static final int MONITOR_ORIENTATION = 1 << 1;
    public static final int MONITOR_PHONE_STATE = 1 << 2;
    public static final int MONITOR_PROXIMITY = 1 << 3;

    public static final int SENSOR_TRIGGER_PHONESTATE_IDLE = 0;
    public static final int SENSOR_TRIGGER_PHONESTATE_INCALL = 1;

    private static final int MSG_UPDATE_AP_INFORMATION = 1;
    private static final int MSG_SERVICE_READY = 2;

    private boolean mSensorMonitoring;
    public static int mInfoToMonitor = 0x0;

    private final Object sRotationLock = new Object();

    //private final WakeLock mWakeLock;  // help while there is pending change

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        if (toMonitorProximitySensor() == 1) {
            mSensors = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        }

        // get default phone
        mPhone = PhoneFactory.getDefaultPhone();

        if (toMonitorReceiver() == 1) {
            mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int device = mAudioManager.getDevicesForStream(AudioManager.STREAM_VOICE_CALL);
            if ((device & AudioManager.DEVICE_OUT_WIRED_HEADSET) != 0) {
                mApInfo.voiceCallDevice = AudioManager.DEVICE_OUT_WIRED_HEADSET;
            } else if ((device & AudioManager.DEVICE_OUT_WIRED_HEADPHONE) != 0) {
                mApInfo.voiceCallDevice = AudioManager.DEVICE_OUT_WIRED_HEADPHONE;
            } else if ((device & AudioManager.DEVICE_OUT_EARPIECE) != 0) {
                mApInfo.voiceCallDevice = AudioManager.DEVICE_OUT_EARPIECE;
            } else {
                mApInfo.voiceCallDevice = AudioManager.DEVICE_NONE;
            }
        } else {
            mApInfo.voiceCallDevice = AudioManager.DEVICE_NONE;
        }

        if (toMonitorOrientation() == 1) {
            updateOrientation();
        }

        mApInfo.phoneState = TelephonyManager.EXTRA_STATE_IDLE;
        setDevicesState();

        // Register receiver for intents
        IntentFilter filter = new IntentFilter();
        if (toMonitorPhoneState() == 1 || toMonitorProximitySensor() == 1) {
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        }
        if (toMonitorReceiver() == 1) {
            filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
        }

        registerReceiver(mAicReceiver, filter);

        if (toMonitorOrientation() == 1) {
            mOrientationEventListener = new OrientationEventListener(this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    boolean changeOrientation = false;
                    if (orientation == ORIENTATION_UNKNOWN) {
                        orientation = 0;
                    }
                    orientation = roundOrientation(orientation);
                    if (mApInfo.deviceRotation != orientation) {
                        changeOrientation = true;
                    }
                    if (changeOrientation) {
                        mApInfo.deviceRotation = orientation;
                        setDevicesState();
                    }
                }
            };
            mOrientationEventListener.enable();
        }
    }

    private int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 180;
    }

    /**
     * Query current display rotation and update if needed.
     */
    private void updateOrientation() {
        // Even though there's event listener to update the information,
        // get an initial value in case there's no update later
        int newRotation = ((WindowManager) getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        synchronized(sRotationLock) {
            if (roundOrientation(newRotation) != mApInfo.deviceRotation) {
                mApInfo.deviceRotation = newRotation;
            }
        }
    }

    private void requestSensor(final int reason) {
        if (toMonitorProximitySensor() == 1) {
            if (reason == SENSOR_TRIGGER_PHONESTATE_INCALL && mSensorMonitoring != true) {
                mSensorMonitoring = true;
                // perform a proximity check
                mProximity = new ProximityCheck() {
                    @Override
                    public void onProximityResult(int result) {
                        final boolean isNear = result == RESULT_NEAR;
                        traceProximityResult(isNear);
                    }
                };
                mProximity.check();
            } else if (reason == SENSOR_TRIGGER_PHONESTATE_IDLE) {
                mProximity.finish();
            }
        }
    }

    private final Handler mHandler = new Handler(Looper.myLooper(), null, true){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_AP_INFORMATION:
                    Log.d(TAG, "MSG_UPDATE_AP_INFORMATION");
                    break;
            }
        }
    };

    private void setDevicesState() {
        int receiver, screen_rotate, near, in_call;

        in_call = -1;
        near = -1;

        if (toMonitorReceiver() == 1) {
            if (mApInfo.voiceCallDevice == AudioManager.DEVICE_OUT_EARPIECE) {
                receiver = 1;   // earpiece
            } else if (mApInfo.voiceCallDevice == AudioManager.DEVICE_NONE) {
                receiver = -1;
            } else {
                receiver = 0;   // other output device
            }
        } else {
            receiver = -1;
        }
        if (toMonitorOrientation() == 1) {
            if (mApInfo.deviceRotation == 0) {
                screen_rotate = 0;   // portrait
            } else {
                screen_rotate = 1;   // landscape
            }
        } else {
            screen_rotate = -1;
        }
        if (toMonitorPhoneState() == 1) {
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(mApInfo.phoneState)) {
                in_call = 0;   //  not in call
            } else {
                in_call = 1;   // in call
            }
        } else {
            in_call = -1;
        }
        if (toMonitorProximitySensor() == 1) {
            if (mApInfo.isNear) {
                near = 1;   // near
            } else {
                near = 0;   // far
            }
        } else {
            near = -1;
        }

        // Strings with data

        String s;

        s = "AT+EAPINFO=" + receiver + "," + screen_rotate + "," + near + "," + in_call;

        mPhone.invokeOemRilRequestRaw(s.getBytes(),
                mHandler.obtainMessage(MSG_UPDATE_AP_INFORMATION));
    }

    /**
     * Listens for AP information collector intents
     */
    private BroadcastReceiver mAicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean changed = false;
            Log.d(TAG, "mAicReceiver: action = " + action);
            // Update voice call and volte call status
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                Log.d(TAG, "state: " + state + " phoneState: " + mApInfo.phoneState);
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                    requestSensor(SENSOR_TRIGGER_PHONESTATE_IDLE);
                } else {
                    requestSensor(SENSOR_TRIGGER_PHONESTATE_INCALL);
                }
                changed = mApInfo.phoneState.equals(state) ? false : true;
                mApInfo.phoneState = state;
            }
            // Update stream devices
            else if (AudioManager.STREAM_DEVICES_CHANGED_ACTION.equals(action)) {
                final int stream = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                final int devices = intent
                        .getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_DEVICES, -1);
                final int oldDevices = intent
                        .getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_DEVICES, -1);
                Log.d(TAG, "onReceive STREAM_DEVICES_CHANGED_ACTION stream="
                        + stream + " devices=" + devices + " oldDevices=" + oldDevices);
                changed = checkVoiceCallDevice(stream, devices, oldDevices);
            }
            if (changed) {
                setDevicesState();
            }
        }
    };

    /** Constructor */
    public ApInfoCollectService() {
        Log.d(TAG, "ApInfoCollectService()");
    }

    private boolean checkVoiceCallDevice(int stream, int device, int oldDevice) {
        boolean changed = false;
        if (stream == AudioManager.STREAM_VOICE_CALL) {
            if ((device == AudioManager.DEVICE_OUT_EARPIECE &&
                    oldDevice != AudioManager.DEVICE_OUT_EARPIECE) ||
                    (device == AudioManager.DEVICE_OUT_WIRED_HEADPHONE &&
                    oldDevice != AudioManager.DEVICE_OUT_WIRED_HEADPHONE) ||
                    (device == AudioManager.DEVICE_OUT_WIRED_HEADSET &&
                    oldDevice != AudioManager.DEVICE_OUT_WIRED_HEADSET)) {
                changed = true;
            }
            updateVoiceCallDevice(device);
        }
        return changed;
    }

    private boolean updateVoiceCallDevice(int device) {
        mApInfo.voiceCallDevice = device;
        Log.d(TAG, "updateVoiceCallDevice voiceCallDevice=" + mApInfo.voiceCallDevice);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * get the information that need to be monitored
     * @return int, the information to be monitored
     */
    public static int getApInfoToMonitor() {
        mInfoToMonitor = SystemProperties.getInt(PROPERTY_AP_INFO_MONITOR, 0);
        Log.d(TAG, "getApInfoToMonitor: infoToMonitor " + mInfoToMonitor);
        return mInfoToMonitor;
    }

    /*
     * check if need to monitor receiver status
     * @return int, cases are following
     *         1, need to monitor
     *         0, no need to monitor
     */
    private static int toMonitorReceiver() {
        int result = (mInfoToMonitor & MONITOR_STREAM_DEVICE) ==
                MONITOR_STREAM_DEVICE ? 1 : 0;
        return result;
    }

    /*
     * check if need to monitor screen orientation
     * @return int, cases are following
     *         1, need to monitor
     *         0, no need to monitor
     */
    private static int toMonitorOrientation() {
        int result = (mInfoToMonitor & MONITOR_ORIENTATION) ==
                MONITOR_ORIENTATION ? 1 : 0;
        return result;
    }

    /*
     * check if need to monitor phone state
     * @return int, cases are following
     *         1, need to monitor
     *         0, no need to monitor
     */
    private static int toMonitorPhoneState() {
        int result = (mInfoToMonitor & MONITOR_PHONE_STATE) ==
                MONITOR_PHONE_STATE ? 1 : 0;
        return result;
    }

    /*
     * check if need to monitor proximity sensor
     * @return int, cases are following
     *         1, need to monitor
     *         0, no need to monitor
     */
    private static int toMonitorProximitySensor() {
        int result = (mInfoToMonitor & MONITOR_PROXIMITY) ==
                MONITOR_PROXIMITY ? 1 : 0;
        return result;
    }

    private abstract class ProximityCheck implements SensorEventListener, Runnable {
        protected static final int RESULT_UNKNOWN = 0;
        protected static final int RESULT_NEAR = 1;
        protected static final int RESULT_FAR = 2;

        private boolean mRegistered;
        private boolean mFinished;
        private float mMaxRange;

        abstract public void onProximityResult(int result);

        public void traceProximityResult(boolean near) {
            Log.d(TAG, "proximityResult near=" + near);
            if (near != mApInfo.isNear) {
                mApInfo.isNear = near;
                setDevicesState();
            }
        }

        public void check() {
            if (mRegistered) return;
            final Sensor sensor = mSensors.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (sensor == null) {
                Log.d(TAG, "No sensor found");
                return;
            }

            mMaxRange = sensor.getMaximumRange();
            mSensors.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, 0, null);
            mRegistered = true;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.values.length == 0) {
                Log.d(TAG, "Event has no values!");
                updateResult(RESULT_UNKNOWN);
            } else {
                Log.d(TAG, "Event: value=" + event.values[0] + " max=" + mMaxRange);
                final boolean isNear = event.values[0] < mMaxRange;
                updateResult(isNear ? RESULT_NEAR : RESULT_FAR);
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "No event received before timeout");
            updateResult(RESULT_UNKNOWN);
        }

        private void updateResult(int result) {
            if (mFinished) return;
            onProximityResult(result);
        }

        private void finish() {
            if (mFinished) return;
            if (mRegistered) {
                mSensors.unregisterListener(this);
                // we're done - call disconnected
                mSensorMonitoring = false;
                mRegistered = false;
            }
            mFinished = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy){
            // noop
        }
    }
}

