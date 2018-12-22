package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/14.
 * 自动提示请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/Search/getComboBox",
        builder = DefaultParamsBuilder.class)
public class AutoTipsRequest extends  BaseRequest {
    /**搜索关键字*/
    public String query;
    /**主题：0 壁纸：1 字体：2 */
    public String type;
    public AutoTipsRequest(){
    }
}
