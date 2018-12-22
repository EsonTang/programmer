package com.prize.prizethemecenter.ui.page;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.ClientInfo;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MainActivity;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.bean.HomeAdBean;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.FontRequest;
import com.prize.prizethemecenter.request.WallpaperRequest;
import com.prize.prizethemecenter.response.HomeResponse;
import com.prize.prizethemecenter.ui.adapter.FontAdapter;
import com.prize.prizethemecenter.ui.adapter.WallpaperAdapter;
import com.prize.prizethemecenter.ui.adapter.WallpaperFontHeadAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;
import com.prize.prizethemecenter.ui.widget.swiperefresh.SwipeRefreshLayoutView;
import com.prize.prizethemecenter.ui.widget.swiperefresh.SwipeRefreshLayoutView.OnRefreshListener;
import com.prize.prizethemecenter.ui.widget.view.ScollerGridView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;

/**
 *  首页壁纸
 * @author pengyang
 */
public class WallpaperFontPage extends BasePage{

	private static final String TAG = "WallpaperFontPage";
	protected static final int REFRESH = 0;
	private MainActivity mActivity;
    private TextView classify_tv;
    private TextView topic_tv;
    private TextView local_tv;
	private TextView hot_topic_tv;
    private ScollerGridView head_view;
    private GridViewWithHeaderAndFooter gridView;
    private View view;
    private SwipeRefreshLayoutView swipeLayout;

    private WallpaperFontHeadAdapter bannerAdpater;

    private WallpaperAdapter wallpaperAdpater;
	private FontAdapter fontAdapter;
    private int lastVisiblePosition;

    private boolean isCanLoadMore =true ;

	// 无更多内容加载
    private View loading = null;
	private boolean hasFootView;

	private View headerView;

	private boolean isFont;

	private boolean hasNextPage = false;
	private WallpaperRequest wallPaperRequest;
	private FontRequest fontRequest;
	private HomeResponse response;
	private int pageIndex = 1;
	private int pageSize = 9;
	private int pageCount;

	private Callback.Cancelable mHandler;

	private TextView loading_tv;
	private TextView caution_tv;
	private ProgressBar bar;

	/**
	 * 创建一个新的实例
	 * @param activity
	 * @param isGame
	 *        是否是字体选项
	 */
	public WallpaperFontPage(RootActivity activity, boolean isGame) {
		super(activity);
		this.isGame = isGame;
		isFont = isGame;
		mActivity = (MainActivity) activity;
		setNeedAddWaitingView(true);
	}
     
