package com.prize.app.beans;

import java.io.Serializable;

import com.prize.app.util.NumberUtils;

public class GameBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 游戏编号 */
	public String gameCode;
	/** 游戏版本 2.0.1 */
	public String gameVer;

	/** 1=单机 2=网游 3=页游 */
	public byte gameType;
	/** 游戏标签，分类 */
	public String gameTag;
	/** 游戏密钥 */
	public String gameKey;
	public String gamePkgName;
	/** 启动入口，如果是网游，这里是网页地址 */
	public String gameActivity;
	/** 游戏大小，显示给用户 1.05M */
	public String gameSizeShow;
	/** 游戏大小，byte单位 */
	public long gameSize;
	/** 游戏名称 */
	public String gameName;
	/** 游戏详情 */
	public String gameDesc;
	/** 游戏评分 0～10 */
	public short gameMark = 10;
	/** 游戏下载地址 */
	public String gameDownloadUrl;
	/** 游戏小图，详情页内显示 */
	public String gameIconSmall;
	/** 游戏大图，列表页显示 */
	public String gameIconBig;
	/** 游戏截图，多个用,分隔 */
	public String gameImg;
	/** 游戏截图显示方向 0=横向 1=纵向 **/
	public byte gameImgDisplayType;
	/** 游戏支持最低的android版本 */
	public String gameAndroidVer;
	/** 分辨率支持，多个用,分隔 400*800 */
	public String gameScreenSupport;
	/** 游戏玩家人数 */
	public long gameUsers;
	/** 游戏版本 AndroidManifest.xml中定义的VersionCode **/
	public int gameVersionCode;

	/** 用户唯一标识usercode **/
	public String usercode;
	/** 是否来自第三方数据 */
	public boolean fromOtherSource;

	/** 语言 1=中文 2=英文 0=其他，2013-05-10 新增 */
	public static final byte LANGUAGE_CHINESE = 1;
	public static final byte LANGUAGE_ENGLISH = 2;
	public static final byte LANGUAGE_UNKNOW = 0;
	public byte language;
	/** 游戏开发公司名称,2013-05-10新增 */
	public String gameCp;
	/** 游戏点评，运营的点评 ,2013-05-10新增 */
	public String gameEditorComment;
	/** 游戏更新时间点评,例如：1小时前,2013-05-10新增 */
	public String gameUpdatetimeNick;
	/** 下载次数，给予什么就显示什么，例如：下载10w+,2013-05-10新增 */
	public String gameDownloadCountNick;
	/** 常规分类，例如 动作冒险，角色扮演,在详情页显示,2013-05-10新增 */
	public String gameClass;
	/** 元素类型 1=列表 2=游戏 3=活动 ;4=第三方游戏 5=游戏礼包 6=游戏攻略 **/
	public byte itemType;
	public static final byte ITEM_TYPE_LIST = 1;
	public static final byte ITEM_TYPE_GAME = 2;
	public static final byte ITEM_TYPE_ACTIVITY = 3;
	/** 从第三方获取游戏 **/
	public static final byte ITEM_TYPE_FROM_OTHER = 4;
	public static final byte ITEM_TYPE_GIFT = 5;
	public static final byte ITEM_TYPE_TACTICS = 6;

	// 资源角标 2013-8-19 增加v3.22终端新增
	public String itemCornerIcon;
	// 资源评论 2013-8-19 增加v3.22终端新增
	public String itemComment;

	public String gameSecurityTags;// v3.30版本-2013-09-27新增，安全标签，例如无广告，无病毒等，颜色+文字

	public String gameSecurityDetail;// v3.30版本-2013-09-27新增，安全信息详细描述，icon+文本

	public String gameBbsUrl;// v3.30版本-2013-09-27新增，游戏论坛主页地址，终端请求需要带上
								// userid=usercode&sessionid=session值，这样可以在wap站直接登录

	public String gameNewsUrl;// v3.30版本-2013-09-27新增，游戏新闻资讯主页地址，终端请求需要带上
								// userid=usercode&sessionid=session值，这样可以在wap站直接登录

	public String gameTacticsUrl;// v3.30版本-2013-09-27新增，游戏攻略主页地址，终端请求需要带上
									// userid=usercode&sessionid=session值，这样可以在wap站直接登录

	public int gameHashcode;// v3.30版本-2013-09-27新增，游戏apk内部签名的hashcode

	public long gameNewsUpdatetime;// v3.30版本-2013-09-27新增，游戏新闻更新时间，毫秒
	/** v3.50版本，网游专区列表显示 */
	public String gameServiceTag;// v3.50版本，网游专区列表显示

	/** 游戏所属的listcode **/
	public String listcode;
	/** 富文本格式的游戏名称 **/
	public String gameRTFGameName;

	public static class GameType {
		public static int SINGLE_GAME = 1;
		public static int ONLINE_GAME = 2;
		public static int WEB_GAME = 3;
	}

	public GameBean(Game game, String gameListcode) {
		gameCode = game.getGameCode();
		/** 游戏版本 2.0.1 */
		gameVer = game.getGameVer();
		;
		/** 游戏版本 AndroidManifest.xml中定义的VersionCode **/
		gameVersionCode = NumberUtils.getIntegerValue(game.getGameVerInt());
		/** 1=单机 2=网游 3=页游 */
		gameType = NumberUtils.getByteValue(game.getGameType());
		/** 游戏标签，分类 */
		gameTag = game.getGameTag();
		/** 游戏密钥 */
		gameKey = game.getGameKey();
		gamePkgName = game.getGamePkgName();
		/** 启动入口，如果是网游，这里是网页地址 */
		gameActivity = game.getGameActivity();
		/** 游戏大小，显示给用户 1.05M */
		gameSizeShow = game.getGameSizeShow();
		/** 游戏大小，byte单位 */
		gameSize = NumberUtils.getLongValue(game.getGameSize());
		/** 游戏名称 */
		gameName = game.getGameName();
		if ((null != gameName) && (gameName.contains("<font"))) {
			// 富文本格式
			gameRTFGameName = gameName;
			gameName = gameName.replaceAll("<font color='red'>", "")
					.replaceAll("</font>", "");
		}
		/** 游戏详情 */
		gameDesc = game.getGameDesc();
		/** 游戏评分 0～10 */
		gameMark = NumberUtils.getShortrValue(game.getGameMark());
		/** 游戏下载地址 */
		gameDownloadUrl = game.getGameDownloadUrl();
		/** 游戏小图，详情页内显示 */
		gameIconSmall = game.getGameIconSmall();
		/** 游戏大图，列表页显示 */
		gameIconBig = game.getGameIconBig();
		/** 游戏截图，多个用,分隔 */
		gameImg = game.getGameImg();
		/** 游戏截图显示方向 0=横向 1=纵向 **/
		gameImgDisplayType = NumberUtils.getByteValue(
				game.getGameImgDisplayType(), (byte) 1);
		/** 游戏支持最低的android版本 */
		gameAndroidVer = game.getGameAndroidVer();
		/** 分辨率支持，多个用,分隔 400*800 */
		gameScreenSupport = game.getGameScreenSupport();
		/** 游戏玩家人数 */
		gameUsers = NumberUtils.getLongValue(game.getGameUsers(), 1000);
		/** 语言 1=中文 2=英文 0=其他，2013-05-10 新增 */
		language = NumberUtils.getByteValue(game.getLanguage());
		/** 游戏开发公司名称,2013-05-10新增 */
		gameCp = game.getGameCp();
		/** 游戏点评，运营的点评 ,2013-05-10新增 */
		gameEditorComment = game.getGameEditorComment();
		/** 游戏更新时间点评,例如：1小时前,2013-05-10新增 */
		gameUpdatetimeNick = game.getGameUpdatetimeNick();
		/** 下载次数，给予什么就显示什么，例如：下载10w+,2013-05-10新增 */
		gameDownloadCountNick = game.getGameDownloadCountNick();
		/** 常规分类，例如 动作冒险，角色扮演,在详情页显示,2013-05-10新增 */
		gameClass = game.getGameClass();
		/** 截屏 **/
		// ArrayList<GameScreenshot> screens = game.getGameScreenshotList();
		// if (screens != null && screens.size() > 0) {
		// for (GameScreenshot shot : screens) {
		// GameScreenshotBean bean = new GameScreenshotBean();
		// bean.gameImgSmall = shot.getGameImgSmall();
		// bean.gameImgBig = shot.getGameImgBig();
		// screenshots.add(bean);
		// }
		// }
		gameSecurityTags = game.getGameSecurityTags();
		gameSecurityDetail = game.getGameSecurityDetail();
		gameBbsUrl = game.getGameBbsUrl();
		gameNewsUrl = game.getGameNewsUrl();
		gameTacticsUrl = game.getGameTacticsUrl();
		gameHashcode = NumberUtils.getIntegerValue(game.getGameHashcode());
		gameNewsUpdatetime = NumberUtils.getLongValue(game
				.getGameNewsUpdatetime());
		gameServiceTag = game.getGameServiceTag();
		listcode = gameListcode;
	}

	public GameBean() {

	}

	public boolean isFree() {
		return false;
	}
}
