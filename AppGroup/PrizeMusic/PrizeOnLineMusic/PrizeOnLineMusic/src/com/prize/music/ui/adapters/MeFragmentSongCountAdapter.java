package com.prize.music.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import com.prize.music.R;
import com.prize.music.database.ListInfo;
import com.prize.music.ui.fragments.MeFragment.SongCount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MeFragmentSongCountAdapter extends BaseAdapter{

	Context mContext;
	int layoutId;
	private LayoutInflater inflater;
	private List<SongCount> mLists = new ArrayList<SongCount>();
	public MeFragmentSongCountAdapter(Context context, int layoutId){
		mContext = context;
		this.layoutId = layoutId;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLists.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mLists.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		GridViewHolder holder = null;
		if (arg1 == null) {
			holder = new GridViewHolder();
			arg1 = inflater.inflate(layoutId, arg2,false);
			holder.title = (TextView) arg1.findViewById(R.id.action_bar_album_name);
			holder.img = (ImageView) arg1.findViewById(R.id.action_bar_album_art);
			holder.count = (TextView)arg1.findViewById(R.id.song_count);

			arg1.setTag(holder);
		} else {
			holder = (GridViewHolder) arg1.getTag();
		}

		holder.img.setImageResource(mLists.get(arg0).img_id);
		holder.title.setText(mContext.getString(mLists.get(arg0).title_id));
		holder.count.setText(mLists.get(arg0).song_count + mContext.getString(R.string.songs));
		return arg1;
	}
	
	
	
	public final class GridViewHolder {
		public TextView title;
		public ImageView img;
		public TextView count;
	}
	
	/**
	 * @see 是否尾部添加，true则直接添加到List里，false则先清除List
	 * @param lists
	 * @param isAppend
	 */
	public void addList(List<SongCount> lists,boolean isAppend){
		if(isAppend){
			mLists.addAll(lists);
			notifyDataSetChanged();
		} else {
			mLists.clear();
			mLists.addAll(lists);
			notifyDataSetChanged();

		} 
	}

	
}
