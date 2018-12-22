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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.task.UnBindTask;
import com.prize.cloud.util.Utils;

/**
 * 已绑定邮箱页
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class BindedActivity extends BaseActivity {
	@ViewInject(R.id.complete_tv_id)
	private TextView mEmailText;
	private String email;
	@ViewInject(R.id.title_id)
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_email_success);
		ViewUtils.inject(this);
		email = getIntent().getStringExtra("email");
		if (!TextUtils.isEmpty(email))
			mEmailText.setText(String.format(
					getString(R.string.complete_bind_email), email));
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it = new Intent(BindedActivity.this, PersonActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if (TextUtils.isEmpty(email))
					it.putExtra("binded", false);
				startActivity(it);
				finish();
			}
		});

	}

	@OnClick(R.id.canclebt_id)
	public void unbindClk(View v) {
		CloudAccount account = Utils.curAccount(this);
		new UnBindTask(this, new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				email = null;
				onBackPressed();
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();

			}
		}, account.getPassport(), email, account.getLoginName()).doExecute();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent it = new Intent(this, PersonActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (TextUtils.isEmpty(email))
			it.putExtra("binded", false);
		startActivity(it);
		finish();
	}
}
