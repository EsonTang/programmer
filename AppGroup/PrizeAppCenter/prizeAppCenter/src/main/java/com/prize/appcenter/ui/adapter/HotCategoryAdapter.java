package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HotCategoryBean;
import com.prize.app.util.DisplayUtil;
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
public class HotCategoryAdapter extends BaseAdapter {

	private ArrayList<HotCategoryBean> datas;
	private Context ctx;



	public HotCategoryAdapter(ArrayList<HotCategoryBean> items, Context c) {
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
	public HotCategoryBean getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		HotCategoryBean bean = getItem(position);

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.hot_category_item, null);
			viewHolder.catImage = (ImageView) convertView.findViewById(R.id.cat_iv);
			viewHolder.title = (TextView) convertView.findViewById(R.id.cat_title_tv);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		ImageLoader.getInstance().displayImage(bean.iconUrl,
				viewHolder.catImage, UILimageUtil.getUILoptions(), null);
		viewHolder.title.setText(bean.title);
		viewHolder.title.setTextColor(Color.parseColor(("#"+bean.color).trim()));

		if(bean.hotStatus == 1) {
			Drawable drawable = ctx.getResources().getDrawable(
					R.drawable.hot_cat_first);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight()); // 设置边界
			viewHolder.title.setCompoundDrawablePadding(DisplayUtil.dip2px(ctx, 9));
			viewHolder.title.setCompoundDrawables(null,
					null, drawable, null);// 画在右边
		}

		return convertView;

	}

	private class ViewHolder {
		public ImageView catImage;
		public TextView title;
	}
}
