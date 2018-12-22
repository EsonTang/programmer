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
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.vp.CodePresenter;
import com.prize.cloud.vp.ICodeView;

/**
 * 验证码收发页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class CodeActivity extends BaseActivity implements ICodeView {
	public static final String PARAM_ACCOUNT = "account_name";
	public static final String PARAM_TYPE = "code_type";
	public static final String PARAM_TITLE = "title";

	private TextView mTipText;
	private TextView mSendText;
	private TextView mLoginBtn;
	private EditText mCodeEdit;
	private TextView mTitleText;

	private CodePresenter mPresenter;
	private String account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_identification);
		findViewById();
		AppManager.getAppManager().addActivity(this);
		account = getIntent().getStringExtra(PARAM_ACCOUNT);
		int type = getIntent().getIntExtra(PARAM_TYPE, CodeType.TYPE_REGISTER);
		mPresenter = new CodePresenter(type, this, this);
		if (TextUtils.isEmpty(account)) {
			finish();
			return;
		}

		String title = getIntent().getStringExtra(PARAM_TITLE);
		if (!TextUtils.isEmpty(title))
			mTitleText.setText(title);
		mTipText.setText(String
				.format(getString(R.string.code_sended), account));
		 boolean isEmail = Utils.isEmail(account);
		 if (isEmail) {
			 mTipText.setText(String.format(getString(R.string.use_safe_email),
			 account));
		 } else {
			 mTipText.setText(String.format(getString(R.string.use_safe_phone),
			 account));
		 }
		mCodeEdit.addTextChangedListener(textWacher);
		mTitleText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void findViewById() {
		mTipText = (TextView) findViewById(R.id.tip_id);
		mSendText = (TextView) findViewById(R.id.sendtv_code);
		mLoginBtn = (TextView) findViewById(R.id.loginbtn_id);
		mCodeEdit = (EditText) findViewById(R.id.msg_code_edit);
		mTitleText = (TextView) findViewById(R.id.title_id);
		mSendText.setOnClickListener(clickListener);
		mLoginBtn.setOnClickListener(verifyClickListener);
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mPresenter.getCode(account);
		}
	};
	
	private OnClickListener verifyClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			hideSoftInput();
			String code = mCodeEdit.getText().toString();
			if (TextUtils.isEmpty(code)) {
				Toast.makeText(getApplicationContext(), CodeActivity.this.getString(R.string.please_input_code),
						Toast.LENGTH_SHORT).show();
				return;
			}
			mPresenter.veriFy(account, code);
		}
	};
	
	private TextWatcher textWacher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {	
			if (TextUtils.isEmpty(s)) {
				mLoginBtn.setEnabled(false);
			    mLoginBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
			} else {
				mLoginBtn.setEnabled(true);
				mLoginBtn.setTextColor(getResources().getColor(R.color.login_color));
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
	
	/*public void sendClk(View v) {
		
	}*/

	private void verifyClk(View v) {
		hideSoftInput();
		String code = mCodeEdit.getText().toString();
		if (TextUtils.isEmpty(code)) {
			Toast.makeText(getApplicationContext(), this.getString(R.string.please_input_code),
					Toast.LENGTH_SHORT).show();
			return;
		}
		mPresenter.veriFy(account, code);
	}

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
			mSendText.setClickable(false);
			mSendText.setBackgroundResource(R.drawable.cloud_send_security_code_disable);
		} else {
			mSendText.setText(R.string.send_security_code);
			mSendText.setClickable(true);
			mSendText.setBackgroundResource(R.drawable.cloud_send_security_code);
		}

	}

}
