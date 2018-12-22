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
package com.prize.cloud.vp;

/**
 * 本机注册的回调接口
 * @author yiyi
 * @version 1.0.0
 */
public interface IOwnView {

	/**
	 * 本机注册请求前的操作，建议显示加载状态
	 */
	void onPreRegister();

	/**
	 * 注册失败
	 * @param msg
	 */
	void onError(String msg);

	/**
	 * 注册成功
	 * @param data 本机号码
	 */
	void onSuccess(String data);
}
