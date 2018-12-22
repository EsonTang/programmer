package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;
/***
 * 搜索应用卡片实体
 * @author fanjunchen
 *
 */
public class AppsCardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public AppsCardBean() {
		
	}
	
	public List<AppBean> items;
}
