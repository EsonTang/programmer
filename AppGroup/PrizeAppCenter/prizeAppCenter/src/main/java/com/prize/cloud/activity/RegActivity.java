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

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.BaseApplication;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.cloud.task.CheckTask;
import com.prize.cloud.task.CodeType;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.ProDialog;

/**
 * 注册开始页，输入手机号后跳转至下一页
 * 
 * @author huangchangguo
 * @version 1.0.0
 */
public class RegActivity extends BaseActivity {
	private EditText mRegistEdit;
	private TextView mNextBtn;
	private TextView mOtherBtn;
	private TextView mAgree;
	private ImageView mTitleBack;

	private boolean isAgreed = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist_other);
		WindowMangerUtils.changeStatus(getWindow());
		AppManager.getAppManager().addActivity(this);

		initView();
	}

	private void initView() {

		mNextBtn = (TextView) findViewById(R.id.regist_next_id);
		mRegistEdit = (EditText) findViewById(R.id.phone_edit_id);
		mTitleBack = (ImageView) findViewById(R.id.actionbar_title_back);

		mRegistEdit.addTextChangedListener(watcher);
		mRegistEdit.setHint(this.getString(R.string.please_input_phone));
		mOtherBtn = (TextView) findViewById(R.id.regist_other_phone_id);
		mAgree = (TextView) findViewById(R.id.regist_agree_id);

		mRegistEdit.setFocusable(true);
		mRegistEdit.setFocusableInTouchMode(true);
		mRegistEdit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) mRegistEdit
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mRegistEdit, 0);
			}
		}, 300);
		// String tel = Utils.getTel(this);
		// if (!TextUtils.isEmpty(tel)) {
		// mOtherBtn.setText(R.string.use_own_phone_two);
		// mOtherBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(RegActivity.this,
		// OwnRegActivity.class));
		// finish();
		// }
		// });
		// } else {
		mOtherBtn.setVisibility(View.GONE);
		// }
		mNextBtn.setOnClickListener(clickListener);
		mNextBtn.setEnabled(false);
		setAgree();
		mTitleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 方法描述：设置酷比条款字体颜色和点击事件
	 * 
	 * @return void
	 */
	private void setAgree() {
		mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cloud_agree,
				0, 0, 0);
		SpannableStringBuilder builder = new SpannableStringBuilder(mAgree
				.getText().toString());
		ForegroundColorSpan prize_default = new ForegroundColorSpan(
				getResources().getColor(R.color.text_color_12b7f5));
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
		builder.setSpan(prize_default, 1, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(RegActivity.this,
						WebviewActivity.class);
				if (BaseApplication.isThird || BaseApplication.isCoosea) {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/useragreement.html");
				} else {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/koobeeuseragreement.html");
				}
				intent.putExtra(WebviewActivity.EXTRA_TITLE,
						RegActivity.this.getString(R.string.user_agreement));
				startActivity(intent);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(getResources().getColor(
						R.color.text_color_12b7f5));
				ds.setUnderlineText(false);
			}
		}, 4, 14, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		mAgree.setHighlightColor(Color.TRANSPARENT);
		mAgree.append(builder);
		mAgree.setMovementMethod(LinkMovementMethod.getInstance());
		mAgree.setText(builder);
	}

	/**
	 * 跳至验证页处理页
	 * 
	 * @param account账号
	 */
	private void toCode(String account) {
		Intent it = new Intent(this, CodeActivity.class);
		it.putExtra(CodeActivity.PARAM_TYPE, CodeType.TYPE_REGISTER);
		it.putExtra(CodeActivity.PARAM_ACCOUNT, account);
		// it.putExtra(CodeActivity.PARAM_TITLE,
		// getString(R.string.regist_koobee));
		startActivity(it);
	}

	/**
	 * 下一步按钮是否可用
	 */
	private void enableNext() {
		String text = mRegistEdit.getText().toString();
		// if (isAgreed && Utils.isPhone(text) || Utils.isEmail(text)) {
		if (isAgreed && Utils.isPhone(text)) {

			mNextBtn.setEnabled(true);
			mNextBtn.setClickable(true);

		} else {
			mNextBtn.setEnabled(false);

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

		}

		@Override
		public void afterTextChanged(Editable s) {

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
					ProgressDialog.THEME_HOLO_LIGHT,
					RegActivity.this.getString(R.string.checking));
			new CheckTask(getApplicationContext(), new TaskCallback<Void>() {

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
		// onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}
}
