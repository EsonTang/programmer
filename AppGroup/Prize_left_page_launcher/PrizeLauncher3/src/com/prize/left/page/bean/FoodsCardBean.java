package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 美食卡片实体
 * @author fanjunchen
 *
 */
public class FoodsCardBean implements Serializable {

	private static final long serialVersionUID = 2L;

	public FoodsCardBean() {
		
	}
	
	public List<FoodsCardItem> items;

}
