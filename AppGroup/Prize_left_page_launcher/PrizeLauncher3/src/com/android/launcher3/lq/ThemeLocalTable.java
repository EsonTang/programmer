package com.android.launcher3.lq;

import android.net.Uri;

public class ThemeLocalTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "theme_local";	
	public static final int TABLE_VERSION = 2;
	public static final Uri LOCAL_THEME_URI = Uri.parse("content://com.nqmobile.live.base.dataprovider"  + "/" + TABLE_NAME);
	/* 本地主题表和主题缓存表的字段 */
	public static final String THEME_ID = "themeId";
	public static final String THEME_NAME = "name";
	public static final String THEME_ICON_URL = "iconUrl";
	public static final String THEME_PREVIEW_URL = "previewUrl";
	public static final String THEME_URL = "themeUrl";
	public static final String THEME_ICON_PATH = "iconPath";
	public static final String THEME_PREVIEW_PATH = "previewPath";
	public static final String THEME_PATH = "themePath";
	public static final String THEME_WITHIN_SYSTEM = "withinSystem";
	public static final String THEME_SELECT = "isSelected";
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
