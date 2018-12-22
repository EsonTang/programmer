package com.prize.prizethemecenter.response;

import com.prize.prizethemecenter.bean.ThemeListData;

/***
 * 主题分类id返回数据
 * @author pengy
 *
 */
public final class ThemeListResponse extends BaseResponse<ThemeListData> {

	public ThemeListResponse(){
        super();
        data = new ThemeListData();
    }
}
