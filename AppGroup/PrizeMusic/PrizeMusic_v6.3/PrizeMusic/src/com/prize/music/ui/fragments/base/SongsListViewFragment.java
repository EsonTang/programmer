package com.prize.music.ui.fragments.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prize.music.Constants;
import com.prize.music.IfragToActivityLister;
import com.prize.music.bean.PopBean;
import com.prize.music.helpers.RefreshableFragment;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.ToastUtils;
import com.prize.music.history.HistoryDao;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.MainPopAdapter;
import com.prize.music.ui.adapters.base.ListViewAdapter;
import com.prize.music.ui.fragments.MeFragment;
import com.prize.music.ui.fragments.MusicLibraryFragment;
import com.prize.music.R;

/**
 * 全部音乐歌曲列表专用
 * 
 * @author Administrator
 * 
 */
public abstract class SongsListViewFragment extends RefreshableFragment
		implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnTouchListener, OnItemLongClickListener, IfragToActivityLister {
	// Adapter
	protected ListViewAdapter mAdapter;
	private IfragToActivityLister mIfragToActivity;
	// ListView
	protected ListView mListView;

	// Cursor
	protected Cursor mCursor;

	// Selected position
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
	private String title;
	private MusicLibraryFragment mMusicLibraryFragment;
	protected OnHeadlineSelectedListener mCallback;

	//
	// private TextView action_back;
	//
	// private TextView search_Tv;
	// protected ViewPager viewPager;

	// Bundle
	public SongsListViewFragment() {
	}

	public SongsListViewFragment(Bundle args) {
		setArguments(args);
	}

	/*
	 * To be overrode in child classes to setup fragment data
	 */
	public abstract void setupFragmentData();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupFragmentData();
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void refresh() {
		// The data need to be refreshed
		if (mListView != null) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	// private OnClickListener listener = new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// int key = v.getId();
	// switch (key) {
	// case R.id.action_back:
	// getActivity().getSupportFragmentManager().popBackStack();
	// break;
	// case R.id.action_search:
	// getActivity().onSearchRequested();
	// break;
	//
	// default:
	// break;
	// }
	//
	// }
	// };

	public void onViewCreated(View view, Bundle savedInstanceState) {

		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	};

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

		View root = inflater
				.inflate(R.layout.listview_layout, container, false);
		mListView = (ListView) root.findViewById(android.R.id.list);
		if (getArguments() != null
				&& getArguments().getSerializable("obj") != null) {
			mMusicLibraryFragment = (MusicLibraryFragment) getArguments()
					.getSerializable("obj");

		}
		setListener();

		return root;
	}

	private void setListener() {

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
		mAdapter.changeCursor(data);
		mListView.invalidateViews();
		mCursor = data;

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null)
			mAdapter.changeCursor(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		if (!isSelectMode) {
			MusicUtils.playAll(getActivity(), mCursor, position - 1);
		} else {
			mAdapter.toggleCheckedState(position - 1);
			int selectCount = mAdapter.getSelectedAudioIds().length;
			mIfragToActivity.countNum(selectCount);
			if (selectCount == mListView.getCount() - 1) {
				mCallback.onArticleSelected(0);
				mAdapter.setIsSelectAll(true);
			} else {
				mAdapter.setIsSelectAll(false);
				mCallback.onArticleSelected(1);
			}
		}
	}

	/**
	 * p 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mListView != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	/**
	 * 插入内存卡和拔出内存卡的监听
	 */
	private final BroadcastReceiver installSdardReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
					|| Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				// SD卡成功挂载
				if (mListView != null) { mAdapter.notifyDataSetChanged(); }

			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)
					|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
				// SD卡挂载失败
				if (mListView != null) { mAdapter.notifyDataSetChanged(); }
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);

		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addDataScheme("file");
		getActivity().registerReceiver(installSdardReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mMediaStatusReceiver);
		getActivity().unregisterReceiver(installSdardReceiver);
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			mIfragToActivity = (IfragToActivityLister) activity;
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()
					+ "must implement  IfragToActivity");
		}
		super.onAttach(activity);
	}

	@Override
	public void countNum(int count) {

	}

	@Override
	public void processAction(String action) {
		if (Constants.ACTION_BELL.equals(action)) {
			if (mAdapter == null) {
				return;
			}
			final long[] selectIds = mAdapter.getSelectedAudioIds();
			if (selectIds == null || selectIds.length <= 0) {
				return;
			}
			MusicUtils.setRingtone(getActivity(),
					mAdapter.getSelectedAudioIds()[0]);
			updateViews();
			if (mCallback != null) {
				mCallback.onArticleSelected(-2);
			}
			// 通知取消刷MainActivity新界面
			mIfragToActivity.processAction(Constants.ACTION_CANCE);
			return;
		} else if (Constants.ACTION_SORT.equals(action)) {
			final long[] selectIds = mAdapter.getSelectedAudioIds();
			if (selectIds == null || selectIds.length <= 0) {
				return;
			}
			new AsyncLoader_GuessInfo(Constants.ACTION_SORT, selectIds)
					.execute();
			return;
		} else if (Constants.ACTION_DELETE.equals(action)) {
			// if
			// (title.equals(getActivity().getString(R.string.tab_recent_add)))
			// {// 来自最近添加，则是删除本地文件
			final long[] selectIds = mAdapter.getSelectedAudioIds();
			if (selectIds == null || selectIds.length <= 0) {
				return;
			}
			df = com.prize.music.ui.fragments.base.PromptDialogFragment
					.newInstance("确定要删除" + selectIds.length + "首歌吗？",
							mDeletePromptListener);
			df.show(getActivity().getSupportFragmentManager(), "deleteDialog");
			// }
		} else if (Constants.ACTION_ADD.equals(action)) {
			final long[] selectIds = mAdapter.getSelectedAudioIds();
			if (selectIds == null || selectIds.length <= 0) {
				return;
			}
			initAddPopu();
		} else if (Constants.ACTION_CANCEL_FR_TO_FR.equals(action)) {
			isSelectMode = false;
			if (mAdapter != null) {
				mAdapter.setSelectMode(false);
				mAdapter.notifyDataSetChanged();
				mAdapter.selectAllItem(false);
			}

		} else if (Constants.ACTION_FR_2_FR_SURE.equals(action)) {
			if (mAdapter == null) {
				return;
			}
			if (mAdapter.isSelectAll()) {
				mAdapter.selectAllItem(false);
				mCallback.onArticleSelected(1);
			} else {
				mAdapter.selectAllItem(true);
				mCallback.onArticleSelected(0);
			}
			mIfragToActivity.countNum(mAdapter.getSelectedAudioIds().length);
		}
	}

	private PromptDialogFragment df;
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			new AsyncLoader_GuessInfo(Constants.ACTION_DELETE, null).execute();
		}

	};

	public boolean isSelectMode = false;

	/**
	 * 恢复原始状态
	 */
	private void updateViews() {
		isSelectMode = false;
		mAdapter.setSelectMode(false);
		mAdapter.selectAllItem(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (!isSelectMode) {
			int selectCount = mAdapter.getSelectedAudioIds().length;
			mIfragToActivity.countNum(selectCount);
			getActivity().findViewById(R.id.bottomactionbar_new).setVisibility(
					View.GONE);
			LinearLayout mLinearLayout = (LinearLayout) getActivity()
					.findViewById(R.id.main_bottom_layout);
			if (mLinearLayout != null) {
				mLinearLayout.setVisibility(View.VISIBLE);
				// mLinearLayout.startAnimation(animation);
			}

			mAdapter.setSelectMode(true);
			mAdapter.toggleCheckedState(position - 1);
			mIfragToActivity.countNum(1);
			if (mCallback != null) {
				mCallback.onArticleSelected(-1);
			}
		}
		isSelectMode = true;
		return true;
	}

	class AsyncLoader_GuessInfo extends AsyncTask<Void, Void, Boolean> {
		String action = null;
		ProgressDialog dialog = null;
		long[] ids = null;

		public AsyncLoader_GuessInfo(String action, long[] ids) {
			this.action = action;
			this.ids = ids;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_LIGHT);
			if (action.equals(Constants.ACTION_ADD)) {
				dialog.setMessage("正在添加，请稍后...");
			} else if (action.equals(Constants.ACTION_SORT)) {
				dialog.setMessage("正在收藏，请稍后...");
				if (mAdapter.getSelectedAudioIds().length > 100) {
					dialog.show();
				}
			} else if (action.equals(Constants.ACTION_DELETE)) {
				dialog.setMessage("正在删除，请稍后...");
				if (mAdapter.getSelectedAudioIds().length > 30) {
					dialog.show();
				}
			}
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isExisted = false;
			if (action.equals(Constants.ACTION_DELETE)) {
				long[] selectIds = mAdapter.getSelectedAudioIds();
				String[] paths = MusicUtils.getAudioPaths(getActivity(),
						selectIds);
				int len = selectIds.length;
				long currentId = MusicUtils.getCurrentAudioId();
				Arrays.sort(selectIds);
				for (int i = 0; i < len; i++) {
					long currenPositiontId = selectIds[i];
					MusicUtils.removeTrack(currenPositiontId);
					HistoryDao.getInstance(getActivity()).deleteByAudioId(
							currenPositiontId);
				}
				MusicUtils.removeTrackFromDatabase(getActivity()
						.getContentResolver(), selectIds);
				if (paths.length > 0) {
					boolean result = MusicUtils.deleteFiles(paths);
					if (result) {
						if ((Arrays.binarySearch(selectIds, currentId)) > 0) {
							try {
								MusicUtils.mService.stop();
								MusicUtils.mService.next();
								MusicUtils.mService.prev();
							} catch (RemoteException e) {
								
								e.printStackTrace();
							}
						}
					}
					return result;
				}
			}

			if (action.equals(Constants.ACTION_SORT)) {
				// MusicUtils.addToaddToFavorites(getActivity(), ids);
				long new_id = MusicUtils.getFavoritesId(getActivity());
				isExisted = MusicUtils.addTrackToPlaylist(getActivity(),
						new_id, ids);
				if (dialog.isShowing()) {
					SystemClock.sleep(500);
				}
				return isExisted;
			}
			return false;
		}

		@Override
		// 处理界面
		protected void onPostExecute(Boolean result) {
			if (mCallback != null) {
				mCallback.onArticleSelected(-2);
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			updateCoorentViews();

			if (action.equals(Constants.ACTION_ADD)) {
				ToastUtils.showOnceToast(getActivity(), getActivity()
						.getString(R.string.addSuccessful));
			} else if (action.equals(Constants.ACTION_SORT)) {
				if (result) {
					ToastUtils.showOnceToast(getActivity(), getActivity()
							.getString(R.string.Song_has_been));
				} else {
					ToastUtils.showOnceToast(getActivity(), getActivity()
							.getString(R.string.collectionSuccessful));
				}
				updateViews();
			} else if (action.equals(Constants.ACTION_DELETE)) {
				if (result) {
					ToastUtils.showOnceToast(getActivity(), getActivity()
							.getString(R.string.deleteSuccessful));
				} else {
					ToastUtils.showOnceToast(getActivity(), getActivity()
							.getString(R.string.deleteFail));

				}
			}
		}

	}

	private PopupWindow window;
	protected TextView popu_cancle;
	private ArrayList<PopBean> areaDatas = new ArrayList<PopBean>();

	private void initAddPopu() {

		if (areaDatas != null) {
			areaDatas.clear();

		}

		if (getActivity() == null) {
			return;
		}
		List<Map<String, Object>> mListss = MusicUtils
				.getTableList(getActivity());

		// List<Map<String, Object>> mListss = MeFragment.getTableList();
		// 初始化弹出菜单
		int len = mListss.size();
		// if (len <= 0) {
		for (int i = 0; i < len; i++) {
			if (!mListss.get(i).get("name")
					.equals(getActivity().getString(R.string.create_list))) {
				long id = (Long) mListss.get(i).get("id");
				String name = (String) mListss.get(i).get("name");
				PopBean mPopBean = new PopBean(id + "", name);
				areaDatas.add(mPopBean);
			}
		}
		// 初始化弹出菜单
		View popupView = LayoutInflater.from(getActivity()).inflate(
				R.layout.popupwindow_add_list, null);

		LinearLayout popu_add_linearlayout = (LinearLayout) popupView
				.findViewById(R.id.popu_add_linearlayout);
		popu_cancle = (TextView) popupView.findViewById(R.id.popu_cancle);
		if (window == null) {
			window = new PopupWindow(popupView,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.WRAP_CONTENT);
		}

		// 设置菜单背景，不设置背景菜单不会显示
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// 菜单外点击菜单自动消失
		window.setOutsideTouchable(true);
		// 初始化菜单上的按键，并设置监听
		ListView li = (ListView) popupView.findViewById(R.id.popul_list);
		MainPopAdapter pAdapter = new MainPopAdapter(getActivity(),
				getPopWindowHandler(), areaDatas);

		if (pAdapter != null && pAdapter.getCount() > 5) {
			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) popu_add_linearlayout
					.getLayoutParams(); // 取控件mGrid当前的布局参数
			linearParams.height = 600;// 当控件的高强制设成600象素
			popu_add_linearlayout.setLayoutParams(linearParams);
		}

		li.setAdapter(pAdapter);
		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		window.showAtLocation(mListView, Gravity.BOTTOM, 0, 0);
		popu_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				window.dismiss();

			}
		});
	}

	/**
	 * @Description:[popwindow的handler]
	 * @return
	 */
	private Handler getPopWindowHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				if (msg.what == 1) {
					long playlistid = data.getLong("selIndex");
					// String tabelName = data.getString("tabelName");
					boolean isExisted = MusicUtils.addTrackToPlaylist(
							getActivity(), playlistid,
							mAdapter.getSelectedAudioIds());
					if (isExisted) {
						ToastUtils.showOnceToast(getActivity(), "添加歌曲已存在");
					} else {
						ToastUtils.showOnceToast(getActivity(), "添加成功");

					}
				}
				window.dismiss();
				updateViews();
				if (mCallback != null) {
					mCallback.onArticleSelected(-2);
				}
				// 通知取消刷MainActivity新界面
				mIfragToActivity.processAction(Constants.ACTION_CANCE);
			}
		};
	}

	private void updateCoorentViews() {
		updateViews();
		// 通知取消刷MainActivity新界面
		mIfragToActivity.processAction(Constants.ACTION_CANCE);

		refresh();
	}

	public interface OnHeadlineSelectedListener {
		public void onArticleSelected(int position);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (window != null && window.isShowing()) {
				window.dismiss();
				return true;
			}
			updateViews();
			mCallback.onArticleSelected(-2);
		}
		return true;
	}

}
