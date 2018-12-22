/*
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

package com.prize.appcenter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.HotKeyBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.search.HotAppData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.fragment.base.BaseFragment;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarActivity.ReloadFunction;
import com.prize.appcenter.ui.adapter.SearchHotAppAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.SearchFlowLayout;
import com.prize.appcenter.ui.widget.flow.FlowTagLayout;
import com.prize.appcenter.ui.widget.flow.OnTagClickListener;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * *
 * 搜索初始化界面
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchOriginalFragment extends BaseFragment {
    private ScrollView mScrollView;
    private TextView clear_history_Tv;
    private TextView change_data_Tv;
    private RelativeLayout search_Rlyt;
    private View root;
    private SearchActivity activity;
    private SearchHotAppAdapter mSearchHotAppAdapter;
    private FlowTagLayout mHotApp_ftl;
    private Cancelable mCancelable;
    private View reloadView;
    private SearchFlowLayout mSearchFlowLayout;
//    private View view_line;
    private HotAppData data;
    /**
     * 是否是第一页需要统计总数  2.6 add
     **/
    private boolean isOriginalPage = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_original, container,
                false);
        activity = (SearchActivity) getActivity();
        findViewById();
        init();
        setListener();
        return root;
    }

    private void changeData() {
        requestRandchange();
    }

    @Override
    protected void findViewById() {
        mSearchFlowLayout = (SearchFlowLayout) root
                .findViewById(R.id.tag_container);
//        view_line = root
//                .findViewById(R.id.view_line);
        reloadView = root.findViewById(R.id.reload_Llyt);
        waitView = root.findViewById(R.id.loading_Llyt_id);
        mScrollView = (ScrollView) root.findViewById(R.id.mListView);
        search_Rlyt = (RelativeLayout) root.findViewById(R.id.search_Rlyt);
        clear_history_Tv = (TextView) root.findViewById(R.id.clear_history_Tv);
        change_data_Tv = (TextView) root.findViewById(R.id.change_data_Tv);
        mHotApp_ftl = (FlowTagLayout) root.findViewById(R.id.mHotApp_rv);
    }

    @Override
    protected void setListener() {
        change_data_Tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(CommonUtils.isFastDoubleClick())return;
                changeData();
                MTAUtil.onClickSearchChange();

            }
        });
        mHotApp_ftl.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onItemClick(FlowTagLayout parent, View view, int position) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) BaseApplication.curContext .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm !=null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘
                }
                if (mSearchHotAppAdapter.getItem(position) != null) {
                    HotKeyBean bean = mSearchHotAppAdapter.getItem(position);
                    if (!TextUtils.isEmpty(bean.searchWord)
                            && bean.ikey.equals("text")) {
                        activity.searchView.setTextForEditText(bean.searchWord);
                        activity.goToSearResFragmnet(bean.searchWord);
                        activity.setKeyword(bean.searchWord);
                        MTAUtil.onSearchHotKey(bean.searchWord);
                        if (isOriginalPage) {
                            MTAUtil.onSearchHotKeyFirstPage(bean.searchWord);
                        }
                    } else if (!TextUtils.isEmpty(bean.searchWord)
                            && bean.ikey.equals("app")) {
                        AppsItemBean appBean = mSearchHotAppAdapter
                                .getItem(position).data.app;
                        if (appBean == null || TextUtils.isEmpty(appBean.id)) {
                            UIUtils.gotoAppDetail(appBean, bean.ivalue, activity);
                        } else {
                            UIUtils.gotoAppDetail(appBean,
                                    appBean.id, activity);
                        }
                        if (appBean != null && !TextUtils.isEmpty(appBean.name)
                                && !TextUtils.isEmpty(appBean.packageName)) {
                            MTAUtil.onDetailClick(activity, appBean.name,
                                    appBean.packageName);
                            MTAUtil.onSearchHotKey(appBean.name);
                            if (isOriginalPage) {
                                MTAUtil.onSearchHotKeyFirstPage(appBean.name);
                            }

                        }
                    }

                }
            }
        });

        clear_history_Tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SearchHistoryDao.cleardata();
                search_Rlyt.setVisibility(View.GONE);
                mSearchFlowLayout.setVisibility(View.GONE);
