package com.prize.factorytest.Version;

import com.prize.factorytest.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemProperties;
import com.mediatek.telephony.TelephonyManagerEx;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.os.Handler;
import android.os.IBinder;
import com.prize.factorytest.NvRAMAgent;
import android.os.ServiceManager;
import android.util.Log;
import android.view.KeyEvent;

public class Version extends Activity{
	private TextView mVersion;
	private TelephonyManager mTelMgr;
	private TelephonyManagerEx mTelMgrEx;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean bSoftinfo = intent.getBooleanExtra("softinfo", false);
        if(!bSoftinfo){
        	setContentView(R.layout.version_info);
        	mVersion = (TextView)findViewById(R.id.version_show);
        	confirmButton();
        }else{
        	setContentView(R.layout.version);
        	mVersion = (TextView)findViewById(R.id.version_show);
        }
		String message = getVersionInfo();
        mVersion.setText(message);
    }	
	
    public void confirmButton()
	{
    	final Button buttonPass = (Button)findViewById(R.id.passButton);
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
	private boolean getCalibrationInfo(String temp){ 
		if(SystemProperties.get("ro.prize_customer").equals("odm")){
			return odmCalibrationInfo(temp);
		}else{
			return koobeeCalibrationInfo(temp);
		}
	}
	private boolean odmCalibrationInfo(String temp){			
		if(null != temp&&temp.length()>=63){
			if(!temp.substring(62, 63).equals("P"))
				return false;
		}else
			return false;
		
		if(null != temp&&temp.length()>=61){
			if(!temp.substring(60, 61).equals("P"))
				return false;
		}else 
			return false;
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("4M")){
			if(null != temp&&temp.length()>=55){
				if(!temp.substring(54, 55).equals("P"))
					return false;
			}else
				return false;
		}
		if(!SystemProperties.get("ro.prize_board_network_type").equals("2M")){
			if(null != temp&&temp.length()>=53){
				if(!temp.substring(52, 53).equals("P"))
					return false;
			}else 
				return false;
		}
		return true;
	}
	private boolean koobeeCalibrationInfo(String temp){
		if(null != temp&&temp.length()>=63){
			if(!temp.substring(62, 63).equals("P"))
				return false;
		}else
			return false;
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			if(null != temp&&temp.length()>=61){
				if(!temp.substring(60, 61).equals("P"))
					return false;
			}else 
				return false;
		}
		
		if(null != temp&&temp.length()>=59){
			if(!temp.substring(58, 59).equals("P"))
				return false;
		}else
			return false;
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			if(null != temp&&temp.length()>=57){
				if(!temp.substring(56, 57).equals("P"))
					return false;
			}else
				return false;
		}
		
		if(null != temp&&temp.length()>=55){
			if(!temp.substring(54, 55).equals("P"))
				return false;
		}else
			return false;
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			if(null != temp&&temp.length()>=53){
				if(!temp.substring(52, 53).equals("P"))
					return false;
			}else 
				return false;
		}
		
		return true;
	}
    private String getVersionInfo(){
		String temp = null;
		StringBuilder info = new StringBuilder();
		mTelMgr = ((TelephonyManager)getSystemService("phone"));
		mTelMgrEx = TelephonyManagerEx.getDefault();
				
		info.append("[SN] : ");
		temp = null;		
		temp = SystemProperties.get("gsm.serial");
		String pcba = readProInfo(49);
		String mobile = readProInfo(45);
		
		if(null != temp){
			if(temp.length() >50){
				info.append(temp.substring(0, 45)+mobile+temp.substring(46, 49)+pcba+temp.substring(50));						
			}else if(temp.length()==50){
				info.append(temp.substring(0, 45)+mobile+temp.substring(46, 49)+pcba);
			}else if(temp.length()>46&&temp.length()<50){
				info.append(temp.substring(0, 45)+mobile+temp.substring(46));
			}else if(temp.length()==46){
				info.append(temp.substring(0, 45)+mobile);
			}else if(temp.length()>0&&temp.length()<46){
				info.append(temp.substring(0));
			}
		}else{
			info.append(temp);
		}
		
		Log.e("lwq","gsm serial length = "+temp.length()+" prize_customer = "+SystemProperties.get("ro.prize_customer"));
		info.append(getString(R.string.calibration));
		if(getCalibrationInfo(temp))
			info.append(getString(R.string.calibration_yes));
		else
			info.append(getString(R.string.calibration_no));
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			String meid=SystemProperties.get("gsm.mtk.meid");
			info.append("\n[MEID] : ");
			temp = null;
			if(null == meid){
				temp = meid;
			}else{
				temp = meid.toUpperCase();
			}
			info.append(temp);
		}
		String imei1=SystemProperties.get("gsm.mtk.imei1");
		String imei2=SystemProperties.get("gsm.mtk.imei2");
		
		info.append("\n[IMEI1] : ");
		temp = null;
		temp = imei1;//mTelMgr.getDeviceId(0);
		info.append(temp);
		
		info.append("\n[IMEI2] : ");
		temp = null;
		temp = imei2;//mTelMgr.getDeviceId(1);
		info.append(temp);
		
		info.append("\n[Build Type] : ");
		temp = null;
		temp = Build.TYPE;
		info.append(temp);
		
		info.append("\n[Build Brand] : ");
		temp = null;
		temp = Build.BRAND;
		info.append(temp);
		
		info.append("\n[Build Model] : ");
		temp = null;
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
				
        return info.toString();
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
		char c=(char)buff[index];
		String sn=new String(buff);
		return String.valueOf((char)buff[index]);
	}
}