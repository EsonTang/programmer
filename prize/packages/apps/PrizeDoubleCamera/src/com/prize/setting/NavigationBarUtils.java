package com.prize.setting;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;

public class NavigationBarUtils {
	protected static String TAG = "NavigationBarUtils";
	public static String PRIZE_NAVBAR_STATE = Settings.System.PRIZE_NAVBAR_STATE;
	public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }

        return hasNavigationBar;

    }
	//PRIZE-modify-fix nav observer-20170302-pengcancan-start
	public static boolean isShowNavigationBar(Context context) {
		boolean show = false;
		Resources rs = context.getResources();
		int postion = 0;
		try {
			postion = Settings.System.getInt(context.getContentResolver(), PRIZE_NAVBAR_STATE);
		} catch (SettingNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if (postion != 0) {
			show = true;
		}
		return show;
	}
	//PRIZE-modify-fix nav observer-20170302-pengcancan-end
    
    public static int getNavigationBarHeight(Context context) {
       /* DisplayMetrics metric = new DisplayMetrics();  
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metric);
    	int realScreenheight = metric.heightPixels;
    	((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
    	int navScreenheight = metric.heightPixels;
        return realScreenheight-navScreenheight;*/
    	 Resources res = context.getResources();
         int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
         if (resourceId > 0) {
             return res.getDimensionPixelSize(resourceId);
         }
         return 0;
    }
    
    public static void adpaterNavigationBar(Context context, View v){
    	if(v != null && checkDeviceHasNavigationBar(context)){
    		try{
    			
    			RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
    			LogTools.d(TAG, "lp.bottomMargin:"+lp.bottomMargin+","+v);
    			if(lp.bottomMargin != getNavigationBarHeight(context) * 5/8){
    				lp.bottomMargin = getNavigationBarHeight(context) * 5/8;
            		LogTools.d(TAG, "lp.bottomMargin:"+lp.bottomMargin+","+v);
            		v.setLayoutParams( lp);
    			}
    		}catch(Exception e){
    			try{
	    			FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) v.getLayoutParams();
	        		if(lp.bottomMargin != getNavigationBarHeight(context) * 5/8){
	        			lp.bottomMargin = getNavigationBarHeight(context) * 5/8;
		        		LogTools.d(TAG, "FrameLayout lp.bottomMargin:"+lp.bottomMargin+","+v);
		        		v.setLayoutParams( lp);
	        		}
    			}catch(Exception e1){
    				LogTools.d(TAG, "unable layoutParams:"+v);
    			}
    		}
    	}
    	
    }
    
	/*prize bug 14674 Camera settings select the interface, the virtual key to display the white can not see wanzhijuan 2016-4-28 start*/
    public static void checkNavigationBar(Context context, View v, int orientation){
    	LogTools.d(TAG, "checkNavigationBar orientation=" + orientation);
    	checkNavigationBar(context, v, isShowNavigationBar(context));
    }
	/*prize bug 14674 Camera settings select the interface, the virtual key to display the white can not see wanzhijuan 2016-4-28 end*/
    
    public static void checkNavigationBar(Context context, View v, boolean isShow){
    	LogTools.d(TAG, "checkNavigationBar isShow=" + isShow);
    	if(v != null && checkDeviceHasNavigationBar(context)){
    		/*if (orientation == 270) {
//    			v.setPadding(0, 0, getNavigationBarHeight(context), 0); //right
    			v.setPadding(0, 0, 0, 0); //right
    		} else if (orientation == 180) {
    			v.setPadding(0, getNavigationBarHeight(context), 0, 0); // top
    		} else if (orientation == 90) {
//    			v.setPadding(getNavigationBarHeight(context), 0, 0, 0); // left
    			v.setPadding(0, 0, 0, 0); // left
    		} else*/ {
    			v.setPadding(0, 0, 0, isShow ? getNavigationBarHeight(context) : 0); // bottom
    		}
    	}
    }
    
    public static void adpaterNavigationBar(Context context, View v, int bottomMargin){
    	if(v != null && checkDeviceHasNavigationBar(context)){
    		try{
    			
    			RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
        		if(lp.bottomMargin != bottomMargin){
        			lp.bottomMargin = bottomMargin;
            		LogTools.d(TAG, "lp.bottomMargin:"+lp.bottomMargin);
            		v.setLayoutParams( lp);
        		}
    			
    		}catch(Exception e){
    			try {
	    			FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) v.getLayoutParams();
	        		if(lp.bottomMargin != bottomMargin){
	        			lp.bottomMargin = bottomMargin;
		        		LogTools.d(TAG, "FrameLayout lp.bottomMargin:"+lp.bottomMargin+","+v);
		        		v.setLayoutParams( lp);
	        		}
    			} catch(Exception e1){
    				LogTools.d(TAG, "unable layoutParams:"+v);
    			}
    		}
    	}
    }
}
