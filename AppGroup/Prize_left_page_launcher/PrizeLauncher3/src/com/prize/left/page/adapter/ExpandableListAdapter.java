package com.prize.left.page.adapter;

import java.util.HashMap;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.launcher3.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private String[] groupList;
	private String[][] childList;
	Context mContext;
	private HashMap<String, Boolean> statusHashMap;
	private int currentGroupId = -1;
	private int selectGroupId = -1;
	private int currentChildId = -1;
	private int[][] selectId = { {}, {} };

	public void setCurrentChildId(int currentChildId) {
		this.currentChildId = currentChildId;
	}

	public void setSelectGroupId(int selectGroupId) {
		this.selectGroupId = selectGroupId;
	}

	ExpandableListAdapter() {
	}

	public void putValue2Map(String str, boolean paramBoolean) {
		statusHashMap.put(str, paramBoolean);
		notifyDataSetChanged();
	}

	public ExpandableListAdapter(String[] groupList, String[][] childList,
			Context mContext) {
		super();
		this.groupList = groupList;
		this.childList = childList;
		this.mContext = mContext;
		statusHashMap = new HashMap<String, Boolean>();
		for (int i = 0; i < childList.length; i++) {// 初始时,让所有的子选项均未被选中
			for (int a = 0; a < childList[i].length; a++) {
				statusHashMap.put(childList[i][a], false);
			}
		}
	}

	@Override
	public int getGroupCount() {

		return groupList.length;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		return childList[groupPosition].length;
	}

	@Override
	public Object getGroup(int groupPosition) {

		return groupList[groupPosition];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return childList[groupPosition][childPosition];
	}

	@Override
	public long getGroupId(int groupPosition) {

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
		String str = groupList[groupPosition];
		ViewHolder localViewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.layout_parent, null);
			localViewHolder = new ViewHolder();
			localViewHolder.mTextTitle = ((TextView) convertView
					.findViewById(R.id.parent_textview));
			convertView.setTag(localViewHolder);
		} else {
			localViewHolder = (ViewHolder) convertView.getTag();

		}
		if (isExpanded) {
			localViewHolder.mTextTitle.setBackgroundColor(this.mContext
					.getResources().getColor(R.color.app_title));
			currentGroupId = groupPosition;
		} else {
			localViewHolder.mTextTitle.setBackgroundColor(color.transparent);
		}
		localViewHolder.mTextTitle.setText(str);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		String str = childList[groupPosition][childPosition];
		final ViewHolderRadioButton localViewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.layout_children, null);
			localViewHolder = new ViewHolderRadioButton();
			localViewHolder.radioButton = (CheckBox) convertView
					.findViewById(R.id.second_textview);
			convertView.setTag(localViewHolder);
		} else {
			localViewHolder = (ViewHolderRadioButton) convertView.getTag();

		}
		localViewHolder.radioButton.setText(str);

		// String param = childList[currentGroupId][childPosition];
		// if (!TextUtils.isEmpty(param)) {
		//
		// } else {
		//
		// }

		Boolean nowStatus = statusHashMap
				.get(childList[groupPosition][childPosition]);// 当前状态
		localViewHolder.radioButton.setChecked(nowStatus);
		if (selectGroupId == groupPosition && currentChildId == childPosition) {
			localViewHolder.radioButton.setTextColor(Color.WHITE);
			localViewHolder.radioButton.setBackgroundColor(Color
					.parseColor("#3AC2CF"));
		} else {
			localViewHolder.radioButton.setTextColor(mContext.getResources()
					.getColor(R.color.text_color_737373));
			localViewHolder.radioButton.setBackgroundColor(this.mContext
					.getResources().getColor(R.color.app_title));
		}
		// localViewHolder.radioButton
		// .setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// if (isChecked) {
		// localViewHolder.radioButton
		// .setBackgroundResource(color.holo_blue_light);
		// } else {
		// localViewHolder.radioButton
		// .setBackgroundResource(color.transparent);
		// }
		//
		// }
		// });

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		// TODO Auto-generated method stub
		return true;
	}

	// class ItemBaseAdapter extends BaseAdapter {
	// ItemBaseAdapter() {
	// }
	//
	// @Override
	// public int getCount() {
	// return mPageList.size();
	// }
	//
	// @Override
	// public Object getItem(int paramInt) {
	// return mPageList.get(paramInt);
	// }
	//
	// @Override
	// public long getItemId(int paramInt) {
	// return paramInt;
	// }
	//
	// @Override
	// public View getView(int paramInt, View paramView,
	// ViewGroup paramViewGroup) {
	// ViewHolder localViewHolder;
	// if (paramView == null) {
	// // paramView = ManualItemActivity.this.mInflater.inflate(
	// // 2130903045, null);
	// // localViewHolder = new ManualItemActivity.ViewHolder();
	// // localViewHolder.mTextTitle = ((TextView) paramView
	// // .findViewById(2131230737));
	// // paramView.setTag(localViewHolder);
	// }
	// while (true) {
	// // String str =
	// // Utils.getDisplayName(getItem(paramInt).toString());
	// // localViewHolder = (ManualItemActivity.ViewHolder) paramView
	// // .getTag();
	// // localViewHolder.mTextTitle.setText(str);
	// return paramView;
	// }
	// }
	// }

	private class ViewHolder {
		TextView mTextTitle;

		private ViewHolder() {
		}
	}

	private class ViewHolderRadioButton {
		CheckBox radioButton;

		private ViewHolderRadioButton() {
		}
	}
}
