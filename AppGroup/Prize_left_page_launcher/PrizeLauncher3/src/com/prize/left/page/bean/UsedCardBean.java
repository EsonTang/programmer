package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 最近使用 实体
 * @author fanjunchen
 *
 */
public class UsedCardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public UsedCardBean() {
		
	}
	
	public List<ContactPerson> items;
	
	public List<AppBean> appDatas;
}
