package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.DownloadHistoryBean;

/**
 * Created by Administrator on 2016/9/12.
 */
public class DownloadHistoryResponse extends BaseResponse<DownloadHistoryBean.DataBean> {

	public DownloadHistoryResponse() {
		super();
		data = new DownloadHistoryBean.DataBean();
	}
}
