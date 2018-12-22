package com.prize.appcenter.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.prize.appcenter.R.id.ourtag_container;

/**
 * 游戏排行适配器
 *
 * @author prize
 */
public class GameRankingAdapter extends GameListBaseAdapter {
    public ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private IUIDownLoadListenerImp listener;
    private boolean isActivity = true; // 默认true
    private DownDialog mDownDialog;
    private String pageName;
    private String gui, widget;

    public GameRankingAdapter(RootActivity activity, String gui, String widget) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, 1.0f);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        mHandler = new Handler();
        this.gui = gui;
        this.widget = widget;
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
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

    public void setIsActivity(boolean state) {
        isActivity = state;
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
     * @param data     ArrayList<AppsItemBean>
     * @param pageName 页面名称
     */
    public void setData(ArrayList<AppsItemBean> data, String pageName) {
        if (null != data) {
            this.items = data;
        }
        this.pageName = pageName;
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
        return CommonUtils.formatAppPageInfo(items.get(position), gui, widget, position + 1);
//        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mListView = (ListView) parent;
        final ViewHolder viewHolder;
        final Activity activity = mActivities.get();
        if (activity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.activity_game_ranking_item, null);
            viewHolder = new ViewHolder();
            viewHolder.game_download_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.game_download_Rlyt);
            viewHolder.gameIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
//            viewHolder.gift_flag_Iv = (TextView) convertView
//                    .findViewById(R.id.gift_flag_Iv);
            viewHolder.gameNumber = (TextView) convertView
                    .findViewById(R.id.game_number_tv);
            viewHolder.gameName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.gameSize = (TextView) convertView
                    .findViewById(R.id.game_size_tv);
//            viewHolder.downLoadCount = (TextView) convertView
//                    .findViewById(R.id.download_count_tv);
            viewHolder.downLoadBtn = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.game_download_btn);
            viewHolder.rank_cicle = (ImageView) convertView
                    .findViewById(R.id.rank_cicle);
            viewHolder.game_brief = (TextView) convertView
                    .findViewById(R.id.game_brief);
            viewHolder.tag_container = (FlowLayout) convertView
                    .findViewById(R.id.tag_container);
            viewHolder.ourtag_container = (FlowLayout) convertView
                    .findViewById(ourtag_container);
            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean gameBean = getItem(position);
        viewHolder.gameName.setLayoutParams(param);
        viewHolder.ourtag_container.setVisibility(View.GONE);
        viewHolder.ourtag_container.removeAllViews();
        LinearLayout.LayoutParams params1 = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 12, 0);
        if (TextUtils.isEmpty(gameBean.customTags)) {
            if (!TextUtils.isEmpty(gameBean.ourTag) || gameBean.giftCount > 0) {
                viewHolder.gameName.setLayoutParams(param2);
                viewHolder.ourtag_container.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(gameBean.ourTag)) {
                    String[] tags = null;
                    if (gameBean.ourTag.contains(",")) {
                        tags = gameBean.ourTag.split(",");

                    } else {
                        tags = new String[]{gameBean.ourTag};
                    }
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 2 ? 2 : size;
                        TextView tagView;
                        for (int i = 0; i < requireLen; i++) {
                            if (TextUtils.isEmpty(tags[i])) {
                                continue;
                            }
                            tagView = (TextView) LayoutInflater.from(activity)
                                    .inflate(R.layout.item_textview, null);
                            tagView.setText(tags[i]);
                            tagView.setTextColor(activity.getResources()
                                    .getColor(R.color.text_color_009def));
                            tagView.setBackgroundResource(R.drawable.bg_list_tag);
                            tagView.setLayoutParams(params1);
                            viewHolder.ourtag_container.addView(tagView);
                        }
                    }
                }
                if (gameBean.giftCount > 0) {
                    TextView tagView = (TextView) LayoutInflater.from(activity)
                            .inflate(R.layout.item_textview, null);
                    tagView.setText(R.string.gamedetail_gift_title);
                    tagView.setTextColor(activity.getResources()
                            .getColor(R.color.text_color_ff9732));
                    tagView.setBackgroundResource(R.drawable.bg_list_tag_gift);
                    tagView.setLayoutParams(params1);
                    viewHolder.ourtag_container.addView(tagView);
                }
            }
        } else {
            viewHolder.gameName.setLayoutParams(param2);
            viewHolder.ourtag_container.setVisibility(View.VISIBLE);
            TextView tagView = (TextView) LayoutInflater.from(activity)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(gameBean.customTags);
            tagView.setGravity(Gravity.CENTER);
            tagView.setTextColor(activity.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setLayoutParams(params1);
            viewHolder.ourtag_container.addView(tagView);
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
        viewHolder.gameNumber.setText(position + 1 + ".");
        viewHolder.gameNumber.setTextColor(Color.parseColor("#000000"));
        if (position == 0) {
            viewHolder.gameNumber.setTextColor(Color.parseColor("#f3251b"));
        }
        if (position == 1) {
            viewHolder.gameNumber.setTextColor(Color.parseColor("#f77e13"));
        }
        if (position == 2) {
            viewHolder.gameNumber.setTextColor(Color.parseColor("#efab19"));
        }
        if (BaseApplication.isThird) {
            viewHolder.rank_cicle
                    .setBackgroundResource(R.color.transparent);
        } else {
            viewHolder.rank_cicle.setBackground(null);
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
                LinearLayout.LayoutParams params = new LayoutParams(
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
        viewHolder.downLoadBtn.enabelDefaultPress(true);
        viewHolder.downLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        if (BaseApplication.isDownloadWIFIOnly()&& ClientInfo.getAPNType(activity) != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(activity, R.style.add_dialog);
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
                                            startAnimation(state, viewHolder.gameIcon);
                                            UIUtils.downloadApp(gameBean);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST && mActivities.get() != null && mActivities.get() instanceof MainActivity) {
                                                AIDLUtils.upload360ClickDataNow(gameBean.backParams, gameBean.name, gameBean.packageName);
                                            }
                                            break;
                                    }
                                }
                            });

                        } else {
                            viewHolder.downLoadBtn.onClick();
                            startAnimation(state, viewHolder.gameIcon);
                            if (state == AppManagerCenter.APP_STATE_UNEXIST && mActivities.get() != null && mActivities.get() instanceof MainActivity) {
                                AIDLUtils.upload360ClickDataNow(gameBean.backParams, gameBean.name, gameBean.packageName);
                            }
                        }
                        break;

                    default://不是需要下载，继续 更新的条件
                        viewHolder.downLoadBtn.onClick();
                        break;
                }

                if (position >= 0 && position <= 14) {
                    MTAUtil.onClickCommomGameRank(position + 1, pageName);
                }

            }

        });


        viewHolder.downLoadBtn.setGameInfo(gameBean);
        return convertView;
    }

    static class ViewHolder {
        // 游戏图标
        ImageView gameIcon;

        //        // 游戏礼包
//        TextView gift_flag_Iv;
        // 游戏排行
        TextView gameNumber;
        // 游戏名字
        TextView gameName;
        // 游戏大小
        TextView gameSize;
        // 游戏下载量
//        TextView downLoadCount;
        // 游戏下载按钮
        AnimDownloadProgressButton downLoadBtn;

        /**
         * 游戏介绍
         */
        TextView game_brief;
        RelativeLayout game_download_Rlyt;
        FlowLayout tag_container;
        FlowLayout ourtag_container;
        ImageView rank_cicle;
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */

    public void resetDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {
            listener.setmCallBack(null);
        }
        mHandler.removeCallbacksAndMessages(null);
    }

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

    // /** 开启下载动画 */
    // private void startAnimation(final ImageView view,
    // final AppsItemBean gameBean, int states) {
    // if (activity instanceof MainActivity) {
    // if (states == AppManagerCenter.APP_STATE_UNEXIST
    // || states == AppManagerCenter.APP_STATE_UPDATE) {
    //
    // // 目标图片
    // View decView = ((MainActivity) activity).getSearchView();
    //
    // AnimationUtil.startAnimationToTop(activity, gameBean, view,
    // decView);
    //
    // }
    // }
    // }

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
