package com.prize.prizethemecenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.util.DataStoreUtils;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.bean.TopicDetailData.TopicDetailHead;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.FontTopicDetailRequest;
import com.prize.prizethemecenter.request.TopicDetailRequest;
import com.prize.prizethemecenter.request.WallTopicDetailRequest;
import com.prize.prizethemecenter.response.TopicDetailResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarActivity;
import com.prize.prizethemecenter.ui.adapter.TopicFontListAdapter;
import com.prize.prizethemecenter.ui.adapter.TopicThemeListAdapter;
import com.prize.prizethemecenter.ui.adapter.TopicWallListAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.TopicFadingActionBarHelper;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 专题详情页
 * Created by pengy on 2016/9/5.
 */
public class TopicDetailActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = "TopicDetailActivity";
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    private GridViewWithHeaderAndFooter topic_gv;

    /**
     * 不同的适配器 1.theme 2.wallpaper 3.font
     **/
    private TopicThemeListAdapter adapter;
    private TopicWallListAdapter wallListAdapter;
    private TopicFontListAdapter fontListAdapter;

    /**
     * 不同的详情请求 1. theme 2. wallpapew 3.font
     */
    private TopicDetailRequest request;
    private WallTopicDetailRequest wallRequest;
    private FontTopicDetailRequest fontRequest;

    private TopicDetailResponse response;
    private Callback.Cancelable mHandler;

    private int specialId;
    private int pageIndex = 1;
    private int pageSize = 9;
    private int pageCount;

    private boolean isCanLoadMore;
    private int lastVisiblePosition;
    private boolean hasNextPage = false;
    private boolean isPushBack = false;

    // 无更多内容加载
    private View loading = null;
    private boolean hasFootView;

    private TextView loading_tv;
    private ProgressBar bar;
    private TextView more_topic_Btn;

    private View headerView;

    private ImageView topic_detail_Iv;
    private TextView description_Tv;
    private ImageButton action_bar_back;
    private TextView action_bar_title;
    private ImageButton bar_search;

    private TopicFadingActionBarHelper mFadingActionBarHelper;
    private RelativeLayout topic_title_actionbar;

    private int imgHeight;
    private int actionBarHeight;

    /**
     * 属于哪个page页 1.theme 2。wallpaper 3.font
     */
    private String page;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.activity_topic_layout);
        ButterKnife.inject(this);

        if (getIntent() != null) {
            if (getIntent().getStringExtra("specialId")!=null&&getIntent().getStringExtra("page")!=null){
                specialId = Integer.parseInt(getIntent().getStringExtra("specialId"));
                page = getIntent().getStringExtra("page");
                isPushBack = getIntent().getBooleanExtra("isPush",false);
            }
        }
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            initPushData();
        }
        setIsDetail(true);
        init();
        setListener();
        initLoadVIew();
        loadData();

        mFadingActionBarHelper = new TopicFadingActionBarHelper(this,
                getWindow(), topic_title_actionbar, getResources().getDrawable(
                R.drawable.actionbar_bg));
    }

    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        loadingView.setPadding(0,0,0,0);
        loadingView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void initActionBar() {
        enableSlideLayout(false);
        action_bar_back = (ImageButton) findViewById(R.id.action_bar_back_topic);
        action_bar_title = (TextView) findViewById(R.id.action_bar_title_topic);
        bar_search = (ImageButton) findViewById(R.id.bar_search_topic);

        action_bar_back.setOnClickListener(this);
        bar_search.setOnClickListener(this);
//        action_bar_title.setText(R.string.recommend_topic);
    }

    private void initPushData() {
        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            isPushBack = true;
            Log.d("TPush", "TopicDetailActivity：" + pushJson);
            String[] value = null;
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    Iterator<String> keys = jsonObject.keys();
                    String key = (String) keys.next();
                    if (key.equals("theme_specialId")){
                        value = jsonObject.getString("theme_specialId").trim().split("_");
                    }else if (key.equals("wallpaper_specialId")){
                        value = jsonObject.getString("wallpaper_specialId").trim().split("_");
                    }else if (key.equals("font_specialId")){
                        value = jsonObject.getString("font_specialId").trim().split("_");
                    }
                    if (value != null) {
                        page = value[0];
                        specialId = Integer.parseInt(value[1]);
                    }
                    Log.d(TAG, "initPushData: "+page+"---"+page+"---"+specialId+"---"+specialId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_back_topic:
                onBackPressed();
                break;
            case R.id.bar_search_topic:
                UIUtils.goSearchActivity(TopicDetailActivity.this);
                break;
            case R.id.more_topic_Btn:
                Intent intent = new Intent(TopicDetailActivity.this,
                        TopicActivity.class);
                intent.putExtra("page", page);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(this);
        //footView
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        loading_tv = (TextView) loading.findViewById(R.id.loading_tv);
        bar = (ProgressBar) loading.findViewById(R.id.progress_loading_loading);
        more_topic_Btn = (TextView) loading.findViewById(R.id.more_topic_Btn);
        more_topic_Btn.setOnClickListener(this);

        headerView = inflater.inflate(R.layout.activity_topic_detail_head_view, null);

        description_Tv = (TextView) headerView.findViewById(R.id.description_Tv);
        topic_detail_Iv = (ImageView) headerView.findViewById(R.id.topic_detail_Iv);

        topic_gv = (GridViewWithHeaderAndFooter) findViewById(R.id.classify_id_gv);
        topic_gv.addHeaderView(headerView);
        addFootView();

        adapter = new TopicThemeListAdapter(this);
        adapter.setIsActivity(true);
        adapter.setDownlaodRefreshHandle();
        wallListAdapter = new TopicWallListAdapter(this);
        wallListAdapter.setIsActivity(true);
        wallListAdapter.setDownlaodRefreshHandle();
        fontListAdapter = new TopicFontListAdapter(this);
        fontListAdapter.setIsActivity(true);
        fontListAdapter.setDownlaodRefreshHandle();
        if ("wallpaper".equals(page)) {
            topic_gv.setNumColumns(3);
            topic_gv.setAdapter(wallListAdapter);
        } else if ("theme".equals(page)) {
            topic_gv.setNumColumns(3);
            topic_gv.setAdapter(adapter);
        } else {
            topic_gv.setNumColumns(2);
            topic_gv.setAdapter(fontListAdapter);
        }
    }

    private void setListener() {

        topic_title_actionbar = (RelativeLayout) findViewById(R.id.action_bar_topic);
        topic_title_actionbar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // TODO Auto-generated method stub
                        actionBarHeight = topic_title_actionbar.getHeight();
                    }
                });

        topic_detail_Iv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // TODO Auto-generated method stub
                        imgHeight = headerView.getHeight();
                    }
                });

        topic_gv.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

        topic_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到详情页
                if ("font".equals(page)) {
                    ThemeItemBean fontBean = fontListAdapter.getItem(position);
                    if (fontBean != null&&fontBean.ad_pictrue!=null)
                        UIUtils.gotoFontDetail(fontBean.id, fontBean.ad_pictrue,isPushBack);
                    if (isPushBack) {
                        TopicDetailActivity.this.finish();
                    }
                } else if ("wallpaper".equals(page)) {
                    ThemeItemBean wallBean = wallListAdapter.getItem(position);
                    if(wallBean!=null&&wallBean.wallpaper_pic!=null){
                        String p = UILimageUtil.getPicPath(TopicDetailActivity.this, wallBean.wallpaper_pic);
                        UIUtils.gotoWallDetail(TopicDetailActivity.this, wallBean.id, wallBean.wallpaper_type, p);
                    }
                } else {
                    ThemeItemBean bean = adapter.getItem(position);
                    if (bean != null&&bean.ad_pictrue!=null)
                        UIUtils.gotoThemeDetail(bean.id, bean.ad_pictrue);
                }
            }
        });

    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (!isCanLoadMore) {
                return;
            }
            if (lastVisiblePosition >= topic_gv.getCount() - 1
                    && isCanLoadMore) {
                isCanLoadMore = false;
                if (hasNextPage) {
                    loading_tv.setVisibility(View.VISIBLE);
                    bar.setVisibility(View.VISIBLE);
                    more_topic_Btn.setVisibility(View.GONE);
                    addFootView();
                    loadWitchData();
                } else {
                    loading_tv.setVisibility(View.GONE);
                    bar.setVisibility(View.GONE);
                    more_topic_Btn.setVisibility(View.VISIBLE);
                    addFootView();
                    isCanLoadMore = true;
                }
            }
        }

        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = topic_gv.getLastVisiblePosition();

            if (firstVisibleItem == 0) {
                if (actionBarHeight != 0 && imgHeight != 0) {
                    float progress = (float) getScrollY() / (imgHeight - actionBarHeight);
                    if (progress > 1f)
                        progress = 1f;
                    int alpha = (int) (255 * progress);
                    mFadingActionBarHelper.setActionBarAlpha((int) (255 * progress));
                    WindowMangerUtils.changeStatus2White(getWindow());
                }
            } else {
                mFadingActionBarHelper.setActionBarAlpha((int) (255));
                WindowMangerUtils.changeStatus(getWindow());
            }
        }
    };

    private void loadData() {
        if (0 == adapter.getCount()) {
            containerWait.setVisibility(View.VISIBLE);
            topic_gv.setVisibility(View.INVISIBLE);
            loadWitchData();
        } else {
            containerWait.setVisibility(View.INVISIBLE);
            topic_gv.setVisibility(View.VISIBLE);
        }
    }

    private void loadWitchData() {
        if ("theme".equals(page)) {
            request = new TopicDetailRequest();
            request.specialId = specialId;
            request.pageIndex = pageIndex;
            request.pageSize = pageSize;
            loadClassifyData(request);
        } else if ("wallpaper".equals(page)) {
            wallRequest = new WallTopicDetailRequest();
            wallRequest.specialId = specialId;
            wallRequest.pageIndex = pageIndex;
            wallRequest.pageSize = pageSize;
            loadClassifyData(wallRequest);
        } else {
            fontRequest = new FontTopicDetailRequest();
            fontRequest.specialId = specialId;
            fontRequest.pageIndex = pageIndex;
            fontRequest.pageSize = pageSize;
            loadClassifyData(fontRequest);
        }
    }

    private void loadClassifyData(final BaseRequest request) {

        mHandler = x.http().post(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        response = CommonUtils.getObject(result,
                                TopicDetailResponse.class);

                        containerWait.setVisibility(View.INVISIBLE);
                        topic_gv.setVisibility(View.VISIBLE);

                        ArrayList<ThemeItemBean> itemBeen = response.data.items;
                        TopicDetailHead specials = response.data.specials;
                        if (specials != null && !TextUtils.isEmpty(specials.intro)) {
                            description_Tv.setText(specials.intro);
                        }
                        if (specials != null) {
                            ImageLoader.getInstance().displayImage(
                                    specials.big_image, topic_detail_Iv,
                                    UILimageUtil.getTopicDetailLoptions(), null);
                        }

                        if (!TextUtils.isEmpty(specials.name)) {
                            action_bar_title.setText(specials.name);
                        }

                        pageCount = response.data.pageCount;
                        hideWaiting();
                        removeFootView();
                        if (itemBeen != null) {
                            if ("wallpaper".equals(page)) {
                                wallListAdapter.addData(itemBeen);
                            } else if ("font".equals(page)) {
                                fontListAdapter.addData(itemBeen);
                            } else {
                                adapter.addData(itemBeen);
                            }
                        }
                        isCanLoadMore = true;
                        if (pageIndex == 1 && itemBeen.size() < 4) {
                            loading_tv.setVisibility(View.GONE);
                            bar.setVisibility(View.GONE);
                            more_topic_Btn.setVisibility(View.VISIBLE);
                            addFootView();
                        }
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
            topic_gv.addFooterView(loading);
        }
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != topic_gv)) {
            topic_gv.removeFooterView(loading);
            hasFootView = false;
        }
    }

    public int getScrollY() {
        View c = topic_gv.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = topic_gv.getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.cancel();
        }
        if(adapter !=null ){
            adapter.setIsActivity(false);
            adapter.removeDownLoadHandler();
        }
        if(wallListAdapter !=null ){
            wallListAdapter.setIsActivity(false);
            wallListAdapter.removeDownLoadHandler();
        }
        if(fontListAdapter !=null ){
            fontListAdapter.setIsActivity(false);
            fontListAdapter.removeDownLoadHandler();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPushBack){
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                // 监控返回键
                UIUtils.gotoActivity(MainActivity.class);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
