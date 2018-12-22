package com.prize.app.constants;

import android.os.Environment;

import java.io.File;

public class Constants {
    /**
     * 正式环境URL
     */
    public static final String GIS_URL = "http://appstore.szprize.cn/appstore";
    public static final String GIS_URL_DEVICE = "http://ics.szprize.cn/ics";
    public static final String GIS_ICSVIP_URL = "http://icsvip.szprize.cn/ics";
    public static final String BASE_CLOUD_URL = "http://cloud.szprize.cn/";
    /*HttpManagerImplement 类中requestSync方法有判断host字段***/
    public static final String NEW_BASE_URL = "http://testaccess.szprize.cn/showdown.php?";
     /*HttpManagerImplement 类中requestSync方法有判断host字段（access.szprize.cn）***/
    // public static final String NEW_BASE_URL="http://access.szprize.cn/showdown.php?";


//	public static final String GIS_URL_DEVICE = "http://192.168.1.187:8080/ics";
//	public static final String GIS_URL_DEVICE = "http://192.168.0.126:8080/ics";
//	public static final String GIS_ICSVIP_URL = GIS_URL_DEVICE;
    /**
     * 测试环境URL
     */
//	public static final String GIS_URL = "http://192.168.1.148:8080/appstore";

//	public static final String GIS_URL = "http://101.200.187.142:8080/appstore";
//	public static final String GIS_URL = "http://192.168.0.126:8080/appstore";
//	public static final String GIS_URL = "http://192.168.0.154:8080/appstore";
//	public static final String GIS_URL = "http://192.168.1.187:8080/appstore";
//	public static final String BASE_CLOUD_URL = "http://192.168.1.148:8080";

    // -------------------------------------------常量，不作修改------------------------------------------------//


    public static final String POINT_RULE_URL = GIS_URL
            + "/assets/rule/index.html";
    public static final String SYSTEM_UPGRADE_URL = GIS_URL
            + "/appinfo/upgrade"; // 系统版自升级url
    public static final String THIRD_UPGRADE_URL = GIS_URL
            + "/appinfo/upgradeSpecial"; // 三方版自升级url


    /****接口安全校验，先请求改接口来获取一个pid*****/
    public static final String PID_URL = GIS_URL_DEVICE + "/api/pid";
    /****此接口需要与pid接口联用*****/
    public static final String UUID_URL = GIS_URL_DEVICE + "/api/uuid";


    // 下载模块的常量
    public static final String QES_ACCEPT_CONTENT_TYPE = "application/octet-stream,application/vnd.android.package-archive";
    public static final String QES_UNACCEPT_CONTENT_TYPE = "text/html,text/plain";
    public static final String ANDROID_APP_SUFFIX = ".apk";
    public static final String PRIZE_TEM_FILE_SUFFIX = ".prize";
    private static final String PRIZEAPPCENTER = "PrizeAppCenter";
    /****实际发布后****/
    public static final int PUSH_FOR_TIME = 60 * 60 * 4;
    public static final int TRASH_PUSH_FOR_TIME = 60 * 60 * 48;
    public static final int PAGE_SIZE = 20;
    public static String DOWNLOAD_FOLDER_NAME = "download";
    public static final String DOWNLOAD_FILE_NAME = PRIZEAPPCENTER
            + ANDROID_APP_SUFFIX;
    public static final String APKFILEPATH = new StringBuilder(Environment
            .getExternalStorageDirectory().getAbsolutePath())
            .append(File.separator).append(Constants.DOWNLOAD_FOLDER_NAME)
            .append(File.separator).append(Constants.DOWNLOAD_FILE_NAME)
            .toString();

    public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
    public static final String APP_MD5 = "appMD5";
    public static final String KEY_CHECK_MESSAGE_TIME = "check_message_time";
    public static final String KEY_MESSAGE_NOTITY_TIME = "notify_message_time";
    public static final String ACTION_LOGIN_SUCCESS = "action.prize.login.success";
    public static final String KEY_WELFARE_GOT = "prize_welfare_got_point";
    public static final String KEY_WELFARE_GOT_GIFT = "prize_welfare_got_gift";

    //记录垃圾清理的时间，1分钟内不能再次清理
    public static final String KEY_TRASH_CLEAR_TIME = "trash_clear_time";

    // ///数据统计event
    public static final String E_ENTER_APP_DETAIL = "E_ENTER_APP_DETAIL";
    // 统计参数命名
    public static final String EVT_ENTER_APP_DETAIL_ID = "ENTER_APP_DETAIL_ID";


    /**
     * 热门专题 游戏游戏带我飞
     */
    public static final String E_CLICK_APP_TOPIC = "E_CLICK_APP_TOPIC";
    public static final String EVT_CLICK_APP_TOPIC_ID = "CLICK_APP_TOPIC_ID_";

