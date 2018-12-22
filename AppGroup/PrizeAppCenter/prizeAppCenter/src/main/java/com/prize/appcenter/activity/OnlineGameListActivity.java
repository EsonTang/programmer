package com.prize.appcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.NetTypeBean;
import com.prize.appcenter.bean.SingGameResData;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.OnlineGameListAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * *
 * 网游新作
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class OnlineGameListActivity extends ActionBarNoTabActivity {
    private final String TAG = "OnlineGameListActivity";
    private ListView mListView;
    private Cancelable mCancelable;
    private ImageView game_iv;
    private OnlineGameListAdapter mOnlineGameListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_singlegame_layout);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        mToken = AIDLUtils.bindToService(this);
        init();
        setNeedAddWaitingView(true);
        setListener();
        requestData();
    }

    private void init() {

        Intent intent = getIntent();
        if (null != intent) {
            String title = getIntent().getStringExtra("title");
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }
        mOnlineGameListAdapter = new OnlineGameListAdapter(this);
        mListView.setAdapter(mOnlineGameListAdapter);
    }

    private void setListener() {

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long categoryId) {
                if (position - 1 >=0&&position - 1 < mOnlineGameListAdapter.getCount() && mOnlineGameListAdapter.getItem(position - 1) != null && mOnlineGameListAdapter.getItem(position - 1).mAppItemBean != null) {
                    AppsItemBean bean = mOnlineGameListAdapter.getItem(position - 1).mAppItemBean;
                    UIUtils.gotoAppDetail(bean, bean.id, OnlineGameListActivity.this);
                    MTAUtil.onLineGamePosition(position);
                }

            }
        });

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

    }

    OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    mOnlineGameListAdapter.setIsActivity(true);
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mOnlineGameListAdapter.setIsActivity(true);
                    break;
                case OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    mOnlineGameListAdapter.setIsActivity(false);
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }
    };

    private void findViewById() {
        mListView = (ListView) findViewById(android.R.id.list);
        View headView = LayoutInflater.from(this).inflate(
                R.layout.head_netgame_layout, null);
        game_iv = (ImageView) headView
                .findViewById(R.id.topic_detail_Iv);
        mListView.addHeaderView(headView);
    }

    /**
     * 请求列表数据
     */
    private void requestData() {
        showWaiting();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        RequestParams entity = new RequestParams(Constants.GIS_URL
                + "/recommand/newOnlineGame/v300");
        mCancelable = XExtends.http().post(entity,
                new CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        hideWaiting();
                        try {
                            JSONObject o = new JSONObject(result);
                            int code = o.getInt("code");
                            if (0 == code) {
                                String res = o.getString("data");
                                SingGameResData data = GsonParseUtils.parseSingleBean(res, SingGameResData.class);
                                if (data == null || data.onlineGames == null) {
                                    ToastUtils.showToast(R.string.no_data);
                                    finish();
                                    return;
                                }
                                ArrayList<NetTypeBean> targetList = filterNetGamenstalled(data.onlineGames, data.typeList, 5);
                                mOnlineGameListAdapter.addData(targetList);
                                processHeadData(data.banner);
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
                        loadingFailed(new ReloadFunction() {

                            @Override
                            public void reload() {
                                requestData();
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


    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mOnlineGameListAdapter != null) {
            mOnlineGameListAdapter.removeDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "CategoryAppGameListActivity";
    }

    @Override
    protected void onResume() {
        if (mOnlineGameListAdapter != null) {
            mOnlineGameListAdapter.setIsActivity(true);
            mOnlineGameListAdapter.setDownlaodRefreshHandle();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mOnlineGameListAdapter != null) {
            mOnlineGameListAdapter.setIsActivity(false);
        }
        super.onPause();
    }

    private void processHeadData(final HomeAdBean data) {
        if (data == null)
            return;
        if (TextUtils.isEmpty(data.bigImageUrl)) {
            ImageLoader.getInstance().displayImage(data.imageUrl, game_iv,
                    UILimageUtil.getUILoptions(R.drawable.bg_ad), null);
        } else {
            ImageLoader.getInstance().displayImage(data.bigImageUrl, game_iv,
                    UILimageUtil.getUILoptions(R.drawable.bg_ad), null);
        }
        game_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.processGoIntent(data, OnlineGameListActivity.this);
                MTAUtil.onOnLineGameHeadBanner(data.title);
            }
        });
    }

    /***
     * 过滤已经安装的应用，但是当小于needSize 时，自动补充一些已安装数据到列表。直到其size到达needSize
     *
     * @param listData 需要处理的list
     * @param needSize 最少显示个数
     * @return 过滤后的list
     */
    private ArrayList<NetTypeBean> filterNetGamenstalled(
            List<AppsItemBean> listData, ArrayList<AppHeadCategories> typeList, int needSize) {
        ArrayList<NetTypeBean> targetList = new ArrayList<NetTypeBean>();
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        ArrayList<AppsItemBean> installedList = new ArrayList<AppsItemBean>();
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName)) {
                installedList.add(item);
                continue;
            }
            targetList.add(new NetTypeBean(item, null, "list"));
            if (targetList.size() == 4) {
                targetList.add(new NetTypeBean(null, typeList, "typeList"));
            }
        }

        int a = 0;
        while (targetList.size() < needSize && a < installedList.size()
                && installedList.get(a) != null) {
            targetList.add(new NetTypeBean(installedList.get(a), null, "list"));
            if (targetList.size() == 4) {
                targetList.add(new NetTypeBean(null, typeList, "typeList"));
            }
            a++;
        }
        return targetList;
    }
}
