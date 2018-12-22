package com.android.launcher3;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.bean.Theme;
import com.android.launcher3.lq.DefaultConfig;
import com.android.launcher3.lq.FindDefaultResoures;
import com.android.launcher3.notify.PreferencesManager;

public class PrizeThemeScrollView extends PrizeScrollView {

	private boolean defaultEffct = false;

	// 静态变量
	// 数据库相关
	private static final Uri STORE_LOCAL_THEME_URI = Uri
			.parse("content://com.nqmobile.live.base.dataprovider" + "/"
					+ "theme_local");
	private static final String ThemeLocalTable_THEME_ID = "themeId";// 主题id
	private static final String ThemeLocalTable_THEME_ICON_PATH = "iconPath";
	private static final String ThemeLocalTable_THEME_PATH = "themePath";
	private static final String ThemeLocalTable_THEME_NAME = "name";
	private static final String ThemeLocalTable_THEME_SELECT = "isSelected";
	private String mCurrentThemeId = null;

	private static HashMap themeNameMap = new HashMap<>();

	private static final Uri TABLE_URI = Uri
			.parse("content://com.nqmobile.live.base.dataprovider" + "/"
					+ "download");
	public static final String DOWNLOAD_RES_ID = "resId";
	public static final String _ID = "_id";
	public static final String DOWNLOAD_IS_FINISH = "is_finish";
	public static final int DOWNLOAD_FINISH = 1;
	private static final int i = 0;

	public static String BROADCAST_INTENTFILTER = "lqstore_download_compelete";// 商店的主题或者壁纸下载完成会发这个广播
	public static String BROADCAST_INTENTFILTER_DELETE = "lqstore_delete_compelete";// 商店的主题或者壁纸下载完成会发这个广播

	public static String KEY_RESOURCE_PREVIEW = "key_resource_preview";// 预览图
	public static String KEY_RESOURCE_NAME = "key_resource_name";// 资源名字
	public static String KEY_RESOURCE_PATH = "key_resource_path";// 资源路径
	public static String KEY_RESOURCE_ID = "key_resource_id";// 资源唯一id
	public static String KEY_RESOURCE_TYPE = "key_resource_type";// 资源类型是主题还是壁纸int类型
																	// 0：主题
	HorizontalListView mHorizontalListView; // ，1：壁纸

	/**
	 * 主题集合
	 */
	private List<Theme> mThemeItems;
	/**
	 * 加载主题任务类
	 */
	LoadThemeTask task = new LoadThemeTask();

	private Launcher mLauncher;

	public static int THEME_MAX = 5;

	/**
	 * 加载固定的项 默认主题，更多（主题商店入口） 初始化适配器
	 * 
	 * @param context
	 * @param resource
	 */
	public void initTheme() {
		// setDefultTheme();
		themeNameMap = Launcher.themeNames;
		mThemeItems = new ArrayList<Theme>();
		Theme more = new Theme();
		// more.thumb = this.getContext().getDrawable(R.drawable.more);
		more.themeName = mLauncher.getString(R.string.more);
		more.id = "more";
		mThemeItems.add(more);

		Theme easy_launcher = new Theme();
		easy_launcher.themeName = mLauncher.getString(R.string.easy_launcher);
		easy_launcher.id = "easy";
		boolean easy_item = Utilities.supportEasyLauncher();
		if (easy_item) {
			// mThemeItems.add(easy_launcher);
		}
		new LoadThemeTask().execute();

	}

	@Override
	protected void update(View view) {
		if (view != null) {
			Theme info = (Theme) view.getTag();
			if (info.id.equals("more")) {
				return;
			}
		}
		super.update(view);
	}

	/**
	 * @author Administrator 加载主题类
	 */
	class LoadThemeTask extends AsyncTask<Theme, Theme, List<Theme>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(List<Theme> result) {
			if(result.size()<=0)  {
				mThemeItems.clear();
				mLauncher.commit(true);
				mLauncher.loadWallAndTHeme();
				return;
			}
			mThemeItems.addAll(result);

			setDatas(mThemeItems);
			updatePageCounts();
			requestLayout();

			DisplayMetrics dm = new DisplayMetrics();
			mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);
			int w = (int) (dm.widthPixels / 4.2);
			updateCurrentThemeFromDb();

