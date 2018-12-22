/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.gamegift.MyGiftsData;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.MyGiftListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

/**
 ** 
 * 我的礼包界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MyGameGiftActivity extends ActionBarNoTabActivity {
	private int currentIndex = 1;

	private MyGiftListAdapter adapter;
	private ListView listView;
	private RequestParams params;
	private String mUserId;
	private Person person;
	private Cancelable reqHandler;
	private MyGiftsData data;
	protected final String TAG = "MyGameGiftActivity";

	private TextView no_data_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNeedAddWaitingView(true);
		setContentView(R.layout.activity_main_home);
		LayoutInflater inflater = LayoutInflater.from(this);
		noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
		loading = inflater.inflate(R.layout.footer_loading_small, null);
		WindowMangerUtils.changeStatus(getWindow());
		mToken = AIDLUtils.bindToService(this, this);
		super.setTitle(R.string.my_gift);
		findViewById();
		queryUserId();
		init();
		setListener();
	}

	protected void queryUserId() {
		person = ((MainApplication) getApplication()).getPerson();
		if (person != null) {
			mUserId = person.getUserId();
		} else {
			mUserId = null;
		}
	}

	private void init() {

		if (adapter == null) {
			adapter = new MyGiftListAdapter(this);
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
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/mine");
        if (!TextUtils.isEmpty(mUserId)) {
            params.addBodyParameter("userId", mUserId);
        }
        params.addBodyParameter("pageSize", String.valueOf(Constants.PAGE_SIZE));
        params.addBodyParameter("pageIndex", String.valueOf(currentIndex));

		reqHandler = XExtends.http().post(params,
				new org.xutils.common.Callback.CommonCallback<String>() {

					@Override
					public void onSuccess(String result) {
						hideWaiting();
						JLog.i(TAG, result);
						currentIndex++;
						try {
							String response = new JSONObject(result)
									.getString("data");
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
						if (adapter != null && adapter.getCount() <= 0) {
							loadingFailed(new ReloadFunction() {

								@Override
								public void reload() {
									requestData();
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

	private void setListener() {

		adapter.setDownlaodRefreshHandle();

		OnScrollListener scrollListener = new OnScrollListener() {

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
				.getInstance(), true, true, scrollListener));

	}

	private void findViewById() {
		listView = (ListView) findViewById(android.R.id.list);
		no_data_tv = (TextView) findViewById(R.id.no_data_tv);
	}

	@Override
	public String getActivityName() {
		return "MyGameGiftActivity";
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
	}

	@Override
	protected void initActionBar() {
		findViewById(R.id.action_bar_feedback).setVisibility(View.GONE);
		super.initActionBar();
	}

	private void processData() {
		if (data == null || data.appGiftCodes == null
				|| data.appGiftCodes.size() <= 0) {
			if (currentIndex == 2) {
				no_data_tv.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			}
			return;

		}
		adapter.setData(data.appGiftCodes);
		if (data.pageCount < currentIndex) {
			addFootViewNoMore();
		}
	}

	@Override
	protected void onResume() {
		if (adapter != null) {
			adapter.setIsActivity(true);
			adapter.setDownlaodRefreshHandle();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (adapter != null) {
			adapter.setIsActivity(false);
			adapter.removeDownLoadHandler();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (reqHandler != null) {
			reqHandler.cancel();

		}
		AIDLUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	// 无更多内容加载
	private View noLoading = null;
	private View loading = null;
	private boolean hasFootView;
	private boolean isLoadMore = true;
	private boolean isFootViewNoMore = true;
	private int lastVisiblePosition;

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

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		adapter.setDownlaodRefreshHandle();
	}
}
