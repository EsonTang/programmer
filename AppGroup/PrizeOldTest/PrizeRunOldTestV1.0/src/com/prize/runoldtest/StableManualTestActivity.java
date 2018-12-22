package com.prize.runoldtest;

import com.prize.runoldtest.camera.StableCameraTestActivity;
import com.prize.runoldtest.emmc.StableEmmcActivity;
import com.prize.runoldtest.flight.StableFlightActivity;
import com.prize.runoldtest.lcd.StableLcdActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.InputFilterMaxMin;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.vibrate.VibrateTestActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

@SuppressLint("Wakelock")
@SuppressWarnings("deprecation")
public class StableManualTestActivity extends Activity {
	private PowerManager.WakeLock wakeLock = null;
	private Button ButtonOk;
	private Button ButtonCancel;
	private ScrollView scrollView = null;
	private EditText editTextLcd;
	private EditText editTextFlagLight;
	private EditText editTextVibrate;
	private EditText editTextCamera;
	private EditText editTextEmmc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("onCreateStableManualTestActivity");
		setContentView(R.layout.activity_stable_manual_test);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "StableManualTestActivity");
		wakeLock.acquire();
		initView();
		DataUtil.addDestoryActivity(StableManualTestActivity.this,
				"StableManualTestActivity");
		scrollView = (ScrollView) findViewById(R.id.stable_sv_testitem);
		scrollView.setVerticalScrollBarEnabled(true);
		ButtonOk = (Button) findViewById(R.id.stable_bt_ok);
		ButtonCancel = (Button) findViewById(R.id.stable_bt_cancel);

		ButtonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.e("StableManualTestActivity", "start click");
				Intent intent = new Intent(StableManualTestActivity.this,
						StableLcdActivity.class);
				boolean iscorrect = SaveCurruentEditData();
				Log.e("StableManualTestActivity", "iscorrect = " + iscorrect);
				if (iscorrect) {
					editTextLcd = (EditText) findViewById(R.id.stable_et_lcdtest);
					editTextLcd
							.setFilters(new InputFilter[] { new InputFilterMaxMin(
									"1", "9999") });
					String testTime = editTextLcd.getText().toString();
					int LcdTime = Integer.parseInt(testTime);
					intent.putExtra(Const.EXTRA_MESSAGE, LcdTime);
					startActivity(intent);
				}
			}
		});

		ButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				StableManualTestActivity.this.finish();
			}
		});
	}

	private void initView() {
		editTextLcd = (EditText) findViewById(R.id.stable_et_lcdtest);
		editTextFlagLight = (EditText) findViewById(R.id.stable_et_lcdtest);
		editTextVibrate = (EditText) findViewById(R.id.stable_et_vbtest);
		editTextCamera = (EditText) findViewById(R.id.stable_et_cameratest);
		editTextEmmc = (EditText) findViewById(R.id.stable_et_emmctest);
	}

	protected void onStart() {
		super.onStart();
		LogUtil.e("onStartStableManualTestActivity");
		if (DataUtil.FlagLight_stable) {
			DataUtil.FlagLight_stable = false;
			Intent intent = new Intent(this, StableFlightActivity.class);
			editTextFlagLight = (EditText) findViewById(R.id.stable_et_lcdtest);
			editTextFlagLight
					.setFilters(new InputFilter[] { new InputFilterMaxMin("1",
							"9999") });
			String message = editTextFlagLight.getText().toString();
			int LcdTime = Integer.parseInt(message);
			intent.putExtra(Const.EXTRA_MESSAGE, LcdTime);
			startActivity(intent);
		} else if (DataUtil.FlagVibrate_stable) {
			DataUtil.FlagVibrate_stable = false;
			Intent intent = new Intent(this, VibrateTestActivity.class);
			editTextVibrate = (EditText) findViewById(R.id.stable_et_vbtest);
			editTextVibrate
					.setFilters(new InputFilter[] { new InputFilterMaxMin("1",
							"9999") });
			String message = editTextVibrate.getText().toString();
			int VibrateTime = Integer.parseInt(message);
			intent.putExtra(Const.EXTRA_MESSAGE, VibrateTime);
			startActivity(intent);
		} else if (DataUtil.FlagCamera_stable) {
			DataUtil.FlagCamera_stable = false;
			Intent intent = new Intent(this, StableCameraTestActivity.class);
			editTextCamera = (EditText) findViewById(R.id.stable_et_cameratest);
			editTextCamera
					.setFilters(new InputFilter[] { new InputFilterMaxMin("1",
							"9999") });
			String message = editTextCamera.getText().toString();
			int CameraTime = Integer.parseInt(message);
			intent.putExtra(Const.EXTRA_MESSAGE, CameraTime);
			startActivity(intent);
		} else if (DataUtil.FlagEmmc_stable) {
			DataUtil.FlagEmmc_stable = false;
			Intent intent = new Intent(this, StableEmmcActivity.class);
			editTextEmmc = (EditText) findViewById(R.id.stable_et_emmctest);
			editTextEmmc.setFilters(new InputFilter[] { new InputFilterMaxMin(
					"1", "9999") });
			String message = editTextEmmc.getText().toString();
			intent.putExtra(Const.EXTRA_MESSAGE, message);
			startActivity(intent);
		}
	}

	private boolean SaveCurruentEditData() {
		Log.e("StableManualTestActivity", "start SaveCurruentEditData()");
		boolean ContentCorrect = true;
		SharedPreferences sharedPreferences = StableManualTestActivity.this.getSharedPreferences("savedata",
				ManualTestActivity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();

		String message = editTextLcd.getText().toString().trim();
		String message1 = editTextFlagLight.getText().toString().trim();
		String message2 = editTextVibrate.getText().toString().trim();
		String message3 = editTextCamera.getText().toString().trim();
		String message4 = editTextEmmc.getText().toString().trim();

		if (message.equals("") || message1.equals("") || message2.equals("")
				|| message3.equals("") || message4.equals("")) {
			ContentCorrect = false;
			ShowDialog();
		} else {
			editor.putString("lcdtest", message);
			editor.putString("lighttestt", message1);
			editor.putString("vibratetest", message2);
			editor.putString("camtest", message3);
			editor.putString("emmctest", message4);
			editor.commit();
		}
		return ContentCorrect;
	}

	private void ShowDialog() {
		Log.e("StableManualTestActivity", "start ShowDialog()");
		String str1 = this.getResources().getString(R.string.notice);
		String str2 = this.getResources().getString(R.string.mcontent);
		String str3 = this.getResources().getString(R.string.sure);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(str1);
		builder.setMessage(str2);
		builder.setPositiveButton(str3, null);
		builder.show();
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.e("onPauseStableManualTestActivity");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.e("onDestroyStableManualTestActivity");
		DataUtil.resetFlag();
		wakeLock.release();
	}
}
