package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.AppGiftCodes;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ProgressButton;

import java.util.ArrayList;

/**
 * 我的礼包adaptger
 * *
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class MyGiftListAdapter extends GameListBaseAdapter {
    private ArrayList<AppGiftCodes> items = new ArrayList<AppGiftCodes>();
    // private ArrayList<AppsItemBean> items = new ArrayList<AppsItemBean>();

    private IUIDownLoadListenerImp listener = null;
    protected RootActivity activity;
    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private DownDialog mDownDialog;

    private MySubGiftListAdapter adapter;

    public MyGiftListAdapter(RootActivity activity) {
        super(activity);
        this.activity = activity;
        isActivity = true;
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state,boolean isNewDownload) {
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

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /**
     * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
     */
    public void setData(ArrayList<AppGiftCodes> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<AppGiftCodes> data) {
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
    public AppGiftCodes getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.mygift_listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.gameIcon = (ImageView) convertView
                    .findViewById(R.id.game_iv);
            viewHolder.Rlyt = (RelativeLayout) convertView
                    .findViewById(R.id.Rlyt);
            viewHolder.gameName = (TextView) convertView
                    .findViewById(R.id.game_name_tv);
            viewHolder.downloadBtn = (ProgressButton) convertView
                    .findViewById(R.id.game_download_btn);
            viewHolder.listView = (ListView) convertView
                    .findViewById(R.id.mylist);
            convertView.setTag(viewHolder);
            super.getView(position, convertView, parent);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AppsItemBean gameBean = getItem(position).app;

        viewHolder.Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                // 跳转到详细界面
                // UIUtils.gotoAppDetail(gameBean.id);
                // View shareView = view.findViewById(R.id.game_iv);
                UIUtils.gotoAppDetail(gameBean,
                        gameBean.id, activity);
            }
        });

        viewHolder.downloadBtn.setGameInfo(gameBean);
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int state = AIDLUtils.getGameAppState(gameBean.packageName,
                        gameBean.id + "", gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:


                        if (ClientInfo.networkType == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.networkType != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(activity,
                                    R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(gameBean);
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
        adapter = new MySubGiftListAdapter(this.activity);
        adapter.setData(getItem(position).giftCodes, gameBean.id);
        viewHolder.listView.setAdapter(adapter);
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
        // 下载按钮
        ProgressButton downloadBtn;
        /**
         * 评分
         */
        ListView listView;
        RelativeLayout Rlyt;

    }

    public void onItemClick(int position) {
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
    public void setDownlaodRefreshHandle() {
//		AppManagerCenter.setDownloadRefreshHandle(listener);
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

    // /**
    // * 继续提示对话框
    // */
    // private View.OnClickListener mDeletePromptListener = new
    // View.OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // JLog.i("long", "mBean=" + mBean + "--df=" + df);
    // df.dismissAllowingStateLoss();
    // if (mBean != null) {
    // UIUtils.downloadApp(mBean);
    // }
    // }
    // };

}
