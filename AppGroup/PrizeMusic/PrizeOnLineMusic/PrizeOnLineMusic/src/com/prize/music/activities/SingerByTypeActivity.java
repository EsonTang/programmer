package com.prize.music.activities;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.online.task.SingerByTypeTask;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.SingerByTypeAdapter;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.R;
import com.prize.onlinemusibean.ArtistByCategoryResponse;
import com.xiami.sdk.XiamiSDK;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 根据歌手类型Activity
 * @author pengyang
 */
public class SingerByTypeActivity extends FragmentActivity implements OnClickListener,ServiceConnection{
    
	private ListView listView;
	private ImageButton action_bar_back;
	private TextView action_bar_title;
	private ImageButton action_bar_search;
	private View reloadView;
	private View waitView;
	
	private SingerByTypeAdapter adapter;
	private RequestManager requestManager;
	private XiamiSDK xiamiSDK;
	
	
	/** 歌手类型*/
	private String type;
	
	/** 是否有下一页*/
	private boolean more;
	
	/** 当前可见的最后位置*/
    private int lastVisiblePosition;
    private boolean isLoadingMore;
    
	/** 加载更多*/ 
	private View loading = null;
	private boolean hasFootView = false;

	/** 无更多内容加载 */
	private View noLoading = null;
	private boolean hasFootViewNoMore;
	
	private int pageIndex = 1; 
	private BottomActionBarFragment mBActionbar;
	
	private ServiceToken mToken;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.singer_type_list);
		StateBarUtils.changeStatus(getWindow());
		initView();
		init();
	}
	private void init() {
		
		requestManager = RequestManager.getInstance();
		xiamiSDK = new XiamiSDK(getApplicationContext(),SDKUtil.KEY, SDKUtil.SECRET);
		
		action_bar_title.setText(getIntent().getStringExtra("title"));	
		adapter = new SingerByTypeAdapter(this);
		
		addFootView();
		listView.setAdapter(adapter);
		removeFootView();

		type = getIntent().getStringExtra("type");
		loadData(type,pageIndex);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				if (adapter.getItem(position) != null) {
					UiUtils.JumpToSingerOnlineActivity(getApplicationContext(),adapter.getItem(position),
							adapter.getItem(position).artist_id);
				}
			}
		});
		
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		
	}
	
	OnScrollListener mOnScrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (lastVisiblePosition >= adapter.getCount() - 1
					&& isLoadingMore == false) {
				// 分页显示
				if (more) {
					isLoadingMore = true;
					addFootView();
					pageIndex++;
					loadData(type,pageIndex);
				} else {
					if (!hasFootViewNoMore) {  
						addFootViewNoMore();
					}
				}
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = listView.getLastVisiblePosition();
		}
	};
	
	private void initView() {
		
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		
		action_bar_back = (ImageButton) findViewById(R.id.action_bar_back);
		action_bar_title= (TextView) findViewById(R.id.action_bar_title);
		action_bar_search = (ImageButton) findViewById(R.id.action_bar_search);
		action_bar_back.setOnClickListener(this);
		action_bar_title.setOnClickListener(this);
		action_bar_search.setOnClickListener(this);
		
		loading = LayoutInflater.from(this).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(this).inflate(
				R.layout.footer_no_loading, null);
		
		reloadView = (View)findViewById(R.id.reload_Llyt);
		waitView = (View)findViewById(R.id.loading_Llyt_id);
		
		listView = (ListView)findViewById(android.R.id.list);
	}
	
	private void loadData(String type,int page) {
		SingerByTypeTask task = new SingerByTypeTask(xiamiSDK, RequestMethods.ARTIST_WORDBOOK, typeHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("type", type);
		params.put("page", page);
		task.execute(params);
	}
	
	private Handler typeHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
                
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				ArtistByCategoryResponse bean = gson.fromJson(element, ArtistByCategoryResponse.class);
				removeFootView();
				listView.setVisibility(View.VISIBLE);
				isLoadingMore = false;
				if (adapter != null && bean.artists != null) {
					adapter.setData(bean.artists);
				}
				more = bean.more;
				break;
			case RequestResCode.REQUEST_FAILE:
				removeFootView();
				isLoadingMore = false;
				if (null != adapter && adapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							loadData(type,pageIndex);
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	/**
	 * 取消加载更多
	 */
	private void removeFootView() {
		if (hasFootView) {
			listView.removeFooterView(loading);
			hasFootView = false;
		}
	}
	
	/**
	 * 加载更多
	 */
	private void addFootView() {
		listView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		listView.addFooterView(noLoading, null, false);
		hasFootViewNoMore = true;
	}

	/**
	 * 加载失败
	 */
	public void loadingFailed(final ReloadFunction reload) {
		waitView.setVisibility(View.GONE);
		reloadView.setVisibility(View.VISIBLE);
	    listView.setVisibility(View.GONE);
		LinearLayout reloadLinearLayout = (LinearLayout) reloadView
				.findViewById(R.id.reload_Llyt);
		if (reloadLinearLayout != null) {
			reloadLinearLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					reload.reload();
				}
			});
		}
	}
	/**
	 * 显示等待框
	 */
	public void showWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
		reloadView.setVisibility(View.GONE);
	}
	
	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
		reloadView.setVisibility(View.GONE);

	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.action_bar_back:
			onBackPressed();
			break;
		case R.id.action_bar_title:
			onBackPressed();
			break;
		case R.id.action_bar_search:
			UiUtils.goToSearchtActivity(this);
			break;
		}
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
		JLog.i("hu", "SingerByTypeActivity====onServiceConnected");
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
	protected void onResume() {
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

			@Override
			public void onClick(View v) {
				   Intent intent = new Intent(SingerByTypeActivity.this,
						AudioPlayerActivity.class);
				   startActivity(intent);
			}
		});
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		if (typeHandler != null) {
			typeHandler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}
}
