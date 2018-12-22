package com.prize.app.util;

import android.content.Context;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;


/**
 * 腾讯移动分析 MTAUtil
 * <p/>
 * longbaoxiu 2016-7-5 下午5:24:05
 *
 * @version 1.0.0
 */
public class MTAUtil {
    private static final String DETAIL = "Detail";
    private static final String CLICKDOWNLOAD = "ClickDownload";
    //	private static final String CLICKSHARE = "ClickShare";
    private static final String CLICKFIRSTDOWNLOAD = "ClickFirstDownload";
    private static final String TIMESFIRSTDOWNLOAD = "TimesFirstDownload";
    private static final String VERSIONUPDATEDOWNLOAD = "VersionUpdateDownload";
    private static final String SEARCHKEYWORD = "SearchKeyWord";
    private static final String SEARCHORIGINAL = "onSearchOriginal";
    private static final String CLICKMENU = "ClickMenu";
    private static final String CLICKPAGERECOMMENDBANNER = "ClickPageRecommendBanner";
    //	private static final String CLICKPAGEAPPLICATIONBANNER = "ClickPageApplicationBanner";
//	private static final String CLICKPAGEGAMEBANNER = "ClickPageGameBanner";
    private static final String CLICKPAGERECOMMENDENTRANCE = "ClickPageRecommendEntrance";
    //	private static final String CLICKPAGEAPPLICATIONENTRANCE = "ClickPageApplicationEntrance";
//	private static final String CLICKPAGEGAMEENTRANCE = "ClickPageGameEntrance";
//	private static final String CLICKHOTRANK = "ClickHotRank";
    private static final String CLICKNAVUSERPAGE = "ClickNavUserPage";
    private static final String CLICKNAVSEARCH = "ClickNavSearch";
    private static final String CLICKNAVDOWNLOADMANAGE = "ClickNavDownloadManage";
    //    private static final String CLICKCARDAREA = "ClickCardArea";
    private static final String DOWNLOADSUCCESS = "DownloadSuccess";
    private static final String INSTALLSUCESSNOTUPDATE = "installSucessNotUpdate";
    //    private static final String CLICKPAGEGAMETOPADS = "ClickPageGameTopAds";
    private static final String CLICKPAGEAPPLICATIONTOPADS = "ClickPageApplicationTopAds";
    //    private static final String CLICKCANCELFIRSTDOWNLOAD = "ClickCancelFirstDownload";
    private static final String CLICKHOTTESTAPPDETAIL = "HottestAppDetailClick";
    private static final String CLICKHOTTESTSEEMORE = "HottestSeeMoreClick";
    private static final String ONCLICKHOMECATEGORYTOPIC = "onClickHomeCategoryTopic";
    private static final String ONCLICKHOMECATEGORYGAME = "onClickHomeCategoryGame";
    private static final String ONCLICKHOMECATEGORYAPP = "onClickHomeCategoryApp";

