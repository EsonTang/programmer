package com.pr.scuritycenter.optimize;

import java.util.List;

import com.pr.scuritycenter.R;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressLint("InflateParams")
public class OptimizingExpandableListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater mLayoutInflater;
	private List<OptimizingBean> mParentListOptimizingBeans;
	private List<OptimizingBean> mChildListOptimizingBeans;
	
	public OptimizingExpandableListAdapter(LayoutInflater mLayoutInflater,
			List<OptimizingBean> mParentListOptimizingBeans,
			List<OptimizingBean> mChildListOptimizingBeans) {
		super();
		this.mLayoutInflater = mLayoutInflater;
		this.mParentListOptimizingBeans = mParentListOptimizingBeans;
		this.mChildListOptimizingBeans = mChildListOptimizingBeans;
	}

	@Override
	public int getGroupCount() {
		return mParentListOptimizingBeans == null ? 0 : mParentListOptimizingBeans.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildListOptimizingBeans == null ? 0 : mChildListOptimizingBeans.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mParentListOptimizingBeans == null ? null : mParentListOptimizingBeans.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildListOptimizingBeans == null ? null : mChildListOptimizingBeans.get(childPosition);
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
		OptimizingBean optimizingBean = (OptimizingBean) getGroup(groupPosition);
		
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.optimizing_elist_parent_item, null);
		}
		ImageView iv_parent_pic = (ImageView) convertView.findViewById(R.id.iv_parent_pic);
		ImageView iv_parent_bg = (ImageView) convertView.findViewById(R.id.iv_parent_bg);
		TextView tv_parent_name = (TextView) convertView.findViewById(R.id.tv_parent_name);
		TextView tv_parent_status = (TextView) convertView.findViewById(R.id.tv_parent_status);
		
		iv_parent_pic.setBackgroundResource(optimizingBean.getPic());
		
		Animation animation = AnimationUtils.loadAnimation(mLayoutInflater.getContext(), R.anim.animation_item_status);
		iv_parent_bg.startAnimation(animation);
		tv_parent_name.setText(optimizingBean.getName());
		String status = "";
		switch (optimizingBean.getStatus()) {
		case OptimizingBean.STATUS_OPTIMIZATION_BEFORE:
			status = "未优化";
			iv_parent_bg.setVisibility(View.VISIBLE);
			break;
		case OptimizingBean.STATUS_OPTIMIZATION:
			status = "正在优化";
			iv_parent_bg.setVisibility(View.VISIBLE);
			break;
		case OptimizingBean.STATUS_OPTIMIZATION_AFTER:
			status = "已完成";
			iv_parent_bg.clearAnimation();
			iv_parent_bg.setVisibility(View.INVISIBLE);
			break;

		default:
			break;
		}
		tv_parent_status.setText(status);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.optimizing_elist_child_item, null);
		}
		
		// ImageView iv_child_status = (ImageView) convertView.findViewById(R.id.iv_child_status);
		TextView tv_child_name = (TextView) convertView.findViewById(R.id.tv_child_name);
		tv_child_name.setText(((OptimizingBean) getChild(groupPosition, childPosition)).getName());
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
