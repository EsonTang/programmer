package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.adapter.MineFontAdapter;
import com.prize.prizethemecenter.ui.adapter.MineThemeAdapter;
import com.prize.prizethemecenter.ui.adapter.MineWallPaperAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Fanghui on 2016/11/26.
 */
public class MineEditActivity extends ActionBarNoTabActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "MineEditActivity";
    @InjectView(R.id.mine_loaded_delete_rl)
    RelativeLayout mMineLoadedDeleteRl;
    @InjectView(R.id.mine_loaded_ll)
    LinearLayout mMineLoadedLl;
    @InjectView(R.id.gridview)
    public GridViewWithHeaderAndFooter mGridView;
    @InjectView(R.id.mine_loaded_delete_tv)
    public RadioButton mDeleteRButton;

    private int mTabIdPage;
    private MineThemeAdapter mThemeAdapter;
    private MineWallPaperAdapter mWallPaperAdapter;
    private MineFontAdapter mFontAdapter;
    private RelativeLayout mBarLayout;
    private ImageButton mImageButton;
    private ImageButton mSearchBtn;
    private TextView mLeftTv;
    private TextView mTitleTv;
    private TextView mRightTv;

    private int mCheckedNum = 0; // 记录主题选中的条目数量
    private int mCheckedNum1 = 0; // 记录壁纸选中的条目数量
    private int mCheckedNum2 = 0; // 记录字体选中的条目数量
    private AlertDialog rightPopupWindow = null;
    private AlertDialog mCenterAlertDialog = null;

    private ArrayList<DownloadInfo> mThemeList;
    private ArrayList<DownloadInfo> mWallList;
    private ArrayList<DownloadInfo> mFontList;

    @InjectView(R.id.mine_loaded_delete_rl)
    public RelativeLayout mMineDeleteLayout;
    private TextView mContent;
    private LinearLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        window.setStatusBarColor(this.getResources().getColor(R.color.white));
        setContentView(R.layout.activity_mine_edit);
        WindowMangerUtils.changeStatus(getWindow());
        setUpWindowTrisience();
        ButterKnife.inject(this);
        if (getIntent() != null) {
            mTabIdPage = getIntent().getIntExtra(MineActivity.mTabId, 0);
        }
        if (mTabIdPage==2){
            mGridView.setNumColumns(2);
        }
        initView();
        initData();
        setOnclickListener();
        setDeleteColor();

    }

    private void setDeleteColor() {
        switch (mTabIdPage) {
            case 0:
                mDeleteRButton.setChecked(mCheckedNum > 0);
                break;
            case 1:
                mDeleteRButton.setChecked(mCheckedNum1 > 0);
                break;
            case 2:
                mDeleteRButton.setChecked(mCheckedNum2 > 0);
                break;
        }
    }

    private void setUpWindowTrisience() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
            TransitionSet mtransitionset = new TransitionSet();
            mtransitionset.setDuration(3000);
            getWindow().setEnterTransition(mtransitionset);
            getWindow().setExitTransition(mtransitionset);
        }
    }


    private void initData() {
        try {
            switch (mTabIdPage) {
                case 0:
                    mThemeList = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(1);
                    mThemeAdapter.setData(mThemeList);
                    mGridView.setAdapter(mThemeAdapter);
                    if (mThemeList.size()==0){
                        setNoDataBg();
                    }
                    JLog.d(TAG, "run: " + mThemeList.size());
                    break;
                case 1:
                    mWallList = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(2);
                    mWallPaperAdapter.setData(mWallList);
                    mGridView.setAdapter(mWallPaperAdapter);
                    if (mWallList.size()==0){
                        setNoDataBg();
                    }
                    break;
                case 2:
                    mFontList = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(3);
                    mFontAdapter.setData(mFontList);
                    mGridView.setAdapter(mFontAdapter);
                    if (mFontList.size()==0){
                        setNoDataBg();
                    }
                    break;
            }
        } catch (Exception pE) {
            pE.printStackTrace();
        }
    }


    private void setOnclickListener() {

        mLeftTv.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        mRightTv.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        mMineDeleteLayout.setOnClickListener(this);
        mDeleteRButton.setOnClickListener(this);

    }

    private void initView(){
        mLayout = (LinearLayout) findViewById(R.id.mine_loaded_ll);
        mLayout.setBackgroundColor(this.getResources().getColor(R.color.white));
        mImageButton = (ImageButton) findViewById(R.id.action_bar_back);
        mImageButton.setVisibility(View.GONE);
        mSearchBtn = (ImageButton) findViewById(R.id.bar_search);
        mSearchBtn.setVisibility(View.GONE);
        mLeftTv = (TextView) findViewById(R.id.action_bar_left_text);
        mLeftTv.setVisibility(View.VISIBLE);
        mLeftTv.setText(R.string.common_cancel);
        mLeftTv.setClickable(true);
        mRightTv = (TextView) findViewById(R.id.action_bar_right_text);
        mRightTv.setVisibility(View.VISIBLE);
        mRightTv.setText(R.string.common_all_seleted_text);
        mRightTv.setFocusable(true);
        mRightTv.setClickable(true);
        mRightTv.setEnabled(true);
        mRightTv.setTextColor(this.getResources().getColor(R.color.text_color_33cccc));
        mTitleTv = (TextView) findViewById(R.id.action_bar_title);
        mTitleTv.setText("已选中" + 0 + "项");
        mThemeAdapter = new MineThemeAdapter(this,true);
        mWallPaperAdapter = new MineWallPaperAdapter(this,true);
        mFontAdapter = new MineFontAdapter(this,true);
    }

    private void initPop() {
        rightPopupWindow = new AlertDialog.Builder(this,
                R.style.wallpaper_use_dialog_style).create();
        rightPopupWindow.show();
        View loginwindow = this.getLayoutInflater().inflate(
                R.layout.popwindow_pay_layout, null);
        TextView title = (TextView) loginwindow.findViewById(R.id.title_tv);
        TextView neg = (TextView) loginwindow.findViewById(R.id.add_neg);
        TextView sure = (TextView) loginwindow.findViewById(R.id.sure_Btn);
        TextView hint = (TextView) loginwindow.findViewById(R.id.hint_tv);
        mContent = (TextView) loginwindow.findViewById(R.id.content_tv);
        String deleteContent = getNumString();
        mContent.setText(deleteContent);
        title.setText(R.string.common_delete_text);
        hint.setVisibility(View.GONE);
        neg.setOnClickListener(this);
        sure.setOnClickListener(this);

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

        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
        rightPopupWindow.setContentView(loginwindow);
    }

    public void showProgressBar(){
        if (mCenterAlertDialog != null) {
            if (!mCenterAlertDialog.isShowing()) {
                mCenterAlertDialog.show();
            } else {
                mCenterAlertDialog.dismiss();
            }
        } else {
            initProgressPop(this);
        }
        new Handler().postDelayed(new Runnable(){

            public void run() {
                mCenterAlertDialog.dismiss();
                deleteItem();
                setResult();
            }
        }, 2000);
    }

    private  void initProgressPop(Activity context) {
        mCenterAlertDialog = new AlertDialog.Builder(context).create();
        mCenterAlertDialog.show();
        View loginwindow = context.getLayoutInflater().inflate(
                R.layout.popwindow_setwallpaper_layout, null);
        TextView textView = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        textView.setText(R.string.deleting);
        Window window = mCenterAlertDialog.getWindow();
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

        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
    }

    private String getNumString() {
        String deleteContent = getResources().getString(R.string.mine_delete_dialog);
        int num = 0;
        switch (mTabIdPage){
            case 0:
                num = mCheckedNum;
                deleteContent = String.format(deleteContent,num,getResources().getString(R.string.app_theme));
                break;
            case 1:
                num = mCheckedNum1;
                deleteContent = String.format(deleteContent,num,getResources().getString(R.string.app_wallpaper));
                break;
            case 2:
                num = mCheckedNum2;
                deleteContent = String.format(deleteContent,num,getResources().getString(R.string.app_font));
                break;
        }
        return deleteContent;
    }

    private int getNum() {
        int num = 0;
        switch (mTabIdPage){
            case 0:
                num= mCheckedNum;
                break;
            case 1:
                num= mCheckedNum1;
                break;
            case 2:
                num= mCheckedNum2;
                break;
        }
        return num;
    }


    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    public void onClick(View v) {
        mMineDeleteLayout.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.action_bar_right_text:
                switch (mTabIdPage) {
                    case 0:
                        String stringContent = mRightTv.getText().toString().trim();
                        if ( stringContent.equals(MineEditActivity.this.getString(R.string.common_all_seleted_text))) {
                            selectAll();
                            mThemeAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this.getString(R.string.common_all_diseleted_text));
                            mCheckedNum = mThemeAdapter.getCount();
                        } else if (stringContent.equals(MineEditActivity.this.getString(R.string.common_all_diseleted_text))) {
                            selectNone();
                            mThemeAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this
                                    .getString(R.string.common_all_seleted_text));
                            mCheckedNum = 0;
                        }
                        mTitleTv.setText("已选中" + mCheckedNum + "项");
                        break;
                    case 1:
                        String stringContent1 = mRightTv.getText().toString().trim();
                        if (stringContent1.equals(MineEditActivity.this.getString(R.string.common_all_seleted_text))) {
                            selectAll();
                            mWallPaperAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this
                                    .getString(R.string.common_all_diseleted_text));
                            mCheckedNum1 = mWallPaperAdapter.getCount();
                        } else if (stringContent1.equals(MineEditActivity.this.getString(R.string.common_all_diseleted_text))) {
                            selectNone();
                            mWallPaperAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this
                                    .getString(R.string.common_all_seleted_text));
                            mCheckedNum1 = 0;
                        }
                        mTitleTv.setText("已选中" + mCheckedNum1 + "项");
                        break;
                    case 2:
                        String stringContent2 = mRightTv.getText().toString().trim();
                        if (stringContent2.equals(MineEditActivity.this.getString(R.string.common_all_seleted_text))) {
                            selectAll();
                            mFontAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this.getString(R.string.common_all_diseleted_text));
                            mCheckedNum2 = mFontAdapter.getCount();
                        } else if (stringContent2.equals(MineEditActivity.this.getString(R.string.common_all_diseleted_text))) {
                            selectNone();
                            mFontAdapter.notifyDataSetChanged();
                            mRightTv.setText(MineEditActivity.this.getString(R.string.common_all_seleted_text));
                            mCheckedNum2 = 0;
                        }
                        mTitleTv.setText("已选中" + mCheckedNum2 + "项");
                        break;
                }
                break;
            case R.id.mine_loaded_delete_rl:

                break;

            case R.id.action_bar_left_text:
                setResult();
                break;

            case R.id.mine_loaded_delete_tv:
                if (rightPopupWindow != null) {
                    if (!rightPopupWindow.isShowing()&&getNum()>0) {
                        String deleteContent = getNumString();
                        mContent.setText(deleteContent);
                        rightPopupWindow.show();
                    }else {
                        ToastUtils.showToast(R.string.common_no_selected);
                        mDeleteRButton.setChecked(false);
                    }
                } else {
                    if (getNum()>0){
                        initPop();
                    }else {
                        ToastUtils.showToast(R.string.common_no_selected);
                        mDeleteRButton.setChecked(false);
                    }
                }
                Log.d(TAG, "mine_loaded_delete_rl: ");
                break;
            case R.id.add_neg:
                if (rightPopupWindow != null) {
                    if (rightPopupWindow.isShowing())
                        rightPopupWindow.dismiss();
                }
                break;
            case R.id.sure_Btn:
                if (rightPopupWindow != null) {
                    if (rightPopupWindow.isShowing())
                        rightPopupWindow.dismiss();
                }
