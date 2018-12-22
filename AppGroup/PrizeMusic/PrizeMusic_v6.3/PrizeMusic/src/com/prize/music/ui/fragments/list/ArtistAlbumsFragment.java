package com.prize.music.ui.fragments.list;

import static com.prize.music.Constants.EXTERNAL;
import static com.prize.music.Constants.TYPE_ALBUM;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.prize.music.activities.Scroller;
import com.prize.music.helpers.utils.ApolloUtils;
import com.prize.music.ui.adapters.list.ArtistAlbumAdapter;
import com.prize.music.ui.fragments.base.AlbumsListGridFragment;
import com.prize.music.ui.widgets.GridViewWith;
import com.prize.music.R;

/**
 * 歌手列表里的小专辑界面
 * 
 * @author Administrator
 *
 */
public class ArtistAlbumsFragment extends AlbumsListGridFragment {

	public ArtistAlbumsFragment(Bundle args) {
		setArguments(args);
	}
	
	public ArtistAlbumsFragment() {
	}

	public void setupFragmentData() {
		mAdapter = new ArtistAlbumAdapter(getActivity(),
				R.layout.item_albums_gride_layout, null, new String[] {},
				new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, AlbumColumns.ALBUM,
				AlbumColumns.NUMBER_OF_SONGS, AlbumColumns.ARTIST };
		mSortOrder = Audio.Albums.DEFAULT_SORT_ORDER;
		long artistId = getArguments().getLong((BaseColumns._ID));
		mUri = Audio.Artists.Albums.getContentUri(EXTERNAL, artistId);
		mFragmentGroupId = 7;
		mType = TYPE_ALBUM;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (id > 0)
			ApolloUtils.startTracksBrowser(mType, id, mCursor, getActivity());
	}

	private Scroller mScroller;
	private int mHeaderHeight;
	private int mMinHeaderHeight;

	public ArtistAlbumsFragment setScroller(Scroller scrollTabHolder) {
		mScroller = scrollTabHolder;
		return this;
	}

	public void adjustScroll(int scrollHeight) {
		// if (scrollHeight == 0 && mGridView.getFirstVisiblePosition() >= 6) {
		// return;
		// }

		if (Math.abs(scrollHeight) > mHeaderHeight) {
			mGridView.scrollListBy(-mHeaderHeight);
			mGridView.scrollListBy(mHeaderHeight);
		} else {
			mGridView.setSelection(0);
		}
		// mGridView.setSelectionFromTop(0, scrollHeight);
		// igored = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if (mGridView instanceof GridViewWith) {
			View placeHolderView = inflater.inflate(
					R.layout.view_header_placeholder, mGridView, false);
			placeHolderView.setOnClickListener(null);
			((GridViewWith) mGridView).addHeaderView(placeHolderView);
			View footer = inflater.inflate(R.layout.view_footer_placeholder,
					null);
			footer.setOnClickListener(null);
			((GridViewWith) mGridView).addFooterView(footer);
		}

		mGridView.setPadding(mGridView.getPaddingLeft(), (int) getResources()
				.getDimension(R.dimen.artist_tab_h), mGridView
				.getPaddingRight(), mGridView.getPaddingBottom());
		mHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.header_height);
		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.min_header_height);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					int scrollY = getScrollY(view);
					int middle = mHeaderHeight / 2;
					if (scrollY < middle)
						mGridView.smoothScrollToPosition(0);
					else if (mGridView.getFirstVisiblePosition() < 6)
						mGridView.setSelection(3);
				} 

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mScroller != null) {
					int scrollY = getScrollY(view);
					if (Math.abs(-scrollY + mMinHeaderHeight) < 15)
						scrollY = mMinHeaderHeight;
					mScroller.onScroll(Math.max(-scrollY, -mMinHeaderHeight));
				}
			}
		});
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

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v,
	// ContextMenuInfo menuInfo) {
	// menu.add(mFragmentGroupId, PLAY_SELECTION, 0,
	// getResources().getString(R.string.play_all));
	// menu.add(mFragmentGroupId, ADD_TO_PLAYLIST, 0, getResources()
	// .getString(R.string.add_to_playlist));
	// menu.add(mFragmentGroupId, SEARCH, 0,
	// getResources().getString(R.string.search));
	// mCurrentId = mCursor.getString(mCursor
	// .getColumnIndexOrThrow(BaseColumns._ID));
	// menu.setHeaderView(ApolloUtils.setHeaderLayout(mType, mCursor,
	// getActivity()));
	// }
	//
	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// if (item.getGroupId() == mFragmentGroupId) {
	// switch (item.getItemId()) {
	// case PLAY_SELECTION: {
	// long[] list = MusicUtils.getSongListForAlbum(getActivity(),
	// Long.parseLong(mCurrentId));
	// MusicUtils.playAll(getActivity(), list, 0);
	// break;
	// }
	// case ADD_TO_PLAYLIST: {
	// Intent intent = new Intent(INTENT_ADD_TO_PLAYLIST);
	// long[] list = MusicUtils.getSongListForAlbum(getActivity(),
	// Long.parseLong(mCurrentId));
	// intent.putExtra(INTENT_PLAYLIST_LIST, list);
	// getActivity().startActivity(intent);
	// break;
	// }
	// case SEARCH: {
	// MusicUtils.doSearch(getActivity(), mCursor,
	// mCursor.getColumnIndexOrThrow(AlbumColumns.ALBUM));
	// break;
	// }
	// default:
	// break;
	// }
	// return true;
	// }
	// return super.onContextItemSelected(item);
	// }
}
