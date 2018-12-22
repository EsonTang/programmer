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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppUpdateData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingListData;
import com.prize.app.threads.SingleThreadUpdateExecutor;
import com.prize.app.util.ApkUtils;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.SignUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.RecommandAppData;
import com.prize.appcenter.callback.IUpdateWatcherEtds;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.actionBar.ActionBarActivity;
import com.prize.appcenter.ui.adapter.AppDetailGridViewAdapter;
import com.prize.appcenter.ui.adapter.AppUpdateListAdapter;
import com.prize.appcenter.ui.datamgr.AppListDataManager;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.AppUpdateCache;
import com.prize.appcenter.ui.util.RecommendPoolUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.PrizeCommButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：个人中心-->应用更新
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppUpdateActivity extends ActionBarActivity {
    private final String TAG = "AppUpdateActivity";
    private AppUpdateListAdapter adapter;
    private AppUpdateListAdapter mFootAdapter;
    private ListView appListView;
    private List<AppsItemBean> data = new ArrayList<>();
    private List<AppsItemBean> totalData = new ArrayList<>();
    private DownDialog mDownDialog;
    private PrizeCommButton oneKey_install_Btn;
    private String from = null;
    private RelativeLayout mDefualtLyt;
    private RelativeLayout mDownLoadRlyt;
    private AppDetailGridViewAdapter mGridViewAdapter;
    private GridView releaGridView;
    private ListView mFootListView;
    private MyHander mHandler = new MyHander(this);
    private IUpdateWatcherEtds wather;
    private View footerView;
    private LinearLayout recommand_Llyt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_app_list);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());

        appListView = (ListView) findViewById(android.R.id.list);
        oneKey_install_Btn = (PrizeCommButton) findViewById(R.id.oneKey_install_Btn);
        oneKey_install_Btn.enabelDefaultPress(true);
        mDefualtLyt = (RelativeLayout) findViewById(R.id.default_Llyt);
        recommand_Llyt = (LinearLayout) findViewById(R.id.recommand_Llyt);
        ImageView default_head_icon = (ImageView) findViewById(R.id.default_head_icon);
        mDownLoadRlyt = (RelativeLayout) findViewById(R.id.oneKey_down_Rlyt);
        mDownLoadRlyt.setVisibility(View.VISIBLE);
        releaGridView = (GridView) findViewById(R.id.gridView_hot_id);
        if (AIDLUtils.mService == null) {
            mToken = AIDLUtils.bindToService(this, this);
        }
        footerView = LayoutInflater.from(this).inflate(R.layout.foot_appupdate, null);
        if (adapter == null) {
            adapter = new AppUpdateListAdapter(this);
        }
        adapter.setDownlaodRefreshHandle();
        appListView.setAdapter(adapter);
        appListView.addFooterView(footerView);
        mFootListView = (ListView) footerView.findViewById(R.id.mFootListView);
        init();
        setListener();
        if (AppUpdateCache.getInstance().getCache() != null && AppUpdateCache.getInstance().getCache().size() > 0) {
            data.addAll(AppUpdateCache.getInstance().getCache());
            totalData.clear();
            totalData.addAll(data);
            processData();
        } else {
            requestData();
        }
        default_head_icon.setBackgroundResource(R.drawable.no_update_icon);
    }

    private void init() {
        if (adapter == null) {
            adapter = new AppUpdateListAdapter(this);
        }
        if (mFootAdapter == null) {
            mFootAdapter = new AppUpdateListAdapter(this);
        }
        mFootListView.setAdapter(mFootAdapter);
        if (mGridViewAdapter == null) {
            mGridViewAdapter = new AppDetailGridViewAdapter(this);
            mGridViewAdapter.setNeedLoadImg(true);
        }
        releaGridView.setAdapter(mGridViewAdapter);
        adapter.setDownlaodRefreshHandle();
        wather = IUpdateWatcherEtds.getInstance();
        wather.setmCallBack(new IUpdateWatcherEtds.IUpdateWatcherEtdsCallBack() {

            @Override
            public void update(int number, List<String> imgs,
                               List<AppsItemBean> listItem) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.arg1 = number;
                msg.obj = listItem;
                mHandler.sendMessage(msg);
            }
        });
        AIDLUtils.registObserver(wather);
        if (getIntent() != null) {
            from = getIntent().getStringExtra("from");
        }
    }

    private void setListener() {
        OnScrollListener scrollListener = new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        };
        appListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, scrollListener));

        oneKey_install_Btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.nonet_connect);
                    return;
                }
                MTAUtil.oneKeyUpdate();
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    mDownDialog = new DownDialog(AppUpdateActivity.this,
                            R.style.add_dialog);
                    mDownDialog.show();
                    mDownDialog.setmOnButtonClic(new OnButtonClic() {

                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    startDownLoad();
                                    break;
                            }
                        }
                    });
                } else {
                    // oneKeyDown();
                    startDownLoad();
                }
            }
        });
        releaGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position < 0 || mGridViewAdapter == null || position >= mGridViewAdapter.getCount())
                    return;
                AppsItemBean bean = mGridViewAdapter.getItem(position);
                if (bean == null || bean.id == null) return;
                UIUtils.gotoAppDetail(bean, bean.id, AppUpdateActivity.this);
                MTAUtil.onNoAppUpdateRecommond(bean.name, position + 1);
            }
        });
        mFootListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position < 0 || mFootAdapter == null || position >= mFootAdapter.getCount())
                    return;
                AppsItemBean bean = mFootAdapter.getItem(position);
                if (bean == null || bean.id == null) return;
                UIUtils.gotoAppDetail(bean, bean.id, AppUpdateActivity.this);
            }
        });

    }

    private void requestData() {
        showWaiting();
        Intent intent = new Intent(this, PrizeAppCenterService.class);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 3);
        startService(intent);
    }

    @Override
    public String getActivityName() {

        return null;
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        synchronized (AppUpdateActivity.class) {
            switch (what) {
                case AppListDataManager.UPDATE_SUCCESS:
                    if (obj == null) {
                        loadingFailed(new ReloadFunction() {
                            @Override
                            public void reload() {
                                requestData();
                            }
                        });
                        return;
                    }
                    data = ((AppUpdateData) obj).apps;
                    JLog.i(TAG, "onBack-data.size()=" + data.size());
                    processData();
                    break;
                case AppListDataManager.UPDATE_FAILURE:
                    // Toast.makeText(this,this.getString(R.string.failure),
                    // Toast.LENGTH_SHORT).show();
                    break;
                case NetSourceListener.WHAT_NETERR:
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                    // removeFootView();
                    break;

                default:
                    break;
            }
        }
    }

    private void processData() {
        if (data != null && data.size() <= 0) {
            isShowDefaultView(true);
            requestRecommendData();
        } else {
            isShowDefaultView(false);
            if (data != null && data.size() > 4) {
                requestrecommandData();
            } else {
                if (footerView != null && appListView != null && appListView.getFooterViewsCount() > 0) {
                    appListView.removeFooterView(footerView);
                }
                initData();
            }
        }
    }

    private boolean isRequest = false;

    /**
     * 返回相关推荐
     */
    private void requestrecommandData() {
        if (isRequest) {
            return;
        }
        RequestParams entity = new RequestParams(Constants.GIS_URL + "/recommand/updateapps");
        entity.addBodyParameter("type", String.valueOf(1));
        XExtends.http().post(entity, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {// AppDetailRecommandData
                try {
                    JSONObject o = new JSONObject(result);
                    if (o.getInt("code") == 0) {
                        isRequest = true;
                        AppsKeyInstallingListData datas = GsonParseUtils.parseSingleBean(o.getString("data"), AppsKeyInstallingListData.class);
                        List<AppsItemBean> list = CommonUtils.filterInstalledNeedSize(datas.apps, 1);
                        if (JLog.isDebug) {
                            JLog.i(TAG, "requestrecommandData-list:" + list.size());
                        }
                        if (list == null || list.size() <= 0) {
                            if (footerView != null && appListView != null && appListView.getFooterViewsCount() > 0) {
                                appListView.removeFooterView(footerView);
                            }
                        } else {
                            totalData.addAll(list);
                            mFootAdapter.setData(list);
                        }
                        initData();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                initData();
                if (footerView != null && appListView != null && appListView.getFooterViewsCount() > 0) {
                    appListView.removeFooterView(footerView);
                }
            }
        });

    }

    /**
     * Desc: 请求无更新任务时的推荐内容
     */
    private void requestRecommendData() {
        if (isRequest)
            return;

        //类型(1:搜索首页 2：下载空白页 3：下载非空白页)
        RecommendPoolUtils.requestRecommendPoolData("2", null, null, new RecommendPoolUtils.RecommendPoolDataCallBack() {
            @Override
            public void getRecommendPoolData(boolean isRequestSuccess, RecommandAppData datas) {
                if (isRequestSuccess) {
                    isRequest = true;
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

                        hideWaiting();
                        if (itemdatas.size() < 4) {//推荐少于4个不显示
                            if (recommand_Llyt != null) {
                                recommand_Llyt.setVisibility(View.GONE);
                            }
                        } else {
                            mGridViewAdapter.setData(itemdatas);
                        }

                    }

                } else {
                    hideWaiting();
                    if (recommand_Llyt != null) {
                        recommand_Llyt.setVisibility(View.GONE);
                    }
                }
            }
        });


    }

    private void initData() {
        if (adapter == null) {
            adapter = new AppUpdateListAdapter(this);
        }
        adapter.setDownlaodRefreshHandle();
        SingleThreadUpdateExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                String totalSize = calTatolSize();
                if (TextUtils.isEmpty(totalSize))
                    return;
                StringBuilder content = new StringBuilder(
                        getString(R.string.oneKey_install))
                        .append(totalSize);
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = content.toString();
                    mHandler.sendMessage(msg);
                }
            }
        });

    }

    private String calTatolSize() {
        long total = 0;
        long realTotal = 0;
        if (totalData == null)
            return "";
        int size = totalData.size();
        if (size <= 0)
            return "";
        for (int i = 0; i < size; i++) {
            AppsItemBean bean = null;
            try {
                if (totalData == null)
                    return "";
                bean = totalData.get(i);
                if (bean.apkSize == null) {
                    continue;
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                finish();
            }
            if (bean == null)
                return null;
            long apkSize = Long.parseLong(bean.apkSize);
            total += apkSize;
            if (bean.appPatch != null) {
                String oldApkSource = ApkUtils.getSourceApkPath(BaseApplication.curContext, bean.packageName);
                if (JLog.isDebug) {
                    JLog.i(TAG, "calTatolSize-oldApkSource=" + oldApkSource);
                }
                if (!TextUtils.isEmpty(oldApkSource) && !TextUtils.isEmpty(bean.appPatch.fromApkMd5)) {
                    // 校验一下本地安装APK的MD5是不是和真实的MD5一致
                    if (SignUtils.checkMd5(oldApkSource, bean.appPatch.fromApkMd5)) {
                        realTotal += bean.appPatch.patchSize;
                    } else {
                        realTotal += apkSize;
                        bean.appPatch = null;
                    }
                } else {
                    realTotal += apkSize;
                    bean.appPatch = null;
                }
            } else {
                realTotal += apkSize;
            }
        }
        if (total == realTotal) {
            return CommonUtils.formatSize(realTotal, "#.0");
        }
        return CommonUtils.formatSize(realTotal, "#.0") + "(省" + CommonUtils.formatSize(total - realTotal, "#.0") + ")";
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.removeDownLoadHandler();
        }
        if (mFootAdapter != null) {
            mFootAdapter.removeDownLoadHandler();
        }
        if (mGridViewAdapter != null) {
            mGridViewAdapter.setActivity(false);
            mGridViewAdapter.removeDownLoadHandler();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (data != null) {
            data.clear();
        }
        AIDLUtils.unregistObserver(wather);
        if (wather != null) {
            wather.setmCallBack(null);
        }
        AIDLUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    @Override
    protected void initActionBar() {

        enableSlideLayout(false);
        findViewById(R.id.action_bar_tab).setVisibility(View.VISIBLE);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    // 返回
                    case R.id.back_IBtn:
                        onBackPressed();
                        break;
                    case R.id.action_bar_search:
                        UIUtils.goSearchActivity(AppUpdateActivity.this);
                        break;
                    case R.id.action_go_downQueen:
                        UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
                                AppUpdateActivity.this);
                        break;
                }
            }
        };
        // 增加点击返回的灵敏度
        findViewById(R.id.action_go_downQueen).setOnClickListener(
                onClickListener);
        findViewById(R.id.back_IBtn).setOnClickListener(onClickListener);
        findViewById(R.id.action_bar_search)
                .setOnClickListener(onClickListener);
        TextView title = (TextView) findViewById(R.id.bar_title);
        title.setText(R.string.update_app);
        title.setOnClickListener(onClickListener);
    }


    private void startDownLoad() {
        SingleThreadUpdateExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (totalData == null)
                    return;
                int noInstalledAppCount = 0;
                int size = totalData.size();
                int count = 0;
                for (int i = 0; i < size; i++) {
                    if (totalData == null || i >= totalData.size())
                        return;
                    AppsItemBean gameBean = totalData.get(i);
                    int state = AIDLUtils.getGameAppState(gameBean.packageName,
                            String.valueOf(gameBean.id), gameBean.versionCode);
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            UIUtils.downloadApp(gameBean);
                            break;
                        case AppManagerCenter.APP_STATE_DOWNLOADED:
                            if (!BaseApplication.isThird) {
                                UIUtils.downloadApp(gameBean);
                                break;
                            }
                        case AppManagerCenter.APP_STATE_DOWNLOADING:
                        case AppManagerCenter.APP_STATE_WAIT:
                            noInstalledAppCount++;
                            break;
                        default:
                            count++;
                            if (count == size) {
                                ToastUtils.showToast(R.string.all_app_isdowned);
                            }
                            break;
                    }
                }
                if (noInstalledAppCount == size) {
                    ToastUtils.showToast(R.string.downing);
                }
            }
        });

    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            UIUtils.gotoActivity(MainActivity.class, AppUpdateActivity.this);
        }
        super.onBackPressed();
    }

    public void isShowDefaultView(boolean isShowDefaultView) {
        if (isShowDefaultView) {
            mDefualtLyt.setVisibility(View.VISIBLE);
            mDownLoadRlyt.setVisibility(View.GONE);
        } else {
            mDefualtLyt.setVisibility(View.GONE);
            mDownLoadRlyt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        adapter.setDownlaodRefreshHandle();
        AIDLUtils.registObserver(wather);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setIsActivity(true);
            adapter.setDownlaodRefreshHandle();
        }
        if (mFootAdapter != null) {
            mFootAdapter.setIsActivity(true);
            mFootAdapter.setDownlaodRefreshHandle();
        }
        if (mGridViewAdapter != null) {
            mGridViewAdapter.setActivity(true);
            mGridViewAdapter.setDownlaodRefreshHandle();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.setIsActivity(false);
        }
        if (mFootAdapter != null) {
            mFootAdapter.setIsActivity(false);
        }
        if (mGridViewAdapter != null) {
            mGridViewAdapter.setActivity(false);
        }
    }

    private static class MyHander extends Handler {
        private WeakReference<AppUpdateActivity> mActivities;

        MyHander(AppUpdateActivity mActivity) {
            this.mActivities = new WeakReference<AppUpdateActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null) return;
            final AppUpdateActivity activity = mActivities.get();
            if (activity != null) {
                //执行业务逻辑
                if (msg != null) {
                    if (msg.what == 0 && msg.obj != null && activity.oneKey_install_Btn != null) {
                        activity.oneKey_install_Btn.setCurrentText((String) msg.obj);
                        activity.hideWaiting();
                        if (activity.data != null && activity.adapter != null) {
                            activity.adapter.setData(activity.data);
                        }

                    }
                    if (msg.what == 1) {
                        if (msg.arg1 == -1) {
                            activity.loadingFailed(new ReloadFunction() {
                                @Override
                                public void reload() {
                                    activity.requestData();
                                }
                            });
                        } else {
                            if (msg.obj != null) {
                                activity.data = (List<AppsItemBean>) msg.obj;
                                activity.totalData.clear();
                                activity.totalData.addAll(activity.data);
                                AppUpdateCache.getInstance().saveCache(activity.data);
                                activity.processData();
                            }
                        }
                    }
                }
            }
        }
    }
}