    /******
     * start add by longbaoxiu 20160524 1.7.release
     ***/
//	private static final String CLICKMYSORT = "clickmysort";
    private static final String CLICKAPPUPDATE = "clickappupdate";
    //	private static final String CLICKINSTALLRECORD = "clickinstallrecord";
    private static final String ONCLICKUNINSTALL = "onClickUninstall";
    private static final String ONCLICKAPPSYNCASSISTANT = "onClickAppSyncAssistant";
    private static final String ONCLICKFEEDBACK = "onClickFeedback";
    private static final String CLICKDOWNLOADQUEUE = "clickdownloadqueue";
    //    private static final String CLICKPAGERECOMMENDLISTTOP4 = "ClickPageRecommendListTop4";
//    private static final String CLICKPAGEGAMELISTTOP8 = "ClickPageGameListTop8";
//    private static final String CLICKPAGEAPPLICATIONLISTTOP8 = "ClickPageApplicationListTop8";
    private static final String CLICKSEARCHHOTKEY = "clicksearchhotkey";
    private static final String CLICKWELCOMEADS = "clickWelcomeAds";
    private static final String ONCLICKCATEGORY = "onClickCategory";
    private static final String ONCLICKRELA_ALLLIKE = "onClickRela_AllLike";
    private static final String ONCLICKGAMERANK = "onclickgamerank";
    private static final String ONCLICKAPPRANK = "onclickapprank";
    private static final String ONCLICKGAMECATEGORYTAB = "onclickgamecategorytab";
    private static final String ONCLICKAPPCATEGORYTAB = "onclickappcategorytab";
    private static final String APPDOWNLOADSUCCESS = "appdownloadsuccess";
    private static final String ONCLICKSETTINGS = "onClickSettings";
    //    private static final String ONCLICKHOMEPAGEHOTLIST = "onClickHomePageHotList";
    private static final String ONCLICKHOMEPAGEREQUIRELIST = "onClickHomePageRequireList";
    private static final String ONCLICKHOMEPAGENEWPRODUCTORLIST = "onClickHomePageNewProductorList";
    private static final String ONPOINTDOWNLOADSUCCESS = "onPointDownloadSuccess";
    private static final String ONPOINTDOWNLOADINSTALL = "onPointDownloadInstall";
    private static final String ONEARN_POINTS = "onEarn_points";
    private static final String ONPOINTS_MALL = "onPoints_mall";
    private static final String ONCONVERT_RECORDS = "onConvert_records";
    private static final String ONCLICKSEARCHCHANGE = "onClickSearchChange";
    //    private static final String ONCLICKSEARCHRECOMMAND = "onClickSearchRecommand";
    private static final String ONCLICKFEEDBACKANDHELP = "onClickFeedBackAndHelp";
    private static final String ONCLICKSINGGAMEFIRSTITEM = "onClickSingGameFirstItem";
    private static final String ONCLICKSINGGAMESECONDITEM = "onClickSingGameSecondItem";
    private static final String ONCLICKDOWNLOADQUEENNODATA = "onClickDownLoadQueenNOData";
    private static final String ONCLICKDOWNLOADQUEENHAVEDATA = "onClickDownLoadQueenHaveData";
    private static final String ONCLICKSEARCHRESULTLABEL = "onClickSearchResultLabel";
    private static final String ONCLICKHOMEPOSITIONLIST = "onclickhomepositionlist";
    private static final String ONCLICKHOMELIST = "onclickhomelist";
    private static final String ONCLICKAPPPOSITIONLIST = "onclickapppositionlist";
    private static final String ONCLICKAPPLIST = "onclickapplist";
    private static final String ONCLICKGAMEPOSITIONLIST = "onclickgamepositionlist";
    private static final String ONCLICKGAMELIST = "onclickgamelist";
    private static final String ONCLICKHOMESINGPIC = "onclickhomesingpic";
    private static final String ONCLICKAPPSINGPIC = "onclickappsingpic";
    private static final String ONCLICKGAMESINGPIC = "onclickgamesingpic";
    private static final String ONCLICAPPSIN = "onclicappsin";
    private static final String ONCLICMATTS = "onclicmatts";
    private static final String ONCLICHOMEFOCUSTOPIC = "onclicHomeFocusTopic";
    private static final String ONCLICAPPFOCUSTOPIC = "onclicAppFocusTopic";
    private static final String ONCLICAPPFOCUSNOPICTOPIC = "onclicappfocusnopictopic";
    private static final String ONCLICGAMEFOCUSTOPIC = "onclicGameFocusTopic";
    private static final String ONCLICGAMEFOCUSNOPICTOPIC = "onclicGameFocusNoPicTopic";
    //    private static final String ONCLICGAMEAPPRANK = "onclicGameAppRank";
    private static final String ONCLICAPPCARDCATSIN = "onclicAppCardCatsin";
    private static final String ONCLICGAMECARDCATSIN = "onclicGameCardCatsin";
    private static final String ONUNINSTALLCMD = "onUninstallCMD";
    private static final String ONINSTALLCMD = "onInstallCMD";
    private static final String ONOPENAPPCMD = "onOpenAppCMD";
    private static final String ONCLICKREQUIREMENU = "onClickRequireMenu";
    private static final String ONCLICKREQUIRECLASSLIST = "onClickRequireClassList";
    private static final String ONCLICKPUSH = "onClickPush";
    private static final String ONCLICKPUSHONEKEYDOWN = "onClickPushOneKeyDown";
    private static final String ONCLICKAPPDETAILBTNDOWN = "onClickAppDetailBtnDown";
    private static final String ONCLICKHOMEUPDATEBTN = "onClickHomeUpdateBtn";
    private static final String ONHOMECONERCLICKED = "onHomeConerClicked";
    private static final String ONHOMEBROADCASTCLICKED = "onHomeBroadcastClicked";
    private static final String ONPUSHARRIVE = "onPushArrive";
    private static final String ONSIGNINCLICKED = "onSignInClicked";
    private static final String ONMSGCENTERCLICKED = "onMsgCenterClicked";
    private static final String ONLITTLEPAPERCLICKED = "onLittlePaperClicked";
    private static final String ONAWARDPROGRAMCLICKED = "onAwardProgramClicked";
    private static final String ONCLICKTRASHCLEAR = "onClickTrashClear";
    private static final String ONCLICKTRASHCLEARUNINSTALL = "onClickTrashClearUninstall";
    //    private static final String ONCLICKTRASHCLEARINSTALL = "onClickTrashClearInstall";
    private static final String ONCLICKTRASHCLEARPUSH = "onClickTrashClearPush";
    private static final String ONTRASHCLEARPUSHSHOW = "onTrashClearPushShow";
    private static final String ONRANKMORECLICKED = "onRankMoreClicked";
    private static final String ONSUBCLASSRANKLIST = "onSubClassRankList";
    private static final String ONSEARCHHOTKEYFIRSTPAGE = "onSearchHotKeyFirstPage";
    //    private static final String ONAPPPAGETAB = "onAPPPageTab";
//    private static final String ONGAMEPAGETAB = "onGamePageTab";
    private static final String ONNEWGAMECARDCLICK = "onNewGameCardClick";
    private static final String ONMORENEWGAMECARDCLICK = "onMoreNewGameCardClick";
    private static final String ONWONDERFULCLICK = "onWonderfuClick";
    private static final String ONCATENTRYCLICK = "onCatEntryClick";
    private static final String ONWELFARECLICK = "onWelfareClick";
    private static final String ONWELFAREBUTTONCLICK = "onWelfareButtonClick";
    private static final String ONVIDEOPLAYCLICK = "onVideoPlayClick";
    private static final String ONVIDEOAPPCLICK = "onVideoAppClick";
    private static final String ONFOCUSHOTTESTAPPCLICK = "onFocusHottestAppClick";
    private static final String ONFOCUSHOTTESTSEEALLCLICK = "onFocusHottestSeeAllClick";
    private static final String ONSTARTUPSKIPCLICK = "onStartupSkipClick";
    private static final String ONHOMEPAGEFOCUSCLICK = "onHomePageFocusClick";
    private static final String ONAPPPAGEFOCUSCLICK = "onAppPageFocusClick";
    private static final String ONGAMEPAGEFOCUSCLICK = "onGamePageFocusClick";
    private static final String HOMEDRAWERVIEWSHOW = "homeDrawerViewShow";
    private static final String APPDRAWERVIEWSHOW = "appDrawerViewShow";
    private static final String GAMEDRAWERVIEWSHOW = "gameDrawerViewShow";
    private static final String HOMEDRAWERSUBVIEWCLICK = "homeDrawerSubViewClick";
    private static final String APPDRAWERSUBVIEWCLICK = "appDrawerSubViewClick";
    private static final String GAMEDRAWERSUBVIEWCLICK = "gameDrawerSubViewClick";
    private static final String ONNEWGAMELISTCLICK = "onNewGameListClick";
    private static final String ONSEARCHADAPPCLICK = "onSearchADAppClick";
    private static final String ONCLICKGAMESUBCATE = "onClickGameSubCate";
    private static final String ONCLICKAPPSUBCATE = "onClickAppSubCate";
    //    private static final String ONSINGLEBANNER = "onSingleBanner";
//    private static final String ONSINGLEBANNERBTN = "onSingleBannerBtn";
    private static final String ONCLICKPOINTMALLMENU = "onClickPointMallMenu";
    private static final String ONHOMEADSHOW = "onHomeADShow";
    private static final String ONHOMEADCLICK = "onHomeADClick";
    private static final String ONSEARCHDRAWERCLICK = "onSearchDrawerClick";
    private static final String ONSEARCHRESULTLISTCLICK = "onSearchResultListClick";
    private static final String ONTOPICDETAILPOSITION = "onTopicDetailPosition";
    private static final String ONONEKEYVERSIONUPDATE = "onOneKeyVersionUpdate";
    private static final String ONCLICHOMEFOCUSNOPICTOPIC = "onclicHomeFocusNoPicTopic";
    private static final String ONCOMMONCATEGORYHEADCLICK = "onCommonCategoryHeadClick";
    private static final String ONCLICKAPPPAGEHEADENTRANCE = "onClickAppPageHeadEntrance";
    private static final String ONCLICKGAMEPAGEHEADENTRANCE = "onClickGamePageHeadEntrance";
    private static final String ONMATCHSEARCHCLICK = "onMatchSearchClick";
    private static final String ONCLICKCOMMOMGAMERANK = "onClickCommomGameRank";
    private static final String ONSINGLEGAMEPOSITION = "onSingleGamePosition";
    private static final String ONSINGLEGAMECLASS = "onSingleGameClass";
    private static final String ONONLINEGAMECLASS = "onOnLineGameClass";
    private static final String ONLINEGAMEPOSITION = "onLineGamePosition";
    private static final String ONONLINEGAMEHEADBANNER = "onOnLineGameHeadBanner";
    private static final String ONCOMMONGAMECATEGORY = "onCommonGameCategory";
    private static final String ONEKEYUPDATE = "oneKeyUpdate";
    private static final String ONUPDATEBTNCLICK = "onUpdateBtnClick";
    private static final String ONTRASHRECOMMONDAPP = "onTrashRecommondApp";
    private static final String ONNOAPPUPDATERECOMMOND = "onNoAppUpdateRecommond";
    private static final String ONCOMMONRANKSUBTAB = "onCommonRankSubTab";
    private static final String ONPOPULARRANKCLICK = "onPopularRankClick";
    private static final String ONNEWRANKCLICK = "onNewRankClick";
    private static final String ONSPECIALTOPICDETAIL = "onSpecialTopicdetail";
//    private static final String ONGAMERANKPOSITON = "onGameRankpositon";


