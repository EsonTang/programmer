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

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.prize.cloud.bean.Respond;
import com.prize.cloud.task.pojo.LoginInfo;
import com.prize.cloud.util.CloudIntent;
import com.prize.cloud.util.Utils;
import com.prize.custmerxutils.XExtends;

/**
 * 非本机短信方式注册---快速注册
 * 
 * @author yiyi
 * @version 1.9 changed by huangchangguo
 */
public class RegisterTask extends NetTask<String> {
	private String checkcode, username, password;

	public RegisterTask(Context ctx, TaskCallback<String> taskCallback,
			String checkcode, String username, String password) {
		super(ctx, taskCallback);
		this.checkcode = checkcode;
		this.username = username;
		this.password = password;
	}

	@Override
	public void doExecute() {

		String url = HOST + "/cloud/account/registernew";
		RequestParams params = new RequestParams(url);
		params.setConnectTimeout(NETWORK_TIMEOUT);
		params.addHeader("KOOBEE", "dido");
		params.addBodyParameter("checkcode", checkcode);
		params.addBodyParameter("username", username);
		params.addBodyParameter("password", Utils.getMD5(password));

		/*
		 * XExtends.http().post(params, new RespondCallback() {
		 * 
		 * @Override public void onCancelled(CancelledException cex) {
		 * 
		 * }
		 * 
		 * @Override public void onFinished() {
		 * 
		 * }
		 * 
		 * @Override public void onSuccess(Respond respond) {
		 * 
		 * if (!respond.getMsg().equals(NETWORK_OK)) { if (mContext != null) {
		 * onTaskError( 0, mContext.getResources().getString(
		 * R.string.network_connection_fail)); }
		 * 
		 * return; }
		 * 
		 * LoginInfo info = respond.convert(LoginInfo.class); if (info == null)
		 * { return; } Utils.saveInfo(mContext, info); ContentValues values =
		 * new ContentValues(); values.put("loginName", info.getUserId());
		 * values.put("password", password); values.put("passport",
		 * info.getPassport()); Utils.saveAccount(mContext, values);
		 * 
		 * Intent it = new Intent(CloudIntent.ACTION_PASSPORT_GET);
		 * mContext.sendBroadcast(it);
		 * 
		 * onTaskSuccess(info.getUserId()); }
		 * 
		 * @Override public void onError(int errorCode, String msg) { if
		 * (errorCode == NetTask.ERROR_TIMEOUT) { onTaskError(errorCode,
		 * mContext.getString(R.string.timeout)); } else if (errorCode ==
		 * NetTask.ERROR_NETWORK) { onTaskError( 0,
		 * mContext.getResources().getString(
		 * R.string.network_connection_fail)); } else if (errorCode ==
		 * NetTask.ERROR_FAILURE) { onTaskError(errorCode,
		 * mContext.getString(R.string.failure)); } else {
		 * onTaskError(errorCode, msg); } }
		 * 
		 * });
		 */

		// 改了还是有问题,错误方法会走
		XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {

				Gson gson = new Gson();

				Respond respond = gson.fromJson(result, Respond.class);
				int code = respond.getCode();
				String msg = respond.getMsg();
				if (code != 0) {

					Toast.makeText(x.app(), msg, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(x.app(), "您的账号注册成功！", Toast.LENGTH_SHORT)
							.show();

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

					Intent it = new Intent(CloudIntent.ACTION_PASSPORT_GET);
					mContext.sendBroadcast(it);

					// 执行完成之后走成功的方法
					// onTaskSuccess(info.getUserId());
					// TODO
					onTaskSuccess(info.getPhone());
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				/*
				 * if (ex instanceof HttpException) { // 网络错误 HttpException
				 * httpEx = (HttpException) ex; int responseCode =
				 * httpEx.getCode(); String message = httpEx.getMessage(); if
				 * (message.contains("TimeOut")) {
				 * 
				 * Toast.makeText(x.app(), "失败了,请求超时" + message,
				 * Toast.LENGTH_LONG).show(); } } else {
				 * 
				 * Toast.makeText(x.app(), "失败了...", Toast.LENGTH_LONG).show();
				 * }
				 */
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Toast.makeText(x.app(), "联网被取消", Toast.LENGTH_LONG).show();

			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub

			}

		});

	}

}
