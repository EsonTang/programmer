package com.prize.factorytest;

import android.app.Application;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.prize.factorytest.BlueTooth.DeviceInfo;

import java.util.List;
import java.util.ArrayList;

import com.prize.factorytest.util.SharedPreferencesHelper;
public class FactoryTestApplication extends Application {
	private static final String TAG = "FactoryTestApplication";
	private List<ScanResult> wifiScanResult = new ArrayList<ScanResult>();	
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
	private boolean isWifiScanning =false;
	private boolean isBluetoothScanning =false;
	private SharedPreferencesHelper shared_pref;
	private String defDuration = "00:00:00";
    @Override
    public void onCreate() {
        super.onCreate();
		shared_pref=new SharedPreferencesHelper(this, "com.prize.factorytest"); 
		loadDefaultDuration();
		loadDefaultAgingTestItem();
		loadDefaultAgingTestTime();
		loadCurrentTimeMillis();
		loadDefaultTestReport();
    }
    private void loadDefaultTestReport(){
    	if(shared_pref.getValue("reboot_result")==null){
    		shared_pref.putValue("reboot_result", "untest");
    	}
    	if(shared_pref.getValue("sleep_result")==null){
    		shared_pref.putValue("sleep_result", "untest");
    	}
    }
    private void loadDefaultAgingTestItem(){
    	if(shared_pref.getValue("reboot_selected")==null){
    		shared_pref.putValue("reboot_selected", "0");
    	}
    	if(shared_pref.getValue("sleep_selected")==null){
    		shared_pref.putValue("sleep_selected", "0");
    	}
    	if(shared_pref.getValue("video_speaker_selected")==null){
    		shared_pref.putValue("video_speaker_selected", "0");
    	}
    	if(shared_pref.getValue("video_receiver_selected")==null){
    		shared_pref.putValue("video_receiver_selected", "0");
    	}
    	if(shared_pref.getValue("vibrate_selected")==null){
    		shared_pref.putValue("vibrate_selected", "0");
    	}
    	if(shared_pref.getValue("mic_loop_selected")==null){
    		shared_pref.putValue("mic_loop_selected", "0");
    	}
    	if(shared_pref.getValue("front_camera_selected")==null){
    		shared_pref.putValue("front_camera_selected", "0");
    	}
    	if(shared_pref.getValue("back_camera_selected")==null){
    		shared_pref.putValue("back_camera_selected", "0");
    	}
    }
    //time is value seconds
    private void loadDefaultAgingTestTime(){
    	if(shared_pref.getValue("reboot_time")==null){
    		shared_pref.putValue("reboot_time", "1200");
    	}
    	if(shared_pref.getValue("sleep_time")==null){
    		shared_pref.putValue("sleep_time", "1200");
    	}
    	if(shared_pref.getValue("parallel_time")==null){
    		shared_pref.putValue("parallel_time", "4800");
    	}
    }
    private void loadCurrentTimeMillis(){
    	if(shared_pref.getValue("reboot_currenttimemillis")==null){
    		shared_pref.putValue("reboot_currenttimemillis", String.valueOf(System.currentTimeMillis()));
    	}
    }
    private void loadDefaultDuration(){
    	if(shared_pref.getValue("video_speaker_duration")==null){
    		shared_pref.putValue("video_speaker_duration", defDuration);
    	}
    	if(shared_pref.getValue("video_receiver_duration")==null){
    		shared_pref.putValue("video_receiver_duration", defDuration);
    	}
    	if(shared_pref.getValue("vibrate_duration")==null){
    		shared_pref.putValue("vibrate_duration", defDuration);
    	}
    	if(shared_pref.getValue("mic_loop_duration")==null){
    		shared_pref.putValue("mic_loop_duration", defDuration);
    	}
    	if(shared_pref.getValue("front_camera_duration")==null){
    		shared_pref.putValue("front_camera_duration", defDuration);
    	}
    	if(shared_pref.getValue("back_camera_duration")==null){
    		shared_pref.putValue("back_camera_duration", defDuration);
    	}
	}
	public void setWifiScanResult(List<ScanResult> wifiScanResult)
    {
        this.wifiScanResult = wifiScanResult;
    }
    
    public List<ScanResult> getWifiScanResult()
    {
        return wifiScanResult;
    }
	
	public void setBluetoothDeviceList(List<DeviceInfo> mDeviceList)
    {
        this.mDeviceList = mDeviceList;
    }
    
    public List<DeviceInfo> getBluetoothDeviceList()
    {
        return mDeviceList;
    }
	
	public void setIsWifiScanning(boolean isWifiScanning){
		this.isWifiScanning = isWifiScanning;
	}
	
	public boolean getIsWifiScanning(){
		return isWifiScanning;
	}
	
	public void setIsBluetoothScanning(boolean isBluetoothScanning){
		this.isBluetoothScanning = isBluetoothScanning;
	}
	
	public boolean getIsBluetoothScanning(){
		return isBluetoothScanning;
	}
	
	public SharedPreferencesHelper getSharePref(){
		return shared_pref;
	}

}
