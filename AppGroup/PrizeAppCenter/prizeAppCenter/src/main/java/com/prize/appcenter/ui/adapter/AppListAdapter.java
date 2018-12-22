package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.ProgressNoGiftButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 游戏列表适配器
 *
 * @author prize
 */
public class AppListAdapter extends GameListBaseAdapter {
    private ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private IUIDownLoadListenerImp listener = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private String cardId;
    protected View.OnClickListener mOnClickListener;

    public AppListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mHandler = new Handler();
//        drawable = activity.getResources().getDrawable(
//                R.drawable.icon_list_gift);
//        transparentDrawable = new ColorDrawable(Color.TRANSPARENT);

        listener = new IUIDownLoadListenerImp() {
            @Override
            public void onRefreshUI(String pkgName, int position) {
                if (isActivity) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            notifyDataSetChanged();

                        }
                    });
                }
            }

        };
        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                final AppsItemBean collectionBean = (AppsItemBean) v.getTag(R.id.id_appitem);
                ProgressNoGiftButton downloadBtn = (ProgressNoGiftButton) v.getTag(R.id.id_viewhold);
                int state = AIDLUtils.getGameAppState(
                        collectionBean.packageName, collectionBean.id,
                        collectionBean.versionCode);
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
                            mDownDialog = new DownDialog(mActivities.get(),
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
                                            UIUtils.downloadApp(collectionBean);
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
        };
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(ArrayList<AppsItemBean> data, String cardId) {
        if (data != null && data.size() > 0) {
            items = data;
            this.cardId = cardId;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<AppsItemBean> data) {
        if (data != null && data.size() > 0) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空游戏列表
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Activity activity = mActivities.get();
        if (activity == null) {
            return convertView;
        }
        super.getView(position, convertView, parent);
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.activity_game_listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.game_download_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.game_download_Rlyt);
            viewHolder.gameIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
            viewHolder.gameName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.gameSize = (TextView) convertView
                    .findViewById(R.id.game_size_tv);
            viewHolder.downLoadCount = (TextView) convertView
                    .findViewById(R.id.download_count_tv);
            viewHolder.gameTagTV = (TextView) convertView
                    .findViewById(R.id.game_tag_tv);
            viewHolder.downloadBtn = (ProgressNoGiftButton) convertView
                    .findViewById(R.id.game_download_btn);
            viewHolder.tag_container = (FlowLayout) convertView
                    .findViewById(R.id.tag_container);
            viewHolder.ourtag_container = (FlowLayout) convertView
                    .findViewById(R.id.ourtag_container);
            viewHolder.game_brief = (TextView) convertView
                    .findViewById(R.id.game_brief);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean collectionBean = items.get(position);
        if (null == collectionBean) {
            return convertView;
        }
        if (!TextUtils.isEmpty(cardId)) {
            collectionBean.cardId = this.cardId;
        }

        viewHolder.gameName.setLayoutParams(param);
        viewHolder.ourtag_container.setVisibility(View.GONE);
        viewHolder.ourtag_container.removeAllViews();
        if (!TextUtils.isEmpty(collectionBean.ourTag)) {
            viewHolder.gameName.setLayoutParams(param2);
            viewHolder.ourtag_container.setVisibility(View.VISIBLE);
            String[] tags = null;
            if (collectionBean.ourTag.contains(",")) {
                tags = collectionBean.ourTag.split(",");

            } else {
                tags = new String[]{collectionBean.ourTag};
            }
            if (tags != null && tags.length > 0) {
                LinearLayout.LayoutParams params1 = new LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params1.setMargins(0, 0, 12, 0);
                int size = tags.length;
                int requireLen = size > 2 ? 2 : size;
                for (int i = 0; i < requireLen; i++) {
                    if (TextUtils.isEmpty(tags[i])) {
                        continue;
                    }
                    TextView tagView = (TextView) LayoutInflater.from(activity)
                            .inflate(R.layout.item_textview, null);
                    tagView.setText(tags[i]);
                    if ("首发".equals(tags[i])) {
                        tagView.setTextColor(activity.getResources().getColor(
                                R.color.text_color_shoufa));
                        tagView.setBackgroundResource(R.drawable.bg_list_tag);

                    } else if ("独家".equals(tags[i])) {

                        tagView.setTextColor(activity.getResources().getColor(
                                R.color.text_color_dujia));
                        tagView.setBackgroundResource(R.drawable.icon_dujia);
                    } else {

                        tagView.setTextColor(activity.getResources().getColor(
                                R.color.text_color_fea53c));
                        tagView.setBackgroundResource(R.drawable.bg_list_tag);
                    }
                    tagView.setLayoutParams(params1);
                    viewHolder.ourtag_container.addView(tagView);
                }
            }
        }

        if (!TextUtils.isEmpty(collectionBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(collectionBean.largeIcon,
                    viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
        } else {

            ImageLoader.getInstance()
                    .displayImage(collectionBean.iconUrl,
                            viewHolder.gameIcon,
                            UILimageUtil.getUILoptions(), null);
        }
        viewHolder.gameName.setText(collectionBean.name);
        // 2015-12-08 名称显示不全

        viewHolder.gameName.setLayoutParams(param);

        viewHolder.gameSize.setText(collectionBean.apkSizeFormat);
        if (null != collectionBean.downloadTimesFormat) {
            viewHolder.downLoadCount.setVisibility(View.VISIBLE);
            String user = collectionBean.downloadTimesFormat.replace("次", "人");
            viewHolder.downLoadCount.setText(activity.getString(
                    R.string.person_use, user));
        } else {
            viewHolder.downLoadCount.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(collectionBean.brief)) {
            viewHolder.game_brief.setVisibility(View.VISIBLE);
            viewHolder.game_brief.setText(collectionBean.brief);
            viewHolder.tag_container.setVisibility(View.GONE);
        } else {
            viewHolder.game_brief.setVisibility(View.GONE);
            // 添加标签
            if (!TextUtils.isEmpty(collectionBean.categoryName)
                    || !TextUtils.isEmpty(collectionBean.tag)) {
                viewHolder.game_brief.setVisibility(View.GONE);
                // 添加标签
                viewHolder.tag_container.setVisibility(View.VISIBLE);
                viewHolder.tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                int rightMargin = activity.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, rightMargin, 12, 0);
                TextView tagView1 = (TextView) LayoutInflater.from(activity)
                        .inflate(R.layout.item_textview, null);
                tagView1.setText(collectionBean.categoryName);
                tagView1.setLayoutParams(params);
                viewHolder.tag_container.addView(tagView1);
                if (!TextUtils.isEmpty(collectionBean.tag)) {
                    String[] tags = collectionBean.tag.split(" ");
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 3 ? 3 : size;
                        for (int i = 0; i < requireLen; i++) {
                            if (!TextUtils.isEmpty(collectionBean.categoryName)
                                    && collectionBean.categoryName
                                    .equals(tags[i])) {
                                continue;
                            }
                            TextView tagView = (TextView) LayoutInflater.from(
                                    activity).inflate(R.layout.item_textview,
                                    null);
                            tagView.setText(tags[i]);
                            tagView.setLayoutParams(params);
                            viewHolder.tag_container.addView(tagView);
                        }
                    }
                }
            } else {
                viewHolder.game_brief.setVisibility(View.VISIBLE);
                viewHolder.game_brief.setText("");
                viewHolder.tag_container.setVisibility(View.GONE);
            }

        }

        viewHolder.game_download_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewHolder.downloadBtn.performClick();

            }
        });
        viewHolder.downloadBtn.setOnClickListener(mOnClickListener);
        viewHolder.downloadBtn.setTag(R.id.id_appitem, collectionBean);
        viewHolder.downloadBtn.setTag(R.id.id_viewhold, viewHolder.downloadBtn);
        viewHolder.downloadBtn.setGameInfo(collectionBean);
        return convertView;
    }

    private DownDialog mDownDialog;

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    static class ViewHolder {
        // 游戏图标
        ImageView gameIcon;
        // 游戏名称
        TextView gameName;
        // 游戏大小
        TextView gameSize;
        // 游戏下载量
        TextView downLoadCount;
        // 下载按钮
        ProgressNoGiftButton downloadBtn;
        // 游戏推荐图标
        // ImageView gameCornerIcon;
        // 游戏介绍
        TextView game_brief;
        /**
         * 内测，公测等
         */
        TextView gameTagTV;
        // /** 评分 */
        // TextView ratingBar;
        FlowLayout ourtag_container;
        FlowLayout tag_container;

        RelativeLayout game_download_Rlyt;
    }

    // 取消下载
    // private void downloadCancel(GameBean item) {
    // AppManagerCenter.cancelDownload(item);
    // }

    public void onItemClick(int position, boolean isFrom3rdApp) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        AppsItemBean item = items.get(position);
        if (null != item) {
            // 跳转到详细界面
            // UIUtils.gotoAppDetail(item.id);

            Intent intent = new Intent(MainApplication.curContext,
                    AppDetailActivity.class);
            intent.putExtra("appId", item.id);
            intent.putExtra("from", "list");
            MainApplication.curContext.startActivity(intent);
        }
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public boolean setDownlaodRefreshHandle() {
        // AppManagerCenter.setDownloadRefreshHandle(listener);
        return AIDLUtils.registerCallback(listener);
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
}
