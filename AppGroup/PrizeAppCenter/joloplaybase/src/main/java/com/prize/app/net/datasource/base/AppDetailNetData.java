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

package com.prize.app.net.datasource.base;

import com.prize.app.net.AbstractNetData;

public class AppDetailNetData extends AbstractNetData {
	public String code;
	public String msg;
	public AppDetailData data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public AppDetailData getData() {
		return data;
	}

	public void setData(AppDetailData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "AppDetailNetData [code=" + code + ", msg=" + msg + ", data="
				+ data + "]";
	}

}
