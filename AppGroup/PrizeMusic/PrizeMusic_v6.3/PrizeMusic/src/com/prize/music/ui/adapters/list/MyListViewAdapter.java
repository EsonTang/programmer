package com.prize.music.ui.adapters.list;

import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.TYPE_ARTIST;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prize.music.R;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.views.ViewHolderList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListViewAdapter extends BaseAdapter {

	private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

	private WeakReference<ViewHolderList> holderReference;

	protected Context mContext;

	private ImageProvider mImageProvider;

	public String mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	private List<Map<String, Object>> mData;
	public static Map<Integer, Boolean> isSelected;
	private LayoutInflater mInflater;

	private final View.OnClickListener showContextMenu = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			v.showContextMenu();
		}
	};

	public MyListViewAdapter(Context context, List<Map<String, Object>> data,
			Boolean boo) {
		mContext = context;
		mImageProvider = ImageProvider.getInstance((Activity) mContext);
		mData = data;
		mInflater = LayoutInflater.from(context);
		init(boo);
	}

	// 初始化
	private void init(Boolean boo) {
		// 这儿定义isSelected这个map是记录每个listitem的状态，初始状态全部为false。
		isSelected = new HashMap<Integer, Boolean>();
		for (int i = 0; i < mData.size(); i++) {
			isSelected.put(i, boo);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// Cursor mCursor = (Cursor) getItem(position);
		// setupViewData(mCursor);

		ViewHolder viewholder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_items, null);
			viewholder = new ViewHolder(convertView);
			// holderReference = new WeakReference<ViewHolderList>(viewholder);
			// convertView.setTag(holderReference.get());
			viewholder.mViewHolderLineOne = (TextView) convertView
					.findViewById(R.id.listview_item_line_one);
			viewholder.mViewHolderLineTwo = (TextView) convertView
					.findViewById(R.id.listview_item_line_two);
			viewholder.mQuickContext = (FrameLayout) convertView
					.findViewById(R.id.track_list_context_frame);
			viewholder.mPeakOne = (ImageView) convertView
					.findViewById(R.id.peak_one);
			viewholder.mPeakTwo = (ImageView) convertView
					.findViewById(R.id.peak_two);
			viewholder.mQuickContextDivider = (ImageView) convertView
					.findViewById(R.id.quick_context_line);
			viewholder.mQuickContextTip = (ImageView) convertView
					.findViewById(R.id.quick_context_tip);

			viewholder.item_checkBox = (CheckBox) convertView
					.findViewById(R.id.checkBox);
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		viewholder.mViewHolderLineOne.setText(mData.get(position).get("title")
				.toString());

		viewholder.mViewHolderLineTwo.setText(mData.get(position).get("artist")
				.toString());

		ImageInfo mInfo = new ImageInfo();
		mInfo.type = TYPE_ARTIST;
		mInfo.size = SIZE_THUMB;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = mImageData;
		// mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);

		viewholder.mQuickContext.setOnClickListener(showContextMenu);

		if (mPlayingId == mCurrentId) {
			viewholder.mPeakOne.setImageResource(R.anim.peak_meter_1);
			viewholder.mPeakTwo.setImageResource(R.anim.peak_meter_black_2);
			mPeakOneAnimation = (AnimationDrawable) viewholder.mPeakOne
					.getDrawable();
			mPeakTwoAnimation = (AnimationDrawable) viewholder.mPeakTwo
					.getDrawable();
			try {
				if (MusicUtils.mService.isPlaying()) {
					mPeakOneAnimation.start();
					mPeakTwoAnimation.start();
				} else {
					mPeakOneAnimation.stop();
					mPeakTwoAnimation.stop();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			viewholder.mPeakOne.setImageResource(0);
			viewholder.mPeakTwo.setImageResource(0);
		}
		viewholder.item_checkBox.setChecked(isSelected.get(position));
		return convertView;
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

	public class ViewHolder {
		// public final ImageView mViewHolderImage;
		public ImageView mPeakOne, mPeakTwo, mQuickContextDivider,
				mQuickContextTip;

		public TextView mViewHolderLineOne;

		public TextView mViewHolderLineTwo;

		public FrameLayout mQuickContext;
		// huanglingjun
		public CheckBox item_checkBox;

		public ViewHolder(View view) {
			// mViewHolderImage = (ImageView) view
			// .findViewById(R.id.listview_item_image);

		}
	}
}
