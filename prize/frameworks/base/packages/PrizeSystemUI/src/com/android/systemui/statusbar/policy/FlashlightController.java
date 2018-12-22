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

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.os.SystemClock;
import android.os.PowerManager;

/**
 * Manages the flashlight.
 */
public class FlashlightController {

    private static final String TAG = "FlashlightController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final int DISPATCH_ERROR = 0;
    private static final int DISPATCH_CHANGED = 1;
    private static final int DISPATCH_AVAILABILITY_CHANGED = 2;

    private final CameraManager mCameraManager;
    private final Context mContext;
    /** Call {@link #ensureHandler()} before using */
    private Handler mHandler;

    /** Lock on mListeners when accessing */
    private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList<>(1);

    /** Lock on {@code this} when accessing */
    private boolean mFlashlightEnabled;

    private String mCameraId;
    private boolean mTorchAvailable;

    public FlashlightController(Context context) {
        mContext = context;
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

    public void setFlashlight(boolean enabled) {
        boolean pendingError = false;
        synchronized (this) {
            if (mCameraId == null) return;
            //if (mFlashlightEnabled != enabled) {
                //mFlashlightEnabled = enabled;
                Log.d(TAG, "setFlashlight enabled = " + enabled);
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

                /*try {
                    mCameraManager.setTorchMode(mCameraId, enabled);
                } catch (CameraAccessException e) {
                    Log.e(TAG, "Couldn't set torch mode", e);
                    mFlashlightEnabled = false;
                    pendingError = true;
                }*/
            //}
        }
        dispatchModeChanged(mFlashlightEnabled);
        if (pendingError) {
            dispatchError();
        }
    }	
	
    public interface OnSetFlashlightCallBack {
        public void onSetSuccessed();
        public void onSetFailed();
    }
    public OnSetFlashlightCallBack onSetFlashlightCallBack;
	private int value;

    public synchronized void setFlashlight(boolean enabled, int value, OnSetFlashlightCallBack callback) {
        Log.i(TAG, "[setFlashlight] mFlashlightEnabled = " + mFlashlightEnabled + ", enabled=" + enabled
                + ", this.value = " + this.value + ", value=" + value);
        if (this.value != value || mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            this.value = value;
            onSetFlashlightCallBack = callback;
            postUpdateFlashlight();
        }
    }

    public void killFlashlight() {
        Log.i(TAG, "[killFlashlight]mFlashlightEnabled=" + mFlashlightEnabled);
        boolean enabled;
        synchronized (this) {
            enabled = mFlashlightEnabled;
        }
        if (enabled) {
            mHandler.post(mKillFlashlightRunnable);
        }
    }

    public boolean hasFlashlight() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public synchronized boolean isEnabled() {
        return mFlashlightEnabled;
    }

    public synchronized boolean isAvailable() {
        return mTorchAvailable;
    }

    public void addListener(FlashlightListener l) {
        synchronized (mListeners) {
            if (mCameraId == null) {
                tryInitCamera();
            }
            cleanUpListenersLocked(l);
            mListeners.add(new WeakReference<>(l));
        }
    }

    public void removeListener(FlashlightListener l) {
        synchronized (mListeners) {
            cleanUpListenersLocked(l);
        }
    }

    private synchronized void ensureHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }
    }

    private void postUpdateFlashlight() {
        ensureHandler();
        mHandler.post(mUpdateFlashlightRunnable);
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


    /*PRIZE-change the open method of the Camera-liufan-2015-09-21-start*/
    private void updateFlashlight(boolean forceDisable) {
        try {
            boolean enabled;
            Log.i(TAG, "[updateFlashlight]mFlashlightEnabled = " + mFlashlightEnabled + ", forceDisable = " + forceDisable);
            synchronized (this) {
                enabled = mFlashlightEnabled && !forceDisable;
            }
            if (enabled) {
                setFlashlight(true);
            } else {
                setFlashlight(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateFlashlight", e);
            handleError();
        }
    }
	
	private void turnOffScreen() {
	//printMyLog("--- Double tap to turn off screen -----");
	PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
	if(mPowerManager != null) {
		if(mPowerManager.isScreenOn()) {
			mPowerManager.goToSleep(SystemClock.uptimeMillis()); 
		} else {
			//printMyLog("--- skip !! The device is screen off -----");
		}
	} else {
		//printMyLog("--- mPowerManager = null !! -----");
	}
   }
	private void updateFlashlight(int value) {
		switch(value){
		case 0:
			//if(camera!=null){
				updateFlashlight(false);
                runSetFlashCallback(true);
			//}
			break;
		case 1:
			//if(camera==null){
			//	try{
			//		camera = Camera.open();
			//	}catch(Exception e){
			//		mHandler.postDelayed(mRunnable, 300);
			//		break;
			//	}
			//}
			//if(camera!=null){
				updateFlashlight(false);
			//}
			runSetFlashCallback(true);
			break;
		case 2:
			//if(camera != null){
			//	camera.release();
			//	camera = null;
                runSetFlashCallback(true);
			//}
			break;
		case 3:
			//if(camera==null){
			//	try{
			//		camera = Camera.open();
			//	}catch(Exception e){
			//		mHandler.postDelayed(mRunnable, 300);
			//		break;
			//	}
			//}
			//if(camera!=null){
				updateFlashlight(false);
			//}
			runSetFlashCallback(true);
			turnOffScreen();
			break;
        case 4:
            //if(camera!=null){
                updateFlashlight(false);
            //    camera.release();
            //    camera = null;
            //}
			runSetFlashCallback(true);
            break;
		}
	}
    /*PRIZE-change the open method of the Camera-liufan-2015-09-21-end*/
    private void handleError() {
        synchronized (this) {
            mFlashlightEnabled = false;
        }
        dispatchError();
        dispatchModeChanged(false);
        updateFlashlight(true /* forceDisable */);
    }
    private void dispatchModeChanged(boolean enabled) {
        dispatchListeners(DISPATCH_CHANGED, enabled);
    }

    private void dispatchError() {
        dispatchListeners(DISPATCH_CHANGED, false /* argument (ignored) */);
    }

    private void dispatchAvailabilityChanged(boolean available) {
        dispatchListeners(DISPATCH_AVAILABILITY_CHANGED, available);
    }

    private void dispatchListeners(int message, boolean argument) {
        synchronized (mListeners) {
            final int N = mListeners.size();
            boolean cleanup = false;
            for (int i = 0; i < N; i++) {
                FlashlightListener l = mListeners.get(i).get();
                if (l != null) {
                    if (message == DISPATCH_ERROR) {
                        l.onFlashlightError();
                    } else if (message == DISPATCH_CHANGED) {
                        l.onFlashlightChanged(argument);
                    } else if (message == DISPATCH_AVAILABILITY_CHANGED) {
                        l.onFlashlightAvailabilityChanged(argument);
                    }
                } else {
                    cleanup = true;
                }
            }
            if (cleanup) {
                cleanUpListenersLocked(null);
            }
        }
    }

    private void cleanUpListenersLocked(FlashlightListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            FlashlightListener found = mListeners.get(i).get();
            if (found == null || found == listener) {
                mListeners.remove(i);
            }
        }
    }

    private final CameraManager.TorchCallback mTorchCallback =
            new CameraManager.TorchCallback() {

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            Log.d(TAG, "onTorchModeUnavailable");
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(false);
            }
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            Log.d(TAG, "onTorchModeChanged(" + enabled + ")");
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(true);
                setTorchMode(enabled);
            }
        }

        private void setCameraAvailable(boolean available) {
            Log.d(TAG, "setCameraAvailable(" + available + ")");
            boolean changed;
            synchronized (FlashlightController.this) {
                changed = mTorchAvailable != available;
                mTorchAvailable = available;
            }
            if (changed) {
                if (DEBUG) Log.d(TAG, "dispatchAvailabilityChanged(" + available + ")");
                dispatchAvailabilityChanged(available);
            }
        }

        private void setTorchMode(boolean enabled) {
            Log.d(TAG, "setTorchMode(" + enabled + ")");
            boolean changed;
            synchronized (FlashlightController.this) {
                changed = mFlashlightEnabled != enabled;
                mFlashlightEnabled = enabled;
            }
            if (changed) {
                if (DEBUG) Log.d(TAG, "dispatchModeChanged(" + enabled + ")");
                dispatchModeChanged(enabled);
            }
        }
    };

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("FlashlightController state:");

        pw.print("  mCameraId=");
        pw.println(mCameraId);
        pw.print("  mFlashlightEnabled=");
        pw.println(mFlashlightEnabled);
        pw.print("  mTorchAvailable=");
        pw.println(mTorchAvailable);
    }

	
    private final Runnable mUpdateFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
			updateFlashlight(value);
            //updateFlashlight(false /* forceDisable */);
        }
    };

    private final Runnable mKillFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mFlashlightEnabled = false;
            }
            updateFlashlight(true /* forceDisable */);
            dispatchModeChanged(false);
        }
    };

    public interface FlashlightListener {

        /**
         * Called when the flashlight was turned off or on.
         * @param enabled true if the flashlight is currently turned on.
         */
        void onFlashlightChanged(boolean enabled);


        /**
         * Called when there is an error that turns the flashlight off.
         */
        void onFlashlightError();

        /**
         * Called when there is a change in availability of the flashlight functionality
         * @param available true if the flashlight is currently available.
         */
        void onFlashlightAvailabilityChanged(boolean available);
    }
    /** PRIZE-release the Camera when exit Camera and open flashlight immediately-xuchunming-2015-10-8-start*/
//    private Runnable mRunnable = new Runnable() {
//        private final int  TOTALCOUNT = 3;
//        private int count = 0;
//		public void run() {
//
//			// TODO Auto-generated method stub
//			try{
//				camera = Camera.open();
//				if(camera!=null){
//					updateFlashlight(false);
//					count = 0;
//                    runSetFlashCallback(true);
//				}
//			}catch(Exception e){
//				if(count++ < TOTALCOUNT){
//					mHandler.postDelayed(mRunnable, 300);
//				} else {
//                    runSetFlashCallback(false);
//                    FlashlightController.this.value = 0;
//                    mFlashlightEnabled = false;
//                }
//
//			}
//		}
//	};
	/** PRIZE-release the Camera when exit Camera and open flashlight immediately-xuchunming-2015-10-8-end*/

    public void runSetFlashCallback(boolean success){
        if(onSetFlashlightCallBack!=null){
            if(success){
                onSetFlashlightCallBack.onSetSuccessed();
            }else{
                onSetFlashlightCallBack.onSetFailed();
            }
            onSetFlashlightCallBack = null;
        }
    }
}
