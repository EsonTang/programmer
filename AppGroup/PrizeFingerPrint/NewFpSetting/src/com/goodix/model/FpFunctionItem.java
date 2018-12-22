package com.goodix.model;

public class FpFunctionItem {
	
	public static final String ANSWER = "ANSWER";
	public static final String CAMERA = "CAMERA";
	public static final String STOP_CLOCK = "STOP_CLOCK";
	public static final String DISPLAY_NOTICE_PANEL = "DISPLAY_NOTICE_PANEL";
	public static final String ONE_KEY_SWITCH_APP_LOCK = "ONE_KEY_SWITCH_APP_LOCK";
	public static final String ONE_KEY_SWITCH_FP_LOCK = "ONE_KEY_SWITCH_FP_LOCK";
	
	public static final String  ID= "_id";
	public static final String  FP_FUNCTION_NAME= "fpFunctionName";
	public static final String  FP_FUNCTION_STATUS= "fpFunctionStatus";

	private int _id;  
	private String fpFunctionName;  
	private int fpFunctionStatus;

	public FpFunctionItem() {
		super();
	}

	public FpFunctionItem(int id, String fpFunctionName, int fpFunctionStatus) {
		super();
		this._id = id;
		this.fpFunctionName = fpFunctionName;
		this.fpFunctionStatus = fpFunctionStatus;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public String getFpFunctionName() {
		return fpFunctionName;
	}

	public void setFpFunctionName(String fpFunctionName) {
		this.fpFunctionName = fpFunctionName;
	}

	public int getFpFunctionStatus() {
		return fpFunctionStatus;
	}

	public void setFpFunctionStatus(int fpFunctionStatus) {
		this.fpFunctionStatus = fpFunctionStatus;
	}
	
}
