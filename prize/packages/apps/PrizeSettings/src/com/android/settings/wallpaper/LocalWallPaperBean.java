package com.android.settings.wallpaper;

import java.io.Serializable;

public class LocalWallPaperBean implements Serializable{

	private String wallpaperId;
    private String name;
    private String iconPath;
    private String wallpaperPath;
    private String isSelected;

    public String getWallpaperId() {
        return wallpaperId;
    }

    public void setWallpaperId(String pWallpaperId) {
        wallpaperId = pWallpaperId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String pIconPath) {
        iconPath = pIconPath;
    }

    public String getWallpaperPath() {
        return wallpaperPath;
    }

    public void setWallpaperPath(String pWallpaperPath) {
        wallpaperPath = pWallpaperPath;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String pIsSelected) {
        isSelected = pIsSelected;
    }
	
}
