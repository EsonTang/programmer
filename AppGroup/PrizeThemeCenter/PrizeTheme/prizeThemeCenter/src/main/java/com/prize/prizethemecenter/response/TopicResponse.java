package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.TopicData;

/***
 * 主题专题返回数据
 * @author pengy
 *
 */
public final class TopicResponse extends BaseResponse<TopicData> {
	public TopicResponse(){
        super();
        data = new TopicData();
    }
}
