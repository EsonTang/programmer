package com.prize.runoldtest.camera;

import java.io.IOException;
import java.util.List;

import com.prize.runoldtest.R;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.ResultUtil;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

@SuppressWarnings("deprecation")
public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "zwl_camera";
	
	private MediaRecorder mRecorder;
	private SurfaceHolder mHolder;
	private Camera mCamera;
	Camera.Parameters params;
	private int mJpegRotation = -1;
	
	private MediaPlayer mMediaPlayer = null;

	public static int cameraPosition = 0;// camera id;
	
	public Context mContext;

	public CameraSurfacePreview(Context context) {
		super(context);
		mContext = context;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.w(TAG, "surfaceCreated() is called");
		// Open the Camera in preview mode
		openBackCamera(holder);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.w(TAG, "surfaceChanged() is called");
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseCamera();
		mHolder = null;
		Log.w(TAG, "surfaceDestroyed() is called");
	}
	
	/**
	 * Start Recorder Video
	 * */
	public void startRecorderVideo() {
		mCamera.unlock();
		mRecorder = new MediaRecorder();
		mRecorder.reset();
		mRecorder.setCamera(mCamera);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置格式
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
		mRecorder.setVideoEncodingBitRate(5 * 1920 * 1080);// 设置视频编码帧率
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOrientationHint(90);// 不改变preview,只对录下来的是视频起作用(顺时针旋转90度)
		mRecorder.setVideoFrameRate(30);// 每秒3帧
		// 设置保存路径
		mRecorder.setOutputFile("/mnt/sdcard/prizeoldtest/uu"
				+ System.currentTimeMillis() + ".mp4");
		mRecorder.setVideoSize(640, 480);
		mRecorder.setPreviewDisplay(mHolder.getSurface());
		try {
			mRecorder.prepare();
			mRecorder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stop Recorder Video
	 * */
	public void stopRecorderVideo() {
		if (mRecorder != null) {
			mRecorder.setPreviewDisplay(null);
			mRecorder.stop();
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			mCamera.lock();
			releaseCamera();
		}
	}
	

	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	/**
	 * Switch Camera
	 */
	public void switchCamera() {
		// 切换前后摄像头
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
																					// CAMERA_FACING_BACK后置
					openBackCamera(mHolder);
					break;
				}
			} else {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
																				// CAMERA_FACING_BACK后置
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
		if(mHolder == null){
			LogUtil.e("openBackCamera()------mHolder == null --------");
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		openBackCamera(mHolder);
	}

	/**
	 * Open front Camera
	 */
	public void openFrontCamera() {
		if(mHolder == null){
			LogUtil.e("openBackCamera()------mHolder == null --------");
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
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
			params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			mCamera.setParameters(params);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			if(!ResultUtil.isShutter){
				callBack.onResultCallback(Const.RESULT_SUCCUSS);
			}
		} catch (IOException e) {
			callBack.onResultCallback(Const.RESULT_FAIL);
			ResultUtil.setOpenCameraSuccess(false);
			Log.w(TAG, "Error setting camera preview: " + e.getMessage());
		} catch (Exception e) {
			callBack.onResultCallback(Const.RESULT_FAIL);
			ResultUtil.setOpenCameraSuccess(false);
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
			mPlayAudio();
		}
	}

	/** set ZOOM start */
	public void setMaxZoom() {
		if (mCamera == null || params == null) {
			callBack.onResultCallback(Const.RESULT_FAIL);
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
			if(!ResultUtil.isShutter){
				callBack.onResultCallback(Const.RESULT_SUCCUSS);
			}
		} 
		Log.w(TAG, "setMaxZoom() after is curZoom = " + params.getZoom());
	}

	public void setMinZoom() {
		if (mCamera == null || params == null) {
			callBack.onResultCallback(Const.RESULT_FAIL);
			return;
		}
		int zoomValue = params.getZoom();
		Log.w(TAG, "setMaxZoom() is curZoom = " + params.getZoom());
		if (mCamera.getParameters().isZoomSupported() && mCamera.getParameters().isSmoothZoomSupported()) {
			params.setZoom(0);
			mCamera.setParameters(params);
			mCamera.startSmoothZoom(zoomValue);
			if(!ResultUtil.isShutter){
				callBack.onResultCallback(Const.RESULT_SUCCUSS);
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
			if (outorin == Const.SET_ZOOM_OUT) {
				if (zoomValue > 0) {
					zoomValue -= 1;
				}
			} else if (outorin == Const.SET_ZOOM_IN) {
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
			callBack.onResultCallback(Const.RESULT_FAIL);
		}
	}

	private class AutoFocusCallbackImpl implements AutoFocusCallback {
		public void onAutoFocus(boolean success, Camera camera) {
			Log.w(TAG, "AutoFocusCallback() is callback ...");
			if (success) { // 成功
				if(!ResultUtil.isShutter){
					callBack.onResultCallback(Const.RESULT_SUCCUSS);
				}
				ResultUtil.setAutoFocusSuccess(true);
				Log.w(TAG, "AutoFocusCallback() is success ...");
			} else {
				callBack.onResultCallback(Const.RESULT_FAIL);
				ResultUtil.setAutoFocusSuccess(false);
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
	
	public void mPlayAudio() {
		mMediaPlayer = MediaPlayer.create(mContext, R.raw.camera_shutter);
		mMediaPlayer.setLooping(false);
		mMediaPlayer.start();
	}
}