package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.MineThemeBean;

/**
 * Created by Fanghui on 2017/1/13.
 */
public class MineThemeResponse extends BaseResponse<MineThemeBean.DataBean> {

    public MineThemeResponse() {
        super();
        data = new MineThemeBean.DataBean();
    }
}