    /**
     * 热门精选
     */
    public static final String ACTION_UNREAD_CHANGED = "com.prizeappcenter.action.UNREAD_CHANGED";
    public static final String EXTRA_UNREAD_NUMBER = "com.prizeappcenter.intent.extra.UNREAD_NUMBER";

    /**
     * 首页卡片类型
     */
    public static final String WEB = "web";
    public static final String TOPIC = "topic";
    public static final String RANK = "rank";
    public static final String APPSIN = "appsin";
    public static final String APP = "app";
    public static final String CATSIN = "catsin";
    public static final String MATTS = "matts";
    /**
     * 2.6新增期刊类型
     */
    public static final String HOTTEST = "hottest";
    public static final String VIDEO = "video";
    public static final String NOTOPIC = "notopic";

    public static final String LAST_MODIFY = "last-modify";

    public static final String KEY_TID = "persist.sys.tid";
    public static final int SYNC_SUCESS = 0;
    public static final int SYNC_ERROR = 1;

    public static final String SHIELDPACKAGES = "shieldPackages";
    /**
     * 预装白名单
     **/
    public static final String PREALOADS = "prealoads";

    public final static String FROM = "from";
    public final static String[] BROADCAST_AD_TYPES = {"topic", "web", "app", "giftcenter", "pointapplist", "pointgoods", "app_download", "detail_download"};

    /**
     * 更新安装
     */
    public final static String UPDATE_INSTALL = "update_install";
    /**
     * 下载安装
     */
    public final static String DOWNLOAD_INSTALL = "download_install";
    /**
     * 手机启动时间
     */
    public final static String PHONE_BOOOT_TIME = "phone_booot_time";
    /**
     * 刷新间隔时间
     */
    public final static long REFRESH_TIME = 5 * 60 * 1000L;


    /**
     * FOCUS_应用类型
     */
    public static final int TYPE_FOCUS_APP = 0;
    /**
     * FOCUS_网页类型
     */
    public static final int TYPE_FOCUS_WEB = 1;
    /**
     * FOCUS_专题类型
     */
    public static final int TYPE_FOCUS_TOPIC = 2;
    /**
     * FOCUS_榜单类型
     */
    public static final int TYPE_FOCUS_RANK = 3;
    /**
     * FOCUS_雷达类型
     */
    public static final int TYPE_FOCUS_APPSIN = 4;
    /**
     * FOCUS_小分类气泡类型
     */
    public static final int TYPE_FOCUS_CATSIN = 5;
    /**
     * FOCUS_田字格类型
     */
    public static final int TYPE_FOCUS_MATTS = 6;
    /**
     * 应用列表类型
     */
    public static final int TYPE_APP_LIST = 7;
    /**
     * FOCUS_期刊类型
     */
    public static final int TYPE_FOCUS_HOTTEST = 8;
    /**
     * FOCUS_视频类型
     */
    public static final int TYPE_FOCUS_VIDEO = 9;
    /**
     * FOCUS_无背景专题类型
     */
    public static final int TYPE_FOCUS_NOTOPIC = 10;
    /**
     * 启动页的界面名称
     */
    public final static String STARTPAGE_GUI = "startPage";

    /**
     * 首页的界面名称
     */
    public final static String HOME_GUI = "home";
    /**
     * 游戏页的界面名称
     */
    public final static String GAME_GUI = "game";
    /**
     * 应用页的界面名称
     */
    public final static String APP_GUI = "app";
    /**
     * 榜单页的界面名称
     */
    public final static String RANK_GUI = "rank";
    /**
     * 分类列表页的界面名称
     */
    public final static String CATEGORY_GUI = "category";
    /**
     * 抽屉
     */
    public final static String DRAWER = "drawer";
    /**
     * 赚取积分的界面名称
     */
    public final static String EARNPOINTS_GUI = "point";
    public final static String LIST = "list";
    public final static String FOCUS = "focus";
    /**
     * 游戏页的界面名称
     */
    public final static String APPDETAIL_GUI = "appdetail";
    /**
     * 搜索结果的界面名称
     */
    public final static String SEARCH_RESULT_GUI = "search";
    /**
     * 详情标签搜索结果的
     */
    public final static String TAG_RESULT_GUI = "tag_result";
    /**
     * 自定义统计事件下载完成事件名称
     */
    public final static String EVEN_NAME_DOWNLOAD = "download";
    /**
     * 自定义统计事件下载完成事件名称
     */
    public final static String EVEN_NAME_NEWDOWNLOAD = "newdownload";
    /**
     * 自定义统计事件曝光事件名称
     */
    public final static String EVEN_NAME_NEWEXPOSURE = "newExposure";
    /**
     * 自定义统计事件下载更新完成事件名称
     */
    public final static String EVEN_NAME_UPDATE = "update";

    /**
     * 自定义统计事件360打点点击
     */
    public final static String EVEN_NAME_BACKPARAMS = "onClickBackParams";
}
