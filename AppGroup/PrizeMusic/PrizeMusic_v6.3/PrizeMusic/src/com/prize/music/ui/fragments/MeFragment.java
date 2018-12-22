package com.prize.music.ui.fragments;

import static com.prize.music.Constants.MIME_TYPE;
import static com.prize.music.Constants.PLAYLIST_NAME;
import static com.prize.music.Constants.TYPE_PLAYLIST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.music.MusicWindowManager;
import com.prize.music.activities.EditActivity;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.activities.SearchBrowserActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.ToastUtils;
import com.prize.music.ui.adapters.list.MyGridViewAdapter;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.fragments.list.RecentlyAddedFragment;
import com.prize.music.ui.fragments.list.RecentlyPlayFragment;
import com.prize.music.views.SongListGridView;
import com.prize.music.R;

/**
 * 我的
 * 
 * @author Administrator
 *
 */
public class MeFragment extends Fragment implements OnClickListener,
		LoaderCallbacks<Cursor> {

	public GridView mGridView;
	private ListAdapter adapter;
	private SimpleCursorAdapter meAdapter;
	private int[] imageData = { R.drawable.icon_all_music,
			R.drawable.icon_recent_music, R.drawable.icon_recent_add };
	private View layoutView;
	private MainActivity mainActivity;
	/** 搜索 */
	private TextView search_Tv;
	//private GridView me_grideView;
	private SongListGridView me_grideView;
	/** 数据源 */
	ArrayList<HashMap<String, Object>> lstImageItem;

	private TextView head_edit; // 编辑按钮
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
	public ImageView mOnlineBtn;
	private boolean hasInstalledXiaMi = false;
	private RelativeLayout mOnLineRelayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		if (layoutView == null) {
			layoutView = inflater.inflate(R.layout.fragment_me_layout, null);
			findViewById();
			mGridView = (GridView) layoutView.findViewById(R.id.grideView);
			me_grideView = (SongListGridView) layoutView
					.findViewById(R.id.me_grideView);
			head_edit = (TextView) layoutView.findViewById(R.id.head_edit);
			mOnlineBtn = (ImageView) layoutView.findViewById(R.id.online_music_btn);
			mOnLineRelayout = (RelativeLayout) layoutView.findViewById(R.id.online_relayout_id);
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		getLoaderManager().initLoader(0, null, this);
		if (mainActivity != null) {
			hasInstalledXiaMi = MusicUtils.hasInstalledXiaMi(mainActivity);
			isShowXiaMiOnLine(hasInstalledXiaMi);
		}
		return layoutView;
	}

	private void findViewById() {
		search_Tv = (TextView) layoutView.findViewById(R.id.search_Tv);
	}

	private void setListener() {
		search_Tv.setOnClickListener(this);
	}

	private void init() {
		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		String[] data = { getString(R.string.all_music),
				getString(R.string.tab_recent_play),
				getString(R.string.tab_recent_add) };
		int length = data.length;
		for (int i = 0; i < length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemText", data[i]);
			map.put("ItemImage", imageData[i]);
			lstImageItem.add(map);
		}

		adapter = new SimpleAdapter(getActivity(), lstImageItem,
				R.layout.item_me_gride_layout, new String[] { "ItemImage",
						"ItemText" }, new int[] { R.id.action_bar_album_art,
						R.id.action_bar_album_name });
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(grideViewListener);

		head_edit.setOnClickListener(this);
		mOnlineBtn.setOnClickListener(this);
		setupFragmentData();
	}

	private OnItemClickListener meGrideItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FragmentTransaction ft = mainActivity.getSupportFragmentManager()
					.beginTransaction();
			if (position == 1) {
				initDialog();
			} else {
				Uri uri = ContentUris.withAppendedId(
						MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
				new_id = Long.parseLong(uri.getLastPathSegment());
				TextView name = (TextView) view.findViewById(R.id.edit_title);
				String itemName = name.getText().toString().trim();
				// ApolloUtils.startTracksBrowser2(mType, id, mCursor,
				// getActivity(),new_id);
				startTracksBrowser2(mType, id, mCursor, getActivity(), new_id,
						ft, itemName);
			}
		}
	};

	private OnItemClickListener grideViewListener = new OnItemClickListener() {
		private MusicLibraryFragment mMusicLibraryFragment;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FragmentManager manager = mainActivity.getSupportFragmentManager();
			FragmentTransaction ft = manager.beginTransaction();
			mMusicLibraryFragment = (MusicLibraryFragment) manager
					.findFragmentByTag(MusicLibraryFragment.class
							.getSimpleName());
			Bundle args = new Bundle();
			switch (position) {
			case 0:// All music
				if (mMusicLibraryFragment != null
						&& mMusicLibraryFragment.isAdded()) {
					ft.show(mMusicLibraryFragment);//
					ft.hide(MeFragment.this);
					// 只是切换显示和影藏而已，有数据变化时需要传参数进去更新数据，因为有些fragment是复用的
					ft.commitAllowingStateLoss();
					break;
				}

				ft.add(R.id.container_Fryt,
						mMusicLibraryFragment = new MusicLibraryFragment(),
						MusicLibraryFragment.class.getSimpleName());
				ft.hide(MeFragment.this);
				ft.commitAllowingStateLoss();

				break;
			case 1:// 最近播放
					// RecentlyPlayFragment
				RecentlyPlayFragment mRecentlyPlayFragment = new RecentlyPlayFragment();
				args.putString("flag",
						getActivity().getString(R.string.tab_recent_play));
				mRecentlyPlayFragment.setArguments(args);
				ft.add(R.id.container_Fryt, mRecentlyPlayFragment,
						RecentlyPlayFragment.class.getSimpleName());
				ft.hide(MeFragment.this);
				ft.addToBackStack(null);
				ft.commitAllowingStateLoss();

				break;
			case 2:// 最近添加
				RecentlyAddedFragment mRecentlyAddedFragment = new RecentlyAddedFragment();
				args.putString("flag",
						getActivity().getString(R.string.tab_recent_add));
				mRecentlyAddedFragment.setArguments(args);
				ft.add(R.id.container_Fryt, mRecentlyAddedFragment,
						RecentlyAddedFragment.class.getSimpleName());
				ft.hide(MeFragment.this);
				ft.addToBackStack(null);
				ft.commitAllowingStateLoss();
				break;

			default:
				break;
			}
		}

	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.search_Tv:
			Intent searchIntent = new Intent(mainActivity,
					SearchBrowserActivity.class);
			startActivity(searchIntent);
			break;
		case R.id.head_edit:
			ArrayList<String> myIds = new ArrayList<String>();
			for (int i = 0; i < me_grideView.getCount(); i++) {
				Cursor mCursor = (Cursor) meAdapter.getItem(i);
				if (mCursor == null) continue;
				String name = mCursor.getString(mCursor
						.getColumnIndexOrThrow(PlaylistsColumns.NAME));
				SharedPreferences sharedPreferences = getActivity()
						.getSharedPreferences("ShareXML", Context.MODE_PRIVATE);
				String loveName = sharedPreferences.getString("Favorites",
						getActivity().getString(R.string.my_love));
				String listName = sharedPreferences.getString("NewList",
						getActivity().getString(R.string.create_list));
				if (!name.equals(loveName) && !name.equals(listName)) {
					long mId = me_grideView.getItemIdAtPosition(i);
					myIds.add(mId + "");
				}
				// long mId = me_grideView.getItemIdAtPosition(i);
				// myIds.add(mId + "");
			}

			Intent intent2 = new Intent(getActivity(), EditActivity.class);
			// intent2.putExtra("tableName", DBContast.TABLENAME_LIST);
			intent2.putStringArrayListExtra("myIds", myIds);
			startActivity(intent2);
			break;
		case R.id.dia_sure:
			sure();
			break;
		case R.id.dia_neg:
			dissmissDialog();
			break;
		case R.id.online_music_btn:
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {		
			/*	ComponentName cn = new ComponentName("fm.xiami.main",
						"fm.xiami.bmamba.activity.StartActivity");   //系统内置的虾米
*/				ComponentName cn = new ComponentName("com.duomi.android",
						"com.duomi.android.DMLauncher");   //系统内置的多米
				intent.setComponent(cn);	
				startActivity(intent);
			} catch (Exception e) {
				if (hasInstalledXiaMi) {
					try {
						ComponentName cn2 = new ComponentName("com.duomi.android",
								"com.duomi.android.DMLauncher");   //第三方的多米
						intent.setComponent(cn2);
						startActivity(intent);
					} catch (Exception e2) {
						if (mainActivity != null) {
							ToastUtils.showOnceToast(mainActivity, mainActivity.getString(R.string.
									uninstall_duomi));
						}
					}
				} else {
					if (mainActivity != null) {
						ToastUtils.showOnceToast(mainActivity, mainActivity.getString(R.string.
								uninstall_duomi));
					}
				}	
			}
			
			break;
		}

	}

	private void initDialog() {
		createTbleDialog();
	}

	public void setupFragmentData() {
		SharedPreferences sharedPreferences = getActivity()
				.getSharedPreferences("ShareXML", Context.MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		if (isFirstRun) {
			editor.putBoolean("isFirstRun", false);
			MusicUtils.createPlaylist(getActivity(),
					mainActivity.getString(R.string.my_love));
			MusicUtils.createPlaylist(getActivity(),
					mainActivity.getString(R.string.create_list));
			editor.putString("Favorites",
					mainActivity.getString(R.string.my_love));
			editor.putString("NewList",
					mainActivity.getString(R.string.create_list));
			editor.commit();
		}
		meAdapter = new MyGridViewAdapter(getActivity(),
				R.layout.item_gride_layout, null, new String[] {},
				new int[] {}, 0);
		me_grideView.setAdapter(meAdapter);
		me_grideView.setOnItemClickListener(meGrideItemListener);
		mProjection = new String[] { BaseColumns._ID, PlaylistsColumns.NAME };
		mSortOrder = Audio.Playlists.DATE_ADDED;
		mUri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 5;
		mType = TYPE_PLAYLIST;
		mTitleColumn = PlaylistsColumns.NAME;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), mUri, mProjection, mWhere, null,
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
		//prize-public-bug:14382 music crash -pengcancan-20160413-start
		meAdapter.swapCursor(data);
		//prize-public-bug:14382 music crash -pengcancan-20160413-end
		me_grideView.invalidateViews();
		mCursor = data;
		if (data.getCount() > 2) {
			head_edit.setVisibility(View.VISIBLE);
		} else {
			head_edit.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//prize-public-bug:14382 music crash -pengcancan-20160413-start
		 if (meAdapter != null){ 
			 meAdapter.swapCursor(null);
		 }
		//prize-public-bug:14382 music crash -pengcancan-20160413-end
		 
	}

	public void startTracksBrowser2(String Type, long id, Cursor mCursor,
			Context context, Long new_id, FragmentTransaction ft, String name) {
		if (mCursor == null || mCursor.getColumnCount() == 0) return;
		Bundle bundle = new Bundle();
		if (Type == TYPE_PLAYLIST) {
			String playlistName = mCursor.getString(mCursor
					.getColumnIndexOrThrow(PlaylistsColumns.NAME));
			bundle.putString(MIME_TYPE, Audio.Playlists.CONTENT_TYPE);
			bundle.putString(PLAYLIST_NAME, playlistName);
			bundle.putLong(BaseColumns._ID, id);
		}

		bundle.putLong(BaseColumns._ID, id);
		bundle.putLong("new_id", new_id);
		bundle.putString("name", name);
		PlaylistListFragment listFragment = new PlaylistListFragment(bundle);
		ft.add(R.id.container_Fryt, listFragment,
				PlaylistListFragment.class.getSimpleName());
		ft.addToBackStack(null);
		ft.hide(MeFragment.this);
		ft.commitAllowingStateLoss();
	}

	public void createTbleDialog() {
		createDialog = new AlertDialog.Builder(mainActivity).create();
		LayoutInflater inflater = LayoutInflater.from(mainActivity);
		View view = inflater.inflate(R.layout.dialog_new_list, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Window mWindow = createDialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();
		lp.y = -105;// 设置竖直偏移量
		mWindow.setAttributes(lp);

		Button dia_neg = (Button) view.findViewById(R.id.dia_neg);
		Button dia_sure = (Button) view.findViewById(R.id.dia_sure);
		dia_edit = (EditText) view.findViewById(R.id.dia_edit);

		String defaultContent = mainActivity
				.getString(R.string.create_play_list) + getlistCount();
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
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}

	// 中文和英文环境下得到当前列表中列表最大结尾数字
	public int getlistCount() {
		List<Map<String, Object>> maps = MusicUtils.getTableList(getActivity());
		listCount = 1;
		for (int i = 0; i < maps.size(); i++) {
			String name = (String) maps.get(i).get("name");
			int nameLeng = name.length();
			String childName = null;
			if (nameLeng > 7) {
				childName = name.substring(0, name.length() - 2);
			} else {
				childName = name.substring(0, name.length() - 1);
			}
			if (childName.equals(mainActivity
					.getString(R.string.create_play_list))) {
				String childNumber = name.substring(6, name.length());
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

	public void sure() {
		String name = dia_edit.getText().toString().trim();
		try {
			if (!TextUtils.isEmpty(name)) {
				int id = idForplaylist(name);
				if (id >= 0) {
					MusicUtils.clearPlaylist(mainActivity, id);
					// MusicUtils.addToPlaylist(PlaylistDialog.this, mList, id);
				} else {
					long new_id = MusicUtils.createPlaylist(mainActivity, name);
					if (new_id != -1) {
						Intent intent = new Intent(mainActivity,
								NewListActivity.class);
						intent.putExtra("new_id", new_id);
						// intent.putStringArrayListExtra("mListData", null);
						mainActivity.startActivity(intent);
						FragmentActivity activity = (FragmentActivity) mainActivity;
						activity.overridePendingTransition(R.anim.in_from_left,
								R.anim.out_to_right);
						refresh();
					} else {
						Toast.makeText(mainActivity,
								mainActivity.getString(R.string.create_fail),
								Toast.LENGTH_LONG).show();
					}
				}
				dissmissDialog();
			} else {
				Toast.makeText(mainActivity,
						mainActivity.getString(R.string.name_cannot_be_empty),
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
			cursor = MusicUtils.query(mainActivity,
					Audio.Playlists.EXTERNAL_CONTENT_URI,
					new String[] { Audio.Playlists._ID }, Audio.Playlists.NAME
							+ "=?", new String[] { name }, Audio.Playlists.NAME, 0);
			
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					id = cursor.getInt(0);
				}
			} else {
				Toast.makeText(getActivity(),
						getActivity().getString(R.string.List_name_repetition),
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			if (cursor != null) {
				if(Integer.parseInt(Build.VERSION.SDK) < 10){
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
			InputMethodManager inputmanger = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(dia_edit.getWindowToken(), 0);
			createDialog.dismiss();
		}
	}
	
	private void isShowXiaMiOnLine(boolean hasInstalledXiaMi) {
		if (hasInstalledXiaMi) {
			mOnLineRelayout.setVisibility(View.VISIBLE);
		} else {
			mOnLineRelayout.setVisibility(View.GONE);
		}
	}
}
