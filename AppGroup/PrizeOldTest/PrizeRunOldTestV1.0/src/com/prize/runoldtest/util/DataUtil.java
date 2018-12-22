package com.prize.runoldtest.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DataUtil {
	private static Map<String, Activity> destoryMap = new HashMap<String, Activity>();
	private static Map<String, Activity> BackPressdestoryActvity = new HashMap<String, Activity>();
	public static boolean FlagCpu =false;
	public static boolean FlagLcd = false;
	public static boolean Flag3D = false;
	public static boolean FlagEmmc = false;
	public static boolean FlagSr = false;
	public static boolean FlagVideo = false;
	public static boolean FlagCamera = false;
	public static boolean FlagReboot = false;
	public static boolean FlagDdr = true;
	public static boolean FlagRebootFinish=false;
	public static boolean FlagtlfDdrFinalFinish=false;
	public static boolean FlagsixDdrFinalFinish=false;
	public static boolean FlagfourDdrFinalFinish=false;
	
	public static boolean isTwlfTest=false;
	public static boolean isSixTest=false;
	public static boolean isfourTest=false;
	public static boolean isManualTst=false;
	public static boolean ManualTestFinish=false;
	

	/**
	 * Stable Test Flag List
	 * */
	public static boolean FlagLcd_stable = false;
	public static boolean FlagLight_stable = false;
	public static boolean FlagVibrate_stable = false;
	public static boolean FlagCamera_stable = false;
	public static boolean FlagEmmc_stable = false;
	public static boolean FlagALL_stable = false;
	private static Context mcontext;
	
	public static final int AUTOTESTACTIVITY=1;
	public static final int SIXHOURSTESTACTIVIT=2;
	public static final int TELVEHOURSTESTACTIVITY=3;
	public static final int SINGLEACTIVITY=4;
	public static final int FOURTESTACTIVITY=5;

/*	public DataUtil(Context context){
		mcontext=context;
	}*/
	/**
	 * 添加到销毁队列
	 * 
	 * @param activity
	 *            要销毁的activity
	 */

	public static void addDestoryActivity(Activity activity, String activityName) {
		destoryMap.put(activityName, activity);
	}

	/**
	 * 销毁指定Activity
	 */
	public static void destoryActivity() {
		Set<String> keySet = destoryMap.keySet();
		for (String key : keySet) {
			destoryMap.get(key).finish();
		}
	}
	
	
	
	
	
	public static void  addBackPressActivity(Activity activity, String activityName){
		BackPressdestoryActvity.put(activityName, activity);
	}
	
	public static void  finishBackPressActivity(){
		Set<String> keySet = BackPressdestoryActvity.keySet();
	    for (String key : keySet) {
	    	BackPressdestoryActvity.get(key).finish();
		}
	}
	public static void resetFlag() {
		FlagLcd = false;
		Flag3D = false;
		FlagEmmc = false;
		FlagSr = false;
		FlagVideo = false;
		FlagCamera = false;
		FlagReboot = false;
		FlagDdr = false;
		FlagRebootFinish=false;

		FlagLcd_stable = false;
		FlagLight_stable = false;
		FlagVibrate_stable = false;
		FlagCamera_stable = false;
		FlagEmmc_stable = false;
	}
	/*//保存开启重启应用之前的activity，这样重启之后获得这个activity再重新开启
	public  void savePreRebootActivity(String Activityname){
		
		SharedPreferences sharedPreferences =mcontext. getSharedPreferences("activityname", Context.MODE_PRIVATE); //私有数据
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putString("activityname", Activityname);		
		editor.commit();//提交修改			
	}
	*/
}
