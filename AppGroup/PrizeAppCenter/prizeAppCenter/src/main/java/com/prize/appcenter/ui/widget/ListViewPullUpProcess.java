package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.appcenter.R;

/**
 * listview的基本处理，继续滑动，加载更多内容的处理 在setAdapter之前调用，因为需要addFootView
 * 
 * @author prize
 *
 */
public class ListViewPullUpProcess {
	private boolean hasFootView = false;
	private View noLoading = null;
	private View loading = null;
	private ListView listView;
	protected int lastVisiblePosition;
	protected boolean isLoadingMore = false;
	private OnDataChangeListener dataChangeListener;
	private BaseAdapter adapter;

	/**
	 * 继续滑动，加载更多内容的处理，调用该类后，不需要再setAdapter
	 */
	public ListViewPullUpProcess(Context context, ListView view,
			OnDataChangeListener listener, BaseAdapter listAdapter) {
		listView = view;
		dataChangeListener = listener;
		adapter = listAdapter;
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		LayoutInflater inflater = LayoutInflater.from(context);
		loading = inflater.inflate(R.layout.footer_loading_small, null);
		noLoading = inflater.inflate(R.layout.footer_no_loading, null);
		// Call addFooterView before calling setAdapter
		addFootView();
		listView.setAdapter(adapter);
		removeFootView();
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView) {
			listView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			int count = adapter.getCount() - 1;

			if (lastVisiblePosition >= count && !isLoadingMore) {
				// 分页显示
				if (dataChangeListener.hasMoreData()) {
					addFootView();
					dataChangeListener.requestMoreData();
					isLoadingMore = true;
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = listView.getLastVisiblePosition();
		}
	};

	/**
	 * 添加加载更多
	 */
	private void addFootView() {
		if (!hasFootView) {
			listView.addFooterView(loading);
			hasFootView = true;
		}
	}

	/**
	 * 无更多加载
	 */
	private void addFootNoView() {
		if (!hasFootView) {
			listView.addFooterView(noLoading);
			hasFootView = true;
		}
	}

	public interface OnDataChangeListener {
		public boolean hasMoreData();

		public void requestMoreData();
	}

	/**
	 * 完成加载数据
	 */
	public void requestDone() {
		isLoadingMore = false;
		removeFootView();
		if (dataChangeListener.hasMoreData()) {

		} else {
			addFootNoView();
		}
	}
}
