package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/18.
 * 搜索提示请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/ThemeInfo/SearchWord",
        builder = DefaultParamsBuilder.class)
public class SearchOriginRequest extends  BaseRequest {
    /**搜索类型 主题：0 壁纸：1 字体：2*/
    public String type;
    public SearchOriginRequest(){
    }
}
