package com.prize.appcenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.util.UIUtils;

public class AppRestoreActivity extends ActionBarNoTabActivity {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_sync_assistant);
		mContext = getApplicationContext();
		WindowMangerUtils.changeStatus(getWindow());

	}

	// ------------------------------------默认样式------------------------------------------//
	@Override
	protected void initActionBar() {

		enableSlideLayout(false);
		findViewById(R.id.action_bar_tab).setVisibility(View.VISIBLE);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				// 返回
				case R.id.back_IBtn:
					finish();
					break;
				case R.id.bar_title:
					onBackPressed();
					break;
				case R.id.action_bar_search:
					UIUtils.goSearchActivity(AppRestoreActivity.this);
					break;
				case R.id.action_go_downQueen:
					UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
							AppRestoreActivity.this);
					break;
				}
			}
		};

		// 增加点击返回的灵敏度
		findViewById(R.id.action_go_downQueen).setOnClickListener(
				onClickListener);
		findViewById(R.id.back_IBtn).setOnClickListener(onClickListener);
		findViewById(R.id.action_bar_search)
				.setOnClickListener(onClickListener);
		TextView title = (TextView) findViewById(R.id.bar_title);
		title.setText(R.string.app_sync_restore);
	}

	@Override
	public String getActivityName() {

		return this.getClass().getSimpleName();
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {

	}

}
