package com.prize.prizethemecenter.ui.utils;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * 腾讯移动分析 自定义事件  MTAUtil
 * pengy 2016-11-18
 */
public class MTAUtil {

    /**搜索词统计*/
	private static final String CLICKSEARCHWORD = "clicksearchword";
	/**搜索框推荐词搜索次数*/
	private static final String CLICKSEARCHHOTKEY = "clicksearchhotkey";
	/**搜索热词推荐词点击次数主题*/
	private static final String CLICKPAGETHEMEHOTKEY = "clickpagethemehotkey";
	/**搜索热词推荐词点击次数壁纸*/
	private static final String CLICKPAGEWALLHOTKEY = "clickpagewallhotkey";
	/**搜索热词推荐词点击次数字体*/
	private static final String CLICKPAGEFONTHOTKEY = "clickpagefonthotkey";

	/**热门主题列表点击数*/
	private static final String CLICKTHEMELIST = "clickthemelist";
	/**热门壁纸列表点击数*/
	private static final String CLICKWALLPAPERLIST = "clickwallpaperlist";
	/**热门字体列表点击数*/
	private static final String CLICKFONTLIST = "clickfontlist";
	/**首页banner轮播图点击数*/
	private static final String CLICKTHEMEBANNER = "clickthemebanner";
	/**壁纸页4张banner图点击数*/
	private static final String CLICKWALLPAPERBANNER = "clickwallpaperbanner";
	/**字体页4张banner图点击数*/
	private static final String CLICKFONTBANNER = "clickfontbanner";
	/**单个主题下载次数*/
	private static final String CLICKSINGLETHEME = "clicksingletheme";
	/**单张壁纸下载次数*/
	private static final String CLICKSINGLEWALLPAPER = "clicksinglewallpaper";
	/**单个字体下载次数*/
	private static final String CLICKSINGLEFONT = "clicksinglefont";
	/**主题详情页相似主题点击数*/
	private static final String CLICKSIMILARTHEME = "clicksimilartheme";
	/**主题详情页标签点击数*/
	private static final String CLICKTHEMETAG= "clickthemetag";
	/**分类入口点击（主题、壁纸）*/
	private static final String CLICKCLASSIFY = "clickclassify";
	/**排行入口点击（主题、字体）*/
	private static final String CLICKRANKING = "clickranking";
	/**专题入口点击（主题、壁纸、字体）*/
	private static final String CLICKTOPIC = "clicktopic";
	/**本地入口点击（主题、壁纸、字体）*/
	private static final String CLICKLOCAL = "clicklocal";

	/**专题列表点击主题*/
	private static final String CLICKTOPICTHEMELIST = "clicktopicthemelist";
	/**专题列表点击壁纸*/
	private static final String CLICKTOPICWALLLIST = "clicktopicwalllist";
	/**专题列表点击字体*/
	private static final String CLICKTOPICFONTLIST = "clicktopicfontlist";

	/**push消息点击*/
	private static final String CLICKPUSH = "clickpush";
	/**下载管理入口点击*/
	private static final String CLICKDWONLOAD = "clickdownload";
	/**设置入口点击*/
	private static final String CLICKSETTING= "clicksetting";
	/**意见反馈入口点击*/
	private static final String CLICKFEEDBACK = "clickfeedback";

