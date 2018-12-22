package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/2.
 * 首页主题请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/WallpaperInfo/TopicList",
        builder = DefaultParamsBuilder.class)
public class TopicWallRequest extends  BaseRequest {
    public TopicWallRequest(){

    }
}
