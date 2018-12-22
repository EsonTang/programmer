package com.prize.music.ui.fragments.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.activities.LocalSongActivity;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.activities.SearchBrowserActivity;
import com.prize.music.activities.ToAlbumDetailActivity;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.database.SQLUtils;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.ShopRightPopAdapter;
import com.prize.music.ui.fragments.base.SongsListViewFragment.OnHeadlineSelectedListener;
import com.prize.music.views.ParabolaView;
import com.prize.onlinemusibean.PopBean;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 
 ** 我喜欢的和播放记录,歌单通用
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SongsLoveFragment extends Fragment implements
		IfragToActivityLister {
	private static final String TAG = "SongsLoveFragment";
	// Cursor mCursor;
	IfragToActivityLister mIfragToActivity;
	OnHeadlineSelectedListener mCallback;
	boolean isSelectMode = false;
	List<MusicInfo> mList = new ArrayList<MusicInfo>();
	SongsListInSongsLoveAdapter mAdapter;
	RelativeLayout none_data;
	RelativeLayout mDragHead;
	RelativeLayout title_container;
	TextView drag_mEdit_all;
	TextView item_select_Tv;
	TextView drag_mEdit_neg;
	ListView mListView;
	ImageView mActionBack;
	ImageView mActionSearch;
	ImageView mActionOverflow;
	TextView mTitle;

	TextView drog_new_songs;

	String mTableName;
	String mListName;

	LinearLayout mBottomActionLinearLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View root = inflater.inflate(R.layout.songs_love_fragment_layout,
				container, false);
		findViewById(root);

		init();
		setListener();
		return root;
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			mIfragToActivity = (IfragToActivityLister) activity;
			mCallback = (OnHeadlineSelectedListener) activity;
			mBottomActionLinearLayout = (LinearLayout) getActivity()
					.findViewById(R.id.main_bottom_layout);
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()
					+ "must implement  IfragToActivity");
		}
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateData();
		if (mAdapter != null) {
			mAdapter.setDownlaodRefreshHandle();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.removeDownLoadHandler();
		}
	}

	private void updateData() {
		// if (mCursor != null) {
		// mCursor.close();
		// mCursor = null;
		// }
		if (mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
			mList = SQLUtils.getInstance(getActivity())
					.queryHistory(mTableName);
		} else {
			mList = SQLUtils.getInstance(getActivity()).query(mTableName);
			Log.i(TAG, "mTableName : " + mTableName + ", mList : " + mList);
		}
		// mCursor = SQLUtils.getInstance(getActivity()).query(mTableName);
		setArrayList(mList);
	}
	
	private boolean isShow = false;

	private void setListener() {
		// TODO Auto-generated method stub
		mActionBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().finish();
			}
		});

		mActionSearch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent searchIntent = new Intent(getActivity(),
						SearchBrowserActivity.class);
				startActivity(searchIntent);
			}
		});

		mActionOverflow.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (rightPopupWindow != null) {
					if (isShow) {
						rightPopupWindow.dismiss();
						isShow = rightPopupWindow.isShowing();
					} else {
						rightPopupWindow.showAsDropDown(mActionOverflow);
						isShow = rightPopupWindow.isShowing();
					}
				}
			}
		});

		drog_new_songs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(), NewListActivity.class);
				intent.putExtra(LocalSongActivity.TABLE_NAME, mTableName);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.in_from_left,
						R.anim.out_to_right);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				if (!isSelectMode) {
					int selectCount = mAdapter.getSelectedAudioIds().size();
					mIfragToActivity.countNum(selectCount);
					setDragLayoutVisiable(true);
					mAdapter.setSelectMode(true);
					mAdapter.toggleCheckedState(arg2 - 1);
					mIfragToActivity.countNum(1);
					if (mCallback != null) {
						mCallback.onArticleSelected(-1);
					}
				}
				isSelectMode = true;
				return true;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//prize-public-bug:18520 monkey test ,music cause system freeze -pengcancan-0160725-start
				if (arg2 < 0 || arg2 > mAdapter.getCount()) {
					return;
				}
				//prize-public-bug:18520 monkey test ,music cause system freeze -pengcancan-0160725-end
				if (!isSelectMode) {
					try {
						MusicUtils.playMusic(
								getActivity(),
								(MusicInfo) (mAdapter.getItem(arg2
										- mListView.getHeaderViewsCount())),
								mTableName, mAdapter.getArrayList(),
								Constants.KEY_SONGS);
						mAdapter.notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 动画
					ImageView icon_fly = (ImageView) arg1
							.findViewById(R.id.icon_fly);
					if (parabolaView != null) {
						ImageView bottomView = null;
						if (getActivity() instanceof MainActivity) {
							bottomView = ((MainActivity) getActivity())
									.getBottomView();
						}
						if (getActivity() instanceof ToAlbumDetailActivity) {
							bottomView = ((ToAlbumDetailActivity) getActivity())
									.getBottomView();
						}
						if (getActivity() instanceof LocalSongActivity) {
							bottomView = ((LocalSongActivity) getActivity())
									.getBottomView();
						}
						parabolaView.setAnimationPara(icon_fly, bottomView);
						if (!parabolaView.isRunning()) {
							parabolaView.showMovie();
						}
					}

				} else {
					mAdapter.toggleCheckedState(arg2 - 1);
					int selectCount = mAdapter.getSelectedAudioIds().size();
					mIfragToActivity.countNum(selectCount);
					if (selectCount == mListView.getCount()
							- mListView.getHeaderViewsCount()) {
						mCallback.onArticleSelected(0);
						mAdapter.setIsSelectAll(true);
						drag_mEdit_all.setText(R.string.no_select);
					} else {
						mAdapter.setIsSelectAll(false);
						mCallback.onArticleSelected(1);
						drag_mEdit_all.setText(R.string.all_select);
					}
				}

			}
		});

		drag_mEdit_all.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				processAction(Constants.ACTION_FR_2_FR_SURE);
			}
		});

		drag_mEdit_neg.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				drag_mEdit_all.setText(getString(R.string.all_select));
				processAction(Constants.ACTION_CANCEL_FR_TO_FR);
			}
		});

	}

	private void setDragLayoutVisiable(boolean visiable) {
		if (visiable) {
			if (mBottomActionLinearLayout != null) {
				mBottomActionLinearLayout.setVisibility(View.VISIBLE);
			}
			mDragHead.setVisibility(View.VISIBLE);
			title_container.setVisibility(View.GONE);
			getActivity().findViewById(R.id.bottomactionbar_new).setVisibility(
					View.GONE);
		} else {
			if (mBottomActionLinearLayout != null) {
				mBottomActionLinearLayout.setVisibility(View.GONE);
			}
			mDragHead.setVisibility(View.GONE);
			title_container.setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.bottomactionbar_new).setVisibility(
					View.VISIBLE);
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		Bundle bundle = getArguments();
		mTableName = bundle.getString(LocalSongActivity.TABLE_NAME);
		mListName = bundle.getString(LocalSongActivity.LIST_NAME);
		if (mTableName == null && mListName == null) {
			getActivity().finish();
		}

		mTitle.setText(mListName);

		if (mTableName.equals(DatabaseConstant.TABLENAME_LOVE)
				|| mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
			mActionSearch.setVisibility(View.VISIBLE);
			mActionOverflow.setVisibility(View.GONE);
		} else {
			mActionSearch.setVisibility(View.GONE);
			mActionOverflow.setVisibility(View.VISIBLE);
		}

		mAdapter = new SongsListInSongsLoveAdapter(getActivity());
		mListView.setAdapter(mAdapter);

		View shuffle_temp = View.inflate(getActivity(),
				R.layout.songs_love_shuffle_all, null);

		mListView.addHeaderView(shuffle_temp);
		RelativeLayout shuffle = (RelativeLayout) shuffle_temp
				.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				if (isSelectMode) {
					return;
				}
				if (mAdapter.getArrayList() == null
						|| mAdapter.getArrayList().size() <= 0) {
					return;
				}
				int position = new Random().nextInt(mAdapter.getArrayList()
						.size());
				try {
					MusicUtils.mService
							.setRepeatMode(ApolloService.REPEAT_NONE);
					MusicUtils.playMusic(getActivity(), mAdapter.getArrayList()
							.get(position), mTableName,
							mAdapter.getArrayList(), Constants.KEY_SONGS);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

		areaDatas.add(new PopBean("1", getString(R.string.add_song), true));
		areaDatas.add(new PopBean("2", getString(R.string.rename), true));
		initPop();
	}

	private ArrayList<PopBean> areaDatas = new ArrayList<PopBean>();
	ListView mPopListView;
	ShopRightPopAdapter optionsAdapter;
	PopupWindow rightPopupWindow;
	private AlertDialog createDialog;
	private EditText dia_edit;

	/**
	 * @Description:[初始化popwindow]
	 */
	private void initPop() {
		Handler popWindowhandler = getPopWindowHandler();
		View loginwindow = (View) getActivity().getLayoutInflater().inflate(
				R.layout.popupwindow_options, null);
		mPopListView = (ListView) loginwindow.findViewById(R.id.pop_lv);
		optionsAdapter = new ShopRightPopAdapter(getActivity(),
				popWindowhandler, areaDatas);
		mPopListView.setAdapter(optionsAdapter);
		rightPopupWindow = new PopupWindow(getActivity());
		rightPopupWindow.setContentView(loginwindow);
		rightPopupWindow.setOutsideTouchable(true);
		// 必须设置BackgroundDrawable,不然setOutsideTouchable(true)无效
		// 这一句是为了实现弹出PopupWindow后，当点击屏幕其他部分及Back键时PopupWindow会消失，
		rightPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		rightPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
		rightPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		loginwindow.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				rightPopupWindow.dismiss();
				return true;
			}
		});
	}

	/**
	 * @Description:[popwindow的handler]
	 * @return
	 */
	private Handler getPopWindowHandler() {
		return new Handler() {
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				rightPopupWindow.dismiss();
				if (msg.what == 1) {
					int selIndex = data.getInt("selIndex");
					switch (selIndex) {
					case 1:// 添加歌曲
						Intent intent = new Intent(getActivity(),
								NewListActivity.class);
						intent.putExtra(LocalSongActivity.TABLE_NAME,
								mTableName);
						startActivity(intent);
						break;
					case 2:// 重命名
						createTbleDialog();
						break;
					}
				}
			}
		};
	}

	public void createTbleDialog() {
		createDialog = new AlertDialog.Builder(getActivity()).create();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View view = inflater.inflate(R.layout.dialog_new_list, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Window mWindow = createDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		lp.y = -105;// 设置竖直偏移量
		mWindow.setAttributes(lp);

		Button dia_neg = (Button) view.findViewById(R.id.dia_neg);
		Button dia_sure = (Button) view.findViewById(R.id.dia_sure);
		dia_edit = (EditText) view.findViewById(R.id.dia_edit);
		dia_edit.setText(mListName);
		dia_edit.selectAll();
		dia_neg.setOnClickListener(mClickListener);
		dia_sure.setOnClickListener(mClickListener);
		createDialog.setView(view);
		createDialog.setCanceledOnTouchOutside(false);
		createDialog.show();
		dia_edit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}

	private void dissmissDialog() {
		if (createDialog != null && createDialog.isShowing()) {
			InputMethodManager inputmanger = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(dia_edit.getWindowToken(), 0);
			createDialog.dismiss();
		}
	}

	OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.dia_neg:
				dissmissDialog();
				break;
			case R.id.dia_sure:
				String new_list_name = dia_edit.getText().toString().trim();
				if (TextUtils.isEmpty(new_list_name)) {
					ToastUtils.showToast(R.string.name_cannot_be_empty);
					return;
				} else if (new_list_name.equals(mTableName)) {
					dissmissDialog();
				} else {
					// 判断歌单名是否存在
					boolean isExit = MusicUtils.isPlayListNameExit(
							getActivity(), new_list_name);
					if (isExit
							|| new_list_name
									.equals(getString(R.string.new_play_list))) {
						ToastUtils.showToast(R.string.list_name_exit);
					} else {
						MusicUtils.reNamePlayListName(getActivity(), mListName,
								new_list_name);
						mListName = new_list_name;
						mTitle.setText(mListName);
						dissmissDialog();
						ToastUtils.showToast(R.string.rename_sucess);
					}
				}
				break;
			default:
				break;
			}
		}
	};

	// /**
	// *
	// * 获取所在歌单的全部歌曲信息
	// *
	// * @param cursor
	// * @param list
	// */
	// private void setArrayList(Cursor cursor, List<MusicInfo> list) {
	// // TODO Auto-generated method stub
	// if (list != null)
	// list.clear();
	// // List<MusicInfo> deleteList = new ArrayList<MusicInfo>();
	// if (cursor != null && cursor.getCount() > 0) {
	// mListView.setVisibility(View.VISIBLE);
	// none_data.setVisibility(View.GONE);
	// if (mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
	// int index_title = cursor
	// .getColumnIndex(HistoryColumns.AUDIO_TITLE);
	// int index_artist = cursor
	// .getColumnIndex(HistoryColumns.AUDIO_ARTIST);
	// int index_base_id = cursor
	// .getColumnIndex(HistoryColumns.HISTORY_BASE_ID);
	// int index_album_id = cursor
	// .getColumnIndex(HistoryColumns.AUDIO_ALUBM);
	// // .getColumnIndex(HistoryColumns.HISTORY_USER_ID);
	// int index_imageurl = cursor
	// .getColumnIndex(HistoryColumns.IMAGEURL);
	// int index_source_type = cursor
	// .getColumnIndex(HistoryColumns.HISTORY_SOURCE_TYPE);
	// while (cursor.moveToNext()) {
	// MusicInfo music_info = new MusicInfo();
	// music_info.songName = cursor.getString(index_title);
	// music_info.albumName = cursor.getString(index_album_id);
	// music_info.albumLogo = cursor.getString(index_imageurl);
	// music_info.singer = cursor.getString(index_artist);
	// music_info.songId = cursor.getLong(index_base_id);
	// music_info.userId = /* cursor.getString(index_user_id) */MusicUtils
	// .getUserId();
	// music_info.source_type = cursor
	// .getString(index_source_type);
	// list.add(music_info);
	// }
	// cursor.close();
	// Collections.reverse(list);
	// mAdapter.addData(list, false, mTableName);
	// } else {
	// int index_title = cursor
	// .getColumnIndex(DatabaseConstant.MEIDA_TITLE);
	// int index_artist = cursor
	// .getColumnIndex(DatabaseConstant.AUDIO_ARTIST);
	// int index_base_id = cursor
	// .getColumnIndex(DatabaseConstant.SONG_BASE_ID);
	// int index_album_id = cursor
	// .getColumnIndex(DatabaseConstant.MEIDA_ALBUM_NAME);
	// int index_logo_id = cursor
	// .getColumnIndex(DatabaseConstant.MEIDA_ALBUM_LOGO);
	// int index_user_id = cursor
	// .getColumnIndex(DatabaseConstant.SONG_USER_ID);
	// int index_source_type = cursor
	// .getColumnIndex(DatabaseConstant.SONG_SOURCE_TYPE);
	// while (cursor.moveToNext()) {
	// MusicInfo music_info = new MusicInfo();
	// music_info.songName = cursor.getString(index_title);
	// music_info.singer = cursor.getString(index_artist);
	// music_info.songId = cursor.getLong(index_base_id);
	// music_info.albumLogo = cursor.getString(index_logo_id);
	// music_info.albumName = cursor.getString(index_album_id);
	// music_info.userId = cursor.getString(index_user_id);
	// music_info.source_type = cursor
	// .getString(index_source_type);
	// // pengy 2016=-05-07 start
	// if (music_info.source_type
	// .equals(DatabaseConstant.LOCAL_TYPE)) {
	// // if
	// (DownloadHelper.isFileExists(MusicUtils.loacalMusicInfoToSongDetailInfo(music_info)))
	// {
	// // JLog.i(TAG, "歌曲存在"+music_info);
	// // list.add(music_info);
	// if (MusicUtils.isSongInSysData(music_info.songId)) {
	// JLog.i(TAG, "歌曲存在"+music_info);
	// list.add(music_info);
	//
	// } else {
	// deleteList.add(music_info);
	// JLog.i(TAG, "歌曲不存在"+music_info);
	// }
	// }else{
	// list.add(music_info);
	// }
	// }
	// cursor.close();
	//
	// if(list.size()<=0){
	// mListView.setVisibility(View.GONE);
	// none_data.setVisibility(View.VISIBLE);
	// }
	// mAdapter.addData(list, false, mTableName);
	// MusicUtils.removeMultiFromMyCollect(getContext(), deleteList,
	// null, mTableName);
	// // pengy 2016=-05-07 end
	// }
	// } else {
	// if (mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
	// mAdapter.addData(list, false, mTableName);
	// } else {
	// mListView.setVisibility(View.GONE);
	// none_data.setVisibility(View.VISIBLE);
	// }
	// }
	// }
	/**
	 * 
	 * 获取所在歌单的全部歌曲信息
	 * 
	 * @param cursor
	 * @param list
	 */
	private void setArrayList(List<MusicInfo> list) {
	  
		if (list != null && list.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			none_data.setVisibility(View.GONE);
			if (mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
				mAdapter.addData(list, false, mTableName);
			} else {
				if (list.size() <= 0) {
					mListView.setVisibility(View.GONE);
					none_data.setVisibility(View.VISIBLE);
				}
				mAdapter.addData(list, false, mTableName);
			}
		} else {
			if (mTableName.equals(DatabaseConstant.TABLENAME_HISTORY)) {
				mAdapter.addData(list, false, mTableName);
			} else {
				mListView.setVisibility(View.GONE);
				none_data.setVisibility(View.VISIBLE);
			}
		}
	}

	private ParabolaView parabolaView;

	private void findViewById(View root) {
		ViewGroup rootView = (ViewGroup) getActivity().getWindow()
				.getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
		title_container = (RelativeLayout) root
				.findViewById(R.id.title_container);
		mDragHead = (RelativeLayout) root
				.findViewById(R.id.drag_head_layout_edit);
		none_data = (RelativeLayout) root.findViewById(R.id.none_data);
		mListView = (ListView) root.findViewById(android.R.id.list);
		mActionBack = (ImageView) root.findViewById(R.id.action_back);
		mActionSearch = (ImageView) root.findViewById(R.id.action_search);
		mActionOverflow = (ImageView) root.findViewById(R.id.action_overflow);
		mTitle = (TextView) root.findViewById(R.id.title);
		drog_new_songs = (TextView) root.findViewById(R.id.drog_new_songs);
		drag_mEdit_all = (TextView) root.findViewById(R.id.drag_mEdit_all);
		item_select_Tv = (TextView) root.findViewById(R.id.item_select_Tv);
		drag_mEdit_neg = (TextView) root.findViewById(R.id.drag_mEdit_neg);
	}

	@Override
	public void countNum(int count) {
		// 在标题栏中间显示已选择多少项
		if (count == 0) {

		} else {

		}
	}

	@Override
	public void processAction(String action) {
		List<MusicInfo> list = mAdapter.getSelectedAudioIds();

		if (Constants.ACTION_BELL.equals(action)) {
			if (mAdapter == null) {
				return;
			}
			try {
				if (list.get(0).source_type.equals(DatabaseConstant.LOCAL_TYPE)) {
					MusicUtils.setRingtone(getContext(), list.get(0).songId);
				} else {
					SongDetailInfo song_info = MusicUtils
							.MusicInfoToSongDetailInfo(list.get(0));
					if (DownloadHelper.isFileExists(song_info)) { // 已下载
						long audio_id = MusicUtils.getAudioIdFromExitFile(
								getActivity(), song_info);
						MusicUtils.setRingtone(getActivity(), audio_id);
					} else {
						ToastUtils.showToast(R.string.can_not_set_as_ring);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		} else if (Constants.ACTION_SORT.equals(action)) {

			if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
				UiUtils.jumpToLoginActivity();
				return;
			}
			MusicUtils
					.addAllOnLineAndLocalMusicToCloundAndLocalTable(list,
							getActivity(), addCallback,
							DatabaseConstant.TABLENAME_LOVE);

			return;
		} else if (Constants.ACTION_DELETE.equals(action)) {
			if (mTableName.equals(DatabaseConstant.TABLENAME_LOVE)) {// 我喜欢的
																		// 先删除服务器端数据
				MusicUtils.deleteAllOnLineAndLocalMusicFromCloundAndLocalTable(
						list, getContext(), removeCallback, mTableName);
			} else {// 非 我喜欢的 只做本地删除
				MusicUtils.removeMultiFromMyCollect(getContext(), list,
						removeCallback, mTableName);
				mIfragToActivity.countNum(0);
			}
			
			// modify for bugID 17425  start
			setDragLayoutVisiable(false);
			isSelectMode = false;
			if (mAdapter != null) {
				mAdapter.setSelectMode(false);
				mAdapter.notifyDataSetChanged();
				mAdapter.selectAllItem(false);
			}
			// modify for bugID 17425  end

		} else if (Constants.ACTION_ADD.equals(action)) {

			MusicUtils
					.addAllMusicToTableDialog(getContext(), list, addCallback,null);

		} else if (Constants.ACTION_CANCEL_FR_TO_FR.equals(action)) {
			isSelectMode = false;
			if (mAdapter != null) {
				mAdapter.setSelectMode(false);
				mAdapter.notifyDataSetChanged();
				mAdapter.selectAllItem(false);
			}
			setDragLayoutVisiable(false);
		} else if (Constants.ACTION_FR_2_FR_SURE.equals(action)) {
			if (mAdapter == null) {
				return;
			}
			if (mAdapter.isSelectAll()) {
				mAdapter.selectAllItem(false);
				mCallback.onArticleSelected(1);
				drag_mEdit_all.setText(R.string.all_select);
			} else {
				mAdapter.selectAllItem(true);
				mCallback.onArticleSelected(0);
				drag_mEdit_all.setText(R.string.no_select);
			}
			mIfragToActivity.countNum(mAdapter.getSelectedAudioIds().size());
		}
	}

	AddCollectCallBack removeCallback = new AddCollectCallBack() {
		public void addCollectResult(boolean result, String Name) {
			mAdapter.selectAllItem(false);
			updateData();
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void isCollected() {

		}
	};

	AddCollectCallBack addCallback = new AddCollectCallBack() {
		public void addCollectResult(boolean result, String tableName) {
			mAdapter.notifyDataSetChanged();
			ToastUtils.showToast(R.string.addSuccessful);
		}

		@Override
		public void isCollected() {
			ToastUtils.showToast(R.string.song_has_bean);

		}
	};

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mMediaStatusReceiver);
	}

	/**
	 * 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (mListView != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	private UIDownLoadListener listener = null;

	class SongsListInSongsLoveAdapter extends BaseAdapter {
		private boolean isSelectAll = false;
		protected boolean isSelectMode = false;
		private int currentSelectItem = -1;

		protected AnimationDrawable mPeakTwoAnimation;
		Handler mHandler;
		Context context;
		LayoutInflater layoutinflater;
		List<MusicInfo> mList = new ArrayList<MusicInfo>();
		int left, top;
		private String table_name;

		public SongsListInSongsLoveAdapter(Context context) {
			this.context = context;
			layoutinflater = LayoutInflater.from(context);
			mHandler = new Handler();
			mCheckedStates = new SparseBooleanArray();
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

		/** 存储每个条目勾选的状态 */
		protected SparseBooleanArray mCheckedStates = null;

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mList.get(arg0);
		}

		public List<MusicInfo> getArrayList() {
			return mList;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		String mLineTwoText = null;

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			Holder holder = null;
			final MusicInfo musicBean = mList.get(arg0);
			JLog.i(TAG, musicBean.toString());
			if (arg1 == null) {
				holder = new Holder();
				arg1 = layoutinflater.inflate(
						R.layout.songs_in_love_recent_listview_item, null);
				holder.song_name = (TextView) arg1
						.findViewById(R.id.listview_item_line_one);
				holder.singer_name = (TextView) arg1
						.findViewById(R.id.listview_item_line_two);
				holder.peak_two = (ImageView) arg1.findViewById(R.id.peak_two);
				holder.peak_two.setImageResource(R.anim.peak_meter_orange);
				holder.peak_two.setVisibility(View.INVISIBLE);
				holder.checkbox = (CheckBox) arg1.findViewById(R.id.checkBox);
				holder.download = (ImageButton) arg1
						.findViewById(R.id.item_download);
				holder.more = (ImageButton) arg1.findViewById(R.id.more_item);
				holder.delete_song_Tv = (TextView) arg1
						.findViewById(R.id.delete_song_Tv);
				holder.add_Tv = (TextView) arg1.findViewById(R.id.add_Tv);
				holder.sort_Tv = (TextView) arg1.findViewById(R.id.sort_Tv);
				holder.share_Tv = (TextView) arg1.findViewById(R.id.share_Tv);
				holder.edit_Llyt = (LinearLayout) arg1
						.findViewById(R.id.edit_Llyt);
				holder.select_Llyt = (LinearLayout) arg1
						.findViewById(R.id.select_Llyt);
				holder.more_and_download_layout = (LinearLayout) arg1
						.findViewById(R.id.more_and_download_layout);
				holder.icon_fly = (ImageView) arg1.findViewById(R.id.icon_fly);
				arg1.setTag(holder);
			} else {
				holder = (Holder) arg1.getTag();
			}

			if (isSelectMode) {
				// holder.select_Llyt.setVisibility(View.VISIBLE);
				currentSelectItem = -1;
				holder.checkbox.setVisibility(View.VISIBLE);
				holder.more_and_download_layout.setVisibility(View.GONE);
			} else {
				// holder.checkbox.setVisibility(View.INVISIBLE);
				holder.checkbox.setVisibility(View.INVISIBLE);
				holder.more_and_download_layout.setVisibility(View.VISIBLE);
			}

			if (mCheckedStates.get(arg0)) {
				holder.checkbox.setChecked(true);
			} else {
				holder.checkbox.setChecked(false);
			}

			holder.song_name.setText(musicBean.songName);
			holder.singer_name.setText(musicBean.singer);

			if (currentSelectItem == arg0) {
				holder.edit_Llyt.setVisibility(View.VISIBLE);
			} else {
				holder.edit_Llyt.setVisibility(View.GONE);
			}

			// 更多
			holder.more.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (currentSelectItem == arg0) {
						currentSelectItem = -1;
					} else {
						currentSelectItem = arg0;
					}
					notifyDataSetChanged();
				}
			});
					
			
			//下载
			String sourceType=musicBean.source_type;
			if(!TextUtils.isEmpty(sourceType)&&sourceType.equals(DatabaseConstant.LOCAL_TYPE)){
				holder.download.setEnabled(false);
			} else {
				final SongDetailInfo bean = MusicUtils.MusicInfoToSongDetailInfo(musicBean);
				if (DownloadHelper.isFileExists(bean)) {
					holder.download.setEnabled(false);
				}else{
					holder.download.setEnabled(true);
				}
				holder.download.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						if (ClientInfo.networkType == ClientInfo.NONET) {
							ToastUtils.showToast(R.string.net_error);
							return;
						}
						DownLoadUtils.downloadMusic(bean);
						ToastUtils.showToast(R.string.add_download_queue_ok);
						// 下载完应该更新数据
					}
				});
			}

			// 删除
			holder.delete_song_Tv
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							if (table_name
									.equals(DatabaseConstant.TABLENAME_LOVE)) { // “我喜欢的”要先删除服务器端的数据
								MusicUtils.postAndCancelToMyLocalTableAndCloud(
										mList.get(arg0), context,
										RequestResCode.CANCEL,
										new AddCollectCallBack() {
											public void addCollectResult(
													boolean result, String name) {
												mList.remove(arg0);
												currentSelectItem = -1;
												notifyDataSetChanged();
												//我喜欢列表  更新
												updateData();
											}

											@Override
											public void isCollected() {

											}
										}, table_name);
							} else { // 非“我喜欢的”只需删除本地数据库表单数据
								MusicUtils.removeFromMyCollect(context,
										mList.get(arg0),
										new AddCollectCallBack() {
											public void addCollectResult(
													boolean result,
													String tableName) {
												mList.remove(arg0);
												currentSelectItem = -1;
												notifyDataSetChanged();
											}

											@Override
											public void isCollected() {

											}
										}, table_name);
							}
						}
					});

			// 添加
			holder.add_Tv.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					List<MusicInfo> list = new ArrayList<MusicInfo>();
					list.add(musicBean);
					MusicUtils.addAllMusicToTableDialog(context, list,
							new AddCollectCallBack() {

								@Override
								public void addCollectResult(boolean result,
										String tableName) {
									if (!TextUtils.isEmpty(tableName)
											&& DatabaseConstant.TABLENAME_LOVE
													.equals(tableName)) {
										notifyDataSetChanged();
									}
									ToastUtils
											.showToast(R.string.addSuccessful);
									notifyDataSetChanged();

								}

								@Override
								public void isCollected() {
									ToastUtils
											.showToast(R.string.Song_has_been);

								}

							},null);

				}
			});

			// 收藏
			final boolean isSorted = MusicUtils.isCollected(context,
					musicBean, DatabaseConstant.TABLENAME_LOVE);
			holder.sort_Tv.setSelected(isSorted);
			holder.sort_Tv.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
						UiUtils.jumpToLoginActivity();
						return;
					}

					String post_or_cancel = isSorted ? RequestResCode.CANCEL
							: RequestResCode.POST;
					MusicUtils.postAndCancelToMyLocalTableAndCloud(
							musicBean, context, post_or_cancel,
							new AddCollectCallBack() {
								public void addCollectResult(boolean result,
										String tableName) {
									if (result) {
										ToastUtils
												.showToast(R.string.sort_love_list_yet);
									} else {
										if (mTableName
												.equals(DatabaseConstant.TABLENAME_LOVE)) { // 如果当前列表是我喜欢的则删除这条数据
											mList.remove(arg0);
										}
										currentSelectItem = -1;
										ToastUtils
												.showToast(R.string.already_cancel_sort);
									}
									notifyDataSetChanged();
								}

								@Override
								public void isCollected() {

								}
							}, DatabaseConstant.TABLENAME_LOVE);
				}
			});

			// 分享
			holder.share_Tv.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					MusicUtils.doShare(context, Constants.KEY_SONGS,
							musicBean.singer, musicBean.songName,
							Integer.parseInt(musicBean.songId + ""));
				}
			});

			if (!TextUtils.isEmpty(sourceType)&&sourceType
					.equals(DatabaseConstant.ONLIEN_TYPE)) {
				holder.share_Tv.setEnabled(true);
			} else {
				holder.share_Tv.setEnabled(false);
			}

			if (MusicUtils.mService != null) {
				try {
					if (MusicUtils.getCurrentAudioId() == musicBean.songId) {
						holder.song_name.setTextColor(context.getResources()
								.getColor(R.color.gold_color));
						holder.singer_name.setTextColor(context.getResources()
								.getColor(R.color.gold_color));
						/*
						 * holder.peak_two.setVisibility(View.VISIBLE);
						 * mPeakTwoAnimation = (AnimationDrawable)
						 * holder.peak_two.getDrawable(); if(MusicUtils.mService
						 * != null && MusicUtils.mService.isPlaying()){
						 * mHandler.post(new Runnable() { public void run() {
						 * mPeakTwoAnimation.start(); } }); } else {
						 * mPeakTwoAnimation.stop(); } oldView =
						 * holder.peak_two;
						 */} else {
						holder.song_name.setTextColor(context.getResources()
								.getColor(R.color.gray_black));
						holder.singer_name.setTextColor(context.getResources()
								.getColor(R.color.text_color_gray));
						holder.peak_two.setVisibility(View.INVISIBLE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return arg1;
		}

		class Holder {
			TextView song_name;
			TextView singer_name;
			ImageView peak_two;
			CheckBox checkbox;
			ImageButton download;
			ImageButton more;
			TextView delete_song_Tv;
			TextView add_Tv;
			TextView sort_Tv;
			TextView share_Tv;
			LinearLayout edit_Llyt;
			LinearLayout select_Llyt;
			LinearLayout more_and_download_layout;
			ImageView icon_fly;
		}

		public void addData(List<MusicInfo> list, boolean add, String table_name) {
			this.table_name = table_name;
			if (add) {
				mList.addAll(list);
				notifyDataSetChanged();
			} else {
				mList.clear();
				mList.addAll(list);
				notifyDataSetChanged();
			}
		}

		public void play(View convertView) {
			Holder viewholder = (Holder) convertView.getTag();
			if (viewholder != null && viewholder.peak_two != null) {
				showAnim(viewholder.peak_two);
			}
		}

		private ImageView oldView;

		private void showAnim(ImageView newView) {
			if (newView == null || newView.equals(oldView))
				return;
			if (oldView != null)
				oldView.setVisibility(View.INVISIBLE);
			oldView = newView;
			oldView.setVisibility(View.VISIBLE);
			mPeakTwoAnimation = (AnimationDrawable) oldView.getDrawable();
			try {
				if (MusicUtils.mService != null
						&& MusicUtils.mService.isPlaying()) {
					mHandler.post(new Runnable() {
						public void run() {
							mPeakTwoAnimation.start();
						}
					});
				} else {
					mPeakTwoAnimation.stop();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		public boolean isSelectAll() {
			return isSelectAll;
		}

		public boolean isSelectMode() {
			return isSelectMode;
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

		public List<MusicInfo> getSelectedAudioIds() {
			int[] checkedPostions = getSelectedItemPositions();
			List<MusicInfo> array_list = new ArrayList<MusicInfo>();
			int len = checkedPostions.length;

			for (int i = 0; i < len; i++) {
				array_list.add((MusicInfo) (getItem(checkedPostions[i])));
			}

			return array_list;
		}

		public void setIsSelectAll(boolean selectAll) {
			isSelectAll = selectAll;
		}

		public void setSelectMode(boolean isSelectMode) {
			this.isSelectMode = isSelectMode;
		}

	}

}