//                deleteItem();
                showProgressBar();

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: " + ((DownloadInfo) parent.getAdapter().getItem(position)).getTitle());
        switch (mTabIdPage) {
            case 0:
                MineThemeAdapter.ViewHolder holder = (MineThemeAdapter.ViewHolder) view.getTag();
                // 改变CheckBox的状态
                holder.mCheckBox.toggle();
                if (holder.mCheckBox.getVisibility() == View.VISIBLE) {
                    holder.mCheckBox.setVisibility(View.GONE);
                } else {
                    holder.mCheckBox.setVisibility(View.VISIBLE);
                }
                // 将CheckBox的选中状况记录下来
                mThemeList.get(position).setChecked(holder.mCheckBox.isChecked());
                Drawable d = null;
                if (holder.mCornerImageView.getDrawable() != null) {
                    d = holder.mCornerImageView.getDrawable();
                }
                if (holder.mCheckBox.isChecked()) {
                    // 调整选定条目
                    mCheckedNum++;
                    if (d != null) {
                        d.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    }
                } else {
                    mCheckedNum--;
                    if (d != null) {
                        d.setColorFilter(null);
                    }
                }
                holder.mCornerImageView.invalidate();
                mCheckedNum = mCheckedNum < 0 ? 0 : mCheckedNum;
                if (mCheckedNum == mGridView.getCount()) {
                    mRightTv.setText(R.string.common_all_diseleted_text);
                } else {
                    mRightTv.setText(R.string.common_all_seleted_text);
                }
                if (mCheckedNum > 0) {
                    mDeleteRButton.setChecked(true);
                } else {
                    mDeleteRButton.setChecked(false);
                }
                mTitleTv.setText("已选中" + mCheckedNum + "项");
                break;
            case 1:
                MineWallPaperAdapter.ViewHolder holder1 = (MineWallPaperAdapter.ViewHolder) view.getTag();
                holder1.mCheckBox.toggle();
                if (holder1.mCheckBox.getVisibility() == View.VISIBLE) {
                    holder1.mCheckBox.setVisibility(View.GONE);
                } else {
                    holder1.mCheckBox.setVisibility(View.VISIBLE);
                }
                mWallList.get(position).setChecked(holder1.mCheckBox.isChecked());
                Drawable d1 = null;
                if (holder1.mCornerImageView.getDrawable() != null) {
                    d1 = holder1.mCornerImageView.getDrawable();
                }
                if (holder1.mCheckBox.isChecked()) {
                    mCheckedNum1++;
                    if (d1 != null) {
                        d1.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    }
                } else {
                    mCheckedNum1--;
                    if (d1 != null) {
                        d1.setColorFilter(null);
                    }
                }
                if (mCheckedNum1 > 0) {
                    mDeleteRButton.setChecked(true);
                } else {
                    mDeleteRButton.setChecked(false);
                }
                if (mCheckedNum1 == mGridView.getCount()) {
                    mRightTv.setText(R.string.common_all_diseleted_text);
                } else {
                    mRightTv.setText(R.string.common_all_seleted_text);
                }
                holder1.mCornerImageView.invalidate();
                mCheckedNum1 = mCheckedNum1 < 0 ? 0 : mCheckedNum1;
                mTitleTv.setText("已选中" + mCheckedNum1 + "项");
                break;
            case 2:
                MineFontAdapter.ViewHolder holder2 = (MineFontAdapter.ViewHolder) view.getTag();
                holder2.mCheckBox.toggle();
                if (holder2.mCheckBox.getVisibility() == View.VISIBLE) {
                    holder2.mCheckBox.setVisibility(View.GONE);
                } else {
                    holder2.mCheckBox.setVisibility(View.VISIBLE);
                }
                mFontList.get(position).setChecked(holder2.mCheckBox.isChecked());
                Drawable d2 = null;
                if (holder2.mCornerImageView.getDrawable() != null) {
                    d2 = holder2.mCornerImageView.getDrawable();
                }
                if (holder2.mCheckBox.isChecked()) {
                    mCheckedNum2++;
                    if (d2 != null) {
                        d2.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    }
                } else {
                    mCheckedNum2--;
                    if (d2 != null) {
                        d2.setColorFilter(null);
                    }
                }
                if (mCheckedNum2 > 0) {
                    mDeleteRButton.setChecked(true);
                } else {
                    mDeleteRButton.setChecked(false);
                }
                if (mCheckedNum2 == mGridView.getCount()) {
                    mRightTv.setText(R.string.common_all_diseleted_text);
                } else {
                    mRightTv.setText(R.string.common_all_seleted_text);
                }
                holder2.mCornerImageView.invalidate();
                mCheckedNum2 = mCheckedNum2 < 0 ? 0 : mCheckedNum2;
                mTitleTv.setText("已选中" + mCheckedNum2 + "项");
                break;
        }
    }

    /**
     * 删除
     */
    private void deleteItem() {
        switch (mTabIdPage) {
            case 0:
                Iterator it = mThemeList.iterator();
                while (it.hasNext()) {
                    DownloadInfo next = (DownloadInfo) it.next();
                    if (next.getChecked()) {
                        String downloadPath = com.prize.prizethemecenter.ui.utils.FileUtils.getDownloadPathMine(next.themeID, 1);
                        if (downloadPath!=null) {
                            FileUtils.recursionDeleteFile(new File(downloadPath));
                        }
                        DBUtils.deleteDownloadById(next.getThemeID());
//                        MainApplication.getDownloadManager().notifyStateChanged(next);
                        DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_REFRESH,next.themeID);
                        it.remove();
                        mCheckedNum--;
                        if (mCheckedNum == 0) {
                            mDeleteRButton.setChecked(false);
                        }
                    }
                }
                mThemeAdapter.setData(mThemeList);
                mThemeAdapter.notifyDataSetChanged();
//                mCheckedNum = mThemeList.size();
                if (mThemeList.size()==0){
                    setNoDataBg();
                    mTitleTv.setVisibility(View.GONE);
                    return;
                }
                mTitleTv.setText("已选中" + 0 + "项");
                break;
            case 1:
                Iterator it1 = mWallList.iterator();
                while (it1.hasNext()) {
                    DownloadInfo next = (DownloadInfo) it1.next();
                    if (next.getChecked()) {
                        String downloadPath = com.prize.prizethemecenter.ui.utils.FileUtils.getDownloadPathMine(next.themeID, 2);
                        if (downloadPath!=null) {
                            FileUtils.recursionDeleteFile(new File(downloadPath));
                        }
                        DBUtils.deleteDownloadById(next.getThemeID());
//                        MainApplication.getDownloadManager().notifyStateChanged(next);
                        DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_REFRESH, next.themeID);
                        it1.remove();
                        mCheckedNum1--;
                        if (mCheckedNum1 == 0) {
                            mDeleteRButton.setChecked(false);
                        }
                    }
                }
                mWallPaperAdapter.setData(mWallList);
                mWallPaperAdapter.notifyDataSetChanged();
