package com.prize.left.page.bean;

import java.io.Serializable;
/***
 * 百度电影信息实体类
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
public class BDMovieItem implements Serializable {
	public BDMovieItem() {
	}
	/**ID*/
	public int id;
	/**片名*/
	public String title;
//	public String name;
//	/**英文名*/
//	public String englishName;
//	/**类型*/
//	public String type;
//	/**导演*/
//	public String director;
//	/**演员*/
//	public String actor;
//	/**时长*/
//	public int length;
//	/**一句话评价*/
//	public String highlight;
//	/**上映地区*/
//	public String state;
//	
//	public int interest;
//	/**评分*/
//	public int score;
	/**图片Url*/
	public String imageUrl;
	
	public String figure;
	
	public String clickUrl;
//	/**电影的制式 eg: 2D, 3D, IMAX*/
//	public String edition;
//	/**来源*/
//	public String from;		
//	/**在售状态*/
//	public int sale_status;	
//	/**发行时间*/
//	public long release_date;
	
	

}
