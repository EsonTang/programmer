package com.prize.appcenter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingPageItemBean;
import com.prize.app.threads.SingleThreadUpdateExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.adapter.OneKeyNewVersionAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.PrizeCommButton;
import com.prize.statistics.model.ExposureBean;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.List;

public class OneKeyUpdateActivity extends FragmentActivity {

    private ArrayList<AppsKeyInstallingPageItemBean> mDatas;
    private ServiceToken mToken;
    private OneKeyNewVersionAdapter mOneKeyNewVersionAdapter;
    private ImageView head_img;
    private ListView mListView;
    public static final String TAG ="OneKeyUpdateActivity" ;
    private boolean isNeedStatic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.onekey_update_layout);
        View headView = LayoutInflater.from(this).inflate(R.layout.head_onekey_layout, null);
        View bootomView = LayoutInflater.from(this).inflate(R.layout.bottomview_onekey_layout, null);
        if (ClientInfo.getAPNType(this) != ClientInfo.WIFI && ClientInfo.getAPNType(this) != ClientInfo.NONET) {
            TextView cautionView = (TextView) bootomView.findViewById(R.id.wifi_caution_Tv);
            cautionView.setText(R.string.mobile_caution);
        }
        PrizeCommButton entry_main_Tv = (PrizeCommButton) bootomView.findViewById(R.id.entry_main_Tv);
        head_img = (ImageView) headView.findViewById(R.id.head_img);
        entry_main_Tv.enabelDefaultPress(true);
        entry_main_Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOneKeyNewVersionAdapter != null && mOneKeyNewVersionAdapter.getChecks().size() > 0) {
                    if (ClientInfo.networkType == ClientInfo.NONET) {
                        ToastUtils.showToast(R.string.net_error);
                        return;
                    }
                }
                oneKeyDown();
                fadeToMainActivity();
            }
        });
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.addHeaderView(headView);
        mListView.addFooterView(bootomView);
        mDatas = getIntent().getParcelableArrayListExtra("datas");

        mToken = AIDLUtils.bindToService(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        init(mListView);
    }

    private void init(ListView mListView) {
        isNeedStatic = JLog.isDebug || !TextUtils.isEmpty(CommonUtils.getNewTid());
        ArrayList<AppsItemBean> appsToInstall = new ArrayList<AppsItemBean>();
        final int pageCount = mDatas.size();
        for (int i = 0; i < pageCount; i++) {
            AppsKeyInstallingPageItemBean pageItem = mDatas.get(i);
            int currentCheck = 0;
            for (AppsItemBean item : pageItem.apps) {//2.8版本改变：每页选中个数后台控制
                item.pageTitle = pageItem.title;
                if (pageItem.checkedCnt > 0 && currentCheck < pageItem.checkedCnt) {
                    item.isCheck = true;
                    currentCheck++;
                } else {
                    item.isCheck = false;

                }
                appsToInstall.add(item);
            }
            String bg_color = "#" + pageItem.color;
            int bgColor;
            try {
                bgColor = Color.parseColor(bg_color.trim());
            } catch (Exception e) {
                Log.i("OneKeyUpdateActivity", "e=" + e);
                bgColor = Color.parseColor("#d3f0ff");//防止后台配置出错时，默认浅绿色
            }
            mListView.setBackgroundColor(bgColor);
            mListView.setDivider(new ColorDrawable(bgColor));
            mListView.setDividerHeight((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 40f));
            ImageLoader.getInstance().displayImage(pageItem.iconUrl, head_img, UILimageUtil.getColorDrawableOptions(new ColorDrawable(bgColor)));
        }

        mOneKeyNewVersionAdapter = new OneKeyNewVersionAdapter(OneKeyUpdateActivity.this, appsToInstall);
        mListView.setAdapter(mOneKeyNewVersionAdapter);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
    }
    private int mFirstVisibleItem;
    private int lastVisiblePosition;
    private boolean isFirstStatistics = true;
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {



        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (mFirstVisibleItem < 0&&!isNeedStatic)//此时可见头布局
                        break;
                    AppsItemBean bean;
                    ExposureBean pbean;
                    for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                        bean = mOneKeyNewVersionAdapter.getItem(i);
                        if (bean != null) {
                            pbean = CommonUtils.formNewPagerExposure(bean, "onekey", "default");
                            if(!mExposureBeans.contains(pbean)){
                                mExposureBeans.add(pbean);
                            }
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-去重前mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans.size=" + mExposureBeans.size());
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    break;
            }

        }

        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = mListView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
            if (isFirstStatistics && lastVisiblePosition > 0&&isNeedStatic) {
                AppsItemBean bean;
                ExposureBean pbean;
                for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                    bean = mOneKeyNewVersionAdapter.getItem(i);
                    if (bean != null) {
                        pbean = CommonUtils.formNewPagerExposure(bean, "onekey", "default");
                        if(!mExposureBeans.contains(pbean)){
                            mExposureBeans.add(pbean);
                        }
                    }
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i(TAG, "onScroll-去重前mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onScroll-mExposureBeans.size=" + mExposureBeans.size());
                }
            }
            mFirstVisibleItem = firstVisibleItem;
        }

    };
    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    private void fadeToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AIDLUtils.unbindFromService(mToken);
        if (isNeedStatic&&mExposureBeans != null && mExposureBeans.size() > 0) {
            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
        }
        super.onDestroy();
    }


    private void oneKeyDown() {
        MTAUtil.onClickFirstDownload();
        SingleThreadUpdateExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (mOneKeyNewVersionAdapter != null && mOneKeyNewVersionAdapter.getChecks() != null && mOneKeyNewVersionAdapter.getChecks().size() > 0) {
                    for (int i = 0; i < mOneKeyNewVersionAdapter.getChecks().size(); i++) {
//                        UIUtils.downloadApp(mOneKeyNewVersionAdapter.getChecks().get(i));
                        UIUtils.downloadApp(CommonUtils.formatAppPageInfo(mOneKeyNewVersionAdapter.getChecks().get(i), "onekey", "default", i));
                        MTAUtil.onOneKeyVersionUpdate(mOneKeyNewVersionAdapter.getChecks().get(i).name,
                                mOneKeyNewVersionAdapter.getChecks().get(i).packageName, mOneKeyNewVersionAdapter.getChecks().get(i).pageTitle);

                    }
                }
            }
        });
    }

}
