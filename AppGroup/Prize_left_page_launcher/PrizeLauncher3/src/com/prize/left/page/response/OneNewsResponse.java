package com.prize.left.page.response;

import java.util.List;

import com.prize.left.page.bean.OneNewsCardBean;
import com.prize.left.page.bean.OneNewsCardItem;

/***
 * 一点资讯新闻响应类
 * @author fanjunchen
 *
 */
public final class OneNewsResponse extends BaseResponse<OneNewsCardBean> {
	/**状态原因, eg: success, failed*/
	public String status;
	/**一点资讯返回的数据*/
	public List<OneNewsCardItem> result;
}
