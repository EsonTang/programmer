package com.prize.appcenter.ui.actionBar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.ui.util.UIUtils;

public abstract class ActionBarTabActivity extends ActionBarActivity{
	/** 标题 */
	protected TextView mTitle = null;
	/**
	 * 初始化Action Bar
	 */
	protected void initActionBar() {
		enableSlideLayout(false);
		findViewById(R.id.action_bar_tab).setVisibility(View.VISIBLE);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				// 搜索
				// 返回
				case R.id.back_IBtn:
					onBackPressed();
					break;
			
				case R.id.action_bar_search:
					UIUtils.goSearchActivity(ActionBarTabActivity.this);
					break;
				// 设置
				case R.id.action_go_downQueen:
					UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
							ActionBarTabActivity.this);
					break;
				}
			}
		};
		// 初始化ActionBar样式
		// 增加点击返回的灵敏度
		findViewById(R.id.back_IBtn).setOnClickListener(onClickListener);
		findViewById(R.id.action_bar_search)
				.setOnClickListener(onClickListener);
		findViewById(R.id.action_go_downQueen).setOnClickListener(
				onClickListener);
		mTitle = (TextView) findViewById(R.id.bar_title);
		mTitle.setOnClickListener(onClickListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * 设置标题栏
	 * 
	 * @param title
	 */
	public void setTopicTitle(int title) {
		if (this.mTitle != null) {
			this.mTitle.setText(title);
		}
	}
	/**
	 * 设置标题栏
	 *
	 * @param title
	 */
	public void setTopicTitle(String title) {
		if (this.mTitle != null) {
			this.mTitle.setText(title);
		}
	}

}
