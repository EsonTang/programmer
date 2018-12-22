package com.prize.appcenter.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.NewGameData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/*
 * Created by Administrator on 2017/4/18.
 */

public class NewGameListActivity extends ActionBarTabActivity {
    private ListView mListView;
    private Callback.Cancelable mCancelable;
    private NewGameData mData;
    // 无更多内容加载
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView = false;
    private boolean isCanLoadMore = true;
    private int lastVisiblePosition;
    private boolean isFootViewNoMore = true;
    private GameListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.new_game_layout);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            super.setTopicTitle(title);
        } else {
            setTopicTitle(R.string.game_header_new_game_title);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);

        mListView = (ListView) findViewById(R.id.new_game_list);
        mAdapter = new GameListAdapter(this,null,null);
        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

        requestData("1");

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long categoryId) {
                if (mAdapter.getItem(position) != null) {
                    UIUtils.gotoAppDetail(mAdapter.getItem(position),
                            mAdapter.getItem(position).id,
                            NewGameListActivity.this);
                    MTAUtil.onNewGameListClick(position + 1);
                }
            }
        });
    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    mAdapter.setIsActivity(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mAdapter.setIsActivity(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    mAdapter.setIsActivity(false);
                    break;
            }
            if (!isCanLoadMore) {
                return;
            }
            if (JLog.isDebug) {
                JLog.i("NewGameListActivity", "lastVisiblePosition=" + lastVisiblePosition + "--mAdapter.getCount()=" + mAdapter.getCount() + "-isCanLoadMore-"
                        + isCanLoadMore + "--mData.pageIndex=" + mData.pageIndex + "--mData.pageCount=" + mData.pageCount);
            }
            if (lastVisiblePosition >= mAdapter.getCount() - 1 && isCanLoadMore) {
                isCanLoadMore = false;
                boolean hasNextPage = mData.pageIndex + 1 <= mData.pageCount;
                if (hasNextPage && !hasFootView) {
                    addFootView();
                    requestData(mData.pageIndex + 1 + "");
                } else {
                    addFootViewNoMore();
                    isCanLoadMore = true;
                }
            }


        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = mListView.getLastVisiblePosition();
        }
    };

    /**
     * 加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        mListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            mListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
        isCanLoadMore = false;
    }

    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            mListView.removeFooterView(noLoading);
            isFootViewNoMore = true;
        }
        isCanLoadMore = true;
    }

    /**
     * 取消加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != mListView)) {
            mListView.removeFooterView(loading);
            hasFootView = false;
        }

    }

    @Override
    public String getActivityName() {
        return "NewGameListActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void requestData(final String pageIndext) {
        if (!hasFootView) {
            showWaiting();
        }
        RequestParams params = new RequestParams(Constants.GIS_URL + "/recommand/newgame");
        params.addBodyParameter("pageIndex", pageIndext);
        params.addBodyParameter("pageSize", "20");
        mCancelable = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                if (!hasFootView) {
                    hideWaiting();
                }
                removeFootViewNoMore();
                removeFootView();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        mData = GsonParseUtils.parseSingleBean(res, NewGameData.class);
                        if (mData != null && mData.apps != null && mData.apps.size() > 0) {
                            mData.apps.removeAll(CommonUtils.filterUnInstalled(mData.apps));
                        }
                        mAdapter.addData(mData.apps);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    isCanLoadMore=true;
                    if (!hasFootView) {
                        loadingFailed(new ReloadFunction() {

                            @Override
                            public void reload() {
                                requestData(pageIndext);
                            }

                        });
                    }
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                isCanLoadMore=true;
                if (!"1".equals(pageIndext)) {
                    ToastUtils.showToast(R.string.net_error);
                }
                if (!hasFootView) {
                    hideWaiting();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData(pageIndext);
                        }

                    });
                }
                removeFootView();
            }
        });

    }

    @Override
    protected void onResume() {
        if (mAdapter != null) {
            mAdapter.setIsActivity(true);
            mAdapter.setDownlaodRefreshHandle();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mAdapter != null) {
            mAdapter.setIsActivity(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }

        if (mAdapter != null) {
            mAdapter.removeDownLoadHandler();
        }
        super.onDestroy();
    }

}
