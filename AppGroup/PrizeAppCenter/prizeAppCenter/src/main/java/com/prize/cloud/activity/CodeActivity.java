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
package com.prize.cloud.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.LoginTask;
import com.prize.cloud.task.RegisterTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.vp.CodePresenter;
import com.prize.cloud.vp.ICodeView;
import com.prize.cloud.widgets.ProDialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 验证码收发-快速注册 页面
 * 
 * 注册成功后直接弹栈，返回原始页
 * 
 * @author huangchangguo
 * @version 1.0.0
 */
public class CodeActivity extends BaseActivity implements ICodeView {
	public static final String PARAM_ACCOUNT = "account_name";
	public static final String PARAM_TYPE = "code_type";
	public static final String PARAM_TITLE = "title";
	protected static final String TAG = "CodeActivity";

	private TextView mSendText;
	private TextView mLoginBtn;
	private EditText mCodeEdit, mPassword, mPasswordConfirm;

	private TextView mTitleText;
	private ImageView mTitleBack;

	private CodePresenter mPresenter;
	private String account;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_identification_password);
		WindowMangerUtils.changeStatus(getWindow());
		findViewById();
		mContext = getApplicationContext();
		AppManager.getAppManager().addActivity(this);
		account = getIntent().getStringExtra(PARAM_ACCOUNT);
		int type = getIntent().getIntExtra(PARAM_TYPE, CodeType.TYPE_REGISTER);
		mPresenter = new CodePresenter(type, this, this);
		if (TextUtils.isEmpty(account)) {
			finish();
			return;
		}

		/*
		 * String title = getIntent().getStringExtra(PARAM_TITLE); if
		 * (!TextUtils.isEmpty(title)) mTitleText.setText(title);
		 */

		/*
		 * boolean isEmail = Utils.isEmail(account); if (isEmail) {
		 * mTipText.setText(String.format(getString(R.string.use_safe_email),
		 * account)); } else {
		 * mTipText.setText(String.format(getString(R.string.use_safe_phone),
		 * account)); }
		 */
		mPasswordConfirm.addTextChangedListener(textWacher);
		mPassword.addTextChangedListener(textWacher);
		mCodeEdit.addTextChangedListener(textWacher);
		mTitleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void findViewById() {

		mSendText = (TextView) findViewById(R.id.sendtv_code);
		mLoginBtn = (TextView) findViewById(R.id.loginbtn_id);
		mCodeEdit = (EditText) findViewById(R.id.msg_code_edit);
		mPassword = (EditText) findViewById(R.id.password_edit_id);
		mPasswordConfirm = (EditText) findViewById(R.id.confirm_password_id);
		// mTitleText = (TextView) findViewById(R.id.title_id);
		mTitleBack = (ImageView) findViewById(R.id.actionbar_title_back);

		mSendText.setOnClickListener(clickListener);
		mLoginBtn.setOnClickListener(LoginClickListener);

		mCodeEdit.setFocusable(true);
		mCodeEdit.setFocusableInTouchMode(true);
		mCodeEdit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) mCodeEdit
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mCodeEdit, 0);
			}
		}, 300);
	}

	/**
	 * 点击发送验证码
	 */
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			JLog.e(TAG, account);
			mPresenter.getCode(account);
		}
	};

	private OnClickListener LoginClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			hideSoftInput();
			// 快速点击则返回
			if (Utils.isFastDoubleClick()) {
				return;
			}

			String code = mCodeEdit.getText().toString();
			String password = mPassword.getText().toString();
			String passwordConfirm = mPasswordConfirm.getText().toString();

			if (TextUtils.isEmpty(code)) {
				ToastUtils.showToast(
						mContext.getString(R.string.please_input_code),
						Toast.LENGTH_SHORT);
				return;
			} else if (code.length() < 4) {

				ToastUtils.showToast(
						mContext.getString(R.string.please_input_current_code),
						Toast.LENGTH_SHORT);
				return;
			} else if (password.length() < 6 || passwordConfirm.length() < 6) {
				ToastUtils.showToast(
						mContext.getString(R.string.password_must_sex_above),
						Toast.LENGTH_SHORT);
				return;

			} else if (password.equals(passwordConfirm)) {

				doResetPswd(code, account, password);

			} else {
				JLog.d(TAG, "-------password--" + password
						+ "----passwordConfirm--" + passwordConfirm);
				ToastUtils.showToast(mContext
						.getString(R.string.password_different));
			}

			// mPresenter.veriFy(account, code);
		}
	};

	/**
	 * 注册登录，成功后的返回
	 * 
	 * @param key
	 *            验证码验证成功后返回的key
	 * @param username
	 *            账号
	 * @param pswd
	 *            密码
	 */
	private void doResetPswd(final String key, final String username,
			final String pswd) {

		synchronized (CodeActivity.class) {

			final ProDialog proDialog = new ProDialog(mContext,
					ProgressDialog.THEME_HOLO_LIGHT,
					mContext.getString(R.string.registing));

			new RegisterTask(mContext, new TaskCallback<String>() {

				@Override
				public void onTaskSuccess(String data) {

					// 弹栈，返回原始页
					AppManager.getAppManager().finishAllActivity();

					JLog.d(TAG, "onTaskSuccess---------这里应该是返回的电话号码-------："
							+ data);

					/**
					 * 登录账号
					 */

					new LoginTask(mContext, new TaskCallback<String>() {

						@Override
						public void onTaskSuccess(String data) {
							if (proDialog != null && proDialog.isShowing()) {
								proDialog.dismiss();
							}
							JLog.d(TAG,
									"doResetPswd-LoginTask-------登录成功的返回onTaskSuccess，这里应该是Passport----："
											+ data);

							// Utils.onComplete(CodeActivity.this, true);
						}

						@Override
						public void onTaskError(int errorCode, String msg) {
							// 这个方法会总会执行
							JLog.d(TAG,
									"onTaskError---------弹栈，返回原始页+onTaskError(这个方法会总会执行)-------------");
							if (proDialog != null && proDialog.isShowing()) {
								proDialog.dismiss();
							}

						}
					}, username, pswd).doExecute();

					proDialog.show();

				}

				@Override
				public void onTaskError(int errorCode, String msg) {
					Toast.makeText(getApplicationContext(), msg + "....",
							Toast.LENGTH_SHORT).show();

				}
			}, key, username, pswd).doExecute();

			/**
			 * 设置密码 的网络请求
			 */
			// new SetPswdTask(mContext, new TaskCallback<Void>() {
			//
			// @Override
			// public void onTaskSuccess(Void data) {
			//
			// new LoginTask(mContext, new TaskCallback<String>() {
			//
			// @Override
			// public void onTaskSuccess(String data) {
			// if (proDialog != null && proDialog.isShowing()) {
			// proDialog.dismiss();
			// }
			// // 弹栈，返回原始页
			// Utils.onComplete(CodeActivity.this,true);
			// }
			//
			// @Override
			// public void onTaskError(int errorCode, String msg) {
			// if (proDialog != null && proDialog.isShowing()) {
			// proDialog.dismiss();
			// }
			// Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
			// .show();
			//
			// }
			// }, username, pswd).doExecute();
			// proDialog.show();
			//
			// }
			//
			// @Override
			// public void onTaskError(int errorCode, String msg) {
			// Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
			// .show();
			//
			// }
			// }, key, username, pswd).doExecute();

		}
	}

	private TextWatcher textWacher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (mCodeEdit != null && mPassword != null
					&& mPasswordConfirm != null) {

				mLoginBtn.setEnabled(true);

			} else {
				mLoginBtn.setEnabled(false);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	public void onGetCodeFail(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		mPresenter.getCodeFirst();
	}

	@Override
	public void onVerifyFail(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBindFail(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void calculate(int seconds) {
		if (seconds > 0) {
			mSendText.setText(seconds + this.getString(R.string.second));
			mSendText.setEnabled(false);
		} else {
			mSendText.setText(R.string.send_security_code);
			mSendText.setEnabled(true);
		}

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}
}
