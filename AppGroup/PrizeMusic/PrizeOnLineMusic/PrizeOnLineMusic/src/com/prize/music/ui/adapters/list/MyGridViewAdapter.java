package com.prize.music.ui.adapters.list;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.music.R;
import com.prize.music.activities.MainActivity;
import com.prize.music.cache.ImageProvider;
import com.prize.music.views.ViewHolderList;

public class MyGridViewAdapter extends SimpleCursorAdapter {

	private WeakReference<ViewHolderList> holderReference;

	private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

	protected Context mContext;

	private int left, top;

	public String mListType = null, mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public boolean showContextEnabled = true;

	private ImageProvider mImageProvider;

	private LayoutInflater inflater;

	private Cursor mCursor;

	private FragmentActivity mActivity;
	// public static List<String> tableName = new ArrayList<String>();
	/**
	 * Used to quickly show our the ContextMenu
	 */
	private final View.OnClickListener showContextMenu = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			v.showContextMenu();
		}
	};

	public MyGridViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		inflater = LayoutInflater.from(context);
		mActivity = (FragmentActivity) context;

	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return super.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return super.getItem(position);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		// tableName.clear();
		super.changeCursor(cursor);
		// initData(cursor);
	}

	public void initData(Cursor cursor) {
		// cursor.moveToFirst();
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(PlaylistsColumns.NAME));
			// tableName.add(name);
		}

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// final View view = super.getView(position, convertView, parent);
		Cursor mCursor = (Cursor) getItem(position);

		GridViewHolder holder = null;
		if (convertView == null) {
			holder = new GridViewHolder();
			convertView = inflater.inflate(R.layout.edit_activity_item, null);
			holder.title = (TextView) convertView.findViewById(R.id.edit_title);
			holder.img = (ImageView) convertView.findViewById(R.id.eidt_img);
			holder.cb = (CheckBox) convertView.findViewById(R.id.edit_check);
			holder.cb.setVisibility(View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (GridViewHolder) convertView.getTag();
		}

		setupViewData(mCursor);
		holder.img.setBackgroundResource(R.drawable.new_list_old);
		holder.title.setText(mLineOneText);
		if (position == 0) {
			if (BaseApplication.SWITCH_UNSUPPORT) {
				holder.img.setBackgroundResource(R.drawable.icon_oldlove_music);

			} else {
				holder.img.setBackgroundResource(R.drawable.icon_love_music);

			}
			holder.title.setText(mActivity.getString(R.string.my_love));
		} else if (position == 1) {
			holder.img.setBackgroundResource(R.drawable.icon_add_list);
			holder.title.setText(mActivity.getString(R.string.create_list));
		}
		// holder.title.setText(mLineOneText);

		return convertView;
	}

	// public abstract void setupViewData(Cursor mCursor);
	public void setupViewData(Cursor mCursor) {
		mLineOneText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(PlaylistsColumns.NAME));
	}

	public final class GridViewHolder {
		public TextView title;
		public ImageView img;
		public CheckBox cb;
	}
}
