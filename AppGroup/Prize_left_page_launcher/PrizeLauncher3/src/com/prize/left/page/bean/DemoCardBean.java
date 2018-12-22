package com.prize.left.page.bean;

import java.util.List;

import com.prize.left.page.bean.table.CardType;
/***
 * 示例卡片类型实体类
 * @author fanjunchen
 *
 */
public class DemoCardBean {

	/**示例图片*/
	public static final int DEMO = 0;
	/**未添加的卡片类型*/
	public static final int UNADD = 1;
	/**已添加的卡片类型*/
	public static final int ADD = 2;
	/**未添加的卡片类型(特殊)*/
	public static final int UNADD_SPE = 8;
	
	public int type;
	
	public List<CardType> items;
}
