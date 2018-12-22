package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.RankOverViewResponse;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.RankOverViewAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.util.ArrayList;


/**
 * *
 * 榜单综述Activity （add 2.6版本）
 *
 * @author longbaoxiu
 * @version V1.0
 */

public class RankOverViewActivity extends ActionBarNoTabActivity {
    private final String TAG = "RankOverViewActivity";
    private ListView gameListView;
    private Cancelable mCancelable;
    private RankOverViewAdapter mRankOverViewAdapter;
    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_singlegame_layout);
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        mToken = AIDLUtils.bindToService(this,this);
        init();
        setListener();
        requestData();
    }

    private void init() {
        mRankOverViewAdapter = new RankOverViewAdapter(this);
        mRankOverViewAdapter.setDownlaodRefreshHandle();
        gameListView.setAdapter(mRankOverViewAdapter);
        if(getIntent()!=null&& !TextUtils.isEmpty(getIntent().getStringExtra("title"))){
            setTitle(getIntent().getStringExtra("title"));
        }else{
            setTitle(R.string.ranking);
        }
    }

    private void setListener() {
        gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

    }

    private void findViewById() {
        gameListView = (ListView) findViewById(android.R.id.list);
        gameListView.setDividerHeight(0);
    }

    /**
     * 请求总榜概述数据
     */
    private void requestData() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        showWaiting();
        RequestParams entity = new RequestParams(Constants.GIS_URL
                + "/rank/totalRankList");
        mCancelable = XExtends.http().post(entity,
                new PrizeXutilStringCallBack<String>() {

                    @Override
                    public void onSuccess(String result) {
                        try {
                            hideWaiting();
                            JSONObject o = new JSONObject(result);
                            String o1 = o.getString("data");
                            RankOverViewResponse rankOverViewResponse = GsonParseUtils.parseSingleBean(o1, RankOverViewResponse.class);
                            ArrayList<CategoryContent> categoryContents = new ArrayList<>();
                            categoryContents.clear();
                            if (rankOverViewResponse != null) {
                                for (int i = 0; i < rankOverViewResponse.list.size(); i++) {
                                    rankOverViewResponse.list.get(i).apps = CommonUtils.filterInstalled(rankOverViewResponse.list.get(i).apps, 3);
                                    categoryContents.add(new CategoryContent(rankOverViewResponse.list.get(i).rankType,rankOverViewResponse.list.get(i).rankName));
                                }
                                mRankOverViewAdapter.setData(rankOverViewResponse.list,categoryContents);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        ToastUtils.showToast(R.string.net_error);
                        hideWaiting();
                        loadingFailed(new ReloadFunction() {
                            @Override
                            public void reload() {
                                requestData();
                            }
                        });
                    }
                });
    }



    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "RankOverViewActivity";
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mRankOverViewAdapter.setDownlaodRefreshHandle();
    }

    @Override
    public void onResume() {
        if (mRankOverViewAdapter != null) {
            mRankOverViewAdapter.setIsActivity(true);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mRankOverViewAdapter != null) {
            mRankOverViewAdapter.setIsActivity(false);
        }
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mRankOverViewAdapter != null) {
            mRankOverViewAdapter.resetDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
    }
}
