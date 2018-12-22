package com.prize.prizethemecenter.ui.adapter;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.DownLoadQueenActivity;
import com.prize.prizethemecenter.bean.AppDownloadQueenData;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.StringUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.CornerImageView;
import com.prize.prizethemecenter.ui.widget.DownLoadButton;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Administrator on 2016/11/11.
 */
public class DownloadQueenListViewAdapter extends BaseAdapter {

    private static final String SIZE = "size";
    public static final int NOPAUSE = 1;
    public static final int NODOWNLOAD = 2;

    public static final int DOWNLOADING_DATA = 0;
    public static final int DOWNLOADED_DATA = 1;
    public static final int DIVIDE = 2;
    int tempSpeed = 0;

    private static String TAG = "DownloadQueenListViewAdapter";
    private DownLoadQueenActivity mCtx;
    /*判断当前页是否处于显示状态*/
    private boolean isActivity = true;

    private ArrayList<HashMap<String, Object>> cacheData = new ArrayList<>();
    private AppDownloadQueenData queenData;
    private UIDownLoadListener listener = null;

    private boolean hasDownLoaded = false;
    private Handler mMainHandler = new Handler();
    private Runnable DataSetChanged = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    public DownloadQueenListViewAdapter(DownLoadQueenActivity mCtx) {
        super();
        this.mCtx = mCtx;
        listener = new UIDownLoadListener() {

            @Override
            protected void onErrorCode(int theme_Id, int errorCode) {
                notifyDataSetChanged();
            }

            @Override
            protected void onUpdateProgress(int theme_Id) {
                if (isActivity) {
                    notifyDataSetChanged();
                }
            }

            @Override
            protected void onFinish(int theme_Id) {
                notifyDataSetChanged();
            }

            @Override
            protected void onStart(int theme_Id) {
                notifyDataSetChanged();
            }

            @Override
            public void onRefreshUI(int theme_Id) {

            }
        };
    }

    public void setDownLoadQueenData(AppDownloadQueenData queenData) {
        if (this.queenData != null) {
            this.queenData.clear();
        }
        this.queenData = queenData;
        cacheData.clear();
        for (int i = 0; i < queenData.size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(SIZE, 0);
            // map.put(TIME, System.currentTimeMillis());
            cacheData.add(map);
        }
        notifyDataSetChanged();

    }

    public void sethasDownLoaded(boolean hasDownLoaded) {
        this.hasDownLoaded = hasDownLoaded;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        return (int) queenData.get(position).get(DownLoadQueenActivity.TYPE);
    }

