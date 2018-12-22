package com.prize.appcenter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.prize.app.threads.PriorityRunnable;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.pager.AppTypeCategoryPager;
import com.prize.appcenter.ui.pager.BasePager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.PagerSlidingTabStripExtends;
import com.viewpagerindicator.IconPagerAdapter;

/**
 * 应用页游戏页分类跳转目标
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class CommonCategoryActivity extends ActionBarNoTabActivity {
    private final String TAG = "CommonCategoryActivity";
    private static final int[] ICONS = {R.string.apps,
            R.string.game};
    private PagerSlidingTabStripExtends mTabs;
    private BasePager[] pagers = new BasePager[ICONS.length];
    /***精品 */
    private static final int APP_CAT = 0;
    /***排行*/
    private static final int GAME_CAT = 1;
    private int mCurrentPage;
    private boolean isGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.changeStatus(getWindow());
        setContentView(R.layout.app_category_pager);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        Intent intent = getIntent();
        if (null != intent) {
            isGame = intent.getBooleanExtra("isPopular", false);
        }
        findViewById();
        mToken = AIDLUtils.bindToService(this);
        init();
        setNeedAddWaitingView(false);
        setListener();
    }

    @Override
    public String getActivityName() {
        return "CommonCategoryActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void init() {
        setTitle(R.string.app_category_type);
    }

    private void setListener() {
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                //释放所有视频
                PriorityRunnable.decreaseBase();
                // 友盟统计
                if (mCurrentPage != position) {
                    pagers[mCurrentPage].onPause(); // 前一页pause
                    pagers[position].onResume();
                }

                pagers[position].loadData();

                mCurrentPage = position;

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }


    private void findViewById() {
        // 初始化ViewPager并且添加适配器
        ViewPager pager = (ViewPager) findViewById(R.id.slidingpager);
        mTabs = (PagerSlidingTabStripExtends) findViewById(R.id.slidingtabs);
        mTabs.setTabPaddingLeftRight((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,58.75f));
        AppCategoryPagerAdapter adapter = new AppCategoryPagerAdapter(this);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(adapter);
        mTabs.setViewPager(pager);
        if (isGame) {
            pager.setCurrentItem(1);
        }
        initTabsValue();
    }

    /**
     * mPagerSlidingTabStrip默认值配置
     */
    private void initTabsValue() {
        // 底部游标颜色
        mTabs.setIndicatorColor(Color.parseColor("#009def"));
        // tab的分割线颜色
        mTabs.setDividerColor(Color.TRANSPARENT);
        // tab背景
        mTabs.setBackgroundColor(getResources().getColor(android.R.color.white));
        // 游标高度
        mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
                        .getDisplayMetrics()));
        mTabs.setUnderlineColor(Color.parseColor("#eaeaea"));
    }


    private class AppCategoryPagerAdapter extends PagerAdapter implements IconPagerAdapter {
        public AppCategoryPagerAdapter(RootActivity activity) {
            super();
            AppTypeCategoryPager mAppTypeCategoryPager = new AppTypeCategoryPager(activity, false);
            pagers[APP_CAT] = mAppTypeCategoryPager;
            pagers[APP_CAT].getView(); // 需要初始化，原因：page 可以跳转
            if (!isGame) {
                pagers[APP_CAT].loadData();
            }
            AppTypeCategoryPager mGamePager = new AppTypeCategoryPager(activity, true);
            pagers[GAME_CAT] = mGamePager;
            pagers[GAME_CAT].getView(); // 需要初始化，原因：page 可以跳转
            if (isGame) {
                pagers[GAME_CAT].loadData();
            }

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pagers[position].getView());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            container.addView(pagers[position].getView());
            return pagers[position].getView();
        }

        @Override
        public int getIconResId(int index) {
            return ICONS[index];
        }

        @Override
        public int getCount() {
            return pagers.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int id = ICONS[position % ICONS.length];
            return getString(id);
        }

    }

    @Override
    public void onResume() {
        pagers[mCurrentPage].onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        pagers[mCurrentPage].onPause();
        super.onPause();
    }

    @Override
    protected void initActionBar() {
        super.initActionBar();
        if (divideLine != null) {
            divideLine.setVisibility(View.GONE);
        }
    }
}
