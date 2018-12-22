package com.prize.left.page.bean;

import java.io.Serializable;

public class NewsCardItem implements Serializable {

	private static final long serialVersionUID = 8145212667089002691L;
	/**图片URL*/
	public String iconUrl;
	/**标题或名称*/
	public String name;
	/**描述*/
	public String describe;
	/**0 网页, 1: sdk*/
	public int linkType;
	/**链接参数或SDK参数*/
	public String linkParam;
	
	public NewsCardItem() {
		//
	}
	
	
}
