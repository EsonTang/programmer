package com.goodix.util;

public class ConstantUtil {
	/*数据库表名*/
	public static final String APP_LOCK_TB_NAME = "lock_app";
	public static final String APP_LOCK_INFO_TB_NAME = "lock_info";
	public static final String FP_INFO_TB_NAME = "fp_info";
	
	/*PIN码*/
	public static final String SETTING_CONFIRM_STYLE = "SETTING_CONFIRM_STYLE";
	public static final int PIN_REQUEST_CODE = 100000;
	public static final int PIN_CIPHER_STYLE = 101010;
	public static final int PIN_CIPHER_SETTING = 101011;
	public static final int PIN_CIPHER_CONFIRM = 101012;
	
	public static final int PIN_VERIFIC_OK_CODE = 101013;
	public static final int PIN_VERIFIC_FAIL_CODE = 101014;
	
	/*复杂密码*/
	public static final int COMPLEX_REQUEST_CODE = 200000;
	public static final int COMPLEX_CIPHER_STYLE = 201010;
	public static final int COMPLEX_CIPHER_SETTING = 201011;
	public static final int COMPLEX_CIPHER_CONFIRM = 201012;
	
	public static final int COMPLEX_VERIFIC_OK_CODE = 201013;
	public static final int COMPLEX_VERIFIC_FAIL_CODE = 201014;
	/*删除设备验证密码*/
	public static final String INTENT_TYPE = "INTENT_TYPE";
	public static final int DEVICE_LOCK_DELETE_TYPE = 300000;
}
