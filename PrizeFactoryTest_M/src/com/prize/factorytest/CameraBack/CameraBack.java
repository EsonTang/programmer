package com.prize.factorytest.CameraBack;

import java.io.IOException;
import java.util.List;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import com.prize.factorytest.R;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.View.OnTouchListener; 
import android.view.WindowManager;

import android.util.Log;
import android.hardware.Camera.Size;

public class CameraBack extends Activity implements SurfaceHolder.Callback {

	private Camera mCamera = null;
	private Button takeButton, passButton, failButton;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Object mFaceDetectionSync = new Object();
	final static String TAG = "CameraBack";
	public static final String ZSD_MODE_ON = "on";
	public static final String ZSD_MODE_OFF = "off";

	/* prize-xucm-20160127-add FocuseView-start */
	private FocuseView mFocuseView;
	private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private Camera.Parameters parameters;
	/* prize-xucm-20160127-add FocuseView-end */
	
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_back);
		lp = getWindow().getAttributes();
		lp.screenBrightness = 1.0f;
		getWindow().setAttributes(lp);
		mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
		mSurfaceView.setOnTouchListener(new OnTouchListener() {  
             public boolean onTouch(View v, MotionEvent event) {  
                 // TODO Auto-generated method stub  
                mFocuseView.moveView(event.getRawX() - mFocuseView.getWidth() / 2,
				event.getRawY() - mFocuseView.getHeight() / 2);
				mFocuseView.showStart();
				if (mCamera != null&&isSupportFocusMode(Parameters.FOCUS_MODE_AUTO, mCamera
						.getParameters().getSupportedFocusModes())) {
					parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
					mCamera.setParameters(parameters);
					mCamera.cancelAutoFocus();
					mCamera.autoFocus(mAutoFocusCallback);
				}
				return false;
             }  
         });  
		/* prize-xucm-20160127-add FocuseView-start */
		mFocuseView = (FocuseView) findViewById(R.id.focuseview);
		/* prize-xucm-20160127-add FocuseView-end */
		bindView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(CameraBack.this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	void bindView() {
		takeButton = (Button) findViewById(R.id.take_picture);
		passButton = (Button) findViewById(R.id.camera_pass);
		failButton = (Button) findViewById(R.id.camera_fail);
		takeButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {

				takeButton.setVisibility(View.GONE);
				try {
					synchronized (mFaceDetectionSync) {
						if (mCamera != null) {
							takePicture();
						} else {
							finish();
						}
					}
				} catch (Exception e) {
					fail(getString(R.string.autofocus_fail));
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
		try {
			mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
		} catch (Exception exception) {
			toast(getString(R.string.cameraback_fail_open));
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

	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
			int h) {
		if (mCamera != null) {

			try {
				parameters = mCamera.getParameters();

				List<Size> previewSizeList = parameters.getSupportedPreviewSizes();
				int previewSizeWidth = previewSizeList.get(previewSizeList.size() -1).width;
				int previewSizeHeight = previewSizeList.get(previewSizeList.size() -1).height;
				for(int i=1;i<=previewSizeList.size(); i++){
					previewSizeWidth = previewSizeList.get(previewSizeList.size()-i).width;
					previewSizeHeight = previewSizeList.get(previewSizeList.size()-i).height;
					Log.e("xxx","(double)previewSizeWidth/previewSizeHeight111 = " + (double)previewSizeWidth/previewSizeHeight);
					if(Math.abs((double)previewSizeWidth/previewSizeHeight - (double)4/3) < 0.01){
						break;
					}
				}
				parameters.setPreviewSize(previewSizeWidth, previewSizeHeight);
				/*
				List<Size> pictureSizeList = parameters.getSupportedPictureSizes();
				parameters.setPictureSize(pictureSizeList.get(pictureSizeList.size() -1).width, pictureSizeList.get(pictureSizeList.size() -1).height);
				Log.e("xxx","pictureSizeList.get(pictureSizeList.size() -1).height = " + pictureSizeList.get(pictureSizeList.size() -1).height + "pictureSizeList.get(pictureSizeList.size() -1).width = " + pictureSizeList.get(pictureSizeList.size() -1).width);
				*/
				
				parameters.setPictureFormat(PixelFormat.JPEG);
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				parameters.setRotation(CameraInfo.CAMERA_FACING_BACK);
				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				if (ZSD_MODE_ON.equals(parameters.getZSDMode())){
					parameters.setZSDMode(ZSD_MODE_OFF);
				}
				mCamera.setParameters(parameters);
				mCamera.setDisplayOrientation(90);
				mCamera.startPreview();
				mCamera.cancelAutoFocus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		surfaceholder.removeCallback(this);
		stopCamera();
	}

	private void takePicture() {
		if (mCamera != null) {
			try {
				mCamera.takePicture(mShutterCallback, rawPictureCallback,
						jpegCallback);
			} catch (Exception e) {

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
			mFocuseView.showSuccess(true);
		}
	};

	private void stopCamera() {
		synchronized (mFaceDetectionSync) {
			if (mCamera != null) {
				try {
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					mCamera.release();
					mCamera=null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
	/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mFocuseView.moveView(event.getRawX() - mFocuseView.getWidth() / 2,
				event.getRawY() - mFocuseView.getHeight() / 2);
		mFocuseView.showStart();
		if (mCamera != null&&isSupportFocusMode(Parameters.FOCUS_MODE_AUTO, mCamera
				.getParameters().getSupportedFocusModes())) {
			parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			mCamera.setParameters(parameters);
			mCamera.cancelAutoFocus();
			mCamera.autoFocus(mAutoFocusCallback);
		}
		return super.onTouchEvent(event);
	}
	*/
	private boolean isSupportFocusMode(Object value, List<?> supported) {
		return supported == null ? false : supported.indexOf(value) >= 0;
	}

	@Override
	protected void onStop() {
		mFocuseView.moveView(0, 0);
		super.onStop();
	}
}
