/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/
package com.prize.cloud.task;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.util.LogUtils;
import com.prize.cloud.db.DbManager;

/**
 * 请求基类，所有请求需继承该类
 * @author yiyi
 * @version 1.0.0
 * @param <T>
 */
public abstract class NetTask<T> {
	public static final int ERROR_TIMEOUT = -996;
	public static final int ERROR_FAILURE = -997;// 与服务器连接失败
	public static final int ERROR_NETWORK = -998;
	public static final int ERROR_UNIFIED = -999;
	public static final String NETWORK_OK = "OK";
	public static final int NETWORK_TIMEOUT = 15000;

	//public static final String HOST = "http://101.200.187.142";
	public static final String HOST = "http://cloud.szprize.cn";
	//public static final String HOST = "http://192.168.1.187:8080";
	DbUtils dbUtils;
	TaskCallback<T> callback;
	Context mContext;

	public NetTask(Context ctx, TaskCallback<T> taskCallback) {
		mContext = ctx.getApplicationContext();
		this.dbUtils = DbManager.getInstance().getDb();
		callback = taskCallback;
	}

	public abstract void doExecute();

	/**
	 * 请求成功，获得数据
	 * @param data
	 */
	protected void onTaskSuccess(T data) {
		if (callback != null)
			callback.onTaskSuccess(data);
	}

	/**
	 * 请求失败
	 * @param errorCode 失败代码
	 * @param msg 失败原因
	 */
	protected void onTaskError(int errorCode, String msg) {
		LogUtils.e(msg);
		if (callback != null)
			callback.onTaskError(errorCode, msg);

	}
}
