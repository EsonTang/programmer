package com.android.prizefloatwindow.config;

import com.android.prizefloatwindow.R;
import com.android.prizefloatwindow.application.PrizeFloatApp;

import android.util.TypedValue;

public class Config {

	public static final String FLOAT_VIEW_LAND_X="float_view_land_x";
    public static final String FLOAT_VIEW_LAND_Y="float_view_land_Y";
    public static final String FLOAT_VIEW_PORT_X="float_view_port_x";
    public static final String FLOAT_VIEW_PORT_Y="float_view_port_y";
    public static final String FLOAT_MODE="float_mode";
    public static final String FLOAT_AUTOHIDE="float_autohide";
    //quick
    public static final String FLOAT_SINGLE="float_single";
    public static final String FLOAT_DOUBLE="float_double";
    public static final String FLOAT_LONG="float_long";
    //menu
    public static final String FLOAT_MENU1="float_menu1";
    public static final String FLOAT_MENU2="float_menu2";
    public static final String FLOAT_MENU3="float_menu3";
    public static final String FLOAT_MENU4="float_menu4";
    public static final String FLOAT_MENU5="float_menu5";
    
    
    
    
    
    
    //floatwidow  config
    public static final int DEFAULT_MAX_LENGTH = dp2px(250);//270//arclayout size
    public static final int DEFAULT_MIN_LENGTH = dp2px(50);//75//floatimg  size
    public static final int DEFAULT_MIN_HIDE = dp2px(40);//
    public static final float DEFAULT_ALPHA = 0.7f;
    
    //floatbutton
    public static final int DEFAULT_HIDE_DISTANCE = 50;
    public static final int DEFAULT_AUTOHIDE_TIME = 1000;
    public static final int DEFAULT_HIDETOEDGE_TIME = 2000;
    public static final boolean default_mode_menu = false;
    public static final boolean default_autohide = true;
    
    //quickmodedefault
    public static final String default_single_action="back";
    public static final String default_double_action="home";
    public static final String default_long_action="lockscreen";
    
    //menudefaultstr
    public static final String default_menu1_action="home";
    public static final String default_menu2_action="control";
    public static final String default_menu3_action="recent";
    public static final String default_menu4_action="xiaoku";
    public static final String default_menu5_action="float_settings";
    
    //quickmode  actionlist
    
    public static final String action_scan_wx="scan_wx";
    public static final String action_scan_alipay="scan_alipay";
    public static final String action_paycode_wx="paycode_wx";
    public static final String action_paycode_alipay="paycode_alipay";
    
    public static final String action_nothing="nothing";
    public static final String action_lockcscreen="lockscreen";
    public static final String action_back="back";
    public static final String action_home="home";
    public static final String action_recent="recent";
    public static final String action_control="control";
    
    public static final String action_screenshot="screenshot";
    public static final String action_xiaoku="xiaoku";
    public static final String action_huyan="huyan";
    public static final String action_divide_screen="divide_screen";
    public static final String action_gamemode="gamemode";
    public static final String action_clean="clean";
    public static final String action_float_settings="float_settings";
    public static final String action_application="application";
    

    
    public static int dp2px(float dp){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, PrizeFloatApp.getInstance().getApplicationContext().getResources().getDisplayMetrics());
    }

    public static float px2dp(float px){
        return px/ PrizeFloatApp.getInstance().getResources().getDisplayMetrics().density;
    }

    public static float sp2px(float sp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, PrizeFloatApp.getInstance().getApplicationContext().getResources().getDisplayMetrics());
    } 
}

