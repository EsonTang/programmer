/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.music.online.task.base;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.xiami.RequestManager;
import com.prize.app.xiami.XiamiApiResponse;
import com.prize.music.helpers.utils.LogUtils;
import com.xiami.core.exceptions.AuthExpiredException;
import com.xiami.core.exceptions.ResponseErrorException;
import com.xiami.sdk.XiamiSDK;

/**
 * 
 **
 * 针对虾米api网络请求基类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public abstract class BaseRequestTask extends
		AsyncTask<HashMap<String, Object>, Long, JsonElement> {

	private XiamiSDK xiamiSDK;
	private String requestHost;
	private RequestManager requestManager;

	public BaseRequestTask(XiamiSDK xiamiSDK, String requestHost) {
		this.xiamiSDK = xiamiSDK;
		this.requestHost = requestHost;
		requestManager = RequestManager.getInstance();
	}

	public abstract void postInBackground(Exception e);

	@Override
	public JsonElement doInBackground(HashMap<String, Object>... params) {
		try {
			
			if(isCancelled()){
				return null;
			}
			HashMap<String, Object> param = params[0];
			String result = xiamiSDK.xiamiSDKRequest(requestHost, param);
			if (!TextUtils.isEmpty(result)) {
				Gson gson = requestManager.getGson();
				XiamiApiResponse response = gson.fromJson(result,
						XiamiApiResponse.class);
				if (requestManager.isResponseValid(response)) {
					return response.getData();
				}

				return null;

			} else {
				LogUtils.i("0000", "R.string.error_response-");
				return null;
			}
		} catch (NoSuchAlgorithmException e) {
			LogUtils.i("0000", "NoSuchAlgorithmException-" + e.getMessage());
			postInBackground(e);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			LogUtils.i("0000", "IOException-" + e.getMessage());
			postInBackground(e);
			e.printStackTrace();
			return null;
		} catch (AuthExpiredException e) {
			postInBackground(e);
			LogUtils.i("0000", "AuthExpiredException-" + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (ResponseErrorException e) {
			postInBackground(e);
			LogUtils.i("0000", "ResponseErrorException-" + e.getMessage());
			return null;
		}
	}
}
