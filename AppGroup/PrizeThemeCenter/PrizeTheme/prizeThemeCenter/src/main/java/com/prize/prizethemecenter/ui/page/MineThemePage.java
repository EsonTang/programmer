package com.prize.prizethemecenter.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MineLocalActivity;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.ui.adapter.MineThemeAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import org.xutils.ex.DbException;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 已下载主题
 */
public class MineThemePage extends BasePage implements View.OnClickListener {

    private String TAG = "MineThemePage";
    public GridViewWithHeaderAndFooter mGridView;
    public MineThemeAdapter mThemeAdapter;
    private RelativeLayout mLocalRl;
    public RelativeLayout mBgRl;
    private ArrayList<DownloadInfo> list;
    private View headView;
    private TextView mHeadTitleTv;
    private TextView mGridViewTitle;
    private View footView;
    public ImageView mFootIv;

    public MineThemePage(RootActivity activity) {
        super(activity);
    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.mine_theme_page, null);
        root.setBackgroundColor(activity.getResources().getColor(R.color.white));
        headView = inflater.inflate(R.layout.mine_local_head, null, false);
        footView = inflater.inflate(R.layout.mine_local_foot, null, false);
        mHeadTitleTv = (TextView) headView.findViewById(R.id.mine_headView_title_tv);
        mGridViewTitle = (TextView) headView.findViewById(R.id.hot_topic_tv);
//        mFootIv = (ImageView) footView.findViewById(R.id.mine_foot_tv);
        mLocalRl = (RelativeLayout) headView.findViewById(R.id.mine_local_head_rl);
        mGridView = (GridViewWithHeaderAndFooter) root.findViewById(R.id.gridview);
        mBgRl = (RelativeLayout) root.findViewById(R.id.rl_default);
        mHeadTitleTv.setText(R.string.mine_local_theme);
//        mHeadTitleTv.setTextColor(R.color.text_color_33cccc);
//        mHeadTitleTv.setTextColor(activity.getResources().getColor(R.color.text_color_33cccc));
        mThemeAdapter = new MineThemeAdapter(activity,false);
        mGridViewTitle.setText(R.string.common_downloaded_text);
//        mFootTv.setText(R.string.common_not_data);
//        mFootIv.setImageResource(R.drawable.mine_load_bg);
        mGridView.addHeaderView(headView);
//        mGridView.addFooterView(footView);
        mLocalRl.setOnClickListener(this);
        mGridView.setFocusable(false);
        mGridView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        return root;
    }

    @Override
    public void loadData() {
        initData();
        mGridView.setAdapter(mThemeAdapter);
    }

    private void initData() {
        try {
            list = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(1);
            JLog.d(TAG, "run: " + list.size());
        } catch (DbException pE) {
            pE.printStackTrace();
        }
        mThemeAdapter.setData(list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mine_local_head_rl:
                UIUtils.startActivity(activity, MineLocalActivity.class);
                break;
        }
    }

    @Override
    public void onResume() {
        initData();
        mThemeAdapter.notifyDataSetChanged();
        super.onResume();
    }


    public ArrayList<DownloadInfo> getList() {
        return list;
    }


    @Override
    public void showHistory() {

    }

    @Override
    public void addToHistory(String text) {

    }

    @Override
    public void onActivityCreated() {

    }

    @Override
    public String getPageName() {
        return null;
    }

    @Override
    public void onDestroy() {

    }

}
