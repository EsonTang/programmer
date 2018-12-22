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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.cloud.R;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;

/**
 * 登录与非登录状态的跳转逻辑页，无UI，逻辑判断过程中的过渡
 * @author yiyi
 * @version 1.0.0
 */
public class MainActivityCloud extends BaseActivity implements OnClickListener {

	private TextView mLoginTv;
	private TextView mRegister;
	private TextView mTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utils.curAccount(this) != null && Utils.getPersonalInfo(this) != null) {
			startActivity(new Intent(this, PersonActivity.class));
			finish();
			return;
		}
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.regist_my_koobee);
		//ViewUtils.inject(this);
		mLoginTv= (TextView) findViewById(R.id.login_id);
		mRegister= (TextView) findViewById(R.id.regist_new_id);
		
		mLoginTv.setOnClickListener(this);
		mRegister.setOnClickListener(this);
		mTitle = (TextView) findViewById(R.id.title_id);
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/*public void loginClk(View v) {
		startActivity(new Intent(this, LoginActivity.class));
	}

	public void regClk(View v) {
		//String tel = Utils.getTel(this);
		if (!Utils.getSimState(this))
			startActivity(new Intent(this, RegActivity.class));
		else
			startActivity(new Intent(this, OwnRegActivity.class));
	}*/
	
	public void onBackClk(View v) {
		onBackPressed();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.login_id) {
			startActivity(new Intent(this, LoginActivity.class));
		} else if (id == R.id.regist_new_id) {
			if (!Utils.getSimState(this))
				startActivity(new Intent(this, RegActivity.class));
			else
				startActivity(new Intent(this, OwnRegActivity.class));
		}
	}
}
