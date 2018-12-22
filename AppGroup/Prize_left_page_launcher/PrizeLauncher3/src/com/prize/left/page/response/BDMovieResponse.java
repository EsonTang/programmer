package com.prize.left.page.response;

import java.util.List;

import com.prize.left.page.bean.BDMovieData;
import com.prize.left.page.bean.BDMovieItem;

/***
 * 百度在映电影响应类
 * @author fanjunchen
 *
 */
public final class BDMovieResponse extends BaseResponse<BDMovieData> {
//	/**哪个接口返回的数据*/
//	public String action;
//	/**总数*/
//	public int total;
//	/**是否还有更多, 0 没有, 1有*/
//	public int has_more;
//	/**每页的数量*/
//	public int page_size;
	
//	/**一点资讯返回的数据*/
//	public List<BDMovieItem> list;
	public BDMovieResponse() {
		data=new BDMovieData();
	}
}
