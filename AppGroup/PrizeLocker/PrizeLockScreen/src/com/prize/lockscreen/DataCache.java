package com.prize.lockscreen;

import java.util.ArrayList;
import com.prize.lockscreen.bean.NoticeInfo;
/***
 * 数据缓存
 * @author Administrator
 *
 */
public class DataCache {
	/**用于显示在界面上的通知*/
	public final static ArrayList<NoticeInfo> mNoticeList = new ArrayList<NoticeInfo>();
	/**用于操作的通知**/
	public final static ArrayList<NoticeInfo> tempList = new ArrayList<NoticeInfo>();
}
