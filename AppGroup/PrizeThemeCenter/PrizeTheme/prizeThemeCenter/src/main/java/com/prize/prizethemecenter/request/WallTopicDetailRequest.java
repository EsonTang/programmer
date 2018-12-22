package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/7.
 * 首页专题详情请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/WallpaperInfo/getWallpaperBySpecial",
        builder = DefaultParamsBuilder.class)
public class WallTopicDetailRequest extends  BaseRequest {

    /**请求分类Id*/
    public int specialId;
    /**请求页数*/
    public int pageIndex ;
    /**每次请求数量*/
    public int pageSize ;

    public WallTopicDetailRequest(){

    }

}
