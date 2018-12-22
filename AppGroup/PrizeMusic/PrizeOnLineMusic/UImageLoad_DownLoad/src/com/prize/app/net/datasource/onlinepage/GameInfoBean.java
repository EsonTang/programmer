package com.prize.app.net.datasource.onlinepage;

import com.prize.app.net.AbstractNetData;
 
/**
 * 游戏相关信息的配置bean
 * 新闻入口图标
 * 礼包入口图标
 * @author prize
 *
 */
public class GameInfoBean extends AbstractNetData{
	/**新闻的图标URL，这个图标是入口图标*/
	private String newsImgUrl;
	/**礼包的图标URL, 这个图标是入口图标*/
	private String giftsImgUrl;
	
	public String getNewsImgUrl() {
		return newsImgUrl;
	}
	public void setNewsImgUrl(String newsImgUrl) {
		this.newsImgUrl = newsImgUrl;
	}
	public String getGiftsImgUrl() {
		return giftsImgUrl;
	}
	public void setGiftsImgUrl(String giftsImgUrl) {
		this.giftsImgUrl = giftsImgUrl;
	}
}
