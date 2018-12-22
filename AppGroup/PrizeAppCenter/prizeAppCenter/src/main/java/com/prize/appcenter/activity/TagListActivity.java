package com.prize.appcenter.activity;

import android.os.Bundle;

import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.SearchResultFragment;
import com.prize.appcenter.fragment.TagResultFragment;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.util.UIUtils;

public class TagListActivity extends ActionBarNoTabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setNeedAddWaitingView(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);
		WindowMangerUtils.changeStatus(getWindow());
		String tag = getIntent().getStringExtra("tag");
		if (tag != null) {
			super.setTitle(tag);
			goToSearResFragmnet(tag);
		}
		UIUtils.addActivity(this);
	}

	/**
	 * 跳转到 {@link SearchResultFragment}执行搜索，显示结果
	 * 
	 * @param keyword
	 *            关键字
	 */
	public void goToSearResFragmnet(String keyword) {
		TagResultFragment mSearchResultFragment = (TagResultFragment) getSupportFragmentManager()
				.findFragmentByTag(TagResultFragment.class.getName());
		if (mSearchResultFragment == null) {
			mSearchResultFragment = new TagResultFragment();
			Bundle args = new Bundle();
			args.putString("keyword", keyword);
			args.putBoolean("tag", true);
			mSearchResultFragment.setArguments(args);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.search_container, mSearchResultFragment,
							TagResultFragment.class.getName())
					.commitAllowingStateLoss();
		} else {
			mSearchResultFragment.requestData(keyword);
		}

	}

	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UIUtils.removeActivity(this);
	}
}
