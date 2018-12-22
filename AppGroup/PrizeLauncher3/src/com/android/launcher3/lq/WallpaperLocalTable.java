package com.android.launcher3.lq;

import android.net.Uri;

public class WallpaperLocalTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "wallpaper_local";	
	public static final Uri LOCAL_WALLPAPER_URI = Uri.parse("content://com.nqmobile.live.base.dataprovider" + "/" + TABLE_NAME);
	
	
	/* 本地壁纸表和壁纸缓存表的字段 */
	public static final String WALLPAPER_ID = "wallpaperId";
	public static final String WALLPAPER_NAME = "name";
	public static final String WALLPAPER_ICON_URL = "iconUrl";
	public static final String WALLPAPER_URL = "url";
	public static final String WALLPAPER_ICON_PATH = "iconPath";
    public static final String WALLPAPER_PREVIEW_URL = "previewUrl";
	public static final String WALLPAPER_PREVIEW_PATH = "previewPath";
	public static final String WALLPAPER_PATH = "path";
	public static final String WALLPAPER_DOWNLOAD_ID = "downloadId";
	public static final String WALLPAPER_IS_SYSTEM = "is_system_default";
	public static final String WALLPAPER_SELECT = "isSelected";
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
