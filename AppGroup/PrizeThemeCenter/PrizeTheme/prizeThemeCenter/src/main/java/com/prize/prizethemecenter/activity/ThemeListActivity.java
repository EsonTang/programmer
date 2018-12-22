package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.ClassifyIdRequest;
import com.prize.prizethemecenter.request.ClassifyIdWallRequest;
import com.prize.prizethemecenter.request.RankFontRequest;
import com.prize.prizethemecenter.request.RankRequest;
import com.prize.prizethemecenter.response.ThemeListResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.FontAdapter;
import com.prize.prizethemecenter.ui.adapter.ThemeListAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 主題分类ID
 * Created by pengy on 2016/9/5.
 */
public class ThemeListActivity extends ActionBarNoTabActivity {

    private static final String TAG = "ThemeListActivity";
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    private GridViewWithHeaderAndFooter classify_gv;
    /**
     * 分类，主题排行，专题的adapter
     */
    private ThemeListAdapter adapter;
    /**
     * 排行字体列表的adapter
     */
    private FontAdapter fontAdapter;

    /**
     * 分类的请求
     */
    private ClassifyIdRequest request;
    private ClassifyIdWallRequest wallRequest;
    /**
     * 排行的请求
     */
    private RankRequest rankRequest;
    private RankFontRequest fontRankRequest;

    private ThemeListResponse response;
    private Callback.Cancelable mHandler;
    private String rankType = "payList";
    private int typeId;
    private int pageIndex = 1;
    private int pageSize = 9;
    private int pageCount;
    /**
     * 是否加载更多
     */
    private boolean isCanLoadMore;
    private int lastVisiblePosition;
    private boolean hasNextPage = false;

    // 无更多内容加载
    private View loading = null;
    private boolean hasFootView;

    private TextView loading_tv;
    private TextView caution_tv;
    private ProgressBar bar;
    /**
     * 属于1.分类 2。排行 3.本地 列表
     */
    private String from;

