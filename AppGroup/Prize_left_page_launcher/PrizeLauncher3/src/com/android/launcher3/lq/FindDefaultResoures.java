package com.android.launcher3.lq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.launcher3.SQLSingleThreadExcutor;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Xml;

public class FindDefaultResoures {

	protected static final String DATA_AUTHORITY = "com.nqmobile.live.base.dataprovider";
	public static String DEFALUT_WALLPAPER_PATH = "";// 内壁纸路径
	public static String DEFALUT_THEME_PATH = "";//内置主题路径
	private static final Uri STORE_LOCAL_THEME_URI = Uri
			.parse("content://com.nqmobile.live.base.dataprovider" + "/"
					+ "theme_local");
	private Context mContext;

	public FindDefaultResoures(Context context) {
		this.mContext = context;

	}

	public void loadWallPapaperAndTHeme(Runnable wall, Runnable theme) {

		SharedPreferences sp = mContext.getSharedPreferences(
				"load_default_res", Context.MODE_PRIVATE);
		boolean isloaded = sp.getBoolean("load_default_res_loaded", false);

		if (!isloaded) {
			BultInsertWallpaperTask wallTask = new BultInsertWallpaperTask();
			wallTask.setWallRun(wall);
			wallTask.execute();
			BultInsertThemeTask themeTask = new BultInsertThemeTask();
			themeTask.setThemeRunnable(theme);
			themeTask.execute();
			sp.edit().putBoolean("load_default_res_loaded", true).commit();
		}else if(!hasThemes()) {
			BultInsertWallpaperTask wallTask = new BultInsertWallpaperTask();
			wallTask.setWallRun(wall);
			wallTask.execute();
			BultInsertThemeTask themeTask = new BultInsertThemeTask();
			themeTask.setThemeRunnable(theme);
			themeTask.execute();
			sp.edit().putBoolean("load_default_res_loaded", true).commit();
		}
	}
	
	public boolean hasThemes() {
		int count = 0;
				Cursor cursor = null;
				try {
					cursor = mContext.getContentResolver().query(
							STORE_LOCAL_THEME_URI, null, null, null, null);
					count = cursor.getCount();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						cursor.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
		return count > 0;
	}

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	// beginadd by ouyangjin for 主题商店内置主题
	public List<Theme> findDefaultTheme() {// 获取内置主题
		List<Theme> themesList = new ArrayList<Theme>();
		File themesFile = new File(DEFALUT_THEME_PATH);

		if (!themesFile.exists() || !themesFile.isDirectory()) {// 不存在内置主题
			return null;
		}

		// 通过配置文件去读取对应的所有主题
		LinkedHashMap<String, String> namesAndIds = findConfig(DEFALUT_THEME_PATH);
		if (namesAndIds == null || namesAndIds.size() == 0) {
			return null;// 没有配置内置主题
		}

		Iterator iter = namesAndIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();// 主题id
			Object val = entry.getValue();// 名字name
			themesList.add(parseToTheme(
					DEFALUT_THEME_PATH + String.valueOf(key),
					String.valueOf(val)));
		}
		return themesList;

	}

