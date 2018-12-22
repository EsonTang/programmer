package com.prize.onlinemusibean;

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
	private int imageId;
	private boolean enable;

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

	public PopBean(String areaId, String areaName, boolean enable) {
		this.areaId = areaId;
		this.areaName = areaName;
		this.enable = enable;
	}

	
	public PopBean(String areaId, String areaName, int imageId) {
		this.areaId = areaId;
		this.areaName = areaName;
		this.imageId = imageId;
	}

	public PopBean(String areaId, String areaName, int imageId, boolean enable) {
		this.areaId = areaId;
		this.areaName = areaName;
		this.imageId = imageId;
		this.enable = enable;
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

	/**
	 * @return the imageId
	 */
	public int getImageId() {
		return imageId;
	}

	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	/**
	 * @return the enable
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	
}
