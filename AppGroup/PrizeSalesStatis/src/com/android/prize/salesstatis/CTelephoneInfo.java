package com.android.prize.salesstatis;

import java.lang.reflect.Method;
import android.content.Context;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

import android.text.TextUtils;
import android.util.Log;

public class CTelephoneInfo {
	private static final String TAG = "PrizeSalesStatis";
	private String imeiSIM1;// IMEI
	private String imeiSIM2;//IMEI
	private String meidSIM1;//IMEI
	private String meidSIM2;//IMEI
	private String iNumeric1;//sim1 code number 
	private String iNumeric2;//sim2 code number	
	private boolean isSIM1Ready;//sim1
	private boolean isSIM2Ready;//sim2
	private String iDataConnected1 = "0";//sim1 0 no, 1 connecting, 2 connected, 3 suspended.
	private String iDataConnected2 = "0";//sim2
	private static CTelephoneInfo CTelephoneInfo;
	private static Context mContext;
	
	private CTelephoneInfo() {
	}

	public synchronized static CTelephoneInfo getInstance(Context context){
	    if(CTelephoneInfo == null) {	
	        CTelephoneInfo = new CTelephoneInfo();
	    }
	    mContext = context;	    
	    return CTelephoneInfo;
	}
	
	public String getImeiSIM1() {
	    return imeiSIM1;
	}
	
	public String getImeiSIM2() {
	    return imeiSIM2;
	}
	
	public String getMeidSIM1() {
	    return meidSIM1;
	}
	
	public String getMeidSIM2() {
	    return meidSIM2;
	}
	
	public boolean isSIM1Ready() {
	    return isSIM1Ready;
	}
	
	public boolean isSIM2Ready() {
	    return isSIM2Ready;
	}
	
	public boolean isDualSim(){
		return imeiSIM2 != null;
	}
	
	public boolean isDataConnected1(){
		if(TextUtils.equals(iDataConnected1, "2")||TextUtils.equals(iDataConnected1, "1"))
			return true;
		else 
			return false;
	}
	
	public boolean isDataConnected2(){
		if(TextUtils.equals(iDataConnected2, "2")||TextUtils.equals(iDataConnected2, "1"))
			return true;
		else 
			return false;
	}
	
	public String getINumeric1(){
		return iNumeric1;
	}
	
	public String getINumeric2(){
		return iNumeric2;
	}
	
	public String getINumeric(){
		if(imeiSIM2 != null){
			if(iNumeric1 != null && iNumeric1.length() > 1)
				return iNumeric1;
			
			if(iNumeric2 != null && iNumeric2.length() > 1)
				return iNumeric2;
		}		
		return iNumeric1;
	}
	
	public void setPhoneInfo(){
		TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		for (int slotId = 0; slotId < telephonyManager.getSimCount(); slotId ++) {
			Log.v(TAG, "[CTelephoneInfo]setPhoneInfo()-->SimCount = " + telephonyManager.getSimCount());
            setPhoneFactoryValue(slotId);
            Log.v(TAG, "[CTelephoneInfo]setPhoneInfo()-->imeiSIM1 = " + imeiSIM1 +", imeiSIM2 = "+imeiSIM2+", meidSIM1 = "+meidSIM1+", meidSIM2 = "+meidSIM2);
        }
	}
	private void setPhoneFactoryValue(int phoneId) {
        final Phone phone = PhoneFactory.getPhone(phoneId);

        if (phone != null) {
            if (phone.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            	if(phoneId == 0){
            		meidSIM1 = phone.getMeid();
            	}else{
            		meidSIM2 = phone.getMeid();
            	}

                if (phone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                	if(phoneId == 0){
                		imeiSIM1 = phone.getImei();
                		iNumeric1 = phone.getIccSerialNumber();
                	}else{
                		imeiSIM2 = phone.getImei();
                		iNumeric2 = phone.getIccSerialNumber();
                	}
                }
            } else {
            	if(phoneId == 0){
            		imeiSIM1 = phone.getImei();
            		iNumeric1 = phone.getIccSerialNumber();
            	}else{
            		imeiSIM2 = phone.getImei();
            		iNumeric2 = phone.getIccSerialNumber();
            	}
            }
        }
    }
}
