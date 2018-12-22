package com.prize.left.page.response;

import java.util.ArrayList;
import java.util.List;

import com.prize.left.page.bean.HotWordBean;
import com.prize.left.page.bean.table.HotBox;
import com.prize.left.page.bean.table.HotWordTable;

/***
 * 百度热词响应类
 * @author fanjunchen
 *
 */
public final class HotWordResponse extends BaseResponse<HotWordBean> {

	public HotWordResponse() {
		super();
		data = new HotWordBean();
		data.list = new ArrayList<>();
		data.box = new HotBox();
		
	}
	
	
}
