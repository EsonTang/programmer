package com.prize.lockscreen.setting;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;
/***
 * 锁屏设置
 * @author fanjunchen
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	private RelativeLayout lockStyle;
	private RelativeLayout lockPwd;
	private TextView styleTitle;
	private TextView styleType;
	private TextView pwdTitle;
	private TextView pwdType;
	
	private Switch nfSwitch;

	@Override
	protected void onCreate(Bundle instance) {
		super.onCreate(instance);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}
		
		setContentView(R.layout.lock_setting);
		WindowManager.LayoutParams lp= getWindow().getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        getWindow().setAttributes(lp);
        
		init();
	}

	private void init() {
		
		Switch lockSwitch = (Switch) findViewById(R.id.lock_screen_sw);
		nfSwitch = (Switch) findViewById(R.id.notification_sw);
		lockStyle = (RelativeLayout) findViewById(R.id.lock_style_layout);
		lockPwd = (RelativeLayout) findViewById(R.id.lock_pwd_layout);
		styleTitle = (TextView) findViewById(R.id.lock_style_title);
		styleType = (TextView) findViewById(R.id.style_classical);
		pwdTitle = (TextView) findViewById(R.id.lock_pwd_title);
		pwdType = (TextView) findViewById(R.id.lock_pwd_type);

		lockSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						isLayoutOk(isChecked);
					}
				});

		lockStyle.setOnClickListener(this);
		lockPwd.setOnClickListener(this);
		
		boolean state = SharedPreferencesTool.isLockScreenEnable(this);
		lockSwitch.setChecked(state);
		isLayoutOk(state);
		
		TextView tv = (TextView)findViewById(R.id.title);
		tv.setText(R.string.lockscreen_setting_text);
		
		nfSwitch.setOnClickListener(this);
		/*nfSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				openNotificationAccess();
			}
		});*/
	}
	
	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
	/***
	 * 通知权限是否打开
	 * @return
	 */
	private boolean isAccessEnabled() {
		String pkgName = "com.prize.prizelockscreen";//getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/***
	 * 打开通知权限选择框
	 */
	private void openNotificationAccess() {
		startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
	}
	
	private void getPwdType() {
		int style = SharedPreferencesTool.getLockPwdType(this);
		switch (style) {
		case 0:
			pwdType.setText(getResources().getString(
					R.string.no_pwd_text));
			break;
		case 1:
			pwdType.setText(getResources().getString(
					R.string.lockscreen_style_number_text));
			break;
		case 2:
			pwdType.setText(getResources().getString(
					R.string.lockscreen_style_icon_text));
			break;
		case 3:
			pwdType.setText(getResources().getString(
					R.string.lockscreen_style_complex_text));
			break;
		}
	}
	/***
	 * 获取锁屏样式类型
	 */
	private void getStyleType() {
		int style = SharedPreferencesTool.getLockStyle(this);
		switch (style) {
			case LockConfigBean.DEFAULT_LOCK_TYPE:
				styleType.setText(getResources().getString(
						R.string.lock_style_default));
				break;
			case LockConfigBean.CIRCLE_LOCK_TYPE:
				styleType.setText(getResources().getString(
						R.string.lock_style_circle));
				break;
			case LockConfigBean.CLOSE_LOCK_TYPE:
				styleType.setText(getResources().getString(
						R.string.lock_style_blink));
				break;
			case LockConfigBean.COLOR_LOCK_TYPE:
				styleType.setText(getResources().getString(
						R.string.lock_style_fashion));
				break;
			case LockConfigBean.FLY_LOCK_TYPE:
				styleType.setText(getResources().getString(
						R.string.lock_style_fly));
				break;
		}
	}
	/***
	 * 改变布局
	 * @param isChecked
	 */
	protected void isLayoutOk(boolean isChecked) {
		if (isChecked) {
			lockStyle.setClickable(true);
			lockPwd.setClickable(true);
			styleTitle.setTextColor(getResources().getColor(
					R.color.item_text_color));
			pwdTitle.setTextColor(getResources().getColor(
					R.color.item_text_color));
			//styleClassical.setVisibility(View.VISIBLE);
			//noPwd.setVisibility(View.VISIBLE);
			SharedPreferencesTool.setLockScreenEnable(this, isChecked);
		} 
		else {
			lockStyle.setClickable(false);
			lockPwd.setClickable(false);
			styleTitle.setTextColor(getResources().getColor(
					R.color.item_text_light_color));
			pwdTitle.setTextColor(getResources().getColor(
					R.color.item_text_light_color));
			// styleClassical.setVisibility(View.INVISIBLE);
			// noPwd.setVisibility(View.INVISIBLE);
			SharedPreferencesTool.setLockScreenEnable(this, isChecked);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lock_style_layout:
			openLockStyle();
			break;
		case R.id.lock_pwd_layout:
			openOrUpdateLockScreenPwd();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.notification_sw:
			openNotificationAccess();
			break;
		default:
			break;
		}
	}
	/**
	 * 打开选择锁屏密码样式
	 */
	private void openOrUpdateLockScreenPwd() {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName(this, OpenLockScreenPwd.class);
		intent.setComponent(cn);
		startActivity(intent);
	}
	
	private void openLockStyle() {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName(this, LockStyleActivity.class);
		intent.setComponent(cn);
		startActivity(intent);
	}
	@Override
	protected void onResume() {
		super.onResume();
		getPwdType();
		getStyleType();
		nfSwitch.setChecked(isAccessEnabled());
	}
}
