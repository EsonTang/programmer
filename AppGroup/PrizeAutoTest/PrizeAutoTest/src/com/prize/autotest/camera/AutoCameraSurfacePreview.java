package com.prize.autotest.camera;

import java.io.IOException;
import java.util.List;

import com.prize.autotest.AutoConstant;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AutoCameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "zwl_camera";

	private SurfaceHolder mHolder;
	private Camera mCamera;
	Camera.Parameters params;
	private int mJpegRotation = -1;

	public static int cameraPosition = 0;// camera id;

	public AutoCameraSurfacePreview(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		openBackCamera(holder);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseCamera();
	}

	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mHolder = null;
			callBack.onResultCallback(AutoConstant.RESULT_SUCCUSS);
		} else {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
			AutoResultUtil.setCloseCameraSuccess(false);
		}
	}

	/**
	 * Switch Camera
	 */
	public void switchCamera() {
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();

		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					openBackCamera(mHolder);
					break;
				}
			} else {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					openFrontCamera(mHolder);
					break;
				}
			}
		}
	}

	/** open camera start */
	/**
	 * Open Back Camera
	 */
	public void openBackCamera() {
		openBackCamera(mHolder);
	}

	/**
	 * Open front Camera
	 */
	public void openFrontCamera() {
		openFrontCamera(mHolder);
	}

	/**
	 * Open Back Camera
	 */
	private void openBackCamera(SurfaceHolder holder) {
		openCamera(holder, Camera.CameraInfo.CAMERA_FACING_BACK);
		cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
	}

	/**
	 * Open front Camera
	 */
	private void openFrontCamera(SurfaceHolder holder) {
		openCamera(holder, Camera.CameraInfo.CAMERA_FACING_FRONT);
		cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	private void openCamera(SurfaceHolder holder, int index) {
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			mCamera = Camera.open(index);
			mCamera.setDisplayOrientation(90);// set Preview Rotation
			params = mCamera.getParameters();
			setJpegRotation(index, 0);// set picture Rotation
			List<Camera.Size> sizeList = params.getSupportedPictureSizes();
			params.setPictureSize(sizeList.get(sizeList.size() - 1).width, sizeList.get(sizeList.size() - 1).height);
			mCamera.setParameters(params);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			if(!AutoResultUtil.isShutter){
				callBack.onResultCallback(AutoConstant.RESULT_SUCCUSS);
			}
		} catch (IOException e) {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
			AutoResultUtil.setOpenCameraSuccess(false);
			Log.w(TAG, "Error setting camera preview: " + e.getMessage());
		} catch (Exception e) {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
			AutoResultUtil.setOpenCameraSuccess(false);
			Log.w(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}
	/** open camera end */

	/**
	 * shut camera take picture
	 * 
	 * @param imageCallback
	 */
	public void takePicture(PictureCallback imageCallback) {
		if (mCamera != null) {
			Log.w(TAG, "takePicture() is called");
			if(cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK){
				makeAutoFocus();
			}
			mCamera.takePicture(null, null, imageCallback);
		}
	}

	/** set ZOOM start */
	public void setMaxZoom() {
		if (mCamera == null || params == null) {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
			return;
		}
		int zoomValue = params.getZoom();
		Log.w(TAG, "setMaxZoom() is curZoom = " + params.getZoom() + ", params.getMaxZoom() = " + params.getMaxZoom());
		if (mCamera.getParameters().isZoomSupported() && mCamera.getParameters().isSmoothZoomSupported()) {
			int MAX = params.getMaxZoom();
			if (MAX == 0)
				return;
			params.setZoom(MAX);
			mCamera.setParameters(params);
			mCamera.startSmoothZoom(zoomValue);
			if(!AutoResultUtil.isShutter){
				callBack.onResultCallback(AutoConstant.RESULT_SUCCUSS);
			}
		} 
		Log.w(TAG, "setMaxZoom() after is curZoom = " + params.getZoom());
	}

	public void setMinZoom() {
		if (mCamera == null || params == null) {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
			return;
		}
		int zoomValue = params.getZoom();
		Log.w(TAG, "setMaxZoom() is curZoom = " + params.getZoom());
		if (mCamera.getParameters().isZoomSupported() && mCamera.getParameters().isSmoothZoomSupported()) {
			params.setZoom(0);
			mCamera.setParameters(params);
			mCamera.startSmoothZoom(zoomValue);
			if(!AutoResultUtil.isShutter){
				callBack.onResultCallback(AutoConstant.RESULT_SUCCUSS);
			}
		}
		Log.w(TAG, "setMaxZoom() after is curZoom = " + params.getZoom());
	}

	/***
	 * set Zoom
	 * 
	 * @param outorin
	 *            if param is 0, zoom out ; if param is 1 , zoom in
	 */
	public void setZoom(int outorin) {
		if (mCamera == null || params == null) {
			return;
		}
		int zoomValue = params.getZoom();
		Log.w(TAG, "setMaxZoom() is curZoom = " + params.getZoom() + ", params.getMaxZoom() = " + params.getMaxZoom());
		if (mCamera.getParameters().isZoomSupported() && mCamera.getParameters().isSmoothZoomSupported()) {
			int MAX = params.getMaxZoom();
			if (MAX == 0)
				return;
			if (outorin == 0) {
				if (zoomValue > 0) {
					zoomValue -= 1;
				}
			} else if (outorin == 1) {
				if (zoomValue < MAX) {
					zoomValue += 1;
				}
			} else {
				Log.w(TAG, "setZoom() is out of range");
				return;
			}
			params.setZoom(zoomValue);
			mCamera.setParameters(params);
			mCamera.startSmoothZoom(zoomValue);
		}
		Log.w(TAG, "setMaxZoom() after is curZoom = " + params.getZoom());
	}
	/** set ZOOM end */

	/** set auto focus start */
	public void makeAutoFocus() {
		if (mCamera != null) {
			Log.w(TAG, "makeAutoFocus() is called");
			mCamera.autoFocus(new AutoFocusCallbackImpl());
		} else {
			callBack.onResultCallback(AutoConstant.RESULT_FAIL);
		}
	}

	private class AutoFocusCallbackImpl implements AutoFocusCallback {
		public void onAutoFocus(boolean success, Camera camera) {
			Log.w(TAG, "AutoFocusCallback() is callback ...");
			if (success) { 
				if(!AutoResultUtil.isShutter){
					callBack.onResultCallback(AutoConstant.RESULT_SUCCUSS);
				}
				AutoResultUtil.setAutoFocusSuccess(true);
				Log.w(TAG, "AutoFocusCallback() is success ...");
			} else {
				callBack.onResultCallback(AutoConstant.RESULT_FAIL);
				AutoResultUtil.setAutoFocusSuccess(false);
				Log.w(TAG, "AutoFocusCallback() is fail ...");
			}
		}
	}
	/** set auto focus end */

	/** set picture Rotation start */
	public void setJpegRotation(int mCameraId, int orientation) {
		mJpegRotation = -1;
		mJpegRotation = getJpegRotation(mCameraId, orientation);
		params.setRotation(mJpegRotation);
		Log.w(TAG, "setRotationToParameters() mCameraId=" + mCameraId + ", mOrientation=" + orientation
				+ ", jpegRotation = " + mJpegRotation);
	}

	public int getJpegRotation(int cameraId, int orientation) {
		// See android.hardware.Camera.Parameters.setRotation for
		// documentation.
		int rotation = 0;
		CameraInfo info = getCameraInfo()[cameraId];
		if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				rotation = (info.orientation - orientation + 360) % 360;
			} else { // back-facing camera
				rotation = (info.orientation + orientation) % 360;
			}
		} else {
			// Get the right original orientation
			rotation = info.orientation;
		}
		return rotation;
	}

	private int mNumberOfCameras;
	private CameraInfo[] mInfo;

	public CameraInfo[] getCameraInfo() {
		mNumberOfCameras = Camera.getNumberOfCameras();
		mInfo = new CameraInfo[mNumberOfCameras];
		Log.w(TAG, "mNumberOfCameras = " + mNumberOfCameras);
		for (int i = 0; i < mNumberOfCameras; i++) {
			mInfo[i] = new CameraInfo();
			Camera.getCameraInfo(i, mInfo[i]);
			Log.w(TAG, "camerainfo,mInfo[" + i + "]= " + mInfo[i]);
		}
		Log.w(TAG, "getCameraInfo,size = " + mInfo.length);
		return mInfo;
	}

	/** set picture Rotation end */
	ResultCallback callBack = null;

	public void setResultCallback(ResultCallback callback) {
		callBack = callback;
	}

	public interface ResultCallback {
		public void onResultCallback(String result);
	}
}