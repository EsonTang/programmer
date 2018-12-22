package com.prize.uploadappinfo.constants;
/**
 *
 * 类名称：Constant
 * 
 * 创建人：longbaoxiu
 * 
 * 修改时间：2016年6月12日 下午5:28:19
 * 
 * @version 1.0.0
 *
 */
public class Constant {
	/*****3天启动一次上传任务*****/
	public static final int PUSH_FOR_TIME = 60 * 60 * 24*3;

	// 现网地址
//	 public static final String GIS_URL =
//	 "http://appstore.szprize.cn/appstore";
	public static final String GIS_URL = "http://192.168.1.235:8080/ics";
//	public static final String GIS_URL = "http://192.168.1.187:8080/ics";
	/****接口安全校验，先请求改接口来获取一个pid，在后续请求中将此pid和加密之后的sign上传，保证接口的安全性*****/
	public static final String PID_URL = GIS_URL+"/api/pid";
	/****此接口需要与pid接口联用*****/
	public static final String UUID_URL = GIS_URL+"/api/uuid";
	/****上传app信息*****/
	public static final String APPINFOS_URL = GIS_URL+"/collect/appinfos";
	public static final String KEY_TID = "persist.sys.tid";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_LAST_GET_ADDRESS_TIME = "last_get_address_time";
	public static final String KEY_UPLOAD_ALL_APP = "upload_all_app";
	public static final String APP_TYPE_INSTALL = "install";
	public static final String APP_TYPE_UNINSTALL = "uninstall";
	/****刷新获取地址的时间间隔 12小时*****/
	public static final long REQUEST_ADDRESS_RATE = 12*60*60*1000;

}
