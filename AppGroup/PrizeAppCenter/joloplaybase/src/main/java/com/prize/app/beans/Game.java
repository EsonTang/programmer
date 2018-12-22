package com.prize.app.beans;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 游戏信息
 * 
 * @author prize
 * @version 1.0 2013-2-4
 *
 */
public class Game implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -143774256883570214L;

	private String gameCode;// 游戏编号

	private String gameVer;// 游戏版本 2.0.1

	private Byte gameType; // 1=单机 2=网游 3=页游

	private String gameTag; // 游戏标签，分类

	private String gameKey; // 游戏密钥
	private String gamePkgName;

	private String gameActivity; // 启动入口，如果是网游，这里是网页地址

	private String gameSizeShow;// 游戏大小，显示给用户 1.05M

	private Long gameSize;// 游戏大小，byte单位

	private String gameName;// 游戏名称

	private String gameDesc;// 游戏详情

	private Short gameMark = 5; // 游戏评分 0～5

	private String gameDownloadUrl;// 游戏下载地址

	private String gameIconSmall;// 游戏小图，详情页内显示

	private String gameIconBig;// 游戏大图，列表页显示

	private String gameImg;// 游戏截图，多个用,分隔,v3.10.xxx之前版本使用

	private Byte gameImgDisplayType;// 0=横向 1=纵向

	private String gameAndroidVer;// 游戏支持最低的android版本

	private String gameScreenSupport;// 分辨率支持，多个用,分隔 400*800

	private Long gameUsers;// 游戏玩家人数

	private Integer gameVerInt;// 游戏版本号

	private Byte language;// 语言 1=中文 2=英文 0=其他，2013-05-10 新增

	private String gameCp;// 游戏开发公司名称,2013-05-10新增

	private String gameEditorComment;// 游戏点评，运营的点评 ,2013-05-10新增

	private String gameUpdatetimeNick;// 游戏更新时间点评,例如：1小时前,2013-05-10新增

	private String gameDownloadCountNick;// 下载次数，给予什么就显示什么，例如：下载10w+,2013-05-10新增

	private String gameClass;// 常规分类，例如 动作冒险，角色扮演,在详情页显示,2013-05-10新增

	// v3.10.xxx版本新增
	private String gameSecurityTags;// v3.30版本-2013-09-27新增，安全标签，例如无广告，无病毒等，颜色+文字

	private String gameSecurityDetail;// v3.30版本-2013-09-27新增，安全信息详细描述，icon+文本

	private String gameBbsUrl;// v3.30版本-2013-09-27新增，游戏论坛主页地址，终端请求需要带上
								// userid=usercode&sessionid=session值，这样可以在wap站直接登录

	private String gameNewsUrl;// v3.30版本-2013-09-27新增，游戏新闻资讯主页地址，终端请求需要带上
								// userid=usercode&sessionid=session值，这样可以在wap站直接登录
	private String gameTacticsUrl;// v3.30版本-2013-09-27新增，游戏攻略主页地址，终端请求需要带上
									// userid=usercode&sessionid=session值，这样可以在wap站直接登录

	private Integer gameHashcode;// v3.30版本-2013-09-27新增，游戏apk内部签名的hashcode
	private Long gameNewsUpdatetime;// v3.30版本-2013-09-27新增，游戏新闻更新时间，毫秒

	private String gameServiceTag;// v3.50版本，网游专区列表显示

	public String getGameCode() {
		return gameCode;
	}

	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}

	public String getGameVer() {
		return gameVer;
	}

	public void setGameVer(String gameVer) {
		this.gameVer = gameVer;
	}

	public Byte getGameType() {
		return gameType;
	}

	public void setGameType(Byte gameType) {
		this.gameType = gameType;
	}

	public String getGameTag() {
		return gameTag;
	}

	public void setGameTag(String gameTag) {
		this.gameTag = gameTag;
	}

	public String getGameKey() {
		return gameKey;
	}

	public void setGameKey(String gameKey) {
		this.gameKey = gameKey;
	}

	public String getGamePkgName() {
		return gamePkgName;
	}

	public void setGamePkgName(String gamePkgName) {
		this.gamePkgName = gamePkgName;
	}

	public String getGameActivity() {
		return gameActivity;
	}

	public void setGameActivity(String gameActivity) {
		this.gameActivity = gameActivity;
	}

	public String getGameSizeShow() {
		return gameSizeShow;
	}

	public void setGameSizeShow(String gameSizeShow) {
		this.gameSizeShow = gameSizeShow;
	}

	public Long getGameSize() {
		return gameSize;
	}

	public void setGameSize(Long gameSize) {
		this.gameSize = gameSize;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameDesc() {
		return gameDesc;
	}

	public void setGameDesc(String gameDesc) {
		this.gameDesc = gameDesc;
	}

	public Short getGameMark() {
		return gameMark;
	}

	public void setGameMark(Short gameMark) {
		this.gameMark = gameMark;
	}

	public String getGameDownloadUrl() {
		return gameDownloadUrl;
	}

	public void setGameDownloadUrl(String gameDownloadUrl) {
		this.gameDownloadUrl = gameDownloadUrl;
	}

	public String getGameIconSmall() {
		return gameIconSmall;
	}

	public void setGameIconSmall(String gameIconSmall) {
		this.gameIconSmall = gameIconSmall;
	}

	public String getGameIconBig() {
		return gameIconBig;
	}

	public void setGameIconBig(String gameIconBig) {
		this.gameIconBig = gameIconBig;
	}

	public String getGameImg() {
		return gameImg;
	}

	public void setGameImg(String gameImg) {
		this.gameImg = gameImg;
	}

	public String getGameAndroidVer() {
		return gameAndroidVer;
	}

	public void setGameAndroidVer(String gameAndroidVer) {
		this.gameAndroidVer = gameAndroidVer;
	}

	public String getGameScreenSupport() {
		return gameScreenSupport;
	}

	public void setGameScreenSupport(String gameScreenSupport) {
		this.gameScreenSupport = gameScreenSupport;
	}

	public Long getGameUsers() {
		return gameUsers;
	}

	public void setGameUsers(Long gameUsers) {
		this.gameUsers = gameUsers;
	}

	public Byte getGameImgDisplayType() {
		return gameImgDisplayType;
	}

	public void setGameImgDisplayType(Byte gameImgDisplayType) {
		this.gameImgDisplayType = gameImgDisplayType;
	}

	public Integer getGameVerInt() {
		return gameVerInt;
	}

	public void setGameVerInt(Integer gameVerInt) {
		this.gameVerInt = gameVerInt;
	}

	public Byte getLanguage() {
		return language;
	}

	public void setLanguage(Byte language) {
		this.language = language;
	}

	public String getGameCp() {
		return gameCp;
	}

	public void setGameCp(String gameCp) {
		this.gameCp = gameCp;
	}

	public String getGameEditorComment() {
		return gameEditorComment;
	}

	public void setGameEditorComment(String gameEditorComment) {
		this.gameEditorComment = gameEditorComment;
	}

	public String getGameUpdatetimeNick() {
		return gameUpdatetimeNick;
	}

	public void setGameUpdatetimeNick(String gameUpdatetimeNick) {
		this.gameUpdatetimeNick = gameUpdatetimeNick;
	}

	public String getGameDownloadCountNick() {
		return gameDownloadCountNick;
	}

	public void setGameDownloadCountNick(String gameDownloadCountNick) {
		this.gameDownloadCountNick = gameDownloadCountNick;
	}

	public String getGameClass() {
		return gameClass;
	}

	public void setGameClass(String gameClass) {
		this.gameClass = gameClass;
	}

	public String getGameSecurityTags() {
		return gameSecurityTags;
	}

	public void setGameSecurityTags(String gameSecurityTags) {
		this.gameSecurityTags = gameSecurityTags;
	}

	public String getGameSecurityDetail() {
		return gameSecurityDetail;
	}

	public void setGameSecurityDetail(String gameSecurityDetail) {
		this.gameSecurityDetail = gameSecurityDetail;
	}

	public String getGameBbsUrl() {
		return gameBbsUrl;
	}

	public void setGameBbsUrl(String gameBbsUrl) {
		this.gameBbsUrl = gameBbsUrl;
	}

	public String getGameNewsUrl() {
		return gameNewsUrl;
	}

	public void setGameNewsUrl(String gameNewsUrl) {
		this.gameNewsUrl = gameNewsUrl;
	}

	public String getGameTacticsUrl() {
		return gameTacticsUrl;
	}

	public void setGameTacticsUrl(String gameTacticsUrl) {
		this.gameTacticsUrl = gameTacticsUrl;
	}

	public Integer getGameHashcode() {
		return gameHashcode;
	}

	public void setGameHashcode(Integer gameHashcode) {
		this.gameHashcode = gameHashcode;
	}

	public Long getGameNewsUpdatetime() {
		return gameNewsUpdatetime;
	}

	public void setGameNewsUpdatetime(Long gameNewsUpdatetime) {
		this.gameNewsUpdatetime = gameNewsUpdatetime;
	}

	public String getGameServiceTag() {
		return gameServiceTag;
	}

	public void setGameServiceTag(String gameServiceTag) {
		this.gameServiceTag = gameServiceTag;
	}

}
