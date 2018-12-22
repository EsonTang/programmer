package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CustomImageView;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @ longbaoxiu
 *  2017/10/19.11:59
 *
 */

public class NotopicRcycleAdapter extends RecyclerView.Adapter<NotopicRcycleAdapter.ViewHolder> implements View.OnClickListener {
    private List<AppsItemBean> mData = new ArrayList<>();
    private DownDialog mDownDialog;
    private Context mContext;
    public NotopicRcycleAdapter(Context mContext) {
        this.mContext=mContext;
    }


    public void updateData(List<AppsItemBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_notopic_view, parent, false);
        v.setOnClickListener(this);
        // 实例化viewholder
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final  ViewHolder viewHolder, int position) {
        final AppsItemBean itemBean = mData.get(position);
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, viewHolder.itemImg,
                    UILimageUtil.getUILoptions(), null);
        } else {
            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
            }
        }
        viewHolder.itemView.setTag(position);
        viewHolder.itemName.setText(itemBean.name.trim());
        viewHolder.mProgressNoGiftButton.setGameInfo(itemBean);
        viewHolder.mProgressNoGiftButton.enabelDefaultPress(true);
        viewHolder.mProgressNoGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimDownloadProgressButton downloadBtn = (AnimDownloadProgressButton) v;
                final int state = AIDLUtils.getGameAppState(
                        itemBean.packageName, itemBean.id,
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
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                                mDownDialog = new DownDialog(mContext, R.style.add_dialog);
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
                                                startAnimation(state,viewHolder.itemImg);
                                                break;
                                        }
                                    }
                                });
                            break;
                        default:
                            downloadBtn.onClick();
                            startAnimation(state,viewHolder.itemImg);
                            break;
                    }

                } else {
                    downloadBtn.onClick();
                    startAnimation(state,viewHolder.itemImg);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    public void startAnimation(int state, ImageView itemImg) {
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (mContext instanceof MainActivity) {
                ((MainActivity) mContext).startAnimation(itemImg);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CustomImageView itemImg;
        TextView itemName;
        AnimDownloadProgressButton mProgressNoGiftButton;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImg = (CustomImageView) itemView.findViewById(R.id.appItem_img_id);
            itemName = (TextView) itemView.findViewById(R.id.appItem_name_id);
            mProgressNoGiftButton = (AnimDownloadProgressButton) itemView.findViewById(R.id.progressButton_id);
        }
    }

    private OnItemClickListener mOnItemClickListener = null;
    //define interface
    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }
}
