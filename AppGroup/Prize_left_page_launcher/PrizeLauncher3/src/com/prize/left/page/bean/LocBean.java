package com.prize.left.page.bean;
/***
 * 存放经纬度
 * @author fanjunchen
 *
 */
public class LocBean {

	public LocBean() {
	}
	/**经度*/
	public double lat;
	/**纬度*/
	public double lng;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(lat).append(',').append(lng);
		return sb.toString();
	}
}
