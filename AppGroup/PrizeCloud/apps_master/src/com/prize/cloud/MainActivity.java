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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.prize.cloud.app.AppManager;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.util.Utils;

/**
 * 登录与非登录状态的跳转逻辑页，无UI，逻辑判断过程中的过渡
 * @author yiyi
 * @version 1.0.0
 */
public class MainActivity extends BaseActivity {
	private static final String RELOGIN_ACTION = "com.prize.cloud.relogin";
	private TextView login_id;
	private TextView regist_new_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utils.curAccount(this) != null && Utils.getPersonalInfo(this) != null) {
			startActivity(new Intent(this, LogonActivity.class));
			finish();
			return;
		} else {
			setContentView(R.layout.regist_my_koobee);
			ViewUtils.inject(this);
			Intent broadIntent = new Intent(RELOGIN_ACTION);
			sendBroadcast(broadIntent);
		}
		AppManager.getAppManager().addActivity(this);
		initView();
	}

	private void initView() {
		
	}

	@OnClick(R.id.login_id)
	public void loginClk(View v) {
		Intent intent = new Intent(this, LoginActivity.class);
		if (getIntent() != null) {
			Bundle bundle = getIntent().getBundleExtra("otherApp");
			if (bundle != null) {
				intent.putExtra("otherApp", bundle);
			}
		}
		startActivity(intent);
	}

	@OnClick(R.id.regist_new_id)
	public void regClk(View v) {
		//String tel = Utils.getTel(this);
		if (!Utils.getSimState(this))
			startActivity(new Intent(this, RegActivity.class));
		else
			startActivity(new Intent(this, OwnRegActivity.class));
	}
}
