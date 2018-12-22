/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.download.DownloadHelper;
import com.prize.app.util.ToastUtils;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 批选界面adapter
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class BatchDownloadedEditAdapter extends BaseAdapter {
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();
	private Context context;
	/** 存储每个条目勾选的状态 */
	protected SparseBooleanArray mCheckedStates = null;

	private boolean isSelectAll = false;

	private ColorDrawable transparentDrawable;

	public BatchDownloadedEditAdapter() {
	}

	public boolean isSelectAll() {
		return isSelectAll;
	}

	public void setIsSelectAll(boolean selectAll) {
		isSelectAll = selectAll;
	}

	/**
	 * 全选或全不选
	 *
	 * @param selectAll
	 *            true表示全选,false表示全不选
	 */
	public void selectAllItem(boolean selectAll) {
		if (selectAll) {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, true);
			}
			isSelectAll = true;
		} else {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, false);
			}
			isSelectAll = false;
		}
		notifyDataSetChanged();
	}

	PromptDialogFragment df = null;

	public BatchDownloadedEditAdapter(Context activity) {
		this.context = activity;
		transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
		mCheckedStates = new SparseBooleanArray();
	}

	@Override
	public int getCount() {

		return items == null ? 0 : items.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<SongDetailInfo> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<SongDetailInfo> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final SongDetailInfo bean = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_batch_editsong_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.songName_Tv = (TextView) convertView
					.findViewById(R.id.songName_Tv);
			viewHolder.select_flag_Tv = (TextView) convertView
					.findViewById(R.id.select_flag_Tv);
			viewHolder.singer_Tv = (TextView) convertView
					.findViewById(R.id.singer_Tv);
			convertView.setTag(viewHolder);
			transparentDrawable.setBounds(0, 0,
					transparentDrawable.getMinimumWidth(),
					transparentDrawable.getMinimumHeight()); // 设置边界
			viewHolder.singer_Tv.setCompoundDrawables(null, null,
					transparentDrawable, null);// 画在右边
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.singer_Tv.setCompoundDrawablePadding(10);
		viewHolder.songName_Tv.setText(bean.song_name);
		viewHolder.singer_Tv.setText(bean.singers);
		if (mCheckedStates.get(position)) {
			viewHolder.select_flag_Tv
					.setBackgroundResource(R.drawable.icon_batch_selected);
		} else {
			viewHolder.select_flag_Tv
					.setBackgroundResource(R.drawable.icon_batch_unselect);
		}

		return convertView;
	}

	static class ViewHolder {
		// 榜单排行名称
		TextView songName_Tv;
		TextView singer_Tv;
		ImageView download_flag_Tv;
		TextView select_flag_Tv;

	}

	/**
	 * 改变指定位置条目的选择状态，如果已经处于勾选状态则取消勾选，如果处于没有勾选则勾选
	 * 
	 * @param position
	 *            要改变的条目选择状态的位置
	 */
	public void toggleCheckedState(int position) {
		if (position >= 0 && position < getCount()) {
			if (mCheckedStates.get(position)) {
				mCheckedStates.put(position, false);
			} else {
				mCheckedStates.put(position, true);
			}
			notifyDataSetChanged();
		}
	}

	/**
	 * 获得已选择的条目们在列表中的位置
	 * 
	 * @return 所有已选择的条目在列表中的位置
	 */
	public int[] getSelectedItemPositions() {
		int count = 0;
		for (int i = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				count++;
			}
		}
		int[] checkedPostions = new int[count];
		for (int i = 0, j = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				checkedPostions[j] = i;
				j++;
			}
		}
		return checkedPostions;
	}

	public SparseBooleanArray getmCheckedStates() {
		return mCheckedStates;
	}

	public long[] getSelectedAudioIds() {
		int[] checkedPostions = getSelectedItemPositions();
		long[] selectedAudioIds = new long[checkedPostions.length];
		SongDetailInfo mCursor;
		int len = checkedPostions.length;
		for (int i = 0; i < len; i++) {
			mCursor = (SongDetailInfo) getItem(checkedPostions[i]);

			selectedAudioIds[i] = mCursor.song_id;
		}
		return selectedAudioIds;
	}

	public ArrayList<SongDetailInfo> getSelectedSongDetailInfo() {
		int[] checkedPostions = getSelectedItemPositions();
		ArrayList<SongDetailInfo> infos = new ArrayList<SongDetailInfo>();
		int len = checkedPostions.length;
		for (int i = 0; i < len; i++) {
			SongDetailInfo mCursor = (SongDetailInfo) getItem(checkedPostions[i]);
			infos.add(mCursor);
		}
		return infos;
	}
}
