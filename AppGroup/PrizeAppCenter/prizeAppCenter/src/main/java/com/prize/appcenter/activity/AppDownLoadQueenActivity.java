/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.threads.SingleThreadExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.AppDownloadQueenData;
import com.prize.appcenter.bean.RecommandAppData;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.DownloadQueenDefaultGridAdapter;
import com.prize.appcenter.ui.adapter.DownloadQueenListViewAdapter;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.RecommendPoolUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.DownloadfootertypeItem;
import com.tencent.stat.StatService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

/**
 * 类描述：下载队列
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppDownLoadQueenActivity extends ActionBarNoTabActivity {
    public static final String TYPE = "type";
    public static final String DATA = "data";
    private static final String TAG = "AppDownLoadQueenActivity";
    private static final int INITDATAS = 5;
    private static final int UPLOAD_WHAT = INITDATAS + 1;
    private static final int UPDATA_CALLBACK = UPLOAD_WHAT + 1;
    private static final int DOWNLOAD_TASK = UPDATA_CALLBACK + 1;
    //    private static final int MORE_DOWNED_DATA = DOWNLOAD_TASK + 1;
    //类型(1:搜索首页 2：下载空白页 3：下载非空白页)
    private static int DEFAULTPARM = 2;
    private static int FOOTERTYPEPARM = 3;
    private static int CURRENTSIZE = 0;
    /**
     * 下载队列有任务时候 底部的推荐位
     **/
    public DownloadfootertypeItem mDownloadfooter;
    //控制默认页和加载页不同是出现
    public boolean hasDdefaultMoreBgView;
    public int mFirstsize;
    //是否adapter里面删除了全部已完成的数据
    public boolean isCancleAll;
    private RelativeLayout mDdefaultRlyt;
    private ArrayList<AppsItemBean> downLoadedDatas;
    private ArrayList<AppsItemBean> downLoadingDatas;
    private ListView mListView;
    private DownloadQueenListViewAdapter mAdapter;
    private View headView;
    private boolean hasHeadView;
    private boolean hasFooterView;
    /**
     * 无数据显示布局
     **/
    private View mDdefaultMoreBg;
    private TextView mDownloadNoItemBtn;
    private DownloadQueenDefaultGridAdapter mGridAdapter;
    private boolean isFistCome;
    private boolean isClickAddMoreBtn;
    private IUIDownLoadListenerImp refreshHanle;
    private boolean isRunnIng = false;
    private AppsItemBean mFirstApp;
    private boolean isRefresh;
    private boolean isNeedReresh = true;
    private String from = null;
    private Handler mHandler = new MyHander(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setNeedAddWaitingView(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloadqueen_layout);
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(this.getString(R.string.download_quene));
        isFistCome = true;
        isClickAddMoreBtn = false;
        initView();

        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String arg0, int state, boolean isNewDownload) {
                if (JLog.isDebug) {
                    JLog.i(TAG, "setmCallBack-(0,2,6,7才执行）实际状态=" + state + "--arg0=" + arg0 + "--isNewDownload=" + isNewDownload);
                }
                if (isRunnIng) {
                    return;
                }
                if (state == DownloadState.STATE_DOWNLOAD_WAIT && !isNewDownload) {
                    return;
                }
                switch (state) {
                    case DownloadState.STATE_DOWNLOAD_WAIT:
//                    case DownloadState.STATE_DOWNLOAD_SUCESS:
                    case DownloadState.STATE_DOWNLOAD_ERROR:
                    case DownloadState.STATE_DOWNLOAD_INSTALLED:
                    case DownloadState.STATE_DOWNLOAD_CANCEL:
                    case DownloadState.STATE_DOWNLOAD_REFRESH:
                        changeFloatViewStates();
                        break;

                }

            }

        });

        AIDLUtils.registerCallback(refreshHanle);
        if (getIntent() != null) {
            from = getIntent().getStringExtra("from");
        }
    }

    private TextView moneKeyDown;

    private void initView() {
        if (getIntent() != null) {
            from = getIntent().getStringExtra(Constants.FROM);
        }
        mDdefaultRlyt = (RelativeLayout) findViewById(R.id.defalutRlyt_id);
        //无下载的推荐位
        mDdefaultMoreBg = findViewById(R.id.download_default_more_rlyt);
        mDownloadNoItemBtn = (TextView) findViewById(R.id.download_no_item_btn);

        GridView mDdefaultMoreGridView = (GridView) findViewById(R.id.download_default_more_grideView);
        mGridAdapter = new DownloadQueenDefaultGridAdapter(this);
        mGridAdapter.setDownlaodRefreshHandle();
        mDdefaultMoreGridView.setAdapter(mGridAdapter);

        mListView = (ListView) findViewById(R.id.queenlist_id);
        mAdapter = new DownloadQueenListViewAdapter(this);
        headView = this.getLayoutInflater().inflate(
                R.layout.loading_title_layout, null);
        moneKeyDown = (TextView) headView.findViewById(R.id.all_suspended_id);
        if (AIDLUtils.mService == null) {
            mToken = AIDLUtils.bindToService(this, this);
        }
        setHeadListener();
    }


    public void addHeadView() {
        if (!hasHeadView) {
            if (Build.VERSION.SDK_INT <= 17) {
                mListView.setAdapter(null);
                mListView.addHeaderView(headView);
                mListView.setAdapter(mAdapter);
            } else {
                mListView.addHeaderView(headView);
            }
            hasHeadView = true;
        }
    }

    public void removeHeadView() {
        if (hasHeadView) {
            mListView.removeHeaderView(headView);
            hasHeadView = false;
        }
    }

    public void addFooterView() {
        if (!hasFooterView) {
            mListView.addFooterView(mDownloadfooter);
            hasFooterView = true;
        }
    }

    public void removeFooterView() {
        if (hasFooterView && mDownloadfooter != null) {
            mListView.removeFooterView(mDownloadfooter);
            hasFooterView = false;
        }
    }

    /**
     * Desc: footerview
     * <p/>
     * Created by huangchangguo
     * Date:  2016/9/14 14:38
     */

    private void processFooterType() {

        String AppId = null;
        String cardId = null;
        if (mFirstApp != null) {
            AppId = mFirstApp.id;
            cardId = mFirstApp.cardId;
        }
        requestRecommendData(String.valueOf(FOOTERTYPEPARM), cardId, AppId);

        mDownloadfooter.setAddMoreOnClickListener(new DownloadfootertypeItem.OnclickLinstener() {
            @Override
            public void AddMoreListener() {
                //点击加载更多安装记录
                if (CommonUtils.isFastDoubleClick())
                    return;
                isClickAddMoreBtn = true;
                mDownloadfooter.setAddMoreVisibility(false);
                initData();
            }
        });


    }

    private void setHeadListener() {
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        mAdapter.setIsActivity(true);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        mAdapter.setIsActivity(true);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                        mAdapter.setIsActivity(false);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        }));

        mDownloadNoItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainApplication.curContext, MainActivity.class);
                intent.putExtra("position", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.curContext.startActivity(intent);
                LocalBroadcastManager.getInstance(AppDownLoadQueenActivity.this).sendBroadcast(new Intent(RootActivity.FINISH_ACTION));
            }
        });
        moneKeyDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClientInfo.getAPNType(AppDownLoadQueenActivity.this) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    return;
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(AppDownLoadQueenActivity.this) != ClientInfo.WIFI) {
                    mDownDialog = new DownDialog(AppDownLoadQueenActivity.this, R.style.add_dialog);
                    mDownDialog.show();
                    mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    AIDLUtils.continuePauseTask(AppDownLoadQueenActivity.this);
                                    moneKeyDown.setEnabled(false);
                                    break;
                            }
                        }
                    });
                } else {
                    AIDLUtils.continuePauseTask(AppDownLoadQueenActivity.this);
                    moneKeyDown.setEnabled(false);
                }
            }
        });
    }

    private DownDialog mDownDialog;

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setDownlaodRefreshHandle();
        if (mAdapter != null && AIDLUtils.mService != null) {
            isRefresh = false;
            initData();
        }
        mAdapter.setIsActivity(true);
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFistCome = false;
        mAdapter.setIsActivity(false);
        StatService.onPause(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isClickAddMoreBtn", isClickAddMoreBtn);
        outState.putBoolean("isFistCome", isFistCome);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        isClickAddMoreBtn = savedInstanceState.getBoolean("isClickAddMoreBtn");
        isFistCome = savedInstanceState.getBoolean("isFistCome");
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 是否显示无数据时的界面
     *
     * @param isShow boolean
     */
    public void isShowDefaultView(boolean isShow) {
        if (isShow) {
            mListView.setVisibility(View.GONE);
            mDdefaultRlyt.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mDdefaultRlyt.setVisibility(View.GONE);
        }

    }

    /**
     * 查询下载任务的个数
     */
    private synchronized void changeFloatViewStates() {
        if (JLog.isDebug) {
            JLog.i(TAG, "changeFloatViewStates");
        }
        if (isRunnIng) {
            return;
        }
        //数据库返回不刷新
        isRefresh = !isCancleAll;
        //重新加载下载队列的数据
        initData();
    }

    @Override
    public String getActivityName() {
        return "AppDownLoadQueenActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
    }


    private void initData() {
        if (isNeedReresh) {
            if (AIDLUtils.hasPauseTaskMoreTwo()) {
                moneKeyDown.setVisibility(View.VISIBLE);
            } else {
                moneKeyDown.setVisibility(View.GONE);
            }
            isNeedReresh = false;
        }


        SingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (JLog.isDebug) {
                    JLog.i(TAG, "initData下run()方法执行=");
                }
                if (isRunnIng) {
                    return;
                }
                AppDownloadQueenData queenData = new AppDownloadQueenData();
                downLoadedDatas = (ArrayList<AppsItemBean>) AIDLUtils.getHasDownloadedAppList();
                //加上同步块 java.util.ConcurrentModificationException
                downLoadingDatas = (ArrayList<AppsItemBean>) AIDLUtils.getDownloadAppList();
                if (JLog.isDebug) {//友盟上：java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference
                    if (downLoadingDatas != null && downLoadedDatas != null) {
                        JLog.i(TAG, "initData下载任务个数=" + downLoadingDatas.size() + "---已下载个数=" + downLoadedDatas.size());
                    }
                }
                List<HashMap<String, Object>> downloadingData = new ArrayList<>();
                List<HashMap<String, Object>> downloadedData = new ArrayList<HashMap<String, Object>>();
                Message msg = Message.obtain();
                msg.what = INITDATAS;

                try {
                    if (downLoadingDatas != null && downLoadingDatas.size() > 0) {
                        for (AppsItemBean data : downLoadingDatas) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(TYPE, DownloadQueenListViewAdapter.DOWNLOADING_DATA);
                            map.put(DATA, data);
                            downloadingData.add(map);
                        }
                        msg.arg1 = downLoadingDatas.size();
                        if (queenData == null) {
                            return;
                        }
                        queenData.setDownloadingData(downloadingData);
                    } else {
                        msg.arg1 = 0;
                        if (queenData == null) {
                            return;
                        }
                        queenData.setDownloadingData(downloadingData);//20170629 modify
                    }
                    if (downLoadedDatas != null && downLoadedDatas.size() > 0) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put(TYPE, DownloadQueenListViewAdapter.DIVIDE);
                        map.put(DATA, null);
                        downloadedData.add(map);
                        mAdapter.sethasDownLoaded(true);
                        for (AppsItemBean data : downLoadedDatas) {
                            int state = AIDLUtils.getGameAppState(data.packageName,
                                    data.id, data.versionCode);
                            if (state == AppManagerCenter.APP_STATE_UNEXIST || state == AppManagerCenter.APP_STATE_UPDATE) {
                                downloadedData.remove(data);
                                AIDLUtils.deleteSingle(data.packageName);
                            } else {
                                HashMap<String, Object> map2 = new HashMap<String, Object>();
                                map2.put(TYPE, DownloadQueenListViewAdapter.DOWNLOADED_DATA);
                                map2.put(DATA, data);
                                downloadedData.add(map2);
                            }
                        }
                        if (JLog.isDebug) {//友盟上：java.lang.NullPointerException: Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference
                            if (downLoadingDatas != null && downLoadedDatas != null) {
                                JLog.i(TAG, "initData下载isClickAddMoreBtn=" + isClickAddMoreBtn + "---isFistCome=" + isFistCome);
                            }
                        }
                        //如果添加更多没有点击,收起加载更多
                        if (!isClickAddMoreBtn) {
//                            if (isFistCome) {
                            int size = downloadedData.size();
                            //如果大于3则收起，不大于3则展开
                            if (size > 3) {
                                //第一次去掉的size，保存。用作判断是否需要显示点击更多按钮
                                mFirstsize = size - 3;
                                // downloadedData第一个位置是分割线
                                for (int i = size - 1; i >= 3; i--) {
                                    downloadedData.remove(i);
                                }
                            } else {
                                //size小于等于3，做一个标记
                                downloadedData.get(0).put(DATA, 0);
                            }

                        }
                        if (queenData != null) {
                            queenData.setDownloadedData(downloadedData);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                    finish();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    finish();
                }
                if (mHandler == null || queenData == null)
                    return;
                mHandler.removeMessages(INITDATAS);
                msg.arg2 = queenData.size();
                msg.obj = queenData;
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     * Desc: 请求无下载任务时的推荐内容|底部的推荐内容
     * 2.1下载优化
     * Created by huangchangguo
     * Date:  2016/9/8 9:36
     */
    private void requestRecommendData(final String type, String catId, String appId) {
        if (isRefresh)
            return;

        //类型(1:搜索首页 2：下载空白页 3：下载非空白页)
        RecommendPoolUtils.requestRecommendPoolData(type, catId, appId, new RecommendPoolUtils.RecommendPoolDataCallBack() {
            @Override
            public void getRecommendPoolData(boolean isRequestSuccess, RecommandAppData datas) {
                if (isRequestSuccess) {
                    RecommandAppData Alldata = datas;
                    ArrayList<AppsItemBean> itemdatas = new ArrayList<>();
                    if (Alldata != null && Alldata.type1 != null && Alldata.type2 != null
                            && Alldata.type3 != null && Alldata.type4 != null) {
                        Alldata.type1 = CommonUtils.filterInstalled(Alldata.type1, 1);

                        itemdatas.addAll(Alldata.type1);
                        //第二个位置里面移除第一个位置相同的应用
                        Alldata.type2 = RecommendPoolUtils.filterSameApp(itemdatas, Alldata.type2);

                        Alldata.type2 = CommonUtils.filterInstalled(Alldata.type2, 1);
                        itemdatas.addAll(Alldata.type2);
                        //第三个位置里面移除前两个位置相同的应用
                        Alldata.type3 = RecommendPoolUtils.filterSameApp(itemdatas, Alldata.type3);

                        Alldata.type3 = CommonUtils.filterInstalled(Alldata.type3, 1);
                        itemdatas.addAll(Alldata.type3);
                        //第四个位置里面移除前三个位置相同的应用
                        Alldata.type4 = RecommendPoolUtils.filterSameApp(itemdatas, Alldata.type4);
                        Alldata.type4 = CommonUtils.filterInstalled(Alldata.type4, 1);
                        itemdatas.addAll(Alldata.type4);

                        //if =2,默认无下载界面情况的推荐位数据
                        if (type.contains(String.valueOf(DEFAULTPARM))) {
                            mGridAdapter.setData(itemdatas);
                            mDdefaultMoreBg.setVisibility(View.VISIBLE);
                            hasDdefaultMoreBgView = true;


                        } else if (type.contains(String.valueOf(FOOTERTYPEPARM))) {
                            //if=3 这里是有下载任务时候的推荐位数据
                            if (mDownloadfooter != null) {
                                mDownloadfooter.setData(itemdatas, mFirstApp.name);
                                mDownloadfooter.setFooterVisibility(true);

                            }
                        }
                    }

                }
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        isRunnIng = true;
//        SingleThreadExecutor.getInstance().cancelTask();
        AIDLUtils.unregisterCallback(refreshHanle);
        refreshHanle.setmCallBack(null);
        refreshHanle = null;
        if (mDownloadfooter != null) {
            mDownloadfooter.unBindregisterCallback();
            mDownloadfooter = null;
        }
        if (mAdapter != null) {
            mAdapter.removeDownLoadHandler();
        }
        if (downLoadedDatas != null) {
            downLoadedDatas.clear();
            downLoadedDatas = null;
        }
        if (downLoadingDatas != null) {
            downLoadingDatas.clear();
            downLoadingDatas = null;
        }
        mGridAdapter.removeDownLoadHandler();
//        if (queenData != null) {
//            queenData = null;
//        }
        AIDLUtils.unbindFromService(mToken);

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AIDLUtils.registerCallback(refreshHanle);
        mAdapter.setDownlaodRefreshHandle();
        mGridAdapter.setDownlaodRefreshHandle();
        if (mDownloadfooter != null) {
            mDownloadfooter.setDownlaodRefreshHandle();
        }
        if (mAdapter != null && mAdapter.getCount() <= 0) {
            initData();
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
//            UIUtils.gotoActivity(MainActivity.class, AppDownLoadQueenActivity.this);
//        }
//        super.onBackPressed();
//    }


    /**
     * 处理获取的已下载以及下载中的数据
     *
     * @param msg Message
     */
    private void processDbDate(Message msg) {
        if (msg.arg1 > 0) {
            addHeadView();
        } else {
            removeHeadView();
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "processDbDate()方法正在下载msg.arg1=" + msg.arg1 + "--AppDownloadQueenData个数msg.arg2=" + msg.arg2);
        }
        //总共的下载任务
        if (msg.arg2 > 0) {
            //有数据的加载页面
            mAdapter.sethasDownLoaded(true);
            isShowDefaultView(false);
            hideWaiting();
        } else {
            //没有数据的逻辑再次更改为直接显示默认图
            hasDdefaultMoreBgView = true;
            //无数据的加载
            isShowDefaultView(true);
            hideWaiting();
            if (mDownloadfooter != null)
                removeFooterView();
            /*显示无下载的推荐内容*/
            //1：请求数据 2：展示数据
            //请求无下载内容的推荐位 type=2
            requestRecommendData(String.valueOf(DEFAULTPARM), null, null);
        }
        if (msg == null || msg.obj == null)
            return;
        AppDownloadQueenData queenData = (AppDownloadQueenData) msg.obj;
        if (isFistCome) {
            isFistCome = false;
            if (queenData.getDownloadingData().size() > 0 || queenData.getDownloadedData().size() > 0) {
                mDdefaultMoreBg.setVisibility(View.GONE);
                hasDdefaultMoreBgView = false;
                if (downLoadingDatas != null && downLoadingDatas.size() > 0) {
                    //匹配第一个
                    mFirstApp = downLoadingDatas.get(0);
                } else if (downLoadedDatas != null && downLoadedDatas.size() > 0) {
                    //当没有下载的时候，获得第一个已完成的应用信息
                    mFirstApp = downLoadedDatas.get(0);
                }
            }
        }
        //有数据的情况
        if (queenData.getDownloadingData().size() > 0 || queenData.getDownloadedData().size() > 0) {
            mAdapter.setDownLoadQueenData(queenData);
            //添加有数据的推荐数据,没有默认下载才添加
            if (!hasDdefaultMoreBgView) {
                if (mDownloadfooter == null) {
                    mDownloadfooter = new DownloadfootertypeItem(AppDownLoadQueenActivity.this);
                }
                addFooterView();
                //每次进来默认设置不可见
                if (queenData.getDownloadedData().size() > 0) {
                    if (queenData.getDownloadedData().get(0).get(DATA) == null && mFirstsize > 0 && !isClickAddMoreBtn) {
                        mDownloadfooter.setAddMoreVisibility(true);
                    } else
                        mDownloadfooter.setAddMoreVisibility(false);
                } else
                    mDownloadfooter.setAddMoreVisibility(false);


                /*显示无下载的推荐内容*/
                //1：请求数据 2：展示数据
                //请求有下载内容的推荐位 type=3
                processFooterType();
            }
        }
    }

    //    /**
//     * 处理获取更多已下载的数据
//     */
//    private void processMoreDownedDate() {
//        //有数据的情况
//        if (JLog.isDebug) {
//            JLog.i(TAG, "processMoreDownedDate.1下载中任务个数：" + queenData.getDownloadingData().size() + "--已下载个数：" + queenData.getDownloadedData().size());
//        }
//        if (queenData != null && queenData.getDownloadedData().size() > 0) {
//            mAdapter.setDownLoadQueenData(queenData);
//        }
//    }
    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    private static class MyHander extends Handler {
        private WeakReference<AppDownLoadQueenActivity> mActivities;

        public MyHander(AppDownLoadQueenActivity mActivity) {
            this.mActivities = new WeakReference<AppDownLoadQueenActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final AppDownLoadQueenActivity activity = mActivities.get();
            if (activity != null) {
                switch (msg.what) {
                    //下载队列初始化数据的(第一次进入)
                    case INITDATAS:
                        if (msg != null && msg.what == INITDATAS) {
                            //正在下载应用的个数
                            activity.processDbDate(msg);
                        }
                        activity.hideWaiting();
                        break;

                    case DOWNLOAD_TASK:// 回调下载任务
                        int size = msg.arg1;
                        //说明添加了下载任务
                        if (CURRENTSIZE < size) {
                            activity.mAdapter.notifyDataSetChanged();
                        }
                        CURRENTSIZE = size;
                        activity.hideWaiting();
                        break;

                }
            }
        }

    }

    @Override
    public void finish() {
        super.finish();
//        if (!TextUtils.isEmpty(from)) {
//            if("folder".equals(from))
//            UIUtils.gotoMainActivityNewTask(this);
//        }
        UIUtils.gotoMainActivity(this,from);
//        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
//            UIUtils.gotoActivity(MainActivity.class, AppDownLoadQueenActivity.this);
//        }
    }
}
