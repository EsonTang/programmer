package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

public class Categories implements Serializable {
	
	
	public Categories() {
		
	}

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;

	public String id;

	/** 显示名称 */
	public String typeName;
	/** icon地址 */
	public String icon;

	public ArrayList<CategoryContent> tags;


}
