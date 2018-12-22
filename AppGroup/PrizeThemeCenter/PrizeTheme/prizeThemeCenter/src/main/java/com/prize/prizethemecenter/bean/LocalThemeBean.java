package com.prize.prizethemecenter.bean;

/**
 * Created by Fanghui on 2016/11/18.
 * 本地主题bean
 */
public class LocalThemeBean {

    private String themeId;
    private String name;
    private String iconPath;
    private String themePath;
    private String isSelected;

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String pThemeId) {
        themeId = pThemeId;
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

    public String getThemePath() {
        return themePath;
    }

    public void setThemePath(String pThemePath) {
        themePath = pThemePath;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String pIsSelected) {
        isSelected = pIsSelected;
    }
}
