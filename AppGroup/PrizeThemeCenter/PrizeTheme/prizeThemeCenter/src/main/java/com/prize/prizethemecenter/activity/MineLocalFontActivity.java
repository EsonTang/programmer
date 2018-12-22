package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.LocalFontBean;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.MineLocalFontAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import org.xutils.ex.DbException;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 本地字体
 */
public class MineLocalFontActivity extends ActionBarNoTabActivity implements View.OnClickListener{

    private String TAG="MineLocalFontActivity";
    private GridViewWithHeaderAndFooter mGridView;
    private MineLocalFontAdapter mLocalFontAdapter;
    private ArrayList<LocalFontBean> mFontList;
    private TextView mLeftTv;
    private ImageButton mImageButton;
    private RelativeLayout mBarLayout;
    private ImageButton mSearchBtn;
    private TextView mTitleTv;
    private FrameLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        setContentView(R.layout.font_page);
        WindowMangerUtils.changeStatus(getWindow());
        setUpWindowTrisience();
        initView();
        initData();
        mGridView.setAdapter(mLocalFontAdapter);
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
        mLayout = (FrameLayout)findViewById(R.id.root_layout);
        mLayout.setBackgroundColor(this.getResources().getColor(R.color.white));
        mSearchBtn = (ImageButton) findViewById(R.id.bar_search);
        mSearchBtn.setVisibility(View.GONE);
        mLeftTv = (TextView) findViewById(R.id.action_bar_left_text);
        mLeftTv.setVisibility(View.VISIBLE);
        mLeftTv.setText(R.string.mine_local_font);
        mTitleTv = (TextView) findViewById(R.id.action_bar_title);
        mTitleTv.setVisibility(View.GONE);
        mImageButton = (ImageButton) findViewById(R.id.action_bar_back);
        mGridView = (GridViewWithHeaderAndFooter)findViewById(R.id.gridview);
        FrameLayout.LayoutParams gridParams=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        gridParams.setMargins(0,20,0,0);
        mGridView.setLayoutParams(gridParams);
        mLocalFontAdapter = new MineLocalFontAdapter(this);
        setClickListner();
    }

    private void setClickListner() {
        mImageButton.setOnClickListener(this);
    }

    private void initData() {
        try {
            mFontList = (ArrayList<LocalFontBean>) DBUtils.findAllLocalFontTask();
        } catch (DbException pE) {
            pE.printStackTrace();
        }
        JLog.d(TAG, "run: " + mFontList.size());
        mLocalFontAdapter.setData(mFontList);

    }



    @Override
    public void onResume() {
        initData();
        mLocalFontAdapter.notifyDataSetChanged();
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

    private AlertDialog rightPopupWindow = null;
    public void showProgressBar(final boolean isSuccess){
        if (rightPopupWindow != null) {
            if (!rightPopupWindow.isShowing()) {
                rightPopupWindow.show();
            } else {
                rightPopupWindow.dismiss();
            }
        } else {
            initPop(this);
        }
        new Handler().postDelayed(new Runnable(){

            public void run() {
                rightPopupWindow.dismiss();
                if(isSuccess){
                    ToastUtils.showToast(getString(R.string.changing_font_suc));
                }else{
                    ToastUtils.showToast(getString(R.string.changing_font_failed));
                }
            }
        }, 3000);
    }

    private  void initPop(Activity context) {
        rightPopupWindow = new AlertDialog.Builder(context).create();
        rightPopupWindow.show();
        View loginwindow = context.getLayoutInflater().inflate(
                R.layout.popwindow_setwallpaper_layout, null);
        loginwindow.setBackgroundColor(getResources().getColor(R.color.white));
        TextView textView = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        textView.setTextColor(R.color.text_color_323232);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rightPopupWindow != null)rightPopupWindow.dismiss();
    }
}