//                view_line.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void init() {
        mSearchHotAppAdapter = new SearchHotAppAdapter(activity);
        mHotApp_ftl.setAdapter(mSearchHotAppAdapter);
        getSearchHistory();
        requestHotAppData();
    }

    /**
     * 获取搜索记录
     */
    private void getSearchHistory() {
        final ArrayList<String> list = SearchHistoryDao.getSearchHistoryList();
        int size = list.size();
        if (size <= 0) {
            search_Rlyt.setVisibility(View.GONE);
            mSearchFlowLayout.setVisibility(View.GONE);
//            view_line.setVisibility(View.GONE);
        } else {
            search_Rlyt.setVisibility(View.VISIBLE);
            mSearchFlowLayout.setVisibility(View.VISIBLE);
//            view_line.setVisibility(View.VISIBLE);
        }
        mSearchFlowLayout.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 20, 0);

        for (int i = 0; i < size; i++) {
            final TextView tagView = (TextView) LayoutInflater.from(activity)
                    .inflate(R.layout.item_search_history, null);
            String content = list.get(i);
            content = CommonUtils.getMaxLenStr(content);
            tagView.setText(content);
            tagView.setLayoutParams(params);
            mSearchFlowLayout.addView(tagView);
            tagView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    activity.searchView.setTextForEditText(tagView.getText().toString());
                    activity.goToSearResFragmnet(tagView.getText().toString());
                    activity.setKeyword(tagView.getText().toString());
                }
            });
        }
    }

    /**
     * 请求热搜热搜数据
     */
    private void requestHotAppData() {
        String url = Constants.GIS_URL + "/search/hotwords";
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("pageIndex", String.valueOf(1));
        params.addBodyParameter("pageSize", String.valueOf(30));
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        mCancelable = XExtends.http().post(params,
                new PrizeXutilStringCallBack<String>() {

                    @Override
                    public void onSuccess(String result) {
                        hideWaiting();
                        mScrollView.setVisibility(View.VISIBLE);
                        try {
                            String response = new JSONObject(result)
                                    .getString("data");
                            data = GsonParseUtils.parseSingleBean(response,
                                    HotAppData.class);
                            if (data != null && data.hotwords.size() > 0) {
                                data.hotwords = CommonUtils.filterSearchHotInstalled(data.hotwords, 20);
                                mSearchHotAppAdapter.setList(data.hotwords);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        loadingFailed(new ReloadFunction() {

                            @Override
                            public void reload() {
                                showWaiting();
                                requestHotAppData();
                            }

                        });
                    }
                });
    }

    /**
     * 请求热搜热搜数据
     */
    private void requestRandchange() {
        String url = Constants.GIS_URL + "/search/randchange";
        RequestParams params = new RequestParams(url);
        params.addHeader("Connection", "close");
//        if (mCancelable != null) {
//            mCancelable.cancel();
//        }
        mCancelable = XExtends.http().post(params,
                new PrizeXutilStringCallBack<String>() {

                    @Override
                    public void onSuccess(String result) {
                        mScrollView.setVisibility(View.VISIBLE);
                        try {
                            String response = new JSONObject(result)
                                    .getString("data");
                            data = GsonParseUtils.parseSingleBean(response,
                                    HotAppData.class);
                            if (data != null && data.hotwords.size() > 0) {
                                data.hotwords = CommonUtils.filterSearchHotInstalled(data.hotwords, 20);
                                mSearchHotAppAdapter.setList(data.hotwords);
                                isOriginalPage = false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        JLog.i("SearchOriginalFragment","ex:"+ex.getMessage());
                        ToastUtils.showToast(R.string.net_error);
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        super.onDestroy();
    }

    private View waitView = null;

    /**
     * 隐藏等待框
     */
    public void hideWaiting() {
        if (waitView == null)
            return;
        waitView.setVisibility(View.GONE);
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(true);
        reloadView.setVisibility(View.GONE);

    }

    /**
     * 加载失败
     */
    public void loadingFailed(final ReloadFunction reload) {
        waitView.setVisibility(View.GONE);
        reloadView.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.GONE);
        LinearLayout reloadLinearLayout = (LinearLayout) reloadView
                .findViewById(R.id.reload_Llyt);
        if (reloadLinearLayout != null) {
            reloadLinearLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    reload.reload();
                }
            });
        }

    }

    /**
     * 显示等待框
     */
    public void showWaiting() {
        if (waitView == null)
            return;
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(false);
        waitView.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.GONE);
        reloadView.setVisibility(View.GONE);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getSearchHistory();
        }
    }

}
