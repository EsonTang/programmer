package com.prize.runoldtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.UsbService;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Broadcast extends	BroadcastReceiver{
	/** 存储的文件名 */  
   // public static final String DATABASE = "Database";  
	public static boolean ActivityisFinish=true;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	//	SharedPreferences sharepreference = context.getSharedPreferences(DATABASE,  
       //         Activity.MODE_PRIVATE);
      //  Editor editor = sharepreference.edit();
      //  editor.putString("testenable", "false");  
      //  editor.commit();
        
        DataUtil.destoryActivity();
       /* ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        Method method;
		try {
			method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
			method.invoke(mActivityManager, "com.prize.runoldtest.Test3DActivity");  //packageName是需要强制停止的应用程序包名  
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  */
        if(!ActivityisFinish){
        	Intent  show = new Intent(context, UsbService.class);
            show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
            context.startService(show);
        }
        
	}
	

    public  static void SteTag(boolean isfinish){
    	ActivityisFinish=isfinish;
    }
 
   
}
