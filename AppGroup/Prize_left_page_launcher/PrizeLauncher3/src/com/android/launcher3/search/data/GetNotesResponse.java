package com.android.launcher3.search.data;

import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.search.GroupMemberBean;
import com.android.launcher3.search.MyExpandableAdapter;
import com.android.launcher3.search.data.GetMmsResponse.ChildHolderView;

public class GetNotesResponse extends RSTResponse {

	public static final String COLUMN_NAME_NOTE_NAME = "title";
	public static final String COLUMN_NAME_NOTE_CONTENT = "content";
	public static final String COLUMN_NAME_NOTE_DATE = "date";
	public static final String COLUMN_NAME_ID = "_id";

	public GetNotesResponse(Context mContext,
			List<GroupMemberBean> mGroupBeanList,
			HashMap<Integer, List<GroupMemberBean>> mGroupChildList,
			String groupTitle, MyExpandableAdapter adpter, Runnable r,
			HashMap<String, AsyncTaskCallback> groupClass, List<String> groups) {
		super(mContext, mGroupBeanList, mGroupChildList, groupTitle, adpter, r,
				groupClass, groups);

		this.mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO Auto-generated constructor stub
	}

	private void loadFileData() {

		Uri uri = Uri
				.parse("content://com.kxd.notes.contentprovider.NotesContentProvider/table_notepad");

		ContentResolver contentresolver = this.mContext.getContentResolver();
		Cursor cur = contentresolver.query(uri, null, null, null, null);

		GroupMemberBean childBean = null;
		while (cur.moveToNext()) {

			NotesBean notes = new NotesBean();
			String title = cur.getString(cur
					.getColumnIndex(COLUMN_NAME_NOTE_NAME));
			String content = cur.getString(cur
					.getColumnIndex(COLUMN_NAME_NOTE_CONTENT));
			String date = cur.getString(cur
					.getColumnIndex(COLUMN_NAME_NOTE_DATE));
			String id = cur.getString(cur.getColumnIndex(COLUMN_NAME_ID));

			notes.setContent(content);
			notes.setTitle(title);
			notes.setDate(date);
			notes.setId(id);

			childBean = fillData(title);
			childBean.notes = notes;

			Drawable icon = mContext.getResources().getDrawable(
					R.drawable.add_normal_icon);
			childBean.setIcon(icon);
			mTask.doPublishProgress(childBean);
		}
		cur.close();

	}

	private LayoutInflater mInflater;
	

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

		final GroupMemberBean item = child.get(groupPosition)
				.get(childPosition);
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
		childHolderView.number.setText(item.notes.title);
		childHolderView.content.setText(item.notes.content);
		// childHolderView.date.setText(String.valueOf(item.getDate().toString()));
		childHolderView.date.setText(item.notes.date);
		childHolderView.item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GetNotesResponse.this.onClick(item);

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
	public List<GroupMemberBean> run() {
		try {
			loadFileData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onClick(GroupMemberBean item) {

		Bundle b = new Bundle();
		b.putString("titleItem", item.notes.title);
		b.putString("contentItem", item.notes.content);
		b.putString("dateItem", item.notes.date);
		b.putString("idItem", item.notes.id);
		b.putString("position", item.notes.id);

		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.kxd.notes", "com.kxd.notes.EditActivity"));

		intent.putExtras(b);
		mContext.startActivity(intent);

	}

}
