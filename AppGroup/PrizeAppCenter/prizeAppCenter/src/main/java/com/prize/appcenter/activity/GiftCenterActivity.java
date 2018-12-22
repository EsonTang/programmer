/*******************************************
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.GiftCenterResData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.GameCenterInstalledAdapter;
import com.prize.appcenter.ui.adapter.GameCenterUnInstalledAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ScrollLineGridView;
import com.prize.appcenter.ui.widget.ScrollListView;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * *
 * 礼包中心
 *
 * @author 聂礼刚
 * @version V1.0
 */
public class GiftCenterActivity extends ActionBarTabActivity {
    private ScrollListView mInstalledList;
    private ScrollLineGridView mGridView;
    private GameCenterInstalledAdapter mInstalledListAdapter;
    private GameCenterUnInstalledAdapter mUnInstalledListAdapter;
    public static  String TOPICIDKEY="topicidkey";
    private ImageView topBanner_iv;
    private TextView pullTv;
    private LinearLayout installedLayout;
    private Callback.Cancelable mCancelable;
    private HomeAdBean mHomeAdBean;

    private ArrayList<AppsItemBean> installedApps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);

        setContentView(R.layout.activity_gitcenter_listview);
        WindowMangerUtils.changeStatus(getWindow());
        setTopicTitle(R.string.gift_center);
        findViewById();

        mInstalledListAdapter = new   GameCenterInstalledAdapter(this);
        mUnInstalledListAdapter = new    GameCenterUnInstalledAdapter(this);
        mToken = AIDLUtils.bindToService(this, this);

        init();

        setListener();

    }

    private void setListener() {
        mInstalledList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                UIUtils.gotoGameGiftDetai(GiftCenterActivity.this,
                        mInstalledListAdapter.getItem(position).id,
                        position);
            }
        });

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                UIUtils.gotoGameGiftDetai(GiftCenterActivity.this,
                        mUnInstalledListAdapter.getItem(position).id,
                        position);
            }
        });

        topBanner_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIUtils.gotoGameGiftDetai(GiftCenterActivity.this,
                        mHomeAdBean.app.id,
                        0);
            }
        });

    }

    public void goToqueen(View view) {
        UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
                GiftCenterActivity.this);
    }

    private void init() {
        if(getIntent()!=null){
            from = getIntent().getStringExtra(Constants.FROM);
        }
        if (mHomeAdBean != null) {
            ImageLoader.getInstance().displayImage(mHomeAdBean.imageUrl,
                    topBanner_iv, UILimageUtil.getTopicListUILoptions(),
                    null);
        }
        mInstalledList.setAdapter(mInstalledListAdapter);
        mGridView.setAdapter(mUnInstalledListAdapter);

        mInstalledList.setFocusable(false);
        mGridView.setFocusable(false);
        requestData();

    }

    private void findViewById() {
        topBanner_iv = (ImageView) findViewById(R.id.topic_img);
        installedLayout = (LinearLayout) findViewById(R.id.installed_ll);
        pullTv = (TextView) findViewById(R.id.pull_tv);
        mInstalledList = (ScrollListView) findViewById(R.id.installed_list);
        mGridView = (ScrollLineGridView) findViewById(android.R.id.list);
    }

    @Override
    public String getActivityName() {
        return "TopicDetailActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/center");
        mCancelable = XExtends.http().get(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        GiftCenterResData data = new Gson().fromJson(res, GiftCenterResData.class);

                        if (data != null) {
                            if (data.apps != null && data.apps.size() > 0) {
                                CommonUtils.filterNoGift(data.apps);
                                installedApps = CommonUtils.filterUnInstalled(data.apps);
                            }

                            if (data.banner != null) {
                                mHomeAdBean = data.banner;
                                // if (!TextUtils.isEmpty(topic.style.imageUrl)) {
                                ImageLoader.getInstance().displayImage(mHomeAdBean.imageUrl,
                                        topBanner_iv,
                                        UILimageUtil.getTopicListUILoptions(), null);
                                // }
                            }
                        }

                        if (installedApps.size() > 0) {
                            mInstalledListAdapter.setData(installedApps);
                            mInstalledListAdapter.setItemNum(installedApps.size());
                            if (installedApps.size() > 2) {
                                final Drawable arrowDown = getResources().getDrawable(R.drawable.gift_center_pull_down);
                                arrowDown.setBounds(0, 0, arrowDown.getMinimumWidth(), arrowDown.getMinimumHeight());

                        final Drawable arrowUp = getResources().getDrawable(R.drawable.gift_center_pull_up);
                        arrowUp.setBounds(0, 0, arrowUp.getMinimumWidth(), arrowUp.getMinimumHeight());

                        mInstalledListAdapter.setItemNum(2);
                        pullTv.setVisibility(View.VISIBLE);
                        pullTv.setCompoundDrawablePadding(8);
                        pullTv.setCompoundDrawables(arrowDown, null, null, null);
                        pullTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mInstalledListAdapter.getCount() == 2) {
                                    //当前为收起状态，点击展开
                                    pullTv.setCompoundDrawables(arrowUp, null, null, null);
                                    mInstalledListAdapter.setItemNum(installedApps.size());
                                    pullTv.setText(getResources().getString(R.string.gamedetail_brief_unexpend));
                                } else {
                                    //当前为展开状态，点击收起
                                    pullTv.setCompoundDrawables(arrowDown, null, null, null);
                                    mInstalledListAdapter.setItemNum(2);
                                    pullTv.setText(getResources().getString(R.string.expand_all));
                                }
                            }
                        });
                    }
                } else {
                    installedLayout.setVisibility(View.GONE);
                }

                        if (mUnInstalledListAdapter != null) {
                            data.apps.removeAll(installedApps);
                            mUnInstalledListAdapter.setData(data.apps);
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

    // /**
    // * 隐藏加载进度条
    // */
    // private void hideDialog() {
    // if (progressBarLayout != null
    // && progressBarLayout.getVisibility() == View.VISIBLE) {
    // progressBarLayout.setVisibility(View.GONE);
    // }
    // }

    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    protected void onStart() {
//		if (mUnInstalledListAdapter != null) {
//			mUnInstalledListAdapter.setIsActivity(true);
//		}
        super.onStart();
    }

    @Override
    protected void onPause() {
//		if (mUnInstalledListAdapter != null) {
//			mUnInstalledListAdapter.setIsActivity(false);
//		}
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
//		mInstalledListAdapter.setDownlaodRefreshHandle();
//		mUnInstalledListAdapter.setDownlaodRefreshHandle();
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            try {
                UIUtils.gotoActivity(MainActivity.class,GiftCenterActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onBackPressed();
    }
}
