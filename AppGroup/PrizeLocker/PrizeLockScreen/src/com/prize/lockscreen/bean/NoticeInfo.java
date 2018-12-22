package com.prize.lockscreen.bean;
/***
 * 消息实体 (大)
 * @author Administrator
 *
 */
public class NoticeInfo {
	/**普通消息类型, 默认*/
	public static final int NORMAL = 0;
	/**音乐消息类型*/
	public static final int MUSIC = 1;
	/**其他消息类型*/
	public static final int OTHERS = 9;
	
	/**消息来自的包名*/
	public String packageName;
	/**消息的标签, 暂时末用*/
	public String tag;
	/**消息ID*/
	public int id;
	
	private NoticeBean mNoticeBean;
	/**消息类型, 0 普通消息, 1 音乐消息, 9其他*/
	public int type = 0;
	/**用于消息删除的key*/
	public String key;
	
	public NoticeInfo() {

	}
	
	public NoticeBean getNoticeBean() {
		return mNoticeBean;
	}

	public void setNoticeBean(NoticeBean noticeBean) {
		this.mNoticeBean = noticeBean;
	}

}
