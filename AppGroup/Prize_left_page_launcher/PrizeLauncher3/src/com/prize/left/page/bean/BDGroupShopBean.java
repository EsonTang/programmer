package com.prize.left.page.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BDGroupShopBean implements Serializable {

	public BDGroupShopBean() {
		// TODO Auto-generated constructor stub
	}
	/**店铺ID*/
	public int shop_id;
	
	public double lat;
	
	public double lng;
	/**距离*/
	public float distance;
	/**来源ID*/
	public String provider_id;
	/**来源名*/
	public String provider_name;
}
