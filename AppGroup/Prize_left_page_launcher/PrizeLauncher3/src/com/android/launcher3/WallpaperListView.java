package com.android.launcher3;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.bean.Wallpaper;
import com.android.launcher3.config.WallpaperSetService;
import com.android.launcher3.lq.FindDefaultResoures;
import com.android.launcher3.view.WallPaperDialog;
import com.android.launcher3.view.WallPaperDialog.OnItemClick;

//add by zhouerlong

public class WallpaperListView extends GridView implements View.OnClickListener {

	public WallpaperListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private Launcher mLauncher;

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
	public static String BROADCAST_INTENTFILTER_WALLPAPER_APPPLY = "com.live.store.wallpaper.apply";//商店里点击主题应用会发这个广播
	private static HashMap wallpaperNameMap = new HashMap<>();
	
	/**
	 * 主题集合
	 */
	private List<Wallpaper> mWallItems;
	private WallpapersAdapter mWallAdapter;
	/**
	 * 加载主题任务类
	 */
	LoadWallpaperTask task = new LoadWallpaperTask();

	public static int THEME_MAX = 5;

	/**
	 * 加载固定的项 默认主题，更多（主题商店入口） 初始化适配器
	 * 
	 * @param context
	 * @param resource
	 */
	public void init(Context context, int resource,boolean referenceView) {
		wallpaperNameMap = Launcher.wallpaperNames;
		mWallItems = new ArrayList<Wallpaper>();
		Wallpaper more = new Wallpaper();
		// more.thumb = this.getContext().getDrawable(R.drawable.more);
		more.wallpaperName = this.getContext().getString(R.string.more);
		more.id = "more";
		mWallItems.add(more);
		if(referenceView) {
		mWallAdapter = new WallpapersAdapter(context, resource);
		this.setAdapter(mWallAdapter);
		/*task.cancel(true);
		task.execute();*/
		new LoadWallpaperTask().execute();

		this.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						if (mCurrentWallpaperId != null) {
							updateWallpaperSelect(mCurrentWallpaperId);
						}

					}
				});
		}

	}
	

	public void init(Context c) {
		init(c, R.layout.wallpapers_list_item,true);
	}
	
	public boolean isNeedAdd(Wallpaper item) {
		for(Wallpaper t:mWallItems) {
			if(item.id.equals(t.id)) {
				return false;
			}
		}
		return true;
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
						WallpaperListView.this.post(new Runnable() {
							
							@Override
							public void run() {
								updateWallpaperSelect(mCurrentWallpaperId);
							}
						});

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

	private String mCurrentWallpaperId = null;

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
			DisplayMetrics dm = new DisplayMetrics();
			mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);
			int w = (int) (dm.widthPixels / 4.2);
			// int w = 720 / 4;
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w
					* mWallAdapter.getCount(), LayoutParams.MATCH_PARENT);
			setLayoutParams(lp);
			setNumColumns(mWallAdapter.getCount());
			mWallAdapter.notifyDataSetChanged();
			updateCurrentThemeFromDb();
		}

		@Override
		protected List<Wallpaper> doInBackground(Wallpaper... params) {
			return loadStoreThemes();

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
	
	

	public void release() {
		mWallItems.clear();
//		this.removeAllViewsInLayout();
		mWallAdapter=null;
		wallpaperNameMap=null;
	}
	
	public void reload() {
		init(getContext(),
				R.layout.wallpapers_list_item,true);
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
				wallpaper.setWallpaperName(getWallpaperName(cursor.getString(cursor
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
			

			if(file != null && file.exists() && file.getPath().contains(FindDefaultResoures.DEFALUT_WALLPAPER_PATH)) {
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

	/**
	 * 更新新装主题到列表
	 * 
	 * @param pakcagename
	 * @return
	 */
	public void bindAddWallpaperItem(Wallpaper theme) {
		if(!isNeedAdd(theme)) {
			return;
		}
		mWallItems.add(theme);
		DisplayMetrics dm = new DisplayMetrics();
		mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int w = (int) (dm.widthPixels / 4.2);
		// int w = 720 / 4;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w
				* mWallItems.size(), LayoutParams.MATCH_PARENT);
		setLayoutParams(lp);
		setNumColumns(mWallItems.size());
		this.mWallAdapter.notifyDataSetChanged();

	}

	/**
	 * 移除主题刷新View
	 * 
	 * @param pakcagename
	 */

	public void bindRemove(String id) {
		for (Wallpaper wall : mWallItems) {
			if (wall.id.equals(id)) {
				mWallItems.remove(wall);
				break;
			}
		}
		DisplayMetrics dm = new DisplayMetrics();
		mLauncher.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int w = (int) (dm.widthPixels / 4.2);
		// int w = 720 / 4;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w
				* mWallItems.size(), LayoutParams.MATCH_PARENT);
		setLayoutParams(lp);
		setNumColumns(mWallItems.size());
		mWallAdapter.notifyDataSetChanged();
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	
	
	
	class HodlerView {
		AsyncImageView themeView;
		ImageView applyImg;
		TextView textView;
	}

	
	
	
	// add by zhouerlong
	class WallpapersAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private int mResource;

		public WallpapersAdapter(Context context, int resource) {
			this.mResource = resource;
		}

		@Override
		public Wallpaper getItem(int position) {
			return mWallItems.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			mInflater = LayoutInflater.from(getContext());
			HodlerView holder;
			Wallpaper item = this.getItem(position);
			// add by zhouerlong
			if (convertView == null) {
				holder = new HodlerView();
				convertView = this.mInflater.inflate(mResource, null);
//				convertView.setLayerType(LAYER_TYPE_HARDWARE, null);
				holder.themeView = (AsyncImageView) convertView
						.findViewById(R.id.theme_id);
				holder.applyImg = (ImageView) convertView
						.findViewById(R.id.wallpaper_set);
				holder.textView = (TextView) convertView
						.findViewById(R.id.title);

				holder.applyImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						View view = (View) v.getParent();
						final View img = view.findViewById(R.id.theme_id);
						final Wallpaper item = (Wallpaper) v.getTag();
//						AlertDialog.Builder applyDialog = new Builder(v
//								.getContext(),R.style.prize_dialog_style);
//						applyDialog.setItems(R.array.applytype,
//								new DialogInterface.OnClickListener() {
//
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//										switch (which) {
//										case 0:
//											mApplyType = WALLAPPLYTYPE.DESK;
//											selectWallpaper(item,img);
//											break;
//										case 1:
//											mApplyType = WALLAPPLYTYPE.LOCK;
//											applyType(mApplyType, item);
//											break;
//										case 2:
//											mApplyType = WALLAPPLYTYPE.ALL;
//											selectWallpaper(item,img);
//											applyType(mApplyType, item);
//											break;
//										default:
//											mApplyType = WALLAPPLYTYPE.DESK;
//											break;
//										}
//									}
//								});
//						applyDialog.show();
					
				   // m b 2016.03.04
				    String[] items= v.getContext().getResources().getStringArray(R.array.applytype);
				    final WallPaperDialog applyDialog = new WallPaperDialog(v.getContext(), R.style.add_dialog, items);
				   
				    applyDialog.setOnItemClick(new OnItemClick() {
						
						@Override
						public void onClick(int which) {
							switch (which) {
							case 0:
								mApplyType = WALLAPPLYTYPE.LOCK;
								applyType(mApplyType, item);
								applyDialog.dismiss();
								break;
							case 1:
								mApplyType = WALLAPPLYTYPE.DESK;
								selectWallpaper(item,img);
								applyDialog.dismiss();
								break;
							case 2:
								mApplyType = WALLAPPLYTYPE.ALL;
								selectWallpaper(item,img);
								applyType(mApplyType, item);
								applyDialog.dismiss();
								break;
							default:
								mApplyType = WALLAPPLYTYPE.DESK;
								break;
							}
						}
					});
				    applyDialog.show();
				 
					}
				});
				convertView.setTag(holder);
			} else {
				holder = (HodlerView) convertView.getTag();
			}
			if (item.id != null && item.id.equals("more")) {
				holder.applyImg.setVisibility(View.GONE);
				holder.themeView.setImageDrawable(getContext().getDrawable(
						R.drawable.choose_more_wallpaper));
			}
			if (holder.themeView != null) {
				if (item.iconPreviewPath != null) {
					holder.themeView.loadImage(item.iconPreviewPath);
				}
				holder.themeView.setScaleType(ScaleType.CENTER_CROP);
				// add by zhouerlong
				holder.themeView.setOnClickListener(WallpaperListView.this);
				holder.themeView.setTag(item);
			}
			if (holder.textView != null) {
				holder.textView.setText(item.wallpaperName);
			}
			if (holder.applyImg != null) {
				holder.applyImg.setTag(item);
			}
			return convertView;
		}

		
		private int mMax=7;
		@Override
		public int getCount() {
			/*if(mWallItems.size()>mMax) {
				return mMax;
			}*/
			return mWallItems.size();
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	public InputStream getSavedWallpaper(String path) {
		FileInputStream fis = null;
		File imgFile = new File(path);
		if (imgFile.exists()) {
			try {
				fis = new FileInputStream(path);
			} catch (Exception e) {
			}
		} else {
		}
		return fis;
	}

	public String applyType(WALLAPPLYTYPE type, Wallpaper info) {
		if (type != WALLAPPLYTYPE.DESK) {
			final ProgressDialog dialog = new ProgressDialog(mLauncher);
			dialog.setMessage(mLauncher.getString(R.string.dialog_loading));
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);
			dialog.show();
			ApplyWallLockTask applyLock = new ApplyWallLockTask(dialog);
			applyLock.execute(info);
			return applyLock.getPath();

		}
		return null;
	}

	public void selectWallpaper(final Wallpaper wallpaper,View view) {

		mCurrentWallpaperId = wallpaper.id;
		updateThemeSelect(view);
		notifyWallPaperDb(wallpaper);
		Utilities.startSetWallpaperSevice(wallpaper.getStrWallpaperPath(), getContext(),false,mLauncher.getWallService());

	}

	class ApplyWallLockTask extends AsyncTask<Wallpaper, Boolean, Boolean> {

		ProgressDialog mDialog;
		InputStream mInputStream;
		public final static String IMG_CACHE = "imglod/cache";
		String path;

		public ApplyWallLockTask(ProgressDialog d) {
			super();
			mDialog = d;
		}

		@Override
		protected Boolean doInBackground(Wallpaper... item) {

			InputStream is = getSavedWallpaper(item[0].getStrWallpaperPath());
			boolean isSuccess = FileUtils.copyFile(is,
					getRawFile(String.valueOf(item[0].id) + ".png"));

			return isSuccess;
		}

		private File getRawFile(String fileName) {
			File dir = mLauncher.getExternalFilesDir(IMG_CACHE);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(dir, fileName);
			path = file.getPath();
			return file;

		}

		public String getPath() {
			return path;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result) {
				Settings.System.putString(mLauncher.getContentResolver(),
						"keyguard_wallpaper", path);
//				Toast.makeText(mLauncher, "锁屏壁纸设置成功", Toast.LENGTH_SHORT)
//						.show();

			} else {
				// Toast.makeText(WallpaperPickerActivity.this, "锁屏壁纸设置错误",
				// Toast.LENGTH_SHORT).show();
			}
			mDialog.dismiss();

		}

	}

	// add by zhouerlong
	public void updateThemeSelect(View v) {

		Wallpaper item = (Wallpaper) v.getTag();

		if (item.id.equals("more")) {
			return;
		}
		for (int i = 0; i < this.getChildCount(); i++) {
			View child = this.getChildAt(i);
			child.findViewById(R.id.theme_id).setSelected(false);

		}
		v.setSelected(true);
	}

	// add by zhouerlong
	public void updateWallpaperSelect(String selectId) {
		if (selectId != null) {
			if(!selectId.equals(mCurrentWallpaperId)) {
				mCurrentWallpaperId = selectId;
			}
			for (int i = 0; i < this.getChildCount(); i++) {
				View child = this.getChildAt(i).findViewById(R.id.theme_id);
				Wallpaper info = (Wallpaper) child.getTag();
				if (info.id.equals(selectId)) {
					child.setSelected(true);
				} else {
					child.setSelected(false);
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view != null) {
			view.setSelected(false);
		}
		// add by zhouerlong
//		BlueTask b = new BlueTask(mLauncher, null, mLauncher.getWallpaperBg());
//		b.execute();
		
		Wallpaper info = (Wallpaper) view.getTag();
		if (info.id != null && info.id.equals("more")) {
		/*	Intent intent = new Intent();
			intent.setClassName("com.nqmobile.live.base",
					"com.nqmobile.livesdk.commons.ui.StoreControlACT");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
			Intent i = new Intent();
        	i.setClassName("com.nqmobile.live.base", "com.nqmobile.livesdk.commons.ui.StoreMainActivity");
        	i.putExtra("fragment_index_to_show", 2);
			try {
				view.getContext().startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// notifyThemeChange(info);
			if(mCurrentWallpaperId!=null&&mCurrentWallpaperId.equals(info.id)) {
				view.setSelected(true);
//    			Toast.makeText(getContext(), "同一壁纸不能切换", Toast.LENGTH_SHORT).show();
				return;
			}
			mCurrentWallpaperId = info.id;
			selectWallpaper(info,view);
			postDelayed(new Runnable() {
				
				@Override
				public void run() {

					BlueTaskWall b = new BlueTaskWall(mLauncher, mLauncher.getWallpaperBg());
					b.execute();
				}
			}, 500);
		}

	}

	// add by huanglingjun
	public void removeSelect() {
		for (int i = 0; i < this.getChildCount(); i++) {
			View child = this.getChildAt(i);
			child.findViewById(R.id.theme_id).setSelected(false);
			child.setSelected(false);
		}
		mCurrentWallpaperId = null;
		removeSelectDb();
		mWallAdapter.notifyDataSetChanged();
	}

	// add by huanglingjun 清除数据库中已选壁纸标志
	private void removeSelectDb() {

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
				try {
					if (cursor != null) {
						cursor.close();
					}
				} catch (Exception e) {
				}
			}
		});
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
				if (null!=value && key.equals(ID) && value.split(";").length >1) {
					return value.split(";")[i];
				}
			}
		}
		return null;
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
}
