package com.prize.left.page.bean;

import java.io.Serializable;

import android.content.Intent;
/***
 * 桌面ICON的实体BEAN, 带intent
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
public class AppBean implements Serializable {

	public AppBean() {
		// TODO Auto-generated constructor stub
	}

	public Intent it;
	
	public String title;
	/**类型, 0 表示快捷方式等*/
	public int type;
}
