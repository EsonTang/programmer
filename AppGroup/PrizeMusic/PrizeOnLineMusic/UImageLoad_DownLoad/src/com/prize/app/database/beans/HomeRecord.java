package com.prize.app.database.beans;

public class HomeRecord {
	/** code */
	public int id;
	/** 标题或名称 */
	public String name;
	/** 播放次数 */
	public int playCount;
	/** 类型 */
	public int contentType;
	/** 图片地址 */
	public String iconUrl;
	public String subTitle;
	/** 专题或者app */
	public String adType;
	public static final int CONTENT_TYPE_AD = 1;
	public static final int CONTENT_TYPE_DAILY_SONGS = 2;
	public static final int CONTENT_TYPE_NEW_ALBUMS_LIST = 3;
	public static final int CONTENT_TYPE_HOT_SONGS_LIST = 4;
	public static final int CONTENT_TYPE_RECOMMEND_COLLECT = 5;
	public static final int CONTENT_TYPE_SCENE_SONGS = CONTENT_TYPE_RECOMMEND_COLLECT + 1;
	public static final int CONTENT_TYPE_HOTSONG_RANK = CONTENT_TYPE_SCENE_SONGS + 1;

}