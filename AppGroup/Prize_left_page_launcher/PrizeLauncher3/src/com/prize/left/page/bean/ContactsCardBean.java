package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 搜索联系人卡片实体
 * @author fanjunchen
 *
 */
public class ContactsCardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public ContactsCardBean() {
		
	}
	
	public List<ContactPerson> items;
}
