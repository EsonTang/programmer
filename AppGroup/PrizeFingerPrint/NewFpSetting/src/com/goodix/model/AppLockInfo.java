package com.goodix.model;

public class AppLockInfo {
	public static final String  ID= "_id";
	public static final String  INFO= "info";

	/**
	 * id，无需主动设置，数据库已设为自增长
	 */
	private int _id;  

	/**
	 * 应用锁信息
	 */
	private String info;

	public AppLockInfo() {
		super();
	}

	public AppLockInfo(int id, String info) {
		super();
		this._id = id;
		this.info = info;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}  
}
