package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.SearchResultData.ResultData;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;


/**
 * 主题分类ID的adapter
 * Created by pengy on 2016/9/6.
 */
public class SearchWallAdapter extends BaseAdapter{

    private  ArrayList<ResultData> items = new ArrayList<>();

    public  Activity context;
    private UIDownLoadListener listener = null;
    /** 当前页是否处于显示状态 */
    private boolean isActivity = true; // 默认true

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    /***
     *
     * @param activity
     */
    public SearchWallAdapter(Activity activity){
        context = activity;
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                DownloadInfo info = DBUtils.findDownloadById(theme_Id+"");
                if(info != null){
                    if(info.getCurrentState() == DownloadState.STATE_DOWNLOAD_SUCESS || info.getCurrentState()==DownloadState.STATE_DOWNLOAD_INSTALLED)
                        notifyDataSetChanged();
                }
            }
        };

    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ResultData getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<ResultData> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    public void addData(ArrayList<ResultData> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
             convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_wallpaper_layout, null);
             viewHolder = new ViewHolder();
             viewHolder.wallpaper_logo = (CornerImageView) convertView
                    .findViewById(R.id.wallpaper_logo);
             viewHolder.isNew = (ImageView) convertView
                    .findViewById(R.id.isNew);
             convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ResultData bean = getItem(position);
        viewHolder.isNew.setBackground(null);

        /**
         * 判断是否1.新品 2.更新 3.已下载 4.正应用
         */
        int state = 0;
        DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(bean.id+""+2);
        if(task!= null) {
            state = task.gameDownloadState;
        }

        if(bean!=null && !TextUtils.isEmpty(bean.is_update) && bean.is_update.equals("1")){   /**更新*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_update);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        else if(state==6){      /**已下载*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_download);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        else if(state==7){    /**正在使用*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_used);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        else if(bean!=null && !TextUtils.isEmpty(bean.is_latest) && bean.is_latest.equals("1")){  /**新品*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_new);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }

        if(bean!=null && bean.wallpaper_pic!=null){
            String p = UILimageUtil.getPicPath(context,bean.wallpaper_pic);
            if(p!=null){
                ImageLoader.getInstance().displayImage(p,viewHolder.wallpaper_logo,
                        UILimageUtil.getHomeThemeDpLoptions(), null);
            }
        }
        return convertView;
    }


    public static class ViewHolder{
        ImageView wallpaper_logo;
        ImageView isNew;
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
}
