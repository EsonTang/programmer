package com.koobee.koobeecenter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.koobee.koobeecenter02.R;

/**
 * Created by yiyi on 2015/5/21.
 */
public class OutletsRowView extends FrameLayout {
	private TextView mTitleText;
	private GridView mGridView;

	public OutletsRowView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.outlets_row, this, true);
		mTitleText = (TextView) findViewById(R.id.title_text);
		mGridView = (GridView) findViewById(R.id.gridview);
	}

	public OutletsRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.outlets_row, this, true);
		mTitleText = (TextView) findViewById(R.id.title_text);
		mGridView = (GridView) findViewById(R.id.gridview);
	}

	public void setAdapter(ListAdapter adapter) {
		mGridView.setAdapter(adapter);
	}

	public void setOnItemClickListener(GridView.OnItemClickListener listenerl) {
		mGridView.setOnItemClickListener(listenerl);
	}

	public void setTitle(String text) {
		mTitleText.setText(text);
	}

	public ListAdapter getAdapter() {
		return mGridView.getAdapter();
	}
}
