package com.prize.factorytest.Torchled;

import java.io.FileOutputStream;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.KeyEvent;
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
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Torchled extends Activity {
	public static final boolean HasFlashlightFile = false;
	String TAG = "Flashlight";
	private Camera mycam = null;
	private Parameters camerPara = null;
	final byte[] LIGHTE_ON = { '2', '5', '5' };
	final byte[] LIGHTE_OFF = { '0' };
	private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";
	

	private CameraManager mCameraManager = null;
	private String mCameraId;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		
		if (!HasFlashlightFile) {
			try {
				mCameraManager.setTorchMode(mCameraId, false);				
				finish();
			} catch (Exception e) {
			}
		} else {

			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(FLASHLIGHT_NODE);
				flashlight.write(LIGHTE_OFF);
				flashlight.close();

			} catch (Exception e) {
			}
		}
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout TorchLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.torch, null);
		setContentView(TorchLayout);

		if (!HasFlashlightFile) {
			try {
				mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
				tryInitCamera();
                mCameraManager.setTorchMode(mCameraId, true);
			} catch (Exception e) {
			}
		} else {

			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(
						"/sys/class/leds/flashlight/brightness");
				flashlight.write(LIGHTE_ON);
				flashlight.close();

			} catch (Exception e) {
			}

		}
		confirmButton();
	}

    private void tryInitCamera() {
        try {
            mCameraId = getCameraId();
        } catch (Throwable e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        }
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
	
	
	void confirmButton() {
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}
}