			if (Utilities.supportTestTheme()) {

				mLauncher.getworkspace().snapToPage(0);
				postDelayed(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(mLauncher, "所有主题图片截取", Toast.LENGTH_LONG)
								.show();
						// updateTheme(mThemeItems.get(1));
					}
				}, 10 * 1000);

			}
		}

		@Override
		protected List<Theme> doInBackground(Theme... params) {
			return loadStoreThemes();

		}

	}

	public PrizeThemeScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mLauncher = (Launcher) context;
		SharedPreferences sp = mContext.getSharedPreferences(
				"load_default_res", Context.MODE_PRIVATE);
		boolean isloaded = sp.getBoolean("load_default_res_loaded", false);
		if (isloaded) {
			initTheme();
		}
	}

	public PrizeThemeScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		SharedPreferences sp = mContext.getSharedPreferences(
				"load_default_res", Context.MODE_PRIVATE);
		boolean isloaded = sp.getBoolean("load_default_res_loaded", false);
		if (isloaded) {
			initTheme();
		}
	}

	public PrizeThemeScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void updateCurrentThemeFromDb() {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				Cursor cursor = mLauncher.getContentResolver().query(
						STORE_LOCAL_THEME_URI, null,
						ThemeLocalTable_THEME_SELECT + "=?",
						new String[] { "1" }, null);
				if (cursor != null && cursor.moveToNext()) {
					mCurrentThemeId = cursor.getString(cursor
							.getColumnIndex(ThemeLocalTable_THEME_ID));

				}
			}

		});
	}