    /*end 1.7.release ***/

    /**
     * 统计跳转详情次数
     */
    public static void onDetailClick(Context context, String appName,
                                     String packageName) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName) || context == null) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(context, DETAIL, prop);
    }

    /**
     * 统计下载次数
     */
    public static void onClickDownload(Context context, String appName,
                                       String packageName) {
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(context, CLICKDOWNLOAD, prop);
    }

    /**
     * 统计一键装机按钮点击次数
     */
    public static void onClickFirstDownload() {
        Properties prop = new Properties();
        prop.setProperty("name", "一键装机按钮");
        StatService.trackCustomKVEvent(BaseApplication.curContext, CLICKFIRSTDOWNLOAD, prop);
    }

    /**
     * 统计一键装机中应用下载次数
     */
    public static void onTimesFirstDownload(String appName,
                                            String packageName, String pageTitle) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(pageTitle))
            return;
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        prop.setProperty("page", pageTitle);
        StatService.trackCustomKVEvent(BaseApplication.curContext, TIMESFIRSTDOWNLOAD, prop);
    }

    /**
     * 统计版本迭代一键装机中应用下载次数 2.8.1 add
     */
    public static void onVersionUpdateDownload(String appName,
                                               String packageName, String pageTitle) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(pageTitle))
            return;
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        prop.setProperty("page", pageTitle);
        StatService.trackCustomKVEvent(BaseApplication.curContext, VERSIONUPDATEDOWNLOAD, prop);
    }

    /**
     * 统计版本迭代一键装机中应用下载次数 3.0 add
     */
    public static void onOneKeyVersionUpdate(String appName,
                                             String packageName, String pageTitle) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(pageTitle))
            return;
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        prop.setProperty("title", pageTitle);
        StatService.trackCustomKVEvent(BaseApplication.curContext, ONONEKEYVERSIONUPDATE, prop);
    }

    /**
     * 首页角标点击次数
     */
    public static void onHomeConerClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "首页角标点击事件");
        StatService.trackCustomKVEvent(context, ONHOMECONERCLICKED, prop);
    }

    /**
     * 首页广播点击次数
     */
    public static void onHomeBroadcastClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "首页广播点击事件");
        StatService.trackCustomKVEvent(context, ONHOMEBROADCASTCLICKED, prop);
    }

    /**
     * 签到入口点击次数
     */
    public static void onSignInClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "签到入口点击次数");
        StatService.trackCustomKVEvent(context, ONSIGNINCLICKED, prop);
    }

    /**
     * 消息中心入口点击次数
     */
    public static void onMsgCenterClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "消息中心入口点击次数");
        StatService.trackCustomKVEvent(context, ONMSGCENTERCLICKED, prop);
    }

    /**
     * 小纸条点击次数
     */
    public static void onLittlePaperClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "小纸条点击次数");
        StatService.trackCustomKVEvent(context, ONLITTLEPAPERCLICKED, prop);
    }

    /**
     * 最热中应用详情点击次数
     */
    public static void onHottestAppDetailClick(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "最热应用详情点击");
        StatService.trackCustomKVEvent(context, CLICKHOTTESTAPPDETAIL, prop);
    }

    /**
     * 最热中查看更多点击次数
     */
    public static void onHottestSeeMoreClick(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "最热全部评论点击");
        StatService.trackCustomKVEvent(context, CLICKHOTTESTSEEMORE, prop);
    }

    /**
     * 统计搜索内容
     */
    public static void onSearch(String searchContent) {
        Properties prop = new Properties();
        prop.setProperty("SearchKeyWord", searchContent);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                SEARCHKEYWORD, prop);
    }

    /**
     * 统计默认搜索内容(2.7 add)
     *
     * @param searchContent 搜索内容
     */
    public static void onSearchOriginal(String searchContent) {
        Properties prop = new Properties();
        prop.setProperty("SearchKeyWord", searchContent);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                SEARCHORIGINAL, prop);
    }

    /**
     * 统计首页底部菜单点击事件
     */
    public static void onClickMenu(String moduleName) {
        Properties prop = new Properties();
        prop.setProperty("menu", moduleName);
        StatService.trackCustomKVEvent(BaseApplication.curContext, CLICKMENU,
                prop);
    }

    /**
     * 统计精品页顶部Banner点击事件
     */
    public static void onClickPageRecommendBanner(String position) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onClickPageRecommendBanner-position=" + position);
        }
        Properties prop = new Properties();
        prop.setProperty("position", position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKPAGERECOMMENDBANNER, prop);
    }

    /**
     * 统计精品页常驻入口点击事件统计
     */
    public static void onClickPageRecommendEntrance(String title) {
        if (TextUtils.isEmpty(title))
            return;
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKPAGERECOMMENDENTRANCE, prop);
    }

    /**
     * 统计个人中心按钮点击次数
     */
    public static void onClickNavUserPage() {
        Properties prop = new Properties();
        prop.setProperty("PersonCenterBtnName", "个人中心");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKNAVUSERPAGE, prop);
    }

    /**
     * 统计顶部导航搜索框点击次数
     */
    public static void onClickNavSearch() {
        Properties prop = new Properties();
        prop.setProperty("NavSearch", "搜索框");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKNAVSEARCH, prop);
    }

    /**
     * 统计顶部导航下载点击次数
     */
    public static void onClickNavDownloadManage() {
        Properties prop = new Properties();
        prop.setProperty("NavDownLoad", "下载按钮");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKNAVDOWNLOADMANAGE, prop);
    }


    /**
     * 统计下载安装完成的应用
     */
    public static void onDownloadSuccess(String appName, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                DOWNLOADSUCCESS, prop);
    }

    /**
     * 统计下载安装成功的应用（不包含更新安装成功2.0版本）
     */
    public static void onInstallSucess(String appName, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                INSTALLSUCESSNOTUPDATE, prop);
    }

    /**
     * 统计应用下载完成(不包含更新)
     */
    public static void onAPPDownloadSuccess(String appName, String packageName) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                APPDOWNLOADSUCCESS, prop);
    }


    /**
     * 安装有礼（领取积分）下载成功事件2.0新增
     */
    public static void onPointDownloadSuccess(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("packageName", packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONPOINTDOWNLOADSUCCESS, prop);
    }

    /**
     * 安装有礼界面（领取积分）安装下载点击事件— 2.0新增
     */
    public static void onPointDownloadInstall(String appName, String packageName) {
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(packageName)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("name-packageName", appName + "-" + packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONPOINTDOWNLOADINSTALL, prop);
    }


    /**
     * 应用页顶部田字格点击
     */
    public static void onClickPageApplicationTopAds(String position) {
        Properties prop = new Properties();
        prop.setProperty("position", position);
        StatService.trackCustomKVEvent(BaseApplication.curContext.getApplicationContext(),
                CLICKPAGEAPPLICATIONTOPADS, prop);
    }

