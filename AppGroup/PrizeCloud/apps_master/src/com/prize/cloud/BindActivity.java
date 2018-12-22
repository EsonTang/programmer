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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
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
	@ViewInject(R.id.bindbtn_id)
	private TextView mBindBtn;
	@ViewInject(R.id.email_edit_id)
	private EditText mEmailEdit;
	@ViewInject(R.id.email_code_edit)
	private EditText mCodeEdit;
	@ViewInject(R.id.sendtv_code)
	private TextView mSendText;

	private CodePresenter mPresenter;
	private String account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_email);
		ViewUtils.inject(this);
		mEmailEdit.addTextChangedListener(watcher);
		mCodeEdit.addTextChangedListener(watcher);

		mBindBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String checkcode = mCodeEdit.getText().toString();
				mPresenter.veriFy(account, checkcode);
			}
		});
		mBindBtn.setEnabled(false);
		mBindBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		mPresenter = new CodePresenter(CodeType.TYPE_BIND, this, this);
	}

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

	@OnClick(R.id.sendtv_code)
	public void sendClk(View v) {
		mPresenter.getCode(account);
	}

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
					.setBackgroundResource(R.drawable.send_security_code_disable);
		} else {
			mSendText.setText(R.string.send_security_code);
			mSendText.setClickable(true);
			mSendText.setBackgroundResource(R.drawable.send_security_code);
		}

	}

}
