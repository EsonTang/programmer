package com.prize.appcenter.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.net.datasource.base.Categories;
import com.prize.app.net.datasource.base.CategoryContent;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.activity.RootActivity;

import java.util.ArrayList;

/**
 **
 * 应用-分类adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SubTypeCategoryAdapter extends GameListBaseAdapter implements AdapterView.OnItemClickListener{

	private ArrayList<CategoryContent> typeItems = new ArrayList<CategoryContent>();
	private RootActivity activity;
	private Categories data;
	private boolean isGame=false;
	public SubTypeCategoryAdapter(RootActivity activity,boolean isGame) {
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
	 * @param data Categories
	 */
	public void setData(Categories data) {
		if (null != data&&data.tags!=null) {
			this.typeItems = data.tags;
			this.data = data;
		}
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<CategoryContent> data) {
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
	public CategoryContent getItem(int position) {

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
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_sub_category_layout, null);
			viewHolder = new ViewHolder();

			viewHolder.typeIcon = (ImageView) convertView
					.findViewById(R.id.item_app_category_iv);

			viewHolder.typeTitle = (TextView) convertView
					.findViewById(R.id.item_app_category_title_tv);

			viewHolder.mGridView = (GridView) convertView
					.findViewById(R.id.mGridView);

			viewHolder.lineView=convertView.findViewById(R.id.lineView);

			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		GridView gridView= (GridView) parent;
		gridView.setOnItemClickListener(this);
		CategoryContent categories = getItem(position);
		viewHolder.typeTitle.setText(categories.subTag);
		if ((position+1)%3==0){
			viewHolder.lineView.setVisibility(View.GONE);
		}

		return convertView;
	}

	static class ViewHolder {
		// 分类图标
		ImageView typeIcon;
		// 分类标题
		TextView typeTitle;
		GridView mGridView;

//		ImageButton imageButton;

		View lineView;
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Intent intent = new Intent(activity,
				CategoryAppGameListActivity.class);
		intent.putExtra(CategoryAppGameListActivity.parentID,data.id);
		intent.putExtra(CategoryAppGameListActivity.typeName, data.typeName);
		intent.putExtra(CategoryAppGameListActivity.SUBTYPEID, getItem(position).keyId);
		intent.putExtra(CategoryAppGameListActivity.tags,typeItems);
		intent.putExtra(CategoryAppGameListActivity.selectPos,position+1);
		intent.putExtra(CategoryAppGameListActivity.isGameKey, isGame);
		activity.startActivity(intent);
		if (isGame) {
			MTAUtil.onClickGameSubCate( getItem(position).subTag);
		}else{
			MTAUtil.onClickAppSubCate( getItem(position).subTag);
		}

	}
}
