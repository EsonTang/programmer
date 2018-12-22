
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：异步加载抽象类 
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.search.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;

public abstract class RSTResponse extends AsyncTaskCallback {

	public Context mContext;
	public int mIconDpi;
	Runnable mRunnable;
	HashMap<String, AsyncTaskCallback> mGroupClass;
	public List<GroupMemberBean> mGroupBeanList = null;
	// public HashMap<String, List<GroupMemberBean>> mGroupChildBeanList = null;

	public HashMap<Integer, List<GroupMemberBean>> mGroupChildBeanList = null;
	public String groupTitle = null;

	RequestDataTask mTask = new RequestDataTask(this);
	private MyExpandableAdapter mMyAdapter;
	public List<String> groups;

	public List<GroupMemberBean> mApps = new ArrayList<GroupMemberBean>();

	private LayoutInflater mInflater;

	public void excute() {
		mTask.execute("");
		
	}
	
	public void cancel() {
		mTask.cancel(true);
	}

	public GroupMemberBean mGroupMemberBean =null;
	public RSTResponse(Context mContext, List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,
			HashMap<String, AsyncTaskCallback> groupClass, List<String> groups) {
		super();
		this.mContext = mContext;
		this.mGroupBeanList = mGroupBeanList;
		this.mGroupChildBeanList = (HashMap<Integer, List<GroupMemberBean>>) mGroupChildList;
		this.groupTitle = groupTitle;
		mGroupMemberBean =this.fillData(groupTitle);
		this.mGroupBeanList.add(mGroupMemberBean);
		mMyAdapter = adpter;
		mGroupClass = groupClass;
		mGroupClass.put(groupTitle, this);
		this.groups = groups;
		groups.add(groupTitle);
		
		mRunnable = r;

		this.mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	class ChildHolderView {
		ImageView childIv;
		TextView childTv;
		LinearLayout childLl;
	}

	@Override
	public View getchildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent,
			final List<GroupMemberBean> group,
			final HashMap<Integer, List<GroupMemberBean>> child) {
		
		List<GroupMemberBean> items = child.get(groupPosition);
		final GroupMemberBean item = items.get(childPosition);
		ChildHolderView childHolderView;
		if (/* convertView == null */true) {
			childHolderView = new ChildHolderView();
			convertView = (View) mInflater.inflate(R.layout.search_child,
					null);
			childHolderView.childIv = (ImageView) convertView
					.findViewById(R.id.my_child_iv);
			childHolderView.childTv = (TextView) convertView
					.findViewById(R.id.my_child_tv);
			childHolderView.childLl = (LinearLayout) convertView
					.findViewById(R.id.my_child_ll);
			convertView.setTag(childHolderView);
		} else {
			childHolderView = (ChildHolderView) convertView.getTag();
		}
		childHolderView.childTv.setText(child.get(groupPosition)
				.get(childPosition).getName());

		childHolderView.childIv.setImageDrawable(child.get(groupPosition)
				.get(childPosition).getIcon());
		childHolderView.childLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				InputMethodManager imm = (InputMethodManager) mContext
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();
				if (isOpen) {
					View vs =((Activity) mContext).getCurrentFocus();
					if (vs!=null) {
						imm.hideSoftInputFromWindow(v
								.getWindowToken(),

						InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
		    	//isOpen若返回true，则表示输入法打开
				RSTResponse.this.onClick(item);
			}
		});

		return convertView;
	}

	@Override
	public List<GroupMemberBean> doBackground(String reqeust) {

		mGroupChildBeanList
				.put(mGroupBeanList.indexOf(mGroupMemberBean), mApps);
		synchronized (RSTResponse.class) {
			return run();
		}
	}

	public List<GroupMemberBean> run() {
		return null;

	}

	@Override
	public void setAsyncTask(RequestDataTask task) {
		mTask = task;
	}

	@Override
	public void onProgressUpdate(GroupMemberBean... values) {
		/*List<GroupMemberBean> groupItems  = mGroupChildBeanList.get(mGroupBeanList.indexOf(mGroupMemberBean));
		if (groupItems !=null) {
			groupItems.add(values[0]);
		}*/
//		synchronized (RSTResponse.class) {
			if (!mTask.isCancelled()) {

			values[0].groupTitle = groupTitle;
				int groupPosition =mGroupBeanList.indexOf(mGroupMemberBean);
				mGroupChildBeanList.get(groupPosition).add(values[0]);
			}else {

			}
//		}
//		mMyAdapter.updateListView(mGroupBeanList, mGroupChildBeanList);
//		mMyAdapter.onGroupExpanded(groupPosition);
	}

	@Override
	public void onPostExecute(List<GroupMemberBean> result) {
		if (mRunnable != null) {
			mRunnable.run();
		}
	}

	@Override
	public void setCallback(Runnable callback) {
		mRunnable = callback;
	}

}
