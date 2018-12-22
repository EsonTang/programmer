/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.app.net.datasource.base;

import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;
import com.prize.app.util.GsonParseUtils;

import java.util.HashMap;
import java.util.Map;

public class CategoryNetSource
        extends
        AppAbstractNetSource<AppGameCategoryData, Map<String, String>, GetGameListResp> {
    private String parentId = null;

    /**
     * 是否请求游戏
     *
     * @param parentId 根类别 ： 1-软件 2-游戏
     */
    public CategoryNetSource(String parentId) {
        this.parentId = parentId;
    }

    @Override
    protected Map<String, String> getRequest() {
        // GetGameListReq req = new GetGameListReq();
        Map<String, String> param = new HashMap<String, String>();
        if (this.parentId != null) {
            param.put("parentId", this.parentId);
        }
        return param;
    }

    @Override
    protected Class<? extends BaseResp> getRespClass() {

        // TODO Auto-generated method stub
        return GetGameListResp.class;
    }

    @Override
    protected AppGameCategoryData parseStrResp(String resp) {
        return GsonParseUtils.parseSingleBean(resp, AppGameCategoryData.class);
    }

    @Override
    public String getUrl() {

        return Constants.GIS_URL + "/category/newCatList";
    }

}
