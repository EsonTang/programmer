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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.constants.Constants;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.cloud.task.LoginTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.ProDialog;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 1.9版本登录&注册页
 * 
 * @author huangchangguo
 * @version 1.9 2016/07/7 14:06:11
 */
public class LoginActivityNew extends BaseActivity {
	private String TAG = "LoginActivityNew";
	public static final String HIDE_LOSTBTN = "hide_lostpswd_btn";
	public static final int LOGINSUCESS = 0;
	private EditText mUserName;
	private EditText mPassWord;
	private ImageView mShowPassword;
	private TextView mLoginBtn;
	// private TextView mAgree;
	private boolean mIsPassWordShow = false;
	private boolean isAgreed = true;
	private TextView mTitle;
	private ImageView mTitleBack;
	private TextView mFastRegistration;
	private TextView mLostBtn;

	private static final String LOGIN_ACTION = "com.prize.cloud.login";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * 判断账号是否已经登录
		 */
		/*
		 * if (Utils.curAccount(this) != null && Utils.getPersonalInfo(this) !=
		 * null) { startActivity(new Intent(this, PersonActivity.class));
		 * finish(); return; }
		 */
		setContentView(R.layout.cloud_login_new);
		WindowMangerUtils.changeStatus(getWindow());
		AppManager.getAppManager().addActivity(this);
		// ViewUtils.inject(this);

		mContext = getApplicationContext();
		initView();
		initData();
		initClick();

	}

	Handler handler = new MyHander(this);
