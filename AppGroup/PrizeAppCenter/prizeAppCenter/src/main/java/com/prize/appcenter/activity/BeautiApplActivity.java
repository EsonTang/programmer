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
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.Category;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HeadBeauBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.beautiful.BeautifulData;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.BeautyListAdapter;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ProgressButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * *
 * 最美应用专题详情列表
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class BeautiApplActivity extends ActionBarTabActivity {
    private ListView mListView;
    private BeautyListAdapter mAdapter;

    private View headView;
    private TextView description_Tv;
    private ImageView topic_detail_Iv;
    private ImageView beauty_detail_Iv;
    private Cancelable reqHandler;
    private String TAG = "BeautiApplActivity";
    private int currentIndex = 1;
    // 无更多内容加载
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;
    private boolean isFootViewNoMore = true;
    private int lastVisiblePosition;
    private BeautifulData data;
    ProgressButton mProgressButton;
    private IUIDownLoadListenerImp listener = null;
    private boolean isActivity = true; // 默认true
    AppsItemBean gameInfo;
    Map<String, Category> mHeaderIdMap = new HashMap<String, Category>();
    private DownDialog mDownDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (isActivity) {
                if (gameInfo != null) {
                    mProgressButton.invalidate();
                }
            }

        }

        ;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);

        setContentView(R.layout.activity_game_ranking);
        WindowMangerUtils.changeStatus(getWindow());
        LayoutInflater inflater = LayoutInflater.from(this);
        headView = inflater.inflate(R.layout.head_beauty, null);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        findViewById();
        init();
        setListener();

    }

    private boolean isLoadMore = true;

    private void setListener() {
        OnScrollListener mOnScrollListener = new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastVisiblePosition >= mListView.getCount() - 1
                        && isLoadMore) {
                    isLoadMore = false;
                    // JLog.e("huang", "data.pageCount="+data.pageCount
                    // +" currentIndex="+currentIndex);
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
				lastVisiblePosition = mListView.getLastVisiblePosition();
			}
		};

		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		mProgressButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (gameInfo == null) {
					return;
				}
				int state = AIDLUtils.getGameAppState(gameInfo.packageName,
						gameInfo.id, gameInfo.versionCode);
				switch (state) {
				case AppManagerCenter.APP_STATE_UNEXIST:
				case AppManagerCenter.APP_STATE_UPDATE:
				case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

					if (ClientInfo.networkType == ClientInfo.NONET) {
						ToastUtils.showToast(R.string.nonet_connect);
						return;
					}
				}
				if (BaseApplication.isDownloadWIFIOnly()
						&& ClientInfo.networkType != ClientInfo.WIFI) {
					switch (state) {
					case AppManagerCenter.APP_STATE_UNEXIST:
					case AppManagerCenter.APP_STATE_UPDATE:
					case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
						mDownDialog = new DownDialog(BeautiApplActivity.this,
								R.style.add_dialog);
						mDownDialog.show();
						mDownDialog.setmOnButtonClic(new OnButtonClic() {

							@Override
							public void onClick(int which) {
								dismissDialog();
								switch (which) {
								case 0:
									break;
								case 1:
									UIUtils.downloadApp(gameInfo);
									break;
								}
							}
						});
						break;
					default:
						mProgressButton.onClick();
						break;
					}
				} else {
					mProgressButton.onClick();
				}
			}
		});

		// mListView.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1,
		// int position, long id) {
		// // mAdapter.onItemClick(position - 1); // -1
		// // beacause
		// }
		// });

		headView.findViewById(R.id.relativity).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (gameInfo == null) {
							return;
						}
						UIUtils.gotoAppDetail(gameInfo.id,BeautiApplActivity.this);
						MTAUtil.onDetailClick(BeautiApplActivity.this,
								gameInfo.name, gameInfo.packageName);
						// UIUtils.gotoMostAppDetail(v.findViewById(R.id.beauty_detail_Iv),
						// gameInfo, gameInfo.id,BeautiApplActivity.this);
					}
				});
	}

	private void init() {
		mListView.addHeaderView(headView);
		mAdapter = new BeautyListAdapter(this);
		mToken = AIDLUtils.bindToService(this, this);
		mListView.setAdapter(mAdapter);

		listener = IUIDownLoadListenerImp.getInstance();
		listener.setmCallBack(new IUIDownLoadCallBack() {

			@Override
			public void callBack(String pkgName, int state,boolean isNewDownload) {
				mHandler.removeCallbacksAndMessages(null);
				mHandler.sendEmptyMessage(0);

			}
		});
		AIDLUtils.registerCallback(listener);
		requestData();
	}

    private void requestData() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/topic/zuimeia");
        params.addBodyParameter("pageSize", String.valueOf(Constants.PAGE_SIZE));
        params.addBodyParameter("pageIndex", String.valueOf(currentIndex));
        reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				hideWaiting();
				currentIndex++;
				isLoadMore = true;
				try {
					int code = new JSONObject(result).getInt("code");
					if (code == 0) {
						String response = new JSONObject(result)
								.getString("data");
						data = GsonParseUtils.parseSingleBean(response,
								BeautifulData.class);
						if (data.pageCount <= 0) {
							loadingFailed(new ReloadFunction() {

								@Override
								public void reload() {
									requestData();
								}
							});
							return;
						}
						if (currentIndex == 2 && data.topic != null) {
							processHeadData(data.topic);
						}
						if (data != null && data.apps != null
								&& data.apps.size() > 0) {
							processData(data.apps);
						}
					} else {
						loadingFailed(new ReloadFunction() {

							@Override
							public void reload() {
								requestData();
							}
						});
					}
					removeFootView();
				} catch (JSONException e) {
					JLog.i(TAG, "JSONException");
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				ToastUtils.showToast(R.string.net_error);
				JLog.i(TAG, "onError-ex.getMessage()=" + ex.getMessage());
				hideWaiting();
				isLoadMore = true;
				if (mAdapter != null && mAdapter.getCount() <= 0) {
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

	protected void processHeadData(HeadBeauBean topic) {
		gameInfo = new AppsItemBean();
		gameInfo.downloadUrl = topic.app.downloadUrl;
		gameInfo.versionCode = topic.app.versionCode;
		gameInfo.versionName = topic.app.versionName;
		gameInfo.apkSize = topic.app.apkSize;
		gameInfo.id = topic.app.id;
		gameInfo.packageName = topic.app.packageName;
		gameInfo.name = topic.app.name;
		gameInfo.iconUrl = topic.app.iconUrl;
		mProgressButton.setGameInfo(gameInfo);
		description_Tv.setText(topic.description);
		String url = topic.app.largeIcon == null ? topic.app.iconUrl
				: topic.app.largeIcon;
		ImageLoader.getInstance().displayImage(url, beauty_detail_Iv,
				UILimageUtil.getUILoptions(), null);
		ImageLoader.getInstance().displayImage(topic.imageUrl, topic_detail_Iv,
				UILimageUtil.getADCacheUILoptions(), null);
		description_Tv.setText(topic.description);
		setTitle(topic.title);
	}

	private void processData(ArrayList<AppsItemBean> beans) {
		if (beans != null && beans.size() <= 0 && currentIndex == 2) {
			// no_data_tv.setVisibility(View.VISIBLE);
			// listView.setVisibility(View.GONE);
			return;
		} else {
			// no_data_tv.setVisibility(View.GONE);
			// listView.setVisibility(View.VISIBLE);
		}
		mAdapter.addData(generateHeaderId(beans));
		// if (data.app == null) {
		// return;
		// }

		if (data.pageCount < currentIndex) {
			addFootViewNoMore();
		}
	}

	/**
	 * 对GridView的Item生成HeaderId,
	 * 
	 * @param nonHeaderIdList
	 * @return
	 */
	protected ArrayList<Category> generateHeaderId(
			List<AppsItemBean> nonHeaderIdList) {
		ArrayList<Category> hasHeaderIdList = new ArrayList<Category>();

		for (ListIterator<AppsItemBean> it = nonHeaderIdList.listIterator(); it
				.hasNext();) {
			AppsItemBean mGridItem = it.next();
			String ymd = mGridItem.updateTime.substring(0,
					mGridItem.updateTime.lastIndexOf("-"));
			JLog.i(TAG, "ymd=" + ymd);
			Category category = null;
			if (!mHeaderIdMap.containsKey(ymd)) {
				category = new Category(ymd);
				// category = new Category(mGridItem.updateTime.substring(0,
				// mGridItem.updateTime.lastIndexOf("-")));
				mHeaderIdMap.put(ymd, category);
				category.addItem(mGridItem);
				hasHeaderIdList.add(category);
			} else {
				mHeaderIdMap.get(ymd).addItem(mGridItem);
			}
		}
		return hasHeaderIdList;
	}

	private void findViewById() {
		beauty_detail_Iv = (ImageView) headView
				.findViewById(R.id.beauty_detail_Iv);
		mProgressButton = (ProgressButton) headView
				.findViewById(R.id.game_download_btn);
		description_Tv = (TextView) headView.findViewById(R.id.description_Tv);
		topic_detail_Iv = (ImageView) headView
				.findViewById(R.id.topic_detail_Iv);
		mListView = (ListView) findViewById(android.R.id.list);
	}

	@Override
	public String getActivityName() {
		return "BeautiApplActivity";
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {

	}

	public void onDestroy() {
		super.onDestroy();
		reqHandler.cancel();
		mHandler.removeCallbacksAndMessages(null);
		AIDLUtils.unregisterCallback(listener);
		listener.setmCallBack(null);
		listener = null;
		AIDLUtils.unbindFromService(mToken);
	}

	@Override
	protected void onResume() {
		if (mAdapter != null) {
			mAdapter.setIsActivity(true);
			mAdapter.setDownlaodRefreshHandle();
		}
		isActivity = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mAdapter != null) {
			mAdapter.setIsActivity(false);
			mAdapter.removeDownLoadHandler();

		}
		isActivity = false;
		super.onPause();
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView && (null != mListView)) {
			mListView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	/**
	 * 添加无更多加载布局
	 */
	private void addFootViewNoMore() {
		if (isFootViewNoMore) {
			removeFootView();
			// mListView.addFooterView(noLoading, null, false);
			mListView.addFooterView(noLoading);
			isFootViewNoMore = false;
		}
	}

	/**
	 * 添加加载更多
	 */
	private void addFootView() {
		if (hasFootView) {
			return;
		}
		mListView.addFooterView(loading);
		hasFootView = true;
	}

	private void dismissDialog() {
		if (mDownDialog != null && mDownDialog.isShowing()) {
			mDownDialog.dismiss();
			mDownDialog = null;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mAdapter.setDownlaodRefreshHandle();
	}
}
