/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.prizethemecenter.ui.utils;

import com.prize.prizethemecenter.ui.utils.SearchResult.WatcherChange;

import java.util.ArrayList;
import java.util.List;

/**
 **
 * Watched管理类
 * @author
 * @version V1.0
 */
public class ChangeWatchedManager {
	// 存放观察者
	private static List<WatcherChange> list = new ArrayList<WatcherChange>();

	public static void notifyTipsChange(String key) {
		int i = 0;
		try {
			while (i < list.size()) {
				list.get(i).updateTips(key);
				i++;
			}
		} catch (Exception localException) {
		}
	}

	/**
	 * 注册观察者
	 * 
	 * @param paramNotifyChangeObserver
	 * @return void
	 */
	public static void registObserver(WatcherChange paramNotifyChangeObserver) {
		list.add(paramNotifyChangeObserver);
	}

	/**
	 * 解除注册
	 * 
	 * @param paramNotifyChangeObserver
	 * @return void
	 */
	public static void unregistObserver(WatcherChange paramNotifyChangeObserver) {
		if (list.contains(paramNotifyChangeObserver)) {
			list.remove(paramNotifyChangeObserver);
		}
	}

}
