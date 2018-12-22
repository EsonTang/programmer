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
import android.content.Intent;
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
import com.prize.cloud.task.CheckTask;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.ProDialog;

/**
 * 忘记密码页
 * @author yiyi
 * @version 1.0.0
 */
public class LostPswdActivity extends BaseActivity {
	private EditText mEditText;
	private TextView mNextBtn;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lost);
		findViewById();
		AppManager.getAppManager().addActivity(this);
		mNextBtn.setOnClickListener(btnOnClickListener);
		mNextBtn.setClickable(true);
		mNextBtn.setEnabled(false);
		mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		mEditText.addTextChangedListener(textWatcher);
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void findViewById() {
		mEditText = (EditText) findViewById(R.id.regist_password_edit_id);
		mNextBtn = (TextView) findViewById(R.id.regist_next_id);
		mTitle = (TextView) findViewById(R.id.title_id);
	}

	private TextWatcher textWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String text = s.toString();
			if (Utils.isEmail(text) || Utils.isPhone(text)) {
				mNextBtn.setEnabled(true);
				mNextBtn.setTextColor(getResources().getColor(R.color.login_color));
			} else {
				mNextBtn.setEnabled(false);
				mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
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
	
	private OnClickListener btnOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final String account = mEditText.getText().toString();
			if (TextUtils.isEmpty(account)) return;
			final ProDialog proDialog = new ProDialog(LostPswdActivity.this,
					ProgressDialog.THEME_HOLO_LIGHT,LostPswdActivity.this.getString(R.string.checking));
			new CheckTask(getApplicationContext(),
					new TaskCallback<Void>() {

						@Override
						public void onTaskSuccess(Void data) {
							if (proDialog != null && proDialog.isShowing()) {
								proDialog.dismiss();
							}
							toCode(account);
						}

						@Override
						public void onTaskError(int errorCode, String msg) {
							if (proDialog != null && proDialog.isShowing()) {
								proDialog.dismiss();
							}
							Toast.makeText(getApplicationContext(), msg,
									Toast.LENGTH_SHORT).show();

						}
					}, account, CodeType.TYPE_LOSTPSWD).doExecute();
			proDialog.show();
		}
	}; 
		
	/**
	 * 跳至验证码页
	 * @param account 账号
	 */
	private void toCode(String account) {
		Intent it = new Intent(this, CodeActivity.class);
		it.putExtra(CodeActivity.PARAM_TYPE, CodeType.TYPE_LOSTPSWD);
		it.putExtra(CodeActivity.PARAM_ACCOUNT, account);
		it.putExtra(CodeActivity.PARAM_TITLE,
				getString(R.string.retrieve_password));
		startActivity(it);
	}
}
