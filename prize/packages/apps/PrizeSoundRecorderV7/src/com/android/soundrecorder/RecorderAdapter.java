package com.android.soundrecorder;

import java.util.List;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecorderAdapter extends BaseExtAdapter {
	private Context context;
	private List<FileEntity> list;
	private OnClickRecordListener onClickRecordListener;
	/**There should be a normal height of 0, said it had not been calculated  */
	private int NORMAL_HEIGHT = 0;
	/**Minimum height can be the default of a minimum height  eg: 720p is 152*/
	private int MIN_HEIGHT = 152;
	/**Minimum height  */
	private int MAX_HEIGHT = MIN_HEIGHT + DLT_HEIGHT;
	/**True height */
	private int mItemHeight = 0;
	private boolean mIsPause;
	private boolean mIsEditMode;
	private FileEntity mPlayFileEntity;
	
	public RecorderAdapter(Context context, List<FileEntity> list,
			OnClickRecordListener onClickRecordListener) {
		this.context = context;
		this.list = list;
		this.onClickRecordListener = onClickRecordListener;
	}

	public interface OnClickRecordListener {

		public void onClick(int position, FileEntity fe);
		
	}
	
	public void setPlayPosition(FileEntity fe) {
		mPlayFileEntity = fe;
		notifyDataSetChanged();
	}
	
	public void setEditMode(boolean isEditMode) {
		mIsEditMode = isEditMode;
	}
	
	public void setPlayStatus(boolean isPause) {
		mIsPause = isPause;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.list_item, null);
			viewHolder.mNameTxt = (TextView) convertView
					.findViewById(R.id.record_name);
			viewHolder.mDurationTxt = (TextView) convertView
					.findViewById(R.id.record_duration);
			viewHolder.mTimeTxt = (TextView) convertView
					.findViewById(R.id.record_time);
			viewHolder.mSelectCB = (CheckBox) convertView
					.findViewById(R.id.record_checkbox);
			viewHolder.itemLayout = (RelativeLayout) convertView
					.findViewById(R.id.record_item_layout);
			viewHolder.mAnimIm = (ImageView) convertView.findViewById(R.id.im_play_anim);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final FileEntity fe = list.get(position);
		viewHolder.mNameTxt.setText(fe.getFileName());
		viewHolder.mDurationTxt.setText(fe.getDuration());
		viewHolder.mTimeTxt.setText(fe.getCreateTime());
		final CheckBox cb = viewHolder.mSelectCB;
		cb.setClickable(false);
		
		AnimationDrawable animation = (AnimationDrawable) viewHolder.mAnimIm.getDrawable();
		
		if (mIsEditMode) {
			if (fe.isChecked()) {
				cb.setChecked(true);
			} else {
				cb.setChecked(false);
			}
			cb.setVisibility(View.VISIBLE);
			viewHolder.mAnimIm.setVisibility(View.GONE);
			if (animation.isRunning()) {
				animation.stop();
			}
		} else {
			cb.setVisibility(View.GONE);
			if (mPlayFileEntity != null && mPlayFileEntity.getPath().equals(fe.getPath())) {
				viewHolder.mAnimIm.setVisibility(View.VISIBLE);
				if (onClickRecordListener != null) {
					if (mIsPause) {
						if (animation.isRunning()) {
							animation.stop();
						}
					} else {
						if (!animation.isRunning()) {
							animation.start();
						}
					}
				}
			} else {
				viewHolder.mAnimIm.setVisibility(View.GONE);
				if (animation.isRunning()) {
					animation.stop();
				}
			}
		}
		fe.setCheckBox(cb);
		viewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (onClickRecordListener != null) {
					if (!mIsEditMode) {
						mPlayFileEntity = fe;
					}
					onClickRecordListener.onClick(position, fe);
				}
			}
		});
		return convertView;
	}
	
	public class ViewHolder {
		public RelativeLayout itemLayout;
		public TextView mNameTxt, mDurationTxt, mTimeTxt;
		public CheckBox mSelectCB;
		public ImageView mAnimIm;
	}

	@Override
	public void setItemHeight(int h) {
		// TODO Auto-generated method stub
		mItemHeight = h;
	}

	@Override
	public int getCurrentItemHeight() {
		// TODO Auto-generated method stub
		if (mItemHeight == 0)
			mItemHeight = MIN_HEIGHT;
		return mItemHeight;
	}

	@Override
	public int getMaxItemHeight() {
		// TODO Auto-generated method stub
		return MAX_HEIGHT;
	}

	@Override
	public int getMinItemHeight() {
		// TODO Auto-generated method stub
		return MIN_HEIGHT;
	}
	
	@Override
	public int getNormalItemHeight() {
		if (NORMAL_HEIGHT != 0)
			return NORMAL_HEIGHT;
		else
			return MIN_HEIGHT;
	}

	public List<FileEntity> getListData() {
		return list;
	}
}
