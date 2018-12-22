package com.prize.appcenter.ui.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.CategoryData.Categories;
import com.prize.app.net.datasource.base.CategoryData.CategoriesParent;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 **
 * 应用分类adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppCategoryAdapter extends GameListBaseAdapter {
	public ArrayList<CategoriesParent> parentitems = new ArrayList<CategoriesParent>();
	private Activity activity;

	public AppCategoryAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
	}

	/**
	 * 清空游戏排行集合
	 */
	public void clearAll() {
		parentitems.clear();
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<CategoriesParent> data) {
		if (data != null) {
			parentitems.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (parentitems != null && parentitems.size() > 0) {
			return parentitems.get(0).items.size();
		} else {
			return 0;
		}
	}

	@Override
	public Categories getItem(int position) {
		if (parentitems != null && parentitems.size() > 0) {
			return parentitems.get(0).items.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.activity_app_category_item, null);
			viewHolder = new ViewHolder();
			viewHolder.gameIcon = (ImageView) convertView
					.findViewById(R.id.game_iv);
			viewHolder.gameName = (TextView) convertView
					.findViewById(R.id.game_name_tv);
			viewHolder.view = convertView.findViewById(R.id.line);
			// viewHolder.layout = (FrameLayout) convertView
			// .findViewById(R.id.layout);
			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Categories gameBean = getItem(position);
		// mBitmapUtil.displayImg(viewHolder.gameIcon, gameBean.icon,
		// R.drawable.default_icon);
		ImageLoader.getInstance().displayImage(gameBean.icon,
				viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
		// BitmapMgr.loadBitmap(viewHolder.gameIcon, gameBean.icon,
		// R.drawable.default_icon);
		viewHolder.gameName.setText(gameBean.typeName);
		setLine(position, viewHolder.view, parentitems.get(0).items);
		return convertView;
	}

	static class ViewHolder {
		// 游戏图标
		ImageView gameIcon;
		// 游戏名字
		TextView gameName;
		View view;
		// FrameLayout layout;

	}

	private void setLine(int position, View view, ArrayList<Categories> items) {
		int i = 0;
		i = items.size() % 2;
		if (position + i + 1 > items.size()) {
			// 最后一行分割线隐藏
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}
}
