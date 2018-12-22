package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.SingleThemeItemBean.ItemsBean.TypesBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/21.
 */
public class SingleThemeGridAdapter extends BaseAdapter {

	private List<TypesBean> items = new ArrayList<TypesBean>();
	private Context context;
	private ViewHolder holder;

	public SingleThemeGridAdapter(Context context) {
		this.context = context;
	}

	public void setData(List<TypesBean> data) {
		if (data != null) items = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items == null ? null: items.size();
	}

	@Override
	public Object getItem(int position) {
		return items == null ? null: items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(MainApplication.curContext).inflate(R.layout.item_grid_layout, null);
			holder = new ViewHolder();
			holder.type = (TextView) convertView.findViewById(R.id.theme_type);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.type.setText(items.get(position).getName());
		holder.id = items.get(position).getId();
		return convertView;
	}


	private class ViewHolder{
		TextView type;
		String id;
	}
}
