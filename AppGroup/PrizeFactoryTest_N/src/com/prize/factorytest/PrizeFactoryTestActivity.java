package com.prize.factorytest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.security.auth.PrivateCredentialPermission;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.prize.factorytest.BlueTooth.BlueTooth;
import com.prize.factorytest.Brightness.Brightness;
import com.prize.factorytest.CameraBack.CameraBack;
import com.prize.factorytest.CameraFront.CameraFront;
import com.prize.factorytest.CameraBackSub.CameraBackSub;
import com.prize.factorytest.CameraBackSub.CameraBackAlsSub;
import com.prize.factorytest.CameraBackSub.CameraBackYuvSub;
import com.prize.factorytest.CameraBackSub.CameraFrontYuvSub;
import com.prize.factorytest.Charger.Charger;
import com.prize.factorytest.FM.FM;
import com.prize.factorytest.FingerPrint.FingerPrint;
import com.prize.factorytest.GPS.GPS;
import com.prize.factorytest.GSensor.GSensor;
import com.prize.factorytest.Hall.Hall;
import com.prize.factorytest.Headset.Headset;
import com.prize.factorytest.HeadsetRe.HeadsetRe;
import com.prize.factorytest.Infrared.Infrared;
import com.prize.factorytest.Key.Key;
import com.prize.factorytest.LCD.LCD;
import com.prize.factorytest.LED.LED;
import com.prize.factorytest.YCD.YCD;
import com.prize.factorytest.LSensor.LSensor;
import com.prize.factorytest.MIC.MIC;
import com.prize.factorytest.MICRe.MICSubRe;
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
import com.prize.factorytest.Version.Version;
import com.prize.factorytest.Vibrate.Vibrate;
import com.prize.factorytest.WiFi.WiFi;
import com.prize.factorytest.GySensor.GySensor;
import com.prize.factorytest.OTG.OTG;
import com.prize.factorytest.NFC.NFC;
import android.os.SystemProperties;
import android.content.ContentResolver;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.IBinder;
import android.widget.Toast;
import com.prize.factorytest.NvRAMAgent;
import android.os.ServiceManager;
import android.location.LocationManager;
import android.provider.Settings;
import com.prize.factorytest.Service.BluetoothScanService;
import com.prize.factorytest.Service.WifiScanService;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.UserHandle;
import android.util.Log;
import android.nfc.NfcAdapter;

