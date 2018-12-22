package com.prize.appcenter.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppGameCategoryData.Data;
import com.prize.app.net.datasource.base.Categories;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;

/**
 **
 * 应用-分类adapter
 * 
 * @author 
 * @version V1.0
 */
public class AppTypeCategoryAdapter extends GameListBaseAdapter {

	private static final String TAG = "AppTypeCategoryAdapter";
	public ArrayList<Categories> typeItems = new ArrayList<Categories>();
	private RootActivity activity;
    private boolean isGame=false;
	public AppTypeCategoryAdapter(RootActivity activity,boolean isGame) {
		super(activity);
		this.activity = activity;
		this.isGame=isGame;
	}

	/**
	 * 清空 应用、游戏 分类集合
	 */
	public void clearAll() {
		typeItems.clear();
		notifyDataSetChanged();
	}

	/**
	 * 设置游戏、应用 分类集合
	 * 
	 * @param data ArrayList<Data>
	 */
	public void setData(ArrayList<Categories> data) {
		if (null != data) {
			this.typeItems = data;
		}
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<Data> data) {
		if (data != null) {
			data.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (typeItems != null && typeItems.size() > 0) {
			return typeItems.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {

		if (typeItems != null && typeItems.size() > 0) {
			return typeItems.get(position);
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
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		SubTypeCategoryAdapter adapter=null;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_app_category_pager, null);
			viewHolder = new ViewHolder();

			viewHolder.typeIcon = (ImageView) convertView
					.findViewById(R.id.item_app_category_iv);
			viewHolder.typeTitle = (TextView) convertView
					.findViewById(R.id.item_app_category_title_tv);
			viewHolder.linearLayout = (LinearLayout) convertView
					.findViewById(R.id.ll);

			viewHolder.mGridView = (GridView) convertView
					.findViewById(R.id.mGridView);
			adapter=new SubTypeCategoryAdapter(activity,isGame);
			convertView.setTag(viewHolder);
			convertView.setTag(R.id.tag_id,adapter);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			adapter= (SubTypeCategoryAdapter) convertView.getTag(R.id.tag_id);
		}
		final Categories categories = (Categories) getItem(position);

		ImageLoader.getInstance().displayImage(categories.icon,
				viewHolder.typeIcon, UILimageUtil.getUILoptions(), null);
		viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String parentID = categories.id;
				String typeName = categories.typeName;
				ArrayList<CategoryContent> tags = categories.tags;
				if (typeName == null) {
					return;
				}

				Intent intent = new Intent(activity,
						CategoryAppGameListActivity.class);

				intent.putExtra(CategoryAppGameListActivity.parentID, parentID);
				intent.putExtra(CategoryAppGameListActivity.typeName,typeName);
				intent.putExtra(CategoryAppGameListActivity.SUBTYPEID, parentID);
				intent.putExtra(CategoryAppGameListActivity.tags, tags);
				intent.putExtra(CategoryAppGameListActivity.isGameKey, isGame);
				activity.startActivity(intent);
				if (isGame) {
					MTAUtil.onClickGameCategoryTAB(typeName);
				} else {
					MTAUtil.onClickAppCategoryTAB(typeName);
				}
			}
		});
		viewHolder.typeTitle.setText(categories.typeName);
		adapter.setData(categories);
		viewHolder.mGridView.setAdapter(adapter);
		return convertView;
	}

	static class ViewHolder {
		// 分类图标
		ImageView typeIcon;
		// 分类标题
		TextView typeTitle;
		GridView mGridView;
		LinearLayout linearLayout;

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
