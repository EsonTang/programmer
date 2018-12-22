package com.prize.music.ui.fragments.list;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.prize.music.activities.Scroller;
import com.prize.music.ui.fragments.base.ListViewFragment;
import com.prize.music.R;

public abstract class ScrollerFragment extends ListViewFragment implements
		Scroller {

	private Scroller mScroller;
	private int mHeaderHeight;
	private int mMinHeaderHeight;
	private View footer;

	public ScrollerFragment setScroller(Scroller scrollTabHolder) {
		mScroller = scrollTabHolder;
		return this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View placeHolderView = inflater.inflate(
				R.layout.view_header_placeholder, mListView, false);
		mListView.addHeaderView(placeHolderView);
		placeHolderView.setOnClickListener(null);
		footer = inflater.inflate(R.layout.view_footer_placeholder2, mListView,
				false);
		footer.setOnClickListener(null);
		mListView.addFooterView(footer);
		mListView.setPadding(mListView.getPaddingLeft(), (int) getResources()
				.getDimension(R.dimen.artist_tab_h), mListView
				.getPaddingRight(), mListView.getPaddingBottom());
		mHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.header_height);
		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.min_header_height);
		return view;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);
		if (mCursor != null) {
			if (mCursor.getCount() > 6)
				mListView.removeFooterView(footer);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setOnScrollListener(new OnScrollListener() {
			private boolean isFromUser;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				isFromUser = true;
				if (scrollState == SCROLL_STATE_IDLE) {
					int scrollY = getScrollY(view);
					int middle = mHeaderHeight / 2;
					if (scrollY < middle)
						mListView.smoothScrollToPosition(0);
					else if (mCursor != null && (mCursor.getCount() < 6
							|| (mCursor.getCount() - view.getChildCount() > 1 && mListView
									.getFirstVisiblePosition() < 2)))
						mListView.setSelection(1);

					mListView.postDelayed(new Runnable() {

						@Override
						public void run() {
							int scrollY = getScrollY(mListView);
							mScroller.onScroll(Math.max(-scrollY,
									-mMinHeaderHeight));
							isFromUser = false;
						}
					}, 200);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mScroller != null && isFromUser) {
					int scrollY = getScrollY(view);
					mScroller.onScroll(Math.max(-scrollY, -mMinHeaderHeight));
				}
			}
		});
	}

	@Override
	public void onScroll(int scrollY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// if (scrollHeight == 0 && mListView.getFirstVisiblePosition() >= 1) {
		// return;
		// }
		// mListView.setSelectionFromTop(1, scrollHeight);
		if (Math.abs(scrollHeight) > mHeaderHeight) {
			mListView.scrollListBy(-mHeaderHeight);
			mListView.scrollListBy(mHeaderHeight);
		} else {
			mListView.setSelection(0);
		}

	}

	private int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop();

		int headerHeight = 0;
		if (firstVisiblePosition >= 1) {
			headerHeight = mHeaderHeight;
		}

		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}
}
