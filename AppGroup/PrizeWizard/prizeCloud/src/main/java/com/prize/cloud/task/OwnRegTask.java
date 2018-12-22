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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.prize.cloud.R;
import com.prize.cloud.bean.Respond;
import com.prize.cloud.task.pojo.LoginInfo;
import com.prize.cloud.util.CloudIntent;
import com.prize.cloud.util.Utils;

import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * 本机注册
 * @author yiyi
 * @version 1.0.0
 */
public class OwnRegTask extends NetTask<String> {
	private String password;
	private String singleFlag;

	public OwnRegTask(Context ctx, TaskCallback<String> taskCallback,
			String passwod, String singleFlag) {
		super(ctx, taskCallback);
		this.password = passwod;
		this.singleFlag = singleFlag;
	}

	@Override
	public void doExecute() {
		String url = HOST + "/cloud/account/registerlocal";

		RequestParams params = new RequestParams(url);
		params.setConnectTimeout(3000000);
		params.addHeader("KOOBEE", "dido");
		params.addBodyParameter("phonekey", singleFlag);
		params.addBodyParameter("password", password);

		x.http().post(params, new RespondCallback() {

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
										R.string.network_connection_fail));
					}
					return;
				}
				LoginInfo info = respond.convert(LoginInfo.class);
				if (info == null) {
					return;
				}
				Utils.saveInfo(mContext, info);

				ContentValues values = new ContentValues();
				values.put("loginName", info.getUserId());
				values.put("password", password);
				values.put("passport", info.getPassport());
				Utils.saveAccount(mContext, values);
				onTaskSuccess("");

				Intent it = new Intent(CloudIntent.ACTION_PASSPORT_GET);
				mContext.sendBroadcast(it);
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
