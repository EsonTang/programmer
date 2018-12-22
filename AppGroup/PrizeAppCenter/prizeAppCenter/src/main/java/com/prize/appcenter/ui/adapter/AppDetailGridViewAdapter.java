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
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.AppUpdateActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 类描述：详情(和应用更新界面无更新时推荐)里的GridView adapter
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppDetailGridViewAdapter extends BaseAdapter {
    // private Activity mCtx;
    private WeakReference<Activity> mCtxs;
    private List<AppsItemBean> datas;
    private IUIDownLoadListenerImp refreshHanle = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true;
    private DownDialog mDownDialog;
    private Handler mHandler;
    private boolean isNeedLoadImg = false;
    private int bgColor;

    public AppDetailGridViewAdapter(Activity context) {
        super();
        mCtxs = new WeakReference<Activity>(context);
        mHandler = new Handler();
        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                if (isActivity) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void setBgColor(int bgColor) {
        this.bgColor= bgColor;
    }

    public void setData(List<AppsItemBean> datas) {
        if (datas == null || datas.size() <= 0)
            return;
        this.datas = datas;
        if (isNeedLoadImg) {
            notifyDataSetChanged();
        }
    }

    public void setNeedLoadImg(boolean needLoadImg) {
        isNeedLoadImg = needLoadImg;
    }

    public void notifyDataSetChange() {
        if (getCount() > 0) {
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        if (datas == null)
            return 0;
        return datas.size() > 4 ? 4 : datas.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
//        if (parent.getChildCount() == position) {
        final ViewHolder viewHolder;
        final Activity mCtx = mCtxs.get();
        if (mCtx == null) {
            return convertView;
        }
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(
                    R.layout.app_item_view, null);
            viewHolder.itemImg = (ImageView) convertView
                    .findViewById(R.id.appItem_img_id);
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.appItem_name_id);
            viewHolder.itemSize = (TextView) convertView
                    .findViewById(R.id.appItem_size_id);
            viewHolder.itemButton = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.progressButton_id);
            convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean itemBean = datas.get(position);
//        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
        if (isNeedLoadImg) {
            ImageLoader.getInstance().displayImage(!TextUtils.isEmpty(itemBean.largeIcon) ? itemBean.largeIcon : itemBean.iconUrl,
                    viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
            if (JLog.isDebug) {
                JLog.i("AppDetailGridViewAdapter", "itemBean.largeIcon=" + itemBean.largeIcon + "--itemBean.name=" + itemBean.name);
            }
        }
//        } else {
//            if (itemBean.iconUrl != null) {
//                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
//                        viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
//            }
//        }

        viewHolder.itemButton.enabelDefaultPress(true);
        if(bgColor!=0){
            viewHolder.itemButton.setBackgroundColor(bgColor,bgColor);
            viewHolder.itemName.setTextColor(Color.WHITE);
            viewHolder.itemSize.setTextColor(Color.parseColor("#66FFFFFF"));
        }
        viewHolder.itemName.setText(itemBean.name);
        if (itemBean.downloadTimesFormat != null) {
            String user = itemBean.downloadTimesFormat.replace("次", "人");
            viewHolder.itemSize.setText(mCtx.getString(R.string.person_use,
                    user));
        }
        viewHolder.itemButton.setGameInfo(itemBean);
//        viewHolder.itemButton.setGameInfo(CommonUtils.formatAppPageInfo(itemBean, Constants.APPDETAIL_GUI, Constants.LIST, position + 1));
        viewHolder.itemButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int state = AIDLUtils.getGameAppState(itemBean.packageName,
                        itemBean.id + "", itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                        if (mCtx != null && mCtx instanceof AppDetailActivity) {
//                            PrizeStatUtil.onClickBackParams(itemBean.backParams,itemBean.name,itemBean.packageName);
                            AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                        }
                        if (mCtx != null && mCtx instanceof AppUpdateActivity) {
                            MTAUtil.onNoAppUpdateRecommond(itemBean.name, position + 1);
                        }
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
//                                            UIUtils.downloadApp(CommonUtils.formatAppPageInfo(itemBean, Constants.APPDETAIL_GUI, Constants.LIST, position + 1));
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
            }
        });
        return convertView;
//        }else{
//            return null;
//        }
    }

    private class ViewHolder {
        ImageView itemImg;
        TextView itemName;
        TextView itemSize;
        AnimDownloadProgressButton itemButton;
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
        refreshHanle = null;
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
}
