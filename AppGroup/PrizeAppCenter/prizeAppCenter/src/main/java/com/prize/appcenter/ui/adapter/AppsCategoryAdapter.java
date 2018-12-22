package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.Categories;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;

/**
 * 
 ** 
 * 热门分类的adapter
 * 
 * @author nieligang
 * @version V1.0
 */
public class AppsCategoryAdapter extends BaseAdapter {

	private ArrayList<Categories> datas;
	private Context ctx;



	public AppsCategoryAdapter(ArrayList<Categories> items, Context c) {
		datas = items;
		ctx = c;
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		return datas.size();
	}

	@Override
	public Categories getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.apps_category_item, null);
			viewHolder.catImage = (ImageView) convertView.findViewById(R.id.apps_iv);
			viewHolder.title = (TextView) convertView.findViewById(R.id.apps_type_tv);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		ImageLoader.getInstance().displayImage(getItem(position).icon,
				viewHolder.catImage, UILimageUtil.getUILoptions(), null);
		viewHolder.title.setText(getItem(position).typeName);

		return convertView;

	}

	private class ViewHolder {
		public ImageView catImage;
		public TextView title;
	}
}