public class PrizeFactoryTestActivity extends Activity {
	Button pcbaTestButton = null;
	Button autoTestButton = null;
	Button manualTestButton = null;
	Button listtestButton = null;
	Button testReportButton = null;
	Button factorySetButton = null;
	Button softInfoButton = null;
	Button agingtestButton = null;	
	Button languageSwitchButton=null;
	private SensorManager mSensorManager = null;
	public static String[] items_copy ={};
	public static String appLanguage=new String();
	private ContentResolver mContentResolver;
	public static String TestResult = "0";
	public static boolean isPcbaTest=false;
	public static boolean isMobileTest=false;
    public static boolean isManualTest=false;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	private Context mContext;
	long startMili;
	long endMili;
	public static long testTime;
	FactoryTestApplication app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.factory_test);
		mContext=this;
		PrizeFactoryTestListActivity.items=getResources().getStringArray(R.array.single_test_items);
		mContentResolver = getContentResolver();
		init(getApplicationContext());
		initKeyEvent();
		appLanguage=getLanguage();
		app = (FactoryTestApplication) getApplication();
		enableGPS(true);
		new Thread(new Runnable(){   
		    public void run(){   
		    	try {
					Thread.sleep(1000);
					startService(new Intent(PrizeFactoryTestActivity.this, WifiScanService.class));
					if(SystemProperties.get("ro.mtk_bt_support").equals("1")) {
						startService(new Intent(PrizeFactoryTestActivity.this, BluetoothScanService.class));	
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
		    }   
		}).start();
		
		if(SystemProperties.get("ro.prize_customer").equals("koobee")){
			agingtestButton.setVisibility(View.GONE);
		}else{
			agingtestButton.setVisibility(View.VISIBLE);
		}
		if((null == getIntent().getExtras()) || !getIntent().getExtras().getBoolean("isAutoTest")){
			if(!isPhoneCalibration()){
				Toast.makeText(mContext, getString(R.string.no_calibration), 0).show();
				return;
			}
			autoTest();
		}
	}
    /**prize-add-by-zhongweilin-20171130-start*/
    private String readProInfo(int index) {
        IBinder binder = ServiceManager.getService("NvRAMAgent");
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
        byte[] buff = null;
        try {
            buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        char c=(char)buff[index];
        String sn=new String(buff);
        return String.valueOf((char)buff[index]);
    }
    /**prize-add-by-zhongweilin-20171130-end*/
	
	private boolean isPhoneCalibration(){
		boolean isCalibration = false;
        String wbg = readProInfo(41);//prize-modify-by-zhongweilin
		if(null != SystemProperties.get("gsm.serial") && SystemProperties.get("gsm.serial").length()>=49){
			if(SystemProperties.get("gsm.serial").substring(48, 49).equals("P") && SystemProperties.get("gsm.serial").substring(47, 48).equals("P") 
                && (SystemProperties.get("gsm.serial").substring(41, 42).equals("P") || wbg.equals("P"))){//add-WBG Coupling check
				if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
					if(SystemProperties.get("gsm.serial").substring(46, 47).equals("P")){
						isCalibration = true;
					}
				}else{
					isCalibration = true;
				}	
			}
		}
		return isCalibration;
	}		
	
	private boolean isAutomanualTest(){
		boolean isAutomanualTest = false;
		if(null != SystemProperties.get("gsm.serial") && SystemProperties.get("gsm.serial").length()>=39){
			if(SystemProperties.get("gsm.serial").substring(38, 39).equals("P")){
				isAutomanualTest = true;
			}else{
				isAutomanualTest = false;
			}	
		}
		return isAutomanualTest;
	}	
	
	private void autoTest(){
		startMili = System.currentTimeMillis();								
		PrizeFactoryTestListActivity.toStartAutoTest = true;
		if(SystemProperties.get("ro.prize_customer").equals("koobee")){
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_wifi)); 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_gps)); 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.bluetooth));
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.step_counter_sensor)); 
			
			/**add autotest by zhongweilin start */
            //if(SystemProperties.get("ro.prize_auto_test").equals("1")){
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.headset));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.receiver));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.speaker));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.microphone));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.microphone_loop));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rear_camera));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rear_camera_sub));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.front_camera_sub));
            //    PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.front_camera));
				
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_sim));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.keys));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.motor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.TF_card));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.ram));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.battery));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.light_sensor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.gravity_sensor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.fingerprint));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rang_sensor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.magnetic_sensor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.step_counter_sensor));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.otg));
			//	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.hall_sensor));
            //}
			/**add autotest by zhongweilin end */
		}
		for(int pos=0;pos<=PrizeFactoryTestListActivity.items.length;pos++){
			PrizeFactoryTestListActivity.resultCodeList[pos] = 0;
			PrizeFactoryTestListActivity.testReportresult[pos] = null;
		}
		PrizeFactoryTestListActivity.itempos = 0;
		isMobileTest=true;
		Intent intent = new Intent().setClass(this,LCD.class);
		startActivityForResult(intent, 0);
	}			
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			if(SystemProperties.get("ro.mtk_bt_support").equals("1")) {
				if(app.getIsBluetoothScanning()){
					stopService(new Intent(PrizeFactoryTestActivity.this, BluetoothScanService.class));
				}
			}
			if(app.getIsWifiScanning()){
				stopService(new Intent(PrizeFactoryTestActivity.this, WifiScanService.class));
			}
			enableWifi(false);
			enableBluetooth(false);
			enableGPS(false);
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void enableBluetooth(boolean enable) {

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			if (enable)
				mBluetoothAdapter.enable();
			else
				mBluetoothAdapter.disable();
		}
	}

	private void enableWifi(boolean enable) {

		WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager != null)
			mWifiManager.setWifiEnabled(enable);
	}
	
	private void enableGPS(boolean enable) {
		if(false == enable){
			Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.LOCATION_MODE, 0, UserHandle.USER_CURRENT);
		}else{
			Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.LOCATION_MODE, 1, UserHandle.USER_CURRENT);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
			if (PrizeFactoryTestListActivity.itempos == 0) {
				String autoappname = PrizeFactoryTestListActivity.items[PrizeFactoryTestListActivity.itempos];
				PrizeFactoryTestListActivity.itempos = 1;
				if (resultCode == RESULT_OK) {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 1;
				} else if (resultCode == RESULT_CANCELED) {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 2;
				}
				FactoryTestList(autoappname);
				return;
			}
			if (PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length) {
				String autoappname = PrizeFactoryTestListActivity.items[PrizeFactoryTestListActivity.itempos];
				if (resultCode == RESULT_OK) {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 1;
				} else if (resultCode == RESULT_CANCELED) {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 2;
				}
				FactoryTestList(autoappname);
			} else {
				PrizeFactoryTestListActivity.toStartAutoTest = false;
				if (resultCode == RESULT_OK) {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 1;
				} else {
					PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] = 2;
				}
			}
			if (PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] == 1) {
				PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos-1] = getResources().getString(R.string.result_normal);
			}
			if (PrizeFactoryTestListActivity.resultCodeList[PrizeFactoryTestListActivity.itempos - 1] == 2) {
				PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos-1] = getResources().getString(R.string.result_error);
			}
			preserveTestResult();
			return;
		}
	}
	private void preserveTestResult(){
		if(isPcbaTest^isMobileTest^isManualTest){
			if(isPcbaTest){
				preserveTestResult("pcbaResult");
			}
			if(isMobileTest){
				preserveTestResult("mobileResult");
			}
            if(isManualTest){
                preserveTestResult("manualResult");
            }
		}
		
	}
	private void preserveTestResult(String type){
		if(PrizeFactoryTestListActivity.itempos==PrizeFactoryTestListActivity.items.length){
			TestResult="P";
			for(int i=0;i<PrizeFactoryTestListActivity.items.length;i++){
				if(null != PrizeFactoryTestListActivity.testReportresult[i] && PrizeFactoryTestListActivity.testReportresult[i].equals(getString(R.string.result_error))){
					TestResult="F";
					break;
				}
			}
			if(type.equals("mobileResult")){
				isMobileTest=false;
				writeProInfo(TestResult,45);
			}
			if(type.equals("pcbaResult")){
				isPcbaTest=false;
				writeProInfo(TestResult,49);
			}
            if(type.equals("manualResult")){
                isManualTest=false;
                writeProInfo(TestResult,37);
            }

			Intent intent;
			if((null == getIntent().getExtras()) || !getIntent().getExtras().getBoolean("isAutoTest")){
				//writeProInfo("P",46);
				endMili = System.currentTimeMillis();
				testTime = (endMili- startMili)/1000;
				intent = new Intent().setClass(PrizeFactoryTestActivity.this, FactoryTestReportQr.class);
			}else{
				intent = new Intent().setClass(PrizeFactoryTestActivity.this, FactoryTestReport.class);
			}
			startActivity(intent);	
			
		}
	}
	private void writeProInfo(String sn,int index) {
		if(null==sn||sn.length()<1){
			Toast.makeText(mContext, "sn empty", 0).show();
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
	public void FactoryTestList(String TestListName) {
		if(null == TestListName){
			return;
		}
		Intent intent = new Intent();
		if (TestListName.equals(getResources().getString(R.string.touch_screen))) {
			intent.setClass(this, TouchPanelEdge.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_sim))) {
			intent.setClass(this, SIM.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_lcd))) {
			intent.setClass(this, LCD.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_led))) {
			intent.setClass(this, LED.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_ycd))) {
			intent.setClass(this, YCD.class);
		}else if (TestListName.equals(getResources().getString(R.string.flash_lamp))) {
			intent.setClass(this, Torchled.class);
		}else if (TestListName.equals(getResources().getString(R.string.flash_lamp_front))) {
			intent.setClass(this, TorchledFront.class);
		}else if (TestListName.equals(getResources().getString(R.string.motor))) {
			intent.setClass(this, Vibrate.class);
		}else if (TestListName.equals(getResources().getString(R.string.keys))) {
			intent.setClass(this, Key.class);
		}else if (TestListName.equals(getResources().getString(R.string.headset))) {
			intent.setClass(this, Headset.class);
		}else if (TestListName.equals(getResources().getString(R.string.receiver))) {
			intent.setClass(this, Receiver.class);
		}else if (TestListName.equals(getResources().getString(R.string.speaker))) {
			intent.setClass(this, Speaker.class);
		}else if (TestListName.equals(getResources().getString(R.string.radio))) {
			intent.setClass(this, FM.class);
		}else if (TestListName.equals(getResources().getString(R.string.microphone_loop))) {
			intent.setClass(this, MIC.class);
		}else if (TestListName.equals(getResources().getString(R.string.microphone))) {
			intent.setClass(this, MICRe.class);
		}else if (TestListName.equals(getResources().getString(R.string.sub_microphone))) {
			intent.setClass(this, MICSubRe.class);
		}else if (TestListName.equals(getResources().getString(R.string.TF_card))) {
			intent.setClass(this, SDCard.class);
		}else if (TestListName.equals(getResources().getString(R.string.ram))) {
			intent.setClass(this, RAM.class);
		}else if (TestListName.equals(getResources().getString(R.string.rear_camera))) {
			intent.setClass(this, CameraBack.class);
		}else if (TestListName.equals(getResources().getString(R.string.rear_camera_sub))) {
			if(SystemProperties.get("ro.prize_rear_camera_sub").equals("1")){
				intent.setClass(this, CameraBackSub.class);
			}else if(SystemProperties.get("ro.prize_rear_camera_sub_als").equals("1")){
				intent.setClass(this, CameraBackAlsSub.class);
			}else if(SystemProperties.get("ro.prize_rear_camera_sub_yuv").equals("1")){
				intent.setClass(this, CameraBackYuvSub.class);
			}
			
		}else if (TestListName.equals(getResources().getString(R.string.front_camera_sub))) {
			intent.setClass(this, CameraFrontYuvSub.class);
		}else if (TestListName.equals(getResources().getString(R.string.front_camera))) {
			intent.setClass(this, CameraFront.class);
		}else if (TestListName.equals(getResources().getString(R.string.battery))) {
			intent.setClass(this, Charger.class);
		}else if (TestListName.equals(getResources().getString(R.string.light_sensor))) {
			intent.setClass(this, LSensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.phone))) {
			intent.setClass(this, Phone.class);
		}else if (TestListName.equals(getResources().getString(R.string.backlight))) {
			intent.setClass(this, Brightness.class);
		}else if (TestListName.equals(getResources().getString(R.string.gravity_sensor))) {
			intent.setClass(this, GSensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.fingerprint))) {
			intent.setClass(this, FingerPrint.class);
		}else if (TestListName.equals(getResources().getString(R.string.rang_sensor))) {
			intent.setClass(this, PSensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.magnetic_sensor))) {
			intent.setClass(this, MSensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.step_counter_sensor))) {
			intent.setClass(this, CSensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.gysensor_name))) {
			intent.setClass(this, GySensor.class);
		}else if (TestListName.equals(getResources().getString(R.string.otg))) {
			intent.setClass(this, OTG.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_wifi))) {
			intent.setClass(this, WiFi.class);
		}else if (TestListName.equals(getResources().getString(R.string.bluetooth))) {
			intent.setClass(this, BlueTooth.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_gps))) {
			intent.setClass(this, GPS.class);
		}else if (TestListName.equals(getResources().getString(R.string.hall_sensor))) {
			intent.setClass(this, Hall.class);
		}else if (TestListName.equals(getResources().getString(R.string.infrared))) {
			intent.setClass(this, Infrared.class);
		}else if (TestListName.equals(getResources().getString(R.string.prize_nfc))) {
			intent.setClass(this, NFC.class);
		}
		if(null != intent){
			startActivityForResult(intent, 0);
		}
	}
	
	private String[] deleteItemByValue(String[] array, String value) {
		List<String> list = new ArrayList();
		for (String i : array) {
			if (null != i && !i.equals(value)) {
				list.add(i);
			}
		}
		String[] newArray = new String[list.size()];
		for (int i = 0; i < newArray.length; i++) {
			newArray[i] = list.get(i);
		}
		return newArray;
	}
	
	void init(Context context) {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager != null) {
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.light_sensor));
			}
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.gravity_sensor));
			}
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.rang_sensor));
			}
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.magnetic_sensor));
			}
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.step_counter_sensor));
			}
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null){
				PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.gysensor_name)); 
		 	}
		}

		/*Prize-modify by liyu-for fingerprint factory test-20160621-start*/
		if (!SystemProperties.get("ro.prize_fingerprint").equals("1") &&!SystemProperties.get("ro.prize_fingerprint").equals("2")) {
			/*Prize-modify by liyu-for fingerprint factory test-20160621-end*/
			PrizeFactoryTestListActivity.items=deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.fingerprint)); 
		} 
		if(!SystemProperties.get("ro.prize_hall").equals("1")) { 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.hall_sensor)); 
		} 
		if(SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) { 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.sub_microphone)); 
		} 
		if(!SystemProperties.get("ro.prize_infrared").equals("1")) { 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items, getString(R.string.infrared)); 
		} 
		if(!SystemProperties.get("ro.prize_led").equals("1")) { 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_led)); 
		}
		if(!SystemProperties.get("ro.prize_led_yc").equals("1")) { 
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_ycd)); 
		}
		if(!SystemProperties.get("ro.mtk_bt_support").equals("1")) {
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.bluetooth)); 
		}

		if(SystemProperties.get("ro.prize_customer").equals("koobee")){
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.phone)); 
		}

		if(!SystemProperties.get("ro.prize_rear_camera_sub").equals("1") && !SystemProperties.get("ro.prize_rear_camera_sub_als").equals("1")
		&& !SystemProperties.get("ro.prize_rear_camera_sub_yuv").equals("1")){
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rear_camera_sub)); 
		}
		if(!SystemProperties.get("ro.prize_front_camera_sub_yuv").equals("1")){
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.front_camera_sub)); 
		}
		
		if(!SystemProperties.get("ro.torch_front").equals("1")) {
			PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.flash_lamp_front)); 
		}
		
		if (!SystemProperties.get("ro.prize_otg").equals("1")) {
		 	PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.otg)); 
		}
		 if(NfcAdapter.getDefaultAdapter(this) == null){
			 PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_nfc));
		 }
		 
		 

		PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.microphone_loop)); 
		items_copy = PrizeFactoryTestListActivity.items;
	}
	void initKeyEvent() {
		pcbaTestButton = (Button) findViewById(R.id.pcbatest);
		autoTestButton = (Button) findViewById(R.id.autotest);
		manualTestButton = (Button) findViewById(R.id.manualtest);
		listtestButton = (Button) findViewById(R.id.listtest);
		testReportButton = (Button) findViewById(R.id.testreport);
		factorySetButton = (Button) findViewById(R.id.factoryset);
		softInfoButton = (Button) findViewById(R.id.softinfo);
		languageSwitchButton = (Button) findViewById(R.id.languageswitch);                              
		agingtestButton = (Button) findViewById(R.id.agingtest);   
		pcbaTestButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						PrizeFactoryTestActivity.this);
				dialog.setCancelable(false)
						.setTitle(R.string.pcbatest)
						.setMessage(R.string.pcbatest_confirm)
						.setPositiveButton

						(R.string.confirm,
								new DialogInterface.OnClickListener() {

									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										PrizeFactoryTestListActivity.toStartAutoTest = true;
										PrizeFactoryTestListActivity.itempos = 3;
										/*
										PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.prize_lcd));
										PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.touch_screen));
										PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getResources().getString(R.string.ram));
										*/
										isPcbaTest=true;
										Intent intent = new Intent().setClass(
												PrizeFactoryTestActivity.this,
												CameraBack.class);
										startActivityForResult(intent, 0);
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
	
		
		autoTestButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(!isPhoneCalibration()){
					Toast.makeText(mContext, getString(R.string.no_calibration), 0).show();
					return;
				}
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						PrizeFactoryTestActivity.this);
				dialog.setCancelable(false)
						.setTitle(R.string.phonetest)
						.setMessage(R.string.phonetest_confirm)
						.setPositiveButton

						(R.string.confirm,
								new DialogInterface.OnClickListener() {

									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										PrizeFactoryTestListActivity.toStartAutoTest = true;
										if(SystemProperties.get("ro.prize_customer").equals("koobee")){
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_wifi)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_gps)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.bluetooth)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.step_counter_sensor)); 
										}
										for(int pos=0;pos<=PrizeFactoryTestListActivity.items.length;pos++){
											PrizeFactoryTestListActivity.resultCodeList[pos] = 0;
											PrizeFactoryTestListActivity.testReportresult[pos] = null;
										}
										PrizeFactoryTestListActivity.itempos = 0;
										isMobileTest=true;
										Intent intent = new Intent().setClass(
												PrizeFactoryTestActivity.this,
												LCD.class);
										startActivityForResult(intent, 0);
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

		manualTestButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
                /**prize-modify-by-zhongweilin-20171130-start */
				/**WBG/GSM/LTE ANT */
				if(!isPhoneCalibration()){
					Toast.makeText(mContext, getString(R.string.no_calibration), 0).show();
					return;
				} 
				/**prize-modify-by-zhongweilin-20171130-end */
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						PrizeFactoryTestActivity.this);
				dialog.setCancelable(false)
						.setTitle(R.string.manualtest)
						.setMessage(R.string.manualtest_confirm)
						.setPositiveButton

						(R.string.confirm,
								new DialogInterface.OnClickListener() {

									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										PrizeFactoryTestListActivity.toStartAutoTest = true;
										if(SystemProperties.get("ro.prize_customer").equals("koobee")){
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.touch_screen)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_sim)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_led)); 
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_ycd));
								            PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.keys));
								            PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.headset));
								            PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.receiver));
								            PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.speaker));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.microphone_loop));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.microphone));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.sub_microphone));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.TF_card));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.ram));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rear_camera));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.front_camera_sub));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.front_camera));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.battery));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.light_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.phone));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.gravity_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.rang_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.magnetic_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.step_counter_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.gysensor_name));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.otg));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_wifi));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.bluetooth));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_gps));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.hall_sensor));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.infrared));
											PrizeFactoryTestListActivity.items = deleteItemByValue(PrizeFactoryTestListActivity.items,getString(R.string.prize_nfc));
										}
										for(int pos=0;pos<=PrizeFactoryTestListActivity.items.length;pos++){
											PrizeFactoryTestListActivity.resultCodeList[pos] = 0;
											PrizeFactoryTestListActivity.testReportresult[pos] = null;
										}
										PrizeFactoryTestListActivity.itempos = 0;
										isManualTest=true;
										Intent intent = new Intent().setClass(
												PrizeFactoryTestActivity.this,
												LCD.class);
										startActivityForResult(intent, 0);
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
		
		listtestButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PrizeFactoryTestListActivity.items = items_copy;
				for(int pos=0;pos<=PrizeFactoryTestListActivity.items.length;pos++){
					PrizeFactoryTestListActivity.resultCodeList[pos] = 0;
					PrizeFactoryTestListActivity.testReportresult[pos] = null;
				}
				Intent intent = new Intent().setClass(
						PrizeFactoryTestActivity.this, PrizeFactoryTestListActivity.class);
				startActivity(intent);
			}
		});
		
		testReportButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent().setClass(
						PrizeFactoryTestActivity.this, FactoryTestReport.class);
				startActivity(intent);
			}
		});

		factorySetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						PrizeFactoryTestActivity.this);
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
										//intent.putExtra("shutdown", true);
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
						PrizeFactoryTestActivity.this, Version.class);
				intent.putExtra("softinfo", true);
				startActivity(intent);
			}
		});
		languageSwitchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				changeLanguage();
			}
		});				
		agingtestButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent().setClass(
						PrizeFactoryTestActivity.this, AgingTestActivity.class);
				startActivity(intent);
			}
		});		
		
	}
	private void changeLanguage(){
        String currentlanguage=getCountry();
        if(currentlanguage.indexOf("CN")>-1){
        	Configuration config = getResources().getConfiguration();  
            DisplayMetrics metrics = getResources().getDisplayMetrics();  
            config.locale = Locale.ENGLISH;  
            getResources().updateConfiguration(config, metrics);
    		
            Intent intent = new Intent();
            intent.setClass(this,PrizeFactoryTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("isAutoTest", true);
            startActivity(intent);            
        }else{
			Configuration config = getResources().getConfiguration();  
            DisplayMetrics metrics = getResources().getDisplayMetrics();  
            config.locale = Locale.SIMPLIFIED_CHINESE;  
            getResources().updateConfiguration(config, metrics);
    		
            Intent intent = new Intent();
            intent.setClass(this,PrizeFactoryTestActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("isAutoTest", true);
            startActivity(intent);
		}  
	}
	private String getLanguage(){
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		return language;
	}
	private String getCountry(){
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		return language;
	}
}
