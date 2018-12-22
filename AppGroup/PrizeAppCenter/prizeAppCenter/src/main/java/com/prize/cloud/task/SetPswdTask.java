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

import org.xutils.http.RequestParams;

import android.content.Context;

import com.prize.appcenter.R;
import com.prize.cloud.bean.Respond;
import com.prize.cloud.util.Utils;
import com.prize.custmerxutils.XExtends;

/**
 *  设置新密码，用于找回密码
 * 
 * @author huangchangguo
 * @version 1.9
 */
public class SetPswdTask extends NetTask<Void> {
	private String key, phoneNum, password;

	public SetPswdTask(Context ctx, TaskCallback<Void> taskCallback,
			String key, String phoneNum, String password) {
		super(ctx,  taskCallback);
		this.key = key;
		this.phoneNum = phoneNum;
		this.password = password;
	}

	@Override
	public void doExecute() {

		String url = HOST + "/cloud/account/retrievepassword";

		RequestParams params = new RequestParams(url);
		params.setConnectTimeout(NETWORK_TIMEOUT);
		params.addHeader("KOOBEE", "dido");
		params.addBodyParameter("checkcode", key);
		params.addBodyParameter("username", phoneNum);
		params.addBodyParameter("password", Utils.getMD5(password));

		XExtends.http().post(params, new RespondCallback() {

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(Respond respond) {
				
				if (!respond.getMsg().equals(NETWORK_OK)) {
					if (mContext != null) {
						onTaskError(
								0,
								mContext.getResources().getString(
										R.string.net_error));
					}
					return;
				}
				
			/*	LoginInfo info = respond.convert(LoginInfo.class);
				if (info == null) {
					return;
				}
				Utils.saveInfo(mContext, info);
				ContentValues values = new ContentValues();
				values.put("loginName", info.getUserId());
				values.put("password", password);
				values.put("passport", info.getPassport());
				Utils.saveAccount(mContext, values);

				Intent it = new Intent(CloudIntent.ACTION_PASSPORT_GET);
				mContext.sendBroadcast(it);*/

				onTaskSuccess(null);
						
			}

			@Override
			public void onError(int errorCode, String msg) {
				if (errorCode == NetTask.ERROR_TIMEOUT) {
					onTaskError(errorCode, mContext.getString(R.string.timeout));
				} else if (errorCode == NetTask.ERROR_NETWORK) {
					onTaskError(
							0,
							mContext.getResources().getString(
									R.string.net_error));
				} else if (errorCode == NetTask.ERROR_FAILURE) {
					onTaskError(errorCode, mContext.getString(R.string.failure));
				} else {
					onTaskError(errorCode, msg);
				}
			}

		});

	}

}
