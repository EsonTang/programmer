package com.prize.app.beans;

import java.io.Serializable;

/**
 * 搜索关键词
 * 
 * @author prize
 * @version 1.0 2013-2-7
 *
 */
public class SearchKeyword implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9110134080305304096L;
	private String sKeyword;// 搜索关键词
	private Short font;// 字体
	private String color;// 颜色 类似于 #a0522d

	public String getsKeyword() {
		return sKeyword;
	}

	public void setsKeyword(String sKeyword) {
		this.sKeyword = sKeyword;
	}

	public Short getFont() {
		return font;
	}

	public void setFont(Short font) {
		this.font = font;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SearchKeyword) {
			// 带字体的关键词比较
			SearchKeyword sk = (SearchKeyword) o;
			if (sk != null && sk.getsKeyword() != null) {
				return sk.getsKeyword().toLowerCase()
						.equals(sKeyword.toLowerCase());
			}
		}
		return false;
	}

}
