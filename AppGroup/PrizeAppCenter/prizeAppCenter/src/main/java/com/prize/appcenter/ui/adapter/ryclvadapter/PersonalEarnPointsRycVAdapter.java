package com.prize.appcenter.ui.adapter.ryclvadapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CustomImageView;
import com.prize.appcenter.ui.widget.progressbutton.EarnPointProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 赚取积分adapter
 */
public class PersonalEarnPointsRycVAdapter extends
        RecyclerView.Adapter<PersonalEarnPointsRycVAdapter.ViewHolder> {

    protected static final String TAG = "PersonalEarnPointsRycVAdapter";

    private DownDialog mDownDialog;

    private ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private IUIDownLoadListenerImp listener = null;
    protected Handler mHandler;
    protected WeakReference<Activity> mActivities;

    public class ViewHolder extends RecyclerView.ViewHolder {
        //应用名称
        public TextView mTitle;
        //应用图标
        public CustomImageView mIcon;
        //应用大小
        TextView mSize;
        //应用描述
        TextView mDesc;
        //积分数目
        TextView mPointsNum;
        //下载应用
        EarnPointProgressButton mDownloadBtn;
        //        RelativeLayout mAppItemRlyt;
        public RelativeLayout game_download_Rlyt;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView
                    .findViewById(R.id.points_item_title_tv);
            mIcon = (CustomImageView) itemView
                    .findViewById(R.id.points_game_iv);
            mSize = (TextView) itemView
                    .findViewById(R.id.points_item_size_tv);
            mDesc = (TextView) itemView
                    .findViewById(R.id.points_item_brief);
            mPointsNum = (TextView) itemView
                    .findViewById(R.id.points_item_pointnum);
            mDownloadBtn = (EarnPointProgressButton) itemView
                    .findViewById(R.id.app_download_btn);
            mDownloadBtn.enabelDefaultPress(true);
//            mAppItemRlyt = (RelativeLayout) itemView
//                    .findViewById(R.id.earn_points_tem_rlyt);
            game_download_Rlyt = (RelativeLayout) itemView
                    .findViewById(R.id.game_download_Rlyt);


        }
    }

    public PersonalEarnPointsRycVAdapter(Activity activity) {
        mActivities = new WeakReference<Activity>(activity);
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
                if (DownloadState.STATE_DOWNLOAD_INSTALLED == state) {
                    MTAUtil.onPointDownloadSuccess(pkgName);
                }
                if (isActivity) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            updateView(pkgName);
                        }
                    });
                }
            }
        });

    }

    /**
     * 设置应用列表
     */
    public void setData(List<AppsItemBean> data) {
        if (data != null && data.size() > 0) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新应用列表到已有集合
     */
    public void addData(ArrayList<AppsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空所有item
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if (items != null && items.size() > 0) {

            return items.size();
        }
        //默认显示5个
        return 0;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final AppsItemBean itemBean = items.get(position);
        CommonUtils.formatAppPageInfo(itemBean, Constants.EARNPOINTS_GUI, Constants.LIST, position + 1);
        if (JLog.isDebug) {
            JLog.i(TAG,"onBindViewHolder-itemBean="+itemBean.name+"---"+itemBean.pageInfo);
        }
        final Activity mActivity = mActivities.get();
        if (mActivity == null) {
            return;
        }
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon,
                    viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
        } else {
            if ((itemBean.iconUrl != null)) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
            }
        }
        // 设置名字
        if (!TextUtils.isEmpty(itemBean.name)) {
            viewHolder.mTitle.setText(itemBean.name);
        } else {
            viewHolder.mTitle.setText("");
        }
        // 设置大小
        if (!TextUtils.isEmpty(itemBean.apkSizeFormat)) {
            viewHolder.mSize.setText(itemBean.apkSizeFormat);
        } else {
            viewHolder.mSize.setText("");
        }
        // 设置描述
        viewHolder.mDesc.setText(BaseApplication.curContext.getString(R.string.person_getted, itemBean.timesCount));
        // 设置单个积分
        if (itemBean.points != 0) {
            viewHolder.mPointsNum.setText("+" + itemBean.points);
        } else {
            viewHolder.mPointsNum.setText("");
        }

        viewHolder.mDownloadBtn.setGameInfo(itemBean);
        viewHolder.game_download_Rlyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.mDownloadBtn.performClick();
            }
        });

        viewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置下载点击
                final int state = AIDLUtils.getGameAppState(
                        itemBean.packageName, itemBean.id + "",
                        itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                        if (BaseApplication.isDownloadWIFIOnly()
                                && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mActivity, R.style.add_dialog);
                            }
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {
                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            ToastUtils.showToast(R.string.rule);
                                            UIUtils.downloadApp(itemBean);
                                            MTAUtil.onPointDownloadInstall(itemBean.name, itemBean.packageName);
                                            XutilsDAO.storeEarnPoints360Bean(itemBean);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                            }
                                            break;
                                    }
                                }
                            });
                        } else {
                            viewHolder.mDownloadBtn.onClick();
                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                            }
                        }

                        break;
                    default:
                        viewHolder.mDownloadBtn.onClick();
                        break;
                }
            }
        });

//        viewHolder.mAppItemRlyt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //点击进入详情页
//                AppsItemBean AppsItemBean = items.get(position);
//                if (AppsItemBean != null) {
//                    UIUtils.gotoAppDetail(AppsItemBean,
//                            AppsItemBean.id, mActivity);
//                    MTAUtil.onDetailClick(BaseApplication.curContext,
//                            AppsItemBean.name,
//                            AppsItemBean.packageName);
//                }
//            }
//        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_personal_earn_points_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        mRecyclerView = (RecyclerView) parent;
        return vh;
    }
    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }
    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private void updateView(String packageName) {

        if (mRecyclerView == null || mRecyclerView.getLayoutManager() == null)
            return;
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        //得到第一个可显示控件的位置，
        int visiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        int headerViewsCount = 1;//此处知道是1个头布局情况下
        int LastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            if (i < 0 || items.isEmpty() || i >= items.size()) {
                continue;
            }
            AppsItemBean bean = items.get(i);
            if (bean == null)
                continue;
            if (bean.packageName.equals(packageName)) {
                View subView = mRecyclerView.getChildAt(i + headerViewsCount - visiblePosition);
                if (subView != null) {
                    View viewBtn = subView.findViewById(R.id.app_download_btn);
                    if (viewBtn != null) {
                        viewBtn.invalidate();
                    }
                }
            }
        }
    }
}
