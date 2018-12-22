package com.prize.music.activities;

import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.WatchedManager;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.SearchOriginalFragment;
import com.prize.music.ui.fragments.SearchResultFragment;
import com.prize.music.ui.widgets.SearchView;
import com.prize.music.ui.widgets.SearchView.SearchViewListener;
import com.prize.music.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 搜索界面
 * 
 * @author pengyang
 */
public class SearchActivity extends BaseActivity implements
		SearchViewListener,ServiceConnection{
	public SearchOriginalFragment mSearchOriginalFragment;
	private SearchResultFragment mSearchResultFragment;

	public SearchView searchView;
	public static final String STR = "str";
	private ServiceToken mToken;
	private BottomActionBarFragment mBActionbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_search);
		StateBarUtils.changeStatus(getWindow());
		
		findViewById();
		init();
		setListener();

	}

	private void findViewById() {
		searchView = (SearchView) findViewById(R.id.main_search_layout);
		if (getIntent() != null
				&& !TextUtils.isEmpty(getIntent().getStringExtra(STR))) {
			searchView.setHint(getIntent().getStringExtra(STR));
		}
		
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		bottom_action_bar_album_art = (ImageView)findViewById(R.id.bottom_action_bar_album_art);
	}
	
	private ImageView bottom_action_bar_album_art;
	public ImageView getBottomView() {
		return bottom_action_bar_album_art;
	}
	
	private void setListener() {
		searchView.setSearchViewListener(this);
	}

	private void init() {
		mSearchOriginalFragment = (SearchOriginalFragment) getSupportFragmentManager()
				.findFragmentByTag(SearchOriginalFragment.class.getName());
		mSearchResultFragment = (SearchResultFragment) getSupportFragmentManager()
				.findFragmentByTag(SearchResultFragment.class.getName());
		if (mSearchResultFragment != null) {
			// hideWaiting();
			getSupportFragmentManager().beginTransaction()
					.show(mSearchResultFragment).commitAllowingStateLoss();
			return;
		} else if (mSearchOriginalFragment == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.search_container, new SearchOriginalFragment(),
							SearchOriginalFragment.class.getName())
					.commitAllowingStateLoss();
			return;
		} else if (mSearchOriginalFragment.isAdded()) {
			// hideWaiting();
			getSupportFragmentManager().beginTransaction()
					.show(mSearchOriginalFragment).commitAllowingStateLoss();
		}

	}

	public void gotoRecommandActivity() {
		Intent in = new Intent(this, RecommandMoreSingerActivity.class);
		startActivity(in);
	}

	/**
	 * 设置标题栏
	 * 
	 * @param title
	 */
	public void setTitle(int title) {

	}

	/**
	 * 跳转到 {@link SearchResultFragment}执行搜索，显示结果
	 * 
	 * @param keyword
	 *            关键字
	 */
	public void goToSearResFragmnet(String keyword) {
		mSearchResultFragment = (SearchResultFragment) getSupportFragmentManager()
				.findFragmentByTag(SearchResultFragment.class.getName());
		if (mSearchResultFragment == null) {
			mSearchResultFragment = new SearchResultFragment();
			Bundle args = new Bundle();
			args.putString("keyword", keyword);
			mSearchResultFragment.setArguments(args);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.search_container, mSearchResultFragment,
							SearchResultFragment.class.getName())
					.commitAllowingStateLoss();
		} else {
			mSearchResultFragment.requestData(keyword);
		}
	}

	@Override
	public void onSearch(String text) {
		if (TextUtils.isEmpty(text)) {
			return;
		}
		WatchedManager.notifyChange(text);
		goToSearResFragmnet(text);
	}
	
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}
	
	@Override
	protected void onStart() {
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

			@Override
			public void onClick(View v) {
				   Intent intent = new Intent(SearchActivity.this,
						AudioPlayerActivity.class);
				   startActivity(intent);
			}
		});
		super.onResume();
	}
    
}