//	{
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case LOGINSUCESS:
//				//发送本地广播
//				LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.ACTION_LOGIN_SUCCESS));
//
//				// 保存电话号码并退出
//				PreferencesUtils.putString(mContext, "phone", mUserName
//						.getText().toString());
//				AppManager.getAppManager().finishAllActivity();
//				break;
//
//			default:
//				break;
//			}
//
//			super.handleMessage(msg);
//		}
//
//	};

	private void initView() {

		mLostBtn = (TextView) findViewById(R.id.lost_btn);

		mUserName = (EditText) findViewById(R.id.username_id);
		mPassWord = (EditText) findViewById(R.id.passwordedit_id);
		// mShowPassword = (ImageView) findViewById(R.id.password_id);
		mLoginBtn = (TextView) findViewById(R.id.loginbtn_id);

		mFastRegistration = (TextView) findViewById(R.id.fast_registration_btn_id);

		mTitle = (TextView) findViewById(R.id.title_id);
		mTitleBack = (ImageView) findViewById(R.id.actionbar_title_back);

	}

	private void initData() {

		mUserName.setFocusable(true);
		mUserName.setFocusableInTouchMode(true);
		mUserName.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) mUserName
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mUserName, 0);
			}
		}, 300);

		// 默认显示手机号码
		String defphone = PreferencesUtils.getString(mContext, "phone", "");

		if (!defphone.equals("")) {
			mUserName.setText(defphone);
			mUserName.setSelection(defphone.length());
		}

	}

	private void initClick() {

		// mShowPassword.setOnClickListener(showPassWordClick);
		mLoginBtn.setOnClickListener(loginClikListener);
		mFastRegistration.setOnClickListener(fastRegistrationClikListener);
		mUserName.addTextChangedListener(watcher);

		mPassWord.addTextChangedListener(watcher);

		mLoginBtn.setEnabled(false);
		boolean hide = getIntent().getBooleanExtra(HIDE_LOSTBTN, false);
		if (hide) {
			mLostBtn.setVisibility(View.GONE);
		} else {
			mLostBtn.setVisibility(View.VISIBLE);

			mLostBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent it = new Intent(LoginActivityNew.this,
							ForgotPasswordActivity.class);
					startActivity(it);
				}
			});
		}
		/*
		 * 设置返回
		 */
		mTitleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private static class MyHander extends Handler {
		private WeakReference<LoginActivityNew> mActivities;

		MyHander(LoginActivityNew mActivity) {
			this.mActivities = new WeakReference<LoginActivityNew>(mActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mActivities == null || mActivities.get() == null) return;
			final LoginActivityNew activity = mActivities.get();
			if (activity != null) {
				switch (msg.what) {
					case LOGINSUCESS:
						//发送本地广播
						LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(Constants.ACTION_LOGIN_SUCCESS));
						// 保存电话号码并退出
						PreferencesUtils.putString(activity, "phone", activity.mUserName
								.getText().toString());
						AppManager.getAppManager().finishAllActivity();
						break;
					default:
						break;
				}

			}
		}
	}


	/**
	 * 跳转到快速注册界面
	 */
	public OnClickListener fastRegistrationClikListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			// if (!Utils.getSimState(LoginActivityNew.this))
			startActivity(new Intent(LoginActivityNew.this, RegActivity.class));
			// else
			// startActivity(new Intent(LoginActivityNew.this,
			// OwnRegActivity.class));

		}

	};

	/**
	 * 点击登录
	 */
	public OnClickListener loginClikListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			hideSoftInput();
			String username = mUserName.getText().toString();
			String password = mPassWord.getText().toString();

			boolean fastDoubleClick = Utils.isFastDoubleClick();
			if (fastDoubleClick) {
				return;
			} else if (password.length() == 0) {
				ToastUtils.showToast(mContext.getResources().getString(
						R.string.null_password));
				/*
				 * Toast.makeText( mContext, mContext.getResources().getString(
				 * R.string.null_password), 0).show();
				 */
				return;
			} else if (password.length() < 6) {
				ToastUtils.showToast(mContext.getResources().getString(
						R.string.not_enough_password));
				/*
				 * Toast.makeText( mContext, mContext.getResources().getString(
				 * R.string.not_enough_password), 0).show();
				 */
				return;
			}

			final ProDialog proDialog = new ProDialog(LoginActivityNew.this,
					ProgressDialog.THEME_HOLO_LIGHT, mContext.getResources()
							.getString(R.string.loging));
			new LoginTask(mContext, new TaskCallback<String>() {

				@Override
				public void onTaskSuccess(String data) {
					// Person person =
					// Utils.getPersonalInfo(LoginActivity.this);
					if (proDialog != null && proDialog.isShowing()) {
						proDialog.dismiss();
					}
					// if (person != null) {
					/*
					 * Intent it = new Intent(LoginActivityNew.this,
					 * PersonActivity.class);
					 * it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 * startActivity(it);
					 */
					// 保存电话号码
					// TODO
					Message msg = Message.obtain();
					msg.what = LOGINSUCESS;
					handler.sendMessage(msg);

					// } else {
					// Utils.logout(LoginActivity.this);
					// }
				}

				@Override
				public void onTaskError(int errorCode, String msg) {
					if (proDialog != null && proDialog.isShowing()) {
						proDialog.dismiss();
					}
					Toast.makeText(getApplicationContext(), msg,
							Toast.LENGTH_SHORT).show();
				}
			}, username, password).doExecute();
			proDialog.show();
		}

	};

//	/**
//	 * 是否显示密码
//	 */
//	public OnClickListener showPassWordClick = new OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			if (!mIsPassWordShow) {
//				mIsPassWordShow = true;
//				mShowPassword
//						.setBackgroundResource(R.drawable.cloud_hide_password_selector);
//				mPassWord
//						.setTransformationMethod(HideReturnsTransformationMethod
//								.getInstance());
//			} else {
//				mIsPassWordShow = false;
//				mShowPassword
//						.setBackgroundResource(R.drawable.cloud_show_password_selector);
//				mPassWord.setTransformationMethod(PasswordTransformationMethod
//						.getInstance());
//			}
//			CharSequence text = mPassWord.getText();
//			if (text instanceof Spannable) {
//				Spannable spanText = (Spannable) text;
//				Selection.setSelection(spanText, text.length());// 将光标移动到最后
//			}
//		}
//	};

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			enableNext();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}
	};

	private Context mContext;

	/**
	 * 登录按钮是否可用
	 */
	private void enableNext() {
		String password = mPassWord.getText().toString();

		String phoneNum = mUserName.getText().toString();
		boolean isPhoneNum = Utils.isPhoneNum(phoneNum);
		if (isPhoneNum && password != null) {

			mLoginBtn.setEnabled(true);

		} else {
			mLoginBtn.setEnabled(false);

		}
	}

	public void onBackClk(View v) {
		// onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}
}
