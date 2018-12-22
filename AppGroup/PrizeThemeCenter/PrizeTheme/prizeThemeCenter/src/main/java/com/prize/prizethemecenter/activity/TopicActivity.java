package com.prize.prizethemecenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.util.DataStoreUtils;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.TopicData.TopicBean;
import com.prize.prizethemecenter.request.BaseRequest;
import com.prize.prizethemecenter.request.TopicFontRequest;
import com.prize.prizethemecenter.request.TopicRequest;
import com.prize.prizethemecenter.request.TopicWallRequest;
import com.prize.prizethemecenter.response.TopicResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.TopicListAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 主題专题页面
 * Created by pengy on 2016/9/7.
 */
public class TopicActivity extends ActionBarNoTabActivity {

    private static final String TAG = "TopicActivity";
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;

    private ListView topic_LV;
    private TopicListAdapter adapter;
    /**
     * 不同专题请求 1.theme 2.wallpaper 3.font
     */
    private TopicRequest request;
    private TopicWallRequest wallRequest;
    private TopicFontRequest fontRequest;

    private TopicResponse response;
    private Callback.Cancelable mHandler;
    private String page;
    private boolean isPushBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.topic_layout);
        ButterKnife.inject(this);
        WindowMangerUtils.changeStatus(getWindow());

        setTitle(R.string.recommend_topic);
        page = getIntent().getStringExtra("page");
        if ("theme".equals(page)) {
            MTAUtil.onClickTopic("主题");
        } else if ("wallpaper".equals(page)) {
            MTAUtil.onClickTopic("壁纸");
        } else {
            MTAUtil.onClickTopic("字体");
        }
        topic_LV = (ListView) findViewById(R.id.topic_LV);
        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            initPushData();
        }
        adapter = new TopicListAdapter(this);
        topic_LV.setAdapter(adapter);
        initLoadVIew();
        setListener();
        initData();
    }

    private void initPushData() {
        XGPushConfig.enableDebug(this, true);
        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            isPushBack = true;
            Log.d(TAG, "initPushData: "+pushJson);
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    page = jsonObject.getString("page").trim();
                    Log.d(TAG, "initPushData: "+page+"---"+page);
                } catch (JSONException e    ) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initLoadVIew() {
        View someView = this.findViewById(R.id.topic_LV);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        loadingView.setPadding(0,300,0,0);
        loadingView.setGravity(Gravity.CENTER_HORIZONTAL);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    private void setListener() {
        topic_LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TopicBean b = (TopicBean) parent.getAdapter().getItem(position);
//                UIUtils.onClickTopicItem(b.id, TopicActivity.this, page);
                UIUtils.onClickTopicToDetail(b.id, TopicActivity.this, page,isPushBack);
                if ("theme".equals(page)) {
                    MTAUtil.onClickTopicThemeList(b.title);
                } else if ("wallpaper".equals(page)) {
                    MTAUtil.onClickTopicWallList(b.title);
                } else {
                    MTAUtil.onClickTopicFontList(b.title);
                }
            }
        });

        topic_LV.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem,
                             int visibleItemCouintnt, int totalItemCount) {
        }
    };

    private void initData() {
        if (0 == adapter.getCount()) {
            containerWait.setVisibility(View.VISIBLE);
            topic_LV.setVisibility(View.INVISIBLE);
            loadWathData();
        } else {
            containerWait.setVisibility(View.INVISIBLE);
            topic_LV.setVisibility(View.VISIBLE);
        }
    }

    private void loadWathData() {
        if ("theme".equals(page)) {
            request = new TopicRequest();
            loadData(request);
        } else if ("wallpaper".equals(page)) {
            wallRequest = new TopicWallRequest();
            loadData(wallRequest);
        } else {
            fontRequest = new TopicFontRequest();
            loadData(fontRequest);
        }
    }

    private void loadData(BaseRequest request) {


        mHandler = x.http().post(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        response = CommonUtils.getObject(result,
                                TopicResponse.class);
                        containerWait.setVisibility(View.INVISIBLE);
                        topic_LV.setVisibility(View.VISIBLE);
                        ArrayList<TopicBean> itemBeen = response.data.topics;

                        if (itemBeen != null) {
                            adapter.addData(itemBeen);
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
                        initData();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.cancel();
        }
        XGPushManager.onActivityStoped(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
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
}
