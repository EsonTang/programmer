package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadHistoryBean.DataBean.ItemBean;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/11/11.
 */
public class DownloadHistoryListViewAdapter extends BaseAdapter {

    private List<ItemBean> items = new ArrayList<>();
    private ViewHolder holder;
    ItemBean bean;
    private Context mCtx;


    public DownloadHistoryListViewAdapter(Context mCtx) {
        this.mCtx = mCtx;
    }

    public void setData(List<ItemBean> data) {
        if (data != null) items = data;
        notifyDataSetChanged();
    }

    public void addData(List<ItemBean> datas) {
        if (datas != null) {
            items.addAll(datas);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : null;
    }

    @Override
    public Object getItem(int position) {
        return items == null ? null : items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(MainApplication.curContext).inflate(R.layout.item_download_history_list, null);
            holder = new ViewHolder();
            holder.image_iv = (CornerImageView) convertView.findViewById(R.id.image_iv);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_total = (TextView) convertView.findViewById(R.id.tv_total);
            holder.tv_model = (TextView) convertView.findViewById(R.id.tv_model);
            convertView.setTag(holder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        bean = items.get(position);
        holder.tv_name.setText(bean.getName());
        holder.tv_total.setText(bean.getSize());
        holder.tv_model.setText(bean.getModel());
        if (TextUtils.isEmpty(bean.getModel())) {
            holder.tv_model.setText(R.string.defaut_model);
        }
        switch (bean.getType()) {
            case 0:
                holder.image_iv.setImageResource(R.drawable.mine_theme);
                break;
            case 2:
                holder.image_iv.setImageResource(R.drawable.mine_wallpaper);
                break;
            case 1:
                holder.image_iv.setImageResource(R.drawable.mine_font);
                break;
        }
        /**
         * ，0待审核，1上线，2下线
         */
        switch (bean.getStatus()){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
        return convertView;
    }

    private class ViewHolder {
        CornerImageView image_iv;
        TextView tv_name;
        TextView tv_total;
        TextView tv_model;
    }
}
