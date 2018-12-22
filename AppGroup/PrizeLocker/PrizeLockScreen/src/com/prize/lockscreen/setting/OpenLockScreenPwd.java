package com.prize.lockscreen.setting;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;

public class OpenLockScreenPwd extends Activity {

	public static final int CONFIRM_CODE = 12345;
	
	private LinearLayout updateLayout;
	// 关闭数字密码
	private TextView closePwdStyleText;
	// 数字密码
	private TextView openNumberPwdStyleText;
	// 图案密码
	private TextView openIconPwdStyleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}
		
		setContentView(R.layout.open_lockscreen_pwd);
		WindowManager.LayoutParams lp= getWindow().getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        getWindow().setAttributes(lp);
        
		init();
		if (isNeedConfirmLockPassword()) {
			showLockPasswordSetting(1,
					SharedPreferencesTool.getLockPwdType(this));
		}
	}

	/***
	 * 初始化
	 */
	private void init() {
		updateLayout = (LinearLayout) findViewById(R.id.update_lockscreen_pwd_layout);
		closePwdStyleText = (TextView) findViewById(R.id.close_lockscreen_pwd_txt);
		openNumberPwdStyleText = (TextView) findViewById(R.id.open_lockscreen_pwd_number_txt);
		openIconPwdStyleText = (TextView) findViewById(R.id.open_lockscreen_pwd_icon_txt);

		closePwdStyleText.setOnClickListener(onClickListener);
		openNumberPwdStyleText.setOnClickListener(onClickListener);
		openIconPwdStyleText.setOnClickListener(onClickListener);
		
		findViewById(R.id.open_complex_pwd_txt).setOnClickListener(onClickListener);
		
		TextView tv = (TextView)findViewById(R.id.title);
		tv.setText(R.string.lock_pwd);
		
		findViewById(R.id.back).setOnClickListener(onClickListener);
	}

	View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.close_lockscreen_pwd_txt:
				closeLockScreenPwd();
				break;
			case R.id.open_lockscreen_pwd_number_txt:
				showLockPasswordSetting(0,
						SharedPreferencesTool.LOCK_STYLE_NUMBER_PASSWORD);
				break;
			case R.id.open_lockscreen_pwd_icon_txt:
				showLockPasswordSetting(0,
						SharedPreferencesTool.LOCK_STYLE_PATTERN_PASSWORD);
				break;
			case R.id.open_complex_pwd_txt:
				showLockPasswordSetting(0,
						SharedPreferencesTool.LOCK_STYLE_COMPLEX_PASSWORD);
				break;
			case R.id.back:
				finish();
				break;
			}
		}
	};

	private boolean isNeedConfirmLockPassword() {
		return SharedPreferencesTool.getLockPwdType(this) != SharedPreferencesTool.LOCK_STYLE_NO_PASSWORD;
	}

	/***
	 * 1表示确认密码，0表示创建密码
	 * @param isConfirm 1表示确认密码，0表示创建密码
	 * @param style 1表示数字密码，2表示图案密码, 3表示复杂密码
	 */
	private void showLockPasswordSetting(int isConfirm, int style) {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName(this, LockPasswordSetting.class);
		intent.setComponent(cn);
		intent.putExtra("isConfirm", isConfirm); // 1表示确认密码，0表示创建密码, 2表示要输入解锁密码
		intent.putExtra("style", style); // 1表示数字密码，2表示图案密码, 3表示复杂密码
		startActivityForResult(intent, CONFIRM_CODE);
	}

	private void closeLockScreenPwd() {
		SharedPreferencesTool.setLockPwdType(OpenLockScreenPwd.this,
				SharedPreferencesTool.LOCK_STYLE_NO_PASSWORD);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CONFIRM_CODE) {
			if (resultCode != Activity.RESULT_OK) {
				this.finish();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// int style = SharedPreferencesTool.getLockPwdType(this);
		// refreshLayout(style);
	}

	private void refreshLayout(int style) {
//		if (style == 0) {
//			updateLayout.setVisibility(View.GONE);
//		} else {
			updateLayout.setVisibility(View.VISIBLE);
//		}
	}

}
