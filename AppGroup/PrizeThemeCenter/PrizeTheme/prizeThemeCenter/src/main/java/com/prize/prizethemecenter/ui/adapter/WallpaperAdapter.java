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
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;


/**
 *
 * Created by pengy on 2016/9/6.
 */
public class WallpaperAdapter extends BaseAdapter{

    private  ArrayList<ThemeItemBean> items = new ArrayList<>();

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
    public WallpaperAdapter(Activity activity){
        context = activity;
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(theme_Id + ""+2);
                if(task != null){
                    if(task.gameDownloadState == DownloadState.STATE_DOWNLOAD_SUCESS || task.gameDownloadState==DownloadState.STATE_DOWNLOAD_INSTALLED)
                    notifyDataSetChanged();
                }else {
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
    public ThemeItemBean getItem(int position) {
        if (position < 0 || items.isEmpty() || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<ThemeItemBean> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    public void addData(ArrayList<ThemeItemBean> data) {
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
        ThemeItemBean bean = getItem(position);
        viewHolder.isNew.setBackground(null);
        /**
         * 判断是否1.新品 2.更新 3.已下载 4.正应用
         */
        int state = 0;

        DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(bean.id+""+2);
        if( task!=null) {
            state = task.gameDownloadState;
        }

       /* boolean update=false;
        if( task!=null) {
            state = task.gameDownloadState;
            update = state>5&&!bean.md5_val.equals( task.loadGame.getMd5());

        }*/
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

        String p = UILimageUtil.getPicPath(context,bean.wallpaper_pic);
        String tag = (String) viewHolder.wallpaper_logo.getTag();
        if(bean!=null && bean.wallpaper_pic!=null&&(tag==null||!tag.equals(bean.wallpaper_pic))){
            ImageLoader.getInstance().displayImage(p,viewHolder.wallpaper_logo,
                    UILimageUtil.getHomeThemeDpLoptions(), UILimageUtil.setTagHolder(viewHolder.wallpaper_logo,bean.wallpaper_pic));
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
