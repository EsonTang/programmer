package com.prize.appcenter.ui.pager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.GameListGiftBean;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.gamegift.SingeGameGiftData;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.MainApplication.LoginDataCallBack;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.ui.adapter.GameGiftListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * 
 ** 
 * 礼包page
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GameGiftPager extends BasePager {
    private final String TAG = "GameGiftPager";
    private GameGiftListAdapter adapter;
    private ListView listView;
    private int currentIndex = 1;
    private View selfView;
    private ArrayList<AppsItemBean> beans = new ArrayList<AppsItemBean>();
    private PromptDialogFragment df;
    private String mUserId;
    private Cancelable reqHandler;
    private SingeGameGiftData data;
//    private RootActivity activity;
    private TextView no_data_tv;

    public GameGiftPager(RootActivity activity) {
        super(activity);
        setNeedAddWaitingView(true);
    }

	public View onCreateView() {
		if (null == selfView) {
			LayoutInflater inflater = LayoutInflater.from(activity);
			adapter = new GameGiftListAdapter(activity);
			View view = inflater.inflate(R.layout.activity_main_home, null);
			noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
			loading = inflater.inflate(R.layout.footer_loading_small, null);
			listView = (ListView) view.findViewById(android.R.id.list);
			no_data_tv = (TextView) view.findViewById(R.id.no_data_tv);
			selfView = view;
		}

		setlisetener();

		df = PromptDialogFragment.newInstance(activity.getString(R.string.tip),
				activity.getString(R.string.toast_tip_download_only_wifi),
				activity.getString(R.string.now_download),
				activity.getString(R.string.download_after),
				mDeletePromptListener);
		if (adapter == null) {
			adapter = new GameGiftListAdapter(activity);
		}
		return selfView;
	}

	/**
	 * 继续提示对话框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			startDownLoad();
		}
	};
	private int lastVisiblePosition;
	private boolean isLoadMore = true;

	private void setlisetener() {

		OnScrollListener mOnScrollListener = new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastVisiblePosition >= listView.getCount() - 1
						&& currentIndex < data.pageCount) {
					isLoadMore = false;
					if (data.pageCount < currentIndex) {
						addFootViewNoMore();
					} else {
						addFootView();
						requestData();

					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lastVisiblePosition = listView.getLastVisiblePosition();
			}
		};

		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

	}

	private void startDownLoad() {
		int size = beans.size();
		for (int i = 0; i < size; i++) {
			AppsItemBean gameBean = beans.get(i);
			int state = AIDLUtils.getGameAppState(gameBean.packageName,
					gameBean.id + "", gameBean.versionCode);
			switch (state) {
			case AppManagerCenter.APP_STATE_UNEXIST:
			case AppManagerCenter.APP_STATE_UPDATE:
			case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
				UIUtils.downloadApp(gameBean);
			}
		}
	}

	public void onActivityCreated() {
		initTab();
		queryUserId();
		((MainApplication) this.activity.getApplication())
				.setLoginCallBack(new LoginDataCallBack() {

					@Override
					public void setPerson(Person person) {
						queryUserId();
					}
				});

	}

	protected void queryUserId() {
		Person person = ((MainApplication) this.activity.getApplication()).getPerson();
		if (person != null) {
			mUserId = person.getUserId();
		} else {
			mUserId = null;
		}
	}

	/**
	 * 初始化TabHost
	 *
	 */
	private void initTab() {
		listView.setAdapter(adapter);

	}

	public void loadData() {
		if (adapter == null) {
			adapter = new GameGiftListAdapter(activity);
		}
		listView.setAdapter(adapter);
		if (0 == adapter.getCount()) {
			showWaiting();
			requestData();
		} else {
			hideWaiting();
		}
	}

    private void requestData() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/list");
        if (!TextUtils.isEmpty(mUserId)) {
            params.addBodyParameter("userId", mUserId);
        }
        params.addBodyParameter("pageSize", String.valueOf(Constants.PAGE_SIZE));
        params.addBodyParameter("pageIndex", String.valueOf(currentIndex));
        reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				hideWaiting();
				currentIndex++;
				try {
					String response = new JSONObject(result).getString("data");
					data = GsonParseUtils.parseSingleBean(response,
							SingeGameGiftData.class);
					if (data != null && data.appGifts != null) {
						processData(data.appGifts);
					}
				} catch (JSONException e) {
					JLog.i(TAG, "JSONException");
					e.printStackTrace();
					hideWaiting();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.i(TAG, "onFailure=" + ex.getMessage());
				hideWaiting();
				if (adapter != null && adapter.getCount() <= 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							loadData();
						}
					});
				}
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

	private void processData(ArrayList<GameListGiftBean> beans) {
		if (beans != null && beans.size() <= 0 && currentIndex == 2) {
			no_data_tv.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			return;
		} else {
			no_data_tv.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
		adapter.addData(beans);
		// if (data.app == null) {
		// return;
		// }

		if (data.pageCount < currentIndex) {
			addFootViewNoMore();
		}
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {

	}

	@Override
	public String getPageName() {
		return "GameGiftPager";
	}

	public void onDestroy() {
		if (adapter != null) {
			adapter.removeDownLoadHandler();
		}
		if (reqHandler != null) {
			reqHandler.cancel();

		}

		((MainApplication) this.activity.getApplication())
				.setLoginCallBack(null);
	}

	@Override
	public void onResume() {
		if (adapter != null) {
			adapter.setIsActivity(true);
			adapter.setDownlaodRefreshHandle();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (adapter != null) {
			adapter.setIsActivity(false);
			adapter.removeDownLoadHandler();
		}
		super.onPause();
	}

	// 无更多内容加载
	private View noLoading = null;
	private View loading = null;
	private boolean hasFootView;
	private boolean isFootViewNoMore = true;

	/**
	 * 添加加载更多
	 */
	private void addFootView() {
		if (hasFootView) {
			return;
		}
		listView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView && (null != listView)) {
			listView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	/**
	 * 添加无更多加载布局
	 */
	private void addFootViewNoMore() {
		if (isFootViewNoMore) {
			removeFootView();
			listView.addFooterView(noLoading, null, false);
			isFootViewNoMore = false;
		}
	}

}
