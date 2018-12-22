package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
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

import java.io.File;
import java.util.ArrayList;


/**
 * 主题分类ID的adapter
 * Created by pengy on 2016/9/6.
 */
public class TopicThemeListAdapter extends BaseAdapter{

    private ArrayList<ThemeItemBean> items = new ArrayList<ThemeItemBean>();

    public Activity context;
    private GridView mGrid;
    private String path;
    private File file;
    private UIDownLoadListener listener = null;
    /** 当前页是否处于显示状态 */
    private boolean isActivity = true; // 默认true

    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    public TopicThemeListAdapter(Activity activity) {
        context = activity;
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(theme_Id + ""+1);
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
        //Bug 25712  25481
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
                    R.layout.topic_theme_list_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.theme_logo = (CornerImageView) convertView
                    .findViewById(R.id.theme_logo);
            viewHolder.theme_title = (TextView) convertView
                    .findViewById(R.id.theme_title);
            viewHolder.theme_prize = (TextView) convertView
                    .findViewById(R.id.theme_prize);
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

        DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(bean.id+1);


        boolean update=false;
        if( task!=null) {
            state = task.gameDownloadState;
            try {
                update = state>5&&!bean.md5_val.equals( task.loadGame.getMd5());
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

//        path = FileUtils.getDownloadPath(bean.id, 1);
//        file = new File(path);
//        md5Check = MD5Util.Md5Check(path, bean.md5_val);
        if (state == 6) {      /**已下载*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_download);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        } else if (state == 7) {    /**正在使用*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_used);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        } else if (bean != null && !TextUtils.isEmpty(bean.is_latest) && bean.is_latest.equals("1")) {  /**新品*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_new);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        if (bean != null && !TextUtils.isEmpty(bean.is_update) && bean.is_update.equals("1") &&update) {   /**更新*/
            viewHolder.isNew.setBackgroundResource(R.drawable.icon_is_update);
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }
        if (bean != null && !TextUtils.isEmpty(bean.name)) {
            viewHolder.theme_title.setText(bean.name);
        }
        if (bean != null && !TextUtils.isEmpty(bean.price)) {
            if (bean.price.equals("0.0")) {
                viewHolder.theme_prize.setText(context.getResources().getString(R.string.free));
            } else {
                viewHolder.theme_prize.setText(String.format("¥%1$s",bean.price));
            }
        }
        String tag = (String) viewHolder.theme_logo.getTag();
        if (bean != null && bean.ad_pictrue != null && (tag == null || !tag.equals(bean.ad_pictrue))) {
            ImageLoader.getInstance().displayImage(bean.ad_pictrue, viewHolder.theme_logo,
                    UILimageUtil.getHomeThemeDpLoptions(), UILimageUtil.setTagHolder(viewHolder.theme_logo, bean.ad_pictrue));
        }
        return convertView;
    }

    public void setParent(GridView v) {
        mGrid = v;
    }

    public static class ViewHolder {
        ImageView theme_logo;
        TextView theme_title;
        TextView theme_prize;
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