/*	public static  String getCurrrentThemePath(Context c) {
		String path = null;
		Cursor cursor = c.getContentResolver().query(
				Uri.parse("content://com.nqmobile.live.base.dataprovider" + "/"
						+ "theme_local"), null, "isSelected" + "=?",
				new String[] { "1" }, null);
		if (cursor != null && cursor.moveToNext()) {
			path = cursor.getString(cursor.getColumnIndex("themeId"));

		}

		return path;
	}*/

	@Override
	protected void onDataReady(int width, int height) {
		// TODO Auto-generated method stub
		super.onDataReady(width, height);
		defaultEffct = true;
		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
//						if (defaultEffct) {
							updateTHemeFromId(mCurrentThemeId);
							defaultEffct = false;
//						}
					}
				});

	}

	private String getThemeName(String ID) {

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
			Iterator iter = themeNameMap.entrySet().iterator();
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

	
	public void setDefultTheme() {
		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				cv.put(ThemeLocalTable_THEME_SELECT, 1);
			int id=	mLauncher.getContentResolver().update(STORE_LOCAL_THEME_URI,
						cv, ThemeLocalTable_THEME_ID + "=?",
						new String[] { Utilities.getdefaultThemeId()});
			LogUtils.i("zhouerlong", "mCurrentThemeId::"+id+" themeId:"+Utilities.getdefaultThemeId());
			}
		});
	}

	private List<Theme> loadStoreThemes() {
		List<Theme> themes = new ArrayList<>();
		Cursor cursor = mLauncher.getContentResolver().query(
				STORE_LOCAL_THEME_URI, null, null, null, null);
		while (cursor != null && cursor.moveToNext()) {
			Theme theme = new Theme();
			theme.setId(cursor.getString(cursor
					.getColumnIndex(ThemeLocalTable_THEME_ID)));
			theme.setIconPreviewPath(cursor.getString(cursor
					.getColumnIndex(ThemeLocalTable_THEME_ICON_PATH)));
			theme.setStrThemePath(cursor.getString(cursor
					.getColumnIndex(ThemeLocalTable_THEME_PATH)));
			if (null != getThemeName(cursor.getString(cursor
					.getColumnIndex(ThemeLocalTable_THEME_ID)))) {
				theme.setThemeName(getThemeName(cursor.getString(cursor
						.getColumnIndex(ThemeLocalTable_THEME_ID))));
			} else {
				theme.setThemeName(cursor.getString(cursor
						.getColumnIndex(ThemeLocalTable_THEME_NAME)));
			}
			if (theme.getStrThemePath() == null) {
				continue;
			}
			File file = new File(theme.getStrThemePath());

			if (file != null && file.exists()
					&& isDownloadCompleted(theme.id, mContext)) {
				// themes.add(theme);
			}

			if (file != null
					&& file.exists()
					&& file.getPath().contains(
							FindDefaultResoures.DEFALUT_THEME_PATH)) {
				themes.add(theme);
			}
		}
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
		}
		return themes;
	}

	public static boolean isDownloadCompleted(String resId, Context c) {
		if (TextUtils.isEmpty(resId)) {
			return false;
		}
		boolean result = false;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = c.getContentResolver();
			cursor = contentResolver.query(TABLE_URI, null, DOWNLOAD_RES_ID
					+ " = ?", new String[] { resId }, _ID
					+ " DESC LIMIT 1 OFFSET 0");
			if (cursor != null && cursor.moveToNext()) {
				Long finish = cursor.getLong(cursor
						.getColumnIndex(DOWNLOAD_IS_FINISH));
				if (finish == DOWNLOAD_FINISH) {
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	@Override
	protected boolean applyInfo(Object t, View icon) {
		Iicon<Theme> i = (Iicon<Theme>) icon;
		i.applyIconInfo((Theme) t);
		return true;
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view != null) {
			view.setSelected(false);
		}
		update(view);
		// add by zhouerlong
		Theme info = (Theme) view.getTag();
		if (info.id != null && info.id.equals("more")) {

			Intent i = new Intent();
			i.setClassName("com.nqmobile.live.base",
					"com.nqmobile.livesdk.commons.ui.StoreMainActivity");
			i.putExtra("fragment_index_to_show", 1);
			try {
				view.getContext().startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (info.id != null && info.id.equals("easy")) {
			mLauncher.showEasyLauncher();
		} else {
			if (mCurrentThemeId != null && mCurrentThemeId.equals(info.id)) {
				// Toast.makeText(getContext(), "切换的是同一主题",
				// Toast.LENGTH_SHORT).show();
				return;
			}
			final ProgressDialog progressDialog = new ProgressDialog(mLauncher,
					R.style.prize_dialog_style);
			progressDialog.setMessage(getResources().getString(
					R.string.theme_is_change));
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					progressDialog.dismiss();
				}

			}.start();
			notifyThemeChange(info);
			notifyThemeDb(info);
		}

	}

	/**
	 * 通知主题中心主题发生改变
	 * 
	 * @param pcg
	 *            改变后的主题包包名
	 */
	private void notifyThemeChange(Theme t) {

		Intent i = new Intent("appley_theme_ztefs");// 发送主题应用的广播
		i.putExtra("resId", t.getId());
		i.putExtra("themePath", t.getStrThemePath());
		mLauncher.sendBroadcast(i);
	}

	public String findThemeId(String strPath) {
		if (strPath != null) {
			for (int i = 0; i < mThemeItems.size(); i++) {
				Theme info = mThemeItems.get(i);
				if (info.strThemePath != null
						&& info.strThemePath.equals(strPath)) {
					mCurrentThemeId = info.id;
				}
			}
		}
		return mCurrentThemeId;
	}

	public void updateTHemeFromId(String id) {
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

	public void updateTHemeFromPath(String path) {

		if (!path.contains(FindDefaultResoures.DEFALUT_THEME_PATH)) {
			String ex_id=path.substring(path.lastIndexOf("/")+1,path.indexOf(".zip"));
			mCurrentThemeId = ex_id;
			Theme t = new Theme();
			t.id = mCurrentThemeId;
			notifyThemeDb(t);
			update(null);
			return;
		}
		String id = findThemeId(path);
		mCurrentThemeId = id;
		updateTHemeFromId(id);

	}

	public void notifyThemeDb(final Theme theme) {

		SQLSingleThreadExcutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {

				Cursor cursor = mLauncher.getContentResolver().query(
						STORE_LOCAL_THEME_URI, null,
						ThemeLocalTable_THEME_SELECT + "=?",
						new String[] { "1" }, null);
				if (cursor != null && cursor.moveToNext()) {
					String oldId = cursor.getString(cursor
							.getColumnIndex(ThemeLocalTable_THEME_ID));
					ContentValues cv = new ContentValues();
					cv.put(ThemeLocalTable_THEME_SELECT, 0);
					mLauncher.getContentResolver().update(
							STORE_LOCAL_THEME_URI, cv,
							ThemeLocalTable_THEME_ID + "=?",
							new String[] { oldId });
				}

				ContentValues cv = new ContentValues();
				cv.put(ThemeLocalTable_THEME_SELECT, 1);
				try {
					mLauncher.getContentResolver().update(
							STORE_LOCAL_THEME_URI, cv,
							ThemeLocalTable_THEME_ID + "=?",
							new String[] { theme.getId() });
				} catch (Exception e) {
				}
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

}
