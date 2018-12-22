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
/**/
package com.prize.cloud.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
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
 * 注册开始页，输入手机号后跳转至下一页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class RegActivity extends BaseActivity {
	private EditText mRegistEdit;
	private TextView mNextBtn;
	private TextView mOtherBtn;
	private TextView mAgree;
	private TextView mTitle;
	
	private boolean isAgreed = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist_other);
		AppManager.getAppManager().addActivity(this);
		mNextBtn = (TextView) findViewById(R.id.regist_next_id);
		mRegistEdit = (EditText) findViewById(R.id.passwordedit_id);
		mRegistEdit.addTextChangedListener(watcher);
		mRegistEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mRegistEdit.setHint(this.getString(R.string.please_input_phone));
		mRegistEdit.setInputType(InputType.TYPE_CLASS_TEXT);

		mOtherBtn = (TextView) findViewById(R.id.regist_other_phone_id);
		mAgree = (TextView) findViewById(R.id.regist_agree_id);

		String tel = Utils.getTel(this);
		if (!TextUtils.isEmpty(tel)) {
			mOtherBtn.setText(R.string.use_own_phone_two);
			mOtherBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(RegActivity.this,
							OwnRegActivity.class));
					finish();
				}
			});
		} else {
			mOtherBtn.setVisibility(View.GONE);
		}
		mNextBtn.setOnClickListener(clickListener);
		mNextBtn.setClickable(true);
		mNextBtn.setEnabled(false);
		mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		setAgree();
		mTitle = (TextView) findViewById(R.id.title_id);
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 方法描述：设置酷比条款字体颜色和点击事件
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setAgree() {
		mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cloud_agree, 0,
				0, 0);
		SpannableStringBuilder builder = new SpannableStringBuilder(mAgree
				.getText().toString());
		ForegroundColorSpan prize_default = new ForegroundColorSpan(
				getResources().getColor(R.color.prize_text_default));
		builder.setSpan(new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				if (isAgreed)
					mAgree.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.cloud_register_not_agree, 0, 0, 0);
				else
					mAgree.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.cloud_agree, 0, 0, 0);
				isAgreed = !isAgreed;
				enableNext();

			}
		}, 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(prize_default, 0, 7,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(RegActivity.this,
						WebviewActivity.class);
				if (AppManager.isThird) {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/useragreement.html");
				} else {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/koobeeuseragreement.html");
				}
				intent.putExtra(WebviewActivity.EXTRA_TITLE, RegActivity.this.getString(R.string.user_agreement));
				startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(false);
			}
		}, 5, 14, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		mAgree.setHighlightColor(Color.TRANSPARENT);
		mAgree.append(builder);
		mAgree.setMovementMethod(LinkMovementMethod.getInstance());
		mAgree.setText(builder);
	}
	/**
	 * 跳至验证页处理页
	 * @param account账号
	 */
	private void toCode(String account) {
		Intent it = new Intent(this, CodeActivity.class);
		it.putExtra(CodeActivity.PARAM_TYPE, CodeType.TYPE_REGISTER);
		it.putExtra(CodeActivity.PARAM_ACCOUNT, account);
		it.putExtra(CodeActivity.PARAM_TITLE, getString(R.string.regist_koobee));
		startActivity(it);
	}

	/**
	 * 下一步按钮是否可用
	 */
	private void enableNext() {
		String text = mRegistEdit.getText().toString();
		if (isAgreed && Utils.isPhone(text) || Utils.isEmail(text)) {
			mNextBtn.setBackgroundResource(R.drawable.cloud_next_selector);
			mNextBtn.setEnabled(true);
			mNextBtn.setTextColor(getResources().getColor(R.color.login_color));

		} else {
			mNextBtn.setEnabled(false);
			mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		}
	}

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

	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			hideSoftInput();
			final String account = mRegistEdit.getText().toString();
			if (TextUtils.isEmpty(account)) {
				return;
			}
			final ProDialog proDialog = new ProDialog(RegActivity.this,
					ProgressDialog.THEME_HOLO_LIGHT,RegActivity.this.getString(R.string.checking));
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
					}, account, CodeType.TYPE_REGISTER).doExecute();
			proDialog.show();
		}
	};
	
	public void onBackClk(View v) {
		onBackPressed();
	}
}
