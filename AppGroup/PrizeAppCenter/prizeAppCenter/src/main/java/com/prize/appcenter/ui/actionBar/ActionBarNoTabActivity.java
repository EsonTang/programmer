package com.prize.appcenter.ui.actionBar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UIUtils;
import com.tencent.stat.StatService;

public abstract class ActionBarNoTabActivity extends ActionBarActivity {

	/** 标题 */
	private TextView title = null;
	protected View divideLine;

	@Override
	protected void initActionBar() {
		enableSlideLayout(false);
		findViewById(R.id.action_bar_no_tab).setVisibility(View.VISIBLE);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				// 返回
				case R.id.action_bar_back:
					onBackPressed();
					break;
				
				case R.id.action_bar_feedback:
					UIUtils.goSearchActivity(ActionBarNoTabActivity.this);
					break;
				}
			}
		};
		// 增加点击返回的灵敏度
		findViewById(R.id.action_bar_back).setOnClickListener(onClickListener);
		findViewById(R.id.action_bar_feedback).setOnClickListener(
				onClickListener);
		divideLine = findViewById(R.id.divide_line);
		title = (TextView) findViewById(R.id.action_bar_title);
		title.setOnClickListener(onClickListener);
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

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		StatService.onPause(this);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}
}
