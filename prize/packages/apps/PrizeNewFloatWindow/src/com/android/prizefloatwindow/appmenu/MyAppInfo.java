package com.android.prizefloatwindow.appmenu;

import android.graphics.drawable.Drawable;

public class MyAppInfo {
    private Drawable image;
    private String appName;
    private String pkgName;
    private boolean isSelect;
    private boolean isCurAction;

    public MyAppInfo(Drawable image, String appName,String pkgName) {
        this.image = image;
        this.appName = appName;
        this.pkgName = pkgName;
    }
    public MyAppInfo() {

    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public boolean isCurAction() {
		return isCurAction;
	}
	public void setCurAction(boolean isCurAction) {
		this.isCurAction = isCurAction;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String appName) {
        this.pkgName = appName;
    }
}

