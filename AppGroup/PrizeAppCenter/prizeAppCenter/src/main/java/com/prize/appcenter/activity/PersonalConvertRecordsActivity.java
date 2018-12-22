/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.ConvertRecordsData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarPointsActivity;
import com.prize.appcenter.ui.adapter.PersonalconvertRecordsAdapter;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 * Title:    个人中心-兑换记录
 * Desc:    2.0积分系统-兑换记录
 * Version:    应用市场2.0
 * Created by huangchangguo
 * on   2016/8/15  15:37
 * <p/>
 * Update Description:  更新描述
 * Updater:   更新者
 * Update Time:   更新时间
 */

public class PersonalConvertRecordsActivity extends ActionBarPointsActivity {
    private String TAG = "PersonalConvertRecordsActivity";
    private Context mContext;

    private ListView mRecordsListView;
    private View     reload_view;

    // 无更多内容加载
    private View noLoading = null;
    private View loading   = null;
    private boolean hasFootView;
    private boolean isFootViewNoMore = true;

    private int lastVisiblePosition;
    private boolean isLoadMore = true;

    // 分页请求的页数
    private       int     currentIndex = 1;
    public static String  tags         = "tags";
    protected     boolean isGame       = false;
    private PersonalconvertRecordsAdapter mAdapter;

    private String userId;
    private ConvertRecordsData mConvertRecordsData = null;
    private RelativeLayout      mNoRecordsDefalutRlyt;
    private Callback.Cancelable cancelable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_personal_convert_records);
        WindowMangerUtils.changeStatus(getWindow());
        mContext = this;
        rule.setVisibility(View.GONE);
        setTitle(getString(R.string.convert_records));
        initView();
        initData();
        initListener();
    }


    private void initListener() {

        mRecordsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));


    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lastVisiblePosition >= mRecordsListView.getCount() - 1
                    && isLoadMore) {
                isLoadMore = false;
                // 如果现在的页小于总共返回的页
                if (currentIndex <= mConvertRecordsData.pageCount) {
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

            lastVisiblePosition = mRecordsListView.getLastVisiblePosition();

        }
    };

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(this);

        mRecordsListView = (ListView) findViewById(R.id.convert_records_listview);
        mAdapter = new PersonalconvertRecordsAdapter(this);
        mRecordsListView.setAdapter(mAdapter);
        mNoRecordsDefalutRlyt = (RelativeLayout) findViewById(R.id.convert_records_defalutRlyt);

        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        View reload = inflater.inflate(R.layout.reload_layout, null);
        reload_view = reload.findViewById(R.id.reload_Llyt);

    }

    private void initData() {
        Person person = CommonUtils.queryUserPerson(mContext);
        if (person != null && person.getUserId() != null) {
            userId = person.getUserId();
        }
         requestData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            String orderId = data.getStringExtra("orderId");
            if (TextUtils.isEmpty(orderId))
                return;
            mAdapter.add2Set(Integer.parseInt(orderId));
        }
    }

    private void requestData() {

        if (currentIndex <= 1) {
            mRecordsListView.setVisibility(View.GONE);
        }
        RequestParams params = new RequestParams(Constants.GIS_URL
                + "/point/exchangerecord");

        params.addBodyParameter("accountId", userId);
        params.addBodyParameter("pageIndex", currentIndex + "");
        params.addBodyParameter("pageSize", Constants.PAGE_SIZE + "");

        // MD5加密校验key
        String sign = Verification.getInstance().getSign(params.getBodyParams());
        params.addBodyParameter("sign", sign);

        cancelable = XExtends.http().post(params,
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        reload_view.setVisibility(View.GONE);
                        removeFootView();
                        removeFootViewNoMore();
                        if (currentIndex <= 1) {
                            hideWaiting();
                            mRecordsListView.setVisibility(View.VISIBLE);
                        }
                        isLoadMore = true;
                        try {
                            String data = new JSONObject(result).getString("data");
                            mConvertRecordsData = new Gson().fromJson(data, ConvertRecordsData.class);
                            if (currentIndex <= 1) {
                                if (mConvertRecordsData.record == null || mConvertRecordsData.record.size() <= 0) {
                                    //没有记录，显示没有记录页
                                    mNoRecordsDefalutRlyt.setVisibility(View.VISIBLE);
                                    mRecordsListView.setVisibility(View.GONE);
                                }

                                mAdapter.setData(mConvertRecordsData.record);

                            } else {
                                //如果index大于1，则说明肯定有数据
                                mAdapter.addData(mConvertRecordsData.record);
                            }
                            currentIndex++;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        JLog.i(TAG, "requestData-ex=" + ex);
                        removeFootView();
                        hideWaiting();
                        // mWaiting.setVisibility(View.GONE);
                        if (currentIndex <= 1) {
//                            mRecordsListView.setVisibility(View.GONE);
//                            reload_view.setVisibility(View.VISIBLE);
                            loadingFailed(new ReloadFunction() {

                                @Override
                                public void reload() {
                                    requestData();
                                }

                            });
                        } else {
                            mRecordsListView.setVisibility(View.VISIBLE);
                            reload_view.setVisibility(View.GONE);
                            //	ToastUtils.showToast(R.string.net_error);
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


//    private void isShowReloadView(boolean showRelooadView) {
//
//        if (showRelooadView) {
//            hideWaiting();
//            reload_view.setVisibility(View.VISIBLE);
//        } else {
//            showWaiting();
//            reload_view.setVisibility(View.GONE);
//        }
//
//    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            mRecordsListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    /**
     * 移除无数据提示
     */
    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            mRecordsListView.removeFooterView(noLoading);
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
        mRecordsListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != mRecordsListView)) {
            mRecordsListView.removeFooterView(loading);
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
        if (mAdapter != null) {
            mAdapter.clearSet();
        }
    }
}