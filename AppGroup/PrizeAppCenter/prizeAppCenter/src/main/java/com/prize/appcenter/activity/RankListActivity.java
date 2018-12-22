package com.prize.appcenter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.pager.BasePager;
import com.prize.appcenter.ui.pager.RankListPager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.PagerSlidingTabStripExtends;

import java.util.ArrayList;

/**
 * *
 * 榜单详细Activity （add 2.6版本）
 *
 * @author longbaoxiu
 * @version V1.0
 */

public class RankListActivity extends ActionBarNoTabActivity {
    private final String TAG = "RankListActivity";
    public static final String LISTCATEGORY = "Listcategory";
    public static final String SELECT_POSITION = "select_position";
    private BasePager[] pagers;
    private int currentPage;
    private ArrayList<CategoryContent> categoryContents;
    private int selectPosition=0;
    private PagerSlidingTabStripExtends mTabs;
    private ViewPager ViewPager ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranklist_layout);
        setNeedAddWaitingView(false);
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        mToken = AIDLUtils.bindToService(this);
        init();
        setListener();
    }

    private void init() {
        Intent intent = getIntent();
        if (intent == null){
            finish();
            return;

        }
        categoryContents= intent.getParcelableArrayListExtra(LISTCATEGORY);
        selectPosition= intent.getIntExtra(SELECT_POSITION,0);
        currentPage=selectPosition;
        if (categoryContents == null||categoryContents.size()<=0){
            finish();
            return;

        }

        setTitle(R.string.ranking);
        pagers = new BasePager[categoryContents.size()];
        RanKlistAdapter adapter = new RanKlistAdapter(this);
        ViewPager.setOffscreenPageLimit(categoryContents.size());
        ViewPager.setAdapter(adapter);
        mTabs.setViewPager(ViewPager);
        initTabsValue();
    }
    /**
     * mPagerSlidingTabStrip默认值配置
     *
     */
    private void initTabsValue() {
        // 底部游标颜色
        mTabs.setIndicatorColor(R.color.app_background);
//        mTabs.setIndicatorColor(Color.parseColor("#12b7f5"));
        // tab的分割线颜色
        mTabs.setDividerColor(Color.TRANSPARENT);
        // tab背景
        mTabs.setBackgroundColor(getResources().getColor(R.color.app_background));
//        // 游标高度
        mTabs.setIndicatorHeight(0);
        mTabs.getPager().setCurrentItem(selectPosition);
    }

    private void setListener() {
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (currentPage != position) {
                    currentPage = position;
                    pagers[currentPage].onPause(); // 前一页pause
                    pagers[position].onResume();
                }
                currentPage = position;
                pagers[position].loadData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private void findViewById() {
        // 初始化ViewPager并且添加适配器
        ViewPager = (ViewPager) findViewById(R.id.slidingpager);
        mTabs = (PagerSlidingTabStripExtends) findViewById(R.id.slidingtabs);
    }

    class RanKlistAdapter extends PagerAdapter{

        public RanKlistAdapter(RootActivity activity) {
            super();
            int size=categoryContents.size();
            for(int i=0;i<size;i++){
                RankListPager pager = new RankListPager(activity,categoryContents.get(i).keyId,i);
                pager.getView();
                if(selectPosition==i){
                    pager.loadData();
                }
                pagers[i]=pager;
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
        public int getCount() {
            return categoryContents.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryContents.get(position).subTag;
        }

    }


    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "RankListActivity";
    }

    @Override
    protected void onResume() {
        pagers[currentPage].onResume();
        pagers[currentPage].setAutoScroll(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        pagers[currentPage].onPause();
        pagers[currentPage].setAutoScroll(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(categoryContents !=null&&categoryContents.size()>0){
            int size=categoryContents.size();
            for(int i=0;i<size;i++){
                pagers[i].onDestroy();
            }
        }
        AIDLUtils.unbindFromService(mToken);
    }
}
