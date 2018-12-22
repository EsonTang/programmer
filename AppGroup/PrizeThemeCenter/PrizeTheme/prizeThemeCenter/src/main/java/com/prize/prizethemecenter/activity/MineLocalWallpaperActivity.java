package com.prize.prizethemecenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionSet;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.LocalWallPaperBean;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.MineLocalWallPaperAdapter;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 本地主题
 */
public class MineLocalWallpaperActivity extends ActionBarNoTabActivity implements View.OnClickListener{

    private String TAG="MineLocalWallpaperActivity";
    private GridViewWithHeaderAndFooter mGridView;
    public MineLocalWallPaperAdapter mLocalWallPaperAdapter;
    public ArrayList<LocalWallPaperBean> mWallPaperList;
    private TextView mLeftTv;
    private ImageButton mImageButton;
    private RelativeLayout mBarLayout;
    private ImageButton mSearchBtn;
    private TextView mTitleTv;
    private LocaWallBroadcastReceiver receiver = new LocaWallBroadcastReceiver();
    public static String LOCAL_WALL_SELECTED_ACTION ="com.prize.local.wall";
    private String localWallPath;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOCAL_WALL_SELECTED_ACTION);
        this.registerReceiver(receiver, filter);

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
        mLeftTv.setText(R.string.mine_local_wallpaper);
        mTitleTv = (TextView) findViewById(R.id.action_bar_title);
        mTitleTv.setVisibility(View.GONE);
        mImageButton = (ImageButton) findViewById(R.id.action_bar_back);
        mGridView = (GridViewWithHeaderAndFooter)findViewById(R.id.gridview);
        mLocalWallPaperAdapter = new MineLocalWallPaperAdapter(this);
        initData();
        setClickListner();
    }

    private void setClickListner() {
        mImageButton.setOnClickListener(this);
    }

    private void initData() {
        try {
            mWallPaperList = (ArrayList<LocalWallPaperBean>) UIUtils.queryLocalWallPaper(MineLocalWallpaperActivity.this);
            JLog.d(TAG, "run: " + mWallPaperList.size());
            mLocalWallPaperAdapter.addData(mWallPaperList);
            mGridView.setAdapter(mLocalWallPaperAdapter);
        } catch (Exception pE) {
            pE.printStackTrace();
        }

    }



    @Override
    public void onResume() {
        try {
            mWallPaperList = (ArrayList<LocalWallPaperBean>) UIUtils.queryLocalWallPaper(MineLocalWallpaperActivity.this);
            mLocalWallPaperAdapter.setData(mWallPaperList);
            mLocalWallPaperAdapter.notifyDataSetChanged();
        } catch (Exception pE) {
            pE.printStackTrace();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class LocaWallBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LOCAL_WALL_SELECTED_ACTION)){
                localWallPath = intent.getStringExtra("localWallPath");
                String id =  UIUtils.queryLocalWallIsSelected(MineLocalWallpaperActivity.this, Constants.LOCAL_WALLPAGE_PATH,"wallpaperId",localWallPath);
                UIUtils.updateLocalWallData(id,MineLocalWallpaperActivity.this);
            }
        }
    }
}
