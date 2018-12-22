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

package com.prize.music.online.task;

import android.os.Handler;
import android.os.Message;

import com.google.gson.JsonElement;
import com.prize.app.constants.RequestResCode;
import com.prize.music.online.task.base.BaseRequestTask;
import com.xiami.sdk.XiamiSDK;

/**
 * 
 ** 推荐位 排行异步任务
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RankTask extends BaseRequestTask {
	private Handler mHandler;

	public RankTask(XiamiSDK xiamiSDK, String requestHost) {

		super(xiamiSDK, requestHost);
		// TODO Auto-generated constructor stub

	}

	public RankTask(XiamiSDK xiamiSDK, String requestHost, Handler handler) {
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
