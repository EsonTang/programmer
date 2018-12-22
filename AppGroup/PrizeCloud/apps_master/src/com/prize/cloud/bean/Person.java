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
package com.prize.cloud.bean;

import com.lidroid.xutils.db.annotation.Id;

/**
 * 已登录用户的个人信息
 * @author yiyi
 * @version 1.0.0
 */
public class Person {
	@Id
	private int tableId;

	/**
	 * koobee id
	 */
	private String userId;

	private String phone;

	private String email;

	/**
	 * 头像的url
	 */
	private String avatar;

	private int sex;

	/**
	 * 昵称
	 */
	private String realName;

	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Override
	public String toString() {
		return "Person [tableId=" + tableId + ", userId=" + userId + ", phone="
				+ phone + ", email=" + email + ", avatar=" + avatar + ", sex="
				+ sex + ", realName=" + realName + "]";
	}

	
}
