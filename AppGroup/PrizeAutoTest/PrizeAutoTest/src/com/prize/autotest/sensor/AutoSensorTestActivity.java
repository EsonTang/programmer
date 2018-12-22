package com.prize.autotest.sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;
import com.prize.autotest.horcali.SensorCalibration;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.ConsumerIrManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class AutoSensorTestActivity extends Activity {

	private static final String filePath = "/data/prize_backup/";
	private static final String fileName = "prize_factory_gsensor";

	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	private Sensor mAccSensor = null;
	private Sensor mStepSensor = null;
	private Sensor mGySensor = null;

	private SensorListener mSensorListener;
	private AccSensorListener mAccSensorListener;
	private StepSensorListener mStepSensorListener;
	private GySensorListener mGySensorListener;

	private static float lastValues = 0;
	private int ccount = 0;
	private int stepccount = 0;
	private static float ccountbak = 0;
	private String cmdOrder = null;
	private Context context;
	private BroadcastReceiver mBroadcast = null;
	boolean up = false, down = false, right = false, left = false;
	boolean accup = false, accdown = false, accright = false, accleft = false;
	boolean stepup = false, stepdown = false, stepright = false, stepleft = false;
	boolean gyup = false, gydown = false, gyright = false, gyleft = false;
	private ConsumerIrManager mCIR;
	private boolean islightwritefile = false, ismagneticwritefile = false, isproxwritefile = false,
			isaccwritefile = false, isstepwritefile = false, isgywritefile = false, isAccHoliwritefile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor);
		context = this;

		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		try {
			String[] cmdMode = new String[] { "/system/bin/sh", "-c", "echo" + " " + 0 + " > /proc/dummystep" };
			Runtime.getRuntime().exec(cmdMode);
			Log.e("liup", "onResume success");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			String[] cmdMode = new String[] { "/system/bin/sh", "-c", "echo" + " " + 1 + " > /proc/dummystep" };
			Runtime.getRuntime().exec(cmdMode);
			Log.e("liup", "onPause success");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
			/*
			 * Toast.makeText(AutoSensorTestActivity.this,
			 * intent.getStringExtra("back"), Toast.LENGTH_SHORT).show();
			 */
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			String sHorCali = data.getStringExtra("sHorCali");
			creatFile();
			writeFile(sHorCali);
			try {
				String[] cmdMode = new String[] { "/system/bin/sh", "-c",
						"echo" + " " + sHorCali + " > /proc/gsensor_cali" };
				Runtime.getRuntime().exec(cmdMode);
			} catch (IOException e) {
				e.printStackTrace();
			}
			isAccHoliwritefile = true;
			AutoConstant.writeFile("acc sensor cabration: PASS" + sHorCali + "\n");
			// AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
			// context);
		} else {
			AutoConstant.writeFile("acc sensor cabration: FAIL" + "\n");
			isAccHoliwritefile = false;
			// AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
			// context);
		}
	}

	private void creatFile() {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
		}

		file = new File(filePath + fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		}

		int status = -1;
		try {
			Process p = Runtime.getRuntime().exec("chmod 777 " + filePath + fileName);
			status = p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (status == 0) {
			Log.e("liup", "chmod succeed");
		} else {
			Log.e("liup", "chmod failed");
		}

	}

	private void writeFile(String data) {
		try {
			FileOutputStream fout = new FileOutputStream(filePath + fileName);
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();
			fout.close();
			Log.e("liup", "writeFile succcess");
		} catch (Exception e) {
		}
	}

	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			AutoConstant.writeFile("SensorManager open FAIL" + "\n");
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			return;
		}
		String temp = cmdOrder.substring(1);
		// Toast.makeText(this, temp + "", Toast.LENGTH_SHORT).show();
		if (temp.startsWith(AutoConstant.CMD_SENSOR_ACC_CABRATION_START)) {
			Intent intent = new Intent().setClass(this, SensorCalibration.class);
			startActivityForResult(intent, 0);
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_ACC_STEP_GY_START)) {
			startAccStepGySensorService();
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_ACC_STEP_GY_STOP)) {
			stopAccStepGySensorService();
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_LIGHT_ACCHOR_PROX_START)) {
			startSensorService(Sensor.TYPE_LIGHT);
			startSensorService(Sensor.TYPE_PROXIMITY);
			Intent intent = new Intent().setClass(this, SensorCalibration.class);
			startActivityForResult(intent, 0);
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_LIGHT_ACCHOR_PROX_STOP)) {
			if (isAccHoliwritefile && islightwritefile && isproxwritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				if (!islightwritefile) {
					AutoConstant.writeFile("light sensor: FAIL" + "\n");
				}
				if (!isproxwritefile) {
					AutoConstant.writeFile("prox sensor: FAIL" + "\n");
				}
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}
			stopSensorService();
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_PROX_START)) {
			startSensorService(Sensor.TYPE_PROXIMITY);
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_PROX_STOP)) {
			if (isproxwritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				AutoConstant.writeFile("prox sensor: FAIL" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}
			stopSensorService();
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_MAGNETIC_START)) {
			startSensorService(Sensor.TYPE_MAGNETIC_FIELD);
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_MAGNETIC_STOP)) {
			if (ismagneticwritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				AutoConstant.writeFile("magnetic sensor: FAIL" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}
			stopSensorService();
		} else if (temp.startsWith(AutoConstant.CMD_SENSOR_INFRARED_START)) {
			startInfraredService();
		} /*
			 * else if (temp.startsWith(AutoConstant.CMD_SENSOR_INFRARED_STOP))
			 * { AutoConstant .SendDataToService(AutoConstant.RESULT_SUCCUSS,
			 * context); finish(); }
			 */

	}

	void startInfraredService() {
		mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
		if (!mCIR.hasIrEmitter()) {
			return;
		}
		int[] pattern = { 1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625, 442, 625, 442, 625, 468, 625, 442, 625, 494,
				572, 1614, 625, 1588, 625, 1614, 625, 494, 572, 442, 651, 442, 625, 442, 625, 442, 625, 1614, 625, 1588,
				651, 1588, 625, 442, 625, 494, 598, 442, 625, 442, 625, 520, 572, 442, 625, 442, 625, 442, 651, 1588,
				625, 1614, 625, 1588, 625, 1614, 625, 1588, 625, 48958 };
		mCIR.transmit(38400, pattern);

		StringBuilder b = new StringBuilder();
		TextView infraredSensorResultData = (TextView) findViewById(R.id.infrared_sensor_result);
		if (!mCIR.hasIrEmitter()) {
			infraredSensorResultData.setText("No IR Emitter found!");
			AutoConstant.writeFile("Infrared : FAIL" + "\n");
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			finish();
			return;
		}
		ConsumerIrManager.CarrierFrequencyRange[] freqs = mCIR.getCarrierFrequencies();
		b.append("IR Carrier Frequencies:\n");
		for (ConsumerIrManager.CarrierFrequencyRange range : freqs) {
			b.append(String.format("    %d - %d\n", range.getMinFrequency(), range.getMaxFrequency()));
		}
		infraredSensorResultData.setText(b.toString());
		AutoConstant.writeFile("Infrared : PASS" + "\n" + b.toString() + "\n");
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
		finish();
	}

	void stopAccStepGySensorService() {
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
				&& mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
			if (isaccwritefile && isstepwritefile && isgywritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				if (!isaccwritefile) {
					AutoConstant.writeFile("accel sensor: FAIL" + "\n");
					AutoConstant.writeFile(
							"accup = " + accup + ", accleft = " + accleft + ", accright = " + accright + "\n");
				}
				if (!isstepwritefile) {
					AutoConstant.writeFile("step sensor: FAIL" + "\n");
					AutoConstant.writeFile("step ccount = " + ccountbak + "\n");
					ccountbak = 0;
				}
				if (!isgywritefile) {
					AutoConstant.writeFile("gyroscipe sensor: FAIL" + "\n");
					AutoConstant.writeFile("gyup = " + gyup + ", gyleft = " + gyleft + ", gyright = " + gyright + "\n");
				}
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}
		} else if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null
				&& mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
			if (isaccwritefile && isgywritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				if (!isaccwritefile) {
					AutoConstant.writeFile("accel sensor: FAIL" + "\n");
					AutoConstant.writeFile(
							"accup = " + accup + ", accleft = " + accleft + ", accright = " + accright + "\n");
				}
				if (!isgywritefile) {
					AutoConstant.writeFile("gyroscipe sensor: FAIL" + "\n");
					AutoConstant.writeFile("gyup = " + gyup + ", gyleft = " + gyleft + ", gyright = " + gyright + "\n");
				}
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}

		} else if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
				&& mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
			if (isaccwritefile && isstepwritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				if (!isaccwritefile) {
					AutoConstant.writeFile("accel sensor: FAIL" + "\n");
					AutoConstant.writeFile(
							"accup = " + accup + ", accleft = " + accleft + ", accright = " + accright + "\n");
				}
				if (!isstepwritefile) {
					AutoConstant.writeFile("step sensor: FAIL" + "\n");
					AutoConstant.writeFile("step ccount = " + ccountbak + "\n");
					ccountbak = 0;
				}
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}

		} else if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null
				&& mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
			if (isaccwritefile) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
			} else {
				if (!isaccwritefile) {
					AutoConstant.writeFile("accel sensor: FAIL" + "\n");
					AutoConstant.writeFile(
							"accup = " + accup + ", accleft = " + accleft + ", accright = " + accright + "\n");
				}
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			}

		}

		mSensorManager.unregisterListener(mSensorListener, mAccSensor);
		mSensorManager.unregisterListener(mSensorListener, mStepSensor);
		mSensorManager.unregisterListener(mSensorListener, mGySensor);
		finish();
	}

	@Override
	public void finish() {
		unregisterReceiver(mBroadcast);
		super.finish();
	}

	void startAccStepGySensorService() {
		accup = false;
		accdown = false;
		accright = false;
		accleft = false;

		gyup = false;
		gydown = false;
		gyright = false;
		gyleft = false;

		mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (mAccSensor == null) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			return;
		}
		mAccSensorListener = new AccSensorListener(this);
		if (!mSensorManager.registerListener(mAccSensorListener, mAccSensor, SensorManager.SENSOR_DELAY_FASTEST)) {
			mSensorManager.registerListener(mAccSensorListener, mAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
			return;
		}
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
			mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
			if (mStepSensor == null) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
				return;
			}
			mStepSensorListener = new StepSensorListener(this);
			stepccount = 0;
			if (!mSensorManager.registerListener(mStepSensorListener, mStepSensor,
					SensorManager.SENSOR_DELAY_FASTEST)) {
				mSensorManager.registerListener(mStepSensorListener, mStepSensor, SensorManager.SENSOR_DELAY_FASTEST);
				return;
			}
		}
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
			mGySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			if (mGySensor == null) {
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
				return;
			}
			mGySensorListener = new GySensorListener(this);
			if (!mSensorManager.registerListener(mGySensorListener, mGySensor, SensorManager.SENSOR_DELAY_FASTEST)) {
				mSensorManager.registerListener(mGySensorListener, mGySensor, SensorManager.SENSOR_DELAY_FASTEST);
				return;
			}
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);

	}

	public class AccSensorListener implements SensorEventListener {
		public AccSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			synchronized (this) {
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					TextView accelSensorResultData = (TextView) findViewById(R.id.accelerometer_sensor_result);
					if (event.values[0] > 3 && event.values[0] > event.values[1]) {
						accleft = true;
					}
					if (event.values[0] < -3 && event.values[0] < event.values[1]) {
						accright = true;
					}
					if (event.values[1] > 1 && event.values[1] > event.values[0]) {
						accdown = true;
					}
					if (event.values[1] < -1 && event.values[1] < event.values[0]) {
						accup = true;
					}
					if (event.values[0] > 3 && event.values[1] < -1) {
						if (event.values[0] > Math.abs(event.values[1])) {
							accleft = true;
						} else {
							accup = true;
						}
					}
					if (event.values[0] < -3 && event.values[1] > 1) {
						if (Math.abs(event.values[0]) > event.values[1]) {
							accright = true;
						} else {
							accdown = true;
						}
					}
					if (accup && accright && accleft) {
						if (!isaccwritefile) {
							AutoConstant.writeFile("accel sensor: PASS" + "\n" + "accel value :" + "x: "
									+ event.values[0] + "y: " + event.values[1] + "\n");
							isaccwritefile = true;
						}
					}
					accelSensorResultData
							.setText("accel value :" + "x: " + event.values[0] + "y: " + event.values[1] + "\n");
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

	public class StepSensorListener implements SensorEventListener {
		public StepSensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			synchronized (this) {
				if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
					TextView stepSensorResultData = (TextView) findViewById(R.id.step_sensor_result);
					if(stepccount == 0){
						lastValues = event.values[0];
					}
					stepccount ++;
					/*
					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
					}*/
					if (event.values[0] - lastValues >= 5) {
						ccountbak = event.values[0] - lastValues;
						if (!isstepwritefile) {
							AutoConstant.writeFile("step sensor: PASS" + "\n"
									+ "step value: " + event.values[0] + "\n");
							isstepwritefile = true;
							stepccount = 0;
						}
					}
					stepSensorResultData.setText("step value: "
							+ event.values[0] + "\n");
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

	public class GySensorListener implements SensorEventListener {
		public GySensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {
			synchronized (this) {
				if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					TextView gyroscopeSensorResultData = (TextView) findViewById(R.id.gyroscope_sensor_result);
					Log.e("liup", "X(0) = " + event.values[0] + " , Y(1) = " + event.values[1] + " , Z(2) = "
							+ event.values[2]);
					if (event.values[0] > 3) {
						gyup = true;
					} else if (event.values[0] < -3) {
						gydown = true;
					}
					if (event.values[1] > 3) {
						gyright = true;
					} else if (event.values[1] < -3) {
						gyleft = true;
					}
					if (event.values[2] > 3) {
						gyleft = true;
					} else if (event.values[2] < -3) {
						gyright = true;
					}
					if (gyup && gyright && gyleft) {
						if (!isgywritefile) {
							AutoConstant.writeFile("gyroscipe sensor: PASS" + "\n" + "gyroscipe value: " + "x: "
									+ event.values[0] + "y: " + event.values[1] + "z: " + event.values[2] + "\n");
							isgywritefile = true;
						}

					}
					gyroscopeSensorResultData.setText("gyroscipe value: " + "x: " + event.values[0] + "y: "
							+ event.values[1] + "z: " + event.values[2]);
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

	void startSensorService(int sensorType) {
		up = false;
		down = false;
		right = false;
		left = false;
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			return;
		}
		mSensor = mSensorManager.getDefaultSensor(sensorType);
		if (mSensor == null) {
			AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, context);
			return;
		}
		mSensorListener = new SensorListener(this);
		if (!mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST)) {
			mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
			return;
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, context);
	}

	void stopSensorService() {
		mSensorManager.unregisterListener(mSensorListener, mSensor);
		finish();
	}
	String pSensorValue = "prox value: ";
	public class SensorListener implements SensorEventListener {
		public SensorListener(Context context) {
			super();
		}

		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				TextView lightSensorResultData = (TextView) findViewById(R.id.light_sensor_result);
				TextView proxSensorResultData = (TextView) findViewById(R.id.prox_sensor_result);
				TextView magneticSensorResultData = (TextView) findViewById(R.id.magnetic_sensor_result);
				if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
					}
					if (ccount >= 3 && event.values[0] <= 200) {
						if (!islightwritefile) {
							AutoConstant
									.writeFile("light sensor: PASS" + "\n" + "light value: " + event.values[0] + "\n");
							islightwritefile = true;
						}
						ccount = 0;
					}
					lightSensorResultData.setText("light value: " + event.values[0] + "\n");
				} else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
					}
					pSensorValue += ", " + event.values[0];
					if (ccount >= 2) {
						if (!isproxwritefile) {
							AutoConstant.writeFile("prox sensor: PASS" + "\n"
									+ pSensorValue + "\n");
							isproxwritefile = true;
						}
						ccount = 0;
					}
					proxSensorResultData.setText("prox value: " + event.values[0] + "\n");
				} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					if (event.values[0] != lastValues) {
						ccount++;
						lastValues = event.values[0];
					}
					if (ccount >= 3) {
						if (!ismagneticwritefile) {
							AutoConstant.writeFile(
									"magnetic sensor: PASS" + "\n" + "magetic value: " + event.values[0] + "\n");
							ismagneticwritefile = true;
						}
						ccount = 0;
					}
					magneticSensorResultData.setText("magetic value: " + event.values[0] + "\n");
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

}
