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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.cloud.R;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.util.Utils;
import com.prize.cloud.vp.CodePresenter;
import com.prize.cloud.vp.ICodeView;

/**
 * 绑定邮箱页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class BindActivity extends BaseActivity implements ICodeView {
	private TextView mBindBtn;
	private EditText mEmailEdit;
	private EditText mCodeEdit;
	private TextView mSendText;
	private TextView mTitle;

	private CodePresenter mPresenter;
	private String account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_email);
		findViewById();
		mEmailEdit.addTextChangedListener(emailWatcher);
		mCodeEdit.addTextChangedListener(watcher);

		mBindBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hideSoftInput();
				if (TextUtils.isEmpty(account) || !Utils.isEmail(account)) {
					Toast.makeText(getApplicationContext(),
							R.string.pl_enter_correct_email, Toast.LENGTH_SHORT)
							.show();
					return;

				}
				String checkcode = mCodeEdit.getText().toString();
				mPresenter.veriFy(account, checkcode);
			}
		});
		mBindBtn.setEnabled(false);
		mBindBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		mPresenter = new CodePresenter(CodeType.TYPE_BIND, this, this);
		mTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void findViewById() {
		mBindBtn = (TextView) findViewById(R.id.bindbtn_id);
		mEmailEdit = (EditText) findViewById(R.id.email_edit_id);
		mCodeEdit = (EditText) findViewById(R.id.email_code_edit);
		mSendText = (TextView) findViewById(R.id.sendtv_code);
		mTitle = (TextView) findViewById(R.id.title_id);
		mSendText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPresenter.getCode(account);
			}
		});
	}

	private TextWatcher emailWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String email = mEmailEdit.getText().toString();
			account = email;
			if (!TextUtils.isEmpty(account) && Utils.isEmail(account)) {
				mSendText.setText(R.string.send_security_code);
				mSendText.setClickable(true);
				mSendText
						.setBackgroundResource(R.drawable.cloud_send_security_code);
			} else {
				mSendText.setClickable(false);
				mSendText
						.setBackgroundResource(R.drawable.cloud_send_security_code_disable);
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
	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			String email = mEmailEdit.getText().toString();
			account = email;
			String code = mCodeEdit.getText().toString();
			if (Utils.isEmail(email) && !TextUtils.isEmpty(code)) {
				mBindBtn.setEnabled(true);
				mBindBtn.setClickable(true);
				mBindBtn.setTextColor(getResources().getColor(R.color.login_color));
			} else {
				mBindBtn.setEnabled(false);
				mBindBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
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

	@Override
	public void onGetCodeFail(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
			mSendText.setText(seconds + "秒");
			mSendText.setClickable(false);
			mSendText
					.setBackgroundResource(R.drawable.cloud_send_security_code_disable);
		} else {
			mSendText.setText(R.string.send_security_code);
			mSendText.setClickable(true);
			mSendText.setBackgroundResource(R.drawable.cloud_send_security_code);
		}

	}

}
