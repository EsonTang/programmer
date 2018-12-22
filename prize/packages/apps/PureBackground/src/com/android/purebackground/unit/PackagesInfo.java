package com.android.purebackground.unit;

import android.graphics.drawable.Drawable;

public class PackagesInfo {	
  
    private Drawable appIicon;    
  
    private String appName;  

    private String packageName;

    public void setAppIcon(Drawable icon) {  
        this.appIicon = icon;  
    }  
    
    public Drawable getAppIcon() {  
        return appIicon;  
    }  

    public void setAppName(String name) {  
        this.appName = name;  
    } 
    
    public String getAppName() {  
        return appName;  
    }  
    
    public void setPackageName(String name) {  
        this.packageName = name;  
    } 
    
    public String getPackageName() {  
        return packageName;  
    } 
}
