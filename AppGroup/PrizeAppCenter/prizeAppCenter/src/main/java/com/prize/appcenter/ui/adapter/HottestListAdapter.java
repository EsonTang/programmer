package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HottestBean;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppCommentActivity;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CenterDrawableTextView;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.prize.appcenter.R.id.game_image_tag;

/**
 * *
 * 热门分类的adapter
 *
 * @author nieligang
 * @version V1.0
 */
public class HottestListAdapter extends GameListBaseAdapter {
    private ArrayList<HottestBean> items = new ArrayList<HottestBean>();
    private IUIDownLoadListenerImp listener = null;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true

    private ListView mListView;

    public HottestListAdapter(RootActivity activity) {
        super(activity);
        mActivities = new WeakReference<RootActivity>(activity);
        isActivity = true;
        param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
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

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(ArrayList<HottestBean> data) {
        if (data != null && data.size() > 0) {
            items = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<HottestBean> data) {
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
    public HottestBean getItem(int position) {
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
        final Activity activity = mActivities.get();
        mListView = (ListView) parent;
        if (activity == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.activity_hottest_listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.title_layout = (LinearLayout) convertView.findViewById(R.id.title_ll);
            viewHolder.title = (TextView) convertView.findViewById(R.id.hottest_title);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.hottest_img);
            viewHolder.seeMore = (CenterDrawableTextView) convertView.findViewById(R.id.see_more);
            viewHolder.description = (CenterDrawableTextView) convertView.findViewById(R.id.comment_tv);
            viewHolder.game_image_tag = convertView.findViewById(game_image_tag);
            viewHolder.game_detail_Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.item_rlyt);
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
            viewHolder.downloadBtn = (AnimDownloadProgressButton) convertView
                    .findViewById(R.id.game_download_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final HottestBean hottestBean = items.get(position);
        if (null == hottestBean) {
            return convertView;
        }

        viewHolder.title.setText(hottestBean.title);
        ImageLoader.getInstance().displayImage(hottestBean.images,
                viewHolder.image, UILimageUtil.getHottestImgLoptions(), null);

        StringBuilder source = new StringBuilder();
        source.append("<img src='" + R.drawable.quotation_left + "'/>");
        source.append(hottestBean.description);
        source.append("<img src='" + R.drawable.quotation_right + "'/>");

        viewHolder.description.setText(Html.fromHtml(source.toString(), imageGetter, null));

        if (!TextUtils.isEmpty(hottestBean.app.largeIcon)) {
            ImageLoader.getInstance().displayImage(hottestBean.app.largeIcon,
                    viewHolder.gameIcon, UILimageUtil.getHottestAppLoptions(), null);
        } else {

            ImageLoader.getInstance()
                    .displayImage(hottestBean.app.iconUrl,
                            viewHolder.gameIcon,
                            UILimageUtil.getHottestAppLoptions(), null);
        }
        viewHolder.gameName.setText(hottestBean.app.name);
        // 2015-12-08 名称显示不全

        viewHolder.gameName.setLayoutParams(param);

        viewHolder.gameSize.setText(hottestBean.app.apkSizeFormat);
        if (null != hottestBean.app.downloadTimesFormat) {
            viewHolder.downLoadCount.setVisibility(View.VISIBLE);
            String user = hottestBean.app.downloadTimesFormat.replace("次", "人");
            viewHolder.downLoadCount.setText(activity.getString(
                    R.string.person_use, user));
        } else {
            viewHolder.downLoadCount.setVisibility(View.GONE);
        }

        viewHolder.game_download_Rlyt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                viewHolder.downloadBtn.performClick();

            }
        });

        viewHolder.seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hottestBean == null || hottestBean.app == null) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable("appsItemBean", hottestBean.app);
                bundle.putBoolean("isComment", false);

                Intent intent = new Intent(activity, AppCommentActivity.class);
                intent.putExtra("bundle", bundle);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                MTAUtil.onHottestSeeMoreClick(activity);
            }
        });

        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position < 0 || position >= items.size()) {
                    return;
                }
                if (null != hottestBean) {
                    // 跳转到详细界面
                    // UIUtils.gotoAppDetail(item.id);

                    Intent intent = new Intent(activity,
                            AppDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("appid", hottestBean.app.id);
                    intent.putExtra("bundle", bundle);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    MTAUtil.onHottestAppDetailClick(activity);
                }
            }
        });
        viewHolder.game_detail_Rlyt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                viewHolder.image.performClick();
            }
        });

        if (position == (getCount() - 1)) {
            viewHolder.game_image_tag.setVisibility(View.GONE);
        } else {
            viewHolder.game_image_tag.setVisibility(View.VISIBLE);
        }
        viewHolder.downloadBtn.enabelDefaultPress(true);
        viewHolder.downloadBtn.setGameInfo(hottestBean.app);
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int state = AIDLUtils.getGameAppState(
                        hottestBean.app.packageName, hottestBean.app.id + "",
                        hottestBean.app.versionCode);
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
                                            UIUtils.downloadApp(hottestBean.app);
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
            }
        });
        return convertView;
    }

    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            int id = Integer.parseInt(source);

            //根据id从资源文件中获取图片对象
            Drawable d = mActivities.get().getResources().getDrawable(id);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    };

    private DownDialog mDownDialog;

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    static class ViewHolder {

        LinearLayout title_layout;
        TextView title;
        ImageView image;
        CenterDrawableTextView description;
        CenterDrawableTextView seeMore;

        // 游戏图标
        ImageView gameIcon;
        // 游戏名称
        TextView gameName;
        // 游戏大小
        TextView gameSize;
        // 游戏下载量
        TextView downLoadCount;
        // 下载按钮
        AnimDownloadProgressButton downloadBtn;

        RelativeLayout game_detail_Rlyt;
        RelativeLayout game_download_Rlyt;
        View game_image_tag;
    }

    // 取消下载
    // private void downloadCancel(GameBean item) {
    // AppManagerCenter.cancelDownload(item);
    // }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener=null;
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public boolean setDownlaodRefreshHandle() {
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

    public void startAnimation(int state, ImageView imgeView) {
        Activity activity = mActivities.get();
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(imgeView);
            }
        }
    }

    private void updateView(String packageName) {
        if (mListView == null)
            return;
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        int LastVisiblePosition = mListView.getLastVisiblePosition();
        JLog.i("HomePagerListAdapter", "visiblePosition=" + visiblePosition + "----LastVisiblePosition=" + LastVisiblePosition + "---headerViewsCount=" + headerViewsCount + "--packageName=" + packageName);
        for (int i = visiblePosition - headerViewsCount; i <= LastVisiblePosition - headerViewsCount; i++) {
            HottestBean bean = getItem(i);
            if (bean == null)
                continue;
            if (bean.app.packageName.equals(packageName)) {
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
