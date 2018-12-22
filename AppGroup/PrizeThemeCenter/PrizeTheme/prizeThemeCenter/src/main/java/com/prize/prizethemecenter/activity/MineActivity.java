package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.callback.IconPagerAdapter;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.page.BasePage;
import com.prize.prizethemecenter.ui.page.MineFontPage;
import com.prize.prizethemecenter.ui.page.MineThemePage;
import com.prize.prizethemecenter.ui.page.MineWallPaperPage;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.view.CustomViewPager;
import com.prize.prizethemecenter.ui.widget.view.PagerSlidingTabStripExtends;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 主题、壁纸、字体
 */
public class MineActivity extends ActionBarNoTabActivity implements View.OnClickListener {

    private static final String TAG = "MineActivity";
    private PagerSlidingTabStripExtends mTabs;

    private static final int[] ICONS = {R.string.app_theme,
            R.string.app_wallpaper, R.string.app_font};

    private BasePage[] pagers = new BasePage[ICONS.length];

    private MineThemePage mThemePage;
    private MineWallPaperPage mWallPage;
    private MineFontPage mFontPage;

    public static final String mTabId = "tabId";

    private int mCurrentPage;
    /***
     * 主题
     */
    private int APP_THEME_ID = 0;
    /***
     * 壁纸
     */
    private int APP_WALLPAPER_ID = 1;
    /***
     * 字体
     */
    private int APP_FONT_ID = 2;

