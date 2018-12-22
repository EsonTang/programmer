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
package com.prize.appcenter.callback;

import com.prize.app.util.JLog;

import java.util.Observable;

/**
 * 
 * @author huangchangguo
 * 
 * 同步助手-恢复应用选择框的观察者，观察每个item是否被选中，时时刷新。用于一键恢复， 显示总App的 大小
 *  2016.7.22
 * 
 * 
 */

public class AppSyncRestoreWatcher extends Observable {
	private static final String TAG = "AppSyncRestoreWatcher";
	public String message;

	public void publishMessage(String message) {
		this.message = message;
		if (message.contains("update")) {
			setChanged();
		}

		JLog.d(TAG, "-----------" + message);

		notifyObservers();
	}

}
