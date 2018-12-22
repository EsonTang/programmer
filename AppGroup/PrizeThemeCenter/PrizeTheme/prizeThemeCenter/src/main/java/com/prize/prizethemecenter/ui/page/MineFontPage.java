package com.prize.prizethemecenter.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MineLocalFontActivity;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.ui.adapter.MineFontAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import org.xutils.ex.DbException;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 字体
 */
public class MineFontPage extends BasePage implements View.OnClickListener{

    private String TAG="MineFontPage";
    public GridViewWithHeaderAndFooter mGridView;
    private RelativeLayout mLocalRl;
    public RelativeLayout mBgRl;
    private View footView;
    public ImageView mFootIv;

    public ArrayList<DownloadInfo> getList() {
        return list;
    }

    private ArrayList<DownloadInfo> list;
    public MineFontAdapter mFontAdapter;

    public MineFontPage(RootActivity activity) {
        super(activity);
    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.font_page, null);
        root.setBackgroundColor(activity.getResources().getColor(R.color.white));
        View headView = inflater.inflate(R.layout.mine_local_head, null, false);
//        footView = inflater.inflate(R.layout.mine_local_foot, null, false);
        TextView viewById = (TextView) headView.findViewById(R.id.mine_headView_title_tv);
//        mFootIv = (ImageView) footView.findViewById(R.id.mine_foot_tv);
        TextView gridViewTitle = (TextView) headView.findViewById(R.id.hot_topic_tv);
        mLocalRl = (RelativeLayout) headView.findViewById(R.id.mine_local_head_rl);
        mGridView = (GridViewWithHeaderAndFooter) root.findViewById(R.id.gridview);
        mBgRl = (RelativeLayout) root.findViewById(R.id.rl_default);
        viewById.setText(R.string.mine_local_font);
        mFontAdapter = new MineFontAdapter(activity,false);
        gridViewTitle.setText(R.string.common_downloaded_text);
//        mFootIv.setImageResource(R.drawable.mine_load_bg);
        mGridView.addHeaderView(headView);
//        mGridView.addFooterView(footView);
        setClickListner();
        return root;
    }

    private void setClickListner() {
        mLocalRl.setOnClickListener(this);
    }

    @Override
    public void loadData() {
        initData();
        mGridView.setAdapter(mFontAdapter);
    }

    private void initData() {
        try {
            list = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(3);
            JLog.d(TAG, "run: "+list.size());
        } catch (DbException pE) {
            pE.printStackTrace();
        }
        mFontAdapter.setData(list);
    }

    @Override
    public void onResume() {
        initData();
        mFontAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mine_local_head_rl:
                UIUtils.startActivity(activity, MineLocalFontActivity.class);
                break;

        }
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
