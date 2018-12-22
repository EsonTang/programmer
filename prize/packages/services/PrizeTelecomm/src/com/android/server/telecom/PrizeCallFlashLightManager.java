package com.android.server.telecom;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import android.provider.Settings;

/**
 * @author xiarui
 * @description PRIZE_LED_BLINK, for flash light control when incoming call
 * @date 2018/8/3
 */
public class PrizeCallFlashLightManager extends CallsManagerListenerBase {

    private static final String TAG = "FlashLightManager";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final int SWITCH_OPEN = 0;
    private static final int SWITCH_OFF = 1;
    private static final int GAP = 500;
    private final Context mContext;
    private final CameraManager mCameraManager;
    private Handler mHandler;
    private String mCameraId;
    private boolean mFlashlightEnabled;
    private boolean mTorchAvailable;
    private final CallAudioModeStateMachine mCallAudioModeStateMachine;

    private Handler mFlashControlHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITCH_OPEN:
                    setFlashlight(true);
                    mFlashControlHandler.sendEmptyMessageDelayed(SWITCH_OFF, GAP);
                    break;
                case SWITCH_OFF:
                    setFlashlight(false);
                    mFlashControlHandler.sendEmptyMessageDelayed(SWITCH_OPEN, GAP);
                    break;
            }
        }
    };

    public PrizeCallFlashLightManager(Context context, CallAudioModeStateMachine callAudioModeStateMachine) {
        mContext = context;
        mCallAudioModeStateMachine = callAudioModeStateMachine;
        mCallAudioModeStateMachine.setFlashLightManager(this);
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        tryInitCamera();
    }

    private void tryInitCamera() {
        try {
            mCameraId = getCameraId();
        } catch (Throwable e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        }

        if (mCameraId != null) {
            ensureHandler();
            mCameraManager.registerTorchCallback(mTorchCallback, mHandler);
        }
    }

    private synchronized void ensureHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }
    }

    public void setFlashlight(boolean enabled) {
        boolean pendingError = false;
        synchronized (this) {
            if (mCameraId == null) return;
            if (mFlashlightEnabled != enabled) {
                mFlashlightEnabled = enabled;
                /*
                try {
                    mCameraManager.setTorchMode(mCameraId, enabled);
                } catch (CameraAccessException e) {
                    Log.e(TAG, "Couldn't set torch mode", e);
                    mFlashlightEnabled = false;
                    pendingError = true;
                }
                */
                /*
                int value = enabled ? 1 : 0;
                try {
                    String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + value + " > /proc/koobee_flash"};
                    Runtime.getRuntime().exec(cmdMode);
                    Log.d(TAG, "set flash light success");
                } catch (IOException e) {
                    mFlashlightEnabled = false;
                    pendingError = true;
                    e.printStackTrace();
                    Log.d(TAG, "set flash light error");
                }
                */
                if (enabled) {
                    startFlash();
                } else {
                    closeFlash();
                }
            }
        }
        //dispatchModeChanged(mFlashlightEnabled);
        //if (pendingError) {
        //    dispatchError();
        //}
    }

    private void startFlash() {
        if (mTorchAvailable) {
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 1);
        }
    }
    private void closeFlash() {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 0);
    }


    public void startBlink() {
        if (hasFlashlight()) {
            SystemProperties.set("debug.blinking.flashlight", "true");
            mFlashControlHandler.sendEmptyMessage(SWITCH_OPEN);
        }
    }

    public void stopBlink() {
        if (hasFlashlight()) {
            SystemProperties.set("debug.blinking.flashlight", "false");
            mFlashControlHandler.removeMessages(SWITCH_OPEN);
            mFlashControlHandler.removeMessages(SWITCH_OFF);
            setFlashlight(false);
        }
    }

    public boolean hasFlashlight() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    private final CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(false);
            }
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(true);
                setTorchMode(enabled);
            }
        }

        private void setCameraAvailable(boolean available) {
            boolean changed;
            synchronized (PrizeCallFlashLightManager.this) {
                changed = mTorchAvailable != available;
                mTorchAvailable = available;
            }
            if (changed) {
                if (DEBUG) Log.d(TAG, "dispatchAvailabilityChanged(" + available + ")");
                //dispatchAvailabilityChanged(available);
            }
        }

        private void setTorchMode(boolean enabled) {
            boolean changed;
            synchronized (PrizeCallFlashLightManager.this) {
                changed = mFlashlightEnabled != enabled;
                mFlashlightEnabled = enabled;
            }
            if (changed) {
                if (DEBUG) Log.d(TAG, "dispatchModeChanged(" + enabled + ")");
                //dispatchModeChanged(enabled);
            }
        }
    };

}
