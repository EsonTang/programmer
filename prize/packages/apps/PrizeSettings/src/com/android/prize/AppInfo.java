
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：huangdianjun
 *完成日期：
 *修改记录：
 *修改日期：20151102
 *版 本 号：
 *修 改 人：
 *修改内容:notification_centre data
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.android.prize;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo {  
    
    private String appLabel;    
    private Drawable appIcon ;  
    private Intent intent ;     
    private String pkgName ;
    private boolean floatBln;
	public boolean isFloatBln() {
		return floatBln;
	}

	public void setFloatBln(boolean floatBln) {
		this.floatBln = floatBln;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public AppInfo(){}  
      
    public String getAppLabel() {  
        return appLabel;  
    }  
    public void setAppLabel(String appName) {  
        this.appLabel = appName;  
    }  
    public Drawable getAppIcon() {  
        return appIcon;  
    }  
    public void setAppIcon(Drawable appIcon) {  
        this.appIcon = appIcon;  
    }  
    public Intent getIntent() {  
        return intent;  
    }  
    public void setIntent(Intent intent) {  
        this.intent = intent;  
    }  

}  

