package com.prize.app.net.req;

import com.prize.app.beans.AbstractCommonBean;

/**
 **
 * 基本请求参数类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class BaseReq extends AbstractCommonBean {
	private String usercode;
	/**
	 * 会话id,可用来判断用户是否登录 以下业务类型需要设置sessionid，其他情况可为null * 游戏礼包业务 系统消息业务 问题反馈业务
	 * 网游启动业务 游戏评论业务
	 */
	private String sessionid;

	public String getUsercode() {
		return usercode;
	}

	public void setUsercode(String usercode) {
		this.usercode = usercode;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
}
