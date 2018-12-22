package com.prize.appcenter.ui.pager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.adapter.RankListAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * longbaoxiu
 *  2017/3/31.17:30
 *
 */

public class RankListPager extends BasePager {
    private RankListAdapter adapter;
    private ListView mListView;
    private Callback.Cancelable mCancelable;
    private int currentIndex = 1;
    private String rankType = null;
    private boolean hasFootView;
    private boolean isFootViewNoMore = true;
    private int lastVisiblePosition;
    private boolean isLoadMore = true;
    private View noLoading = null;
    private View loading = null;
    private int classPosition = 0;
    public RankListPager(RootActivity activity, String rankType,int classPosition) {
        super(activity);
        setNeedAddWaitingView(true);
        this.rankType = rankType;
        this.classPosition = classPosition;
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.activity_singlegame_layout, rootView,false);
        mListView = (ListView) root.findViewById(android.R.id.list);
        mListView.setDividerHeight(0);
        adapter = new RankListAdapter(activity);
        mListView.setAdapter(adapter);

        noLoading = inflater.inflate(R.layout.footer_nomore_show, mListView,false);
        loading = inflater.inflate(R.layout.footer_loading_small, mListView,false);
        setLister();
        return root;
    }

    private void setLister() {
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null && adapter.getItem(position) != null && !TextUtils.isEmpty(adapter.getItem(position).id)) {
                    UIUtils.gotoAppDetail(adapter.getItem(position), adapter.getItem(position).id, activity);
                    if(position<=14){
                        MTAUtil.onRankListClicked(activity,position+1,classPosition+1);
                    }
                }
            }
        });
    }

    @Override
    public void loadData() {
        if (0 == adapter.getCount()) {
            showWaiting();
            requestRankListData();
        } else {
            hideWaiting();
        }
    }

    private PrizeAppsTypeData mPrizeAppsTypeData;

    private void requestRankListData() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (currentIndex <= 1) {
            showWaiting();
        }
        RequestParams entity = new RequestParams(Constants.GIS_URL
                + "/rank/ranklist");
        entity.addBodyParameter("pageIndex", String.valueOf(currentIndex));
        entity.addBodyParameter("pageSize", String.valueOf(20));
        entity.addBodyParameter("rankType", rankType);
        mCancelable = XExtends.http().post(entity,
                new PrizeXutilStringCallBack<String>() {

                    @Override
                    public void onSuccess(String result) {
                        try {
                            isLoadMore = true;
                            hideWaiting();
                            removeFootView();
                            removeFootViewNoMore();
                            JSONObject o = new JSONObject(result);
                            if (o.getInt("code") == 0) {
                                String o1 = o.getString("data");
                                mPrizeAppsTypeData = GsonParseUtils.parseSingleBean(o1, PrizeAppsTypeData.class);
                                ArrayList<AppsItemBean> gameList = CommonUtils.filterResData(mPrizeAppsTypeData.apps, currentIndex == 1, 5);
                                if (currentIndex == 1) {
                                    adapter.setData(gameList);

                                } else {
                                    adapter.addData(gameList);
                                }
                                currentIndex++;
                                if (currentIndex > mPrizeAppsTypeData.getPageCount()) {
                                    addFootViewNoMore();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        isLoadMore = true;
                        if (currentIndex == 1) {
                            hideWaiting();
                            loadingFailed(new ReloadFunction() {
                                @Override
                                public void reload() {
                                    requestRankListData();
                                }
                            });
                        }else{
                            ToastUtils.showToast(R.string.net_error);
                            removeFootView();
                        }
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


    /**
     * 移除无数据提示
     */
    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            mListView.removeFooterView(noLoading);
            isFootViewNoMore = true;
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
            mListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lastVisiblePosition >= mListView.getCount() - 1
                    && isLoadMore) {
                isLoadMore = false;
                // 如果现在的页小于总共返回的页
                if (currentIndex <= mPrizeAppsTypeData.getPageCount()) {
                    addFootView();
                    requestRankListData();
                } else {
                    addFootViewNoMore();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            lastVisiblePosition = mListView.getLastVisiblePosition();

        }
    };

    @Override
    public void onResume() {
        if(adapter !=null){
            adapter.setDownlaodRefreshHandle();
            adapter.setIsActivity(true);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if(adapter !=null){
            adapter.setIsActivity(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(adapter !=null){
            adapter.setIsActivity(false);
            adapter.resetDownLoadHandler();
        }
    }
}
