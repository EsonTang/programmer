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

import com.prize.cloud.BuildConfig;
import com.prize.cloud.bean.Respond;
import com.prize.cloud.util.GObjectMapper;
import com.prize.cloud.util.JLog;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.xutils.common.Callback.CommonCallback;

import java.io.IOException;

/**
 * 服务器JSON的拦截器，将data部分数据返回，或调用失败接口
 * 
 * @author yiyi
 * @version 1.0.0
 */
public abstract class RespondCallback implements CommonCallback<String> {

	private static final String TAG = "RespondCallback";

	@Override
	public void onSuccess(String responseInfo) {
		JLog.i(TAG, "onSuccess-->" + responseInfo);
		intercept(responseInfo);
	}

	/**
	 * 拦截下发数据
	 * 
	 * @param responseInfo
	 */
	private void intercept(String responseInfo) {
		if (BuildConfig.DEBUG)
			JLog.i(TAG, responseInfo);
		Respond respond = null;
		try {
			respond = GObjectMapper.get()
					.readValue(responseInfo, Respond.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (respond == null)
			onError(NetTask.ERROR_UNIFIED, "json error.");
		if (respond.getCode() != 0)
			onError(respond.getCode(), respond.getMsg());
		else
			onSuccess(respond);

	}

	@Override
	public void onError(Throwable ex, boolean isOnCallback) {
		String msg = ex.getMessage();
		if (BuildConfig.DEBUG)
			JLog.i(TAG, ex.getMessage());
		if (msg.contains("TimeoutException")) {
			onError(NetTask.ERROR_TIMEOUT, msg);
			return;
		}
		if (msg.contains("UnknownHostException")) {
			onError(NetTask.ERROR_NETWORK, msg);
			return;
		}
		onError(NetTask.ERROR_FAILURE, msg);

	}

	public abstract void onSuccess(Respond respond);

	public abstract void onError(int errorCode, String msg);
}
