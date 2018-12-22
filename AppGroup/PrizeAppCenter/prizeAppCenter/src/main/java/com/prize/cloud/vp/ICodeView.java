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
 * 验证码相关操作的回调接口，在Activity中实现该接口
 * @author yiyi
 * @version 1.0.0
 */
public interface ICodeView {
	
	/**
	 * 计算验证码发送完成后离下一次发送的时间
	 * @param seconds 剩余时间，单位秒
	 */
	void calculate(int seconds);

	/**
	 * 获取验证码失败
	 * @param msg 失败原因
	 */
	void onGetCodeFail(String msg);

	/**
	 * 校验失败
	 * @param msg 
	 */
	void onVerifyFail(String msg);

	/**
	 * 绑定失败
	 * @param msg
	 */
	void onBindFail(String msg);
	
}
