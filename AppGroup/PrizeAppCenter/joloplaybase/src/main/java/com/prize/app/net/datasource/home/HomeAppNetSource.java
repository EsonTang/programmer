package com.prize.app.net.datasource.home;

import android.text.TextUtils;

import com.prize.app.beans.PageBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.GamePageNetSource;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;
import com.prize.app.util.GsonParseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取列表:该列表的ITEM可能是游戏,礼包,公告,子列表等,是混合型列表 如果要获取存游戏的列表,请使用{@link GamePageNetSource}
 */
public class HomeAppNetSource
        extends
        AppAbstractNetSource<PrizeAppsCardData, Map<String, String>, GetGameListResp> {

    protected PageBean page = new PageBean();
    /*
     * 分类的请求
     */
    protected String rankType = null;
    protected String appType = null;
    protected boolean isGame = false;

    public HomeAppNetSource() {
    }

    /*
     *
     * @param rankType 排序类型： downloadTimes - 下载榜 ； hotValue - 飙升榜； latest - 新品榜
     * @param isGame 是否是游戏
     */
    public HomeAppNetSource(String rankType, boolean isGame) {
        this.rankType = rankType;
        this.isGame = isGame;
    }

    @Override
    protected Map<String, String> getRequest() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("pageIndex", page.pageIndex + 1 + "");
        param.put("pageSize", page.pageSize + "");
        if (this.rankType != null) {
            param.put("rankType", this.rankType);
        }
        return param;
    }

    @Override
    protected Class<? extends BaseResp> getRespClass() {
        return GetGameListResp.class;
    }

    @Override
    public String getUrl() {
        return Constants.GIS_URL + "/recommand/focus";// 首页推荐
    }

    public void setPage(PageBean page) {
        this.page = page;
    }

    public PageBean getPage() {
        return page;
    }

    /**
     * 判断是否有下一页
     *
     * @return boolean
     */
    public boolean hasNextPage() {
        return page.hasNext();
    }

    /**
     * 获得当前页
     *
     * @return 当前页的index
     */
    public int getCurrentPage() {
        return page.getCurrentPage();
    }

    @Override
    protected PrizeAppsCardData parseStrResp(String resp) {
        PrizeAppsCardData bean= GsonParseUtils.parseSingleBean(resp,
                PrizeAppsCardData.class);
        if (bean != null) {
            page.pageIndex = bean.getPageIndex();
            page.pageCount = bean.getPageCount();
            page.pageItemCount = bean.getPageItemCount();
            page.pageSize = bean.getPageSize();
        }
        if (isFirstPage() && !TextUtils.isEmpty(this.rankType) && "0".equals(this.rankType)) {
            ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
            HomeRecord record = new HomeRecord();
            record.json = resp;
            record.content_type = HomeRecord.CONTENT_TYPE_LIST;
            records.add(record);
            HomeDataSource.replaceHomeRecords(records,
                    HomeRecord.CONTENT_TYPE_LIST);
        }
        return bean;
    }

    private boolean isFirstPage() {
        return page.getCurrentPage() <= PageBean.FIRST_PAGE;
    }

    public void reSetPagerIndex(int index) {
        page.pageIndex = index;
    }
}