//                mCheckedNum1 = mWallList.size();
                if (mWallList.size()==0){
                    setNoDataBg();
                    mTitleTv.setVisibility(View.GONE);
                    return;
                }
                mTitleTv.setText("已选中" + 0 + "项");
                break;
            case 2:
                Iterator it2 = mFontList.iterator();
                while (it2.hasNext()) {
                    DownloadInfo next = (DownloadInfo) it2.next();
                    if (next.getChecked()) {
                        // 从集合中删除上一次next方法返回的元素
                        String downloadPath = com.prize.prizethemecenter.ui.utils.FileUtils.getDownloadPathMine(next.themeID, 3);
                        if (downloadPath!=null) {
                            FileUtils.recursionDeleteFile(new File(downloadPath));
                        }
                        DBUtils.deleteDownloadById(next.getThemeID());
//                        MainApplication.getDownloadManager().notifyStateChanged(next);
                        DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_REFRESH, next.themeID);
                        it2.remove();
                        mCheckedNum2--;
                        if (mCheckedNum2 == 0) {
                            mDeleteRButton.setChecked(false);
                        }
                    }
                }
                mFontAdapter.setData(mFontList);
                mFontAdapter.notifyDataSetChanged();
                if (mFontList.size()==0){
                    setNoDataBg();
                    mTitleTv.setVisibility(View.GONE);
                    return;
                }
                mTitleTv.setText("已选中" + 0 + "项");
                break;
        }
    }

    /**
     * 全选
     */
    private void selectAll() {
        switch (mTabIdPage) {
            case 0:
                if (mThemeList.size() == 0) return;
                for (int i = 0; i < mThemeList.size(); i++) {
                    mThemeList.get(i).setChecked(true);
                }
                mCheckedNum = mThemeList.size();
                break;
            case 1:
                if (mWallList.size() == 0) return;
                for (int i = 0; i < mWallList.size(); i++) {
                    mWallList.get(i).setChecked(true);
                }
                mCheckedNum1 = mWallList.size();
                break;
            case 2:
                if (mFontList.size() == 0) return;
                for (int i = 0; i < mFontList.size(); i++) {
                    mFontList.get(i).setChecked(true);
                }
                mCheckedNum2 = mFontList.size();
                break;
        }
        mDeleteRButton.setChecked(true);
        dataChanged();
    }

    /**
     * 反选
     */
    private void selectNone() {
        switch (mTabIdPage) {
            case 0:
                if (mThemeList.size() == 0) return;
                for (int i = 0; i < mThemeList.size(); i++) {
                    if (mThemeList.get(i).getChecked()) {
                        mThemeList.get(i).setChecked(false);
                        mCheckedNum--;
                    } else {
                        mThemeList.get(i).setChecked(true);
                        mCheckedNum++;
                    }
                }
                break;
            case 1:
                if (mWallList.size() == 0) return;
                for (int i = 0; i < mWallList.size(); i++) {
                    if (mWallList.get(i).getChecked()) {
                        mWallList.get(i).setChecked(false);
                        mCheckedNum1--;
                    } else {
                        mWallList.get(i).setChecked(true);
                        mCheckedNum1++;
                    }
                }
                break;
            case 2:
                if (mFontList.size() == 0) return;
                for (int i = 0; i < mFontList.size(); i++) {
                    if (mFontList.get(i).getChecked()) {
                        mFontList.get(i).setChecked(false);
                        mCheckedNum2--;
                    } else {
                        mFontList.get(i).setChecked(true);
                        mCheckedNum2++;
                    }
                }
                break;
        }
        mDeleteRButton.setChecked(false);
        dataChanged();
    }

    public void setResult() {
        Intent intent = new Intent();
        intent.setClass(MineEditActivity.this, MineActivity.class);
        switch (mTabIdPage) {
            case 0:
                intent.putExtra(MineActivity.mTabId, mThemeList);
                mThemeAdapter.notifyDataSetChanged();
                if (mThemeList.size()==0){
                    setNoDataBg();
                }
                break;
            case 1:
                intent.putExtra(MineActivity.mTabId, mWallList);
                mWallPaperAdapter.notifyDataSetChanged();
                if (mWallList.size()==0){
                    setNoDataBg();
                }
                break;
            case 2:
                intent.putExtra(MineActivity.mTabId, mFontList);
                mFontAdapter.notifyDataSetChanged();
                if (mFontList.size()==0){
                    setNoDataBg();
                }
                break;
        }
        setResult(mTabIdPage, intent);
        this.finish();
    }

    private void dataChanged() {
        // 通知GridView刷新
        switch (mTabIdPage) {
            case 0:
                mThemeAdapter.notifyDataSetChanged();
                if (mThemeList.size()==0){
                    setNoDataBg();
                }
                break;
            case 1:
                mWallPaperAdapter.notifyDataSetChanged();
                if (mWallList.size()==0){
                    setNoDataBg();
                }
                break;
            case 2:
                mFontAdapter.notifyDataSetChanged();
                if (mFontList.size()==0){
                    setNoDataBg();
                }
                break;
        }
    }

    private void setNoDataBg() {
//        Drawable drawable = this.getDrawable(R.drawable.mine_load_bg);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(410, 300);
//        params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
//        mGridView.setLayoutParams(params);
//        mGridView.setBackground(drawable);
        mGridView.setEnabled(false);
        mRightTv.setEnabled(false);
        mRightTv.setClickable(false);
        mMineLoadedDeleteRl.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
