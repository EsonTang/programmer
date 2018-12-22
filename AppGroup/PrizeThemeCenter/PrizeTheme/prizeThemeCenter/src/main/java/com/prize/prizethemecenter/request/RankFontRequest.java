package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/10/11.
 * 字体排行请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/FontInfo/RankList",
        builder = DefaultParamsBuilder.class)
public class RankFontRequest extends  BaseRequest {

    /**请求榜单类型 payList - 付费榜 ；
     freeList - 免费榜；
     latest - 新品榜 */
    public String rankType;
    /**请求页数*/
    public int pageIndex ;
    /**每次请求数量*/
    public int pageSize ;
    public RankFontRequest(){

    }

}
