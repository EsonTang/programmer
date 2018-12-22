package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 一点资讯新闻卡片实体
 * @author fanjunchen
 *
 */
public class OneNewsCardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public OneNewsCardBean() {
		
	}
	
	public List<OneNewsCardItem> items;

}
