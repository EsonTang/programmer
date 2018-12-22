package com.prize.music.online.task;

import android.os.Handler;
import android.os.Message;

import com.google.gson.JsonElement;
import com.prize.app.constants.RequestResCode;
import com.prize.music.online.task.base.BaseRequestTask;
import com.xiami.sdk.XiamiSDK;

/**
 * 歌手分类 
 */
public class SingerByTypeTask extends BaseRequestTask{

    private Handler mHandler;

	public SingerByTypeTask(XiamiSDK xiamiSDK, String requestHost) {

		super(xiamiSDK, requestHost);

	}

	public SingerByTypeTask(XiamiSDK xiamiSDK, String requestHost, Handler handler) {
		super(xiamiSDK, requestHost);
		this.mHandler = handler;
	}

	@Override
	public void postInBackground(Exception e) {// RequestMethods.METHOD_MOBILE_SDK_IMAGE
		Message msg = Message.obtain();
		msg.what = RequestResCode.REQUEST_EXCEPTION;
		msg.obj = e;
		mHandler.sendMessage(msg);

	}

	@Override
	protected void onPostExecute(JsonElement result) {
		super.onPostExecute(result);
		if (result != null) {
			Message msg = Message.obtain();
			msg.what = RequestResCode.REQUEST_OK;
			msg.obj = result;
			mHandler.sendMessage(msg);
		} else {
			mHandler.sendEmptyMessage(RequestResCode.REQUEST_FAILE);
		}
	}

}
