package com.prize.app.xiami;

import com.google.gson.JsonElement;

import java.io.Serializable;

/**
 * Created by shizhao.czc on 2015/5/6.
 */
public class XiamiApiResponse implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;
	private String status;
	private JsonElement data;
	private String message;
	private int state;

	public XiamiApiResponse() {
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public JsonElement getData() {
		return data;
	}

	public void setData(JsonElement data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
