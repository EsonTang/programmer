package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
 * 主题分类ID的adapter
 * Created by pengy on 2016/9/6.
 */
public class ThemeListAdapter extends BaseAdapter{

    private ArrayList<ThemeItemBean> items = new ArrayList<ThemeItemBean>();

    public  Activity context;

    public boolean isLocal;
    /***1.theme  2.wallpaper*/
    public String type;


    private UIDownLoadListener listener = null;

    /***
     *
     * @param activity
     * @param isLocal  是否为本地主题列表
     */
    public ThemeListAdapter(Activity activity, boolean isLocal){

        context = activity;
        this.isLocal = isLocal;
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                DownloadTask task;
                if("wallpaper".equals(type)){
                    task = DownloadTaskMgr.getInstance().getDownloadTask(theme_Id+""+2);
                }else{
                    task = DownloadTaskMgr.getInstance().getDownloadTask(theme_Id+""+1);
                }
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
                    R.layout.item_list_layout, null);
             viewHolder = new ViewHolder();
             viewHolder.theme_logo = (CornerImageView) convertView
                    .findViewById(R.id.theme_logo);
             viewHolder.theme_title = (TextView) convertView
                    .findViewById(R.id.theme_title);
             viewHolder.theme_prize = (TextView) convertView
                    .findViewById(R.id.theme_prize);
            viewHolder.isNew = (ImageView) convertView
                    .findViewById(R.id.isNew);
             viewHolder.use_FL = (FrameLayout) convertView
                    .findViewById(R.id.use_FL);
             viewHolder.use_IV = (ImageView) convertView
                    .findViewById(R.id.use_IV);
             viewHolder.use_TV = (TextView) convertView
                     .findViewById(R.id.use_TV);
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
        DownloadTask task ;
        int state = 0;
        if("wallpaper".equals(type)){
            task = DownloadTaskMgr.getInstance().getDownloadTask(bean.id+2);
        }else{
            task = DownloadTaskMgr.getInstance().getDownloadTask(bean.id+1);
        }
        boolean update=false;
        if( task!=null) {
            state = task.gameDownloadState;
            try {
                if(bean.md5_val!=null){
                    update = state>5&&!bean.md5_val.equals( task.loadGame.getMd5());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(state==6){      /**已下载*/
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

        /**判断是否是本地主题列表 */
        if(isLocal){
            viewHolder.use_FL.setVisibility(View.VISIBLE);
            viewHolder.theme_prize.setVisibility(View.GONE);
        }else{
            viewHolder.use_FL.setVisibility(View.GONE);
            viewHolder.theme_prize.setVisibility(View.VISIBLE);
        }
        if(bean!=null && !TextUtils.isEmpty(bean.is_update) && bean.is_update.equals("1") &&update){   /**更新*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_update);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        if(bean!=null && !TextUtils.isEmpty(bean.name)){
            viewHolder.theme_title.setText(bean.name);
        }
        if(!isLocal && bean!=null && !TextUtils.isEmpty(bean.price)){
            if(bean.price.equals("0.0")){
                viewHolder.theme_prize.setText(context.getResources().getString(R.string.free));
            }else{
                viewHolder.theme_prize.setText(bean.price);
            }
        }
        String tag = (String) viewHolder.theme_logo.getTag();
        if("theme".equals(type) || "font".equals(type)){

            if(bean!=null && bean.ad_pictrue!=null&&(tag==null||!tag.equals(bean.ad_pictrue))){
                ImageLoader.getInstance().displayImage(bean.ad_pictrue, viewHolder.theme_logo,
                        UILimageUtil.getHomeThemeDpLoptions(),UILimageUtil.setTagHolder(viewHolder.theme_logo,bean.ad_pictrue));
            }
        }
        else if("wallpaper".equals(type)){
            viewHolder.theme_title.setVisibility(View.GONE);
            viewHolder.theme_prize.setVisibility(View.GONE);
            String p = UILimageUtil.getPicPath(context,bean.wallpaper_pic);
            if(bean!=null && bean.wallpaper_pic!=null&&(tag==null||!tag.equals(bean.wallpaper_pic))){
                ImageLoader.getInstance().displayImage(p, viewHolder.theme_logo,
                        UILimageUtil.getHomeThemeDpLoptions(),UILimageUtil.setTagHolder(viewHolder.theme_logo,bean.wallpaper_pic));
            }
        }
        return convertView;
    }

//    @Override
//    public void onStateChanged(DownloadInfo info) {
//        if (info != null && info.currentState == 4) {
//            notifyDataSetChanged();
//        }
//        if (info != null && info.currentState == 5) {
//            notifyDataSetChanged();
//        }
//    }

    public static class ViewHolder{
        ImageView theme_logo;
        TextView theme_title;
        TextView theme_prize;
        ImageView isNew;

        /**本地主题使用按钮布局*/
        FrameLayout use_FL;
        ImageView use_IV;
        TextView use_TV;
    }

    public void setType(String type){
        this.type = type;
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