//    /**
//     * 跳过
//     */
//    public static void onClickCancelFirstDownload() {
//        Properties prop = new Properties();
//        prop.setProperty("name", "跳过");
//        StatService.trackCustomKVEvent(BaseApplication.curContext.getApplicationContext(),
//                CLICKCANCELFIRSTDOWNLOAD, prop);
//    }

    /**
     * 应用更新点击次数
     */
    public static void onClickAppUpdate(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "应用更新");
        StatService.trackCustomKVEvent(context, CLICKAPPUPDATE, prop);
    }

    /**
     * 应用卸载点击次数
     */
    public static void onClickUninstall(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "应用卸载");
        StatService.trackCustomKVEvent(context, ONCLICKUNINSTALL, prop);
    }

    /**
     * 设置点击次数
     */
    public static void onClickSettings(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "设置");
        StatService.trackCustomKVEvent(context, ONCLICKSETTINGS, prop);
    }

    /**
     * 同步助手点击次数
     */
    public static void onClickAppSyncAssistant(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "同步助手");
        StatService.trackCustomKVEvent(context, ONCLICKAPPSYNCASSISTANT, prop);
    }

    /**
     * 垃圾清理点击次数
     */
    public static void onClickTrashClear(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "垃圾清理入口点击事件");
        StatService.trackCustomKVEvent(context, ONCLICKTRASHCLEAR, prop);
    }

    /**
     * 垃圾清理卸载按钮点击次数
     */
    public static void onClickTrashClearUninstall(Context context, String appName) {
        if (TextUtils.isEmpty(appName))
            return;
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(context, ONCLICKTRASHCLEARUNINSTALL, prop);
    }

//    /**
//     * 垃圾清理体验一下按钮点击次数
//     */
//    public static void onClickTrashClearInstall(Context context, String appName) {
//        if (TextUtils.isEmpty(appName))
//            return;
//        Properties prop = new Properties();
//        prop.setProperty("appName", appName);
//        StatService.trackCustomKVEvent(context, ONCLICKTRASHCLEARINSTALL, prop);
//    }

    /**
     * 垃圾清理push点击次数
     */
    public static void onClickTrashClearPush(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "垃圾清理push点击事件");
        StatService.trackCustomKVEvent(context, ONCLICKTRASHCLEARPUSH, prop);
    }

    /**
     * 垃圾清理push出现次数（2.5.1增加）
     */
    public static void onTrashClearPushShow() {
        if (BaseApplication.curContext == null)
            return;
        Properties prop = new Properties();
        prop.setProperty("value", "垃圾清理push出现次数");
        StatService.trackCustomKVEvent(BaseApplication.curContext, ONTRASHCLEARPUSHSHOW, prop);
    }

    /**
     * 意见反馈点击次数
     */
    public static void onClickFeedback(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "意见反馈");
        StatService.trackCustomKVEvent(context, ONCLICKFEEDBACK, prop);
    }

    /**
     * 下载队列点击次数
     */
    public static void onClickDownLoadQueue(Context context) {
        Properties prop = new Properties();
        prop.setProperty("name", "下载队列");
        StatService.trackCustomKVEvent(context, CLICKDOWNLOADQUEUE, prop);
    }

    /**
     * 统计搜索24小时关键字
     */
    public static void onSearchHotKey(String searchContent) {
        Properties prop = new Properties();
        prop.setProperty("SearchHotValue", searchContent);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKSEARCHHOTKEY, prop);
    }

    /***
     * 启动页的广告点击统计
     *
     * @param value  对应跳转的id，url之类
     * @param adType ad类型
     */
    public static void onWelcomeAds(String value, String adType) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        prop.setProperty("adType", adType);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                CLICKWELCOMEADS, prop);
    }

    /**
     * 游戏和应用页每个子分类前8位点击次数；
     *
     * @param categoryName 当前的分类
     */
    public static void onClickCategory(String categoryName) {
        Properties prop = new Properties();
        prop.setProperty("categoryName", categoryName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKCATEGORY, prop);
    }

    /**
     * 应用页排行榜前15位点击次数；
     *
     * @param position 位置
     */
    public static void onClickAppRank(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPRANK, prop);
    }

    /**
     * 游戏页排行榜前15位点击次数；
     *
     * @param position 点击位置
     */
    public static void onClickGameRank(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMERANK, prop);
    }

    /**
     * 应用TAB下各分类项点击次数；
     *
     * @param value 分类名称
     */
    public static void onClickAppCategoryTAB(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPCATEGORYTAB, prop);
    }

    /**
     * 游戏TAB下各分类项点击次数；
     *
     * @param value 分类名称
     */
    public static void onClickGameCategoryTAB(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMECATEGORYTAB, prop);
    }

    /**
     * 分类中游戏分类点击次数；
     *
     * @param value 分类名称
     */
    public static void onClickGameCategoryHome(String value) {
        Properties prop = new Properties();
        prop.setProperty("name", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMECATEGORYGAME, prop);
    }

    /**
     * 分类中应用分类点击次数；
     *
     * @param value 分类名称
     */
    public static void onClickAppCategoryHome(String value) {
        Properties prop = new Properties();
        prop.setProperty("name", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMECATEGORYAPP, prop);
    }

    /**
     * 分类中应用分类点击次数；
     *
     * @param value 分类名称
     */
    public static void onClickHomeCategoryTopic(String value) {
        Properties prop = new Properties();
        prop.setProperty("name", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMECATEGORYTOPIC, prop);
    }

    /**
     * 大家也喜欢/相关推荐
     *
     * @param isRela 是否是相关推荐
     */
    public static void onClickRela_AllLike(boolean isRela) {
        Properties prop = new Properties();
        if (isRela) {
            prop.setProperty("name", "相关推荐");
        } else {

            prop.setProperty("name", "大家也喜欢");
        }
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKRELA_ALLLIKE, prop);
    }

    /**
     * 首页必备list位置（前15位）点击事件（1.9版本添加）
     */
    public static void onClickHomePageRequireList(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", "" + position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMEPAGEREQUIRELIST, prop);
    }

    /**
     * 首页新品list位置（前15位）点击事件（1.9版本添加）
     */
    public static void onClickHomePageNewProductorList(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", "" + position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMEPAGENEWPRODUCTORLIST, prop);
    }

    /**
     * 赚取积分（2.0版本添加）
     */
    public static void onClickEARN_POINTS() {
        Properties prop = new Properties();
        prop.setProperty("name", " 赚取积分");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONEARN_POINTS, prop);
    }

    /**
     * 积分商城（2.0版本添加）
     */
    public static void onClickPOINTS_MALL() {
        Properties prop = new Properties();
        prop.setProperty("name", " 积分商城");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONPOINTS_MALL, prop);
    }

    /**
     * 兑换记录（2.0版本添加）
     */
    public static void onClickCONVERT_RECORDS() {
        Properties prop = new Properties();
        prop.setProperty("name", " 兑换记录");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCONVERT_RECORDS, prop);
    }

    /**
     * 搜索换一换点击次数（2.1版本添加）
     */
    public static void onClickSearchChange() {
        Properties prop = new Properties();
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKSEARCHCHANGE, prop);
    }

