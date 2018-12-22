package com.prize.appcenter.ui.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.CategoryData.Categories;
import com.prize.app.net.datasource.base.CategoryData.CategoriesParent;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppListActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.ScrollGridView;

/**
 **
 * 应用分类adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GameCategoryAdapter extends BaseExpandableListAdapter {
	public ArrayList<CategoriesParent> items = new ArrayList<CategoriesParent>();
	private Activity activity;

	public GameCategoryAdapter(RootActivity activity) {
		this.activity = activity;
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<CategoriesParent> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();

	}

	@Override
	public int getGroupCount() {

		return items.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;// /
		// 返回值必须为1，否则会重复数据
		// return items.get(groupPosition).categories.size();
	}

	@Override
	public Object getGroup(int groupPosition) {

		return items.get(groupPosition).name;
	}

	@Override
	public ArrayList<Categories> getChild(int groupPosition, int childPosition) {

		return items.get(groupPosition).items;
	}

	@Override
	public long getGroupId(int groupPosition) {

		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {

		return childPosition;
	}

	@Override
	public boolean hasStableIds() {

		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (null == convertView) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_group_head, null);
		}

		TextView textView = (TextView) convertView.findViewById(R.id.date_tv);
		String itemValue = (String) getGroup(groupPosition);
		textView.setText(itemValue);
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		GridViewHolder viewHolder = null;
		GameSubCategoryAdapter subAdapter = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.activity_app_category, null);
			viewHolder = new GridViewHolder();
			viewHolder.gridView = (ScrollGridView) convertView
					.findViewById(R.id.grideView);
			if (subAdapter == null) {
				subAdapter = new GameSubCategoryAdapter();
				convertView.setTag(R.id.tag_id, subAdapter);
			}
			convertView.setTag(viewHolder);
		}
		viewHolder = (GridViewHolder) convertView.getTag();
		subAdapter = (GameSubCategoryAdapter) convertView.getTag(R.id.tag_id);
		viewHolder.gridView.setAdapter(subAdapter);
		subAdapter.setData(getChild(groupPosition, childPosition));
		viewHolder.gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Categories bean = getChild(groupPosition, childPosition).get(
						position);
				if (bean == null) {
					return;
				}
				Intent intent = new Intent(activity,
						CategoryAppListActivity.class);
				intent.putExtra("title", bean.typeName);
				intent.putExtra("id", bean.id + "");
				activity.startActivity(intent);
				activity.overridePendingTransition(R.anim.fade_in,
						R.anim.fade_out);

			}
		});

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		return true;
	}

	class GameSubCategoryAdapter extends BaseAdapter {
		public ArrayList<Categories> subItems = new ArrayList<Categories>();

		public GameSubCategoryAdapter() {

		}

		/**
		 * 设置游戏排行集合
		 * 
		 * @param data
		 */
		public void setData(ArrayList<Categories> data) {
			if (null != data) {
				this.subItems = data;
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return subItems.size();
		}

		@Override
		public Categories getItem(int position) {
			return subItems.get(position);
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
						R.layout.activity_game_category_item, null);
				viewHolder = new ViewHolder();
				viewHolder.gameIcon = (ImageView) convertView
						.findViewById(R.id.game_iv);
				viewHolder.gameName = (TextView) convertView
						.findViewById(R.id.game_name_tv);
				viewHolder.view = convertView.findViewById(R.id.line);
				// viewHolder.layout = (FrameLayout) convertView
				// .findViewById(R.id.layout);
				convertView.setTag(viewHolder);
				convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Categories gameBean = subItems.get(position);
			ImageLoader.getInstance().displayImage(gameBean.icon,
					viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
			viewHolder.gameName.setText(gameBean.typeName);
			setLine(position, viewHolder.view, subItems.size());
			return convertView;
		}

		private void setLine(int position, View view, int size) {
			JLog.i("long", "size=" + size + "---position=" + position);

			int i = 0;
			i = size % 2;
			if (position + i + 1 > size) {
				// 最后一行分割线隐藏
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
		}
	}

	static class ViewHolder {
		// 游戏图标
		ImageView gameIcon;
		// 游戏名字
		TextView gameName;
		View view;
		// FrameLayout layout;

	}

	static class GridViewHolder {
		// 游戏图标
		ScrollGridView gridView;

	}

}
