package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadHistoryBean;
import com.prize.prizethemecenter.request.DownloadAllHistoryRequest;
import com.prize.prizethemecenter.response.DownloadHistoryResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.DownloadHistoryListViewAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.StateBarUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DownLoadHistoryActivity extends ActionBarNoTabActivity implements AdapterView.OnItemClickListener, View.OnClickListener {


    @InjectView(R.id.action_back)
    TextView actionBack;
    @InjectView(R.id.header_edt)
    RelativeLayout headerEdt;
    @InjectView(R.id.lv_queenlist)
    ListView lvQueenlist;
    @InjectView(R.id.iv_dafaultImg)
    ImageView ivDafaultImg;
    @InjectView(R.id.rl_default)
    RelativeLayout rlDefault;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
    @InjectView(R.id.container)
    FrameLayout container;
    private JSONObject obj;

    private DownloadHistoryResponse response;
    private DownloadAllHistoryRequest request;
    private DownloadHistoryListViewAdapter adapter;


    private boolean hasFootView;
    private boolean isFootViewNoMore = true;
    private int nums = 8;
    private int page = 1;
    private boolean hasNextPage = false;
    private int pageCount;
    private int lastVisiblePosition;
    private boolean isCanLoadMore = true;
    private View noLoading;
    private View loading;
    private TextView loading_tv;
    private TextView caution_tv;
    private ProgressBar bar;
    private int currentPosition = 0;
    private int top = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.initStateBar(this,
                getResources().getColor(R.color.statusbar_color));
        setContentView(R.layout.downloadhistory_layout);
        ButterKnife.inject(this);
        StateBarUtils.changeStatus(getWindow());
        adapter = new DownloadHistoryListViewAdapter(MainApplication.curContext);
        initLoadVIew();
        setListener();
        initData();
    }

    @Override
    public String getActivityName() {
        return null;
    }


    private void initLoadVIew() {

        findViewById(R.id.action_bar_no_tab).setVisibility(View.GONE);
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        noLoading = LayoutInflater.from(this).inflate(R.layout.footer_nomore_show, null);
        loading = LayoutInflater.from(this).inflate(R.layout.footer_loading_small, null);
        loading_tv = (TextView) loading.findViewById(R.id.loading_tv);
        caution_tv = (TextView) loading.findViewById(R.id.caution_tv);
        bar = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);
        addFootView();
        lvQueenlist.setAdapter(adapter);
    }

    private void setListener() {
        actionBack.setOnClickListener(this);
        headerEdt.setOnClickListener(this);
        lvQueenlist.setOnItemClickListener(this);
        lvQueenlist.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lastVisiblePosition <= lvQueenlist.getCount() - 1 && isCanLoadMore) {
                    isCanLoadMore = false;
                    if (hasNextPage) {
                        loading_tv.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.VISIBLE);
                        caution_tv.setVisibility(View.GONE);
                        addFootView();
                        loadData();
                    } else {
                        loading_tv.setVisibility(View.GONE);
                        bar.setVisibility(View.GONE);
                        caution_tv.setVisibility(View.VISIBLE);
                        addFootViewNoMore();
                        isCanLoadMore = true;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastVisiblePosition = lvQueenlist.getLastVisiblePosition();
                View v = lvQueenlist.getChildAt(0);
                top = (v == null) ? 0 : v.getTop();
                currentPosition = firstVisibleItem;
            }
        }));
    }

    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            lvQueenlist.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    private void removeFootView() {
        if (hasFootView && lvQueenlist != null) {
            lvQueenlist.removeFooterView(loading);
            hasFootView = false;
        }
    }


    /**
     * 添加加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        ViewGroup parent = (ViewGroup) loading.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        if (loading != null) {
            lvQueenlist.addFooterView(loading);
        }
        hasFootView = true;
    }

    private void initData() {
        if (response == null) {
            container.setVisibility(View.GONE);
            containerWait.setVisibility(View.VISIBLE);
            showWaiting();
            loadData();
        } else {
            container.setVisibility(View.VISIBLE);
            containerWait.setVisibility(View.GONE);
            hideWaiting();
            lvQueenlist.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        request = new DownloadAllHistoryRequest();
        request.nums = nums;
        request.page = page;
        request.userid = CommonUtils.queryUserId();
        x.http().post(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    obj = new JSONObject(result);
                    containerWait.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    hideWaiting();
                    if (obj.getInt("code") == 0) {
                        isShowDefaultView(false);
                        response = CommonUtils.getObject(result, DownloadHistoryResponse.class);
                        List<DownloadHistoryBean.DataBean.ItemBean> datas = response.data.getItem();
                        adapter.addData(datas);
                        lvQueenlist.setAdapter(adapter);
                        lvQueenlist.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                        lvQueenlist.setSelectionFromTop(currentPosition, top);

                        pageCount = response.data.getPageCount();
                        isCanLoadMore = true;
                        page++;
                        if (page <= pageCount) {
                            hasNextPage = true;
                        } else {
                            hasNextPage = false;
                        }
                        removeFootView();

                    }else{
                        isShowDefaultView(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                containerWait.setVisibility(View.GONE);
                hideWaiting();
                loadingFailed(new ReloadFunction() {
                    @Override
                    public void reload() {
                        loadData();
                    }
                });
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadHistoryBean.DataBean.ItemBean item = (DownloadHistoryBean.DataBean.ItemBean) adapter.getItem(position);
        if (item.getId() != null) {
            switch (item.getType()) {
                case 0:
                    if (item.getAd_picture() != null) {
                        UIUtils.gotoThemeDetail(item.getId(),item.getAd_picture());
                    }
                    break;
                case 1:
                    if (item.getAd_picture() != null) {
                        UIUtils.gotoFontDetail(item.getId(), item.getAd_picture(),false);
                    }
                    break;
                case 2:
                    if (item.getAd_picture() != null) {
                        UIUtils.gotoWallDetail(this, item.getId(), 1+"", item.getAd_picture());
                    }
                    break;
            }

        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_edt:
            case R.id.action_back:
                onBackPressed();
                break;
        }
    }

    public void isShowDefaultView(boolean isShowDefaultView) {
        if (!isShowDefaultView) {
            lvQueenlist.setVisibility(View.VISIBLE);
            rlDefault.setVisibility(View.GONE);
        } else {
            lvQueenlist.setVisibility(View.GONE);
            rlDefault.setVisibility(View.VISIBLE);
        }
    }
}
