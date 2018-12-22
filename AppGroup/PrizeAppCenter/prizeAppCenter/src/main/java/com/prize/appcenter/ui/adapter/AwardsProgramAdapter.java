package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.AwardaProgramBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 有奖活动adapter
 * Created by longbaoixiu
 * Date:  2017/02/21
 */

public class AwardsProgramAdapter extends BaseAdapter {
    private Activity mContext;
    private List<AwardaProgramBean> items = new ArrayList<AwardaProgramBean>();

    public AwardsProgramAdapter(Activity context) {
        mContext = context;

    }


    /**
     * 这是数据
     */
    public void setData(List<AwardaProgramBean> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }


    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(List<AwardaProgramBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空列表
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 获得item
     */
    public List<AwardaProgramBean> gesItemsData() {
        if (items != null) {
            return items;
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AwardaProgramBean getItem(int position) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (mContext == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_awards_program, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.banner_Iv = (ImageView) convertView
                    .findViewById(R.id.banner_Iv);
            viewHolder.over_program_Iv = (ImageView) convertView
                    .findViewById(R.id.over_program_Iv);
            viewHolder.coming_program_Iv = (ImageView) convertView
                    .findViewById(R.id.coming_program_Iv);
            viewHolder.over_program_cover_Iv = (ImageView) convertView
                    .findViewById(R.id.over_program_cover_Iv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AwardaProgramBean orderBean = getItem(position);
        viewHolder.over_program_Iv.setVisibility(View.GONE);
        viewHolder.coming_program_Iv.setVisibility(View.GONE);
        viewHolder.over_program_cover_Iv.setVisibility(View.GONE);
        if (orderBean != null) {
            if (!TextUtils.isEmpty(orderBean.bannerUrl)) {
                ImageLoader.getInstance().displayImage(orderBean.bannerUrl,
                        viewHolder.banner_Iv, UILimageUtil.getADUILoptions(), null);
            }
            if(orderBean.istatus==2){
                viewHolder.coming_program_Iv.setVisibility(View.VISIBLE);
            }
            if(orderBean.istatus==3){
                viewHolder.over_program_Iv.setVisibility(View.VISIBLE);
                viewHolder.over_program_cover_Iv.setVisibility(View.VISIBLE);
            }

        }

        return convertView;
    }


    static class ViewHolder {
        /**
         * 有奖活动banner
         */
        ImageView banner_Iv;
        /**
         * 有奖活动即将开始标志icon
         */
        ImageView coming_program_Iv;
        /**
         * 有奖活动已结束icon
         */
        ImageView over_program_Iv;
        /**
         * 有奖活动已结束icon蒙层
         */
        ImageView over_program_cover_Iv;
    }

}
