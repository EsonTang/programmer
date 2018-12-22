package com.prize.factorytest.BlueTooth;

public class DeviceInfo {
	private String name = "";
	private String address = "";
	private short rssi = 0;
	public DeviceInfo(String name, String address,short rssi) {

		super();
		this.name = name;
		this.address = address;
		this.rssi = rssi;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getAddress() {

		return address;
	}

	public void setAddress(String address) {

		this.address = address;
	}
	
	public short getRssi() {

		return rssi;
	}

	public void setRssi(short rssi) {

		this.rssi = rssi;
	}

}