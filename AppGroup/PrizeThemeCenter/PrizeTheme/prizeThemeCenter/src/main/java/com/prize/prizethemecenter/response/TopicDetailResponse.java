package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.TopicDetailData;

/***
 * 首页专题详情请求返回数据
 * @author pengy
 *
 */
public final class TopicDetailResponse extends BaseResponse<TopicDetailData> {

	public TopicDetailResponse(){
        super();
        data = new TopicDetailData();
    }
}