//    /**
//     * 搜索精品推荐位置点击（2.1版本添加）
//     */
//    public static void onClickSearchRecommand(int position) {
//        Properties prop = new Properties();
//        prop.setProperty("position", position + "");
//        StatService.trackCustomKVEvent(BaseApplication.curContext,
//                ONCLICKSEARCHRECOMMAND, prop);
//    }

    /**
     * 搜索结果推荐标签位置点击（2.1版本添加）
     */
    public static void onClickSearchResultLabel(int position) {
        if (position > 4) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKSEARCHRESULTLABEL, prop);
    }

    /**
     * 意见反馈帮助与反馈条目位置点击（2.1版本添加）
     */
    public static void onClickFeedBackAndHelp(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKFEEDBACKANDHELP, prop);
    }

    /**
     * 首页单机列表第一个大项前6位点击（2.1版本添加）
     */
    public static void onClickSingGameFirstItem(int position) {
        if (position >= 6) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKSINGGAMEFIRSTITEM, prop);
    }

    /**
     * 首页单机列表第二个大项前6位点击（2.1版本添加）
     */
    public static void onClickSingGameSecondItem(int position) {
        if (position > 6) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKSINGGAMESECONDITEM, prop);
    }

    /**
     * 下载队列推荐位（下载无内容时）（2.1版本添加）
     */
    public static void onClickDownLoadQueenNOData(int position) {
        if (position > 6) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKDOWNLOADQUEENNODATA, prop);
    }

    /**
     * 下载队列推荐位（下载队列有数据时）（2.1版本添加）
     */
    public static void onClickDownLoadQueenHaveData(int position, String appName) {
        if (position >= 6) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKDOWNLOADQUEENHAVEDATA, prop);
    }

    /**
     * 首页list数据列表每个位置的点击次数（2.2版本添加）
     */
    public static void onclickhomePositionlist(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMEPOSITIONLIST, prop);
    }

    /**
     * 首页list各应用的点击次数（2.2版本添加）
     */
    public static void onclickhomelist(String name, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("value", name + "-" + packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMELIST, prop);
    }

    /**
     * 应用页list数据列表每个位置的点击次数（2.2版本添加）
     */
    public static void onclickAppPositionlist(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPPOSITIONLIST, prop);
    }

    /**
     * 应用页list各应用的下载次数（2.2版本添加）
     */
    public static void onclickApplist(String name, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("value", name + "-" + packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPLIST, prop);
    }

    /**
     * 游戏页List数据列表每个位置的点击次数（2.2版本添加）
     */
    public static void onclickGamePositionlist(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMEPOSITIONLIST, prop);
    }

    /**
     * 游戏页list各应用的点击次数（2.2版本添加）
     */
    public static void onclickGamelist(String name, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("value", name + "-" + packageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMELIST, prop);
    }

    /**
     * 首页单图点击次数（2.2版本添加）
     */
    public static void onclickhomesingpic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMESINGPIC, prop);
    }

    /**
     * 应用页单图点击次数（2.2版本添加）
     */
    public static void onclickappsingpic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPSINGPIC, prop);
    }

    /**
     * 游戏页单图点击次数（2.2版本添加）
     */
    public static void onclickgamesingpic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMESINGPIC, prop);
    }

    /**
     * 首页雷达点击位置次数（2.2版本添加）
     */
    public static void onclicappsin(String title, int position) {
        Properties prop = new Properties();
        prop.setProperty("value", title + "--" + position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICAPPSIN, prop);
    }

    /**
     * 首页田字格点击次数（2.2版本添加）
     */
    public static void onclicmatts(String title, int position) {
        Properties prop = new Properties();
        prop.setProperty("value", title + "--" + position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICMATTS, prop);
    }

    /**
     * 首页专题focus点击次数（2.2版本添加）
     */
    public static void onclicHomeFocusTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICHOMEFOCUSTOPIC, prop);
    }

    /**
     * 首页无图专题focus点击次数（3.0版本添加）
     */
    public static void onclicHomeFocusNoPicTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICHOMEFOCUSNOPICTOPIC, prop);
    }

    /**
     * 应用页专题focus点击次数（2.2版本添加）
     */
    public static void onclicAppFocusTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICAPPFOCUSTOPIC, prop);
    }

    /**
     * 应用页无图专题focus点击次数（3.0版本添加）
     */
    public static void onclicAppFocusNoPicTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICAPPFOCUSNOPICTOPIC, prop);
    }

    /**
     * 游戏页专题focus点击次数（2.2版本添加）
     */
    public static void onclicGameFocusTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICGAMEFOCUSTOPIC, prop);
    }

    /**
     * 游戏页无图专题focus点击次数（3.0版本添加）
     */
    public static void onclicGameFocusNoPicTopic(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICGAMEFOCUSNOPICTOPIC, prop);
    }

