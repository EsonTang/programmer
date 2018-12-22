package com.prize.autotest.mmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StatFs;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.TextView;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.IBinder;
import com.prize.autotest.NvRAMAgent;

import android.os.ServiceManager;

public class AutoVersionTestActivity extends Activity {
	private TextView mVersion;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";

	private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
	private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

	final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
	final String STATUS = "/sys/class/power_supply/battery/status";
	private File mChargingCurrentPath = new File(
			"/sys/devices/platform/battery/charging_current_value");

	private Handler mhandle;
	private Timer mTimer;

	String chargesate;
	String currentelectricity;
	String chargermax;
	String batterytemperature;
	String batterytype;
	String currentvoltage;
	String chargercurrent;
	String chargercurrent1;

	public static final boolean MTK_2SDCARD_SWAP = SystemProperties.get(
			"ro.mtk_2sdcard_swap").equals("1");
	private String externalMemoryTextView;
	private String sdCard2MemoryTextView;

	private SubscriptionManager mSubscriptionManager;

	private boolean iswritefile = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.version);
		mVersion = (TextView) findViewById(R.id.version_show);

		startChargeHandler();
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);

		// ---------------------charge------------------
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
		registerReceiver(this.mChargeBroadcastReceiver, localIntentFilter);
		if (mTimer == null) {
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.e("AutoVersionTestActivity", "-------run()--------");
				mhandle.sendEmptyMessage(0x123);
			}
			}, 0, 1000);
		}

	}

	@Override
	protected void onDestroy() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		unregisterReceiver(mChargeBroadcastReceiver);

		super.onDestroy();
	}

	public void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}
	// -----------------------------Charge----------------------------------
	int chargecount = 0;
	int chargingMax = 0;
	private void startChargeHandler() {
		mhandle = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					int current =scanChargingCurrent(); 
					if(current > chargingMax){
						chargingMax = current;
						Log.e("AutoVersionTestActivity", "chargingMax = " + chargingMax);
					}
					
					chargercurrent = getString(R.string.charger_current)
							+ " : " + Integer.toString(chargingMax)
							+ "mA";
					chargercurrent1 = getString(R.string.charger_current) + " : " + scanChargingCurrent() + "mA";

					String ChargeMessage = chargesate + "\n"
							+ currentelectricity + "\n" + chargermax + "\n"
							+ batterytemperature + "\n" + batterytype + "\n"
							+ currentvoltage + "\n" + chargercurrent + "\n";
					String ChargeMessage1 = chargesate + "\n" + currentelectricity + "\n" + chargermax + "\n"
							+ batterytemperature + "\n" + batterytype + "\n" + currentvoltage + "\n" + chargercurrent1
							+ "\n";

					String versionMessage = getVersionInfo();
					String RamMessage = getRamInfo();
					String TFCardMessage = getTFCardInfo();
					String SIMCardMessage = getSIMCardInfo();

					mVersion.setText(versionMessage + RamMessage + ChargeMessage1 + TFCardMessage + SIMCardMessage);
					
					Log.e("AutoVersionTestActivity", "---handleMessage-------chargingMax==="+chargingMax+"  chargecount=="+chargecount);	
					if (chargecount < 8) {
						chargecount ++;
						return;
					}
					if (!iswritefile) {
						AutoConstant.writeFile(versionMessage + RamMessage
								+ ChargeMessage + TFCardMessage
								+ SIMCardMessage);
						iswritefile = true;
					}
				}
			}
		};
	}

	private int scanChargingCurrent() {
		try {
			Scanner scan = new Scanner(mChargingCurrentPath);
			int val = scan.nextInt();
			scan.close();
			Log.e("AutoVersionTestActivity", "val- = " + val);
			return val;
		} catch (Exception e) {
		}
		return 0;
	}

	private BroadcastReceiver mChargeBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context paramContext, Intent paramIntent) {
			int level;
			int scale;
			int voltage;
			int temperature;
			String technology;
			String state;

			paramIntent.getBooleanExtra("present", false);
			paramIntent.getIntExtra("icon-small", 0);

			state = getBatteryState(STATUS);
			if (state.equals("Not charging")) {
				state = getString(R.string.not_charging);
			} else if (state.equals("Charging")) {
				state = getString(R.string.charging);
			} else if (state.equals("Full")) {
				state = getString(R.string.charging_finish);
			}
			level = paramIntent.getIntExtra("level", 0);
			scale = paramIntent.getIntExtra("scale", 0);
			temperature = paramIntent.getIntExtra("temperature", 0);
			technology = paramIntent.getStringExtra("technology");
			voltage = paramIntent.getIntExtra("voltage", 0);

			chargesate = getString(R.string.charger_state) + " : " + state;
			currentelectricity = getString(R.string.current_electricity)
					+ " : " + String.valueOf(level) + "%";
			chargermax = getString(R.string.charger_max) + " : "
					+ String.valueOf(scale) + "%";
			batterytemperature = getString(R.string.battery_temperature)
					+ " : " + String.valueOf(temperature / 10.0D) + " "
					+ getResources().getString(R.string.degrees_centigrade);
			batterytype = getString(R.string.battery_type) + " : " + technology;
			currentvoltage = getString(R.string.current_voltage) + " : "
					+ String.valueOf(voltage) + "mV";

			return;
		}
	};

	@SuppressWarnings("resource")
	private String getBatteryState(String path) {

		File mFile;
		FileReader mFileReader;
		mFile = new File(path);

		try {
			mFileReader = new FileReader(mFile);
			char data[] = new char[128];
			int charCount;
			String status[] = null;
			try {
				charCount = mFileReader.read(data);
				status = new String(data, 0, charCount).trim().split("\n");
				return status[0];
			} catch (IOException e) {

			}
		} catch (FileNotFoundException e) {

		}
		return null;
	}

	// ---------------------------TF-CARD----------------------------------------
	private String getTFCardInfo() {
		if (MTK_2SDCARD_SWAP) {
			if ((getTotalExternalMemorySize() != 0)
					&& (getTotalSDCard2MemorySize() != 0)) {
				externalMemoryTextView = getString(R.string.sdcard)
						+ getString(R.string.detected) + "\n"
						+ getString(R.string.total_volume)
						+ getTotalExternalMemorySize() + "MB" + "\n"
						+ getString(R.string.available_volume)
						+ getAvailableExternalMemorySize() + "MB";
				sdCard2MemoryTextView = getString(R.string.internal_storage)
						+ getString(R.string.detected) + "\n"
						+ getString(R.string.total_volume)
						+ getTotalSDCard2MemorySize() + "MB" + "\n"
						+ getString(R.string.available_volume)
						+ getAvailableSDCard2MemorySize() + "MB";
			} else {
				if (getTotalExternalMemorySize() != 0) {
					externalMemoryTextView = getString(R.string.internal_storage)
							+ getString(R.string.detected)
							+ "\n"
							+ getString(R.string.total_volume)
							+ getTotalExternalMemorySize()
							+ "MB"
							+ "\n"
							+ getString(R.string.available_volume)
							+ getAvailableExternalMemorySize() + "MB";
					sdCard2MemoryTextView = getString(R.string.internal_storage)
							+ getString(R.string.not_detected);
				}
			}
		} else {
			if (getTotalSDCard2MemorySize() != 0
					&& getTotalExternalMemorySize() != 0) {
			}
			if (getTotalSDCard2MemorySize() != 0) {
				sdCard2MemoryTextView = getString(R.string.sdcard)
						+ getString(R.string.detected) + "\n"
						+ getString(R.string.total_volume)
						+ getTotalSDCard2MemorySize() + "MB" + "\n"
						+ getString(R.string.available_volume)
						+ getAvailableSDCard2MemorySize() + "MB";
			} else {
				sdCard2MemoryTextView = getString(R.string.sdcard)
						+ getString(R.string.not_detected);
			}
			if (getTotalExternalMemorySize() != 0) {
				externalMemoryTextView = getString(R.string.internal_storage)
						+ getString(R.string.detected) + "\n"
						+ getString(R.string.total_volume)
						+ getTotalExternalMemorySize() + "MB" + "\n"
						+ getString(R.string.available_volume)
						+ getAvailableExternalMemorySize() + "MB";
			} else {
				externalMemoryTextView = getString(R.string.internal_storage)
						+ getString(R.string.not_detected);
			}

		}
		return externalMemoryTextView + "\n" + sdCard2MemoryTextView + "\n";
	}

	private String getExternalMemoryPath() {
		return "/mnt/sdcard";
	}

	private String getSDCard2MemoryPath() {
		return "/mnt/m_external_sd";// mnt/sdcard2
	}

	private StatFs getStatFs(String path) {
		try {
			return new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private int calculateAvailableSizeInMB(StatFs stat) {
		if (stat != null)
			return (int) (stat.getAvailableBlocks() * (stat.getBlockSize() / (1024f * 1024f)));

		return 0;
	}

	@SuppressWarnings("deprecation")
	private int calculateTotalSizeInMB(StatFs stat) {
		if (stat != null)
			return (int) (stat.getBlockCount() * (stat.getBlockSize() / (1024f * 1024f)));

		return 0;
	}

	private int getTotalExternalMemorySize() {
		String path = getExternalMemoryPath();
		StatFs stat = getStatFs(path);
		return calculateTotalSizeInMB(stat);
	}

	private int getTotalSDCard2MemorySize() {
		String path = getSDCard2MemoryPath();
		StatFs stat = getStatFs(path);
		return calculateTotalSizeInMB(stat);
	}

	private int getAvailableExternalMemorySize() {
		String path = getExternalMemoryPath();
		StatFs stat = getStatFs(path);
		return calculateAvailableSizeInMB(stat);
	}

	private int getAvailableSDCard2MemorySize() {
		String path = getSDCard2MemoryPath();
		StatFs stat = getStatFs(path);
		return calculateAvailableSizeInMB(stat);
	}

	// ---------------------------SIM-CARD----------------------------------------
	private String getSIMCardInfo() {
		mSubscriptionManager = SubscriptionManager.from(this);
		final SubscriptionInfo IMSI1 = mSubscriptionManager
				.getActiveSubscriptionInfoForSimSlotIndex(0);
		final SubscriptionInfo IMSI2 = mSubscriptionManager
				.getActiveSubscriptionInfoForSimSlotIndex(1);
		String sim1String, sim2String;
		if (IMSI1 != null && !IMSI1.equals("")) {
			sim1String = "SIM1" + ":pass";
		} else {
			sim1String = "SIM1" + ":fail";
		}
		if (IMSI2 != null && !IMSI2.equals("")) {
			sim2String = "SIM2" + ":pass";
		} else {
			sim2String = "SIM2" + ":fail";
		}
		return sim1String + "\n" + sim2String + "\n";
	}

	// ---------------------------RAM----------------------------------------
	private String getRamInfo() {
		return getString(R.string.total_memory)
				+ formatFileSize(getTotalMemorySize(this), true) + "\n"
				+ getString(R.string.available_memory)
				+ formatFileSize(getAvailableMemory(this), true) + "\n";
	}

	public static long getTotalMemorySize(Context context) {
		String dir = "/proc/meminfo";
		try {
			FileReader fr = new FileReader(dir);
			BufferedReader br = new BufferedReader(fr, 2048);
			String memoryLine = br.readLine();
			String subMemoryLine = memoryLine.substring(memoryLine
					.indexOf("MemTotal:"));
			br.close();
			return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static long getAvailableMemory(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem;
	}

	public static String formatFileSize(long size, boolean isInteger) {
		DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
		String fileSizeString = "0M";
		if (size < 1024 && size > 0) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1024 * 1024) {
			fileSizeString = df.format((double) size / 1024) + "K";
		} else {
			fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
		}
		return fileSizeString;
	}

	// ---------------------------version------------------------------------------------------------
	private String getVersionInfo() {
		String temp = null;
		StringBuilder info = new StringBuilder();

		info.append("[SN] : ");
		temp = null;
		temp = SystemProperties.get("gsm.serial");
		String pcba = readProInfo(49);
		String mobile = readProInfo(45);

		if (null != temp) {
			if (temp.length() > 50) {
				info.append(temp.substring(0, 45) + mobile
						+ temp.substring(46, 49) + pcba + temp.substring(50));
			} else if (temp.length() == 50) {
				info.append(temp.substring(0, 45) + mobile
						+ temp.substring(46, 49) + pcba);
			} else if (temp.length() > 46 && temp.length() < 50) {
				info.append(temp.substring(0, 45) + mobile + temp.substring(46));
			} else if (temp.length() == 46) {
				info.append(temp.substring(0, 45) + mobile);
			} else if (temp.length() > 0 && temp.length() < 46) {
				info.append(temp.substring(0));
			}
		} else {
			info.append(temp);
		}
		info.append(getString(R.string.calibration));
		if (getCalibrationInfo(temp))
			info.append(getString(R.string.calibration_yes));
		else
			info.append(getString(R.string.calibration_no));

		if (SystemProperties.get("ro.prize_board_network_type").equals("6M")) {
			String meid = SystemProperties.get("gsm.mtk.meid");
			info.append("\n[MEID] : ");
			temp = null;
			if (null == meid) {
				temp = meid;
			} else {
				temp = meid.toUpperCase();
			}
			info.append(temp);
		}
		String imei1 = SystemProperties.get("gsm.mtk.imei1");
		String imei2 = SystemProperties.get("gsm.mtk.imei2");

		info.append("\n[IMEI1] : ");
		temp = null;
		temp = imei1;// mTelMgr.getDeviceId(0);
		info.append(temp);

		info.append("\n[IMEI2] : ");
		temp = null;
		temp = imei2;// mTelMgr.getDeviceId(1);
		info.append(temp);

		info.append("\n[Build Type] : ");
		temp = null;
		temp = Build.TYPE;
		info.append(temp);

		info.append("\n[Build Brand] : ");
		temp = null;
		 temp = Build.BRAND;
		//temp = "Love 816";
		info.append(temp);

		info.append("\n[Build Model] : ");
		temp = null;
		//temp = "Love 816";
		temp = Build.MODEL;
		info.append(temp);

		info.append("\n[Android Version] : ");
		temp = null;
		temp = Build.VERSION.RELEASE;
		info.append(temp);

		info.append("\n[Build Data] : ");
		temp = null;
		temp = SystemProperties.get("ro.build.date");
		info.append(temp);

		info.append("\n[Baseband Version] : ");
		temp = null;
		temp = SystemProperties.get("gsm.version.baseband");
		info.append(temp);

		info.append("\n[MTK Version] : ");
		temp = null;
		temp = SystemProperties.get("ro.mediatek.version.release");
		info.append(temp);

		info.append("\n[Version] : ");
		temp = null;
		temp = Build.DISPLAY;
		info.append(temp);

		info.append("\n");

		return info.toString();
	}

	private boolean getCalibrationInfo(String temp) {
		if (null != temp && temp.length() >= 63) {
			if (!temp.substring(62, 63).equals("P"))
				return false;
		} else
			return false;

		if (SystemProperties.get("ro.prize_board_network_type").equals("6M")
				|| SystemProperties.get("ro.prize_board_network_type").equals(
						"5M")) {
			if (null != temp && temp.length() >= 61) {
				if (!temp.substring(60, 61).equals("P"))
					return false;
			} else
				return false;
		}

		if (null != temp && temp.length() >= 59) {
			if (!temp.substring(58, 59).equals("P"))
				return false;
		} else
			return false;

		if (SystemProperties.get("ro.prize_board_network_type").equals("6M")) {
			if (null != temp && temp.length() >= 57) {
				if (!temp.substring(56, 57).equals("P"))
					return false;
			} else
				return false;
		}

		if (null != temp && temp.length() >= 55) {
			if (!temp.substring(54, 55).equals("P"))
				return false;
		} else
			return false;

		if (SystemProperties.get("ro.prize_board_network_type").equals("6M")
				|| SystemProperties.get("ro.prize_board_network_type").equals(
						"5M")) {
			if (null != temp && temp.length() >= 53) {
				if (!temp.substring(52, 53).equals("P"))
					return false;
			} else
				return false;
		}

		return true;
	}

	private String readProInfo(int index) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf((char) buff[index]);
	}
}