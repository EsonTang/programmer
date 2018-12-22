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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.cloud.R;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;

/**
 * 注册成功
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class RegSuccessActivity extends BaseActivity {
	private TextView mPromptText;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_success);
		mPromptText = (TextView) findViewById(R.id.prompt_id);
		AppManager.getAppManager().addActivity(this);
		String phone = getIntent().getStringExtra("phone");
		if (TextUtils.isEmpty(phone)) {
			mPromptText.setText(this.getString(R.string.your_kb_account));
		} else {
			mPromptText.setText(String.format(getString(R.string.your_kb),
					phone));
		}
		mTitle = (TextView) findViewById(R.id.title_id);
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onComplete(View view) {
		boolean booting = Utils.isBootActivate(getApplicationContext());
		if (!booting) {
			Intent it = new Intent(this, PersonActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			AppManager.getAppManager().finishAllActivity();
		} else {
			Intent it = new Intent();
			ComponentName comp = new ComponentName("com.prize.boot",
					"com.prize.boot.OtherSetActivity");
			it.setComponent(comp);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		}
		finish();
	}
	
	public void onBackClk(View v) {
		onBackPressed();
	}
}
