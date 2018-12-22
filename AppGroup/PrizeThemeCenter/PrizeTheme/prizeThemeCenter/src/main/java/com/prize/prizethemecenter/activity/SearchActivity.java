package com.prize.prizethemecenter.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.ui.page.BasePage;
import com.prize.prizethemecenter.ui.page.SearchFontPage;
import com.prize.prizethemecenter.ui.page.SearchThemePage;
import com.prize.prizethemecenter.ui.page.SearchWallPage;
import com.prize.prizethemecenter.ui.utils.ChangeWatchedManager;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.WatchedManager;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.SearchView;
import com.prize.prizethemecenter.ui.widget.SearchView.SearchViewListener;
import com.prize.prizethemecenter.ui.widget.indicator.IconTextPagerAdapter;
import com.prize.prizethemecenter.ui.widget.indicator.TabTextPageIndicator;

/**
 * 搜索界面
 * Created by pengy on 2016/9/8.
 */
public class SearchActivity extends RootActivity implements SearchViewListener{

    private ViewPager mViewPager;
    private static final int[] ICONS = { R.string.app_theme,
            R.string.app_wallpaper, R.string.app_font };
    private BasePage[] pagers = new BasePage[ICONS.length];
    /*** pageID */
    private static final int SEARCH_THEME_PAGER_ID = 0;
    private static final int SEARCH_WALLPAPER_PAGER_ID = 1;
    private static final int SEARCH_FONT_PAGER_ID = 2;

    private SearchThemePage themePage;

    public SearchView searchView;
    private int currentPage;
    public static final String STR = "str";

    public static final String STR_SEARCH = "str_search";

    public static final String CURRENTPOSITION = "";

    private TabTextPageIndicator mIndicator;

    private String hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.search_layout);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        setListener();

    }
    private int position;

    private void findViewById() {
        mIndicator = (TabTextPageIndicator) findViewById(R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        searchView = (SearchView)findViewById(R.id.main_search_layout);
        searchView.setSearchViewListener(this);

        if (getIntent() != null
                && !TextUtils.isEmpty(getIntent().getStringExtra(CURRENTPOSITION))) {
            position = Integer.parseInt(getIntent().getStringExtra(CURRENTPOSITION));
            if(position==3){
                position = 0;
            }
            currentPage = position;
        }

        init();
        if (getIntent() != null
                && !TextUtils.isEmpty(getIntent().getStringExtra(STR))) {
            searchView.setHint(getIntent().getStringExtra(STR));
            hint = getIntent().getStringExtra(STR);
        }
        if (getIntent() != null
                && !TextUtils.isEmpty(getIntent().getStringExtra(STR_SEARCH))) {
            searchView.setText(getIntent().getStringExtra(STR_SEARCH));
        }


    }

    @Override
    public void onSearch(String text) {
        if(TextUtils.isEmpty(text)){
            return;
        }
        JLog.i("hu","text=="+text+"---pagers[currentPage]=="+pagers[currentPage]+"---currentPage=="+currentPage);
        if(pagers[currentPage]!=null){
            pagers[currentPage].addToHistory(text);
            WatchedManager.notifyChange(text);
        }
        MTAUtil.onSearchWord(text);
    }

    @Override
    public void showHistory() {
        pagers[currentPage].showHistory();
    }

    @Override
    public void showTips(String content) {
        ChangeWatchedManager.notifyTipsChange(content);
    }

    private void init() {
        SearchAdapter adapter = new SearchAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);
        mIndicator.setViewPager(mViewPager,position);
    }

    private void setListener() {

        mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (currentPage != position) {
                    pagers[currentPage].onPause(); // 前一页pause
                    pagers[position].onResume();
                }
                pagers[position].loadData();
                currentPage = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class SearchAdapter extends PagerAdapter implements IconTextPagerAdapter {

        public SearchAdapter() {
            // 主题
            themePage = new SearchThemePage(SearchActivity.this);
            themePage.getView(); // 需要初始化，原因：page 可以跳转
            pagers[SEARCH_THEME_PAGER_ID] = themePage;
//            themePage.loadData();

            // 壁纸
            SearchWallPage wallPage = new SearchWallPage(SearchActivity.this);
            pagers[SEARCH_WALLPAPER_PAGER_ID]=wallPage;
            pagers[SEARCH_WALLPAPER_PAGER_ID].getView();
            wallPage.setEditHint(hint);

            // 字体
            SearchFontPage fontPage = new SearchFontPage(SearchActivity.this);
            pagers[SEARCH_FONT_PAGER_ID]=fontPage;
            pagers[SEARCH_FONT_PAGER_ID].getView();
            fontPage.setEditHint(hint);

            if(position==0 || position ==3){
                themePage.loadData();
            }else if(position==1){
                wallPage.loadData();
            }else if(position==2){
                fontPage.loadData();
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pagers[position].getView());
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            container.addView(pagers[position].getView());
            return pagers[position].getView();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int id = ICONS[position % ICONS.length];
            return getString(id);
        }

        @Override
        public int getCount() {
            return ICONS.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getIconResId(int index) {
            return ICONS[index];
        }
    }

    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pagers[currentPage].onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pagers[currentPage].onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pagers[SEARCH_THEME_PAGER_ID].onDestroy();
        pagers[SEARCH_WALLPAPER_PAGER_ID].onDestroy();
        pagers[SEARCH_FONT_PAGER_ID].onDestroy();
    }
}
