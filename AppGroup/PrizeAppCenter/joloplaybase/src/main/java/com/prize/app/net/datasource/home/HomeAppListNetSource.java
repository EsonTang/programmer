package com.prize.app.net.datasource.home;


import java.util.HashMap;
import java.util.Map;

public class HomeAppListNetSource extends HomeAppNetSource {

    /**
     * 是否请求游戏
     *
     * @param appType  应用类型： 0-不区分； 1-软件； 2-游戏
     * @param isGame 是否来自游戏界面
     */
    public HomeAppListNetSource(String appType, boolean isGame) {
        super(appType, isGame);
        super.appType = appType;
    }

    @Override
    protected Map<String, String> getRequest() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("pageIndex",String.valueOf(page.pageIndex + 1));
        param.put("pageSize", String.valueOf(100));
        if (super.appType != null) {
            param.put("appType", appType);

        }
        return param;
    }

}
