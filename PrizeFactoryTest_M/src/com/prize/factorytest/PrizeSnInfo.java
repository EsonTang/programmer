package com.prize.factorytest;

import com.prize.factorytest.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.mediatek.telephony.TelephonyManagerEx;
import android.content.ContentResolver;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import java.util.HashMap;
import android.content.Context;
import android.os.IBinder;
import android.widget.Toast;
import com.prize.factorytest.NvRAMAgent;
import android.os.ServiceManager;

public class PrizeSnInfo extends Activity {
	private TextView mVersion;
	Button button;
	String version;
	private TelephonyManager mTelMgr;
	private TelephonyManagerEx mTelMgrEx;
	String mBarCode;
	static String[] mImsi;
    private ContentResolver mContentResolver;
	String pcba;
	String mobile;
	String wbg;
	private static final String PRODUCT_INFO_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
	private Context mContext;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sn_info);
		mVersion = (TextView) findViewById(R.id.sn_show);
		mContext=this;
		mContentResolver = getContentResolver();	
		String message = getVersionInfo();
		mVersion.setText(message);		
		snInfoDisplay();
	}
	private void koobeeTestInfo(String temp){
		HashMap<String, String> koobeeInfo = new HashMap<String, String>();
		
		koobeeInfo.put("key", "GSM-BT: ");
		if(null != temp&&temp.length()>=63){
			if(temp.substring(62, 63).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(62, 63).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
		
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "GSM-FT: ");
		if(null != temp&&temp.length()>=62){
			if(temp.substring(61, 62).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(61, 62).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "WCDMA-BT: ");
			if(null != temp&&temp.length()>=61){
				if(temp.substring(60, 61).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(60, 61).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
			
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "WCDMA-FT: ");
			if(null != temp&&temp.length()>=60){
				if(temp.substring(59, 60).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(59, 60).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
		}
						
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "TDSCDMA-BT: ");
		if(null != temp&&temp.length()>=59){
			if(temp.substring(58, 59).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(58, 59).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
				
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "TDSCDMA-FT: ");
		if(null != temp&&temp.length()>=58){
			if(temp.substring(57, 58).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(57, 58).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else 
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "CDMA-BT: ");
			if(null != temp&&temp.length()>=57){
				if(temp.substring(56, 57).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(56, 57).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
		
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "CDMA-FT: ");
			if(null != temp&&temp.length()>=56){
				if(temp.substring(55, 56).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(55, 56).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
		}
				
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "LTETDD-BT: ");
		if(null != temp&&temp.length()>=55){
			if(temp.substring(54, 55).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(54, 55).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
		
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "LTETDD-FT: ");
		if(null != temp&&temp.length()>=54){
			if(temp.substring(53, 54).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(53, 54).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else 
			koobeeInfo.put("value", getString(R.string.no_test));
		pcbaList.add(koobeeInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "LTEFDD-BT: ");
			if(null != temp&&temp.length()>=53){
				if(temp.substring(52, 53).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(52, 53).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
					
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "LTEFDD-FT: ");
			if(null != temp&&temp.length()>=52){
				if(temp.substring(51, 52).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(51, 52).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			pcbaList.add(koobeeInfo);
		}
						
		final String ata;
		if(null != temp&&temp.length()>=51){
			if(temp.substring(50, 51).equals("P"))
				ata="P";
			else if(temp.substring(50, 51).equals("F"))
				ata="F";
			else 
				ata="0";
		}else 
			ata="0";
		
		final String pcba_mmi;
		if(pcba.equals("P"))
			pcba_mmi="P";
		else if(pcba.equals("F"))
			pcba_mmi="F";
		else
			pcba_mmi="0";
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "PCBA功能：");
		if(ata.equals("P")||pcba_mmi.equals("P"))
			koobeeInfo.put("value", getString(R.string.pass));
		else if(ata.equals("0")&&pcba_mmi.equals("0"))
			koobeeInfo.put("value", getString(R.string.no_test));
		else
			koobeeInfo.put("value", getString(R.string.fail));
		pcbaList.add(koobeeInfo);
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "整机CIT: ");
		if(mobile.equals("P"))
			koobeeInfo.put("value", getString(R.string.pass));
		else if(mobile.equals("F"))
			koobeeInfo.put("value", getString(R.string.fail));
		else
			koobeeInfo.put("value", getString(R.string.no_test));
		phoneList.add(koobeeInfo);	
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "GSM耦合: ");
		if(null != temp&&temp.length()>=49){
			if(temp.substring(48, 49).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(48, 49).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));	
		}else 
			koobeeInfo.put("value", getString(R.string.no_test));
		phoneList.add(koobeeInfo);
		
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "LTETDD耦合: ");
		if(null != temp&&temp.length()>=48){
			if(temp.substring(47, 48).equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(47, 48).equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else
			koobeeInfo.put("value", getString(R.string.no_test));	
		phoneList.add(koobeeInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			koobeeInfo = new HashMap<String, String>();
			koobeeInfo.put("key", "CDMA耦合: ");
			if(null != temp&&temp.length()>=47){
				if(temp.substring(46, 47).equals("P"))
					koobeeInfo.put("value", getString(R.string.pass));
				else if(temp.substring(46, 47).equals("F"))
					koobeeInfo.put("value", getString(R.string.fail));
				else 
					koobeeInfo.put("value", getString(R.string.no_test));
			}else 
				koobeeInfo.put("value", getString(R.string.no_test));
			phoneList.add(koobeeInfo);
		}		
		
		koobeeInfo = new HashMap<String, String>();
		koobeeInfo.put("key", "WBG耦合: ");
		if(null != temp&&temp.length()>=42){
			if(temp.substring(41, 42).equals("P") || wbg.equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(temp.substring(41, 42).equals("F") || wbg.equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else 
				koobeeInfo.put("value", getString(R.string.no_test));
		}else{
			if(wbg.equals("P"))
				koobeeInfo.put("value", getString(R.string.pass));
			else if(wbg.equals("F"))
				koobeeInfo.put("value", getString(R.string.fail));
			else
			koobeeInfo.put("value", getString(R.string.no_test));	
		}
		phoneList.add(koobeeInfo);
	}
	
	private void cooseaTestInfo(String temp){
		HashMap<String, String> cooseaInfo = new HashMap<String, String>();
		
		cooseaInfo.put("key", "GSM-BT: ");				
		if(null != temp&&temp.length()>=63){
			if(temp.substring(62, 63).equals("P"))
				cooseaInfo.put("value", getString(R.string.pass));
			else if(temp.substring(62, 63).equals("F"))
				cooseaInfo.put("value", getString(R.string.fail));
			else 
				cooseaInfo.put("value", getString(R.string.no_test));
		}else
			cooseaInfo.put("value", getString(R.string.no_test));
		pcbaList.add(cooseaInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			cooseaInfo = new HashMap<String, String>();
			cooseaInfo.put("key", "WCDMA-BT: ");	
			if(null != temp&&temp.length()>=61){
				if(temp.substring(60, 61).equals("P"))
					cooseaInfo.put("value", getString(R.string.pass));
				else if(temp.substring(60, 61).equals("F"))
					cooseaInfo.put("value", getString(R.string.fail));
				else 
					cooseaInfo.put("value", getString(R.string.no_test));
			}else 
				cooseaInfo.put("value", getString(R.string.no_test));
			pcbaList.add(cooseaInfo);
		}				
		
		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "TDSCDMA-BT: ");	
		if(null != temp&&temp.length()>=59){
			if(temp.substring(58, 59).equals("P"))
				cooseaInfo.put("value", getString(R.string.pass));
			else if(temp.substring(58, 59).equals("F"))
				cooseaInfo.put("value", getString(R.string.fail));
			else 
				cooseaInfo.put("value", getString(R.string.no_test));
		}else
			cooseaInfo.put("value", getString(R.string.no_test));		
		pcbaList.add(cooseaInfo);
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			cooseaInfo = new HashMap<String, String>();
			cooseaInfo.put("key", "CDMA-BT: ");	
			if(null != temp&&temp.length()>=57){
				if(temp.substring(56, 57).equals("P"))
					cooseaInfo.put("value", getString(R.string.pass));
				else if(temp.substring(56, 57).equals("F"))
					cooseaInfo.put("value", getString(R.string.fail));
				else 
					cooseaInfo.put("value", getString(R.string.no_test));
			}else 
				cooseaInfo.put("value", getString(R.string.no_test));
			pcbaList.add(cooseaInfo);
		}		
		
		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "LTETDD-BT: ");	
		if(null != temp&&temp.length()>=55){
			if(temp.substring(54, 55).equals("P"))
				cooseaInfo.put("value", getString(R.string.pass));
			else if(temp.substring(54, 55).equals("F"))
				cooseaInfo.put("value", getString(R.string.fail));
			else 
				cooseaInfo.put("value", getString(R.string.no_test));
		}else
			cooseaInfo.put("value", getString(R.string.no_test));
		pcbaList.add(cooseaInfo);
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")||SystemProperties.get("ro.prize_board_network_type").equals("5M")){
			cooseaInfo = new HashMap<String, String>();
			cooseaInfo.put("key", "LTEFDD-BT: ");	
			if(null != temp&&temp.length()>=53){
				if(temp.substring(52, 53).equals("P"))
					cooseaInfo.put("value", getString(R.string.pass));
				else if(temp.substring(52, 53).equals("F"))
					cooseaInfo.put("value", getString(R.string.fail));
				else 
					cooseaInfo.put("value", getString(R.string.no_test));
			}else 
				cooseaInfo.put("value", getString(R.string.no_test));
			pcbaList.add(cooseaInfo);
		}				
		
		final String ata;
		if(null != temp&&temp.length()>=51){
			if(temp.substring(50, 51).equals("P"))
				ata="P";
			else if(temp.substring(50, 51).equals("F"))
				ata="F";
			else 
				ata="0";
		}else 
			ata="0";
		
		final String pcba_mmi;
		if(pcba.equals("P"))
			pcba_mmi="P";
		else if(pcba.equals("F"))
			pcba_mmi="F";
		else
			pcba_mmi="0";
		
		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "PCBA功能：");
		if(ata.equals("P")||pcba_mmi.equals("P"))
			cooseaInfo.put("value", getString(R.string.pass));
		else if(ata.equals("0")&&pcba_mmi.equals("0"))
			cooseaInfo.put("value", getString(R.string.no_test));
		else
			cooseaInfo.put("value", getString(R.string.fail));
		pcbaList.add(cooseaInfo);

		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "整机CIT: ");	
		if(mobile.equals("P"))
			cooseaInfo.put("value", getString(R.string.pass));
		else if(mobile.equals("F"))
			cooseaInfo.put("value", getString(R.string.fail));
		else
			cooseaInfo.put("value", getString(R.string.no_test)); 		
		phoneList.add(cooseaInfo);
		
		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "GSM耦合: ");	
		if(null != temp&&temp.length()>=49){
			if(temp.substring(48, 49).equals("P"))
				cooseaInfo.put("value", getString(R.string.pass));
			else if(temp.substring(48, 49).equals("F"))
				cooseaInfo.put("value", getString(R.string.fail));
			else 
				cooseaInfo.put("value", getString(R.string.no_test)); 		
		}else 
			cooseaInfo.put("value", getString(R.string.no_test)); 		
		phoneList.add(cooseaInfo);
		
		cooseaInfo = new HashMap<String, String>();
		cooseaInfo.put("key", "LTETDD耦合: ");	
		if(null != temp&&temp.length()>=48){
			if(temp.substring(47, 48).equals("P"))
				cooseaInfo.put("value", getString(R.string.pass));
			else if(temp.substring(47, 48).equals("F"))
				cooseaInfo.put("value", getString(R.string.fail));
			else 
				cooseaInfo.put("value", getString(R.string.no_test)); 	
		}else
			cooseaInfo.put("value", getString(R.string.no_test)); 	
		phoneList.add(cooseaInfo);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			cooseaInfo = new HashMap<String, String>();
			cooseaInfo.put("key", "CDMA耦合: ");	
			if(null != temp&&temp.length()>=47){
				if(temp.substring(46, 47).equals("P"))
					cooseaInfo.put("value", getString(R.string.pass));
				else if(temp.substring(46, 47).equals("F"))
					cooseaInfo.put("value", getString(R.string.fail));
				else 
					cooseaInfo.put("value", getString(R.string.no_test)); 	
			}else 
				cooseaInfo.put("value", getString(R.string.no_test)); 	
			phoneList.add(cooseaInfo);
		}		
	}
	
	private void odmTestInfo(String temp){
		HashMap<String, String> odmInfo = new HashMap<String, String>();
		
		odmInfo.put("key", "GSM-BT: ");				
		if(null != temp&&temp.length()>=63){
			if(temp.substring(62, 63).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(62, 63).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test));
		}else
			odmInfo.put("value", getString(R.string.no_test));
		pcbaList.add(odmInfo);
		
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "GSM-FT: ");	
		if(null != temp&&temp.length()>=62){
			if(temp.substring(61, 62).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(61, 62).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test));
		}else
			odmInfo.put("value", getString(R.string.no_test));
		pcbaList.add(odmInfo);
		
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "WCDMA-BT: ");	
		if(null != temp&&temp.length()>=61){
			if(temp.substring(60, 61).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(60, 61).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test));
		}else 
			odmInfo.put("value", getString(R.string.no_test));
		pcbaList.add(odmInfo);
		if(SystemProperties.get("ro.prize_board_network_type").equals("4M")){
			odmInfo = new HashMap<String, String>();
			odmInfo.put("key", "LTETDD-BT: ");	
			if(null != temp&&temp.length()>=55){
				if(temp.substring(54, 55).equals("P"))
					odmInfo.put("value", getString(R.string.pass));
				else if(temp.substring(54, 55).equals("F"))
					odmInfo.put("value", getString(R.string.fail));
				else 
					odmInfo.put("value", getString(R.string.no_test));
			}else
				odmInfo.put("value", getString(R.string.no_test));
			pcbaList.add(odmInfo);
		}
		if(!SystemProperties.get("ro.prize_board_network_type").equals("2M")){
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "LTEFDD-BT: ");	
		if(null != temp&&temp.length()>=53){
			if(temp.substring(52, 53).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(52, 53).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test));
		}else 
			odmInfo.put("value", getString(R.string.no_test));
		pcbaList.add(odmInfo);
		}
		final String ata;
		if(null != temp&&temp.length()>=51){
			if(temp.substring(50, 51).equals("P"))
				ata="P";
			else if(temp.substring(50, 51).equals("F"))
				ata="F";
			else 
				ata="0";
		}else 
			ata="0";
		
		final String pcba_mmi;
		if(pcba.equals("P"))
			pcba_mmi="P";
		else if(pcba.equals("F"))
			pcba_mmi="F";
		else
			pcba_mmi="0";
		
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "PCBA功能：");
		if(ata.equals("P")||pcba_mmi.equals("P"))
			odmInfo.put("value", getString(R.string.pass));
		else if(ata.equals("0")&&pcba_mmi.equals("0"))
			odmInfo.put("value", getString(R.string.no_test));
		else
			odmInfo.put("value", getString(R.string.fail));
		pcbaList.add(odmInfo);

		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "整机CIT: ");	
		if(mobile.equals("P"))
			odmInfo.put("value", getString(R.string.pass));
		else if(mobile.equals("F"))
			odmInfo.put("value", getString(R.string.fail));
		else
			odmInfo.put("value", getString(R.string.no_test)); 		
		phoneList.add(odmInfo);
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "GSM耦合: ");	
		if(null != temp&&temp.length()>=49){
			if(temp.substring(48, 49).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(48, 49).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test)); 		
		}else 
			odmInfo.put("value", getString(R.string.no_test)); 		
		phoneList.add(odmInfo);
		if(!SystemProperties.get("ro.prize_board_network_type").equals("2M")){
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "LTE耦合: ");	
		if(null != temp&&temp.length()>=48){
			if(temp.substring(47, 48).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(47, 48).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test)); 	
		}else
			odmInfo.put("value", getString(R.string.no_test)); 	
		phoneList.add(odmInfo);		
        }
		odmInfo = new HashMap<String, String>();
		odmInfo.put("key", "WCDMA耦合: ");	
		if(null != temp&&temp.length()>=45){
			if(temp.substring(44, 45).equals("P"))
				odmInfo.put("value", getString(R.string.pass));
			else if(temp.substring(44, 45).equals("F"))
				odmInfo.put("value", getString(R.string.fail));
			else 
				odmInfo.put("value", getString(R.string.no_test)); 	
		}else 
			odmInfo.put("value", getString(R.string.no_test)); 	
		phoneList.add(odmInfo);

	}
	
	private String getVersionInfo() {
		String temp = null;
		StringBuilder info = new StringBuilder();
		mTelMgr = ((TelephonyManager)getSystemService("phone"));
		mTelMgrEx = TelephonyManagerEx.getDefault();
		pcba = readProInfo(49);
		mobile = readProInfo(45);
		wbg = readProInfo(41);
		
		if(SystemProperties.get("ro.prize_board_network_type").equals("6M")){
			String meid=SystemProperties.get("gsm.mtk.meid");
			info.append("[MEID] : ");
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
		temp = imei1;//mTelMgr.getDeviceId(0);
		info.append(temp);

		info.append("\n[IMEI2] : ");
		temp = imei2;//mTelMgr.getDeviceId(1);
		info.append(temp);
		
		info.append("\n[SN] : ");
		temp = SystemProperties.get("gsm.serial");
		
		if(null != temp){
			if(temp.length() >25){
				info.append(temp.substring(0, 25));
			}else if(temp.length()>0&&temp.length()<=25){
				info.append(temp.substring(0));
			}			
		}else{
			info.append(temp);
		}
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
	
	private ArrayList<HashMap<String,String>> pcbaList = new ArrayList<HashMap<String,String>>();
    private ArrayList<HashMap<String,String>> phoneList = new ArrayList<HashMap<String,String>>();
    ListView pcbaListView;
    ListView phoneListView;
    SnInfoAdapter pcbaAdapter;
    SnInfoAdapter phoneAdapter;
	private void snInfoDisplay(){
		pcbaListView = (ListView) findViewById(R.id.pcba_lv);
		phoneListView = (ListView) findViewById(R.id.phone_lv);
		getListViewData(SystemProperties.get("gsm.serial"));
		pcbaAdapter = new SnInfoAdapter(getBaseContext(),pcbaList);    
		phoneAdapter = new SnInfoAdapter(getBaseContext(),phoneList);   
		pcbaListView.setEnabled(false);
		phoneListView.setEnabled(false);
		pcbaListView.setAdapter(pcbaAdapter);
		phoneListView.setAdapter(phoneAdapter);
	}
	private void getListViewData(String temp){
		if(SystemProperties.get("ro.prize_customer").equals("coosea")){
			cooseaTestInfo(temp);
		}else if(SystemProperties.get("ro.prize_customer").equals("odm")){
			odmTestInfo(temp);
		}else{
			koobeeTestInfo(temp);
		}
	}
	public class SnInfoAdapter extends BaseAdapter{
    	Context context;
    	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    	
    	public SnInfoAdapter(Context context,ArrayList<HashMap<String,String>> snList){
    		this.context = context;
    		list = snList;
    	}    	
    	
    	@Override
    	public int getCount() {
    		// TODO Auto-generated method stub
    		return list.size();
    	}

    	@Override
    	public Object getItem(int position) {
    		// TODO Auto-generated method stub
    		return position;  
    	}

    	@Override
    	public long getItemId(int id) {
    		// TODO Auto-generated method stub
    		return id;
    	}

    	
    	@Override
    	public View getView(int position, View convertView,
    			ViewGroup parent) {
    		// TODO Auto-generated method stub
    		ViewHolder holder;
    		if(convertView==null){
    	    LayoutInflater inflater = LayoutInflater.from(context);
    		convertView = inflater.inflate(R.layout.sn_item,parent, false);
    		holder = new ViewHolder();
    		holder.textViewItem01 = (TextView)convertView.findViewById(  
                    R.id.key);  
    		holder.textViewItem02 = (TextView)convertView.findViewById(  
                    R.id.value);  

    		convertView.setTag(holder);
    		}else{
    			holder = (ViewHolder)convertView.getTag();                 
    		}
    		holder.textViewItem01.setText(list.get(position).get("key").toString());     		
    		holder.textViewItem02.setText(list.get(position).get("value").toString());  
			if(list.get(position).get("value").toString().equals(getString(R.string.pass))){
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.GREEN);
			}else if(list.get(position).get("value").toString().equals(getString(R.string.fail))){
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.RED);
			}else{
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.GRAY);
			}	
    		return convertView;
    	}
    }
    
    public class ViewHolder{  
        TextView textViewItem01;  
        TextView textViewItem02;   
    }
}