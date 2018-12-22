package com.android.launcher3.search;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.search.data.AsyncTaskCallback;

public class MyExpandableAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private List<GroupMemberBean> mGroup;
	private HashMap<Integer, List<GroupMemberBean>> mChild;
	private LayoutInflater mInflater;

	private HashMap<String, AsyncTaskCallback> mGroupsClass = new HashMap<String, AsyncTaskCallback>();

	public MyExpandableAdapter(Context mContext, List<GroupMemberBean> mGroup,
			HashMap<Integer, List<GroupMemberBean>> mChild,
			HashMap<String, AsyncTaskCallback> gourps) {
		this.mContext = mContext;
		this.mGroup = mGroup;
		this.mChild = mChild;
		mGroupsClass = gourps;
		this.mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<GroupMemberBean> mGroup,
			HashMap<Integer, List<GroupMemberBean>> mChild) {
		this.mGroup = mGroup;
		this.mChild = mChild;
		
		notifyDataSetChanged();
		
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mGroup.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		if (mChild.get(groupPosition) !=null) {
			return mChild.get(groupPosition).size();
		}
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mGroup.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupHolderView groupHolderView;

		if (convertView == null) {

			ExpandableListView ex = (ExpandableListView) parent;
			ex.expandGroup(groupPosition);
			groupHolderView = new GroupHolderView();
			convertView = (View) mInflater.inflate(R.layout.search_group,
					null);
			groupHolderView.groupTv = (TextView) convertView
					.findViewById(R.id.my_group_tv);
			convertView.setTag(groupHolderView);
		} else {
			groupHolderView = (GroupHolderView) convertView.getTag();
		}
		groupHolderView.groupTv.setText(mGroup.get(groupPosition).getName());
		return convertView;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		/*
		 * ChildHolderView childHolderView; if (convertView == null) {
		 * childHolderView = new ChildHolderView(); convertView = (View)
		 * mInflater.inflate(R.layout.activity_child, null);
		 * childHolderView.childIv = (ImageView) convertView
		 * .findViewById(R.id.my_child_iv); childHolderView.childTv = (TextView)
		 * convertView .findViewById(R.id.my_child_tv); childHolderView.childLl
		 * = (LinearLayout) convertView .findViewById(R.id.my_child_ll);
		 * convertView.setTag(childHolderView); } else { childHolderView =
		 * (ChildHolderView) convertView.getTag(); }
		 * childHolderView.childTv.setText(mChild.get(groupPosition)
		 * .get(childPosition).getName());
		 * 
		 * childHolderView.childIv.setImageDrawable(mChild.get(groupPosition)
		 * .get(childPosition).getIcon());
		 * childHolderView.childLl.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub Toast.makeText( mContext, "group:" +
		 * mGroup.get(groupPosition).getName() + "->child:" +
		 * mChild.get(groupPosition).get(childPosition) .getName(),
		 * Toast.LENGTH_SHORT).show(); } });
		 * 
		 * return convertView;
		 */
//		if (childPosition == 0) {
			convertView = null;
//		}
			
		return mGroupsClass.get(mGroup.get(groupPosition).getName())
				.getchildView(groupPosition, childPosition, isLastChild,
						convertView, parent, mGroup, mChild);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	class GroupHolderView {
		TextView groupTv;
	}

	class ChildHolderView {
		ImageView childIv;
		TextView childTv;
		LinearLayout childLl;
	}

}
