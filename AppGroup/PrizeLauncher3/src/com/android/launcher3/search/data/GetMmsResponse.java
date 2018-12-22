package com.android.launcher3.search.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;
import com.android.launcher3.search.data.recent.Utils.RexseeSMS;
import com.android.launcher3.search.data.recent.Utils.SMSBean;

public class GetMmsResponse extends RSTResponse {

	private LayoutInflater mInflater;
	private Date mDate;
	private SimpleDateFormat sdf;

	public GetMmsResponse(Context mContext,
			List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,
			HashMap<String, AsyncTaskCallback> groupClass, List<String> groups) {
		super(mContext, mGroupBeanList, mGroupChildList, groupTitle, adpter, r,
				groupClass, groups);

		this.mDate = new Date();
		this.sdf = new SimpleDateFormat("MM/dd HH:mm");
		this.mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO Auto-generated constructor stub
	}

	public static final String CONTENT_URI_SMS = "content://sms/"; // 短信

	public List<GroupMemberBean> getThreadsNum() {

		Cursor cursor = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		List<GroupMemberBean> list = new ArrayList<GroupMemberBean>();

		RexseeSMS rsms = new RexseeSMS(mContext);

		List<GroupMemberBean> list_mmt = getThreadsNum(rsms.getThreads(0));

		return list;
	}

	@Override
	public List<GroupMemberBean> run() {

		return getThreadsNum();
	}

	public List<GroupMemberBean> getThreadsNum(List<SMSBean> ll) {
		Cursor cursor = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		List<GroupMemberBean> list = new ArrayList<GroupMemberBean>();
		for (SMSBean mmt : ll) {
			cursor = contentResolver.query(Uri.parse(CONTENT_URI_SMS),
					RexseeSMS.SMS_COLUMNS, "thread_id = " + mmt.getThread_id(),
					null, null);
			if (cursor == null || cursor.getCount() == 0)
				return list;
			cursor.moveToFirst();
			String address = cursor.getString(MmsContact.ADDRESS);
			if (address == null) {
				address = "";
			}
			long data = cursor.getLong(MmsContact.DATA);

			String Read = cursor.getString(MmsContact.READ);
			String content = cursor.getString(MmsContact.CONTENT);
			if (content == null) {
				content = "";
			}

			GroupMemberBean childBean = null;
			if (!TextUtils.isEmpty(address)) {
				childBean = this.fillData(address);
			} else if (!TextUtils.isEmpty(content)) {
				childBean = this.fillData(content);
			}

			childBean.setDate(data);
			childBean.setMsg_snippet(content);
			childBean.setRead(Read);
			childBean.setAddress(address);

			mTask.doPublishProgress(childBean);
			// mApps.add(childBean);
			list.add(childBean);
		}
		// mGroupChildBeanList.add(mApps);

		return list;
	}

	@Override
	public void onProgressUpdate(GroupMemberBean... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	@Override
	public View getchildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent,
			final List<GroupMemberBean> group,
			final HashMap<Integer, List<GroupMemberBean>> child) {
		// TODO Auto-generated method stub
		/*
		 * return super.getchildView(groupPosition, childPosition, isLastChild,
		 * convertView, parent, group, child);
		 */

		final GroupMemberBean item = child.get(groupPosition).get(childPosition);
		ChildHolderView childHolderView;
		if (true) {
			childHolderView = new ChildHolderView();
			convertView = (View) mInflater.inflate(R.layout.search_mms_item,
					null);
			childHolderView.icon = (ImageView) convertView
					.findViewById(R.id.icon);
			childHolderView.number = (TextView) convertView
					.findViewById(R.id.number);

			childHolderView.content = (TextView) convertView
					.findViewById(R.id.content);
			childHolderView.item = (LinearLayout) convertView
					.findViewById(R.id.item);
			childHolderView.date = (TextView) convertView
					.findViewById(R.id.date);
			convertView.setTag(childHolderView);
		} else {
			childHolderView = (ChildHolderView) convertView.getTag();
		}
		childHolderView.icon.setImageDrawable(item.getIcon());
		childHolderView.number.setText(item.getName());
		childHolderView.content.setText(item.getMsg_snippet());
		// childHolderView.date.setText(String.valueOf(item.getDate().toString()));
		this.mDate.setTime(item.getDate() != null ? item.getDate() : 0);
		childHolderView.date.setText(this.sdf.format(mDate));
		childHolderView.item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GetMmsResponse.this.onClick(item);

			}
		});

		return convertView;
	}

	class ChildHolderView {
		ImageView icon;
		TextView number;
		TextView content;
		TextView date;
		LinearLayout item;
	}

	@Override
	public void onClick(GroupMemberBean item) {
		try {

			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
					"sms", item.getAddress(), null));
			mContext.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
