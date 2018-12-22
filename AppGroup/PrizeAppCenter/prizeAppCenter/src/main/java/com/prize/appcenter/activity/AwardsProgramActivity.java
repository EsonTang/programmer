/*
 * 版权所有©2017,深圳市铂睿智恒科技有限公司
 * <p/>
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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AwardsProgramTypeData;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarActivity;
import com.prize.appcenter.ui.adapter.AwardsProgramAdapter;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;


/**
 *
 * 有奖活动Activity
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class AwardsProgramActivity extends ActionBarActivity {
    private String TAG = "AwardsProgramActivity";
    private ListView awards_program_Lv;
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;
    private boolean isFootViewNoMore = true;
    private int lastVisiblePosition;
    private boolean isLoadMore = true;

    // 分页请求的页数
    private int currentIndex = 1;
    private AwardsProgramAdapter mAdapter;

    private RelativeLayout mNoRecordsDefalutRlyt;
    private Callback.Cancelable cancelable;
    private AwardsProgramTypeData mAwardsProgramTypeData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_awards_program);
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        initView();
        initListener();
        requestData();
    }


    private void initListener() {

        awards_program_Lv.setOnScrollListener(mOnScrollListener);
        awards_program_Lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter == null || mAdapter.getItem(position) == null || TextUtils.isEmpty(mAdapter.getItem(position).url))
                    return;
                Intent intent = new Intent(AwardsProgramActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.P_URL, mAdapter.getItem(position).url);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
            }
        });


    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lastVisiblePosition >= awards_program_Lv.getCount() - 1
                    && isLoadMore) {
                isLoadMore = false;
                // 如果现在的页小于总共返回的页
                if (currentIndex <= mAwardsProgramTypeData.pageCount) {
                    addFootView();
                    requestData();
                } else {
                    addFootViewNoMore();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            lastVisiblePosition = awards_program_Lv.getLastVisiblePosition();

        }
    };

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(this);

        awards_program_Lv = (ListView) findViewById(R.id.awards_program_Lv);
        mAdapter = new AwardsProgramAdapter(this);
        awards_program_Lv.setAdapter(mAdapter);
        mNoRecordsDefalutRlyt = (RelativeLayout) findViewById(R.id.convert_records_defalutRlyt);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);

    }


    /**
     * 请求有奖活动数据
     */
    private void requestData() {
        if (currentIndex <= 1) {
            awards_program_Lv.setVisibility(View.GONE);
        }
        RequestParams params = new RequestParams(Constants.GIS_URL
                + "/web/activitylist");
        params.addBodyParameter("pageIndex", currentIndex + "");
        params.addBodyParameter("pageSize", String.valueOf(5));
        cancelable = XExtends.http().post(params,
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        JLog.i(TAG, "onSuccess-result=" + result);
                        removeFootView();
                        removeFootViewNoMore();
                        if (currentIndex <= 1) {
                            hideWaiting();
                            awards_program_Lv.setVisibility(View.VISIBLE);
                        }
                        isLoadMore = true;
                        try {
                            String res = new JSONObject(result).getString("data");

                            mAwardsProgramTypeData = new Gson().fromJson(res, AwardsProgramTypeData.class);
                            if (currentIndex <= 1) {
                                if (mAwardsProgramTypeData.list == null || mAwardsProgramTypeData.list.size() <= 0) {
                                    //没有记录，显示没有记录页
                                    mNoRecordsDefalutRlyt.setVisibility(View.VISIBLE);
                                    awards_program_Lv.setVisibility(View.GONE);
                                }
                                mAdapter.setData(mAwardsProgramTypeData.list);

                            } else {
                                //如果index大于1，则说明肯定有数据
                                //awards_program_Lv.setVisibility(View.VISIBLE);
                                mAdapter.addData(mAwardsProgramTypeData.list);
                            }
                            JLog.i(TAG, "   currentIndex:" + currentIndex);
                            currentIndex++;
                            if (currentIndex > mAwardsProgramTypeData.pageCount) {
                                addFootViewNoMore();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        JLog.i(TAG, "requestData-ex=" + ex);
                        removeFootView();
                        hideWaiting();
                        if (currentIndex <= 1) {
                            loadingFailed(new ReloadFunction() {

                                @Override
                                public void reload() {
                                    requestData();
                                }

                            });
                        } else {
                            awards_program_Lv.setVisibility(View.VISIBLE);
                        }
                        isLoadMore = true;
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
    }


    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            awards_program_Lv.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    /**
     * 移除无数据提示
     */
    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            awards_program_Lv.removeFooterView(noLoading);
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
        awards_program_Lv.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != awards_program_Lv)) {
            awards_program_Lv.removeFooterView(loading);
            hasFootView = false;
        }
    }

    @Override
    public String getActivityName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    @Override
    protected void initActionBar() {
        enableSlideLayout(false);
        findViewById(R.id.action_bar_no_tab).setVisibility(View.VISIBLE);
        findViewById(R.id.action_bar_feedback).setVisibility(View.GONE);
        TextView topTitle_Tv = (TextView) findViewById(R.id.action_bar_title);
        topTitle_Tv.setText(R.string.awards_program);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    // 返回
                    case R.id.action_bar_back:
                        onBackPressed();
                        break;
                }
            }
        };
        findViewById(R.id.action_bar_back).setOnClickListener(onClickListener);

    }
}