	private Theme parseToTheme(String fileString, String name) {// 解析内置主题文件夹
		File themeFile = new File(fileString);

		if (!themeFile.exists() || !themeFile.isDirectory()) {// 不存在内置主题
			return null;
		}

		String[] filesName = themeFile.list();
		Theme theme = new Theme();// 新建一个主题对象
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < filesName.length; i++) {
			if (filesName[i].endsWith("zip")) {// 是主题包
				theme.setStrThemePath(fileString + "/" + filesName[i]);
			} else if (filesName[i].contains("icon")) {// 小预览图
				theme.setStrIconPath(fileString + "/" + filesName[i]);
			} else if (filesName[i].contains("preview0")) {// 主题详情里的预览图
				map.put("preview0", fileString + "/" + filesName[i]);
			} else if (filesName[i].contains("preview1")) {// 主题详情里的预览图
				map.put("preview1", fileString + "/" + filesName[i]);
			} else if (filesName[i].contains("preview2")) {// 主题详情里的预览图
				map.put("preview2", fileString + "/" + filesName[i]);
			} else if (filesName[i].endsWith(".jar")) {
				theme.setStrThemePath(fileString + "/" + filesName[i]);
			}
		}
		String strId = fileString.substring(fileString.lastIndexOf("/") + 1);
		theme.setStrId(strId);
		List<String> themePreviews = new ArrayList<String>();
		if(null != map.get("preview0")){
			themePreviews.add(map.get("preview0"));
		}
		if(null != map.get("preview1")){
			themePreviews.add(map.get("preview1"));
		}
		if(null != map.get("preview2")){
			themePreviews.add(map.get("preview2"));
		}
		theme.setArrPreviewPath(themePreviews);
		theme.setArrPreviewUrl(themePreviews);
		theme.setWithinSystem(0);// 是内置主题
		theme.setStrName(name);// 主题名字
		return theme;
	}

	// END by ouyangjin for 主题商店内置主题

	// begin add by ouyangjin for 内置壁纸
	public List<Wallpaper> findDefaultWallpapers() { // BEGIN add by ouyangjin
														// for 内置主题
		File file = new File(DEFALUT_WALLPAPER_PATH);
		List<Wallpaper> wallpapersList = new ArrayList<Wallpaper>();
		if (!file.exists() || !file.isDirectory()) {// 不存在内置壁纸
			return null;
		}

		// 通过配置文件去读取对应的所有壁纸
		LinkedHashMap<String, String> namesAndIds = findConfig(DEFALUT_WALLPAPER_PATH);
		if (namesAndIds == null || namesAndIds.size() == 0) {
			return null;// 没有配置内置壁纸
		}

		Iterator iter = namesAndIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();// 主题id
			Object val = entry.getValue();// 名字name
			wallpapersList.add(parseToWallpaper(
					DEFALUT_WALLPAPER_PATH + String.valueOf(key),
					String.valueOf(val)));
		}
		return wallpapersList;
	}

	public static  LinkedHashMap<String, String> findCustomConfig(String path) {
		String configPath = path + "kb.xml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		LinkedHashMap<String, String> configs = new LinkedHashMap<>();
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("general_config")) {
						for(int i=0;i<xpp.getAttributeCount();i++) {
							String key = xpp.getAttributeName(i);
							String value = xpp.getAttributeValue(i);
							configs.put(key, value);
						}
					} 
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					/*if (xpp.getName().equals("item-info")) {
						if (id != null && name != null)
							configs.put(id, name);
					}*/
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configs;
	}

	
		private LinkedHashMap<String, String> findConfig(String path) {
		String configPath = path + "config.xml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		String name = null;
		String id = null;
		LinkedHashMap<String, String> namesAndIds = new LinkedHashMap<>();
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("item-info")) {
						name = null;
						id = null;
					} else if (xpp.getName().equals("name")) {
						eventType = xpp.next();
						name = xpp.getText();
						if(null!= name && name.split(";").length >1){
							name = name.split(";")[0];
						}
					
					} else if (xpp.getName().equals("id")) {
						eventType = xpp.next();
						id = xpp.getText();
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("item-info")) {
						if (id != null)
						{
							if(name ==null) {
								name="";
							}
							namesAndIds.put(id, name);
						}
					}
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return namesAndIds;
	}

	private Wallpaper parseToWallpaper(String fileString, String name) {
		File file = new File(fileString);

		if (!file.exists() || !file.isDirectory()) {// 不存在内置壁纸
			return null;
		}

		String[] filesName = file.list();
		Wallpaper wallpaper = new Wallpaper();// 新建一个壁纸对象
		for (int i = 0; i < filesName.length; i++) {
			if (filesName[i].contains("icon")) {// 小预览图
				wallpaper.setStrIconPath(fileString + "/" + filesName[i]);
				wallpaper.setStrIconUrl(fileString + "/" + filesName[i]);
				// wallpaper.setPreviewPicture(fileString+"/"+filesName[i]);
				// wallpaper.setPreviewPicturePath(fileString+"/"+filesName[i]);
			} else if (filesName[i].contains("preview")) {// 壁纸详情里的预览图
				wallpaper.setStrWallpaperPath(fileString + "/" + filesName[i]);
				// wallpaper.setStrWallpaperUrl(fileString+"/"+filesName[i]);
			} else {// 壁纸文件
				wallpaper.setPreviewPicture(fileString + "/" + filesName[i]);
				wallpaper
						.setPreviewPicturePath(fileString + "/" + filesName[i]);
			}
		}
		wallpaper
				.setStrId(fileString.substring(fileString.lastIndexOf("/") + 1));
		wallpaper.setStrName(name);
		wallpaper.setIsSystemDefault(1);// 是内置壁纸
		return wallpaper;
	}

	public ContentValues wallpaperToContentValues(int column,
			Wallpaper wallpaper) {
		ContentValues values = null;
		if (wallpaper != null) {
			values = new ContentValues();
			values.put(WallpaperLocalTable.WALLPAPER_ID, wallpaper.getStrId());
			values.put(WallpaperLocalTable.WALLPAPER_NAME,
					wallpaper.getStrName());
			values.put(WallpaperLocalTable.WALLPAPER_ICON_URL,
					wallpaper.getStrIconUrl());
			values.put(WallpaperLocalTable.WALLPAPER_ICON_PATH,
					wallpaper.getStrIconPath());
			values.put(WallpaperLocalTable.WALLPAPER_PREVIEW_URL,
					wallpaper.getPreviewPicture());
			values.put(WallpaperLocalTable.WALLPAPER_PREVIEW_PATH,
					wallpaper.getPreviewPicturePath());
			values.put(WallpaperLocalTable.WALLPAPER_URL,
					wallpaper.getStrWallpaperUrl());
			values.put(WallpaperLocalTable.WALLPAPER_PATH,
					wallpaper.getStrWallpaperPath());
			values.put(WallpaperLocalTable.WALLPAPER_IS_SYSTEM,
					wallpaper.getIsSystemDefault());
		}
		return values;
	}

	class BultInsertThemeTask extends AsyncTask<Void, Void, Void> {

		private Runnable themeRunnable;

		public Runnable getThemeRunnable() {
			return themeRunnable;
		}

		public void setThemeRunnable(Runnable themeRunnable) {
			this.themeRunnable = themeRunnable;
		}

		@Override
		protected Void doInBackground(Void... params) {

			List<Theme> mdefaultList = findDefaultTheme();
			if(mdefaultList!=null) {
				for (Theme t : mdefaultList) {
					insertTheme(t);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			themeRunnable.run();
			super.onPostExecute(result);
		}

	}

	class BultInsertWallpaperTask extends AsyncTask<Void, Void, Void> {

		private Runnable wallRun;

		public Runnable getWallRun() {
			return wallRun;
		}

		public void setWallRun(Runnable wallRun) {
			this.wallRun = wallRun;
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<Wallpaper> mdefaultList = findDefaultWallpapers();
			if(mdefaultList!=null) {
				for (Wallpaper t : mdefaultList) {
					insertWall(t);
				}	
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			wallRun.run();
			super.onPostExecute(result);
		}

	}

	public void insertWall(Wallpaper wallpaper) {

		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder deleteOp = ContentProviderOperation.newDelete(
					WallpaperLocalTable.LOCAL_WALLPAPER_URI).withSelection(
					WallpaperLocalTable.WALLPAPER_ID + " = ?",
					new String[] { wallpaper.getStrId() });
			ops.add(deleteOp.build());

			wallpaper.setLongLocalTime(System.currentTimeMillis());
			ContentValues values = wallpaperToContentValues(-1, wallpaper);
			Builder insertOp = ContentProviderOperation.newInsert(
					WallpaperLocalTable.LOCAL_WALLPAPER_URI).withValues(values);
			ops.add(insertOp.build());

			mContext.getContentResolver().applyBatch(DATA_AUTHORITY, ops);

		} catch (Exception e) {

		}

	}

	public boolean insertTheme(Theme theme) {

		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder deleteOp = ContentProviderOperation.newDelete(
					ThemeLocalTable.LOCAL_THEME_URI).withSelection(
					ThemeLocalTable.THEME_ID + " = ?",
					new String[] { theme.getStrId() });
			ops.add(deleteOp.build());

			theme.setLongLocalTime(System.currentTimeMillis());
			ContentValues values = themeToContentValues(-1, theme);
			Builder insertOp = ContentProviderOperation.newInsert(
					ThemeLocalTable.LOCAL_THEME_URI).withValues(values);
			ops.add(insertOp.build());

			mContext.getContentResolver().applyBatch(DATA_AUTHORITY, ops);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public ContentValues themeToContentValues(int column, Theme theme) {
		ContentValues values = null;
		if (theme != null) {
			values = new ContentValues();
			values.put(ThemeLocalTable.THEME_ID, theme.getStrId());
			values.put(ThemeLocalTable.THEME_NAME, theme.getStrName());
			values.put(ThemeLocalTable.THEME_ICON_URL, theme.getStrIconUrl());
			// 预览图网址
			StringBuilder previewUrl = new StringBuilder();
			List<String> previewUrls = theme.getArrPreviewUrl();
			if (previewUrls != null && previewUrls.size() > 0) {
				for (int j = 0; j < previewUrls.size(); j++) {
					previewUrl.append(previewUrls.get(j)).append(";");
				}
			}
			if (previewUrl.length() > 1) {
				values.put(ThemeLocalTable.THEME_PREVIEW_URL,
						previewUrl.substring(0, previewUrl.length() - 1));
			} else {
				values.put(ThemeLocalTable.THEME_PREVIEW_URL, "");
			}
			values.put(ThemeLocalTable.THEME_URL, theme.getStrThemeUrl());
			values.put(ThemeLocalTable.THEME_ICON_PATH, theme.getStrIconPath());
			// 预览图本地路径
			StringBuilder previewPath = new StringBuilder();
			List<String> previewPaths = theme.getArrPreviewPath();
			if (previewPaths != null && previewPaths.size() > 0) {
				for (int j = 0; j < previewPaths.size(); j++) {
					previewPath.append(previewPaths.get(j)).append(";");
				}
			}
			if (previewPath.length() > 1) {
				values.put(ThemeLocalTable.THEME_PREVIEW_PATH,
						previewPath.substring(0, previewPath.length() - 1));
			} else {
				values.put(ThemeLocalTable.THEME_PREVIEW_PATH, "");
			}
			values.put(ThemeLocalTable.THEME_PATH, theme.getStrThemePath());
			values.put(ThemeLocalTable.THEME_WITHIN_SYSTEM,
					theme.getWithinSystem());
		}
		return values;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
