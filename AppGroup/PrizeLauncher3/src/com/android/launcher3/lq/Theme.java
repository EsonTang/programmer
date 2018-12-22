package com.android.launcher3.lq;

import java.io.Serializable;
import java.util.List;

import android.text.TextUtils;

/**
 * 主题
 * @author changxiaofei
 * @time 2013-11-14 下午5:30:46
 */
public class Theme implements Serializable {
	private static final long serialVersionUID = 7741080483467466968L;
	/**
     * strId
     */
    private String strId;
    /**
     * 功能模块来源编号：0：store列表；1：banner；2每日推荐
     */
    private int intSourceType;
    /**
     * 名称
     */
    private String strName;
    /**
     * 作者
     */
    private String strAuthor;
    /**
     * 版本号
     */
    private String strVersion;
    /**
     * 来源
     */
    private String strSource;
    /**
     * 大小
     */
    private long longSize;
    /**
     * 下载数
     */
    private long longDownloadCount;
    /**
     * 主题图标下载url
     */
    private String strIconUrl;
    /**
     * 主题预览图下载url
     */
    private List<String> arrPreviewUrl;
    /**
     * 主题下载url
     */
    private String strThemeUrl;
    /**
     * 主题图标本地文件路径
     */
    private String strIconPath;
    /**
     * 主题预览图本地文件路径
     */
    private List<String> arrPreviewPath;
    /**
     * 主题本地文件路径
     */
    private String strThemePath;
    /**
     * 服务器端的更新时间
     */
    private long longUpdateTime;
    /**
     * 本地缓存时间
     */
    private long longLocalTime;
    /**
     * 每日推荐icon
     */
    private String dailyIcon;

    private int consumePoints;
    private int pointsflag;

    private String bannerUrl;

    private String packName;

    private String desc;

    private String desktopIcon;

    private String textColor;
    
    private int clickActionType;

    private String tid;
    /** 0:是预制鼎智主题, 1:非预制鼎智主题 **/
    private int withinSystem = 0;

	/**
     * strThemeType #主题类型：0-通用主题; 1-滤镜主题apk格式; 2-Go主题; 3-滤镜主题zip格式 ; 100-中兴主题 
     */
    private int themeVersion;
    
    // BEGIN add by ouyangjin at 2016-1-18 上午11:03:34 for 选中主题标示
    private int isSelected ;//是否是当前主题
    // END add by ouyangjin at 2016-1-18 上午11:03:34 for 选中主题标示
    
    

	public Theme(){
    }

    
    public int getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(int isSelected) {
		this.isSelected = isSelected;
	}

	public String getStrId() {
        return strId;
    }

    public void setStrId(String strId) {
        this.strId = strId;
    }

    
    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public String getStrAuthor() {
        return strAuthor;
    }

    public void setStrAuthor(String strAuthor) {
        this.strAuthor = strAuthor;
    }

    public String getStrVersion() {
        return strVersion;
    }

    public void setStrVersion(String strVersion) {
        this.strVersion = strVersion;
    }

    public String getStrSource() {
        return strSource;
    }

    public void setStrSource(String strSource) {
        this.strSource = strSource;
    }

    public long getLongSize() {
        return longSize;
    }

    public void setLongSize(long longSize) {
        this.longSize = longSize;
    }

    public String getStrIconUrl() {
        return strIconUrl;
    }

    public void setStrIconUrl(String strIconUrl) {
        this.strIconUrl = strIconUrl;
    }

    public List<String> getArrPreviewUrl() {
        return arrPreviewUrl;
    }

    public void setArrPreviewUrl(List<String> arrPreviewUrl) {
        this.arrPreviewUrl = arrPreviewUrl;
    }

    public String getStrThemeUrl() {
        return strThemeUrl;
    }

    public void setStrThemeUrl(String strThemeUrl) {
        this.strThemeUrl = strThemeUrl;
    }

    public long getLongUpdateTime() {
        return longUpdateTime;
    }

    public void setLongUpdateTime(long longUpdateTime) {
        this.longUpdateTime = longUpdateTime;
    }

    public long getLongLocalTime() {
        return longLocalTime;
    }

    public void setLongLocalTime(long longLocalTime) {
        this.longLocalTime = longLocalTime;
    }

    public long getLongDownloadCount() {
        return longDownloadCount;
    }

    public void setLongDownloadCount(long longDownloadCount) {
        this.longDownloadCount = longDownloadCount;
    }

    public String getStrIconPath() {
        return strIconPath;
    }

    public void setStrIconPath(String strIconPath) {
        this.strIconPath = strIconPath;
    }

    public List<String> getArrPreviewPath() {
        return arrPreviewPath;
    }

    public void setArrPreviewPath(List<String> arrPreviewPath) {
        this.arrPreviewPath = arrPreviewPath;
    }

    public String getStrThemePath() {
        return strThemePath;
    }

    public void setStrThemePath(String strThemePath) {
        this.strThemePath = strThemePath;
    }

    public int getIntSourceType() {
        return intSourceType;
    }

    public void setIntSourceType(int intSourceType) {
        this.intSourceType = intSourceType;
    }

    public String getDailyIcon() {
        return dailyIcon;
    }

    public void setDailyIcon(String dailyIcon) {
        this.dailyIcon = dailyIcon;
    }

    public int getConsumePoints() {
        return consumePoints;
    }

    public void setConsumePoints(int consumePoints) {
        this.consumePoints = consumePoints;
    }

    public int getPointsflag() {
        return pointsflag;
    }

    public void setPointsflag(int pointsflag) {
        this.pointsflag = pointsflag;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesktopIcon() {
        return desktopIcon;
    }

    public void setDesktopIcon(String desktopIcon) {
        this.desktopIcon = desktopIcon;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }
    
    public int getClickActionType() {
        return clickActionType;
    }

    public void setClickActionType(int clickActionType) {
        this.clickActionType = clickActionType;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getResIdAndTid(){
        if(TextUtils.isEmpty(tid)){
            return strId;
        }

        return strId + "#" + tid;
    }

    /** 0:是预制主题, 1:非预制主题 **/
    public int getWithinSystem() {
		return withinSystem;
	}

	public void setWithinSystem(int withinSystem) {
		this.withinSystem = withinSystem;
	}
	 
    public int getThemeVersion() {
		return themeVersion;
	}

	public void setThemeVersion(int themeVersion) {
		this.themeVersion = themeVersion;
	}
}