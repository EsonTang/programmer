package com.prize.factorytest;

import java.sql.Date;
import java.text.SimpleDateFormat;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.graphics.Color;
import java.util.HashMap;
import android.content.Context;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.os.SystemProperties;

import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
import com.prize.factorytest.Version.Version;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.IBinder;
import com.prize.factorytest.NvRAMAgent;
import android.os.ServiceManager;

public class FactoryTestReportQr extends Activity {
	private TextView mTestReportResult;
	private TextView mTestReportResultItem;
	private TextView mSnNumber;
	private ImageView mImageViewQr;
	private Button factorySetButton = null;
	private Button softInfoButton = null;
	private String snNumber = null;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		LinearLayout VersionLayout = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.testreport_qr, null);
		setContentView(VersionLayout);
		
		snNumber = SystemProperties.get("gsm.serial");
		if(null!=snNumber && snNumber.length()>=14){
			snNumber = SystemProperties.get("gsm.serial").substring(0,14);
		}
		
		mTestReportResult = (TextView) findViewById(R.id.testreport_result);
		mTestReportResult.setText(getTestReportResult());
		
		mTestReportResultItem = (TextView) findViewById(R.id.testreport_result_item);
		mTestReportResultItem.setText(getTestReportResultItem());
		
		mSnNumber = (TextView) findViewById(R.id.sn_number);
		mSnNumber.setText("sn" + ":" + snNumber);

		makeQRCode(getTestReportQr());
		initKeyEvent();
	}
		
	private void makeQRCode(String content) {
		mImageViewQr = (ImageView) findViewById(R.id.prize_image_view);
		try {
			Bitmap qrcodeBitmap = EncodingHandler.createQRCode(content, 400);
			mImageViewQr.setImageBitmap(qrcodeBitmap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void initKeyEvent(){
		factorySetButton = (Button) findViewById(R.id.factoryset);
		softInfoButton = (Button) findViewById(R.id.softinfo);
		factorySetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						FactoryTestReportQr.this);
				dialog.setCancelable(false)
						.setTitle(R.string.factoryset)
						.setMessage(R.string.factoryset_confirm)
						.setPositiveButton

						(R.string.confirm,
								new DialogInterface.OnClickListener() {

									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										Intent intent = new Intent(
												"android.intent.action.MASTER_CLEAR");
										intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
										intent.putExtra(
												"android.intent.extra.REASON",
												"MasterClearConfirm");
										intent.putExtra("shutdown", true);
										sendBroadcast(intent);

									}
								})
						.setNegativeButton

						(R.string.cancel,
								new DialogInterface.OnClickListener() {

									public void onClick(
											DialogInterface dialoginterface,
											int i) {

									}
								}).show();

			}
		});

		softInfoButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent().setClass(
						FactoryTestReportQr.this, Version.class);
				intent.putExtra("softinfo", true);
				startActivity(intent);
			}
		});
	}
	private String getTestReportQr() {
		String temp = null;
		StringBuilder info = new StringBuilder();
		temp = "usetime" + ":" + PrizeFactoryTestActivity.testTime + "s,";
		info.append(temp);
		temp = "sn" + ":" + snNumber + ",";
		info.append(temp);
		for (PrizeFactoryTestListActivity.itempos = 0; PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length; PrizeFactoryTestListActivity.itempos++) {
			temp = changeLanguageToEnglish(PrizeFactoryTestListActivity.items[PrizeFactoryTestListActivity.itempos]);
			info.append(temp);
			
			temp = ":" + PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos] + ",";
			info.append(temp);
		}

		return info.toString();
	}
	
	private String changeLanguageToEnglish(String TestListName) {
		String name = null;
		if(null == TestListName){
			name = null;
		}else if (TestListName.equals(getResources().getString(R.string.touch_screen))) {
			name = "touchScreen";
		}else if (TestListName.equals(getResources().getString(R.string.prize_sim))) {
			name = "SIM";
		}else if (TestListName.equals(getResources().getString(R.string.prize_lcd))) {
			name = "LCD";
		}else if (TestListName.equals(getResources().getString(R.string.prize_led))) {
			name = "LED";
		}else if (TestListName.equals(getResources().getString(R.string.prize_ycd))) {
			name = "YCD";
		}else if (TestListName.equals(getResources().getString(R.string.flash_lamp))) {
			name = "flashLamp";
		}else if (TestListName.equals(getResources().getString(R.string.motor))) {
			name = "motor";
		}else if (TestListName.equals(getResources().getString(R.string.keys))) {
			name = "keys";
		}else if (TestListName.equals(getResources().getString(R.string.headset))) {
			name = "headset";
		}else if (TestListName.equals(getResources().getString(R.string.receiver))) {
			name = "receiver";
		}else if (TestListName.equals(getResources().getString(R.string.speaker))) {
			name = "speaker";
		}else if (TestListName.equals(getResources().getString(R.string.radio))) {
			name = "radio";
		}else if (TestListName.equals(getResources().getString(R.string.microphone_loop))) {
			name = "microphone(loop)";
		}else if (TestListName.equals(getResources().getString(R.string.microphone))) {
			name = "microphone";
		}else if (TestListName.equals(getResources().getString(R.string.TF_card))) {
			name = "TFCard";
		}else if (TestListName.equals(getResources().getString(R.string.ram))) {
			name = "RAM";
		}else if (TestListName.equals(getResources().getString(R.string.rear_camera))) {
			name = "rearCamera";
		}else if (TestListName.equals(getResources().getString(R.string.rear_camera_sub))) {
			name = "rearCameraSub";
		}else if (TestListName.equals(getResources().getString(R.string.front_camera))) {
			name = "frontCamera";
		}else if (TestListName.equals(getResources().getString(R.string.battery))) {
			name = "battery";
		}else if (TestListName.equals(getResources().getString(R.string.light_sensor))) {
			name = "lightSensor";
		}else if (TestListName.equals(getResources().getString(R.string.phone))) {
			name = "phone";
		}else if (TestListName.equals(getResources().getString(R.string.backlight))) {
			name = "backlight";
		}else if (TestListName.equals(getResources().getString(R.string.gravity_sensor))) {
			name = "gravitySensor";
		}else if (TestListName.equals(getResources().getString(R.string.step_counter_sensor))) {
			name = "stepCounterSensor";
		}else if (TestListName.equals(getResources().getString(R.string.fingerprint))) {
			name = "fingerprint";
		}else if (TestListName.equals(getResources().getString(R.string.rang_sensor))) {
			name = "rangSensor";
		}else if (TestListName.equals(getResources().getString(R.string.magnetic_sensor))) {
			name = "magneticSensor";
		}else if (TestListName.equals(getResources().getString(R.string.prize_wifi))) {
			name = "WiFi";
		}else if (TestListName.equals(getResources().getString(R.string.bluetooth))) {
			name = "bluetooth";
		}else if (TestListName.equals(getResources().getString(R.string.prize_gps))) {
			name = "GPS";
		}else if (TestListName.equals(getResources().getString(R.string.hall_sensor))) {
			name = "hallSensor";
		}else if (TestListName.equals(getResources().getString(R.string.infrared))) {
			name = "infrared";
		}
		return name;
	}
	
	private String getTestReportResult() {
		String temp = null;
		for (PrizeFactoryTestListActivity.itempos = 0; PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length; PrizeFactoryTestListActivity.itempos++) {
			if(PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos] != null){
				if(PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos].equals(getString(R.string.result_error))){
					temp = getString(R.string.fail);
					mTestReportResult.setTextColor(Color.RED);
					return temp;
				}else{
					temp = getString(R.string.pass);
					mTestReportResult.setTextColor(Color.GREEN);
				}
			}else{
				temp = getString(R.string.no_test);
				return temp;
			}
		}
		writeProInfo("P",46);
		return temp;
	}
	
	private void writeProInfo(String sn,int index) {
		if(null==sn||sn.length()<1){
			return;
		}			
		try {
            int flag = 0;
			byte[] buff=null;
			IBinder binder = ServiceManager.getService("NvRAMAgent");
			NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
			
			try {
				buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] by = sn.toString().getBytes();
			
			for(int i=0;i<50;i++)
			{
				if(buff[i]==0x00){
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
	
	private String getTestReportResultItem() {
		StringBuilder info = new StringBuilder();
		for (PrizeFactoryTestListActivity.itempos = 0; PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length; PrizeFactoryTestListActivity.itempos++) {
			if(PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos] != null){
				if(PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos].equals(getString(R.string.result_error))){
					mTestReportResultItem.setVisibility(View.VISIBLE);
					info.append((PrizeFactoryTestListActivity.itempos + 1) + ",");
					mTestReportResultItem.setTextColor(Color.RED);
				}
			}
		}
		return info.toString();
	}
}
