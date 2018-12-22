package com.prize.factorytest;

import com.prize.factorytest.FingerPrint.FingerPrint;
import com.prize.factorytest.BlueTooth.BlueTooth;
import com.prize.factorytest.Brightness.Brightness;
import com.prize.factorytest.CameraBack.CameraBack;
import com.prize.factorytest.CameraFront.CameraFront;
import com.prize.factorytest.CameraBackSub.CameraBackSub;
import com.prize.factorytest.CameraBackSub.CameraBackAlsSub;
import com.prize.factorytest.Charger.Charger;
import com.prize.factorytest.FM.FM;
import com.prize.factorytest.GPS.GPS;
import com.prize.factorytest.GSensor.GSensor;
import com.prize.factorytest.Hall.Hall;
import com.prize.factorytest.Headset.Headset;
import com.prize.factorytest.Infrared.Infrared;
import com.prize.factorytest.Key.Key;
import com.prize.factorytest.LCD.LCD;
import com.prize.factorytest.LED.LED;
import com.prize.factorytest.YCD.YCD;
import com.prize.factorytest.LSensor.LSensor;
import com.prize.factorytest.MIC.MIC;
import com.prize.factorytest.MICRe.MICRe;
import com.prize.factorytest.MSensor.MSensor;
import com.prize.factorytest.CSensor.CSensor;
import com.prize.factorytest.PSensor.PSensor;
import com.prize.factorytest.Phone.Phone;
import com.prize.factorytest.RAM.RAM;
import com.prize.factorytest.Receiver.Receiver;
import com.prize.factorytest.SDCard.SDCard;
import com.prize.factorytest.SIM.SIM;
import com.prize.factorytest.Speaker.Speaker;
import com.prize.factorytest.Torchled.Torchled;
import com.prize.factorytest.TorchledFront.TorchledFront;
import com.prize.factorytest.TouchPanelEdge.TouchPanelEdge;
import com.prize.factorytest.Vibrate.Vibrate;
import com.prize.factorytest.WiFi.WiFi;
import com.prize.factorytest.GySensor.GySensor;
import com.prize.factorytest.OTG.OTG;
import com.prize.factorytest.NFC.NFC;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.util.Log;
import android.os.SystemProperties;


