/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：观察者类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-8
 *********************************************/
package com.android.launcher3.nifty;

import com.android.launcher3.StateInfo;

/**
 * @author Administrator
 *观察值
 */
public interface NiftyObservers {
	/**
	 * 响应时间方法发生改变
	 */
	public void onChanged(StateInfo st);
}