//    /**
//     * 游戏榜单余应用榜单（2.2版本添加）
//     */
//    public static void onclicGameAppRank(boolean isGame) {
//        Properties prop = new Properties();
//        if (isGame) {
//            prop.setProperty("value", "游戏榜");
//        } else {
//            prop.setProperty("value", "应用榜");
//        }
//        StatService.trackCustomKVEvent(BaseApplication.curContext,
//                ONCLICGAMEAPPRANK, prop);
//    }

    /**
     * 专题focus 各气泡点击次数点击次数（2.2版本添加）
     */
    public static void onclicGameAppCardCatsinView(boolean isGame, String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        if (isGame) {
            StatService.trackCustomKVEvent(BaseApplication.curContext,
                    ONCLICGAMECARDCATSIN, prop);
        } else {
            StatService.trackCustomKVEvent(BaseApplication.curContext,
                    ONCLICAPPCARDCATSIN, prop);
        }

    }

    /**
     * 透传静默卸载执行的次数（2.3版本添加）
     *
     * @param value 包名
     */
    public static void onUninstallCMD(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONUNINSTALLCMD, prop);

    }

    /**
     * 透传静默安装执行的次数（2.3版本添加）
     *
     * @param value 名称-包名
     */
    public static void onInstallCMD(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONINSTALLCMD, prop);

    }

    /**
     * 透传静默打开应用次数（2.4版本添加）
     *
     * @param value 包名
     */
    public static void onOpenAppCMD(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONOPENAPPCMD, prop);

    }

    /**
     * 必备左边菜单点击次数（2.3版本添加）
     *
     * @param value 分类名称
     */
    public static void onClickRequireMenu(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKREQUIREMENU, prop);

    }

    /**
     * 必备list点击次数（2.3版本添加）
     *
     * @param value 分类名称-应用名称
     */
    public static void onClickRequireClassList(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKREQUIRECLASSLIST, prop);

    }

    /**
     * PUSH点击次数（2.3版本添加）
     *
     * @param value 标题
     */
    public static void onClickPush(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKPUSH, prop);

    }

    /**
     * PUSH专题一键下载点击次数（2.3版本添加）
     */
    public static void onClickPushOneKeyDown() {
        Properties prop = new Properties();
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKPUSHONEKEYDOWN, prop);

    }

    /**
     * 详情下载按钮单机次数（2.3版本添加）
     *
     * @param value 名称-包名
     */
    public static void onClickAppDetailBtnDown(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPDETAILBTNDOWN, prop);

    }

    /**
     * 首页升级框按钮点击次数（2.3版本添加）
     *
     * @param value 取消或者确定
     */
    public static void onClickHomeUpdateBtn(String value) {
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKHOMEUPDATEBTN, prop);

    }

    /**
     * 收到push的次数（2.3版本添加）
     */
    public static void onPushArrive(int value) {
        Properties prop = new Properties();
        prop.setProperty("value", value + "");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONPUSHARRIVE, prop);

    }

    /**
     * 有奖活动入口点击次数(2.5 add)
     *
     * @param context Context
     */
    public static void onAwardProgramClicked(Context context) {
        Properties prop = new Properties();
        prop.setProperty("value", "有奖活动入口点击");//2.7新增参数
        StatService.trackCustomKVEvent(context, ONAWARDPROGRAMCLICKED, prop);
    }

    /**
     * 首页子榜查看更多点击(2.6 add)
     *
     * @param context Context
     */
    public static void onRankMoreClicked(Context context, int position) {
        Properties prop = new Properties();
        prop.setProperty("position", position + "");
        StatService.trackCustomKVEvent(context, ONRANKMORECLICKED, prop);
    }

    /**
     * 首页榜单子榜前15点击(2.6 add)
     *
     * @param context       Context
     * @param list_position list对应位置
     * @param type_position 分类对应位置 从1开始算
     */
    public static void onRankListClicked(Context context, int list_position, int type_position) {
        Properties prop = new Properties();
        prop.setProperty("typelistPos", type_position + "-" + list_position);
        prop.setProperty("typePos", type_position + "");
        StatService.trackCustomKVEvent(context, ONSUBCLASSRANKLIST, prop);
    }

    /**
     * 统计搜索24小时热搜第一页的点击总数 2.6 add
     */
    public static void onSearchHotKeyFirstPage(String searchContent) {
        Properties prop = new Properties();
        prop.setProperty("value", searchContent);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSEARCHHOTKEYFIRSTPAGE, prop);
    }

//    /**
//     * 统计游戏页或者应用页tab统计 2.6 add
//     */
//    public static void onAPPOrGamePageTab(String tabName, boolean isGame) {
//        Properties prop = new Properties();
//        prop.setProperty("value", tabName);
//        if (isGame) {
//            StatService.trackCustomKVEvent(BaseApplication.curContext,
//                    ONGAMEPAGETAB, prop);
//        } else {
//            StatService.trackCustomKVEvent(BaseApplication.curContext,
//                    ONAPPPAGETAB, prop);
//        }
//    }

    /**
     * 新游尝鲜卡片点击 2.6 add
     */
    public static void onNewGameCardClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONNEWGAMECARDCLICK, prop);
    }

    /**
     * 新游尝鲜查看更多点击 2.6 add
     */
    public static void onMoreNewGameCardClick() {
        Properties prop = new Properties();
        prop.setProperty("value", "新游尝鲜查看更多");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONMORENEWGAMECARDCLICK, prop);
    }

    /**
     * 统计游戏页精彩游戏点击次数（分位置）
     */
    public static void onWonderfuClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONWONDERFULCLICK, prop);
    }

    /**
     * 统计游戏页分类入口点击次数
     */
    public static void onCatEntryClick(String entry) {
        Properties prop = new Properties();
        prop.setProperty("entry", entry);

        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCATENTRYCLICK, prop);
    }


    /**
     * 统计游戏页玩家福利点击次数（分位置）
     */
    public static void onWelfareClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));

        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONWELFARECLICK, prop);
    }

    /**
     * 统计游戏页玩家福利按钮点击次数
     */
    public static void onWelfareButtonClick(String buttonType) {
        Properties prop = new Properties();
        prop.setProperty("buttonType", buttonType);

        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONWELFAREBUTTONCLICK, prop);
    }

    /**
     * 统计focus视频播放点击次数
     */
    public static void onVideoPlayClick(String pageType) {
        Properties prop = new Properties();
        prop.setProperty("pageType", pageType);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONVIDEOPLAYCLICK, prop);
    }

    /**
     * 统计focus视频详情点击次数
     */
    public static void onVideoAppClick(String pageType) {
        Properties prop = new Properties();
        prop.setProperty("pageType", pageType);

        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONVIDEOAPPCLICK, prop);
    }

    /**
     * 统计focus期刊应用详情点击次数
     */
    public static void onFocusHottestAppClick(String position) {
        Properties prop = new Properties();
        prop.setProperty("position", position);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONFOCUSHOTTESTAPPCLICK, prop);
    }

    /**
     * 统计focus期刊查看所有点击次数
     */
    public static void onFocusHottestSeeAllClick(String position) {
        Properties prop = new Properties();
        prop.setProperty("position", position);

        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONFOCUSHOTTESTSEEALLCLICK, prop);
    }

    /**
     * 统计启动页跳过点击次数 2.6 add
     */
    public static void onStartupSkipClick() {
        Properties prop = new Properties();
        prop.setProperty("value", "统计启动页跳过点击次数");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSTARTUPSKIPCLICK, prop);
    }

    /**
     * 统计首页Focus点击次数，记录Focus位置 2.6 add
     */
    public static void onHomePageFocusClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONHOMEPAGEFOCUSCLICK, prop);
    }

    /**
     * 统计应用页Focus点击次数，记录Focus位置 2.6 add
     */
    public static void onAppPageFocusClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONAPPPAGEFOCUSCLICK, prop);
    }

    /**
     * 统计游戏页Focus点击次数，记录Focus位置 2.6 add
     */
    public static void onGamePageFocusClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONGAMEPAGEFOCUSCLICK, prop);
    }

    /**
     * 首页抽屉出现次数 2.7 add
     */
    public static void homeDrawerViewShow() {
        Properties prop = new Properties();
        prop.setProperty("value", "首页抽屉出现次数");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                HOMEDRAWERVIEWSHOW, prop);
    }

    /**
     * 应用页抽屉出现次数 2.7 add
     */
    public static void appDrawerViewShow() {
        Properties prop = new Properties();
        prop.setProperty("value", "应用页抽屉出现次数");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                APPDRAWERVIEWSHOW, prop);
    }

    /**
     * 游戏页抽屉出现次数 2.7 add
     */
    public static void gameDrawerViewShow() {
        Properties prop = new Properties();
        prop.setProperty("value", "游戏页抽屉出现次数");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                GAMEDRAWERVIEWSHOW, prop);
    }

    /**
     * 首页抽屉推荐点击次数 2.7 add
     */
    public static void homeDrawerSubViewClick(String appName) {
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                HOMEDRAWERSUBVIEWCLICK, prop);
    }

    /**
     * 应用页抽屉推荐点击次数 2.7 add
     */
    public static void appDrawerSubViewClick(String appName) {
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                APPDRAWERSUBVIEWCLICK, prop);
    }

    /**
     * 游戏页抽屉推荐点击次数 2.7 add
     */
    public static void gameDrawerSubViewClick(String appName) {
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                GAMEDRAWERSUBVIEWCLICK, prop);
    }

    /**
     * 新游尝鲜列表点击次数（分位置）
     */
    public static void onNewGameListClick(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONNEWGAMELISTCLICK, prop);
    }

    /**
     * 搜索结果推广应用点击
     */
    public static void onSearchADAppClick(String appName) {
        if (TextUtils.isEmpty(appName))
            return;
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSEARCHADAPPCLICK, prop);
    }

    /**
     * 游戏分类小分类点击  2.7add
     */
    public static void onClickGameSubCate(String name) {
        Properties prop = new Properties();
        prop.setProperty("value", name);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMESUBCATE, prop);
    }

    /**
     * 应用分类小分类点击 2.7add
     */
    public static void onClickAppSubCate(String name) {
        Properties prop = new Properties();
        prop.setProperty("value", name);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPSUBCATE, prop);
    }

