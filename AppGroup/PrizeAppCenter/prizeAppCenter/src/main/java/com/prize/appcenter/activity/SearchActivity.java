package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.SearchOriginalFragment;
import com.prize.appcenter.fragment.SearchResultFragment;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.SearchView;
import com.prize.appcenter.ui.widget.SearchView.SearchViewListener;

/**
 * *
 * 搜索界面
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchActivity extends ActionBarNoTabActivity implements
        SearchViewListener {
    private static final String TAG = "SearchActivity";
    private SearchOriginalFragment mSearchOriginalFragment;
    private SearchResultFragment mSearchResultFragment;
    public SearchView searchView;
    public static final String STR = "str";
    private String keyword;
    private String mTAG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.search_title);
        if (savedInstanceState != null) {
            mTAG = savedInstanceState.getString("TAG");
        }
        findViewById();
        mToken = AIDLUtils.bindToService(this, this);
        UIUtils.addActivity(this);
        init();
        setListener();

    }

    private void findViewById() {
        searchView = (SearchView) findViewById(R.id.main_search_layout);
        if (getIntent() != null
                && !TextUtils.isEmpty(getIntent().getStringExtra(STR))) {
            searchView.setHint(getIntent().getStringExtra(STR));
        }
    }

    private void setListener() {
        searchView.setSearchViewListener(this);
        searchView.setDownlaodRefreshHandle();
    }

    private void init() {
        mSearchOriginalFragment = (SearchOriginalFragment) getSupportFragmentManager()
                .findFragmentByTag(SearchOriginalFragment.class.getName());
        mSearchResultFragment = (SearchResultFragment) getSupportFragmentManager()
                .findFragmentByTag(SearchResultFragment.class.getName());
        if (JLog.isDebug) {
            JLog.i(TAG, "mTAG=" + mTAG + "--mSearchResultFragment=" + (mSearchResultFragment == null) + "--mSearchOriginalFragment=" + (mSearchOriginalFragment == null));
        }
        if (mSearchResultFragment != null) {
            hideWaiting();
            if (!TextUtils.isEmpty(mTAG)) {
                if (mTAG.equals(SearchResultFragment.class.getName())) {
                    getSupportFragmentManager().beginTransaction()
                            .show(mSearchResultFragment).hide(mSearchOriginalFragment).commitAllowingStateLoss();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .show(mSearchOriginalFragment).hide(mSearchResultFragment).commitAllowingStateLoss();
                }
            }
        } else if (mSearchOriginalFragment == null) {
            mSearchOriginalFragment = new SearchOriginalFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.search_container, mSearchOriginalFragment,
                            SearchOriginalFragment.class.getName())
                    .commitAllowingStateLoss();
        } else if (mSearchOriginalFragment.isAdded()) {
            hideWaiting();
            if (JLog.isDebug) {
                JLog.i(TAG, "mTAG=" + mTAG + "--mSearchOriginalFragment.isAdded()=");
            }
            if (!TextUtils.isEmpty(mTAG) && mTAG.equals(SearchOriginalFragment.class.getName())) {
                getSupportFragmentManager().beginTransaction()
                        .show(mSearchOriginalFragment).hide(mSearchResultFragment).commitAllowingStateLoss();
            }
            getSupportFragmentManager().beginTransaction()
                    .show(mSearchOriginalFragment).commitAllowingStateLoss();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (searchView != null) {
            searchView.setIsActivity(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (searchView != null) {
            searchView.setIsActivity(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchView != null) {
            searchView.removeDownLoadHandler();
        }
        UIUtils.removeActivity(this);
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
    }

    @Override
    public String getActivityName() {
        return "SearchActivity";
    }

    @Override
    protected void initActionBar() {
        enableSlideLayout(false);
    }

    /**
     * 设置标题栏
     *
     * @param title 标题
     */
    public void setTitle(int title) {
    }

    /**
     * 跳转到 {@link SearchResultFragment}执行搜索，显示结果
     *
     * @param keyword 关键字
     */
    public void goToSearResFragmnet(String keyword) {
        keyword = CommonUtils.getMaxLenStr(keyword);
        mSearchResultFragment = (SearchResultFragment) getSupportFragmentManager()
                .findFragmentByTag(SearchResultFragment.class.getName());
        if (mSearchResultFragment == null) {
            mSearchResultFragment = new SearchResultFragment();
            Bundle args = new Bundle();
            args.putString("keyword", keyword);
            mSearchResultFragment.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.search_container, mSearchResultFragment,
                            SearchResultFragment.class.getName()).hide(mSearchOriginalFragment)
                    .commitAllowingStateLoss();
        } else {
            mSearchResultFragment.requestData(keyword);
            if (mSearchResultFragment != null && mSearchOriginalFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(mSearchOriginalFragment).show(mSearchResultFragment).commitAllowingStateLoss();
            }
        }
        mTAG = SearchResultFragment.class.getName();

    }


    public void hideFragment() {
        if (JLog.isDebug) {
            JLog.i(TAG,"hideFragment-mSearchResultFragment="+mSearchResultFragment+"--mSearchOriginalFragment="+mSearchOriginalFragment);
        }
        if (mSearchResultFragment != null && mSearchOriginalFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(mSearchResultFragment).show(mSearchOriginalFragment).commitAllowingStateLoss();
            mTAG = SearchOriginalFragment.class.getName();
        }
    }

//	public void gotoRecommandActivity() {
//		Intent in = new Intent(this, RecommandMoreActivity.class);
//		startActivity(in);
//	}

    @Override
    public void onSearch(String text, String id) {
        if (TextUtils.isEmpty(text)) {
            if (!TextUtils.isEmpty(id)) {
                UIUtils.gotoAppDetail(id, SearchActivity.this);
            }
            return;
        }
        keyword = text;
        goToSearResFragmnet(text);
    }

    public String getKeyWord() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (searchView != null) {
            searchView.setSearchViewListener(this);
            searchView.setDownlaodRefreshHandle();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("TAG", mTAG);
    }
}
