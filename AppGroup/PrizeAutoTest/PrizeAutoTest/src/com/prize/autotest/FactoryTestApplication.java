package com.prize.autotest;

import android.app.Application;
import android.net.wifi.ScanResult;

import com.prize.autotest.mmi.DeviceInfo;

import java.util.List;
import java.util.ArrayList;

public class FactoryTestApplication extends Application {
	private List<ScanResult> wifiScanResult = new ArrayList<ScanResult>();
	private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
	private boolean isWifiScanning = false;
	private boolean isBluetoothScanning = false;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void setWifiScanResult(List<ScanResult> wifiScanResult) {
		this.wifiScanResult = wifiScanResult;
	}

	public List<ScanResult> getWifiScanResult() {
		return wifiScanResult;
	}

	public void setBluetoothDeviceList(List<DeviceInfo> mDeviceList) {
		this.mDeviceList = mDeviceList;
	}

	public List<DeviceInfo> getBluetoothDeviceList() {
		return mDeviceList;
	}

	public void setIsWifiScanning(boolean isWifiScanning) {
		this.isWifiScanning = isWifiScanning;
	}

	public boolean getIsWifiScanning() {
		return isWifiScanning;
	}

	public void setIsBluetoothScanning(boolean isBluetoothScanning) {
		this.isBluetoothScanning = isBluetoothScanning;
	}

	public boolean getIsBluetoothScanning() {
		return isBluetoothScanning;
	}

}
