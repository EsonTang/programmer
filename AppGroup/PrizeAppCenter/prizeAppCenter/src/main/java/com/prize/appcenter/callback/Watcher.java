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

/**
 ** 
 * 收藏或则取消收藏 观察者
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public interface Watcher {
	/**
	 * 
	 * 收藏或则取消收藏时候，更新界面信息
	 * 
	 * @param appId
	 *            appId
	 * @param isCollected
	 *            是否被收藏
	 */
	public void update(String appId, boolean isCollected);
}
