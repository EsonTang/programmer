package com.prize.prizethemecenter.ui.actionbar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.prize.app.util.DataStoreUtils;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.ui.utils.UIUtils;

public abstract class ActionBarNoTabActivity extends ActionBarActivity {

	/** 标题 */
	private TextView title = null;
	protected View divideLine;

	private ImageButton  bar_search;

	@Override
	protected void initActionBar() {
		enableSlideLayout(false);
		findViewById(R.id.action_bar_no_tab).setVisibility(View.VISIBLE);
		findViewById(R.id.action_bar_no_tab).setBackgroundColor(this.getResources().getColor(R.color.white));
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				// 返回
				case R.id.action_bar_back:
					onBackPressed();
					if (DataStoreUtils.readShareInfo(DataStoreUtils.FONT_DETAIL_KEY)){
						DataStoreUtils.removeShareInfo(DataStoreUtils.FONT_DETAIL_KEY);
					}
					break;
				case R.id.bar_search:
					UIUtils.goSearchActivity(ActionBarNoTabActivity.this);
					break;
				}
			}
		};
		// 增加点击返回的灵敏度
		findViewById(R.id.action_bar_back).setOnClickListener(onClickListener);
		findViewById(R.id.bar_search).setOnClickListener(
				onClickListener);
		divideLine = findViewById(R.id.divide_line);
		title = (TextView) findViewById(R.id.action_bar_title);
		bar_search = (ImageButton) findViewById(R.id.bar_search);
		title.setOnClickListener(onClickListener);
	}

	public void setSearchGone(){
		if(bar_search!=null){
			bar_search.setVisibility(View.GONE);
		}
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
	}

	@Override
	protected void onStart() {
		super.onStart();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}
}