	private Handler flowHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
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
		if (gridView != null) {
			gridView.setSelection(0);
		}
	}
	@Override
	public View onCreateView() {
		setMainpage(true);
		LayoutInflater inflater = LayoutInflater.from(activity);
		view = inflater.inflate(R.layout.wall_page, null);
		headerView = inflater.inflate(R.layout.wallpaper_page_header, null);
		loading = inflater.inflate(R.layout.footer_loading_tab, null);
		loading_tv = (TextView)loading.findViewById(R.id.loading_tv);
		caution_tv = (TextView)loading.findViewById(R.id.caution_tv);
		bar  = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);
		findIdBy();
		setListener();
		return view;
	}

	@SuppressWarnings("deprecation")
	private void findIdBy() {
		    
       classify_tv = (TextView) headerView.findViewById(R.id.classify_tv);
       if(isFont){
    	   classify_tv.setText(R.string.recommend_rank);
    	   Drawable drawable= mActivity.getResources().getDrawable(R.drawable.rank_icon_selector);
    	   /// 这一步必须要做,否则不会显示.
    	   drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
    	   // setCompoundDrawables  (Drawable left, Drawable top, Drawable right, Drawable bottom);
    	   classify_tv.setCompoundDrawables(null,drawable,null,null);  
       }
	   topic_tv = (TextView) headerView.findViewById(R.id.topic_tv);
	   local_tv = (TextView) headerView.findViewById(R.id.local_tv);
		hot_topic_tv= (TextView) headerView.findViewById(R.id.hot_topic_tv);
		if(isFont){
			hot_topic_tv.setText(R.string.hot_font);
		}else{
			hot_topic_tv.setText(R.string.hot_wallpaper);
		}
	   head_view = (ScollerGridView) headerView.findViewById(R.id.head_view);
	   bannerAdpater = new WallpaperFontHeadAdapter(mActivity);
	   head_view.setAdapter(bannerAdpater);
	   gridView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.gridview);
	   if(isFont){
		   gridView.setNumColumns(2);
	   }else {
		   gridView.setNumColumns(3);
	   }
	   //为gridview添加尾部
	   addFootView();
	   swipeLayout = (SwipeRefreshLayoutView) view
				.findViewById(R.id.swipeRefreshLayout);
		// 顶部刷新的样式
	   swipeLayout.setColorScheme(android.R.color.holo_red_light,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);
	   swipeLayout.setBackgroundColor(activity.getResources().getColor(
				R.color.white));
	   swipeLayout.setProgressViewOffset(false, 100, 200);
	   
	   if (null != headerView) {
		   gridView.addHeaderView(headerView);
	   }
       if(isFont){
    	   if(fontAdapter==null){
			   fontAdapter  = new FontAdapter(mActivity,false);
			   fontAdapter.setIsActivity(true);
			   fontAdapter.setDownlaodRefreshHandle();
    	   }
    	   gridView.setAdapter(fontAdapter);
       }else{
    	   if(wallpaperAdpater==null){
    		   wallpaperAdpater = new WallpaperAdapter(mActivity);
			   wallpaperAdpater.setIsActivity(true);
			   wallpaperAdpater.setDownlaodRefreshHandle();
    	   }
    	   gridView.setAdapter(wallpaperAdpater);
       }
	}
	private void setListener() {
		if(isFont){
			classify_tv.setTag(0);
		}else {
			classify_tv.setTag(1);
		}
		topic_tv.setTag(2);
		local_tv.setTag(3);
	    classify_tv.setOnClickListener(onClickListener);
	    topic_tv.setOnClickListener(onClickListener);
		local_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isFont){
					UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"2");
				}else {
					UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"1");
				}
			}
		});
	    
		head_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HomeAdBean gallery = bannerAdpater.getItem(position);
				if(isFont){
					MTAUtil.onClickPageFontBanner(position);
				}else{
					MTAUtil.onClickPageWallBanner(position);
				}
				if ("2".equals(gallery.ad_type)) {  // 单个字体
//					UIUtils.gotoWallDetail(gallery.correlation_id,null,null);
					UIUtils.gotoFontDetail(gallery.correlation_id,null,false);
				}
				if ("4".equals(gallery.ad_type)) {  // 壁纸专题
					UIUtils.onClickTopicItem(gallery.correlation_id, activity,"wallpaper");
				}
				if ("5".equals(gallery.ad_type)) {  // 字体专题
					UIUtils.onClickTopicItem(gallery.correlation_id, activity,"font");
				}

			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isFont){
					ThemeItemBean fontBean = fontAdapter.getItem(position);
					if(fontBean==null)
						return;
					if (fontBean.ad_pictrue != null) {
						UIUtils.gotoFontDetail(fontBean.id,fontBean.ad_pictrue,false);
					}
					MTAUtil.onHotFont(fontBean.name);
				}else{
					ThemeItemBean wallBean = wallpaperAdpater.getItem(position);
					if (wallBean==null)
						return;
					String p=null;
					if (wallBean.wallpaper_pic != null) {
						p = UILimageUtil.getPicPath(activity,wallBean.wallpaper_pic);
						UIUtils.gotoWallDetail(mActivity,wallBean.id,wallBean.wallpaper_type,p);
					}
					MTAUtil.onHotWallpaper(wallBean.name);
				}
			}
		});
		
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		
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
	}
	long last_modify = 0;
	public void doHeadRequest() {
		long modify_time = System.currentTimeMillis();
		if(last_modify!=modify_time){
			pageIndex = 1;
			getWallpaperFontData ();
			isCanLoadMore = false;
		}else{
			swipeLayout.setRefreshing(false);
			pageIndex = 2;
			getWallpaperFontData ();
		}
		last_modify = modify_time;
	}
	
    OnClickListener onClickListener = new OnClickListener() {
			
		@Override
		public void onClick(View v) {
			if(isFont){
				UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"font");
			}else {
				UIUtils.onClickNavbarsItem((int) v.getTag(), activity,"wallpaper");
		   }
		}
	};
	
	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (swipeLayout.isRefreshing() || !isCanLoadMore) {
				return;
			}
			if (lastVisiblePosition >= gridView.getCount() - 1
					&& isCanLoadMore) {
				isCanLoadMore = false;
				if (hasNextPage) {
					loading_tv.setVisibility(View.VISIBLE);
					bar.setVisibility(View.VISIBLE);
					caution_tv.setVisibility(View.GONE);
					addFootView();
					getWallpaperFontData ();
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
			lastVisiblePosition = gridView.getLastVisiblePosition();
		}
	};

	@Override
	public void loadData() {
		if(isFont){
			if (0 == fontAdapter.getCount()) {
				showWaiting();
				initData();
			} else {
				hideWaiting();
				gridView.setVisibility(View.VISIBLE);
			}
		}else{
			if (0 == wallpaperAdpater.getCount()) {
				showWaiting();
				initData();
			} else {
				hideWaiting();
				gridView.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void initData() {
		getWallpaperFontData();
	}

	private void getWallpaperFontData() {

		if(isFont){
			fontRequest = new FontRequest ();
			fontRequest.pageIndex = pageIndex;
			fontRequest.pageSize = pageSize;
			RequsetData(fontRequest);
		}else {
			wallPaperRequest = new WallpaperRequest ();
			wallPaperRequest.pageIndex = pageIndex;
			wallPaperRequest.pageSize = pageSize;
			RequsetData(wallPaperRequest);
		}
	}

	private void RequsetData(BaseRequest request) {
		mHandler = x.http().post(request, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject obj = new JSONObject(result);
					if(obj.getInt("code")==00000){
						response = CommonUtils.getObject(result,
								HomeResponse.class);

						pageCount = response.data.hot.pageCount;
						ArrayList<ThemeItemBean> itemBeen =  response.data.hot.item;
						ArrayList<HomeAdBean> ads = response.data.banner;

						hideWaiting();
						if (swipeLayout.isRefreshing()) {
							swipeLayout.setRefreshing(false);
						}
						bannerAdpater.setData(ads);
						if(pageIndex==1){
							if(isFont){
								fontAdapter.clearAll();
							}else {
								wallpaperAdpater.clearAll();
							}
						}
						if(isFont){
							fontAdapter.addData (itemBeen);
						}else{
							wallpaperAdpater.addData(itemBeen);
						}

						isCanLoadMore = true;
						pageIndex++;
						if(pageIndex<=pageCount){
							hasNextPage = true;
						}else{
							hasNextPage = false;
						}
						removeFootView();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				setMainpage(true);
				hideWaiting();
				if(isFont){
					if (null != fontAdapter && fontAdapter.getCount() == 0) {
						loadingFailed(new ReloadFunction() {

							@Override
							public void reload() {
								loadData();
							}
						});
					} else {
						ToastUtils.showToast(R.string.net_error);
					}
				}else {
					if (null != wallpaperAdpater && wallpaperAdpater.getCount() == 0) {
						loadingFailed(new ReloadFunction() {

							@Override
							public void reload() {
								loadData();
							}
						});
					} else {
						ToastUtils.showToast(R.string.net_error);
					}
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
		if(isFont){
			fontAdapter.setDownlaodRefreshHandle();
			fontAdapter.notifyDataSetChanged();
		}else {
			wallpaperAdpater.setDownlaodRefreshHandle();
			wallpaperAdpater.notifyDataSetChanged();
		}
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
		if(wallpaperAdpater != null ){
			wallpaperAdpater.setIsActivity(false);
			wallpaperAdpater.removeDownLoadHandler();
		}
		if(fontAdapter!= null){
			fontAdapter.setIsActivity(false);
			fontAdapter.removeDownLoadHandler();
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
			gridView.addFooterView(loading);
		}
		hasFootView = true;
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView && (null != gridView)) {
			gridView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	@Override
	public void showHistory() {
	}

	@Override
	public void addToHistory(String text) {
	}
}
