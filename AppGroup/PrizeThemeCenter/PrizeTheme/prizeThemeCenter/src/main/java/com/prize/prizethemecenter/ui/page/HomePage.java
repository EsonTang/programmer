package com.prize.prizethemecenter.ui.page;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MainActivity;
import com.prize.prizethemecenter.activity.WebViewActivity;
import com.prize.prizethemecenter.bean.HomeAdBean;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.bean.table.AdTable;
import com.prize.prizethemecenter.bean.table.ThemeItemTable;
import com.prize.prizethemecenter.bean.table.TipsTable;
import com.prize.prizethemecenter.callback.NetConnectedListener;
import com.prize.prizethemecenter.request.HomeRequest;
import com.prize.prizethemecenter.response.HomeResponse;
import com.prize.prizethemecenter.ui.adapter.HomeGalleryFlowAdapter;
import com.prize.prizethemecenter.ui.adapter.TopicThemeListAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.HomeCacheUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.FlowIndicator;
import com.prize.prizethemecenter.ui.widget.GalleryFlow;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;
import com.prize.prizethemecenter.ui.widget.swiperefresh.SwipeRefreshLayoutView;
import com.prize.prizethemecenter.ui.widget.swiperefresh.SwipeRefreshLayoutView.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 *  首页主题
 * @author Administrator pengyang
 */
public class HomePage extends BasePage implements OnItemClickListener,NetConnectedListener{

	private View headerView;
	
	/** 滚动区 */
	private GalleryFlow galleryFlow = null;
	
	/** 图片滚动间隔 */
	private long delayMillis = 3 * 1000;
	/** 自动滚动 */
	private boolean isAutoScroll = false;
	// 初始化推荐列表 初始个数
	private static final int DEFAULT_RECOMMAND_NUMBER = 60;

	protected static final int SCROLL = 0;

	protected static final int REFRESH = 1;

	protected static final String TAG = "HomePage";

	private  FlowIndicator flowIndicator;
	
    private HomeGalleryFlowAdapter adsAdapter;
    
    private SwipeRefreshLayoutView swipeLayout;
    
    private boolean isCanLoadMore = true;
    
    private TextView rank_tv;
	private TextView classify_tv;
	private TextView topic_tv;
	private TextView local_tv;
	
	public GridViewWithHeaderAndFooter gridview;
	
	private TopicThemeListAdapter adapter;

	private int lastVisiblePosition;
	
    private View loading = null;
	private boolean hasFootView;
	private TextView loading_tv;
	private TextView caution_tv;
	private ProgressBar bar;
	private MainActivity context;

	// 头布局的高度
    private int headerHeight;
    private boolean isRequestOk = false;
	private Cancelable mHandler;
	private int pageIndex = 1;
	private int pageSize = 9;
	private int pageCount;

	private boolean hasNextPage = false;
	private HomeRequest request;
	private HomeResponse response;

	private int adSize = 0;
	private int itemSize = 0;
	private int wordSize = 0;

	public HomePage(MainActivity context) {
		super(context);
		this.context = context;
		setNeedAddWaitingView(true);
		adapter = new TopicThemeListAdapter(activity);
	}
    
	/** 设置是否自动滚动 */
	public void setAutoScroll(boolean auto) {
		isAutoScroll = auto;
//		if (adapter != null) {
//			adapter.setIsActivity(auto);
//		}
	}
   
