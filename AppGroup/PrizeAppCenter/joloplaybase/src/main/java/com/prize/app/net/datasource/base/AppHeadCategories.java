package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

/***
 * 应用页头部分类
 */
public class AppHeadCategories implements Serializable {


	public AppHeadCategories() {
		
	}

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 1L;

	public String id;

	/** 分类id */
	public String catId;
	/** 分类名称 */
	public String catName;
	/** 显示名称(分类名称) */
	public String catTypeId;
	/** 显示名称(分类名称) */
	public int cIdpos;
	/** 显示名称(分类名称) */
	public String pCatName;
	/** 显示名称(分类名称) */
	public String showText;
	/** icon地址 */
	public String imageUrl;

	public ArrayList<CategoryContent> tags;


}
