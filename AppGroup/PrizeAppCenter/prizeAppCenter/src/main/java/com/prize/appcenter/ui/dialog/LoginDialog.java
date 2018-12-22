package com.prize.appcenter.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;

public class LoginDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Activity mContext;

	public LoginDialog(Activity context, int theme) {
		super(context, theme);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.setGravity(Gravity.CENTER);
		setContentView(R.layout.login);
		findViewById(R.id.cancel).setOnClickListener(this);
		findViewById(R.id.login).setOnClickListener(this);
	}

	/**
	 * 方法描述：跳转到云账号登录页面
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void jumpToLoginActivity() {
		UIUtils.gotoActivity(LoginActivityNew.class, mContext);
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.login:
			jumpToLoginActivity();
			this.dismiss();
			break;
		default:
			this.dismiss();
			break;
		}
	}

}
