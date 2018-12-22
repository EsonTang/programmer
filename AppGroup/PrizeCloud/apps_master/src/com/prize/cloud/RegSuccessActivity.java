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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.prize.cloud.util.Utils;

/**
 * 注册成功
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class RegSuccessActivity extends BaseActivity {
	@ViewInject(R.id.prompt_id)
	private TextView mPromptText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_success);
		ViewUtils.inject(this);
		String phone = getIntent().getStringExtra("phone");
		if (TextUtils.isEmpty(phone)) {
			mPromptText.setText(this.getString(R.string.your_kb_account));
		} else {
			mPromptText.setText(String.format(getString(R.string.your_kb), phone));
		}	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@OnClick(R.id.completetn_id)
	public void onComplete(View view) {
		boolean booting = Utils.isBootActivate(getApplicationContext());
		if (!booting) {
			Intent it = new Intent(this, LogonActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} else {
			Intent it = new Intent();
			ComponentName comp = new ComponentName("com.prize.boot",
					"com.prize.boot.OtherSetActivity");
			it.setComponent(comp);
			it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		}
		finish();
	}
}
