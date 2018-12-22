package com.prize.appcenter.bean;

import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.WelfareBean;
import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏页头布局数据 服务器返回数据
 * @创建者 聂礼刚
 * @创建者 2017/3/31
 * @描述
 */
public class GameGatherData implements Serializable {
    private static final long serialVersionUID = 6780078392418032561L;
    /**导航**/
    public ArrayList<HomeAdBean> ads = new ArrayList<>();
    /**玩家福利*/
    public GameWelfare gamewelfare;
    /**精彩游戏**/
    public Wonderful wonderful;
    /**新游尝鲜**/
    public GameComment gamecomment;
    /**应用页增加头部分类 3.0新增**/
    public ArrayList<AppHeadCategories> categorys = new ArrayList<AppHeadCategories>();
    /**
     * 玩家福利
     */
    public static class GameWelfare{
        public List<WelfareBean> welfares = new ArrayList<>();
        public String title;
    }
    /**
     * 精彩游戏
     */
    public static class Wonderful{
        public String title;
        public List<AppsItemBean> apps = new ArrayList<>();
    }
    /**
     * 新游尝鲜*
     */
    public static class GameComment{
        public List<GameCommentBean> comments = new ArrayList<>();
        public String title;
    }
}
