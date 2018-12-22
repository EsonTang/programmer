package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.ui.adapters.base.BaseOnlineAdapter;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.MainApplication;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.SongsBean;

/**
 * @author prize 热门歌曲的全部显示
 */
public class SongsPlayAdapter extends BaseOnlineAdapter {
	// private FragmentActivity ctx;
	private ArrayList<SongDetailInfo> datas;
	private UIDownLoadListener listener = null;

	private int currentSelectItem = -1;
//	private boolean isMyLove;
	private SongDetailInfo SelectBean;
	PromptDialogFragment df = null;

	public SongsPlayAdapter(FragmentActivity activity) {
		super(activity);
		// ctx = activity;
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
						MainApplication.getContext().getString(R.string.sure_you_want_to_delete_local_musci),
						mDeletePromptListener);
		Log.i("inr","str="+MainApplication.getContext().getString(R.string.sure_you_want_to_delete_local_musci));
		df.setmListener(mDeletePromptListener);
	}

	public void setData(ArrayList<SongDetailInfo> datas) {
		if (datas == null || datas.size() <= 0)
			return;
		this.datas = datas;
		notifyDataSetChanged();
	}

	public void allData(ArrayList<SongDetailInfo> datas) {
		if (datas == null || datas.size() <= 0)
			return;
		this.datas.addAll(datas);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		return datas.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_hotsong_layout, null);
			holder.game_number_tv = (TextView) convertView
					.findViewById(R.id.game_number_tv);
			holder.song_name_tv = (TextView) convertView
					.findViewById(R.id.song_name_tv);
			holder.album_name_tv = (TextView) convertView
					.findViewById(R.id.album_name_tv);
			holder.download_iBtn = (ImageButton) convertView
					.findViewById(R.id.download_iBtn);
			holder.more_menu_iBtn = (ImageButton) convertView
					.findViewById(R.id.more_menu_iBtn);
			holder.edit_Llyt = (LinearLayout) convertView
					.findViewById(R.id.edit_Llyt);
			holder.delete_song_Tv = (TextView) convertView
					.findViewById(R.id.delete_song_Tv);
			holder.share_Tv = (TextView) convertView
					.findViewById(R.id.share_Tv);
			holder.sort_Tv = (TextView) convertView.findViewById(R.id.sort_Tv);
			holder.add_Tv = (TextView) convertView.findViewById(R.id.add_Tv);
			holder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			holder.Llyt = (LinearLayout) convertView.findViewById(R.id.Llyt);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final SongDetailInfo bean = datas.get(position);

		holder.game_number_tv.setText(String.valueOf(position + 1));

		if (!TextUtils.isEmpty(bean.song_name)) {
			holder.song_name_tv.setText(bean.song_name);
		}
		if (!TextUtils.isEmpty(bean.album_name)) {
			holder.album_name_tv.setText(bean.album_name);
		}
//		if (bean.permission != null) {
//			if (!bean.permission.available) {
//				holder.download_iBtn.setVisibility(View.INVISIBLE);
//				holder.add_Tv.setEnabled(false);
//				holder.share_Tv.setEnabled(false);
//				holder.sort_Tv.setEnabled(false);
//			} else {
//				holder.download_iBtn.setVisibility(View.VISIBLE);
//				holder.add_Tv.setEnabled(true);
//				holder.share_Tv.setEnabled(true);
//				holder.sort_Tv.setEnabled(true);
//			}
//		}
		if (currentSelectItem == position) {
			holder.edit_Llyt.setVisibility(View.VISIBLE);
		} else {
			holder.edit_Llyt.setVisibility(View.GONE);
		}

		if (DownloadHelper.isFileExists(bean)) {
			holder.download_iBtn.setEnabled(false);
			holder.delete_song_Tv.setEnabled(true);
		} else {
			holder.download_iBtn.setEnabled(true);
			holder.delete_song_Tv.setEnabled(false);
		}

		holder.more_menu_iBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (currentSelectItem == position) {
					currentSelectItem = -1;
				} else {
					currentSelectItem = position;
				}
				notifyDataSetChanged();
			}
		});
		/** 下载的点击事件 */
		holder.download_iBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				DownLoadUtils.downloadMusic(bean);
				ToastUtils.showToast(R.string.add_download_queue_ok);
			}
		});

		holder.share_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MusicUtils.doShare(context, Constants.KEY_SONGS, bean.singers,
						bean.song_name, bean.song_id);
			}
		});

		holder.delete_song_Tv.setOnClickListener(new OnClickListener() {

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

//		try {
//			isMyLove = MusicUtils.mService.isCollected(music_info,
//					DatabaseConstant.TABLENAME_LOVE);
//		} catch (RemoteException e) {
//
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//
//		}
		final MusicInfo music_info = new MusicInfo(bean.song_name,
				bean.singers, bean.song_id, CommonUtils.queryUserId(),
				DatabaseConstant.ONLIEN_TYPE);
		final boolean isMyLove = MusicUtils.isCollected(context, music_info,
				DatabaseConstant.TABLENAME_LOVE);
		holder.sort_Tv.setSelected(isMyLove);
		holder.sort_Tv.setOnClickListener(new OnClickListener() {

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

		holder.add_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MusicInfo music_info = MusicUtils.SongDetailInfoToMusicInfo(
						bean, MusicUtils.getUserId());
				MusicUtils.addMusicToTableDialog(context, music_info,
						mAddCallback);
			}
		});

		int mCurrentId = bean.song_id;
		mPlayingId = MusicUtils.getCurrentAudioId();
		if ((mPlayingId != 0 && mCurrentId != 0) && mPlayingId == mCurrentId) {
			holder.song_name_tv.setTextColor(context.getResources().getColor(
					R.color.gold_color));
			holder.game_number_tv.setTextColor(context.getResources().getColor(
					R.color.gold_color));
			holder.album_name_tv.setTextColor(context.getResources().getColor(
					R.color.gold_color));
		} else {
			holder.song_name_tv.setTextColor(context.getResources().getColor(
					R.color.text_color_262626));
			holder.album_name_tv.setTextColor(context.getResources().getColor(
					R.color.text_color_969696));
			holder.game_number_tv.setTextColor(context.getResources().getColor(
					R.color.text_color_969696));
		}
		return convertView;
	}

	static class ViewHolder {

		public LinearLayout Llyt;
		ImageButton download_iBtn;
		ImageButton more_menu_iBtn;

		TextView game_number_tv;
		TextView song_name_tv;
		TextView album_name_tv;

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

		}

		@Override
		public void isCollected() {
			
		}
	};
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			df.dismissAllowingStateLoss();
			if (DownloadHelper.deleteDownloadedFile(SelectBean)) {
				ToastUtils.showToast(R.string.deleteSuccessful);
				notifyDataSetChanged();
			}
		}

	};
	AddCollectCallBack mAddCallback = new AddCollectCallBack() {
		public void addCollectResult(boolean result,String tableName) {
			if(!TextUtils.isEmpty(tableName)&&DatabaseConstant.TABLENAME_LOVE.equals(tableName)){
				notifyDataSetChanged();
			}
			ToastUtils.showShortToast(context, context.getResources()
					.getString(R.string.addSuccessful));
		}

		@Override
		public void isCollected() {
			
		ToastUtils.showToast(R.string.song_has_bean);
			
		}
	};
}
