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
package com.prize.boot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/**
 * 用于第一开机时云账号的启动页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class BootUpAccountActivity extends AbstractGuideActivity {
	private TextView mLogin;
	private TextView mRegist;
	/** 注册**/
	private static final int SELECT_REGIST = 1;
	/** 登陆**/
	private static final int SELECT_LOGIN = 2;
	private int mSelect = SELECT_REGIST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_account);
		initView();
		setGuideTitle(R.drawable.account_icon, R.string.kobee_account);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		mLogin = (TextView) findViewById(R.id.login_id);
		mRegist = (TextView) findViewById(R.id.regist_id);
		selRegist();
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.next_btn) {
			/*final String tel = getTel(this);
			Intent intent = new Intent();
			ComponentName comp = null;
			if (mSelect == 1) {
				if (TextUtils.isEmpty(tel))
					comp = new ComponentName("com.prize.cloud",
							"com.prize.cloud.RegActivity");
				else
					comp = new ComponentName("com.prize.cloud",
							"com.prize.cloud.OwnRegActivity");
			} else {
				comp = new ComponentName("com.prize.cloud",
						"com.prize.cloud.LoginActivity");
				intent.putExtra("hide_lostpswd_btn", true);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			intent.setComponent(comp);
			BootUpAccountActivity.this.startActivity(intent);*/
			WelcomeApplication.getInstance().finishAllActivity();
		} else if (v.getId() == R.id.skip_id) {
			startActivity(new Intent(this, SetOverActivity.class));
		} else if (v.getId() == R.id.login_id) {
			selLogin();
		} else if (v.getId() == R.id.regist_id) {
			selRegist();
		} else if (v.getId() == R.id.skip_id) {
			startActivity(new Intent(BootUpAccountActivity.this,
					OtherSetActivity.class));
		} else if (v.getId() == R.id.im_back) {
			finish();
		}
	}
	
	private void selLogin() {
		mSelect = SELECT_LOGIN;
		mLogin.setTextColor(getResources().getColor(R.color.prize_text_select));
		mRegist.setTextColor(getResources().getColor(
				R.color.prize_text_default));
	}
	
	private void selRegist() {
		mSelect = SELECT_REGIST;
		mRegist.setTextColor(getResources().getColor(R.color.prize_text_select));
		mLogin.setTextColor(getResources().getColor(
				R.color.prize_text_default));
	}

	public static String getTel(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		String tel = tm.getLine1Number();
		return tel;
	}

}
