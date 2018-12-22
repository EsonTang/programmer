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

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.prize.cloud.R;
import com.prize.cloud.bean.Respond;
import com.prize.cloud.util.Utils;

/**
 * 取消绑定
 * @author yiyi
 * @version 1.0.0
 */
public class UnBindTask extends NetTask<Void> {
	private String passport, email, userId;

	public UnBindTask(Context ctx, TaskCallback<Void> taskCallback,
			String passport, String email, String userId) {
		super(ctx, taskCallback);
		this.passport = passport;
		this.email = email;
		this.userId = userId;
	}

	@Override
	public void doExecute() {

		HttpUtils http = new HttpUtils(NETWORK_TIMEOUT);
		RequestParams params = new RequestParams();
		params.addHeader("KOOBEE", "dido");
		params.addBodyParameter("email", email);
		params.addBodyParameter("passport", passport);
		params.addBodyParameter("userId", userId);
		String url = HOST + "/cloud/account/unbinding";
		http.send(HttpMethod.POST, url, params, new RespondCallback() {

			@Override
			public void onSuccess(Respond respond) {
				if (respond.getMsg() == null) {
					onTaskError(0, mContext.getResources().getString(R.string.request_failure));
					return;
				}
				if (respond.getMsg().equals(NETWORK_OK)) {
					Utils.updateEmail(mContext, null);
					onTaskSuccess(null);// 验证码验证成功，进行下一步操作
				} else {
					onTaskError(0, mContext.getResources().getString(R.string.unbind_failure));
				}	
			}

			@Override
			public void onError(int errorCode, String msg) {
				if (errorCode == NetTask.ERROR_TIMEOUT) {
					onTaskError(errorCode, mContext.getString(R.string.timeout));
				} else if (errorCode == NetTask.ERROR_NETWORK) {
					onTaskError(0, mContext.getResources().getString(R.string.network_connection_fail));
				} else if (errorCode == NetTask.ERROR_FAILURE) {
					onTaskError(errorCode, mContext.getString(R.string.failure));
				} else {
					onTaskError(errorCode, msg);
				}
			}

		});

	}

}
