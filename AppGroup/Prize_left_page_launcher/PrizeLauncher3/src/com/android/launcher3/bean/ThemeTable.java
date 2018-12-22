package com.android.launcher3.bean;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import com.prize.left.page.bean.table.ITable;

/**
 * 主题
 * 
 * @author zhouerlong
 * 
 */
@SuppressWarnings("serial")
@Table(name = ThemeTable.table_name)
public class ThemeTable implements ITable, Serializable {

	static final String table_name = "t_theme_table";

	/** id */
	@Column(name = "themeId", isId = true)
	public String themeId;
	/** name */
	@Column(name = "name")
	public String name;
	/** iconUrl */
	@Column(name = "iconUrl")
	public String iconUrl;
	/** previewUrl */
	@Column(name = "previewUrl")
	public String previewUrl;
	/** 类型排序号 */
	@Column(name = "_sort")
	public int sort;
	/** 主题URl */
	@Column(name = "themeUrl")
	public String themeUrl;
	/** iconPath */
	@Column(name = "iconPath")
	public String iconPath;
	/** preview Path */
	@Column(name = "previewPath")
	public String previewPath;
	/** 主题路径 */
	@Column(name = "themePath")
	public String themePath;
	/** withinSystem */
	@Column(name = "withinSystem")
	public int withinSystem;
	/** isSelected */
	@Column(name = "isSelected")
	public int isSelected;

	@Override
	public String getTableName() {
		return table_name;
	}

	@Override
	public String toString() {
		return "ThemeTable [themeId=" + themeId + ", name=" + name
				+ ", iconUrl=" + iconUrl + ", previewUrl=" + previewUrl
				+ ", sort=" + sort + ", themeUrl=" + themeUrl + ", iconPath="
				+ iconPath + ", previewPath=" + previewPath + ", themePath="
				+ themePath + ", withinSystem=" + withinSystem
				+ ", isSelected=" + isSelected + ", getTableName()="
				+ getTableName() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	public void toContentValues() {

	}
}
