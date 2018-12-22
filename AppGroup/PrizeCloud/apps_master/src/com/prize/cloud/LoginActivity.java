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
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.cloud.app.AppManager;
import com.prize.cloud.bean.Person;
import com.prize.cloud.task.LoginTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.ProDialog;

/**
 *  登录页
 * @author yiyi
 * @version 1.0.0
 */
public class LoginActivity extends BaseActivity {
	public static final String HIDE_LOSTBTN = "hide_lostpswd_btn";
	private TextView mUserName;
	private TextView mPassWord;
	private ImageView mShowPassword;
	private TextView mLoginBtn;
	private TextView mAgree;
	private boolean mIsPassWordShow = false;
	private boolean isAgreed = true;

	private static final String LOGIN_ACTION = "com.prize.cloud.login";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		AppManager.getAppManager().addActivity(this);
		{
			View lostBtn = findViewById(R.id.lost_btn);
			boolean hide = getIntent().getBooleanExtra(HIDE_LOSTBTN, false);
			if (hide) {
				lostBtn.setVisibility(View.GONE);
			} else {
				lostBtn.setVisibility(View.VISIBLE);
				lostBtn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent it = new Intent(LoginActivity.this,LostPswdActivity.class);
						startActivity(it);
					}
				});
			}
		}
		mUserName = (TextView) findViewById(R.id.username_id);
		mPassWord = (TextView) findViewById(R.id.passwordedit_id);
		mShowPassword = (ImageView) findViewById(R.id.password_id);
		mLoginBtn = (TextView) findViewById(R.id.loginbtn_id);
		mAgree = (TextView)findViewById(R.id.agree_id); 
		
		mShowPassword.setOnClickListener(showPassWordClick);
		mLoginBtn.setOnClickListener(loginClikListener);		
		mUserName.addTextChangedListener(watcher);	
		mPassWord.addTextChangedListener(watcher);
		mLoginBtn.setEnabled(false);
		mLoginBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		setAgree();
	}
	
	/**
	 * 方法描述：设置酷比条款提示颜色，点击事件
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setAgree() {
		mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.agree, 0, 0, 0);
		SpannableStringBuilder builder = new SpannableStringBuilder(mAgree.getText().toString());
		ForegroundColorSpan prize_default = new ForegroundColorSpan(getResources().getColor(R.color.prize_text_default));  
		builder.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				if(isAgreed)
					mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.register_not_agree, 0, 0, 0);
				else
					mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.agree, 0, 0, 0);
				isAgreed = !isAgreed;
				enableNext();
			}
		}, 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
		builder.setSpan(prize_default, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {

				Intent intent = new Intent(LoginActivity.this,
						WebviewActivity.class);
				intent.putExtra(WebviewActivity.EXTRA_URL,
						"file:///android_asset/html/useragreement.html");
				intent.putExtra(WebviewActivity.EXTRA_TITLE,
						LoginActivity.this.getString(R.string.user_agreement));
				startActivity(intent);				
			}
			
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				 ds.setUnderlineText(false);
			}
		}, 7, 16, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
		mAgree.setHighlightColor(Color.TRANSPARENT); 
		mAgree.append(builder);
        mAgree.setMovementMethod(LinkMovementMethod.getInstance());
		mAgree.setText(builder); 
	}
	
	public OnClickListener loginClikListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String username = mUserName.getText().toString();
			String password = mPassWord.getText().toString();
			if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
				return;
			final ProDialog proDialog = new ProDialog(LoginActivity.this,
					ProgressDialog.THEME_HOLO_LIGHT,LoginActivity.this.getString(R.string.loging));
			new LoginTask(getApplicationContext(), new TaskCallback<String>() {
				
				@Override
				public void onTaskSuccess(String data) {
					Person person = Utils.getPersonalInfo(LoginActivity.this);
					if (proDialog != null && proDialog.isShowing()) {
						proDialog.dismiss();
					}
					if (person == null) {
						Toast.makeText(getApplicationContext(), LoginActivity.this.getString(R.string.login_failure), Toast.LENGTH_SHORT);
						Utils.logout(LoginActivity.this);
						return;
					}
					Intent broadIntent = new Intent(LOGIN_ACTION);
					sendBroadcast(broadIntent);
					if (getIntent() != null) {
						Bundle bundle = getIntent().getBundleExtra("otherApp");
						if (bundle != null) {
							String clsName = bundle.getString("clsName");
							String pkgName = bundle.getString("pkgName");
							if (!TextUtils.isEmpty(clsName) && !TextUtils.isEmpty(pkgName)) {
								AppManager.getAppManager().finishAllActivity();
							}
						}
					}
					boolean booting = Utils.isBootActivate(getApplicationContext());
					if (!booting) {
						Intent it = new Intent(LoginActivity.this,LogonActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(it);
					}else{
						Intent it = new Intent();
						ComponentName comp = new ComponentName("com.prize.boot",
								"com.prize.boot.OtherSetActivity");
						it.setComponent(comp);
						it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(it);
						Toast.makeText(getApplicationContext(), LoginActivity.this.getString(R.string.login_successful), 
								Toast.LENGTH_SHORT).show();
					}
					finish();
				}
				
				@Override
				public void onTaskError(int errorCode, String msg) {
					if (proDialog != null && proDialog.isShowing()) {
						proDialog.dismiss();
					}
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();;
					
				}
			}, username, password).doExecute();	
			proDialog.show();
		}
	};
	
	/**
	 * 是否显示密码
	 */
	public OnClickListener showPassWordClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!mIsPassWordShow) {
				mIsPassWordShow = true;
				mShowPassword.setBackgroundResource(R.drawable.hide_password_selector);
				mPassWord.setTransformationMethod(HideReturnsTransformationMethod
			            .getInstance());
			} else {
				mIsPassWordShow = false;
				mShowPassword.setBackgroundResource(R.drawable.show_password_selector);
				mPassWord.setTransformationMethod(PasswordTransformationMethod
			            .getInstance());
			}
			CharSequence text = mPassWord.getText();
			if (text instanceof Spannable) {
				Spannable spanText = (Spannable) text;
				Selection.setSelection(spanText, text.length());// 将光标移动到最后
			}		
		}
	};
	
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

	/**
	 * 登录按钮是否可用
	 */
	private void enableNext() {
		String text = mPassWord.getText().toString();
		if (isAgreed && text !=null && text.length()>=8 && text.length()<=16) {
			if(mUserName != null && mUserName.getText().toString().length() > 0){
				mLoginBtn.setEnabled(true);
				mLoginBtn.setTextColor(getResources().getColor(R.color.login_color));
			}else{
				mLoginBtn.setEnabled(false);
				mLoginBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
			}

		} else {
			mLoginBtn.setEnabled(false);
			mLoginBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		}
	}
}
