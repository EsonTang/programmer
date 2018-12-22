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

import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.SetPswdTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.vp.CodePresenter;
import com.prize.cloud.vp.ICodeView;
import com.prize.cloud.widgets.ProDialog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 快速 找回 密码
 * 
 * @author huangchangguo
 * @version 1.9
 * 
 */
public class ForgotPasswordActivity extends BaseActivity implements ICodeView {
	private EditText mMsgCodes;
	private EditText mPhoneNum;
	private EditText mPassword;
	private TextView mSubmit;
	private TextView mSendCodes;
	private TextView mTitleName;
	private ImageView mTitleBack;
	private CodePresenter mPresenter;
	private Context mContext;

	private String phoneNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password);
		WindowMangerUtils.changeStatus(getWindow());
		mContext = getApplicationContext();
		AppManager.getAppManager().addActivity(this);
		findViewById();
		initListener();

		// 短信验证码
		int type = CodeType.TYPE_LOSTPSWD;
		// 验证码读秒与发送 操作类
		mPresenter = new CodePresenter(type, this, this);

	}

	private void findViewById() {

		mPhoneNum = (EditText) findViewById(R.id.forgot_phone_number_et);
		mMsgCodes = (EditText) findViewById(R.id.forgot_msg_code_et);
		mPassword = (EditText) findViewById(R.id.forgot_password_edit_id);
		mSubmit = (TextView) findViewById(R.id.forgot_submit_btn_id);
		mSendCodes = (TextView) findViewById(R.id.forgot_cloud_send_codes);
		mTitleName = (TextView) findViewById(R.id.title_id);
		mTitleBack = (ImageView) findViewById(R.id.actionbar_title_back);

		mTitleName.setText(R.string.retrieve_password);

	}

	private void initListener() {
		// 默认呼出小键盘
		mPhoneNum.setFocusable(true);
		mPhoneNum.setFocusableInTouchMode(true);
		mPhoneNum.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) mPhoneNum
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mPhoneNum, 0);
			}
		}, 300);

		// 提交
		mSubmit.setOnClickListener(mSubmitListener);
		// mSubmit.setClickable(true);
		mSubmit.setEnabled(false);

		mSendCodes.setOnClickListener(sendCodesListener);

		// 对三个输入框添加监听
		mMsgCodes.addTextChangedListener(textWatcher);
		mPassword.addTextChangedListener(textWatcher);
		mPhoneNum.addTextChangedListener(textWatcher);
		/**
		 * 设置返回
		 */
		mTitleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	/**
	 * 短信验证码监听
	 * 
	 * 1：首先判断手机号码合法性 2：发送短信验证码
	 * 
	 */
	private OnClickListener sendCodesListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mPhoneNum == null) {
				ToastUtils.showToast(mContext.getResources().getString(
						R.string.input_null_phonenum));
				return;
			}

			phoneNum = mPhoneNum.getText().toString();
			/**
			 * 检查phoneNum的合法性
			 * 
			 */
			if (Utils.isPhoneNum(phoneNum)) {

				// 成功则发送验证码
				mPresenter.getCode(phoneNum);

			} else {

				ToastUtils.showToast(mContext.getResources().getString(
						R.string.please_input_current_phone));

			}

		}
	};

	private TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			String phoneNum = mPhoneNum.getText().toString();
			String msgCode = mMsgCodes.getText().toString();
			String password = mPassword.getText().toString();
			// if (Utils.isEmail(text) || Utils.isPhone(text)) {
			if (Utils.isPhone(phoneNum) && msgCode.length() >= 6
					&& password.length() >= 6) {

				mSubmit.setEnabled(true);

			} else {

				mSubmit.setEnabled(false);

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

	private OnClickListener mSubmitListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 获得已经检查过的电话号码
			String phoneNum = mPhoneNum.getText().toString();

			String msgCode = mMsgCodes.getText().toString();
			hideSoftInput();
			String passWord = mPassword.getText().toString();
			if (TextUtils.isEmpty(msgCode)) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.please_input_code), Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (TextUtils.isEmpty(passWord)) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.null_password), Toast.LENGTH_SHORT)
						.show();
				return;
			} else if (passWord.length() < 6) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.not_enough_password),
						Toast.LENGTH_SHORT).show();
				return;
			}

			doRegister(msgCode, phoneNum, passWord);
		}

	};

	/**
	 * 设置完密码后提交修改请求
	 * 
	 * @param msgCode
	 *            验证码
	 * @param phoneNum
	 *            账号
	 * @param pswd
	 *            密码
	 */
	private void doRegister(String msgCode, String phoneNum, String pswd) {

		final ProDialog proDialog = new ProDialog(ForgotPasswordActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT, mContext.getResources()
						.getString(R.string.setpswding));

		new SetPswdTask(mContext, new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				Toast.makeText(mContext,
						mContext.getString(R.string.set_password_sucess),
						Toast.LENGTH_SHORT).show();

//				Utils.onComplete(ForgotPasswordActivity.this, false);
				finish();

			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();

			}

		}, msgCode, phoneNum, pswd).doExecute();
		proDialog.show();

	}

	// ---------------------验证码相关的回调---------------------------//
	@Override
	public void calculate(int seconds) {
		if (seconds > 0) {
			mSendCodes.setText(seconds + this.getString(R.string.second));
			// mSendCodes.setClickable(false);
			// mSendCodes
			// .setBackgroundResource(R.drawable.cloud_codes_security_disabled);
			mSendCodes.setEnabled(false);
			mPhoneNum.setEnabled(false);
			mPhoneNum.setTextColor(mContext.getResources().getColor(
					R.color.prize_text_default));

		} else {

			mSendCodes.setText(mContext.getResources().getString(
					R.string.to_get_codes));
			// mSendCodes
			// .setBackgroundResource(R.drawable.cloud_codes_security_normal);
			// mSendCodes
			// .setBackgroundResource(R.drawable.cloud_codes_security_selector);
			// mSendCodes.setClickable(true);
			mSendCodes.setClickable(true);
			mSendCodes.setEnabled(true);
			mPhoneNum.setEnabled(true);
			mPhoneNum.setTextColor(mContext.getResources().getColor(
					R.color.text_color_6c6c6c));

		}
	}

	@Override
	public void onGetCodeFail(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		mPresenter.getCodeFirst();

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}
	@Override
	public void onVerifyFail(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBindFail(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}
}
