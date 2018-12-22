package com.prize.music.ui.adapters.list;

import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.TYPE_ARTIST;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.music.cache.ImageInfo;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.views.ViewHolderList;

public class NewListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Map<String, Object>> mData;
	public static Map<Integer, Boolean> isSelected;

	public NewListAdapter(Context context, List<Map<String, Object>> data,
			Boolean boo) {
		mInflater = LayoutInflater.from(context);
		mData = data;
		isSelected = new HashMap<Integer, Boolean>();
		init(boo);
	}

	// 初始化
	public void init(Boolean boo) {
		// 这儿定义isSelected这个map是记录每个listitem的状态，初始状态全部为false。
		if (boo == null) {
			return;
		}
		isSelected.clear();
		for (int i = 0; i < mData.size(); i++) {
			isSelected.put(i, boo);
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NewViewHolder holder = null;
		// convertView为null的时候初始化convertView。
		if (convertView == null) {
			holder = new NewViewHolder();

			convertView = mInflater.inflate(R.layout.my_listview_items, null);
			holder.title = (TextView) convertView
					.findViewById(R.id.listview_item_line_one);
			holder.artist = (TextView) convertView
					.findViewById(R.id.listview_item_line_two);
			holder.cb = (CheckBox) convertView
					.findViewById(R.id.checkBox);
			convertView.setTag(holder);
		} else {
			holder = (NewViewHolder) convertView.getTag();
		}

		holder.cb.setVisibility(View.VISIBLE);
		holder.title.setText(mData.get(position).get("title").toString());
		holder.artist.setText(mData.get(position).get("artist").toString());
		if (holder.cb != null) {
			holder.cb.setChecked(isSelected.get(position));
		}
		/*
		 * final ViewHolderList viewholder; if (convertView != null) {
		 * viewholder = new ViewHolderList(convertView); //holderReference = new
		 * WeakReference<ViewHolderList>(viewholder);
		 * convertView.setTag(viewholder); } else { viewholder =
		 * (ViewHolderList) convertView.getTag(); }
		 * viewholder.mViewHolderLineOne
		 * .setText(mData.get(position).get("title").toString());
		 * 
		 * viewholder.mViewHolderLineTwo.setText(mData.get(position).get("artist"
		 * ).toString());
		 * 
		 * ImageInfo mInfo = new ImageInfo(); mInfo.type = TYPE_ARTIST;
		 * mInfo.size = SIZE_THUMB; mInfo.source = SRC_FIRST_AVAILABLE;
		 * //mInfo.data = mImageData; //
		 * mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);
		 * 
		 * //viewholder.mQuickContext.setOnClickListener(showContextMenu);
		 * 
		 * if (mPlayingId == mCurrentId) { holderReference.get().mPeakOne
		 * .setImageResource(R.anim.peak_meter_1);
		 * holderReference.get().mPeakTwo
		 * .setImageResource(R.anim.peak_meter_2); mPeakOneAnimation =
		 * (AnimationDrawable) holderReference.get().mPeakOne .getDrawable();
		 * mPeakTwoAnimation = (AnimationDrawable)
		 * holderReference.get().mPeakTwo .getDrawable(); try { if
		 * (MusicUtils.mService.isPlaying()) { mPeakOneAnimation.start();
		 * mPeakTwoAnimation.start(); } else { mPeakOneAnimation.stop();
		 * holderReference.get().mPeakTwo
		 * .setImageResource(R.drawable.icon_play_stop);
		 * mPeakTwoAnimation.stop(); } } catch (RemoteException e) {
		 * e.printStackTrace(); } } else {
		 * holderReference.get().mPeakOne.setImageResource(0);
		 * holderReference.get().mPeakTwo.setImageResource(0); }
		 * 
		 * viewholder.checkBox.setVisibility(View.VISIBLE);
		 * 
		 * viewholder.checkBox.setChecked(isSelected.get(position));
		 */
		return convertView;
	}

	public final class NewViewHolder {
		public TextView title;
		public TextView artist;
		public CheckBox cb;
	}

}
