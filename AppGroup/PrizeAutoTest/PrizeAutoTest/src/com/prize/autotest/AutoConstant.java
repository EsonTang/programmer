package com.prize.autotest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

public class AutoConstant {
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";

	private static final String filePath = "/sdcard/prize_backup/";
	private static final String fileName = "prize_autotest_data";

	public static final String ACTION_UI = "com.prize.autotest.ACTION_UI";
	public static final String ACTION_SERVICE = "com.prize.autotest.ACTION_SERVICE";

	// CAMERA CMD ORDER
	public static final String CMD_CAMERA = "1";

	public static final String CMD_CAMERA_FRONT_OPEN = "11";
	public static final String CMD_CAMERA_FRONT_CLOSE = "10";
	public static final String CMD_CAMERA_FRONT_SHUTTER = "12";
	public static final String CMD_CAMERA_BACK_OPEN = "01";
	public static final String CMD_CAMERA_BACK_CLOSE = "00";
	public static final String CMD_CAMERA_BACK_FOCUS = "03";
	public static final String CMD_CAMERA_BACK_SHUTTER = "02";
	public static final String CMD_CAMERA_SUCCESS = "33";
	public static final String CMD_CAMERA_FAIL = "44";

	// AUDIO CMD ORDER
	public static final String CMD_AUDIO = "2";

	public static final String CMD_AUDIO_MIC = "12";
	public static final String CMD_AUDIO_MIC_SUB = "32";
	public static final String CMD_AUDIO_PLAYER = "23";
	public static final String CMD_AUDIO_RECEIVER = "21";
	public static final String CMD_AUDIO_HEADSET = "22";
	public static final String CMD_AUDIO_FINISH = "00";
	public static final String CMD_AUDIO_FAIL = "44";
	public static final String CMD_AUDIO_SUCCESS = "33";

	// MMI CMD ORDER
	public static final String CMD_MMI = "3";

	// message
	public static final String CMD_MMI_MESSAGE = "00";

	// key
	public static final String CMD_MMI_KEY_START = "10";
	public static final String CMD_MMI_KEY_RESULT = "11";

	// tp
	public static final String CMD_MMI_TP_START = "20";
	public static final String CMD_MMI_TP_RESULT = "21";

	// wlan bt gps
	public static final String CMD_MMI_WBG_START = "30";
	public static final String CMD_MMI_WBG_RESULT = "31";
	public static final String CMD_MMI_WBG_STOP = "32";

	// otg
	public static final String CMD_MMI_OTG_START = "40";
	public static final String CMD_MMI_OTG_RESULT = "41";

	// finger
	public static final String CMD_MMI_FINGER_START = "50";
	public static final String CMD_MMI_FINGER_RESULT = "51";

	// vibrate
	public static final String CMD_MMI_VIBRATE_START = "60";
	public static final String CMD_MMI_VIBRATE_STOP = "61";

	// torch
	public static final String CMD_MMI_TORCH_START = "70";
	public static final String CMD_MMI_TORCH_STOP = "71";
	public static final String CMD_MMI_TORCH_FRONT_START = "72";
	public static final String CMD_MMI_TORCH_FRONT_STOP = "73";

	// SENSOR CMD ORDER
	public static final String CMD_SENSOR = "4";

	public static final String CMD_SENSOR_ACC_CABRATION_START = "00";
 	
	public static final String CMD_SENSOR_ACC_STEP_GY_START = "10";
 	public static final String CMD_SENSOR_ACC_STEP_GY_STOP = "11";
 	
	//public static final String CMD_SENSOR_ACCESS_START = "10";
	//public static final String CMD_SENSOR_ACCESS_STOP = "11";

	//public static final String CMD_SENSOR_LIGHT_START = "20";
	//public static final String CMD_SENSOR_LIGHT_STOP = "21";
 	public static final String CMD_SENSOR_LIGHT_ACCHOR_PROX_START = "20";
 	public static final String CMD_SENSOR_LIGHT_ACCHOR_PROX_STOP = "21";

	public static final String CMD_SENSOR_PROX_START = "30";
	public static final String CMD_SENSOR_PROX_STOP = "31";

	public static final String CMD_SENSOR_STEP_START = "40";
	public static final String CMD_SENSOR_STEP_STOP = "41";

	public static final String CMD_SENSOR_MAGNETIC_START = "50";
	public static final String CMD_SENSOR_MAGNETIC_STOP = "51";

	public static final String CMD_SENSOR_GYROSCOPE_START = "60";
	public static final String CMD_SENSOR_GYROSCOPE_STOP = "61";

	public static final String CMD_SENSOR_INFRARED_START = "70";
	public static final String CMD_SENSOR_INFRARED_STOP = "71";

	public static final String CMD_SENSOR_HALL_START = "80";
	public static final String CMD_SENSOR_HALL_STOP = "81";

	public static final String CMD_SENSOR_NFC_START = "90";
	public static final String CMD_SENSOR_NFC_STOP = "91";

	// LCD CMD ORDER
	public static final String CMD_LCD = "5";

	public static final String CMD_LCD_WHITE = "10";
	public static final String CMD_LCD_RED = "11";
	public static final String CMD_LCD_GREEN = "12";
	public static final String CMD_LCD_BLUE = "13";
	public static final String CMD_LCD_BLACK = "14";
	public static final String CMD_LCD_GRAY = "15";
	public static final String CMD_LCD_BRIGNESS_SLOW = "16";
	public static final String CMD_LCD_SLEEP = "17";
	public static final String CMD_LCD_SUCCESS = "18";
	public static final String CMD_LCD_FAIL = "19";

	
	// MMI SENSOR RESULT
	public static final String CMD_MMI_SENSOR = "6";
	public static final String CMD_MMI_SENSOR_SUCCESS = "00";
	public static final String CMD_MMI_SENSOR_FAIL = "10";
		
		
	public static final String RESULT_FAIL = "0";
	public static final String RESULT_LOW_BATTERY = "3";
	public static final String RESULT_SUCCUSS = "2";
	public static final String RESULT_FACTORYTEST_FAIL = "4";

	public static void SendDataToService(String msg, Context context) {
		Intent intent = new Intent(context, SocketService.class);
		intent.setAction(AutoConstant.ACTION_SERVICE);
		intent.putExtra("msg", msg);
		context.startService(intent);
	}

	public static void creatFile() {
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
			Process p = Runtime.getRuntime().exec(
					"chmod 777 " + filePath + fileName);
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

	public static void writeFile(String data) {
		try {
			FileOutputStream fout = new FileOutputStream(filePath + fileName,
					true);
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();
			fout.close();
			Log.e("liup", "writeFile succcess");
		} catch (Exception e) {
		}
	}

	public static void writeProInfo(String sn, int index) {
		if (null == sn || sn.length() < 1) {
			return;
		}
		try {
			int flag = 0;
			byte[] buff = null;
			IBinder binder = ServiceManager.getService("NvRAMAgent");
			NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);

			try {
				buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] by = sn.toString().getBytes();

			for (int i = 0; i < 50; i++) {
				if (buff[i] == 0x00) {
					buff[i] = " ".toString().getBytes()[0];
				}
			}

			buff[index] = by[0];
			try {
				flag = agent.writeFileByName(PRODUCT_INFO_FILENAME, buff);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readProInfo(int index) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		char c = (char) buff[index];
		String sn = new String(buff);
		return String.valueOf((char) buff[index]);
	}
}
