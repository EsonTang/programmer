package com.prize.runoldtest.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.prize.runoldtest.R;
import com.prize.runoldtest.camera.CameraSurfacePreview.ResultCallback;
import com.prize.runoldtest.emmc.EmmcActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;
import com.prize.runoldtest.util.ResultUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

@SuppressLint("SdCardPath") @SuppressWarnings("deprecation")
public class CameraTestActivity extends Activity implements OnClickListener, PictureCallback, ResultCallback {
	private static final String ACTION_UI = "com.prize.cameratest.ACTION_UI";
	private CameraSurfacePreview mCameraSurPreview = null;
	public static int cameraPosition;// camera id;
	private int mCameraTestCount = 0;
	private int mTakePictureCount = 0;
	private int mBackCameraCount = 0;
	private int mFrontCameraCount = 0;
	
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
	private String TAG="RuninCameraTest";
	public String SDPATH = "/mnt/sdcard/prizeoldtest/";
	
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
				LogUtil.e("CameraTestActivity-----------OPENBACKCAMERA(" + mBackCameraCount + ")---------");
				break;
			case MAKEAUTOFOCUS:
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 2500);
				mMakeAutoFocus = false;
				mSetBackMinZoom = true;
				LogUtil.e("CameraTestActivity-----------MAKEAUTOFOCUS(" + mBackCameraCount +")---------");
				break;
			case SETBACKMINZOOM:
				mCameraSurPreview.setMinZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 3000);
				mSetBackMinZoom = false;
				mSetBackMaxZoom = true;
				LogUtil.e("CameraTestActivity-----------SETBACKMINZOOM(" + mBackCameraCount + ")---------");
				break;
			case SETBACKMAXZOOM:
				mCameraSurPreview.setMaxZoom();
				mCameraSurPreview.makeAutoFocus();
				mHandler.postDelayed(pRunnable, 3000);
				mSetBackMaxZoom = false;
				mBackCameraShut = true;
				LogUtil.e("CameraTestActivity-----------SETBACKMAXZOOM(" + mBackCameraCount + ")---------");
				break;
			case BACKCAMERASHUT:
				mCameraSurPreview.takePicture(CameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 3000);
				mBackCameraCount++;
				cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
				mBackCameraShut = false;
				mOpenFrontCamera = true;
				LogUtil.e("CameraTestActivity-----------BACKCAMERASHUT(" + mBackCameraCount + ")---------");
				break;
			case OPENFRONTCAMERA:
				mCameraSurPreview.openFrontCamera();
				mHandler.postDelayed(pRunnable, 2000);
				mOpenFrontCamera = false;
				mSetFrontMinZoom = true;
				LogUtil.e("CameraTestActivity-----------OPENFRONTCAMERA(" + mFrontCameraCount + ")---------");
				break;
			case SETFRONTMINZOOM:
				mCameraSurPreview.setMinZoom();
				mHandler.postDelayed(pRunnable, 2000);
				mSetFrontMinZoom = false;
				mSetFrontMaxZoom = true;
				LogUtil.e("CameraTestActivity-----------SETFRONTMINZOOM(" + mFrontCameraCount + ")---------");
				break;
			case SETFRONTMAXZOOM:
				mCameraSurPreview.setMaxZoom();
				mHandler.postDelayed(pRunnable, 2000);
				mSetFrontMaxZoom = false;
				mFrontCameraShut = true;
				LogUtil.e("CameraTestActivity-----------SETFRONTMAXZOOM(" + mFrontCameraCount + ")---------");
				break;
			case FRONTCAMERASHUT:
				mCameraSurPreview.takePicture(CameraTestActivity.this);
				mHandler.postDelayed(pRunnable, 2500);
				mFrontCameraCount++;
				cameraPosition = Camera.CameraInfo.CAMERA_FACING_FRONT;
				mFrontCameraShut = false;
				mSwitchCamera = true;
				LogUtil.e("CameraTestActivity-----------FRONTCAMERASHUT(" + mFrontCameraCount + ")---------");
				mTakePictureCount++;
				if(mTakePictureCount >1){
					if(mCameraTestCount > 0){
						mCameraTestCount--;
						mTakePictureCount = 0;
					}else{
						LogUtil.e("KEYCODE_BACKFlagCam");
						DataUtil.FlagReboot = true;
						OldTestResult.CameraTestresult=true;
						Intent intent = new Intent();
			            //把返回数据存入Intent
			            intent.putExtra("result", "CameraTest:PASS");
			            //设置返回数据
			            CameraTestActivity.this.setResult(RESULT_OK, intent);
			            
				    	CameraTestActivity.this.finish();
					}
				}
				break;
			case SWITCHCAMERA:
				mCameraSurPreview.switchCamera();
				mHandler.postDelayed(pRunnable, 1500);
				mSwitchCamera = false;
				mMakeAutoFocus = true;
				break;
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		DataUtil.addDestoryActivity(CameraTestActivity.this, "CameraTestActivity");
		this.mbr = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UI);
		registerReceiver(this.mbr, filter);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		mCameraSurPreview = new CameraSurfacePreview(this);
		mCameraSurPreview.setResultCallback(this);
		preview.addView(mCameraSurPreview);
		mOpenBackCamera= true;
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "begin CameraTest......."+"\n");
		mHandler.post(pRunnable);
		DataUtil.addDestoryActivity(CameraTestActivity.this, "LcdActivity");
        Intent intent=getIntent();
		mCameraTestCount = intent.getIntExtra(Const.EXTRA_MESSAGE, 0);
	}
	
	
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onConfigurationChanged......."+"\n");
	}




	@Override
	protected void onStart() {
		super.onStart();
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onStart......."+"\n");
		LogUtil.e("onStartCameraTestActivity");
		File dirFirstFolder = new File(SDPATH);
		if (!dirFirstFolder.exists()) {
			dirFirstFolder.mkdirs();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	Runnable pRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mOpenBackCamera){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mOpenBackCamera......."+"\n");
				mHandler.sendEmptyMessage(OPENBACKCAMERA);
				LogUtil.e("CameraTestActivity-----------mBackCameraShut= " + mBackCameraShut + "---------");
			}
			if(mMakeAutoFocus){
				mHandler.sendEmptyMessage(MAKEAUTOFOCUS);
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mMakeAutoFocus......."+"\n");
				LogUtil.e("CameraTestActivity-----------mMakeAutoFocus= " + mMakeAutoFocus + "---------");
			}
			if(mSetBackMaxZoom){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mSetBackMaxZoom......."+"\n");
				mHandler.sendEmptyMessage(SETBACKMAXZOOM);
				LogUtil.e("CameraTestActivity-----------mSetBackMaxZoom= " + mSetBackMaxZoom + "---------");
			}
			if(mSetBackMinZoom){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mSetBackMinZoom......."+"\n");
				mHandler.sendEmptyMessage(SETBACKMINZOOM);
				LogUtil.e("CameraTestActivity-----------mSetBackMinZoom= " + mSetBackMinZoom + "---------");
			}
			if(mBackCameraShut){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mBackCameraShut......."+"\n");
				mHandler.sendEmptyMessage(BACKCAMERASHUT);
				LogUtil.e("CameraTestActivity-----------mBackCameraShut= " + mBackCameraShut + "---------");
			}
			if(mOpenFrontCamera){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mOpenFrontCamera......."+"\n");
				mHandler.sendEmptyMessage(OPENFRONTCAMERA);
				LogUtil.e("CameraTestActivity-----------mOpenBackCamera= " + mOpenBackCamera + "---------");
			}
			if(mSetFrontMaxZoom){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mSetFrontMaxZoom......."+"\n");
				mHandler.sendEmptyMessage(SETFRONTMAXZOOM);
				LogUtil.e("CameraTestActivity-----------mSetFrontMaxZoom= " + mSetFrontMaxZoom + "---------");
			}
			if(mSetFrontMinZoom){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mSetFrontMinZoom......."+"\n");
				mHandler.sendEmptyMessage(SETFRONTMINZOOM);
				LogUtil.e("CameraTestActivity-----------mSetFrontMinZoom= " + mSetFrontMinZoom + "---------");
			}
			if(mFrontCameraShut){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mFrontCameraShut......."+"\n");
				mHandler.sendEmptyMessage(FRONTCAMERASHUT);
				LogUtil.e("CameraTestActivity-----------mFrontCameraShut= " + mFrontCameraShut + "---------");
			}
			if(mSwitchCamera){
				LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "mSwitchCamera......."+"\n");
				mHandler.sendEmptyMessage(SWITCHCAMERA);
				LogUtil.e("CameraTestActivity-----------mSwitchCamera= " + mSwitchCamera + "---------");
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
			LogUtil.e("Error creating media file, check storage permissions: ");
			ResultUtil.setTakePictureSuccess(false);
			return;
		}
		try {
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "FileOutputStream......."+"\n");
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Toast.makeText(this, "Image has been saved to " + pictureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			ResultUtil.setTakePictureSuccess(false);
			LogUtil.e("File not found: " + e.getMessage());
		} catch (IOException e) {
			ResultUtil.setTakePictureSuccess(false);
			LogUtil.e("Error accessing file: " + e.getMessage());
		}
	}

	private File getOutputMediaFile() {
		if(cameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK){
			return new File(SDPATH + "backimage_" + mBackCameraCount + ".jpg");
		}else {
			return new File(SDPATH + "backimage_" + mFrontCameraCount + ".jpg");
		}
	}

	@Override
	protected void onStop() {
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onStop......."+"\n");
		LogUtil.e("onStop() is called");
		super.onStop();
		mCameraSurPreview.releaseCamera();
		mHandler.removeCallbacks(pRunnable);
		deleteDir();
		CameraTestActivity.this.finish();
	}

	@Override
	protected void onDestroy() {
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "onDestroy......."+"\n");
		LogUtil.e("onDestroy() is called");
		unregisterReceiver(mbr);
		mCameraSurPreview.releaseCamera();
		
		super.onDestroy();
	
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				LogUtil.e("CameraActivity MyBroadcastReceiver() receiveData : " + cmdOrder);
			}
			Toast.makeText(CameraTestActivity.this, intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * ɾ���ļ��к��ļ���������ļ�
	 * */
	public void deleteDir() {
		File dir = new File(SDPATH);
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // ɾ�������ļ�
			else if (file.isDirectory())
				deleteDir(); // �ݹ�ķ�ʽɾ���ļ���
		}
		dir.delete();
	}

	@Override
	public void onClick(View v) {
	}
	
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	mHandler.removeCallbacks(pRunnable);
    	deleteDir();
    	/*Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("result", "CameraTest:FAIL");
        //设置返回数据
        CameraTestActivity.this.setResult(RESULT_OK, intent);*/
    	DataUtil.finishBackPressActivity();
    }
}
