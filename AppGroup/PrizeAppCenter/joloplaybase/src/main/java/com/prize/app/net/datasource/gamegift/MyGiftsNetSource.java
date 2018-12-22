package com.prize.app.net.datasource.gamegift;

import java.util.ArrayList;

import com.prize.app.beans.GameGift;
import com.prize.app.beans.Page;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetMyGameGiftListReq;
import com.prize.app.net.req.GetMyGameGiftListResp;

public class MyGiftsNetSource
		extends
		AbstractNetSource<MyGiftsData, GetMyGameGiftListReq, GetMyGameGiftListResp> {
	private int giftType;
	private PageBean page;

	public MyGiftsNetSource(int type) {
		giftType = type;
		page = new PageBean();
	}

	public boolean hasMoreGifts() {
		return page.hasNext();
	}

	@Override
	protected GetMyGameGiftListReq getRequest() {
		GetMyGameGiftListReq request = new GetMyGameGiftListReq();
		request.setGameGiftReqType((byte) giftType);
		Page reqPage = PageBean.convertToNetPage(page);
		reqPage.setPageIndex(page.pageIndex + 1);// 请求下一页 ，pageIndex ==
													// 0时，其实请求的是第一页
		request.setPage(reqPage);

		return request;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetMyGameGiftListResp.class;
	}

	@Override
	protected MyGiftsData parseResp(GetMyGameGiftListResp resp) {
		if (null == resp) {
			return null;
		}
		ArrayList<GameGift> respGifts = resp.getGameGiftList();
		MyGiftsData data = new MyGiftsData();
		data.addGifts(respGifts);

		Page respPage = resp.getPage();
		page.setPage(respPage);

		return data;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/getmygamegiftlist";
	}

	@Override
	protected MyGiftsData parseStrResp(String resp) {

		// TODO Auto-generated method stub
		return null;
	}
}
