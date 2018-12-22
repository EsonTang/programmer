package com.pr.scuritycenter.bean;

public class BlackNumberInfo {
	// 电话号码
	private String number;
	/**
	 * 拦截模式 1 全部拦截 2 短信拦截 3 电话拦截
	 */
	private String mode;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
