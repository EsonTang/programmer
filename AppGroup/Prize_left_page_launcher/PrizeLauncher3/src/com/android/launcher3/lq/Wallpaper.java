package com.android.launcher3.lq;

import java.io.Serializable;

import android.text.TextUtils;

/**
 * 壁纸
 * @author changxiaofei
 * @time 2013-11-14 下午5:30:46
 */
public class Wallpaper implements Serializable {
	private static final long serialVersionUID = -6275700529807318914L;
	/** strId */
	private String strId;
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	private int intSourceType;
	/** 0静态壁纸；1动态壁纸 */
	private int intType;
	/** 作者 */
	private String strAuthor;
	/** 名称。服务器没有该字段，暂时为空 */
	private String strName;
	/** 版本 */
	private String strVersion;
	/** 来源 */
	private String strSource;
	/** 大小 */
	private long longSize;
	/** 下载数 */
	private long longDownloadCount;
	/** 图标地址url */
	private String strIconUrl;
	/** 壁纸下载url */
	private String strWallpaperUrl;
	/** 图标地址本地文件路径 */
	private String strIconPath;
	/** 壁纸本地文件路径 */
	private String strWallpaperPath;
    /** 壁纸预览图url */
    private String previewPicture;
    /** 壁纸预览图path */
    private String previewPicturePath;
	/** 服务器端的更新时间 */
	private long longUpdateTime;
	/** 本地缓存时间 */
	private long longLocalTime;
    /**
     * 每日推荐icon
     */
    private String dailyIcon;

	private String tid;
	//是不是内置壁纸
	private int isSystemDefault = 0;
	 // BEGIN add by ouyangjin at 2016-1-18 上午11:03:34 for 选中主题标示
    private int isSelected ;//是否是当前主题
    // END add by ouyangjin at 2016-1-18 上午11:03:34 for 选中主题标示
    public Wallpaper(){
    	super();
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
	
	public int getIntType() {
		return intType;
	}
	
	public void setIntType(int intType) {
		this.intType = intType;
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
	
	public String getStrWallpaperUrl() {
		return strWallpaperUrl;
	}
	
	public void setStrWallpaperUrl(String strWallpaperUrl) {
		this.strWallpaperUrl = strWallpaperUrl;
	}
	
	public long getLongDownloadCount() {
		return longDownloadCount;
	}
	
	public void setLongDownloadCount(long longDownloadCount) {
		this.longDownloadCount = longDownloadCount;
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

	public String getStrIconPath() {
		return strIconPath;
	}

	public void setStrIconPath(String strIconPath) {
		this.strIconPath = strIconPath;
	}

	public String getStrWallpaperPath() {
		return strWallpaperPath;
	}

	public void setStrWallpaperPath(String strWallpaperPath) {
		this.strWallpaperPath = strWallpaperPath;
	}

	public String getStrName() {
		return strName;
	}

	public void setStrName(String strName) {
		this.strName = strName;
	}

	public int getIntSourceType() {
		return intSourceType;
	}

	public void setIntSourceType(int intSourceType) {
		this.intSourceType = intSourceType;
	}

    public String getPreviewPicture() {
        return previewPicture;
    }

    public void setPreviewPicture(String previewPicture) {
        this.previewPicture = previewPicture;
    }

    public String getPreviewPicturePath() {
        return previewPicturePath;
    }

    public void setPreviewPicturePath(String previewPicturePath) {
        this.previewPicturePath = previewPicturePath;
    }

    public String getDailyIcon() {
        return dailyIcon;
    }

    public void setDailyIcon(String dailyIcon) {
        this.dailyIcon = dailyIcon;
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
	
	public int getIsSystemDefault() {
		return isSystemDefault;
	}

	public void setIsSystemDefault(int isSystemDefault) {
		this.isSystemDefault = isSystemDefault;
	}


}
