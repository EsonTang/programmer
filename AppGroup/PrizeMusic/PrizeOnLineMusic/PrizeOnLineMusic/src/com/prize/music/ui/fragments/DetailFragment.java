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

package com.prize.music.ui.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestResCode;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.ToAlbumDetailActivity;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.ListInfo;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.fragments.DetailListFragment.RequestBack;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.AlbumDetailResponse;
import com.prize.onlinemusibean.response.CollectDetailResponse;
import com.xiami.sdk.utils.ImageUtil;

public class DetailFragment extends Fragment implements OnClickListener,
		RequestBack, OnTouchListener {
	private final String TAG = "DetailFragment";
	private FragmentActivity mainActivity;
	private View layoutView;
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private DetailListFragment mFragment;
	private TextView action_back;
	// private ImageView action_search;
	private ImageView img_Iv;
	private ImageView sort_Tv;
	private TextView numsong_Tv;
	private String id;
	private String key;
	private String where;
	private TextView play_count_Tv;
	private AlbumDetailResponse mAlbumDetailResponse;
	private CollectDetailResponse mCollectDetailResponse;
	private TextView all_downLoad_Tv;
	private TextView batch_select_Tv;
	private TextView share_Tv;
//	private ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
	private ArrayList<SongDetailInfo> canListerList = new ArrayList<SongDetailInfo>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

			id = getArguments().getString(Constants.KEY);
			key = getArguments().getString(Constants.TYPE);
			where = getArguments().getString(Constants.WHERE);
		}
	}

	/**
	 * 解决事件穿透问题
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	/**
	 * 解决fragment事件穿透问题
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = getActivity();
		if (layoutView == null) {
			layoutView = inflater
					.inflate(R.layout.fragment_detail_layout, null);
			StateBarUtils.changeOriginalStatus(mainActivity.getWindow());
			findViewById();
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		return layoutView;
	}

	protected void findViewById() {
		mViewPager = (ViewPager) layoutView
				.findViewById(R.id.id_stickynavlayout_viewpager);
		action_back = (TextView) layoutView.findViewById(R.id.action_back);
		// action_search = (ImageView)
		// layoutView.findViewById(R.id.action_search);
		numsong_Tv = (TextView) layoutView.findViewById(R.id.numsong_Tv);
		play_count_Tv = (TextView) layoutView.findViewById(R.id.play_count_Tv);
		img_Iv = (ImageView) layoutView.findViewById(R.id.img_Iv);
		sort_Tv = (ImageView) layoutView.findViewById(R.id.sort_love_Tv);
		batch_select_Tv = (TextView) layoutView
				.findViewById(R.id.batch_select_Tv);
		all_downLoad_Tv = (TextView) layoutView
				.findViewById(R.id.all_downLoad_Tv);
		layoutView.findViewById(R.id.all_downLoad_Tv);
		share_Tv = (TextView) layoutView.findViewById(R.id.share_Tv);
	}

	protected void setListener() {
		action_back.setOnClickListener(this);
		// action_search.setOnClickListener(this);
		sort_Tv.setOnClickListener(this);
		all_downLoad_Tv.setOnClickListener(this);
		batch_select_Tv.setOnClickListener(this);
		share_Tv.setOnClickListener(this);
		mFragment.setBack(this);
		layoutView.findViewById(R.id.share_Tv).setOnClickListener(this);
	}

	protected void init() {
		mFragment = new DetailListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY, id);
		bundle.putString(Constants.TYPE, key);
		bundle.putString(Constants.WHERE, where);
		mFragment.setArguments(bundle);
		mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
			@Override
			public Fragment getItem(int arg0) {
				return mFragment;
			}

			@Override
			public int getCount() {
				return 1;
			}

		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(0);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.action_back:
			DetailFragment.this.getFragmentManager().popBackStack();
			break;
		case R.id.share_Tv:
			if(ClientInfo.networkType==ClientInfo.NONET){
				ToastUtils.showToast(R.string.net_error_share);
				return;
			}
			if(canListerList.size()<=0){
				ToastUtils.showToast(R.string.nodata_to_operator);
				return;
			}
			switch (key) {
			case Constants.KEY_ALBUM:
				if (mAlbumDetailResponse == null)
					return;
				MusicUtils.doShare(getActivity(), Constants.KEY_ALBUM,
						mAlbumDetailResponse.artist_name,
						mAlbumDetailResponse.album_name,
						mAlbumDetailResponse.album_id);
				break;
			case Constants.KEY_COLLECT:
				if (mCollectDetailResponse == null)
					return;
				MusicUtils.doShare(getActivity(), Constants.KEY_COLLECT,
						mCollectDetailResponse.user_name,
						mCollectDetailResponse.collect_name,
						mCollectDetailResponse.list_id);
				break;
			}
			break;

		case R.id.batch_select_Tv:
			UiUtils.goToBatchEidtActivity(mainActivity, canListerList);
			break;
		case R.id.all_downLoad_Tv:
			if(ClientInfo.networkType==ClientInfo.NONET){
				ToastUtils.showToast(R.string.net_error);
				return;
			}
			if(canListerList.size()<=0){
				ToastUtils.showToast(R.string.nodata_to_operator);
				return;
			}
			boolean hasDownload = false;
			ArrayList<SongDetailInfo> list =new ArrayList<SongDetailInfo>();
			for (SongDetailInfo songDetailInfo : canListerList) {
				if (!DownloadHelper.isFileExists(songDetailInfo)) {
					hasDownload = true;
					list.add(songDetailInfo);
				}
			}
			if (!hasDownload) {
				ToastUtils.showToast(R.string.no_downloadTask);
			} else {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				DownLoadUtils.downloadMultMusic(list);
				ToastUtils.showToast(R.string.add_download_queue_ok);

			}
			// ToastUtils.showToast(R.string.filter_cannot_down);
			break;
		// 收藏歌单
		case R.id.sort_love_Tv:
			// DailyDetailFragment.this.getFragmentManager().popBackStack();
			// Toast.makeText(getActivity(), "click sort love button",
			// Toast.LENGTH_LONG).show(); mAlbumDetailResponse
			if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
				UiUtils.jumpToLoginActivity();
				return;
			}
			ListInfo list_info = new ListInfo();
			if (mAlbumDetailResponse != null) {
				list_info.source_type = DatabaseConstant.ONLIEN_TYPE;
				list_info.menuName = mAlbumDetailResponse.album_name;
				list_info.list_table_name = mAlbumDetailResponse.album_name;
				list_info.menuId = mAlbumDetailResponse.album_id;
				list_info.menuImageUrl = mAlbumDetailResponse.album_logo;
				list_info.list_user_id = MusicUtils.getUserId();
				list_info.menuType = Constants.KEY_ALBUM;

			} else if (mCollectDetailResponse != null) {
				list_info.source_type = DatabaseConstant.ONLIEN_TYPE;
				list_info.menuName = mCollectDetailResponse.collect_name;
				list_info.list_table_name = mCollectDetailResponse.collect_name;
				list_info.menuId = mCollectDetailResponse.list_id;
				list_info.menuImageUrl = mCollectDetailResponse.collect_logo;
				list_info.list_user_id = MusicUtils.getUserId();
				list_info.menuType = Constants.KEY_COLLECT;
			}
			String post_or_cancel = MusicUtils.isPlayListNameExit(getActivity(),
					list_info.menuName) ? RequestResCode.CANCEL
					: RequestResCode.POST;
			MusicUtils.sortMenu2Server(list_info, getActivity(),
					post_or_cancel, mSortCallBack);

			break;
		case R.id.action_search:
			UiUtils.goToSearchtActivity(mainActivity);
			break;
		}
	}

	AddCollectCallBack mSortCallBack = new AddCollectCallBack() {
		public void addCollectResult(boolean result,String tableName) {
			if (result) {
				sort_Tv.setImageResource(R.drawable.audioplayer_icon_favourite_nomal_red);
				ToastUtils.showToast(R.string.collectionSuccessful);
			} else {
				sort_Tv.setImageResource(R.drawable.icon_detail_love_nomal);
				ToastUtils.showToast(R.string.already_cancel_sort);
				Intent intent = new Intent();
				intent.setAction(Constants.REFLUSH_BROADCAST);
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
			}
		}

		@Override
		public void isCollected() {
			
			
		}
	};

	@Override
	public void onDestroy() {
		StateBarUtils.changeStatus(mainActivity.getWindow());
		if (mFragment != null) {
			mFragment.setBack(null);
		}
		if (mainActivity instanceof ToAlbumDetailActivity) {
			mainActivity.finish();
		}
		super.onDestroy();
	}

	public static DetailFragment newInstance(String id, String key) {
		DetailFragment tabFragment = new DetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY, id);
		bundle.putString(Constants.TYPE, key);
		tabFragment.setArguments(bundle);
		return tabFragment;
	}
	public static DetailFragment newInstance(String id, String key,String where) {
		DetailFragment tabFragment = new DetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY, id);
		bundle.putString(Constants.TYPE, key);
		bundle.putString(Constants.WHERE, where);
		tabFragment.setArguments(bundle);
		return tabFragment;
	}

	@Override
	public void back(Object res,ArrayList<SongDetailInfo> list) {
		if (res != null && getActivity() != null) {
			if (res instanceof CollectDetailResponse) {
				mCollectDetailResponse = (CollectDetailResponse) res;
				if (mCollectDetailResponse != null) {
					if (!TextUtils.isEmpty(mCollectDetailResponse.collect_name)) {
						action_back
								.setText(mCollectDetailResponse.collect_name);
					}
					String url = TextUtils
							.isEmpty(mCollectDetailResponse.collect_logo) ? mCollectDetailResponse.author_avatar
							: mCollectDetailResponse.collect_logo;
					if (!TextUtils.isEmpty(url)) {
						JLog.i(TAG, ImageUtil.transferImgUrl(url, 640));
						ImageLoader.getInstance().displayImage(
								ImageUtil.transferImgUrl(url, 640), img_Iv,
								UILimageUtil.getDetaiHeadImgUILoptions());
					}
					play_count_Tv.setText(getActivity().getString(
							R.string.number_play,
							mCollectDetailResponse.play_count));// number_songs

					if (MusicUtils.isPlayListNameExit(getActivity(),
							mCollectDetailResponse.collect_name)) {
						sort_Tv.setImageResource(R.drawable.icon_detail_love_press);// sort_love_Tv
					} else {
						sort_Tv.setImageResource(R.drawable.icon_detail_love_nomal);
					}

				}

				canListerList = list;
//				canListerList = CommonUtils.filterUnabelListerSong(list);
				numsong_Tv.setText(getActivity().getString(
						R.string.number_songs,
						canListerList.size()));
				return;
			}
			if (res instanceof AlbumDetailResponse) {
				mAlbumDetailResponse = (AlbumDetailResponse) res;
				if (mAlbumDetailResponse != null) {
					if (!TextUtils.isEmpty(mAlbumDetailResponse.album_name)) {
						action_back.setText(mAlbumDetailResponse.album_name);
					}
					String url = TextUtils
							.isEmpty(mAlbumDetailResponse.album_logo) ? mAlbumDetailResponse.artist_logo
							: mAlbumDetailResponse.album_logo;
					if (!TextUtils.isEmpty(url)) {
						JLog.i(TAG, ImageUtil.transferImgUrl(url, 640));
						ImageLoader.getInstance().displayImage(
								ImageUtil.transferImgUrl(url, 640), img_Iv,
								UILimageUtil.getDetaiHeadImgUILoptions());
					}
					play_count_Tv.setVisibility(View.GONE);

					if (MusicUtils.isPlayListTableNameExit(getActivity(),
							mAlbumDetailResponse.album_name)) {
						sort_Tv.setImageResource(R.drawable.icon_detail_love_press);
					} else {
						sort_Tv.setImageResource(R.drawable.icon_detail_love_nomal);
					}

				}
//				list = mAlbumDetailResponse.songs;
				canListerList =list;
//				canListerList = CommonUtils.filterUnabelListerSong(list);
				numsong_Tv.setText(getActivity().getString(
						R.string.number_songs,
						canListerList.size()));
			}

		}
	}

}