	/**搜索词统计*/
	public static void onSearchWord(String searchContent) {
		Properties prop = new Properties();
		prop.setProperty("SearchHotValue", searchContent);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSEARCHWORD, prop);
	}

	/**搜索框推荐词搜索次数*/
	public static void onSearchHotKey(String searchContent) {
		Properties prop = new Properties();
		prop.setProperty("SearchHotValue", searchContent);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSEARCHHOTKEY, prop);
	}

	/**搜索热词推荐词点击次数主题*/
	public static void onSearchPageThemeKey(String key) {
		Properties prop = new Properties();
		prop.setProperty("SearchHotValue",key);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKPAGETHEMEHOTKEY, prop);
	}

	/**搜索热词推荐词点击次数壁纸*/
	public static void onSearchPageWallKey(String key) {
		Properties prop = new Properties();
		prop.setProperty("SearchHotValue",key);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKPAGEWALLHOTKEY, prop);
	}

	/**搜索热词推荐词点击次数字体*/
	public static void onSearchPageFontKey(String key) {
		Properties prop = new Properties();
		prop.setProperty("SearchHotValue",key);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKPAGEFONTHOTKEY, prop);
	}

	/**热门主题列表点击数*/
	public static void onHotTheme(String themeName) {
		Properties prop = new Properties();
		prop.setProperty("themeName", themeName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTHEMELIST, prop);
	}

	/**热门壁纸列表点击数*/
	public static void onHotWallpaper(String wallName) {
		Properties prop = new Properties();
		prop.setProperty("wallName", wallName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKWALLPAPERLIST, prop);
	}

	/**热门字体列表点击数*/
	public static void onHotFont(String fontName) {
		Properties prop = new Properties();
		prop.setProperty("fontName", fontName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKFONTLIST, prop);
	}

	/**首页banner轮播图点击数*/
	public static void onClickPageThemeBanner(int position) {
		Properties prop = new Properties();
		JLog.i("hu","position1111=="+String.valueOf(position));
		prop.setProperty("position", String.valueOf(position));
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTHEMEBANNER, prop);
	}

	/**壁纸页4张banner图点击数*/
	public static void onClickPageWallBanner(int position) {
		Properties prop = new Properties();
		prop.setProperty("position", String.valueOf(position));
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKWALLPAPERBANNER, prop);
	}

	/**字体页4张banner图点击数*/
	public static void onClickPageFontBanner(int position) {
		Properties prop = new Properties();
		prop.setProperty("position", String.valueOf(position));
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKFONTBANNER, prop);
	}

	/**单个主题下载次数*/
	public static void onClickSingleTheme() {
		Properties prop = new Properties();
		prop.setProperty("name", "单个主题下载");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSINGLETHEME, prop);
	}

	/**单张壁纸下载次数*/
	public static void onClickSingleWall() {
		Properties prop = new Properties();
		prop.setProperty("name", "单个壁纸下载");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSINGLEWALLPAPER, prop);
	}

	/**单个字体下载次数*/
	public static void onClickSingleFont() {
		Properties prop = new Properties();
		prop.setProperty("name", "单个字体下载");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSINGLEFONT, prop);
	}

	/**主题详情页相似主题点击数*/
	public static void onClickSimilarTheme(String themeName) {
		Properties prop = new Properties();
		prop.setProperty("themeName", themeName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSIMILARTHEME, prop);
	}

	/**主题详情页标签点击数*/
	public static void onClickTagTheme(String tag) {
		Properties prop = new Properties();
		prop.setProperty("tag", tag);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTHEMETAG, prop);
	}

	/**分类入口点击（主题、壁纸）*/
	public static void onClickClassify(String name) {
		Properties prop = new Properties();
		prop.setProperty("name", name);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKCLASSIFY, prop);
	}

	/**排行入口点击（主题、字体）*/
	public static void onClickRanking(String name) {
		Properties prop = new Properties();
		prop.setProperty("name", name);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKRANKING, prop);
	}
	/**专题入口点击（主题、壁纸、字体）*/
	public static void onClickTopic(String name) {
		Properties prop = new Properties();
		prop.setProperty("name", name);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTOPIC, prop);
	}
	/**本地入口点击（主题、壁纸、字体）*/
	public static void onClickLocal(String name) {
		Properties prop = new Properties();
		prop.setProperty("name", name);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKLOCAL, prop);
	}

	/**专题列表点击主题*/
	public static void onClickTopicThemeList(String itemName) {
		Properties prop = new Properties();
		prop.setProperty("itemName", itemName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTOPICTHEMELIST, prop);
	}

	/**专题列表点击壁纸*/
	public static void onClickTopicWallList(String itemName) {
		Properties prop = new Properties();
		prop.setProperty("itemName", itemName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTOPICWALLLIST, prop);
	}

	/**专题列表点击字体*/
	public static void onClickTopicFontList(String itemName) {
		Properties prop = new Properties();
		prop.setProperty("itemName", itemName);
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKTOPICFONTLIST, prop);
	}

	/**push消息点击*/
	public static void onPushClick() {
		Properties prop = new Properties();
		prop.setProperty("name", "push消息");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKPUSH, prop);
	}

	/**下载管理入口点击 */
	public static void onDownloadClick() {
		Properties prop = new Properties();
		prop.setProperty("name", "下载管理");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKDWONLOAD, prop);
	}

	/**设置入口点击 */
	public static void onSettingClick() {
		Properties prop = new Properties();
		prop.setProperty("name", "设置");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKSETTING, prop);
	}

	/** 意见反馈入口点击*/
	public static void onFeedbackClick() {
		Properties prop = new Properties();
		prop.setProperty("name", "意见反馈");
		StatService.trackCustomKVEvent(MainApplication.getInstance(),
				CLICKFEEDBACK, prop);
	}

	/**
	 * 统计接口耗时
	 */
	public static void testNetPort(String url) {
		// 新建监控接口对象
		StatAppMonitor monitor = new StatAppMonitor("ping:" + url);
		String ip = url;
		Runtime run = Runtime.getRuntime();
		Process proc = null;
		try {
			String str = "ping -c 3 -i 0.2 -W 1 " + ip;
			long starttime = System.currentTimeMillis();
			// 被监控的接口
			proc = run.exec(str);
			proc.waitFor();
			long difftime = System.currentTimeMillis() - starttime;
			// 设置接口耗时
			monitor.setMillisecondsConsume(difftime);
			int retCode = proc.waitFor();
			// 设置接口返回码
			monitor.setReturnCode(retCode);
			// // 设置请求包大小，若有的话
			// monitor.setReqSize(1000);
			// // 设置响应包大小，若有的话
			// monitor.setRespSize(2000);
			// 设置抽样率
			// 默认为1，表示100%。
			// 如果是50%，则填2(100/50)，如果是25%，则填4(100/25)，以此类推。
			monitor.setSampling(1);
			JLog.i("hu", "code=" + retCode);
			if (retCode == 200) {
				JLog.i("hu", "ping连接成功");
				// 标记为成功
				monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
			} else {
				JLog.e("hu", "ping测试失败");
				// 标记为逻辑失败，可能由网络未连接等原因引起的
				// 但对于业务来说不是致命的，是可容忍的
				monitor.setResultType(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 接口调用出现异常，致命的，标识为失败
			monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
		} finally {
			proc.destroy();
		}
		// 上报接口监控
		StatService.reportAppMonitorStat(BaseApplication.curContext, monitor);
	}

}
