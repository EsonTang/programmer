/*******************************************
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

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 必备的adapter
 *
 * @author 龙宝修
 * @version 版本
 */
public class RequireGridViewAdapter extends BaseAdapter {
    // private Activity mCtx;
    private WeakReference<Activity> mCtxs;
    private ArrayList<AppsItemBean> datas;
    private IUIDownLoadListenerImp refreshHanle = null;
    /** 当前页是否处于显示状态 */
    private boolean isActivity = true;
    private DownDialog mDownDialog;
    private Handler mHandler;

    public RequireGridViewAdapter(Activity context) {
        super();
        mCtxs = new WeakReference<Activity>(context);
        mHandler = new Handler();
//        refreshHanle = new IUIDownLoadListenerImp() {
//
//            @Override
//            public void onRefreshUI(final String pkgName, int position) {
//                if (isActivity) {
//                    mHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            updateView(pkgName);
//                        }
//                    });
//                }
//            }
//        };

        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(final String pkgName, int state,boolean isNewDownload) {
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

    public void setData(ArrayList<AppsItemBean> datas) {
        if (datas == null || datas.size() <= 0)
            return;
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (datas == null)
            return 0;
        return datas.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if (position < 0 || datas.isEmpty() || position >= datas.size()) {
            return null;
        }
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Activity mCtx = mCtxs.get();
        if (mCtx == null) {
            return convertView;
        }
        mListView= (GridView) parent;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(
                    R.layout.item_require_view, null);
            viewHolder.itemImg = (ImageView) convertView
                    .findViewById(R.id.appItem_img_id);
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.appItem_name_id);
            viewHolder.itemButton = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.progressButton_id);
            convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean itemBean = datas.get(position);

        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon,
                    viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
        } else {

            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
            }
        }

        viewHolder.itemName.setText(itemBean.name);
        viewHolder.itemButton.enabelDefaultPress(true);
        viewHolder.itemButton.setGameInfo(itemBean);
        viewHolder.itemButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int state = AIDLUtils.getGameAppState(itemBean.packageName,
                        itemBean.id + "", itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly() && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(mCtx, R.style.add_dialog);
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
                            viewHolder.itemButton.onClick();
                            break;
                    }

                } else {
                    viewHolder.itemButton.onClick();
                }
                // if(ClientInfo.networkType == ClientInfo.WIFI){
                // startAnimation(viewHolder.itemImg, itemBean, state);
                // }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        public ImageView itemImg;
        public TextView itemName;
        public AnimDownloadProgressButton itemButton;
    }

    public void onItemClick(int position) {
        if (position < 0 || position >= datas.size()) {
            return;
        }
        AppsItemBean item = datas.get(position);
        if (null != item) {
            // 跳转到详细界面
            UIUtils.gotoAppDetail(item,item.id,mCtxs.get());

        }
    }

    /**
     * 设置刷新handler,mCtx OnResume 时调用 //
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(refreshHanle);
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(refreshHanle);
        refreshHanle.setmCallBack(null);
        refreshHanle=null;
        mHandler.removeCallbacksAndMessages(null);
       
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public void setActivity(boolean activity) {
        isActivity = activity;
    }

    private GridView mListView;

    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        JLog.i("HomePagerListAdapter", "visiblePosition=" + visiblePosition + "----LastVisiblePosition=" + LastVisiblePosition + "--packageName=" + packageName);
        for (int i = visiblePosition; i <= LastVisiblePosition; i++) {
            AppsItemBean bean = getItem(i);
            if (bean == null)
                continue;
            if (bean.packageName.equals(packageName)) {
                View subView = mListView.getChildAt(i-visiblePosition);
                if (subView != null) {
                    View viewBtn=   subView.findViewById(R.id.progressButton_id);
                    if(viewBtn !=null){
                        viewBtn.invalidate();
                    }
                }
            }
        }
    }
}