//    /**
//     * 单机banner点击 2.8add
//     */
//    public static void onSingleBanner() {
//        Properties prop = new Properties();
//        prop.setProperty("value", "单机banner点击");
//        StatService.trackCustomKVEvent(BaseApplication.curContext,
//                ONSINGLEBANNER, prop);
//    }

//    /**
//     * 单机banner下载按钮点击 2.8add
//     */
//    public static void onSingleBannerBtn() {
//        Properties prop = new Properties();
//        prop.setProperty("value", "单机banner下载按钮点击");
//        StatService.trackCustomKVEvent(BaseApplication.curContext,
//                ONSINGLEBANNERBTN, prop);
//    }

    /**
     * 首页list数据列表每个位置的点击次数（2.8版本添加）
     */
    public static void onUMclickhomePositionlist(int position) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("position", String.valueOf(position));
        MobclickAgent.onEvent(BaseApplication.curContext, ONCLICKHOMEPOSITIONLIST, map);
    }

    /**
     * 首页list各应用的点击次数（2.8版本添加）
     */
    public static void onUMclickhomelist(String name, String packageName) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("value", name + "-" + packageName);
        MobclickAgent.onEvent(BaseApplication.curContext,
                ONCLICKHOMELIST, map);
    }

    /**
     * 游戏页List数据列表每个位置的点击次数（2.8版本添加）
     */
    public static void onUMclickGamePositionlist(int position) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("position", String.valueOf(position));
        MobclickAgent.onEvent(BaseApplication.curContext,
                ONCLICKGAMEPOSITIONLIST, map);
    }

    /**
     * 游戏页list各应用的点击次数（2.8版本添加）
     */
    public static void onUMclickGamelist(String name, String packageName) {
        HashMap<String, String> prop = new HashMap<String, String>();
        prop.put("value", name + "-" + packageName);
        MobclickAgent.onEvent(BaseApplication.curContext,
                ONCLICKGAMELIST, prop);
    }

    /**
     * 应用页list各应用的下载次数（2.8版本添加）
     */
    public static void onUMclickApplist(String name, String packageName) {
        HashMap<String, String> prop = new HashMap<String, String>();
        prop.put("value", name + "-" + packageName);
        MobclickAgent.onEvent(BaseApplication.curContext,
                ONCLICKAPPLIST, prop);
    }

    /**
     * 应用页list数据列表每个位置的点击次数（2.8版本添加）
     */
    public static void onUMclickAppPositionlist(int position) {
        HashMap<String, String> prop = new HashMap<String, String>();
        prop.put("position", position + "");
        MobclickAgent.onEvent(BaseApplication.curContext,
                ONCLICKAPPPOSITIONLIST, prop);
    }

    /***
     * 启动页的广告点击统计 2.8add
     *
     * @param value  对应跳转的id，url之类
     * @param adType ad类型
     */
    public static void onUMWelcomeAds(String value, String adType) {
        HashMap<String, String> prop = new HashMap<String, String>();
        prop.put("value", value);
        prop.put("adType", adType);
        MobclickAgent.onEvent(BaseApplication.curContext,
                CLICKWELCOMEADS, prop);
    }

    /**
     * 积分商城3个入口点击 2.8add
     */
    public static void onClickPointMallMenu(String title) {
        Properties prop = new Properties();
        prop.setProperty("value", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKPOINTMALLMENU, prop);
    }

    /**
     * 首页插屏广告出现的次数（2.9add）
     */
    public static void onHomeADShow(String adID) {
        if (TextUtils.isEmpty(adID))
            return;
        Properties prop = new Properties();
        prop.setProperty("value", adID);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONHOMEADSHOW, prop);
    }

    /**
     * 首页插屏广告被点击的次数包含关闭按钮（2.9add）
     */
    public static void onHomeADClick(String adID) {
        if (TextUtils.isEmpty(adID))
            return;
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onHomeADClick-adID=" + adID);
        }
        Properties prop = new Properties();
        prop.setProperty("value", adID);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONHOMEADCLICK, prop);
    }

    /**
     * 搜索抽屉应用点击次数（2.9add）
     */
    public static void onSearchDrawerClick(int position) {
        JLog.i("MTAUtil", "position=" + position);
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSEARCHDRAWERCLICK, prop);
    }

    /**
     * 搜索列表点击次数（2.9add）
     */
    public static void onSearchResultListClick(int position, String appName) {
        if (position > 10) return;
        JLog.i("MTAUtil", "onSearchResultListClick-position=" + position + "--appName=" + appName);
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSEARCHRESULTLISTCLICK, prop);
    }

    /**
     * 专题位置点击
     *
     * @param value (专题id_位置)
     */
    public static void onTopicDetailPositionClick(String value) {
        if (BaseApplication.curContext == null) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext, ONTOPICDETAILPOSITION, prop);
    }

    /**
     * 分类页面-游戏分类和应用分类页面顶部四个入口的点击（3.0add）
     *
     * @param value 名称
     */
    public static void onCommonCategoryHeadClick(String value, boolean isGame) {
        if (BaseApplication.curContext == null) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("value", value);
        if (isGame) {
            prop.setProperty("pageinfo", "GamePage");
        } else {
            prop.setProperty("pageinfo", "AppPage");
        }
        StatService.trackCustomKVEvent(BaseApplication.curContext, ONCOMMONCATEGORYHEADCLICK, prop);
    }


    /**
     * 应用页顶部分类入口点击事件(3.0add)
     */
    public static void onClickAppPageHeadEntrance(String title) {
        if (TextUtils.isEmpty(title))
            return;
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKAPPPAGEHEADENTRANCE, prop);
    }

    /**
     * 游戏页顶部分类入口点击事件(3.0add)
     */
    public static void onClickGamePageHeadEntrance(String title) {
        if (TextUtils.isEmpty(title))
            return;
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKGAMEPAGEHEADENTRANCE, prop);
    }

    /**
     * 及时搜索推广应用点击事件(3.0add)
     */
    public static void onMatchSearchClick(String name) {
        if (TextUtils.isEmpty(name))
            return;
        Properties prop = new Properties();
        prop.setProperty("name", name);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONMATCHSEARCHCLICK, prop);
    }

    /**
     * 排行榜前15位点击次数 3.0add；
     *
     * @param position 点击位置
     */
    public static void onClickCommomGameRank(int position, String pageName) {
        if (TextUtils.isEmpty(pageName)) return;
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        prop.setProperty("pageName", pageName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCLICKCOMMOMGAMERANK, prop);
    }

    /**
     * 单机位置点击次数 3.0add；
     *
     * @param position 点击位置
     */
    public static void onSingleGamePosition(int position) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onSingleGamePosition-position=" + position);
        }
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSINGLEGAMEPOSITION, prop);
    }

    /**
     * 单机界面分类点击次数 3.0add；
     *
     * @param title 点击位置
     */
    public static void onSingleGameClass(String title) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onSingleGameClass-title=" + title);
        }
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSINGLEGAMECLASS, prop);
    }

    /**
     * 网游界面位置点击 3.0add
     *
     * @param position 点击位置
     */
    public static void onLineGamePosition(int position) {
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONLINEGAMEPOSITION, prop);
    }

    /**
     * 网游界面分类点击次数 3.0add；
     *
     * @param title 点击位置
     */
    public static void onOnLineGameClass(String title) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onOnLineGameClass-title=" + title);
        }
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONONLINEGAMECLASS, prop);
    }

    /**
     * 网游界面头部banner击次数 3.0add；
     *
     * @param title 点击位置
     */
    public static void onOnLineGameHeadBanner(String title) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onOnLineGameHeadBanner-title=" + title);
        }
        Properties prop = new Properties();
        prop.setProperty("title", title);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONONLINEGAMEHEADBANNER, prop);
    }

    private final static String[] TYPE_IDS = {"-20", "-51", "139", "-26", "140", "149"};

    /**
     * 休闲益智 策略塔防 格斗 网络游戏 跑酷 射击这六个分类列表里面，按每个位置的点击(3.0add)
     *
     * @param position     位置
     * @param categoryName 当前的分类
     */
    public static void onCommonGameCategory(String categoryName, String mParentID, int position) {
        if (TextUtils.isEmpty(mParentID) || !Arrays.asList(TYPE_IDS).contains(mParentID)) return;
        if (mParentID.equals(TYPE_IDS[0])) {
            categoryName = "休闲益智";
        }
        if (mParentID.equals(TYPE_IDS[1])) {
            categoryName = "策略塔防";
        }
        if (mParentID.equals(TYPE_IDS[3])) {
            categoryName = "网络游戏";
        }
        Properties prop = new Properties();
        prop.setProperty("value", categoryName + "_" + (position + 1));
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onCommonGameCategory value=" + categoryName + "_" + (position + 1));

        }
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCOMMONGAMECATEGORY, prop);
    }


    /**
     * 应用更新界面一键更新按钮点击（3.0add）
     */
    public static void oneKeyUpdate() {
        Properties prop = new Properties();
        prop.setProperty("value", "一键更新");
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONEKEYUPDATE, prop);
    }

    /**
     * 应用更新界面列表更新按钮点击（3.0add）
     */
    public static void onUpdateBtnClick(String appName) {
        if (TextUtils.isEmpty(appName)) return;
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONUPDATEBTNCLICK, prop);
    }

    /**
     * 垃圾清理推荐界面（3.1 add）
     *
     * @param value 应用名称或者关键词
     */
    public static void onTrashRecommondApp(String value) {
        if (TextUtils.isEmpty(value)) return;
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onTrashRecommondApp-value=" + value);
        }
        Properties prop = new Properties();
        prop.setProperty("value", value);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONTRASHRECOMMONDAPP, prop);
    }

    /**
     * 应用更新界面无更新时推荐位（3.1 add）
     *
     * @param appName  应用名称
     * @param position 位置
     */
    public static void onNoAppUpdateRecommond(String appName, int position) {
        if (TextUtils.isEmpty(appName)) return;
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onNoAppUpdateRecommond-appName=" + appName + "--position=" + position);
        }
        Properties prop = new Properties();
        prop.setProperty("appName", appName);
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONNOAPPUPDATERECOMMOND, prop);
    }

    /**
     * 榜单subTab点击（3.1版本添加）
     */
    public static void onCommonRankSubTab(String tabName) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onCommonRankSubTab-tabName=" + tabName);
        }
        Properties prop = new Properties();
        prop.setProperty("value", tabName);
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONCOMMONRANKSUBTAB, prop);
    }

    /**
     * 流行榜或者新品榜位置点击（3.1版本添加）
     */
    public static void onPopularOrNewRank(boolean isPopular, int position) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onPopularRankPosition-position=" + position + "--isPopular=" + isPopular);
        }
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        if (isPopular) {
            StatService.trackCustomKVEvent(BaseApplication.curContext,
                    ONPOPULARRANKCLICK, prop);
        } else {
            StatService.trackCustomKVEvent(BaseApplication.curContext,
                    ONNEWRANKCLICK, prop);
        }
    }

    /**
     * 某个专题详情位置点击（3.1版本添加）
     */
    public static void onSpecialTopicdetail(int position) {
        if (JLog.isDebug) {
            JLog.i("MTAUtil", "onSpecialTopicdetail-position=" + position);
        }
        Properties prop = new Properties();
        prop.setProperty("position", String.valueOf(position));
        StatService.trackCustomKVEvent(BaseApplication.curContext,
                ONSPECIALTOPICDETAIL, prop);
    }


}