    private RelativeLayout mBarLayout;
    private ImageButton mImageButton;
    private ImageButton mSearchBtn;
    private TextView mLeftTv;
    private TextView mTitleTv;
    private TextView mRightTv;
    private int mTabIdPage;
    private CustomViewPager mPager;
    private LinearLayout mLayout;


    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        setContentView(R.layout.activity_mine);
        WindowMangerUtils.changeStatus(getWindow());
        setUpWindowTrisience();
        if (getIntent() != null) {
            mTabIdPage = getIntent().getIntExtra(mTabId, 0);
        }
        initView();

    }
    private void setUpWindowTrisience() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
            TransitionSet mtransitionset = new TransitionSet();
            mtransitionset.setDuration(3000);
            getWindow().setEnterTransition(mtransitionset);
            getWindow().setExitTransition(mtransitionset);
        }
    }

    private void initView() {
        mLayout = (LinearLayout) findViewById(R.id.mine_loaded_ll);
        mLayout.setBackgroundColor(this.getResources().getColor(R.color.white));
        // 初始化ViewPager并且添加适配器
        mPager = (CustomViewPager) findViewById(R.id.slidingpager);
        mTabs = (PagerSlidingTabStripExtends) findViewById(R.id.slidingtabs);
//        mBarLayout = (RelativeLayout) findViewById(R.id.action_bar_no_tab);
//        mBarLayout.setVisibility(View.VISIBLE);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 63);
//        params.setMargins(0, 63, 0, 0);
//        mBarLayout.setLayoutParams(params);
        mImageButton = (ImageButton) findViewById(R.id.action_bar_back);
        mSearchBtn = (ImageButton) findViewById(R.id.bar_search);
        mSearchBtn.setVisibility(View.GONE);
        mLeftTv = (TextView) findViewById(R.id.action_bar_left_text);
        mLeftTv.setVisibility(View.VISIBLE);
        mLeftTv.setText(R.string.app_my);
        mLeftTv.setClickable(true);
        mLeftTv.setEnabled(true);
        mLeftTv.setFocusable(true);
        mRightTv = (TextView) findViewById(R.id.action_bar_right_text);
        mRightTv.setVisibility(View.VISIBLE);
        mRightTv.setText(R.string.app_edit);
        mRightTv.setClickable(true);
        mRightTv.setEnabled(true);
        mRightTv.setFocusable(true);
        mTitleTv = (TextView) findViewById(R.id.action_bar_title);
        mTitleTv.setVisibility(View.GONE);
        AppCategoryPagerAdapter adapter = new AppCategoryPagerAdapter(this);
        mPager.setOffscreenPageLimit(3);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(mTabIdPage);
        mTabs.setViewPager(mPager);
        initTabsValue();
        setOnclickListener();
        mLeftTv.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        mRightTv.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_left_text:
                finish();
                break;
            case R.id.action_bar_back:
                mPager.setScanScroll(true);
                finish();
                break;
            case R.id.action_bar_right_text:
                UIUtils.startActivityForResult(this, MineEditActivity.class, mTabIdPage, mTabId);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0:
                if (data != null) {
                    ArrayList<DownloadInfo> vThemeList = (ArrayList<DownloadInfo>) data.getSerializableExtra(mTabId);
                    mThemePage.mThemeAdapter.setData(vThemeList);
                    setColor(vThemeList);
                }
                break;
            case 1:
                if (data != null) {
                    ArrayList<DownloadInfo> vWallList = (ArrayList<DownloadInfo>) data.getSerializableExtra(mTabId);
                    mWallPage.mWallPaperAdapter.setData(vWallList);
                    setColor(vWallList);
                }
                break;
            case 2:
                if (data != null) {
                    ArrayList<DownloadInfo> vWallList = (ArrayList<DownloadInfo>) data.getSerializableExtra(mTabId);
                    mFontPage.mFontAdapter.setData(vWallList);
                    setColor(vWallList);
                }
                break;
        }
    }

    class AppCategoryPagerAdapter extends PagerAdapter implements
            IconPagerAdapter {

        public AppCategoryPagerAdapter(MineActivity activity) {
            super();

            // 主题
            mThemePage = new MineThemePage(activity);
            pagers[APP_THEME_ID] = mThemePage; // 需要初始化，原因：page 可以跳转
            mThemePage.getView();
            mThemePage.onResume();//防止无法绑定下载监听

            // 壁纸
            mWallPage = new MineWallPaperPage(activity);
            pagers[APP_WALLPAPER_ID] = mWallPage;
            pagers[APP_WALLPAPER_ID].getView(); // 需要初始化，原因：page 可以跳转
            mWallPage.onResume();
            // 字体
            mFontPage = new MineFontPage(activity);
            pagers[APP_FONT_ID] = mFontPage;
            pagers[APP_FONT_ID].getView(); // 需要初始化，原因：page 可以跳转
            mFontPage.onResume();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pagers[position].getView());
            // container.removeView(container.getChildAt(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(TAG, "instantiateItem: " + position);
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
            return MineActivity.this.getString(id);
        }

    }

    private void initTabsValue() {
        // 底部游标颜色
        // mTabs.setIndicatorColor(mContext.getResources().getColor(R.color.text_color_blue));
        mTabs.setIndicatorColor(Color.TRANSPARENT);
        mTabs.setUnderlineColorResource(R.color.bg_gray_color);
        // tab的分割线颜色
        mTabs.setDividerColor(Color.TRANSPARENT);
        // tab背景
//        mTabs.setBackgroundColor(this.getResources().getColor(R.color.app_background));
//        mTabs.setBackgroundColor(Color.TRANSPARENT);
        mTabs.setBackgroundColor(this.getResources().getColor(R.color.white));
        // tab底线高度
//		mTabs.setUnderlineHeight(1);
//		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources()
//				.getDisplayMetrics()));
        // 游标高度
        mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, this.getResources()
                        .getDisplayMetrics()));
        /*
		 * // 正常文字颜色 mTabs.setTextColor(Color.GRAY);
		 */
        // 设置可延伸
        // mTabs.setShouldExpand(true);

    }

    private void setOnclickListener() {

        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                // 友盟统计
                if (mTabIdPage != position) {
                    pagers[mTabIdPage].onPause(); // 前一页pause
                    JLog.d(TAG, "mTabs中执行了onPause()");
                    pagers[position].onResume();
                }

                pagers[position].loadData();

                mTabIdPage = position;
               switch (mTabIdPage){
                   case 0:
                       setColor(mThemePage.getList());
                       break;
                   case 1:
                       setColor(mWallPage.getList());
                       break;
                   case 2:
                       setColor(mFontPage.getList());
                       break;
               }


            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    protected void onPause() {
        pagers[mTabIdPage].onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pagers[mTabIdPage].onResume();
    }

    @Override
    protected void onDestroy() {
        if(rightPopupWindow != null)rightPopupWindow.dismiss();
        super.onDestroy();
        pagers[APP_THEME_ID].onDestroy();
        pagers[APP_FONT_ID].onDestroy();
        pagers[APP_WALLPAPER_ID].onDestroy();
    }

    public void setColor(ArrayList<DownloadInfo> pList) {
        int colorValue = pList.size() == 0 ?
                this.getResources().getColor(R.color.text_color_969696) :
                this.getResources().getColor(R.color.text_color_33cccc);
        mRightTv.setTextColor(colorValue);
        int isVisible  = pList.size() == 0 ? TextView.VISIBLE : TextView.GONE;
        boolean isEnable  = pList.size() == 0 ? false: true;
        mRightTv.setClickable(isEnable);
//        if (pList.size() == 0) ToastUtils.showToast(R.string.no_download_data);
        switch (mTabIdPage) {
            case 0:
                mThemePage.mBgRl.setVisibility(isVisible);
                mThemePage.mGridView.setEnabled(isEnable);
                break;
            case 1:
                mWallPage.mBgRl.setVisibility(isVisible);
                mWallPage.mGridView.setEnabled(isEnable);
                break;
            case 2:
                mFontPage.mBgRl.setVisibility(isVisible);
                mFontPage.mGridView.setEnabled(isEnable);
                break;
        }
    }
    private AlertDialog rightPopupWindow = null;
    public void showProgressBar(final boolean isSuccess){
//        if (rightPopupWindow != null) {
//            if (!rightPopupWindow.isShowing()) {
//                rightPopupWindow.show();
//            } else {
//                rightPopupWindow.dismiss();
//            }
//        } else {
//            initPop(this);
//        }
        new Handler().postDelayed(new Runnable(){

            public void run() {
//                rightPopupWindow.dismiss();
                if(isSuccess){
//                    ToastUtils.showToast(getString(R.string.changing_font_suc));
                    UIUtils.backToLauncher(MineActivity.this,null);
                }else{
                    ToastUtils.showToast(getString(R.string.changing_font_failed));
                }
            }
        }, 2000);
    }

    private  void initPop(Activity context) {
        rightPopupWindow = new AlertDialog.Builder(context).create();
        rightPopupWindow.show();
        View loginwindow = context.getLayoutInflater().inflate(
                R.layout.popwindow_setwallpaper_layout, null);
        loginwindow.setBackgroundColor(getResources().getColor(R.color.white));
        TextView textView = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        textView.setTextColor(R.color.text_color_333333);
        textView.setText(R.string.font_changing);
        Window window = rightPopupWindow.getWindow();
        window.setContentView(loginwindow);
        WindowManager.LayoutParams p = window.getAttributes();
        WindowManager wm = (WindowManager) MainApplication.curContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        if(width<=720){
            p.width = 600;
        }else{
            p.width = 900;
        }
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.alpha =1f;
        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
    }
    
    /**
     *  实现按两次Back键退出
     */
    @Override
    public void onBackPressed() {
        int page = mTabIdPage;
        Intent intent = new Intent(MainApplication.curContext, MainActivity.class);
        intent.putExtra("page",page);
        MainApplication.curContext.startActivity(intent);
        finish();
    }
}
