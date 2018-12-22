package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.ThemeCommitBean.DataBean;

/***
 * 首页返回数据
 * @author pengy
 *
 */
public final class ThemeCommitResponse extends BaseResponse<DataBean> {
	public ThemeCommitResponse(){
        super();
        data = new DataBean();
    }
}
