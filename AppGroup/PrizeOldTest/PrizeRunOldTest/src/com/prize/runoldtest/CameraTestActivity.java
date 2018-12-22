package com.prize.runoldtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.prize.runoldtest.CameraSurfacePreview.ResultCallback;
import com.prize.runoldtest.util.ResultUtil;

import android.annotation.SuppressLint;
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
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class CameraTestActivity extends Activity implements OnClickListener, PictureCallback, ResultCallback {
	private static final String TAG = "zwl_camera";
	private static final String ACTION_UI = "com.prize.cameratest.ACTION_UI";
	private static final String ACTION_SERVICE = "com.prize.cameratest.ACTION_SERVICE";
	private CameraSurfacePreview mCameraSurPreview = null;
	private int mBackCameraCount = 0;
	private int mFrontCameraCount = 0;
	private int mSwitchCameraCount = 0;
	
	private boolean mOpenBackCamera = false;
	private boolean mMakeAutoFocus = false;
	private boolean mSetBackMaxZoom = false;
	private boolean mSetBackMinZoom = false;
	private boolean mOpenFrontCamera = false;
	private boolean mBackCameraShut = false;
	private boolean mFrontCameraShut = false;
	private boolean mSwitchCamera = false;
	private boolean mSetFrontMaxZoom = false;
	private boolean mSetFrontMinZoom = false;
	
	private final static int BACKCAMERASHUT = 1;
	private final static int FRONTCAMERASHUT = 2;
	private final static int SWITCHCAMERA = 3;
	private final static int OPENBACKCAMERA = 4;
	private final static int MAKEAUTOFOCUS = 5;
	private final static int SETBACKMAXZOOM = 6;
	private final static int SETBACKMINZOOM = 7;
	private final static int OPENFRONTCAMERA = 8;
	private final static int SETFRONTMAXZOOM = 9;
	private final static int SETFRONTMINZOOM = 10;
	
	// Thread mThreadSocket;
	private BroadcastReceiver mbr = null;
	private String cmdOrder = null;
	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case OPENBACKCAMERA:
				mCameraSurPreview.openBackCamera();
				mHandler.postDelayed(pRunnable, 1000);
				mOpenBackCamera = false;
				mMakeAutoFocus = true;
				Log.e("Ganxiayong", "-----------OPENBACKCAMERA(" + mBackCameraCount + ")---------");
				break;
			case MAKEAUTOFOCUS:
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 2500);
				mMakeAutoFocus = false;
				mSetBackMinZoom = true;
				Log.e("Ganxiayong", "-----------MAKEAUTOFOCUS(" + mBackCameraCount +")---------");
				break;
			case SETBACKMINZOOM:
				mCameraSurPreview.setMinZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 3000);
				mSetBackMinZoom = false;
				mSetBackMaxZoom = true;
				Log.e("Ganxiayong", "-----------SETBACKMINZOOM(" + mBackCameraCount + ")---------");
				break;
			case SETBACKMAXZOOM:
				mCameraSurPreview.setMaxZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 3000);
				mSetBackMaxZoom = false;
				mBackCameraShut = true;
				Log.e("Ganxiayong", "-----------SETBACKMAXZOOM(" + mBackCameraCount + ")---------");
				break;
			case BACKCAMERASHUT:
				mCameraSurPreview.takePicture(CameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 3000);
				mBackCameraCount++;
				if (mBackCameraCount < 5) {
					mBackCameraShut = false;
					mOpenBackCamera = true;
				}else {
					mBackCameraShut = false;
					mOpenFrontCamera = true;
				}
				Log.e("Ganxiayong", "-----------BACKCAMERASHUT(" + mBackCameraCount + ")---------");
				break;
			case OPENFRONTCAMERA:
				mCameraSurPreview.openFrontCamera();
				mHandler.postDelayed(pRunnable, 2000);
				mOpenFrontCamera = false;
				mSetFrontMinZoom = true;
				Log.e("Ganxiayong", "-----------OPENFRONTCAMERA(" + mFrontCameraCount + ")---------");
				break;
			case SETFRONTMINZOOM:
				mCameraSurPreview.setMinZoom();
				mHandler.postDelayed(pRunnable, 2000);
				mSetFrontMinZoom = false;
				mSetFrontMaxZoom = true;
				Log.e("Ganxiayong", "-----------SETFRONTMINZOOM(" + mFrontCameraCount + ")---------");
				break;
			case SETFRONTMAXZOOM:
				mCameraSurPreview.setMaxZoom();
				mHandler.postDelayed(pRunnable, 2000);
				mSetFrontMaxZoom = false;
				mFrontCameraShut = true;
				Log.e("Ganxiayong", "-----------SETFRONTMAXZOOM(" + mFrontCameraCount + ")---------");
				break;
			case FRONTCAMERASHUT:
				mCameraSurPreview.takePicture(CameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 2500);
				mFrontCameraCount++;
				if (mFrontCameraCount < 5) {
					mFrontCameraShut = false;
					mOpenFrontCamera = true;
				}else {
					mFrontCameraShut = false;
					mSwitchCamera = true;
				}
				Log.e("Ganxiayong", "-----------FRONTCAMERASHUT(" + mFrontCameraCount + ")---------");
				break;
			case SWITCHCAMERA:
				mCameraSurPreview.switchCamera();
				mHandler.postDelayed(pRunnable, 1000);
				mSwitchCameraCount++;
				Log.e("Ganxiayong", "-----------SWITCHCAMERA(" + mSwitchCameraCount + ")---------");
				break;
			}
		};
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		this.mbr = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UI);
		registerReceiver(this.mbr, filter);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		mCameraSurPreview = new CameraSurfacePreview(this);
		mCameraSurPreview.setResultCallback(this);
		preview.addView(mCameraSurPreview);
		
		mOpenBackCamera= true;
		mHandler.post(pRunnable);
		Log.e("Ganxiayong", "-----------onCreate()---------");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e("Ganxiayong", "-----------onPause()---------");
		finish();
	}
	
	Runnable pRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mOpenBackCamera){
				mHandler.sendEmptyMessage(OPENBACKCAMERA);
				Log.e("Ganxiayong", "-----------mBackCameraShut= " + mBackCameraShut + "---------");
			}
			if(mMakeAutoFocus){
				mHandler.sendEmptyMessage(MAKEAUTOFOCUS);
				Log.e("Ganxiayong", "-----------mMakeAutoFocus= " + mMakeAutoFocus + "---------");
			}
			if(mSetBackMaxZoom){
				mHandler.sendEmptyMessage(SETBACKMAXZOOM);
				Log.e("Ganxiayong", "-----------mSetBackMaxZoom= " + mSetBackMaxZoom + "---------");
			}
			if(mSetBackMinZoom){
				mHandler.sendEmptyMessage(SETBACKMINZOOM);
				Log.e("Ganxiayong", "-----------mSetBackMinZoom= " + mSetBackMinZoom + "---------");
			}
			if(mBackCameraShut){
				mHandler.sendEmptyMessage(BACKCAMERASHUT);
				Log.e("Ganxiayong", "-----------mBackCameraShut= " + mBackCameraShut + "---------");
			}
			if(mOpenFrontCamera){
				mHandler.sendEmptyMessage(OPENFRONTCAMERA);
				Log.e("Ganxiayong", "-----------mOpenBackCamera= " + mOpenBackCamera + "---------");
			}
			if(mSetFrontMaxZoom){
				mHandler.sendEmptyMessage(SETFRONTMAXZOOM);
				Log.e("Ganxiayong", "-----------mSetFrontMaxZoom= " + mSetFrontMaxZoom + "---------");
			}
			if(mSetFrontMinZoom){
				mHandler.sendEmptyMessage(SETFRONTMINZOOM);
				Log.e("Ganxiayong", "-----------mSetFrontMinZoom= " + mSetFrontMinZoom + "---------");
			}
			if(mFrontCameraShut){
				mHandler.sendEmptyMessage(FRONTCAMERASHUT);
				Log.e("Ganxiayong", "-----------mFrontCameraShut= " + mFrontCameraShut + "---------");
			}
			if(mSwitchCamera){
				mHandler.sendEmptyMessage(SWITCHCAMERA);
				Log.e("Ganxiayong", "-----------mSwitchCamera= " + mSwitchCamera + "---------");
			}
		}
	};

	@Override
	public void onResultCallback(String result) {
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		// save the picture to sdcard
		File pictureFile = getOutputMediaFile();
		if (pictureFile == null) {
			Log.w(TAG, "Error creating media file, check storage permissions: ");
			ResultUtil.setTakePictureSuccess(false);
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(this, "Image has been saved to " + pictureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			ResultUtil.setTakePictureSuccess(false);
			Log.w(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			ResultUtil.setTakePictureSuccess(false);
			Log.w(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	private File getOutputMediaFile() {
		// get the mobile Pictures directory
		File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		// get the current time
//		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		
		if(mBackCameraCount < 5){
			return new File(picDir.getPath() + File.separator + "backimage_" + mBackCameraCount + ".jpg");
		}else {
			return new File(picDir.getPath() + File.separator + "frontimage_" + mFrontCameraCount + ".jpg");
		}
	}

	@Override
	protected void onStop() {
		Log.w(TAG, "onStop() is called");
		super.onStop();
		mHandler.removeCallbacks(pRunnable);
	}

	@Override
	protected void onDestroy() {
		Log.w(TAG, "onDestroy() is called");
		unregisterReceiver(mbr);
		mCameraSurPreview.releaseCamera();
		// mSocketServer.close();
		// mThreadSocket.destroy();
		super.onDestroy();
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				Log.w(TAG, "CameraActivity MyBroadcastReceiver() receiveData : " + cmdOrder);
//				runCmdOrder();
			}
			Toast.makeText(CameraTestActivity.this, intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
	
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	ManualTestActivity.FlagMc = true;
    	mHandler.removeCallbacks(pRunnable);
    }
}
