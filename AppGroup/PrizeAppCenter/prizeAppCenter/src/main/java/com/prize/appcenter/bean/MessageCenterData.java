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
package com.prize.appcenter.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @desc 系统安装app的信息
 * @author huangchangguo
 * @version 版本1.7
 * @Date 2016.5.20
 *
 */
public class MessageCenterData implements Serializable {
	private static final long serialVersionUID = -8392315010365295445L;
	public ArrayList<MessageBean> privateInformation;
	public ArrayList<MessageBean> systemInformation;
}
