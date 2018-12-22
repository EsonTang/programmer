package com.prize.prizethemecenter.ui.actionbar;

import com.prize.prizethemecenter.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public abstract class ActionBarTabActivity extends ActionBarActivity{
	/** 标题 */
	private TextView title = null;
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
					finish();
					break;
				case R.id.bar_title:
					onBackPressed();
					break;
//				case R.id.action_bar_search:
//					UIUtils.goSearchActivity(ActionBarTabActivity.this);
//					break;
//				// 设置
//				case R.id.action_go_downQueen:
//					UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
//							ActionBarTabActivity.this);
//					break;
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
		title = (TextView) findViewById(R.id.bar_title);
		title.setOnClickListener(onClickListener);
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
//		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
	public void setTitle(int title) {
		if (this.title != null) {
			this.title.setText(title);
		}
	}

	/**
	 * 设置标题栏
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		if (this.title != null) {
			this.title.setText(title);
		}
	}
	
}
