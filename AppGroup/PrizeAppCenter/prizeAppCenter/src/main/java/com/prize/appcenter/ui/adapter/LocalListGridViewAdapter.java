/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.FolderActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CustomImageView;
import com.prize.appcenter.ui.widget.FolderProgress;

import java.util.ArrayList;

/**
 * 类描述：桌面文件夹的GridView adapter
 *
 * @author longbaoxiu
 * @version 版本
 */
public class LocalListGridViewAdapter extends BaseAdapter {
    private FolderActivity mCtx;
    private ArrayList<AppsItemBean> datas;
    private IUIDownLoadListenerImp listener = null;
    private Handler mHandler;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true

    public LocalListGridViewAdapter(FolderActivity context) {
        super();
        mCtx = context;
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, final int state, boolean isNewDownload) {
                if (isActivity) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (state == DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS
                                    || state == DownloadState.STATE_DOWNLOAD_PAUSE) {
                                updateView(pkgName);
                            } else {
                                notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
        setListener();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        notifyDataSetChanged();
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {

            listener.setmCallBack(null);
            listener = null;

        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private int textColor=0;



    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    public void setData(ArrayList<AppsItemBean> datas) {
        if (datas == null || datas.size() <= 0)
            return;
        this.datas = datas;
        AppsItemBean bean = new AppsItemBean();
        bean.id = String.valueOf(-1);
        bean.name = mCtx.getString(R.string.see_more);
        this.datas.add(bean);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        gridViewApp = (GridView) parent;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(
                    R.layout.app_local_gridview_item, null);
            viewHolder.itemImg = (CustomImageView) convertView
                    .findViewById(R.id.appItem_img_id);
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.appItem_name_id);
            viewHolder.seemore_Iv = (ImageView) convertView
                    .findViewById(R.id.seemore_Iv);
            viewHolder.folder_down_flag = (ImageView) convertView
                    .findViewById(R.id.folder_down_flag);
            viewHolder.appItem_prg = (FolderProgress) convertView
                    .findViewById(R.id.appItem_prg);
            convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AppsItemBean data = getItem(position);
        if (data == null) return convertView;
        int state = AIDLUtils.getGameAppState(data.packageName, data.id, data.versionCode);
        if (data.id.equals("-1")) {
            viewHolder.itemImg.setVisibility(View.GONE);
            viewHolder.folder_down_flag.setVisibility(View.GONE);
            viewHolder.seemore_Iv.setVisibility(View.VISIBLE);
            if(textColor!=0){
                viewHolder.seemore_Iv.getBackground().mutate().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            }
            viewHolder.seemore_Iv.setTag(data);
            viewHolder.seemore_Iv.setOnClickListener(mOnClickListener);
        } else {
            if (state == AppManagerCenter.APP_STATE_UNEXIST||state==AppManagerCenter.APP_STATE_UPDATE) {
                viewHolder.folder_down_flag.setVisibility(View.VISIBLE);
            } else {
                viewHolder.folder_down_flag.setVisibility(View.GONE);
            }
            viewHolder.seemore_Iv.setVisibility(View.GONE);
            viewHolder.itemImg.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(data.largeIcon)) {
                ImageLoader.getInstance().displayImage(data.largeIcon,
                        viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
            } else {
                if (data.iconUrl != null) {
                    ImageLoader.getInstance().displayImage(data.iconUrl,
                            viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
                }
            }
            viewHolder.appItem_prg.setGameInfo(data);
            viewHolder.appItem_prg.setTag(data);
            viewHolder.appItem_prg.setOnClickListener(mOnClickListener);
//            viewHolder.itemImg.setTag(data);
//            viewHolder.itemImg.setOnClickListener(mOnClickListener);
        }
        if(textColor!=0){
            viewHolder.itemName.setTextColor(textColor);
        }
        switch (state) {
            case AppManagerCenter.APP_STATE_DOWNLOADING:
                viewHolder.itemName.setText(R.string.progress_downloading);
                break;
            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                viewHolder.itemName.setText(R.string.progress_pause);
                break;
            case AppManagerCenter.APP_STATE_WAIT:
                viewHolder.itemName.setText(R.string.progress_btn_wait);
                break;
            default:
                viewHolder.itemName.setText(data.name);
                break;
        }
        return convertView;
    }

    private class ViewHolder {
        CustomImageView itemImg;
        ImageView seemore_Iv;
        ImageView folder_down_flag;
        TextView itemName;
        FolderProgress appItem_prg;
    }

    private GridView gridViewApp;

    private void updateView(String packageName) {
        JLog.i("LocalListGridViewAdapter", "--packageName=" + packageName);
        if (gridViewApp == null || TextUtils.isEmpty(packageName))
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = gridViewApp.getFirstVisiblePosition();
        int LastVisiblePosition = gridViewApp.getLastVisiblePosition();
        for (int i = visiblePosition; i <= LastVisiblePosition; i++) {
            AppsItemBean bean = getItem(i);
            if (bean == null || TextUtils.isEmpty(bean.packageName))
                continue;
            View parentView = gridViewApp.getChildAt(i - visiblePosition);
            if (parentView == null)
                continue;
            int state;
            if (bean.packageName.equals(packageName)) {
                View viewBtn = parentView.findViewById(R.id.appItem_prg);
                if (viewBtn != null) {
                    viewBtn.invalidate();
                }
                TextView stateView = (TextView) parentView.findViewById(R.id.appItem_name_id);
                state = AIDLUtils.getGameAppState(bean.packageName, bean.id, bean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_DOWNLOADING:
                        if (stateView != null) {
                            stateView.setText(R.string.progress_downloading);
                        }
                        break;
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (stateView != null) {
                            stateView.setText(R.string.progress_pause);
                        }
                        break;
                    default:
                        break;
                }
            }

        }
    }

    private View.OnClickListener mOnClickListener;
    private DownDialog mDownDialog;

    private void setListener() {
        mOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final AppsItemBean itemBean = (AppsItemBean) v.getTag();
                if (itemBean == null) return;
                if (itemBean.id.equals("-1")) {
                    Intent intent = new Intent(mCtx,
                            MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("position", 1);
                    mCtx.startActivity(intent);
                    mCtx.finish();
                    return;
                }
                final int state = AIDLUtils.getGameAppState(
                        itemBean.packageName, itemBean.id,
                        itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.networkType == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                        if (BaseApplication.isDownloadWIFIOnly() && ClientInfo.networkType != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mCtx, R.style.add_dialog);
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
                                            UIUtils.downloadApp(itemBean);
                                            break;
                                    }
                                }
                            });

                        } else {
//                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
//                                if (mDrawerContainer != null && mDrawerContainer.getChildCount() > 0)
//                                    return;
//                                requestDrawerData(itemBean.id, itemBean.name, position);
//                            }
                            UIUtils.downloadApp(itemBean);
                        }

                        break;


                    default://不是需要下载，继续 更新的条件
                        if (v instanceof FolderProgress) {
                            ((FolderProgress) v).onClick();
                        }
                        break;
                }
            }
        };

    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

}
