package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CustomImageView;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏列表适配器
 *
 * @author prize
 */
public class NoTopicListAdapter extends GameListBaseAdapter {
    private List<AppsItemBean> items = new ArrayList<AppsItemBean>();
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private DownDialog mDownDialog;
    private GridView mGridView;

    public NoTopicListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;

    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }


    public void setData(List<AppsItemBean> data) {
        if (data != null) {
            items = data;
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
        final Activity mActivity = mActivities.get();
        if (mActivity == null) {
            return convertView;
        }
        mGridView = (GridView) parent;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.card_item_notopic_view, null);
            viewHolder = new ViewHolder();
            viewHolder.itemImg = (CustomImageView) convertView.findViewById(R.id.appItem_img_id);
            viewHolder.itemName = (TextView) convertView.findViewById(R.id.appItem_name_id);
            viewHolder.mProgressNoGiftButton = (AnimDownloadProgressButton) convertView.findViewById(R.id.progressButton_id);

            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean itemBean = getItem(position);
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, viewHolder.itemImg,
                    UILimageUtil.getUILoptions(), null);
        } else {
            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        viewHolder.itemImg, UILimageUtil.getUILoptions(), null);
            }
        }
        viewHolder.itemName.setText(itemBean.name.trim());
        viewHolder.mProgressNoGiftButton.setGameInfo(itemBean);
        viewHolder.mProgressNoGiftButton.enabelDefaultPress(true);
        viewHolder.mProgressNoGiftButton.setOnClickListener(new OnClickListener() {
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
                            mDownDialog = new DownDialog(mActivity, R.style.add_dialog);
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
                            break;
                        default:
                            downloadBtn.onClick();
                            break;
                    }

                } else {
                    downloadBtn.onClick();
                }
            }
        });


        return convertView;
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    static class ViewHolder {
        CustomImageView itemImg;
        TextView itemName;
        AnimDownloadProgressButton mProgressNoGiftButton;

    }

    public void onItemClick(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        AppsItemBean item = items.get(position);
        if (null != item) {
            // 跳转到详细界面
            UIUtils.gotoAppDetail(item, item.id, mActivities.get());
        }
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

    public void updateView(String packageName) {
        if (mGridView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mGridView.getFirstVisiblePosition();
        int LastVisiblePosition = mGridView.getLastVisiblePosition();
        AppsItemBean bean;
        for (int i = visiblePosition; i <= LastVisiblePosition; i++) {
            bean = getItem(i);
            if (bean == null)
                continue;
            View parentView = mGridView.getChildAt(i - visiblePosition);
            if (parentView == null)
                continue;
            if (bean.packageName.equals(packageName)) {
                View viewBtn = parentView.findViewById(R.id.progressButton_id);
                if (viewBtn != null) {
                    viewBtn.invalidate();
                }
            }
        }
    }

}
