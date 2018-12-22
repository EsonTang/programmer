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
package com.prize.cloud.vp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.prize.cloud.activity.BindedActivity;
import com.prize.cloud.activity.SetPswdActivity;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.task.AchieveTask;
import com.prize.cloud.task.BindTask;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.task.VerifyTask;
import com.prize.cloud.util.Utils;

/**
 * 验证码相关的操作类
 * @author yiyi
 * @version 1.0.0
 */
public class CodePresenter {
	private int type;
	private Context mContext;
	private ICodeView iCodeView;

	private String username;

	private Handler mHandler;
	private int duration = 60;

	public CodePresenter(int type, Context context, ICodeView codeView) {
		super();
		this.type = type;
		this.mContext = context;
		this.iCodeView = codeView;

		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				duration--;
				if (iCodeView != null)
					iCodeView.calculate(duration);
				if (duration > 0) {
					mHandler.sendEmptyMessageDelayed(0, 1000);
				} else {
					mHandler.removeMessages(0);
				}

			};
		};
	}

	public void onDestroy() {
		mHandler.removeMessages(0);
		mHandler = null;
		iCodeView = null;
	}

	/**
	 * 获取验证码
	 * @param editText 电话或邮箱
	 */
	public void getCode(String editText) {
		this.username = editText;
		
		new AchieveTask(mContext, new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				//Toast.makeText(mContext, mContext.getString(R.string.code_has_been_send), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (iCodeView != null)
					iCodeView.onGetCodeFail(msg);
				mHandler.removeMessages(0);
				if (iCodeView != null)
					iCodeView.calculate(0);
			}
		}, username, type).doExecute();

		duration = 60;
		mHandler.sendEmptyMessage(0);

	}
	
	/**
	 * 方法描述：首次跳转页面获得验证码
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void getCodeFirst(){
		duration = 60;
		mHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 对验证码进行校验
	 * @param account 手机or邮箱
	 * @param checkcode 验证码
	 */
	public void veriFy(String account, String checkcode) {
		username = account;
		new VerifyTask(mContext, new TaskCallback<String>() {

			@Override
			public void onTaskSuccess(String data) {
				onVerifySuccess(data);
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (iCodeView != null)
					iCodeView.onVerifyFail(msg);

			}
		}, account, checkcode, type).doExecute();
	}

	/**
	 * 验证成功
	 * @param key 成功后的凭证
	 */
	private void onVerifySuccess(String key) {
		switch (type) {
		case CodeType.TYPE_REGISTER:
		case CodeType.TYPE_LOSTPSWD:
			goSetPswd(key);
			break;
		case CodeType.TYPE_BIND:// 验证成功后直接请求绑定邮箱
			binding(key);
			break;

		default:
			break;
		}

	}

	/**
	 * 请求绑定
	 * @param key 验证码校验成功后的凭证
	 */
	private void binding(String key) {
		CloudAccount account = Utils.curAccount(mContext);
		new BindTask(mContext, new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				goBinded(username);
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (iCodeView != null)
					iCodeView.onBindFail(msg);

			}
		}, account.getPassport(), key, username, account.getLoginName())
				.doExecute();

	}

	/**
	 * 跳转至设置密码页面
	 * @param key
	 */
	private void goSetPswd(String key) {
		Intent it = new Intent(mContext, SetPswdActivity.class);
		it.putExtra("username", username);
		it.putExtra("key", key);
		it.putExtra("type", type);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(it);

	}

	/**
	 * 跳至绑定成功页
	 * @param email
	 */
	private void goBinded(String email) {
		Intent it = new Intent(mContext, BindedActivity.class);
		it.putExtra("email", email);
		mContext.startActivity(it);
	}

}
