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

package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.bean.AppDownloadQueenData;
import com.prize.appcenter.ui.dialog.DelTaskDialog;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.util.UpdateDataUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.prize.appcenter.R.id.versionName_Tv;

/**
 * 类描述：
 *
 * @author huanglingjun
 * @version 版本
 */
public class DownloadQueenListViewAdapter extends BaseAdapter {
    private static String TAG = "DownloadQueenListViewAdapter";
    /**
     * 多种类型的item注意事项： 1、必须覆写getItemViewType(int position)方法和getViewTypeCount()方法
     * 2、type类型必须 从0开始递增 0、1、2、3....
     */
    private DownDialog mDownDialog;
    private ArrayList<HashMap<String, Object>> cacheData = new ArrayList<HashMap<String, Object>>();
    private AppDownloadQueenData queenData;
    protected WeakReference<AppDownLoadQueenActivity> mActivities;
    private static final String SIZE = "size";

    public static final int DOWNLOADING_DATA = 0;
    public static final int DOWNLOADED_DATA = 1;
    public static final int DIVIDE = 2;

    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true

    private boolean hasDownLoaded = false;
    private Handler mMainHandler = new Handler();

    private IUIDownLoadListenerImp listener;

    private Runnable DataSetChanged = new Runnable() {

        @Override
        public void run() {
            notifyDataSetChanged();
        }

    };

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    public DownloadQueenListViewAdapter(AppDownLoadQueenActivity mCtx) {
        super();
        mActivities = new WeakReference<AppDownLoadQueenActivity>(mCtx);
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
                if (JLog.isDebug) {
                    JLog.i(TAG, "实际状态=" + state + "--isNewDownload=" + isNewDownload + "--pkgName=" + pkgName);
                }
                if (isActivity) {
                    if (state == DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS) {
                        mMainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                updateView(pkgName);

                            }
                        });
                    } else {
//                        if (isNewDownload || state == DownloadState.STATE_DOWNLOAD_CANCEL || state == DownloadState.STATE_DOWNLOAD_ERROR ||
//                                state == DownloadState.STATE_DOWNLOAD_INSTALLED) {//因为在此状态下会重新填充数据（AppDownLoadQueenActivity.changeFloatViewStates（）方法）
//                            return;
//                        }
                        mMainHandler.removeCallbacks(DataSetChanged);
                        mMainHandler.post(DataSetChanged);
                    }
                }
            }
        });

    }


    public void setDownLoadQueenData(AppDownloadQueenData originQueenData) {
        if (this.queenData != null) {
            this.queenData.clear();
        }
        this.queenData = originQueenData;
        cacheData.clear();
        for (int i = 0; i < queenData.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(SIZE, 0);
            cacheData.add(map);
            if (JLog.isDebug) {
                JLog.i(TAG, "setDownLoadQueenData=" + queenData.get(i).get(AppDownLoadQueenActivity.DATA));
            }

        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return queenData == null ? 0 : queenData.size();
    }

    @Override
    public HashMap<String, Object> getItem(int position) {
        return queenData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= queenData.size()) {
            return DOWNLOADING_DATA;
        }
        return (int) queenData.get(position).get(AppDownLoadQueenActivity.TYPE);
    }

    @Override
    public int getViewTypeCount() {
        //return 5;
        return 3;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mListView = (ListView) parent;
        final AppDownLoadQueenActivity mActivity = mActivities.get();
        synchronized (DownloadQueenListViewAdapter.class) {
            if (queenData == null || queenData.size() <= 0 || mActivity == null)
                return convertView;
            final ViewHolderDownLoaded viewHolderDownLoaded;
            final ViewHolder viewHolder;
            final ViewHolderDevider viewHolderDevider;
            int itemViewType = getItemViewType(position);
            if (convertView != null) {
                convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            if (convertView == null) {
                switch (itemViewType) {
                    case DOWNLOADING_DATA:
                        viewHolder = new ViewHolder();
                        convertView = LayoutInflater.from(mActivity).inflate(
                                R.layout.item_downloading_list, parent, false);
                        viewHolder.mIcon = (ImageView) convertView
                                .findViewById(R.id.game_iv);
//                        viewHolder.item_Rlyt = (RelativeLayout) convertView
//                                .findViewById(R.id.item_Rlyt);
                        viewHolder.mName = (TextView) convertView
                                .findViewById(R.id.name_id);
                        viewHolder.mRadiu = (TextView) convertView
                                .findViewById(R.id.radiu_id);
                        viewHolder.mDownLoadSize = (TextView) convertView
                                .findViewById(R.id.downloadSize_id);
                        viewHolder.mTotlaSize = (TextView) convertView
                                .findViewById(R.id.totalSize_id);
                        viewHolder.mProgressbar = (ProgressBar) convertView
                                .findViewById(R.id.download_progressbar_id);
                        viewHolder.mDownloadBtn = (AnimDownloadProgressButton) convertView
                                .findViewById(R.id.game_download_btn);
                        viewHolder.mDelete = (Button) convertView
                                .findViewById(R.id.btn_delete_id);
//                        viewHolder.mDivide = convertView
//                                .findViewById(R.id.game_image_tag);
                        viewHolder.mPauseed = (TextView) convertView
                                .findViewById(R.id.pauseed_id);

                        convertView.setTag(viewHolder);
//                        convertView.setTag(R.id.id_adapter_downloading, viewHolder);
                        convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        getLoadingView(viewHolder, position);
                        break;

                    case DOWNLOADED_DATA:
                        viewHolderDownLoaded = new ViewHolderDownLoaded();
                        convertView = LayoutInflater.from(mActivity).inflate(
                                R.layout.item_downloaded_list, parent, false);
                        viewHolderDownLoaded.mIcon = (ImageView) convertView
                                .findViewById(R.id.game_iv);
                        viewHolderDownLoaded.mName = (TextView) convertView
                                .findViewById(R.id.name_id);
                        viewHolderDownLoaded.versionName_Tv = (TextView) convertView
                                .findViewById(versionName_Tv);
                        viewHolderDownLoaded.mDownloadBtn = (AnimDownloadProgressButton) convertView
                                .findViewById(R.id.game_download_btn);
                        viewHolderDownLoaded.isInstall = (TextView) convertView
                                .findViewById(R.id.isInstalled_id);
                        viewHolderDownLoaded.parent_Rlyt = (RelativeLayout) convertView
                                .findViewById(R.id.parent_Rlyt);
                        viewHolderDownLoaded.mDownloadBtn
                                .setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        if (viewHolderDownLoaded.mDownloadBtn != null) {
                                            viewHolderDownLoaded.mDownloadBtn.onClick();

                                        }
                                    }
                                });
//                        viewHolderDownLoaded.mDivide = convertView
//                                .findViewById(R.id.game_image_tag);
                        convertView.setTag(viewHolderDownLoaded);
//                        convertView.setTag(R.id.id_adapter_downloaded, viewHolderDownLoaded);
                        getLoadedView(viewHolderDownLoaded, position);
                        break;

                    case DIVIDE:
                        viewHolderDevider = new ViewHolderDevider();
                        convertView = LayoutInflater.from(mActivity).inflate(
                                R.layout.downloaded_title_layout, null);
                        viewHolderDevider.cleanRecords = (TextView) convertView.findViewById(R.id.all_delete_id);
                        convertView.setTag(viewHolderDevider);
//                        convertView.setTag(R.id.id_adapter_devide, viewHolderDevider);
                        viewHolderDevider.cleanRecords.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (CommonUtils.isFastDoubleClick())
                                    return;

                                if (queenData != null && queenData.size() > 0) {
                                    List<AppsItemBean> downloadDatas = new ArrayList<AppsItemBean>();
                                    List<HashMap<String, Object>> downloadedData = queenData
                                            .getDownloadedData();

                                    queenData.clearDownloadedData();

                                    hasDownLoaded = false;

                                    //默认页则不执行该方法
                                    if (!mActivity.hasDdefaultMoreBgView && mActivity.mDownloadfooter != null)
                                        mActivity.mDownloadfooter.setAddMoreVisibility(false);
                                    notifyDataSetChanged();

                                    //默认页无数据则显示默认背景
                                    if (queenData.size() <= 0 && !mActivity.hasDdefaultMoreBgView) {
                                        mActivity.isShowDefaultView(true);
                                        mActivity.isCancleAll = true;
                                        mActivity.hasDdefaultMoreBgView = true;

                                    }

                                    for (HashMap<String, Object> hashMap : downloadedData) {
                                        Object o = hashMap.get(AppDownLoadQueenActivity.DATA);

                                        if (o instanceof AppsItemBean) {
                                            AppsItemBean itemBean = (AppsItemBean) o;
                                            if (itemBean != null) {
                                                downloadDatas.add(itemBean);
                                                AIDLUtils.cancelDownload(itemBean);
                                            }
                                        } else {
                                            //title位置
                                            downloadDatas.add(null);
                                        }
                                    }

                                    // 清空已完成的下载任务，如果已经清空了，则不做操作
                                    AIDLUtils.clearAllLoadGametasks(downloadDatas);
                                    // 删除所有已下载完成的本地apk
                                    UpdateDataUtils.getUpdateInstance()
                                            .removeAllDownLoadApk();
                                    //删除数据库记录，会回调刷新
                                    AIDLUtils.deleteAllDownloadedData();
                                }
                            }
                            // }
                        });
                        break;
                }
            } else {
                if (JLog.isDebug) {
                    JLog.i(TAG, "convertView!=null-itemViewType=" + itemViewType + "--position=" + position);
                }
                switch (itemViewType) {
                    case DOWNLOADING_DATA: // 返回下载中的itemView
                        if (JLog.isDebug) {
                            JLog.i(TAG, "convertView!=null--执行下载任务逻辑" + "--position=" + position + "--" + (convertView.getTag(R.id.id_adapter_downloading) instanceof ViewHolder));
                        }
//                        if (convertView.getTag(R.id.id_adapter_downloading) instanceof ViewHolder) {
                        if (convertView.getTag() instanceof ViewHolder) {
                            viewHolder = (ViewHolder) convertView.getTag();
//                            viewHolder = (ViewHolder) convertView.getTag(R.id.id_adapter_downloading);
                            getLoadingView(viewHolder, position);
                        }
                        break;
                    case DOWNLOADED_DATA: // 返回已下载的itemView
//                        if (convertView.getTag(R.id.id_adapter_downloaded) instanceof ViewHolderDownLoaded) {
                        if (convertView.getTag() instanceof ViewHolderDownLoaded) {
                            viewHolderDownLoaded = (ViewHolderDownLoaded) convertView
                                    .getTag();
//                            viewHolderDownLoaded = (ViewHolderDownLoaded) convertView
//                                    .getTag(R.id.id_adapter_downloaded);
                            getLoadedView(viewHolderDownLoaded, position);

                        }
                        break;

                    case DIVIDE: // 返回下载中和已下载中间的分割视图
                        if (JLog.isDebug) {
                            JLog.i(TAG, "convertView!=null--执行分割线逻辑" + "--position=" + position + "--" + (convertView.getTag() instanceof ViewHolderDevider));
                            JLog.i(TAG, "convertView!=null--执行分割线逻辑view-" + convertView.getTag());
                            JLog.i(TAG, "convertView!=null--执行分割线逻辑是否是下载中view-" + (convertView.getTag() instanceof ViewHolder));
                            JLog.i(TAG, "convertView!=null--执行分割线逻辑是否是下已下载view-" + (convertView.getTag() instanceof ViewHolderDownLoaded));
                        }
//                        viewHolderDevider = (ViewHolderDevider) convertView
//                                .getTag();
                        break;
                    default:
                        break;
                }
            }
        }
        return convertView;
    }


    private void getLoadedView(final ViewHolderDownLoaded viewHolder, int position) {
        final AppsItemBean itemBean = (AppsItemBean) queenData.get(position).get(
                AppDownLoadQueenActivity.DATA);

        final AppDownLoadQueenActivity mActivity = mActivities.get();
        if (itemBean == null || mActivity == null || viewHolder == null)
            return;
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon,
                    viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
        } else {
            if ((itemBean.iconUrl != null)) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
            }

        }

