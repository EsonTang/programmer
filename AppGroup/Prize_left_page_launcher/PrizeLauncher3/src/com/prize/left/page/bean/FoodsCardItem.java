package com.prize.left.page.bean;

import java.io.Serializable;
/***
 * 美食(团购)数据项实体
 * @author fanjunchen
 *
 */
public class FoodsCardItem implements Serializable {

	private static final long serialVersionUID = 8145212667089002691L;
	/**左边图标URL*/
	public String iconUrl;
	/**标题或名称*/
	public String name;
	/**描述*/
	public String describe;
	/**0 网页, 1: sdk*/
	public int linkType;
	/**内容类型, 1: 美食, 2:团购*/
	public int type;
	/**连接参数或包名类名*/
	public String linkParam;
	/**价格*/
	public String price;
	/**评分*/
	public String grade;
	/**距离*/
	public String distance;
	
	public FoodsCardItem() {
		//
	}
	
	
}
