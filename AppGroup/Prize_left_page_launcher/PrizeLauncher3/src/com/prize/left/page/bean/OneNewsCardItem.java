package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;

public class OneNewsCardItem implements Serializable {

	private static final long serialVersionUID = 8145212667089002693L;
	/**标题或名称*/
	public String title;
	/**点击链接*/
	public String url;
	/**时间*/
	public String date;
	/**文章ID*/
	public String docid;
	/**来源*/
	public String source;
	/**图片数组*/
	public List<String> images;
	/**单个图片,用于'美女'子类*/
	public String image;
	/**摘要,用于'段子 (10)'子类, 16 没有内容*/
	public String summary;
	/**美女图片高*/
	public int height;
	/**美女图片宽*/
	public int width;
	
	public OneNewsCardItem() {
		//
	}
	
	
}
