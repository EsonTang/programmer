package com.prize.factorytest.CameraFront;

import java.io.IOException;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.WindowManager;

public class CameraFront extends Activity implements SurfaceHolder.Callback {

	private Camera mCamera = null;
	private Button takeButton, passButton, failButton;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	public static final String ZSD_MODE_ON = "on";
	public static final String ZSD_MODE_OFF = "off";
	private WindowManager.LayoutParams lp;
	
	@Override
	public void finish() {
		stopCamera();
		super.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_front);
		lp = getWindow().getAttributes();
		lp.screenBrightness = 1.0f;
		getWindow().setAttributes(lp);
		mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(CameraFront.this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		bindView();
	}

	void bindView() {

		takeButton = (Button) findViewById(R.id.take_picture);
		passButton = (Button) findViewById(R.id.camera_pass);
		failButton = (Button) findViewById(R.id.camera_fail);
		takeButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {

				takeButton.setVisibility(View.GONE);
				try {
					if (mCamera != null) {
						takePicture();
					} else {
						finish();
					}
				} catch (Exception e) {

				}
			}
		});

		passButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		failButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	public void surfaceCreated(SurfaceHolder surfaceholder) {
		int oritationAdjust = 0;
		try {
			mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			mCamera.setDisplayOrientation(oritationAdjust);
		} catch (Exception exception) {
			showToast(getString(R.string.cameraback_fail_open));
			mCamera = null;
		}

		if (mCamera == null) {
			finish();
		} else {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
				finish();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
			int h) {
		if (mCamera != null) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setPreviewSize(h,w);
				parameters.setPictureSize(h,w);
				parameters.setPictureFormat(PixelFormat.JPEG);
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				/*
				if(ZSD_MODE_ON.equals(parameters.getZSDMode())){
					parameters.setZSDMode(ZSD_MODE_OFF);
				}
				*/
				mCamera.setParameters(parameters);
				mCamera.setDisplayOrientation(90);
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {

		stopCamera();
	}

	private void takePicture() {

		if (mCamera != null) {
			try {
				mCamera.takePicture(mShutterCallback, rawPictureCallback,
						jpegCallback);
			} catch (Exception e) {
				e.printStackTrace();
				finish();
			}
		} else {
			finish();
		}
	}

	private ShutterCallback mShutterCallback = new ShutterCallback() {

		public void onShutter() {

			try {
				takeButton.setVisibility(View.GONE);
				passButton.setVisibility(View.VISIBLE);
				failButton.setVisibility(View.VISIBLE);
			} catch (Exception e) {

			}
		}
	};

	private PictureCallback rawPictureCallback = new PictureCallback() {

		public void onPictureTaken(byte[] _data, Camera _camera) {

			try {
				takeButton.setVisibility(View.GONE);
				passButton.setVisibility(View.VISIBLE);
				failButton.setVisibility(View.VISIBLE);
			} catch (Exception e) {

			}
		}
	};

	private PictureCallback jpegCallback = new PictureCallback() {

		public void onPictureTaken(byte[] _data, Camera _camera) {

			try {
				mCamera.stopPreview();
				takeButton.setVisibility(View.GONE);
				passButton.setVisibility(View.VISIBLE);
				failButton.setVisibility(View.VISIBLE);
			} catch (Exception e) {

			}
		}
	};

	public final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {

		public void onAutoFocus(boolean focused, Camera camera) {

			if (focused) {
				takePicture();
			}
		}
	};

	private void stopCamera() {
		if (mCamera != null) {
			try {
				mCamera.stopPreview();
				mCamera.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
}
