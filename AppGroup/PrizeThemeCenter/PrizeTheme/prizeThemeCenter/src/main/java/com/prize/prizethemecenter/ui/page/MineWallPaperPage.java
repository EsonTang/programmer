package com.prize.prizethemecenter.ui.page;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MineLocalWallpaperActivity;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.ui.adapter.MineWallPaperAdapter;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.GridViewWithHeaderAndFooter;

import org.xutils.ex.DbException;

import java.util.ArrayList;

/**
 * Created by Fanghui on 2016/11/14.
 * 已下载壁纸
 */
public class MineWallPaperPage extends BasePage implements View.OnClickListener{

    private String TAG="MineThemePage";
    public GridViewWithHeaderAndFooter mGridView;
    public MineWallPaperAdapter mWallPaperAdapter;
    private RelativeLayout mLocalRl;
    public RelativeLayout mBgRl;
    private ArrayList<DownloadInfo> list;
    public LinearLayout mMineWallLayout;
    public RelativeLayout mMineDeleteLayout;
    public View headView;
    private View footView;
    public ImageView mFootIv;

    public MineWallPaperPage(RootActivity activity) {
        super(activity);
    }
    public ArrayList<DownloadInfo> getList() {
        return list;
    }
    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.mine_theme_page, null);
        root.setBackgroundColor(activity.getResources().getColor(R.color.white));
        headView = inflater.inflate(R.layout.mine_local_head, null, false);
//        footView = inflater.inflate(R.layout.mine_local_foot, null, false);
        TextView viewById = (TextView) headView.findViewById(R.id.mine_headView_title_tv);
        TextView gridViewTitle = (TextView) headView.findViewById(R.id.hot_topic_tv);
        mLocalRl = (RelativeLayout) headView.findViewById(R.id.mine_local_head_rl);
//        mFootIv = (ImageView) footView.findViewById(R.id.mine_foot_tv);
        mGridView = (GridViewWithHeaderAndFooter) root.findViewById(R.id.gridview);
        mBgRl = (RelativeLayout) root.findViewById(R.id.rl_default);
        mMineWallLayout = (LinearLayout) headView.findViewById(R.id.mine_local_head_ll);
        mMineDeleteLayout = (RelativeLayout) root.findViewById(R.id.mine_loaded_delete_rl);
        viewById.setText(R.string.mine_local_wallpaper);
        gridViewTitle.setText(R.string.common_downloaded_text);
        mWallPaperAdapter = new MineWallPaperAdapter(activity,false);
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
        mGridView.setAdapter(mWallPaperAdapter);
    }

    private void initData() {
        try {
            list = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(2);
            JLog.d(TAG, "run: "+list.size());
        } catch (DbException pE) {
            pE.printStackTrace();
        }
        mWallPaperAdapter.setData(list);
    }

    @Override
    public void onResume() {
        initData();
        mWallPaperAdapter.setDownlaodRefreshHandle();
        mWallPaperAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mine_local_head_rl:
                UIUtils.startActivity(activity, MineLocalWallpaperActivity.class);
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
        mWallPaperAdapter.removeDownLoadHandler();
    }


}