public class PrizeFactoryTestListActivity extends Activity implements
		OnItemSelectedListener, OnItemClickListener {
	private LayoutInflater mInflater;
	FunnyLookingAdapter mFunnyLookingAdapter;
	final static int[] resultCodeList = new int[99];
	int mItem = 0;
	Button autoTestButton = null;
	Button testReportButton = null;
	Button factorySetButton = null;
	Button softInfoButton = null;
	public static boolean toStartAutoTest = false;
	public static int itempos = 0;
	
	public static String[] items = new String[29];
	public static String[] testReportresult = new String[99];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.factory_test_list);
		mInflater = LayoutInflater.from(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		GridView grid = (GridView) findViewById(R.id.grid);
		mFunnyLookingAdapter = new FunnyLookingAdapter(this,
				android.R.layout.simple_list_item_1, items);
		grid.setAdapter(mFunnyLookingAdapter);
		grid.setOnItemSelectedListener(this);
		grid.setOnItemClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (toStartAutoTest == true) {
			if (itempos == 0) {
				String autoappname = items[itempos];
				itempos = 1;
				if (resultCode == RESULT_OK) {
					resultCodeList[itempos - 1] = 1;
				} else {
					resultCodeList[itempos - 1] = 2;
				}
				FactoryTestList(autoappname);
				mFunnyLookingAdapter.notifyDataSetChanged();
				return;
			}
			if (itempos == 1) {
				if (resultCode == RESULT_OK) {
					resultCodeList[itempos - 1] = 1;
				} else if (resultCode == RESULT_CANCELED) {
					resultCodeList[itempos - 1] = 2;
				}
				mFunnyLookingAdapter.notifyDataSetChanged();
			}
			if (itempos < items.length) {
				String autoappname = items[itempos];
				if (resultCode == RESULT_OK) {
					resultCodeList[itempos - 1] = 1;
				} else if (resultCode == RESULT_CANCELED) {
					resultCodeList[itempos - 1] = 2;
				}
				FactoryTestList(autoappname);
				mFunnyLookingAdapter.notifyDataSetChanged();
			} else {
				toStartAutoTest = false;
				if (resultCode == RESULT_OK) {
					resultCodeList[itempos - 1] = 1;
				} else {
					resultCodeList[itempos - 1] = 2;
				}
				mFunnyLookingAdapter.notifyDataSetChanged();
			}
			if (resultCodeList[itempos - 1] == 1) {
				testReportresult[itempos] = getResources().getString(R.string.result_normal);
			}
			if (resultCodeList[itempos - 1] == 2) {
				testReportresult[itempos] = getResources().getString(R.string.result_error);
			}

			return;
		}

		if (resultCode == RESULT_OK) {
			resultCodeList[mItem] = 1;
		} else if (resultCode == RESULT_CANCELED) {
			resultCodeList[mItem] = 2;
		} else if (resultCode == 3) {
			String appname = items[mItem];
			FactoryTestList(appname);
			return;
		}

		mFunnyLookingAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		String appname = items[position];
		mItem = position;
		toStartAutoTest = false;
		FactoryTestList(appname);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public void FactoryTestList(String TestListName) {
		if(null == TestListName){
			return;
		}
		if (TestListName.equals(getResources().getString(R.string.touch_screen))) {
			Intent intent = new Intent().setClass(this, TouchPanelEdge.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_sim))) {
			Intent intent = new Intent().setClass(this, SIM.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_lcd))) {
			Intent intent = new Intent().setClass(this, LCD.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_led))) {
			Intent intent = new Intent().setClass(this, LED.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_ycd))) {
			Intent intent = new Intent().setClass(this, YCD.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.flash_lamp))) {
			Intent intent = new Intent().setClass(this, Torchled.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.flash_lamp_front))) {
			Intent intent = new Intent().setClass(this, TorchledFront.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.motor))) {
			Intent intent = new Intent().setClass(this, Vibrate.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.keys))) {
			Intent intent = new Intent().setClass(this, Key.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.headset))) {
			Intent intent = new Intent().setClass(this, Headset.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.receiver))) {
			Intent intent = new Intent().setClass(this, Receiver.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.speaker))) {
			Intent intent = new Intent().setClass(this, Speaker.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.radio))) {
			Intent intent = new Intent().setClass(this, FM.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.microphone_loop))) {
			Intent intent = new Intent().setClass(this, MIC.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.microphone))) {
			Intent intent = new Intent().setClass(this, MICRe.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.TF_card))) {
			Intent intent = new Intent().setClass(this, SDCard.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.ram))) {
			Intent intent = new Intent().setClass(this, RAM.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.rear_camera_sub))) {
			Intent intent = null;
			if(SystemProperties.get("ro.prize_rear_camera_sub").equals("1")){
				intent = new Intent().setClass(this, CameraBackSub.class);
			}else if(SystemProperties.get("ro.prize_rear_camera_sub_als").equals("1")){
				intent = new Intent().setClass(this, CameraBackAlsSub.class);
			}
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.rear_camera))) {
			Intent intent = new Intent().setClass(this, CameraBack.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.front_camera))) {
			Intent intent = new Intent().setClass(this, CameraFront.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.battery))) {
			Intent intent = new Intent().setClass(this, Charger.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.light_sensor))) {
			Intent intent = new Intent().setClass(this, LSensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.phone))) {
			Intent intent = new Intent().setClass(this, Phone.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.backlight))) {
			Intent intent = new Intent().setClass(this, Brightness.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.gravity_sensor))) {
			Intent intent = new Intent().setClass(this, GSensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.fingerprint))) {
			Intent intent = new Intent().setClass(this, FingerPrint.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.rang_sensor))) {
			Intent intent = new Intent().setClass(this, PSensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.magnetic_sensor))) {
			Intent intent = new Intent().setClass(this, MSensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.step_counter_sensor))) {
			Intent intent = new Intent().setClass(this, CSensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.gysensor_name))) {
			Intent intent = new Intent().setClass(this, GySensor.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.otg))) {
			Intent intent = new Intent().setClass(this, OTG.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_wifi))) {
			Intent intent = new Intent().setClass(this, WiFi.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.bluetooth))) {
			Intent intent = new Intent().setClass(this, BlueTooth.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_gps))) {
			Intent intent = new Intent().setClass(this, GPS.class);
			startActivityForResult(intent, 0);
		}

		if (TestListName.equals(getResources().getString(R.string.hall_sensor))) {
			Intent intent = new Intent().setClass(this, Hall.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.infrared))) {
			Intent intent = new Intent().setClass(this, Infrared.class);
			startActivityForResult(intent, 0);
		}
		if (TestListName.equals(getResources().getString(R.string.prize_nfc))) {
			Intent intent = new Intent().setClass(this, NFC.class);
			startActivityForResult(intent, 0);
		}
	}

	private class FunnyLookingAdapter extends ArrayAdapter<String> {
		private String[] theItems;

		FunnyLookingAdapter(Context context, int resource, String[] items) {
			super(context, resource, items);
			theItems = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView label;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item, null);
				WindowManager wm = getWindowManager();
				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();

				convertView.setLayoutParams(new GridView.LayoutParams(
						(width - 10) / 3, height / 11));

			}
			label = (TextView) convertView.findViewById(R.id.item_red);
			if (resultCodeList[position] == 0) {
				label.setBackgroundResource(R.drawable.gray_button);
			} else if (resultCodeList[position] == 1) {
				testReportresult[position] = getResources().getString(R.string.result_normal);
				label.setBackgroundResource(R.drawable.green_button);
			} else if (resultCodeList[position] == 2) {
				testReportresult[position] = getResources().getString(R.string.result_error);
				label.setBackgroundResource(R.drawable.red_button);
			}
			label.setText(theItems[position]);
			return convertView;
		}
	}

}
