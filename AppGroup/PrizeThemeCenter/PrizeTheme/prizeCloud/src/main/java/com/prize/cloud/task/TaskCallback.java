/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.cloud.task;

/**
 * 监听请求结果，并处理
 * @author yiyi
 * @version 1.0.0
 * @param <T> 提供给UI层的自定数据
 */
public interface TaskCallback<T> {

	/**
	 * 
	 * @param data
	 *            需要回传的数据，类型在请求task中定义。 只关心事件结果，不需回传相应数据时可设空。
	 */
	void onTaskSuccess(T data);

	/**
	 * 
	 * @param errorCode
	 *            备注字段
	 * @param msg
	 *            错误信息，一般用于告知用户
	 */
	void onTaskError(int errorCode, String msg);

}
