package com.prize.appcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.prize.app.beans.ClientInfo;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingPageItemBean;
import com.prize.app.threads.SingleThreadUpdateExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.OneKeyFragment;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.statistics.model.ExposureBean;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.List;

public class OneKeyFragmentActivity extends FragmentActivity implements OneKeyFragment.NextClickInterface {

    private int[] pageIndext = new int[5];
    private ArrayList<AppsKeyInstallingPageItemBean> mDatas;
    private ArrayList<AppsItemBean> appsToInstall = new ArrayList<AppsItemBean>();

    private ViewPager mViewPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private boolean isNewVersion = false;
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    /*已经曝光的页面***/
    private List<Integer> pages = new ArrayList<>();
    private boolean isNeedStatic=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.onekey_install_layout);
        mDatas = getIntent().getParcelableArrayListExtra("datas");
        pageIndext = getIntent().getIntArrayExtra("page_flag");
        isNewVersion = getIntent().getBooleanExtra("isNewVersion", false);
        isNeedStatic=JLog.isDebug||!TextUtils.isEmpty(CommonUtils.getNewTid());
        initViewPager();

        mToken = AIDLUtils.bindToService(this);
    }

    private ServiceToken mToken;
    private int pageCount;
//    private boolean isNeedStatics = true;

    private void initViewPager() {
        pageCount = mDatas.size();
        for (int i = 0; i < pageCount; i++) {
            AppsKeyInstallingPageItemBean pageItem = mDatas.get(i);
            int currentCheck = 0;
            ExposureBean pbean;
            for (AppsItemBean item : pageItem.apps) {//2.8版本改变：每页选中个数后台控制
                item.pageTitle = pageItem.title;
                if (i == 0&&isNeedStatic) {//默认开始曝光第一页
                    pbean = CommonUtils.formNewPagerExposure(item, "onekey", "default");
                    if (!mExposureBeans.contains(pbean)) {
                        mExposureBeans.add(pbean);
                    }
                }
                if (pageItem.checkedCnt > 0 && currentCheck < pageItem.checkedCnt) {
                    item.isCheck = true;
                    appsToInstall.add(item);
                    currentCheck++;
                } else {
                    item.isCheck = false;

                }
            }
            pages.add(0);
            Bundle bundle = new Bundle();
            bundle.putString("bg_color", pageItem.color);
            bundle.putString("title", pageItem.title);
            bundle.putString("icon_url", pageItem.iconUrl);
            bundle.putParcelableArrayList("data", pageItem.apps);
            bundle.putIntArray("page_flag", pageIndext);
            bundle.putInt("current_page", i);
            Fragment fragment = new OneKeyFragment();
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }

        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        mViewPager = (ViewPager) findViewById(R.id.onekey_viewpager);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (JLog.isDebug) {
                    JLog.i("OneKeyFragmentActivity", "onPageSelected-position=" + position
                            + "--pageCount=" + pageCount + "--pages.contains(position)=" + pages.contains(position));
                }
                if (!isNeedStatic||pages.contains(position)) return;
                OneKeyFragment fragment = (OneKeyFragment) fragments.get(position);
                List<ExposureBean> tempList = fragment.getmExposureBeans();
                if (tempList != null && !mExposureBeans.containsAll(tempList)) {
                    mExposureBeans.addAll(tempList);
                    pages.add(position);
                    if (JLog.isDebug) {
                        JLog.i("OneKeyFragmentActivity", "mExposureBeans.size=" + mExposureBeans.size());
                        JLog.i("OneKeyFragmentActivity", "mExposureBeans=" + mExposureBeans);
                    }
                }
                if (position == pageCount - 1) {
                    if (mExposureBeans != null && mExposureBeans.size() > 0) {
                        PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                        mExposureBeans.clear();
//                        mExposureBeans = null;
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        StatService.onPause(this);
        AIDLUtils.unbindFromService(mToken);
    }

    private void oneKeyDown() {
        MTAUtil.onClickFirstDownload();
        SingleThreadUpdateExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (appsToInstall != null && appsToInstall.size() > 0) {
                    for (int i = 0; i < appsToInstall.size(); i++) {
                        UIUtils.downloadApp(CommonUtils.formatAppPageInfo(appsToInstall.get(i), "onekey", "default", i));
                        if (isNewVersion) {
                            MTAUtil.onTimesFirstDownload(appsToInstall.get(i).name,
                                    appsToInstall.get(i).packageName, appsToInstall.get(i).pageTitle);
                        } else {
                            MTAUtil.onVersionUpdateDownload(appsToInstall.get(i).name,
                                    appsToInstall.get(i).packageName, appsToInstall.get(i).pageTitle);
                        }


                    }
                }
            }
        });
    }

    private void fadeToMainActivity() {
        JLog.e("huang", "jump---3");
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
        super.onDestroy();
    }

    @Override
    public void onNextClick(boolean flag) {
        if (flag) {
            if (appsToInstall != null && appsToInstall.size() > 0) {
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    return;
                }
            }
            oneKeyDown();
            fadeToMainActivity();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onItemCheck(ArrayList<AppsItemBean> checks, int currentPage) {
        ArrayList<AppsItemBean> pageApps = mDatas.get(currentPage).apps;
        appsToInstall.removeAll(pageApps);
        if (checks != null) {
            appsToInstall.addAll(checks);
        }
        int checkedCount = appsToInstall.size();
        for (int i = 0; i < fragments.size(); i++) {
            OneKeyFragment fragment = (OneKeyFragment) fragments.get(i);
            if (fragment.isAdded()) {
                fragment.setCheckedCount(checkedCount);
            }

        }
    }

    private class FragAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments;

        public FragAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            // TODO Auto-generated constructor stub
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int arg0) {
            // TODO Auto-generated method stub
            /*Fragment fragment = mFragments.get(arg0);

			Bundle bundle = new Bundle();
			bundle.putString("bg_color", pageItem.color);
			bundle.putString("title", pageItem.title);
			bundle.putString("icon_url", pageItem.iconUrl);
			bundle.putParcelableList("data", pageItem.apps);
			bundle.putBoolean("is_last_page", i==(pageCount-1));
			bundle.putIntArray("page_flag", pageIndext);
			bundle.putInt("current_page", i);

			fragment.setArguments(bundle);
			*/
            return mFragments.get(arg0);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mFragments.size();
        }

    }
}
