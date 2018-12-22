/*
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

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.HottestData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.HottestListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 * 类描述：最热
 *
 * @author 作者 nieligang
 * @version 版本
 */
public class UucListActivity extends ActionBarTabActivity {
    final String TAG = "UucListActivity";
    private HottestListAdapter adapter;
    private ListView appListView;
    private HottestData data;
    private Callback.Cancelable mCancelable;
    public static  final String TITLE="title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_uuc_list);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        if(getIntent()!=null&& !TextUtils.isEmpty(getIntent().getStringExtra(TITLE))){
            setTopicTitle(getIntent().getStringExtra(TITLE));
        }else{
            setTopicTitle(R.string.hot_magazine);
        }
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        if (adapter == null) {
            adapter = new HottestListAdapter(this);
        }
        mToken = AIDLUtils.bindToService(this, this);
        appListView.setAdapter(adapter);
        requestData();
        appListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, onScrollListener));

    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {


        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.setIsActivity(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.setIsActivity(true);
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    adapter.setIsActivity(false);
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    private void findViewById() {
        appListView = (ListView) findViewById(android.R.id.list);
//        noLoading = LayoutInflater.from(this).inflate(
//                R.layout.footer_nomore_show, null);
    }


    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/hottest/list?pageIndex=1&pageSize=20");
        mCancelable = XExtends.http().get(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        data = new Gson().fromJson(res, HottestData.class);

                        if (data != null) {
                            if (data.apps != null && data.apps.size() > 0) {
                                adapter.setData(data.apps);
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
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
    public String getActivityName() {

        return "UucListActivity";
    }

    @Override
    protected void onResume() {
        if (adapter != null) {
            adapter.setIsActivity(true);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (adapter != null) {
            adapter.setIsActivity(true);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (adapter != null) {
            adapter.removeDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        adapter.setDownlaodRefreshHandle();
    }

}
