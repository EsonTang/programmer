package com.prize.prizethemecenter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.AppDownloadQueenData;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.ui.adapter.DownloadQueenListViewAdapter;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.StateBarUtils;
import com.prize.prizethemecenter.ui.utils.StringUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DownLoadQueenActivity extends FragmentActivity implements View.OnClickListener {

    @InjectView(R.id.lv_queenlist)
    ListView lvQueenlist;
    @InjectView(R.id.iv_dafaultImg)
    ImageView ivDafaultImg;
    @InjectView(R.id.tv_default)
    TextView tvDefault;
    @InjectView(R.id.rl_default)
    RelativeLayout rlDefault;


    @InjectView(R.id.action_back)
    TextView actionBack;
    @InjectView(R.id.header_edt)
    RelativeLayout headerEdt;
    @InjectView(R.id.bt_download)
    Button btDownload;

    private List<SingleThemeItemBean.ItemsBean> downloadFinishInfos = new ArrayList<>();//完成下载的列表
    private List<SingleThemeItemBean.ItemsBean> downloadingInfos = new ArrayList<>();//未完成下载的列表
    private List<DownloadInfo> downloadInfoList = new ArrayList<>();//下载列表中的所有下载对象

    public static final String TYPE = "type";
    public static final String DATA = "data";
    private DownloadQueenListViewAdapter adapter;
    private PopupWindow pop;
    private View headView;
    private boolean hasHeadView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        setContentView(R.layout.downloadqueen_layout);
        StateBarUtils.changeStatus(getWindow());
        ButterKnife.inject(this);
        adapter = new DownloadQueenListViewAdapter(this);
        headView = this.getLayoutInflater().inflate(R.layout.loading_title_layout,null);
        setOnClicListener();
//        initData();
    }

    public void addHeadView() {
        if (!hasHeadView) {
            lvQueenlist.addHeaderView(headView);
            hasHeadView = true;
        }
    }

    public void removeHeadView() {
        if (hasHeadView) {
            lvQueenlist.removeHeaderView(headView);
            hasHeadView = false;
        }
    }

    private void setOnClicListener() {
        headerEdt.setOnClickListener(this);
        actionBack.setOnClickListener(this);
        btDownload.setOnClickListener(this);
    }

    private void initData(){
        downloadInfoList = DBUtils.findAllDownloadTask();

        downloadingInfos = getDownloadingList(downloadInfoList);
        downloadFinishInfos = DBUtils.findDownloadCompleteTask();

        AppDownloadQueenData queenData = new AppDownloadQueenData();
        ArrayList<HashMap<String, Object>> downloadingData = new ArrayList<>();
        ArrayList<HashMap<String, Object>> downloadedData = new ArrayList<>();

        if(downloadingInfos != null && downloadingInfos.size() >0){
            for (SingleThemeItemBean.ItemsBean info : downloadingInfos) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(TYPE, DownloadQueenListViewAdapter.DOWNLOADING_DATA);
                map.put(DATA, info);
                downloadingData.add(map);
            }
            addHeadView();
            queenData.setDownloadingData(downloadingData);
        }else{
            removeHeadView();
        }

        if(downloadFinishInfos != null && downloadFinishInfos.size() >0){
            HashMap<String, Object> map = new HashMap<>();
            map.put(TYPE, DownloadQueenListViewAdapter.DIVIDE);
            map.put(DATA, null);
            downloadedData.add(map);
            adapter.sethasDownLoaded(true);
            for(SingleThemeItemBean.ItemsBean info : downloadFinishInfos){
                int state = AppManagerCenter.getGameAppState(info, info.getType());
                if(state == AppManagerCenter.APP_STATE_UNEXIST){
                    downloadedData.remove(info);
                    DBUtils.deleteDownloadById(info.getId(),info.getType());
                }else{
                    HashMap<String, Object> map2 = new HashMap<>();
                    map2.put(TYPE, DownloadQueenListViewAdapter.DOWNLOADED_DATA);
                    map2.put(DATA, info);
                    downloadedData.add(map2);
                }
            }
            queenData.setDownloadedData(downloadedData);

        }
        if (downloadFinishInfos.size() > 0 || downloadingInfos.size()>0 ) {
            isShowDefaultView(false);
        } else {
            isShowDefaultView(true);
        }
//        adapter.setDownloadQueenData(downloadInfoList);
        adapter.setDownLoadQueenData(queenData);
        lvQueenlist.setSelector(new ColorDrawable(Color.TRANSPARENT));
        lvQueenlist.setAdapter(adapter);
    }

    private ArrayList<SingleThemeItemBean.ItemsBean> getDownloadingList(List<DownloadInfo> downloadInfoList) {
        ArrayList<SingleThemeItemBean.ItemsBean> lists = new ArrayList<>();
        if(downloadInfoList != null){
            for(DownloadInfo info : downloadInfoList){
                if(info.getCurrentState() < 6){
                    SingleThemeItemBean.ItemsBean bean = new SingleThemeItemBean.ItemsBean();
                    bean.setId(info.getThemeID().substring(0,info.getThemeID().length()-1));
                    bean.setDownload_url(info.getDownloadUrl());
                    bean.setThumbnail(info.getThumbnail());
                    bean.setName(info.getTitle());
                    bean.setSize(StringUtils.formatFileSize(info.getTotaleSize()));
                    bean.setType(info.type);
                    lists.add(bean);
                }
            }
            return lists;
        }
        return null;
    }


    public void isShowDefaultView(boolean isShowDefaultView) {
        if (!isShowDefaultView) {
            lvQueenlist.setVisibility(View.VISIBLE);
            rlDefault.setVisibility(View.GONE);
        } else {
            lvQueenlist.setVisibility(View.GONE);
            rlDefault.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_edt:
            case R.id.action_back:
                onBackPressed();
                break;
            case R.id.bt_download:
                if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                    ToastUtils.showToast("登陆账号才能查看历史记录");
                    UIUtils.jumpToLoginActivity();
                }else {
                    Intent intent = new Intent(this, DownLoadHistoryActivity.class);
                    startActivity(intent);
                }
        }
    }


    @Override
    protected void onResume() {
        adapter.setDownlaodRefreshHandle();
        if(adapter != null){
            initData();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        adapter.removeDownLoadHandler();
        super.onPause();
    }
}
