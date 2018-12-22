package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.SearchOriginData.OriginData;

import java.util.ArrayList;


/**
 * 主题分类的adapter
 * Created by pengy on 2016/9/6.
 */
public class OriginAdapter extends BaseAdapter{


    private ArrayList<OriginData> items;
    private Context context;

    public OriginAdapter(Context activity) {
        this.context = activity;
    }

    public void setData(ArrayList<OriginData> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public OriginData getItem(int position) {
        if (items == null)
            return null;
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
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.search_origin_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.hotwords_Tv = (TextView) convertView
                    .findViewById(R.id.hotwords_Tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final OriginData bean = getItem(position);
        viewHolder.hotwords_Tv.setText(bean.name);

        return convertView;
    }

    static class ViewHolder {
        TextView hotwords_Tv;
    }

    public void addData(ArrayList<OriginData> data) {
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
}