    @Override
    public int getCount() {
        return queenData != null ? queenData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return queenData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        synchronized (DownloadQueenListViewAdapter.class) {
            if (queenData == null || queenData.size() <= 0) return convertView;

            final ViewHolder viewHolder;
            final ViewHolderDownloaded viewHolderDownLoaded;
            int itemViewType = getItemViewType(position);
            if (convertView == null) {
                switch (itemViewType) {
                    case DOWNLOADING_DATA:
                        viewHolder = new ViewHolder();
                        convertView = LayoutInflater.from(mCtx).inflate(R.layout.item_downloading_list, null);
                        viewHolder.imageIv = (CornerImageView) convertView.findViewById(R.id.image_iv);
                        viewHolder.btManage = (DownLoadButton) convertView.findViewById(R.id.bt_manage);
                        viewHolder.pbCurrent = (ProgressBar) convertView.findViewById(R.id.pb_current);
                        viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                        viewHolder.tvSpeed = (TextView) convertView.findViewById(R.id.tv_speed);
                        viewHolder.tvLoad = (TextView) convertView.findViewById(R.id.tv_load);
                        viewHolder.tvTotal = (TextView) convertView.findViewById(R.id.tv_total);
                        viewHolder.btDelete = (Button) convertView.findViewById(R.id.bt_delete);
                        viewHolder.tvPause = (TextView) convertView.findViewById(R.id.tv_pause);
                        viewHolder.pbCurrent.setMax(100);
                        convertView.setTag(viewHolder);
                        convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        getLoadingView(viewHolder, position);
                        break;

                    case DOWNLOADED_DATA:
                        viewHolderDownLoaded = new ViewHolderDownloaded();
                        convertView = LayoutInflater.from(mCtx).inflate(R.layout.item_downloaded_list, null);
                        viewHolderDownLoaded.imageIv = (CornerImageView) convertView.findViewById(R.id.image_iv);
                        viewHolderDownLoaded.rlItem = (RelativeLayout) convertView.findViewById(R.id.RL_item);
                        viewHolderDownLoaded.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                        viewHolderDownLoaded.tvTotal = (TextView) convertView.findViewById(R.id.tv_total);
                        convertView.setTag(viewHolderDownLoaded);
                        getLoadedView(viewHolderDownLoaded, position);
                        break;
                    case DIVIDE:
                        convertView = LayoutInflater.from(mCtx).inflate(R.layout.downloaded_title_layout, null);
                        TextView mDeleteAll = (TextView) convertView.findViewById(R.id.all_delete_id);
                        mDeleteAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (queenData != null && queenData.size() >= 0) {
                                    ArrayList<SingleThemeItemBean.ItemsBean> downloadDatas = new ArrayList<>();
                                    ArrayList<HashMap<String, Object>> downloadedData = queenData.getDownloadedData();
                                    for (HashMap<String, Object> hashMap : downloadedData) {
                                        SingleThemeItemBean.ItemsBean itemBean = (SingleThemeItemBean.ItemsBean) hashMap.get(DownLoadQueenActivity.DATA);
                                        if (itemBean != null) {
                                            downloadDatas.add(itemBean);
//                                            DBUtils.deleteDownloadById(itemBean.getId(), itemBean.getType());
                                        }
                                    }
                                    DBUtils.deleteDownloadTable();
                                    queenData.clearDownloadedData();
                                    hasDownLoaded = false;
                                }
                                notifyDataSetChanged();
                                if (queenData.size() <= 0) {
                                    mCtx.isShowDefaultView(true);
                                }
                            }
                        });
                }
            } else {
                switch (itemViewType) {
                    case DOWNLOADING_DATA:
                        viewHolder = (ViewHolder) convertView.getTag();
                        getLoadingView(viewHolder, position);
                        break;
                    case DOWNLOADED_DATA: // 返回已下载的itemView
                        viewHolderDownLoaded = (ViewHolderDownloaded) convertView.getTag();
                        getLoadedView(viewHolderDownLoaded, position);
                        break;
                    case DIVIDE: // 返回下载中和已下载中间的分割视图
                        break;
                    default:
                        break;
                }
            }
        }
        return convertView;
    }


    private class ViewHolder {
        private CornerImageView imageIv;
        private TextView tvName;
        private TextView tvLoad;
        private TextView tvTotal;
        private TextView tvSpeed;
        private DownLoadButton btManage;
      private Button btDelete;
        private TextView tvPause;
        private ProgressBar pbCurrent;
    }

    private class ViewHolderDownloaded {
        private CornerImageView imageIv;
        private TextView tvName;
        private TextView tvTotal;
        private RelativeLayout rlItem;
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AppManagerCenter.removeDownloadRefreshHandle(listener);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AppManagerCenter.setDownloadRefreshHandle(listener);
    }

    private void getLoadingView(final ViewHolder viewHolder, final int position) {
        final SingleThemeItemBean.ItemsBean bean = (SingleThemeItemBean.ItemsBean) queenData.get(position).get(DownLoadQueenActivity.DATA);

        final int state = AppManagerCenter.getGameAppState(bean, bean.getType());
        viewHolder.btManage.setGameInfo(bean);
        viewHolder.btManage.setProgressEnable(false);
        viewHolder.btManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(mCtx.getString(R.string.nonet_connect));
                    return;
                }
                String wifiSettingString = DataStoreUtils.readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
                if (wifiSettingString.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE)
                        && ClientInfo.networkType != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            MainApplication.getDownloadManager().startDownload(bean, true, bean.getType());
                            break;
                        default:
                            viewHolder.btManage.OnClick(mCtx);
                            notifyDataSetChanged();
                            break;
                    }
                } else {
                    viewHolder.btManage.OnClick(mCtx);
                    notifyDataSetChanged();
                }
            }
        });

        viewHolder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < queenData.size() && position < cacheData.size()
                        && position >= 0) {
                    AppManagerCenter.cancelDownload(bean,bean.getType());
                    queenData.removeDownloadingItemData(position);
                    cacheData.remove(position);
                }
                notifyDataSetChanged();
                if (queenData.downloadingDataSize() <= 0) {
                    mCtx.removeHeadView();
                }
                if (queenData.size() <= 0) {
                    mCtx.isShowDefaultView(true);
                }
            }
        });

        switch (bean.getType()) {
            case 1:
                viewHolder.imageIv.setImageResource(R.drawable.mine_theme);
                break;
            case 2:
                viewHolder.imageIv.setImageResource(R.drawable.mine_wallpaper);
                break;
            case 3:
                viewHolder.imageIv.setImageResource(R.drawable.mine_font);
                break;
        }
        if (bean.getName() != null) {
            viewHolder.tvName.setText(bean.getName());
        }
        long total_size = DBUtils.findFifleSizeByID(bean.getId() + bean.getType());
        viewHolder.tvTotal.setText("/" + StringUtils.formatFileSize(total_size));
        final float progress = MainApplication.getDownloadManager().getDownloadProgress(bean.getId()+bean.getType());
        mMainHandler.post(new Runnable() {
            int mProgress = 0;

            @Override
            public void run() {
                if (progress > 0.0 && progress < 1.0f) {
                    mProgress = 1;
                } else {
                    mProgress = (int) progress;
                }
                viewHolder.pbCurrent.setProgress((int) progress);
            }
        });
        viewHolder.tvLoad.setText(StringUtils.formatFileSize((long) (progress * total_size / 100f)));

        if (state == AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE) {
            viewHolder.tvSpeed.setVisibility(View.GONE);
            viewHolder.tvPause.setVisibility(View.VISIBLE);
            viewHolder.tvPause.setText("已暂停");
        } else if (state == AppManagerCenter.APP_STATE_WAIT) {
            viewHolder.tvSpeed.setVisibility(View.GONE);
            viewHolder.tvPause.setVisibility(View.VISIBLE);
            viewHolder.tvPause.setText(mCtx.getResources().getText(R.string.wait_task_start));
        } else {
            viewHolder.tvSpeed.setVisibility(View.VISIBLE);
            viewHolder.tvPause.setVisibility(View.GONE);

            int speed = MainApplication.getDownloadManager().getDownloadSpeed(bean.getId()+bean.getType());

            if (speed >= 1000 ) {
                viewHolder.tvSpeed.setText(String.format("%1$.2f", speed / (1024f)) + "MB/s");
            } else if (speed > 0 && speed < 1000) {
                viewHolder.tvSpeed.setText(speed + "KB/s");
            }
            if(tempSpeed == speed){
                viewHolder.tvSpeed.setText(0 + "KB/s");
            }
            tempSpeed = speed;
            if (progress == 100) {
                mHandler.sendEmptyMessageDelayed(position, 0);
            }

            if (state == AppManagerCenter.APP_STATE_INSTALLED) {
                mHandler.sendEmptyMessageDelayed(position, 0);
            }
        }
    }

    private void getLoadedView(ViewHolderDownloaded viewHolderDownLoaded, int position) {
        final SingleThemeItemBean.ItemsBean itemBean = (SingleThemeItemBean.ItemsBean) queenData.get(position).get(DownLoadQueenActivity.DATA);
        if (itemBean == null|| itemBean.getSize().equals("0B"))
            return;
        switch (itemBean.getType()) {
            case 1:
                viewHolderDownLoaded.imageIv.setImageResource(R.drawable.mine_theme);
                break;
            case 2:
                viewHolderDownLoaded.imageIv.setImageResource(R.drawable.mine_wallpaper);
                break;
            case 3:
                viewHolderDownLoaded.imageIv.setImageResource(R.drawable.mine_font);
                break;
        }

        if (itemBean.getName() != null) {
            viewHolderDownLoaded.tvName.setText(itemBean.getName());
        }

        viewHolderDownLoaded.rlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (itemBean.getType()) {
                    case 1:
                        if (itemBean.getThumbnail() != null) {
                            UIUtils.gotoThemeDetail(itemBean.getId(), itemBean.getThumbnail());
                        }
                        break;
                    case 2:
                        if (itemBean.getThumbnail() != null) {
                            UIUtils.gotoWallDetail(mCtx, itemBean.getId(), itemBean.getWallpaper_type(), itemBean.getWallpaper_pic());
                        }
                        break;
                    case 3:
                        if (itemBean.getThumbnail() != null) {
                            UIUtils.gotoFontDetail(itemBean.getId(), itemBean.getAd_pictrue(),false);
                        }
                        break;
                }
            }
        });
        long total_size = DBUtils.findFifleSizeByID(itemBean.getId()+itemBean.getType());
        viewHolderDownLoaded.tvTotal.setText(StringUtils.formatFileSize(total_size));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final SingleThemeItemBean.ItemsBean itemBean = (SingleThemeItemBean.ItemsBean) queenData.get(msg.what)
                    .get(DownLoadQueenActivity.DATA);
            if (itemBean != null) {
                int state = AppManagerCenter.getGameAppState(itemBean, itemBean.getType());
                switch (state) {
                    case AppManagerCenter.APP_STATE_DOWNLOADED:
                    case AppManagerCenter.APP_STATE_INSTALLED:
                    case AppManagerCenter.APP_STATE_UNEXIST:
                        if (msg.what < cacheData.size()) {
                            queenData.removeDownloadingItemData(msg.what);
                            cacheData.remove(msg.what);
                            if (queenData.downloadingDataSize() <= 0) {
                                mCtx.removeHeadView();
                            }
                            if (!hasDownLoaded) {
                                HashMap<String, Object> mapOne = new HashMap<>();
                                mapOne.put(DownLoadQueenActivity.TYPE,
                                        DownloadQueenListViewAdapter.DIVIDE);
                                mapOne.put(DownLoadQueenActivity.DATA,
                                        null);
                                queenData.addDivideData(mapOne);
                                hasDownLoaded = true;
                            }
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(DownLoadQueenActivity.TYPE,
                                    DOWNLOADED_DATA);
                            map.put(DownLoadQueenActivity.DATA, itemBean);
                            queenData.reSetDownloadedData(map);
                            //去除重复
                            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                            for (int i = queenData.getDownloadedData()
                                    .size() - 1; i >= 0; i--) {
                                if (!list.contains(queenData.getDownloadedData().get(i))) {
                                    list.add(queenData.getDownloadedData().get(i));
                                } else {
                                    queenData.getDownloadedData().remove(
                                            queenData.getDownloadedData()
                                                    .get(i));
                                }
                            }
                            notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
                notifyDataSetChanged();
            }
        }
    };
}