	/** 图片滚动任务 */
	private Handler flowHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCROLL: {
				if (isAutoScroll && (null != galleryFlow)) {
					galleryFlow.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
					flowHandler.removeMessages(SCROLL);
				}
				flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
			}
				break;
			case REFRESH:
				if (swipeLayout.isRefreshing()) {
					swipeLayout.setRefreshing(false);
				}
				break;
			}
		}
	};
	
	@Override
	public void scrollToTop() {
		if (gridview != null) {
			gridview.setSelection(0);
		}
	}
	
	@Override
	public View onCreateView() {
		setMainpage(true);
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.home_page, null);
		headerView = inflater.inflate(R.layout.home_page_header, null);
		galleryFlow = (GalleryFlow) headerView.findViewById(R.id.recommand_galleryflow);
		flowIndicator = (FlowIndicator) headerView.findViewById(R.id.flowIndicator);
		adsAdapter = new HomeGalleryFlowAdapter(activity);
		galleryFlow.setAdapter(adsAdapter);
		galleryFlow.setSelection(DEFAULT_RECOMMAND_NUMBER * 10);
		galleryFlow.setFocusable(false);
		galleryFlow.setFocusableInTouchMode(false);
		galleryFlow.setVerticalFadingEdgeEnabled(false);
		galleryFlow.setHorizontalFadingEdgeEnabled(false);
		
		flowHandler.sendEmptyMessage(SCROLL);
		swipeLayout = (SwipeRefreshLayoutView) root
				.findViewById(R.id.swipeRefreshLayout);
		// 顶部刷新的样式
		swipeLayout.setColorScheme(android.R.color.holo_red_light,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);
		swipeLayout.setBackgroundColor(activity.getResources().getColor(
				R.color.white));
		swipeLayout.setProgressViewOffset(false, 100, 200);
		
		rank_tv = (TextView) headerView.findViewById(R.id.rank_tv);
		classify_tv = (TextView) headerView.findViewById(R.id.classify_tv);
		topic_tv = (TextView) headerView.findViewById(R.id.topic_tv);
		local_tv = (TextView) headerView.findViewById(R.id.local_tv);

		loading = inflater.inflate(R.layout.footer_loading_tab, null);
		loading_tv = (TextView)loading.findViewById(R.id.loading_tv);
		caution_tv = (TextView)loading.findViewById(R.id.caution_tv);
		bar  = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);
		
		gridview = (GridViewWithHeaderAndFooter)root.findViewById(R.id.gridview);
		//为gridview添加尾部
		addFootView();
		if (null != headerView) {
			gridview.addHeaderView(headerView);
		}

		if(adapter==null){
			adapter = new TopicThemeListAdapter(activity);
		}
		gridview.setAdapter(adapter);
		adapter.setParent(gridview);
		
		if (mCallBack != null) {
			mCallBack.onScrollStates(0);
		}
		setListener();
		return root;
	}

	private void setListener() {
		rank_tv.setTag(0);
		classify_tv.setTag(1);
		topic_tv.setTag(2);
		local_tv.setTag(3);
	    rank_tv.setOnClickListener(onClickListener);
	    classify_tv.setOnClickListener(onClickListener);
	    topic_tv.setOnClickListener(onClickListener);
		local_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"0");
			}
		});
		galleryFlow.setOnItemClickListener(this);
		galleryFlow.setOnTouchListener(new OnTouchListener() {
			/** 处理当用户点中图片时，不进行滚动 */
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
					swipeLayout.setEnabled(true);
					break;
				case MotionEvent.ACTION_DOWN:
					flowHandler.removeMessages(SCROLL);
					break;
				case MotionEvent.ACTION_MOVE:
					swipeLayout.setEnabled(false);
					break;
				}
				return false;
			}
		});

		galleryFlow.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (flowIndicator != null && adsAdapter != null)
					flowIndicator.setSeletion(adsAdapter.getItemIndex(position));

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					swipeLayout.setRefreshing(false);
				} else {
					if (!isCanLoadMore) {
						swipeLayout.setRefreshing(false);
						return;
					}
					doHeadRequest();
					flowHandler.sendEmptyMessageDelayed(REFRESH, 10 * 1000);
				}
			}
		});
        
		gridview.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		if(adapter != null){
			adapter.setIsActivity(true);
			adapter.setDownlaodRefreshHandle();
		}
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(adapter.getItem(position)==null)
					return;
				if (adapter.getItem(position).ad_pictrue != null) {
					UIUtils.gotoThemeDetail(adapter.getItem(position).id,adapter.getItem(position).ad_pictrue);
				}
				MTAUtil.onHotTheme(adapter.getItem(position).name);
			}
		});
	}
	
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"theme");
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		switch (parent.getId()) {
			case R.id.recommand_galleryflow:
				HomeAdBean gallery = adsAdapter.getItem(position);
				MTAUtil.onClickPageThemeBanner(adsAdapter.getItemIndex(position));
				if ("0".equals(gallery.ad_type)) {  // 单个主题
					UIUtils.gotoThemeDetail(gallery.correlation_id,null);
				}
				if ("1".equals(gallery.ad_type)) {  // 单个壁纸
					UIUtils.gotoWallDetail(activity,gallery.correlation_id,null,null);
				}
				if ("2".equals(gallery.ad_type)) {  // 单个字体

					UIUtils.gotoFontDetail(gallery.correlation_id,null,false);
				}
				if ("3".equals(gallery.ad_type)) {  // 主题专题
					UIUtils.onClickTopicItem(gallery.correlation_id, activity,"theme");
				}
				if ("4".equals(gallery.ad_type)) {  // 壁纸专题
					UIUtils.onClickTopicItem(gallery.correlation_id, activity,"wallpaper");
				}
				if ("5".equals(gallery.ad_type)) {  // 字体专题
					UIUtils.onClickTopicItem(gallery.correlation_id, activity,"font");
				}
				if ("6".equals(gallery.ad_type)) {  // 网页
					Intent it = new Intent(activity, WebViewActivity.class);
					it.putExtra(WebViewActivity.P_URL, gallery.correlation_id);
					activity.startActivity(it);
					activity.overridePendingTransition(R.anim.fade_in,
							R.anim.fade_out);
				}
				break;
			default:
				break;
		}
	}
	
	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (swipeLayout.isRefreshing() || !isCanLoadMore) {
				return;
			}
			if (lastVisiblePosition >= gridview.getCount() - 1
					&& isCanLoadMore) {
				isCanLoadMore = false;
				if (hasNextPage) {
					loading_tv.setVisibility(View.VISIBLE);
					bar.setVisibility(View.VISIBLE);
					caution_tv.setVisibility(View.GONE);
					addFootView();
					getHomeData ();
				} else {
					loading_tv.setVisibility(View.GONE);
					bar.setVisibility(View.GONE);
					caution_tv.setVisibility(View.VISIBLE);
					addFootView();
					isCanLoadMore = true;
				}
			}
		}

		@Override
		public void onScroll(AbsListView listView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = gridview.getLastVisiblePosition();
			// 判断当前最上面显示的是不是头布局，因为listview，所以头布局的位置是1，即第二个
			if (firstVisibleItem == 0) {
				// 获取头布局
				View view = gridview.getChildAt(0);
				if (view != null) {
					// 获取头布局现在的最上部的位置的相反数
					int top = -view.getTop();
					// 获取头布局的高度
					headerHeight = galleryFlow.getHeight();
					// 满足这个条件的时候，是头布局在Listview的最上面第一个控件的时候，只有这个时候，我们才调整透明度
					if (top <= headerHeight && top >= 0) {
						// 获取当前位置占头布局高度的百分比
						float f = (float) top / (float) headerHeight;
						if (mCallBack != null) {
							mCallBack.onScrollStates((int) (f * 255));
						}
					}
				}
			} 
			else if (firstVisibleItem >= 1) {
				if (mCallBack != null) {
					mCallBack.onScrollStates(255);
				}
			} else {
				if (mCallBack != null) {
					mCallBack.onScrollStates(0);
				}
			}
		}
	};
    
	/** 
	 * *回调
	 */
	public static interface CallBack {
		void onScrollStates(int alpha);
	}
	private CallBack mCallBack;

	public void setmCallBack(CallBack mCallBack) {
		this.mCallBack = mCallBack;
	}

	long last_modify = 0;
	public void doHeadRequest() {
		long modify_time = System.currentTimeMillis();
		JLog.i("hu","modify_time=="+modify_time +"--last_modify=="+last_modify);
		if(last_modify!=modify_time){
			pageIndex = 1;
			getHomeData ();
			isCanLoadMore = false;
//			JLog.i("hu","getHomeData==");
		}else{
			swipeLayout.setRefreshing(false);
			pageIndex = 2;
			getHomeData ();
			initThemeListData();
		}
		last_modify = modify_time;
//		removeFootViewNoMore();
	}
	
	@Override
	public void loadData() {
		if (0 == adapter.getCount()) {
			showWaiting();
			initData();
		} else {
			hideWaiting();
			gridview.setVisibility(View.VISIBLE);
		}
		if (!flowHandler.hasMessages(SCROLL)) {
			flowHandler.sendEmptyMessageDelayed(SCROLL, 100);
		}
		isAutoScroll = true;
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		initThemeListData();
		getHomeData();
	}

	private void getHomeData() {

		request = new HomeRequest ();
		request.pageIndex = pageIndex;
		request.pageSize = pageSize;
		mHandler = x.http().post(request, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject obj = new JSONObject(result);
					if(obj.getInt("code")==00000){
						response = CommonUtils.getObject(result,
								HomeResponse.class);

						pageCount = response.data.hot.pageCount;
						ArrayList<ThemeItemBean>  itemBeen =  response.data.hot.item;
						ArrayList<HomeAdBean> ads = response.data.banner;
						words = response.data.searchbox.tips;

						hideWaiting();
						if (swipeLayout.isRefreshing()) {
							swipeLayout.setRefreshing(false);
						}

						if(pageIndex == 1){
							addToDB(response.data.hot.item,response.data.banner,words);
							adapter.clearAll();
						}

						flowIndicator.setVisibility(View.VISIBLE);
						adsAdapter.setData(ads);
						flowIndicator.setCount(adsAdapter.getItemsSize());

						activity.setStrs(words);

						adapter.addData(itemBeen);
						isRequestOk = true;
						isCanLoadMore = true;

						pageIndex++;
						if(pageIndex<=pageCount){
							hasNextPage = true;
						}else{
							hasNextPage = false;
						}
						JLog.i("hu","onSuccess==");
						removeFootView();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override


			public void onError(Throwable ex, boolean isOnCallback) {
				setMainpage(true);
				if (null != adapter && adapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							loadData();
						}
					});
				} else {
//					ToastUtils.showToast(R.string.net_error);
				}
				isCanLoadMore = true;
				removeFootView();
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}


	/**
	 * 存储到数据库
	 * @param themelist
	 * @param bannerlist
     */
	private void addToDB(List<ThemeItemBean> themelist, List<HomeAdBean> bannerlist,ArrayList<String> words) {
		if(themelist!=null) {
			itemSize = themelist.size();
			List<ThemeItemTable> itemTables = new ArrayList<>(itemSize);
			for (int i = 0; i < itemSize; i++) {
				ThemeItemTable tt = HomeCacheUtils.toThemeTable(
						themelist.get(i));
				itemTables.add(tt);
			}
			try {
				MainApplication.getDbManager().delete(ThemeItemTable.class);
				MainApplication.getDbManager().save(itemTables);
			} catch (DbException e) {
				e.printStackTrace();
			}
		}

		if(bannerlist!=null) {
			adSize = bannerlist.size();
			List<AdTable> adTables = new ArrayList<>(adSize);
			for (int i = 0; i < adSize; i++) {
				AdTable at = HomeCacheUtils.toADTable(
						bannerlist.get(i), i);
				adTables.add(at);
			}
			try {
				MainApplication.getDbManager().delete(AdTable.class);
				MainApplication.getDbManager().save(adTables);
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
		if(words!=null) {
			wordSize = words.size();
			List<TipsTable> tipsTables = new ArrayList<>(wordSize);
			for (int i = 0; i < wordSize; i++) {
				TipsTable tst = HomeCacheUtils.toTipsTable(
						words.get(i), i);
				tipsTables.add(tst);
			}
			try {
				MainApplication.getDbManager().delete(TipsTable.class);
				MainApplication.getDbManager().save(tipsTables);
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 从数据库中取数据
	 */
	private void initThemeListData() {
		try {
			List<AdTable> ta = MainApplication.getDbManager().findAll(AdTable.class);
			ArrayList<HomeAdBean> ll = new ArrayList<>();
			for(int i = 0;i<ta.size();i++){
				ll.add(HomeCacheUtils.toAD(ta.get(i)));
			}
			List<ThemeItemTable> tt = MainApplication.getDbManager().findAll(ThemeItemTable.class);
			ArrayList<ThemeItemBean> ls = new ArrayList<>();
			for(int i = 0;i<tt.size();i++){
				ls.add(HomeCacheUtils.toTheme(tt.get(i)));
			}

			List<TipsTable> tips = MainApplication.getDbManager().findAll(TipsTable.class);
			ArrayList<String> str = new ArrayList<>();
			if(tips!=null){
				for(int i = 0;i<tips.size();i++){
					str.add(HomeCacheUtils.toTips(tips.get(i)));
				}
			}
			if(ta.size()==0 && tt.size()==0 && tips.size()==0){
				showWaiting();
			}else{
				hideWaiting();
			}
			if(pageIndex==1){
				adapter.clearAll();
				adapter.addData(ls);
				adsAdapter.setData(ll);
				flowIndicator.setCount(adsAdapter.getItemsSize());
				flowIndicator.setVisibility(
						adsAdapter.getItemsSize() > 0 ? View.VISIBLE: View.GONE);
				activity.setStrs(str);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onActivityCreated() {
		
	}

	@Override
	public String getPageName() {
		return null;
	}
    
	@Override
	public void onResume() {
		if (ClientInfo.networkType == ClientInfo.NONET) {
			swipeLayout.setRefreshing(false);
		}
		adapter.notifyDataSetChanged();
		adapter.setDownlaodRefreshHandle();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		swipeLayout.setRefreshing(false);
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (flowHandler != null) {
			flowHandler.removeCallbacksAndMessages(null);
		}
		if(adapter != null){
			adapter.setIsActivity(false);
			adapter.removeDownLoadHandler();
		}
		if(mHandler!=null){
			mHandler.cancel();
		}
	}
	
	/**
	 * 添加加载更多
	 */
	private void  addFootView() {
		if (hasFootView) {
			return;
		}
		ViewGroup parent = (ViewGroup) loading.getParent();
		if (parent != null) {
			parent.removeAllViews();
		}
		if(loading!=null){
			gridview.addFooterView(loading);
		}
		hasFootView = true;
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView && (null != gridview)) {
			gridview.removeFooterView(loading);
			hasFootView = false;
		}
	}
	
	@Override
	public void onNetConnected() {
		if (isRequestOk) {
			return;
		}
		if (isAutoScroll) {
			getHomeData ();
		}
	}

	@Override
	public void showHistory() {
	}

	@Override
	public void addToHistory(String text) {
	}
}
