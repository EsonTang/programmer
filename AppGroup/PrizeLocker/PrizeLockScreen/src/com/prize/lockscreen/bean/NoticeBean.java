package com.prize.lockscreen.bean;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
/***
 * 消息实体 (详情)
 * @author fanjunchen
 *
 */
public class NoticeBean {
	/**标题*/
	public String title;
	/**简要 或演唱者*/
	public String text;
	/**icon*/
	public Drawable appIcon;
	/**时间 以ms为单位*/
	public long when;
	/**可以进入的应用intent*/
	public PendingIntent contentIntent;
	/**状态, 音乐状态 PlaybackState.STATE_PAUSE, PlaybackState.STATE_PLAYING*/
	public int status = 0;
	
	public NoticeBean() {

	}

	public NoticeBean(String title, String text , long when,
			Drawable icon,PendingIntent contentIntent) {
		this.title = title;
		this.text = text;
		this.when = when;
		this.appIcon = icon;
		this.contentIntent = contentIntent;
	}
}
