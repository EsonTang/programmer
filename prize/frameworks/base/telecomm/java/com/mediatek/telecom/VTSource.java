/* MediaTek Inc. (C) 2016. All rights reserved.
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

package com.mediatek.telecom;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.StateCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.OutputConfiguration;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Range;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider source buffer for video call.
 * @hide
 */
public class VTSource {

    /**
     * The interface used to callback VTSource event
     */
    public interface EventCallback {
        void onError();
    }

    /**
     * The structure used to describe one camera sensor's resolution and install orientation.
     */
    public static final class Resolution {
        int mId;
        int mMaxWidth;
        int mMaxHeight;
        int mDegree;
        int mFacing;
    }

    public static final int VT_SRV_CALL_3G = 1;
    public static final int VT_SRV_CALL_4G = 2;
    private static final String TAG = "VT SRC";
    private static final int TIME_OUT_MS = 6500; //ms,the timeout in legacy camera is 4000ms.
    private final EventCallback mEventCallBack;
    private final CameraManager mCameraManager;
    private final int mMode;
    private static Context sContext;
    private static Resolution[] sCameraResolutions;
    private HandlerThread mRequestThread;
    private Handler mRequestHandler;

    private Surface mCachedPreviewSurface;
    private Surface mCachedRecordSurface;
    private boolean mNeedRecordStream;
    private boolean mIsWaitRelease = false;

    private String mTAG;

    /**
     * Set Context to VTSource.
     * @param context from IMS.
     * @hide
     */
    public static void setContext(Context context) {
        Log.d(TAG, "[STC] [setContext] context:" + context);
        sContext = context;
    }

    /**
     * Get current platform's all camera resolutions when boot then send to MA.
     * @return an array of all camera's resolution.
     * @hide
     */
    public static Resolution[] getAllCameraResolutions() {

        Log.d(TAG, "[STC] [getAllCameraResolutions] Start");

        if (sCameraResolutions == null) {
            ArrayList<Resolution> sensorResolutions = new ArrayList<>();
            CameraManager cameraManager =
                    (CameraManager) sContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIds = cameraManager.getCameraIdList();
                for (String cameraId : cameraIds) {
                    Resolution resolution = new Resolution();
                    CameraCharacteristics characteristics =
                            cameraManager.getCameraCharacteristics(cameraId);
                    Rect sensorRes = characteristics.get(
                            CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int sensorOrientation = characteristics.get(
                            CameraCharacteristics.SENSOR_ORIENTATION);
                    int facing = characteristics.get(
                            CameraCharacteristics.LENS_FACING);
                    resolution.mId = Integer.valueOf(cameraId);
                    resolution.mMaxWidth = sensorRes.width();
                    resolution.mMaxHeight = sensorRes.width();
                    resolution.mDegree = sensorOrientation;
                    resolution.mFacing = facing;
                    sensorResolutions.add(resolution);
                }
            } catch (Exception  e) {
                Log.e(TAG, "[STC] [getAllCameraResolutions] getCameraIdList with exception:"
                        + e);
            }
            if (sensorResolutions.size() > 0) {
                sCameraResolutions = new Resolution[sensorResolutions.size()];
                sCameraResolutions = sensorResolutions.toArray(sCameraResolutions);
            }
            Log.d(TAG, "[STC] [getAllCameraResolutions] resolution size:"
                    + sensorResolutions.size());
        }
        Log.d(TAG, "[STC] [getAllCameraResolutions] Finish");
        return sCameraResolutions;
    }

    /**
     * New VTSource with 3G/4G mode;
     * 4G need rotate buffer to portrait.
     * 3G no need rotate buffer, keep it the same with sensor orientation.
     * @param mode current mode.
     * @param callId  call ID.
     * @hide
     */
    public VTSource(int mode, int callId, EventCallback cb) {
        mTAG = "VT SRC - " + callId;

        Log.d(mTAG, "[INT] [VTSource] Start");
        Log.d(mTAG, "[INT] [VTSource] mode: " + mode);

        mMode = mode;
        mEventCallBack = cb;
        mCameraManager = (CameraManager) sContext.getSystemService(Context.CAMERA_SERVICE);
        createRequestThreadAndHandler();
        Log.d(mTAG, "[INT] [VTSource] Finish");
    }

