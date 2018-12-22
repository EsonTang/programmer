package com.prize.prizenavigation.utils;

/**
 * 提示app通用常量接口
 * Created by liukun on 2017/3/2.
 */
public interface IConstants {

    boolean ISDEBUG = false;
    /**数据库名称*/
    String DB_NAEM = "prizeNav.db";
    /**数据库版本号*/
    int DB_VERSION = 1;

    /**请求数据测试地址*/
    String NAVIDATAS_TEST_URL = "http://testnewapi.szprize.cn/Prompt/Prompt/getList";
    /**请求数据正式地址*/
    String NAVIDATAS_FORMAL_URL = "http://newapi.szprize.cn/Prompt/Prompt/getList";

    /**提交点赞测试地址*/
    String NAVIUPDOWN_TEST_URL = "http://testnewapi.szprize.cn/Prompt/Prompt/setUpDown";
    /**提交点赞正式地址*/
    String NAVIUPDOWN_FORMAL_URL = "http://newapi.szprize.cn/Prompt/Prompt/setUpDown";

    /**NaviFragment数据flag*/
    String NAVIFRAGMENT_DATAS_FLAG = "navifragment_datas";
    /**viewpager缓存数*/
    int VIEWPAGER_OFF_SCREEN_PAGE_LIMIT = 0;
    /**fragment延迟加载时长*/
    int FRAGMENT_DELAYMILLIS = 500;

    String NAVIFRAGMENT_DATAS_TOTAL_FLAG ="navifragment_total";
}
