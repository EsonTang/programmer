package com.prize.app.net.datasource.base;

import android.text.TextUtils;

import com.prize.app.beans.GameList;
import com.prize.app.beans.Page;
import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListReq;
import com.prize.app.net.req.GetGameListResp;

/**
 * 获取列表:该列表的ITEM可能是游戏,礼包,公告,子列表等,是混合型列表 如果要获取存游戏的列表,请使用{@link GamePageNetSource}
 * 
 */
public class SimpleGamePageNetSource extends
		AbstractNetSource<GameTypeData, GetGameListReq, GetGameListResp> {

	private String listCode;
	private PageBean page = new PageBean();
	private byte requestListTag = 0;
	private final String TAG = "SimpleGamePageNetSource";
	/** 分类的请求 */
	public static final int LIST_TAG_TYPE = 1;

	public SimpleGamePageNetSource(String listCode) {
		this(listCode, (byte) 0);
	}

	public SimpleGamePageNetSource(String listCode, byte listTag) {
		if (TextUtils.isEmpty(listCode)) {
			throw new RuntimeException("list Code can not null");
		}
		this.listCode = listCode;
		this.requestListTag = listTag;
	}

	@Override
	protected GameTypeData parseResp(GetGameListResp resp) {
		GameTypeData data = new GameTypeData(listCode);

		GameList respList = resp.getGameList();
		Page netPage = resp.getPage();
		if (netPage != null) {
			page.setPage(netPage);
		}
		return data;
	}

	@Override
	protected GetGameListReq getRequest() {
		GetGameListReq req = new GetGameListReq();
		Page netPage = PageBean.convertToNetPage(page);
		req.setPageIndex(page.pageIndex + 1);
		req.setPageSize((short) 20);
		return req;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetGameListResp.class;
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/recommand/list";
	}

	public void setPage(PageBean page) {
		this.page = page;
	}

	public PageBean getPage() {
		return page;
	}

	/**
	 * 判断是否有下一页
	 * 
	 * @return
	 */
	public boolean hasNextPage() {
		return page.hasNext();
	}

	/**
	 * 获得当前页
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return page.getCurrentPage();
	}

	@Override
	protected GameTypeData parseStrResp(String resp) {

		// TODO Auto-generated method stub
		return null;
	}

}