    /**
     * Set replace picture path.
     * @param uri the replaced picture's uri.
     * @hide
     */
    public void setReplacePicture(Uri uri) {
        Log.d(mTAG, "[INT] [setReplacePicture] uri:" + uri);
    }

    /**
     * open camera, if another camera is running, switch camera.
     * @param cameraId indicate which camera to be opened.
     * @hide
     */
    public void open(String cameraId) {

        Log.d(mTAG, "[INT] [open] Start");
        Log.d(mTAG, "[INT] [open] id : " + cameraId);

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [open] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_OPEN_CAMERA, cameraId).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [open] Finish");
    }

    /**
     * Close current opened camera.
     * @hide
     */
    public void close() {

        Log.d(mTAG, "[INT] [close] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [close] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_CLOSE_CAMERA).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [close] Finish");
    }

    /**
     * Release resource when do not use it.
     * @hide
     */
    public void release() {

        Log.d(mTAG, "[INT] [release] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [release] Fail");
            return;
        }

        mIsWaitRelease = true;

        mRequestHandler.obtainMessage(DeviceHandler.MSG_RELEASE).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }
        mRequestHandler.removeCallbacksAndMessages(null);
        // Only quit thread if thread not null.
        if (mRequestThread != null) {
            mRequestThread.quitSafely();
            mRequestThread = null;
        }

        Log.d(mTAG, "[INT] [release] Finish");
    }

    /**
     * Set BufferQueueProducer to VTSource to put image data.
     * @param surface the surface used to receive record buffer.
     * @hide
     */
    public void setRecordSurface(Surface surface) {

        Log.d(mTAG, "[INT] [setRecordSurface] Start");
        Log.d(mTAG, "[INT] [setRecordSurface] surface:" + surface);

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [setRecordSurface] Fail");
            return;
        }

        mRequestHandler.obtainMessage(
                DeviceHandler.MSG_UPDATE_RECORD_SURFACE, surface).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [setRecordSurface] Finish");
    }

    /**
     * Update preview surface, if surface is null, do stop preview and clear cached preview surface.
     * @param surface the surface used to receive preview buffer.
     * @hide
     */
    public void setPreviewSurface(Surface surface) {

        Log.d(mTAG, "[INT] [updatePreviewStatus] Start");
        Log.d(mTAG, "[INT] [updatePreviewStatus] surface:" + surface);

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [setPreviewSurface] Fail");
            return;
        }

        if (surface == null) {
            mRequestHandler.obtainMessage(
                    DeviceHandler.MSG_STOP_PREVIEW).sendToTarget();
        } else {

            if (mCachedPreviewSurface != null) {
                // Ex: surface:Surface(name=android.graphics.SurfaceTexture@f1fbca5)/@0x7d5913f
                // @SurfaceTexture hash code/@Surface hash code
                String[] oriSurfaceToken = mCachedPreviewSurface.toString().split("@");
                String[] newSurfaceToken = surface.toString().split("@");

                Log.d(mTAG, "[INT] [setPreviewSurface] oriSurfaceToken[1]:" + oriSurfaceToken[1] +
                                                    ", newSurfaceToken[1]:" + newSurfaceToken[1]);

                // Here we only compare surfaceTexture hash code
                if (newSurfaceToken[1].equals(oriSurfaceToken[1])) {
                    Log.d(mTAG, "[INT] [setPreviewSurface] surface not changed, ignore!");
                    return;
                }
            }

            mRequestHandler.obtainMessage(
                    DeviceHandler.MSG_START_PREVIEW, surface).sendToTarget();
        }

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [updatePreviewStatus] Finish");
    }

    /**
     * Perform zoom by specified zoom value.
     * @param zoomValue the wanted zoom value.
     * @hide
     */
    public void setZoom(float zoomValue) {

        Log.d(mTAG, "[INT] [setZoom] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [setZoom] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_PERFORM_ZOOM, zoomValue).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [setZoom] Finish");
    }

    /**
     * Get current using camera's characteristics.
     * @return an instance of camera's characteristics, if camera closed we return null.
     */
    public CameraCharacteristics getCameraCharacteristics() {

        Log.d(mTAG, "[INT] [getCameraCharacteristics] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [getCameraCharacteristics] Fail");
            return null;
        }

        CameraCharacteristics[] characteristicses = new CameraCharacteristics[1];
        mRequestHandler.obtainMessage(
                DeviceHandler.MSG_GET_CAMERA_CHARACTERISTICS, characteristicses).sendToTarget();

        if (waitDone(mRequestHandler)) {

            Log.d(mTAG, "[INT] [getCameraCharacteristics] Finish");
            return characteristicses[0];
        } else {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [getCameraCharacteristics] Finish (null)");
        return null;
    }

    /**
     * Start preview and recording.
     * @hide
     */
    public void startRecording() {

        Log.d(mTAG, "[INT] [startRecording] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [startRecording] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_START_RECORDING).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [startRecording] Finish");
    }

    /**
     * Stop preview and recording.
     * @hide
     */
    public void stopRecording() {
        Log.d(mTAG, "[INT] [stopRecording] Start");

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [stopRecording] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_STOP_RECORDING).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [stopRecording] Finish");
    }

    /**
     * If RJIL NW, replace output with picture data else drop camera data.
     * @hide
     */
    public void hideMe() {
        Log.d(mTAG, "[INT] [hideMe] Start");
        Log.d(mTAG, "[INT] [hideMe] Finish");
    }

    /**
     * If RJIL NW, resume the camera output else stop dropping camera data.
     * @hide
     */
    public void showMe() {
        Log.d(mTAG, "[INT] [showMe] Start");
        Log.d(mTAG, "[INT] [showMe] Finish");
    }

    /**
     * Set device orientation.
     * @hide
     */
    public void setDeviceOrientation(int degree) {

        Log.d(mTAG, "[INT] [setDeviceOrientation] Start");
        Log.d(mTAG, "[INT] [setDeviceOrientation] degree : " + degree);

        if (IsHandlerThreadUnavailable()) {
            Log.d(mTAG, "[INT] [setDeviceOrientation] Fail");
            return;
        }

        mRequestHandler.obtainMessage(DeviceHandler.MSG_DEVICE_ORIENTATION, degree).sendToTarget();

        boolean ret = waitDone(mRequestHandler);
        if (!ret) {
            mEventCallBack.onError();
        }

        Log.d(mTAG, "[INT] [setDeviceOrientation] Finish");
    }

    private void createRequestThreadAndHandler() {
        if (mRequestThread == null) {
            mRequestThread = new HandlerThread("VTSource-Request");
            mRequestThread.start();
            mRequestHandler = new DeviceHandler(mRequestThread.getLooper(),
                    mMode == VT_SRV_CALL_4G, mEventCallBack);
        }
    }

    private boolean IsHandlerThreadUnavailable() {
        if (mRequestThread == null || mIsWaitRelease) {
            Log.d(mTAG, "Thread = null:" + (mRequestThread == null) + ", mIsWaitRelease:" + mIsWaitRelease);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The handler used to process device operation.
     */
    private class DeviceHandler extends Handler {
        public static final int MSG_OPEN_CAMERA = 0;
        public static final int MSG_START_PREVIEW = 1;
        public static final int MSG_STOP_PREVIEW = 2;
        public static final int MSG_UPDATE_RECORD_SURFACE = 3;
        public static final int MSG_START_RECORDING = 4;
        public static final int MSG_STOP_RECORDING = 5;
        public static final int MSG_SUBMIT_REQUEST = 6;
        public static final int MSG_PERFORM_ZOOM = 7;
        public static final int MSG_GET_CAMERA_CHARACTERISTICS = 8;
        public static final int MSG_CLOSE_CAMERA = 9;
        public static final int MSG_RELEASE = 10;
        public static final int MSG_DEVICE_ORIENTATION = 11;

        private static final int MAX_RETRY_OPEN_CAMERA_COUNT = 5;

        private HandlerThread mRespondThread;
        private CameraDevice mCameraDevice;
        private String mCameraId;
        private int mRetryCount;
        private int mDeviceDegree;
        private ConditionVariable mDeviceConditionVariable = new ConditionVariable();
        private CameraCharacteristics mCameraCharacteristics;
        private EventCallback mEventCallBack;

        private boolean mNeedPortraitBuffer;
        private float mZoomValue = 1.0f;
        private CameraCaptureSession mCameraCaptureSession;
        private ConditionVariable mSessionConditionVariable = new ConditionVariable();
        private List<Surface> mSessionUsedSurfaceList = new ArrayList<>();
        private List<OutputConfiguration> mOutputConfigurations = new ArrayList<>();

        DeviceHandler(Looper looper, boolean needPortraitBuffer, EventCallback cb) {
            super(looper);
            mNeedPortraitBuffer = needPortraitBuffer;
            mRespondThread = new HandlerThread("VTSource-Respond");
            mRespondThread.start();
            mDeviceDegree = 0;
            mEventCallBack = cb;
        }

        @Override
        public void handleMessage(Message msg) {

            if (mRespondThread == null) {
                Log.w(mTAG, "[handleMessage] mRespondThread null, ignore message!!");
                return;
            }

            switch (msg.what) {
                case MSG_OPEN_CAMERA:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_OPEN_CAMERA");

                    String cameraId = (String) msg.obj;
                    if (mCameraDevice != null && mCameraDevice.getId().equals(cameraId)) {
                        Log.w(mTAG, "open existing camera, ignore open!!!");
                        return;
                    }
                    // close camera if camera is running, update camera characteristics
                    prepareForOpenCamera(cameraId);
                    mRetryCount = 0;
                    mDeviceConditionVariable.close();
                    doOpenCamera(mCameraId);
                    mDeviceConditionVariable.block();
                    break;

                case MSG_START_PREVIEW: {

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_START_PREVIEW");

                    Surface newSurface = (Surface) msg.obj;
                    if (mCameraDevice == null || newSurface == null || !newSurface.isValid()) {
                        Log.w(mTAG, "[HDR] [handleMessage] start preview with status error, device:"
                                + mCameraDevice + ", new surface:" + newSurface);
                        if (newSurface != null && newSurface.isValid()) {
                            Log.d(mTAG, "[HDR] [handleMessage] Camera closed, "
                                    + "store the surface for use later.");
                            mCachedPreviewSurface = newSurface;
                        }
                        return;
                    }

                    closeSession();
                    mCachedPreviewSurface = newSurface;
                    createSession();
                    break;
                }

                case MSG_SUBMIT_REQUEST:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_SUBMIT_REQUEST");

                    if (mCameraDevice == null || mCameraCaptureSession == null) {
                        Log.w(mTAG, "[HDR] [handleMessage] submitRepeatingRequest illegal state"
                                + ", ignore!");
                        return;
                    }
                    submitRepeatingRequest();
                    break;

                case MSG_STOP_PREVIEW:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_STOP_PREVIEW");

                    mCachedPreviewSurface = null;
                    closeSession();
                    createSession();
                    break;

                case MSG_UPDATE_RECORD_SURFACE: {

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_UPDATE_RECORD_SURFACE");

                    Surface newSurface = (Surface) msg.obj;
                    if (newSurface == null && mCachedRecordSurface == null) {
                        return;
                    }

                    mCachedRecordSurface = newSurface;
                    closeSession();
                    createSession();
                    break;
                }

                case MSG_START_RECORDING:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_START_RECORDING");

                    if (mCameraDevice == null ||
                            mCameraCaptureSession == null || mNeedRecordStream) {
                        Log.w(mTAG, "[HDR] [handleMessage] start recording status error, device:"
                                            + mCameraDevice
                                            + ", session:" + mCameraCaptureSession
                                            + ", record status:" + mNeedRecordStream);

                        mNeedRecordStream = true;
                        return;
                    }
                    mNeedRecordStream = true;
                    submitRepeatingRequest();
                    break;

                case MSG_STOP_RECORDING:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_STOP_RECORDING");

                    if (mNeedRecordStream) {
                        mNeedRecordStream = false;
                        closeSession();
                        createSession();
                    }
                    break;

                case MSG_PERFORM_ZOOM:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_PERFORM_ZOOM");

                    if (mCameraDevice == null || mCameraCaptureSession == null) {
                        Log.w(mTAG, "[HDR] [handleMessage] perform zoom with"
                                + " null device or session!!!");

                        return;
                    }
                    mZoomValue = (float) msg.obj;
                    submitRepeatingRequest();
                    break;

                case MSG_GET_CAMERA_CHARACTERISTICS:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_GET_CAMERA_CHARACTERISTICS");

                    CameraCharacteristics[] characteristicses = (CameraCharacteristics[]) msg.obj;
                    characteristicses[0] = mCameraCharacteristics;
                    break;

                case MSG_CLOSE_CAMERA:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_CLOSE_CAMERA");

                    // mNeedRecordStream = false;
                    mCameraCaptureSession = null;
                    mZoomValue = 1.0f;
                    doCloseCamera();
                    break;

                case MSG_RELEASE:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_RELEASE");

                    // mNeedRecordStream = false;
                    mCameraCaptureSession = null;
                    mZoomValue = 1.0f;
                    doCloseCamera();
                    mRespondThread.quitSafely();
                    break;

                case MSG_DEVICE_ORIENTATION:

                    Log.d(mTAG, "[HDR] [handleMessage] MSG_DEVICE_ORIENTATION");

                    if(mDeviceDegree != (int) msg.obj) {
                        Log.d(mTAG, "[HDR] [handleMessage] Change device orientation from "
                                + mDeviceDegree + "to " + (int) msg.obj);
                        mDeviceDegree = (int) msg.obj;
                    }
                    break;

                default:
                    Log.d(mTAG, "[HDR] [handleMessage] what:" + msg.what);
                    break;
            }
        }

        private void createSession() {

            Log.d(mTAG, "[HDR] [createSession] Start");

            if (mCameraDevice == null) {
                Log.w(mTAG, "[HDR] [createSession] mCameraDevice is null !!!");
                return;
            }
            boolean ret = prepareOutputConfiguration();
            if (mSessionUsedSurfaceList.size() <= 0 || !ret) {
                Log.w(mTAG, "[HDR] [createSession] Session surface list size <=0 "
                        + "or prepareOutputConfiguration fail");
                return;
            }
            mSessionConditionVariable.close();
            try {
                mCameraDevice.createCaptureSessionByOutputConfigurations(
                        mOutputConfigurations,
                        mSessionCallback,
                        new Handler(mRespondThread.getLooper()));
            } catch (Exception e) {

                Log.e(mTAG, "[HDR] [createSession] create preview session with exception:"
                        + e);
                mEventCallBack.onError();
                return;
            }
            mSessionConditionVariable.block();

            Log.d(mTAG, "[HDR] [createSession] Finish");

        }

        private int getSessionRotationIndex(int rotation) {

            Log.d(mTAG, "[HDR] [getSessionRotationIndex] rotation = "+ rotation);

            switch (rotation) {
                case 0:
                    return 0;
                case 90:
                    return 1;
                case 180:
                    return 2;
                case 270:
                    return 3;
                default:
                    return 0;
            }
        }

        private void closeSession() {

            Log.d(mTAG, "[HDR] [closeSession] Start");

            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;

            } else {
                Log.d(mTAG, "[HDR] [closeSession] mCameraCaptureSession = NULL");
            }

            Log.d(mTAG, "[HDR] [closeSession] Finish");
        }

        private Rect calculateCropRegionByZoomValue(float zoomValue) {

            Log.d(mTAG, "[HDR] [calculateCropRegionByZoomValue] Start");
            Log.d(mTAG, "[HDR] [calculateCropRegionByZoomValue] zoomValue = "+ zoomValue);

            PointF center = new PointF(0.5f, 0.5f); // center only crop
            float maxZoom = mCameraCharacteristics.get(
                    CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            Rect sensorArraySize = mCameraCharacteristics.get(
                    CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            Log.d(mTAG, "[HDR] [calculateCropRegionByZoomValue] Finish");

            return getCropRegionForZoom(
                    zoomValue,
                    center,
                    maxZoom,
                    sensorArraySize);
        }

        private Range calculateAeFpsRange() {

            Log.d(mTAG, "[HDR] [calculateAeFpsRange] Start");

            Range<Integer>[] availableFpsRange = mCameraCharacteristics.get(
                    CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            // Pick FPS range with highest max value, tiebreak on lower min value
            // Expect to set [5,30]
            Range<Integer> bestRange = availableFpsRange[0];
            for (Range<Integer> r : availableFpsRange) {
                if (bestRange.getUpper() < r.getUpper()) {
                    bestRange = r;
                } else if (bestRange.getUpper() == r.getUpper() &&
                    bestRange.getLower() > r.getLower()) {
                    bestRange = r;
                }
            }

            Log.d(mTAG, "[HDR] [calculateAeFpsRange] Range = [" +
                    bestRange.getLower() + ", " + bestRange.getUpper() + "]");
            Log.d(mTAG, "[HDR] [calculateAeFpsRange] Finish");

            return bestRange;
        }

        private void submitRepeatingRequest() {

            Log.d(mTAG, "[HDR] [submitRepeatingRequest] Start");

            if (mCameraDevice == null || mCameraCaptureSession == null) {
                Log.w(mTAG, "submitRepeatingRequest illegal state, ignore!");
                return;
            }
            boolean hasAddTarget = false;
            Rect cropRegion = calculateCropRegionByZoomValue(mZoomValue);
            try {
                Builder builder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                builder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion);

                Range aeFps = calculateAeFpsRange();
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, aeFps);

                Log.d(mTAG, "[HDR] [submitRepeatingRequest] submitRepeatingRequest "
                        + "mNeedRecordStream = " + mNeedRecordStream);
                Log.d(mTAG, "[HDR] [submitRepeatingRequest] submitRepeatingRequest "
                        + "mCachedRecordSurface = " + mCachedRecordSurface);
                if (mNeedRecordStream && mCachedRecordSurface != null &&
                        mSessionUsedSurfaceList.contains(mCachedRecordSurface)) {
                    builder.addTarget(mCachedRecordSurface);
                    hasAddTarget = true;
                }
                Log.d(mTAG, "[HDR] [submitRepeatingRequest] submitRepeatingRequest "
                        + "mCachedPreviewSurface = " + mCachedPreviewSurface);
                if (mCachedPreviewSurface != null &&
                        mSessionUsedSurfaceList.contains(mCachedPreviewSurface)) {
                    builder.addTarget(mCachedPreviewSurface);
                    hasAddTarget = true;
                }

                if (hasAddTarget) {
                    mCameraCaptureSession.setRepeatingRequest(
                            builder.build(),
                            null,
                            new Handler(mRespondThread.getLooper()));
                }
            } catch (Exception e) {

                Log.d(mTAG, "[HDR] [submitRepeatingRequest] exception: " + e);
                e.printStackTrace();
                mEventCallBack.onError();
            }

            Log.d(mTAG, "[HDR] [submitRepeatingRequest] Finish");
        }

        private void prepareForOpenCamera(String cameraId) {

            Log.d(mTAG, "[HDR] [prepareForOpenCamera] Start");
            Log.d(mTAG, "[HDR] [prepareForOpenCamera] cameraId = "+ cameraId);

            if (mCameraId != null && !mCameraId.equals(cameraId)) {
                closeSession();
                doCloseCamera();
            }
            mCameraId = cameraId;
            try {
                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            } catch (Exception e) {
                Log.e(mTAG, "[HDR] [prepareForOpenCamera] before open camera "
                        + "getCameraCharacteristics access exception: " + e);
                mEventCallBack.onError();
            }

            Log.d(mTAG, "[HDR] [prepareForOpenCamera] Finish");
        }

        private void doCloseCamera() {

            Log.d(mTAG, "[HDR] [doCloseCamera] Start");

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;

            } else {
                Log.d(mTAG, "[HDR] [doCloseCamera] mCameraDevice = NULL");
            }

            Log.d(mTAG, "[HDR] [doCloseCamera] Finish");
        }

        private boolean prepareOutputConfiguration() {

            Log.d(mTAG, "[HDR] [prepareOutputConfiguration] Start");

            mSessionUsedSurfaceList.clear();
            mOutputConfigurations.clear();

            if (mCachedPreviewSurface != null) {

                Log.d(mTAG, "[HDR] [prepareOutputConfiguration][Preview]");

                mSessionUsedSurfaceList.add(mCachedPreviewSurface);

                /* Not rotate preview by camera HAL3, current AOSP codes have FOV issue
                int bufferRotation = 0;
                if (mNeedPortraitBuffer) {
                    //rotation buffer to portrait
                    bufferRotation = getCameraRotation(mDeviceDegree, mCameraCharacteristics);
                }

                int rotationIndex = getSessionRotationIndex(bufferRotation);

                Log.d(mTAG, "[HDR] [prepareOutputConfiguration] prepareOutputConfiguration, "
                        + "set bufferRotation:" + bufferRotation
                        + ", rotationIndex: " + rotationIndex);
                */

                try {
                    mOutputConfigurations.add(new OutputConfiguration(mCachedPreviewSurface));

                } catch (Exception ex) {
                    Log.e(mTAG, "[HDR] [prepareOutputConfiguration][Preview] "
                            + "new OutputConfiguration with exception: " + ex);
                    mSessionUsedSurfaceList.remove(mCachedPreviewSurface);
                    mCachedPreviewSurface = null;
                    mEventCallBack.onError();

                    Log.d(mTAG, "[HDR] [prepareOutputConfiguration] Finish");

                    return false;
                }
            }

            if (mCachedRecordSurface != null) {

                Log.d(mTAG, "[HDR] [prepareOutputConfiguration][Record]");

                mSessionUsedSurfaceList.add(mCachedRecordSurface);
                try {
                    mOutputConfigurations.add(new OutputConfiguration(mCachedRecordSurface));

                } catch (Exception ex) {
                    Log.e(mTAG, "[HDR] [prepareOutputConfiguration][Record] "
                            + "new OutputConfiguration with exception: " + ex);
                    mSessionUsedSurfaceList.remove(mCachedRecordSurface);
                    mCachedRecordSurface = null;
                    mEventCallBack.onError();

                    Log.d(mTAG, "[HDR] [prepareOutputConfiguration] Finish");

                    return false;
                }
            }

            Log.d(mTAG, "[HDR] [prepareOutputConfiguration] Finish");

            return true;
        }

        private void doOpenCamera(String cameraId) {

            Log.d(mTAG, "[HDR] [doOpenCamera] Start");

            try {
                mCameraManager.openCamera(cameraId,
                        mDeviceCallback,
                        new Handler(mRespondThread.getLooper()));
            } catch (Exception e) {
                Log.i(mTAG, "[HDR] [doOpenCamera] open camera with access exception:" + e);
                mEventCallBack.onError();
            }

            Log.d(mTAG, "[HDR] [doOpenCamera] Finish");
        }

        private int getCameraRotation(int degrees, CameraCharacteristics characteristics) {

            Log.d(mTAG, "[HDR] [getCameraRotation] Start");

            int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Log.d(mTAG, "[HDR] [getCameraRotation] degrees: " + degrees
                    + ", facing: " + facing + ", orientation: " + orientation);

            int result;
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                switch (degrees) {
                    case 0:
                        result = 0;
                        break;
                    case 90:
                        result = 270;
                        break;
                    case 180:
                        result = 180;
                        break;
                    case 270:
                        result = 90;
                        break;
                    default:
                        result = 0;
                        break;
                }

                /* Original logic
                result = (orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
                result = (result + 270) % 360;
                */
            } else { // back-facing
                switch (degrees) {
                    case 0:
                        result = 0;
                        break;
                    case 90:
                        result = 90;
                        break;
                    case 180:
                        result = 180;
                        break;
                    case 270:
                        result = 270;
                        break;
                    default:
                        result = 0;
                        break;
                }

                /* Original logic
                result = (orientation - degrees + 360) % 360;

                if (degrees == 0 || degrees == 180) {
                    result = (result + 270) % 360;
                } else {
                    result = (result + 90) % 360;
                }
                */
            }
            Log.d(mTAG, "[HDR] [getCameraRotation] Final angle = "+ result);
            Log.d(mTAG, "[HDR] [getCameraRotation] Fisnish");

            return result;
        }

        private Rect getCropRegionForZoom(float zoomFactor, final PointF center,
                                                final float maxZoom, final Rect activeArray) {

            Log.d(mTAG, "[HDR] [getCropRegionForZoom] Start");
            Log.d(mTAG, "[HDR] [getCropRegionForZoom] zoomFactor = " + zoomFactor
                    + ", center = " + center);
            Log.d(mTAG, "[HDR] [getCropRegionForZoom] maxZoom = " + maxZoom
                    + ", activeArray = " + activeArray);

            if (zoomFactor < 1.0) {
                throw new IllegalArgumentException(
                        "zoom factor " + zoomFactor + " should be >= 1.0");
            }
            if (center.x > 1.0 || center.x < 0) {
                throw new IllegalArgumentException("center.x " + center.x
                        + " should be in range of [0, 1.0]");
            }
            if (center.y > 1.0 || center.y < 0) {
                throw new IllegalArgumentException("center.y " + center.y
                        + " should be in range of [0, 1.0]");
            }
            if (maxZoom < 1.0) {
                throw new IllegalArgumentException(
                        "max zoom factor " + maxZoom + " should be >= 1.0");
            }
            if (activeArray == null) {
                throw new IllegalArgumentException("activeArray must not be null");
            }

            float minCenterLength = Math.min(Math.min(center.x, 1.0f - center.x),
                    Math.min(center.y, 1.0f - center.y));
            float minEffectiveZoom =  0.5f / minCenterLength;
            if (minEffectiveZoom > maxZoom) {
                throw new IllegalArgumentException("Requested center " + center.toString() +
                        " has minimal zoomable factor " + minEffectiveZoom + ", which exceeds max"
                        + " zoom factor " + maxZoom);
            }

            if (zoomFactor < minEffectiveZoom) {
                Log.w(mTAG, "Requested zoomFactor " + zoomFactor + " > minimal zoomable factor "
                        + minEffectiveZoom + ". It will be overwritten by " + minEffectiveZoom);
                zoomFactor = minEffectiveZoom;
            }

            int cropCenterX = (int) (activeArray.width() * center.x);
            int cropCenterY = (int) (activeArray.height() * center.y);
            int cropWidth = (int) (activeArray.width() / zoomFactor);
            int cropHeight = (int) (activeArray.height() / zoomFactor);

            Log.d(mTAG, "[HDR] [getCropRegionForZoom] Finish");

            return new Rect(
                /*left*/cropCenterX - cropWidth / 2,
                /*top*/cropCenterY - cropHeight / 2,
                /*right*/ cropCenterX + cropWidth / 2 - 1,
                /*bottom*/cropCenterY + cropHeight / 2 - 1);
        }

        private CameraDevice.StateCallback mDeviceCallback = new CameraDevice.StateCallback() {
            @Override
            public void onError(CameraDevice cameraDevice, int error) {

                Log.e(mTAG, "[HDR] [onError] error:" + error);
                if (mRetryCount < MAX_RETRY_OPEN_CAMERA_COUNT &&
                       (error == CameraDevice.StateCallback.ERROR_CAMERA_IN_USE ||
                        error == CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE)) {
                    mRetryCount++;
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    doOpenCamera(mCameraId);
                    return;
                }
                mDeviceConditionVariable.open();
                mEventCallBack.onError();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                Log.e(mTAG, "[HDR] [onDisconnected] cameraDevice:" + cameraDevice);
                mCameraDevice = null;
            }

            @Override
            public void onOpened(CameraDevice cameraDevice) {
                Log.d(mTAG, "[HDR] [onOpened]");
                mCameraDevice = cameraDevice;
                if (mCachedPreviewSurface != null) {
                    obtainMessage(MSG_START_PREVIEW, mCachedPreviewSurface).sendToTarget();
                }
                mDeviceConditionVariable.open();
            }

            @Override
            public void onClosed(CameraDevice cameraDevice) {
                Log.d(mTAG, "[HDR] [onClosed]");
                super.onClosed(cameraDevice);
            }
        };
        private StateCallback mSessionCallback = new StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                Log.d(mTAG, "[onConfigured]");
                mCameraCaptureSession = cameraCaptureSession;
                obtainMessage(MSG_SUBMIT_REQUEST).sendToTarget();
                mSessionConditionVariable.open();
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                Log.d(mTAG, "[onConfigureFailed]");
                mSessionConditionVariable.open();
                mEventCallBack.onError();
            }
        };
    }

    /**
     * Wait for the message is processed by post a runnable.
     * @param handler the post notify wait done handler.
     * @return whether wait done success.
     */
    private boolean waitDone(Handler handler) {
        if (handler == null) {
            return false;
        }
        final ConditionVariable waitDoneCondition = new ConditionVariable();
        final Runnable unlockRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (waitDoneCondition) {
                    waitDoneCondition.open();
                }
            }
        };
        synchronized (waitDoneCondition) {
            if (handler.post(unlockRunnable)) {
                boolean successed = waitDoneCondition.block(TIME_OUT_MS);
                Log.d(mTAG, "[waitDone] wait successed " + successed);
                return successed;
            }
        }
        return true;
    }
}
