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
import com.prize.prizethemecenter.bean.SearchSimilartyData.TagBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * 主题分类ID的adapter
 * Created by bxh on 2016/11/8.
 */
public class SimilartyThemeListAdapter extends BaseAdapter {

    private List<TagBean> items = new ArrayList<>();

    public Activity context;

    public boolean isLocal;
    /***
     * 1.theme  2.wallpaper
     */
    public String type;

    /***
     * @param activity
     * @param isLocal  是否为本地主题列表
     */
    public SimilartyThemeListAdapter(Activity activity, boolean isLocal) {
        context = activity;
        this.isLocal = isLocal;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public TagBean getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<TagBean> data) {
        if (data != null) {
            if (items!=null){
                items.clear();
            }
            items = data;
        }
        notifyDataSetChanged();
    }

    public void addData(ArrayList<TagBean> data) {
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
            viewHolder.use_FL = (FrameLayout) convertView
                    .findViewById(R.id.use_FL);
            viewHolder.use_IV = (ImageView) convertView
                    .findViewById(R.id.use_IV);
            viewHolder.use_TV = (TextView) convertView
                    .findViewById(R.id.use_TV);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TagBean bean = getItem(position);

        /**判断是否是本地主题列表 */
        if (isLocal) {
            viewHolder.use_FL.setVisibility(View.VISIBLE);
            viewHolder.theme_prize.setVisibility(View.GONE);
        } else {
            viewHolder.use_FL.setVisibility(View.GONE);
            viewHolder.theme_prize.setVisibility(View.VISIBLE);
        }

        if (bean != null && !TextUtils.isEmpty(bean.getName())) {
            viewHolder.theme_title.setText(bean.getName());
        }
        if (!isLocal && bean != null && !TextUtils.isEmpty(bean.getPrice())) {
            viewHolder.theme_prize.setText(bean.getPrice());
            if(bean.getPrice().equals("0.0")){
                viewHolder.theme_prize.setText(R.string.free);
            }
        }


        if (bean != null && bean.getAd_pictrue() != null) {
            ImageLoader.getInstance().displayImage(bean.getAd_pictrue(), viewHolder.theme_logo,
                    UILimageUtil.getHomeThemeDpLoptions(), null);
        }

        return convertView;
    }
    public static class ViewHolder {
        ImageView theme_logo; TextView theme_title;
        TextView theme_prize;

        /**
         * 本地主题使用按钮布局
         */
        FrameLayout use_FL;
        ImageView use_IV;
        TextView use_TV;
    }

    public void setType(String type) {
        this.type = type;
    }
}
