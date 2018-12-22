package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class BDGroupItem implements Serializable {

	public BDGroupItem() {
	}
	/**ID*/
	public int id;
	/**图片*/
	public String imageUrl;
	/**详情*/
	public String clickUrl;
	/**名称或标题*/
	public String title;
	/**描述*/
	public String subTitle;
	/**当前价格*/
	public String price;
	/**市场价*/
	public String originalPrice;
	/**距离*/
	public String distance;
	/**评分*/
	public String score;
//	/**ID*/
//	public int deal_id;
//	/**名称或标题*/
//	public String title;
//	/**描述*/
//	public String desc;
//	/**图片*/
//	public String image;
//	/**小图片*/
//	public String tiny_image;
//	/**市场价*/
//	public float market_price;
//	/**当前价格*/
//	public float current_price;
//	/**售出数量*/
//	public int sold_cnt;
//	/**评分*/
//	public float score;
//	/**分数排名*/
//	public int score_top;
//	/**评论数*/
//	public int comment_cnt;
//	/**开始时间*/
//	public long start_time;
//	/**结束时间*/
//	public long end_time;
//	/**距离*/
//	public float distance;
//	/**店铺数*/
//	public int shop_total;
//	/**店铺列表*/
//	public List<BDGroupShopBean> shop_list;
//	/**来源ID*/
//	public String provider_id;
//	/**来源名*/
//	public String provider_name;
}
