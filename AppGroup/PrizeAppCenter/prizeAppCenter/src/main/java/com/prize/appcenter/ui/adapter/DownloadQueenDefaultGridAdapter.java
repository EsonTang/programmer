package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CustomImageView;
import com.prize.appcenter.ui.widget.ProgressButton;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.util.ArrayList;

/**
 * Desc: 下载队列没有下载任务时候的推荐内容
 * <p/>
 * Created by huangchangguo
 * Date:  2016/9/9 10:36
 */

public class DownloadQueenDefaultGridAdapter extends BaseAdapter {

    private final IUIDownLoadListenerImp mListener;
    private ArrayList<AppsItemBean> itemDatas = new ArrayList<>();
    private Context    mContext;
    private DownDialog mDownDialog;
    Handler handler = new Handler();
    private ProgressButton mProgressButton;

    public DownloadQueenDefaultGridAdapter(Context context) {
        this.mContext = context;
        mListener = IUIDownLoadListenerImp.getInstance();
        mListener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state,boolean isNewDownload) {
                Message msg = Message.obtain();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        });
        AIDLUtils.registerCallback(mListener);

    }

    /**
     * 设置数据
     */
    public void setData(ArrayList<AppsItemBean> data) {
        if (null == data) {
            return;
        }
        itemDatas = data;
        notifyDataSetChanged();
    }

    /**
     * 获得数据
     */
    public AppsItemBean getData(int position) {
        if (null == itemDatas) {
            return null;
        }
        AppsItemBean appsItemBean = itemDatas.get(position);
        return appsItemBean;
    }

    @Override
    public int getCount() {

        return itemDatas.size();
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(mListener);
        handler.removeCallbacksAndMessages(null);
        if (mListener != null) {
            mListener.setmCallBack(null);
        }
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(mListener);
    }


    @Override
    public AppsItemBean getItem(int position) {
        return itemDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.app_item_view_download, null);
            viewHolder = new ViewHolder();
            viewHolder.customImageView = (CustomImageView) convertView.findViewById(R.id.appItem_img_id);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.appItem_name_id);
            viewHolder.appSize = (TextView) convertView.findViewById(R.id.appItem_size_id);
            viewHolder.progressButton = (AnimDownloadProgressButton) convertView.findViewById(R.id.progressButton_id);
            viewHolder.HeaderRylt = (LinearLayout) convertView.findViewById(R.id.appItem_header_llyt);

            convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AppsItemBean itemData = getData(position);
        setItemDatas(itemData, viewHolder,position);

        return convertView;
    }


    public void setItemDatas(final AppsItemBean itemBean, final ViewHolder viewHolder,final int position) {
        if (itemBean == null)
            return;
        //填充图片
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, viewHolder.customImageView,
                    UILimageUtil.getUILoptions(), null);
        } else {
            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.customImageView, UILimageUtil.getUILoptions(), null);
            }
        }
//        if(itemBean.name.trim().length()>5){
//            viewHolder.appName.setText(itemBean.name.trim().substring(0,4)+"...");
//        }else{
            viewHolder.appName.setText(itemBean.name.trim());
//        }
        if (null != itemBean.downloadTimesFormat) {
            String user = itemBean.downloadTimesFormat.replace("次", "人");
            viewHolder.appSize.setText(BaseApplication.curContext.getString(
                    R.string.person_use, user));
        }

        // viewHolder.HeaderRylt.requestDisallowInterceptTouchEvent(true);
        viewHolder.HeaderRylt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UIUtils.gotoAppDetail(itemBean, itemBean.id, (AppDownLoadQueenActivity) mContext );
                MTAUtil.onDetailClick(mContext, itemBean.name, itemBean.packageName);
                MTAUtil.onClickDownLoadQueenNOData(position+1);
            }
        });

        viewHolder.progressButton.setGameInfo(itemBean);
        viewHolder.progressButton.enabelDefaultPress(true);

        //------------------------添加点击事件------------------------//
        viewHolder.progressButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

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
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        &&ClientInfo.getAPNType(BaseApplication.curContext)!= ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(mContext,
                                    R.style.add_dialog);
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
                                            //viewHolder.progressButton.onClick();
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            viewHolder.progressButton.onClick();
                            break;
                    }

                } else {
                    viewHolder.progressButton.onClick();
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



    private static class ViewHolder {
        AnimDownloadProgressButton progressButton;
        TextView        appName;
        TextView        appSize;
        CustomImageView customImageView;
        LinearLayout    HeaderRylt;
    }

}