//        if (queenData.downloadingDataSize() > 0) {
//            int size = queenData.downloadedDataSize() + queenData.downloadingDataSize();
//            //判断是否有更多
////            if (queenData.downloadedDataSize() == 3) {
////                //提出去会影响效率
////                Object o = queenData.getDownloadedData().get(0).get(AppDownLoadQueenActivity.DATA);
////                // o!=null说明小于三个
//////                if (o != null && position + 1 == size) {
//////                    viewHolder.mDivide.setVisibility(View.GONE);
//////                } else {
//////                    viewHolder.mDivide.setVisibility(View.VISIBLE);
//////                }
////            } else {
////                //最后一个位置
////                if (queenData.downloadedDataSize() > 3 && position + 1 == size) {
////
////                    viewHolder.mDivide.setVisibility(View.GONE);
////                } else {
////
////                    viewHolder.mDivide.setVisibility(View.VISIBLE);
////                }
////            }
//        } else {
//            //三个应用的情况
//            if (queenData.downloadedDataSize() == 3) {
//                //提出去会卡
//                Object o = queenData.getDownloadedData().get(0).get(AppDownLoadQueenActivity.DATA);
//
//                // o==null说明有更多
//                if (o != null && position + 1 == queenData.downloadedDataSize()) {
//
//                    viewHolder.mDivide.setVisibility(View.GONE);
//                } else {
//                    viewHolder.mDivide.setVisibility(View.VISIBLE);
//                }
//            } else {
//
//                if (queenData.downloadedDataSize() > 3 && position + 1 == queenData.downloadedDataSize()) {
//                    //最后一个位置分割线去掉
//                    viewHolder.mDivide.setVisibility(View.GONE);
//
//                } else {
//                    viewHolder.mDivide.setVisibility(View.VISIBLE);
//                }
//            }
//        }

        if (itemBean.name != null) {
            viewHolder.mName.setText(itemBean.name);
        }

        if (!TextUtils.isEmpty(itemBean.installType)) {
            if (Constants.UPDATE_INSTALL.equals(itemBean.installType)) {
                if (TextUtils.isEmpty(itemBean.dowloadedStamp)) {
                    viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_update_time,
                            CommonUtils.getInstallTime(itemBean.packageName)));
                } else {
                    viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_update_time,
                            CommonUtils.getFormateTime(Long.parseLong(itemBean.dowloadedStamp))));

                }
            } else {
                if (TextUtils.isEmpty(itemBean.dowloadedStamp)) {
                    viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_install_time,
                            CommonUtils.getInstallTime(itemBean.packageName)));
                } else {
                    viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_install_time,
                            CommonUtils.getFormateTime(Long.parseLong(itemBean.dowloadedStamp))));

                }
            }
        } else {
            if (TextUtils.isEmpty(itemBean.dowloadedStamp)) {
                viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_install_time,
                        CommonUtils.getInstallTime(itemBean.packageName)));
            } else {
                viewHolder.isInstall.setText(BaseApplication.curContext.getString(R.string.app_install_time,
                        CommonUtils.getFormateTime(Long.parseLong(itemBean.dowloadedStamp))));

            }
        }
        if (!TextUtils.isEmpty(itemBean.versionName)) {
            viewHolder.versionName_Tv.setText(BaseApplication.curContext.getString(R.string.gamedetail_detail_version, itemBean.versionName));
        } else {
            viewHolder.versionName_Tv.setText(BaseApplication.curContext.getString(R.string.gamedetail_detail_version, AppManagerCenter.getAppVersionName(itemBean.packageName, mActivity)));
        }

        viewHolder.parent_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(itemBean.id)) {
                    UIUtils.gotoAppDetail(itemBean.id, mActivities.get());
                    MTAUtil.onDetailClick(mActivity, itemBean.name,
                            itemBean.packageName);

                }

            }
        });
        if (viewHolder.mDownloadBtn != null) {
            viewHolder.mDownloadBtn.enabelDefaultPress(true);
            viewHolder.mDownloadBtn.setGameInfo(itemBean);
        }

    }

    private void getLoadingView(final ViewHolder viewHolder, final int position) {
        final AppsItemBean itemBean = (AppsItemBean) queenData.get(position)
                .get(AppDownLoadQueenActivity.DATA);
        final AppDownLoadQueenActivity mActivity = mActivities.get();
        if (itemBean == null || mActivity == null)
            return;
        final int state = AIDLUtils.getGameAppState(itemBean.packageName,
                itemBean.id + "", itemBean.versionCode);
        viewHolder.mDownloadBtn.enabelDefaultPress(true);
        viewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.nonet_connect);
                    return;
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mActivity, R.style.add_dialog);
                            }
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(itemBean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            viewHolder.mDownloadBtn.onClick();
                            break;
                    }

                } else {
                    viewHolder.mDownloadBtn.onClick();
                }

            }
        });
        viewHolder.mDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mDelTaskDialog == null) {
                    mDelTaskDialog = new DelTaskDialog(mActivity, R.style.add_dialog);
                }
                mDelTaskDialog.show();
                mDelTaskDialog.setmOnButtonClic(new DelTaskDialog.OnButtonClic() {
                    @Override
                    public void onClick(int which) {
                        dismissDelTaskDialog();
                        switch (which) {
                            case 0:
                                if (position < queenData.size() && position < cacheData.size()
                                        && position >= 0) {
                                    AIDLUtils.cancelDownload(itemBean);
                                    //先移除内存中的数据，bug：删除不同步
                                    queenData.removeDownloadingItemData(position);
                                    cacheData.remove(position);
                                    // hasDdefaultMoreBgView没有显示默认页
                                    if (queenData.size() <= 0 && !mActivity.hasDdefaultMoreBgView) {
                                        //设置是否请求网络
                                        mActivity.isCancleAll = true;
                                        mActivity.isShowDefaultView(true);
                                        //设置footeriew的显示类型
                                        mActivity.hasDdefaultMoreBgView = true;
                                    }
                                    notifyDataSetChanged();
                                    if (queenData.downloadingDataSize() <= 0) {
                                        mActivity.removeHeadView();
                                    }
                                } else {
                                    ToastUtils.showToast(R.string.task_unexsit);
                                }
                                break;
                            case 1:
                                break;
                        }
                    }
                });
            }
        });

        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon,
                    viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
        } else {

            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.mIcon, UILimageUtil.getUILoptions(), null);
            }
        }

        if (itemBean.name != null) {
            viewHolder.mName.setText(itemBean.name);
        }
        if (itemBean != null && !TextUtils.isEmpty(itemBean.apkSize)) {
            viewHolder.mTotlaSize.setText("/" + CommonUtils.formatSize(Long.parseLong(itemBean.apkSize), "#0.00"));
        }
        // 进度条进度指示
        final float progress = AIDLUtils.getDownloadProgress(itemBean.packageName);
        String downloadSize = CommonUtils.paresAppSize((long) (Long.parseLong(itemBean.apkSize) * (progress / 100f)));
        viewHolder.mDownLoadSize.setText(downloadSize + "MB");
        int mProgress;
        if (progress > 0.0 && progress < 1.0f) {
            mProgress = 1;
        } else {
            mProgress = (int) progress;
        }
        viewHolder.mProgressbar.setProgress(mProgress);
