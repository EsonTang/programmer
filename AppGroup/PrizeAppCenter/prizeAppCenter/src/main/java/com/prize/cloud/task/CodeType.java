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
 * 对验证码类型的定义
 * @author yiyi
 * @version 1.0.0
 */
public class CodeType {

	public static final int TYPE_REGISTER = 1;// 请求下发用于注册的验证码
	public static final int TYPE_LOSTPSWD = 2;// 请求下发用于找回密码的验证码
	public static final int TYPE_BIND = 3;// 用于绑定email
}
