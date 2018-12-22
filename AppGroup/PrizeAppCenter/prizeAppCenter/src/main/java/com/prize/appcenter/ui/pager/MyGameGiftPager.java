package com.prize.appcenter.ui.pager;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.gamegift.MyGiftsData;
import com.prize.app.util.GsonParseUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.MainApplication.LoginDataCallBack;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.callback.ReceiveCodeCallback;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.ui.adapter.MyGiftListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;

/**
 * 
 **
 * 我的礼包page
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MyGameGiftPager extends BasePager implements ReceiveCodeCallback {
	private final String TAG = "MyGameGiftPager";
	private MyGiftListAdapter adapter;
	private ListView listView;
	private RequestParams params;
	private View selfView;
	private ArrayList<AppsItemBean> beans = new ArrayList<AppsItemBean>();
	private PromptDialogFragment df;
	private int currentIndex = 1;
	private String mUserId;
	private Person person;
	private Cancelable reqHandler;
	private MyGiftsData data;
	private RootActivity activity;
	private TextView no_data_tv;
	private TextView login_tv;
	private boolean loginFlag = false;
	private RelativeLayout noLogin_Rlyt;

	public MyGameGiftPager(RootActivity activity) {
		super(activity);
		this.activity = activity;
		setNeedAddWaitingView(true);
	}

	public View onCreateView() {
		if (null == selfView) {
			LayoutInflater inflater = LayoutInflater.from(activity);
			adapter = new MyGiftListAdapter(activity);
			adapter.setDownlaodRefreshHandle();
			View view = inflater.inflate(R.layout.activity_main_home, null);
			noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
			loading = inflater.inflate(R.layout.footer_loading_small, null);
			listView = (ListView) view.findViewById(android.R.id.list);
			no_data_tv = (TextView) view.findViewById(R.id.no_data_tv);
			login_tv = (TextView) view.findViewById(R.id.login_tv);
			noLogin_Rlyt = (RelativeLayout) view
					.findViewById(R.id.noLogin_Rlyt);
			selfView = view;
		}

		setlisetener();

		df = PromptDialogFragment.newInstance(activity.getString(R.string.tip),
				activity.getString(R.string.toast_tip_download_only_wifi),
				activity.getString(R.string.now_download),
				activity.getString(R.string.download_after),
				mDeletePromptListener);

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
	private boolean refreash = false;

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
		login_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				jumpToLoginActivity();
			}
		});

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
		((MainApplication) this.activity.getApplication())
				.setLoginCallBack(new LoginDataCallBack() {

					@Override
					public void setPerson(Person person) {
						loadData();
					}
				});

	}

	protected void queryUserId() {
		person = ((MainApplication) this.activity.getApplication()).getPerson();
		if (person != null) {
			mUserId = person.getUserId();
			loginFlag = true;
			noLogin_Rlyt.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		} else {
			noLogin_Rlyt.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			mUserId = null;
			loginFlag = false;
		}
	}

	/**
	 * 初始化TabHost
	 * 
	 * @param id
	 */
	private void initTab() {
		listView.setAdapter(adapter);

	}

	public void loadData() {
		queryUserId();
		if (!loginFlag) {
			// if (!BaseApplication.isThird) {
			// ToastUtils.showToast(R.string.login_koobee);
			// }
			noLogin_Rlyt.setVisibility(View.VISIBLE);
			listView.setVisibility(View.VISIBLE);
			hideWaiting();
			return;
		}

		if (adapter == null) {
			adapter = new MyGiftListAdapter(activity);
		}
		listView.setAdapter(adapter);
		if (0 == adapter.getCount() || refreash) {
			showWaiting();
			requestData();
		} else {
			hideWaiting();
		}
	}

	private void requestData() {
		if (params == null) {
			params = new RequestParams(Constants.GIS_URL + "/gift/mine");
		}
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
							MyGiftsData.class);
					if (data != null) {
						processData();
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
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

				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished() {

				// TODO Auto-generated method stub

			}
		});

	}

	private void processData() {
		if (data == null || data.appGiftCodes == null
				|| data.appGiftCodes.size() <= 0) {
			if (currentIndex == 2) {
				no_data_tv.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				return;
			} else {
				no_data_tv.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}

			return;
		}
		adapter.setData(data.appGiftCodes);
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
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (adapter != null) {
			adapter.setIsActivity(false);
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

	/**
	 * 方法描述：跳转到云账号登录页面
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void jumpToLoginActivity() {
		UIUtils.gotoActivity(LoginActivityNew.class, activity);

	}

	@Override
	public void callBack(boolean refreash) {
		this.refreash = refreash;
	}
}
