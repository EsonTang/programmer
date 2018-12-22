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

import com.prize.prizethemecenter.ui.utils.SearchResult.Watcher;

import java.util.ArrayList;
import java.util.List;

/**
 **
 * Watched管理类
 * @author
 * @version V1.0
 */
public class WatchedManager {
	// 存放观察者
	private static List<Watcher> list = new ArrayList<Watcher>();

	public static void notifyChange(String key) {
		int i = 0;
		try {
			while (i < list.size()) {
				list.get(i).updateNotify(key);
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
	public static void registObserver(Watcher paramNotifyChangeObserver) {
		list.add(paramNotifyChangeObserver);
	}

	/**
	 * 解除注册
	 * 
	 * @param paramNotifyChangeObserver
	 * @return void
	 */
	public static void unregistObserver(Watcher paramNotifyChangeObserver) {
		if (list.contains(paramNotifyChangeObserver)) {
			list.remove(paramNotifyChangeObserver);
		}
	}

}
