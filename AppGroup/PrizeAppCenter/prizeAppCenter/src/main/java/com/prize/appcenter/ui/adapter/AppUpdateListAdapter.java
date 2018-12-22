package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ExpendSingleTextView;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用列表适配器
 *
 * @author prize
 */
public class AppUpdateListAdapter extends GameListBaseAdapter {
    private ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private IUIDownLoadListenerImp refreshHanle = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private DownDialog mDownDialog;

    public AppUpdateListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;
        mHandler = new Handler();
        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new MyIUIDownLoadCallBack(this));
    }

    private static class MyIUIDownLoadCallBack implements IUIDownLoadCallBack {
        protected WeakReference<AppUpdateListAdapter> mActivities;

        private MyIUIDownLoadCallBack(AppUpdateListAdapter appUpdateListAdapter) {
            mActivities = new WeakReference<AppUpdateListAdapter>(appUpdateListAdapter);
        }

        @Override
        public void callBack(final String pkgName, int state, boolean isNewDownload) {
            if (mActivities == null) return;
            final AppUpdateListAdapter instance = mActivities.get();
            if (instance == null) return;
            if (instance.isActivity) {
                instance.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        instance.updateView(pkgName);
                    }
                });
            }

        }
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置应用列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(List<AppsItemBean> data) {
        if (items != null&&data !=null) {
            items.clear();
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新应用列表到已有集合中
     */
    public void addData(ArrayList<AppsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空应用列表
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if (position < 0 || items.isEmpty() || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        mListView = (ListView) parent;
        final Activity mActivity = mActivities.get();
        if (mActivity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.item_update_listview, null);
            viewHolder = new ViewHolder();
            viewHolder.update_version_tv = (TextView) convertView
                    .findViewById(R.id.update_version_tv);
            viewHolder.container_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.container_Rlyt);
            viewHolder.update_oldversion_tv = (TextView) convertView
                    .findViewById(R.id.update_oldversion_tv);
            viewHolder.appIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
            viewHolder.appName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.appSize = (TextView) convertView
                    .findViewById(R.id.game_size_tv);
            viewHolder.realsize_tv = (TextView) convertView
                    .findViewById(R.id.realsize_tv);
            viewHolder.appComment = (ExpendSingleTextView) convertView
                    .findViewById(R.id.game_desc);
            viewHolder.appTagTV = (TextView) convertView
                    .findViewById(R.id.game_tag_tv);
            viewHolder.downloadBtn = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.game_download_btn);
            viewHolder.game_image_tag = convertView.findViewById(R.id.game_image_tag);
            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean itembean = items.get(position);
        if (null == itembean) {
            return convertView;
        }
        viewHolder.game_image_tag.setVisibility(View.VISIBLE);
        if (getCount()>=4&&position == getCount() - 1) {
            viewHolder.game_image_tag.setVisibility(View.GONE);
        }
        itembean.position = position;
        viewHolder.downloadBtn.enabelDefaultPress(true);
        viewHolder.downloadBtn.setGameInfo(itembean);
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int state = AIDLUtils.getGameAppState(itembean.packageName,
                        itembean.id, itembean.versionCode);
                MTAUtil.onUpdateBtnClick(itembean.name);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                                ToastUtils.showToast(R.string.nonet_connect);
                                return;
                            }
                            mDownDialog = new DownDialog(mActivity,
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
                                            UIUtils.downloadApp(itembean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            viewHolder.downloadBtn.onClick();
                            break;
                    }

                } else {
                    viewHolder.downloadBtn.onClick();
                }
            }
        });

        viewHolder.container_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                // onItemClick(position);
                UIUtils.gotoAppDetail(itembean,
                        itembean.id, mActivity);

            }
        });

        if (!TextUtils.isEmpty(itembean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itembean.largeIcon,
                    viewHolder.appIcon, UILimageUtil.getUILoptions(), null);
        } else {
            if ((itembean.iconUrl != null)) {
                ImageLoader.getInstance().displayImage(itembean.iconUrl,
                        viewHolder.appIcon, UILimageUtil.getUILoptions(), null);
            }

        }

        if (itembean.name != null) {
            viewHolder.appName.setText(itembean.name);
        } else {
            viewHolder.appName.setText("");
        }

        if (null != itembean.appPatch) {
            if (null != itembean.appPatch) {
                viewHolder.appSize.setText(CommonUtils.formatSize(itembean.appPatch.patchSize, "#.00"));
                viewHolder.realsize_tv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                viewHolder.realsize_tv.setText(itembean.apkSizeFormat);
            } else {
                viewHolder.realsize_tv.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
                viewHolder.realsize_tv.setText(itembean.apkSizeFormat);
                viewHolder.appSize.setText("");
            }
        } else {
            viewHolder.realsize_tv.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            viewHolder.realsize_tv.setText(itembean.apkSizeFormat);
            viewHolder.appSize.setText("");
        }
        String netVersionName = itembean.versionName;
        String currVersionName = AppManagerCenter.getAppVersionName(
                itembean.packageName, mActivity);
        if (!TextUtils.isEmpty(currVersionName)) {
            viewHolder.update_oldversion_tv.setText(currVersionName);
            viewHolder.update_version_tv.setText(netVersionName);
            viewHolder.update_version_tv.setVisibility(View.VISIBLE);
        }else{
            viewHolder.update_oldversion_tv.setText(netVersionName);
            viewHolder.update_version_tv.setVisibility(View.GONE);

        }
        viewHolder.appComment.setContentDesc(mActivity.getApplication()
                .getString(R.string.update_time), itembean.updateInfo);
        return convertView;
    }

    static class ViewHolder {
        // 应用图标
        ImageView appIcon;
        // 应用名称
        TextView appName;
        // 应用大小
        TextView appSize;
        // 真实需要下载的apk大小
        TextView realsize_tv;
        // 下载按钮
        AnimDownloadProgressButton downloadBtn;
        // 应用介绍
        ExpendSingleTextView appComment;
        RelativeLayout container_Rlyt;
        /**
         * 内测，公测等
         */
        TextView appTagTV;
        /**
         * 评分
         */
        // RatingBar ratingBar;
        TextView update_version_tv;
        TextView update_oldversion_tv;
        View game_image_tag;

    }

    // 取消下载
    // private void downloadCancel(GameBean item) {
    // AppManagerCenter.cancelDownload(item);
    // }

	public void onItemClick(int position) {
		if (position < 0 || position >= items.size()) {
			return;
		}
		AppsItemBean item = items.get(position);
		if (null != item) {
			// 跳转到详细界面
			UIUtils.gotoAppDetail(item,item.id,mActivities.get());

		}
	}

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(refreshHanle);
        if (refreshHanle != null) {
            refreshHanle.setmCallBack(null);
            refreshHanle = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(refreshHanle);
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


    private ListView mListView;

    /**
     * ListView局部刷新（仅仅刷新下载按钮状态）----add by longbaoxiu 2.5版本
     *
     * @param packageName 应用包名
     */
    private void updateView(String packageName) {
        if (mListView == null)
            return;
        JLog.i("AppUpdateListAdapter", "updateView-package=" + packageName);
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            AppsItemBean bean = getItem(i);
            if (JLog.isDebug&&bean !=null) {
                JLog.i("AppUpdateListAdapter", "updateView-bean=" + bean);
            }
            if (bean == null)
                continue;
            if (bean.packageName.equals(packageName)) {
                View subView = mListView.getChildAt(i + headerViewsCount - visiblePosition);
                if (subView != null) {
                    View viewBtn = subView.findViewById(R.id.game_download_btn);
                    if (viewBtn != null) {
                        viewBtn.invalidate();
                    }
                }
            }
        }
    }
}
