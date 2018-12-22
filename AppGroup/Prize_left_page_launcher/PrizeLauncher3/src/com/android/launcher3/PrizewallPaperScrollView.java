package com.android.launcher3;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.android.launcher3.bean.Theme;
import com.android.launcher3.bean.Wallpaper;
import com.android.launcher3.lq.FindDefaultResoures;

public class PrizewallPaperScrollView extends PrizeScrollView implements
		View.OnClickListener {

	private Launcher mLauncher;
	private String mCurrentWallpaperId = null;

	private boolean defaultEffct = false;

	enum WALLAPPLYTYPE {
		ALL, DESK, LOCK
	};// add by zhouerlong

	WALLAPPLYTYPE mApplyType;

	// 静态变量
	// 数据库相关
	private static final Uri STORE_LOCAL_WALLPAPER_URI = Uri
			.parse("content://com.nqmobile.live.base.dataprovider" + "/"
					+ "wallpaper_local");
	private static final String THEMELOCALTABLE_WALLPAPER_ID = "wallpaperId";
	private static final String THEMELOCALTABLE_WALLPAPER_ICON_PATH = "iconPath";
	private static final String THEMELOCALTABLE_WALLPAPER_PATH = "path";
	private static final String THEMELOCALTABLE_WALLPAPER_NAME = "name";
	private static final String ThemeLocalTable_THEME_SELECT = "isSelected";
	public static String BROADCAST_INTENTFILTER_WALLPAPER_APPPLY = "com.live.store.wallpaper.apply";// 商店里点击主题应用会发这个广播
	private static HashMap wallpaperNameMap = new HashMap<>();

	/**
	 * 主题集合
	 */
	private List<Wallpaper> mWallItems;

	public static int THEME_MAX = 5;

	/**
	 * @author Administrator 加载主题类
	 */
	class LoadWallpaperTask extends
			AsyncTask<Wallpaper, Wallpaper, List<Wallpaper>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		

		@Override
		protected void onPostExecute(List<Wallpaper> result) {
			// setCurrentSelect();
			mWallItems.addAll(result);

			setDatas(mWallItems);
			updatePageCounts();
			requestLayout();
			updateCurrentThemeFromDb();
		}

		@Override
		protected List<Wallpaper> doInBackground(Wallpaper... params) {
			return loadStoreThemes();

		}

	}

	/**
	 * 加载固定的项 默认主题，更多（主题商店入口） 初始化适配器
	 * 
	 * @param context
	 * @param resource
	 */
	public void initWallPaper() {
		wallpaperNameMap = Launcher.wallpaperNames;
		mWallItems = new ArrayList<Wallpaper>();
		Wallpaper more = new Wallpaper();
		// more.thumb = this.getContext().getDrawable(R.drawable.more);
		more.wallpaperName = this.getContext().getString(R.string.more);
		more.id = "more";
		mWallItems.add(more);
		new LoadWallpaperTask().execute();

	}
	
	
	public void updateCurrentThemeFromDb() {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

			@Override
			public void run() {

				Cursor cursor = mLauncher.getContentResolver().query(
						STORE_LOCAL_WALLPAPER_URI, null,
						ThemeLocalTable_THEME_SELECT + "=?",
						new String[] { "1" }, null);
				try {
					if (cursor != null && cursor.moveToNext()) {
						mCurrentWallpaperId = cursor.getString(cursor
								.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ID));

					}
					if (cursor != null) {
						cursor.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}
	
	

	@Override
	protected boolean applyInfo(Object t, View icon) {
		// TODO Auto-generated method stub
		return false;
	}

	public PrizewallPaperScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PrizewallPaperScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		SharedPreferences sp = mContext.getSharedPreferences(
				"load_default_res_wall", Context.MODE_PRIVATE);
		boolean isloaded = sp.getBoolean("load_default_res_loaded_wall", false);
		if (isloaded) {
			initWallPaper();
		}
	}
	
	public void setDefultWallpaper(){
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				cv.put(ThemeLocalTable_THEME_SELECT, 1);
				mLauncher.getContentResolver().update(STORE_LOCAL_WALLPAPER_URI,
						cv, THEMELOCALTABLE_WALLPAPER_ID + "=?",
						new String[] {"wallpaper00"});
			}});
	}
	

	public PrizewallPaperScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private List<Wallpaper> loadStoreThemes() {
		List<Wallpaper> wallpapers = new ArrayList<>();
		Cursor cursor = mLauncher.getContentResolver().query(
				STORE_LOCAL_WALLPAPER_URI, null, null, null, null);
		while (cursor != null && cursor.moveToNext()) {
			Wallpaper wallpaper = new Wallpaper();
			wallpaper.setId(cursor.getString(cursor
					.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ID)));
			wallpaper.setIconPreviewPath(cursor.getString(cursor
					.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ICON_PATH)));
			wallpaper.setStrWallpaperPath(cursor.getString(cursor
					.getColumnIndex(THEMELOCALTABLE_WALLPAPER_PATH)));
			wallpaper.setWallpaperName(cursor.getString(cursor
					.getColumnIndex(THEMELOCALTABLE_WALLPAPER_NAME)));
			if (null != getWallpaperName(cursor.getString(cursor
					.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ID)))) {
				wallpaper
						.setWallpaperName(getWallpaperName(cursor.getString(cursor
								.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ID))));
			} else {
				wallpaper.setWallpaperName(cursor.getString(cursor
						.getColumnIndex(THEMELOCALTABLE_WALLPAPER_NAME)));
			}
			File file = new File(wallpaper.getStrWallpaperPath());
			if (file != null
					&& file.exists()
					&& ThemeListView
							.isDownloadCompleted(wallpaper.id, mContext)) {
				wallpapers.add(wallpaper);
			}

			if (file != null
					&& file.exists()
					&& file.getPath().contains(
							FindDefaultResoures.DEFALUT_WALLPAPER_PATH)) {
				wallpapers.add(wallpaper);
			}

		}
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
		}

		return wallpapers;
	}

	private String getWallpaperName(String ID) {

		String t = "CN;TW;US";
		String t1 = "CN;HK;US";
		String t2 = "CN;TW;GB";
		List<String> languages = Arrays.asList(t.split(";"));
		int i = languages.indexOf(Launcher.locale);
		if (i == -1) {
			languages = Arrays.asList(t1.split(";"));
			i = languages.indexOf(Launcher.locale);
		}
		if (i == -1) {
			languages = Arrays.asList(t2.split(";"));
			i = languages.indexOf(Launcher.locale);
		}
		if (i != -1) {
			Iterator iter = wallpaperNameMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				String value = (String) entry.getValue();
				if (null != value && key.equals(ID)
						&& value.split(";").length > 1) {
					return value.split(";")[i];
				}
			}
		}
		return null;
	}

	@Override
	protected void update(View view) {
		if (view != null) {
			Wallpaper info = (Wallpaper) view.getTag();
			if (info.id.equals("more")) {
				return;
			}
		}
		super.update(view);
	}

	public void selectWallpaper(final Wallpaper wallpaper, View view) {

		mCurrentWallpaperId = wallpaper.id;
		notifyWallPaperDb(wallpaper);
		Utilities.startSetWallpaperSevice(wallpaper.getStrWallpaperPath(),
				getContext(), false, mLauncher.getWallService());

	}

	public void updateWallFromId(String id) {
		List<View> icons = getAllIcons();
		for (View v : icons) {
			if (v.getTag() != null) {
				Theme tag = (Theme) v.getTag();
				if (tag.id.equals(id)) {
					update(v);
					return;
				}
			}
		}
	}

	private void notifyWallPaperDb(final Wallpaper wall) {

		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {// 更新主题商店本地数据库数据
				Cursor cursor = mLauncher.getContentResolver().query(
						STORE_LOCAL_WALLPAPER_URI, null,
						ThemeLocalTable_THEME_SELECT + "=?",
						new String[] { "1" }, null);
				if (cursor != null && cursor.moveToNext()) {
					String oldId = cursor.getString(cursor
							.getColumnIndex(THEMELOCALTABLE_WALLPAPER_ID));
					ContentValues cv = new ContentValues();
					cv.put(ThemeLocalTable_THEME_SELECT, 0);
					mLauncher.getContentResolver().update(
							STORE_LOCAL_WALLPAPER_URI, cv,
							THEMELOCALTABLE_WALLPAPER_ID + "=?",
							new String[] { oldId });
				}

				ContentValues cv = new ContentValues();
				cv.put(ThemeLocalTable_THEME_SELECT, 1);
				mLauncher.getContentResolver().update(
						STORE_LOCAL_WALLPAPER_URI, cv,
						THEMELOCALTABLE_WALLPAPER_ID + "=?",
						new String[] { wall.getId() });
				try {
					if (cursor != null) {
						cursor.close();
					}
				} catch (Exception e) {
				}
			}
		});

	}

	@Override
	protected View syncGetLayout(ViewGroup layout, Object t) {
		View icon = mInflate.inflate(R.layout.prize_theme_icon, layout, false);
		return icon;
	}

	@Override
	protected void onDataReady(int width, int height) {
		// TODO Auto-generated method stub
		super.onDataReady(width, height);
		defaultEffct = true;
		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// if (defaultEffct) {
						updateWallFromId(mCurrentWallpaperId);
						defaultEffct = false;
						// }
					}
				});

	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		Wallpaper info = (Wallpaper) view.getTag();
		if (info.id != null && info.id.equals("more")) {
			Intent i = new Intent();
			i.setClassName("com.nqmobile.live.base",
					"com.nqmobile.livesdk.commons.ui.StoreMainActivity");
			i.putExtra("fragment_index_to_show", 2);
			try {
				view.getContext().startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (mCurrentWallpaperId != null
					&& mCurrentWallpaperId.equals(info.id)) {
				view.setSelected(true);
				return;
			}
			mCurrentWallpaperId = info.id;
			selectWallpaper(info, view);
		}

	}
}
