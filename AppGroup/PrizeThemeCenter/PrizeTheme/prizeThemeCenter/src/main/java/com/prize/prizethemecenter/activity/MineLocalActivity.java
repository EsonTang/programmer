package com.prize.prizethemecenter.activity;

import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionSet;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.LocalThemeBean;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.MineLocalThemeAdapter;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 本地主题
 */
public class MineLocalActivity extends ActionBarNoTabActivity implements View.OnClickListener{

    private String TAG="MineLocalActivity";
    private GridViewWithHeaderAndFooter mGridView;
    private MineLocalThemeAdapter mThemeAdapter;
    private ArrayList<LocalThemeBean> mThemeList;
    private TextView mLeftTv;
    private ImageButton mImageButton;
    private RelativeLayout mBarLayout;
    private ImageButton mSearchBtn;
    private TextView mTitleTv;
    private LinearLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        setContentView(R.layout.activity_mine_local);
        WindowMangerUtils.changeStatus(getWindow());
        setUpWindowTrisience();
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
        mLayout = (LinearLayout) findViewById(R.id.root_layout);
        mLayout.setBackgroundColor(this.getResources().getColor(R.color.white));
        mSearchBtn = (ImageButton) findViewById(R.id.bar_search);
        mSearchBtn.setVisibility(View.GONE);
        mLeftTv = (TextView) findViewById(R.id.action_bar_left_text);
        mLeftTv.setVisibility(View.VISIBLE);
        mLeftTv.setText(R.string.mine_local_theme);
        mTitleTv = (TextView) findViewById(R.id.action_bar_title);
        mTitleTv.setVisibility(View.GONE);
        mImageButton = (ImageButton) findViewById(R.id.action_bar_back);
        mGridView = (GridViewWithHeaderAndFooter)findViewById(R.id.gridview);
        mThemeAdapter = new MineLocalThemeAdapter(MineLocalActivity.this);
        initData();
        setClickListner();
    }

    private void setClickListner() {
        mImageButton.setOnClickListener(this);
    }

    private void initData() {
        try {
            mThemeList = (ArrayList<LocalThemeBean>) UIUtils.queryLocalTheme(getApplicationContext());
            JLog.d(TAG, "run: "+mThemeList.size());
            mThemeAdapter.addData(mThemeList);
            mGridView.setAdapter(mThemeAdapter);
        } catch (Exception pE) {
            pE.printStackTrace();
        }
    }



    @Override
    public void onResume() {
        String id =  UIUtils.querySelected(this);
        for (LocalThemeBean t:mThemeList)  {
            t.setIsSelected("0");
            if(t.getThemeId().equals(id)) {
                t.setIsSelected("1");
            }
        }
        mThemeAdapter.setData(mThemeList);
        mThemeAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public String getActivityName() {
        return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_bar_back:
                finish();
                break;
        }
    }
}
