package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 新闻卡片实体
 * @author fanjunchen
 *
 */
public class NewsCardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public NewsCardBean() {
		
	}
	
	public List<NewsCardItem> items;

}
