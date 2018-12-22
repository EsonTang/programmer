package com.prize.left.page.bean;

import java.io.Serializable;
/***
 * 百度suggestion地址信息
 * @author fanjunchen
 *
 */
public class AddrBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public AddrBean() {
	}
	/**地名*/
	public String name;
	/**城市名*/
	public String city;
	/**所属区*/
	public String district;
	/**城市编号*/
	public String cityid;
	/**唯一编号*/
	public String uid;
	/**地址信息*/
	public LocBean location;
	
	public String getAddrDistrict() {
		if (city != null && !city.equals(district))
			return city + district;
		else
			return district;
	}
}
