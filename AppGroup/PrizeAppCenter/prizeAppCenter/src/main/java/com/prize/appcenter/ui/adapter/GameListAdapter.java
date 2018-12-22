package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
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
import com.prize.app.beans.TopicItemBean;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.activity.SingleGameActivity;
import com.prize.appcenter.activity.TagListActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;
import static com.prize.appcenter.R.id.ourtag_container;

/**
 * 游戏列表适配器
 *
 * @author prize
 */
public class GameListAdapter extends GameListBaseAdapter {
    private List<AppsItemBean> items = new ArrayList<AppsItemBean>();
    private IUIDownLoadListenerImp listener = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private DownDialog mDownDialog;
    private TopicItemBean topicBean;
    /**
     * 应用类型
     */
    private static final int COMMON_APP = 0;
    /**
     * 推广应用
     */
    private static final int ADVER_APP = 1;
    private String gui,  widget;
    public GameListAdapter(RootActivity activity,String gui, String widget) {
        super(activity);
        this.gui=gui;
        this.widget=widget;
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, 1.0f);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int state, boolean isNewDownload) {
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

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(listener);
    }

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(List<AppsItemBean> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    /**
     * 设置样式
     */
    public void setStyle(TopicItemBean bean) {
        if (bean != null) {
            topicBean = bean;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(List<AppsItemBean> data) {
        if (data != null) {
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
        if(!TextUtils.isEmpty(gui)&&!TextUtils.isEmpty(widget)){
            return CommonUtils.formatAppPageInfo(items.get(position), gui, widget, position + 1);
        }else{
            return items.get(position);

        }
    }

    @Override
    public int getItemViewType(int position) {
        AppsItemBean bean = getItem(position);
        return bean.isAdvertise ? ADVER_APP : COMMON_APP;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
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
            viewHolder.game_brief = (TextView) convertView
                    .findViewById(R.id.game_brief);
            viewHolder.tag_container = (FlowLayout) convertView
                    .findViewById(R.id.tag_container);
            viewHolder.ourtag_container = (FlowLayout) convertView
                    .findViewById(ourtag_container);
            viewHolder.downloadBtn = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.game_download_btn);

            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean gameBean = getItem(position);

        if (topicBean != null) {
            // 设置style颜色
            if (topicBean.style != null) {
                viewHolder.gameName.setTextColor(Color
                        .parseColor(topicBean.style.nameColor));
                viewHolder.gameSize.setTextColor(Color
                        .parseColor(topicBean.style.contentColor));
                viewHolder.game_brief.setTextColor(Color
                        .parseColor(topicBean.style.contentColor));
            }
        }

        viewHolder.gameName.setLayoutParams(param);
        viewHolder.ourtag_container.setVisibility(View.GONE);
        viewHolder.ourtag_container.removeAllViews();
        if (JLog.isDebug) {
            JLog.i(TAG, "gameBean=" + gameBean.name + "--gameBean.ourTag=" + gameBean.ourTag);
        }
        if (TextUtils.isEmpty(gameBean.customTags)) {
            LinearLayout.LayoutParams params1 = new LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
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
                        params1.setMargins(0, 0, 8, 0);
                        int size = tags.length;
                        int requireLen = size > 2 ? 2 : size;
                        TextView tagView;
                        for (int i = 0; i < requireLen; i++) {
                            if (TextUtils.isEmpty(tags[i])) {
                                continue;
                            }
                            tagView = (TextView) LayoutInflater.from(mActivity)
                                    .inflate(R.layout.item_textview, null);
                            tagView.setText(tags[i]);
                            tagView.setTextColor(mActivity.getResources()
                                    .getColor(R.color.text_color_009def));
                            tagView.setBackgroundResource(R.drawable.bg_list_tag);
                            tagView.setLayoutParams(params1);
                            viewHolder.ourtag_container.addView(tagView);
                        }
                    }

                }
                if (gameBean.giftCount > 0) {
                    TextView tagView = (TextView) LayoutInflater.from(mActivity)
                            .inflate(R.layout.item_textview, null);
                    tagView.setText(R.string.gamedetail_gift_title);
                    tagView.setTextColor(mActivity.getResources()
                            .getColor(R.color.text_color_ff9732));
                    tagView.setBackgroundResource(R.drawable.bg_list_tag_gift);
                    tagView.setLayoutParams(params1);
                    viewHolder.ourtag_container.addView(tagView);
                }

            }
        } else {
            viewHolder.gameName.setLayoutParams(param2);
            viewHolder.ourtag_container.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params1 = new LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(0, 0, 8, 0);
            TextView tagView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(gameBean.customTags);
            tagView.setTextColor(mActivity.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setLayoutParams(params1);
            viewHolder.ourtag_container.addView(tagView);
        }

        viewHolder.downloadBtn.setGameInfo(gameBean);
        viewHolder.downloadBtn.enabelDefaultPress(true);
        viewHolder.game_download_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewHolder.downloadBtn.performClick();

            }
        });
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int state = AIDLUtils.getGameAppState(
                        gameBean.packageName, gameBean.id + "",
                        gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

                        if (ClientInfo.getAPNType(mActivity) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                        if (mActivity instanceof TagListActivity && state == AppManagerCenter.APP_STATE_UNEXIST) {
//                            PrizeStatUtil.onClickBackParams(gameBean.backParams, gameBean.name, gameBean.packageName);
                            AIDLUtils.upload360ClickDataNow(gameBean.backParams, gameBean.name, gameBean.packageName);
                        }
                        if (mActivity instanceof TopicDetailActivity && state == AppManagerCenter.APP_STATE_UNEXIST) {
                            if (null != topicBean && !TextUtils.isEmpty(topicBean.id)) {
                                MTAUtil.onTopicDetailPositionClick(topicBean.id + "_" + (position + 1));
                            }
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(mActivity) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mActivity, R.style.add_dialog);
                            }
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            startAnimation(state, viewHolder.gameIcon);
                                            UIUtils.downloadApp(gameBean);
                                            if (mActivity instanceof SearchActivity) {
                                                PrizeStatUtil.onSearchResultItemClick(gameBean.id, gameBean.packageName, gameBean.name, ((SearchActivity) mActivity).getKeyWord(), false);
                                            }
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
                if (ClientInfo.networkType == ClientInfo.WIFI) {
                    startAnimation(state, viewHolder.gameIcon);
                }
                if (mActivity instanceof SingleGameActivity) {
                    MTAUtil.onSingleGamePosition(position + 1);

                }
            }
        });
        if (!TextUtils.isEmpty(gameBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(gameBean.largeIcon,
                    viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
        } else {

            if (gameBean.iconUrl != null) {
                ImageLoader.getInstance()
                        .displayImage(gameBean.iconUrl, viewHolder.gameIcon,
                                UILimageUtil.getUILoptions(), null);
            }
        }

        if (gameBean.name != null) {
            viewHolder.gameName.setText(gameBean.name);
        }
        viewHolder.gameSize.setText(gameBean.apkSizeFormat);
//        if (null != gameBean.downloadTimesFormat) {
//            String user = gameBean.downloadTimesFormat.replace("次", "人");
//            viewHolder.downLoadCount.setText(mActivity.getString(
//                    R.string.person_use, user));
//        }
        if (!TextUtils.isEmpty(gameBean.brief)) {
            viewHolder.game_brief.setVisibility(View.VISIBLE);
            viewHolder.game_brief.setText(gameBean.brief);
            viewHolder.tag_container.setVisibility(View.GONE);

        } else {
            if (!TextUtils.isEmpty(gameBean.categoryName)
                    || !TextUtils.isEmpty(gameBean.tag)) {
                viewHolder.game_brief.setVisibility(View.GONE);
                // 添加标签
                viewHolder.tag_container.setVisibility(View.VISIBLE);
                viewHolder.tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                int rightMargin = mActivity.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, rightMargin, 12, 0);
                TextView tagView1 = (TextView) LayoutInflater.from(mActivity)
                        .inflate(R.layout.item_textview, null);
                if (!TextUtils.isEmpty(gameBean.categoryName)) {
                    tagView1.setText(gameBean.categoryName);
                    tagView1.setLayoutParams(params);
                    viewHolder.tag_container.addView(tagView1);

                }
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
                                    mActivity).inflate(R.layout.item_textview,
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


        return convertView;
    }

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
        //        // 游戏下载量
//        TextView downLoadCount;
        // 下载按钮
        AnimDownloadProgressButton downloadBtn;
        // 游戏推荐图标
        // ImageView gameCornerIcon;
        /**
         * 游戏介绍
         */
        TextView game_brief;
        FlowLayout tag_container;
        FlowLayout ourtag_container;

        RelativeLayout game_download_Rlyt;

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


    public void startAnimation(int state, ImageView imgeView) {
        Activity activity = mActivities.get();
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(imgeView);
            }
        }
    }

//    public interface OnClickCallBack {
//        public void onClickItem(ImageView view);
//    }

//    public OnClickCallBack onClickCallBack;
//
//    public void setOnClickCallBackListener(OnClickCallBack onClickCallBack) {
//        this.onClickCallBack = onClickCallBack;
//    }
}
