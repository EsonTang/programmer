package com.prize.appcenter.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
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
import java.util.List;

/**
 * 榜单总榜
 *
 * @author longbaoxiu
 */
public class SubRankOverViewAdapter extends GameListBaseAdapter {
    public List<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private DownDialog mDownDialog;

    public SubRankOverViewAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        param2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }


    /**
     * 清空游戏排行集合
     */
    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * 设置游戏排行集合
     *
     * @param data
     */
    public void setData(List<AppsItemBean> data) {
        if (null != data) {
            this.items = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<AppsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if (position >= items.size() || position < 0)
            return null;
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mListView = (ListView) parent;
        final ViewHolder viewHolder;
        final Activity activity = mActivities.get();
        if (activity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.subitem_rankoverview, null);
            viewHolder = new ViewHolder();
            viewHolder.game_download_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.game_download_Rlyt);
            viewHolder.gameIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
            viewHolder.game_image_tag = convertView
                    .findViewById(R.id.game_image_tag);
            viewHolder.gift_flag_Iv = (TextView) convertView
                    .findViewById(R.id.gift_flag_Iv);
            viewHolder.gameName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.gameSize = (TextView) convertView
                    .findViewById(R.id.game_size_tv);
            viewHolder.downLoadCount = (TextView) convertView
                    .findViewById(R.id.download_count_tv);
            viewHolder.downLoadBtn = (ProgressNoGiftButton) convertView
                    .findViewById(R.id.game_download_btn);
            viewHolder.rank_cicle = (ImageView) convertView
                    .findViewById(R.id.rank_cicle);
            viewHolder.game_brief = (TextView) convertView
                    .findViewById(R.id.game_brief);
            viewHolder.tag_container = (FlowLayout) convertView
                    .findViewById(R.id.tag_container);
            viewHolder.ourtag_container = (FlowLayout) convertView
                    .findViewById(R.id.ourtag_container);
            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean gameBean = items.get(position);
        viewHolder.gameName.setLayoutParams(param);
        viewHolder.ourtag_container.setVisibility(View.GONE);
        viewHolder.ourtag_container.removeAllViews();
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 12, 0);
        if (!TextUtils.isEmpty(gameBean.ourTag)) {
            viewHolder.gameName.setLayoutParams(param2);
            viewHolder.ourtag_container.setVisibility(View.VISIBLE);
            String[] tags = null;
            if (gameBean.ourTag.contains(",")) {
                tags = gameBean.ourTag.split(",");

            } else {
                tags = new String[]{gameBean.ourTag};
            }
            if (tags != null && tags.length > 0) {
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
        if (!TextUtils.isEmpty(gameBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(gameBean.largeIcon,
                    viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
        } else {
            if ((gameBean.iconUrl != null)) {
                ImageLoader.getInstance()
                        .displayImage(gameBean.iconUrl, viewHolder.gameIcon,
                                UILimageUtil.getUILoptions(), null);
            }
        }
        viewHolder.gameName.setText(gameBean.name);
        viewHolder.gameSize.setText(gameBean.apkSizeFormat);

        String user = gameBean.downloadTimesFormat.replace("次", "人");
        viewHolder.downLoadCount.setText(activity.getString(
                R.string.person_use, user));
        viewHolder.game_image_tag.setVisibility(View.VISIBLE);
        if (position %3==0) {
            viewHolder.rank_cicle
                    .setImageResource(R.drawable.icon_numone);
        } else if (position %3==1) {
            viewHolder.rank_cicle
                    .setImageResource(R.drawable.icon_numtwo);
        } else if (position %3==2) {
            viewHolder.rank_cicle
                    .setImageResource(R.drawable.icon_numthree);
            viewHolder.game_image_tag.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(gameBean.brief)) {
            viewHolder.game_brief.setVisibility(View.VISIBLE);
            viewHolder.game_brief.setText(gameBean.brief);
            viewHolder.tag_container.setVisibility(View.GONE);
        } else {
            // 添加标签
            if (!TextUtils.isEmpty(gameBean.categoryName)
                    || !TextUtils.isEmpty(gameBean.tag)) {
                viewHolder.game_brief.setVisibility(View.GONE);
                // 添加标签
                viewHolder.tag_container.setVisibility(View.VISIBLE);
                viewHolder.tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                int rightMargin = activity.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, rightMargin, 12, 0);
                TextView tagView1 = (TextView) LayoutInflater.from(activity)
                        .inflate(R.layout.item_textview, null);
                tagView1.setText(gameBean.categoryName);
                tagView1.setLayoutParams(params);
                viewHolder.tag_container.addView(tagView1);
                if (!TextUtils.isEmpty(gameBean.tag)) {
                    String[] tags = gameBean.tag.split(" ");
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 3 ? 3 : size;
                        for (int i = 0; i < requireLen; i++) {
                            if (!TextUtils.isEmpty(gameBean.categoryName)
                                    && gameBean.categoryName.equals(tags[i])) {
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
                viewHolder.downLoadBtn.performClick();

            }
        });
        viewHolder.downLoadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int state = AIDLUtils.getGameAppState(
                        gameBean.packageName, gameBean.id + "",
                        gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(activity) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(activity) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(activity,
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
                                            startAnimation(state, viewHolder.gameIcon);
                                            UIUtils.downloadApp(gameBean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            viewHolder.downLoadBtn.onClick();
                            break;
                    }

                } else {
                    viewHolder.downLoadBtn.onClick();
                }
                if (ClientInfo.networkType == ClientInfo.WIFI) {
                    startAnimation(state, viewHolder.gameIcon);
                }
            }
        });
        viewHolder.downLoadBtn.setGameInfo(gameBean);
        return convertView;
    }

    static class ViewHolder {
        // 游戏图标
        ImageView gameIcon;

        TextView gift_flag_Iv;
        // 游戏名字
        TextView gameName;
        // 游戏大小
        TextView gameSize;
        // 游戏下载量
        TextView downLoadCount;
        // 游戏下载按钮
        ProgressNoGiftButton downLoadBtn;

        /**
         * 游戏介绍
         */
        TextView game_brief;
        RelativeLayout game_download_Rlyt;
        FlowLayout tag_container;
        FlowLayout ourtag_container;
        ImageView rank_cicle;
        View game_image_tag;
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

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public void startAnimation(int state, ImageView img) {
        final Activity activity = mActivities.get();
        if (activity == null) {
            return;
        }
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(img);
            }
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
            AppsItemBean bean = getItem(i);
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
