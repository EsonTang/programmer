/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.datamgr.TopicManager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;

/**
 **
 * 专题详情
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class TopicDetailActivity extends ActionBarTabActivity {
    private final String TAG = "TopicDetailActivity";
    private ListView mListView;
    private GameListAdapter mTopicListAdapter;
    private TopicManager mTopicManager;

    private String topicId;
    private View headView;
    private TextView description_Tv;
    private ImageView topic_detail_Iv;
    private TopicItemBean bean;
    private View footView;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_topic_listview);
        WindowMangerUtils.changeStatus(getWindow());
        LayoutInflater inflater = LayoutInflater.from(this);
        headView = inflater.inflate(R.layout.activity_topic_detail_head_view,
                null);
        headView.setClickable(false);
        headView.setEnabled(false);
        footView=inflater.inflate(R.layout.footer_nomore_show, null);
        Bundle b = getIntent().getExtras();
        if (b != null && b.getSerializable("bean") != null) {
            bean = (TopicItemBean) b.getSerializable("bean");
            title = bean.title;
            topicId = bean.id;
        }
        if(getIntent()!=null){
            from = getIntent().getStringExtra(Constants.FROM);
        }
        findViewById();
        mTopicListAdapter = new GameListAdapter(this,null,null);
        mTopicListAdapter.setDownlaodRefreshHandle();
        mToken = AIDLUtils.bindToService(this, this);
        init();

        setListener();

        initActionBar();
    }

    private void setListener() {
        OnScrollListener mOnScrollListener = new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        };

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                if (position == 0
                        || mTopicListAdapter.getItem(position - 1) == null) {
                    return;
                }
                UIUtils.gotoAppDetail(mTopicListAdapter.getItem(position - 1),
                        mTopicListAdapter.getItem(position - 1).id,
                        TopicDetailActivity.this);
                MTAUtil.onDetailClick(TopicDetailActivity.this,
                        mTopicListAdapter.getItem(position - 1).name,
                        mTopicListAdapter.getItem(position - 1).packageName);
                MTAUtil.onTopicDetailPositionClick(topicId+"_"+position);
            }
        });

    }


    private void init() {
        description_Tv.setText(bean.description);
        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }
        if (topic != null) {
            ImageLoader.getInstance().displayImage(topic.imageUrl,
                    topic_detail_Iv, UILimageUtil.getTopicListUILoptions(),
                    null);
        }
        mListView.addHeaderView(headView);
        mListView.setAdapter(mTopicListAdapter);
        getData();

    }


    private void findViewById() {
        description_Tv = (TextView) headView.findViewById(R.id.description_Tv);
        topic_detail_Iv = (ImageView) headView
                .findViewById(R.id.topic_detail_Iv);
        mListView = (ListView) findViewById(android.R.id.list);
    }

    @Override
    public String getActivityName() {
        return "TopicDetailActivity";
    }

    private TopicItemBean topic;

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        switch (what) {
            case TopicManager.GET_TOPIC_DETAIL_SUCCESS:// 请求成共返回
                if (mListView.getFooterViewsCount() <= 0) {
                    mListView.addFooterView(footView);
                }
                PrizeAppsTypeData data = (PrizeAppsTypeData) obj;
                if (data != null) {
                    topic = data.topic;
                }
                if (bean != null && topic != null && data != null) {

                    if (topic != null) {
                        ImageLoader.getInstance().displayImage(topic.imageUrl,
                                topic_detail_Iv,
                                UILimageUtil.getTopicListUILoptions(), null);
                        if (!TextUtils.isEmpty(topic.description)) {
                            description_Tv.setText(topic.description);
                        }

                        if (TextUtils.isEmpty(title)) {
                            mTitle.setText(topic.title);
                        }
                    }
                }

                if (mTopicListAdapter != null) {
                    mTopicListAdapter.setData(data.apps);
                    mTopicListAdapter.setStyle(data.topic);
                }
                break;

            case TopicManager.GET_TOPIC_DETAIL_FAILE:
                if (mTopicListAdapter != null && mTopicListAdapter.getCount() <= 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            getData();
                        }

                    });
                }
                break;
        }
    }

    private void getData() {
        if (mTopicManager == null) {
            mTopicManager = new TopicManager(this, null, topicId + "");
        }
        mTopicManager.getTopicDetailData(TAG);
    }


    public void onDestroy() {
        super.onDestroy();
        BaseApplication.cancelPendingRequests(TAG);
        if (mTopicManager != null) {
            mTopicManager.setNullListener();
        }
        if (mTopicListAdapter != null) {
            mTopicListAdapter.removeDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    protected void onStart() {
        if (mTopicListAdapter != null) {
            mTopicListAdapter.setIsActivity(true);
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        if (mTopicListAdapter != null) {
            mTopicListAdapter.setIsActivity(false);
        }
        super.onPause();
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mTopicListAdapter.setDownlaodRefreshHandle();
    }
    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            UIUtils.gotoActivity(MainActivity.class,TopicDetailActivity.this);
        }
        super.onBackPressed();
    }
}
