package com.prize.music.bean;

import java.io.Serializable;

/**
 * @Description:[]
 * @UpdateUser: UpdateUser
 * @UpdateRemark: [说明本次修改内容]
 * @version [V1.0]
 */

public class PopBean implements Serializable {
	private static final long serialVersionUID = -4988162845158690288L;
	private String areaId;
	private String areaName;

	/**
	 * @return the areaId
	 */
	public String getAreaId() {
		return areaId;
	}

	/**
	 * @param areaId
	 *            the areaId to set
	 */

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public PopBean(String areaId, String areaName) {
		this.areaId = areaId;
		this.areaName = areaName;
	}

	/**
	 * @return the areaName
	 */
	public String getAreaName() {
		return areaName;
	}

	/**
	 * @param areaName
	 *            the areaName to set
	 */

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
}
