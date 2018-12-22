package com.prize.autotest.mmi;

import java.io.FileOutputStream;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
//import android.widget.Toast;

@SuppressWarnings("deprecation")
public class AutoTorchTestActivity extends Activity {
	public static final boolean HasFlashlightFile = false;
	String TAG = "Flashlight";
	private Camera mycam = null;
	private Parameters camerPara = null;
	final byte[] LIGHTE_ON = { '2', '5', '5' };
	final byte[] LIGHTE_OFF = { '0' };
	private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";

	
	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.torch);
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			}, 500);
		}		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
			/*Toast.makeText(AutoTorchTestActivity.this,
					intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();*/
		}
	}
	
	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		//Toast.makeText(this, temp + "", Toast.LENGTH_SHORT).show();
		if (temp.startsWith(AutoConstant.CMD_MMI_TORCH_START)) {
			openTorch(Camera.CameraInfo.CAMERA_FACING_BACK);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
		}else if (temp.startsWith(AutoConstant.CMD_MMI_TORCH_STOP)) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
			AutoConstant.writeFile("Torch : PASS" + "\n");
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_MMI_TORCH_FRONT_START)) {
			openTorch(Camera.CameraInfo.CAMERA_FACING_FRONT);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
		} else if (temp.startsWith(AutoConstant.CMD_MMI_TORCH_FRONT_STOP)) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,this);
			AutoConstant.writeFile("TorchFront : PASS" + "\n");
			finish();
		}

	}

	private void openTorch(int cameraid) {
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(cameraid);
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

}
