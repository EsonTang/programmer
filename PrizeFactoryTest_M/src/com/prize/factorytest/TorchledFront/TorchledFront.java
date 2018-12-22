package com.prize.factorytest.TorchledFront;

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

public class TorchledFront extends Activity {
	public static final boolean HasFlashlightFile = false;
	String TAG = "Flashlight";
	private Camera mycam = null;
	private Parameters camerPara = null;
	final byte[] LIGHTE_ON = { '2', '5', '5' };
	final byte[] LIGHTE_OFF = { '0' };
	private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";

	
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
				camerPara.setFlashMode(Parameters.FLASH_MODE_OFF);
				mycam.setParameters(camerPara);
				mycam.release();
				mycam = null;
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

		
		confirmButton();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
				camerPara = mycam.getParameters();
				camerPara.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mycam.setParameters(camerPara);
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
