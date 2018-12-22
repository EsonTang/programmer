package com.prize.left.page.bean;

import java.io.Serializable;

import com.prize.left.page.bean.table.CardType;
/***
 * 卡片实体bean, 包括卡片数据及标题
 * @author fanjunchen
 *
 */
public class CardBean implements Serializable, Cloneable {

	private static final long serialVersionUID = 3L;

	/**已选某个卡片类型对象*/
	public CardType cardType;
	/**卡片数据对象, 这个对象的类型是根据cardType.code来决定的*/
	public Object datas;
	/**是否正在加载中*/
	private boolean isLoading = false;
	
	public int insertPos = -1;
	
	public CardBean() {
		// 
	}

	public boolean isLoading() {
		return isLoading;
	}
	
	public void setLoading(boolean f) {
		isLoading = f;
	}
	
	@Override
	public CardBean clone() throws CloneNotSupportedException {
		
		CardBean c = new CardBean();
		c.cardType = cardType != null ? cardType.clone() : null;
		c.setLoading(isLoading);
		c.insertPos = insertPos;
		
		return c;
	}
}