//        mMainHandler.post(new Runnable() {
//            int mProgress = 0;
//
//            public void run() {
//                // do something
//                if (progress > 0.0 && progress < 1.0f) {
//                    mProgress = 1;
//                } else {
//                    mProgress = (int) progress;
//                }
//                viewHolder.mProgressbar.setProgress(mProgress);
//            }
//        });

        if (state == AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE) {
            viewHolder.mRadiu.setVisibility(View.GONE);
            viewHolder.mPauseed.setVisibility(View.VISIBLE);
            viewHolder.mPauseed.setText(mActivity.getResources().getString(
                    R.string.has_suspend));
            viewHolder.mRadiu.setText("0KB/s");
        } else if (state == AppManagerCenter.APP_STATE_WAIT) {
            viewHolder.mRadiu.setVisibility(View.GONE);
            viewHolder.mPauseed.setVisibility(View.VISIBLE);
            viewHolder.mPauseed.setText(mActivity.getResources().getString(
                    R.string.wait_task_start));
            viewHolder.mRadiu.setText("0KB/s");
        } else {
            viewHolder.mRadiu.setVisibility(View.VISIBLE);
            viewHolder.mPauseed.setVisibility(View.GONE);
            int radiu = AIDLUtils.getDownloadSpeed(itemBean.packageName);
            if (radiu >= 1000) {
                viewHolder.mRadiu.setText(String.format("%1$.2f",
                        radiu / (1024f)) + "MB/s");

            } else if (radiu > 0 && radiu < 1000) {
                viewHolder.mRadiu.setText(radiu + "KB/s");
            }
            // 下载完成移除
            if (progress == 100) {
                mHandler.sendEmptyMessageDelayed(position, 0);
            }
            if (state == AppManagerCenter.APP_STATE_INSTALLED) {
                mHandler.sendEmptyMessageDelayed(position, 0);
            }
        }
        viewHolder.mDownloadBtn.setGameInfo(itemBean);
    }

    public void sethasDownLoaded(boolean hasDownLoaded) {
        this.hasDownLoaded = hasDownLoaded;
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (JLog.isDebug) {
                JLog.i(TAG, "mHandler-handleMessage-msg.what=" + msg.what);
            }
            try {
                if (msg.what < queenData.size() && msg.what >= 0) {
                    final AppsItemBean itemBean = (AppsItemBean) queenData.get(
                            msg.what).get(AppDownLoadQueenActivity.DATA);

                    if (itemBean != null) {
                        int state = AIDLUtils.getGameAppState(
                                itemBean.packageName, itemBean.id + "",
                                itemBean.versionCode);
                        final AppDownLoadQueenActivity mActivity = mActivities
                                .get();
                        if (mActivity == null)
                            return;
                        switch (state) {
                            case AppManagerCenter.APP_STATE_DOWNLOADED:
                            case AppManagerCenter.APP_STATE_INSTALLING:
                            case AppManagerCenter.APP_STATE_INSTALLED:
                            case AppManagerCenter.APP_PATCHING:
                            case AppManagerCenter.APP_STATE_UNEXIST:
                            case AppManagerCenter.APP_STATE_UPDATE:
                                if (msg.what < cacheData.size()) {
                                    queenData.removeDownloadingItemData(msg.what);
                                    cacheData.remove(msg.what);
                                    if (queenData.downloadingDataSize() <= 0) {
                                        mActivity.removeHeadView();
                                    }
                                    if (!hasDownLoaded) {
                                        HashMap<String, Object> mapOne = new HashMap<String, Object>();
                                        mapOne.put(AppDownLoadQueenActivity.TYPE,
                                                DownloadQueenListViewAdapter.DIVIDE);
                                        mapOne.put(AppDownLoadQueenActivity.DATA,
                                                null);
                                        queenData.addDivideData(mapOne);
                                        hasDownLoaded = true;
                                    }
                                    HashMap<String, Object> map = new HashMap<String, Object>();
                                    map.put(AppDownLoadQueenActivity.TYPE,
                                            DOWNLOADED_DATA);
                                    map.put(AppDownLoadQueenActivity.DATA, itemBean);
                                    queenData.reSetDownloadedData(map);
                                    // 去除重复

                                    List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

                                    for (int i = queenData.getDownloadedData()
                                            .size() - 1; i >= 0; i--) {

                                        if (!list.contains(queenData
                                                .getDownloadedData().get(i))) {
                                            list.add(queenData.getDownloadedData()
                                                    .get(i));
                                        } else {
                                            queenData.getDownloadedData().remove(
                                                    queenData.getDownloadedData()
                                                            .get(i));
                                        }
                                    }

                                    notifyDataSetChanged();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private class ViewHolder {
        private ImageView mIcon;
        private TextView mName;
        private TextView mRadiu;
        private TextView mDownLoadSize;
        private TextView mTotlaSize;
        private ProgressBar mProgressbar;
        private AnimDownloadProgressButton mDownloadBtn;
        private Button mDelete;
        private TextView mPauseed;
//        private View mDivide;
//        private RelativeLayout item_Rlyt;
    }

    private class ViewHolderDownLoaded {
        private ImageView mIcon;
        private TextView mName;
        private AnimDownloadProgressButton mDownloadBtn;
        private TextView isInstall;
        private TextView versionName_Tv;
        private RelativeLayout parent_Rlyt;
//        private View mDivide;
    }

    private class ViewHolderDevider {
        private TextView cleanRecords;

    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private DelTaskDialog mDelTaskDialog;

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }


    /**
     * 充写原因 ViewPager在Android4.0上有兼容性错误
     * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
     * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
     * http://blog.csdn.net/guxiao1201/article/details/8818734
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    private void dismissDelTaskDialog() {
        if (mDelTaskDialog != null && mDelTaskDialog.isShowing()) {
            mDelTaskDialog.dismiss();
        }
    }


    private ListView mListView;

    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            if (queenData == null || queenData.get(i) == null)
                continue;
            if (!(queenData.get(i).get(AppDownLoadQueenActivity.DATA) instanceof AppsItemBean) || queenData.get(i)
                    .get(AppDownLoadQueenActivity.DATA) == null) {
                continue;
            }
            final AppsItemBean bean = (AppsItemBean) queenData.get(i)
                    .get(AppDownLoadQueenActivity.DATA);
            if (bean == null)
                continue;
            int itemViewType = getItemViewType(i);
            if (!TextUtils.isEmpty(bean.packageName) && bean.packageName.equals(packageName)) {
                View subView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
                if (subView != null) {
                    if (itemViewType == DOWNLOADING_DATA) {
                        final ProgressBar mProgressbar = (ProgressBar) subView
                                .findViewById(R.id.download_progressbar_id);
                        View viewBtn = subView
                                .findViewById(R.id.game_download_btn);
                        TextView mRadiu = (TextView) subView
                                .findViewById(R.id.radiu_id);
                        TextView mDownLoadSize = (TextView) subView
                                .findViewById(R.id.downloadSize_id);
                        if (viewBtn != null) {
                            viewBtn.invalidate();
                        }
                        final float progress = AIDLUtils.getDownloadProgress(packageName);
                        String downloadSize = CommonUtils.paresAppSize((long) (Long.parseLong(bean.apkSize) * (progress / 100f)));
                        if (mDownLoadSize != null) {
                            mDownLoadSize.setText(downloadSize + "MB");
                        }
                        if (mRadiu != null) {
                            int radiu = AIDLUtils.getDownloadSpeed(packageName);
                            if (radiu >= 1000) {
                                mRadiu.setText(String.format("%1$.2f", radiu / (1024f)) + "MB/s");

                            } else if (radiu > 0 && radiu < 1000) {
                                mRadiu.setText(radiu + "KB/s");
                            }

                        }

                        // 进度条进度指示
                        int mProgress = 0;
                        if (progress > 0.0 && progress < 1.0f) {
                            mProgress = 1;
                        } else {
                            mProgress = (int) progress;
                        }
                        if (mProgressbar != null) {
                            mProgressbar.setProgress(mProgress);
                        }

                    }
                }
            }
        }
    }
}
