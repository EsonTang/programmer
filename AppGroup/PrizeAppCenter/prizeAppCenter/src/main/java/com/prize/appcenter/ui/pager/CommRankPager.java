package com.prize.appcenter.ui.pager;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prize.app.net.HeadResultCallBack;
import com.prize.app.threads.PriorityRunnable;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.PagerSlidingTabStripExtends;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

import static com.prize.appcenter.ui.widget.flow.FlowUIUtils.getResources;

/***
 * 首页榜单
 *
 * 类名称：CommRankPager
 *
 * 创建人：longbaoxiu
 *
 *
 * @version 1.0.0
 *
 */
public class CommRankPager extends BasePager implements HeadResultCallBack {
    private static final String TAG = "CommRankPager";
    private PagerSlidingTabStripExtends mTabs;

    private int mCurrentPage;

    private static final int[] ICONS = {R.string.popular_rank,
            R.string.app_rank, R.string.game_rank, R.string.new_product_rank};
    private BasePager[] pagers = new BasePager[ICONS.length];
    /***流行榜 */
    private static final int POPULAR_RANK_ID = 0;
    /***应用榜*/
    private static final int APP_RANK_ID = 1;
    /***游戏榜 */
    private static final int GAME_RANK_ID = 2;
    /***新品榜 */
    private static final int NEW_PRODUCT_RANK_ID = 3;


    /**
     * 创建一个新的实例 CommRankPager.
     *
     * @param activity RootActivity
     */
    public CommRankPager(RootActivity activity) {
        super(activity);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View mView = inflater.inflate(R.layout.app_category_pager, rootView, false);
        // 初始化ViewPager并且添加适配器
        ViewPager pager = (ViewPager) mView.findViewById(R.id.slidingpager);
        mTabs = (PagerSlidingTabStripExtends) mView
                .findViewById(R.id.slidingtabs);
        mTabs.setTabPaddingLeftRight(getResources().getDimensionPixelSize(R.dimen.rank_tabpadding));
        AppCategoryPagerAdapter adapter = new AppCategoryPagerAdapter((MainActivity) activity);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(adapter);
        mTabs.setViewPager(pager);
        initTabsValue();
        setOnclickListener();

        return mView;
    }

    private void setOnclickListener() {

        mTabs.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                //释放所有视频
                JCVideoPlayer.releaseAllVideos();

                PriorityRunnable.decreaseBase();
                // 友盟统计
                if (mCurrentPage != position) {
                    pagers[mCurrentPage].onPause(); // 前一页pause
                    pagers[position].onResume();
//                    int id = ICONS[position % ICONS.length];
//                    MTAUtil.onAPPOrGamePageTab(activity.getString(id), isPopular);
                }

                pagers[position].loadData();

                mCurrentPage = position;
                MTAUtil.onCommonRankSubTab(pagers[position].getPageName());

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
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
        mTabs.setBackgroundColor(activity.getResources().getColor(R.color.app_background));
        // 游标高度
        mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, activity.getResources()
                        .getDisplayMetrics()));
        mTabs.setDimension((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 20f));
        mTabs.setUnderlineHeight(0);
    }

    @Override
    public void loadData() {
        // 当选中的时候读取精品页的数据
    }

    @Override
    public void onActivityCreated() {

    }

    @Override
    public String getPageName() {
        return activity.getResources().getString(R.string.ranking);
    }

    @Override
    public void onDestroy() {
        pagers[POPULAR_RANK_ID].onDestroy();
        pagers[APP_RANK_ID].onDestroy();
        pagers[GAME_RANK_ID].onDestroy();
        pagers[NEW_PRODUCT_RANK_ID].onDestroy();

    }

    @Override
    public void onResponseHeaders(Map<String, String> headers) {
        // TODO Auto-generated method stub

    }


    private class AppCategoryPagerAdapter extends PagerAdapter implements IconPagerAdapter {
        AppCategoryPagerAdapter(MainActivity activity) {
            super();
            pagers[POPULAR_RANK_ID] = new PopularRankPager(activity, true);
            pagers[POPULAR_RANK_ID].getView(); // 需要初始化，原因：page 可以跳转
            // 应用榜单
            pagers[APP_RANK_ID] = new AppCategoryRankingPager(activity, false);
            pagers[APP_RANK_ID].getView(); // 需要初始化，原因：page 可以跳转
            // 游戏榜单
            pagers[GAME_RANK_ID] = new AppCategoryRankingPager(activity, true);
            pagers[GAME_RANK_ID].getView(); // 需要初始化，原因：page 可以跳转

            pagers[NEW_PRODUCT_RANK_ID] = new PopularRankPager(activity, false);
            pagers[NEW_PRODUCT_RANK_ID].getView(); // 需要初始化，原因：page 可以跳转
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
            // TODO Auto-generated method stub
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
            return activity.getString(id);
        }

    }

    @Override
    public void onResume() {
        pagers[mCurrentPage].onResume();
        pagers[mCurrentPage].loadData();
        super.onResume();
    }

    @Override
    public void onPause() {
        pagers[mCurrentPage].onPause();
        super.onPause();
    }

}
