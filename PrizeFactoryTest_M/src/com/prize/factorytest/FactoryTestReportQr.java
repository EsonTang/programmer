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
import android.view.WindowManager;
import android.util.Log;

public class FactoryTestReportQr extends Activity {
	private TextView mTestReportResult;
	private TextView mTestReportResultItem;
	private TextView mSnNumber;
	private ImageView mImageViewQr;
	private Button factorySetButton = null;
	private Button softInfoButton = null;
	private String snNumber = null;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	
	private WindowManager.LayoutParams lp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		LinearLayout VersionLayout = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.testreport_qr, null);
		setContentView(VersionLayout);
		lp = getWindow().getAttributes();
		lp.screenBrightness = 1.0f;
		getWindow().setAttributes(lp);
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
		temp = PrizeFactoryTestActivity.testTime + ",";
		info.append(temp);
		temp = snNumber + ",";
		info.append(temp);
		char[] resultHex = new char[4];
		for (PrizeFactoryTestListActivity.itempos = 0; PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length; PrizeFactoryTestListActivity.itempos++) {
			if(PrizeFactoryTestListActivity.itempos < 18){
				int index =	PrizeFactoryTestListActivity.itempos%4;
				resultHex[index] = get(mustChangeResultTohex(PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos]),index);	
				if(index == 3){
					temp = String.valueOf(hexToChar((char)(resultHex[0] | resultHex[1] | resultHex[2] | resultHex[3])));
					info.append(temp);
				}
				if(PrizeFactoryTestListActivity.itempos == 17){
					temp = String.valueOf(hexToChar((char)(resultHex[0] | resultHex[1])));
					info.append(temp);
				}
			}else{
				String testItem = PrizeFactoryTestListActivity.items[PrizeFactoryTestListActivity.itempos];
				String testItemResult = PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos];
				temp = saveValueArray(testItem,testItemResult);
			}
		}
		temp = "," + temp;
		info.append(temp);
		return info.toString();
	}
	
	private char get(char resultHexSinger, int index)
    {
        return (char)(resultHexSinger << index);
    }
	
	private char hexToChar(char c){
		char r='0';
		switch(c){
			case 0x00:
				r='0';
				break;
			case 0x01:
				r='1';
				break;
			case 0x02:
				r='2';
				break;
			case 0x03:
				r='3';
				break;
			case 0x04:
				r='4';
				break;
			case 0x05:
				r='5';
				break;
			case 0x06:
				r='6';
				break;
			case 0x07:
				r='7';
				break;
			case 0x08:
				r='8';
				break;
			case 0x09:
				r='9';
				break;
			case 0x0a:
				r='A';
				break;
			case 0x0b:
				r='B';
				break;
			case 0x0c:
				r='C';
				break;
			case 0x0d:
				r='D';
				break;
			case 0x0e:
				r='E';
				break;
			case 0x0f:
				r='F';
				break;
			default:
				r='0';
				break;
		}
		return r;
	}
	
	
	private char unMustchangeResultTohex(String TestResult) {
		char result = 0x00;
		if(null != TestResult){
			if(TestResult.equals("pass")){
				result = 0x02;
			}else if(TestResult.equals("fail")){
				result = 0x01;
			}
		}
		return result;
	}
	
	private char mustChangeResultTohex(String TestResult) {
		char result = 0x00;
		if(null != TestResult){
			if(TestResult.equals("pass")){
				result = 0x01;
			}else if(TestResult.equals("fail")){
				result = 0x00;
			}
		}
		return result;
	}
	
	char[][] resultHex = {{0x00,0x00},{0x00,0x00},{0x00,0x00},{0x00,0x00},{0x00,0x00},{0x00,0x00}};
	char[] resultValues = new char[6];
	private String saveValueArray(String testItem,String testItemResult) {
		if(null == testItem){
			return null;
		}else if (testItem.equals(getResources().getString(R.string.fingerprint))) {
			resultHex[0][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.light_sensor))) {
			resultHex[0][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}else if (testItem.equals(getResources().getString(R.string.rang_sensor))) {
			resultHex[1][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.magnetic_sensor))) {
			resultHex[1][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}else if (testItem.equals(getResources().getString(R.string.gysensor_name))) {
			resultHex[2][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.infrared))) {
			resultHex[2][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}else if (testItem.equals(getResources().getString(R.string.flash_lamp_front))) {
			resultHex[3][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.hall_sensor))) {
			resultHex[3][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}else if (testItem.equals(getResources().getString(R.string.prize_led))) {
			resultHex[4][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.prize_ycd))) {
			resultHex[4][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}else if (testItem.equals(getResources().getString(R.string.otg))) {
			resultHex[5][0] = (char)unMustchangeResultTohex(testItemResult);
		}else if (testItem.equals(getResources().getString(R.string.prize_nfc))) {
			resultHex[5][1] = (char)(unMustchangeResultTohex(testItemResult) << 2);
		}
		for(int i=0;i<6;i++){
			resultValues[i] = hexToChar((char)(resultHex[i][0] | resultHex[i][1]));
		}
		
		return new String(resultValues);
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
