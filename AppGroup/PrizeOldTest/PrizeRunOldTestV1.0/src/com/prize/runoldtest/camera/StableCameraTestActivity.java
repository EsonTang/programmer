package com.prize.runoldtest.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.prize.runoldtest.R;
import com.prize.runoldtest.camera.CameraSurfacePreview.ResultCallback;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.ResultUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint({ "SdCardPath", "HandlerLeak", "Wakelock" })
@SuppressWarnings("deprecation")
public class StableCameraTestActivity extends Activity implements
		OnClickListener, PictureCallback, ResultCallback {
	private static final String ACTION_UI = "com.prize.cameratest.ACTION_UI";
	private PowerManager.WakeLock wakeLock = null;
	private CameraSurfacePreview mCameraSurPreview = null;
	private FrameLayout preview;
	public static int cameraPosition;// camera id;
	private int mCameraTestCount = 0;
	private int mBackCameraCount = 0;
	private int mFrontCameraCount = 0;
	private int mSwitchCameraCount = 0;

	private boolean mOpenBackCamera = false;
	private boolean mBackCameraShut = false;
	private boolean mSetBackMaxZoom = false;
	private boolean mSetBackMinZoom = false;
	private boolean mRecorderVideo = false;
	private boolean mStopRecorderVideo = false;
	private boolean mOpenFrontCamera = false;
	private boolean mFrontCameraShut = false;
	private boolean mSwitchCamera = false;

	private final static int OPENBACKCAMERA = 1;
	private final static int BACKCAMERASHUT = 2;
	private final static int SETBACKMAXZOOM = 3;
	private final static int SETBACKMINZOOM = 4;
	private final static int RECORDERVIDEO = 5;
	private final static int STOPRECORDERVIDEO = 6;
	private final static int OPENFRONTCAMERA = 7;
	private final static int FRONTCAMERASHUT = 8;
	private final static int SWITCHCAMERA = 9;

	public String SDPATH = "/mnt/sdcard/prizeoldtest/";

	private BroadcastReceiver mbr = null;
	private String cmdOrder = null;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case OPENBACKCAMERA:
				mCameraSurPreview.openBackCamera();
				mHandler.postDelayed(pRunnable, 2500);
				mOpenBackCamera = false;
				mBackCameraShut = true;
				LogUtil.e("CameraTestActivity-----------File("
						+ getOutputMediaFile().toString() + ")---------");
				LogUtil.e("CameraTestActivity-----------OPENBACKCAMERA("
						+ mBackCameraCount + ")---------");
				break;
			case BACKCAMERASHUT:
				mCameraSurPreview.takePicture(StableCameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 3000);
				mBackCameraCount++;
				cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
				mBackCameraShut = false;
				mSetBackMinZoom = true;
				LogUtil.e("CameraTestActivity-----------BACKCAMERASHUT("
						+ mBackCameraCount + ")---------");
				break;
			case SETBACKMINZOOM:
				mCameraSurPreview.openBackCamera();
				mCameraSurPreview.setMinZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 2000);
				mSetBackMinZoom = false;
				mSetBackMaxZoom = true;
				LogUtil.e("CameraTestActivity-----------SETBACKMINZOOM("
						+ mBackCameraCount + ")---------");
				break;
			case SETBACKMAXZOOM:
				mCameraSurPreview.setMaxZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 2000);
				mSetBackMaxZoom = false;
				mRecorderVideo = true;
				LogUtil.e("CameraTestActivity-----------SETBACKMAXZOOM("
						+ mBackCameraCount + ")---------");
				break;
			case RECORDERVIDEO:
				mCameraSurPreview.startRecorderVideo();
				mHandler.postDelayed(pRunnable, 6000);
				mRecorderVideo = false;
				mStopRecorderVideo = true;
				LogUtil.e("CameraTestActivity-----------startRecorderVideo("
						+ mBackCameraCount + ")---------");
				break;
			case STOPRECORDERVIDEO:
				mCameraSurPreview.stopRecorderVideo();
				mHandler.postDelayed(pRunnable, 2000);
				mStopRecorderVideo = false;
				mOpenFrontCamera = true;
				LogUtil.e("CameraTestActivity-----------StopRecorderVideo("
						+ mBackCameraCount + ")---------");
				break;
			case OPENFRONTCAMERA:
				mCameraSurPreview.openFrontCamera();
				mHandler.postDelayed(pRunnable, 2000);
				mOpenFrontCamera = false;
				mFrontCameraShut = true;
				LogUtil.e("CameraTestActivity-----------OPENFRONTCAMERA("
						+ mFrontCameraCount + ")---------");
				break;
			case FRONTCAMERASHUT:
				mCameraSurPreview.takePicture(StableCameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 2500);
				mFrontCameraCount++;
				cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
				mFrontCameraShut = false;
				mSwitchCamera = true;
				LogUtil.e("CameraTestActivity-----------FRONTCAMERASHUT("
						+ mFrontCameraCount + ")---------");
				break;
			case SWITCHCAMERA:
				mCameraSurPreview.switchCamera();
				mSwitchCameraCount++;
				mHandler.postDelayed(pRunnable, 3000);
				mSwitchCamera = false;
				mOpenBackCamera = true;
				LogUtil.e("CameraTestActivity-----------SWITCHCAMERA("
						+ mSwitchCameraCount + ")---------");
				if (mSwitchCameraCount >= 5) {
					if (mCameraTestCount > 0) {
						mCameraTestCount--;
						mSwitchCameraCount = 0;
						LogUtil.e("CameraTestActivity-----------mCameraTestCount = "
								+ mCameraTestCount + "---------");
					} else {
						LogUtil.e("KEYCODE_BACKFlagCam");
						DataUtil.FlagEmmc_stable = true;
						StableCameraTestActivity.this.finish();
					}
				}
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableCameraTestActivity");
		setContentView(R.layout.activity_stable_camera);
		DataUtil.addDestoryActivity(StableCameraTestActivity.this,
				"StableCameraTestActivity");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
				"StableCameraTestActivity");
		if (wakeLock == null || !wakeLock.isHeld()) {
			wakeLock.acquire();
		}
		this.mbr = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UI);
		registerReceiver(this.mbr, filter);
		preview = (FrameLayout) findViewById(R.id.camera_recorder);
		mCameraSurPreview = new CameraSurfacePreview(this);
		mCameraSurPreview.setResultCallback(this);
		preview.addView(mCameraSurPreview);
		mOpenBackCamera = true;
		mHandler.post(pRunnable);
		Intent intent = getIntent();
		mCameraTestCount = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
		LogUtil.e("StableCameraTestActivity-----test_counts = "
				+ mCameraTestCount);
	}

	Runnable pRunnable = new Runnable() {
		public void run() {
			if (mOpenBackCamera) {
				mHandler.sendEmptyMessage(OPENBACKCAMERA);
				LogUtil.e("CameraTestActivity-----------mBackCameraShut= "
						+ mBackCameraShut + "---------");
			}
			if (mBackCameraShut) {
				mHandler.sendEmptyMessage(BACKCAMERASHUT);
				LogUtil.e("CameraTestActivity-----------mBackCameraShut= "
						+ mBackCameraShut + "---------");
			}
			if (mSetBackMaxZoom) {
				mHandler.sendEmptyMessage(SETBACKMAXZOOM);
				LogUtil.e("CameraTestActivity-----------mSetBackMaxZoom= "
						+ mSetBackMaxZoom + "---------");
			}
			if (mSetBackMinZoom) {
				mHandler.sendEmptyMessage(SETBACKMINZOOM);
				LogUtil.e("CameraTestActivity-----------mSetBackMinZoom= "
						+ mSetBackMinZoom + "---------");
			}
			if (mRecorderVideo) {
				mHandler.sendEmptyMessage(RECORDERVIDEO);
				LogUtil.e("CameraTestActivity-----------mRecorderVideo= "
						+ mRecorderVideo + "---------");
			}
			if (mStopRecorderVideo) {
				mHandler.sendEmptyMessage(STOPRECORDERVIDEO);
				LogUtil.e("CameraTestActivity-----------mStopRecorderVideo= "
						+ mStopRecorderVideo + "---------");
			}
			if (mOpenFrontCamera) {
				mHandler.sendEmptyMessage(OPENFRONTCAMERA);
				LogUtil.e("CameraTestActivity-----------mOpenFrontCamera= "
						+ mOpenFrontCamera + "---------");
			}
			if (mFrontCameraShut) {
				mHandler.sendEmptyMessage(FRONTCAMERASHUT);
				LogUtil.e("CameraTestActivity-----------mFrontCameraShut= "
						+ mFrontCameraShut + "---------");
			}
			if (mSwitchCamera) {
				mHandler.sendEmptyMessage(SWITCHCAMERA);
				LogUtil.e("CameraTestActivity-----------mSwitchCamera= "
						+ mSwitchCamera + "---------");
			}
		}
	};

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// save the picture to sdcard
		File pictureFile = getOutputMediaFile();
		if (pictureFile == null) {
			LogUtil.e("Error creating media file, check storage permissions: ");
			ResultUtil.setTakePictureSuccess(false);
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(this,
					"Image has been saved to " + pictureFile.getAbsolutePath(),
					Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			ResultUtil.setTakePictureSuccess(false);
			LogUtil.e("File not found: " + e.getMessage());
		} catch (IOException e) {
			ResultUtil.setTakePictureSuccess(false);
			LogUtil.e("Error accessing file: " + e.getMessage());
		}
	}

	private File getOutputMediaFile() {
		if (cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK) {
			return new File(SDPATH + "backimage_" + mBackCameraCount + ".jpg");
		} else {
			return new File(SDPATH + "backimage_" + mFrontCameraCount + ".jpg");
		}
	}

	/**
	 * 删除文件夹和文件夹里面的文件
	 * */
	public void deleteDir() {
		File dir = new File(SDPATH);
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteDir(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		LogUtil.e("onBackPressedStableCameraTestActivity");
		mCameraSurPreview.releaseCamera();
		mHandler.removeCallbacks(pRunnable);
		deleteDir();
		StableCameraTestActivity.this.finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		LogUtil.e("onStartStableCameraTestActivity");
		File dirFirstFolder = new File(SDPATH);
		if (!dirFirstFolder.exists()) {
			dirFirstFolder.mkdirs();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableCameraTestActivity");
		mCameraSurPreview.releaseCamera();
		mHandler.removeCallbacks(pRunnable);
		deleteDir();
		StableCameraTestActivity.this.finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.e("onStopStableCameraTestActivity");
		mCameraSurPreview.releaseCamera();
		mHandler.removeCallbacks(pRunnable);
		StableCameraTestActivity.this.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableCameraTestActivity");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
		mCameraSurPreview.releaseCamera();

	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				LogUtil.e("CameraActivity MyBroadcastReceiver() receiveData : "
						+ cmdOrder);
			}
			Toast.makeText(StableCameraTestActivity.this,
					intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResultCallback(String result) {
	}

	@Override
	public void onClick(View v) {
	}
}