    /**
     * 属于哪个page页 1.theme 2。wallpaper 3.font
     */
    private String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.classify_byid_layout);
        ButterKnife.inject(this);
        WindowMangerUtils.changeStatus(getWindow());

        if (getIntent() != null) {
            from = getIntent().getStringExtra("from");
            page = getIntent().getStringExtra("page");
            if (from.equals("classify")) {
                setTitle(getIntent().getStringExtra("name"));
                typeId = Integer.parseInt(getIntent().getStringExtra("typeId"));
                setSearchGone();
            } else if (from.equals("rank") && "theme".equals(page)) {
                setTitle(R.string.theme_rank);
                MTAUtil.onClickRanking("主题");
            } else if (from.equals("rank") && "font".equals(page)) {
                setTitle(R.string.font_rank);
                MTAUtil.onClickRanking("字体");
            }
        }
        View someView = this.findViewById(R.id.classify_id_gv);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));
        LayoutInflater inflater = LayoutInflater.from(this);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        loading_tv = (TextView) loading.findViewById(R.id.loading_tv);
        caution_tv = (TextView) loading.findViewById(R.id.caution_tv);
        bar = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);

        classify_gv = (GridViewWithHeaderAndFooter) findViewById(R.id.classify_id_gv);
        addFootView();
        adapter = new ThemeListAdapter(this, false);
        adapter.setDownlaodRefreshHandle();

        if (from.equals("rank") && "font".equals(page)) {
            // 字体分类
            fontAdapter = new FontAdapter(this, false);
            fontAdapter.setDownlaodRefreshHandle();
            classify_gv.setNumColumns(2);
            classify_gv.setAdapter(fontAdapter);
        } else {
            classify_gv.setNumColumns(3);
            classify_gv.setAdapter(adapter);
        }
        setListener();
        initLoadVIew();
        loadData();
    }

    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        loadingView.setPadding(0,300,0,0);
        loadingView.setGravity(Gravity.CENTER_HORIZONTAL);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }
    private void setListener() {
        classify_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到详情页
                if ("font".equals(page)) {
                    ThemeItemBean fontBean = fontAdapter.getItem(position);
                    if (fontBean == null)
                        return;
                    if (fontBean.ad_pictrue != null) {
                        UIUtils.gotoFontDetail(fontBean.id, fontBean.ad_pictrue,false);
                    }
                } else if ("wallpaper".equals(page)) {
                    ThemeItemBean wallBean = adapter.getItem(position);
                    if (wallBean == null)
                        return;
                    if (wallBean.wallpaper_pic != null) {
                        String p = UILimageUtil.getPicPath(ThemeListActivity.this, wallBean.wallpaper_pic);
                        UIUtils.gotoWallDetail(ThemeListActivity.this, wallBean.id, wallBean.wallpaper_type, p);
                    }
                } else {
                    ThemeItemBean bean = adapter.getItem(position);
                    if (bean == null)
                        return;
                    if (bean.ad_pictrue != null) {
                        UIUtils.gotoThemeDetail(bean.id, bean.ad_pictrue);
                    }
                }
            }
        });

        classify_gv.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

    }


    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (!isCanLoadMore) {
                return;
            }
            if (lastVisiblePosition >= classify_gv.getCount() - 1
                    && isCanLoadMore) {
                isCanLoadMore = false;
                if (hasNextPage) {
                    loading_tv.setVisibility(View.VISIBLE);
                    bar.setVisibility(View.VISIBLE);
                    caution_tv.setVisibility(View.GONE);
                    addFootView();
                    loadWhatData();
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
            lastVisiblePosition = classify_gv.getLastVisiblePosition();
        }
    };

    private void loadData() {
        if (0 == adapter.getCount()) {
            containerWait.setVisibility(View.VISIBLE);
            classify_gv.setVisibility(View.INVISIBLE);
            loadWhatData();
        } else {
            containerWait.setVisibility(View.INVISIBLE);
            classify_gv.setVisibility(View.VISIBLE);
        }
    }

    private void loadWhatData() {

        if (from.equals("classify") && "theme".equals(page)) {
            request = new ClassifyIdRequest();
            request.typeId = typeId;
            request.pageIndex = pageIndex;
            request.pageSize = pageSize;
            loadClassifyData(request);
        } else if (from.equals("classify") && "wallpaper".equals(page)) {
            wallRequest = new ClassifyIdWallRequest();
            wallRequest.typeId = typeId;
            wallRequest.pageIndex = pageIndex;
            wallRequest.pageSize = pageSize;
            loadClassifyData(wallRequest);
        } else if (from.equals("rank") && "theme".equals(page)) {
            rankRequest = new RankRequest();
            rankRequest.pageIndex = pageIndex;
            rankRequest.pageSize = pageSize;
            loadClassifyData(rankRequest);
        } else if (from.equals("rank") && "font".equals(page)) {
            fontRankRequest = new RankFontRequest();
            fontRankRequest.pageIndex = pageIndex;
            fontRankRequest.pageSize = pageSize;
            loadClassifyData(fontRankRequest);
        }
    }

    private void loadClassifyData(BaseRequest request) {

        mHandler = x.http().post(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        response = CommonUtils.getObject(result,
                                ThemeListResponse.class);
                        ArrayList<ThemeItemBean> itemBeen = response.data.item;
                        pageCount = response.data.pageCount;

                        containerWait.setVisibility(View.INVISIBLE);
                        classify_gv.setVisibility(View.VISIBLE);
                        removeFootView();

                        if (itemBeen.size() > 0) {
                            if (from.equals("rank") && "font".equals(page)) {
                                fontAdapter.addData(itemBeen);
                            } else {
                                adapter.setType(page);
                                adapter.addData(itemBeen);
                            }
                        }
                        isCanLoadMore = true;
                        pageIndex++;
                        if (pageIndex <= pageCount) {
                            hasNextPage = true;
                        } else {
                            hasNextPage = false;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override


            public void onError(Throwable ex, boolean isOnCallback) {
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
    public String getActivityName() {
        return null;
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
            classify_gv.addFooterView(loading);
        }
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != classify_gv)) {
            classify_gv.removeFooterView(loading);
            hasFootView = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.cancel();
        }
        if(adapter != null){
            adapter.removeDownLoadHandler();
        }
        if(fontAdapter!=null){
            fontAdapter.removeDownLoadHandler();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(adapter!=null){
//            adapter.notifyDataSetChanged();
//            adapter.setDownlaodRefreshHandle();
//        }
//        if(fontAdapter!=null){
//            fontAdapter.notifyDataSetChanged();
//            fontAdapter.setDownlaodRefreshHandle();
//        }
    }
}
