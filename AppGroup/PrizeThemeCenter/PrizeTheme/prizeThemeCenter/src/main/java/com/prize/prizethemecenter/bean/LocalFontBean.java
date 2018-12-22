package com.prize.prizethemecenter.bean;

import java.io.Serializable;

/**
 * Created by Fanghui on 2016/11/18.
 * 本地字体bean
 */
public class LocalFontBean implements Serializable{

    public String fontId;
    public String name;
    public String iconPath;
    public String fontPath;
    public boolean isSelected;

    public String getFontId() {
        return fontId;
    }

    public void setFontId(String pFontId) {
        fontId = pFontId;
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

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String pFontPath) {
        fontPath = pFontPath;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean pIsSelected) {
        isSelected = pIsSelected;
    }
}
