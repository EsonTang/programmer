package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/10/13.
 * 首页主题请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/WallpaperInfo/SingleWallpaper",
        builder = DefaultParamsBuilder.class)
public class WallpaperDetailRequest extends  BaseRequest {
    /**请求壁纸的ID*/
    public int wallpaperId ;
    /**每次请求数量*/
    public int pageSize ;
//    /**图片尺寸,宽*高*/
//    public String size;

    public WallpaperDetailRequest(){

    }

}
