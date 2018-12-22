package com.prize.music.online.task;

import android.os.Handler;
import android.os.Message;

import com.google.gson.JsonElement;
import com.prize.music.online.task.base.BaseRequestTask;
import com.xiami.sdk.XiamiSDK;

/**
 * 关键字热搜
 */
public class AutoTipsTask extends BaseRequestTask{

    private Handler mHandler;

	public AutoTipsTask(XiamiSDK xiamiSDK, String requestHost) {

		super(xiamiSDK, requestHost);

	}

	public AutoTipsTask(XiamiSDK xiamiSDK, String requestHost, Handler handler) {
		super(xiamiSDK, requestHost);
		this.mHandler = handler;
	}

	@Override
	public void postInBackground(Exception e) {// RequestMethods.METHOD_MOBILE_SDK_IMAGE
		Message msg = Message.obtain();
		msg.what = com.prize.app.constants.RequestResCode.REQUEST_EXCEPTION;
		msg.obj = e;
		mHandler.sendMessage(msg);

	}

	@Override
	protected void onPostExecute(JsonElement result) {
		super.onPostExecute(result);
		if (result != null) {
			Message msg = Message.obtain();
			msg.what = com.prize.app.constants.RequestResCode.REQUEST_OK;
			msg.obj = result;
			mHandler.sendMessage(msg);
		} else {
			mHandler.sendEmptyMessage(com.prize.app.constants.RequestResCode.REQUEST_FAILE);
		}
	}

}
