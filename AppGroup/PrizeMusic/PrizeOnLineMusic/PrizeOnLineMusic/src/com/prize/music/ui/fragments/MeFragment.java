package com.prize.music.ui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.constants.Constants;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.ToastUtils;
import com.prize.music.EditSheetActivity;
import com.prize.music.R;
import com.prize.music.activities.DownLoadManagerActivity;
import com.prize.music.activities.LocalSongActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.ListInfo;
import com.prize.music.database.MusicInfo;
import com.prize.music.database.SQLUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.adapters.MeFragmentSongCountAdapter;
import com.prize.music.ui.adapters.MeFragmentSongSheetAdapter;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.fragments.list.RecentlyAddedFragment;
import com.prize.music.ui.fragments.list.SongsLoveFragment;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * "我的" 界面fragment
 * 
 * @author Administrator
 * 
 */
public class MeFragment extends Fragment implements OnClickListener,
		LoaderCallbacks<Cursor> {

	public GridView mGridView;
	private ListAdapter adapter;
	private MeFragmentSongCountAdapter mSongsCountAdapter;
	private SimpleCursorAdapter meAdapter;
	private MeFragmentSongSheetAdapter mSongSheetAdapter;
	private int[] imageData = { R.drawable.icon_online_all_sl,
			R.drawable.icon_online_recent_sl, R.drawable.icon_online_love_sl,
			R.drawable.icon_online_dwonload_sl /* , R.drawable.icon_recent_add */};
	private int[] data = new int[] { R.string.local_music,
			R.string.tab_recent_play, R.string.my_love, R.string.music_download /*
																				 * ,
																				 * getString
																				 * (
																				 * R
																				 * .
																				 * string
																				 * .
																				 * tab_recent_add
																				 * )
																				 */};;
	private View layoutView;
	private Context mContext;
	/** 搜索 */
	// private TextView search_Tv;
	private TextView mSetting;
	TextView mEditSheet;
	// private GridView me_grideView;
	private GridView me_grideView;
	/*** 歌单列表（本地新建和服务器收藏的） **/
	private GridView mSongSheetGridview;

	/** 数据源 */
	List<SongCount> lstImageItem;

	// private TextView head_edit; // 编辑按钮
	protected Cursor mCursor;
	protected int mSelectedPosition;

	// Used to set ringtone
	protected long mSelectedId;

	// Options
	protected final int PLAY_SELECTION = 0;

	protected final int USE_AS_RINGTONE = 1;

	protected final int ADD_TO_PLAYLIST = 2;

	protected final int SEARCH = 3;

	protected int mFragmentGroupId = 0;

	protected String mCurrentId, mSortOrder = null, mWhere = null,
			mType = null, mTitleColumn = null;

	protected String[] mProjection = null;

	protected Uri mUri = null;

	private long new_id;

	// 我喜欢的表的new_id
	public static long my_love_new_id;

	private EditText dia_edit;
	// 新建列表
	private AlertDialog createDialog;
	public int listCount = 1;
	// public ImageView mOnlineBtn;
	private boolean hasInstalledXiaMi = false;
	private MeFragmentPager mePager;

	// private RelativeLayout mOnLineRelayout;
	BroadcastReceiver mReceiver;
	private FragmentActivity activity;
	SQLUtils sqlutils;
	private BroadcastReceiver mReceivers;
	private Handler handler; /* PRIZE-nieligang add for bug15178 20160509 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = activity;
		mePager = new MeFragmentPager(activity);
		sqlutils = SQLUtils.getInstance(activity);
		if (layoutView == null) {
			layoutView = /* inflater.inflate(R.layout.fragment_me_layout, null); */mePager
					.getView();
			findViewById();
			mGridView = (GridView) layoutView.findViewById(R.id.grideView);
			me_grideView = (GridView) layoutView
					.findViewById(R.id.me_grideView);
			mSongSheetGridview = (GridView) layoutView
					.findViewById(R.id.song_sheet_gridview);
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		if (mContext != null) {
			hasInstalledXiaMi = MusicUtils.hasInstalledXiaMi(mContext);
			isShowXiaMiOnLine(hasInstalledXiaMi);
		}
		return layoutView;
	}

	/* PRIZE-nieligang add for bug15178 20160509 start */
	private UIDownLoadListener listener = new UIDownLoadListener() {
		protected void onFinish(int song_Id) {
			if (handler == null) {
				handler = new Handler();
			}
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					initSongsCount();
				}

			}, 500);
		}

		@Override
		public void onRefreshUI(int song_Id) {
		};
	};

	public void onAttach(android.app.Activity activity) {
	    this.activity = (FragmentActivity) activity;
		super.onAttach(activity);
	};

	/* PRIZE-nieligang add for bug15178 20160509 end */

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.REFLUSH_BROADCAST);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//prize-public-bug:21885 Fragment not attached to Activity-pengcancan-20160918-start
				if (intent.getAction().equals(Constants.REFLUSH_BROADCAST) && MeFragment.this.isAdded()) {
					setupFragmentData();
				}
				//prize-public-bug:21885 Fragment not attached to Activity-pengcancan-20160918-end
			}
		};
		LocalBroadcastManager.getInstance(activity).registerReceiver(mReceiver,
				filter);

		IntentFilter filters = new IntentFilter();
		filters.addAction(Constants.REFLUSH_SONGS_BROADCAST);

		mReceivers = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction()
						.equals(Constants.REFLUSH_SONGS_BROADCAST)) {
					initSongsCount();
				}
			}
		};
		LocalBroadcastManager.getInstance(activity).registerReceiver(
				mReceivers, filters);

	}

	private void findViewById() {
		mSetting = (TextView) layoutView.findViewById(R.id.setting);
		mEditSheet = (TextView) layoutView.findViewById(R.id.edit_sheet);

	}

	private void setListener() {
		mSetting.setOnClickListener(this);
		mEditSheet.setOnClickListener(this);
		AppManagerCenter.setDownloadRefreshHandle(listener); /*
															 * PRIZE-nieligang
															 * add for bug15178
															 * 20160509
															 */
	}

	private void init() {

		lstImageItem = new ArrayList<SongCount>();

		mSongsCountAdapter = new MeFragmentSongCountAdapter(activity,
				R.layout.item_me_gride_layout);
		mGridView.setAdapter(mSongsCountAdapter);
		mGridView.setOnItemClickListener(grideViewListener);

		mSongSheetAdapter = new MeFragmentSongSheetAdapter(activity,
				R.layout.item_sheet_layout);
		mSongSheetGridview.setAdapter(mSongSheetAdapter);
		mSongSheetGridview
				.setOnItemClickListener(mSongSheetGridviewItemListener);

	}

	/**
	 * @author lixing
	 * @see 歌单列表结尾增加一个点击按钮，
	 */
	private void addCreateNewListButton() {
		List<ListInfo> lists = new ArrayList<ListInfo>();
		ListInfo list_info = new ListInfo(getString(R.string.new_play_list),
				getString(R.string.new_play_list), ListInfo.DEFAULT_LOCAL_LIST_ID, DatabaseConstant.LOCAL_TYPE, ListInfo.DEFALUT_LOCAL_SOURCE_ONLINE_TYPE, MusicUtils.getUserId());
		lists.add(list_info);
		if(mSongSheetAdapter.getCount() > 0){
			String list_name = ((ListInfo)(mSongSheetAdapter.getItem(mSongSheetAdapter.getCount() - 1))).menuName;
			if(!list_name.equals(getString(R.string.new_play_list))){
				mSongSheetAdapter.addList(lists, true);
			}
		} else {
			mSongSheetAdapter.addList(lists, true);
		}
	}

	private OnItemClickListener mSongSheetGridviewItemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position == mSongSheetAdapter.getCount() - 1) {
				createTbleDialog();
			} else {
				ListInfo list_info = (ListInfo) mSongSheetAdapter
						.getItem(position);
				String table_name = list_info.list_table_name;
				if (!TextUtils.isEmpty(list_info.source_type)&&list_info.source_type.equals(DatabaseConstant.LOCAL_TYPE)) { // 本地歌单
					Bundle bundle = new Bundle();
					Intent intent = new Intent(mContext,
							LocalSongActivity.class);
					intent.putExtra(LocalSongActivity.FROM,
							MeFragment.class.getSimpleName());
					intent.putExtra(LocalSongActivity.TO,
							SongsLoveFragment.class.getSimpleName());
					bundle.putString(LocalSongActivity.TABLE_NAME, table_name);
					bundle.putString(LocalSongActivity.LIST_NAME,
							list_info.menuName);
					intent.putExtras(bundle);
					startActivity(intent);
				} else { // 在线歌单
					int list_id = (int) list_info.menuId;
					String type = list_info.menuType;
					UiUtils.gotoMoreDailyFromSortMenu(getActivity(), list_id, type,"MeFragment");
				}
			}

		}
	};

	private OnItemClickListener meGrideItemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FragmentTransaction ft = ((FragmentActivity) mContext)
					.getSupportFragmentManager().beginTransaction();
			if (position == 1) {
				initDialog();
			} else {
				Uri uri = ContentUris.withAppendedId(
						MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
				new_id = Long.parseLong(uri.getLastPathSegment());
				TextView name = (TextView) view.findViewById(R.id.edit_title);
				String itemName = name.getText().toString().trim();
				startTracksBrowser2(mType, id, mCursor, activity, new_id, ft,
						itemName);
			}
		}
	};

	private OnItemClickListener grideViewListener = new OnItemClickListener() {
		private LocalMusicLibraryFragment mMusicLibraryFragment;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FragmentManager manager = ((FragmentActivity) mContext)
					.getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			mMusicLibraryFragment = (LocalMusicLibraryFragment) manager
					.findFragmentByTag(LocalMusicLibraryFragment.class
							.getSimpleName());
			Bundle bundle = new Bundle();
			Intent intent = new Intent(mContext, LocalSongActivity.class);
			switch (position) {
			case 0:// All music 本地歌曲
				intent.putExtra(LocalSongActivity.FROM,
						MeFragment.class.getSimpleName());
				intent.putExtra(LocalSongActivity.TO,
						LocalMusicLibraryFragment.class.getSimpleName());
				break;
			case 1:// 最近播放
				intent.putExtra(LocalSongActivity.FROM,
						MeFragment.class.getSimpleName());
				intent.putExtra(LocalSongActivity.TO,
						SongsLoveFragment.class.getSimpleName());
				bundle.putString(LocalSongActivity.TABLE_NAME,
						DatabaseConstant.TABLENAME_HISTORY);
				bundle.putString(LocalSongActivity.LIST_NAME,
						getString(R.string.tab_recent_play));
				intent.putExtras(bundle);
				break;
			case 2:// 我喜欢的
				intent.putExtra(LocalSongActivity.FROM,
						MeFragment.class.getSimpleName());
				intent.putExtra(LocalSongActivity.TO,
						SongsLoveFragment.class.getSimpleName());
				bundle.putString(LocalSongActivity.TABLE_NAME,
						DatabaseConstant.TABLENAME_LOVE);
				bundle.putString(LocalSongActivity.LIST_NAME,
						getString(R.string.my_love));
				intent.putExtras(bundle);
				break;
			case 3: // 歌曲下载
				Intent downIntent = new Intent(mContext,
						DownLoadManagerActivity.class);
				mContext.startActivity(downIntent);
				return;
			case 4:// 最近添加
				intent.putExtra(LocalSongActivity.FROM,
						MeFragment.class.getSimpleName());
				intent.putExtra(LocalSongActivity.TO,
						RecentlyAddedFragment.class.getSimpleName());
				break;

			default:
				break;
			}
			mContext.startActivity(intent);
		}

	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.dia_sure:
			String name = dia_edit.getText().toString().trim();
			name = MusicUtils.deleteSpaceInString(name);
			if (!TextUtils.isEmpty(name)) {
				if (MusicUtils.isPlayListNameExit(activity, name)
						|| name.equals(getString(R.string.new_play_list))) {
					ToastUtils.showToast(R.string.list_name_exit);
				} 
				else {
					ListInfo list_info = makePlayList(name, addPlayListCallback);
					dissmissDialog();
					Intent intent = new Intent(mContext, NewListActivity.class);
					intent.putExtra(LocalSongActivity.TABLE_NAME,
							list_info.list_table_name);
					mContext.startActivity(intent);
				}
			}else{
				ToastUtils.showToast(R.string.list_name_empty);
			}				
			break;
		case R.id.dia_neg:
			dissmissDialog();
			break;
		case R.id.setting:
			UiUtils.goToSettingtActivity(mContext);
			break;
		case R.id.edit_sheet:
			if (mSongSheetAdapter == null || mSongSheetAdapter.getCount() <= 0) {
				return;
			}
			Intent intent2 = new Intent(activity, EditSheetActivity.class);
			intent2.putExtra("tableName", DatabaseConstant.TABLENAME_LIST);
			startActivity(intent2);
			break;
		}
	}

	/**
	 * @see 添加播放列表后的回调
	 */
	private MusicUtils.AddCollectCallBack addPlayListCallback = new MusicUtils.AddCollectCallBack() {
		@Override
		public void addCollectResult(boolean result, String tableName) {
			setupFragmentData();
			// FragmentActivity activity = (FragmentActivity) mainActivity;
			// activity.overridePendingTransition(R.anim.in_from_left,
			// R.anim.out_to_right);
		}

		@Override
		public void isCollected() {

		}
	};

	private void initDialog() {
		createTbleDialog();
	}

	public void setupFragmentData() {

		List<ListInfo> arraylist = SQLUtils.getInstance(activity).queryMenu();
		mSongSheetAdapter.addList(arraylist, false);
		if (arraylist != null && arraylist.size() > 0) {
			mEditSheet.setVisibility(View.VISIBLE);
		} else {
			mEditSheet.setVisibility(View.GONE);
		}
		addCreateNewListButton();
		setGridViewHeightBasedOnChildren(mSongSheetGridview);

		initSongsCount();

	}

	/**
	 * @author lixing
	 * @see 用于显示本地歌曲数量,最近播放歌曲数量，我喜欢的歌曲数量，正在下载的歌曲的数量
	 */
	private void initSongsCount() {
		// TODO Auto-generated method stub
		List<SongCount> songcount_list = new ArrayList<SongCount>();
		for (int i = 0; i < data.length; i++) {
			SongCount song_count = new SongCount();
			song_count.title_id = data[i];
			song_count.img_id = imageData[i];
			song_count.song_count = 0;
			songcount_list.add(song_count);
		}
		// pengy reflush count for mylove start
		List<MusicInfo> lista = sqlutils.query(DatabaseConstant.TABLENAME_LOVE);
		if (lista == null) {
			songcount_list.get(2).song_count = 0;
		} else {
			songcount_list.get(2).song_count = lista.size();
		}
		
		ArrayList<SongDetailInfo> list = GameDAO.getInstance()
				.getDownLoadedAppList();
		songcount_list.get(3).song_count = list.size();
        
		// modify by pengy for 18155 start 2016.07.05
		List<MusicInfo> listb = sqlutils.queryHistory(DatabaseConstant.TABLENAME_HISTORY);
		if (listb == null) {
			songcount_list.get(1).song_count = 0;
		} else {
			songcount_list.get(1).song_count = listb.size();
		}
//		Cursor cursor_history = sqlutils
//				.queryCursor(DatabaseConstant.TABLENAME_HISTORY);
//		try {
//			if (cursor_history == null||cursor_history.isClosed()) {
//				songcount_list.get(1).song_count = 0;
//			} else {
//				songcount_list.get(1).song_count = cursor_history.getCount();
//				cursor_history.close();
//			}
//		} catch (Exception e) {
//		}finally{
//			if(cursor_history !=null){
//				cursor_history.close();
//			}
//		}
		// modify by pengy for 18155 start  2016.07.05
		
		//所有本地歌曲
		String[] mProjection = new String[] { MediaColumns.TITLE };
//	String[] mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
//				AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		String mWhere = where.toString();
		String mSortOrder = MediaColumns.TITLE;
		Uri mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		if(activity==null){
			return;
		}
		Cursor cursor_all = activity.getContentResolver().query(mUri,
				mProjection, mWhere, null, mSortOrder);
		try {
			if (cursor_all == null || cursor_all.getCount() <= 0) {
				songcount_list.get(0).song_count = 0;
			} else {
				songcount_list.get(0).song_count = cursor_all.getCount();
				cursor_all.close();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(cursor_all !=null){
				cursor_all.close();
			}
		}
		lstImageItem.clear();
		lstImageItem.addAll(songcount_list);
		songcount_list.clear();
		songcount_list = null;
		mSongsCountAdapter.addList(lstImageItem, false);
		mSongsCountAdapter.notifyDataSetChanged();

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(activity, mUri, mProjection, mWhere, null,
				mSortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Check for database errors
		if (data == null) {
			return;
		}
		if (mCursor != null)
			mCursor.close();
		meAdapter.changeCursor(data);
		me_grideView.invalidateViews();
		// seGridViewHeightBasedOnChildren(me_grideView);

		mCursor = data;
		if (data.getCount() > 2) {
			// head_edit.setVisibility(View.VISIBLE);
		} else {
			// head_edit.setVisibility(View.GONE);
		}
	}

	/**
	 * @author lixing
	 * @see 修改gridview 高度， 完全显示gridview 里的信息
	 * @param view
	 */
	public void setGridViewHeightBasedOnChildren(GridView view) {
		ListAdapter listAdapter = view.getAdapter();
		if (listAdapter == null || listAdapter.getCount() <= 0) {
			return;
		}
		int totalHeight = 0;
		int i = listAdapter.getCount() % 3 == 0 ? listAdapter.getCount() / 3
				: listAdapter.getCount() / 3 + 1;
		View item = listAdapter.getView(0, null, view);
		item.measure(0, 0);
		int itmeHeight = item.getMeasuredHeight();
		totalHeight = itmeHeight * i;

		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = totalHeight;
		view.setLayoutParams(params);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * if (mAdapter != null) mAdapter.changeCursor(null);
		 */
	}

	public void startTracksBrowser2(String Type, long id, Cursor mCursor,
			Context context, Long new_id, FragmentTransaction ft, String name) {
		if (mCursor == null || mCursor.getColumnCount() == 0)
			return;
		Bundle bundle = new Bundle();
		if (Type == Constants.TYPE_PLAYLIST) {
			String playlistName = mCursor.getString(mCursor
					.getColumnIndexOrThrow(PlaylistsColumns.NAME));
			bundle.putString(Constants.MIME_TYPE, Audio.Playlists.CONTENT_TYPE);
			bundle.putString(Constants.PLAYLIST_NAME, playlistName);
			bundle.putLong(BaseColumns._ID, id);
		}

		bundle.putLong(BaseColumns._ID, id);
		bundle.putLong("new_id", new_id);
		bundle.putString("name", name);
		PlaylistListFragment listFragment = new PlaylistListFragment(bundle);
		ft.add(R.id.MainFragment_container, listFragment,
				PlaylistListFragment.class.getSimpleName());
		ft.addToBackStack(null);
		ft.hide(MeFragment.this);
		ft.commitAllowingStateLoss();
	}

	public void createTbleDialog() {
		createDialog = new AlertDialog.Builder(mContext).create();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.dialog_new_list, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Window mWindow = createDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		lp.y = -105;// 设置竖直偏移量
		mWindow.setAttributes(lp);

		Button dia_neg = (Button) view.findViewById(R.id.dia_neg);
		Button dia_sure = (Button) view.findViewById(R.id.dia_sure);
		dia_edit = (EditText) view.findViewById(R.id.dia_edit);

		String defaultContent = mContext.getString(R.string.play_list)
				+ getlistCount();
		dia_edit.setText(defaultContent);
		dia_edit.setSelection(defaultContent.length());
		dia_edit.selectAll();
		dia_neg.setOnClickListener(this);
		dia_sure.setOnClickListener(this);
		createDialog.setView(view);
		createDialog.setCanceledOnTouchOutside(false);
		createDialog.show();
		dia_edit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}

	// 中文和英文环境下得到当前列表中列表最大结尾数字
	public int getlistCount() {
		// List<Map<String, Object>> maps =
		// MusicUtils.getTableList(activity);
		List<String> names = MusicUtils.getAllListName(activity);
		listCount = 1;
		for (int i = 0; i < names.size(); i++) {
			String name = (String) names.get(i);
			int nameLeng = name.length();
			String childName = null;
			if (nameLeng > 5) {
				childName = name.substring(0, name.length() - 2);
			} else {
				childName = name.substring(0, name.length() - 1);
			}
			if (childName.equals(mContext.getString(R.string.play_list))) {
				String childNumber = name.substring(4, name.length());
				if (isNum(childNumber)) {
					int maxNumber = Integer.parseInt(childNumber);
					listCount = listCount > maxNumber ? listCount
							: maxNumber + 1;
				}
			}
		}
		return listCount;
	}

	// 判断输入的表名最后一个字符是否为数字
	public static boolean isNum(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	/**
	 * @author lixing
	 * @see 创建本地播放表单
	 * @param sheet_name
	 * @param callback
	 */
	public ListInfo makePlayList(String sheet_name,
			MusicUtils.AddCollectCallBack callback) {
		ListInfo list = new ListInfo();
		list.menuName = sheet_name;
		list.menuId = ListInfo.DEFAULT_LOCAL_LIST_ID;
		list.list_table_name = MusicUtils.crateNewListTableName(activity,
				list.menuName);
		list.list_user_id = MusicUtils.getUserId();
		list.source_type = DatabaseConstant.LOCAL_TYPE;
		MusicUtils.addPlayListAndCreateTable(activity, list, callback);
		return list;
	}

	public void sure() {
		String name = dia_edit.getText().toString().trim();
		try {
			if (!TextUtils.isEmpty(name)) {
				int id = idForplaylist(name);
				if (id >= 0) {
					MusicUtils.clearPlaylist(mContext, id);
					// MusicUtils.addToPlaylist(PlaylistDialog.this, mList, id);
				} else {
					long new_id = MusicUtils.createPlaylist(mContext, name);
					if (new_id != -1) {
						Intent intent = new Intent(mContext,
								NewListActivity.class);
						intent.putExtra("new_id", new_id);
						// intent.putStringArrayListExtra("mListData", null);
						mContext.startActivity(intent);
						FragmentActivity activity = (FragmentActivity) mContext;
						activity.overridePendingTransition(R.anim.in_from_left,
								R.anim.out_to_right);
						refresh();
					} else {
						Toast.makeText(mContext,
								mContext.getString(R.string.create_fail),
								Toast.LENGTH_LONG).show();
					}
				}
				dissmissDialog();
			} else {
				Toast.makeText(mContext,
						mContext.getString(R.string.name_cannot_be_empty),
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 判断列表名是否重复
	private int idForplaylist(String name) {
		Cursor cursor = null;
		int id = -1;
		try {
			cursor = MusicUtils.query(mContext,
					Audio.Playlists.EXTERNAL_CONTENT_URI,
					new String[] { Audio.Playlists._ID }, Audio.Playlists.NAME
							+ "=?", new String[] { name },
					Audio.Playlists.NAME, 0);

			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					id = cursor.getInt(0);
				}
			} else {
				Toast.makeText(activity,
						activity.getString(R.string.List_name_repetition),
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (cursor != null) {
				if (Integer.parseInt(Build.VERSION.SDK) < 10) {
					cursor.close();
				}
			}
		}
		return id;
	}

	public void refresh() {
		// The data need to be refreshed
		if (me_grideView != null) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	private void dissmissDialog() {
		if (createDialog != null && createDialog.isShowing()) {
			InputMethodManager inputmanger = (InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(dia_edit.getWindowToken(), 0);
			createDialog.dismiss();
		}
	}

	private void isShowXiaMiOnLine(boolean hasInstalledXiaMi) {
		if (hasInstalledXiaMi) {
			// mOnLineRelayout.setVisibility(View.GONE);
			// } else {
			// mOnLineRelayout.setVisibility(View.GONE);
		}
	}

	public class SongCount {
		public SongCount() {

		}

		public int img_id;
		public int title_id;
		public int song_count;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mePager != null) {
			mePager.onResume();
		}

		setupFragmentData();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mePager != null) {
			mePager.onPause();
		}
		LocalBroadcastManager.getInstance(activity).unregisterReceiver(
				mReceiver);
		LocalBroadcastManager.getInstance(activity).unregisterReceiver(
				mReceivers);
	}

	/* PRIZE-nieligang add for bug15178 20160509 start */
	@Override
	public void onDestroy() {
		AppManagerCenter.removeDownloadRefreshHandle(listener);
		activity = null;
		super.onDestroy();
	}

	/* PRIZE-nieligang add for bug15178 20160509 end */

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			setupFragmentData();
			if (mePager != null) {
				mePager.onResume();
			}
		} else {
			if (mePager != null) {
				mePager.onResume();
			}
		}
	}

}
