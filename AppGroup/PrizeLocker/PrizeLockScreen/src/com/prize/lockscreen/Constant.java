package com.prize.lockscreen;

public class Constant {
	
	public final static int VIEW_MOVE_SPEED = 2;

	public final static int BUBBLE_TYPE_CALCULATOR = 1;
	public final static int BUBBLE_TYPE_MUSIC = 2;

	public final static String CANCEL_ALL_NOTICE = "cancel_all_notice";
	public final static String CANCEL_NOTICE = "cancel_notice";
	public final static String GET_ACTIVE_NOTICE = "active_notifications";
	/**要执行的命令*/
	public final static String COMMAND = "command";
	/**通知消息的包名*/
	public final static String PACKAGE_NAME = "pack_name";
	/**通知消息的tag值*/
	public final static String TAG = "tag";
	/**通知消息的ID值*/
	public final static String ID = "id";
	/**通知消息的key值*/
	public final static String KEY = "key";
	
	public static final String MUSIC_SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPLAY = "play";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";
	
	public static boolean MUSIC_PLAY = false;
	
	/**常用音乐播放器的包名*/
	public static final String PKG_MUSIC = "com.prize.music,com.qq.music";
	/**
	 * 根据包名进行过滤
	 */
	public static String[] FILTER_PACKAGE_NAME={
		"com.prize.music"
		, "android"
	};
}
