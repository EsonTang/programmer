/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.onlinemusibean.CategoryBean;

/**
 * 电台类别
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioCategoryAdapter extends BaseAdapter {
	private ArrayList<CategoryBean> items = new ArrayList<CategoryBean>();
	private int selectedPosition = 0;
	private Context context;// CategoryBean

	public RadioCategoryAdapter(Context activity) {
		this.context = activity;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
	}

	@Override
	public CategoryBean getItem(int position) {
		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	public void setSelectPostion(int position) {
		this.selectedPosition = position;
		notifyDataSetChanged();
	}

	public void setData(ArrayList<CategoryBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<CategoryBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_category_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.categoryName_Tv = (TextView) convertView
					.findViewById(R.id.categoryName_Tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final CategoryBean bean = getItem(position);
		if("推荐".equals(bean.category_name)){
			viewHolder.categoryName_Tv.setText(R.string.hot_topic);
		}else{
			viewHolder.categoryName_Tv.setText(bean.category_name);
		}
		if (selectedPosition == position) {
			viewHolder.categoryName_Tv.setEnabled(false);
		} else {
			viewHolder.categoryName_Tv.setEnabled(true);
		}

		return convertView;
	}

	static class ViewHolder {
		// 榜单排行名称
		TextView categoryName_Tv;

	}
}
