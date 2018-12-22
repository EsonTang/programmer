package com.prize.app.net.req;

import com.prize.app.beans.AbstractCommonBean;

/**
 * 基本响应信息，其他相应都继承于它
 * 
 * @author prize
 * @version 1.0 2013-2-4
 * 
 */
public class BaseResp extends AbstractCommonBean {
	private Integer code;//

	private String msg;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "BaseResp [code=" + code + ", msg=" + msg + "]";
	}

}
