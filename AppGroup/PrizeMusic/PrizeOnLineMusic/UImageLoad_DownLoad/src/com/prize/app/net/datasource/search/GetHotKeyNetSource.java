package com.prize.app.net.datasource.search;

import java.util.Random;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.AbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetSearchKeywordsReq;
import com.prize.app.net.req.GetSearchKeywordsResp;
import com.prize.app.util.JLog;

public class GetHotKeyNetSource
		extends
		AbstractNetSource<HotKeyData, GetSearchKeywordsReq, GetSearchKeywordsResp> {

	private Random random = new Random();

	/**
	 * 取RGB值
	 * 
	 * @return
	 */
	private int getColorValue() {
		while (true) {
			int value = random.nextInt(255);
			if (value < 128) {
				return value;
			}
		}
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/search/hot";
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		return GetSearchKeywordsResp.class;
	}

	@Override
	protected HotKeyData parseStrResp(String resp) {
		JLog.i("long", resp);
		if (resp != null) {

			return new Gson().fromJson(resp, HotKeyData.class);
		}
		return null;
	}

	@Override
	protected HotKeyData parseResp(GetSearchKeywordsResp resp) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GetSearchKeywordsReq getRequest() {
		GetSearchKeywordsReq req = new GetSearchKeywordsReq();
		return req;
	}

}
