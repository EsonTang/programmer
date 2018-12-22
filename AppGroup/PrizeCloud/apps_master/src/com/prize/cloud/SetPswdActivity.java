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
package com.prize.cloud;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.LoginTask;
import com.prize.cloud.task.RegisterTask;
import com.prize.cloud.task.SetPswdTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.widgets.ProDialog;

/**
 * 密码设置页，包括注册的密码设置和密码重置功能
 * @author yiyi
 * @version 1.0.0
 */
/**
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class SetPswdActivity extends BaseActivity {

	@ViewInject(R.id.password_id)
	private EditText mPswdEdit;
	@ViewInject(R.id.confirm_password_id)
	private EditText mConfirmEdit;
	@ViewInject(R.id.confirmtn_id)
	private TextView mCommitBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_password);
		ViewUtils.inject(this);
		final int type = getIntent()
				.getIntExtra("type", CodeType.TYPE_REGISTER);
		final String key = getIntent().getStringExtra("key");
		final String username = getIntent().getStringExtra("username");
		mPswdEdit.addTextChangedListener(watcher);
		mConfirmEdit.addTextChangedListener(watcher);
		mCommitBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String pswd1 = mPswdEdit.getText().toString();
				String pswd2 = mConfirmEdit.getText().toString();
				if (pswd1.equals(pswd2)) {
					if (type == CodeType.TYPE_REGISTER) {
						doRegister(key, username, pswd1);
					} else if (type == CodeType.TYPE_LOSTPSWD) {
						doResetPswd(key, username, pswd1);
					}
				} else {
					Toast.makeText(getApplicationContext(), SetPswdActivity.this.getString(R.string.password_different),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		mCommitBtn.setEnabled(false);
		mCommitBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
	}

	/**
	 * 设置完密码后提交注册请求
	 * @param key 验证码验证成功后返回的key
	 * @param username 账号
	 * @param pswd1 密码
	 */
	private void doRegister(final String key, final String username,
			String pswd1) {
		final ProDialog proDialog = new ProDialog(SetPswdActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT,SetPswdActivity.this.getString(R.string.registing));
		new RegisterTask(getApplicationContext(), new TaskCallback<String>() {

			@Override
			public void onTaskSuccess(String data) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				Intent it = new Intent(SetPswdActivity.this,
						RegSuccessActivity.class);
				it.putExtra("phone", username);
				it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();

			}
		}, key, username, pswd1).doExecute();
		proDialog.show();
	}

	/**
	 * 重置密码，重置成功后跳至已登录页
	 * @param key 验证码验证成功后返回的key
	 * @param username 账号
	 * @param pswd1 密码
	 */
	private void doResetPswd(final String key, final String username,
			final String pswd1) {
		final ProDialog proDialog = new ProDialog(SetPswdActivity.this,
				ProgressDialog.THEME_HOLO_LIGHT,SetPswdActivity.this.getString(R.string.setpswding));
		new SetPswdTask(getApplicationContext(), new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				new LoginTask(getApplicationContext(),
						new TaskCallback<String>() {

							@Override
							public void onTaskSuccess(String data) {
								if (proDialog != null && proDialog.isShowing()) {
									proDialog.dismiss();
								}
								Intent it = new Intent(SetPswdActivity.this,
										LogonActivity.class);
								it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
										| Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(it);

							}

							@Override
							public void onTaskError(int errorCode, String msg) {
								if (proDialog != null && proDialog.isShowing()) {
									proDialog.dismiss();
								}
								Toast.makeText(getApplicationContext(), msg,
										Toast.LENGTH_SHORT).show();

							}
						}, username, pswd1).doExecute();
				proDialog.show();

			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();

			}
		}, key, username, pswd1).doExecute();
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (mPswdEdit.getText().length() >= 8
					&& mConfirmEdit.getText().length() >= 8) {
				mCommitBtn.setEnabled(true);
				mCommitBtn.setTextColor(getResources().getColor(R.color.login_color));
			} else {
				mCommitBtn.setEnabled(false);
				mCommitBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
			}

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

}
