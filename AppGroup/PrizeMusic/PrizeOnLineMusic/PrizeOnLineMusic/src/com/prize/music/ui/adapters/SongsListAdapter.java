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

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestResCode;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.CommonClickUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.adapters.base.BaseOnlineAdapter;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.views.ViewHolderList;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 可以编辑的歌曲列表
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SongsListAdapter extends BaseOnlineAdapter {
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();

	private Drawable drawable;
	// private Drawable sortOk;
	// private Drawable unsort;

	private ColorDrawable transparentDrawable;
	private int currentSelectItem = -1;
	private UIDownLoadListener listener = null;
	/** 当前页是否处于显示状态 */
	private boolean isActivity = true; // 默认true
	PromptDialogFragment df = null;
	private SongDetailInfo SelectBean;
	private String where;
	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public SongsListAdapter(FragmentActivity activity) {
		super(activity);
		drawable = context.getResources().getDrawable(R.drawable.icon_hq, null);
		transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
		listener = new UIDownLoadListener() {
			@Override
			protected void onErrorCode(int song_Id, int errorCode) {
			}

			@Override
			protected void onFinish(int song_Id) {
				notifyDataSetChanged();
			}

			@Override
			public void onRefreshUI(int song_Id) {
			}
		};

		df = PromptDialogFragment
				.newInstance(
						context.getString(R.string.sure_you_want_to_delete_local_musci),
						mDeletePromptListener);
		df.setmListener(mDeletePromptListener);
		// df.show(context.getSupportFragmentManager(), "loginDialog");
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final SongDetailInfo bean = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_song_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.num_Tv = (TextView) convertView
					.findViewById(R.id.num_Tv);
			viewHolder.songName_Tv = (TextView) convertView
					.findViewById(R.id.songName_Tv);
			viewHolder.singer_Tv = (TextView) convertView
					.findViewById(R.id.singer_Tv);
			viewHolder.delete_song_Tv = (TextView) convertView
					.findViewById(R.id.delete_song_Tv);
			viewHolder.share_Tv = (TextView) convertView
					.findViewById(R.id.share_Tv);
			viewHolder.sort_Tv = (TextView) convertView
					.findViewById(R.id.sort_Tv);
			viewHolder.add_Tv = (TextView) convertView
					.findViewById(R.id.add_Tv);
			viewHolder.edit_Llyt = (LinearLayout) convertView
					.findViewById(R.id.edit_Llyt);
			viewHolder.more_menu_iBtn = (ImageButton) convertView
					.findViewById(R.id.more_menu_iBtn);
			viewHolder.download_iBtn = (ImageButton) convertView
					.findViewById(R.id.download_iBtn);
			viewHolder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			// viewHolder.Llyt = (LinearLayout) convertView
			// .findViewById(R.id.Llyt);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.num_Tv.setText(position + 1 + "");
		viewHolder.songName_Tv.setText(bean.song_name);
		viewHolder.singer_Tv.setText(bean.singers);
		transparentDrawable.setBounds(0, 0,
				transparentDrawable.getMinimumWidth(),
				transparentDrawable.getMinimumHeight()); // 设置边界
		viewHolder.singer_Tv.setCompoundDrawables(null, null,
				transparentDrawable, null);// 画在右边
		if (bean.permission.quality != null
				&& bean.permission.quality.length > 0) {
			int len = bean.permission.quality.length;
			for (int i = 0; i < len; i++) {
				if ("h".equalsIgnoreCase(bean.permission.quality[i])) {
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight()); // 设置边界
					viewHolder.singer_Tv.setCompoundDrawables(null, null,
							drawable, null);// 画在右边
					viewHolder.singer_Tv.setCompoundDrawablePadding(10);
					break;
				}
			}
		}
		if (currentSelectItem == position) {
			viewHolder.edit_Llyt.setVisibility(View.VISIBLE);
		} else {
			viewHolder.edit_Llyt.setVisibility(View.GONE);
		}
		viewHolder.share_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MusicUtils.doShare(context, Constants.KEY_SONGS, bean.singers,
						bean.song_name, bean.song_id);

			}
		});
		if (DownloadHelper.isFileExists(bean)) {
			viewHolder.download_iBtn.setEnabled(false);
			viewHolder.delete_song_Tv.setEnabled(true);
		} else {
			viewHolder.download_iBtn.setEnabled(true);
			viewHolder.delete_song_Tv.setEnabled(false);
		}
		viewHolder.more_menu_iBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (currentSelectItem == position) {
					currentSelectItem = -1;
				} else {
					currentSelectItem = position;
				}
				notifyDataSetChanged();
			}
		});
		viewHolder.download_iBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				DownLoadUtils.downloadMusic(bean);
				ToastUtils.showToast(R.string.add_download_queue_ok);
			}
		});
		viewHolder.delete_song_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectBean = bean;
				if (df != null && !df.isAdded()) {
					// df.setTitle(R.string.sure_you_want_to_delete
					// + bean.song_name + "?");
					df.setmListener(mDeletePromptListener);
					df.show(context.getSupportFragmentManager(), "loginDialog");
				}

			}
		});
		final MusicInfo music_info = new MusicInfo(bean.song_name,
				bean.singers, bean.album_name, bean.album_logo,bean.song_id,CommonUtils.queryUserId(),
				DatabaseConstant.ONLIEN_TYPE);
		final boolean isMyLove = MusicUtils.isCollected(context, music_info,
				DatabaseConstant.TABLENAME_LOVE);
		viewHolder.sort_Tv.setSelected(isMyLove);
		viewHolder.sort_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
					UiUtils.jumpToLoginActivity();
					return;
				}
				//防止快速点击出现网络错误提示
				if (CommonClickUtils.isFastDoubleClick()) {
					return;
				}
				music_info.userId = CommonUtils.queryUserId();
				MusicUtils.doCollectMusic(music_info,
						isMyLove ? RequestResCode.CANCEL : RequestResCode.POST,
						context, mAddLoveCallBack,
						DatabaseConstant.TABLENAME_LOVE);
			}
		});
		viewHolder.add_Tv.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				MusicInfo music_info = MusicUtils.SongDetailInfoToMusicInfo(
						bean, MusicUtils.getUserId());
				MusicUtils.addMusicToTableDialog(context, music_info,
						mAddCallback);
			}
		});
		int mCurrentId = bean.song_id;
		mPlayingId = MusicUtils.getCurrentAudioId();
		if ((mPlayingId != 0 && mCurrentId != 0) && mPlayingId == mCurrentId) {
			viewHolder.songName_Tv.setTextColor(context.getResources()
					.getColor(R.color.gold_color));
			viewHolder.num_Tv.setTextColor(context.getResources().getColor(
					R.color.gold_color));
			viewHolder.singer_Tv.setTextColor(context.getResources().getColor(
					R.color.gold_color));
		} else {
			viewHolder.songName_Tv.setTextColor(context.getResources()
					.getColor(R.color.text_color_262626));
			viewHolder.singer_Tv.setTextColor(context.getResources().getColor(
					R.color.text_color_969696));
			viewHolder.num_Tv.setTextColor(context.getResources().getColor(
					R.color.text_color_969696));
		}
		return convertView;
	}

	static class ViewHolder {
		// public LinearLayout Llyt;
		TextView num_Tv;
		TextView songName_Tv;
		TextView singer_Tv;
		ImageButton download_iBtn;
		ImageButton more_menu_iBtn;
		LinearLayout edit_Llyt;
		TextView delete_song_Tv;
		TextView add_Tv;
		TextView sort_Tv;
		TextView share_Tv;
		ImageView icon_fly;
	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AppManagerCenter.removeDownloadRefreshHandle(listener);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AppManagerCenter.setDownloadRefreshHandle(listener);
	}

	/**
	 * @see 收藏/取消收藏 操作后回调更新UI
	 * @author lixing
	 */
	MusicUtils.AddCollectCallBack mAddLoveCallBack = new MusicUtils.AddCollectCallBack() {
		@Override
		public void addCollectResult(boolean result,String tableName) {
			if (result) {
				ToastUtils.showToast(R.string.sort_love_list_yet);
			} else {
				ToastUtils.showToast(R.string.already_cancel_sort);
			}
			notifyDataSetChanged();
			if( !TextUtils.isEmpty(where) &&!TextUtils.isEmpty(tableName) && tableName.equals(DatabaseConstant.TABLENAME_LOVE)){
				Intent intent = new Intent();
				intent.setAction(Constants.REFLUSH_SONGS_BROADCAST);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
		}

		@Override
		public void isCollected() {
			ToastUtils.showToast(R.string.data_has_sort);

		}
	};
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			df.dismissAllowingStateLoss();
			if (DownloadHelper.deleteDownloadedFile(SelectBean)) {
				ToastUtils.showToast(R.string.deleteSuccessful);
				currentSelectItem = -1;
				notifyDataSetChanged();
			}
		}

	};
	AddCollectCallBack mAddCallback = new AddCollectCallBack() {
		public void addCollectResult(boolean result,String tableName) {
			if(!TextUtils.isEmpty(tableName)&&DatabaseConstant.TABLENAME_LOVE.equals(tableName)){
				notifyDataSetChanged();
			}
			if( !TextUtils.isEmpty(where) &&!TextUtils.isEmpty(tableName) && tableName.equals(DatabaseConstant.TABLENAME_LOVE)){
				Intent intent = new Intent();
				intent.setAction(Constants.REFLUSH_SONGS_BROADCAST);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			}
			ToastUtils.showShortToast(context, context.getResources()
					.getString(R.string.addSuccessful));
		}

		@Override
		public void isCollected() {
			ToastUtils.showToast(R.string.Song_has_been);

		}
	};
}
