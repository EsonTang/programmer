package com.prize.autotest.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;
import com.prize.autotest.camera.AutoCameraSurfacePreview.ResultCallback;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

//import android.widget.Toast;

public class AutoCameraActivity extends Activity implements OnClickListener,
		PictureCallback, ResultCallback {
	private static final String TAG = "zwl_camera";
	private AutoCameraSurfacePreview mCameraSurPreview = null;
	private Button mCaptureButton = null;
	private Button mAutoFocusButton = null;
	private Button mSwitchCamera = null;
	private Button mZoomOut = null;
	private Button mZoomIn = null;

	// Thread mThreadSocket;
	private BroadcastReceiver mBroadcast = null;
	private String cmdOrder = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");

		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		// Create our Preview view and set it as the content of our activity.
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		mCameraSurPreview = new AutoCameraSurfacePreview(this);
		mCameraSurPreview.setResultCallback(this);
		preview.addView(mCameraSurPreview);

		/*
		 * mSocketServer = new SocketServer(handler); mThreadSocket = new
		 * Thread(mSocketServer); mThreadSocket.start();
		 */

		// Add a listener to the Capture button
		mCaptureButton = (Button) findViewById(R.id.button_capture);
		mCaptureButton.setOnClickListener(this);

		mAutoFocusButton = (Button) findViewById(R.id.button_autofocus);
		mAutoFocusButton.setOnClickListener(this);

		mSwitchCamera = (Button) findViewById(R.id.button_switchcamera);
		mSwitchCamera.setOnClickListener(this);

		mZoomOut = (Button) findViewById(R.id.button_zoomout);
		mZoomOut.setOnClickListener(this);

		mZoomIn = (Button) findViewById(R.id.button_zoomin);
		mZoomIn.setOnClickListener(this);
		if (cmdOrder != null) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			}, 1500);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_capture:
			Log.w(TAG, "mCaptureButton  takePicture()");
			AutoResultUtil.setTakePictureSuccess(true);
			mCaptureButton.setEnabled(false);
			// get an image from the camera
			mCameraSurPreview.takePicture(this);
			break;
		case R.id.button_autofocus:
			Log.w(TAG, "mAutoFocusButton  makeAutoFocus()");
			AutoResultUtil.setAutoFocusSuccess(true);
			mCameraSurPreview.makeAutoFocus();
			break;
		case R.id.button_switchcamera:
			Log.w(TAG, "mAutoFocusButton  switchCamera()");
			mCameraSurPreview.switchCamera();
			break;
		case R.id.button_zoomout:
			Log.w(TAG, "mAutoFocusButton  setZoom()  out");
			mCameraSurPreview.setZoom(0);
			break;
		case R.id.button_zoomin:
			Log.w(TAG, "mAutoFocusButton  setZoom()  in");
			mCameraSurPreview.setZoom(1);
			break;
		default:
			break;
		}
	}

	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}
		String temp = cmdOrder.substring(1);
		if (temp.startsWith(AutoConstant.CMD_CAMERA_FRONT_OPEN)) {
			AutoResultUtil.setShutter(false);
			mCameraSurPreview.openFrontCamera();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_FRONT_CLOSE)) {
			AutoResultUtil.setShutter(false);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_FRONT_SHUTTER)) {
			AutoResultUtil.setShutter(true);
			mCameraSurPreview.openFrontCamera();
			mCameraSurPreview.takePicture(this);
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_BACK_OPEN)) {
			AutoResultUtil.setShutter(false);
			mCameraSurPreview.openBackCamera();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_BACK_CLOSE)) {
			AutoResultUtil.setShutter(false);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_BACK_FOCUS)) {
			AutoResultUtil.setShutter(false);
			mCameraSurPreview.makeAutoFocus();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_BACK_SHUTTER)) {
			AutoResultUtil.setShutter(true);
			mCameraSurPreview.openBackCamera();
			mCameraSurPreview.makeAutoFocus();
			mCameraSurPreview.takePicture(this);
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_SUCCESS)) {
			AutoConstant.writeProInfo("P", 39);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_CAMERA_FAIL)) {
			AutoConstant.writeProInfo("F", 39);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		}

	}

	@Override
	public void onResultCallback(String result) {
		AutoConstant.SendDataToService(result, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		File pictureFile = getOutputMediaFile();
		if (pictureFile == null) {
			AutoResultUtil.setTakePictureSuccess(false);
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			/*
			 * Toast.makeText(this, "Image has been saved to " +
			 * pictureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
			 */
		} catch (FileNotFoundException e) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
			AutoResultUtil.setTakePictureSuccess(false);
		} catch (IOException e) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
			AutoResultUtil.setTakePictureSuccess(false);
		}

		camera.startPreview();

		mCaptureButton.setEnabled(true);
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
	}

	private File getOutputMediaFile() {
		File picDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());

		return new File(picDir.getPath() + File.separator + "image_"
				+ AutoCameraSurfacePreview.cameraPosition + ".jpg");
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcast);
		mCameraSurPreview.releaseCamera();
		super.onDestroy();
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
			/*
			 * Toast.makeText(AutoCameraActivity.this,
			 * intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();
			 */
		}
	}

}
